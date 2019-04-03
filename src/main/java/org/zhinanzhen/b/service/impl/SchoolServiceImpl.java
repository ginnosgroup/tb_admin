package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
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
import com.ikasoa.core.utils.StringUtil;

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
	public int updateSchool(int id, String name, String subject, String country) throws ServiceException {
		try {
			return schoolDao.updateSchool(id, name, subject, country);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public List<SchoolDTO> list(String name, String subject, String country) throws ServiceException {
		List<SchoolDTO> schoolDtoList = new ArrayList<SchoolDTO>();
		List<SchoolDO> schoolDoList = new ArrayList<SchoolDO>();
		try {
			schoolDoList = schoolDao.list(name, subject, country);
			if (schoolDoList == null) {
				return null;
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (SchoolDO schoolDo : schoolDoList) {
			SchoolDTO schoolDto = mapper.map(schoolDo, SchoolDTO.class);
			schoolDtoList.add(schoolDto);
		}
		return schoolDtoList;
	}

	@Override
	public List<SchoolDTO> listSchool(String name, String country) throws ServiceException {
		List<SchoolDTO> schoolDtoList = new ArrayList<SchoolDTO>();
		List<SchoolDO> schoolDoList = new ArrayList<SchoolDO>();
		try {
			schoolDoList = schoolDao.listSchool(name, country);
			if (schoolDoList == null) {
				return null;
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (SchoolDO schoolDo : schoolDoList) {
			SchoolDTO schoolDto = mapper.map(schoolDo, SchoolDTO.class);
			schoolDtoList.add(schoolDto);
		}
		return schoolDtoList;
	}

	@Override
	public SchoolDTO getSchoolById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		SchoolDTO schoolDto = null;
		try {
			SchoolDO schoolDo = schoolDao.getSchoolById(id);
			if (schoolDo == null) {
				return null;
			}
			schoolDto = mapper.map(schoolDo, SchoolDTO.class);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		return schoolDto;
	}

	@Override
	public int deleteSchoolById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			return schoolDao.deleteSchoolById(id);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int deleteSchoolByName(String name) throws ServiceException {
		if (StringUtil.isEmpty(name)) {
			ServiceException se = new ServiceException("name error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			return schoolDao.deleteSchoolByName(name);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

}
