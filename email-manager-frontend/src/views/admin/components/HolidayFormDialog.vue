<template>
  <el-dialog
    v-model="dialogVisibleComputed"
    :title="dialogTitle"
    width="600px"
    @close="handleDialogInternalClose" 
    :close-on-click-modal="false"
    draggable
    append-to-body
    destroy-on-close 
  >
    <el-form
      ref="holidayFormRef"
      :model="holidayForm"
      :rules="holidayRules"
      label-width="100px"
      v-loading="formLoading"
      label-position="right"
    >
      <el-form-item label="节日/事件名称" prop="name">
        <el-input v-model="holidayForm.name" placeholder="例如：元旦、公司年会" />
      </el-form-item>
      
      <el-form-item label="日期" prop="holidayDate">
        <el-date-picker
          v-model="holidayForm.holidayDate"
          type="date"
          placeholder="选择日期"
          value-format="YYYY-MM-DD"
          style="width: 100%;"
          :disabled="mode === 'edit'" 
        />
        <div 
          v-if="mode === 'edit'" 
          class="el-form-item__description"
        >
          提示：通常不建议修改已设定节假日的日期。如需更改日期，请删除后重新添加。
        </div>
      </el-form-item>

      <el-form-item label="类型" prop="isWorkday">
        <el-radio-group v-model="holidayForm.isWorkday">
          <el-radio :label="0">节假日/休息日</el-radio>
          <el-radio :label="1">调休工作日</el-radio>
        </el-radio-group>
      </el-form-item>
      
      <el-form-item label="描述" prop="description">
        <el-input 
          type="textarea" 
          v-model="holidayForm.description" 
          placeholder="可选，对该日期的简要描述" 
          :rows="3"
        />
      </el-form-item>

    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleUserCancel">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, computed, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import { adminAddHolidayApi, adminUpdateHolidayApi } from '@/api/admin.js';

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

const dialogTitle = computed(() => props.mode === 'add' ? '新增节假日/调休记录' : '编辑节假日/调休记录');

const holidayFormRef = ref(null);
const formLoading = ref(false); 
const submitLoading = ref(false);

const createEmptyForm = () => ({
  id: null,
  name: '',
  holidayDate: '',
  isWorkday: 0,
  description: '',
});

const holidayForm = reactive(createEmptyForm());

const holidayRules = reactive({
  name: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
  holidayDate: [{ required: true, message: '日期不能为空', trigger: 'change' }],
  isWorkday: [{ required: true, message: '请选择类型', trigger: 'change' }],
  description: [{ max: 255, message: '描述信息过长，最多255字符', trigger: 'blur' }] // 可选的描述长度校验
});

// 监听 props.visible 的变化，当对话框从不可见变为可见时，
// 并且是编辑模式且有初始数据时，填充表单。
// 新增模式或无初始数据时，确保表单是空的。
watch(() => props.visible, (newVal) => {
  if (newVal) { // 对话框打开时
    formLoading.value = true;
    // 先重置为初始空状态，确保切换模式时表单干净
    Object.assign(holidayForm, createEmptyForm()); 
    if (props.mode === 'edit' && props.initialData) {
      holidayForm.id = props.initialData.id;
      holidayForm.name = props.initialData.holidayName || props.initialData.name;
      holidayForm.holidayDate = props.initialData.holidayDate;
      holidayForm.isWorkday = props.initialData.isWorkday === undefined ? 0 : props.initialData.isWorkday;
      holidayForm.description = props.initialData.description || '';
    }
    // 清除上一次的校验信息
    nextTick(() => {
      if (holidayFormRef.value) {
        holidayFormRef.value.clearValidate();
      }
    });
    formLoading.value = false;
  }
}, { immediate: true }); // immediate 确保组件首次创建时，如果visible为true也能执行

async function submitForm() {
  if (!holidayFormRef.value) return;
  await holidayFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true;
      try {
        const dataToSubmit = {
          name: holidayForm.name,
          holidayDate: holidayForm.holidayDate,
          isWorkday: holidayForm.isWorkday,
          description: holidayForm.description,
        };
        if (props.mode === 'add') {
          await adminAddHolidayApi(dataToSubmit);
          ElMessage.success('新增记录成功！');
        } else {
          await adminUpdateHolidayApi(holidayForm.id, dataToSubmit);
          ElMessage.success('更新记录成功！');
        }
        emit('success'); // 通知父组件操作成功
        dialogVisibleComputed.value = false; // 关闭对话框
      } catch(error) {
        console.error("Holiday form submission error:", error);
      } finally {
        submitLoading.value = false;
      }
    }
  });
}

// 当用户点击“取消”按钮或右上角的“X”时调用
function handleUserCancel() {
  dialogVisibleComputed.value = false; // 直接关闭对话框
}

// el-dialog 的 @close 事件处理器
// 这个事件在对话框完全关闭动画结束后触发
function handleCloseDialog() {
  // 由于设置了 destroy-on-close, 组件销毁时其内部状态会自动重置
  // 但为了保险起见，或在没有 destroy-on-close 时，手动重置响应式数据是好习惯
  Object.assign(holidayForm, createEmptyForm());
  if (holidayFormRef.value) {
      holidayFormRef.value.clearValidate();
  }
  // 确保父组件的 visible 状态被更新（如果不是通过v-model关闭的）
  // 但由于我们用了v-model:visible，这一步通常由dialogVisibleComputed的set自动处理了
  // emit('update:visible', false); 
}
</script>

<style scoped>
.el-form-item__description {
  font-size: 12px;
  color: #909399;
  line-height: 1.2;
  margin-top: 4px;
}
</style>