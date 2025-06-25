// src/main.js

import { createApp } from 'vue'
import App from './App.vue'

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import router from './router'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// --- 新增代码开始 ---
import { createPinia } from 'pinia'
// --- 新增代码结束 ---

const app = createApp(App)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// --- 新增代码开始 ---
app.use(createPinia()) // 使用Pinia
// --- 新增代码结束 ---

app.use(ElementPlus)
app.use(router)
app.mount('#app')