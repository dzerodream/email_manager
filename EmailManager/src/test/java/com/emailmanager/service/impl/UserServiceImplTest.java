// 文件路径: com/emailmanager/service/impl/UserServiceImplTest.java
package com.emailmanager.service.impl;

import com.emailmanager.entity.User;
import com.emailmanager.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * UserServiceImpl 的最终完整单元测试类
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    // --- register ---
    @Test
    @DisplayName("注册新用户 - 成功路径")
    void testRegister_Success() {
        // Arrange
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("rawPassword");

        // 模拟insert成功，并回填ID
        doAnswer(invocation -> {
            User passedUser = invocation.getArgument(0);
            passedUser.setId(1L);
            return 1;
        }).when(userMapper).insert(any(User.class));

        // Act
        Long userId = userService.register(user);

        // Assert
        assertEquals(1L, userId);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertNotEquals("rawPassword", capturedUser.getPassword());
        assertEquals("USER", capturedUser.getRole());
        assertEquals(1, capturedUser.getStatus());
    }

    @Test
    @DisplayName("注册新用户 - 数据库插入失败时应抛出异常")
    void testRegister_FailureOnDbInsert() {
        // Arrange
        User user = new User();
        user.setUsername("failUser");
        user.setPassword("password");

        // --- 核心修改：不再模拟返回值，而是直接模拟抛出异常 ---
        // 当调用userMapper.insert时，让它直接抛出一个模拟的数据库异常
        // 我们使用一个通用的RuntimeException来模拟即可
        doThrow(new RuntimeException("Simulated DB error"))
                .when(userMapper).insert(any(User.class));

        // Act & Assert
        // 验证调用userService.register时，是否会抛出异常
        // 这里的异常可能是我们模拟的，也可能是Service层自己包装的
        assertThrows(RuntimeException.class, () -> {
            userService.register(user);
        });

        // 最后，验证insert方法确实被调用了
        verify(userMapper, times(1)).insert(any(User.class));
    }

    // --- login ---
    @Test
    @DisplayName("登录 - 成功")
    void testLogin_Success() {
        String username = "testuser";
        String rawPassword = "password123";
        String encryptedPassword = DigestUtils.md5DigestAsHex(rawPassword.getBytes(StandardCharsets.UTF_8));

        User userFromDb = new User();
        userFromDb.setUsername(username);
        userFromDb.setPassword(encryptedPassword);

        when(userMapper.findByUsername(username)).thenReturn(userFromDb);

        User result = userService.login(username, rawPassword);
        assertNotNull(result);
        assertEquals(username, result.getUsername());
    }

    @Test
    @DisplayName("登录 - 失败（用户不存在）")
    void testLogin_Failure_UserNotFound() {
        when(userMapper.findByUsername("unknown")).thenReturn(null);
        assertNull(userService.login("unknown", "anypass"));
    }

    @Test
    @DisplayName("登录 - 失败（密码错误）")
    void testLogin_Failure_WrongPassword() {
        String username = "testuser";
        String rawPassword = "wrongPassword";
        String encryptedPassword = DigestUtils.md5DigestAsHex("correctPassword".getBytes(StandardCharsets.UTF_8));
        User userFromDb = new User();
        userFromDb.setPassword(encryptedPassword);
        when(userMapper.findByUsername(username)).thenReturn(userFromDb);
        assertNull(userService.login(username, rawPassword));
    }

    // --- findById ---
    @Test
    @DisplayName("根据ID查找用户 - 找到")
    void testFindById_Found() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        when(userMapper.findById(userId)).thenReturn(user);
        User result = userService.findById(userId);
        assertNotNull(result);
        assertEquals(userId, result.getId());
    }

    @Test
    @DisplayName("根据ID查找用户 - 未找到")
    void testFindById_NotFound() {
        when(userMapper.findById(anyLong())).thenReturn(null);
        assertNull(userService.findById(99L));
    }

    // --- update ---
    @Test
    @DisplayName("更新用户信息")
    void testUpdate() {
        User user = new User();
        when(userMapper.update(user)).thenReturn(1);
        assertTrue(userService.update(user));
        verify(userMapper).update(user);
    }

    // --- deleteById ---
    @Test
    @DisplayName("删除用户")
    void testDeleteById() {
        Long userId = 1L;
        when(userMapper.deleteById(userId)).thenReturn(1);
        assertTrue(userService.deleteById(userId));
        verify(userMapper).deleteById(userId);
    }

    // --- findAll ---
    @Test
    @DisplayName("查找所有用户")
    void testFindAll() {
        when(userMapper.findAll()).thenReturn(Collections.singletonList(new User()));
        List<User> result = userService.findAll();
        assertFalse(result.isEmpty());
    }

    // --- changePassword ---
    @Test
    @DisplayName("修改密码 - 成功")
    void testChangePassword_Success() {
        Long userId = 1L;
        String oldPass = "old123";
        String newPass = "new456";
        String encryptedOldPass = DigestUtils.md5DigestAsHex(oldPass.getBytes(StandardCharsets.UTF_8));

        User userFromDb = new User();
        userFromDb.setId(userId);
        userFromDb.setPassword(encryptedOldPass);

        when(userMapper.findById(userId)).thenReturn(userFromDb);
        when(userMapper.update(any(User.class))).thenReturn(1);

        assertTrue(userService.changePassword(userId, oldPass, newPass));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).update(captor.capture());
        String encryptedNewPass = DigestUtils.md5DigestAsHex(newPass.getBytes(StandardCharsets.UTF_8));
        assertEquals(encryptedNewPass, captor.getValue().getPassword());
    }

    @Test
    @DisplayName("修改密码 - 失败（旧密码错误）")
    void testChangePassword_Failure_WrongOldPassword() {
        Long userId = 1L;
        String oldPasswordAttempt = "wrongOldPass";
        String newPassword = "newPass";

        // --- 核心修改点：统一变量名 ---
        // 我们统一使用 "correctEncryptedOldPassword" 这个更清晰的名称
        String correctEncryptedOldPassword = DigestUtils.md5DigestAsHex("correctOldPass".getBytes(StandardCharsets.UTF_8));

        User userFromDb = new User();
        userFromDb.setId(userId);
        // 确保这里使用的是上面定义的同一个变量
        userFromDb.setPassword(correctEncryptedOldPassword);
        // --- 修改结束 ---

        when(userMapper.findById(userId)).thenReturn(userFromDb);

        assertFalse(userService.changePassword(userId, oldPasswordAttempt, newPassword));

        verify(userMapper, never()).update(any(User.class));
    }

    // --- isUsernameExists & isEmailExists ---
    @Test
    @DisplayName("检查用户名是否存在")
    void testIsUsernameExists() {
        when(userMapper.findByUsername("exists")).thenReturn(new User());
        when(userMapper.findByUsername("not-exists")).thenReturn(null);

        assertTrue(userService.isUsernameExists("exists"));
        assertFalse(userService.isUsernameExists("not-exists"));
    }

    @Test
    @DisplayName("检查邮箱是否存在")
    void testIsEmailExists() {
        when(userMapper.findByEmail("exists@a.com")).thenReturn(new User());
        when(userMapper.findByEmail("not-exists@a.com")).thenReturn(null);

        assertTrue(userService.isEmailExists("exists@a.com"));
        assertFalse(userService.isEmailExists("not-exists@a.com"));
    }

    // --- updateStatus ---
    @Test
    @DisplayName("更新用户状态")
    void testUpdateStatus() {
        when(userMapper.updateStatus(1L, 0)).thenReturn(1);
        assertTrue(userService.updateStatus(1L, 0));
        verify(userMapper).updateStatus(1L, 0);
    }

    // resetPassword方法的测试比较特殊，因为它包含随机性。
    // 我们主要测试它是否调用了update，以及返回的密码是否与加密前的不一致。
    @Test
    @DisplayName("重置密码")
    void testResetPassword() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        when(userMapper.findById(userId)).thenReturn(user);
        when(userMapper.update(any(User.class))).thenReturn(1);

        String newPlainTextPassword = userService.resetPassword(userId);
        assertNotNull(newPlainTextPassword);
        assertEquals(8, newPlainTextPassword.length());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).update(captor.capture());

        // 验证存入数据库的密码不是明文
        assertNotEquals(newPlainTextPassword, captor.getValue().getPassword());
        // 验证存入数据库的密码是明文加密后的结果
        assertEquals(DigestUtils.md5DigestAsHex(newPlainTextPassword.getBytes(StandardCharsets.UTF_8)), captor.getValue().getPassword());
    }
}