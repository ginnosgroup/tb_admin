--alter table tb_subject add `type` varchar(8) NOT NULL DEFAULT 'DEFAULT' COMMENT '类型 (DEFAULT:大团, INDIE:小团, CHILD:子团)';
--alter table tb_subject add `parent_id` int DEFAULT 0 COMMENT '父编号,仅CHILD团有效 (对应tb_subject.id)';

-- ----------团购----------

CREATE DATABASE tbdb;


-- 区域
CREATE TABLE `tb_region` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `name` varchar(32) NOT NULL COMMENT '区域名称',
  `parent_id` int DEFAULT NULL COMMENT '父区域编号',
  `weight` int NOT NULL DEFAULT 1 COMMENT '排序权重'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;
ALTER TABLE `tb_region` ADD INDEX index_name (`parent_id`);

-- 课程类目
CREATE TABLE `tb_subject_category` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `name` varchar(32) NOT NULL COMMENT '类目名称',
  `state` varchar(8) NOT NULL COMMENT '状态 (ENABLED:显示,DISABLED:不显示,DELETE:已删除)',
  `weight` int NOT NULL DEFAULT 1 COMMENT '排序权重'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;
ALTER TABLE `tb_subject_category` ADD INDEX index_name (`state`);

-- 课程
CREATE TABLE `tb_subject` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `name` varchar(32) NOT NULL COMMENT '名称',
  `type` varchar(8) NOT NULL DEFAULT 'DEFAULT' COMMENT '类型 (DEFAULT:大团, INDIE:小团, CHILD:子团)',
  `parent_id` int DEFAULT 0 COMMENT '父编号,仅CHILD团有效 (对应tb_subject.id)',
  `logo` varchar(128) DEFAULT NULL COMMENT 'Logo图片地址',
  `price` decimal(8,2) NOT NULL DEFAULT '0.00' COMMENT '原始价格',
  `start_date` datetime NOT NULL COMMENT '拼团开始时间',
  `end_date` datetime NOT NULL COMMENT '拼团结束时间',
  `state` varchar(8) NOT NULL COMMENT '状态 (WAIT:未开始,START:拼团中,END:已结束,STOP:已终止,DELETE:已删除)',
  `category_id` int DEFAULT NULL COMMENT '所属类目编号 (关联tb_subject_category.id)',
  `pre_amount` decimal(8,2) NOT NULL DEFAULT '0.00' COMMENT '预付款金额',
  `codex` varchar(255) DEFAULT NULL COMMENT '拼团规则',
  `details` varchar(2048) DEFAULT NULL COMMENT '详情',
  `region_ids` varchar(255) NOT NULL COMMENT '所属区域编号(关联tb_region.id,多个区域以","分隔)',
  `weight` int NOT NULL DEFAULT 1 COMMENT '排序权重'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;
ALTER TABLE `tb_subject` ADD INDEX index_name (`state`,`category_id`);

-- 课程拼团价格区间
CREATE TABLE `tb_subject_price_interval` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `subject_id` int NOT NULL COMMENT '所属课程编号 (对应tb_subject.id)',
  `start_num` int NOT NULL COMMENT '开始数量',
  `end_num` int DEFAULT NULL COMMENT '结束数量',
  `region_ids` varchar(255) NOT NULL COMMENT '区域价格(以"[区域]:[价格]"方式表示,多个记录以","分隔)'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;
ALTER TABLE `tb_subject_price_interval` ADD INDEX index_name (`subject_id`);

-- 订单
CREATE TABLE `tb_order` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `name` varchar(32) NOT NULL COMMENT '名称',
  `state` varchar(8) NOT NULL DEFAULT 'NEW' COMMENT '状态 (NEW:待付款,WAIT:待成团,SUCCESS:已成团,END:未成团)',
  `subject_id` int NOT NULL COMMENT '所属课程编号 (对应tb_subject.id)',
  `num` int NOT NULL DEFAULT 1 COMMENT '购买数量',
  `amount` decimal(8,2) NOT NULL DEFAULT '0.00' COMMENT '应付金额 (应该支付给第三方平台的金额,不包含余额抵扣)',
  `pay_type` varchar(8) NOT NULL COMMENT '支付类型 (PAYPAL:PayPal,WECHAT:微信支付,IOSPAY:IOS第三方支付,OTHER:其它支付方式)',
  `pay_code` varchar(64) DEFAULT NULL COMMENT '支付编码',
  `pay_amount` decimal(8,2) NOT NULL DEFAULT '0.00' COMMENT '已支付金额',
  `pay_date` datetime DEFAULT NULL COMMENT '支付时间',
  `create_price` decimal(8,2) NOT NULL DEFAULT '0.00' COMMENT '下团价格',
  `finish_price` decimal(8,2) DEFAULT NULL COMMENT '成团价格',
  `remain_pay_amount` decimal(8,2) NOT NULL DEFAULT '0.00' COMMENT '尾款已支付金额',
  `remain_pay_date` datetime DEFAULT NULL COMMENT '尾款支付时间',
  `remain_pay_balance` decimal(8,2) NOT NULL DEFAULT '0.00' COMMENT '尾款余额抵扣金额',
  `user_id` int NOT NULL COMMENT '所属顾客编号 (对应tb_user.id)',
  `introducer_user_id` int DEFAULT NULL COMMENT '介绍人编号 (对应tb_user.id)',
  `adviser_id` int DEFAULT NULL COMMENT '所属顾问编号 (对应tb_adviser.id)',
  `adviser_date` datetime DEFAULT NULL COMMENT '分配顾问时间',
  `region_id` int NOT NULL COMMENT '所属区域编号 (对应tb_region.id)'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;
