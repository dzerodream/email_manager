<template>
  <div>
    <el-page-header @back="goBack" class="page-header">
      <template #content>
        <span class="text-large font-600 mr-3"> 邮件详情 </span>
      </template>
    </el-page-header>

    <el-card v-if="emailDetail" v-loading="loading" class="email-card">
      <div class="email-header">
        <h2 class="email-subject">{{ emailDetail.subject }}</h2>
        <div class="email-meta">
          <div>
            <strong>发件人:</strong>
            <span>{{ emailDetail.sender?.realName || emailDetail.sender?.username }}</span>
            <span class="meta-email">
              <{{ emailDetail.sender?.email }}>
            </span>
          </div>
          <div>
            <strong>时间:</strong> {{ emailDetail.createdTime }}
          </div>
        </div>
      </div>

      <el-divider />

      <div class="email-content" v-html="emailDetail.content"></div>

      <div v-if="emailDetail.attachments && emailDetail.attachments.length > 0" class="attachment-section">
        <el-divider />
        <h3>附件 ({{ emailDetail.attachments.length }})</h3>
        <ul class="attachment-list">
          <li v-for="att in emailDetail.attachments" :key="att.id">
            <el-link :icon="Paperclip" :href="getAttachmentUrl(att.filePath)" target="_blank" type="primary">
              {{ att.fileName }} ({{ formatFileSize(att.fileSize) }})
            </el-link>
          </li>
        </ul>
      </div>
    </el-card>

    <el-empty v-else-if="!loading" description="邮件不存在或加载失败" />

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getEmailDetailsApi } from '@/api/email.js';
import { Paperclip } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const emailDetail = ref(null);

function goBack() {
  router.back();
}

// 构造附件的下载URL
function getAttachmentUrl(filePath) {
  if (!filePath) return '#';
  // filePath 的格式是 /yyyy/MM/dd/filename.ext
  // 我们需要移除开头的斜杠，然后拼接到下载路径上
  const cleanPath = filePath.startsWith('/') ? filePath.substring(1) : filePath;
  // 最终生成的URL会是 /api/files/download/yyyy/MM/dd/filename.ext
  // Vite会代理到 http://localhost:8080/EmailManager/files/download/...
  return `/api/files/download/${cleanPath}`;
}

function formatFileSize(bytes) {
  if (!bytes || bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

onMounted(async () => {
  const mailboxId = route.params.id;
  if (!mailboxId) {
    console.error('在路由中未找到邮件ID');
    return;
  }
  loading.value = true;
  try {
    emailDetail.value = await getEmailDetailsApi(mailboxId);
  } catch (error) {
    console.error('获取邮件详情失败:', error);
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
.page-header {
  background-color: #fff;
  padding: 16px 24px;
  border-radius: 4px;
  margin-bottom: 20px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.email-card {
  border-radius: 4px;
}

.email-header {
  line-height: 1.6;
}

.email-subject {
  margin: 0 0 15px 0;
  font-size: 24px;
  font-weight: 600;
}

.email-meta {
  font-size: 14px;
  color: #606266;
}

.meta-email {
  margin-left: 5px;
  color: #909399;
}

.email-content {
  padding: 20px 0;
  line-height: 1.8;
  font-size: 16px;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.attachment-section h3 {
  margin-bottom: 10px;
  font-size: 16px;
  color: #303133;
}

.attachment-list {
  list-style: none;
  padding: 0;
}

.attachment-list li {
  margin-bottom: 8px;
}
</style>