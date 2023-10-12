package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.ServiceOrderApplicantDAO;
import org.zhinanzhen.b.dao.pojo.ServiceOrderApplicantDO;
import org.zhinanzhen.b.service.ServiceOrderApplicantService;
import org.zhinanzhen.b.service.pojo.ServiceOrderApplicantDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.ErrorCodeEnum;

@Service("ServiceOrderApplicantService")
public class ServiceOrderApplicantServiceImpl extends BaseService implements ServiceOrderApplicantService {

	@Resource
	private ServiceOrderApplicantDAO serviceOrderApplicantDao;

	@Override
	public int addServiceOrderApplicant(ServiceOrderApplicantDTO serviceOrderApplicantDto) throws ServiceException {
		if (serviceOrderApplicantDto == null) {
			ServiceException se = new ServiceException("serviceOrderApplicantDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			ServiceOrderApplicantDO serviceOrderApplicantDo = mapper.map(serviceOrderApplicantDto,
					ServiceOrderApplicantDO.class);
			if (serviceOrderApplicantDao.add(serviceOrderApplicantDo) > 0) {
				serviceOrderApplicantDto.setId(serviceOrderApplicantDo.getId());
				return serviceOrderApplicantDo.getId();
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
	public List<ServiceOrderApplicantDTO> listServiceOrderApplicant(Integer serviceOrderId, Integer applicantId)
			throws ServiceException {
		List<ServiceOrderApplicantDTO> serviceOrderApplicantDtoList = new ArrayList<>();
		List<ServiceOrderApplicantDO> serviceOrderApplicantDoList = new ArrayList<>();
		try {
			serviceOrderApplicantDoList = serviceOrderApplicantDao.list(serviceOrderId, applicantId);
			if (serviceOrderApplicantDoList == null)
				return null;
			for (ServiceOrderApplicantDO serviceOrderApplicantDo : serviceOrderApplicantDoList) {
				ServiceOrderApplicantDTO serviceOrderApplicantDto = mapper.map(serviceOrderApplicantDo,
						ServiceOrderApplicantDTO.class);
				serviceOrderApplicantDtoList.add(serviceOrderApplicantDto);
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		return serviceOrderApplicantDtoList;
	}

	@Override
	public int updateServiceOrderApplicant(ServiceOrderApplicantDTO serviceOrderApplicantDto) throws ServiceException {
		if (serviceOrderApplicantDto == null) {
			ServiceException se = new ServiceException("serviceOrderApplicantDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (serviceOrderApplicantDto.getId() <= 0 || (serviceOrderApplicantDto.getApplicantId() <= 0
				&& serviceOrderApplicantDto.getServiceOrderId() <= 0))
			return 0;
		try {
			int i = serviceOrderApplicantDao
					.update(mapper.map(serviceOrderApplicantDto, ServiceOrderApplicantDO.class));
			return i;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int deleteServiceOrderApplicant(Integer id) throws ServiceException {
		if (id == null || id <= 0) {
			ServiceException se = new ServiceException("applicant id is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		return serviceOrderApplicantDao.delete(id);
	}

}
