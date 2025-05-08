package com.campus.forum.service.impl;

import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.service.EmailService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    // 存储验证码：email -> {code, expireTime}
    private final Map<String, CodeEntry> codeStore = new ConcurrentHashMap<>();

    // 验证码有效期：5分钟
    private static final long CODE_EXPIRE_MINUTES = 5;
    // 发送频率限制：60秒
    private static final long CODE_SEND_INTERVAL_SECONDS = 60;

    private final Random random = new Random();

    @PostConstruct
    public void init() {
        log.info("邮件验证码服务已启动");
    }

    @Override
    public void sendVerificationCode(String email) {
        // 检查发送频率
        CodeEntry existing = codeStore.get(email);
        if (existing != null) {
            long secondsSinceLastSend = java.time.Duration.between(existing.sendTime, LocalDateTime.now()).getSeconds();
            if (secondsSinceLastSend < CODE_SEND_INTERVAL_SECONDS) {
                long remaining = CODE_SEND_INTERVAL_SECONDS - secondsSinceLastSend;
                throw new BusinessException("发送太频繁，请 " + remaining + " 秒后再试");
            }
        }

        // 生成6位数字验证码
        String code = String.format("%06d", random.nextInt(1000000));

        // 存储验证码
        CodeEntry entry = new CodeEntry();
        entry.code = code;
        entry.expireTime = LocalDateTime.now().plusMinutes(CODE_EXPIRE_MINUTES);
        entry.sendTime = LocalDateTime.now();
        codeStore.put(email, entry);

        // 发送邮件
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("532255198@qq.com");
            message.setTo(email);
            message.setSubject("【校园活动平台】注册验证码");
            message.setText("您的注册验证码为：" + code + "\n\n验证码有效期为 " + CODE_EXPIRE_MINUTES + " 分钟，请尽快完成注册。\n\n如非本人操作，请忽略此邮件。");
            mailSender.send(message);
            log.info("验证码已发送至 {}: {}", email, code);
        } catch (Exception e) {
            log.error("发送邮件失败: {}", e.getMessage());
            // 发送失败时也保留验证码（控制台可查看）
            throw new BusinessException("邮件发送失败，请稍后再试");
        }
    }

    @Override
    public boolean verifyCode(String email, String code) {
        CodeEntry entry = codeStore.get(email);
        if (entry == null) {
            return false;
        }
        // 检查是否过期
        if (LocalDateTime.now().isAfter(entry.expireTime)) {
            codeStore.remove(email);
            return false;
        }
        // 验证码匹配（不区分大小写，但验证码是数字）
        if (entry.code.equals(code)) {
            // 验证成功后删除，防止重复使用
            codeStore.remove(email);
            return true;
        }
        return false;
    }

    // 定时清理过期验证码（每5分钟执行一次）
    @Scheduled(fixedRate = 300000)
    public void cleanupExpiredCodes() {
        int before = codeStore.size();
        codeStore.entrySet().removeIf(e -> LocalDateTime.now().isAfter(e.getValue().expireTime));
        int removed = before - codeStore.size();
        if (removed > 0) {
            log.info("清理过期验证码 {} 条", removed);
        }
    }

    // 内部类：验证码条目
    private static class CodeEntry {
        String code;
        LocalDateTime expireTime;
        LocalDateTime sendTime;
    }
}
