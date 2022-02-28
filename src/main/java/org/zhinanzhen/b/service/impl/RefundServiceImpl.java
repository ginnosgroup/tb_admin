package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.*;
import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.service.RefundService;
import org.zhinanzhen.b.service.pojo.RefoundReportDTO;
import org.zhinanzhen.b.service.pojo.RefundDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.ErrorCodeEnum;
import com.ikasoa.core.utils.ObjectUtil;
import com.ikasoa.core.utils.StringUtil;

@Service("RefundService")
public class RefundServiceImpl extends BaseService implements RefundService {

	@Resource
	RefundDAO refundDao;

	@Resource
	SchoolInstitutionDAO schoolInstitutionDao;

	@Resource
	SchoolCourseDAO schoolCourseDao;

	@Resource
	MaraDAO maraDao;

	@Resource
	VisaDAO visaDao;

	@Resource
	ServiceDAO serviceDao;

	@Override
	public int addRefund(RefundDTO refundDto) throws ServiceException {
		if (ObjectUtil.isNull(refundDto)) {
			ServiceException se = new ServiceException("refundDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			RefundDO refundDo = mapper.map(refundDto, RefundDO.class);
			if (StringUtil.equals("OVST", refundDo.getType()) && ObjectUtil.isNotNull(refundDo.getCommissionOrderId())
					&& ObjectUtil.isNotNull(refundDao.getRefundByCommissionOrderId(refundDo.getCommissionOrderId()))) {
				ServiceException se = new ServiceException("不能重复创建留学佣金订单退款单!");
				se.setCode(ErrorCodeEnum.DATA_ERROR.code());
				throw se;
			}
			if (StringUtil.equals("VISA", refundDo.getType()) && ObjectUtil.isNotNull(refundDo.getVisaId())
					&& ObjectUtil.isNotNull(refundDao.getRefundByVisaId(refundDo.getVisaId()))) {
				ServiceException se = new ServiceException("不能重复创建签证佣金订单退款单!");
				se.setCode(ErrorCodeEnum.DATA_ERROR.code());
				throw se;
			}
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
	public List<RefundDTO> listRefund(String type, String state, Integer adviserId, String startDate, String endDate)
			throws ServiceException {
		List<RefundDTO> refundDtoList = new ArrayList<>();
		List<RefundDO> refundDoList = null;
		try {
			refundDoList = refundDao.listRefund(type, state, adviserId, startDate, endDate);
			if (ObjectUtil.isNull(refundDoList))
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (RefundDO refundDo : refundDoList) {
			RefundDTO refundDto = mapper.map(refundDo, RefundDTO.class);
			buildAttr(refundDto);
			refundDtoList.add(refundDto);
		}
		return refundDtoList;
	}

	@Override
	public RefundDTO getRefundById(int id) throws ServiceException {
		RefundDO refundDo = null;
		try {
			refundDo = refundDao.getRefundById(id);
			if (ObjectUtil.isNull(refundDo))
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		RefundDTO refundDto = mapper.map(refundDo, RefundDTO.class);
		buildAttr(refundDto);
		return refundDto;
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

	@Override
	public List<RefoundReportDTO> listRefundReport(String startDate, String endDate, String dateType,
												   String dateMethod, Integer regionId, Integer adviserId, List<String> adviserIdList) throws ServiceException {
		List<RefoundReportDTO> refoundReportDtoList = new ArrayList<>();
		List<RefoundReportDO> refoundReportDoList = new ArrayList<>();
		try {
			refoundReportDoList = refundDao.listRefundReport(startDate,
					theDateTo23_59_59(endDate), dateType, dateMethod, regionId, adviserId, adviserIdList);
			if (refoundReportDoList == null)
				return null;
			refoundReportDoList.forEach(refoundReportDO -> refoundReportDtoList
					.add(mapper.map(refoundReportDO, RefoundReportDTO.class)));
			return refoundReportDtoList;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
	}
	
	private void buildAttr(RefundDTO refundDto) {
		if (refundDto != null && "OVST".equalsIgnoreCase(refundDto.getType())) {
			if (refundDto.getSchoolId() > 0) {
				SchoolInstitutionDO schoolInstitutionDo = schoolInstitutionDao
						.getSchoolInstitutionById(refundDto.getSchoolId());
				refundDto.setSchoolName(schoolInstitutionDo.getInstitutionTradingName());
				refundDto.setInstitutionName(schoolInstitutionDo.getInstitutionName());
				refundDto.setInstitutionTradingName(schoolInstitutionDo.getInstitutionTradingName());
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
		if (refundDto != null && refundDto.getVisaId() != null) {
			VisaDO visaDO = visaDao.getVisaById(refundDto.getVisaId());
			ServiceDO serviceDO = serviceDao.getServiceById(visaDO.getServiceId());
			refundDto.setServiceName(StringUtil.merge(serviceDO.getName(),  "-" , serviceDO.getCode()));
			refundDto.setKjApprovalDate(visaDO.getKjApprovalDate());
		}
	}

}
