<!-- src/App.vue -->

<script setup>
import { RouterView } from 'vue-router'
</script>

<template>
  <!-- 
    这个组件会根据当前浏览器的URL地址，
    自动显示在 router/index.js 中配置好的对应页面组件。
  -->
  <RouterView />
</template>

<style>
/* 我们可以把全局样式写在这里，或者保持为空 */
body {
  margin: 0;
  padding: 0;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
}
</style>