package com.campus.forum;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 校园活动发布平台 启动类
 */
@SpringBootApplication
@MapperScan("com.campus.forum.mapper")
@EnableScheduling
public class CampusForumApplication {
    public static void main(String[] args) {
        SpringApplication.run(CampusForumApplication.class, args);
    }
}
