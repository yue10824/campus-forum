-- ============================================================
-- 校园活动发布平台 数据库初始化脚本
-- ============================================================
CREATE DATABASE IF NOT EXISTS campus_forum DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campus_forum;

-- ============================================================
-- 1. 用户表
-- ============================================================
CREATE TABLE `user` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username`     VARCHAR(32)  NOT NULL UNIQUE COMMENT '用户名',
  `password`     VARCHAR(128) NOT NULL COMMENT '加密密码',
  `nickname`     VARCHAR(32)  NOT NULL COMMENT '昵称',
  `avatar`       VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `bio`          VARCHAR(200) DEFAULT NULL COMMENT '个人简介',
  `email`        VARCHAR(64)  DEFAULT NULL UNIQUE COMMENT '邮箱',
  `role`         TINYINT      NOT NULL DEFAULT 0 COMMENT '角色：0普通用户 1管理员',
  `status`       TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1正常',
  `post_count`   INT          NOT NULL DEFAULT 0 COMMENT '发帖数',
  `follow_count` INT          NOT NULL DEFAULT 0 COMMENT '关注数',
  `fan_count`    INT          NOT NULL DEFAULT 0 COMMENT '粉丝数',
  `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_username` (`username`)
) ENGINE=InnoDB COMMENT='用户表';

-- ============================================================
-- 2. 版块表
-- ============================================================
CREATE TABLE `section` (
  `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '版块ID',
  `name`        VARCHAR(32)  NOT NULL COMMENT '版块名',
  `code`        VARCHAR(32)  NOT NULL UNIQUE COMMENT '版块标识',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '版块描述',
  `icon`        VARCHAR(100) DEFAULT NULL COMMENT '版块图标',
  `sort_order`  INT          NOT NULL DEFAULT 0 COMMENT '排序',
  `post_count`  INT          NOT NULL DEFAULT 0 COMMENT '帖子数',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='版块表';

-- 预置6个版块
INSERT INTO `section` (`name`, `code`, `description`, `sort_order`) VALUES
('综合讨论', 'general',  '校园综合话题讨论',    1),
('学习交流', 'study',    '学习资料与经验交流',   2),
('二手交易', 'trade',    '二手物品买卖信息',    3),
('校园生活', 'life',     '校园生活点滴分享',    4),
('社团活动', 'activity', '社团招募与活动通知',   5),
('美食分享', 'food',     '校园周边美食推荐',    6);

-- ============================================================
-- 3. 活动表
-- ============================================================
CREATE TABLE `activity` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `user_id`         BIGINT       NOT NULL COMMENT '发布用户ID',
  `section_id`      INT          DEFAULT NULL COMMENT '关联版块ID',
  `title`           VARCHAR(100) NOT NULL COMMENT '活动标题',
  `description`     LONGTEXT     NOT NULL COMMENT '活动描述（富文本）',
  `cover_image`     VARCHAR(255) DEFAULT NULL COMMENT '封面图',
  `location`        VARCHAR(100) DEFAULT NULL COMMENT '活动地点',
  `start_time`      DATETIME     DEFAULT NULL COMMENT '开始时间',
  `end_time`        DATETIME     DEFAULT NULL COMMENT '结束时间',
  `max_participants` INT         DEFAULT NULL COMMENT '最大参与人数（NULL=不限）',
  `current_participants` INT     NOT NULL DEFAULT 0 COMMENT '当前报名人数',
  `like_count`      INT          NOT NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count`   INT          NOT NULL DEFAULT 0 COMMENT '评论数',
  `collect_count`   INT          NOT NULL DEFAULT 0 COMMENT '收藏数',
  `view_count`      INT          NOT NULL DEFAULT 0 COMMENT '浏览数',
  `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '0草稿 1报名中 2已结束 3已取消 4待审核',
  `is_top`          TINYINT      NOT NULL DEFAULT 0 COMMENT '是否置顶',
  `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_user_id`    (`user_id`),
  INDEX `idx_section_id` (`section_id`),
  INDEX `idx_start_time` (`start_time`)
) ENGINE=InnoDB COMMENT='活动表';

