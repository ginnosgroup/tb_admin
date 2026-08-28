ALTER TABLE `b_contract_pdf_analysis_cache`
    ADD COLUMN `file_path` VARCHAR(1024) NULL DEFAULT NULL
        COMMENT '文件上传后的访问路径' AFTER `file_name`,
    ADD COLUMN `validation_result` TEXT NULL
        COMMENT '业务校验结果，内容与原前端失败提示一致' AFTER `analysis_result`;
