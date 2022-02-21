package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.ServiceDAO;
import org.zhinanzhen.b.dao.pojo.ServiceDO;
import org.zhinanzhen.b.service.ServiceService;
import org.zhinanzhen.b.service.pojo.ServiceDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.ErrorCodeEnum;

@Service("ServiceService")
public class ServiceServiceImpl extends BaseService implements ServiceService {

	@Resource
	private ServiceDAO serviceDao;

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
	public int updateService(int id, String name, String code) throws ServiceException {
		try {
			return serviceDao.updateService(id, name, code);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public List<ServiceDTO> listService(String name, boolean isZx) throws ServiceException {
		List<ServiceDTO> serviceDtoList = new ArrayList<ServiceDTO>();
		List<ServiceDO> serviceDoList = new ArrayList<ServiceDO>();
		try {
			serviceDoList = serviceDao.listService(name,isZx);
			if (serviceDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (ServiceDO serviceDo : serviceDoList) {
//			if (!"190,491".contains(serviceDo.getCode())){
				ServiceDTO serviceDto = mapper.map(serviceDo, ServiceDTO.class);
				serviceDtoList.add(serviceDto);
//			}
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