ALTER TABLE `tb_order` ADD INDEX index_name (`subject_id`,`user_id`,`adviser_id`);

-- 顾问
CREATE TABLE `tb_adviser` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `name` varchar(32) NOT NULL COMMENT '名称',
  `phone` varchar(16) NOT NULL COMMENT '电话号码',
  `email` varchar(128) NOT NULL COMMENT '邮箱',
  `state` varchar(8) NOT NULL COMMENT '状态 (ENABLED:激活,DISABLED:禁止)',
  `image_url` varchar(128) DEFAULT NULL COMMENT '图片地址',
  `region_id` int NOT NULL COMMENT '所属区域编号 (对应tb_region.id)'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;
ALTER TABLE `tb_adviser` ADD INDEX index_name (`state`);

-- 客户
CREATE TABLE `tb_user` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `name` varchar(32) NOT NULL COMMENT '名称',
  `birthday` datetime NOT NULL COMMENT '生日',
  `phone` varchar(16) NOT NULL COMMENT '电话号码',
  `email` varchar(128) NOT NULL COMMENT '邮箱',
  `wechat_username` varchar(64) DEFAULT NULL COMMENT '微信帐号',
  `first_controller_contents` varchar(255) DEFAULT NULL COMMENT '初次咨询记录',
  `visa_code` varchar(8) DEFAULT NULL COMMENT '签证编号',
  `visa_expiration_date` datetime DEFAULT NULL COMMENT '签证到期日期',
  `source` varchar(32) DEFAULT NULL COMMENT '客户来源',
  `auth_type` varchar(16) NOT NULL COMMENT '登录授权分类 (WECHAT:微信,IOS_WECHAT:IOS微信,FACEBOOK:facebook,BROKERAGE:佣金系统用户,V:虚拟用户)',
  `auth_openid` varchar(64) NOT NULL COMMENT '授权帐号编号',
  `auth_username` varchar(64) DEFAULT NULL COMMENT '授权帐号',
  `auth_nickname` varchar(128) DEFAULT NULL COMMENT '授权帐号用户昵称',
  `auth_logo` varchar(255) DEFAULT NULL COMMENT '授权帐号Logo地址',
  `recommend_openid` varchar(64) DEFAULT NULL COMMENT '推荐人授权帐号编号',
  `balance` decimal(8,2) NOT NULL DEFAULT '0.00' COMMENT '余额',
  `adviser_id` int DEFAULT NULL COMMENT '所属顾问编号 (对应tb_adviser.id)',
  `region_id` int NOT NULL COMMENT '所属区域编号 (对应tb_region.id)'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;
ALTER TABLE `tb_user` ADD INDEX index_name (`region_id`);

/*
CREATE TABLE `tb_user_auth` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `user_id` int NOT NULL COMMENT '所属顾客编号 (对应tb_user.id)',
  `auth_type` varchar(128) NOT NULL COMMENT '登录授权分类 (WECHAT:微信,IOS_WECHAT:IOS微信,FACEBOOK:facebook)',
  `auth_openid` varchar(64) NOT NULL COMMENT '授权帐号编号',
  `auth_username` varchar(64) DEFAULT NULL COMMENT '授权帐号',
  `auth_nickname` varchar(128) DEFAULT NULL COMMENT '授权帐号用户昵称',
  `auth_logo` varchar(128) DEFAULT NULL COMMENT '授权帐号Logo地址'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;
ALTER TABLE `tb_user_auth` ADD INDEX index_name (`user_id`);
*/

