<template>
  <div class="service-terms-root">
    <el-card class="terms-card">
      <template #header>
        <div class="card-header">
          <span>{{ serviceTerms.title || '服务条款' }}</span>
        </div>
      </template>
      <div v-loading="loading" class="terms-content" v-html="serviceTerms.content"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { getCurrentServiceTermsApi } from '@/api/public.js';
import { ElMessage } from 'element-plus';

const loading = ref(false);
const serviceTerms = ref({ title: '', content: '正在加载服务条款...' });

async function fetchTerms() {
  loading.value = true;
  try {
    const data = await getCurrentServiceTermsApi();
    serviceTerms.value = data || { title: '服务条款', content: '暂无内容' };
  } catch (e) {
    ElMessage.error('加载服务条款失败');
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  fetchTerms();
});
</script>

<style scoped>
.service-terms-root {
  max-width: 800px;
  margin: 40px auto;
  padding: 24px 0;
}
.terms-card {
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0,21,41,0.08);
}
.card-header {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}
.terms-content {
  font-size: 15px;
  color: #606266;
  line-height: 1.8;
  min-height: 200px;
  white-space: pre-wrap;
  word-break: break-all;
}
</style> 