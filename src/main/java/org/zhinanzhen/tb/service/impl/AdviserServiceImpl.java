package org.zhinanzhen.tb.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.tb.dao.AdviserDAO;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
import org.zhinanzhen.tb.service.AdviserService;
import org.zhinanzhen.tb.service.AdviserStateEnum;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.pojo.AdviserDTO;
import com.ikasoa.core.thrift.ErrorCodeEnum;
import com.ikasoa.core.utils.StringUtil;
@Service("adviserService")
public class AdviserServiceImpl extends BaseService implements AdviserService {
	@Resource
	private AdviserDAO adviserDAO;
	@Override
	public int addAdviser(AdviserDTO adviserDto) throws ServiceException {
		if (adviserDto == null) {
			ServiceException se = new ServiceException("adviserDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			AdviserDO adviserDo = mapper.map(adviserDto, AdviserDO.class);
			if (adviserDto.getState() != null) {
				adviserDo.setState(adviserDto.getState().toString());
			}
			if (adviserDAO.addAdviser(adviserDo) > 0) {
				return adviserDo.getId();
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
	public int updateAdviser(AdviserDTO adviserDto) throws ServiceException {
		if (adviserDto == null) {
			ServiceException se = new ServiceException("adviserDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			AdviserDO adviserDo = mapper.map(adviserDto, AdviserDO.class);
			if (adviserDto.getState() != null) {
				adviserDo.setState(adviserDto.getState().toString());
			}
			return adviserDAO.updateAdviser(adviserDo);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int countAdviser(String name, Integer regionId) throws ServiceException {
		return adviserDAO.countAdviser(name, regionId);
	}

	@Override
	public List<AdviserDTO> listAdviser(String name, Integer regionId, int pageNum, int pageSize)
			throws ServiceException {
		if (pageNum < 0) {
			pageNum = DEFAULT_PAGE_NUM;
		}
		if (pageSize < 0) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		List<AdviserDTO> adviserDtoList = new ArrayList<AdviserDTO>();
		List<AdviserDO> adviserDoList = new ArrayList<AdviserDO>();
		try {
			adviserDoList = adviserDAO.listAdviser(name, regionId, pageNum * pageSize, pageSize);
			if (adviserDoList == null) {
				return null;
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (AdviserDO adviserDo : adviserDoList) {
			AdviserDTO adviserDto = mapper.map(adviserDo, AdviserDTO.class);
			if (StringUtil.isNotEmpty(adviserDo.getState())) {
				adviserDto.setState(AdviserStateEnum.get(adviserDo.getState()));
			}
			adviserDtoList.add(adviserDto);
		}
		return adviserDtoList;
	}

	@Override
	public AdviserDTO getAdviserById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		AdviserDTO adviserDto = null;
		try {
			AdviserDO adviserDo = adviserDAO.getAdviserById(id);
			if (adviserDo == null) {
//				ServiceException se = new ServiceException("the adviser is't exist .");
//				se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
//				throw se;
				return null;
			}
			adviserDto = mapper.map(adviserDo, AdviserDTO.class);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		return adviserDto;
	}
}