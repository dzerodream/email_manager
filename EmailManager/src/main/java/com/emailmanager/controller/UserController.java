package com.emailmanager.controller;

import com.emailmanager.dto.ChangePasswordDTO;
import com.emailmanager.dto.LoginDTO;
import com.emailmanager.dto.LoginResponseDTO;
import com.emailmanager.dto.UpdateAvatarDTO;
import com.emailmanager.entity.User;
import com.emailmanager.service.UserService;
import com.emailmanager.util.JwtUtil;
import com.emailmanager.vo.Result;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import org.springframework.web.multipart.MultipartFile; // 引入MultipartFile
import com.emailmanager.util.FileUploadUtil;    // 引入我们之前创建的工具类

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody User user) {
        if (userService.isUsernameExists(user.getUsername())) {
            return Result.failure(400, "用户名已存在");
        }
        if (userService.isEmailExists(user.getEmail())) {
            return Result.failure(400, "邮箱已被注册");
        }
        Long userId = userService.register(user);
        return userId != null ? Result.success("注册成功", null) : Result.failure(500, "注册失败，请稍后重试");
    }

    /**
     * 用户登录 (Token认证版)
     */
    @PostMapping("/login")
    public Result<LoginResponseDTO> login(@RequestBody LoginDTO loginDTO) {
        User user = userService.findByUsername(loginDTO.getUsername());
        if (user == null) {
            return Result.failure(401, "用户名或密码错误");
        }

        String encryptedPassword = DigestUtils.md5DigestAsHex(loginDTO.getPassword().getBytes(StandardCharsets.UTF_8));
        if (!user.getPassword().equals(encryptedPassword)) {
            return Result.failure(401, "用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            return Result.failure(403, "账户已被禁用，请联系管理员");
        }

        // 1. 生成Token
        String token = JwtUtil.generateToken(user);

        // 2. 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userService.update(user);
        user.setPassword(null); // 返回前隐藏密码

        // 3. 封装返回对象
        LoginResponseDTO responseDTO = new LoginResponseDTO();
        responseDTO.setToken(token);
        responseDTO.setUserInfo(user);

        return Result.success("登录成功", responseDTO);
    }

    /**
     * 用户登出 (Token认证版)
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        // 对于Token认证，服务器端无需做任何事。前端删除Token即可。
        return Result.success("已成功登出", null);
    }

    /**
     * 获取个人资料 (Token认证版)
     * 注意：我们不再从Session获取用户，而是从Token中获取并查询最新数据
     */
    @GetMapping("/profile")
    public Result<User> getProfile(HttpServletRequest request) {
        // userId 由 TokenInterceptor 验证并放入 request attribute
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.failure(401, "无效的凭证");
        }
        User latestUser = userService.findById(userId);
        if (latestUser != null) {
            latestUser.setPassword(null);
            return Result.success(latestUser);
        }
        return Result.failure(404, "用户不存在");
    }

    /**
     * 更新个人资料 (Token认证版)
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody User user, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        if (currentUserId == null) {
            return Result.failure(401, "无效的凭证");
        }

        user.setId(currentUserId);
        user.setUsername(null);
        user.setPassword(null);
        user.setRole(null);
        user.setStatus(null);
        user.setAvatar(null);

        if (userService.update(user)) {
            return Result.success("个人资料更新成功", null);
        } else {
            return Result.failure(500, "更新失败");
        }
    }

    /**
     * 更新当前用户的头像 (直接接收文件上传)
     * 核心修改：将 @PutMapping 改为 @PostMapping
     */
    @PostMapping("/avatar/upload")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile avatarFile, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        if (currentUserId == null) {
            return Result.failure(401, "无效的凭证");
        }

        if (avatarFile.isEmpty()) {
            return Result.failure(400, "上传文件不能为空");
        }

        String filePath = FileUploadUtil.saveFile(avatarFile);
        if (filePath == null) {
            return Result.failure(500, "头像上传失败，请稍后重试");
        }

        User userToUpdate = new User();
        userToUpdate.setId(currentUserId);
        userToUpdate.setAvatar(filePath);

        if (userService.update(userToUpdate)) {
            return Result.success("头像更新成功", filePath);
        } else {
            return Result.failure(500, "头像信息保存失败");
        }
    }

    /**
     * 修改密码 (Token认证版)
     */
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody ChangePasswordDTO passwordDTO, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        if (currentUserId == null) {
            return Result.failure(401, "无效的凭证");
        }
        boolean success = userService.changePassword(
                currentUserId,
                passwordDTO.getOldPassword(),
                passwordDTO.getNewPassword()
        );
        if (success) {
            return Result.success("密码修改成功，请重新登录", null);
        } else {
            return Result.failure(400, "旧密码错误");
        }
    }
}