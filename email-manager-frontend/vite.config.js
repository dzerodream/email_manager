// vite.config.js

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// --- 新增代码开始 ---
// 引入Node.js的path模块，用于处理文件路径
import { fileURLToPath, URL } from 'node:url'
// --- 新增代码结束 ---

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],

  // --- 新增代码开始 ---
  resolve: {
    alias: {
      // 配置@别名，使其指向 src 目录
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  // --- 新增代码结束 ---

  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080/EmailManager',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
})