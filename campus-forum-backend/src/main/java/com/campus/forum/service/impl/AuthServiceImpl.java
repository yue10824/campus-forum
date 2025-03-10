package com.campus.forum.service.impl;

import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.dto.request.LoginRequest;
import com.campus.forum.dto.request.RegisterRequest;
import com.campus.forum.entity.User;
import com.campus.forum.mapper.UserMapper;
import com.campus.forum.security.JwtTokenProvider;
import com.campus.forum.service.AuthService;
import com.campus.forum.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务实现
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    @Override
    public void register(RegisterRequest req) {
        // 校验邮箱验证码
        if (!emailService.verifyCode(req.getEmail(), req.getEmailCode())) {
            throw new BusinessException("邮箱验证码错误或已过期");
        }
        // 检查用户名是否已存在
        if (userMapper.findByUsername(req.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setNickname(req.getNickname());
        user.setEmail(req.getEmail());
        user.setRole(0);
        user.setStatus(1);
        user.setPostCount(0);
        user.setFollowCount(0);
        user.setFanCount(0);
        userMapper.insert(user);
    }

    @Override
    public Map<String, Object> login(LoginRequest req) {
        User user = userMapper.findByUsername(req.getUsername());
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用，请联系管理员");
        }
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("nickname", user.getNickname());
        result.put("avatar", user.getAvatar());
        result.put("role", user.getRole());
        return result;
    }
}
