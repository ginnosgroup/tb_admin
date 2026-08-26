package org.zhinanzhen.tb.service.impl;

import java.io.File;
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
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.utils.MD5Util;
import org.zhinanzhen.tb.utils.SendEmailUtil;

import com.ikasoa.core.ErrorCodeEnum;
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

	/**
	 * 按现有邮件日志和去重规则发送多附件邮件。附件名称和大小参与去重码，
	 * 同一案件重新生成文件后仍可再次发送。
	 */
	protected void sendMailWithAttachments(String mail, String title, String content, File... attachments)
			throws ServiceException {
		if (StringUtil.isEmpty(mail) || StringUtil.isEmpty(title)) {
			throw mailServiceException("邮件收件人或标题为空.", ErrorCodeEnum.PARAMETER_ERROR.code(), null);
		}
		if (attachments == null || attachments.length == 0) {
			throw mailServiceException("邮件附件为空.", ErrorCodeEnum.PARAMETER_ERROR.code(), null);
		}

		StringBuilder attachmentSignature = new StringBuilder();
		for (File attachment : attachments) {
			if (attachment == null || !attachment.isFile()) {
				throw mailServiceException("邮件附件不存在: " + (attachment == null ? "null" : attachment.getPath()),
						ErrorCodeEnum.DATA_ERROR.code(), null);
			}
			attachmentSignature.append('|').append(attachment.getName()).append(':').append(attachment.length());
		}

		try {
			String code = MD5Util.getMD5(StringUtil.merge(mail, title, content, attachmentSignature.toString()));
			MailLogDO mailLogDo = mailLogDao.getMailLogByCode(code);
			if (mailLogDo != null) {
				mailLogDao.refresh(mailLogDo.getId());
				LOG.warn(StringUtil.merge("该附件邮件已发送过了,code=", code, ",date=", mailLogDo.getGmtCreate()));
				return;
			}
			if (mailLogDao.addMailLog(new MailLogDO(code, mail, title, content)) <= 0) {
				throw mailServiceException("保存邮件发送日志失败.", ErrorCodeEnum.EXECUTE_ERROR.code(), null);
			}
			SendEmailUtil.sendAttachments(mail, title, content, attachments);
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			throw mailServiceException("发送附件邮件失败: " + e.getMessage(), ErrorCodeEnum.OTHER_ERROR.code(), e);
		}
	}

	private ServiceException mailServiceException(String message, int code, Exception cause) {
		ServiceException exception = cause == null ? new ServiceException(message) : new ServiceException(message, cause);
		exception.setCode(code);
		return exception;
	}
	
	protected String getApplicantName(ApplicantDTO applicantDto) {
		return ObjectUtil.isNotNull(applicantDto) ? applicantDto.getFirstname() + " " + applicantDto.getSurname()
				: "unknown";
	}

}
