package com.emailmanager.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ContactDTO {
    // Contact的所有可编辑字段
    private String name;
    private String email;
    private String phone;
    private LocalDate birthday;
    private String remark;
}