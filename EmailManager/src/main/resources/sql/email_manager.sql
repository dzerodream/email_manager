CREATE DATABASE IF NOT EXISTS email_manager DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE email_manager;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 公告表
DROP TABLE IF EXISTS `announcements`;
CREATE TABLE `announcements`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告内容',
  `publisher_id` bigint NOT NULL COMMENT '发布者ID',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `publish_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_publisher`(`publisher_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统公告表' ROW_FORMAT = DYNAMIC;

-- 通讯录表
DROP TABLE IF EXISTS `contacts`;
CREATE TABLE `contacts`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '联系人ID',
  `user_id` bigint NOT NULL COMMENT '所属用户ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '联系人姓名',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '邮箱地址',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '电话号码',
  `birthday` date NULL DEFAULT NULL COMMENT '生日',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '备注',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_email`(`email` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '通讯录表' ROW_FORMAT = DYNAMIC;

-- 邮件附件表
DROP TABLE IF EXISTS `email_attachments`;
CREATE TABLE `email_attachments`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '附件ID',
  `email_id` bigint NOT NULL COMMENT '邮件ID',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件名',
  `file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件路径',
  `file_size` bigint NOT NULL COMMENT '文件大小(字节)',
  `file_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件类型',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_email`(`email_id` ASC) USING BTREE,
  CONSTRAINT `email_attachments_ibfk_1` FOREIGN KEY (`email_id`) REFERENCES `emails` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '邮件附件表' ROW_FORMAT = DYNAMIC;

-- 邮件收件人表
DROP TABLE IF EXISTS `email_recipients`;
CREATE TABLE `email_recipients`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email_id` bigint NOT NULL COMMENT '关联的邮件ID',
  `recipient_email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收件人邮箱地址',
  `recipient_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收件人类型: TO, CC, BCC',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_email_id`(`email_id` ASC) USING BTREE,
  CONSTRAINT `fk_email_recipients_email_id` FOREIGN KEY (`email_id`) REFERENCES `emails` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '邮件收件人记录表' ROW_FORMAT = DYNAMIC;

-- 邮件内容表
DROP TABLE IF EXISTS `emails`;
CREATE TABLE `emails`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '邮件ID',
  `sender_id` bigint NOT NULL COMMENT '发件人用户ID',
  `subject` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '邮件主题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '邮件内容',
  `is_html` tinyint NOT NULL DEFAULT 0 COMMENT '是否HTML格式：0-否，1-是',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT-草稿, SENT-已发送',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `sender_id`(`sender_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `emails_ibfk_1` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '邮件内容核心表' ROW_FORMAT = DYNAMIC;

-- 节假日表
DROP TABLE IF EXISTS `holidays`;
CREATE TABLE `holidays`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '节假日ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '节假日名称',
  `holiday_date` date NOT NULL COMMENT '日期',
  `is_workday` tinyint NOT NULL DEFAULT 0 COMMENT '是否工作日：0-否，1-是',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '描述',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_date`(`holiday_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 78 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '节假日表' ROW_FORMAT = DYNAMIC;

-- 用户邮箱条目表
DROP TABLE IF EXISTS `mailbox`;
CREATE TABLE `mailbox`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '所属用户ID (收件人或发件人)',
  `email_id` bigint NOT NULL COMMENT '关联的邮件ID',
  `folder` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'INBOX' COMMENT '邮件在此用户邮箱中的文件夹 (INBOX, SENT, DRAFT, TRASH, ARCHIVE)',
  `is_read` tinyint NOT NULL DEFAULT 0 COMMENT '是否已读 (此用户视角)',
  `is_starred` tinyint NOT NULL DEFAULT 0 COMMENT '是否星标 (此用户视角)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_email`(`user_id` ASC, `email_id` ASC) USING BTREE,
  INDEX `email_id`(`email_id` ASC) USING BTREE,
  CONSTRAINT `mailbox_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `mailbox_ibfk_2` FOREIGN KEY (`email_id`) REFERENCES `emails` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 36 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户邮箱条目表' ROW_FORMAT = DYNAMIC;

-- 服务条款表
DROP TABLE IF EXISTS `service_terms`;
CREATE TABLE `service_terms`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '条款ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '条款标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '条款内容',
  `version` int NOT NULL COMMENT '版本号',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-草稿，1-生效，2-过期',
  `effective_date` datetime NOT NULL COMMENT '生效日期',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_version`(`version` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_effective_date`(`effective_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '服务条款表' ROW_FORMAT = DYNAMIC;

-- 垃圾邮件规则表
DROP TABLE IF EXISTS `spam_rules`;
CREATE TABLE `spam_rules`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `rule_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '规则名称',
  `rule_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规则类型：SENDER-发件人，SUBJECT-主题，CONTENT-内容',
  `rule_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规则内容',
  `is_global` tinyint NOT NULL DEFAULT 0 COMMENT '是否全局规则：0-否，1-是',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_type`(`rule_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '垃圾邮件规则表' ROW_FORMAT = DYNAMIC;

-- 用户表
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '邮箱地址',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '电话号码',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'USER' COMMENT '角色：ADMIN/USER',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_email`(`email` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;

-- 清空数据
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `email_recipients`;
TRUNCATE TABLE `mailbox`;
TRUNCATE TABLE `email_attachments`;
TRUNCATE TABLE `emails`;
TRUNCATE TABLE `contacts`;
TRUNCATE TABLE `spam_rules`;
TRUNCATE TABLE `announcements`;
TRUNCATE TABLE `service_terms`;
TRUNCATE TABLE `holidays`;
TRUNCATE TABLE `users`;
SET FOREIGN_KEY_CHECKS = 1;

-- 插入用户数据
INSERT INTO `users` (`id`, `username`, `password`, `email`, `real_name`, `phone`, `avatar`, `role`, `status`, `created_time`, `updated_time`) VALUES
(1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', 'admin@example.com', '系统管理员', '13800138000', '/2025/06/24/admin_avatar.png', 'ADMIN', 1, NOW(), NOW()),
(2, 'userA', 'e10adc3949ba59abbe56e057f20f883e', 'userA@example.com', '张三', '13900139001', '/2025/06/24/userA_avatar.jpg', 'USER', 1, NOW(), NOW()),
(3, 'userB', 'e10adc3949ba59abbe56e057f20f883e', 'userB@example.com', '李四', '13700137002', NULL, 'USER', 1, NOW(), NOW()),
(4, 'userC_disabled', 'e10adc3949ba59abbe56e057f20f883e', 'userC@example.com', '王五(禁用)', '13600136003', NULL, 'USER', 0, NOW(), NOW());

-- 插入联系人数据
INSERT INTO `contacts` (`user_id`, `name`, `email`, `phone`, `birthday`, `remark`, `created_time`, `updated_time`) VALUES
(2, '李四（同事）', 'userB@example.com', '13700137002', '1995-08-15', '研发部同事', NOW(), NOW()),
(2, '王五（朋友）', 'userC@example.com', '13600136003', '1998-12-05', '大学同学', NOW(), NOW()),
(2, '外部客户李', 'client_li@external.com', '18811112222', NULL, '重要客户', NOW(), NOW());

-- 插入邮件数据
INSERT INTO `emails` (`id`, `sender_id`, `subject`, `content`, `is_html`, `status`, `created_time`, `updated_time`) VALUES
(101, 3, '关于项目Alpha版本的反馈', '<p>张三你好，</p><p>项目Alpha版本已测试，附件是一些初步反馈意见，请查收。</p><p>BR,<br/>李四</p>', 1, 'SENT', '2025-06-20 10:00:00', '2025-06-20 10:00:00'),
(102, 2, '周末团建活动通知', '<h1>各位同事：</h1><p>本周末公司组织团建活动，详情请见邮件。请务必参与！</p>', 1, 'SENT', '2025-06-21 14:30:00', '2025-06-21 14:30:00'),
(103, 2, 'Re: 新功能上线计划（草稿）', '关于新功能... <i>还在思考中...</i>', 0, 'DRAFT', '2025-06-22 09:15:00', '2025-06-22 09:15:00'),
(104, 1, '【系统公告】服务器维护通知', '<p>全体用户请注意，服务器将于今晚进行例行维护。</p>', 1, 'SENT', '2025-06-23 11:00:00', '2025-06-23 11:00:00');

-- 插入邮件附件
INSERT INTO `email_attachments` (`email_id`, `file_name`, `file_path`, `file_size`, `file_type`, `created_time`, `updated_time`) VALUES
(101, 'Alpha版本反馈.docx', '/2025/06/20/feedback.docx', 25600, 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', NOW(), NOW());

-- 插入邮件收件人
INSERT INTO `email_recipients` (`email_id`, `recipient_email`, `recipient_type`) VALUES 
(101, 'userA@example.com', 'TO'),
(102, 'userB@example.com', 'TO'),
(102, 'userC@example.com', 'CC'),
(104, 'userA@example.com', 'TO'),
(104, 'userB@example.com', 'TO');

-- 插入邮箱条目
INSERT INTO `mailbox` (`user_id`, `email_id`, `folder`, `is_read`, `is_starred`, `created_time`, `updated_time`) VALUES
(2, 101, 'INBOX', 0, 1, '2025-06-20 10:00:05', '2025-06-20 10:00:05'),
(2, 102, 'SENT', 1, 0, '2025-06-21 14:30:05', '2025-06-21 14:30:05'),
(2, 103, 'DRAFT', 1, 0, '2025-06-22 09:15:05', '2025-06-22 09:15:05'),
(2, 104, 'INBOX', 1, 0, '2025-06-23 11:00:05', '2025-06-23 11:05:00'),
(3, 101, 'SENT', 1, 0, '2025-06-20 10:00:05', '2025-06-20 10:00:05'),
(3, 102, 'INBOX', 0, 0, '2025-06-21 14:30:05', '2025-06-21 14:30:05'),
(3, 104, 'TRASH', 0, 0, '2025-06-23 11:00:05', '2025-06-23 12:00:00'),
(1, 104, 'SENT', 1, 0, '2025-06-23 11:00:05', '2025-06-23 11:00:05');

-- 插入公告数据
INSERT INTO `announcements` (`title`, `content`, `publisher_id`, `status`, `publish_time`, `created_time`, `updated_time`) VALUES
('【重要通知】系统将于今晚升级', '<p>详情请关注后续邮件通知。</p>', 1, 1, NOW(), NOW(), NOW()),
('服务功能优化建议征集（草稿）', '<p>我们计划在下个版本优化XX功能，欢迎大家提供宝贵意见！</p>', 1, 0, NOW(), NOW(), NOW());

-- 插入服务条款
INSERT INTO `service_terms` (`title`, `content`, `version`, `status`, `effective_date`, `created_time`, `updated_time`) VALUES
('邮件系统服务协议 V1.0', '<h1>服务协议内容 V1.0</h1><p>请仔细阅读...</p>', 1, 2, '2024-01-01 00:00:00', NOW(), NOW()),
('邮件系统服务协议 V2.0 (当前生效)', '<h1>服务协议内容 V2.0</h1><p>更新了一些条款...</p>', 2, 1, '2025-01-01 00:00:00', NOW(), NOW());

-- 插入节假日
INSERT INTO `holidays` (`name`, `holiday_date`, `is_workday`, `description`, `created_time`, `updated_time`) VALUES
('元旦节', '2025-01-01', 0, '国家法定节假日', NOW(), NOW()),
('春节调休上班', '2025-02-08', 1, '春节法定调休', NOW(), NOW());

-- 插入垃圾邮件规则
INSERT INTO `spam_rules` (`user_id`, `rule_name`, `rule_type`, `rule_content`, `is_global`, `status`, `created_time`, `updated_time`) VALUES
(2, '屏蔽特定发件人', 'SENDER', 'blockme@spam.com', 0, 1, NOW(), NOW()),
(2, '过滤优惠信息', 'SUBJECT', '优惠券大放送', 0, 1, NOW(), NOW()),
(1, '全局屏蔽词-发票', 'CONTENT', '发票', 1, 1, NOW(), NOW()),
(1, '全局禁用发件人-临时', 'SENDER', 'temp_block@example.com', 1, 0, NOW(), NOW());