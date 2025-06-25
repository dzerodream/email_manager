package com.emailmanager.dto;

import com.emailmanager.entity.User;
import lombok.Data;

/**
 * 登录成功后返回给前端的数据传输对象
 */
@Data
public class LoginResponseDTO {

    /**
     * 生成的认证Token
     */
    private String token;

    /**
     * 用户的基本信息（不含密码）
     */
    private User userInfo;
}