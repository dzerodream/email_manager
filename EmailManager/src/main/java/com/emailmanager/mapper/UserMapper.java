package com.emailmanager.mapper;

import com.emailmanager.entity.User;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface UserMapper {
    int insert(User user);
    User findById(Long id);
    int update(User user);
    int deleteById(Long id);
    List<User> findAll();

    // 新增方法：根据用户名查询用户
    User findByUsername(String username);

    // 新增方法：根据邮箱查询用户
    User findByEmail(String email);

    // 新增方法：更新用户状态
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
} 