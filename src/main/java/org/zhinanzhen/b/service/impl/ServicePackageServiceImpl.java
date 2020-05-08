package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.ServicePackageDAO;
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
	public List<ServicePackageDTO> list(Integer serviceId) throws ServiceException {
		List<ServicePackageDTO> servicePackageDtoList = new ArrayList<>();
		List<ServicePackageListDO> servicePackageListDoList = new ArrayList<>();
		try {
			servicePackageListDoList = servicePackageDao.list(serviceId);
			if (servicePackageListDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (ServicePackageListDO servicePackageListDo : servicePackageListDoList)
			servicePackageDtoList.add(mapper.map(servicePackageListDo, ServicePackageDTO.class));
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

}
