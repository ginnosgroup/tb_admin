package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhinanzhen.b.dao.SourceDAO;
import org.zhinanzhen.b.dao.SourceRegionDAO;
import org.zhinanzhen.b.dao.pojo.SourceRegionDO;
import org.zhinanzhen.b.service.SourceRegionService;
import org.zhinanzhen.b.service.pojo.SourceRegionDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.thrift.ErrorCodeEnum;

@Service("SourceRegionService")
public class SourceRegionServiceImpl extends BaseService implements SourceRegionService {

	@Resource
	private SourceRegionDAO sourceRegionDao;

	@Resource
	private SourceDAO sourceDao;

	@Override
	public int addSourceRegion(SourceRegionDTO sourceRegionDto) throws ServiceException {
		if (sourceRegionDto == null) {
			ServiceException se = new ServiceException("sourceRegionDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			SourceRegionDO sourceRegionDo = mapper.map(sourceRegionDto, SourceRegionDO.class);
			if (sourceRegionDao.addSourceRegion(sourceRegionDo) > 0) {
				return sourceRegionDo.getId();
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
	public int updateSourceRegion(SourceRegionDTO sourceRegionDto) throws ServiceException {
		if (sourceRegionDto == null) {
			ServiceException se = new ServiceException("sourceRegionDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			SourceRegionDO sourceRegionDo = mapper.map(sourceRegionDto, SourceRegionDO.class);
			if (sourceRegionDao.updateSourceRegion(sourceRegionDo) > 0) {
				return sourceRegionDo.getId();
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
	public List<SourceRegionDTO> listSourceRegion(Integer parentId) throws ServiceException {
		List<SourceRegionDTO> sourceRegionDtoList = new ArrayList<SourceRegionDTO>();
		List<SourceRegionDO> sourceRegionDoList = new ArrayList<SourceRegionDO>();
		try {
			sourceRegionDoList = sourceRegionDao.listSourceRegion(parentId);
			if (sourceRegionDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (SourceRegionDO sourceRegionDo : sourceRegionDoList) {
			SourceRegionDTO sourceRegionDto = mapper.map(sourceRegionDo, SourceRegionDTO.class);
			sourceRegionDtoList.add(sourceRegionDto);
		}
		return sourceRegionDtoList;
	}

	@Override
	@Transactional
	public int deleteSourceRegion(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			sourceDao.listSource(id).forEach(source -> sourceDao.deleteSource(source.getId()));
			return sourceRegionDao.deleteSourceRegion(id);
		} catch (Exception e) {
			rollback();
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

}
