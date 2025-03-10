package com.campus.forum.security;

import com.campus.forum.entity.User;
import com.campus.forum.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * UserDetailsService 实现
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        // userId 传入的是用户 ID（由 JwtAuthenticationFilter 解析 token 后传入）
        User user;
        try {
            user = userMapper.findById(Long.parseLong(userId));
        } catch (NumberFormatException e) {
            // 兼容直接传用户名的场景
            user = userMapper.findByUsername(userId);
        }
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + userId);
        }
        String role = user.getRole() == 1 ? "ROLE_ADMIN" : "ROLE_USER";
        return new org.springframework.security.core.userdetails.User(
                String.valueOf(user.getId()),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }
}
