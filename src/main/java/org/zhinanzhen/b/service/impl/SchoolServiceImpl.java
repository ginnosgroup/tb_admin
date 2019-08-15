package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.BrokerageSaDAO;
import org.zhinanzhen.b.dao.SchoolDAO;
import org.zhinanzhen.b.dao.SchoolSettingDAO;
import org.zhinanzhen.b.dao.pojo.SchoolDO;
import org.zhinanzhen.b.dao.pojo.SchoolSettingDO;
import org.zhinanzhen.b.service.SchoolService;
import org.zhinanzhen.b.service.pojo.SchoolDTO;
import org.zhinanzhen.b.service.pojo.SchoolSettingDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.thrift.ErrorCodeEnum;
import com.ikasoa.core.utils.StringUtil;

@Service("SchoolService")
public class SchoolServiceImpl extends BaseService implements SchoolService {

	@Resource
	private SchoolDAO schoolDao;

	@Resource
	private SchoolSettingDAO schoolSettingDao;

	@Resource
	private BrokerageSaDAO brokerageSaDao;

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
				schoolDto.setId(schoolDo.getId());
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
	public List<SchoolDTO> list(String name) throws ServiceException {
		List<SchoolDTO> schoolDtoList = new ArrayList<SchoolDTO>();
		List<SchoolDO> schoolDoList = new ArrayList<SchoolDO>();
		try {
			schoolDoList = schoolDao.list2(name);
			if (schoolDoList == null)
				return null;
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
	public List<SchoolDTO> list(String name, String subject, String country) throws ServiceException {
		List<SchoolDTO> schoolDtoList = new ArrayList<SchoolDTO>();
		List<SchoolDO> schoolDoList = new ArrayList<SchoolDO>();
		try {
			schoolDoList = schoolDao.list(name, subject, country);
			if (schoolDoList == null)
				return null;
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
	public int updateSchoolSetting(int id, int type, Date startDate, Date endDate, String parameters)
			throws ServiceException {
		return schoolSettingDao.update(id, type, startDate, endDate, parameters);
	}

	@Override
	public List<SchoolSettingDTO> listSchoolSetting() throws ServiceException {
		List<SchoolSettingDTO> schoolSettingDtoList = new ArrayList<SchoolSettingDTO>();
		List<SchoolDO> schoolDoList = schoolDao.listSchool(null, null);
		if (schoolDoList == null)
			return null;
		schoolDoList.forEach(schoolDo -> {
			String name = schoolDo.getName();
			SchoolSettingDO schoolSettingDo = schoolSettingDao.get(name);
			SchoolSettingDTO schoolSettingDto = null;
			if (schoolSettingDo != null)
				schoolSettingDto = mapper.map(schoolSettingDo, SchoolSettingDTO.class);
			else {
				schoolSettingDo = new SchoolSettingDO();
				schoolSettingDo.setSchoolName(name);
				schoolSettingDao.add(schoolSettingDo);
				schoolSettingDto = mapper.map(schoolSettingDo, SchoolSettingDTO.class);
			}
			if (name != null) {
				schoolSettingDto.setCount(brokerageSaDao.countBrokerageSaBySchoolName(name));
				if (brokerageSaDao.sumTuitionFeeBySchoolName(name) != null)
					schoolSettingDto.setAmount(brokerageSaDao.sumTuitionFeeBySchoolName(name));
			}
			schoolSettingDtoList.add(schoolSettingDto);
		});
		return schoolSettingDtoList;
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
