-- 票根和邮件海报分别保存，路径与 ServiceOrderController.uploadImage 使用的 /data + 相对路径规则一致。
ALTER TABLE `b_seat_reservation`
  ADD COLUMN `ticket_image_path` varchar(512) DEFAULT NULL COMMENT '查询页面展示的完整票根图片路径'
    AFTER `poster_url`,
  ADD COLUMN `email_image_path` varchar(512) DEFAULT NULL COMMENT '邮件发送的独立海报图片路径'
    AFTER `ticket_image_path`;

-- 兼容已有记录：旧记录仍可查询，但没有新生成的两张图片时，发送邮件会提示重新生成票根。
