package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.ServicePackageDAO;
import org.zhinanzhen.b.dao.pojo.ServicePackageDO;
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
	public List<ServicePackageDTO> list() throws ServiceException {
		List<ServicePackageDTO> servicePackageDtoList = new ArrayList<>();
		List<ServicePackageDO> servicePackageDoList = new ArrayList<>();
		try {
			servicePackageDoList = servicePackageDao.listAll();
			if (servicePackageDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (ServicePackageDO servicePackageDo : servicePackageDoList)
			servicePackageDtoList.add(mapper.map(servicePackageDo, ServicePackageDTO.class));
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

}
