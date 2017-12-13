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
  `auth_type` varchar(128) NOT NULL COMMENT '登录授权分类 (WECHAT:微信,IOS_WECHAT:IOS微信,FACEBOOK:facebook)',
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
  `ap_list` varchar(128) DEFAULT NULL COMMENT '权限列表',
  `session_id` varchar(255) DEFAULT NULL COMMENT '当前session_id值',
  `gmt_login` datetime NOT NULL COMMENT '最后登录时间',
  `login_ip` varchar(50) NOT NULL COMMENT '最后登录IP',
  `status` varchar(8) NOT NULL DEFAULT 'ENABLED' COMMENT '账户状态标识 (ENABLED:可用,DISABLED:不可用)'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

-- ----------佣金系统相关表----------


