package org.zhinanzhen.b.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.RemindDAO;
import org.zhinanzhen.b.service.RemindService;
import org.zhinanzhen.b.service.pojo.RemindDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

@Service("OfficialService")
public class RemindServiceImpl extends BaseService implements RemindService {

	@Resource
	private RemindDAO remindDao;

	@Override
	public int addRemind(RemindDTO remindDto) throws ServiceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<RemindDTO> listRemindBySchoolBrokerageSaId(int schoolBrokerageSaId) throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<RemindDTO> listRemindByRemindDate(Date date) throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int deleteSchoolById(int id) throws ServiceException {
		return remindDao.deleteSchoolById(id);
	}

}
