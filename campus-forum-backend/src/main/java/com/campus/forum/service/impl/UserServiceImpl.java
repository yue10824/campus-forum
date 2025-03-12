package com.campus.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.entity.User;
import com.campus.forum.mapper.UserMapper;
import com.campus.forum.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public User getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        user.setPassword(null);
        return user;
    }

    @Override
    public User updateProfile(Long userId, String nickname, String avatar, String bio) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        if (StringUtils.hasText(nickname)) user.setNickname(nickname);
        if (StringUtils.hasText(avatar)) user.setAvatar(avatar);
        if (StringUtils.hasText(bio)) user.setBio(bio);
        userMapper.updateById(user);
        user.setPassword(null);
        return user;
    }

    @Override
    public boolean toggleFollow(Long followerId, Long followedId) {
        if (followerId.equals(followedId)) throw new BusinessException("不能关注自己");
        boolean following = userMapper.isFollowing(followerId, followedId);
        if (following) {
            userMapper.unfollow(followerId, followedId);
            return false;
        } else {
            userMapper.follow(followerId, followedId);
            return true;
        }
    }

    @Override
    public Page<User> getFollowers(Long userId, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<User> list = userMapper.findFollowers(userId, offset, pageSize);
        long total = userMapper.countFollowers(userId);
        Page<User> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(list);
        return page;
    }

    @Override
    public Page<User> getFollowing(Long userId, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<User> list = userMapper.findFollowing(userId, offset, pageSize);
        long total = userMapper.countFollowing(userId);
        Page<User> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(list);
        return page;
    }

    @Override
    public boolean isFollowing(Long followerId, Long followedId) {
        return userMapper.isFollowing(followerId, followedId);
    }
}
