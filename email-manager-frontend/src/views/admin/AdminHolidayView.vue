<template>
  <div class="admin-holiday-view-root">
    <el-page-header content="节假日与调休管理" class="page-header" @back="goBack" />

    <el-card class="content-card-flex">
      <template #header>
        <div class="card-header">
          <div class="header-left-controls">
            <span>当前年份：</span>
            <el-select 
              v-model="selectedYear" 
              placeholder="选择年份" 
              @change="fetchHolidays" 
              size="default" 
              style="width: 120px;"
            >
              <el-option v-for="year in yearOptions" :key="year" :label="`${year}年`" :value="year" />
            </el-select>
          </div>
          <el-button type="primary" :icon="Plus" @click="openDialog('add')">新增记录</el-button>
        </div>
      </template>

      <el-table :data="holidayList" v-loading="loading" style="width: 100%; height:100%;" border stripe empty-text="该年份暂无节假日或调休记录">
        <el-table-column prop="id" label="ID" width="80" align="center" sortable />
        <el-table-column prop="holidayDate" label="日期" width="150" align="center" sortable />
        <el-table-column prop="holidayName" label="名称" width="200" show-overflow-tooltip />
        <el-table-column label="类型" width="150" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.isWorkday === 1 ? 'warning' : 'success'" size="small">
              {{ scope.row.isWorkday === 1 ? '调休上班日' : '节假日/休息日' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="250" show-overflow-tooltip />
        <el-table-column prop="createdTime" label="创建时间" width="180" align="center" sortable />
        <el-table-column label="操作" width="130" align="center" fixed="right">
          <template #default="scope">
            <el-tooltip content="编辑记录" placement="top">
              <el-button 
                type="primary" 
                :icon="Edit" 
                circle 
                size="small" 
                @click.stop="openDialog('edit', scope.row)"
              />
            </el-tooltip>
            <el-tooltip content="删除记录" placement="top">
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

    <!-- 核心修改：移除了 v-if，只保留 v-model:visible -->
    <HolidayFormDialog 
      v-if="dialogVisible" 
      v-model:visible="dialogVisible" 
      :mode="dialogMode" 
      :initial-data="currentHolidayForDialog" 
      @success="onFormSuccess" 
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { adminGetHolidaysByYearApi, adminDeleteHolidayApi } from '@/api/admin.js';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Edit, Delete } from '@element-plus/icons-vue';
import HolidayFormDialog from './components/HolidayFormDialog.vue';

const router = useRouter();
const loading = ref(false);
const holidayList = ref([]);

// --- 年份选择 ---
const currentSystemYear = new Date().getFullYear();
const selectedYear = ref(currentSystemYear);
const yearOptions = computed(() => {
  const startYear = currentSystemYear - 5; // 向前5年
  const endYear = currentSystemYear + 5;   // 向后5年
  const years = [];
  for (let y = startYear; y <= endYear; y++) {
    years.push(y);
  }
  return years;
});

// --- 获取节假日列表 ---
async function fetchHolidays() {
  loading.value = true;
  try {
    const data = await adminGetHolidaysByYearApi(selectedYear.value);
    holidayList.value = data || [];
  } catch (error) {
    console.error('获取节假日列表失败:', error);
    ElMessage.error('加载节假日列表失败');
  } finally {
    loading.value = false;
  }
}
onMounted(() => {
  fetchHolidays(); // 首次加载当前年份的数据
});

// --- 对话框相关 ---
const dialogVisible = ref(false); // 对话框初始时是不可见的
const dialogMode = ref('add');
const currentHolidayForDialog = ref(null);

function openDialog(mode, holiday = null) {
  dialogMode.value = mode;
  currentHolidayForDialog.value = holiday ? { ...holiday } : null; 
  dialogVisible.value = true; // 打开对话框
}

function onFormSuccess() {
  dialogVisible.value = false; // 关闭对话框
  fetchHolidays(); // 操作成功后刷新当前年份的列表
}

// --- 删除记录 ---
async function handleDelete(holiday) {
  ElMessageBox.confirm(`确定要删除记录 "${holiday.holidayName} (${holiday.holidayDate})" 吗? 此操作无法恢复。`, '删除确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await adminDeleteHolidayApi(holiday.id);
      ElMessage.success('记录删除成功！');
      fetchHolidays();
    } catch (error) {
      console.error('删除记录失败:', error);
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
.admin-holiday-view-root {
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
.header-left-controls {
  display: flex;
  align-items: center;
  font-size: 16px; 
}
.header-actions {
  display: flex;
  align-items: center;
}
</style>