-- ============================================================
-- 4. 活动报名表
-- ============================================================
CREATE TABLE `activity_registration` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '报名ID',
  `activity_id` BIGINT   NOT NULL COMMENT '活动ID',
  `user_id`     BIGINT   NOT NULL COMMENT '报名用户ID',
  `status`      TINYINT  NOT NULL DEFAULT 0 COMMENT '0待审核 1已通过 2已拒绝 3已取消',
  `remark`      VARCHAR(200) DEFAULT NULL COMMENT '报名备注',
  `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_user` (`activity_id`, `user_id`),
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB COMMENT='活动报名表';

-- ============================================================
-- 5. 帖子表
-- ============================================================
CREATE TABLE `post` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
  `user_id`       BIGINT       NOT NULL COMMENT '发布用户ID',
  `section_id`    INT          NOT NULL COMMENT '版块ID',
  `title`         VARCHAR(100) NOT NULL COMMENT '标题',
  `content`       LONGTEXT     NOT NULL COMMENT '正文（支持HTML富文本）',
  `cover_image`   VARCHAR(255) DEFAULT NULL COMMENT '封面图',
  `like_count`    INT          NOT NULL DEFAULT 0,
  `comment_count` INT          NOT NULL DEFAULT 0,
  `collect_count` INT          NOT NULL DEFAULT 0,
  `view_count`    INT          NOT NULL DEFAULT 0,
  `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '0草稿 1已发布 2已删除 3审核中',
  `is_top`        TINYINT      NOT NULL DEFAULT 0 COMMENT '是否置顶',
  `is_essence`    TINYINT      NOT NULL DEFAULT 0 COMMENT '是否加精',
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_user_id`    (`user_id`),
  INDEX `idx_section_id` (`section_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB COMMENT='帖子表';

-- ============================================================
-- 6. 评论表（楼中楼，支持帖子和活动）
-- ============================================================
CREATE TABLE `comment` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `target_id`   BIGINT      NOT NULL COMMENT '目标ID（帖子ID或活动ID）',
  `target_type` VARCHAR(16) NOT NULL DEFAULT 'post' COMMENT '目标类型：post/activity',
  `user_id`     BIGINT      NOT NULL COMMENT '评论者ID',
  `parent_id`   BIGINT      DEFAULT NULL COMMENT '父评论ID（NULL为一级评论）',
  `reply_uid`   BIGINT      DEFAULT NULL COMMENT '被回复用户ID',
  `content`     TEXT        NOT NULL COMMENT '评论内容',
  `like_count`  INT         NOT NULL DEFAULT 0,
  `status`      TINYINT     NOT NULL DEFAULT 1 COMMENT '0删除 1正常',
  `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_target` (`target_id`, `target_type`),
  INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB COMMENT='评论表';

-- ============================================================
-- 7. 点赞表（统一处理帖子/活动/评论点赞）
-- ============================================================
CREATE TABLE `post_like` (
  `user_id`     BIGINT      NOT NULL,
  `target_id`   BIGINT      NOT NULL,
  `target_type` VARCHAR(16) NOT NULL DEFAULT 'post' COMMENT 'post/activity/comment',
  `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`, `target_id`, `target_type`)
) ENGINE=InnoDB COMMENT='点赞表';

-- ============================================================
-- 8. 收藏表（统一处理帖子/活动收藏）
-- ============================================================
CREATE TABLE `post_collection` (
  `user_id`     BIGINT      NOT NULL,
  `target_id`   BIGINT      NOT NULL,
  `target_type` VARCHAR(16) NOT NULL DEFAULT 'post' COMMENT 'post/activity',
  `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`, `target_id`, `target_type`)
) ENGINE=InnoDB COMMENT='收藏表';

-- ============================================================
-- 9. 关注关系表
-- ============================================================
CREATE TABLE `user_follow` (
  `follower_id`  BIGINT   NOT NULL COMMENT '关注者ID',
  `following_id` BIGINT   NOT NULL COMMENT '被关注者ID',
  `created_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`follower_id`, `following_id`),
  INDEX `idx_following_id` (`following_id`)
) ENGINE=InnoDB COMMENT='关注关系表';

