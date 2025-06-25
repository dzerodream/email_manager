package com.emailmanager.service;

import com.emailmanager.entity.Holiday;
import java.time.LocalDate;
import java.util.List;

/**
 * 节假日服务接口
 */
public interface HolidayService {
    /**
     * 添加节假日
     * @param holiday 节假日信息
     * @return 是否添加成功
     */
    boolean addHoliday(Holiday holiday);

    /**
     * 更新节假日信息
     * @param holiday 节假日信息
     * @return 是否更新成功
     */
    boolean updateHoliday(Holiday holiday);

    /**
     * 删除节假日
     * @param id 节假日ID
     * @return 是否删除成功
     */
    boolean deleteHoliday(Long id);

    /**
     * 根据ID查询节假日
     * @param id 节假日ID
     * @return 节假日信息
     */
    Holiday findById(Long id);

    /**
     * 查询所有节假日
     * @return 节假日列表
     */
    List<Holiday> findAll();

    /**
     * 根据日期范围查询节假日
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 节假日列表
     */
    List<Holiday> findByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * 检查指定日期是否为节假日
     * @param date 日期
     * @return 是否为节假日
     */
    boolean isHoliday(LocalDate date);

    /**
     * 获取指定年份的所有节假日
     * @param year 年份
     * @return 节假日列表
     */
    List<Holiday> findByYear(int year);

    /**
     * 获取指定月份的节假日
     * @param year 年份
     * @param month 月份
     * @return 节假日列表
     */
    List<Holiday> findByYearAndMonth(int year, int month);

    /**
     * 根据是否工作日查询节假日
     * @param isWorkday 是否工作日（0-否，1-是）
     * @return 节假日列表
     */
    List<Holiday> findByIsWorkday(Integer isWorkday);
} 