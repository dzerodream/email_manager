package com.emailmanager.controller;

import com.emailmanager.dto.AdminCreateUserDTO;
import com.emailmanager.dto.AdminUpdateUserDTO;
import com.emailmanager.entity.User;
import com.emailmanager.service.UserService;
import com.emailmanager.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest; // 引入 HttpServletRequest
import java.util.List;

/**
 * 管理员功能控制器 (Token认证适配版)
 * 所有API都以/admin为前缀，将由AdminInterceptor进行权限校验
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;

    @Autowired
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    // --- 用户管理 API ---

    @GetMapping("/users")
    public Result<List<User>> getAllUsers() {
        List<User> users = userService.findAll();
        users.forEach(user -> user.setPassword(null));
        return Result.success(users);
    }

    @GetMapping("/users/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user != null) {
            user.setPassword(null);
            return Result.success(user);
        } else {
            return Result.failure(404, "用户未找到");
        }
    }

    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestBody User statusUpdate) {
        Integer newStatus = statusUpdate.getStatus();
        if (newStatus == null || (newStatus != 0 && newStatus != 1)) {
            return Result.failure(400, "无效的状态值，必须是0或1");
        }
        boolean success = userService.updateStatus(id, newStatus);
        if (success) {
            return Result.success(newStatus == 1 ? "用户已启用" : "用户已禁用", null);
        } else {
            return Result.failure(404, "用户未找到，操作失败");
        }
    }

    /**
     * 删除一个用户 (Token认证适配版)
     */
    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        // 从request attribute获取当前操作的管理员ID
        Long currentAdminId = (Long) request.getAttribute("userId");

        // 检查管理员是否在尝试删除自己
        if (currentAdminId != null && currentAdminId.equals(id)) {
            return Result.failure(400, "操作失败：不能删除自己的账户");
        }

        boolean success = userService.deleteById(id);
        if (success) {
            return Result.success("用户删除成功", null);
        } else {
            return Result.failure(404, "用户未找到，删除失败");
        }
    }

    @PutMapping("/users/{id}/profile")
    public Result<Void> updateUserByAdmin(@PathVariable Long id, @RequestBody AdminUpdateUserDTO userDTO) {
        User existingUser = userService.findById(id);
        if (existingUser == null) {
            return Result.failure(404, "用户未找到");
        }

        // --- 检查新邮箱是否与其他用户冲突 ---
        if (userDTO.getEmail() != null && !userDTO.getEmail().equals(existingUser.getEmail())) {
            User userWithNewEmail = userService.findByEmail(userDTO.getEmail());
            if (userWithNewEmail != null && !userWithNewEmail.getId().equals(id)) {
                return Result.failure(400, "该邮箱已被其他用户注册");
            }
        }

        User userToUpdate = new User();
        userToUpdate.setId(id);
        userToUpdate.setRealName(userDTO.getRealName());
        userToUpdate.setPhone(userDTO.getPhone());
        userToUpdate.setAvatar(userDTO.getAvatar()); // 假设AdminUpdateUserDTO有avatar
        userToUpdate.setRole(userDTO.getRole());
        userToUpdate.setStatus(userDTO.getStatus());
        userToUpdate.setEmail(userDTO.getEmail()); // <-- 确保这里会设置email
        boolean success = userService.update(userToUpdate);
        if (success) {
            return Result.success("用户信息更新成功", null);
        } else {
            return Result.failure(500, "更新失败，请稍后重试");
        }
    }

    @PostMapping("/users")
    public Result<User> createUserByAdmin(@RequestBody AdminCreateUserDTO userDTO) {
        if (userService.isUsernameExists(userDTO.getUsername())) {
            return Result.failure(400, "用户名已存在");
        }
        if (userService.isEmailExists(userDTO.getEmail())) {
            return Result.failure(400, "邮箱已被注册");
        }
        User newUser = new User();
        newUser.setUsername(userDTO.getUsername());
        newUser.setPassword(userDTO.getPassword());
        newUser.setEmail(userDTO.getEmail());
        newUser.setRealName(userDTO.getRealName());
        newUser.setPhone(userDTO.getPhone());
        if (userDTO.getRole() != null) {
            newUser.setRole(userDTO.getRole());
        }
        if (userDTO.getStatus() != null) {
            newUser.setStatus(userDTO.getStatus());
        }
        Long newUserId = userService.register(newUser);
        if (newUserId != null) {
            User createdUser = userService.findById(newUserId);
            createdUser.setPassword(null);
            return Result.success("用户创建成功", createdUser);
        } else {
            return Result.failure(500, "创建用户失败");
        }
    }
}