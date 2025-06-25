import request from '@/utils/request'

/**
 * 获取当前用户的所有联系人
 */
export function getContactsApi() {
  return request({
    url: '/contacts',
    method: 'get',
  })
}

// 我们先把后续会用到的API函数也定义好
/**
 * 添加一个新联系人
 * @param {object} contactData 
 */
export function addContactApi(contactData) {
  return request({
    url: '/contacts',
    method: 'post',
    data: contactData,
  })
}

/**
 * 更新一个联系人
 * @param {number} id 联系人ID
 * @param {object} contactData 
 */
export function updateContactApi(id, contactData) {
  return request({
    url: `/contacts/${id}`,
    method: 'put',
    data: contactData,
  })
}

/**
 * 删除一个联系人
 * @param {number} id 联系人ID
 */
export function deleteContactApi(id) {
  return request({
    url: `/contacts/${id}`,
    method: 'delete',
  })
}