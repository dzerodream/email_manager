package com.emailmanager.service;

import com.emailmanager.entity.User;
import java.util.List;

public interface UserService {

    /**
     * 用户注册
     * @param user 用户信息
     * @return 注册成功返回新用户的ID，失败则返回null或抛出异常
     */
    Long register(User user);

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 登录成功返回用户信息，失败返回null
     */
    User login(String username, String password);

    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户信息
     */
    User findById(Long id);

    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 是否更新成功
     */
    boolean update(User user);

    /**
     * 删除用户
     * @param id 用户ID
     * @return 是否删除成功
     */
    boolean deleteById(Long id);

    /**
     * 查询所有用户
     * @return 用户列表
     */
    List<User> findAll();

    /**
     * 修改密码
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否修改成功
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 重置密码
     * @param userId 用户ID
     * @return 新密码
     */
    String resetPassword(Long userId);

    /**
     * 检查用户名是否已存在
     * @param username 用户名
     * @return 是否存在
     */
    boolean isUsernameExists(String username);

    /**
     * 检查邮箱是否已存在
     * @param email 邮箱
     * @return 是否存在
     */
    boolean isEmailExists(String email);

    /**
     * 更新用户状态
     * @param userId 用户ID
     * @param status 状态
     * @return 是否更新成功
     */
    boolean updateStatus(Long userId, Integer status);


    /**
     * 根据用户名查询用户
     * (这是我们重构login逻辑时需要的新方法)
     * @param username 用户名
     * @return 找到的用户信息，未找到则返回null
     */
    User findByUsername(String username); // <-- 在这里追加这个方法的声明


    User findByEmail(String email);

}