-- 客户支付日志
CREATE TABLE `tb_pay_log` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `user_id` int NOT NULL COMMENT '所属顾客编号 (对应tb_user.id)',
  `order_id` int NOT NULL COMMENT '所属订单编号 (对应tb_order.id)',
  `pay_type` varchar(8) NOT NULL COMMENT '支付类型 (PAYPAL:PayPal,WECHAT:微信支付,BALANCE:余额,OTHER:其它支付方式)',
  `pay_code` varchar(64) DEFAULT NULL COMMENT '支付编码',
  `pay_amount` decimal(8,2) NOT NULL DEFAULT '0.00' COMMENT '支付金额',
  `pay_date` datetime NOT NULL COMMENT '支付时间'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

-- 管理员信息
CREATE TABLE `tb_admin_user` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `username` varchar(32) NOT NULL COMMENT '管理员账户名称',
  `password` varchar(32) NOT NULL COMMENT '登录密码 (MD5加密)',
  `ap_list` varchar(128) DEFAULT NULL COMMENT '权限列表 (KJ:会计,GW:顾问,AD:管理员,TGAD:团购管理员;如果为NULL则为超级管理员,显示所有选项)',
  `adviser_id` int DEFAULT NULL COMMENT '所属顾问编号 (对应tb_adviser.id)',
  `session_id` varchar(255) DEFAULT NULL COMMENT '当前session_id值',
  `gmt_login` datetime NOT NULL COMMENT '最后登录时间',
  `login_ip` varchar(50) NOT NULL COMMENT '最后登录IP',
  `status` varchar(8) NOT NULL DEFAULT 'ENABLED' COMMENT '账户状态标识 (ENABLED:可用,DISABLED:不可用)'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

-- ----------佣金系统相关表----------

-- 签证类
CREATE TABLE `b_visa` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `handling_date` datetime NOT NULL COMMENT '办理日期',
  `user_id` int NOT NULL COMMENT '用户编号 (对应tb_user.id)',
  `receive_type_id` int NOT NULL COMMENT '收款方式编号(对应b_receive_type.id)',
  `receive_date` datetime NOT NULL COMMENT '收款日期',
  `service_id` int NOT NULL COMMENT '移民-服务项目编号 (对应b_service.id)',
  `receivable` decimal(8,2) NOT NULL COMMENT '总计应收',
  `received` decimal(8,2) NOT NULL COMMENT '总计已收',
  `amount` decimal(8,2) NOT NULL COMMENT '本次收款',
  `gst` decimal(8,2) NOT NULL COMMENT 'GST',
  `deduct_gst` decimal(8,2) NOT NULL COMMENT 'Deduct GST',
  `bonus` decimal(8,2) NOT NULL COMMENT '月奖金',
  `adviser_id` int NOT NULL COMMENT '顾问编号 (对应tb_adviser.id)',
  `official_id` int NOT NULL COMMENT '文案编号 (对应b_official.id)',
`remarks` varchar(255) DEFAULT NULL COMMENT '备注',
  `is_close` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已取消'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

-- 移民佣金
CREATE TABLE `b_brokerage` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `handling_date` datetime NOT NULL COMMENT '办理日期',
  `user_id` int NOT NULL COMMENT '用户编号 (对应tb_user.id)',
  `receive_type_id` int NOT NULL COMMENT '收款方式编号(对应b_receive_type.id)',
  `receive_date` datetime NOT NULL COMMENT '收款日期',
  `service_id` int NOT NULL COMMENT '移民-服务项目编号 (对应b_service.id)',
  `receivable` decimal(8,2) NOT NULL COMMENT '总计应收',
  `received` decimal(8,2) NOT NULL COMMENT '总计已收',
  `amount` decimal(8,2) NOT NULL COMMENT '本次收款',
  `gst` decimal(8,2) NOT NULL COMMENT 'GST',
  `deduct_gst` decimal(8,2) NOT NULL COMMENT 'Deduct GST',
  `bonus` decimal(8,2) NOT NULL COMMENT '月奖金',
  `adviser_id` int NOT NULL COMMENT '顾问编号 (对应tb_adviser.id)',
  `official_id` int NOT NULL COMMENT '文案编号 (对应b_official.id)',
  `remarks` varchar(255) DEFAULT NULL COMMENT '备注',
  `is_close` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已取消'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

