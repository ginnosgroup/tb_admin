package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.RemindDAO;
import org.zhinanzhen.b.dao.SchoolBrokerageSaDAO;
import org.zhinanzhen.b.dao.SchoolDAO;
import org.zhinanzhen.b.dao.OfficialDAO;
import org.zhinanzhen.b.dao.pojo.RemindDO;
import org.zhinanzhen.b.dao.pojo.SchoolBrokerageSaByDashboardListDO;
import org.zhinanzhen.b.dao.pojo.SchoolBrokerageSaDO;
import org.zhinanzhen.b.dao.pojo.SchoolBrokerageSaListDO;
import org.zhinanzhen.b.dao.pojo.SchoolDO;
import org.zhinanzhen.b.dao.pojo.OfficialDO;
import org.zhinanzhen.b.service.AbleStateEnum;
import org.zhinanzhen.b.service.SchoolBrokerageSaService;
import org.zhinanzhen.b.service.pojo.SchoolBrokerageSaByDashboardListDTO;
import org.zhinanzhen.b.service.pojo.SchoolBrokerageSaDTO;
import org.zhinanzhen.tb.dao.AdviserDAO;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.thrift.ErrorCodeEnum;

@Service("SchoolBrokerageSaService")
public class SchoolBrokerageSaServiceImpl extends BaseService implements SchoolBrokerageSaService {

	@Resource
	private SchoolBrokerageSaDAO schoolBrokerageSaDao;

	@Resource
	private RemindDAO remindDao;

	@Resource
	private AdviserDAO adviserDao;

	@Resource
	private OfficialDAO officialDao;

	@Resource
	private SchoolDAO schoolDao;

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
				schoolBrokerageSaDto.setId(schoolBrokerageSaDo.getId());
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
			SchoolBrokerageSaDO schoolBrokerageSaDo = mapper.map(schoolBrokerageSaDto, SchoolBrokerageSaDO.class);
			if (schoolBrokerageSaDo.getId() > 0) {
				if (schoolBrokerageSaDao.getSchoolBrokerageSaById(schoolBrokerageSaDo.getId()).isClose()) {
					ServiceException se = new ServiceException("This case is close ! You can't update it .");
					se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
					throw se;
				}
			}
			return schoolBrokerageSaDao.updateSchoolBrokerageSa(schoolBrokerageSaDo);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int countSchoolBrokerageSa(String keyword, String startHandlingDate, String endHandlingDate,
			String startDate, String endDate, Integer adviserId, Integer schoolId, Integer subagencyId, Integer userId,
			Boolean isSettleAccounts) throws ServiceException {
		if (isSettleAccounts == null)
			isSettleAccounts = false;
		return schoolBrokerageSaDao.countSchoolBrokerageSa(keyword, startHandlingDate,
				theDateTo23_59_59(endHandlingDate), startDate, theDateTo23_59_59(endDate), adviserId, schoolId,
				subagencyId, userId, isSettleAccounts);
	}

