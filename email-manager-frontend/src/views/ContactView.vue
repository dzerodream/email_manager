<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的通讯录</span>
          <div class="header-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="按姓名或邮箱搜索"
              clearable
              :prefix-icon="Search"
              style="width: 240px; margin-right: 10px;"
            />
            <el-button type="primary" :icon="Plus" @click="openDialog('add')">新增联系人</el-button>
          </div>
        </div>
      </template>

      <el-table :data="filteredContactList" v-loading="loading" style="width: 100%" empty-text="通讯录是空的，或者没有找到匹配的联系人">
        <el-table-column prop="name" label="姓名" width="180" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="phone" label="电话" width="180" />
        <el-table-column prop="birthday" label="生日" width="120" />
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column label="操作" width="150" align="center">
          <template #default="scope">
            <el-button type="primary" :icon="Edit" circle @click.stop="openDialog('edit', scope.row)" />
            <el-button type="danger" :icon="Delete" circle @click.stop="handleDelete(scope.row)" />
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑联系人对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      @close="resetFormAndClearValidation"
      :close-on-click-modal="false"
      draggable
    >
      <el-form
        ref="contactFormRef"
        :model="contactForm"
        :rules="contactRules"
        label-width="80px"
        label-position="right"
      >
        <el-form-item label="姓名" prop="name">
          <el-input v-model="contactForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="contactForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="contactForm.phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="生日" prop="birthday">
          <el-date-picker
            v-model="contactForm.birthday"
            type="date"
            placeholder="选择生日"
            value-format="YYYY-MM-DD" 
            style="width: 100%;"
            :editable="false" 
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="contactForm.remark" type="textarea" placeholder="请输入备注" :rows="3" />
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
import { getContactsApi, addContactApi, updateContactApi, deleteContactApi } from '@/api/contact.js';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Edit, Delete, Search } from '@element-plus/icons-vue';

const loading = ref(false);
const contactList = ref([]); // 原始的、从后端获取的完整联系人列表

// --- 搜索相关 ---
const searchKeyword = ref('');

// 计算属性：根据搜索关键词过滤联系人列表
const filteredContactList = computed(() => {
  const keyword = searchKeyword.value.toLowerCase().trim();
  if (!keyword) {
    return contactList.value; // 如果没有关键词，返回完整列表
  }
  return contactList.value.filter(contact => {
    const nameMatch = contact.name && contact.name.toLowerCase().includes(keyword);
    const emailMatch = contact.email && contact.email.toLowerCase().includes(keyword);
    // 可以根据需要添加对电话等其他字段的搜索
    // const phoneMatch = contact.phone && contact.phone.toLowerCase().includes(keyword);
    return nameMatch || emailMatch; // || phoneMatch
  });
});
// --- 搜索相关结束 ---

// --- 列表获取逻辑 ---
async function fetchContacts() {
  loading.value = true;
  try {
    contactList.value = await getContactsApi();
  } catch (error) {
    console.error('获取通讯录失败:', error);
    // 可以在这里给用户一个提示
    // ElMessage.error('加载联系人列表失败');
  } finally {
    loading.value = false;
  }
}
onMounted(() => {
  fetchContacts();
});

// --- 对话框和表单逻辑 ---
const dialogVisible = ref(false);
const submitLoading = ref(false);
const dialogMode = ref('add'); // 'add' 或 'edit'
const contactFormRef = ref(null); // 用于引用表单组件

const dialogTitle = computed(() => dialogMode.value === 'add' ? '新增联系人' : '编辑联系人');

// 创建一个响应式的表单数据对象
const contactForm = reactive({
  id: null,
  name: '',
  email: '',
  phone: '',
  birthday: '',
  remark: '',
});

// 表单校验规则
const contactRules = reactive({
  name: [{ required: true, message: '姓名不能为空', trigger: 'blur' }],
  email: [
    { required: true, message: '邮箱不能为空', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: ['blur', 'change'] },
  ],
  phone: [ // 电话设为非必填，但如果填了，可以加一个格式校验
    { pattern: /^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\d{8}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
});

// 打开对话框的函数
function openDialog(mode, rowData = null) {
  dialogMode.value = mode;
  if (mode === 'edit' && rowData) {
    // 编辑模式：将行数据深拷贝到表单对象中
    nextTick(() => { // 确保DOM更新后再填充，避免校验问题
        Object.assign(contactForm, rowData);
    });
  } else {
    // 新增模式或关闭后再次打开新增模式
    // 手动重置表单的每个字段，因为resetFields可能不会完全清空非初始定义的字段
    contactForm.id = null;
    contactForm.name = '';
    contactForm.email = '';
    contactForm.phone = '';
    contactForm.birthday = '';
    contactForm.remark = '';
    // 清除上一次的校验信息
    if(contactFormRef.value) {
        nextTick(() => { // 等待DOM更新后再清除校验
            contactFormRef.value.clearValidate();
        });
    }
  }
  dialogVisible.value = true;
}

// 关闭对话框时重置表单并清除校验状态
function resetFormAndClearValidation() {
  if (contactFormRef.value) {
    contactFormRef.value.resetFields(); // 重置表单项为初始值（rules中定义的初始值）
    contactFormRef.value.clearValidate(); // 清除校验信息
  }
  // 确保非表单项和响应式数据也完全清空
  contactForm.id = null;
  contactForm.name = '';
  contactForm.email = '';
  contactForm.phone = '';
  contactForm.birthday = '';
  contactForm.remark = '';
}

// 提交表单（新增或编辑）
async function handleSubmit() {
  if (!contactFormRef.value) return;
  await contactFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true;
      try {
        if (dialogMode.value === 'add') {
          const { id, ...addData } = contactForm; // 确保不传递id给新增接口
          await addContactApi(addData);
          ElMessage.success('新增成功！');
        } else {
          await updateContactApi(contactForm.id, contactForm);
          ElMessage.success('更新成功！');
        }
        dialogVisible.value = false;
        await fetchContacts(); // 重新获取列表数据
      } catch (error) {
        // 错误信息已由axios拦截器处理
        // ElMessage.error('操作失败，请稍后重试');
      } finally {
        submitLoading.value = false;
      }
    }
  });
}

// 删除联系人
function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除联系人 "${row.name}" 吗? 此操作无法撤销。`, '警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await deleteContactApi(row.id);
      ElMessage.success('删除成功！');
      await fetchContacts(); // 重新获取列表数据
    } catch (error) {
      // ElMessage.error('删除失败');
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
.header-actions {
  display: flex;
  align-items: center;
}
</style>