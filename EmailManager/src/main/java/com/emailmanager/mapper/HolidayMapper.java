package com.emailmanager.mapper;

import com.emailmanager.entity.Holiday;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.util.List;

public interface HolidayMapper {
    int insert(Holiday holiday);
    Holiday findById(Long id);
    int update(Holiday holiday);
    int deleteById(Long id);
    List<Holiday> findAll();

    // 根据日期范围查询节假日
    List<Holiday> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // 根据具体日期查询
    Holiday findByDate(@Param("date") LocalDate date); // 确保 @Param 注解存在

    // 根据是否工作日查询
    List<Holiday> findByIsWorkday(@Param("isWorkday") Integer isWorkday);

    // 新增：根据年份查询节假日 (解决 HolidayServiceImpl 中的编译错误)
    List<Holiday> findByYear(@Param("year") Integer year); // 添加此方法
}
