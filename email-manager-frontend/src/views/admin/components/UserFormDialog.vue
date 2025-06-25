<template>
  <el-dialog
    v-model="dialogVisibleComputed"
    :title="dialogTitle"
    width="600px"
    @close="handleCloseDialog"
    :close-on-click-modal="false"
    draggable
    append-to-body 
  >
    <el-form
      ref="userFormRef"
      :model="userForm"
      :rules="userRules"
      label-width="100px"
      v-loading="formLoading"
      label-position="right"
    >
      <el-form-item label="用户名" prop="username">
        <el-input 
          v-model="userForm.username" 
          placeholder="请输入用户名 (登录使用)" 
          :disabled="mode === 'edit'"
        />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input 
          v-model="userForm.email" 
          placeholder="请输入邮箱地址" 
        />
      </el-form-item>
      
      <template v-if="mode === 'add'">
        <el-form-item label="初始密码" prop="password">
          <el-input 
            type="password" 
            v-model="userForm.password" 
            placeholder="请输入初始密码 (6-20位)" 
            show-password 
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            type="password" 
            v-model="userForm.confirmPassword" 
            placeholder="请再次输入初始密码" 
            show-password 
          />
        </el-form-item>
      </template>

      <el-form-item label="真实姓名" prop="realName">
        <el-input v-model="userForm.realName" placeholder="请输入真实姓名" />
      </el-form-item>
      <el-form-item label="电话号码" prop="phone">
        <el-input v-model="userForm.phone" placeholder="请输入电话号码" />
      </el-form-item>
      
      <el-form-item label="用户角色" prop="role">
        <el-select v-model="userForm.role" placeholder="请选择用户角色" style="width: 100%;">
          <el-option label="普通用户 (USER)" value="USER" />
          <el-option label="管理员 (ADMIN)" value="ADMIN" />
        </el-select>
      </el-form-item>
      
      <el-form-item label="账户状态" prop="status">
        <el-radio-group v-model="userForm.status">
          <el-radio :label="1">启用</el-radio>
          <el-radio :label="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>

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
import { adminCreateUserApi, adminUpdateUserApi } from '@/api/admin.js';

// --- Props 定义 ---
const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  mode: {
    type: String,
    default: 'add',
    validator: (value) => ['add', 'edit'].includes(value),
  },
  initialData: {
    type: Object,
    default: () => null,
  },
});

// --- Emits 定义 ---
const emit = defineEmits(['update:visible', 'success']);

// --- 对话框显示控制 (使用 computed 实现 v-model) ---
const dialogVisibleComputed = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value),
});

const dialogTitle = computed(() => props.mode === 'add' ? '新增用户' : '编辑用户信息');

// --- 表单相关 ---
const userFormRef = ref(null);
const formLoading = ref(false);
const submitLoading = ref(false);

// 使用一个工厂函数来创建干净的表单对象，方便重置
const createEmptyForm = () => ({
  id: null,
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  realName: '',
  phone: '',
  role: 'USER',
  status: 1,
});

const userForm = reactive(createEmptyForm());

// 密码校验逻辑
const validatePass = (rule, value, callback) => {
  if (props.mode === 'add' && value === '') {
    callback(new Error('请输入初始密码'));
  } else if (props.mode === 'add' && value.length > 0 && (value.length < 6 || value.length > 20)) {
    // 只有当用户输入了内容才校验长度，避免在blur时，空值也报长度错误
    callback(new Error('密码长度应为 6 到 20 个字符'));
  } else {
    if (props.mode === 'add' && userForm.confirmPassword !== '') {
      if (userFormRef.value) { // 确保 userFormRef 存在
        userFormRef.value.validateField('confirmPassword', () => null);
      }
    }
    callback();
  }
};
const validatePass2 = (rule, value, callback) => {
  if (props.mode === 'add' && value === '') {
    callback(new Error('请再次输入密码'));
  } else if (props.mode === 'add' && value !== userForm.password) {
    callback(new Error("两次输入的密码不一致!"));
  } else {
    callback();
  }
};

const userRules = reactive({
  username: [
    { required: true, message: '用户名为必填项', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '邮箱为必填项', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] },
  ],
  password: [
    { required: computed(() => props.mode === 'add'), validator: validatePass, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: computed(() => props.mode === 'add'), validator: validatePass2, trigger: 'blur' }
  ],
  role: [{ required: true, message: '请选择用户角色', trigger: 'change' }],
  status: [{ required: true, message: '请选择账户状态', trigger: 'change' }],
  phone: [
    { pattern: /^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\d{8}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
});

// --- 数据加载与提交 ---

// 监听 initialData (用于编辑) 和 mode (切换新增/编辑时清空表单)
watch([() => props.initialData, () => props.mode], ([newData, newMode]) => {
  if (newMode === 'edit' && newData) {
    formLoading.value = true;
    Object.assign(userForm, createEmptyForm(), newData); // 先重置再赋值
    // 编辑模式下密码字段不预填，也不应该由这个表单修改
    userForm.password = '';
    userForm.confirmPassword = '';
    nextTick(() => { // 清除可能存在的校验状态
        if(userFormRef.value) userFormRef.value.clearValidate();
    });
    formLoading.value = false;
  } else if (newMode === 'add') {
    // 切换到新增模式时，或 initialData 为 null (通常是第一次打开新增)
    Object.assign(userForm, createEmptyForm());
    nextTick(() => {
        if(userFormRef.value) userFormRef.value.clearValidate();
    });
  }
}, { immediate: true, deep: true });


// 提交表单
async function submitForm() {
  if (!userFormRef.value) return;
  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true;
      try {
        // 准备提交的数据，排除 confirmPassword
        const { confirmPassword, ...dataToSubmit } = userForm;
        
        if (props.mode === 'add') {
          await adminCreateUserApi(dataToSubmit);
          ElMessage.success('用户新增成功！');
        } else {
          // 编辑模式下，后端 DTO 通常不包含 password 字段，所以不用特意删除
          // 后端 controller 会判断哪些字段可以更新
          await adminUpdateUserApi(userForm.id, dataToSubmit);
          ElMessage.success('用户信息更新成功！');
        }
        emit('success');
        handleCloseDialog();
      } catch (error) {
        console.error('用户表单提交失败:', error);
        // 错误信息已由axios拦截器处理
      } finally {
        submitLoading.value = false;
      }
    }
  });
}

// 关闭对话框时的处理
function handleCloseDialog(emitUpdate = true) {
  Object.assign(userForm, createEmptyForm()); // 使用工厂函数重置
  if (userFormRef.value) {
    // 使用 nextTick 确保在 DOM 更新后再清除校验，避免潜在问题
    nextTick(() => {
      userFormRef.value.clearValidate();
    });
  }
  if (emitUpdate) {
    dialogVisibleComputed.value = false;
  }
}
</script>

<style scoped>
/* 可以根据需要添加局部样式 */
</style>