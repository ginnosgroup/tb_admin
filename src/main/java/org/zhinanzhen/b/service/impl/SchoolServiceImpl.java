package org.zhinanzhen.b.service.impl;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.pojo.SchoolDO;
import org.zhinanzhen.b.service.SchoolService;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

@Service("SchoolService")
public class SchoolServiceImpl extends BaseService implements SchoolService {

	@Override
	public int addSchool(SchoolDO schoolDO) throws ServiceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int updateSchool(String name, String subject, String country) throws ServiceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public SchoolDO listSchool(String name, String subject, String country) throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SchoolDO getSchoolById(int id) throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int deleteSchoolById(int id) throws ServiceException {
		// TODO Auto-generated method stub
		return 0;
	}

}
