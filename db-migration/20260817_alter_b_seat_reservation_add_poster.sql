ALTER TABLE `b_seat_reservation`
  ADD COLUMN `poster_url` varchar(255) DEFAULT NULL COMMENT '本次预约随机生成的电影海报路径'
    AFTER `consultant_sequence`;

UPDATE `b_seat_reservation`
SET `poster_url` = '/webroot_new/seat-posters/seat-poster-1.jpg'
WHERE `poster_url` IS NULL;
