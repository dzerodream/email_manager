<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>草稿箱</span>
          <el-button type="primary" :icon="EditPen" @click="goToCompose">写邮件</el-button>
        </div>
      </template>

      <el-table :data="draftEmails" v-loading="loading" style="width: 100%">
        <el-table-column label="收件人（草稿）">
          <template #default="scope">{{ formatDraftTo(scope.row.email?.toRecipients) }}</template>
        </el-table-column>
        <el-table-column label="主题" prop="email.subject" />
        <el-table-column label="最后修改时间" prop="email.updatedTime" width="200" align="center" />
        <el-table-column label="操作" width="150" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" @click.stop="editDraft(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click.stop="deleteDraft(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getDraftsEmailsApi, permanentDeleteApi } from '@/api/email.js';
import { ElMessage, ElMessageBox } from 'element-plus';
import { EditPen } from '@element-plus/icons-vue';

const router = useRouter();
const loading = ref(false);
const draftEmails = ref([]);

async function fetchDraftEmails() {
  loading.value = true;
  try {
    draftEmails.value = await getDraftsEmailsApi();
  } catch (error) {
    console.error("获取草稿箱失败:", error);
  } finally {
    loading.value = false;
  }
}
onMounted(fetchDraftEmails);

function formatDraftTo(toRecipientsStr) {
    if (!toRecipientsStr || toRecipientsStr.trim() === '') {
        return '(无收件人)';
    }
    // 草稿的收件人可能还没有被正式记录到email_recipients表，
    // 后端 findWithAttachmentsById 可能不会返回 toRecipients 字段
    // 所以这里我们先做一个简单的显示
    // 理想情况下，创建草稿时，也应该把收件人信息（即使是文本）存入emails表某个字段
    return toRecipientsStr.split(',')[0] + (toRecipientsStr.includes(',') ? ' 等' : '');
}

function goToCompose() { router.push({ name: 'compose' }); }

function editDraft(row) {
  router.push({ 
    name: 'compose', 
    query: { 
      draftMailboxId: row.id, // 这是Mailbox表的ID
      draftEmailId: row.emailId  // 这是Email表的ID
    } 
  });
}

function deleteDraft(row) {
  ElMessageBox.confirm('确定要删除此草稿吗？此操作无法恢复。', '删除草稿', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await permanentDeleteApi(row.id); // 删除草稿也是永久删除其mailbox条目
      ElMessage.success('草稿已删除');
      draftEmails.value = draftEmails.value.filter(item => item.id !== row.id);
    } catch (error) {
      ElMessage.error('删除失败');
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