-- 留学-提前结佣
CREATE TABLE `b_brokerage_sa` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `handling_date` datetime NOT NULL COMMENT '办理日期',
  `user_id` int NOT NULL COMMENT '用户编号 (对应tb_user.id)',
  `receive_type_id` int NOT NULL COMMENT '收款方式编号(对应b_receive_type.id)',
  `school_id` int NOT NULL COMMENT '学校编号 (对应b_school.id)',
  `start_date` datetime NOT NULL COMMENT '开课日期',
  `end_date` datetime NOT NULL COMMENT '结束日期',
  `tuition_fee` decimal(8,2) NOT NULL COMMENT '学费',
  `commission` decimal(8,2) NOT NULL COMMENT '手续费',
  `gst` decimal(8,2) NOT NULL COMMENT 'GST',
  `deduct_gst` decimal(8,2) NOT NULL COMMENT 'Deduct GST',
  `bonus` decimal(8,2) NOT NULL COMMENT '月奖金',
  `adviser_id` int NOT NULL COMMENT '顾问编号 (对应tb_adviser.id)',
  `official_id` int NOT NULL COMMENT '文案编号 (对应b_official.id)',
  `remarks` varchar(255) DEFAULT NULL COMMENT '备注',
  `is_close` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已取消'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

-- 留学-学校待结佣
CREATE TABLE `b_school_brokerage_sa` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `handling_date` datetime NOT NULL COMMENT '办理日期',
  `user_id` int NOT NULL COMMENT '用户编号 (对应tb_user.id)',
  `school_id` int NOT NULL COMMENT '学校编号 (对应b_school.id)',
  `student_code` varchar(32) NOT NULL COMMENT '学号',
  `start_date` datetime NOT NULL COMMENT '开课日期',
  `end_date` datetime NOT NULL COMMENT '结束日期',
  `tuition_fee` decimal(8,2) NOT NULL COMMENT '学费',
  `first_term_tuition_fee` decimal(8,2) NOT NULL COMMENT '第一学期学费',
  `commission` decimal(8,2) NOT NULL COMMENT '手续费',
  `gst` decimal(8,2) NOT NULL COMMENT 'GST',
  `deduct_gst` decimal(8,2) NOT NULL COMMENT 'Deduct GST',
  `bonus` decimal(8,2) NOT NULL COMMENT '月奖金',
  `pay_date` datetime DEFAULT NULL COMMENT '学校支付日期',
  `invoice_code` varchar(32) DEFAULT NULL COMMENT '发票编号',
  `pay_amount` decimal(8,2) DEFAULT NULL COMMENT '学校支付金额',
  `subagency_id` int DEFAULT NULL COMMENT '代理编号 (对应b_subagency.id)',
  `adviser_id` int NOT NULL COMMENT '顾问编号 (对应tb_adviser.id)',
  `official_id` int NOT NULL COMMENT '文案编号 (对应b_official.id)',
  `remarks` varchar(255) DEFAULT NULL COMMENT '备注',
  `is_settle_accounts` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已结佣',
  `is_close` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已取消'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

-- 退款
CREATE TABLE `b_refund` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `handling_date` datetime NOT NULL COMMENT '办理日期',
  `user_id` int NOT NULL COMMENT '用户编号 (对应tb_user.id)',
  `name` varchar(32) NOT NULL COMMENT '项目名称',
  `receive_type_id` int NOT NULL COMMENT '收款方式编号(对应b_receive_type.id)',
  `amount` decimal(8,2) NOT NULL COMMENT '已收款金额',
  `pre_refund_amount` decimal(8,2) NOT NULL COMMENT '申请退款金额',
  `bank_name` varchar(32) DEFAULT NULL COMMENT '银行名称',
  `bank_account` varchar(32) DEFAULT NULL COMMENT '银行帐号',
  `bsb` varchar(8) DEFAULT NULL COMMENT 'Bank, State & Branch Code (Australia)',
  `refund_date` datetime DEFAULT NULL COMMENT '实际退款时间',
  `refund_amount` decimal(8,2) DEFAULT NULL COMMENT '已退款金额',
  `gst` decimal(8,2) NOT NULL COMMENT 'GST',
  `deduct_gst` decimal(8,2) NOT NULL COMMENT 'Deduct GST',
  `refund` decimal(8,2) NOT NULL COMMENT 'Refund',
  `adviser_id` int NOT NULL COMMENT '顾问编号 (对应tb_adviser.id)',
  `official_id` int NOT NULL COMMENT '文案编号 (对应b_official.id)',
  `remarks` varchar(255) DEFAULT NULL COMMENT '备注',
  `is_close` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已取消'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

