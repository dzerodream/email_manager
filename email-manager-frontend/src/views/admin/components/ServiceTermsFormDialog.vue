<template>
  <el-dialog
    v-model="dialogVisibleComputed"
    :title="dialogTitle"
    width="700px" 
    @close="handleCloseDialog"
    :close-on-click-modal="false"
    draggable
    append-to-body
    destroy-on-close 
  >
    <el-form
      ref="termsFormRef"
      :model="termsForm"
      :rules="termsRules"
      label-width="100px"
      v-loading="formLoading"
      label-position="right"
    >
      <el-form-item label="条款标题" prop="title">
        <el-input v-model="termsForm.title" placeholder="例如：邮件管理系统服务条款 V3.0" />
      </el-form-item>
      
      <el-form-item label="条款内容" prop="content">
        <el-input 
          type="textarea" 
          v-model="termsForm.content" 
          placeholder="请输入条款详细内容 (支持HTML标签)" 
          :rows="15"
          show-word-limit
          maxlength="10000" 
        />
      </el-form-item>
      
      <!-- 编辑模式下才允许修改状态和生效日期 -->
      <template v-if="mode === 'edit'">
        <el-form-item label="发布状态" prop="status">
          <el-select v-model="termsForm.status" placeholder="请选择状态" style="width:100%;">
            <el-option label="草稿 (不对用户可见)" :value="0" />
            <el-option label="设为生效 (将替换当前已生效版本)" :value="1" />
            <el-option label="设为过期 (手动标记)" :value="2" />
          </el-select>
          <div class="el-form-item__description" style="font-size: 12px; color: #909399; line-height: 1.5;">
            注意：选择“设为生效”会触发后端逻辑，使当前其他已生效版本变为“已过期”。
          </div>
        </el-form-item>

        <el-form-item label="生效日期" prop="effectiveDate">
          <el-date-picker
            v-model="termsForm.effectiveDate"
            type="datetime"
            placeholder="选择生效日期和时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%;"
            :disabled="termsForm.status === 1"
          />
           <div class="el-form-item__description" style="font-size: 12px; color: #909399; line-height: 1.5;">
            如果是“设为生效”，此日期通常由后端设为当前时间或发布时确定；编辑“草稿”或“过期”时可修改。
          </div>
        </el-form-item>
      </template>
      <!-- 新增模式（即发布新版本），生效日期和状态由后端 adminPublishServiceTermsApi 自动处理 -->

    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleCloseDialog">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">
          {{ mode === 'add' ? '立即发布新版本' : '保存修改' }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, computed, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import { adminPublishServiceTermsApi, adminUpdateServiceTermsApi, adminUpdateServiceTermStatusApi } from '@/api/admin.js';

const props = defineProps({
  visible: { type: Boolean, default: false },
  mode: { type: String, default: 'add', validator: (v) => ['add', 'edit'].includes(v) },
  initialData: { type: Object, default: () => null },
});

const emit = defineEmits(['update:visible', 'success']);

const dialogVisibleComputed = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value),
});

const dialogTitle = computed(() => props.mode === 'add' ? '发布新版本服务条款' : '编辑服务条款');

const termsFormRef = ref(null);
const formLoading = ref(false); // 用于加载初始数据时的loading
const submitLoading = ref(false);

const createEmptyForm = () => ({
  id: null,
  title: '',
  content: '',
  status: 0, // 编辑时用，新增(发布新版)时后端会自动设为1(生效)
  effectiveDate: null,
  // version 由后端自动生成或处理
});

const termsForm = reactive(createEmptyForm());

const termsRules = reactive({
  title: [{ required: true, message: '条款标题不能为空', trigger: 'blur' }],
  content: [{ required: true, message: '条款内容不能为空', trigger: 'blur' }],
  status: [{ required: computed(() => props.mode === 'edit'), message: '请选择发布状态', trigger: 'change' }],
  effectiveDate: [{ required: computed(() => props.mode === 'edit' && termsForm.status !== 1), message: '请选择生效日期', trigger: 'change' }],
});

watch([() => props.initialData, () => props.mode], ([newData, newMode]) => {
  formLoading.value = true;
  // 先重置表单为初始空状态
  Object.assign(termsForm, createEmptyForm());
  if (newMode === 'edit' && newData) {
    // 然后用传入的初始数据填充
    termsForm.id = newData.id;
    termsForm.title = newData.title;
    termsForm.content = newData.content;
    termsForm.status = newData.status;
    termsForm.effectiveDate = newData.effectiveDate;
    // version 由后端管理，不在表单中编辑
  }
  // 等待DOM更新后清除校验状态
  nextTick(() => {
    if (termsFormRef.value) {
      termsFormRef.value.clearValidate();
    }
  });
  formLoading.value = false;
}, { immediate: true, deep: true });


async function submitForm() {
  if (!termsFormRef.value) return;
  await termsFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true;
      try {
        const dataToSubmit = {
          title: termsForm.title,
          content: termsForm.content,
        };

        if (props.mode === 'add') {
          // 新增时，调用发布新版本的API，后端会自动处理版本号和状态
          await adminPublishServiceTermsApi(dataToSubmit);
          ElMessage.success('新版本服务条款发布成功！');
        } else { // 编辑模式
          // 编辑时，我们允许修改status和effectiveDate
          dataToSubmit.status = termsForm.status;
          dataToSubmit.effectiveDate = termsForm.effectiveDate;
          
          // 如果管理员在编辑时，将状态改为了“生效”（status: 1）
          // 我们需要调用专门的更新状态接口，或者后端 updateServiceTermsApi 应该能处理这种情况
          // 为了简单，如果 status 变为1，也调用 adminUpdateServiceTermsApi，
          // 后端 updateServiceTerms 方法需要有能力处理将旧的生效版本设为过期的逻辑，或者我们分开调用
          
          // 方案1：如果 adminUpdateServiceTermsApi 后端会处理状态变更导致的旧版本过期
          await adminUpdateServiceTermsApi(termsForm.id, dataToSubmit);

          // 方案2：更明确的调用（如果后端 update 不处理旧版本过期）
          // await adminUpdateServiceTermsApi(termsForm.id, { title: dataToSubmit.title, content: dataToSubmit.content, effectiveDate: dataToSubmit.effectiveDate });
          // if (termsForm.status === 1) { // 如果是想设为生效
          //   await adminUpdateServiceTermStatusApi(termsForm.id, 1);
          // } else if (termsForm.status === 0 || termsForm.status === 2) { // 如果是设为草稿或过期
          //   await adminUpdateServiceTermStatusApi(termsForm.id, termsForm.status);
          // }
          
          ElMessage.success('服务条款更新成功！');
        }
        emit('success'); // 通知父组件操作成功
        handleCloseDialog(false); // 关闭对话框，但不触发父组件的@close（因为我们已经emit了success）
      } catch (error) {
        console.error('服务条款表单提交失败:', error);
        // 错误信息已由axios拦截器处理
      } finally {
        submitLoading.value = false;
      }
    }
  });
}

function handleCloseDialog(emitUpdate = true) {
  Object.assign(termsForm, createEmptyForm());
  if (termsFormRef.value) {
    nextTick(() => {
      termsFormRef.value.clearValidate();
    });
  }
  if (emitUpdate) { // 只有用户主动关闭（如点取消或X）时才触发v-model更新
    dialogVisibleComputed.value = false;
  }
}
</script>

<style scoped>
.el-form-item__description {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  margin-top: 4px;
}
</style>