package org.zhinanzhen.b.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.RemindDAO;
import org.zhinanzhen.b.dao.pojo.RemindDO;
import org.zhinanzhen.b.service.RemindService;
import org.zhinanzhen.b.service.pojo.RemindDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.thrift.ErrorCodeEnum;

@Service("OfficialService")
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
			RemindDO remindDo = mapper.map(remindDto, RemindDO.class);
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
	public List<RemindDTO> listRemindBySchoolBrokerageSaId(int schoolBrokerageSaId) throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<RemindDTO> listRemindByRemindDate(Date date) throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int deleteSchoolById(int id) throws ServiceException {
		return remindDao.deleteSchoolById(id);
	}

}
