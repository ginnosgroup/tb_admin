-- 案件流程表：新增顾问、MARA填写的合同表单JSON数据字段。
--
-- contract_str 使用 MEDIUMTEXT，避免合同表单包含较多字段或较长文本时超出 TEXT 上限。
-- 已存在 b_portal_list 表的环境执行一次即可。
ALTER TABLE `b_portal_list`
  ADD COLUMN `contract_str` MEDIUMTEXT DEFAULT NULL COMMENT '顾问、MARA填写的合同表单JSON数据' AFTER `json_str`;
