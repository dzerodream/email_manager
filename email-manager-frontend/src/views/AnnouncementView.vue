<template>
  <div>
    <el-page-header content="系统公告" class="page-header" @back="goBack" />

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <el-empty v-else-if="!announcements.length" description="暂无公告" />

    <div v-else class="announcement-list">
      <el-card 
        v-for="announcement in announcements" 
        :key="announcement.id" 
        class="announcement-card" 
        shadow="hover"
      >
        <template #header>
          <div class="card-header">
            <span class="title">{{ announcement.title }}</span>
            <span class="time">{{ announcement.publishTime }}</span>
          </div>
        </template>
        <!-- 使用 v-html 来渲染可能包含格式的公告内容 -->
        <div class="content" v-html="announcement.content"></div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getActiveAnnouncementsApi } from '@/api/public.js';
import { ElMessage, ElSkeleton, ElEmpty } from 'element-plus'; // 引入骨架屏和空状态

const router = useRouter();
const loading = ref(false);
const announcements = ref([]);

async function fetchAnnouncements() {
  loading.value = true;
  try {
    const data = await getActiveAnnouncementsApi();
    announcements.value = data || []; // 确保如果data为null或undefined时，announcements是空数组
  } catch (error) {
    console.error('获取公告列表失败:', error);
    ElMessage.error('加载公告失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  fetchAnnouncements();
});

function goBack() {
  router.back();
}
</script>

<style scoped>
.page-header {
  background-color: #fff;
  padding: 16px 24px;
  border-radius: 4px;
  margin-bottom: 20px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.loading-container {
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
}

.announcement-list {
  display: grid;
  gap: 20px; /* 卡片之间的间距 */
}

.announcement-card {
  border-radius: 4px;
}

.announcement-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.announcement-card .title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.announcement-card .time {
  font-size: 13px;
  color: #909399;
}

.announcement-card .content {
  font-size: 14px;
  line-height: 1.8;
  color: #606266;
  white-space: pre-wrap; /* 保证内容中的换行符能被正确显示 */
  word-wrap: break-word;
}

/* 通过 :deep() 确保能作用于v-html生成的内容 */
:deep(.content p) {
  margin-bottom: 1em;
}
:deep(.content h1), 
:deep(.content h2),
:deep(.content h3) {
  margin-top: 1.5em;
  margin-bottom: 0.8em;
}
</style>