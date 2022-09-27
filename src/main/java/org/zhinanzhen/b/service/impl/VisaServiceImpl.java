package org.zhinanzhen.b.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.*;
import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.service.AbleStateEnum;
import org.zhinanzhen.b.service.MaraService;
import org.zhinanzhen.b.service.ServicePackagePriceService;
import org.zhinanzhen.b.service.VisaService;
import org.zhinanzhen.b.service.pojo.*;
import org.zhinanzhen.b.service.pojo.ant.Sorter;
import org.zhinanzhen.tb.controller.ListResponse;
import org.zhinanzhen.tb.dao.AdminUserDAO;
import org.zhinanzhen.tb.dao.AdviserDAO;
import org.zhinanzhen.tb.dao.UserDAO;
import org.zhinanzhen.tb.dao.pojo.AdminUserDO;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
import org.zhinanzhen.tb.dao.pojo.UserDO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.ErrorCodeEnum;
import com.ikasoa.core.utils.StringUtil;

import lombok.Synchronized;

@Service("VisaService")
public class VisaServiceImpl extends BaseService implements VisaService {

	@Resource
	private VisaDAO visaDao;
	
	@Resource
	private RefundDAO refundDao;

	@Resource
	private AdviserDAO adviserDao;

	@Resource
	private OfficialDAO officialDao;

	@Resource
	private ReceiveTypeDAO receiveTypeDao;

	@Resource
	private ServiceDAO serviceDao;

	@Resource
	private RemindDAO remindDao;

	@Resource
	private UserDAO userDao;
	
	@Resource
	private ApplicantDAO applicantDao;
	
	@Resource
	private ServiceOrderApplicantDAO serviceOrderApplicantDao;
	
	@Resource
	private ServiceOrderDAO serviceOrderDao;

//	@Resource
//	private ServiceOrderReviewDAO serviceOrderReviewDao;

	@Resource
	private VisaCommentDAO visaCommentDao;

	@Resource
	private AdminUserDAO adminUserDao;

	@Resource
	CommissionOrderDAO commissionOrderDAO;

	@Resource
	private MailRemindDAO mailRemindDAO;

	@Resource
	private CommissionOrderTempDAO commissionOrderTempDao;

	@Resource
	private ServicePackagePriceDAO servicePackagePriceDAO;

	@Resource
	private MaraDAO maraDAO;

