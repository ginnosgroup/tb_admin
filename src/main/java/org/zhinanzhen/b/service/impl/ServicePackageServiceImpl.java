package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.ServiceDAO;
import org.zhinanzhen.b.dao.ServicePackageDAO;
import org.zhinanzhen.b.dao.pojo.ServiceDO;
import org.zhinanzhen.b.dao.pojo.ServicePackageDO;
import org.zhinanzhen.b.dao.pojo.ServicePackageListDO;
import org.zhinanzhen.b.service.ServicePackageService;
import org.zhinanzhen.b.service.pojo.ServicePackageDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.ErrorCodeEnum;

@Service("ServicePackageService")
public class ServicePackageServiceImpl extends BaseService implements ServicePackageService {
	
	@Resource
	private ServiceDAO serviceDao;

	@Resource
	private ServicePackageDAO servicePackageDao;

	@Override
	public int add(ServicePackageDTO servicePackageDto) throws ServiceException {
		if (servicePackageDto == null) {
			ServiceException se = new ServiceException("servicePackageDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			ServicePackageDO servicePackageDo = mapper.map(servicePackageDto, ServicePackageDO.class);
			if (servicePackageDao.add(servicePackageDo) > 0) {
				servicePackageDto.setId(servicePackageDo.getId());
				return servicePackageDo.getId();
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
	public int update(ServicePackageDTO servicePackageDto) throws ServiceException {
		if (servicePackageDto == null) {
			ServiceException se = new ServiceException("servicePackageDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (servicePackageDto.getId() <= 0) {
			ServiceException se = new ServiceException("id is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			ServicePackageDO servicePackageDo = mapper.map(servicePackageDto, ServicePackageDO.class);
			if (servicePackageDao.update(servicePackageDo.getId(), servicePackageDo.getType(),
					servicePackageDo.getServiceId(), servicePackageDo.getNum()) > 0) {
				return servicePackageDo.getId();
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
	public int count(Integer serviceId) throws ServiceException {
		return servicePackageDao.count(serviceId);
	}

	@Override
	public List<ServicePackageDTO> list(Integer serviceId, int pageNum, int pageSize) throws ServiceException {
		if (pageNum < 0)
			pageNum = DEFAULT_PAGE_NUM;
		if (pageSize < 0)
			pageSize = DEFAULT_PAGE_SIZE;
		List<ServicePackageDTO> servicePackageDtoList = new ArrayList<>();
		List<ServicePackageListDO> servicePackageListDoList = new ArrayList<>();
		try {
			servicePackageListDoList = servicePackageDao.list(serviceId, pageNum * pageSize, pageSize);
			if (servicePackageListDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (ServicePackageListDO servicePackageListDo : servicePackageListDoList) {
			ServicePackageDTO servicePackageDto = mapper.map(servicePackageListDo, ServicePackageDTO.class);
			ServiceDO serviceDo = serviceDao.getServiceById(servicePackageDto.getServiceId());
			if (serviceDo != null)
				servicePackageDto.setServiceCode(serviceDo.getCode());
			servicePackageDtoList.add(servicePackageDto);
		}
		return servicePackageDtoList;
	}

	@Override
	public ServicePackageDTO getById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		ServicePackageDTO servicePackageDto = null;
		try {
			ServicePackageDO servicePackageDo = servicePackageDao.getById(id);
			if (servicePackageDo == null)
				return null;
			servicePackageDto = mapper.map(servicePackageDo, ServicePackageDTO.class);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		return servicePackageDto;
	}

	@Override
	public int delete(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			return servicePackageDao.delete(id);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

    @Override
    public List<ServicePackageDTO> getByType(String type) throws ServiceException {
		if (type == null) {
			ServiceException se = new ServiceException("type is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		List<ServicePackageDTO> servicePackageDto = null;
		try {
			servicePackageDto = servicePackageDao.getByType(type);
			if (servicePackageDto == null)
				return null;
//			servicePackageDto = mapper.map(servicePackageDo, ServicePackageDTO.class);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		return servicePackageDto;
    }

}
