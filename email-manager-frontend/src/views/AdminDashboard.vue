<!-- src/views/AdminDashboard.vue -->

<template>
  <div class="dashboard-root">
    <div class="dashboard-center">
      <!-- 顶部欢迎语 -->
      <div class="welcome-bar">
        <div class="welcome-text">
          <h2>欢迎回来，管理员！</h2>
          <p>您在这里可以管理整个系统。</p>
        </div>
      </div>

      <!-- 统计数据区 -->
      <el-row :gutter="24" class="stats-row">
        <el-col :md="4" :sm="12" :xs="24" v-for="(item, idx) in statList" :key="idx">
          <el-card class="stat-card">
            <div class="stat-title">{{ item.title }}</div>
            <div class="stat-value">{{ item.value }}</div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 快捷管理入口区 -->
      <el-row :gutter="24" class="quick-row">
        <el-col v-for="item in quickLinks" :key="item.title" :md="4" :sm="12" :xs="24">
          <el-card class="quick-card" @click="goTo(item.route)">
            <el-icon :size="32" class="quick-icon"><component :is="item.icon" /></el-icon>
            <div class="quick-title">{{ item.title }}</div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { User, Message, Bell, Calendar, Warning, Notebook, Setting } from '@element-plus/icons-vue';
import { getAllUsersApi, adminGetAllAnnouncementsApi, adminGetGlobalSpamRulesApi, adminGetHolidaysByYearApi } from '@/api/admin.js';
import { getInboxEmailsApi, getSentEmailsApi, getDraftsEmailsApi, getTrashEmailsApi, getArchivedEmailsApi } from '@/api/email.js';

const router = useRouter();
const userCount = ref(0);
const emailCount = ref(0);
const announcementCount = ref(0);
const spamRuleCount = ref(0);
const holidayCount = ref(0);

async function fetchStats() {
  try {
    const users = await getAllUsersApi();
    userCount.value = users?.length || 0;
  } catch (e) { userCount.value = 0; }
  try {
    const inbox = await getInboxEmailsApi();
    const sent = await getSentEmailsApi();
    const drafts = await getDraftsEmailsApi();
    const trash = await getTrashEmailsApi();
    const archived = await getArchivedEmailsApi();
    emailCount.value = (inbox?.length || 0) + (sent?.length || 0) + (drafts?.length || 0) + (trash?.length || 0) + (archived?.length || 0);
  } catch (e) { emailCount.value = 0; }
  try {
    const anns = await adminGetAllAnnouncementsApi();
    announcementCount.value = anns?.length || 0;
  } catch (e) { announcementCount.value = 0; }
  try {
    const rules = await adminGetGlobalSpamRulesApi();
    spamRuleCount.value = rules?.length || 0;
  } catch (e) { spamRuleCount.value = 0; }
  try {
    const year = new Date().getFullYear();
    const holidays = await adminGetHolidaysByYearApi(year);
    holidayCount.value = holidays?.length || 0;
  } catch (e) { holidayCount.value = 0; }
}

onMounted(() => {
  fetchStats();
});

const quickLinks = [
  { title: '用户管理', icon: User, route: { name: 'admin-user-management' } },
  { title: '公告管理', icon: Bell, route: { name: 'admin-announcement-management' } },
  { title: '服务条款管理', icon: Setting, route: { name: 'admin-service-terms-management' } },
  { title: '节假日管理', icon: Calendar, route: { name: 'admin-holiday-management' } },
  { title: '全局拦截规则', icon: Warning, route: { name: 'admin-global-spam-rules' } },
];

function goTo(route) {
  router.push(route);
}

const statList = [
  { title: '用户总数', value: userCount },
  { title: '邮件总数', value: emailCount },
  { title: '公告总数', value: announcementCount },
  { title: '全局拦截规则', value: spamRuleCount },
  { title: '节假日', value: holidayCount },
];
</script>

<style scoped>
.dashboard-root {
  padding: 32px 24px;
  background: #f5f7fa;
  min-height: 100vh;
}
.dashboard-center {
  width: 100%;
  margin: 0 auto;
  padding: 0 16px;
}
.welcome-bar {
  background: #fff;
  border-radius: 8px;
  padding: 24px 32px 16px 32px;
  margin-bottom: 24px;
  box-shadow: 0 1px 4px rgba(0,21,41,0.08);
}
.welcome-text h2 { margin: 0 0 8px 0; font-size: 24px; font-weight: bold; }
.welcome-text p { margin: 0; color: #888; }
.stats-row { margin-bottom: 24px; }
.stat-card { text-align: center; border-radius: 8px; }
.stat-title { font-size: 16px; color: #888; margin-bottom: 8px; }
.stat-value { font-size: 28px; font-weight: bold; color: #409EFF; }
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