	@Override
	public int addVisa(VisaDTO visaDto) throws ServiceException {
		if (visaDto == null) {
			ServiceException se = new ServiceException("visaDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (visaDao.countVisaByServiceOrderIdAndExcludeCode(visaDto.getServiceOrderId(), visaDto.getCode()) > 0) {
			ServiceException se = new ServiceException("已创建过佣金订单,不能重复创建!");
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		try {
			VisaDO visaDo = mapper.map(visaDto, VisaDO.class);
			if (visaDao.addVisa(visaDo) > 0) {
				visaDto.setId(visaDo.getId());
				return visaDo.getId();
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
	public int updateVisa(VisaDTO visaDto) throws ServiceException {
		if (visaDto == null) {
			ServiceException se = new ServiceException("visaDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		if (visaDto.getVerifyCode() != null){
			List<CommissionOrderDO> commissionOrderDOS = commissionOrderDAO.listCommissionOrderByVerifyCode(visaDto.getVerifyCode());
			List<VisaDO> visaDOS = visaDao.listVisaByVerifyCode(visaDto.getVerifyCode());
			List<CommissionOrderTempDO> list = commissionOrderTempDao.getCommissionOrderTempByVerifyCode(visaDto.getVerifyCode());
			if (commissionOrderDOS.size() > 0 || list.size() > 0) {
				ServiceException se = new ServiceException("对账code:"+visaDto.getVerifyCode()+"已经存在,请重新创建新的code!");
				se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
				throw se;
			}
			for (VisaDO visaDO : visaDOS){
				if (visaDO.getId() != visaDto.getId()){
					ServiceException se = new ServiceException("对账code:"+visaDto.getVerifyCode()+"已经存在,请重新创建新的code!");
					se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
					throw se;
				}
			}
		}
		try {
			putReviews(visaDto);
			VisaDO visaDo = mapper.map(visaDto, VisaDO.class);
			return visaDao.updateVisa(visaDo);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int countVisa(Integer id, String keyword, String startHandlingDate, String endHandlingDate,
                         List<String> stateList, List<String> commissionStateList, String startKjApprovalDate,
                         String endKjApprovalDate, String startDate, String endDate, String startInvoiceCreate, String endInvoiceCreate, List<Integer> regionIdList, Integer adviserId,
                         Integer userId, String applicantName, String state) throws ServiceException {
		return visaDao.countVisa(id, keyword, startHandlingDate, theDateTo23_59_59(endHandlingDate), stateList,
				commissionStateList, theDateTo00_00_00(startKjApprovalDate), theDateTo23_59_59(endKjApprovalDate), startDate,
				theDateTo23_59_59(endDate), startInvoiceCreate,theDateTo23_59_59(endInvoiceCreate),regionIdList, adviserId, userId, applicantName, state);
	}

	@Override
	public List<VisaDTO> listVisa(Integer id, String keyword, String startHandlingDate, String endHandlingDate,
								  List<String> stateList, List<String> commissionStateList, String startKjApprovalDate,
								  String endKjApprovalDate, String startDate, String endDate, String startInvoiceCreate, String endInvoiceCreate, List<Integer> regionIdList, Integer adviserId,
								  Integer userId, String userName, String applicantName, String state, int pageNum, int pageSize, Sorter sorter) throws ServiceException {
		if (pageNum < 0) {
			pageNum = DEFAULT_PAGE_NUM;
		}
		if (pageSize < 0) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		String orderBy = "ORDER BY bv.gmt_create DESC, bv.installment_num ASC";
		if (sorter != null) {
			if (sorter.getId() != null)
				orderBy = StringUtil.merge("ORDER BY ", sorter.getOrderBy("bv.id", sorter.getId()));
			if (sorter.getUserName() != null)
				orderBy = StringUtil.merge("ORDER BY ", sorter.getOrderBy("tbu.name", sorter.getUserName()));
			if (sorter.getAdviserName() != null)
				orderBy = StringUtil.merge("ORDER BY ", sorter.getOrderBy("a.name", sorter.getAdviserName()));
		}
		List<VisaDTO> visaDtoList = new ArrayList<>();
		List<VisaListDO> visaListDoList = new ArrayList<>();
		try {
			visaListDoList = visaDao.listVisa(id, keyword, startHandlingDate, theDateTo23_59_59(endHandlingDate),
					stateList, commissionStateList, theDateTo00_00_00(startKjApprovalDate),
					theDateTo23_59_59(endKjApprovalDate), startDate, theDateTo23_59_59(endDate), startInvoiceCreate,
					theDateTo23_59_59(endInvoiceCreate), regionIdList, adviserId, userId, userName, applicantName,
					state, pageNum * pageSize, pageSize, orderBy);
			if (visaListDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (VisaListDO visaListDo : visaListDoList) {
			VisaDTO visaDto = putVisaDTO(visaListDo);
			/*
			VisaDTO visaDto = mapper.map(visaListDo, VisaDTO.class);
			putReviews(visaDto);
			if (visaDto.getUserId() > 0) {
				UserDO userDo = userDao.getUserById(visaDto.getUserId());
				visaDto.setUserName(userDo.getName());
				visaDto.setPhone(userDo.getPhone());
				visaDto.setBirthday(userDo.getBirthday());
			}
//			ServiceOrderDO serviceOrderDo = serviceOrderDao.getServiceOrderById(visaListDo.getServiceOrderId());
//			if (serviceOrderDo != null && StringUtil.isNotEmpty(serviceOrderDo.getRefuseReason()))
//				visaDto.setRefuseReason(serviceOrderDo.getRefuseReason());
			AdviserDO adviserDo = adviserDao.getAdviserById(visaListDo.getAdviserId());
			if (adviserDo != null) {
				visaDto.setAdviserName(adviserDo.getName());
			}
			OfficialDO officialDo = officialDao.getOfficialById(visaListDo.getOfficialId());
			if (officialDo != null) {
				visaDto.setOfficialName(officialDo.getName());
			}
			ReceiveTypeDO receiveTypeDo = receiveTypeDao.getReceiveTypeById(visaListDo.getReceiveTypeId());
			if (receiveTypeDo != null) {
				visaDto.setReceiveTypeName(receiveTypeDo.getName());
			}
			ServiceDO serviceDo = serviceDao.getServiceById(visaListDo.getServiceId());
			if (serviceDo != null) {
				visaDto.setServiceCode(serviceDo.getCode());
			}*/
			List<Date> remindDateList = new ArrayList<>();
			List<RemindDO> remindDoList = remindDao.listRemindByVisaId(visaDto.getId(), adviserId,
					AbleStateEnum.ENABLED.toString());
			for (RemindDO remindDo : remindDoList) {
				remindDateList.add(remindDo.getRemindDate());
			}
			visaDto.setRemindDateList(remindDateList);
			/*
			List<VisaDO> list = visaDao.listVisaByCode(visaDto.getCode());
			if (list != null) {
				double totalPerAmount = 0.00;
				double totalAmount = 0.00;
				for (VisaDO visaDo : list) {
					totalPerAmount += visaDo.getPerAmount();
					if (visaDo.getPaymentVoucherImageUrl1() != null || visaDo.getPaymentVoucherImageUrl2() != null
							|| visaDo.getPaymentVoucherImageUrl3() != null
							|| visaDo.getPaymentVoucherImageUrl4() != null
							|| visaDo.getPaymentVoucherImageUrl5() != null)
						totalAmount += visaDo.getAmount();
				}
				visaDto.setTotalPerAmount(totalPerAmount);
				visaDto.setTotalAmount(totalAmount);
			}*/
			visaDtoList.add(visaDto);
		}
		return visaDtoList;
	}
	
	@Override
	public List<VisaReportDTO> listVisaReport(String startDate, String endDate, String dateType, String dateMethod,
			Integer regionId, Integer adviserId, List<String> adviserIdList) throws ServiceException {
		List<VisaReportDO> visaReportDoList = new ArrayList<>();
		List<VisaReportDTO> visaReportDtoList = new ArrayList<>();
		try {
			visaReportDoList = visaDao.listVisaReport(startDate, theDateTo23_59_59(endDate), dateType, dateMethod,
					regionId, adviserId, adviserIdList);
			if (visaReportDoList == null)
				return null;
			visaReportDoList
					.forEach(visaReportDo -> visaReportDtoList.add(mapper.map(visaReportDo, VisaReportDTO.class)));
			return visaReportDtoList;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
	}

	@Override
	public VisaDTO getVisaById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		VisaDTO visaDto = null;
		try {
			VisaDO visaDo = visaDao.getVisaById(id);
			if (visaDo == null) {
				return null;
			}
			visaDto = putVisaDTO(visaDo);
			/*
			visaDto = mapper.map(visaDo, VisaDTO.class);
			putReviews(visaDto);
			if (visaDto.getUserId() > 0) {
				UserDO userDo = userDao.getUserById(visaDto.getUserId());
				visaDto.setUserName(userDo.getName());
				visaDto.setPhone(userDo.getPhone());
				visaDto.setBirthday(userDo.getBirthday());
			}
			if (visaDto.getReceiveTypeId() > 0) {
				ReceiveTypeDO receiveTypeDo = receiveTypeDao.getReceiveTypeById(visaDto.getReceiveTypeId());
				if (receiveTypeDo != null)
					visaDto.setReceiveTypeName(receiveTypeDo.getName());
			}
			List<VisaDO> list = visaDao.listVisaByCode(visaDto.getCode());
			if (list != null) {
				double totalPerAmount = 0.00;
				double totalAmount = 0.00;
				for (VisaDO _visaDo : list) {
					totalPerAmount += _visaDo.getPerAmount();
					if (_visaDo.getPaymentVoucherImageUrl1() != null || _visaDo.getPaymentVoucherImageUrl2() != null)
						totalAmount += _visaDo.getAmount();
				}
				visaDto.setTotalPerAmount(totalPerAmount);
				visaDto.setTotalAmount(totalAmount);
			}
			*/
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		return visaDto;
	}

	public VisaDTO putVisaDTO(VisaListDO visaListDo) throws ServiceException {
		VisaDTO visaDto = putVisaDTO((VisaDO) visaListDo);
		if (visaListDo.getApplicantId() > 0) {

			ApplicantDTO applicantDto = mapper.map(applicantDao.getById(visaListDo.getApplicantId()),
					ApplicantDTO.class);

			List<ServiceOrderApplicantDO> serviceOrderApplicantDoList = serviceOrderApplicantDao
					.list(visaListDo.getServiceOrderId(), visaListDo.getApplicantId());
			if (serviceOrderApplicantDoList != null && serviceOrderApplicantDoList.size() > 0
					&& serviceOrderApplicantDoList.get(0) != null) {
				applicantDto.setUrl(serviceOrderApplicantDoList.get(0).getUrl());
				applicantDto.setContent(serviceOrderApplicantDoList.get(0).getContent());
			}

			visaDto.setApplicant(applicantDto);
			visaDto.setApplicantId(visaListDo.getApplicantId());
		}
		return visaDto;
	}

	public VisaDTO putVisaDTO(VisaDO visaListDo) throws ServiceException {
		VisaDTO visaDto = mapper.map(visaListDo, VisaDTO.class);
		putReviews(visaDto);
		if (visaDto.getUserId() > 0) {
			UserDO userDo = userDao.getUserById(visaDto.getUserId());
			visaDto.setUserName(userDo.getName());
			visaDto.setPhone(userDo.getPhone());
			visaDto.setBirthday(userDo.getBirthday());
			visaDto.setUser(mapper.map(userDo, UserDTO.class));
		}
//			ServiceOrderDO serviceOrderDo = serviceOrderDao.getServiceOrderById(visaListDo.getServiceOrderId());
//			if (serviceOrderDo != null && StringUtil.isNotEmpty(serviceOrderDo.getRefuseReason()))
//				visaDto.setRefuseReason(serviceOrderDo.getRefuseReason());
		AdviserDO adviserDo = adviserDao.getAdviserById(visaListDo.getAdviserId());
		if (adviserDo != null) {
			visaDto.setAdviserName(adviserDo.getName());
		}
		OfficialDO officialDo = officialDao.getOfficialById(visaListDo.getOfficialId());
		if (officialDo != null) {
			visaDto.setOfficialName(officialDo.getName());
		}
		ReceiveTypeDO receiveTypeDo = receiveTypeDao.getReceiveTypeById(visaListDo.getReceiveTypeId());
		if (receiveTypeDo != null) {
			visaDto.setReceiveTypeName(receiveTypeDo.getName());
		}
		ServiceDO serviceDo = serviceDao.getServiceById(visaListDo.getServiceId());
		if (serviceDo != null) {
			visaDto.setServiceCode(serviceDo.getCode());
		}
		//List<Date> remindDateList = new ArrayList<>();
		//List<RemindDO> remindDoList = remindDao.listRemindByVisaId(visaDto.getId(), adviserId,
		//		AbleStateEnum.ENABLED.toString());
		//for (RemindDO remindDo : remindDoList) {
		//	remindDateList.add(remindDo.getRemindDate());
		//}
		//visaDto.setRemindDateList(remindDateList);
		List<VisaDO> list = visaDao.listVisaByCode(visaDto.getCode());
		if (list != null) {
			double totalPerAmount = 0.00;
			double totalAmount = 0.00;
			for (VisaDO visaDo : list) {
				totalPerAmount += visaDo.getPerAmount();
				if (visaDo.getPaymentVoucherImageUrl1() != null || visaDo.getPaymentVoucherImageUrl2() != null
						|| visaDo.getPaymentVoucherImageUrl3() != null
						|| visaDo.getPaymentVoucherImageUrl4() != null
						|| visaDo.getPaymentVoucherImageUrl5() != null)
					totalAmount += visaDo.getAmount();
			}
			visaDto.setTotalPerAmount(totalPerAmount);
			visaDto.setTotalAmount(totalAmount);
		}

		//List<MailRemindDO> mailRemindDOS = mailRemindDAO.list(null,null,null,null,visaDto.getId(),null,null,false,true);
		//if (mailRemindDOS.size() > 0){
		//	List<MailRemindDTO> mailRemindDTOS = new ArrayList<>();
		//	mailRemindDOS.forEach(mailRemindDO ->{
		//		mailRemindDTOS.add(mapper.map(mailRemindDO,MailRemindDTO.class));
		//	});
		//	visaDto.setMailRemindDTOS(mailRemindDTOS);
		//}
		
		// 是否退款
		RefundDO refundDo = refundDao.getRefundByVisaId(visaListDo.getId());
		visaDto.setRefunded(refundDo != null && StringUtil.equals("PAID", refundDo.getState()));
		
		// 汇率币种计算金额
		Double exchangeRate = visaDto.getExchangeRate();
		if ("AUD".equalsIgnoreCase(visaDto.getCurrency())) {
			visaDto.setAmountAUD(visaDto.getAmount());
			visaDto.setAmountCNY(roundHalfUp2(visaDto.getAmount() * exchangeRate));
			visaDto.setPerAmountAUD(visaDto.getPerAmount());
			visaDto.setPerAmountCNY(roundHalfUp2(visaDto.getPerAmount() * exchangeRate));
			visaDto.setTotalAmountAUD(visaDto.getAmountAUD());
			visaDto.setTotalAmountCNY(roundHalfUp2(visaDto.getAmountAUD() * exchangeRate));
			visaDto.setTotalPerAmountAUD(visaDto.getTotalPerAmount());
			visaDto.setTotalPerAmountCNY(roundHalfUp2(visaDto.getTotalPerAmount() * exchangeRate));
			visaDto.setExpectAmountAUD(visaDto.getExpectAmount());
			visaDto.setExpectAmountCNY(roundHalfUp2(visaDto.getExpectAmount() * exchangeRate));
			visaDto.setSureExpectAmountAUD(visaDto.getSureExpectAmount());
			visaDto.setSureExpectAmountCNY(roundHalfUp2(visaDto.getSureExpectAmount() * exchangeRate));
			visaDto.setDiscountAUD(visaDto.getDiscount());
			visaDto.setGstAUD(visaDto.getGst());
			visaDto.setDeductGstAUD(visaDto.getDeductGst());
			visaDto.setBonusAUD(visaDto.getBonus());
		}
		if ("CNY".equalsIgnoreCase(visaDto.getCurrency())) {
			visaDto.setAmountAUD(roundHalfUp2(visaDto.getAmount() / exchangeRate));
			visaDto.setAmountCNY(visaDto.getAmount());
			visaDto.setPerAmountAUD(roundHalfUp2(visaDto.getPerAmount() / exchangeRate));
			visaDto.setPerAmountCNY(visaDto.getPerAmount());
			visaDto.setTotalAmountAUD(roundHalfUp2(visaDto.getAmount() / exchangeRate));
			visaDto.setTotalAmountCNY(visaDto.getAmount());
			visaDto.setTotalPerAmountAUD(roundHalfUp2(visaDto.getTotalPerAmount() / exchangeRate));
			visaDto.setTotalPerAmountCNY(visaDto.getTotalPerAmount());
			visaDto.setExpectAmountAUD(roundHalfUp2(visaDto.getExpectAmount() / exchangeRate));
			visaDto.setExpectAmountCNY(visaDto.getExpectAmount());
			visaDto.setSureExpectAmountAUD(roundHalfUp2(visaDto.getSureExpectAmount() / exchangeRate));
			visaDto.setSureExpectAmountCNY(visaDto.getSureExpectAmount());
			visaDto.setDiscountAUD(roundHalfUp2(visaDto.getDiscount() / exchangeRate));
			visaDto.setGstAUD(roundHalfUp2(visaDto.getGst() / exchangeRate));
			visaDto.setDeductGstAUD(roundHalfUp2(visaDto.getDeductGst() / exchangeRate));
			visaDto.setBonusAUD(roundHalfUp2(visaDto.getBonus() / exchangeRate));
		}

		return visaDto;
	}

	@Override
	public VisaDTO getFirstVisaByServiceOrderId(int serviceOrderId) throws ServiceException {
		if (serviceOrderId <= 0) {
			ServiceException se = new ServiceException("serviceOrderId error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		VisaDTO visaDto = null;
		try {
			VisaDO visaDo = visaDao.getFirstVisaByServiceOrderId(serviceOrderId);
			if (visaDo == null)
				return null;
			visaDto = mapper.map(visaDo, VisaDTO.class);
			putReviews(visaDto);
			if (visaDto.getUserId() > 0) {
				UserDO userDo = userDao.getUserById(visaDto.getUserId());
				visaDto.setUserName(userDo.getName());
				visaDto.setPhone(userDo.getPhone());
				visaDto.setBirthday(userDo.getBirthday());
			}
			if (visaDto.getReceiveTypeId() > 0) {
				ReceiveTypeDO receiveTypeDo = receiveTypeDao.getReceiveTypeById(visaDto.getReceiveTypeId());
				if (receiveTypeDo != null)
					visaDto.setReceiveTypeName(receiveTypeDo.getName());
			}
			List<VisaDO> list = visaDao.listVisaByCode(visaDto.getCode());
			if (list != null) {
				double totalPerAmount = 0.00;
				double totalAmount = 0.00;
				for (VisaDO _visaDo : list) {
					totalPerAmount += _visaDo.getPerAmount();
					if (_visaDo.getPaymentVoucherImageUrl1() != null || _visaDo.getPaymentVoucherImageUrl2() != null)
						totalAmount += _visaDo.getAmount();
				}
				visaDto.setTotalPerAmount(totalPerAmount);
				visaDto.setTotalAmount(totalAmount);
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		return visaDto;
	}

	@Override
	public int deleteVisaById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			return visaDao.deleteVisaById(id);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Deprecated
	private void putReviews(VisaDTO visaDto) throws ServiceException {
//		List<ServiceOrderReviewDO> serviceOrderReviewDoList = serviceOrderReviewDao
//				.listServiceOrderReview(visaDto.getServiceOrderId(), null, null, null, null, null);
//		List<ServiceOrderReviewDTO> serviceOrderReviewDtoList = new ArrayList<ServiceOrderReviewDTO>();
//		serviceOrderReviewDoList
//				.forEach(review -> serviceOrderReviewDtoList.add(mapper.map(review, ServiceOrderReviewDTO.class)));
//		if (serviceOrderReviewDtoList != null && serviceOrderReviewDtoList.size() > 0)
//			for (ServiceOrderReviewDTO serviceOrderReviewDto : serviceOrderReviewDtoList)
//				if (serviceOrderReviewDto != null && StringUtil.isNotEmpty(serviceOrderReviewDto.getKjState())) {
//					visaDto.setState(serviceOrderReviewDto.getKjState());
//					visaDao.updateVisa(mapper.map(visaDto, VisaDO.class));
//					break;
//				}
	}

	@Override
	public int addComment(VisaCommentDTO visaCommentDto) throws ServiceException {
		if (visaCommentDto == null) {
			ServiceException se = new ServiceException("visaCommentDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			VisaCommentDO visaCommentDo = mapper.map(visaCommentDto, VisaCommentDO.class);
			if (visaCommentDao.add(visaCommentDo) > 0) {
				visaCommentDto.setId(visaCommentDo.getId());
				return visaCommentDo.getId();
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
	public List<VisaCommentDTO> listComment(int id) throws ServiceException {
		List<VisaCommentDTO> visaCommentDtoList = new ArrayList<>();
		List<VisaCommentDO> visaCommentDoList = new ArrayList<>();
		try {
			visaCommentDoList = visaCommentDao.list(id);
			if (visaCommentDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (VisaCommentDO visaCommentDo : visaCommentDoList) {
			VisaCommentDTO visaCommentDto = mapper.map(visaCommentDo, VisaCommentDTO.class);
			AdminUserDO adminUserDo = adminUserDao.getAdminUserById(visaCommentDo.getAdminUserId());
			if (adminUserDo != null)
				visaCommentDto.setAdminUserName(adminUserDo.getUsername());
			visaCommentDtoList.add(visaCommentDto);
		}
		return visaCommentDtoList;
	}

	@Override
	public int deleteComment(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			return visaCommentDao.delete(id);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	@Synchronized
	public void sendRefuseEmail(VisaDTO visaDto) {
		AdviserDO adviserDo = adviserDao.getAdviserById(visaDto.getAdviserId());
//		OfficialDO officialDo = officialDao.getOfficialById(visaDo.getOfficialId());
		// 发送给顾问
		sendMail(adviserDo.getEmail(), "签证佣金订单驳回提醒", StringUtil.merge("亲爱的:", adviserDo.getName(), "<br/>",
				"您的订单已被驳回。<br>签证订单号:", visaDto.getId(), "<br/>驳回原因:", visaDto.getRefuseReason()));
		// 发送给文案
//		SendEmailUtil.send(officialDo.gemail(), "签证佣金订单驳回提醒", StringUtil.merge("亲爱的:", officialDo.getName(), "<br/>",
//				"您的订单已被驳回。<br>签证订单号:", visaDo.getId(), "<br/>驳回原因:", visaDo.getRefuseReason()));
	}
	@Override
	public List<VisaDTO> getCommissionOrder(Integer officialId,Integer regionId, Integer id,String startHandlingDate,String endHandlingDate,  String commissionState, String startSubmitIbDate, String endSubmitIbDate, String startDate, String endDate, String userName,String applicantName,Integer pageNum, Integer pageSize) {
		if (pageNum!=null&&pageNum < 0) {
			pageNum = DEFAULT_PAGE_NUM;
		}
		if (pageSize!=null&&pageSize < 0) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		Integer offset = null ;
		if(pageNum!=null&&pageSize!=null){
			offset = pageNum * pageSize;
		}

		List<VisaListDO> list = visaDao.get(officialId,regionId, id, startHandlingDate,endHandlingDate, commissionState, theDateTo00_00_00(startSubmitIbDate), theDateTo23_59_59(endSubmitIbDate), theDateTo00_00_00(startDate),
				theDateTo23_59_59(endDate),userName, applicantName,offset, pageSize);
		List<VisaDTO> visaDtoList = new ArrayList<>();
		if(list==null||list.size()==0){
			return null;
		}
		for (VisaListDO visaListDo : list) {
			VisaDTO visaDto = null;
			try {
				visaDto = putVisaDTO(visaListDo);
			} catch (ServiceException e) {
				e.printStackTrace();
			}

			List<Date> remindDateList = new ArrayList<>();
			List<RemindDO> remindDoList = remindDao.listRemindByVisaId(visaDto.getId(), officialId,
					AbleStateEnum.ENABLED.toString());
			ServiceOrderDO serviceOrderDO = serviceOrderDao.getServiceOrderById(visaDto.getServiceOrderId());
			serviceOrderDO.setService(mapper.map(serviceDao.getServiceById(serviceOrderDO.getServiceId()),ServiceDTO.class));
			visaDto.setServiceOrder(mapper.map(serviceOrderDO,ServiceOrderDTO.class));
			visaDto.setServicePackagePriceDO(servicePackagePriceDAO.getByServiceId(visaDto.getServiceId()));
			MaraDO mara = maraDAO.getMaraById(visaDto.getMaraId());
			if(mara!=null){
			visaDto.setMaraDTO(mapper.map(maraDAO.getMaraById(visaDto.getMaraId()),MaraDTO.class));
			}
			for (RemindDO remindDo : remindDoList) {
				remindDateList.add(remindDo.getRemindDate());
			}
			visaDto.setRemindDateList(remindDateList);
			visaDtoList.add(visaDto);
		}
		for (VisaDTO adviserRateCommissionOrderDO : visaDtoList) {
			BigDecimal receivedAUD = BigDecimal.valueOf(adviserRateCommissionOrderDO.getTotalAmountAUD());//总计实收澳币
			BigDecimal refund = BigDecimal.valueOf(adviserRateCommissionOrderDO.getRefund());//退款
			ServicePackagePriceDO servicePackagePriceDO = adviserRateCommissionOrderDO.getServicePackagePriceDO();
			if(servicePackagePriceDO!=null){
				//第三方费用
				Double third_prince1 = servicePackagePriceDO.getThird_prince()==null?0.0:servicePackagePriceDO.getThird_prince();
				BigDecimal third_prince = BigDecimal.valueOf(third_prince1);
				//预计计入佣金金额 总计实收-退款-第三方费用
				BigDecimal amount = receivedAUD.subtract(refund).subtract(third_prince);
				double receivedAUDDouble = receivedAUD.doubleValue();
				double amountDouble = amount.doubleValue();
				if (receivedAUDDouble == 0 || amountDouble < 0) {
					amount = new BigDecimal(adviserRateCommissionOrderDO.getServicePackagePriceDO().getMinPrice()).
							subtract(new BigDecimal(adviserRateCommissionOrderDO.getServicePackagePriceDO().getCost_prince()));
					amountDouble = amount.doubleValue();
				}
				adviserRateCommissionOrderDO.setExpectCommissionAmount(amountDouble);
			}

		}

		return visaDtoList;
	}

	@Override
	public int count(Integer officialId,Integer regionId, Integer id,String startHandlingDate,String endHandlingDate, String commissionState, String startSubmitIbDate, String endSubmitIbDate, String startDate, String endDate,String userName,String applicantName) {

		return visaDao.count(officialId,regionId,id,startHandlingDate,endHandlingDate,commissionState,startSubmitIbDate,endSubmitIbDate,startDate,endDate, userName, applicantName);
	}

}
