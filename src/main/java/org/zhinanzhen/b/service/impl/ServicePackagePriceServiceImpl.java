package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.ServicePackagePriceDAO;
import org.zhinanzhen.b.dao.pojo.ServicePackagePriceDO;
import org.zhinanzhen.b.service.ServicePackagePriceService;
import org.zhinanzhen.b.service.pojo.ServicePackagePriceDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.ErrorCodeEnum;

@Service("ServicePackagePriceService")
public class ServicePackagePriceServiceImpl extends BaseService implements ServicePackagePriceService {

	@Resource
	private ServicePackagePriceDAO servicePackagePriceDao;

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
		return servicePackagePriceDao.update(servicePackagePriceDto.getId(), servicePackagePriceDto.getMinPrice(),
				servicePackagePriceDto.getMaxPrice(), servicePackagePriceDto.getServicePackageId(),
				servicePackagePriceDto.getRegionId()) > 0 ? servicePackagePriceDto.getId() : 0;
	}

	@Override
	public List<ServicePackagePriceDTO> listServicePackagePrice(int servicePackageId, int regionId) throws ServiceException {
		List<ServicePackagePriceDTO> servicePackagePriceDtoList = new ArrayList<>();
		List<ServicePackagePriceDO> servicePackagePriceDoList = new ArrayList<>();
		try {
			servicePackagePriceDoList = servicePackagePriceDao.list(servicePackageId, regionId);
			if (servicePackagePriceDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (ServicePackagePriceDO servicePackagePriceDo : servicePackagePriceDoList)
			servicePackagePriceDtoList.add(mapper.map(servicePackagePriceDo, ServicePackagePriceDTO.class));
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
	public void deleteById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		servicePackagePriceDao.delete(id);
	}

}
