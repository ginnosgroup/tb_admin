package org.zhinanzhen.b.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhinanzhen.b.dao.*;
import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.service.RefundService;
import org.zhinanzhen.b.service.VisaPerformanceAllocation;
import org.zhinanzhen.b.service.pojo.RefoundReportDTO;
import org.zhinanzhen.b.service.pojo.RefundDTO;
import org.zhinanzhen.b.service.pojo.VisaOfficialDTO;
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
	CommissionOrderDAO commissionOrderDao;

	@Resource
	VisaDAO visaDao;

	@Resource
	ServiceDAO serviceDao;

	@Value("${defaultExchangeRate}")
	private double quarterExchangeRate;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int addRefund(RefundDTO refundDto) throws ServiceException {
		if (ObjectUtil.isNull(refundDto)) {
			ServiceException se = new ServiceException("refundDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			RefundDO refundDo = mapper.map(refundDto, RefundDO.class);
			VisaDO sourceVisa = null;
			VisaDO counterpartVisa = null;
			List<VisaDO> visaOrders = null;
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
			if (StringUtil.equals("VISA", refundDo.getType()) && refundDo.getVisaId() != null) {
				sourceVisa = visaDao.getVisaById(refundDo.getVisaId());
				if (sourceVisa != null && isRecommendationShare(sourceVisa.getPerformanceRatio())) {
					visaOrders = visaDao.listVisaByServiceOrderId(sourceVisa.getServiceOrderId());
					counterpartVisa = findRecommendationCounterpart(sourceVisa, visaOrders);
					if (counterpartVisa != null && refundDao.getRefundByVisaId(counterpartVisa.getId()) != null) {
						ServiceException se = new ServiceException("该推荐顾问佣金订单对应的主顾问/推荐顾问退款单已存在!");
						se.setCode(ErrorCodeEnum.DATA_ERROR.code());
						throw se;
					}
				}
			}
			if (StringUtil.equals("OVST", refundDo.getType()) && refundDo.getCommissionOrderId() != null) {
				CommissionOrderListDO commissionOrderListDo = commissionOrderDao
						.getCommissionOrderById(refundDo.getCommissionOrderId());
				if (commissionOrderListDo != null)
					refundDo.setCourseId(commissionOrderListDo.getCourseId());
			}
			RefundDO counterpartRefundDo = null;
			// 推荐顾问佣金单退款时，把完整申请金额按70%/30%拆成两张退款单。
			if (sourceVisa != null && counterpartVisa != null) {
				BigDecimal total = BigDecimal.valueOf(refundDo.getAmount()).setScale(2, RoundingMode.HALF_UP);
				BigDecimal recommendedAmount = total.multiply(new BigDecimal("0.30")).setScale(2, RoundingMode.HALF_UP);
				BigDecimal primaryAmount = total.subtract(recommendedAmount);
				boolean sourceIsPrimary = Math.abs(sourceVisa.getPerformanceRatio() - 0.70) < 0.000001;
				BigDecimal sourceAmount = sourceIsPrimary ? primaryAmount : recommendedAmount;
				BigDecimal counterpartAmount = sourceIsPrimary ? recommendedAmount : primaryAmount;

				refundDo.setVisaId(sourceVisa.getId());
				refundDo.setAdviserId(sourceVisa.getAdviserId());
				refundDo.setAmount(sourceAmount.doubleValue());
				refundDto.setAmount(sourceAmount.doubleValue());

				counterpartRefundDo = new RefundDO();
				BeanUtils.copyProperties(refundDo, counterpartRefundDo);
				counterpartRefundDo.setId(0);
				counterpartRefundDo.setVisaId(counterpartVisa.getId());
				counterpartRefundDo.setAdviserId(counterpartVisa.getAdviserId());
				counterpartRefundDo.setAmount(counterpartAmount.doubleValue());
			}

			// 退款单保留各自佣金单对应的余额；推荐顾问配对退款分别使用拆分后的金额。
			if (sourceVisa != null) {
				if (visaOrders == null)
					visaOrders = visaDao.listVisaByServiceOrderId(sourceVisa.getServiceOrderId());
				if (visaOrders != null) {
					double sum = visaOrders.stream().mapToDouble(VisaDO::getAmount).sum();
					boolean isCny = !visaOrders.isEmpty() && "CNY".equalsIgnoreCase(visaOrders.get(0).getCurrency());
					refundDo.setReceived(sum - (isCny ? refundDo.getAmount() / quarterExchangeRate : refundDo.getAmount()));
					if (counterpartRefundDo != null)
						counterpartRefundDo.setReceived(sum - (isCny ? counterpartRefundDo.getAmount() / quarterExchangeRate
								: counterpartRefundDo.getAmount()));
				}
			}
			if (refundDao.addRefund(refundDo) > 0) {
				if (counterpartRefundDo != null && refundDao.addRefund(counterpartRefundDo) <= 0) {
					throw new IllegalStateException("推荐顾问佣金订单退款单创建失败。");
				}
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

	private VisaDO findRecommendationCounterpart(VisaDO source, List<VisaDO> visaOrders) {
		if (source == null || visaOrders == null || !isRecommendationShare(source.getPerformanceRatio()))
			return null;
		double counterpartRatio = Math.abs(source.getPerformanceRatio() - 0.70) < 0.000001 ? 0.30 : 0.70;
		VisaDO counterpart = null;
		for (VisaDO candidate : visaOrders) {
			if (candidate == null || candidate.getId() == source.getId()
					|| candidate.getInstallmentNum() != source.getInstallmentNum()
					|| !java.util.Objects.equals(candidate.getCode(), source.getCode())
					|| Math.abs(candidate.getPerformanceRatio() - counterpartRatio) >= 0.000001)
				continue;
			if (counterpart != null)
				throw new IllegalStateException("同一期推荐顾问佣金订单不唯一，无法自动生成配对退款单。");
			counterpart = candidate;
		}
		return counterpart;
	}

	private boolean isRecommendationShare(double ratio) {
		return Math.abs(ratio - 0.70) < 0.000001 || Math.abs(ratio - 0.30) < 0.000001;
	}

	@Override
	public int countRefund(String type, String state, Integer visaId, Integer commissionOrderId, Integer regionId,
						   Integer adviserId, String adviserName, String startDate, String endDate, String startReviewedDate,
						   String endReviewedDate, String startCompletedDate, String endCompletedDate) throws ServiceException {
		return refundDao.countRefund(type, state, visaId, commissionOrderId, regionId, adviserId, adviserName,
				startDate, theDateTo23_59_59(endDate));
	}

	@Override
	public List<RefundDTO> listRefund(String type, String state, Integer visaId, Integer commissionOrderId,
									  Integer regionId, Integer adviserId, String adviserName, String startDate, String endDate,
									  String startReviewedDate, String endReviewedDate, String startCompletedDate, String endCompletedDate,
									  int pageNum, int pageSize) throws ServiceException {
		List<RefundDTO> refundDtoList = new ArrayList<>();
		List<RefundDO> refundDoList = null;
		try {
			refundDoList = refundDao.listRefund(type, state, visaId, commissionOrderId, regionId, adviserId,
					adviserName, startDate, endDate, pageNum * pageSize, pageSize);
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
	@Transactional(rollbackFor = Exception.class)
	public int updateRefund(RefundDTO refundDto) throws ServiceException {
		if (refundDto == null) {
			ServiceException se = new ServiceException("refundDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			RefundDO refundDo = mapper.map(refundDto, RefundDO.class);
			RefundDO existingRefund = refundDao.getRefundById(refundDo.getId());
			VisaDO sourceVisa = existingRefund != null && "VISA".equalsIgnoreCase(existingRefund.getType())
					&& existingRefund.getVisaId() != null ? visaDao.getVisaById(existingRefund.getVisaId()) : null;
			if (refundDo.getAmount() > 0 && sourceVisa != null
					&& isRecommendationShare(sourceVisa.getPerformanceRatio())) {
				List<VisaDO> visaOrders = visaDao.listVisaByServiceOrderId(sourceVisa.getServiceOrderId());
				VisaDO counterpartVisa = findRecommendationCounterpart(sourceVisa, visaOrders);
				RefundDO counterpartRefund = counterpartVisa == null ? null
						: refundDao.getRefundByVisaId(counterpartVisa.getId());
				if (counterpartRefund != null) {
					counterpartRefund.setAmount(VisaPerformanceAllocation.counterpartAmount(
							refundDo.getAmount(), sourceVisa.getPerformanceRatio()));
					int updated = refundDao.updateRefund(refundDo);
					if (updated <= 0 || refundDao.updateRefund(counterpartRefund) <= 0)
						throw new IllegalStateException("主顾问和推荐顾问退款金额同步失败。");
					return updated;
				}
			}
			return refundDao.updateRefund(refundDo);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int deleteRefundById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			RefundDO refund = refundDao.getRefundById(id);
			if (refund != null && "VISA".equalsIgnoreCase(refund.getType()) && refund.getVisaId() != null) {
				VisaDO sourceVisa = visaDao.getVisaById(refund.getVisaId());
				if (sourceVisa != null && isRecommendationShare(sourceVisa.getPerformanceRatio())) {
					List<VisaDO> visaOrders = visaDao.listVisaByServiceOrderId(sourceVisa.getServiceOrderId());
					VisaDO counterpartVisa = findRecommendationCounterpart(sourceVisa, visaOrders);
					if (counterpartVisa != null) {
						RefundDO counterpartRefund = refundDao.getRefundByVisaId(counterpartVisa.getId());
						if (counterpartRefund != null && counterpartRefund.getId() != id)
							refundDao.deleteRefundById(counterpartRefund.getId());
					}
				}
			}
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

	@Override
	public List<RefoundReportDTO> listRefundReport2(String startDate, String endDate, String dateType,
													String dateMethod, Integer regionId, Integer adviserId, List<String> adviserIdList) throws ServiceException {
		List<RefoundReportDTO> refoundReportDtoList = new ArrayList<>();
		List<RefoundReportDO> refoundReportDoList = new ArrayList<>();
		try {
			refoundReportDoList = refundDao.listRefundReport2(startDate,
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

	@Override
	public List<RefoundReportDTO> listRefundReportSubtractGst(String startDate, String endDate, String dateType,
															  String dateMethod, Integer regionId, Integer adviserId, List<String> adviserIdList) throws ServiceException {
		List<RefoundReportDTO> refoundReportDtoList = new ArrayList<>();
		List<RefoundReportDO> refoundReportDoList = new ArrayList<>();
		try {
			refoundReportDoList = refundDao.listRefundReportSubtractGst(startDate,
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
			if (refundDto.getCourseId() > 0) {
				SchoolCourseDO schoolCourseDo = schoolCourseDao.schoolCourseById(refundDto.getCourseId());
				refundDto.setCourseName(schoolCourseDo.getCourseName());
				if (schoolCourseDo.getProviderId() > 0) {
					SchoolInstitutionDO schoolInstitutionDo = schoolInstitutionDao
							.getSchoolInstitutionById(schoolCourseDo.getProviderId());
					refundDto.setSchoolName(schoolInstitutionDo.getInstitutionTradingName());
					refundDto.setInstitutionName(schoolInstitutionDo.getInstitutionName());
					refundDto.setInstitutionTradingName(schoolInstitutionDo.getInstitutionTradingName());
				}
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
		if ("AUD".equalsIgnoreCase(refundDto.getCurrencyType()) && refundDto.getExchangeRate() > 0) {
			refundDto.setAmountAUD(refundDto.getAmount());
			refundDto.setAmountCNY(roundHalfUp2(refundDto.getAmount() * refundDto.getExchangeRate()));
		}
		if ("CNY".equalsIgnoreCase(refundDto.getCurrencyType()) && refundDto.getExchangeRate() > 0) {
			refundDto.setAmountAUD(roundHalfUp2(refundDto.getAmount() / refundDto.getExchangeRate()));
			refundDto.setAmountCNY(refundDto.getAmount());
		}
	}

}
