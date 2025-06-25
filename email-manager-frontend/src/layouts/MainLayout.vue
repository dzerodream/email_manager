<template>
  <el-container class="main-layout">
    <!-- 侧边栏 -->
    <el-aside width="200px" class="main-sider">
      <div class="logo-area">
        <img src="/logo.svg" alt="Logo" />
        <span>邮件管理系统</span>
      </div>

      <div class="compose-btn-wrapper">
        <el-button type="primary" :icon="EditPen" @click="goToCompose" style="width: 85%;">
          写邮件
        </el-button>
      </div>
      
      <el-menu
        active-text-color="#ffd04b"
        background-color="#545c64"
        class="el-menu-vertical-demo"
        :default-active="activeMenu"
        text-color="#fff"
        router
      >
        <el-menu-item index="/home" v-if="userStore.userInfo?.role?.includes('USER') || userStore.userInfo?.role?.includes('ADMIN')">
          <el-icon><HomeFilled /></el-icon>
          <span>系统主页</span>
        </el-menu-item>

        <el-sub-menu index="/mail" v-if="userStore.userInfo?.role?.includes('USER') || userStore.userInfo?.role?.includes('ADMIN')">
          <template #title>
            <el-icon><Menu /></el-icon>
            <span>我的邮箱</span>
          </template>
          <el-menu-item index="/inbox"><el-icon><MessageBox /></el-icon><span>收件箱</span></el-menu-item>
          <el-menu-item index="/sent"><el-icon><Promotion /></el-icon><span>已发送</span></el-menu-item>
          <el-menu-item index="/drafts"><el-icon><EditPen /></el-icon><span>草稿箱</span></el-menu-item>
          <el-menu-item index="/archived"><el-icon><Folder /></el-icon><span>归档箱</span></el-menu-item>
          <el-menu-item index="/trash"><el-icon><Delete /></el-icon><span>垃圾箱</span></el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/contacts" v-if="userStore.userInfo?.role?.includes('USER') || userStore.userInfo?.role?.includes('ADMIN')">
          <el-icon><Postcard /></el-icon>
          <span>通讯录</span>
        </el-menu-item>

        <el-menu-item index="/announcements" v-if="userStore.userInfo">
          <el-icon><Bell /></el-icon>
          <span>系统公告</span>
        </el-menu-item>

        <el-menu-item index="/calendar" v-if="userStore.userInfo">
          <el-icon><Calendar /></el-icon>
          <span>日历提醒</span>
        </el-menu-item>

        <el-menu-item index="/spam-rules" v-if="userStore.userInfo">
          <el-icon><Tickets /></el-icon>
          <span>拦截规则</span>
        </el-menu-item>

        <el-sub-menu index="/admin" v-if="userStore.userInfo?.role?.includes('ADMIN')">
           <template #title><el-icon><Setting /></el-icon><span>系统管理</span></template>
          <el-menu-item index="/admin/dashboard"><el-icon><DataAnalysis /></el-icon><span>管理员看板</span></el-menu-item>
          <el-menu-item index="/admin/users"><el-icon><UserFilled /></el-icon><span>用户管理</span></el-menu-item>
          <el-menu-item index="/admin/announcements"><el-icon><Notification /></el-icon><span>公告管理</span></el-menu-item>
          <el-menu-item index="/admin/service-terms"><el-icon><DocumentCopy /></el-icon><span>服务条款</span></el-menu-item>
          <el-menu-item index="/admin/global-spam-rules"><el-icon><Filter /></el-icon><span>全局拦截规则</span></el-menu-item>
          <el-menu-item index="/admin/holidays"><el-icon><Calendar /></el-icon><span>节假日配置</span></el-menu-item>
          <!-- 可以在这里添加更多管理员菜单，例如节假日管理、全局规则管理 -->
        </el-sub-menu>
      </el-menu>
    </el-aside>
    
    <el-container class="main-panel">
      <el-header class="main-header">
        <div class="header-left">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索邮件..."
            :prefix-icon="Search"
            clearable
            @keyup.enter="executeSearch"
            style="width: 300px;"
          />
        </div>
        <div class="user-info">
          <span>欢迎, {{ userStore.userInfo?.realName || userStore.userInfo?.username }}</span>
          <el-dropdown @command="handleUserCommand">
            <el-avatar 
              :size="30" 
              :src="userAvatarSrc"  
              style="margin-left: 15px; cursor: pointer;"
              @error="avatarLoadError" 
            >
              <span v-if="!userStore.userInfo?.avatar || avatarError">{{ userStore.userInfo?.username?.charAt(0).toUpperCase() }}</span>
            </el-avatar>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-main class="main-content-flex">
        <router-view v-slot="{ Component, route }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" :key="route.fullPath" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/stores/userStore';
