package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.ReceiveTypeDAO;
import org.zhinanzhen.b.dao.pojo.ReceiveTypeDO;
import org.zhinanzhen.b.service.ReceiveTypeService;
import org.zhinanzhen.b.service.ReceiveTypeStateEnum;
import org.zhinanzhen.b.service.pojo.ReceiveTypeDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.thrift.ErrorCodeEnum;
import com.ikasoa.core.utils.StringUtil;

@Service("ReceiveTypeService")
public class ReceiveTypeServiceImpl extends BaseService implements ReceiveTypeService {

	@Resource
	private ReceiveTypeDAO ReceiveTypeDAO;

	@Override
	public int addReceiveType(ReceiveTypeDTO receiveTypeDto) throws ServiceException {
		if (receiveTypeDto == null) {
			ServiceException se = new ServiceException("receiveTypeDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			ReceiveTypeDO receiveTypeDo = mapper.map(receiveTypeDto, ReceiveTypeDO.class);
			if (receiveTypeDto.getState() != null) {
				receiveTypeDo.setState(receiveTypeDto.getState().toString());
			}
			if (ReceiveTypeDAO.addReceiveType(receiveTypeDo) > 0) {
				return receiveTypeDo.getId();
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
	public int updateReceiveType(ReceiveTypeDTO receiveTypeDto) throws ServiceException {
		if (receiveTypeDto == null) {
			ServiceException se = new ServiceException("receiveTypeDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			ReceiveTypeDO receiveTypeDo = mapper.map(receiveTypeDto, ReceiveTypeDO.class);
			if (receiveTypeDto.getState() != null) {
				receiveTypeDo.setState(receiveTypeDto.getState().toString());
			}
			return ReceiveTypeDAO.updateReceiveType(receiveTypeDo);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public List<ReceiveTypeDTO> listReceiveType(ReceiveTypeStateEnum state) throws ServiceException {

		List<ReceiveTypeDTO> receiveTypeDtoList = new ArrayList<ReceiveTypeDTO>();
		List<ReceiveTypeDO> receiveTypeDoList = new ArrayList<ReceiveTypeDO>();
		try {
			if (state == null) {
				receiveTypeDoList = ReceiveTypeDAO.listReceiveType(null);
			} else {
				receiveTypeDoList = ReceiveTypeDAO.listReceiveType(state.toString());
			}
			if (receiveTypeDoList == null) {
				return null;
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (ReceiveTypeDO receiveTypeDo : receiveTypeDoList) {
			ReceiveTypeDTO receiveTypeDto = mapper.map(receiveTypeDo, ReceiveTypeDTO.class);
			if (StringUtil.isNotEmpty(receiveTypeDo.getState())) {
				receiveTypeDto.setState(ReceiveTypeStateEnum.get(receiveTypeDo.getState()));
			}
			receiveTypeDtoList.add(receiveTypeDto);
		}
		return receiveTypeDtoList;
	}

}
