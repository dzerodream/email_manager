<!-- src/views/AdminDashboard.vue -->

<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>管理员仪表盘</span>
        </div>
      </template>
      <p>欢迎回来，管理员！您在这里可以管理整个系统。</p>
      <p>后续我们将在这里添加用户管理、系统公告等功能的入口。</p>
    </el-card>
  </div>
</template>

<script setup>
// 目前此页面无逻辑
</script>

<style scoped>
.card-header {
  font-size: 18px;
  font-weight: bold;
}
</style>