-- 案件流程表：新增语聚AI返回的咨询内容字段。
--
-- updatePortal 更新案件后调用语聚AI获取485方案，返回结果中的 content 保存到本字段；
-- AI未调用或返回无content时该字段为NULL，不影响案件主流程。
-- 本地和线上数据库都要执行一次。
ALTER TABLE `b_portal_list`
  ADD COLUMN `ai_consult_content` MEDIUMTEXT DEFAULT NULL COMMENT '语聚AI返回的485方案咨询内容' AFTER `json_str`;
