<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>垃圾箱</span>
          <el-button 
            type="danger" 
            :icon="Delete" 
            @click="handleEmptyTrash"
            :disabled="trashEmails.length === 0"
          >
            清空垃圾箱
          </el-button>
        </div>
      </template>

      <el-table :data="trashEmails" v-loading="loading" style="width: 100%" empty-text="垃圾箱是空的">
        <el-table-column label="发件人/收件人" width="200">
          <template #default="scope">
            <!-- 尝试显示发件人，如果没有（比如是自己发的邮件在垃圾箱），则显示收件人 -->
            {{ scope.row.email.sender?.realName || scope.row.email.sender?.username || scope.row.email.toRecipients || 'N/A' }}
          </template>
        </el-table-column>
        <el-table-column label="主题" prop="email.subject" />
        <el-table-column label="时间" prop="email.createdTime" width="200" align="center" />
        <el-table-column label="操作" width="200" align="center">
          <template #default="scope">
            <el-button type="success" size="small" @click.stop="restoreEmail(scope.row)">恢复</el-button>
            <el-button type="danger" size="small" @click.stop="deletePermanently(scope.row)">彻底删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { 
  getTrashEmailsApi, 
  restoreFromTrashApi, 
  permanentDeleteApi, 
  emptyTrashApi // 引入新API
} from '@/api/email.js';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Delete } from '@element-plus/icons-vue'; // 引入Delete图标

const loading = ref(false);
const trashEmails = ref([]);

async function fetchTrashEmails() {
  loading.value = true;
  try {
    trashEmails.value = await getTrashEmailsApi();
  } catch (error) {
    console.error("获取垃圾箱邮件失败:", error);
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  fetchTrashEmails();
});

// 恢复邮件
async function restoreEmail(row) {
  try {
    await restoreFromTrashApi(row.id);
    ElMessage.success('邮件已恢复');
    // 从UI上移除，因为邮件已经不在垃圾箱了
    trashEmails.value = trashEmails.value.filter(item => item.id !== row.id);
  } catch (error) {
    ElMessage.error('恢复失败');
  }
}

// 彻底删除单个邮件
function deletePermanently(row) {
  ElMessageBox.confirm(`此操作将永久删除邮件 "${row.email.subject}"，无法恢复。是否继续？`, '永久删除确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'error', // 使用 error 类型，更醒目
  }).then(async () => {
    try {
      await permanentDeleteApi(row.id);
      ElMessage.success('邮件已永久删除');
      trashEmails.value = trashEmails.value.filter(item => item.id !== row.id);
    } catch (error) {
      ElMessage.error('删除失败');
    }
  }).catch(() => {
    // 用户点击了取消
  });
}

// 清空垃圾箱
function handleEmptyTrash() {
  ElMessageBox.confirm('此操作将永久删除垃圾箱中的所有邮件，且无法恢复。您确定要清空垃圾箱吗？', '清空垃圾箱警告', {
    confirmButtonText: '全部删除',
    cancelButtonText: '取消',
    type: 'error', // 使用 error 类型，更醒目
  }).then(async () => {
    try {
      await emptyTrashApi();
      ElMessage.success('垃圾箱已清空');
      // 直接清空前端数组，UI即时更新
      trashEmails.value = [];
    } catch (error) {
      ElMessage.error('操作失败');
    }
  }).catch(() => {
    // 用户点击了取消
  });
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}
</style>