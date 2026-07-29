ALTER TABLE `b_contract_pdf_analysis_cache`
    ADD COLUMN `adviser_id` INT(11) NULL DEFAULT NULL
        COMMENT '顾问ID，对应tb_adviser.id；无法从登录信息确认时为空'
        AFTER `request_user_id`,
    ADD INDEX `idx_b_contract_pdf_analysis_cache_adviser_id` (`adviser_id`);
