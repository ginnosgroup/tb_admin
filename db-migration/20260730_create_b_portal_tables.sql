-- 案件流程表
CREATE TABLE `b_portal_list` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `type_id` int(11) DEFAULT NULL COMMENT '案件类型ID，关联portal_type表',
  `case_type` varchar(64) DEFAULT NULL COMMENT '案件类型标识',
  `name` varchar(64) DEFAULT NULL COMMENT '客户姓名',
  `gender` varchar(16) DEFAULT NULL COMMENT '性别',
  `birthday` datetime DEFAULT NULL COMMENT '生日',
  `passport` varchar(64) DEFAULT NULL COMMENT '护照号码',
  `english_score` varchar(64) DEFAULT NULL COMMENT '英语成绩',
  `completion_date` datetime DEFAULT NULL COMMENT '完成信日期',
  `visa_expiration_date` datetime DEFAULT NULL COMMENT '签证到期时间',
  `exam_results_date` datetime DEFAULT NULL COMMENT '考试成绩日期',
  `student_visa_expiration_date` datetime DEFAULT NULL COMMENT '学签到期时间',
  `has_completion_letter` tinyint(1) DEFAULT NULL COMMENT '是否有完成信 0否 1是',
  `json_str` text COMMENT '其他表单字段json，类似服务订单打分那种',
  `contract_str` mediumtext COMMENT '顾问、MARA填写的合同表单JSON数据',
  `contract_file_path` varchar(500) DEFAULT NULL COMMENT '生成后的合同文件访问路径',
  `letter_file_path` varchar(500) DEFAULT NULL COMMENT '生成后的Letter文件访问路径',
  `ai_consult_content` MEDIUMTEXT DEFAULT NULL COMMENT '语聚AI返回的485方案咨询内容',
  `adviser_id` int(11) DEFAULT NULL COMMENT '顾问ID',
  `official_id` int(11) DEFAULT NULL COMMENT '文案ID',
  `mara_id` int(11) DEFAULT NULL COMMENT 'Mara ID',
  `service_order_id` int(11) DEFAULT NULL COMMENT '服务订单ID，后续需要绑定',
  `str_state` varchar(32) DEFAULT NULL COMMENT '流程状态：ALL(全部案件)/PROCESSING(进行中案件)/COMPLETED(已结案)',
  PRIMARY KEY (`id`),
  KEY `idx_b_portal_list_type_id` (`type_id`),
  KEY `idx_b_portal_list_case_type` (`case_type`),
  KEY `idx_b_portal_list_adviser_id` (`adviser_id`),
  KEY `idx_b_portal_list_official_id` (`official_id`),
  KEY `idx_b_portal_list_mara_id` (`mara_id`),
  KEY `idx_b_portal_list_service_order_id` (`service_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='案件流程表';

-- 案件类型表
CREATE TABLE `b_portal_type` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `name` varchar(64) NOT NULL COMMENT '案件类型名称',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序',
  `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0否 1是',
  PRIMARY KEY (`id`),
  KEY `idx_b_portal_type_is_delete` (`is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='案件类型表';

-- 案件附件表
CREATE TABLE `b_portal_attachment` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `portal_id` int(11) DEFAULT NULL COMMENT '案件ID，关联b_portal_list.id',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `file_name` varchar(255) DEFAULT NULL COMMENT '原始文件名',
  `file_path` varchar(255) DEFAULT NULL COMMENT '文件实际存储路径',
  `file_size` bigint(20) DEFAULT NULL COMMENT '文件大小(字节)',
  `file_type` varchar(128) DEFAULT NULL COMMENT '文件类型/MIME',
  `file_ext` varchar(16) DEFAULT NULL COMMENT '扩展名',
  `stage` varchar(32) DEFAULT NULL COMMENT '阶段',
  `ai_text` MEDIUMTEXT DEFAULT NULL COMMENT 'AI提取的文字内容',
  PRIMARY KEY (`id`),
  KEY `idx_b_portal_attachment_portal_id` (`portal_id`),
  KEY `idx_b_portal_attachment_file_path` (`file_path`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='案件附件表';

-- 案件操作日志表
CREATE TABLE `b_portal_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `portal_id` int(11) NOT NULL COMMENT '案件ID，关联b_portal_list.id',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `operator_id` int(11) DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(64) DEFAULT NULL COMMENT '操作人姓名',
  `role` varchar(32) DEFAULT NULL COMMENT '操作人角色：客户/顾问/文案/mara/超管',
  `action` varchar(64) DEFAULT NULL COMMENT '操作类型',
  `from_state` varchar(32) DEFAULT NULL COMMENT '操作前状态',
  `to_state` varchar(32) DEFAULT NULL COMMENT '操作后状态',
  `content` varchar(512) DEFAULT NULL COMMENT '操作说明',
  `ip` varchar(64) DEFAULT NULL COMMENT '操作IP',
  `user_agent` varchar(512) DEFAULT NULL COMMENT '浏览器/客户端信息',
  PRIMARY KEY (`id`),
  KEY `idx_b_portal_log_portal_id` (`portal_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='案件操作日志表';
