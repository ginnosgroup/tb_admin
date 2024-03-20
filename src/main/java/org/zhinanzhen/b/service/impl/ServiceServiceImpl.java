package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ikasoa.core.utils.ObjectUtil;
import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.ServiceDAO;
import org.zhinanzhen.b.dao.ServicePackagePriceDAO;
import org.zhinanzhen.b.dao.pojo.ServiceDO;
import org.zhinanzhen.b.dao.pojo.ServicePackagePriceDO;
import org.zhinanzhen.b.service.ServiceService;
import org.zhinanzhen.b.service.pojo.ServiceDTO;
import org.zhinanzhen.b.service.pojo.ServicePackagePriceDTO;
import org.zhinanzhen.b.service.pojo.ServicePackagePriceV2DTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.ErrorCodeEnum;

@Service("ServiceService")
public class ServiceServiceImpl extends BaseService implements ServiceService {

	@Resource
	private ServiceDAO serviceDao;
	
	@Resource
	private ServicePackagePriceDAO servicePackagePriceDao;

	@Override
	public int addService(ServiceDTO serviceDto) throws ServiceException {
		if (serviceDto == null) {
			ServiceException se = new ServiceException("serviceDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			ServiceDO serviceDo = mapper.map(serviceDto, ServiceDO.class);
			if (serviceDao.addService(serviceDo) > 0) {
				serviceDto.setId(serviceDo.getId());
				return serviceDo.getId();
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
	public int updateService(int id, String name, String code, String role) throws ServiceException {
		try {
			return serviceDao.updateService(id, name, code, role);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int countService(String name, boolean isZx) throws ServiceException {
		return serviceDao.countService(name, isZx);
	}

	@Override
	public List<ServiceDTO> listService(String name, boolean isZx, int pageNum, int pageSize) throws ServiceException {
		if (pageNum < 0)
			pageNum = DEFAULT_PAGE_NUM;
		if (pageSize < 0)
			pageSize = DEFAULT_PAGE_SIZE;
		List<ServiceDTO> serviceDtoList = new ArrayList<ServiceDTO>();
		List<ServiceDO> serviceDoList = new ArrayList<ServiceDO>();
		try {
			serviceDoList = serviceDao.listService(name, isZx, pageNum * pageSize, pageSize);
			if (serviceDoList == null)
				return null;
			for (ServiceDO serviceDo : serviceDoList) {
				ServiceDTO serviceDto = mapper.map(serviceDo, ServiceDTO.class);
				List<ServicePackagePriceDO> servicePackagePriceDoList = servicePackagePriceDao.list(serviceDto.getId(),
						0, 0, 999);
				if (servicePackagePriceDoList == null) {
				}
				if (servicePackagePriceDoList != null) {
					List<ServicePackagePriceDTO> servicePackagePriceDtoList = new ArrayList<>();
					servicePackagePriceDoList.forEach(servicePackagePriceDo -> {
						String rulerV2 = servicePackagePriceDo.getRulerV2();
						List<ServicePackagePriceV2DTO> servicePackagePriceV2DTOS = JSONArray.parseArray(rulerV2, ServicePackagePriceV2DTO.class);
						List<ServicePackagePriceV2DTO> servicePackagePriceV2DTOTmp = new ArrayList<>();
						for (ServicePackagePriceV2DTO a : servicePackagePriceV2DTOS) {
							if ("Australia".equals(a.getCountry()) || "China".equals(a.getCountry())) {
								continue;
							}
							servicePackagePriceV2DTOTmp.add(a);
						}
						servicePackagePriceDo.setServicePackagePriceV2DTO(servicePackagePriceV2DTOTmp);
						servicePackagePriceDtoList.add(mapper.map(servicePackagePriceDo, ServicePackagePriceDTO.class));
					});
					serviceDto.setServicePackagePirceList(servicePackagePriceDtoList);
				}
				serviceDtoList.add(serviceDto);
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		return serviceDtoList;
	}
	
	@Override
	public int countAllService(String name) throws ServiceException {
		return serviceDao.countAllService(name);
	}

	@Override
	public List<ServiceDTO> listAllService(String name, int pageNum, int pageSize) throws ServiceException {
		if (pageNum < 0)
			pageNum = DEFAULT_PAGE_NUM;
		if (pageSize < 0)
			pageSize = DEFAULT_PAGE_SIZE;
		List<ServiceDTO> serviceDtoList = new ArrayList<ServiceDTO>();
		List<ServiceDO> serviceDoList = new ArrayList<ServiceDO>();
		try {
			serviceDoList = serviceDao.listAllService(name, pageNum * pageSize, pageSize);
			if (serviceDoList == null)
				return null;
			for (ServiceDO serviceDo : serviceDoList) {
				ServiceDTO serviceDto = mapper.map(serviceDo, ServiceDTO.class);
				List<ServicePackagePriceDO> servicePackagePriceDoList = servicePackagePriceDao.list(serviceDto.getId(),
						0, 0, 999);
				if (servicePackagePriceDoList != null) {
					List<ServicePackagePriceDTO> servicePackagePriceDtoList = new ArrayList<>();
					servicePackagePriceDoList.forEach(servicePackagePriceDo -> {
						servicePackagePriceDtoList.add(mapper.map(servicePackagePriceDo, ServicePackagePriceDTO.class));
					});
					serviceDto.setServicePackagePirceList(servicePackagePriceDtoList);
				}
				serviceDtoList.add(serviceDto);
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		return serviceDtoList;
	}

	@Override
	public ServiceDTO getServiceById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		ServiceDTO serviceDto = null;
		try {
			ServiceDO serviceDo = serviceDao.getServiceById(id);
			if (serviceDo == null)
				return null;
			serviceDto = mapper.map(serviceDo, ServiceDTO.class);
			List<ServicePackagePriceDO> servicePackagePriceDoList = servicePackagePriceDao.list(serviceDto.getId(),
					0, 0, 999);
			if (servicePackagePriceDoList != null) {
				List<ServicePackagePriceDTO> servicePackagePriceDtoList = new ArrayList<>();
				servicePackagePriceDoList.forEach(servicePackagePriceDo -> {
					servicePackagePriceDtoList.add(mapper.map(servicePackagePriceDo, ServicePackagePriceDTO.class));
				});
				serviceDto.setServicePackagePirceList(servicePackagePriceDtoList);
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		return serviceDto;
	}

	@Override
	public int deleteServiceById(int id, boolean isZx) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			return serviceDao.deleteServiceById(id, isZx);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

}