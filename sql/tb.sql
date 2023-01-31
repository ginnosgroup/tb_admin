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
  `region_id` int NOT NULL COMMENT '所属区域编号 (对应tb_region.id)',
  `oper_userid` varchar(64) DEFAULT NULL COMMENT '企业微信的oper_userid'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;
ALTER TABLE `tb_adviser` ADD INDEX index_name (`state`);

-- 客户
CREATE TABLE `tb_user` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `name` varchar(64) NOT NULL COMMENT '名称',
  `birthday` datetime NOT NULL COMMENT '生日',
  `phone` varchar(16) DEFAULT NULL COMMENT '电话号码',
  `email` varchar(128) DEFAULT NULL COMMENT '邮箱',
  `wechat_username` varchar(64) DEFAULT NULL COMMENT '微信帐号',
  `first_controller_contents` varchar(255) DEFAULT NULL COMMENT '初次咨询记录',
  `visa_code` varchar(16) DEFAULT NULL COMMENT '签证编号',
  `visa_expiration_date` datetime DEFAULT NULL COMMENT '签证到期日期',
  `source` varchar(32) DEFAULT NULL COMMENT '客户来源',
  `auth_type` varchar(16) NOT NULL COMMENT '登录授权分类 (WECHAT:微信,IOS_WECHAT:IOS微信,FACEBOOK:facebook,BROKERAGE:佣金系统用户,V:虚拟用户,WECHAT_WORK:企业微信)',
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

CREATE TABLE `tb_user_adviser` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `user_id` int NOT NULL COMMENT '所属顾客编号 (对应tb_user.id)',
  `adviser_id` int NOT NULL COMMENT '所属顾问编号 (对应tb_adviser.id)',
  `is_creater` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否为创建人'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;
ALTER TABLE `tb_user_adviser` ADD INDEX index_name (`user_id`, `adviser_id`);


-- 申请人
CREATE TABLE `b_applicant` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `surname` varchar(32) NOT NULL COMMENT '姓',
  `firstname` varchar(32) NOT NULL COMMENT '名',
  `birthday` datetime NOT NULL COMMENT '生日',
  `type` varchar(4) NOT NULL COMMENT '与客户关系 (BR:客户本人,FM:父母,QY:亲友)',
  `visa_code` varchar(8) DEFAULT NULL COMMENT '签证编号',
  `visa_expiration_date` datetime DEFAULT NULL COMMENT '签证到期日期',
  `nut_cloud` varchar(255) DEFAULT NULL COMMENT '坚果云地址',
  `file_url` varchar(255) DEFAULT NULL COMMENT '申请人资料表格',
  `first_controller_contents` varchar(255) DEFAULT NULL COMMENT '初次咨询记录',
  `user_id` int NOT NULL COMMENT '所属客户编号 (对应tb_user.id)',
  `adviser_id` int NOT NULL COMMENT '顾问编号 (对应tb_adviser.id)'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;
ALTER TABLE `b_applicant` ADD INDEX index_name (`user_id`);

-- 服务订单申请人关联表
CREATE TABLE `b_service_order_applicant` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `service_order_id` int NOT NULL COMMENT '服务订单编号 (对应b_service_order.id)',
  `applicant_id` int NOT NULL COMMENT '申请人编号 (对应b_applicant.id)',
  `url` varchar(255) DEFAULT NULL COMMENT 'URL地址',
  `message` varchar(255) DEFAULT NULL COMMENT '信息'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

-- 企业微信外部客户
CREATE TABLE `b_qywx_external_user` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `union_id` varchar(128) DEFAULT NULL COMMENT 'UnionId',
  `create_time` int DEFAULT NULL COMMENT '客户创建时间(时间辍)',
  `adviser_id` int DEFAULT NULL COMMENT '所属顾问编号 (对应tb_adviser.id)',
  `name` varchar(32) NOT NULL COMMENT '外部联系人的名称',
  `gender` int NOT NULL COMMENT '外部联系人性别 0-未知 1-男性 2-女性。',
  `type` int NOT NULL COMMENT '外部联系人的类型，1表示该外部联系人是微信用户，2表示该外部联系人是企业微信用户',
  `avatar` varchar(128) DEFAULT NULL COMMENT '头像',
  `external_userid` varchar(64) DEFAULT NULL COMMENT '外部联系人的userid',
  `state` varchar(8) NOT NULL COMMENT '状态(WBD:已存在未绑定,YBD:已存在已绑定,WCZ:未存在)',
  `user_id` int NOT NULL COMMENT '用户编号 (对应tb_user.id)',
  `applicant_id` int NOT NULL COMMENT '申请人编号 (对应b_applicant.id)',
  `phone` varchar(16) DEFAULT NULL COMMENT '电话号码',
  `email` varchar(128) DEFAULT NULL COMMENT '邮箱',
  `wechat_username` varchar(64) DEFAULT NULL COMMENT '微信帐号',
  `weibo_username` varchar(64) DEFAULT NULL COMMENT '微博帐号',
  `qq` varchar(16) DEFAULT NULL COMMENT 'QQ帐号'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

-- 企业微信外部客户详情
CREATE TABLE `b_qywx_external_user_description` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `qywx_external_userid` varchar(64) NOT NULL COMMENT '企业微信外部联系人的userid (对应b_qywx_external_user.external_userid)',
  `qywx_key` varchar(32) NOT NULL COMMENT '外部联系人详情名称',
  `qywx_value` varchar(255) DEFAULT NULL COMMENT '外部联系人详情值'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

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
  `ap_list` varchar(128) DEFAULT NULL COMMENT '权限列表 (KJ:会计,GW:顾问,MA:Mara,WA:文案,AD:管理员,TGAD:团购管理员,SUPERAD:超级管理员)',
  `adviser_id` int DEFAULT NULL COMMENT '所属顾问编号 (对应tb_adviser.id)',
  `mara_id` int DEFAULT NULL COMMENT '所属Mara编号 (对应b_mara.id)',
  `official_id` int DEFAULT NULL COMMENT '所属文案编号 (对应b_official.id)',
  `kj_id` int DEFAULT NULL COMMENT '所属会计编号 (对应b_kj.id)',
  `region_id` int NOT NULL COMMENT '所属区域编号 (对应tb_region.id,不为空就是顾问管理员)',
  `is_official_admin` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否为文案管理员',
  `session_id` varchar(255) DEFAULT NULL COMMENT '当前session_id值',
  `gmt_login` datetime NOT NULL COMMENT '最后登录时间',
  `login_ip` varchar(50) NOT NULL COMMENT '最后登录IP',
  `status` varchar(8) NOT NULL DEFAULT 'ENABLED' COMMENT '账户状态标识 (ENABLED:可用,DISABLED:不可用)',
  `oper_userid` varchar(64) DEFAULT NULL COMMENT '企业微信的oper_userid'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;
ALTER TABLE `tb_admin_user` ADD INDEX index_name (`username`, `adviser_id`, `mara_id`, `official_id`, `kj_id`, `region_id`);

-- ----------佣金系统相关表----------

