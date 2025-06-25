<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>收件箱 ({{ unreadCount }} 封未读)</span>
          <el-button type="primary" :icon="EditPen" @click="goToCompose">写邮件</el-button>
        </div>
      </template>

      <el-table 
        :data="inboxEmails" 
        v-loading="loading" 
        style="width: 100%" 
        @row-click="handleRowClick"
        :row-class-name="tableRowClassName"
      >
        
        <el-table-column width="50" align="center">
          <template #default="scope">
            <el-icon 
              class="star-icon" 
              :class="{ 'is-starred': scope.row.isStarred }" 
              @click.stop="toggleStarStatus(scope.row)"
            >
              <StarFilled />
            </el-icon>
          </template>
        </el-table-column>

        <el-table-column label="发件人" width="200">
          <template #default="scope">
            {{ scope.row.email.sender?.realName || scope.row.email.sender?.username || '未知发件人' }}
          </template>
        </el-table-column>
        
        <el-table-column label="主题">
          <template #default="scope">
            {{ scope.row.email.subject }}
          </template>
        </el-table-column>

        <el-table-column prop="email.createdTime" label="接收时间" width="200" align="center"/>
    
        <el-table-column label="操作" width="200" align="center">
          <template #default="scope">
            <el-tooltip :content="scope.row.isRead ? '标记为未读' : '标记为已读'" placement="top">
              <el-button :icon="scope.row.isRead ? Message : Open" circle @click.stop="toggleReadStatus(scope.row)" />
            </el-tooltip>
            <el-tooltip content="归档邮件" placement="top">
              <el-button :icon="Download" circle @click.stop="archiveEmail(scope.row)" />
            </el-tooltip>
            <el-tooltip content="移至垃圾箱" placement="top">
               <el-button :icon="Delete" circle type="danger" @click.stop="deleteEmail(scope.row)" />
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { 
  getInboxEmailsApi, 
  markAsReadApi, 
  markAsUnreadApi, 
  moveToTrashApi, 
  starEmailApi, 
  unstarEmailApi,
  archiveEmailApi // 引入归档API
} from '@/api/email.js';
import { EditPen, StarFilled, Message, Open, Download, Delete } from '@element-plus/icons-vue';

const router = useRouter();
const inboxEmails = ref([]);
const loading = ref(false);

const unreadCount = computed(() => inboxEmails.value.filter(email => !email.isRead).length);

async function fetchInboxEmails() {
  loading.value = true;
  try {
    inboxEmails.value = await getInboxEmailsApi();
  } catch (error) {
    console.error('获取收件箱失败:', error);
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  fetchInboxEmails();
});

function goToCompose() {
  router.push({ name: 'compose' });
}

function handleRowClick(row) {
  router.push({ name: 'email-detail', params: { id: row.id } });
}

const tableRowClassName = ({ row }) => !row.isRead ? 'unread-row' : '';

async function toggleReadStatus(row) {
  try {
    row.isRead ? await markAsUnreadApi(row.id) : await markAsReadApi(row.id);
    ElMessage.success(row.isRead ? '已标记为未读' : '已标记为已读');
    row.isRead = !row.isRead;
  } catch (error) { ElMessage.error('操作失败'); }
}

async function toggleStarStatus(row) {
  try {
    row.isStarred ? await unstarEmailApi(row.id) : await starEmailApi(row.id);
    ElMessage.success(row.isStarred ? '已取消星标' : '已添加星标');
    row.isStarred = !row.isStarred;
  } catch (error) { ElMessage.error('操作失败'); }
}

function deleteEmail(row) {
  ElMessageBox.confirm('确定要将这封邮件移至垃圾箱吗?', '提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning',
  }).then(async () => {
    try {
      await moveToTrashApi(row.id);
      ElMessage.success('已移至垃圾箱');
      inboxEmails.value = inboxEmails.value.filter(item => item.id !== row.id);
    } catch (error) { ElMessage.error('操作失败'); }
  }).catch(() => {});
}

// === 核心修改：实现归档逻辑 ===
async function archiveEmail(row) {
  try {
    await archiveEmailApi(row.id);
    ElMessage.success('邮件已归档');
    // 归档后，同样从收件箱列表中移除
    inboxEmails.value = inboxEmails.value.filter(item => item.id !== row.id);
  } catch (error) {
    ElMessage.error('归档失败');
  }
}
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; font-size: 18px; font-weight: bold; }
.star-icon { cursor: pointer; color: #c0c4cc; font-size: 18px; transition: color 0.2s; }
.star-icon:hover { color: #f7ba2a; }
.star-icon.is-starred { color: #f7ba2a; }
:deep(.el-table__row) { cursor: pointer; }
:deep(.unread-row) { font-weight: bold; color: #303133; }
</style>