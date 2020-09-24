package org.zhinanzhen.tb.service.impl;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.zhinanzhen.tb.dao.RegionDAO;
import org.zhinanzhen.tb.dao.pojo.RegionDO;
import org.zhinanzhen.tb.service.RegionService;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.pojo.RegionDTO;

import com.ikasoa.core.ErrorCodeEnum;
import com.ikasoa.core.utils.StringUtil;

@Service("regionService")
public class RegionServiceImpl extends BaseService implements RegionService {

	@Resource
	private RegionDAO regionDao;

	@Override
	public List<RegionDTO> listAllRegion() throws ServiceException {
		List<RegionDO> regionDoList = regionDao.selectByParent();
		List<RegionDTO> regionDtoList = new ArrayList<RegionDTO>();
		for (RegionDO regionDo : regionDoList) {
			if (regionDo == null) {
				continue;
			}
			RegionDTO regionDto = mapper.map(regionDo, RegionDTO.class);
			int parentId = regionDto.getId();
			List<RegionDO> childRegionList = regionDao.selectByParentId(parentId);
			regionDto.getRegionList().addAll(childRegionList);
			regionDtoList.add(regionDto);
		}
		return regionDtoList;
	}
	
	@Override
	public List<RegionDTO> listRegion(Integer parentId) throws ServiceException {
		List<RegionDO> regionDoList = regionDao.selectByParentId(parentId);
		List<RegionDTO> regionDtoList = new ArrayList<RegionDTO>();
		for (RegionDO regionDo : regionDoList) {
			if (regionDo == null)
				continue;
			regionDtoList.add(mapper.map(regionDo, RegionDTO.class));
		}
		return regionDtoList;
	}

	@Override
	public int addRegion(String name, int weight) throws ServiceException {
		if (StringUtil.isEmpty(name)) {
			ServiceException se = new ServiceException("name is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (weight < 0) {
			ServiceException se = new ServiceException("weight < 0 ! weight = !" + weight);
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		List<RegionDO> regionDoList = regionDao.listAllRegion();
		for (RegionDO regionDo : regionDoList) {
			if (name.equals(regionDo.getName())) {
				ServiceException se = new ServiceException("name is repeat !");
				se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
				throw se;
			}
		}
		RegionDO regionDo = new RegionDO();
		regionDo.setName(name);
		regionDo.setWeight(weight);
		regionDao.insert(regionDo);
		return regionDo.getId();
	}

	@Override
	public int addCity(int parentId, String name, int weight) throws ServiceException {
		if (StringUtil.isEmpty(name)) {
			ServiceException se = new ServiceException("name is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (weight < 0) {
			ServiceException se = new ServiceException("weight < 0 ! weight = !" + weight);
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		List<RegionDO> regionDoList = regionDao.listAllRegion();
		for (RegionDO regionDo : regionDoList) {
			if (name.equals(regionDo.getName())) {
				ServiceException se = new ServiceException("name is repeat ! name =" + name);
				se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
				throw se;
			}
		}
		RegionDO parent = regionDao.getRegionById(parentId);
		if (parent == null) {
			ServiceException se = new ServiceException("parent region not found! paretId = " + parentId);
			se.setCode(ErrorCodeEnum.DATA_ERROR.code());
			throw se;
		}
		RegionDO regionDo = new RegionDO();
		regionDo.setName(name);
		regionDo.setParentId(parentId);
		regionDo.setWeight(weight);
		regionDao.insert(regionDo);
		return regionDo.getId();
	}

	@Override
	public boolean updateRegion(int id, String name, int weight) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id <=0 id = " + id);
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (StringUtil.isEmpty(name)) {
			ServiceException se = new ServiceException("name is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		RegionDO regionDo = regionDao.getRegionById(id);
		if (regionDo == null) {
			ServiceException se = new ServiceException("this region not found!regionId = " + id);
			se.setCode(ErrorCodeEnum.DATA_ERROR.code());
			throw se;
		}
		if (name.equals(regionDo.getName()) && regionDo.getWeight() == weight) {
			return true;
		}
		if (name.equals(regionDo.getName())) {
			return regionDao.update(name, id, weight);
		}
		List<RegionDO> regionDoList = regionDao.listAllRegion();
		for (RegionDO region : regionDoList) {
			if (name.equals(region.getName())) {
				ServiceException se = new ServiceException("name is repeat ! name =" + name);
				se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
				throw se;
			}
		}
		return regionDao.update(name, id, weight);
	}
}
