<template>
  <div>
    <el-page-header content="个人垃圾邮件规则" class="page-header" @back="goBack" />

    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的拦截规则</span>
          <el-button type="primary" :icon="Plus" @click="openDialog('add')">新增规则</el-button>
        </div>
      </template>

      <el-table :data="ruleList" v-loading="loading" style="width: 100%" empty-text="您还没有设置任何拦截规则">
        <el-table-column prop="ruleName" label="规则名称" width="200" />
        <el-table-column prop="ruleType" label="类型" width="120">
          <template #default="scope">
            <el-tag :type="getRuleTypeTag(scope.row.ruleType)">{{ formatRuleType(scope.row.ruleType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ruleContent" label="规则内容 (关键词/邮箱)" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
              :loading="scope.row.statusLoading"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="scope">
            <el-button type="primary" :icon="Edit" circle @click.stop="openDialog('edit', scope.row)" />
            <el-button type="danger" :icon="Delete" circle @click.stop="handleDelete(scope.row)" />
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑规则对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      @close="resetFormAndClearValidation"
      :close-on-click-modal="false"
      draggable
    >
      <el-form
        ref="ruleFormRef"
        :model="ruleForm"
        :rules="ruleRules"
        label-width="100px"
      >
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="ruleForm.ruleName" placeholder="例如：屏蔽广告邮件" />
        </el-form-item>
        <el-form-item label="规则类型" prop="ruleType">
          <el-select v-model="ruleForm.ruleType" placeholder="请选择规则类型" style="width: 100%;">
            <el-option label="发件人邮箱" value="SENDER" />
            <el-option label="邮件主题包含" value="SUBJECT" />
            <el-option label="邮件内容包含" value="CONTENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="规则内容" prop="ruleContent">
          <el-input v-model="ruleForm.ruleContent" placeholder="请输入关键词或邮箱地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { 
  getMySpamRulesApi, 
  addMySpamRuleApi, 
  updateMySpamRuleApi, 
  deleteMySpamRuleApi,
  enableMySpamRuleApi,
  disableMySpamRuleApi
} from '@/api/spamRule.js';
import { ElMessage, ElMessageBox, ElSwitch } from 'element-plus';
import { Plus, Edit, Delete } from '@element-plus/icons-vue';

const router = useRouter();
const loading = ref(false);
const ruleList = ref([]);

// --- 列表获取逻辑 ---
async function fetchRules() {
  loading.value = true;
  try {
    const data = await getMySpamRulesApi();
    // 为每一行数据添加一个 statusLoading 属性，用于控制Switch的加载状态
    ruleList.value = data.map(rule => ({ ...rule, statusLoading: false }));
  } catch (error) {
    console.error('获取垃圾邮件规则失败:', error);
  } finally {
    loading.value = false;
  }
}
onMounted(() => {
  fetchRules();
});

// --- 对话框和表单逻辑 ---
const dialogVisible = ref(false);
const submitLoading = ref(false);
const dialogMode = ref('add');
const ruleFormRef = ref(null);

const dialogTitle = computed(() => dialogMode.value === 'add' ? '新增拦截规则' : '编辑拦截规则');

const ruleForm = reactive({
  id: null,
  ruleName: '',
  ruleType: '', // 'SENDER', 'SUBJECT', 'CONTENT'
  ruleContent: '',
});

const ruleRules = reactive({
  ruleName: [{ required: true, message: '规则名称不能为空', trigger: 'blur' }],
  ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
  ruleContent: [{ required: true, message: '规则内容不能为空', trigger: 'blur' }],
});

function openDialog(mode, rowData = null) {
  dialogMode.value = mode;
  if (mode === 'edit' && rowData) {
    nextTick(() => { Object.assign(ruleForm, rowData); });
  } else {
    ruleForm.id = null; ruleForm.ruleName = ''; ruleForm.ruleType = ''; ruleForm.ruleContent = '';
    if(ruleFormRef.value) { nextTick(() => { ruleFormRef.value.clearValidate(); }); }
  }
  dialogVisible.value = true;
}

function resetFormAndClearValidation() {
  if (ruleFormRef.value) {
    ruleFormRef.value.resetFields();
    ruleFormRef.value.clearValidate();
  }
  ruleForm.id = null;
}

async function handleSubmit() {
  if (!ruleFormRef.value) return;
  await ruleFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true;
      try {
        const formData = { 
          ruleName: ruleForm.ruleName, 
          ruleType: ruleForm.ruleType, 
          ruleContent: ruleForm.ruleContent 
        };
        if (dialogMode.value === 'add') {
          await addMySpamRuleApi(formData);
          ElMessage.success('新增成功！');
        } else {
          await updateMySpamRuleApi(ruleForm.id, formData);
          ElMessage.success('更新成功！');
        }
        dialogVisible.value = false;
        await fetchRules();
      } finally {
        submitLoading.value = false;
      }
    }
  });
}

async function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除规则 "${row.ruleName || '此规则'}" 吗?`, '警告', {
    confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning',
  }).then(async () => {
    try {
      await deleteMySpamRuleApi(row.id);
      ElMessage.success('删除成功！');
      await fetchRules();
    } catch (error) { /* ... */ }
  }).catch(() => {});
}

// --- 状态切换逻辑 ---
async function handleStatusChange(row) {
  row.statusLoading = true;
  try {
    if (row.status === 1) { // 用户想要启用
      await enableMySpamRuleApi(row.id);
      ElMessage.success(`规则 "${row.ruleName || '此规则'}" 已启用`);
    } else { // 用户想要禁用
      await disableMySpamRuleApi(row.id);
      ElMessage.success(`规则 "${row.ruleName || '此规则'}" 已禁用`);
    }
    // 注意：API成功后，row.status 已经被v-model双向绑定更新了，无需手动修改
  } catch (error) {
    // 操作失败，把switch的状态恢复回去
    row.status = row.status === 1 ? 0 : 1; 
    ElMessage.error('状态切换失败');
  } finally {
    row.statusLoading = false;
  }
}

// --- UI辅助函数 ---
function formatRuleType(type) {
  const map = { SENDER: '发件人', SUBJECT: '主题', CONTENT: '内容' };
  return map[type] || type;
}
function getRuleTypeTag(type) {
  const map = { SENDER: 'primary', SUBJECT: 'success', CONTENT: 'warning' };
  return map[type] || 'info';
}

function goBack() {
  router.back();
}
</script>

<style scoped>
.page-header { /* ... */ }
.card-header { display: flex; justify-content: space-between; align-items: center; font-size: 18px; font-weight: bold; }
</style>