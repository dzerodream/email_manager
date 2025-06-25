// src/stores/userStore.js

import { defineStore } from 'pinia'
import { ref } from 'vue'
import { loginApi } from '@/api/user.js' // 假设 getProfileApi 已被移除
import router from '@/router'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo')) || null)
  const token = ref(localStorage.getItem('token') || null)

  async function login(loginData) {
    try {
      const responseData = await loginApi(loginData)
      userInfo.value = responseData.userInfo
      token.value = responseData.token
      localStorage.setItem('userInfo', JSON.stringify(responseData.userInfo))
      localStorage.setItem('token', responseData.token)
      ElMessage.success('登录成功！')
      if (responseData.userInfo.role === 'ADMIN') {
        await router.push({ name: 'admin-dashboard' })
      } else {
        await router.push({ name: 'home' })
      }
    } catch (error) {
      console.error('登录逻辑失败:', error)
      throw error
    }
  }

  // --- 核心修改：为 logout 函数添加参数 ---
  function logout(showLogoutMessage = true) { 
    userInfo.value = null
    token.value = null
    localStorage.removeItem('userInfo')
    localStorage.removeItem('token')
    
    if (showLogoutMessage) { // 根据参数决定是否显示默认的登出消息
      ElMessage.success('已成功退出')
    }
    router.push('/login')
  }

  return {
    userInfo,
    token,
    login,
    logout,
  }
})