<template>
    <div class="admin-service-terms-view-root">
      <el-page-header content="服务条款管理" class="page-header" @back="goBack" />
      <el-card class="content-card-flex">
        <template #header>
          <div class="card-header">
            <span>服务条款版本列表</span>
            <el-button type="primary" :icon="Plus" @click="openDialog('add')">发布新版本</el-button>
          </div>
        </template>
        <el-table :data="termsList" v-loading="loading" style="width: 100%; height:100%;" border stripe empty-text="暂无服务条款">
          <el-table-column prop="id" label="ID" width="80" align="center" sortable />
          <el-table-column prop="version" label="版本号" width="100" align="center" sortable/>
          <el-table-column prop="title" label="标题" min-width="250" show-overflow-tooltip />
          <el-table-column label="状态" width="120" align="center">
            <template #default="scope">
              <el-tag :type="getStatusTagType(scope.row.status)" size="small">
                {{ formatStatus(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="effectiveDate" label="生效日期" width="180" align="center" sortable />
          <el-table-column prop="createdTime" label="创建时间" width="180" align="center" sortable />
          <el-table-column label="操作" width="180" align="center" fixed="right">
            <template #default="scope">
              <el-tooltip content="编辑条款 (通常仅草稿可编辑)" placement="top">
                <el-button 
                  type="primary" 
                  :icon="Edit" 
                  circle 
                  size="small" 
                  @click.stop="openDialog('edit', scope.row)"
                  :disabled="scope.row.status === 1 || scope.row.status === 2" 
                />
              </el-tooltip>
              <el-tooltip :content="getToggleStatusTooltip(scope.row)" placement="top">
                  <el-button 
                      :icon="scope.row.status === 0 ? Check : (scope.row.status === 1 ? Close : RefreshLeft)" 
                      circle 
                      :type="scope.row.status === 0 ? 'success' : (scope.row.status === 1 ? 'warning' : 'info')"
                      size="small"
                      @click.stop="toggleStatus(scope.row)"
                      :disabled="scope.row.status === 2"  
                  />
              </el-tooltip>
              <!-- 物理删除通常不提供给服务条款，这里不放删除按钮 -->
            </template>
          </el-table-column>
        </el-table>
      </el-card>
  
      <!-- 使用服务条款表单对话框子组件 -->
      <ServiceTermsFormDialog 
        v-if="dialogVisible"
        v-model:visible="dialogVisible" 
        :mode="dialogMode" 
        :initial-data="currentTermForDialog" 
        @success="onFormSuccess" 
      />
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  import { adminGetServiceTermsApi, adminUpdateServiceTermStatusApi } from '@/api/admin.js';
  import { ElMessage, ElMessageBox } from 'element-plus';
  import { Plus, Edit, Check, Close, RefreshLeft } from '@element-plus/icons-vue';
  import ServiceTermsFormDialog from './components/ServiceTermsFormDialog.vue'; // 引入子组件
  
  const router = useRouter();
  const loading = ref(false);
  const termsList = ref([]);
  const dialogVisible = ref(false);
  const dialogMode = ref('add');
  const currentTermForDialog = ref(null);
  
  async function fetchTerms() {
    loading.value = true;
    try {
      const data = await adminGetServiceTermsApi();
      termsList.value = data || [];
    } catch (error) {
      ElMessage.error('加载服务条款列表失败');
    } finally {
      loading.value = false;
    }
  }
  onMounted(fetchTerms);
  
  function openDialog(mode, term = null) {
    dialogMode.value = mode;
    currentTermForDialog.value = term ? { ...term } : null;
    dialogVisible.value = true;
  }
  
  function onFormSuccess() {
    dialogVisible.value = false;
    fetchTerms(); // 操作成功后刷新列表
  }
  
  function goBack() {
    router.back();
  }
  
  function formatStatus(status) {
    if (status === 0) return '草稿';
    if (status === 1) return '已生效';
    if (status === 2) return '已过期';
    return '未知';
  }
  
  function getStatusTagType(status) {
    if (status === 0) return 'warning';
    if (status === 1) return 'success';
    if (status === 2) return 'info';
    return 'info';
  }
  
  function getToggleStatusTooltip(row) {
      if (row.status === 0) return '设为生效 (将替换当前生效版本)';
      if (row.status === 1) return '设为过期';
      // if (row.status === 2) return '重新激活 (不推荐)'; // 通常不直接激活过期版本
      return '切换状态';
  }
  
  async function toggleStatus(row) {
    let newStatus;
    let actionText;
    let confirmMessage;
  
    if (row.status === 0) { // 当前是草稿
      newStatus = 1;
      actionText = '设为生效';
      confirmMessage = `确定要将版本 ${row.version} "${row.title}" ${actionText}吗? 这会使当前已生效的版本 (如有) 自动变为“已过期”。`;
    } else if (row.status === 1) { // 当前是生效
      newStatus = 2;
      actionText = '设为过期';
      confirmMessage = `确定要手动将当前生效的版本 ${row.version} "${row.title}" ${actionText}吗? 如果没有其他生效版本，用户将看不到服务条款。`;
    } else if (row.status === 2) { // 当前是过期
      ElMessage.warning('已过期的条款通常不直接操作状态，请考虑发布新版本或编辑后重新发布。');
      return;
    } else {
      return;
    }
  
    ElMessageBox.confirm(confirmMessage, '状态变更确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(async () => {
      try {
        // 后端 /publish 和 /id/status 接口会处理版本间的状态联动
        // 如果是想让一个草稿生效，可以直接调用更新状态的API
        // 后端ServiceTermsController的updateStatus已经有让旧版本失效的逻辑
        await adminUpdateServiceTermStatusApi(row.id, newStatus);
        ElMessage.success(`版本 ${row.version} 状态已更新为 "${formatStatus(newStatus)}"`);
        fetchTerms(); 
      } catch (error) {
        ElMessage.error('状态更新失败');
      }
    }).catch(() => {
      ElMessage.info('已取消操作');
    });
  }
  </script>
  
  <style scoped>
  .admin-service-terms-view-root, .content-card-flex, :deep(.el-card__body), :deep(.el-table) {
    display: flex;
    flex-direction: column;
    height: 100%; 
    width: 100%;
  }
  .page-header { flex-shrink: 0;}
  .content-card-flex { flex-grow: 1; overflow: hidden; }
  :deep(.el-card__body) { flex-grow: 1; padding: 20px; overflow: hidden; height: 0; }
  :deep(.el-table) { height: 100%; }
  .card-header { display: flex; justify-content: space-between; align-items: center; font-size: 18px; font-weight: bold; }
  .header-actions { display: flex; align-items: center; }
  </style>