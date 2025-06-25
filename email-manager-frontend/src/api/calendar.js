import request from '@/utils/request'

/**
 * 根据年份和月份查询节假日
 * @param {number} year 年份
 * @param {number} month 月份 (1-12)
 */
export function getHolidaysByMonthApi(year, month) {
  return request({
    url: '/calendar/holidays',
    method: 'get',
    params: { year, month }
  })
}

/**
 * 查询当前用户联系人中，指定月份过生日的人
 * @param {number} month 月份 (1-12)
 */
export function getBirthdaysByMonthApi(month) {
  return request({
    url: '/calendar/birthdays',
    method: 'get',
    params: { month }
  })
}

// 后续可以添加获取今日生日、即将生日的API
// export function getTodayBirthdaysApi() { ... }
// export function getUpcomingBirthdaysApi(days) { ... }