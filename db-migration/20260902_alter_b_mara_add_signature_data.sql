-- b_mara：新增 MARA 签名文件路径字段。
ALTER TABLE `b_mara`
    ADD COLUMN `signature_data` VARCHAR(512) DEFAULT NULL COMMENT 'MARA签名文件路径' AFTER `image_url`;
