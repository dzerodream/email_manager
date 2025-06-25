<template>
  <div>
    <el-page-header @back="goBack" content="个人资料与密码设置" class="page-header" />

    <el-row :gutter="20">
      <!-- 基本信息卡片 -->
      <el-col :md="16" :sm="24">
        <el-card v-loading="loading" style="margin-bottom: 20px;">
          <template #header><span>基本信息</span></template>
          <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-width="100px">
            <el-form-item label="用户名">
              <el-input v-model="profileForm.username" disabled />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="profileForm.email" disabled />
            </el-form-item>
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="profileForm.realName" placeholder="请输入真实姓名" />
            </el-form-item>
            <el-form-item label="电话号码" prop="phone">
              <el-input v-model="profileForm.phone" placeholder="请输入电话号码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitProfile" :loading="submitProfileLoading">保存基本信息</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 头像设置卡片 -->
      <el-col :md="8" :sm="24">
        <el-card style="margin-bottom: 20px;">
          <template #header><span>头像设置</span></template>
          <div class="avatar-uploader-container">
            <el-upload
              class="avatar-uploader"
              :action="avatarUploadUrl" 
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :on-error="handleAvatarError"
              :before-upload="beforeAvatarUpload"
              name="file" 
              @change="() => { avatarError = false }"
            >
              <img v-if="(avatarPreviewUrl || (userStore.userInfo && userStore.userInfo.avatar)) && !avatarError" 
                   :src="avatarPreviewUrl || getFullAvatarUrl(userStore.userInfo.avatar)" 
                   class="avatar" 
                   alt="User Avatar"
                   @error="avatarError = true"
              />
              <el-icon v-else class="avatar-uploader-icon"><User /></el-icon>
            </el-upload>
            <div class="el-upload__tip">
              点击上方图片更换头像 (JPG/PNG, <2MB)
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 修改密码卡片 -->
    <el-row>
      <el-col :md="16" :sm="24"> 
           <el-card v-loading="loading">
              <template #header><span>修改密码</span></template>
              <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px">
                <el-form-item label="原密码" prop="oldPassword">
                  <el-input type="password" v-model="passwordForm.oldPassword" show-password placeholder="请输入原密码" />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input type="password" v-model="passwordForm.newPassword" show-password placeholder="请输入新密码 (6-20位字符)" />
                </el-form-item>
                <el-form-item label="确认新密码" prop="confirmPassword">
                  <el-input type="password" v-model="passwordForm.confirmPassword" show-password placeholder="请再次输入新密码" />
                </el-form-item>
                <el-form-item>
                  <el-button type="danger" @click="submitPasswordChange" :loading="submitPasswordLoading">确认修改密码</el-button>
                </el-form-item>
              </el-form>
          </el-card>
      </el-col>
    </el-row>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/stores/userStore';
import { getProfileApi, updateProfileApi, avatarUploadUrl, changePasswordApi } from '@/api/user.js';
import { Plus, User } from '@element-plus/icons-vue';

const router = useRouter();
const userStore = useUserStore();

const loading = ref(false);
const submitProfileLoading = ref(false);
const profileFormRef = ref(null);
const avatarPreviewUrl = ref('');
const uploadHeaders = computed(() => ({ Authorization: `Bearer ${userStore.token}` }));
const avatarError = ref(false);

const profileForm = reactive({
  id: null,
  username: '',
  email: '',
  realName: '',
  phone: '',
});
const profileRules = reactive({
  realName: [{ message: '真实姓名建议填写', trigger: 'blur' }],
  phone: [
    { pattern: /^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\d{8}$/, message: '请输入正确的电话号码格式', trigger: 'blur' }
  ]
});

const passwordFormRef = ref(null);
const submitPasswordLoading = ref(false);
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
});
const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入新密码'));
  } else if (value !== passwordForm.newPassword) {
    callback(new Error("两次输入的新密码不一致!"));
  } else {
    callback();
  }
};
const passwordRules = reactive({
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应为 6 到 20 个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
});

