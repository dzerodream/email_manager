package com.emailmanager.service.impl;

import com.emailmanager.entity.Holiday;
import com.emailmanager.mapper.HolidayMapper;
import com.emailmanager.service.HolidayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime; // 导入 LocalDateTime
import java.util.List;

@Service
public class HolidayServiceImpl implements HolidayService {

    @Autowired
    private HolidayMapper holidayMapper;

    @Override
    @Transactional
    public boolean addHoliday(Holiday holiday) {
        // 修正：在插入前设置 createdTime 和 updatedTime
        holiday.setCreatedTime(LocalDateTime.now());
        holiday.setUpdatedTime(LocalDateTime.now());
        return holidayMapper.insert(holiday) > 0;
    }

    @Override
    @Transactional
    public boolean updateHoliday(Holiday holiday) {
        // 修正：在更新前设置 updatedTime
        holiday.setUpdatedTime(LocalDateTime.now());
        return holidayMapper.update(holiday) > 0;
    }

    @Override
    @Transactional
    public boolean deleteHoliday(Long id) {
        return holidayMapper.deleteById(id) > 0;
    }

    @Override
    public Holiday findById(Long id) {
        return holidayMapper.findById(id);
    }

    @Override
    public List<Holiday> findAll() {
        return holidayMapper.findAll();
    }

    @Override
    public List<Holiday> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return holidayMapper.findByDateRange(startDate, endDate);
    }

    @Override
    public List<Holiday> findByIsWorkday(Integer isWorkday) {
        return holidayMapper.findByIsWorkday(isWorkday);
    }

    @Override
    public boolean isHoliday(LocalDate date) {
        Holiday holiday = holidayMapper.findByDate(date);
        // 如果找到该日期有记录，并且 isWorkday == 0 (表示非工作日，即节假日)
        return holiday != null && holiday.getIsWorkday() == 0;
    }

    @Override
    public List<Holiday> findByYear(int year) {
        // 直接调用 Mapper 中的 findByYear 方法
        return holidayMapper.findByYear(year);
    }

    @Override
    public List<Holiday> findByYearAndMonth(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        return findByDateRange(startDate, endDate);
    }
}
