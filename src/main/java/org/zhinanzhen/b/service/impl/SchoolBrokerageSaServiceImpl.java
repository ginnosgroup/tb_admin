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
		if (schoolBrokerageSaDto == null) {
			ServiceException se = new ServiceException("schoolBrokerageSaDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			// 会计核对closed掉的佣金记录时，编辑该条记录，如果【学校支付金额】字段值不为空，保存时，已结佣字段自动变为是。
			if (schoolBrokerageSaDto.getPayAmount() > 0) {
				schoolBrokerageSaDto.setSettleAccounts(true);
			}
			SchoolBrokerageSaDO schoolBrokerageSaDo = mapper.map(schoolBrokerageSaDto, SchoolBrokerageSaDO.class);
			if (schoolBrokerageSaDao.addSchoolBrokerageSa(schoolBrokerageSaDo) > 0) {
				return schoolBrokerageSaDo.getId();
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
	public int updateSchoolBrokerageSa(SchoolBrokerageSaDTO schoolBrokerageSaDto) throws ServiceException {
		if (schoolBrokerageSaDto == null) {
			ServiceException se = new ServiceException("schoolBrokerageSaDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			// 会计核对closed掉的佣金记录时，编辑该条记录，如果【学校支付金额】字段值不为空，保存时，已结佣字段自动变为是。
			if (schoolBrokerageSaDto.getPayAmount() > 0) {
				schoolBrokerageSaDto.setSettleAccounts(true);
			}
			SchoolBrokerageSaDO schoolBrokerageSaDo = mapper.map(schoolBrokerageSaDto, SchoolBrokerageSaDO.class);
			return schoolBrokerageSaDao.updateSchoolBrokerageSa(schoolBrokerageSaDo);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int countSchoolBrokerageSa(String keyword, String startHandlingDate, String endHandlingDate,
			String startDate, String endDate, Integer adviserId, Integer schoolId, Integer subagencyId,
			Boolean isSettleAccounts) throws ServiceException {
		return schoolBrokerageSaDao.countSchoolBrokerageSa(keyword, startHandlingDate, endHandlingDate, startDate,
				endDate, adviserId, schoolId, subagencyId, isSettleAccounts);
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