	@Override
	public List<SchoolBrokerageSaDTO> listSchoolBrokerageSa(String keyword, String startHandlingDate,
			String endHandlingDate, String startDate, String endDate, Integer adviserId, Integer schoolId,
			Integer subagencyId, Integer userId, Boolean isSettleAccounts, int pageNum, int pageSize)
			throws ServiceException {
		if (pageNum < 0) {
			pageNum = DEFAULT_PAGE_NUM;
		}
		if (pageSize < 0) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		if (isSettleAccounts == null) {
			isSettleAccounts = false;
		}
		List<SchoolBrokerageSaDTO> schoolBrokerageSaDtoList = new ArrayList<>();
		List<SchoolBrokerageSaListDO> schoolBrokerageSaListDoList = new ArrayList<>();
		try {
			schoolBrokerageSaListDoList = schoolBrokerageSaDao.listSchoolBrokerageSa(keyword, startHandlingDate,
					theDateTo23_59_59(endHandlingDate), startDate, theDateTo23_59_59(endDate), adviserId, schoolId,
					subagencyId, userId, isSettleAccounts, pageNum * pageSize, pageSize);
			if (schoolBrokerageSaListDoList == null) {
				return null;
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (SchoolBrokerageSaListDO schoolBrokerageSaListDo : schoolBrokerageSaListDoList) {
			SchoolBrokerageSaDTO schoolBrokerageSaDto = mapper.map(schoolBrokerageSaListDo, SchoolBrokerageSaDTO.class);
			List<Date> remindDateList = new ArrayList<>();
			List<RemindDO> remindDoList = remindDao.listRemindBySchoolBrokerageSaId(schoolBrokerageSaDto.getId(),
					adviserId, AbleStateEnum.ENABLED.toString());
			for (RemindDO remindDo : remindDoList) {
				remindDateList.add(remindDo.getRemindDate());
			}
			schoolBrokerageSaDto.setRemindDateList(remindDateList);
			AdviserDO adviserDo = adviserDao.getAdviserById(schoolBrokerageSaListDo.getAdviserId());
			if (adviserDo != null) {
				schoolBrokerageSaDto.setAdviserName(adviserDo.getName());
			}
			OfficialDO officialDo = officialDao.getOfficialById(schoolBrokerageSaListDo.getOfficialId());
			if (officialDo != null) {
				schoolBrokerageSaDto.setOfficialName(officialDo.getName());
			}
			schoolBrokerageSaDtoList.add(schoolBrokerageSaDto);
		}
		return schoolBrokerageSaDtoList;
	}

	@Override
	public List<SchoolBrokerageSaByDashboardListDTO> listSchoolBrokerageSaByDashboard(int adviserId, int pageNum,
			int pageSize) throws ServiceException {
		if (pageNum < 0) {
			pageNum = DEFAULT_PAGE_NUM;
		}
		if (pageSize < 0) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		List<SchoolBrokerageSaByDashboardListDTO> schoolBrokerageSaByDashboardListDtoList = new ArrayList<>();
		List<SchoolBrokerageSaByDashboardListDO> schoolBrokerageSaByDashboardListDoList = new ArrayList<>();
		try {
			schoolBrokerageSaByDashboardListDoList = schoolBrokerageSaDao
					.listSchoolBrokerageSaByDashboard(adviserId > 0 ? adviserId : null, pageNum * pageSize, pageSize);
			if (schoolBrokerageSaByDashboardListDoList == null) {
				return null;
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (SchoolBrokerageSaByDashboardListDO schoolBrokerageSaByDashboardListDo : schoolBrokerageSaByDashboardListDoList) {
			SchoolBrokerageSaByDashboardListDTO schoolBrokerageSaByDashboardListDto = mapper
					.map(schoolBrokerageSaByDashboardListDo, SchoolBrokerageSaByDashboardListDTO.class);
			List<Date> remindDateList = new ArrayList<>();
			List<RemindDO> remindDoList = remindDao.listRemindBySchoolBrokerageSaId(
					schoolBrokerageSaByDashboardListDto.getId(), adviserId > 0 ? adviserId : null,
					AbleStateEnum.ENABLED.toString());
			for (RemindDO remindDo : remindDoList) {
				remindDateList.add(remindDo.getRemindDate());
			}
			schoolBrokerageSaByDashboardListDto.setRemindDateList(remindDateList);
			AdviserDO adviserDo = adviserDao.getAdviserById(schoolBrokerageSaByDashboardListDo.getAdviserId());
			if (adviserDo != null) {
				schoolBrokerageSaByDashboardListDto.setAdviserName(adviserDo.getName());
			}
			OfficialDO officialDo = officialDao.getOfficialById(schoolBrokerageSaByDashboardListDo.getOfficialId());
			if (officialDo != null) {
				schoolBrokerageSaByDashboardListDto.setOfficialName(officialDo.getName());
			}
			if (schoolBrokerageSaByDashboardListDto.getRemindDateList().size() > 0)
				schoolBrokerageSaByDashboardListDtoList.add(schoolBrokerageSaByDashboardListDto);
		}
		return schoolBrokerageSaByDashboardListDtoList;
	}

	public int updateClose(int id, boolean isClose) throws ServiceException {
		SchoolBrokerageSaDO schoolBrokerageSaDo = new SchoolBrokerageSaDO();
		try {
			schoolBrokerageSaDo.setId(id);
			// 会计核对closed掉的佣金记录时，编辑该条记录，如果【学校支付金额】字段值不为空，保存时，已结佣字段自动变为是。
			if (schoolBrokerageSaDao.getSchoolBrokerageSaById(id).getPayAmount() > 0) {
				schoolBrokerageSaDo.setSettleAccounts(true);
			}
			schoolBrokerageSaDo.setClose(isClose);
			return schoolBrokerageSaDao.updateSchoolBrokerageSa(schoolBrokerageSaDo);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
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
			if (schoolBrokerageSaDo == null)
				return null;
			schoolBrokerageSaDto = mapper.map(schoolBrokerageSaDo, SchoolBrokerageSaDTO.class);
			AdviserDO adviserDo = adviserDao.getAdviserById(schoolBrokerageSaDo.getAdviserId());
			if (adviserDo != null)
				schoolBrokerageSaDto.setAdviserName(adviserDo.getName());
			OfficialDO officialDo = officialDao.getOfficialById(schoolBrokerageSaDo.getOfficialId());
			if (officialDo != null)
				schoolBrokerageSaDto.setOfficialName(officialDo.getName());
			SchoolDO schoolDo = schoolDao.getSchoolById(schoolBrokerageSaDo.getSchoolId());
			if (schoolDo != null)
				schoolBrokerageSaDto.setSchoolName(schoolDo.getName());
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
