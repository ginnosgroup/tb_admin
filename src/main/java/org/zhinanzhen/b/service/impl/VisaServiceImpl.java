package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.*;
import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.service.AbleStateEnum;
import org.zhinanzhen.b.service.VisaService;
import org.zhinanzhen.b.service.pojo.ServiceOrderReviewDTO;
import org.zhinanzhen.b.service.pojo.VisaCommentDTO;
import org.zhinanzhen.b.service.pojo.VisaDTO;
import org.zhinanzhen.b.service.pojo.VisaReportDTO;
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

@Service("VisaService")
public class VisaServiceImpl extends BaseService implements VisaService {

	@Resource
	private VisaDAO visaDao;

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
	private ServiceOrderDAO serviceOrderDao;

	@Resource
	private ServiceOrderReviewDAO serviceOrderReviewDao;

	@Resource
	private VisaCommentDAO visaCommentDao;

	@Resource
	private AdminUserDAO adminUserDao;

	@Resource
	CommissionOrderDAO commissionOrderDAO;

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
			if (commissionOrderDOS.size() > 0) {
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
			String endKjApprovalDate, String startDate, String endDate, List<Integer> regionIdList, Integer adviserId,
			Integer userId, String state) throws ServiceException {
		return visaDao.countVisa(id, keyword, startHandlingDate, theDateTo23_59_59(endHandlingDate), stateList,
				commissionStateList, startKjApprovalDate, theDateTo23_59_59(endKjApprovalDate), startDate,
				theDateTo23_59_59(endDate), regionIdList, adviserId, userId, state);
	}

	@Override
	public List<VisaDTO> listVisa(Integer id, String keyword, String startHandlingDate, String endHandlingDate,
			List<String> stateList, List<String> commissionStateList, String startKjApprovalDate,
			String endKjApprovalDate, String startDate, String endDate, List<Integer> regionIdList, Integer adviserId,
			Integer userId, String state, int pageNum, int pageSize) throws ServiceException {
		if (pageNum < 0) {
			pageNum = DEFAULT_PAGE_NUM;
		}
		if (pageSize < 0) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		List<VisaDTO> visaDtoList = new ArrayList<>();
		List<VisaListDO> visaListDoList = new ArrayList<>();
		try {
			visaListDoList = visaDao.listVisa(id, keyword, startHandlingDate, theDateTo23_59_59(endHandlingDate),
					stateList, commissionStateList, startKjApprovalDate, theDateTo23_59_59(endKjApprovalDate),
					startDate, theDateTo23_59_59(endDate), regionIdList, adviserId, userId, state, pageNum * pageSize,
					pageSize);
			if (visaListDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (VisaDO visaListDo : visaListDoList) {
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
			}
			List<Date> remindDateList = new ArrayList<>();
			List<RemindDO> remindDoList = remindDao.listRemindByVisaId(visaDto.getId(), adviserId,
					AbleStateEnum.ENABLED.toString());
			for (RemindDO remindDo : remindDoList) {
				remindDateList.add(remindDo.getRemindDate());
			}
			visaDto.setRemindDateList(remindDateList);
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
			visaDtoList.add(visaDto);
		}
		return visaDtoList;
	}

	@Override
	public List<VisaReportDTO> listVisaReport(String startDate, String endDate, String dateType, String dateMethod,
			Integer regionId, Integer adviserId) throws ServiceException {
		List<VisaReportDO> visaReportDoList = new ArrayList<>();
		List<VisaReportDTO> visaReportDtoList = new ArrayList<>();
		try {
			visaReportDoList = visaDao.listVisaReport(startDate, theDateTo23_59_59(endDate), dateType, dateMethod,
					regionId, adviserId);
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

	private void putReviews(VisaDTO visaDto) throws ServiceException {
		List<ServiceOrderReviewDO> serviceOrderReviewDoList = serviceOrderReviewDao
				.listServiceOrderReview(visaDto.getServiceOrderId(), null, null, null, null, null);
		List<ServiceOrderReviewDTO> serviceOrderReviewDtoList = new ArrayList<ServiceOrderReviewDTO>();
		serviceOrderReviewDoList
				.forEach(review -> serviceOrderReviewDtoList.add(mapper.map(review, ServiceOrderReviewDTO.class)));
		if (serviceOrderReviewDtoList != null && serviceOrderReviewDtoList.size() > 0)
			for (ServiceOrderReviewDTO serviceOrderReviewDto : serviceOrderReviewDtoList)
				if (serviceOrderReviewDto != null && StringUtil.isNotEmpty(serviceOrderReviewDto.getKjState())) {
					visaDto.setState(serviceOrderReviewDto.getKjState());
					visaDao.updateVisa(mapper.map(visaDto, VisaDO.class));
					break;
				}
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

}
