package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.SubagencyDAO;
import org.zhinanzhen.b.dao.pojo.SubagencyDO;
import org.zhinanzhen.b.service.SubagencyService;
import org.zhinanzhen.b.service.pojo.SubagencyDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.thrift.ErrorCodeEnum;

@Service("SubagencyService")
public class SubagencyServiceImpl extends BaseService implements SubagencyService {

	@Resource
	private SubagencyDAO subagencyDao;

	@Override
	public int addSubagency(SubagencyDTO subagencyDto) throws ServiceException {
		if (subagencyDto == null) {
			ServiceException se = new ServiceException("subagencyDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			SubagencyDO subagencyDo = mapper.map(subagencyDto, SubagencyDO.class);
			if (subagencyDao.addSubagency(subagencyDo) > 0) {
				return subagencyDo.getId();
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
	public int updateSubagency(int id, String name, double commissionRate) throws ServiceException {
		try {
			return subagencyDao.updateSubagency(id, name, commissionRate);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public List<SubagencyDTO> listSubagency(String keyword) throws ServiceException {
		List<SubagencyDTO> subagencyDtoList = new ArrayList<SubagencyDTO>();
		List<SubagencyDO> subagencyDoList = new ArrayList<SubagencyDO>();
		try {
			subagencyDoList = subagencyDao.listSubagency(keyword);
			if (subagencyDoList == null) {
				return null;
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (SubagencyDO subagencyDo : subagencyDoList) {
			SubagencyDTO subagencyDto = mapper.map(subagencyDo, SubagencyDTO.class);
			subagencyDtoList.add(subagencyDto);
		}
		return subagencyDtoList;
	}

	@Override
	public SubagencyDTO getSubagencyById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		SubagencyDTO subagencyDto = null;
		try {
			SubagencyDO subagencyDo = subagencyDao.getSubagencyById(id);
			if (subagencyDo == null) {
				return null;
			}
			subagencyDto = mapper.map(subagencyDo, SubagencyDTO.class);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		return subagencyDto;
	}

	@Override
	public int deleteSubagencyById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			return subagencyDao.deleteSubagencyById(id);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

}
