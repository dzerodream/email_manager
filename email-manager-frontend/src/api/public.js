import request from '@/utils/request'

/**
 * 获取当前生效的服务条款
 */
export function getCurrentServiceTermsApi() {
  return request({
    url: '/public/service-terms/current',
    method: 'get'
  })
}

/**
 * 获取所有当前生效的系统公告
 */
export function getActiveAnnouncementsApi() {
  return request({
    url: '/public/announcements', // 对应后端的公共API
    method: 'get'
  })
}