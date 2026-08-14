package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.PortalLogDAO;
import org.zhinanzhen.b.dao.pojo.PortalLogDO;
import org.zhinanzhen.b.service.PortalLogService;
import org.zhinanzhen.b.service.pojo.PortalLogDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.ErrorCodeEnum;

@Service("PortalLogService")
public class PortalLogServiceImpl extends BaseService implements PortalLogService {

	@Resource
	private PortalLogDAO portalLogDao;

	@Override
	public int addPortalLog(PortalLogDTO portalLogDto) throws ServiceException {
		if (portalLogDto == null) {
			ServiceException se = new ServiceException("portalLogDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			PortalLogDO portalLogDo = mapper.map(portalLogDto, PortalLogDO.class);
			if (portalLogDao.addPortalLog(portalLogDo) > 0) {
				portalLogDto.setId(portalLogDo.getId());
				return portalLogDo.getId();
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
	public List<PortalLogDTO> listPortalLog(Integer portalId, int pageNum, int pageSize) throws ServiceException {
		List<PortalLogDTO> portalLogDtoList = new ArrayList<PortalLogDTO>();
		List<PortalLogDO> portalLogDoList = new ArrayList<PortalLogDO>();
		try {
			portalLogDoList = portalLogDao.listPortalLog(portalId, pageNum * pageSize, pageSize);
			if (portalLogDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (PortalLogDO portalLogDo : portalLogDoList) {
			PortalLogDTO portalLogDto = mapper.map(portalLogDo, PortalLogDTO.class);
			portalLogDtoList.add(portalLogDto);
		}
		return portalLogDtoList;
	}

	@Override
	public int countPortalLog(Integer portalId) throws ServiceException {
		try {
			return portalLogDao.countPortalLog(portalId);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
	}

}
