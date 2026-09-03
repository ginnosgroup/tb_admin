-- 座位预约表索引修复（本地和线上数据库都要执行一次）：
--
-- 1) 取消选座的 IP 唯一限制：只按姓名和邮箱验证，允许多人共用同一 IP 预约。
-- 2) 修正 uk_b_seat_reservation_consultant_sequence 唯一索引：
--    之前错误地建在 (consultant_code, consultant_sequence) 上，
--    而顾问 code（01/02...）是所有顾问共用的序号，不同顾问第一次预约都会是 01/1，
--    导致新顾问（如 Jay01）第一次预约与已有顾问的第一条记录撞唯一键，报"顾问code生成失败"。
--    正确索引应为 (consultant_name, consultant_sequence)，与后端按顾问姓名分组的逻辑一致。
ALTER TABLE `b_seat_reservation`
  DROP INDEX `uk_b_seat_reservation_ip`,
  DROP INDEX `uk_b_seat_reservation_consultant_sequence`,
  ADD UNIQUE KEY `uk_b_seat_reservation_consultant_sequence` (`consultant_name`, `consultant_sequence`);
