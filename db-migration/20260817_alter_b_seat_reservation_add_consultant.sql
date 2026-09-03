ALTER TABLE `b_seat_reservation`
  ADD COLUMN `consultant_name` varchar(128) DEFAULT NULL COMMENT '顾问姓名' AFTER `ip`,
  ADD COLUMN `consultant_code` varchar(64) DEFAULT NULL COMMENT '顾问code' AFTER `consultant_name`,
  ADD COLUMN `consultant_sequence` int(11) DEFAULT NULL COMMENT '顾问第几次记录，从1开始' AFTER `consultant_code`,
  ADD UNIQUE KEY `uk_b_seat_reservation_consultant_sequence` (`consultant_name`, `consultant_sequence`);
