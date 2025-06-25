<template>
  <div class="login-container">
    <el-card class="login-card" shadow="always">
      <div class="login-header">
        <img src="/logo.svg" alt="Logo" class="login-logo" />
        <h1 class="login-title">邮件管理系统</h1>
      </div>

      <el-form 
        ref="loginFormRef" 
        :model="loginForm" 
        :rules="loginRules"
        label-position="top"
        size="large"
      >
        <el-form-item prop="username">
          <el-input 
            v-model="loginForm.username"
            placeholder="用户名"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input 
            v-model="loginForm.password"
            type="password" 
            placeholder="密码" 
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item prop="agreeTerms">
           <el-checkbox v-model="loginForm.agreeTerms" size="default">
             我已阅读并同意
             <!-- 核心修改：为 el-link 添加点击事件，并阻止默认行为 -->
             <el-link 
               type="primary" 
               underline="never" 
               style="font-size: 14px; vertical-align: middle; margin-left: 2px;"
               @click.prevent="showTermsDialog"
             >
               《服务条款》
             </el-link>
           </el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-button 
            type="primary" 
            style="width: 100%;"
            @click="handleLogin"
            :loading="loading"
          >
            登 录
          </el-button>
        </el-form-item>

        <div class="footer-links">
          <el-link type="primary" @click="goToRegister">立即注册</el-link>
        </div>
      </el-form>
    </el-card>

    <!-- === 新增：服务条款对话框 === -->
    <el-dialog
      v-model="dialogVisible"
      :title="serviceTerms.title || '服务条款'"
      width="60%"
      v-loading="termsLoading"
    >
      <!-- 使用 v-html 来渲染可能包含格式的条款内容 -->
      <div class="terms-content" v-html="serviceTerms.content"></div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="dialogVisible = false">我已阅读</el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/userStore'
import { getCurrentServiceTermsApi } from '@/api/public.js' // 引入API

const userStore = useUserStore()
const router = useRouter()

const loginFormRef = ref(null)
const loading = ref(false)
const loginForm = reactive({ username: '', password: '', agreeTerms: false })
const loginRules = reactive({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  agreeTerms: [{
    validator: (rule, value, callback) => value ? callback() : callback(new Error('请先同意服务条款')),
    trigger: 'change'
  }]
})

// --- 新增：控制对话框的逻辑 ---
const dialogVisible = ref(false)
const termsLoading = ref(false)
const serviceTerms = ref({ title: '', content: '正在加载服务条款...' }) // 给个初始提示

async function showTermsDialog() {
  dialogVisible.value = true
  // 只有在内容是初始提示时才加载
  if (serviceTerms.value.content === '正在加载服务条款...') { 
    termsLoading.value = true;
    try {
      const data = await getCurrentServiceTermsApi();
      if (data) {
        serviceTerms.value = data;
      } else {
        serviceTerms.value.content = '暂时无法获取服务条款内容。';
      }
    } catch (error) {
      console.error('获取服务条款失败:', error);
      serviceTerms.value.content = '加载失败，请稍后重试。';
    } finally {
      termsLoading.value = false;
    }
  }
}

function goToRegister() {
  router.push({ name: 'register' }); // 假设我们有一个注册页
}

async function handleLogin() {
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await userStore.login({
          username: loginForm.username,
          password: loginForm.password
        })
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
/* ... (已有样式保持不变) ... */
.login-container { height: 100vh; width: 100vw; background-image: url('data:image/svg+xml,...'); display: flex; justify-content: center; align-items: center; }
.login-card { width: 400px; padding: 20px; background-color: rgba(255, 255, 255, 0.95); border-radius: 12px; box-shadow: 0 10px 30px -10px rgba(0,0,0,0.2); }
.login-header { display: flex; align-items: center; justify-content: center; margin-bottom: 24px; }
.login-logo { height: 32px; margin-right: 12px; }
.login-title { margin: 0; font-size: 24px; font-weight: 600; color: #333; }
.footer-links { display: flex; justify-content: flex-end; width: 100%; margin-top: -10px; }

/* 新增：服务条款内容样式 */
.terms-content {
  max-height: 60vh;
  overflow-y: auto;
  line-height: 1.8;
}
/* 通过 :deep() 确保能作用于v-html生成的内容 */
:deep(.terms-content p) {
  margin-bottom: 1em;
}
:deep(.terms-content h3) {
  margin-top: 1.5em;
  margin-bottom: 0.8em;
}
</style>