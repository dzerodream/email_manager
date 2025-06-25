// 文件路径: com/emailmanager/service/impl/HolidayServiceImplTest.java
package com.emailmanager.service.impl;

import com.emailmanager.entity.Holiday;
import com.emailmanager.mapper.HolidayMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HolidayServiceImplTest {

    @Mock
    private HolidayMapper holidayMapper;

    @InjectMocks
    private HolidayServiceImpl holidayService;

    @Test
    @DisplayName("检查日期是否为节假日 - 是")
    void testIsHoliday_True() {
        LocalDate date = LocalDate.of(2025, 10, 1);
        Holiday holiday = new Holiday();
        holiday.setIsWorkday(0); // 0 表示假日
        when(holidayMapper.findByDate(date)).thenReturn(holiday);

        assertTrue(holidayService.isHoliday(date));
    }

    @Test
    @DisplayName("检查日期是否为节假日 - 是调休上班日")
    void testIsHoliday_False_IsWorkday() {
        LocalDate date = LocalDate.of(2025, 9, 28);
        Holiday holiday = new Holiday();
        holiday.setIsWorkday(1); // 1 表示工作日
        when(holidayMapper.findByDate(date)).thenReturn(holiday);

        assertFalse(holidayService.isHoliday(date));
    }

    @Test
    @DisplayName("检查日期是否为节假日 - 是普通工作日")
    void testIsHoliday_False_NormalDay() {
        LocalDate date = LocalDate.of(2025, 11, 11);
        when(holidayMapper.findByDate(date)).thenReturn(null); // 数据库没记录

        assertFalse(holidayService.isHoliday(date));
    }
}