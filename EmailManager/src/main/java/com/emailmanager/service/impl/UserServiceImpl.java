package com.emailmanager.service.impl;

import com.emailmanager.entity.User;
import com.emailmanager.mapper.UserMapper;
import com.emailmanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 用户注册（最终严谨版）
     */
    @Override
    @Transactional
    public Long register(User user) {
        // 加密密码
        user.setPassword(encryptPassword(user.getPassword()));

        // 如果传入的用户对象没有设置状态，则默认为启用(1)
        if (user.getStatus() == null) {
            user.setStatus(1);
        }

        // 如果传入的用户对象没有设置角色，则默认为普通用户("USER")
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }

        // --- 核心修改点：检查Mapper的返回值 ---
        int affectedRows = userMapper.insert(user);

        // 只有当数据库确实插入了1行数据，并且MyBatis成功回填了ID时，才认为注册成功
        if (affectedRows > 0 && user.getId() != null) {
            return user.getId();
        } else {
            // 如果插入失败，可以根据业务需求选择返回null或抛出自定义异常
            throw new RuntimeException("创建用户失败，数据库操作未成功。");
        }
    }

    @Override
    public User login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user != null && user.getPassword().equals(encryptPassword(password))) {
            return user;
        }
        return null;
    }

    @Override
    public User findById(Long id) {
        return userMapper.findById(id);
    }

    @Override
    @Transactional
    public boolean update(User user) {
        return userMapper.update(user) > 0;
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        return userMapper.deleteById(id) > 0;
    }

    @Override
    public List<User> findAll() {
        return userMapper.findAll();
    }

    @Override
    @Transactional
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = findById(userId);
        if (user == null || !user.getPassword().equals(encryptPassword(oldPassword))) {
            return false;
        }
        user.setPassword(encryptPassword(newPassword));
        return update(user);
    }

    @Override
    @Transactional
    public String resetPassword(Long userId) {
        User user = findById(userId);
        if (user == null) {
            return null;
        }
        String generatedNewPassword = generateRandomPassword();
        user.setPassword(encryptPassword(generatedNewPassword));
        if (update(user)) {
            return generatedNewPassword;
        }
        return null;
    }

    @Override
    public boolean isUsernameExists(String username) {
        return userMapper.findByUsername(username) != null;
    }

    @Override
    public boolean isEmailExists(String email) {
        return userMapper.findByEmail(email) != null;
    }

    @Override
    @Transactional
    public boolean updateStatus(Long userId, Integer status) {
        return userMapper.updateStatus(userId, status) > 0;
    }

    private String encryptPassword(String password) {
        return DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 根据用户名查询用户 (补全的实现)
     * @param username 用户名
     * @return 找到的用户，或null
     */
    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }



    @Override
    public User findByEmail(String email) {
        return userMapper.findByEmail(email);
    }
}