-- 签证类
CREATE TABLE `b_visa` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `code` varchar(64) DEFAULT NULL COMMENT '分组编码',
  `handling_date` datetime NOT NULL COMMENT '办理日期',
  `user_id` int NOT NULL COMMENT '用户编号 (对应tb_user.id)',
  `state` varchar(8) NOT NULL COMMENT '状态',
  `commission_state` varchar(8) NOT NULL COMMENT '佣金状态(DJY:待结佣, YJY:已结佣, DZY:待追佣, YZY:已追佣)',
  `kj_approval_date` datetime DEFAULT NULL COMMENT '财务审核时间',
  `receive_type_id` int NOT NULL COMMENT '收款方式编号(对应b_receive_type.id)',
  `receive_date` datetime DEFAULT NULL COMMENT '收款日期',
  `service_id` int NOT NULL COMMENT '移民-服务项目编号 (对应b_service.id)',
  `service_order_id` int NOT NULL COMMENT '服务订单编号 (对应b_service_order.id)',
  `installment_num` int NOT NULL COMMENT '本次分期付款次数',
  `installment` int NOT NULL COMMENT '付款次数',
  `payment_voucher_image_url_1` varchar(128) DEFAULT NULL COMMENT '支付凭证图片地址1',
  `payment_voucher_image_url_2` varchar(128) DEFAULT NULL COMMENT '支付凭证图片地址2',
  `payment_voucher_image_url_3` varchar(128) DEFAULT NULL COMMENT '支付凭证图片地址3',
  `payment_voucher_image_url_4` varchar(128) DEFAULT NULL COMMENT '支付凭证图片地址4',
  `payment_voucher_image_url_5` varchar(128) DEFAULT NULL COMMENT '支付凭证图片地址5',
  `visa_voucher_image_url` varchar(128) DEFAULT NULL COMMENT '签证凭证图片地址',
  `receivable` decimal(8,2) NOT NULL COMMENT '总计应收',
  `received` decimal(8,2) NOT NULL COMMENT '总计已收',
  `per_amount` decimal(8,2) NOT NULL COMMENT '本次应收款',
  `amount` decimal(8,2) NOT NULL COMMENT '本次收款',
  `expect_amount` decimal(8,2) DEFAULT NULL COMMENT '预收业绩',
  `sure_expect_amount` decimal(8,2) DEFAULT NULL COMMENT '确认预收业绩',
  `currency` varchar(4) DEFAULT 'AUD' COMMENT '币种(AUD:澳币,CNY:人民币)',
  `exchange_rate` decimal(6,4) NOT NULL COMMENT '人民币兑换澳币汇率',
  `discount` decimal(8,2) NOT NULL DEFAULT 0 COMMENT '折扣',
  `gst` decimal(8,2) NOT NULL COMMENT 'GST',
  `deduct_gst` decimal(8,2) NOT NULL COMMENT 'Deduct GST',
  `bonus` decimal(8,2) NOT NULL COMMENT '月奖金',
  `bonus_date` datetime DEFAULT NULL COMMENT '月奖金支付时间',
  `invoice_number` varchar(64) DEFAULT NULL COMMENT 'InvoiceNo.',
  `adviser_id` int NOT NULL COMMENT '顾问编号 (对应tb_adviser.id)',
  `mara_id` int NOT NULL COMMENT '所属MARA编号 (对应b_mara.id)',
  `official_id` int NOT NULL COMMENT '文案编号 (对应b_official.id)',
  `bank_check` varchar(32) DEFAULT NULL COMMENT '银行对账',
  `is_checked` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否对账成功',
  `remarks` text DEFAULT NULL COMMENT '备注',
  `is_close` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已取消',
  `verify_code` varchar(64) DEFAULT NULL COMMENT '对账使用的code,顾问名称+地区+随机数',
  `bank_date` datetime DEFAULT NULL COMMENT '入账时间,对应b_finance_code.bank_date',
  `invoice_create` datetime DEFAULT NULL COMMENT '发票创建时间'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;
ALTER TABLE `b_visa` ADD INDEX index_name (`user_id`, `adviser_id`, `mara_id`, `official_id`);

-- 签证佣金订单评论
CREATE TABLE `b_visa_comment` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `admin_user_id` int NOT NULL COMMENT '所属管理员编号 (对应tb_admin_user.id)',
  `visa_id` int NOT NULL COMMENT '签证佣金订单编号 (对应b_visa.id)',
  `content` text NOT NULL COMMENT '内容'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;
ALTER TABLE `b_visa_comment` ADD INDEX index_name (`admin_user_id`, `visa_id`);

-- 移民佣金 (OLD)
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
  `remarks` text DEFAULT NULL COMMENT '备注',
  `is_close` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已取消'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

-- 留学-提前结佣 (OLD)
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
  `discount` decimal(8,2) NOT NULL DEFAULT 0 COMMENT '折扣',
  `commission` decimal(8,2) NOT NULL COMMENT '手续费',
  `gst` decimal(8,2) NOT NULL COMMENT 'GST',
  `deduct_gst` decimal(8,2) NOT NULL COMMENT 'Deduct GST',
  `bonus` decimal(8,2) NOT NULL COMMENT '月奖金',
  `adviser_id` int NOT NULL COMMENT '顾问编号 (对应tb_adviser.id)',
  `official_id` int NOT NULL COMMENT '文案编号 (对应b_official.id)',
  `remarks` text DEFAULT NULL COMMENT '备注',
  `is_close` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已取消'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

-- 留学-学校待结佣 (OLD)
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
  `discount` decimal(8,2) NOT NULL DEFAULT 0 COMMENT '折扣',
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
  `remarks` text DEFAULT NULL COMMENT '备注',
  `is_settle_accounts` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已结佣',
  `is_close` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已取消'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

-- 服务项目
CREATE TABLE `b_service` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `name` varchar(32) DEFAULT NULL COMMENT '项目名称',
  `code` varchar(32) DEFAULT NULL COMMENT '项目编码',
  `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已删除',
  `role` varchar(32) DEFAULT NULL COMMENT '参与角色',
  `is_zx` tinyint(1) NOT NULL DEFAULT '1' COMMENT '咨询服务'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

-- 服务包
CREATE TABLE `b_service_package` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `type` varchar(4) NOT NULL COMMENT '服务包类型(CA:职业评估,EOI:EOI,SA:学校申请,VA:签证申请,ZD:州担,TM:提名,DB:担保)',
  `service_id` int DEFAULT NULL COMMENT '服务项目编号 (对应b_service.id)',
  `num` int NOT NULL COMMENT '序号 (执行顺序)',
  `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已删除'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

-- 服务包价格
CREATE TABLE `b_service_package_price` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `min_price` decimal(8,2) NOT NULL DEFAULT '0.00' COMMENT '最低价格',
  `max_price` decimal(8,2) NOT NULL DEFAULT '0.00' COMMENT '最高价格',
  `service_id` int DEFAULT NULL COMMENT '服务项目编号 (对应b_service.id)',
  `region_id` int DEFAULT NULL COMMENT '所属区域编号 (对应tb_region.id)'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;
ALTER TABLE b_service_package_price ADD cost_prince decimal(8,2) NOT NULL DEFAULT '0.00' COMMENT '成本价格';
ALTER TABLE b_service_package_price ADD third_prince decimal(8,2) NOT NULL DEFAULT '0.00' COMMENT '第三方成本价格';
ALTER TABLE b_service_package_price ADD ruler int(11) NOT NULL COMMENT '文案佣金规则（0：固定rate，1：固定金额）';
ALTER TABLE b_service_package_price ADD amount decimal(8,2) NOT NULL DEFAULT '0.00' COMMENT '佣金金额';

-- 签证  评估的职业
CREATE TABLE `b_service_assess` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
  `name` varchar(20) DEFAULT NULL COMMENT '职业名称',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '修改时间',
  `service_id` int(11) DEFAULT NULL COMMENT '(b_service.id)',
  `is_delete` tinyint(1) NOT NULL COMMENT '是否删除 0 false,1 true',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;

