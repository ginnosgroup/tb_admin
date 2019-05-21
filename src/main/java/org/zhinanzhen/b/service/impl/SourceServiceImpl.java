package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.SourceDAO;
import org.zhinanzhen.b.dao.pojo.SourceDO;
import org.zhinanzhen.b.service.pojo.SourceDTO;
import org.zhinanzhen.b.service.SourceService;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.thrift.ErrorCodeEnum;

@Service("SourceService")
public class SourceServiceImpl extends BaseService implements SourceService {

	@Resource
	private SourceDAO sourceDao;

	@Override
	public int addSource(SourceDTO sourceDto) throws ServiceException {
		if (sourceDto == null) {
			ServiceException se = new ServiceException("sourceDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			SourceDO sourceDo = mapper.map(sourceDto, SourceDO.class);
			if (sourceDao.addSource(sourceDo) > 0) {
				return sourceDo.getId();
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
	public int updateSource(SourceDTO sourceDto) throws ServiceException {
		if (sourceDto == null) {
			ServiceException se = new ServiceException("sourceDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			SourceDO sourceDo = mapper.map(sourceDto, SourceDO.class);
			if (sourceDao.updateSource(sourceDo) > 0) {
				return sourceDo.getId();
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
	public List<SourceDTO> listSource(int sourceRegionId) throws ServiceException {
		List<SourceDTO> sourceDtoList = new ArrayList<SourceDTO>();
		List<SourceDO> sourceDoList = new ArrayList<SourceDO>();
		try {
			sourceDoList = sourceDao.listSource(sourceRegionId);
			if (sourceDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (SourceDO sourceDo : sourceDoList) {
			SourceDTO sourceDto = mapper.map(sourceDo, SourceDTO.class);
			sourceDtoList.add(sourceDto);
		}
		return sourceDtoList;
	}

	@Override
	public int deleteSource(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			return sourceDao.deleteSource(id);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

}
