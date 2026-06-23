package com.campus.forum.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.entity.User;

public interface UserService {
    User getUserById(Long id);
    User updateProfile(Long userId, String nickname, String avatar, String bio);
    boolean toggleFollow(Long followerId, Long followedId);
    Page<User> getFollowers(Long userId, int pageNum, int pageSize);
    Page<User> getFollowing(Long userId, int pageNum, int pageSize);
    boolean isFollowing(Long followerId, Long followedId);
}
