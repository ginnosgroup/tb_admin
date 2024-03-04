package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ikasoa.core.utils.ObjectUtil;
import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.*;
import org.zhinanzhen.b.dao.pojo.ServicePackagePriceDO;
import org.zhinanzhen.b.service.ServicePackagePriceService;
import org.zhinanzhen.b.service.pojo.ServicePackagePriceDTO;
import org.zhinanzhen.b.service.pojo.ServicePackagePriceV2DTO;
import org.zhinanzhen.tb.dao.RegionDAO;
import org.zhinanzhen.tb.dao.pojo.RegionDO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.ErrorCodeEnum;

@Service("ServicePackagePriceService")
public class ServicePackagePriceServiceImpl extends BaseService implements ServicePackagePriceService {

	@Resource
	private ServicePackagePriceDAO servicePackagePriceDao;

	@Resource
	private OfficialGradeDao officialGradeDao;

	@Resource
	private ServiceDAO serviceDAO;

	@Resource
	private ServiceOrderDAO serviceOrderDAO;

	@Resource
	private OfficialDAO officialDAO;

	@Resource
	private ServiceOrderOfficialRemarksDAO serviceOrderOfficialRemarksDAO;

	@Resource
	private RegionDAO regionDAO;

	@Override
	public int addServicePackagePrice(ServicePackagePriceDTO servicePackagePriceDto) throws ServiceException {
		if (servicePackagePriceDto == null) {
			ServiceException se = new ServiceException("servicePackagePriceDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			ServicePackagePriceDO servicePackagePriceDo = mapper.map(servicePackagePriceDto,
					ServicePackagePriceDO.class);
			if (servicePackagePriceDao.add(servicePackagePriceDo) > 0) {
				servicePackagePriceDto.setId(servicePackagePriceDo.getId());
				return servicePackagePriceDo.getId();
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
	public int updateServicePackagePrice(ServicePackagePriceDTO servicePackagePriceDto) throws ServiceException {
		if (servicePackagePriceDto == null) {
			ServiceException se = new ServiceException("servicePackagePriceDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (servicePackagePriceDto.getId() <= 0) {
			ServiceException se = new ServiceException("id is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		ServicePackagePriceDO byId = servicePackagePriceDao.getById(servicePackagePriceDto.getId());
		String rulerV2Lib = byId.getRulerV2();
		String rulerV2Add = servicePackagePriceDto.getRulerV2();
		List<ServicePackagePriceV2DTO> servicePackagePriceV2DTOSLib = JSONArray.parseArray(rulerV2Lib, ServicePackagePriceV2DTO.class);
		List<ServicePackagePriceV2DTO> servicePackagePriceV2DTOSAdd = JSONArray.parseArray(rulerV2Add, ServicePackagePriceV2DTO.class);
		Map<Integer, ServicePackagePriceV2DTO> collect = servicePackagePriceV2DTOSAdd.stream().collect(Collectors.toMap(ServicePackagePriceV2DTO::getAreaId, e -> e));
		List<ServicePackagePriceV2DTO> objects = new ArrayList<>();
		for (ServicePackagePriceV2DTO a : servicePackagePriceV2DTOSLib) {
			if (ObjectUtil.isNotNull(collect.get(a.getAreaId()))) {
				return -1;
			}
			objects.add(a);
		}
		objects.addAll(servicePackagePriceV2DTOSAdd);
		servicePackagePriceDto.setRulerV2(JSONObject.toJSONString(objects));

		return servicePackagePriceDao.update(servicePackagePriceDto.getId(), servicePackagePriceDto.getMinPrice(),
				servicePackagePriceDto.getMaxPrice(), servicePackagePriceDto.getServiceId(),
				servicePackagePriceDto.getRegionId(),servicePackagePriceDto.getCostPrince(),servicePackagePriceDto.getThirdPrince(),
				servicePackagePriceDto.getRuler(),servicePackagePriceDto.getAmount(), servicePackagePriceDto.getRulerV2()) > 0
						? servicePackagePriceDto.getId(): 0;}

	@Override
	public int countServicePackagePrice(int serviceId, int regionId) throws ServiceException {
		return servicePackagePriceDao.count(serviceId, regionId);
	}

//	@Override
//	public List<ServicePackagePriceDTO> listServicePackagePrice(int serviceId, int regionId,
//			int pageNum, int pageSize) throws ServiceException {
//		List<ServicePackagePriceDTO> servicePackagePriceDtoList = new ArrayList<>();
//		List<ServicePackagePriceDO> servicePackagePriceDoList = new ArrayList<>();
//		try {
//			servicePackagePriceDoList = servicePackagePriceDao.list(serviceId, regionId,
//					pageNum * pageSize, pageSize);
//			if (servicePackagePriceDoList == null)
//				return null;
//		} catch (Exception e) {
//			ServiceException se = new ServiceException(e);
//			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
//			throw se;
//		}
//		for (ServicePackagePriceDO servicePackagePriceDo : servicePackagePriceDoList)
//			servicePackagePriceDtoList.add(mapper.map(servicePackagePriceDo, ServicePackagePriceDTO.class));
//		return servicePackagePriceDtoList;
//	}
	@Override
	public List<ServicePackagePriceDTO> listServicePackagePrice(int serviceId, int regionId,
																int pageNum, int pageSize) throws ServiceException {
		List<ServicePackagePriceDTO> servicePackagePriceDtoList = new ArrayList<>();
		List<ServicePackagePriceDO> servicePackagePriceDoList = new ArrayList<>();
		try {
			servicePackagePriceDoList = servicePackagePriceDao.listDt(serviceId, regionId,
					pageNum * pageSize, pageSize);
			if (servicePackagePriceDoList == null)
				return null;
			servicePackagePriceDoList.forEach(e->{
				String rulerV2 = e.getRulerV2();
				List<ServicePackagePriceV2DTO> servicePackagePriceV2DTOS = JSONArray.parseArray(rulerV2, ServicePackagePriceV2DTO.class);
				servicePackagePriceV2DTOS.forEach(a->{
					RegionDO regionById = regionDAO.getRegionById(a.getAreaId());
					a.setParentId(regionById.getParentId());
				});
				ServicePackagePriceDTO servicePackagePriceDTO = mapper.map(e, ServicePackagePriceDTO.class);
				servicePackagePriceDTO.setServicePackagePriceV2DTO(servicePackagePriceV2DTOS);
				servicePackagePriceDtoList.add(servicePackagePriceDTO);
			});
//			String rulerV2 = servicePackagePriceDoList.getRulerV2();
//			List<ServicePackagePriceV2DTO> servicePackagePriceV2DTOS = JSONArray.parseArray(rulerV2, ServicePackagePriceV2DTO.class);
//			servicePackagePriceDtoList = mapper.map(servicePackagePriceDoList, ServicePackagePriceDTO.class);
//			servicePackagePriceDtoList.setServicePackagePriceV2DTO(servicePackagePriceV2DTOS);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		return servicePackagePriceDtoList;
	}

	@Override
	public ServicePackagePriceDTO getServicePackagePriceById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		ServicePackagePriceDTO servicePackagePriceDto = null;
		try {
			ServicePackagePriceDO servicePackagePriceDo = servicePackagePriceDao.getById(id);
			if (servicePackagePriceDo == null)
				return null;
			servicePackagePriceDto = mapper.map(servicePackagePriceDo, ServicePackagePriceDTO.class);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		return servicePackagePriceDto;
	}

	@Override
	public ServicePackagePriceDO getServicePackagePriceByServiceId(int id)  {
		return servicePackagePriceDao.getByServiceId(id);
	}

	@Override
	public int deleteById(Integer serviceId, ServicePackagePriceV2DTO servicePackagePriceV2DTO) throws ServiceException {
		if (serviceId <= 0) {
			ServiceException se = new ServiceException("serviceId error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		ServicePackagePriceDO byServiceId = servicePackagePriceDao.getByServiceId(serviceId);
		if (ObjectUtil.isNotNull(servicePackagePriceV2DTO)) {
			String rulerV2 = byServiceId.getRulerV2();
			List<ServicePackagePriceV2DTO> servicePackagePriceV2DTOS = JSONArray.parseArray(rulerV2, ServicePackagePriceV2DTO.class);
			Map<String, List<String>> collect = servicePackagePriceV2DTOS.stream().
					collect(Collectors.groupingBy(ServicePackagePriceV2DTO::getCountry,
							Collectors.mapping(ServicePackagePriceV2DTO::getCountry, Collectors.toList())));
			if (collect.get(servicePackagePriceV2DTO.getCountry()).size() == 1) {
				throw new ServiceException("当前国家只有一条规则，不允许删除");
			}
			List<ServicePackagePriceV2DTO> servicePackagePriceV2DTONew = new ArrayList<>();
			servicePackagePriceV2DTOS.forEach(e->{
				if (!e.getAreaId().equals(servicePackagePriceV2DTO.getAreaId())) {
					servicePackagePriceV2DTONew.add(e);
				}
			});
			String jsonString = JSONArray.toJSONString(servicePackagePriceV2DTONew);
			byServiceId.setRulerV2(jsonString);
			return servicePackagePriceDao.update(byServiceId.getId(), byServiceId.getMinPrice(), byServiceId.getMaxPrice(),
					byServiceId.getServiceId(), byServiceId.getRegionId(), byServiceId.getCostPrince(), byServiceId.getThirdPrince(),
					byServiceId.getRuler(), byServiceId.getAmount(), byServiceId.getRulerV2());
		}
		return servicePackagePriceDao.delete(serviceId);

	}

}
