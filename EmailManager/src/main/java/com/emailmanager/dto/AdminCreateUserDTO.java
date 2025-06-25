package com.emailmanager.dto;

import lombok.Data;

/**
 * 用于管理员创建新用户的DTO
 */
@Data
public class AdminCreateUserDTO {
    private String username;
    private String password; // 管理员直接设置初始密码
    private String email;
    private String realName;
    private String phone;
    private String role;   // 可以指定角色, e.g., "USER" or "ADMIN"
    private Integer status; // 可以指定初始状态, e.g., 0 (禁用) or 1 (启用)
}