import request from '@/utils/request'

// === 用户管理 API (管理员) ===
export function getAllUsersApi() {
  return request({ url: '/admin/users', method: 'get' })
}
export function adminCreateUserApi(userData) {
  return request({ url: '/admin/users', method: 'post', data: userData })
}
export function adminUpdateUserApi(userId, userData) {
  return request({ url: `/admin/users/${userId}/profile`, method: 'put', data: userData })
}
export function adminUpdateUserStatusApi(userId, status) {
  return request({ url: `/admin/users/${userId}/status`, method: 'put', data: { status } })
}
export function adminDeleteUserApi(userId) {
  return request({ url: `/admin/users/${userId}`, method: 'delete' })
}

// === 公告管理 API (管理员) ===
export function adminGetAllAnnouncementsApi() {
  return request({ url: '/admin/announcements', method: 'get' })
}
export function adminCreateAnnouncementApi(announcementData) {
  return request({ url: '/admin/announcements', method: 'post', data: announcementData })
}
export function adminUpdateAnnouncementApi(id, announcementData) {
  return request({ url: `/admin/announcements/${id}`, method: 'put', data: announcementData })
}
export function adminDeleteAnnouncementApi(id) {
  return request({ url: `/admin/announcements/${id}`, method: 'delete' })
}

// === 服务条款管理 API (管理员) ===
export function adminGetServiceTermsApi() {
  return request({ url: '/admin/service-terms', method: 'get' })
}
export function adminPublishServiceTermsApi(termsData) {
  return request({ url: '/admin/service-terms/publish', method: 'post', data: termsData })
}
export function adminUpdateServiceTermsApi(id, termsData) {
  return request({ url: `/admin/service-terms/${id}`, method: 'put', data: termsData })
}
export function adminUpdateServiceTermStatusApi(id, status) {
    return request({ url: `/admin/service-terms/${id}/status`, method: 'put', data: { status } })
}

// === 全局垃圾邮件规则管理 API (管理员) ===
export function adminGetGlobalSpamRulesApi() {
  return request({
    url: '/admin/spam-rules/global',
    method: 'get',
  })
}
export function adminAddGlobalSpamRuleApi(ruleData) {
  return request({
    url: '/admin/spam-rules/global',
    method: 'post',
    data: ruleData,
  })
}
export function adminUpdateGlobalSpamRuleApi(id, ruleData) {
  return request({
    url: `/admin/spam-rules/global/${id}`, // 确保这里的URL与后端Controller匹配
    method: 'put',
    data: ruleData,
  })
}
export function adminDeleteGlobalSpamRuleApi(id) {
  return request({
    url: `/admin/spam-rules/global/${id}`, // 确保这里的URL与后端Controller匹配
    method: 'delete',
  })
}
export function adminEnableGlobalSpamRuleApi(id) {
  return request({
    url: `/admin/spam-rules/global/${id}/enable`, // 确保这里的URL与后端Controller匹配
    method: 'put',
  })
}
export function adminDisableGlobalSpamRuleApi(id) {
  return request({
    url: `/admin/spam-rules/global/${id}/disable`, // 确保这里的URL与后端Controller匹配
    method: 'put',
  })
}

/**
 * 管理员根据年份获取节假日列表
 * @param {number} year 
 */
export function adminGetHolidaysByYearApi(year) {
  return request({
    url: '/admin/holidays',
    method: 'get',
    params: { year }
  })
}

/**
 * 管理员添加新节假日
 * @param {object} holidayData - { name, holidayDate, isWorkday, description }
 */
export function adminAddHolidayApi(holidayData) {
  return request({
    url: '/admin/holidays',
    method: 'post',
    data: holidayData,
  })
}

/**
 * 管理员更新节假日
 * @param {number} id 
 * @param {object} holidayData 
 */
export function adminUpdateHolidayApi(id, holidayData) {
  return request({
    url: `/admin/holidays/${id}`,
    method: 'put',
    data: holidayData,
  })
}

/**
 * 管理员删除节假日
 * @param {number} id 
 */
export function adminDeleteHolidayApi(id) {
  return request({
    url: `/admin/holidays/${id}`,
    method: 'delete',
  })
}