import request from '@/utils/request'

// === 个人垃圾邮件规则 API ===

/**
 * 获取当前用户的个人垃圾邮件规则列表
 */
export function getMySpamRulesApi() {
  return request({
    url: '/spam-rules',
    method: 'get',
  })
}

/**
 * 添加一条新的个人垃圾邮件规则
 * @param {object} ruleData - { ruleName, ruleType, ruleContent }
 */
export function addMySpamRuleApi(ruleData) {
  return request({
    url: '/spam-rules',
    method: 'post',
    data: ruleData,
  })
}

/**
 * 更新一条个人垃圾邮件规则
 * @param {number} id - 规则ID
 * @param {object} ruleData - { ruleName, ruleType, ruleContent }
 */
export function updateMySpamRuleApi(id, ruleData) {
  return request({
    url: `/spam-rules/${id}`,
    method: 'put',
    data: ruleData,
  })
}

/**
 * 删除一条个人垃圾邮件规则
 * @param {number} id - 规则ID
 */
export function deleteMySpamRuleApi(id) {
  return request({
    url: `/spam-rules/${id}`,
    method: 'delete',
  })
}

/**
 * 启用一条个人垃圾邮件规则
 * @param {number} id - 规则ID
 */
export function enableMySpamRuleApi(id) {
  return request({
    url: `/spam-rules/${id}/enable`,
    method: 'put',
  })
}

/**
 * 禁用一条个人垃圾邮件规则
 * @param {number} id - 规则ID
 */
export function disableMySpamRuleApi(id) {
  return request({
    url: `/spam-rules/${id}/disable`,
    method: 'put',
  })
}