async function fetchProfileData() {
  loading.value = true;
  try {
    const data = await getProfileApi();
    profileForm.id = data.id;
    profileForm.username = data.username;
    profileForm.email = data.email;
    profileForm.realName = data.realName || '';
    profileForm.phone = data.phone || '';

    if (userStore.userInfo) {
        userStore.userInfo = { ...userStore.userInfo, ...data };
    } else {
        userStore.userInfo = data;
    }
    localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo));
    avatarPreviewUrl.value = '';
  } catch (error) {
    console.error('获取个人资料失败:', error);
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  fetchProfileData();
});

async function submitProfile() {
  if (!profileFormRef.value) return;
  await profileFormRef.value.validate(async (valid) => {
    if (valid) {
      submitProfileLoading.value = true;
      try {
        const dataToUpdate = {
          realName: profileForm.realName,
          phone: profileForm.phone,
        };
        await updateProfileApi(dataToUpdate);
        ElMessage.success('个人资料更新成功！');
        await fetchProfileData(); 
      } catch (error) {
        // 错误已由拦截器处理
      } finally {
        submitProfileLoading.value = false;
      }
    }
  });
}

function getFullAvatarUrl(relativePath) {
  if (!relativePath) return '';
  return `/api/files/download${relativePath.startsWith('/') ? relativePath : '/' + relativePath}`;
}
function handleAvatarSuccess(response, uploadFile) {
  ElMessage.success('头像上传成功!');
  const newAvatarPath = response.data;
  if (userStore.userInfo) {
    userStore.userInfo.avatar = newAvatarPath;
    localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo));
  }
  avatarPreviewUrl.value = '';
}
function handleAvatarError(error, uploadFile) {
  try {
    const errData = JSON.parse(error.message);
    ElMessage.error(errData.message || `${uploadFile.name} 上传失败`);
  } catch (e) {
     ElMessage.error(`${uploadFile.name} 上传失败，请检查网络或文件格式。`);
  }
}
function beforeAvatarUpload(rawFile) {
  const isJPGorPNG = rawFile.type === 'image/jpeg' || rawFile.type === 'image/png';
  const isLt2M = rawFile.size / 1024 / 1024 < 2;
  if (!isJPGorPNG) ElMessage.error('头像图片只能是 JPG 或 PNG 格式!');
  if (!isLt2M) ElMessage.error('头像图片大小不能超过 2MB!');
  if (isJPGorPNG && isLt2M) avatarPreviewUrl.value = URL.createObjectURL(rawFile);
  return isJPGorPNG && isLt2M;
}

async function submitPasswordChange() {
  if (!passwordFormRef.value) return;
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      submitPasswordLoading.value = true;
      try {
        await changePasswordApi({
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword,
        });
        
        // 先提示用户密码修改成功
        ElMessage.success('密码修改成功，您需要重新登录!');
        
        // 清空密码表单
        if (passwordFormRef.value) {
            passwordFormRef.value.resetFields();
        }
        
        // 然后执行登出操作（登出函数内部会处理路由跳转）
        // 传入 false 参数，告诉 logout 函数不要再显示 "已成功退出" 的消息
        userStore.logout(false); 

      } catch (error) {
         // 如果 changePasswordApi 失败（例如旧密码错误），
         // 错误消息会由 axios 拦截器自动弹出
         console.error('修改密码失败:', error);
      } finally {
        submitPasswordLoading.value = false;
      }
    }
  });
}

function goBack() {
  router.back();
}

// 保证每次切换头像或上传新头像时重置 avatarError
watch([avatarPreviewUrl, () => userStore.userInfo?.avatar], () => { avatarError.value = false; });
</script>

<style scoped>
.page-header {
  background-color: #fff;
  padding: 16px 24px;
  border-radius: 4px;
  margin-bottom: 20px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}
.avatar-uploader-container {
  text-align: center;
  padding: 20px;
}
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 50%;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
  width: 120px;
  height: 120px;
  display: inline-flex;
  justify-content: center;
  align-items: center;
}
.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}
.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}
.avatar {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
  border-radius: 50%;
}
.el-upload__tip {
  font-size: 12px;
  color: #909399;
  margin-top: 10px;
}
</style>