import { 
  HomeFilled, Menu, MessageBox, Promotion, EditPen, Delete, Setting, 
  DataAnalysis, Postcard, Folder, Search, Bell, Calendar, Tickets, UserFilled, Notification, DocumentCopy 
} from '@element-plus/icons-vue';

const router = useRouter();
const userStore = useUserStore();
const route = useRoute();

const searchKeyword = ref('');
const avatarError = ref(false);

function executeSearch() {
  if (searchKeyword.value.trim()) {
    router.push({ name: 'email-search', query: { keyword: searchKeyword.value.trim() } });
  }
}

function goToCompose() {
  router.push({ name: 'compose' });
}

const activeMenu = computed(() => {
  // 如果当前是搜索结果页，可以不激活任何菜单，或者激活一个默认项如收件箱
  if (route.name === 'email-search') {
    return '/inbox'; // 或者 return '' 如果不想激活任何项
  }
  return route.path;
});

const userAvatarSrc = computed(() => {
  if (userStore.userInfo && userStore.userInfo.avatar) {
    const avatarPath = userStore.userInfo.avatar;
    avatarError.value = false; // 每次重新计算头像路径时，重置错误状态
    return `/api/files/download${avatarPath.startsWith('/') ? avatarPath : '/' + avatarPath}`;
  }
  return ''; // 没有头像路径，返回空，el-avatar会显示插槽内容
});

function avatarLoadError() {
  console.warn("Avatar image failed to load, falling back to initial.");
  avatarError.value = true; // 标记头像加载失败
  return true; // 阻止el-avatar默认的错误处理（如显示损坏图标）
}

function handleUserCommand(command) {
  if (command === 'profile') {
    router.push({ name: 'user-profile' });
  } else if (command === 'logout') {
    userStore.logout();
  }
}
</script>

<style scoped>
.main-layout {
  height: 100vh;
  overflow: hidden; 
}
.main-sider {
  background-color: #545c64;
  color: white;
  display: flex;
  flex-direction: column;
  height: 100vh;
}
.logo-area {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 60px;
  font-size: 18px;
  font-weight: bold;
  flex-shrink: 0;
}
.logo-area img {
  height: 30px;
  margin-right: 10px;
}
.compose-btn-wrapper {
  padding: 15px 0;
  text-align: center;
  flex-shrink: 0;
}
.el-menu {
  border-right: none;
  flex-grow: 1;
  overflow-y: auto;
}
.main-panel {
  display: flex;
  flex-direction: column;
  height: 100vh;
}
.main-header {
  background-color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e6e6e6;
  padding: 0 20px;
  height: 60px;
  flex-shrink: 0;
}
.header-left {
  display: flex;
  align-items: center;
}
.user-info {
  display: flex;
  align-items: center;
}
.main-content-flex {
  flex-grow: 1;
  padding: 20px;
  background-color: #f0f2f5;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}
:deep(.main-content-flex > div) {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  width: 100%;
}
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: opacity 0.2s ease-out;
}
.fade-transform-enter-from,
.fade-transform-leave-to {
  opacity: 0;
}
</style>