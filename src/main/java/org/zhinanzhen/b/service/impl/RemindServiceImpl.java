package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.RemindDAO;
import org.zhinanzhen.b.dao.pojo.RemindDO;
import org.zhinanzhen.b.service.AbleStateEnum;
import org.zhinanzhen.b.service.RemindService;
import org.zhinanzhen.b.service.pojo.RemindDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.ErrorCodeEnum;

@Service("RemindService")
public class RemindServiceImpl extends BaseService implements RemindService {

	@Resource
	private RemindDAO remindDao;

	@Override
	public int addRemind(RemindDTO remindDto) throws ServiceException {
		if (remindDto == null) {
			ServiceException se = new ServiceException("remindDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			List<RemindDO> remindDoList = remindDao.listRemindBySchoolBrokerageSaId(remindDto.getSchoolBrokerageSaId(),
					null, null);
			for (RemindDO remindDo : remindDoList) {
				if (remindDo.getRemindDate().getTime() == remindDto.getRemindDate().getTime()) {
					ServiceException se = new ServiceException("该提醒日期已存在.");
					se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
					throw se;
				}
			}
			RemindDO remindDo = mapper.map(remindDto, RemindDO.class);
			if (remindDao.addRemind(remindDo) > 0) {
				remindDto.setId(remindDo.getId());
				return remindDo.getId();
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
	public List<RemindDTO> listRemindBySchoolBrokerageSaId(int schoolBrokerageSaId, int adviserId, AbleStateEnum state)
			throws ServiceException {
		List<RemindDTO> remindDtoList = new ArrayList<RemindDTO>();
		List<RemindDO> remindDoList = new ArrayList<RemindDO>();
		try {
			remindDoList = remindDao.listRemindBySchoolBrokerageSaId(schoolBrokerageSaId,
					adviserId > 0 ? adviserId : null, state != null ? state.toString() : null);
			if (remindDoList == null) {
				return null;
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (RemindDO remindDo : remindDoList) {
			RemindDTO remindDto = mapper.map(remindDo, RemindDTO.class);
			remindDtoList.add(remindDto);
		}
		return remindDtoList;
	}

	@Override
	public List<RemindDTO> listRemindByRemindDate(Date date, int adviserId) throws ServiceException {
		List<RemindDTO> remindDtoList = new ArrayList<RemindDTO>();
//		List<RemindDO> remindDoList = new ArrayList<RemindDO>();
//		try {
//			Calendar c = Calendar.getInstance();
//			c.setTime(date);
//			c.set(Calendar.HOUR_OF_DAY, 23);
//			c.set(Calendar.MINUTE, 59);
//			c.set(Calendar.SECOND, 59);
//			// c.add(Calendar.DAY_OF_MONTH, 1);// 包含当天
//			remindDoList = remindDao.listRemindByRemindDate(c.getTime());
//			if (remindDoList == null) {
//				return null;
//			}
//		} catch (Exception e) {
//			ServiceException se = new ServiceException(e);
//			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
//			throw se;
//		}
//		remindDoList.stream().filter(remindDo -> remindDo.getSchoolBrokerageSaId() > 0).forEach(remindDo -> {
//			RemindDTO remindDto = mapper.map(remindDo, RemindDTO.class);
//			SchoolBrokerageSaDO schoolBrokerageSaDo = schoolBrokerageSaDao
//					.getSchoolBrokerageSaById(remindDto.getSchoolBrokerageSaId());
//			if (schoolBrokerageSaDo != null && schoolBrokerageSaDo.getAdviserId() == adviserId)
//				remindDtoList.add(remindDto);
//		});
		return remindDtoList;
	}

	@Override
	public int updateStateById(int id, AbleStateEnum state) throws ServiceException {
		try {
			return remindDao.updateStateById(id, state != null ? state.toString() : null);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int deleteRemindById(int id) throws ServiceException {
		return remindDao.deleteRemindById(id);
	}

	@Override
	public int deleteRemindBySchoolBrokerageSaId(int id) throws ServiceException {
		return remindDao.deleteRemindBySchoolBrokerageSaId(id);
	}

}
