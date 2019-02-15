package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.RemindDAO;
import org.zhinanzhen.b.dao.pojo.RemindDO;
import org.zhinanzhen.b.service.BrokerageSaRemindService;
import org.zhinanzhen.b.service.pojo.BrokerageSaRemindDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.thrift.ErrorCodeEnum;

@Service("BrokerageSaRemindService")
public class BrokerageSaRemindServiceImpl extends BaseService implements BrokerageSaRemindService {

	@Resource
	private RemindDAO remindDao;

	@Override
	public int addRemind(BrokerageSaRemindDTO brokerageSaRemindDto) throws ServiceException {
		if (brokerageSaRemindDto == null) {
			ServiceException se = new ServiceException("brokerageSaRemindDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			List<RemindDO> remindDoList = remindDao.listRemindByBrokerageSaId(brokerageSaRemindDto.getBrokerageSaId());
			for (RemindDO remindDo : remindDoList) {
				if (remindDo.getRemindDate().getTime() == brokerageSaRemindDto.getRemindDate().getTime()) {
					ServiceException se = new ServiceException("该提醒日期已存在.");
					se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
					throw se;
				}
			}
			RemindDO remindDo = mapper.map(brokerageSaRemindDto, RemindDO.class);
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
	public List<BrokerageSaRemindDTO> listRemindByBrokerageSaId(int brokerageSaId) throws ServiceException {
		List<BrokerageSaRemindDTO> brokerageSaRemindDtoList = new ArrayList<BrokerageSaRemindDTO>();
		List<RemindDO> remindDoList = new ArrayList<RemindDO>();
		try {
			remindDoList = remindDao.listRemindByBrokerageSaId(brokerageSaId);
			if (remindDoList == null) {
				return null;
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (RemindDO remindDo : remindDoList) {
			BrokerageSaRemindDTO brokerageSaRemindDto = mapper.map(remindDo, BrokerageSaRemindDTO.class);
			brokerageSaRemindDtoList.add(brokerageSaRemindDto);
		}
		return brokerageSaRemindDtoList;
	}

	@Override
	public List<BrokerageSaRemindDTO> listRemindByRemindDate(Date date) throws ServiceException {
		List<BrokerageSaRemindDTO> brokerageSaRemindDtoList = new ArrayList<BrokerageSaRemindDTO>();
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
		remindDoList.stream().filter(remindDo -> remindDo.getBrokerageSaId() > 0)
				.forEach(remindDo -> brokerageSaRemindDtoList.add(mapper.map(remindDo, BrokerageSaRemindDTO.class)));
		return brokerageSaRemindDtoList;
	}

	@Override
	public int deleteRemindByBrokerageSaId(int id) throws ServiceException {
		return remindDao.deleteRemindByBrokerageSaId(id);
	}

}
