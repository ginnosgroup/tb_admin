package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.ManualRefundDAO;
import org.zhinanzhen.b.dao.pojo.ManualRefundDO;
import org.zhinanzhen.b.service.ManualRefundService;
import org.zhinanzhen.b.service.pojo.ManualRefundDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.ErrorCodeEnum;

@Service
public class ManualRefundServiceImpl extends BaseService implements ManualRefundService {

	@Resource
	ManualRefundDAO manualRefundDao;

	@Override
	public int addManualRefund(ManualRefundDTO manualRefundDto) throws ServiceException {
		if (manualRefundDto == null) {
			ServiceException se = new ServiceException("manualRefundDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			ManualRefundDO manualRefundDo = mapper.map(manualRefundDto, ManualRefundDO.class);
			if (manualRefundDao.addManualRefund(manualRefundDo) > 0) {
				manualRefundDto.setId(manualRefundDo.getId());
				return manualRefundDo.getId();
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
	public List<ManualRefundDTO> listManualRefund(String type, String state) throws ServiceException {
		List<ManualRefundDTO> manualRefundDtoList = new ArrayList<>();
		List<ManualRefundDO> manualRefundDoList = null;
		try {
			manualRefundDoList = manualRefundDao.listManualRefund(type, state);
			if (manualRefundDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (ManualRefundDO manualRefundDo : manualRefundDoList) {
			ManualRefundDTO manualRefundDto = mapper.map(manualRefundDo, ManualRefundDTO.class);
			manualRefundDtoList.add(manualRefundDto);
		}
		return manualRefundDtoList;
	}
	
	@Override
	public ManualRefundDTO getManualRefundById(int id) throws ServiceException {
		ManualRefundDO manualRefundDo = null;
		try {
			manualRefundDo = manualRefundDao.getManualRefundById(id);
			if (manualRefundDo == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		return mapper.map(manualRefundDo, ManualRefundDTO.class);
	}

	@Override
	public int updateManualRefund(ManualRefundDTO manualRefundDto) throws ServiceException {
		if (manualRefundDto == null) {
			ServiceException se = new ServiceException("manualRefundDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			ManualRefundDO manualRefundDo = mapper.map(manualRefundDto, ManualRefundDO.class);
			return manualRefundDao.updateManualRefund(manualRefundDo);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int deleteManualRefundById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			return manualRefundDao.deleteManualRefundById(id);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

}