-- 移民-服务项目
CREATE TABLE `b_service` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `code` varchar(8) DEFAULT NULL COMMENT '项目编码',
  `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已删除'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

-- 留学-学校
CREATE TABLE `b_school` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `name` varchar(32) NOT NULL COMMENT '名称',
  `subject` varchar(128) NOT NULL COMMENT '课程',
  `country` varchar(4) NOT NULL COMMENT '国家编码',
  `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已删除'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

-- Subagency
CREATE TABLE `b_subagency` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `name` varchar(32) NOT NULL COMMENT '名称',
  `commission_rate` decimal(8,2) NOT NULL COMMENT 'Commission Rate',
  `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已删除'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

-- 文案
CREATE TABLE `b_official` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `name` varchar(32) NOT NULL COMMENT '名称',
  `phone` varchar(16) NOT NULL COMMENT '电话号码',
  `email` varchar(128) NOT NULL COMMENT '邮箱',
  `state` varchar(8) NOT NULL COMMENT '状态 (ENABLED:激活,DISABLED:禁止)',
  `image_url` varchar(128) DEFAULT NULL COMMENT '图片地址',
  `region_id` int NOT NULL COMMENT '所属区域编号 (对应tb_region.id)'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

-- 收款方式
CREATE TABLE `b_receive_type` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `name` varchar(32) NOT NULL COMMENT '名称',
  `state` varchar(8) NOT NULL COMMENT '状态 (ENABLED:显示,DISABLED:不显示)',
  `weight` int NOT NULL DEFAULT 1 COMMENT '排序权重'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

-- 提醒设置
CREATE TABLE `b_remind` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `school_brokerage_sa_id` int DEFAULT NULL COMMENT '学校待结佣编号 (对应b_school_brokerage_sa.id)',
  `visa_id` int DEFAULT NULL COMMENT '签证类编号 (对应b_visa.id)',
  `brokerage_sa_id` int DEFAULT NULL COMMENT '提前结佣编号 (对应b_brokerage_sa.id)',
  `remind_date` datetime NOT NULL COMMENT '提醒日期',
  `state` varchar(8) NOT NULL COMMENT '状态 (ENABLED:显示,DISABLED:不显示)'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

-- 来源渠道
CREATE TABLE `b_source` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `name` varchar(32) DEFAULT NULL COMMENT '渠道名称',
  `source_region_id` int DEFAULT 0 COMMENT '所属来源渠道区域编号'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

-- 来源渠道区域
CREATE TABLE `b_source_region` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `name` varchar(32) DEFAULT NULL COMMENT '区域名称',
  `parent_id` int DEFAULT 0 COMMENT '父区域编号'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

-- ----------V2.1相关表----------

-- 客户咨询表
CREATE TABLE `tb_consultation` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `user_id` int NOT NULL COMMENT '所属顾客编号 (对应tb_user.id)',
  `contents` varchar(255) NOT NULL COMMENT '咨询内容',
  `state` varchar(8) NOT NULL COMMENT '状态 (ENABLED:显示,DISABLED:不显示)',
  `remind_date` datetime DEFAULT NULL COMMENT '提醒日期',
  `remind_contents` varchar(255) DEFAULT NULL COMMENT '提醒内容'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

-- ----------V2.2相关表----------

-- 知识库
CREATE TABLE `b_knowledge` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `title` varchar(32) DEFAULT NULL COMMENT '知识库名称',
  `content` varchar(5000) DEFAULT NULL COMMENT '知识库内容',
  `knowledge_menu_id` int DEFAULT 0 COMMENT '所属菜单编号'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

-- 知识库菜单
CREATE TABLE `b_knowledge_menu` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `name` varchar(32) DEFAULT NULL COMMENT '菜单名称',
  `parent_id` int DEFAULT 0 COMMENT '父菜单编号'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

-- 留言主题
CREATE TABLE `b_message_topic` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `user_id` int NOT NULL COMMENT '所属顾客编号 (对应tb_user.id)',
  `title` varchar(32) NOT NULL COMMENT '留言名称',
  `content` varchar(255) NOT NULL COMMENT '留言内容'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

-- 留言回复信息
CREATE TABLE `b_message` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `user_id` int NOT NULL COMMENT '所属顾客编号 (对应tb_user.id)',
  `topic_id` int NOT NULL COMMENT '所属主题编号 (对应b_topic.id)',
  `content` varchar(255) NOT NULL COMMENT '回复内容',
  `parent_id` int DEFAULT 0 COMMENT '父编号'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

-- 赞
CREATE TABLE `b_message_zan` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `user_id` int NOT NULL COMMENT '所属顾客编号 (对应tb_user.id)',
  `message_id` int NOT NULL COMMENT '所属信息编号 (对应b_message.id)'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;
