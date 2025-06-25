// 文件路径: com/emailmanager/dto/AdminUpdateUserDTO.java
package com.emailmanager.dto;

import lombok.Data;

/**
 * 用于管理员更新用户信息的DTO
 */
@Data
public class AdminUpdateUserDTO {
    // 管理员可以修改的字段
    private String realName;
    private String phone;
    private String avatar;
    private String role;   // 允许管理员修改角色 (USER/ADMIN)
    private Integer status; // 允许管理员修改状态 (0/1)
    private String email;

    // 注意：这里没有username和email，因为通常不建议修改这些唯一标识。
    // 也没有password，因为密码应该通过重置流程来处理。
}