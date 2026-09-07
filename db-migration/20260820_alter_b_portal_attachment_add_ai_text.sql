-- 案件附件表：新增 AI 提取的文字内容字段。
--
-- 附件上传成功后，后端调取 DeepSeek 对图片/PDF 进行文字提取，
-- 提取结果保存到本字段；提取失败或附件不支持时该字段为 NULL，不影响附件上传主流程。
-- 本地和线上数据库都要执行一次。
ALTER TABLE `b_portal_attachment`
  ADD COLUMN `ai_text` MEDIUMTEXT DEFAULT NULL COMMENT 'AI提取的文字内容' AFTER `stage`;
