<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>归档箱</span>
        </div>
      </template>
      <el-table :data="archivedEmails" v-loading="loading" style="width: 100%">
        <el-table-column label="发件人/收件人" width="200">
           <template #default="scope">{{ scope.row.email.sender?.realName || scope.row.email.toRecipients || 'N/A' }}</template>
        </el-table-column>
        <el-table-column label="主题" prop="email.subject" />
        <!-- 核心修改：统一显示邮件创建时间 -->
        <el-table-column label="时间" prop="email.createdTime" width="200" align="center"/>
        <el-table-column label="操作" width="200" align="center">
          <template #default="scope">
            <el-button type="success" size="small" @click.stop="unarchive(scope.row)">移回收件箱</el-button>
            <el-button type="danger" size="small" @click.stop="deleteEmail(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { getArchivedEmailsApi, unarchiveEmailApi, moveToTrashApi } from '@/api/email.js';
import { ElMessage, ElMessageBox } from 'element-plus';

const loading = ref(false);
const archivedEmails = ref([]);

async function fetchArchivedEmails() {
  loading.value = true;
  try {
    archivedEmails.value = await getArchivedEmailsApi();
  } catch (error) {
    console.error("获取归档邮件失败:", error);
  } finally {
    loading.value = false;
  }
}

onMounted(fetchArchivedEmails);

async function unarchive(row) {
  try {
    await unarchiveEmailApi(row.id);
    ElMessage.success('邮件已成功移回收件箱');
    fetchArchivedEmails();
  } catch (error) {
    ElMessage.error('操作失败');
  }
}

function deleteEmail(row) {
  ElMessageBox.confirm(`确定要将邮件 "${row.email.subject}" 移至垃圾箱吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await moveToTrashApi(row.id);
      ElMessage.success('邮件已移至垃圾箱');
      fetchArchivedEmails();
    } catch (error) {
      ElMessage.error('操作失败');
    }
  }).catch(() => {});
}
</script>

<style scoped>
.card-header { font-size: 18px; font-weight: bold; }
</style>