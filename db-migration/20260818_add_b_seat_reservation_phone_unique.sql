-- 座位预约表：选座请求的电话号码不允许重复。
--
-- 后端在插入前已用 getByPhone 做校验，但数据库缺少 phone 唯一索引，
-- 并发请求下多个相同电话可能同时通过校验并插入成功（与 seat_code / email 相比少了 DB 层兜底）。
-- 增加唯一索引后，数据库层面保证同一电话号码只能预约一次。
--
-- 注意：执行前请先确认历史数据中不存在重复电话号码：
--   SELECT phone, COUNT(*) c FROM b_seat_reservation GROUP BY phone HAVING c > 1;
-- 如有重复记录，需先人工去重后再执行本迁移。
ALTER TABLE `b_seat_reservation`
  ADD UNIQUE KEY `uk_b_seat_reservation_phone` (`phone`);
