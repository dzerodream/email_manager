<template>
  <div class="admin-announcement-view-root">
    <el-page-header content="系统公告管理" class="page-header" @back="goBack" />

    <el-card class="content-card-flex">
      <template #header>
        <div class="card-header">
          <span>公告列表</span>
          <div class="header-actions">
            <el-button type="primary" :icon="Plus" @click="openAnnouncementDialog('add')">发布新公告</el-button>
          </div>
        </div>
      </template>

      <el-table :data="announcementList" v-loading="loading" style="width: 100%; height: 100%;" empty-text="暂无公告" border stripe>
        <el-table-column prop="id" label="ID" width="80" align="center" sortable />
        <el-table-column prop="title" label="公告标题" min-width="250" show-overflow-tooltip />
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="small">
              {{ scope.row.status === 1 ? '已发布' : '草稿/禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publisherId" label="发布者ID" width="120" align="center" />
        <el-table-column prop="publishTime" label="发布时间" width="180" align="center" sortable />
        <el-table-column prop="createdTime" label="创建时间" width="180" align="center" sortable />
        <el-table-column prop="updatedTime" label="最后更新" width="180" align="center" sortable />
        <el-table-column label="操作" width="130" align="center" fixed="right">
          <template #default="scope">
            <el-tooltip content="编辑公告" placement="top">
              <el-button 
                type="primary" 
                :icon="Edit" 
                circle 
                size="small" 
                @click.stop="openAnnouncementDialog('edit', scope.row)"
              />
            </el-tooltip>
            <el-tooltip content="删除公告" placement="top">
              <el-button 
                type="danger" 
                :icon="Delete" 
                circle 
                size="small" 
                @click.stop="handleDelete(scope.row)"
              />
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 使用公告表单对话框子组件 -->
    <AnnouncementFormDialog 
      v-if="dialogVisible"
      v-model:visible="dialogVisible" 
      :mode="dialogMode" 
      :initial-data="currentAnnouncementForDialog" 
      @success="onFormSuccess" 
    /> 
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { adminGetAllAnnouncementsApi, adminDeleteAnnouncementApi } from '@/api/admin.js';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Edit, Delete } from '@element-plus/icons-vue';
import AnnouncementFormDialog from './components/AnnouncementFormDialog.vue'; // 引入子组件

const router = useRouter();
const loading = ref(false);
const announcementList = ref([]);

async function fetchAnnouncements() {
  loading.value = true;
  try {
    const data = await adminGetAllAnnouncementsApi();
    announcementList.value = data || [];
  } catch (error) {
    console.error('获取公告列表失败:', error);
    ElMessage.error('加载公告列表失败');
  } finally {
    loading.value = false;
  }
}
onMounted(() => {
  fetchAnnouncements();
});

const dialogVisible = ref(false);
const dialogMode = ref('add');
const currentAnnouncementForDialog = ref(null); // 修改变量名以区分

function openAnnouncementDialog(mode, announcement = null) {
  dialogMode.value = mode;
  currentAnnouncementForDialog.value = announcement ? { ...announcement } : null;
  dialogVisible.value = true;
}

function onFormSuccess() {
  dialogVisible.value = false;
  fetchAnnouncements();
}

async function handleDelete(announcement) {
  ElMessageBox.confirm(`确定要删除公告 "${announcement.title}" 吗? 此操作无法恢复。`, '删除确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await adminDeleteAnnouncementApi(announcement.id);
      ElMessage.success('公告删除成功！');
      fetchAnnouncements();
    } catch (error) {
      console.error('删除公告失败:', error);
    }
  }).catch(() => {
    ElMessage.info('已取消删除');
  });
}

function goBack() {
  router.back();
}
</script>

<style scoped>
.admin-announcement-view-root {
  display: flex;
  flex-direction: column;
  height: 100%; 
  width: 100%;
}

.page-header {
  background-color: #fff;
  padding: 16px 24px;
  border-radius: 4px;
  margin-bottom: 20px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  flex-shrink: 0; 
}

.content-card-flex {
  width: 100%;
  flex-grow: 1; 
  display: flex; 
  flex-direction: column;
  overflow: hidden; 
}

/* 
  确保 el-card 的主体部分能够正确地让内部的 el-table 撑满。
  Element Plus 的 el-card__body 默认可能没有 flex 属性，或者其高度不是 100%。
  我们需要确保 el-table 有足够的垂直空间来展示其滚动条。
*/
:deep(.el-card__body) {
  flex-grow: 1; /* 让卡片主体占据所有可用垂直空间 */
  display: flex; /* 将其设为flex容器 */
  flex-direction: column; /* 其内部元素垂直排列 */
  padding: 20px; /* Element Plus默认内边距，可以根据需要调整或移除 */
  overflow: hidden; /* 非常重要：防止内容溢出导致父级flex计算错误 */
  height: calc(100% - 57px); /* 尝试减去 el-card__header 的近似高度 (如果header高度固定为57px) */
                              /* 如果header高度不固定，或者想要更精确，可能需要JavaScript计算 */
                              /* 或者，如果el-card内部结构允许，直接让el-table的父容器是flex-grow:1 */
}

:deep(.el-table) {
  flex-grow: 1; /* 让表格在 .el-card__body 中伸展 */
  height: 100%; /* 让表格尝试占据 .el-card__body 的全部高度，以便其内部滚动条生效 */
}


.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}
.header-actions {
  display: flex;
  align-items: center;
}
</style>