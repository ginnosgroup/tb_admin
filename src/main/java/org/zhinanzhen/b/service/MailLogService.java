package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.MailLogDTO;

public interface MailLogService {

	int countMailLog();

	List<MailLogDTO> listMailLog(int pageNum, int pageSize);

}
