package org.zhinanzhen.tb.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.service.AbleStateEnum;
import org.zhinanzhen.tb.dao.ConsultationDAO;
import org.zhinanzhen.tb.dao.UserDAO;
import org.zhinanzhen.tb.dao.pojo.ConsultationDO;
import org.zhinanzhen.tb.dao.pojo.UserDO;
import org.zhinanzhen.tb.service.ConsultationService;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.pojo.ConsultationDTO;

import com.ikasoa.core.ErrorCodeEnum;
import com.ikasoa.core.utils.StringUtil;

@Service("ConsultationService")
public class ConsultationServiceImpl extends BaseService implements ConsultationService {

	@Resource
	private ConsultationDAO consultationDao;

	@Resource
	private UserDAO userDao;

	@Override
	public int addConsultation(ConsultationDTO consultationDto) throws ServiceException {
		if (consultationDto == null) {
			ServiceException se = new ServiceException("consultationDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			ConsultationDO consultationDo = mapper.map(consultationDto, ConsultationDO.class);
			if (consultationDto.getState() != null)
				consultationDo.setState(consultationDto.getState().toString());
			if (consultationDao.addConsultation(consultationDo) > 0) {
				return consultationDo.getId();
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
	public int updateConsultation(ConsultationDTO consultationDto) throws ServiceException {
		if (consultationDto == null) {
			ServiceException se = new ServiceException("consultationDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			ConsultationDO consultationDo = mapper.map(consultationDto, ConsultationDO.class);
			if (consultationDto.getState() != null)
				consultationDo.setState(consultationDto.getState().toString());
			return consultationDao.updateConsultation(consultationDo);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public List<ConsultationDTO> listConsultation() throws ServiceException {
		List<ConsultationDTO> consultationDtoList = new ArrayList<ConsultationDTO>();
		List<ConsultationDO> consultationDoList = new ArrayList<ConsultationDO>();
		try {
			consultationDoList = consultationDao.listConsultation();
			if (consultationDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (ConsultationDO consultationDo : consultationDoList) {
			ConsultationDTO consultationDto = mapper.map(consultationDo, ConsultationDTO.class);
			if (StringUtil.isNotEmpty(consultationDo.getState()))
				consultationDto.setState(AbleStateEnum.get(consultationDo.getState()));
			consultationDtoList.add(consultationDto);
		}
		return consultationDtoList;
	}

	@Override
	public List<ConsultationDTO> listConsultationByUserId(int userId, AbleStateEnum state) throws ServiceException {
		List<ConsultationDTO> consultationDtoList = new ArrayList<ConsultationDTO>();
		List<ConsultationDO> consultationDoList = new ArrayList<ConsultationDO>();
		try {
			consultationDoList = consultationDao.listConsultationByUserId(userId,
					state == null ? null : state.toString());
			if (consultationDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (ConsultationDO consultationDo : consultationDoList) {
			ConsultationDTO consultationDto = mapper.map(consultationDo, ConsultationDTO.class);
			if (StringUtil.isNotEmpty(consultationDo.getState()))
				consultationDto.setState(AbleStateEnum.get(consultationDo.getState()));
			consultationDtoList.add(consultationDto);
		}
		return consultationDtoList;
	}

	@Override
	public List<ConsultationDTO> listRemindByRemindDate(Date date, int adviserId) throws ServiceException {
		List<ConsultationDTO> consultationDtoList = new ArrayList<ConsultationDTO>();
		List<ConsultationDO> consultationDoList = new ArrayList<ConsultationDO>();
		try {
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			c.add(Calendar.DAY_OF_MONTH, 1);// 包含当天
			consultationDoList = consultationDao.listConsultationByRemindDate(c.getTime(),
					adviserId > 0 ? adviserId : null);
			if (consultationDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		consultationDoList.stream().filter(consultationDo -> consultationDo.getUserId() > 0).forEach(consultationDo -> {
			ConsultationDTO consultationDto = mapper.map(consultationDo, ConsultationDTO.class);
			if (consultationDto.getUserId() != null) {
				UserDO userDo = userDao.getUserById(consultationDto.getUserId());
				if (userDo != null)
					consultationDto.setUserName(userDo.getName());
				if (consultationDto.getRemindDate().getTime() <= 14400000)
					consultationDto.setRemindDate(null);
			}
			if (StringUtil.isNotEmpty(consultationDo.getState()))
				consultationDto.setState(AbleStateEnum.get(consultationDo.getState()));
			consultationDtoList.add(consultationDto);
		});
		return consultationDtoList;
	}

}
