package org.zhinanzhen.b.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.RemindDAO;
import org.zhinanzhen.b.service.RemindService;
import org.zhinanzhen.b.service.pojo.RemindDTO;
import org.zhinanzhen.tb.service.impl.BaseService;

@Service("OfficialService")
public class RemindServiceImpl extends BaseService implements RemindService {

	@Resource
	private RemindDAO remindDao;

	@Override
	public int addRemind(RemindDTO remindDto) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<RemindDTO> listRemindBySchoolBrokerageSaId(int schoolBrokerageSaId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<RemindDTO> listRemindByRemindDate(Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int deleteSchoolById(int id) {
		// TODO Auto-generated method stub
		return 0;
	}

}
