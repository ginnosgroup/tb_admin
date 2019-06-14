package org.zhinanzhen.tb.service.impl;

import java.text.SimpleDateFormat;

import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.ikasoa.core.utils.StringUtil;

/**
 * 基础服务
 * 
 * @author <a href="mailto:leisu@zhinanzhen.org">sulei</a>
 * @version 0.1
 */
public abstract class BaseService {

	protected static final Logger LOG = LoggerFactory.getLogger(BaseService.class);

	protected Mapper mapper = new DozerBeanMapper();

	protected SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");

	/**
	 * 默认起始页编码
	 */
	protected static final int DEFAULT_PAGE_NUM = 0;

	/**
	 * 默认每页最大条数
	 */
	protected static final int DEFAULT_PAGE_SIZE = 20;

	/**
	 * 事务回滚
	 */
	protected void rollback() {
		TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
	}

	protected String theDateTo23_59_59(String date) {
		return StringUtil.isNotEmpty(date) ? date.split(" ")[0] + " 23:59:59" : date;
	}

}
