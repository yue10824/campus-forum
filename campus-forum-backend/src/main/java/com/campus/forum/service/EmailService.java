package com.campus.forum.service;

public interface EmailService {
    /**
     * 发送验证码到指定邮箱
     * @param email 目标邮箱
     */
    void sendVerificationCode(String email);

    /**
     * 验证邮箱验证码是否正确
     * @param email 邮箱
     * @param code 验证码
     * @return 是否验证通过
     */
    boolean verifyCode(String email, String code);
}