-- 服务订单
CREATE TABLE `b_service_order` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `finish_date` datetime DEFAULT NULL COMMENT '办理完成时间',
  `type` varchar(4) DEFAULT NULL COMMENT '服务类型(VISA:签证服务,OVST:留学服务)',
  `service_id` int(11) NOT NULL COMMENT '服务项目编号 (对应b_service.id)',
  `school_id` int(11) DEFAULT NULL COMMENT '学校编号 (对应b_school.id,留学服务专用字段)',
  `course_id` int(11) DEFAULT NULL COMMENT 'b_school_course.id',
  `school_institution_location_id` int(11) DEFAULT NULL COMMENT '学校校区id',
  `institution_trading_name` varchar(124) DEFAULT NULL COMMENT '培训机构名字',
  `state` varchar(20) NOT NULL COMMENT '(PENDING:,REVIEW:,APPLY:,COMPLETE:,PAID:-,CLOSE:)',
  `is_settle` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否提前扣佣 (留学服务专用字段)',
  `is_deposit_user` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否为保证金用户 (留学服务专用字段)',
  `subagency_id` int(11) DEFAULT NULL COMMENT '代理编号 (对应b_subagency.id,留学服务专用字段)',
  `is_pay` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已支付(签证服务非支付不用创建佣金订单)',
  `receive_type_id` int(11) DEFAULT NULL COMMENT '收款方式编号(对应b_receive_type.id)',
  `receive_date` datetime DEFAULT NULL COMMENT '收款日期',
  `receivable` decimal(8,2) DEFAULT NULL COMMENT '总计应收',
  `discount` decimal(8,2) NOT NULL DEFAULT '0.00' COMMENT '折扣',
  `received` decimal(8,2) DEFAULT NULL COMMENT '总计已收',
  `payment_times` int(11) NOT NULL DEFAULT '1' COMMENT '付款次数',
  `amount` decimal(8,2) DEFAULT NULL COMMENT '本次收款',
  `gst` decimal(8,2) DEFAULT NULL COMMENT 'GST',
  `deduct_gst` decimal(8,2) DEFAULT NULL COMMENT 'Deduct GST',
  `bonus` decimal(8,2) DEFAULT NULL COMMENT '月奖金',
  `user_id` int(11) NOT NULL COMMENT '所属顾客编号 (对应tb_user.id)',
  `mara_id` int(11) DEFAULT NULL COMMENT '所属MARA编号 (对应b_mara.id,留学服务MARA为空)',
  `adviser_id` int(11) NOT NULL COMMENT '顾问编号 (对应tb_adviser.id)',
  `official_id` int(11) DEFAULT NULL COMMENT '文案编号 (对应b_official.id,曼拓文案为空)',
  `remarks` text COMMENT '备注',
  `is_delete` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已删除',
  `review_state` varchar(8) DEFAULT NULL COMMENT '审批状态 (OFFICIAL:文案审批通过,MARA:Mara审批通过,KJ:财务审批通过)',
  `closed_reason` varchar(255) DEFAULT NULL COMMENT '关闭原因',
  `expect_amount` decimal(8,2) DEFAULT NULL COMMENT '预收业绩',
  `per_amount` decimal(8,2) NOT NULL COMMENT '本次应收款',
  `payment_voucher_image_url_1` varchar(128) DEFAULT NULL COMMENT '支付凭证图片地址1',
  `payment_voucher_image_url_2` varchar(128) DEFAULT NULL COMMENT '支付凭证图片地址2',
  `installment` int(11) NOT NULL DEFAULT '1' COMMENT '付款次数',
  `visa_voucher_image_url` varchar(128) DEFAULT NULL COMMENT '签证凭证图片地址',
  `is_submitted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已提交 (是否已创建佣金订单)',
  `payment_voucher_image_url_3` varchar(128) DEFAULT NULL COMMENT '支付凭证图片地址3',
  `parent_id` int(11) DEFAULT NULL COMMENT '父服务订单编号 (对应b_service_order.id)',
  `service_package_id` int(11) DEFAULT NULL COMMENT '服务包编号 (对应b_service_package.id,仅子订单才有)',
  `people_number` int(11) NOT NULL DEFAULT '1' COMMENT '人数',
  `people_type` varchar(4) NOT NULL DEFAULT '1A' COMMENT '人类型(1A:单人,1B:单人提配偶,2A:带配偶,XA:带孩子,XB:带配偶孩子,XC:其它)',
  `people_remarks` text COMMENT '人备注',
  `code` varchar(64) DEFAULT NULL COMMENT '分组编码',
  `adviser_id_2` int(11) DEFAULT NULL COMMENT '第二顾问编号 (对应tb_adviser.id,曼拓专用字段)',
  `payment_voucher_image_url_4` varchar(128) DEFAULT NULL COMMENT '支付凭证图片地址4 (留学服务专用字段)',
  `payment_voucher_image_url_5` varchar(128) DEFAULT NULL COMMENT '支付凭证图片地址5 (留学服务专用字段)',
  `coe_payment_voucher_image_url_1` varchar(128) DEFAULT NULL COMMENT 'EOE支付凭证图片地址1',
  `coe_payment_voucher_image_url_2` varchar(128) DEFAULT NULL COMMENT 'EOE支付凭证图片地址2',
  `coe_payment_voucher_image_url_3` varchar(128) DEFAULT NULL COMMENT 'EOE支付凭证图片地址3',
  `coe_payment_voucher_image_url_4` varchar(128) DEFAULT NULL COMMENT 'EOE支付凭证图片地址4',
  `coe_payment_voucher_image_url_5` varchar(128) DEFAULT NULL COMMENT 'EOE支付凭证图片地址5',
  `invoice_voucher_image_url1` varchar(128) DEFAULT NULL COMMENT '发票invoice凭证地址1',
  `invoice_voucher_image_url2` varchar(128) DEFAULT NULL COMMENT '发票invoice凭证地址2',
  `invoice_voucher_image_url3` varchar(128) DEFAULT NULL COMMENT '发票invoice凭证地址3',
  `invoice_voucher_image_url4` varchar(128) DEFAULT NULL COMMENT '发票invoice凭证地址4',
  `invoice_voucher_image_url5` varchar(128) DEFAULT NULL COMMENT '发票invoice凭证地址5',
  `kj_payment_image_url1` varchar(128) DEFAULT NULL COMMENT '文案转账凭证1',
  `kj_payment_image_url2` varchar(128) DEFAULT NULL COMMENT '文案转账凭证2',
  `official_approval_date` datetime DEFAULT NULL COMMENT '文案审核时间',
  `mara_approval_date` datetime DEFAULT NULL COMMENT 'mara审核时间',
  `submit_mara_approval_date` datetime DEFAULT NULL COMMENT '文案提交给mara审核的时间',
  `information` text COMMENT '填写客户背景信息比如特殊要求、紧急程度、家庭背景等',
  `is_history` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否为历史订单，0为否，1为是',
  `nut_cloud` text COMMENT '坚果云地址',
  `service_assess_id` int(11) DEFAULT NULL COMMENT '签证职业评估编号(对应b_service_assess.id)',
  `real_people_number` int(11) NOT NULL DEFAULT '1' COMMENT '历史订单:0,不是历史订单:对应people_number(只文案可修改)',
  `readcommitted_date` datetime DEFAULT NULL COMMENT '已提交申请的时间',
  `verify_code` varchar(64) DEFAULT NULL COMMENT '对账使用的code,顾问名称+地区+随机数',
  `refuse_reason` varchar(255) DEFAULT NULL COMMENT '驳回原因',
  `state_mark` text COMMENT '状态标注',
  `urgent_state` varchar(8) DEFAULT NULL COMMENT '加急状态(JJ:加急, TSJJ:特殊加急),仅限签证订单',
  `ref_no` varchar(32) DEFAULT NULL COMMENT 'Ref.No',
  `low_price_image_url` varchar(128) DEFAULT NULL COMMENT '低价审核账凭证',
  `applicant_id` int(11) DEFAULT NULL COMMENT '申请人编号',
  `applicant_parent_id` int(11) DEFAULT NULL COMMENT ' (b_service_order.id)',
  `currency` varchar(4) DEFAULT 'AUD' COMMENT '(AUD:,CNY:)',
  `exchange_rate` decimal(6,4) NOT NULL DEFAULT '4.8000'
  PRIMARY KEY (`id`) USING BTREE,
  KEY `index_name` (`user_id`,`adviser_id`,`official_id`,`mara_id`,`state`,`service_id`,`parent_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

CREATE TABLE `b_service_order_review` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `service_order_id` int NOT NULL COMMENT '服务订单编号 (对应b_service_order.id)',
  `commission_order_id` int DEFAULT NULL COMMENT '佣金订单编号 (对应b_commission_order.id)',
  `adviser_state` varchar(8) DEFAULT NULL COMMENT '顾问状态 (PENDING:待提交审核,REVIEW:审核中,APPLY:服务申请中,COMPLETE:服务申请完成,PAID:完成-支付成功,CLOSE:关闭)',
  `mara_state` varchar(8) DEFAULT NULL COMMENT 'MARA状态 (WAIT:待审核,FINISH:已审核)',
  `official_state` varchar(8) DEFAULT NULL COMMENT '文案状态 (REVIEW:资料审核中,FINISH:资料审核完毕,APPLY:服务申请中,COMPLETE:服务申请完成,PAID:完成-支付成功)',
  `kj_state` varchar(8) DEFAULT NULL COMMENT '财务状态 (WAIT:佣金待审核,FINISH:佣金已审核,COMPLETE:结算完成)',
  `type` varchar(8) NOT NULL COMMENT '类型 (APPROVAL:通过,REFUSE:驳回)',
  `admin_user_id` int NOT NULL COMMENT '管理员编号 (对应tb_admin_user.id)'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;
ALTER TABLE `b_service_order_review` ADD INDEX index_name (`service_order_id`, `commission_order_id`);

-- 服务订单评论
CREATE TABLE `b_service_order_comment` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `admin_user_id` int NOT NULL COMMENT '所属管理员编号 (对应tb_admin_user.id)',
  `service_order_id` int NOT NULL COMMENT '服务订单编号 (对应b_service_order.id)',
  `content` text NOT NULL COMMENT '内容'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

