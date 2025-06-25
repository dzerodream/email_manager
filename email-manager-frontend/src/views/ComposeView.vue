<template>
  <div>
    <el-page-header @back="goBack" :content="pageTitle" class="page-header" />

    <el-card v-loading="pageLoading">
      <el-form ref="emailFormRef" :model="emailForm" :rules="emailRules" label-position="top" label-width="80px">
        
        <el-form-item label="收件人" prop="to">
          <el-select
            v-model="emailForm.to"
            multiple
            filterable
            allow-create
            default-first-option
            :multiple-limit="10"
            placeholder="请选择或输入收件人邮箱，可按回车创建"
            no-data-text="没有匹配的联系人"
            style="width: 100%;"
          >
            <el-option-group
              v-for="group in contactOptions"
              :key="group.label"
              :label="group.label"
            >
              <el-option
                v-for="item in group.options"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              >
                <span style="float: left">{{ item.label }}</span>
                <span style="float: right; color: var(--el-text-color-secondary); font-size: 13px;">
                  {{ item.value }}
                </span>
              </el-option>
            </el-option-group>
          </el-select>
        </el-form-item>

        <el-form-item label="主题" prop="subject">
          <el-input v-model="emailForm.subject" placeholder="邮件主题" />
        </el-form-item>
        
        <el-form-item label="附件">
          <el-upload
            ref="uploadRef"
            class="upload-demo"
            :action="attachmentUploadUrl"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :on-remove="handleRemove"
            :before-upload="handleBeforeUpload"
            :file-list="fileList"
            multiple
          >
            <el-button type="primary" :icon="UploadFilled">点击上传附件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                单个文件不超过 10MB
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item label="正文" prop="content">
          <div style="border: 1px solid #ccc; width: 100%;">
            <Toolbar
              style="border-bottom: 1px solid #ccc"
              :editor="editorRef"
              :defaultConfig="toolbarConfig"
              mode="default"
            />
            <Editor
              style="height: 400px; overflow-y: hidden;"
              v-model="emailForm.content"
              :defaultConfig="editorConfig"
              mode="default"
              @onCreated="handleCreated"
            />
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="sendEmail" :loading="sending">发送</el-button>
          <el-button @click="saveDraft" :loading="savingDraft">存草稿</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, shallowRef, onBeforeUnmount, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage,ElMessageBox } from 'element-plus';
import '@wangeditor/editor/dist/css/style.css'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import { 
  createDraftApi, 
  sendEmailApi, 
  attachmentUploadUrl,
  getEmailDetailsApi, 
  permanentDeleteApi 
} from '@/api/email.js';
import { getContactsApi } from '@/api/contact.js';
import { useUserStore } from '@/stores/userStore';
import { UploadFilled } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const editorRef = shallowRef(null);
const toolbarConfig = {};
const editorConfig = { placeholder: '请输入邮件内容...' };
onBeforeUnmount(() => {
  const editor = editorRef.value;
  if (editor == null) return;
  editor.destroy();
});
const handleCreated = (editor) => {
  editorRef.value = editor;
  // 如果是从日历或草稿加载，且有内容，则在编辑器创建后设置内容
  if (emailForm.content && (isEditMode.value || route.query.toEmail)) {
    editor.setHtml(emailForm.content)
  }
};

const emailFormRef = ref(null);
const sending = ref(false);
const savingDraft = ref(false);
const pageLoading = ref(false);
const isEditMode = ref(false);
const currentDraftMailboxId = ref(null);

const pageTitle = computed(() => isEditMode.value ? '编辑草稿' : '写邮件');

const emailForm = reactive({
  to: [],
  subject: '',
  content: '',
  isHtml: true,
  attachments: [],
});

const emailRules = reactive({
  to: [{ required: true, type: 'array', message: '收件人不能为空', trigger: 'change' }],
  subject: [{ required: true, message: '主题不能为空', trigger: 'blur' }],
});

const contactOptions = ref([]);
async function fetchContactsForSelect() {
  try {
    const contacts = await getContactsApi();
    const options = contacts.map(contact => ({
      value: contact.email,
      label: `${contact.name} <${contact.email}>`,
    }));
    contactOptions.value = [{ label: '我的联系人', options }];
  } catch (error) {
    console.error('加载联系人列表失败:', error);
  }
}

async function loadDraftData(mailboxIdToLoad) {
  pageLoading.value = true;
  try {
    const draftDetail = await getEmailDetailsApi(mailboxIdToLoad);
    if (draftDetail) {
      emailForm.subject = draftDetail.subject || '';
      emailForm.content = draftDetail.content || ''; // 先存到响应式数据
      
      if (draftDetail.toRecipients) {
        emailForm.to = draftDetail.toRecipients.split(',').map(s => s.trim()).filter(Boolean);
      } else {
        emailForm.to = [];
      }

      if (draftDetail.attachments && draftDetail.attachments.length > 0) {
        emailForm.attachments = draftDetail.attachments.map(att => ({ ...att }));
        fileList.value = draftDetail.attachments.map(att => ({
          name: att.fileName,
          url: '#',
          status: 'success',
          uid: att.id || Date.now() + Math.random(),
          server_response: { ...att }
        }));
      } else {
        emailForm.attachments = [];
        fileList.value = [];
      }
      isEditMode.value = true;
      currentDraftMailboxId.value = mailboxIdToLoad;
      
      // 如果编辑器已经创建，则设置内容，否则等待handleCreated回调
      if (editorRef.value) {
        editorRef.value.setHtml(emailForm.content);
      }

    } else {
      ElMessage.error('加载草稿数据失败');
    }
  } catch (error) {
    ElMessage.error('加载草稿内容时出错');
  } finally {
    pageLoading.value = false;
  }
}

