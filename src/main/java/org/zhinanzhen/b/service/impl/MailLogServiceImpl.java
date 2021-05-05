package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.MailLogDAO;
import org.zhinanzhen.b.dao.pojo.MailLogDO;
import org.zhinanzhen.b.service.MailLogService;
import org.zhinanzhen.b.service.pojo.MailLogDTO;
import org.zhinanzhen.tb.service.impl.BaseService;

@Service("MailLogService")
public class MailLogServiceImpl extends BaseService implements MailLogService {

	@Resource
	private MailLogDAO mailLogDao;

	@Override
	public int countMailLog() {
		return mailLogDao.countMailLog();
	}

	@Override
	public List<MailLogDTO> listMailLog(int pageNum, int pageSize) {
		if (pageNum < 0)
			pageNum = DEFAULT_PAGE_NUM;
		if (pageSize < 0)
			pageSize = DEFAULT_PAGE_SIZE;
		List<MailLogDTO> mailLogDtoList = new ArrayList<>();
		List<MailLogDO> mailLogDoList = mailLogDao.listMailLog(pageNum * pageSize, pageSize);
		if (mailLogDoList != null)
			for (MailLogDO mailLogDo : mailLogDoList)
				mailLogDtoList.add(mapper.map(mailLogDo, MailLogDTO.class));
		return mailLogDtoList;
	}

}
