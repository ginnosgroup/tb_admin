package org.zhinanzhen.tb.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

import javax.annotation.Resource;

import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.zhinanzhen.b.dao.MailLogDAO;
import org.zhinanzhen.b.dao.pojo.MailLogDO;
import org.zhinanzhen.b.service.pojo.ApplicantDTO;
import org.zhinanzhen.tb.utils.MD5Util;
import org.zhinanzhen.tb.utils.SendEmailUtil;

import com.ikasoa.core.utils.ObjectUtil;
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

	@Resource
	private MailLogDAO mailLogDao;

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

	protected String theDateTo00_00_00(String date) {
		return StringUtil.isNotEmpty(date) ? date.split(" ")[0] + " 00:00:00" : date;
	}
	
	protected double roundHalfUp2(double d) {
		return new BigDecimal(d).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	protected void sendMail(String mail, String title, String content) {
		if (StringUtil.isEmpty(mail) || StringUtil.isEmpty(title)) {
			LOG.error("参数错误!");
			return;
		}
		String code;
		try {
			code = MD5Util.getMD5(StringUtil.merge(mail, title, content));
		} catch (Exception e) {
			LOG.error(StringUtil.merge("生成code异常:", e.getMessage()));
			return;
		}
		MailLogDO mailLogDo = mailLogDao.getMailLogByCode(code);
		if (mailLogDo != null) { // 避免发送重复的邮件
			mailLogDao.refresh(mailLogDo.getId());
			LOG.warn(StringUtil.merge("该邮件已发送过了,code=", code, ",date=", mailLogDo.getGmtCreate()));
			return;
		}
		if (mailLogDao.addMailLog(new MailLogDO(code, mail, title, content)) > 0)
			SendEmailUtil.send(mail, title, content);
	}
	
	protected String getApplicantName(ApplicantDTO applicantDto) {
		return ObjectUtil.isNotNull(applicantDto) ? applicantDto.getFirstname() + " " + applicantDto.getSurname()
				: "unknown";
	}

}
