package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.BrokerageSaDAO;
import org.zhinanzhen.b.dao.OfficialDAO;
import org.zhinanzhen.b.dao.ReceiveTypeDAO;
import org.zhinanzhen.b.dao.RemindDAO;
import org.zhinanzhen.b.dao.SchoolDAO;
import org.zhinanzhen.b.dao.pojo.BrokerageSaDO;
import org.zhinanzhen.b.dao.pojo.BrokerageSaListDO;
import org.zhinanzhen.b.dao.pojo.OfficialDO;
import org.zhinanzhen.b.dao.pojo.ReceiveTypeDO;
import org.zhinanzhen.b.dao.pojo.RemindDO;
import org.zhinanzhen.b.dao.pojo.SchoolDO;
import org.zhinanzhen.b.service.AbleStateEnum;
import org.zhinanzhen.b.service.BrokerageSaService;
import org.zhinanzhen.b.service.pojo.BrokerageSaDTO;
import org.zhinanzhen.tb.dao.AdviserDAO;
import org.zhinanzhen.tb.dao.UserDAO;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
import org.zhinanzhen.tb.dao.pojo.UserDO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.ErrorCodeEnum;

@Service("BrokerageSaService")
public class BrokerageSaServiceImpl extends BaseService implements BrokerageSaService {

	@Resource
	private BrokerageSaDAO brokerageSaDao;

	@Resource
	private SchoolDAO schoolDao;

	@Resource
	private AdviserDAO adviserDao;

	@Resource
	private OfficialDAO officialDao;

	@Resource
	private ReceiveTypeDAO receiveTypeDao;

	@Resource
	private RemindDAO remindDao;

	@Resource
	private UserDAO userDao;

	@Override
	public int addBrokerageSa(BrokerageSaDTO brokerageSaDto) throws ServiceException {
		if (brokerageSaDto == null) {
			ServiceException se = new ServiceException("brokerageSaDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			BrokerageSaDO brokerageSaDo = mapper.map(brokerageSaDto, BrokerageSaDO.class);
			if (brokerageSaDao.addBrokerageSa(brokerageSaDo) > 0) {
				brokerageSaDto.setId(brokerageSaDo.getId());
				return brokerageSaDo.getId();
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
	public int updateBrokerageSa(BrokerageSaDTO brokerageSaDto) throws ServiceException {
		if (brokerageSaDto == null) {
			ServiceException se = new ServiceException("brokerageSaDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			BrokerageSaDO brokerageSaDo = mapper.map(brokerageSaDto, BrokerageSaDO.class);
			return brokerageSaDao.updateBrokerageSa(brokerageSaDo);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int countBrokerageSa(String keyword, String startHandlingDate, String endHandlingDate, String startDate,
			String endDate, Integer adviserId, Integer schoolId, Integer userId) throws ServiceException {
		return brokerageSaDao.countBrokerageSa(keyword, startHandlingDate, theDateTo23_59_59(endHandlingDate),
				startDate, theDateTo23_59_59(endDate), adviserId, schoolId, userId);
	}

	@Override
	public List<BrokerageSaDTO> listBrokerageSa(String keyword, String startHandlingDate, String endHandlingDate,
			String startCreateDate, String endCreateDate, Integer adviserId, Integer schoolId, Integer userId,
			int pageNum, int pageSize) throws ServiceException {
		if (pageNum < 0) {
			pageNum = DEFAULT_PAGE_NUM;
		}
		if (pageSize < 0) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		List<BrokerageSaDTO> brokerageSaDtoList = new ArrayList<>();
		List<BrokerageSaListDO> brokerageSaListDoList = new ArrayList<>();
		try {
			brokerageSaListDoList = brokerageSaDao.listBrokerageSa(keyword, startHandlingDate,
					theDateTo23_59_59(endHandlingDate), startCreateDate, theDateTo23_59_59(endCreateDate), adviserId,
					schoolId, userId, pageNum * pageSize, pageSize);
			if (brokerageSaListDoList == null) {
				return null;
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (BrokerageSaListDO brokerageSaListDo : brokerageSaListDoList) {
			BrokerageSaDTO brokerageSaDto = mapper.map(brokerageSaListDo, BrokerageSaDTO.class);
			AdviserDO adviserDo = adviserDao.getAdviserById(brokerageSaListDo.getAdviserId());
			if (adviserDo != null) {
				brokerageSaDto.setAdviserName(adviserDo.getName());
			}
			OfficialDO officialDo = officialDao.getOfficialById(brokerageSaListDo.getOfficialId());
			if (officialDo != null) {
				brokerageSaDto.setOfficialName(officialDo.getName());
			}
			ReceiveTypeDO receiveTypeDo = receiveTypeDao.getReceiveTypeById(brokerageSaListDo.getReceiveTypeId());
			if (receiveTypeDo != null) {
				brokerageSaDto.setReceiveTypeName(receiveTypeDo.getName());
			}
			List<Date> remindDateList = new ArrayList<>();
			List<RemindDO> remindDoList = remindDao.listRemindByBrokerageSaId(brokerageSaDto.getId(), adviserId,
					AbleStateEnum.ENABLED.toString());
			for (RemindDO remindDo : remindDoList) {
				remindDateList.add(remindDo.getRemindDate());
			}
			brokerageSaDto.setRemindDateList(remindDateList);
			brokerageSaDtoList.add(brokerageSaDto);
		}
		return brokerageSaDtoList;
	}

	@Override
	public BrokerageSaDTO getBrokerageSaById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		BrokerageSaDTO brokerageSaDto = null;
		try {
			BrokerageSaDO brokerageSaDo = brokerageSaDao.getBrokerageSaById(id);
			if (brokerageSaDo == null) {
				return null;
			}
			brokerageSaDto = mapper.map(brokerageSaDo, BrokerageSaDTO.class);
			if (brokerageSaDto.getUserId() > 0) {
				UserDO userDo = userDao.getUserById(brokerageSaDto.getUserId());
				if (userDo != null) {
					brokerageSaDto.setUserName(userDo.getName());
					brokerageSaDto.setPhone(userDo.getPhone());
					brokerageSaDto.setBirthday(userDo.getBirthday());
				}
			}
			if (brokerageSaDto.getSchoolId() > 0) {
				SchoolDO schoolDo = schoolDao.getSchoolById(brokerageSaDto.getSchoolId());
				if (schoolDo != null)
					brokerageSaDto.setSchoolName(schoolDo.getName());
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		return brokerageSaDto;
	}

	@Override
	public int deleteBrokerageSaById(int id) throws ServiceException {
		return brokerageSaDao.deleteBrokerageSaById(id);
	}

}
