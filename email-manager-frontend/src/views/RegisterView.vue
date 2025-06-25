<template>
    <div class="register-container">
      <el-card class="register-card" shadow="always">
        <template #header>
          <div class="card-header">
            <span>新用户注册</span>
          </div>
        </template>
  
        <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" label-position="top" size="large">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="registerForm.username" placeholder="请输入用户名 (用于登录)" :prefix-icon="User" clearable />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="registerForm.email" placeholder="请输入邮箱地址" :prefix-icon="Message" clearable />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input type="password" v-model="registerForm.password" placeholder="请输入密码 (6-20位字符)" :prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input type="password" v-model="registerForm.confirmPassword" placeholder="请再次输入密码" :prefix-icon="Lock" show-password />
          </el-form-item>
          <!-- 可以根据需要添加真实姓名、电话等字段 -->
          <el-form-item>
            <el-button type="primary" @click="handleRegister" style="width: 100%;" :loading="loading">立即注册</el-button>
          </el-form-item>
          <div class="footer-links">
            <el-link type="primary" @click="goToLogin">已有账号？立即登录</el-link>
          </div>
        </el-form>
      </el-card>
    </div>
  </template>
  
  <script setup>
  import { ref, reactive } from 'vue';
  import { useRouter } from 'vue-router';
  import { ElMessage } from 'element-plus';
  import { User, Lock, Message } from '@element-plus/icons-vue';
  import { registerApi } from '@/api/user.js'; // 确保这个API函数已存在
  
  const router = useRouter();
  const registerFormRef = ref(null);
  const loading = ref(false);
  
  const registerForm = reactive({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    // realName: '', // 如果需要
    // phone: '',    // 如果需要
  });
  
  // 自定义确认密码校验规则
  const validateConfirmPassword = (rule, value, callback) => {
    if (value === '') {
      callback(new Error('请再次输入密码'));
    } else if (value !== registerForm.password) {
      callback(new Error("两次输入的密码不一致!"));
    } else {
      callback();
    }
  };
  
  const registerRules = reactive({
    username: [
      { required: true, message: '请输入用户名', trigger: 'blur' },
      { min: 3, max: 20, message: '用户名长度应为 3 到 20 个字符', trigger: 'blur' },
    ],
    email: [
      { required: true, message: '请输入邮箱地址', trigger: 'blur' },
      { type: 'email', message: '请输入正确的邮箱格式', trigger: ['blur', 'change'] },
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
      { min: 6, max: 20, message: '密码长度应为 6 到 20 个字符', trigger: 'blur' },
    ],
    confirmPassword: [
      { required: true, message: '请再次输入密码', trigger: 'blur' },
      { validator: validateConfirmPassword, trigger: 'blur' },
    ],
  });
  
  async function handleRegister() {
    if (!registerFormRef.value) return;
    await registerFormRef.value.validate(async (valid) => {
      if (valid) {
        loading.value = true;
        try {
          // 准备提交给后端的数据，不包含confirmPassword
          const { confirmPassword, ...submitData } = registerForm;
          await registerApi(submitData);
          ElMessage.success('注册成功！将跳转到登录页...');
          setTimeout(() => {
            router.push({ name: 'login' });
          }, 1500); // 延迟跳转，让用户看到提示
        } catch (error) {
          // 错误消息已由axios拦截器处理 (例如：用户名已存在)
          console.error('注册失败:', error);
        } finally {
          loading.value = false;
        }
      }
    });
  }
  
  function goToLogin() {
    router.push({ name: 'login' });
  }
  </script>
  
  <style scoped>
  .register-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh; /* 使用min-height确保内容超出时可滚动 */
    padding: 20px 0; /* 上下留白，避免内容贴边 */
    background-image: url('data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="100" height="100" viewBox="0 0 100 100"%3E%3Cg fill-rule="evenodd"%3E%3Cg fill="%239C92AC" fill-opacity="0.1"%3E%3Cpath opacity=".5" d="M96 95h4v1h-4v4h-1v-4h-9v4h-1v-4h-9v4h-1v-4h-9v4h-1v-4h-9v4h-1v-4h-9v4h-1v-4h-9v4h-1v-4h-9v4h-1v-4h-9v4h-1v-4H0v-1h4v-9H0v-1h4v-9H0v-1h4v-9H0v-1h4v-9H0v-1h4v-9H0v-1h4v-9H0v-1h4v-9H0v-1h4v-9H0v-1h4V0h1v4h9V0h1v4h9V0h1v4h9V0h1v4h9V0h1v4h9V0h1v4h9V0h1v4h9V0h1v4h9V0h1v4h4v1h-4v9h4v1h-4v9h4v1h-4v9h4v1h-4v9h4v1h-4v9h4v1h-4v9h4v1h-4v9h4v1h-4v9zm-1 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-9-10h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm9-10v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-9-10h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm9-10v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-9-10h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9z"/%3E%3Cpath d="M6 5V0h1v5h9V0h1v5h9V0h1v5h9V0h1v5h9V0h1v5h9V0h1v5h9V0h1v5h9V0h1v5h9V0h1v5h4v1h-4v9h4v1h-4v9h4v1h-4v9h4v1h-4v9h4v1h-4v9h4v1h-4v9h4v1h-4v9h4v1h-4v9h4v1h-4v5h-1v-5h-9v5h-1v-5h-9v5h-1v-5h-9v5h-1v-5h-9v5h-1v-5h-9v5h-1v-5h-9v5h-1v-5H0v-1h4v-9H0v-1h4v-9H0v-1h4v-9H0v-1h4v-9H0v-1h4v-9H0v-1h4v-9H0v-1h4v-9H0v-1h4V0h1v5h9V0h1v5h9V0h1v5h9V0h1v5h9V0h1v5h9V0h1v5h9V0h1v5h9V0h1v5h9V0h1v5z"/%3E%3C/g%3E%3C/g%3E%3C/svg%3E');
  }
  .register-card {
    width: 450px; /* 可以适当调整宽度 */
    padding: 10px 20px 20px 20px; /* 调整内边距，顶部可以小一些 */
    background-color: rgba(255, 255, 255, 0.98);
    border-radius: 12px;
    box-shadow: 0 10px 30px -5px rgba(0,0,0,0.1);
  }
  .card-header {
    text-align: center;
    font-size: 24px;
    font-weight: bold;
    color: #303133;
    padding-bottom: 10px; /* 标题和表单间距 */
  }
  .footer-links {
    display: flex;
    justify-content: flex-end; /* 只有一个链接时靠右 */
    width: 100%;
    margin-top: 10px;
  }
  </style>