-- 服务订单文案记录
CREATE TABLE `b_service_order_official_remarks` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `official_id` int NOT NULL COMMENT '所属文案编号 (对应b_official.id)',
  `service_order_id` int NOT NULL COMMENT '服务订单编号 (对应b_service_order.id)',
  `content` text NOT NULL COMMENT '内容'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;
ALTER TABLE `b_service_order_official_remarks` ADD INDEX index_name (`official_id`, `service_order_id`);

-- 服务订单文案标签关联
CREATE TABLE `b_service_order_official_tag` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `service_order_id` int NOT NULL COMMENT '服务订单编号 (对应b_service_order.id)',
  `official_tag_id` int NOT NULL COMMENT '标签编号(对应b_official_tag.id)'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;
ALTER TABLE `b_service_order_official_tag` ADD INDEX index_name (`service_order_id`, `official_tag_id`);

-- 服务订单文案标签
CREATE TABLE `b_official_tag` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `name` varchar(32) NOT NULL COMMENT '标签名称',
  `colour` varchar(8) NOT NULL COMMENT '标签颜色'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

-- 佣金订单(留学)
CREATE TABLE `b_commission_order` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `code` varchar(64) DEFAULT NULL COMMENT '分组编码',
  `service_order_id` int NOT NULL COMMENT '服务订单编号 (对应b_service_order.id)',
  `state` varchar(8) NOT NULL COMMENT '状态(PENDING, WAIT, REVIEW, FINISH, COMPLETE, CLOSE)',
  `commission_state` varchar(8) NOT NULL COMMENT '佣金状态(DJY:待结佣, YJY:已结佣, DZY:待追佣, YZY:已追佣)',
  `kj_approval_date` datetime DEFAULT NULL COMMENT '财务审核时间',
  `is_settle` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否提前扣佣',
  `is_deposit_user` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否为保证金用户',
  `school_id` int DEFAULT NULL COMMENT '学校编号 (对应b_school.id)',
  `course_id` int(11) DEFAULT NULL COMMENT 'b_school_course.id',
  `school_institution_location_id` int(11) DEFAULT NULL COMMENT '学校校区id,b_school_institution_location.id',
  `student_code` varchar(32) NOT NULL COMMENT '学号',
  `user_id` int NOT NULL COMMENT '用户编号 (对应tb_user.id)',
  `adviser_id` int NOT NULL COMMENT '顾问编号 (对应tb_adviser.id)',
  `official_id` int NOT NULL COMMENT '文案编号 (对应b_official.id)',
  `is_studying` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否就读',
  `installment_num` int NOT NULL COMMENT '本次分期付款次数',
  `installment` int NOT NULL COMMENT '分期付款次数',
  `installment_due_date` datetime NOT NULL COMMENT '分期付款截止日期',
  `payment_voucher_image_url_1` varchar(128) DEFAULT NULL COMMENT '支付凭证图片地址1',
  `payment_voucher_image_url_2` varchar(128) DEFAULT NULL COMMENT '支付凭证图片地址2',
  `payment_voucher_image_url_3` varchar(128) DEFAULT NULL COMMENT '支付凭证图片地址3',
  `payment_voucher_image_url_4` varchar(128) DEFAULT NULL COMMENT '支付凭证图片地址4',
  `payment_voucher_image_url_5` varchar(128) DEFAULT NULL COMMENT '支付凭证图片地址5',
  `school_payment_amount` decimal(8,2) DEFAULT NULL COMMENT '学校支付金额',
  `school_payment_date` datetime DEFAULT NULL COMMENT '学校支付时间',
  `invoice_number` varchar(64) DEFAULT NULL COMMENT 'InvoiceNo.',
  `start_date` datetime NOT NULL COMMENT '开课日期',
  `end_date` datetime NOT NULL COMMENT '结束日期',
  `commission` decimal(8,2) NOT NULL COMMENT '佣金',
  `tuition_fee` decimal(8,2) NOT NULL COMMENT '总学费',
  `per_term_tuition_fee` decimal(8,2) NOT NULL COMMENT '每学期学费',
  `receive_type_id` int NOT NULL COMMENT '收款方式编号(对应b_receive_type.id)',
  `receive_date` datetime DEFAULT NULL COMMENT '收款日期',
  `per_amount` decimal(8,2) NOT NULL COMMENT '本次应收款',
  `amount` decimal(8,2) NOT NULL COMMENT '本次收款',
  `expect_amount` decimal(8,2) DEFAULT NULL COMMENT '预收业绩',
  `sure_expect_amount` decimal(8,2) DEFAULT NULL COMMENT '确认预收业绩',
  `currency` varchar(4) DEFAULT 'AUD' COMMENT '币种(AUD:澳币,CNY:人民币)',
  `exchange_rate` decimal(6,4) NOT NULL COMMENT '人民币兑换澳币汇率',
  `discount` decimal(8,2) NOT NULL DEFAULT 0 COMMENT '折扣',
  `gst` decimal(8,2) NOT NULL COMMENT 'GST',
  `deduct_gst` decimal(8,2) NOT NULL COMMENT 'Deduct GST',
  `bonus` decimal(8,2) NOT NULL COMMENT '月奖金',
  `bonus_date` datetime DEFAULT NULL COMMENT '月奖金支付时间',
  `zy_date` datetime DEFAULT NULL COMMENT '追佣时间',
  `bank_check` varchar(32) DEFAULT NULL COMMENT '银行对账',
  `is_checked` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否对账成功',
  `remarks` text DEFAULT NULL COMMENT '备注',
  `is_close` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已取消',
  `verify_code` varchar(64) DEFAULT NULL COMMENT '对账使用的code,顾问名称+地区+随机数',
  `bank_date` datetime DEFAULT NULL COMMENT '入账时间,对应b_finance_code.bank_date',
  `invoice_create` datetime DEFAULT NULL COMMENT '发票创建时间'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;
ALTER TABLE `b_commission_order` ADD INDEX index_name (`code`, `school_id`, `user_id`, `adviser_id`, `official_id`);

-- (留学)佣金订单评论
CREATE TABLE `b_commission_order_comment` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `admin_user_id` int NOT NULL COMMENT '所属管理员编号 (对应tb_admin_user.id)',
  `commission_order_id` int NOT NULL COMMENT '佣金订单编号 (对应b_commission_order.id)',
  `content` text NOT NULL COMMENT '内容'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;
ALTER TABLE `b_commission_order_comment` ADD INDEX index_name (`admin_user_id`, `commission_order_id`);

