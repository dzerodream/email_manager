<!-- src/views/HomeView.vue -->
<template>
  <div class="home-root">
    <!-- 顶部欢迎语 -->
    <div class="welcome-bar">
      <div class="welcome-text">
        <h2>欢迎回来，{{ userInfo?.realName || userInfo?.username || '用户' }}！</h2>
        <p>祝您工作顺利，心情愉快！</p>
      </div>
      <div class="user-avatar">
        <img v-if="userInfo?.avatar" :src="getFullAvatarUrl(userInfo.avatar)" alt="avatar" />
        <el-icon v-else size="48"><User /></el-icon>
      </div>
    </div>

    <!-- 统计数据区 -->
    <el-row :gutter="24" class="stats-row">
      <el-col :md="6" :sm="12" :xs="24">
        <el-card class="stat-card">
          <div class="stat-title">未读邮件</div>
          <div class="stat-value unread">{{ unreadCount }}</div>
        </el-card>
      </el-col>
      <el-col :md="6" :sm="12" :xs="24">
        <el-card class="stat-card">
          <div class="stat-title">总邮件数</div>
          <div class="stat-value">{{ totalCount }}</div>
        </el-card>
      </el-col>
      <el-col :md="6" :sm="12" :xs="24">
        <el-card class="stat-card">
          <div class="stat-title">公告数量</div>
          <div class="stat-value">{{ announcementCount }}</div>
        </el-card>
      </el-col>
      <el-col :md="6" :sm="12" :xs="24">
        <el-card class="stat-card">
          <div class="stat-title">联系人</div>
          <div class="stat-value">{{ contactCount }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷入口区 -->
    <el-row :gutter="24" class="quick-row">
      <el-col v-for="item in quickLinks" :key="item.title" :md="6" :sm="12" :xs="24">
        <el-card class="quick-card" @click="goTo(item.route)">
          <el-icon :size="32" class="quick-icon"><component :is="item.icon" /></el-icon>
          <div class="quick-title">{{ item.title }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { User, Message, EditPen, Folder, Delete, Notebook, Bell, Calendar, Warning } from '@element-plus/icons-vue';
import { getInboxEmailsApi } from '@/api/email.js';
import { getActiveAnnouncementsApi } from '@/api/public.js';
import { useUserStore } from '@/stores/userStore';
import { getContactsApi } from '@/api/contact.js';

const router = useRouter();
const userStore = useUserStore();
const userInfo = userStore.userInfo;

// 统计数据
const inboxEmails = ref([]);
const unreadCount = computed(() => inboxEmails.value.filter(email => !email.isRead).length);
const totalCount = computed(() => inboxEmails.value.length);
const announcementCount = ref(0);
const contactCount = ref(0);

async function fetchStats() {
  try {
    inboxEmails.value = await getInboxEmailsApi();
  } catch (e) { inboxEmails.value = []; }
  try {
    const anns = await getActiveAnnouncementsApi();
    announcementCount.value = anns?.length || 0;
  } catch (e) { announcementCount.value = 0; }
  try {
    const contacts = await getContactsApi();
    contactCount.value = contacts?.length || 0;
  } catch (e) { contactCount.value = 0; }
}

onMounted(() => {
  fetchStats();
});

// 快捷入口配置
const quickLinks = computed(() => {
  const base = [
    { title: '写邮件', icon: EditPen, route: { name: 'compose' } },
    { title: '收件箱', icon: Message, route: { name: 'inbox' } },
    { title: '已发送', icon: EditPen, route: { name: 'sent' } },
    { title: '草稿箱', icon: Notebook, route: { name: 'drafts' } },
    { title: '归档箱', icon: Folder, route: { name: 'archived' } },
    { title: '垃圾箱', icon: Delete, route: { name: 'trash' } },
    { title: '通讯录', icon: Notebook, route: { name: 'contacts' } },
    { title: '系统公告', icon: Bell, route: { name: 'announcements' } },
    { title: '日历提醒', icon: Calendar, route: { name: 'calendar' } },
    { title: '拦截规则', icon: Warning, route: { name: 'spam-rules' } },
    { title: '个人中心', icon: User, route: { name: 'user-profile' } },
  ];
  if (userInfo && userInfo.role === 'ADMIN') {
    base.push({ title: '仪表盘', icon: User, route: { name: 'admin-dashboard' } });
  } else {
    base.push({ title: '服务条款', icon: Bell, route: { name: 'service-terms' } });
  }
  return base;
});

function goTo(route) {
  router.push(route);
}

function getFullAvatarUrl(relativePath) {
  if (!relativePath) return '';
  return `/api/files/download${relativePath.startsWith('/') ? relativePath : '/' + relativePath}`;
}
</script>

<style scoped>
.home-root {
  padding: 32px 24px;
  background: #f5f7fa;
  min-height: 100vh;
}
.welcome-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-radius: 8px;
  padding: 24px 32px 16px 32px;
  margin-bottom: 24px;
  box-shadow: 0 1px 4px rgba(0,21,41,0.08);
}
.welcome-text h2 { margin: 0 0 8px 0; font-size: 24px; font-weight: bold; }
.welcome-text p { margin: 0; color: #888; }
.user-avatar img { width: 48px; height: 48px; border-radius: 50%; object-fit: cover; }
.user-avatar { display: flex; align-items: center; justify-content: center; }
.stats-row { margin-bottom: 24px; }
.stat-card { text-align: center; border-radius: 8px; }
.stat-title { font-size: 16px; color: #888; margin-bottom: 8px; }
.stat-value { font-size: 28px; font-weight: bold; color: #409EFF; }
.stat-value.unread { color: #f56c6c; }
.quick-row { margin-top: 8px; }
.quick-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 120px;
  cursor: pointer;
  border-radius: 8px;
  transition: box-shadow 0.2s, transform 0.2s;
  box-shadow: 0 1px 4px rgba(0,21,41,0.04);
  text-align: center;
}
.quick-card:hover {
  box-shadow: 0 4px 16px rgba(64,158,255,0.15);
  transform: translateY(-2px) scale(1.03);
}
.quick-icon { margin-bottom: 12px; color: #409EFF; }
.quick-title { font-size: 16px; font-weight: 500; color: #333; }
</style>