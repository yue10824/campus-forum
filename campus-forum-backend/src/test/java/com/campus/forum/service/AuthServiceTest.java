package com.campus.forum.service;

import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.entity.User;
import com.campus.forum.mapper.UserMapper;
import com.campus.forum.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UT-001~005: AuthService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("认证服务测试")
class AuthServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setPassword("$2a$10$encodedPassword");
        mockUser.setNickname("测试用户");
        mockUser.setStatus(1);
        mockUser.setRole("USER");
        mockUser.setPostCount(0);
    }

    /**
     * UT-001: 注册成功
     */
    @Test
    @DisplayName("UT-001: 正常注册 - 应成功返回用户")
    void register_Success() {
        when(userMapper.findByUsername("newuser")).thenReturn(null);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encoded");
        when(userMapper.insert(any(User.class))).thenReturn(1);

        User result = authService.register("newuser", "password123", "新用户");

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("新用户", result.getNickname());
        verify(userMapper).insert(any(User.class));
    }

    /**
     * UT-002: 用户名重复注册
     */
    @Test
    @DisplayName("UT-002: 重复用户名 - 应抛出BusinessException")
    void register_DuplicateUsername_ThrowsException() {
        when(userMapper.findByUsername("testuser")).thenReturn(mockUser);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.register("testuser", "pass", "昵称"));
        assertEquals("用户名已存在", ex.getMessage());
    }

    /**
     * UT-003: 登录成功
     */
    @Test
    @DisplayName("UT-003: 正确密码登录 - 应成功返回用户")
    void login_Success() {
        when(userMapper.findByUsername("testuser")).thenReturn(mockUser);
        when(passwordEncoder.matches("password123", mockUser.getPassword())).thenReturn(true);

        User result = authService.login("testuser", "password123");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    /**
     * UT-004: 密码错误
     */
    @Test
    @DisplayName("UT-004: 错误密码 - 应抛出BusinessException")
    void login_WrongPassword_ThrowsException() {
        when(userMapper.findByUsername("testuser")).thenReturn(mockUser);
        when(passwordEncoder.matches("wrongpass", mockUser.getPassword())).thenReturn(false);

        assertThrows(BusinessException.class,
                () -> authService.login("testuser", "wrongpass"));
    }

    /**
     * UT-005: 账号被禁用
     */
    @Test
    @DisplayName("UT-005: 账号禁用 - 应抛出BusinessException")
    void login_DisabledAccount_ThrowsException() {
        mockUser.setStatus(0);
        when(userMapper.findByUsername("testuser")).thenReturn(mockUser);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.login("testuser", "password123"));
        assertTrue(ex.getMessage().contains("禁用") || ex.getMessage().contains("封禁"));
    }
}
