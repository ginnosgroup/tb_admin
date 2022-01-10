package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.MaraDAO;
import org.zhinanzhen.b.dao.RefundDAO;
import org.zhinanzhen.b.dao.SchoolCourseDAO;
import org.zhinanzhen.b.dao.SchoolInstitutionDAO;
import org.zhinanzhen.b.dao.pojo.MaraDO;
import org.zhinanzhen.b.dao.pojo.RefundDO;
import org.zhinanzhen.b.dao.pojo.SchoolCourseDO;
import org.zhinanzhen.b.dao.pojo.SchoolInstitutionDO;
import org.zhinanzhen.b.service.RefundService;
import org.zhinanzhen.b.service.pojo.RefundDTO;
import org.zhinanzhen.b.service.pojo.SchoolInstitutionListDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.ErrorCodeEnum;

@Service
public class RefundServiceImpl extends BaseService implements RefundService {

	@Resource
	RefundDAO refundDao;

	@Resource
	SchoolInstitutionDAO schoolInstitutionDao;

	@Resource
	SchoolCourseDAO schoolCourseDao;

	@Resource
	MaraDAO maraDao;

	@Override
	public int addRefund(RefundDTO refundDto) throws ServiceException {
		if (refundDto == null) {
			ServiceException se = new ServiceException("refundDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			RefundDO refundDo = mapper.map(refundDto, RefundDO.class);
			if (refundDao.addRefund(refundDo) > 0) {
				refundDto.setId(refundDo.getId());
				return refundDo.getId();
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
	public List<RefundDTO> listRefund(String type, String state) throws ServiceException {
		List<RefundDTO> refundDtoList = new ArrayList<>();
		List<RefundDO> refundDoList = null;
		try {
			refundDoList = refundDao.listRefund(type, state);
			if (refundDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (RefundDO refundDo : refundDoList) {
			RefundDTO refundDto = mapper.map(refundDo, RefundDTO.class);
			if (refundDto != null && "OVST".equalsIgnoreCase(refundDto.getType())) {
				if (refundDto.getSchoolId() > 0) {
					SchoolInstitutionDO schoolInstitutionDo = schoolInstitutionDao
							.getSchoolInstitutionById(refundDto.getSchoolId());
					refundDto.setSchoolName(schoolInstitutionDo.getInstitutionTradingName());
					refundDto.setInstitutionName(schoolInstitutionDo.getInstitutionName());
				}
				if (refundDto.getCourseId() > 0) {
					SchoolCourseDO schoolCourseDo = schoolCourseDao.schoolCourseById(refundDto.getCourseId());
					refundDto.setCourseName(schoolCourseDo.getCourseName());
				}
			}
			if (refundDto != null && refundDto.getMaraId() > 0) {
				MaraDO maraDo = maraDao.getMaraById(refundDto.getMaraId());
				refundDto.setMaraName(maraDo.getName());
			}
			refundDtoList.add(refundDto);
		}
		return refundDtoList;
	}

	@Override
	public RefundDTO getRefundById(int id) throws ServiceException {
		RefundDO refundDo = null;
		try {
			refundDo = refundDao.getRefundById(id);
			if (refundDo == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		return mapper.map(refundDo, RefundDTO.class);
	}

	@Override
	public int updateRefund(RefundDTO refundDto) throws ServiceException {
		if (refundDto == null) {
			ServiceException se = new ServiceException("refundDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			RefundDO refundDo = mapper.map(refundDto, RefundDO.class);
			return refundDao.updateRefund(refundDo);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int deleteRefundById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			return refundDao.deleteRefundById(id);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

}
