ALTER TABLE `b_contract_pdf_analysis_cache`
    ADD COLUMN `adviser_name` VARCHAR(100) NULL DEFAULT NULL
        COMMENT '顾问姓名，对应tb_adviser.name；无法从登录信息确认时为空'
        AFTER `adviser_id`;
