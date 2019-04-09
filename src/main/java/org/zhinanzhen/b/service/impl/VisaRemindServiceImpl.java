package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.RemindDAO;
import org.zhinanzhen.b.dao.VisaDAO;
import org.zhinanzhen.b.dao.pojo.RemindDO;
import org.zhinanzhen.b.dao.pojo.VisaDO;
import org.zhinanzhen.b.service.VisaRemindService;
import org.zhinanzhen.b.service.pojo.VisaRemindDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.thrift.ErrorCodeEnum;

@Service("VisaRemindService")
public class VisaRemindServiceImpl extends BaseService implements VisaRemindService {

	@Resource
	private RemindDAO remindDao;

	@Resource
	private VisaDAO visaDao;

	@Override
	public int addRemind(VisaRemindDTO visaRemindDto) throws ServiceException {
		if (visaRemindDto == null) {
			ServiceException se = new ServiceException("visaRemindDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			List<RemindDO> remindDoList = remindDao.listRemindByVisaId(visaRemindDto.getVisaId(), null);
			for (RemindDO remindDo : remindDoList) {
				if (remindDo.getRemindDate().getTime() == visaRemindDto.getRemindDate().getTime()) {
					ServiceException se = new ServiceException("该提醒日期已存在.");
					se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
					throw se;
				}
			}
			RemindDO remindDo = mapper.map(visaRemindDto, RemindDO.class);
			if (remindDao.addRemind(remindDo) > 0) {
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
	public List<VisaRemindDTO> listRemindByVisaId(int visaId, int adviserId) throws ServiceException {
		List<VisaRemindDTO> visaRemindDtoList = new ArrayList<VisaRemindDTO>();
		List<RemindDO> remindDoList = new ArrayList<RemindDO>();
		try {
			remindDoList = remindDao.listRemindByVisaId(visaId, adviserId > 0 ? adviserId : null);
			if (remindDoList == null) {
				return null;
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (RemindDO remindDo : remindDoList) {
			VisaRemindDTO visaRemindDto = mapper.map(remindDo, VisaRemindDTO.class);

			visaRemindDtoList.add(visaRemindDto);
		}
		return visaRemindDtoList;
	}

	@Override
	public List<VisaRemindDTO> listRemindByRemindDate(Date date, int adviserId) throws ServiceException {
		List<VisaRemindDTO> visaRemindDtoList = new ArrayList<VisaRemindDTO>();
		List<RemindDO> remindDoList = new ArrayList<RemindDO>();
		try {
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			c.add(Calendar.DAY_OF_MONTH, 1);// 包含当天
			remindDoList = remindDao.listRemindByRemindDate(c.getTime());
			if (remindDoList == null) {
				return null;
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		remindDoList.stream().filter(remindDo -> remindDo.getVisaId() > 0).forEach(remindDo -> {
			VisaRemindDTO visaRemindDto = mapper.map(remindDo, VisaRemindDTO.class);
			VisaDO visaDo = visaDao.getVisaById(visaRemindDto.getVisaId());
			if (visaDo != null && visaDo.getAdviserId() == adviserId)
				visaRemindDtoList.add(visaRemindDto);
		});
		return visaRemindDtoList;
	}

	@Override
	public int deleteRemindByVisaId(int id) throws ServiceException {
		return remindDao.deleteRemindByVisaId(id);
	}

}
