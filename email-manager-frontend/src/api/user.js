import request from '@/utils/request'

/**
 * 登录API函数
 * @param {object} data - 包含 username 和 password
 */
export function loginApi(data) {
  return request({
    url: '/user/login',
    method: 'post',
    data,
  })
}

/**
 * 用户注册API函数
 * @param {object} data - 用户注册信息
 */
export function registerApi(data) {
  return request({
    url: '/user/register', // 确保这个API是公共的，不需要Token认证
    method: 'post',
    data,
  })
}

/**
 * 获取当前登录用户的个人资料
 */
export function getProfileApi() {
  return request({
    url: '/user/profile',
    method: 'get'
  })
}

/**
 * 更新当前登录用户的个人资料
 * @param {object} data - 包含要更新的字段 (如 realName, phone)
 */
export function updateProfileApi(data) {
  return request({
    url: '/user/profile',
    method: 'put',
    data
  })
}

/**
 * 用户头像上传的API地址
 */
export const avatarUploadUrl = '/api/user/avatar/upload';

/**
 * 修改当前登录用户的密码
 * @param {object} data - 包含 oldPassword 和 newPassword
 */
export function changePasswordApi(data) {
  return request({
    url: '/user/password',
    method: 'put',
    data
  })
}