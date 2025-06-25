// src/utils/request.js

import axios from 'axios'
import { ElMessage } from 'element-plus'
// 注意：我们在拦截器中动态获取store，而不是在文件顶部引入，以避免循环依赖问题
import { useUserStore } from '@/stores/userStore' 

const service = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 动态获取 userStore
    const userStore = useUserStore()
    // 检查 store 中是否存在 token
    if (userStore.token) {
      // 如果存在，则为请求头设置 Authorization
      config.headers['Authorization'] = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage({
        message: res.message || '业务错误，请稍后重试',
        type: 'error',
        duration: 3 * 1000,
      })
      return Promise.reject(new Error(res.message || 'Error'))
    } else {
      return res.data
    }
  },
  (error) => {
    // 如果HTTP状态码是401，说明Token无效或已过期
    if (error.response && error.response.status === 401) {
      const userStore = useUserStore()
      // 如果store中还有用户信息，才执行登出操作，避免重复执行
      if (userStore.userInfo) {
        ElMessage.error('认证失败或已过期，请重新登录')
        userStore.logout()
      }
    } else {
      ElMessage({
        message: '网络错误：' + error.message,
        type: 'error',
        duration: 5 * 1000,
      })
    }
    return Promise.reject(error)
  }
)

export default service