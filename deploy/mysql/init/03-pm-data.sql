SET NAMES utf8mb4;

USE `interview_practice_platform`;

-- 基础会员（level_id=1）的四档联调价格；与客户端 membershipPlans 保持一致。
INSERT IGNORE INTO `membership_price`
  (`level_id`, `duration_type`, `duration_value`, `currency`, `amount`, `status`)
VALUES
  (1, 'TRIAL', 7, 'CNY', 1.00, 1),
  (1, 'MONTH', 1, 'CNY', 29.00, 1),
  (1, 'QUARTER', 3, 'CNY', 79.00, 1),
  (1, 'YEAR', 12, 'CNY', 269.00, 1);

-- 联调账号：admin_test（管理员，role=0）；密码字段为项目 Favre BCrypt cost=12 生成的哈希。
INSERT INTO `user`
  (`id`, `user_account`, `user_password`, `user_name`, `user_role`)
SELECT
  10001,
  'admin_test',
  '$2a$12$exev1yT93i5CJR3wbEFPp.v1JA263hHBG7Xdi4PESwOuHowqKf6ea',
  '联调管理员',
  0
WHERE NOT EXISTS (
  SELECT 1
  FROM `user`
  WHERE `user_account` = 'admin_test'
);

-- 联调账号：user_test（普通用户，role=1）；密码字段为项目 Favre BCrypt cost=12 生成的哈希。
INSERT INTO `user`
  (`id`, `user_account`, `user_password`, `user_name`, `user_role`)
SELECT
  10002,
  'user_test',
  '$2a$12$de4ow1UFJSn.GlapZoGq1emneVyUntowm6QIUKxRXs0JblcqE6Rp.',
  '联调用户',
  1
WHERE NOT EXISTS (
  SELECT 1
  FROM `user`
  WHERE `user_account` = 'user_test'
);
