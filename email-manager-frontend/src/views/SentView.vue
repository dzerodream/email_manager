<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>已发送</span>
          <el-button type="primary" :icon="EditPen" @click="goToCompose">写邮件</el-button>
        </div>
      </template>

      <el-table :data="sentEmails" v-loading="loading" style="width: 100%" @row-click="viewEmail">
        
        <el-table-column label="收件人" prop="email.toRecipients" />
        
        <el-table-column label="主题" prop="email.subject" />
        
        <!-- 核心修改：使用 email.createdTime 作为时间戳 -->
        <el-table-column label="发送时间" prop="email.createdTime" width="200" align="center" />
        
        <el-table-column label="操作" width="150" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" @click.stop="viewEmail(scope.row)">查看</el-button>
            <el-button type="danger" size="small" @click.stop="deleteEmail(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getSentEmailsApi, moveToTrashApi } from '@/api/email.js';
import { ElMessage, ElMessageBox } from 'element-plus';
import { EditPen } from '@element-plus/icons-vue';

const router = useRouter();
const loading = ref(false);
const sentEmails = ref([]);

async function fetchSentEmails() {
  loading.value = true;
  try {
    sentEmails.value = await getSentEmailsApi();
  } catch (error) {
    console.error('获取已发送邮件失败:', error);
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  fetchSentEmails();
});

function goToCompose() {
  router.push({ name: 'compose' });
}

function viewEmail(row) {
  router.push({ name: 'email-detail', params: { id: row.id } });
}

function deleteEmail(row) {
  ElMessageBox.confirm(`确定要将这封邮件移至垃圾箱吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await moveToTrashApi(row.id);
      ElMessage.success('已移至垃圾箱');
      sentEmails.value = sentEmails.value.filter(item => item.id !== row.id);
    } catch (error) {
      ElMessage.error('操作失败');
    }
  }).catch(() => {});
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