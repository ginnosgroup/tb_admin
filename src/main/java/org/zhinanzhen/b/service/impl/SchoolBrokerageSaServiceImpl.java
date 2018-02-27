package org.zhinanzhen.b.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.SchoolBrokerageSaDAO;
import org.zhinanzhen.b.dao.pojo.SchoolBrokerageSaDO;
import org.zhinanzhen.b.service.SchoolBrokerageSaService;
import org.zhinanzhen.b.service.pojo.SchoolBrokerageSaDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.thrift.ErrorCodeEnum;

@Service("SchoolBrokerageSaService")
public class SchoolBrokerageSaServiceImpl extends BaseService implements SchoolBrokerageSaService {

	@Resource
	private SchoolBrokerageSaDAO schoolBrokerageSaDao;

	@Override
	public int addSchoolBrokerageSa(SchoolBrokerageSaDTO schoolBrokerageSaDto) throws ServiceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int updateSchoolBrokerageSa(SchoolBrokerageSaDTO schoolBrokerageSaDto) throws ServiceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int countSchoolBrokerageSa(String keyword, String startHandlingDate, String endHandlingDate,
			String startDate, String endDate, Integer adviserId, Integer schoolId, Integer subagencyId,
			Boolean isSettleAccounts) throws ServiceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<SchoolBrokerageSaDTO> listSchoolBrokerageSa(String keyword, String startHandlingDate,
			String endHandlingDate, String startDate, String endDate, Integer adviserId, Integer schoolId,
			Integer subagencyId, Boolean isSettleAccounts, int pageNum, int pageSize) throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SchoolBrokerageSaDTO getSchoolBrokerageSaById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		SchoolBrokerageSaDTO schoolBrokerageSaDto = null;
		try {
			SchoolBrokerageSaDO schoolBrokerageSaDo = schoolBrokerageSaDao.getSchoolBrokerageSaById(id);
			if (schoolBrokerageSaDo == null) {
				return null;
			}
			schoolBrokerageSaDto = mapper.map(schoolBrokerageSaDo, SchoolBrokerageSaDTO.class);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		return schoolBrokerageSaDto;
	}

	@Override
	public int deleteSchoolBrokerageSaById(int id) throws ServiceException {
		return schoolBrokerageSaDao.deleteSchoolBrokerageSaById(id);
	}

}
