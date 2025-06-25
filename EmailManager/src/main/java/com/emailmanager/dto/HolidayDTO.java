package com.emailmanager.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class HolidayDTO {
    private String name; // 字段名与JSON的 'name' 对应
    private LocalDate holidayDate;
    private Integer isWorkday;
    private String description;
}