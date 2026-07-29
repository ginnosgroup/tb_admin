ALTER TABLE `b_contract_pdf_analysis_cache`
    ADD COLUMN `comparison_data` TEXT NULL
        COMMENT '合同提取值与订单比对值JSON' AFTER `validation_result`;