onMounted(() => {
  fetchContactsForSelect();
  const query = route.query;
  if (query.draftMailboxId) {
    loadDraftData(query.draftMailboxId);
  } else if (query.toEmail) {
    if (query.toEmail) {
      emailForm.to = [query.toEmail];
    }
    if (query.subject) {
      emailForm.subject = query.subject;
    }
    // 如果有预设内容，也可以在这里设置 emailForm.content
    // 例如，生日祝福语
    if (query.subject && query.subject.includes('生日快乐')) {
        const contactName = query.subject.substring(query.subject.indexOf(' ') + 1, query.subject.indexOf(' 生日快乐！'));
        const greetingContent = `<p>亲爱的 ${contactName}，</p><p><br></p><p>祝您生日快乐，天天开心，万事如意！</p><p><br></p><p>顺颂时祺！</p>`;
        emailForm.content = greetingContent;
        if (editorRef.value) { // 确保编辑器已创建
            editorRef.value.setHtml(greetingContent);
        }
    }
  }
});

const uploadRef = ref(null);
const fileList = ref([]);
const uploadHeaders = computed(() => ({ Authorization: `Bearer ${userStore.token}` }));

function handleUploadSuccess(response, file, currentFileList) {
  ElMessage.success(`${file.name} 上传成功！`);
  const uploadedAttachment = response.data;
  emailForm.attachments.push(uploadedAttachment);
  const fileInList = currentFileList.find(f => f.uid === file.uid);
  if (fileInList) {
    fileInList.server_response = uploadedAttachment;
  }
}
function handleUploadError(error, file) {
  try {
    const errData = JSON.parse(error.message);
    ElMessage.error(errData.message || `${file.name} 上传失败`);
  } catch (e) {
     ElMessage.error(`${file.name} 上传失败，请检查网络或文件格式。`);
  }
  fileList.value = fileList.value.filter(f => f.uid !== file.uid);
}
function handleRemove(file, currentFileList) {
  const serverResponse = file.server_response;
  if (serverResponse) {
    const index = emailForm.attachments.findIndex(att => att.filePath === serverResponse.filePath);
    if (index !== -1) {
      emailForm.attachments.splice(index, 1);
    }
  }
  fileList.value = currentFileList;
}
function handleBeforeUpload(rawFile) {
  if (rawFile.size / 1024 / 1024 > 10) {
    ElMessage.error('附件大小不能超过 10MB!');
    return false;
  }
  return true;
}

function resetForm() {
  if (emailFormRef.value) emailFormRef.value.resetFields();
  if (editorRef.value) editorRef.value.setHtml('');
  if (uploadRef.value) uploadRef.value.clearFiles();
  emailForm.attachments = [];
  emailForm.to = [];
  isEditMode.value = false;
  currentDraftMailboxId.value = null;
  // 清除路由中的查询参数，避免下次进入时仍加载旧数据
  router.replace({ query: {} });
}

async function sendEmail() {
  await emailFormRef.value.validate(async (valid) => {
    if (valid) {
      sending.value = true;
      try {
        if (isEditMode.value && currentDraftMailboxId.value) {
          await permanentDeleteApi(currentDraftMailboxId.value);
        }
        const createData = {
          subject: emailForm.subject,
          content: emailForm.content,
          isHtml: emailForm.isHtml,
          attachments: emailForm.attachments,
        };
        const draftId = await createDraftApi(createData);
        await sendEmailApi(draftId, {
          toEmails: emailForm.to,
          ccEmails: [], bccEmails: [],
        });
        ElMessage.success('邮件发送成功！');
        resetForm();
        router.push({ name: 'sent' });
      } catch (error) {
        console.error('发送邮件时发生错误:', error);
      } finally {
        sending.value = false;
      }
    }
  });
}

async function saveDraft() {
  if (!emailForm.subject && !emailForm.content && emailForm.attachments.length === 0) {
    return ElMessage.warning('至少需要填写一项才能保存草稿');
  }
  savingDraft.value = true;
  try {
    if (isEditMode.value && currentDraftMailboxId.value) {
      await permanentDeleteApi(currentDraftMailboxId.value);
    }
    const createData = {
      subject: emailForm.subject,
      content: emailForm.content,
      isHtml: emailForm.isHtml,
      attachments: emailForm.attachments,
      // 如果需要保存收件人到草稿的Email实体本身，后端需要支持这个字段
      // toRecipients: emailForm.to.join('; ') // 假设后端Email实体有个toRecipients字段
    };
    await createDraftApi(createData);
    ElMessage.success('草稿保存成功！');
    resetForm();
    router.push({ name: 'drafts' });
  } catch (error) {
    console.error('保存草稿时发生错误:', error);
  } finally {
    savingDraft.value = false;
  }
}

function goBack() {
  // 如果是编辑模式，或表单有内容，可以提示用户是否放弃
  if (isEditMode.value || emailForm.subject || emailForm.content || emailForm.to.length > 0 || emailForm.attachments.length > 0) {
    ElMessageBox.confirm('邮件尚未保存，确定要离开吗？', '提示', {
      confirmButtonText: '确定离开',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      router.back();
    }).catch(() => {});
  } else {
    router.back();
  }
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