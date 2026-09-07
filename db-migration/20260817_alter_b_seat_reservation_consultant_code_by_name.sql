ALTER TABLE `b_seat_reservation`
  DROP INDEX `uk_b_seat_reservation_consultant_sequence`,
  ADD UNIQUE KEY `uk_b_seat_reservation_consultant_sequence`
    (`consultant_name`, `consultant_sequence`);
