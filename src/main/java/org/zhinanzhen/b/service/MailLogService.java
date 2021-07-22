package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.MailLogDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface MailLogService {

	int countMailLog() throws ServiceException;

	List<MailLogDTO> listMailLog(int pageNum, int pageSize) throws ServiceException;

}
