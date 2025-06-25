<template>
  <div class="admin-user-management-view-root">
    <el-page-header content="用户管理" class="page-header" @back="goBack" />

    <el-card class="content-card-flex">
      <template #header>
        <div class="card-header">
          <span>用户列表</span>
          <div class="header-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="按用户名、邮箱或姓名搜索"
              clearable
              :prefix-icon="Search"
              style="width: 280px; margin-right: 10px;"
            />
            <el-button type="primary" :icon="Plus" @click="openUserDialog('add')">新增用户</el-button>
          </div>
        </div>
      </template>

      <el-table :data="filteredUserList" v-loading="loading" style="width: 100%; height: 100%;" empty-text="暂无用户数据或未找到匹配项" border stripe>
        <el-table-column prop="id" label="ID" width="80" align="center" sortable />
        <el-table-column prop="username" label="用户名" width="150" sortable />
        
        <!-- === 核心修改：为“邮箱”列设置min-width，让其自动伸展 === -->
        <el-table-column prop="email" label="邮箱" min-width="200" show-overflow-tooltip /> 
        
        <el-table-column prop="realName" label="真实姓名" width="150" />
        <el-table-column prop="phone" label="电话" width="150" />
        <el-table-column label="角色" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.role === 'ADMIN' ? 'success' : 'primary'" size="small">{{ scope.row.role }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
              :loading="scope.row.statusLoading" 
              :disabled="userStore.userInfo?.id === scope.row.id" 
              style="--el-switch-on-color: #13ce66; --el-switch-off-color: #ff4949"
            />
          </template>
        </el-table-column>

        <el-table-column prop="createdTime" label="创建时间" width="180" align="center" sortable/>
        <el-table-column label="操作" width="130" align="center" fixed="right">
          <template #default="scope">
            <el-tooltip content="编辑用户信息" placement="top">
              <el-button 
                type="primary" 
                :icon="Edit" 
                circle 
                size="small" 
                @click.stop="openUserDialog('edit', scope.row)"
              />
            </el-tooltip>
            <el-tooltip content="删除用户" placement="top">
              <el-button 
                type="danger" 
                :icon="Delete" 
                circle 
                size="small" 
                @click.stop="handleDeleteUser(scope.row)"
                :disabled="userStore.userInfo?.id === scope.row.id" 
              />
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <UserFormDialog 
      v-if="dialogVisible" 
      v-model:visible="dialogVisible" 
      :mode="dialogMode" 
      :initial-data="currentUserForDialog" 
      @success="onFormSuccess" 
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/userStore';
import { getAllUsersApi, adminDeleteUserApi, adminUpdateUserStatusApi } from '@/api/admin.js';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Edit, Delete, Search } from '@element-plus/icons-vue';
import UserFormDialog from './components/UserFormDialog.vue';

const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);
const userList = ref([]);
const searchKeyword = ref('');

const filteredUserList = computed(() => {
  const keyword = searchKeyword.value.toLowerCase().trim();
  if (!keyword) {
    return userList.value;
  }
  return userList.value.filter(user => 
    (user.username && user.username.toLowerCase().includes(keyword)) ||
    (user.email && user.email.toLowerCase().includes(keyword)) ||
    (user.realName && user.realName.toLowerCase().includes(keyword))
  );
});

async function fetchUsers() {
  loading.value = true;
  try {
    const data = await getAllUsersApi();
    userList.value = data ? data.map(user => ({ ...user, statusLoading: false })) : [];
  } catch (error) {
    console.error('获取用户列表失败:', error);
    ElMessage.error('加载用户列表失败');
  } finally {
    loading.value = false;
  }
}
onMounted(() => {
  fetchUsers();
});

const dialogVisible = ref(false);
const dialogMode = ref('add');
const currentUserForDialog = ref(null);

function openUserDialog(mode, user = null) {
  dialogMode.value = mode;
  currentUserForDialog.value = user ? { ...user } : null; 
  dialogVisible.value = true;
}

function onFormSuccess() {
  dialogVisible.value = false;
  fetchUsers();
}

async function handleDeleteUser(user) {
  if (userStore.userInfo && userStore.userInfo.id === user.id) {
    ElMessage.warning('不能删除自己的账户！');
    return;
  }
  ElMessageBox.confirm( `确定要永久删除用户 "${user.username}" 吗? 此操作无法恢复。`, '删除用户确认', 
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'error' }
  ).then(async () => {
    try {
      await adminDeleteUserApi(user.id);
      ElMessage.success(`用户 "${user.username}" 删除成功！`);
      fetchUsers();
    } catch (error) { 
      console.error('删除用户失败:', error);
    }
  }).catch(() => { 
    ElMessage.info('已取消删除'); 
  });
}

async function handleStatusChange(userRow) {
  const newStatus = userRow.status;
  const actionText = newStatus === 1 ? '启用' : '禁用';

  if (userStore.userInfo && userStore.userInfo.id === userRow.id && newStatus === 0) {
    ElMessage.warning('不能禁用自己的账户！');
    userRow.status = 1; 
    return;
  }
  
  userRow.statusLoading = true;
  try {
    await adminUpdateUserStatusApi(userRow.id, newStatus);
    ElMessage.success(`用户 "${userRow.username}" 已${actionText}`);
  } catch (error) {
    ElMessage.error(`${actionText}用户 "${userRow.username}" 失败`);
    userRow.status = newStatus === 1 ? 0 : 1;
  } finally {
    userRow.statusLoading = false;
  }
}

function goBack() {
  router.back();
}
</script>

<style scoped>
.admin-user-management-view-root {
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

:deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  flex-grow: 1;
  padding: 20px;
  overflow: hidden; 
  height: 0; 
}

:deep(.el-table) {
  height: 100%; 
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