import request from '@/utils/request'

// === 列表获取 API ===
export function getInboxEmailsApi() {
  return request({ url: '/emails/inbox', method: 'get' })
}
export function getSentEmailsApi() {
  return request({ url: '/emails/sent', method: 'get' })
}
export function getDraftsEmailsApi() {
  return request({ url: '/emails/drafts', method: 'get' })
}
export function getTrashEmailsApi() {
  return request({ url: '/emails/trash', method: 'get' })
}
export function getArchivedEmailsApi() {
  return request({ url: '/emails/archived', method: 'get' })
}

// === 邮件详情 API ===
export function getEmailDetailsApi(mailboxId) {
  return request({ url: `/emails/${mailboxId}`, method: 'get' })
}

// === 状态变更 API ===
export function markAsReadApi(mailboxId) {
  return request({ url: `/emails/${mailboxId}/read`, method: 'put' })
}
export function markAsUnreadApi(mailboxId) {
  return request({ url: `/emails/${mailboxId}/unread`, method: 'put' })
}
export function starEmailApi(mailboxId) {
  return request({ url: `/emails/${mailboxId}/star`, method: 'put' })
}
export function unstarEmailApi(mailboxId) {
  return request({ url: `/emails/${mailboxId}/unstar`, method: 'put' })
}
export function archiveEmailApi(mailboxId) {
  return request({ url: `/emails/${mailboxId}/archive`, method: 'put' })
}
export function unarchiveEmailApi(mailboxId) {
  return request({ url: `/emails/${mailboxId}/unarchive`, method: 'put' })
}

// === 移动/删除 API ===
export function moveToTrashApi(mailboxId) {
  return request({ url: `/emails/${mailboxId}/trash`, method: 'put' })
}
export function restoreFromTrashApi(mailboxId) {
  return request({ url: `/emails/${mailboxId}/restore`, method: 'put' })
}
export function permanentDeleteApi(mailboxId) {
  return request({ url: `/emails/${mailboxId}/permanent`, method: 'delete' })
}

// === 创建/发送 API ===
export function createDraftApi(emailData) {
  return request({ url: '/emails/drafts', method: 'post', data: emailData })
}
export function sendEmailApi(draftId, recipientData) {
  return request({ url: `/emails/${draftId}/send`, method: 'post', data: recipientData })
}

// === 附件上传 ===
export const attachmentUploadUrl = '/api/emails/attachments/upload';


/** 清空垃圾箱 */
export function emptyTrashApi() {
  return request({
    url: '/emails/trash/empty',
    method: 'delete'
  })
}

// src/api/email.js
// ...
/** 搜索邮件 */
export function searchEmailsApi(keyword) {
  return request({
    url: '/emails/search',
    method: 'get',
    params: { keyword } // GET请求的参数用params
  })
}