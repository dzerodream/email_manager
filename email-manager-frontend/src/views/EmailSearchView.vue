<template>
  <div>
    <el-page-header @back="goBack" class="page-header">
      <template #content>
        <span class="text-large font-600 mr-3">搜索结果： "{{ currentKeyword }}"</span>
      </template>
    </el-page-header>

    <el-card>
      <el-table :data="searchResults" v-loading="loading" style="width: 100%" @row-click="handleRowClick" empty-text="没有找到相关邮件">
        <el-table-column width="50" align="center">
          <template #default="scope">
            <el-icon class="star-icon" :class="{ 'is-starred': scope.row.isStarred }"><StarFilled /></el-icon>
          </template>
        </el-table-column>
        <el-table-column label="发件人/收件人" width="200">
            <template #default="scope">
                {{ scope.row.email.sender?.realName || scope.row.email.sender?.username || scope.row.email.toRecipients || 'N/A' }}
            </template>
        </el-table-column>
        <el-table-column label="主题" prop="email.subject" />
        <el-table-column label="时间" prop="email.createdTime" width="200" align="center"/>
        <el-table-column label="所在文件夹" prop="folder" width="120" align="center">
            <template #default="scope">
                <el-tag size="small">{{ getFolderName(scope.row.folder) }}</el-tag>
            </template>
        </el-table-column>
      </el-table>
       <el-empty v-if="!loading && searchResults.length === 0 && currentKeyword" :description="`未找到与“${currentKeyword}”相关的邮件`" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { searchEmailsApi } from '@/api/email.js'; // 我们需要创建这个API函数
import { StarFilled } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const searchResults = ref([]);
const currentKeyword = ref('');

// 辅助函数：将文件夹代码转换为可读名称
function getFolderName(folderCode) {
  const folderMap = {
    INBOX: '收件箱',
    SENT: '已发送',
    DRAFT: '草稿箱',
    TRASH: '垃圾箱',
    ARCHIVE: '归档箱',
  };
  return folderMap[folderCode] || folderCode;
}

async function performSearch(keyword) {
  if (!keyword || keyword.trim() === '') {
    searchResults.value = [];
    return;
  }
  currentKeyword.value = keyword;
  loading.value = true;
  try {
    searchResults.value = await searchEmailsApi(keyword);
  } catch (error) {
    console.error('搜索邮件失败:', error);
  } finally {
    loading.value = false;
  }
}

// 当路由的查询参数变化时（即用户执行了新的搜索），重新执行搜索
watch(() => route.query.keyword, (newKeyword) => {
  if (newKeyword) {
    performSearch(newKeyword);
  } else {
      searchResults.value = []; // 如果没有关键词，清空结果
      currentKeyword.value = '';
  }
}, { immediate: true }); // immediate: true 确保组件首次加载时也执行一次

function goBack() {
  router.back();
}

function handleRowClick(row) {
  router.push({ name: 'email-detail', params: { id: row.id } });
}
</script>

<style scoped>
.page-header {
  background-color: #fff;
  padding: 16px 24px;
  border-radius: 4px;
  margin-bottom: 20px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}
</style>