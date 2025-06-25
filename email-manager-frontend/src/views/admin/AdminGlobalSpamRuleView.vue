<template>
    <div class="admin-global-spam-rule-view-root">
      <el-page-header content="全局拦截规则管理" class="page-header" @back="goBack" />
  
      <el-card class="content-card-flex">
        <template #header>
          <div class="card-header">
            <span>全局规则列表</span>
            <el-button type="primary" :icon="Plus" @click="openRuleDialog('add')">新增全局规则</el-button>
          </div>
        </template>
  
        <el-table :data="ruleList" v-loading="loading" style="width: 100%; height: 100%;" border stripe empty-text="暂无全局拦截规则">
          <el-table-column prop="id" label="ID" width="80" align="center" sortable />
          <el-table-column prop="ruleName" label="规则名称" width="200" show-overflow-tooltip />
          <el-table-column prop="ruleType" label="类型" width="120" align="center">
            <template #default="scope">
              <el-tag :type="getRuleTypeTag(scope.row.ruleType)">{{ formatRuleType(scope.row.ruleType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="ruleContent" label="规则内容 (关键词/邮箱)" min-width="250" show-overflow-tooltip />
          <el-table-column label="状态" width="100" align="center">
            <template #default="scope">
              <el-switch
                v-model="scope.row.status"
                :active-value="1"
                :inactive-value="0"
                @change="handleStatusChange(scope.row)"
                :loading="scope.row.statusLoading"
                style="--el-switch-on-color: #13ce66; --el-switch-off-color: #ff4949"
              />
            </template>
          </el-table-column>
          <el-table-column prop="userId" label="创建者ID" width="100" align="center"/>
          <el-table-column prop="createdTime" label="创建时间" width="180" align="center" sortable/>
          <el-table-column label="操作" width="130" align="center" fixed="right">
            <template #default="scope">
              <el-tooltip content="编辑规则" placement="top">
                <el-button type="primary" :icon="Edit" circle size="small" @click.stop="openRuleDialog('edit', scope.row)" />
              </el-tooltip>
              <el-tooltip content="删除规则" placement="top">
                <el-button type="danger" :icon="Delete" circle size="small" @click.stop="handleDelete(scope.row)" />
              </el-tooltip>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
  
      <!-- 新增/编辑全局规则对话框 -->
      <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="500px"
        @close="resetFormAndClearValidation"
        :close-on-click-modal="false"
        draggable
        append-to-body
        destroy-on-close
      >
        <el-form ref="ruleFormRef" :model="ruleForm" :rules="ruleRules" label-width="100px" label-position="right">
          <el-form-item label="规则名称" prop="ruleName">
            <el-input v-model="ruleForm.ruleName" placeholder="例如：全局屏蔽广告词" />
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
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
        </template>
      </el-dialog>
    </div>
  </template>
  
  <script setup>
  import { ref, reactive, onMounted, computed, nextTick } from 'vue';
  import { useRouter } from 'vue-router';
  import { 
    adminGetGlobalSpamRulesApi, 
    adminAddGlobalSpamRuleApi, 
    adminUpdateGlobalSpamRuleApi, 
    adminDeleteGlobalSpamRuleApi,
    adminEnableGlobalSpamRuleApi,
    adminDisableGlobalSpamRuleApi
  } from '@/api/admin.js';
  import { ElMessage, ElMessageBox, ElSwitch } from 'element-plus';
  import { Plus, Edit, Delete } from '@element-plus/icons-vue';
  
  const router = useRouter();
  const loading = ref(false);
  const ruleList = ref([]);
  
  async function fetchRules() {
    loading.value = true;
    try {
      const data = await adminGetGlobalSpamRulesApi();
      ruleList.value = data ? data.map(rule => ({ ...rule, statusLoading: false })) : [];
    } catch (error) {
      console.error('获取全局拦截规则失败:', error);
      ElMessage.error('加载全局规则列表失败');
    } finally {
      loading.value = false;
    }
  }
  onMounted(fetchRules);
  
  const dialogVisible = ref(false);
  const submitLoading = ref(false);
  const dialogMode = ref('add');
  const ruleFormRef = ref(null);
  const dialogTitle = computed(() => dialogMode.value === 'add' ? '新增全局拦截规则' : '编辑全局拦截规则');
  
  const createEmptyForm = () => ({ id: null, ruleName: '', ruleType: '', ruleContent: '' });
  const ruleForm = reactive(createEmptyForm());
  
  const ruleRules = reactive({
    ruleName: [{ required: true, message: '规则名称不能为空', trigger: 'blur' }],
    ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
    ruleContent: [{ required: true, message: '规则内容不能为空', trigger: 'blur' }],
  });
  
  function openRuleDialog(mode, rowData = null) {
    dialogMode.value = mode;
    Object.assign(ruleForm, createEmptyForm()); 
    if (mode === 'edit' && rowData) {
      nextTick(() => { Object.assign(ruleForm, rowData); });
    }
    dialogVisible.value = true;
  }
  
  function resetFormAndClearValidation() {
    Object.assign(ruleForm, createEmptyForm());
    if (ruleFormRef.value) {
      nextTick(() => { ruleFormRef.value.clearValidate(); });
    }
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
            await adminAddGlobalSpamRuleApi(formData);
            ElMessage.success('新增全局规则成功！');
          } else {
            await adminUpdateGlobalSpamRuleApi(ruleForm.id, formData);
            ElMessage.success('更新全局规则成功！');
          }
          dialogVisible.value = false;
          await fetchRules();
        } catch (error) {
          console.error("提交全局规则表单失败:", error)
        } finally {
          submitLoading.value = false;
        }
      }
    });
  }
  
  async function handleDelete(row) {
    ElMessageBox.confirm(`确定要删除全局规则 "${row.ruleName || '此规则'}" 吗?`, '删除确认', 
      { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
    ).then(async () => {
      try {
        await adminDeleteGlobalSpamRuleApi(row.id);
        ElMessage.success('全局规则删除成功！');
        await fetchRules();
      } catch (error) {
         console.error("删除全局规则失败:", error)
      }
    }).catch(() => { ElMessage.info('已取消删除');});
  }
  
  async function handleStatusChange(row) {
    row.statusLoading = true;
    const newStatus = row.status;
    const actionText = newStatus === 1 ? '启用' : '禁用';
    try {
      const apiToCall = newStatus === 1 ? adminEnableGlobalSpamRuleApi : adminDisableGlobalSpamRuleApi;
      await apiToCall(row.id);
      ElMessage.success(`全局规则 "${row.ruleName || '此规则'}" 已${actionText}`);
    } catch (error) {
      ElMessage.error(`${actionText}全局规则失败`);
      row.status = newStatus === 1 ? 0 : 1;
    } finally {
      row.statusLoading = false;
    }
  }
  
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
  .admin-global-spam-rule-view-root {
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
  .header-actions {
    display: flex;
    align-items: center;
  }
  </style>