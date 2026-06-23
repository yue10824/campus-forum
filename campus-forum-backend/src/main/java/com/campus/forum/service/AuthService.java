package com.campus.forum.service;

import com.campus.forum.dto.request.LoginRequest;
import com.campus.forum.dto.request.RegisterRequest;

import java.util.Map;

public interface AuthService {
    void register(RegisterRequest req);
    Map<String, Object> login(LoginRequest req);
}
