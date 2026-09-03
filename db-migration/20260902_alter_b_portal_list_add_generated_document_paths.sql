-- b_portal_list保存02B状态生成的合同和Letter文件访问路径。
-- 已存在b_portal_list表的环境执行一次即可。
ALTER TABLE `b_portal_list`
  ADD COLUMN `contract_file_path` varchar(500) DEFAULT NULL COMMENT '生成后的合同文件访问路径'
    AFTER `contract_str`,
  ADD COLUMN `letter_file_path` varchar(500) DEFAULT NULL COMMENT '生成后的Letter文件访问路径'
    AFTER `contract_file_path`;
