<template>
    <el-dialog
      v-model="dialogVisibleComputed"
      :title="dialogTitle"
      width="700px" 
      @close="handleCloseDialog"
      :close-on-click-modal="false"
      draggable
      append-to-body
    >
      <el-form
        ref="announcementFormRef"
        :model="announcementForm"
        :rules="announcementRules"
        label-width="100px"
        v-loading="formLoading"
        label-position="right"
      >
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="announcementForm.title" placeholder="请输入公告标题" />
        </el-form-item>
        
        <el-form-item label="公告内容" prop="content">
          <!-- 为了简单，我们先用普通文本域，如果需要富文本，可以集成wangEditor -->
          <el-input 
            type="textarea" 
            v-model="announcementForm.content" 
            placeholder="请输入公告内容" 
            :rows="10"
            show-word-limit
            maxlength="2000"
          />
        </el-form-item>
        
        <el-form-item label="发布状态" prop="status">
          <el-radio-group v-model="announcementForm.status">
            <el-radio :label="1">立即发布</el-radio>
            <el-radio :label="0">存为草稿/禁用</el-radio>
          </el-radio-group>
        </el-form-item>
  
        <!-- publishTime 和 publisherId 通常由后端自动处理，前端无需填写 -->
  
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseDialog">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </template>
  
  <script setup>
  import { ref, reactive, watch, computed, nextTick } from 'vue';
  import { ElMessage } from 'element-plus';
  import { adminCreateAnnouncementApi, adminUpdateAnnouncementApi } from '@/api/admin.js';
  
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
  
  const dialogTitle = computed(() => props.mode === 'add' ? '发布新公告' : '编辑公告');
  
  const announcementFormRef = ref(null);
  const formLoading = ref(false);
  const submitLoading = ref(false);
  
  const createEmptyForm = () => ({
    id: null,
    title: '',
    content: '',
    status: 1, // 默认立即发布
    // publisherId, publishTime, createdTime, updatedTime 由后端处理
  });
  
  const announcementForm = reactive(createEmptyForm());
  
  const announcementRules = reactive({
    title: [{ required: true, message: '公告标题不能为空', trigger: 'blur' }],
    content: [{ required: true, message: '公告内容不能为空', trigger: 'blur' }],
    status: [{ required: true, message: '请选择发布状态', trigger: 'change' }],
  });
  
  watch([() => props.initialData, () => props.mode], ([newData, newMode]) => {
    formLoading.value = true;
    if (newMode === 'edit' && newData) {
      Object.assign(announcementForm, createEmptyForm(), newData);
    } else { // add mode or no initial data
      Object.assign(announcementForm, createEmptyForm());
    }
    // 清除可能存在的旧校验信息
    nextTick(() => {
      if (announcementFormRef.value) {
        announcementFormRef.value.clearValidate();
      }
    });
    formLoading.value = false;
  }, { immediate: true, deep: true });
  
  async function submitForm() {
    if (!announcementFormRef.value) return;
    await announcementFormRef.value.validate(async (valid) => {
      if (valid) {
        submitLoading.value = true;
        try {
          // 准备提交的数据，后端不需要id之外的字段作为路径参数
          const dataToSubmit = {
            title: announcementForm.title,
            content: announcementForm.content,
            status: announcementForm.status,
          };
  
          if (props.mode === 'add') {
            await adminCreateAnnouncementApi(dataToSubmit);
            ElMessage.success('公告发布成功！');
          } else {
            await adminUpdateAnnouncementApi(announcementForm.id, dataToSubmit);
            ElMessage.success('公告更新成功！');
          }
          emit('success');
          handleCloseDialog(false); // 提交成功后只关闭对话框，不触发父级的@close的reset
        } catch (error) {
          console.error('公告表单提交失败:', error);
        } finally {
          submitLoading.value = false;
        }
      }
    });
  }
  
  function handleCloseDialog(emitUpdate = true) {
    Object.assign(announcementForm, createEmptyForm());
    if (announcementFormRef.value) {
      nextTick(() => { announcementFormRef.value.clearValidate(); });
    }
    if (emitUpdate) {
      dialogVisibleComputed.value = false;
    }
  }
  </script>
  
  <style scoped>
  /* 可以根据需要添加局部样式 */
  </style>