-- ============================================================
-- 10. 私信表
-- ============================================================
CREATE TABLE `message` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT,
  `sender_id`   BIGINT   NOT NULL COMMENT '发送者ID',
  `receiver_id` BIGINT   NOT NULL COMMENT '接收者ID',
  `content`     TEXT     NOT NULL COMMENT '消息内容',
  `is_read`     TINYINT  NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
  `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_sender_receiver` (`sender_id`, `receiver_id`),
  INDEX `idx_receiver_id`     (`receiver_id`)
) ENGINE=InnoDB COMMENT='私信表';

-- ============================================================
-- 11. 系统通知表
-- ============================================================
CREATE TABLE `notification` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT       NOT NULL COMMENT '接收通知的用户ID',
  `type`        VARCHAR(32)  NOT NULL COMMENT 'LIKE/COMMENT/REPLY/FOLLOW/SYSTEM/REGISTER',
  `actor_id`    BIGINT       DEFAULT NULL COMMENT '触发者ID',
  `target_id`   BIGINT       DEFAULT NULL COMMENT '目标对象ID',
  `target_type` VARCHAR(32)  DEFAULT NULL COMMENT '目标类型',
  `content`     VARCHAR(200) DEFAULT NULL COMMENT '通知摘要',
  `is_read`     TINYINT      NOT NULL DEFAULT 0,
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB COMMENT='系统通知表';

-- ============================================================
-- 12. 用户行为记录表（支撑协同过滤推荐）
-- ============================================================
CREATE TABLE `user_behavior` (
  `id`            BIGINT      NOT NULL AUTO_INCREMENT,
  `user_id`       BIGINT      NOT NULL,
  `target_id`     BIGINT      NOT NULL COMMENT '目标内容ID（帖子/活动）',
  `target_type`   VARCHAR(16) NOT NULL DEFAULT 'post' COMMENT 'post/activity',
  `behavior_type` VARCHAR(16) NOT NULL COMMENT 'VIEW/LIKE/COLLECT/COMMENT',
  `score`         FLOAT       NOT NULL DEFAULT 1.0 COMMENT '行为权重分',
  `created_at`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_user_target` (`user_id`, `target_id`, `target_type`)
) ENGINE=InnoDB COMMENT='用户行为记录（推荐引擎）';

-- ============================================================
-- 13. 公告表
-- ============================================================
CREATE TABLE `announcement` (
  `id`         INT          NOT NULL AUTO_INCREMENT,
  `title`      VARCHAR(100) NOT NULL,
  `content`    TEXT         NOT NULL,
  `admin_id`   BIGINT       NOT NULL COMMENT '发布管理员ID',
  `is_active`  TINYINT      NOT NULL DEFAULT 1,
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='公告表';

-- ============================================================
-- 14. AI对话历史表
-- ============================================================
CREATE TABLE `ai_chat_history` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`    BIGINT       NOT NULL COMMENT '用户ID',
  `role`       VARCHAR(16)  NOT NULL COMMENT 'user/assistant',
  `content`    TEXT         NOT NULL COMMENT '消息内容',
  `session_id` VARCHAR(64)  DEFAULT NULL COMMENT '会话ID',
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_user_id`    (`user_id`),
  INDEX `idx_session_id` (`session_id`)
) ENGINE=InnoDB COMMENT='AI对话历史表';

-- ============================================================
-- 初始化管理员账号（密码：demo123，BCrypt加密）
-- ============================================================
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`) VALUES
('admin', '$2a$10$9wTUQ4d2rEvd3afeyQ5gWeLORB9eap6gsPyEY3shJ60xD6IpVzT1G', '管理员', 1, 1),
('test',  '$2a$10$9wTUQ4d2rEvd3afeyQ5gWeLORB9eap6gsPyEY3shJ60xD6IpVzT1G', '测试用户', 0, 1);
