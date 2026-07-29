CREATE TABLE IF NOT EXISTS `b_wecom_chat_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `gmt_create` datetime NOT NULL,
  `gmt_modify` datetime NOT NULL,
  `msg_id` varchar(128) NOT NULL,
  `send_time_epoch_millis` bigint(20) NOT NULL,
  `sender_json` text,
  `receiver_json` mediumtext,
  `chat_id` varchar(128) DEFAULT NULL,
  `msg_type` varchar(64) DEFAULT NULL,
  `secret_key` text NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wecom_chat_message_msg_id` (`msg_id`),
  KEY `idx_wecom_chat_message_send_time` (`send_time_epoch_millis`),
  KEY `idx_wecom_chat_message_chat_time` (`chat_id`, `send_time_epoch_millis`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `b_wecom_chat_message_participant` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `gmt_create` datetime NOT NULL,
  `msg_id` varchar(128) NOT NULL,
  `participant_id` varchar(128) NOT NULL,
  `participant_type` int(11) DEFAULT NULL,
  `participant_role` varchar(16) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wecom_message_participant` (`msg_id`, `participant_id`),
  KEY `idx_wecom_message_participant_user` (`participant_id`, `msg_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `b_wecom_chat_participant` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `gmt_create` datetime NOT NULL,
  `chat_id` varchar(128) NOT NULL,
  `participant_id` varchar(128) NOT NULL,
  `participant_type` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wecom_chat_participant` (`chat_id`, `participant_id`),
  KEY `idx_wecom_chat_participant_user` (`participant_id`, `chat_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `b_wecom_chat_sync_state` (
  `sync_key` varchar(64) NOT NULL,
  `next_cursor` text,
  `has_more` tinyint(4) NOT NULL DEFAULT '0',
  `total_synced` bigint(20) NOT NULL DEFAULT '0',
  `last_sync_time` datetime DEFAULT NULL,
  `last_error` text,
  PRIMARY KEY (`sync_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
