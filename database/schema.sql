CREATE DATABASE IF NOT EXISTS `interview_practice_platform`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

SET NAMES utf8mb4;

USE `interview_practice_platform`;

CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL COMMENT '用户ID，MyBatis-Plus ASSIGN_ID',
  `user_account` VARCHAR(15) NOT NULL COMMENT '用户账号',
  `user_password` VARCHAR(60) NOT NULL COMMENT 'BCrypt 密码哈希',
  `union_id` VARCHAR(128) NULL COMMENT '微信开放平台ID',
  `mp_open_id` VARCHAR(128) NULL COMMENT '微信公众号ID',
  `user_name` VARCHAR(20) NULL COMMENT '用户名称',
  `user_avatar` VARCHAR(1024) NULL COMMENT '用户头像URL',
  `user_role` TINYINT NOT NULL DEFAULT 1 COMMENT '0管理员 1普通用户 2VIP 3封禁',
  `user_profile` VARCHAR(150) NULL COMMENT '用户简介',
  `phone_number` VARCHAR(20) NULL COMMENT '手机号',
  `email` VARCHAR(254) NULL COMMENT '邮箱',
  `grade` TINYINT NULL COMMENT '年级 1-9',
  `work_experience` VARCHAR(150) NULL COMMENT '工作经验',
  `expertise_direction` VARCHAR(150) NULL COMMENT '擅长方向',
  `vip_number` VARCHAR(64) NULL COMMENT '会员编号',
  `vip_code` BIGINT NULL COMMENT '会员兑换码',
  `vip_expire_time` DATETIME NULL COMMENT '会员过期时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `edit_time` DATETIME NULL COMMENT '编辑时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIGINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  `share_code` VARCHAR(64) NULL COMMENT '分享码',
  `invite_user_id` BIGINT NULL COMMENT '邀请用户ID',
  PRIMARY KEY (`id`),
  KEY `idx_user_account_deleted` (`user_account`, `deleted`),
  KEY `idx_user_role_deleted` (`user_role`, `deleted`),
  KEY `idx_user_invite_user_id` (`invite_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `question` (
  `id` BIGINT NOT NULL COMMENT '题目ID，MyBatis-Plus ASSIGN_ID',
  `title` VARCHAR(200) NOT NULL COMMENT '题目名称',
  `content` VARCHAR(500) NOT NULL COMMENT '题目内容',
  `answer` MEDIUMTEXT NOT NULL COMMENT '题目答案',
  `create_user_id` BIGINT NOT NULL COMMENT '创建用户ID',
  `tags` JSON NULL COMMENT '题目标签JSON数组',
  `difficulty` TINYINT NOT NULL DEFAULT 0 COMMENT '难度等级，当前代码校验0-2',
  `view_num` INT NOT NULL DEFAULT 0 COMMENT '浏览量',
  `thumb_num` INT NOT NULL DEFAULT 0 COMMENT '点赞量',
  `favour_num` INT NOT NULL DEFAULT 0 COMMENT '收藏量',
  `need_vip` TINYINT NOT NULL DEFAULT 0 COMMENT '仅会员可见 0否 1是',
  `review_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态 0待审核 1通过 2拒绝',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `edit_time` DATETIME NULL COMMENT '编辑时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIGINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  KEY `idx_question_title_deleted` (`title`, `deleted`),
  KEY `idx_question_creator_deleted` (`create_user_id`, `deleted`),
  KEY `idx_question_review_deleted` (`review_status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目表';

CREATE TABLE IF NOT EXISTS `question_review` (
  `id` BIGINT NOT NULL COMMENT '审核记录ID，MyBatis-Plus ASSIGN_ID',
  `question_id` BIGINT NOT NULL COMMENT '题目ID',
  `reviewer_id` BIGINT NOT NULL COMMENT '审核人ID',
  `review_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态 0待审核 1通过 2拒绝',
  `review_message` VARCHAR(500) NULL COMMENT '审核意见',
  `review_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_question_review_question` (`question_id`),
  KEY `idx_question_review_status_time` (`review_status`, `review_time`),
  KEY `idx_question_review_reviewer` (`reviewer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目审核表';

CREATE TABLE IF NOT EXISTS `question_bank` (
  `id` BIGINT NOT NULL COMMENT '题库ID，MyBatis-Plus ASSIGN_ID',
  `title` VARCHAR(50) NOT NULL COMMENT '题库名称',
  `picture` VARCHAR(1024) NULL COMMENT '题库图片URL',
  `create_user_id` BIGINT NOT NULL COMMENT '创建用户ID',
  `description` VARCHAR(200) NULL COMMENT '题库描述',
  `question_count` INT NOT NULL DEFAULT 0 COMMENT '题目数量',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `edit_time` DATETIME NULL COMMENT '编辑时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIGINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_question_bank_title_deleted` (`title`, `deleted`),
  KEY `idx_question_bank_creator_deleted` (`create_user_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题库表';

CREATE TABLE IF NOT EXISTS `question_bank_question` (
  `id` BIGINT NOT NULL COMMENT '关联ID，MyBatis-Plus ASSIGN_ID',
  `question_bank_id` BIGINT NOT NULL COMMENT '题库ID',
  `question_id` BIGINT NOT NULL COMMENT '题目ID',
  `create_user_id` BIGINT NOT NULL COMMENT '创建用户ID',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_qbq_bank_question` (`question_bank_id`, `question_id`),
  KEY `idx_qbq_question` (`question_id`),
  KEY `idx_qbq_creator` (`create_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题库题目关联表';

CREATE TABLE IF NOT EXISTS `user_thumb` (
  `id` BIGINT NOT NULL COMMENT '点赞ID，MyBatis-Plus ASSIGN_ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `question_id` BIGINT NOT NULL COMMENT '题目ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_thumb_user_question` (`user_id`, `question_id`),
  KEY `idx_user_thumb_question` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户点赞表';

CREATE TABLE IF NOT EXISTS `user_favourite` (
  `id` BIGINT NOT NULL COMMENT '收藏ID，MyBatis-Plus ASSIGN_ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `question_id` BIGINT NOT NULL COMMENT '题目ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_favourite_user_question` (`user_id`, `question_id`),
  KEY `idx_user_favourite_question` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏表';

CREATE TABLE IF NOT EXISTS `membership_price` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `level_id` INT NOT NULL COMMENT '会员等级ID，1基础 2高级 3企业',
  `duration_type` VARCHAR(16) NOT NULL COMMENT 'TRIAL/MONTH/QUARTER/YEAR',
  `duration_value` INT NOT NULL COMMENT '时长数值',
  `currency` CHAR(3) NOT NULL DEFAULT 'CNY' COMMENT 'ISO 4217货币代码',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '定价金额',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_membership_price_lookup`
    (`level_id`, `currency`, `duration_type`, `duration_value`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员定价配置表';

CREATE TABLE IF NOT EXISTS `membership_order` (
  `id` BIGINT NOT NULL COMMENT '主键ID，MyBatis-Plus ASSIGN_ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `out_order_no` VARCHAR(64) NOT NULL COMMENT '平台订单号',
  `order_no` VARCHAR(128) NULL COMMENT '第三方支付订单号',
  `level_id` INT NOT NULL COMMENT '会员等级ID',
  `duration_type` VARCHAR(16) NOT NULL COMMENT 'TRIAL/MONTH/QUARTER/YEAR',
  `duration_value` INT NOT NULL COMMENT '时长数值',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '实际支付金额',
  `currency` CHAR(3) NOT NULL DEFAULT 'CNY' COMMENT 'ISO 4217货币代码',
  `payment_status` VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT '支付状态',
  `payment_method` VARCHAR(16) NOT NULL DEFAULT 'ALIPAY' COMMENT '支付方式',
  `start_time` DATETIME NOT NULL COMMENT '会员生效时间',
  `end_time` DATETIME NOT NULL COMMENT '会员到期时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_membership_order_out_order_no` (`out_order_no`),
  UNIQUE KEY `uk_membership_order_order_no` (`order_no`),
  KEY `idx_membership_order_active`
    (`user_id`, `payment_status`, `start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员订单表';
