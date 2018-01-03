package org.zhinanzhen.b.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.SchoolDAO;
import org.zhinanzhen.b.dao.pojo.SchoolDO;
import org.zhinanzhen.b.service.SchoolService;
import org.zhinanzhen.b.service.pojo.SchoolDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.thrift.ErrorCodeEnum;

@Service("SchoolService")
public class SchoolServiceImpl extends BaseService implements SchoolService {
	
	@Resource
	private SchoolDAO schoolDao;

	@Override
	public int addSchool(SchoolDTO schoolDto) throws ServiceException {
		if (schoolDto == null) {
			ServiceException se = new ServiceException("schoolDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			SchoolDO schoolDo = mapper.map(schoolDto, SchoolDO.class);
			if (schoolDao.addSchool(schoolDo) > 0) {
				return schoolDo.getId();
			} else {
				return 0;
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int updateSchool(String name, String subject, String country) throws ServiceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<SchoolDTO> listSchool(String name, String subject, String country) throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SchoolDTO getSchoolById(int id) throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int deleteSchoolById(int id) throws ServiceException {
		// TODO Auto-generated method stub
		return 0;
	}

}