--(留学)顾问还没创建佣金订单时，填写数据暂存，用于创建佣金订单
CREATE TABLE `b_commission_order_temp` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `service_order_id` int(11) NOT NULL COMMENT '服务订单编号 (对应b_service_order.id)',
  `student_code` varchar(32) NOT NULL COMMENT '学号',
  `dob` datetime DEFAULT NULL COMMENT '生日',
  `start_date` datetime NOT NULL COMMENT '开课日期',
  `end_date` datetime NOT NULL COMMENT '结束日期',
  `installment` int(11) NOT NULL COMMENT '分期付款次数',
  `installment_due_date1` datetime DEFAULT NULL COMMENT '分期付款截止日期1',
  `installment_due_date2` datetime DEFAULT NULL COMMENT '分期付款截止日期2',
  `installment_due_date3` datetime DEFAULT NULL COMMENT '分期付款截止日期3',
  `installment_due_date4` datetime DEFAULT NULL COMMENT '分期付款截止日期4',
  `installment_due_date5` datetime DEFAULT NULL COMMENT '分期付款截止日期5',
  `installment_due_date6` datetime DEFAULT NULL COMMENT '分期付款截止日期6',
  `installment_due_date7` datetime DEFAULT NULL COMMENT '分期付款截止日期7',
  `installment_due_date8` datetime DEFAULT NULL COMMENT '分期付款截止日期8',
  `installment_due_date9` datetime DEFAULT NULL COMMENT '分期付款截止日期9',
  `installment_due_date10` datetime DEFAULT NULL COMMENT '分期付款截止日期10',
  `installment_due_date11` datetime DEFAULT NULL COMMENT '分期付款截止日期11',
  `installment_due_date12` datetime DEFAULT NULL COMMENT '分期付款截止日期12',
  `tuition_fee` decimal(8,2) NOT NULL COMMENT '学费总金额',
  `per_term_tuition_fee` decimal(8,2) NOT NULL COMMENT '每学期学费',
  `receive_date` datetime DEFAULT NULL COMMENT '本次收款日期',
  `receive_type_id` int(11) NOT NULL COMMENT '收款方式编号(对应b_receive_type.id)',
  `per_amount` decimal(8,2) NOT NULL COMMENT '本次应收款',
  `amount` decimal(8,2) NOT NULL COMMENT '本次实收',
  `discount` decimal(8,2) NOT NULL COMMENT '折扣',
  `verify_code` varchar(64) DEFAULT NULL COMMENT '对账使用的code,顾问名称+地区+随机数',
  `expect_amount` decimal(8,2) DEFAULT NULL COMMENT '预收业绩',
  `currency` varchar(4) DEFAULT 'AUD' COMMENT '币种(AUD:澳币,CNY:人民币)',
  `exchange_rate` decimal(6,4) NOT NULL COMMENT '人民币兑换澳币汇率',
  `remarks` varchar(255) DEFAULT NULL COMMENT '备注',
  `payment_voucher_image_url_1` varchar(128) DEFAULT NULL COMMENT '支付凭证图片地址1',
  `payment_voucher_image_url_2` varchar(128) DEFAULT NULL COMMENT '支付凭证图片地址2',
  `payment_voucher_image_url_3` varchar(128) DEFAULT NULL COMMENT '支付凭证图片地址3',
  `payment_voucher_image_url_4` varchar(128) DEFAULT NULL COMMENT '支付凭证图片地址4',
  `payment_voucher_image_url_5` varchar(128) DEFAULT NULL COMMENT '支付凭证图片地址5',
  `invoice_number` varchar(64) DEFAULT NULL COMMENT 'InvoiceNo.暂存发票号,创建佣金订单之后发票号绑定到第一笔留学佣金',
  PRIMARY KEY (`id`),
  KEY `index` (`service_order_id`,`verify_code`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;

-- 留学-学校
CREATE TABLE `b_school` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `name` varchar(128) NOT NULL COMMENT '名称',
  `subject` varchar(128) NOT NULL COMMENT '课程',
  `country` varchar(4) NOT NULL COMMENT '国家编码',
  `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已删除'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

-- 学校附件*
CREATE TABLE `b_school_attachments` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `school_name` varchar(128) DEFAULT NULL COMMENT '学校名称',
  `contract_file_1` varchar(128) DEFAULT NULL COMMENT '合同地址1',
  `contract_file_2` varchar(128) DEFAULT NULL COMMENT '合同地址2',
  `contract_file_3` varchar(128) DEFAULT NULL COMMENT '合同地址3',
  `remarks` text DEFAULT NULL COMMENT '备注',
  `provider_id` int(11) DEFAULT NULL COMMENT 'b_school_institution.id',
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

-- Subagency
CREATE TABLE `b_subagency` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `name` varchar(32) NOT NULL COMMENT '名称',
  `country` varchar(4) NOT NULL DEFAULT 'AUS' COMMENT '国家 (CHN:中国,AUS:澳大利亚)',
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
  `region_id` int NOT NULL COMMENT '所属区域编号 (对应tb_region.id)',
  `specialty` varchar(128) DEFAULT NULL COMMENT '文案标签描述',
  `work_state` varchar(20) DEFAULT NULL COMMENT '文案工作状态,NORMAL:正常/BUSY:忙碌'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;
ALTER TABLE b_official ADD grade_id int(11) DEFAULT NULL COMMENT '等级id';

-- MARA
CREATE TABLE `b_mara` (
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
ALTER TABLE `b_mara` ADD INDEX index_name (`state`);

-- 会计(财务)
CREATE TABLE `b_kj` (
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
ALTER TABLE `b_kj` ADD INDEX index_name (`state`);

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
  `content` text DEFAULT NULL COMMENT '知识库内容',
  `password` varchar(8) DEFAULT NULL COMMENT '知识库密码',
  `knowledge_menu_id` int DEFAULT 0 COMMENT '所属菜单编号',
  `admin_user_id` int NOT NULL COMMENT '创建者编号 (对应tb_admin_user.id)'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

-- 知识库菜单
CREATE TABLE `b_knowledge_menu` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `name` varchar(32) DEFAULT NULL COMMENT '菜单名称',
  `parent_id` int DEFAULT 0 COMMENT '父菜单编号'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

-- 留言信息
CREATE TABLE `b_message` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `admin_user_id` int NOT NULL COMMENT '所属管理员编号 (对应tb_admin_user.id)',
  `knowledge_id` int NOT NULL COMMENT '所属知识库编号 (对应b_knowledge.id)',
  `content` varchar(255) NOT NULL COMMENT '内容'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

-- 赞
CREATE TABLE `b_message_zan` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `admin_user_id` int NOT NULL COMMENT '所属管理员编号 (对应tb_admin_user.id)',
  `message_id` int NOT NULL COMMENT '所属信息编号 (对应b_message.id)'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

-- ----------佣金卡片----------

CREATE TABLE `b_school_setting` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `school_id` int NOT NULL COMMENT '学校专业编号 (对应b_school.id)',
  `type` int COMMENT '设置类型 (0:未选,1:固定比例-无额外补贴,2:固定比例-每人补贴,3:固定比例-一次性补贴,4:变动比例,5:固定底价-无额外补贴,6:固定底价-每人补贴,7:固定底价-一次性补贴)',
  `start_date` datetime NOT NULL COMMENT '合同开始时间',
  `end_date` datetime NOT NULL COMMENT '合同结束时间',
  `parameters` varchar(255) COMMENT '参数'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;
ALTER TABLE `b_school_setting` ADD INDEX index_name (`school_id`);

-- (OLD)
CREATE TABLE `b_subject_setting` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `school_setting_id` int NOT NULL COMMENT '学校设置编号 (对应b_school_setting.id)',
  `subject` varchar(128) NOT NULL COMMENT '课程',
  `price` decimal(8,2) NOT NULL DEFAULT '0.00' COMMENT '价格'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

-- 客户标签关联
CREATE TABLE `b_user_tag` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `user_id` int NOT NULL COMMENT '客户编号',
  `tag_id` int NOT NULL COMMENT '标签编号'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

-- 标签
CREATE TABLE `b_tag` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `name` varchar(32) NOT NULL COMMENT '名称'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;


--------invoice------------
-- 各地区对应的地址
DROP TABLE IF EXISTS `b_invoice_address`;
CREATE TABLE `b_invoice_address` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `branch` varchar(10) NOT NULL COMMENT '各地区名字',
  `address` varchar(100) NOT NULL COMMENT '各地区对应的address',
  `bsb` varchar(10) DEFAULT NULL COMMENT 'BSB',
  `account` varchar(10) DEFAULT NULL COMMENT 'Account No',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;

-- bill to 选择的公司
DROP TABLE IF EXISTS `b_invoice_billto`;
CREATE TABLE `b_invoice_billto` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `company` varchar(100) NOT NULL COMMENT 'company名字',
  `abn` varchar(64) DEFAULT NULL COMMENT 'ABN',
  `address` varchar(100) NOT NULL COMMENT '地址',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COMMENT='Bill to 的时候选择的 Company';


-- 各地区的简写
DROP TABLE IF EXISTS `b_invoice_branch_simple`;
CREATE TABLE `b_invoice_branch_simple` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `branch` varchar(10) NOT NULL COMMENT '地区',
  `simple` varchar(2) NOT NULL COMMENT '简写',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;

--  公司信息
DROP TABLE IF EXISTS `b_invoice_company`;
CREATE TABLE `b_invoice_company` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(50) NOT NULL COMMENT '公司名字',
  `simple` varchar(5) DEFAULT NULL COMMENT '公司简写',
  `abn` varchar(20) NOT NULL COMMENT 'ABN',
  `email` varchar(128) NOT NULL COMMENT '邮箱地址',
  `tel` varchar(20) NOT NULL COMMENT '电话号码',
  `bsb` varchar(10) NOT NULL COMMENT 'BSB',
  `account` varchar(10) NOT NULL COMMENT 'Account No',
  `flag` varchar(4) DEFAULT NULL COMMENT 'SC表示留学，SF表示  Service Fee',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;


-- 留学税务模板
DROP TABLE IF EXISTS `b_invoice_school`;
CREATE TABLE `b_invoice_school` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `gmt_create` datetime NOT NULL COMMENT 'invoiceDate时间',
  `gmt_create_1` datetime NOT NULL COMMENT '创建时间',
  `order_id` varchar(512) DEFAULT NULL COMMENT '对应订单id',
  `email` varchar(128) NOT NULL COMMENT '公司邮箱',
  `company_id` int(11) DEFAULT NULL COMMENT '对应 invoice_company.id',
  `company` varchar(100) NOT NULL COMMENT '公司名称',
  `abn` varchar(20) NOT NULL COMMENT 'ABN',
  `address` varchar(128) NOT NULL COMMENT '公司地址',
  `tel` varchar(20) NOT NULL COMMENT '公司电话',
  `invoice_no` varchar(20) DEFAULT NULL COMMENT '税务发票编号',
  `billto_id` int(11) DEFAULT NULL COMMENT '对应 b_invoice_billto.id  ，付款公司',
  `note` varchar(255) DEFAULT NULL COMMENT '备注NOTE',
  `accountname` varchar(100) NOT NULL COMMENT 'Account Name  账户名字',
  `bsb` varchar(10) NOT NULL COMMENT 'BSB',
  `accountno` varchar(10) NOT NULL COMMENT 'Account Name 账号',
  `state` varchar(10) NOT NULL COMMENT '状态 NORMAL:正常 CANCELED:取消',
  `branch` varchar(10) DEFAULT NULL COMMENT 'branch 地区',
  `flag` varchar(10) DEFAULT NULL COMMENT '标识是normal还是mark',
  `pdf_url` varchar(150) DEFAULT NULL COMMENT '留学pdf地址'
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=utf8 COMMENT='Service Fee 税务的模板';

-- 留学税务模板里面的 description
DROP TABLE IF EXISTS `b_invoice_school_description`;
CREATE TABLE `b_invoice_school_description` (
  `id` int(11) unsigned NOT NULL COMMENT '主键',
  `studentname` varchar(50) NOT NULL COMMENT '学生姓名',
  `dob` datetime NOT NULL COMMENT '出生日期',
  `student_id` varchar(64) DEFAULT NULL COMMENT '对应学生ID',
  `course` varchar(200) NOT NULL COMMENT '课程',
  `startDate` datetime NOT NULL COMMENT '课程开始时时间',
  `instalment` varchar(50) DEFAULT NULL COMMENT '第几期',
  `tuitionfee` decimal(10,2) DEFAULT '0.00' COMMENT '学费',
  `bonus` decimal(10,2) DEFAULT '0.00' COMMENT '奖金',
  `commissionrate` varchar(10) DEFAULT '0' COMMENT '佣金率',
  `commission` decimal(10,2) DEFAULT '0.00' COMMENT '佣金',
  `marketing_bonus` decimal(11,2) DEFAULT '0.00' COMMENT '专属市场bonus',
  `description_id` int(11) DEFAULT NULL COMMENT '对应 b_invoice_school_description.id , 对应description信息',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `invoice_no` varchar(64) DEFAULT NULL COMMENT '税务发票编号 （对应b_invoice_school表中的invoice_no）',
  `non_tuitionfee` decimal(10,2) DEFAULT NULL COMMENT 'nonTuitionFee',
  `commission_order_id` int(11) DEFAULT NULL COMMENT '留学佣金订单b_commission_order.id',
  `installment_due_date` datetime DEFAULT '1900-00-00 00:00:00' COMMENT 'installment due date',
  KEY `invoice_no` (`invoice_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- serviceFee 税务模板
DROP TABLE IF EXISTS `b_invoice_servicefee`;
CREATE TABLE `b_invoice_servicefee` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `gmt_create` datetime NOT NULL COMMENT 'invoiceDate时间',
  `gmt_create_1` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `email` varchar(128) NOT NULL COMMENT '公司邮箱',
  `company` varchar(100) NOT NULL COMMENT '公司名称',
  `abn` varchar(20) NOT NULL COMMENT 'ABN',
  `address` varchar(128) NOT NULL COMMENT '公司地址',
  `tel` varchar(20) NOT NULL COMMENT '公司电话',
  `invoice_no` varchar(20) DEFAULT NULL COMMENT '税务发票编号',
  `note` varchar(255) DEFAULT NULL COMMENT '备注NOTE',
  `accountname` varchar(100) NOT NULL COMMENT 'Account Name  账户名字',
  `bsb` varchar(10) NOT NULL COMMENT 'BSB',
  `accountno` varchar(10) NOT NULL COMMENT 'Account Name 账号',
  `state` varchar(10) NOT NULL COMMENT '状态 NORMAL:正常 CANCELED:取消',
  `order_id` varchar(20) DEFAULT NULL COMMENT '对应佣金订单id',
  `branch` varchar(10) DEFAULT NULL COMMENT 'branch 地区',
  `bill_to` varchar(64) DEFAULT NULL COMMENT '客户姓名',
  `pdf_url` varchar(150) DEFAULT NULL COMMENT '留学pdf地址'
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='Service Fee 税务的模板';

-- serviceFee 税务里面的 description
DROP TABLE IF EXISTS `b_invoice_servicefee_description`;
CREATE TABLE `b_invoice_servicefee_description` (
  `id` int(11) NOT NULL COMMENT '编号',
  `description` varchar(100) NOT NULL COMMENT '客服姓名+f服务项目；中间空格',
  `unit_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '本次收款',
  `quantity` int(2) NOT NULL COMMENT '收款次数，默认为1',
  `amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT 'unit_price * quantity',
  `invoice_no` varchar(20) DEFAULT NULL COMMENT '税务发票编号 （对应b_invoice表中的invoice_no）',
  `servicefee_id` int(11) DEFAULT NULL COMMENT '对应  b_invoice_servicefee.id  ',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  KEY `invoice_servicefee` (`invoice_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- b_service_order 表中的 readcommitted_date 字段的修改历史
CREATE TABLE `b_service_order_readcommitted_date` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `service_order_id` int(11) NOT NULL COMMENT 'b_service_order.id',
  `history_date` datetime NOT NULL COMMENT '已提交申请字段的历史时间(b_service_order.readcommitted_date)',
  `gmt_modify` datetime NOT NULL COMMENT '修改时间',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 财务对账使用的银行
CREATE TABLE `b_finance_bank` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `bank` varchar(255) NOT NULL COMMENT '名字',
  `bsb` varchar(64) NOT NULL COMMENT 'bsb',
  `accountno` varchar(64) NOT NULL COMMENT 'accountno',
  `simple` varchar(5) NOT NULL COMMENT 'bank字段简称',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '更改时间',
  `is_delete` tinyint(1) NOT NULL COMMENT '是否删除(0:否,1是)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

-------   bankstatement表格--
CREATE TABLE `b_finance_code` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '修改时间',
  `bank_date` datetime NOT NULL COMMENT '入账时间',
  `user_id` int(11) NOT NULL COMMENT '客户姓名',
  `is_income` tinyint(1) NOT NULL COMMENT '是否收入(1:是;0否)',
  `money` double(10,2) NOT NULL COMMENT '金额',
  `balance` double(10,2) NOT NULL COMMENT '余额',
  `adviser_id` int(11) NOT NULL COMMENT '顾问id，对应tb_adviser.id',
  `business` varchar(255) DEFAULT NULL COMMENT '签证获取签证+类型；留学获取学校名称',
  `order_id` varchar(64) DEFAULT NULL COMMENT '对应b_commission_order.id/b_visa.id(CS/CV判断)',
  `comment` varchar(255) DEFAULT NULL COMMENT '银行备注',
  `amount` decimal(8,2) DEFAULT NULL COMMENT '佣金订单中的实收金额',
  `code` varchar(64) DEFAULT NULL COMMENT '日期+入账金额+余额作为去重标识'
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
ALTER TABLE `b_finance_code` ADD INDEX index_name (order_id);

-- ----------新学校数据----------

CREATE TABLE `b_school_institution` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `code` varchar(32) DEFAULT NULL COMMENT '编码',
  `name` varchar(255) DEFAULT NULL COMMENT '名称',
  `institution_trading_name` varchar(255) DEFAULT NULL COMMENT '学校名称',
  `institution_name` varchar(255) DEFAULT NULL COMMENT '学校名称',
  `institution_type` varchar(255) DEFAULT NULL COMMENT '学校类型',
  `institution_postal_address` varchar(512) DEFAULT NULL COMMENT '学校地址',
  `website` varchar(128) DEFAULT NULL COMMENT '学校网站',
  `is_freeze` tinyint(1) NOT NULL DEFAULT '0' COMMENT '冻结',
  `summary` text COMMENT '概述',
  PRIMARY KEY (`id`),
  KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

CREATE TABLE `b_school_institution_location` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `name` varchar(255) DEFAULT NULL COMMENT '名称',
  `state` varchar(8) DEFAULT NULL COMMENT '州',
  `number_of_courses` int DEFAULT NULL COMMENT '课程数量',
  `provider_id` int DEFAULT NULL COMMENT '学校编号',
  `provider_code` varchar(32) DEFAULT NULL COMMENT '学校编码',
  `phone` varchar(16) DEFAULT NULL COMMENT '校区电话号码',
  `address` varchar(512) DEFAULT NULL COMMENT '校区地址',
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

CREATE TABLE `b_school_course` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `course_name` varchar(255) DEFAULT NULL COMMENT '课程名称',
  `course_code` varchar(32) DEFAULT NULL COMMENT '课程编码',
  `course_sector` varchar(255) DEFAULT NULL COMMENT '课程所属行业',
  `course_level` varchar(255) DEFAULT NULL COMMENT '课程级别',
  `provider_id` int DEFAULT NULL COMMENT '学校编号',
  `provider_code` varchar(32) DEFAULT NULL COMMENT '学校编码',
  `course_code` varchar(32) DEFAULT NULL COMMENT '专业编码',
  `is_freeze` tinyint(1) NOT NULL DEFAULT '0' COMMENT '冻结'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

----学校评论----
CREATE TABLE `b_school_institution_comment` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '评论id',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '修改时间',
  `school_institution_id` int(11) NOT NULL COMMENT 'b_school_institution.id',
  `username` varchar(64) NOT NULL COMMENT ' (tb_admin_user.id)发表用户id',
  `content` text NOT NULL COMMENT '评论内容',
  `parent_id` int(11) DEFAULT NULL COMMENT '父级评论id',
  `to_username` varchar(64) DEFAULT NULL COMMENT '回复给',
  PRIMARY KEY (`id`),
  KEY `index` (`school_institution_id`,`parent_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;


---群聊id
CREATE TABLE `b_chat` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `service_order_id` int(11) NOT NULL COMMENT '服务订单编号 (对应b_service_order.id)',
  `chat_id` varchar(32) NOT NULL COMMENT '群聊id',
  `user_id` int(11) NOT NULL COMMENT ' (tb_user.id)',
  `mara_id` int(11) DEFAULT NULL COMMENT 'MARA (b_mara.id,MARA)',
  `adviser_id` int(11) NOT NULL COMMENT ' (tb_adviser.id)',
  `official_id` int(11) DEFAULT NULL COMMENT ' (b_official.id,)',
  PRIMARY KEY (`id`),
  KEY `service_order_id` (`service_order_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 提醒邮件日志
CREATE TABLE `b_mail_log` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `code` varchar(128) NOT NULL COMMENT '编码',
  `mail` varchar(255) NOT NULL COMMENT '收件人邮箱',
  `title` varchar(255) NOT NULL COMMENT '标题',
  `content` text DEFAULT NULL COMMENT '内容'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

--设置提醒
CREATE TABLE `b_mail_remind` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `code` varchar(128) NOT NULL COMMENT '编码',
  `mail` varchar(255) NOT NULL COMMENT '收件人邮箱',
  `title` varchar(255) DEFAULT NULL COMMENT '标题',
  `content` text COMMENT '内容',
  `send_date` datetime DEFAULT NULL COMMENT '设置的邮件发送时间',
  `service_order_id` int(11) DEFAULT NULL COMMENT 'b_service_order.id',
  `visa_id` int(11) DEFAULT NULL COMMENT 'b_visa.id',
  `commission_order_id` int(11) DEFAULT NULL COMMENT 'b_commission_order.id',
  `adviser_id` int(11) DEFAULT NULL COMMENT ' (tb_adviser.id)',
  `offcial_id` int(11) DEFAULT NULL COMMENT ' (b_official.id,)',
  `kj_id` int(11) DEFAULT NULL COMMENT 'kj编号',
  `is_send` tinyint(1) DEFAULT '0' COMMENT '已经发送邮件',
  `user_id` int(11) DEFAULT NULL COMMENT '所属顾问编号',
  PRIMARY KEY (`id`),
  KEY `index` (`kj_id`,`send_date`,`service_order_id`,`visa_id`,`commission_order_id`,`adviser_id`,`offcial_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- 新学校库 Setting
CREATE TABLE `b_school_setting_new` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `type` int(11) DEFAULT NULL COMMENT '设置类型 (0:未选,1:固定比例-无额外补贴,2:固定比例-每人补贴,3:固定比例-一次性补贴,4:变动比例,5:固定底价-无额外补贴,6:固定底价-每人补贴,7:固定底价-一次性补贴)',
  `start_date` datetime NOT NULL COMMENT '合同开始时间',
  `end_date` datetime NOT NULL COMMENT '合同结束时间',
  `parameters` varchar(255) DEFAULT NULL COMMENT '参数',
  `level` int(1) DEFAULT NULL COMMENT 'setting级别:1/全部；2/学历级别；3/专业级别',
  `first_register` tinyint(1) DEFAULT NULL COMMENT '是否首次注册费',
  `register_fee` decimal(6,2) DEFAULT NULL COMMENT '注册费',
  `first_book` tinyint(1) DEFAULT NULL COMMENT '是否首次书本费',
  `book_fee` decimal(6,2) DEFAULT NULL COMMENT '书本费',
  `provider_id` int(11) NOT NULL COMMENT ' (b_school_institution.id)',
  `course_level` varchar(255) DEFAULT NULL COMMENT '学历级别',
  `course_id` varchar(255) DEFAULT NULL COMMENT 'b_school_course.id',
  `is_delete` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除，如果删除，可作为历史setting',
  PRIMARY KEY (`id`),
  KEY `index` (`provider_id`,`course_level`,`course_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

--退款
CREATE TABLE `b_refund` (
  `id` int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
`reviewed_date` datetime DEFAULT NULL COMMENT '审核完成时间',
`completed_date` datetime DEFAULT NULL COMMENT '退款完成时间',
  `state` varchar(8) NOT NULL COMMENT '状态',
  `type` varchar(4) NOT NULL COMMENT '服务类型(VISA:签证服务,OVST:留学服务)',
  `visa_id` int DEFAULT NULL COMMENT '签证佣金订单编号 (对应b_visa.id,如果visa_id和commission_order_id都为0则为手工退款)',
  `commission_order_id` int DEFAULT NULL COMMENT '佣金订单编号 (对应b_commission_order.id,如果visa_id和commission_order_id都为0则为手工退款)',
  `user_id` int NOT NULL COMMENT '所属顾客编号 (对应tb_user.id)',
  `applicant_id` int NOT NULL COMMENT '所属申请人编号 (对应b_applicant.id)',
  `adviser_id` int NOT NULL COMMENT '顾问编号 (对应tb_adviser.id)',
  `mara_id` int DEFAULT NULL COMMENT '所属MARA编号 (对应b_mara.id,曼拓和留学服务MARA为空)',
  `official_id` int DEFAULT NULL COMMENT '文案编号 (对应b_official.id,曼拓文案为空)',
  `school_id` int DEFAULT NULL COMMENT '学校编号 (对应b_school.id,留学服务专用字段)',
  `course_id` int(11) DEFAULT NULL COMMENT 'b_school_course.id',
  `receive_date` datetime DEFAULT NULL COMMENT '收款日期',
  `received` decimal(8,2) DEFAULT NULL COMMENT '实付金额',
  `amount` decimal(8,2) DEFAULT NULL COMMENT '退款金额',
  `payment_voucher_image_url` varchar(128) DEFAULT NULL COMMENT '付款凭证图片地址',
`payment_voucher_image_url_2` varchar(128) DEFAULT NULL COMMENT '付款凭证图片地址2',
`payment_voucher_image_url_3` varchar(128) DEFAULT NULL COMMENT '付款凭证图片地址3',
`payment_voucher_image_url_4` varchar(128) DEFAULT NULL COMMENT '付款凭证图片地址4',
`payment_voucher_image_url_5` varchar(128) DEFAULT NULL COMMENT '付款凭证图片地址5',
  `refund_voucher_image_url` varchar(128) DEFAULT NULL COMMENT '退款凭证图片地址',
  `refund_detail_id` int NOT NULL COMMENT '退款原因编号 (1:业务未成功办理，客户要求退款,2:客户取消业务,3:押金退款,4:referfee,5:Subagent结算,6:返佣,7:客户转错钱,全款退还,99:其它)',
  `refund_detail` varchar(255) DEFAULT NULL COMMENT '退款原因说明 (退款原因为其它时必填)',
  `currency_type` varchar(4) NOT NULL COMMENT '货币类型 (CNY:人民币,AUD:澳币)',
  `account_name` varchar(32) DEFAULT NULL COMMENT '银行账户名称',
  `bank_name` varchar(32) DEFAULT NULL COMMENT '银行名称',
  `bsb` varchar(32) DEFAULT NULL COMMENT 'BSB',
  `rmb_remarks` varchar(255) DEFAULT NULL COMMENT '人民币退款信息',
  `reason` varchar(255) DEFAULT NULL COMMENT '驳回原因',
  `remarks` varchar(255) DEFAULT NULL COMMENT '备注',
`note` varchar(255) DEFAULT NULL COMMENT '财务note'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

--汇率

CREATE TABLE `b_everyday_exchange_rate` (
  id int PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '编号',
  gmt_create datetime NOT NULL COMMENT '创建时间',
  gmt_modify datetime NOT NULL COMMENT '最后修改时间',
  currency varchar(4) NOT NULL COMMENT '币种(AUD:澳币,CNY:人民币)',
  original_exchange_rate decimal(6,4) NOT NULL COMMENT '原始汇率',
  znz_exchange_rate decimal(6,4) NOT NULL COMMENT '指南针汇率',
  update_time datetime NOT NULL COMMENT '汇率时间'
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

--文案等级

CREATE TABLE `b_official_grade` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `grade` varchar(32) NOT NULL COMMENT '等级',
  `rate` decimal(8,2) NOT NULL COMMENT 'rate',
  `ruler` int(11) NOT NULL COMMENT '佣金规则（0：固定rate）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100000 DEFAULT CHARSET=utf8;

--文案佣金
CREATE TABLE `b_visa_official` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
  `handling_date` datetime NOT NULL COMMENT '办理日期',
  `user_id` int(11) NOT NULL COMMENT '用户编号 (对应tb_user.id)',
  `receive_type_id` int(11) NOT NULL COMMENT '收款方式编号(对应b_receive_type.id)',
  `receive_date` datetime DEFAULT NULL COMMENT '收款日期',
  `service_id` int(11) NOT NULL COMMENT '移民-服务项目编号 (对应b_service.id)',
  `receivable` decimal(8,2) NOT NULL COMMENT '总计应收',
  `received` decimal(8,2) NOT NULL COMMENT '总计已收',
  `amount` decimal(8,2) NOT NULL COMMENT '本次收款',
  `gst` decimal(8,2) NOT NULL COMMENT 'GST',
  `deduct_gst` decimal(8,2) NOT NULL COMMENT 'Deduct GST',
  `bonus` decimal(8,2) NOT NULL COMMENT '月奖金',
  `adviser_id` int(11) NOT NULL COMMENT '顾问编号 (对应tb_adviser.id)',
  `official_id` int(11) NOT NULL COMMENT '文案编号 (对应b_official.id)',
  `is_close` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已取消',
  `remarks` text COMMENT '备注',
  `service_order_id` int(11) NOT NULL COMMENT '服务订单编号 (对应b_service_order.id)',
  `discount` decimal(8,2) NOT NULL DEFAULT '0.00' COMMENT '折扣',
  `mara_id` int(11) NOT NULL COMMENT '所属MARA编号 (对应b_mara.id)',
  `state` varchar(8) NOT NULL COMMENT '状态 (WAIT:佣金待审核,FINISH:佣金已审核,COMPLETE:结算完成,CLOSE:关闭)',
  `per_amount` decimal(8,2) NOT NULL COMMENT '本次应收款',
  `expect_amount` decimal(8,2) DEFAULT NULL COMMENT '预收业绩',
  `installment` int(11) NOT NULL COMMENT '付款次数',
  `code` varchar(64) DEFAULT NULL COMMENT '分组编码',
  `installment_num` int(11) NOT NULL COMMENT '本次分期付款次数',
  `payment_voucher_image_url_1` varchar(128) DEFAULT NULL COMMENT '支付凭证图片地址1',
  `payment_voucher_image_url_2` varchar(128) DEFAULT NULL COMMENT '支付凭证图片地址2',
  `commission_state` varchar(8) NOT NULL COMMENT '佣金状态(DJY:待结佣, YJY:已结佣, DZY:待追佣, YZY:已追佣, TQKY:提前扣佣)',
  `visa_voucher_image_url` varchar(128) DEFAULT NULL COMMENT '签证凭证图片地址',
  `bonus_date` datetime DEFAULT NULL COMMENT '月奖金支付时间',
  `payment_voucher_image_url_3` varchar(128) DEFAULT NULL COMMENT '支付凭证图片地址3',
  `payment_voucher_image_url_4` varchar(128) DEFAULT NULL COMMENT '支付凭证图片地址4',
  `payment_voucher_image_url_5` varchar(128) DEFAULT NULL COMMENT '支付凭证图片地址5',
  `bank_check` varchar(32) DEFAULT NULL COMMENT '银行对账',
  `is_checked` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否对账成功',
  `sure_expect_amount` decimal(8,2) DEFAULT NULL COMMENT '确认预收业绩',
  `kj_approval_date` datetime DEFAULT NULL COMMENT '财务审核时间',
  `invoice_number` varchar(128) DEFAULT NULL COMMENT 'InvoiceNo.',
  `verify_code` varchar(64) DEFAULT NULL COMMENT '对账使用的code,顾问名称+地区+随机数',
  `bank_date` datetime DEFAULT NULL COMMENT '入账时间,对应b_finance_code.bank_date',
  `refuse_reason` varchar(255) DEFAULT NULL COMMENT '驳回原因',
  `invoice_create` datetime DEFAULT NULL COMMENT '发票创建时间',
  `currency` varchar(4) DEFAULT 'AUD' COMMENT '(AUD:,CNY:)',
  `exchange_rate` decimal(6,4) NOT NULL DEFAULT '4.8000',
  `applicant_ids` varchar(255) DEFAULT NULL COMMENT '申请人编号(逗号分隔)',
  `predict_commission` decimal(8,2) DEFAULT NULL COMMENT '预估佣金',
  `commissionAmount` decimal(8,2) DEFAULT NULL COMMENT '计入佣金提点金额',
  `calculation` varchar(128) DEFAULT NULL COMMENT '计算规则',
  `predict_commissionAmount` decimal(8,2) DEFAULT NULL COMMENT '预估计入佣金提点金额',
  PRIMARY KEY (`id`),
  KEY `index_name` (`user_id`,`adviser_id`,`mara_id`,`official_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1000004 DEFAULT CHARSET=utf8;

--文案交接记录
CREATE TABLE `b_official_handover_log` (
`id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
`gmt_create` datetime NOT NULL COMMENT '创建时间',
`gmt_modify` datetime NOT NULL COMMENT '最后修改时间',
`official_id` int(11) NOT NULL COMMENT '文案id',
`new_official_id` int(11) NOT NULL COMMENT '新文案id',
`service_order_id` int(11) NOT NULL COMMENT '服务订单id',
PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100000 DEFAULT CHARSET=utf8;

--客户资料表
CREATE TABLE `b_customer_information` (
`id` int(11) NOT NULL AUTO_INCREMENT COMMENT '编号',
`gmt_create` datetime DEFAULT NULL COMMENT '创建时间',
`gmt_modify` datetime DEFAULT NULL COMMENT '最后修改时间',
`service_order_id` int(11) DEFAULT NULL COMMENT '服务订单编号 (对应b_service_order.id)',
`main_information` varchar(2000) DEFAULT NULL COMMENT '基本信息',
`past_information_list` varchar(1000) DEFAULT NULL COMMENT '曾用信息',
`chinese_commercial_code` varchar(100) DEFAULT NULL COMMENT '中文电码',
`contact_details` varchar(1000) DEFAULT NULL COMMENT '联系信息列表',
`citizenship_details` varchar(1000) DEFAULT NULL COMMENT '公民身份详情列表',
`ident_ification_number_list` varchar(1000) DEFAULT NULL COMMENT '护照,旅行证件详细信息',
`education_list` varchar(1000) DEFAULT NULL COMMENT '教育信息',
`language_skills` varchar(1000) DEFAULT NULL COMMENT '语言能力',
`addresses` varchar(1000) DEFAULT NULL COMMENT '地址信息',
`postal_address_list` varchar(1000) DEFAULT NULL COMMENT '邮件地址信息',
`health_questions` varchar(2000) DEFAULT NULL COMMENT '健康信息',
`character_issues` varchar(5000) DEFAULT NULL COMMENT '性格信息',
PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8;

