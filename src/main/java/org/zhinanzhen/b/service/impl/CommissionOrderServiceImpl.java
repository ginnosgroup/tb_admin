package org.zhinanzhen.b.service.impl;

import com.ikasoa.core.ErrorCodeEnum;
import com.ikasoa.core.utils.ListUtil;
import com.ikasoa.core.utils.ObjectUtil;
import com.ikasoa.core.utils.StringUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhinanzhen.b.controller.BaseCommissionOrderController.ReviewKjStateEnum;
import org.zhinanzhen.b.dao.*;
import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.service.CommissionOrderService;
import org.zhinanzhen.b.service.pojo.*;
import org.zhinanzhen.b.service.pojo.ant.Sorter;
import org.zhinanzhen.tb.dao.AdminUserDAO;
import org.zhinanzhen.tb.dao.AdviserDAO;
import org.zhinanzhen.tb.dao.UserDAO;
import org.zhinanzhen.tb.dao.pojo.AdminUserDO;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
import org.zhinanzhen.tb.dao.pojo.UserDO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;
import org.zhinanzhen.tb.service.pojo.AdviserDTO;
import org.zhinanzhen.tb.service.pojo.UserDTO;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service("CommissionOrderService")
public class CommissionOrderServiceImpl extends BaseService implements CommissionOrderService {

    @Resource
    private UserDAO userDao;

    @Resource
    private ApplicantDAO applicantDao;

    @Resource
    private ServiceOrderApplicantDAO serviceOrderApplicantDao;

    @Resource
    private SchoolDAO schoolDao;

    @Resource
    private CommissionOrderDAO commissionOrderDao;

    @Resource
    private RefundDAO refundDao;

//	@Resource
//	private ServiceOrderReviewDAO serviceOrderReviewDao;

    @Resource
    private SubagencyDAO subagencyDao;

    @Resource
    private AdviserDAO adviserDao;

    @Resource
    private OfficialDAO officialDao;

    @Resource
    private ReceiveTypeDAO receiveTypeDao;

    @Resource
    private ServiceDAO serviceDao;

    @Resource
    private CommissionOrderCommentDAO commissionOrderCommentDao;

    @Resource
    private AdminUserDAO adminUserDao;

    @Resource
    private VisaDAO visaDao;

    @Resource
    private MailRemindDAO mailRemindDAO;

    @Resource
    private CommissionOrderTempDAO commissionOrderTempDao;

    @Resource
    private SchoolCourseDAO schoolCourseDAO;

    @Resource
    private SchoolInstitutionLocationDAO schoolInstitutionLocationDAO;

    @Resource
    private InvoiceDAO invoiceDAO;

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public int addCommissionOrder(CommissionOrderDTO commissionOrderDto) throws ServiceException {
        if (commissionOrderDto == null) {
            ServiceException se = new ServiceException("commissionOrderDto is null !");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        if (commissionOrderDto.getVerifyCode() != null) {
            List<CommissionOrderTempDO> list = commissionOrderTempDao.getCommissionOrderTempByVerifyCode(commissionOrderDto.getVerifyCode());
            List<CommissionOrderDO> commissionOrderDOS = commissionOrderDao.listCommissionOrderByVerifyCode(commissionOrderDto.getVerifyCode());
            List<VisaDO> visaDOS = visaDao.listVisaByVerifyCode(commissionOrderDto.getVerifyCode());
            if (commissionOrderDOS.size() > 0 || visaDOS.size() > 0) {
                ServiceException se = new ServiceException("对账code:" + commissionOrderDto.getVerifyCode() + "已经存在,请重新创建新的code!");
                se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
                throw se;
            }
            if (list.size() > 0) {
                for (CommissionOrderTempDO temp : list) {
                    if (temp.getServiceOrderId() != commissionOrderDto.getServiceOrderId()) {
                        ServiceException se = new ServiceException("对账code:" + commissionOrderDto.getVerifyCode() + "已经存在,请重新创建新的code!");
                        se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
                        throw se;
                    }
                }
            }
        }
        try {
            CommissionOrderDO commissionOrderDo = mapper.map(commissionOrderDto, CommissionOrderDO.class);
//			List<ServiceOrderReviewDO> serviceOrderReviews = serviceOrderReviewDao
//					.listServiceOrderReview(commissionOrderDo.getServiceOrderId(), null, null, null, null, null);
//			if (serviceOrderReviews == null || serviceOrderReviews.size() == 0) {
//				ServiceException se = new ServiceException("服务订单需要审核后才能创建佣金订单!");
//				se.setCode(ErrorCodeEnum.DATA_ERROR.code());
//				throw se;
//			}
            if (commissionOrderDao.countCommissionOrderByServiceOrderIdAndExcludeCode(
                    commissionOrderDo.getServiceOrderId(), commissionOrderDo.getCode()) > 0) {
                ServiceException se = new ServiceException("已创建过佣金订单,不能重复创建!");
                se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
                throw se;
            }
            if (commissionOrderDao.addCommissionOrder(commissionOrderDo) > 0) {
                commissionOrderDto.setId(commissionOrderDo.getId());
                String invoiceNumber = commissionOrderDo.getInvoiceNumber();
//				ServiceOrderReviewDO serviceOrderReviewDo = serviceOrderReviews.get(0);
//				serviceOrderReviewDo.setCommissionOrderId(commissionOrderDo.getId());
//				serviceOrderReviewDao.addServiceOrderReview(serviceOrderReviewDo);
                if (commissionOrderDo.isSettle() && StringUtil.isNotEmpty(invoiceNumber)
                        && commissionOrderDo.getInstallmentNum() == 1) {// 提前扣拥的第一笔单子可能需要绑定一下发票
                    invoiceDAO.insertCommissionOrderIdInInvoice(String.valueOf(commissionOrderDo.getId()), invoiceNumber);
                    invoiceDAO.updateScDescCommissionOrderByInvoiceNo(invoiceNumber, commissionOrderDo.getId());
                }
                return commissionOrderDo.getId();
            } else
                return 0;
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }

    @Override
    public int countCommissionOrder(Integer id, List<Integer> regionIdList, Integer maraId, Integer adviserId,
                                    Integer officialId, Integer userId, String name, String applicantName, String phone, String wechatUsername,
                                    Integer schoolId, Boolean isSettle, List<String> stateList, List<String> commissionStateList,
                                    String startKjApprovalDate, String endKjApprovalDate, String startDate, String endDate,
                                    String startInvoiceCreate, String endInvoiceCreate, Boolean isYzyAndYjy, String applyState)
            throws ServiceException {
        return commissionOrderDao.countCommissionOrder(id, regionIdList, maraId, adviserId, officialId, userId, name, applicantName,
                phone, wechatUsername, schoolId, isSettle, stateList, commissionStateList,
                theDateTo00_00_00(startKjApprovalDate), theDateTo23_59_59(endKjApprovalDate), startDate, endDate,
                startInvoiceCreate, theDateTo23_59_59(endInvoiceCreate), isYzyAndYjy, applyState);
    }

    @Override
    public List<CommissionOrderListDTO> listCommissionOrder(Integer id, List<Integer> regionIdList, Integer maraId,
                                                            Integer adviserId, Integer officialId, Integer userId, String name, String applicantName, String phone,
                                                            String wechatUsername, Integer schoolId, Boolean isSettle, List<String> stateList,
                                                            List<String> commissionStateList, String startKjApprovalDate, String endKjApprovalDate, String startDate,
                                                            String endDate, String startInvoiceCreate, String endInvoiceCreate, Boolean isYzyAndYjy, String applyState,
                                                            int pageNum, int pageSize, Sorter sorter) throws ServiceException {
        if (pageNum < 0) {
            pageNum = DEFAULT_PAGE_NUM;
        }
        if (pageSize < 0) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        String orderBy = "ORDER BY co.gmt_create DESC, co.installment_num ASC";
        if (sorter != null) {
            if (sorter.getId() != null)
                orderBy = StringUtil.merge("ORDER BY ", sorter.getOrderBy("co.id", sorter.getId()));
            if (sorter.getUserName() != null)
                orderBy = StringUtil.merge("ORDER BY ", sorter.getOrderBy("u.name", sorter.getUserName()));
            if (sorter.getAdviserName() != null)
                orderBy = StringUtil.merge("ORDER BY ", sorter.getOrderBy("a.name", sorter.getAdviserName()));
        }
        List<CommissionOrderListDTO> commissionOrderListDtoList = new ArrayList<>();
        List<CommissionOrderListDO> commissionOrderListDoList = new ArrayList<>();
        try {
            commissionOrderListDoList = commissionOrderDao.listCommissionOrder(id, regionIdList, maraId, adviserId,
					officialId, userId, name, applicantName, phone, wechatUsername, schoolId, isSettle, stateList,
					commissionStateList, theDateTo00_00_00(startKjApprovalDate), theDateTo23_59_59(endKjApprovalDate),
					theDateTo00_00_00(startDate), theDateTo23_59_59(endDate), startInvoiceCreate,
					theDateTo23_59_59(endInvoiceCreate), isYzyAndYjy, applyState, pageNum * pageSize, pageSize,
					orderBy);
            if (commissionOrderListDoList == null)
                return null;
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
            throw se;
        }
        commissionOrderListDoList.forEach(commissionOrderListDo -> commissionOrderListDtoList
                .add(buildCommissionOrderListDto(commissionOrderListDo)));
        return commissionOrderListDtoList;
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public int updateCommissionOrder(CommissionOrderDTO commissionOrderDto) throws ServiceException {
        if (commissionOrderDto == null) {
            ServiceException se = new ServiceException("commissionOrderDto is null !");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        if (commissionOrderDto.getVerifyCode() != null) {
            List<CommissionOrderTempDO> list = commissionOrderTempDao.getCommissionOrderTempByVerifyCode(commissionOrderDto.getVerifyCode());
            List<CommissionOrderDO> commissionOrderDOS = commissionOrderDao.listCommissionOrderByVerifyCode(commissionOrderDto.getVerifyCode());
            List<VisaDO> visaDOS = visaDao.listVisaByVerifyCode(commissionOrderDto.getVerifyCode());
            if (visaDOS.size() > 0) {
                ServiceException se = new ServiceException("对账code:" + commissionOrderDto.getVerifyCode() + "已经存在,请重新创建新的code!");
                se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
                throw se;
            }
            if (commissionOrderDOS.size() > 0) {
                for (CommissionOrderDO commissionOrderDO : commissionOrderDOS) {
                    if (commissionOrderDO.getId() != commissionOrderDto.getId()) {
                        ServiceException se = new ServiceException("对账code:" + commissionOrderDto.getVerifyCode() + "已经存在,请重新创建新的code!");
                        se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
                        throw se;
                    }
                }
            }
            if (list.size() > 0) {
                for (CommissionOrderTempDO temp : list) {
                    if (temp.getServiceOrderId() != commissionOrderDto.getServiceOrderId()) {
                        ServiceException se = new ServiceException("对账code:" + commissionOrderDto.getVerifyCode() + "已经存在,请重新创建新的code!");
                        se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
                        throw se;
                    }
                }
            }
        }
        try {
            CommissionOrderDO commissionOrderDo = mapper.map(commissionOrderDto, CommissionOrderDO.class);
            // 同步修改同一批佣金订单的顾问和文案
            if (commissionOrderDo.getAdviserId() > 0 || commissionOrderDo.getOfficialId() > 0) {
                CommissionOrderListDO commissionOrderListDo = commissionOrderDao.getCommissionOrderById(commissionOrderDo.getId());
                if (commissionOrderListDo == null) {
                    ServiceException se = new ServiceException("佣金订单ID:" + commissionOrderDo.getId() + ",数据不正确或不存在!");
                    se.setCode(ErrorCodeEnum.DATA_ERROR.code());
                    throw se;
                }
                for (CommissionOrderDO _commissionOrderDo : commissionOrderDao
                        .listCommissionOrderByCode(commissionOrderListDo.getCode())) {
                    if (_commissionOrderDo.getId() != commissionOrderDo.getId() && (ReviewKjStateEnum.PENDING.toString()
                            .equalsIgnoreCase(_commissionOrderDo.getState())
                            || ReviewKjStateEnum.REVIEW.toString().equalsIgnoreCase(_commissionOrderDo.getState())
                            || ReviewKjStateEnum.WAIT.toString().equalsIgnoreCase(_commissionOrderDo.getState()))) {
                        _commissionOrderDo.setAdviserId(commissionOrderDo.getAdviserId());
                        _commissionOrderDo.setOfficialId(commissionOrderDo.getOfficialId());
                        LOG.info("修改留学佣金订单(_commissionOrderDo=" + _commissionOrderDo + ").");
                        commissionOrderDao.updateCommissionOrder(_commissionOrderDo);
                    }
                }
            }
            LOG.info("修改留学佣金订单(commissionOrderDo=" + commissionOrderDo + ").");
            return commissionOrderDao.updateCommissionOrder(commissionOrderDo);
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }

    @Override
    public List<CommissionOrderListDTO> listThisMonthCommissionOrder(Integer adviserId, Integer officialId)
            throws ServiceException {
        List<CommissionOrderListDTO> commissionOrderListDtoList = new ArrayList<>();
        List<CommissionOrderListDO> commissionOrderListDoList = new ArrayList<>();
        try {
            commissionOrderListDoList = commissionOrderDao.listThisMonthCommissionOrderAtDashboard(adviserId,
                    officialId);
            if (commissionOrderListDoList == null)
                return null;
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
            throw se;
        }
        commissionOrderListDoList.forEach(commissionOrderListDo -> commissionOrderListDtoList
                .add(buildCommissionOrderListDto(commissionOrderListDo)));
        return commissionOrderListDtoList;
    }

    @Override
    public CommissionOrderListDTO getCommissionOrderById(int id) throws ServiceException {
        if (id <= 0) {
            ServiceException se = new ServiceException("id error !");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        CommissionOrderListDTO commissionOrderListDto = null;
        try {
            CommissionOrderListDO commissionOrderListDo = commissionOrderDao.getCommissionOrderById(id);
            if (commissionOrderListDo == null)
                return null;
            commissionOrderListDto = buildCommissionOrderListDto(commissionOrderListDo);
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
        return commissionOrderListDto;
    }

    @Override
    public List<CommissionInfoDTO> getCommissionInfoById(int id, int adviserId) throws ServiceException {
        if (id <= 0) {
            ServiceException se = new ServiceException("id error !");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        List<CommissionInfoDTO> commissionInfoDTOS = null;
        try {
            List<CommissionInfoDO> commissionInfoDOList = commissionOrderDao.getCommissionInfoById(id, adviserId);
            if (commissionInfoDOList.size() == 0) {
                ServiceException se = new ServiceException("没有找到相应的佣金记录");
                se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
                throw se;
            }
            commissionInfoDTOS = buildCommissionInfoDto(commissionInfoDOList);
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
        return commissionInfoDTOS;

    }

    private List<CommissionInfoDTO> buildCommissionInfoDto(List<CommissionInfoDO> commissionInfoDO) {
        List<CommissionInfoDTO> commissionInfoDTOList = new ArrayList<>();
        CommissionInfoDTO commissionInfoDTO = new CommissionInfoDTO();
        int installment = commissionInfoDO.get(0).getInstallment();
        for (int i = 0; i < installment; i++) {
            commissionInfoDTO = new CommissionInfoDTO();
            DecimalFormat df = new DecimalFormat("0.00");
            commissionInfoDTO.setServiceOrderId(commissionInfoDO.get(i).getServiceOrderId());
            commissionInfoDTO.setCommissionorderid(commissionInfoDO.get(i).getId());
            commissionInfoDTO.setState(commissionInfoDO.get(i).getState());
            commissionInfoDTO.setInstallment(commissionInfoDO.get(i).getInstallment());
            commissionInfoDTO.setInstallmentNum(commissionInfoDO.get(i).getInstallmentNum());
            commissionInfoDTO.setState(commissionInfoDO.get(i).getState());
            commissionInfoDTO.setStartDate(commissionInfoDO.get(i).getStartDate());
            commissionInfoDTO.setEndDate(commissionInfoDO.get(i).getEndDate());
            commissionInfoDTO.setTuitionFee(df.format(commissionInfoDO.get(i).getTuitionFee()));
            commissionInfoDTO.setPerTermTuitionFee(df.format(commissionInfoDO.get(i).getPerTermTuitionFee()));
            commissionInfoDTO.setInstallmentDueDate(commissionInfoDO.get(i).getInstallmentDueDate());
            commissionInfoDTO.setStudentCode(commissionInfoDO.get(i).getStudentCode());
            commissionInfoDTOList.add(commissionInfoDTO);
        }

        return commissionInfoDTOList;
    }

    @Override
    public List<CommissionOrderListDTO> listCommissionOrderByInvoiceNumber(String invoiceNumber)
            throws ServiceException {
        if (StringUtil.isEmpty(invoiceNumber)) {
            ServiceException se = new ServiceException("invoiceNumber error !");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        List<CommissionOrderListDTO> commissionOrderListDtoList = new ArrayList<>();
        try {
            List<CommissionOrderListDO> commissionOrderListDoList = commissionOrderDao
                    .listCommissionOrderByInvoiceNumber(invoiceNumber);
            if (commissionOrderListDoList == null)
                return null;
            commissionOrderListDoList.forEach(commissionOrderListDo -> commissionOrderListDtoList
                    .add(buildCommissionOrderListDto(commissionOrderListDo)));
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
        return commissionOrderListDtoList;
    }

    @Override
    public List<CommissionOrderReportDTO> listCommissionOrderReport(String startDate, String endDate, String dateType,
                                                                    String dateMethod, Integer regionId, Integer adviserId, List<String> adviserIdList) throws ServiceException {
        List<CommissionOrderReportDO> commissionOrderReportDoList = new ArrayList<>();
        List<CommissionOrderReportDTO> commissionOrderReportDtoList = new ArrayList<>();
        try {
            commissionOrderReportDoList = commissionOrderDao.listCommissionOrderReport(startDate,
                    theDateTo23_59_59(endDate), dateType, dateMethod, regionId, adviserId, adviserIdList);
            if (commissionOrderReportDoList == null)
                return null;
            commissionOrderReportDoList.forEach(commissionOrderReportDo -> commissionOrderReportDtoList
                    .add(mapper.map(commissionOrderReportDo, CommissionOrderReportDTO.class)));
            return commissionOrderReportDtoList;
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
            throw se;
        }
    }

    @Override
    public CommissionOrderListDTO getFirstCommissionOrderByServiceOrderId(int serviceOrderId) throws ServiceException {
        if (serviceOrderId <= 0) {
            ServiceException se = new ServiceException("serviceOrderId error !");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        CommissionOrderListDTO commissionOrderListDto = null;
        try {
            CommissionOrderListDO commissionOrderListDo = commissionOrderDao
                    .getFirstCommissionOrderByServiceOrderId(serviceOrderId);
            if (commissionOrderListDo == null)
                return null;
            commissionOrderListDto = buildCommissionOrderListDto(commissionOrderListDo);
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
        return commissionOrderListDto;
    }

    private CommissionOrderListDTO buildCommissionOrderListDto(CommissionOrderListDO commissionOrderListDo) {
        CommissionOrderListDTO commissionOrderListDto = mapper.map(commissionOrderListDo, CommissionOrderListDTO.class);
        if (commissionOrderListDo.getUserId() > 0) {
            UserDO userDo = userDao.getUserById(commissionOrderListDo.getUserId());
            if (userDo != null) {
                commissionOrderListDto.setUser(mapper.map(userDo, UserDTO.class));
                commissionOrderListDto.setBirthday(userDo.getBirthday());
            }
            if (commissionOrderListDo.getApplicantId() > 0) {

                ApplicantDTO applicantDto = mapper.map(applicantDao.getById(commissionOrderListDo.getApplicantId()),
                        ApplicantDTO.class);

                List<ServiceOrderApplicantDO> serviceOrderApplicantDoList = serviceOrderApplicantDao
                        .list(commissionOrderListDo.getServiceOrderId(), commissionOrderListDo.getApplicantId());
                if (serviceOrderApplicantDoList != null && serviceOrderApplicantDoList.size() > 0
                        && serviceOrderApplicantDoList.get(0) != null) {
                    applicantDto.setUrl(serviceOrderApplicantDoList.get(0).getUrl());
                    applicantDto.setContent(serviceOrderApplicantDoList.get(0).getContent());
                }

                commissionOrderListDto.setApplicant(applicantDto);
                commissionOrderListDto.setApplicantId(commissionOrderListDo.getApplicantId());
            }
        }
        if (commissionOrderListDo.getSchoolId() > 0) { // 旧学校库，已废弃
            SchoolDO schoolDo = schoolDao.getSchoolById(commissionOrderListDo.getSchoolId());
            if (schoolDo != null)
                commissionOrderListDto.setSchool(mapper.map(schoolDo, SchoolDTO.class));
        }
        if (commissionOrderListDo.getSubagencyId() > 0) {
            SubagencyDO subagencyDo = subagencyDao.getSubagencyById(commissionOrderListDo.getSubagencyId());
            if (subagencyDo != null)
                commissionOrderListDto.setSubagency(mapper.map(subagencyDo, SubagencyDTO.class));
        }
        if (commissionOrderListDo.getAdviserId() > 0) {
            AdviserDO adviserDo = adviserDao.getAdviserById(commissionOrderListDo.getAdviserId());
            if (adviserDo != null)
                commissionOrderListDto.setAdviser(mapper.map(adviserDo, AdviserDTO.class));
        }
        if (commissionOrderListDo.getOfficialId() > 0) {
            OfficialDO officialDo = officialDao.getOfficialById(commissionOrderListDo.getOfficialId());
            if (officialDo != null)
                commissionOrderListDto.setOfficial(mapper.map(officialDo, OfficialDTO.class));
        }
        if (commissionOrderListDo.getReceiveTypeId() > 0) {
            ReceiveTypeDO receiveTypeDo = receiveTypeDao.getReceiveTypeById(commissionOrderListDo.getReceiveTypeId());
            if (receiveTypeDo != null)
                commissionOrderListDto.setReceiveType(mapper.map(receiveTypeDo, ReceiveTypeDTO.class));
        }
        if (commissionOrderListDo.getServiceId() > 0) {
            ServiceDO serviceDo = serviceDao.getServiceById(commissionOrderListDo.getServiceId());
            if (serviceDo != null)
                commissionOrderListDto.setService(mapper.map(serviceDo, ServiceDTO.class));
        }
        List<CommissionOrderDO> list = commissionOrderDao.listCommissionOrderByCode(commissionOrderListDo.getCode());
        if (list != null) {
            double totalPerAmount = 0.00;
            double totalAmount = 0.00;
            for (CommissionOrderDO commissionOrderDo : list) {
                totalPerAmount += commissionOrderDo.getPerAmount();
                // if (commissionOrderDo.getBonus() > 0)
                if (commissionOrderDo.getPaymentVoucherImageUrl1() != null
                        || commissionOrderDo.getPaymentVoucherImageUrl2() != null)
                    totalAmount += commissionOrderDo.getAmount(); // TODO: sulei
            }
            commissionOrderListDto.setTotalPerAmount(totalPerAmount);
            commissionOrderListDto.setTotalAmount(totalAmount);
        }

        // List<MailRemindDO> mailRemindDOS =
        // mailRemindDAO.list(null,null,null,null,null,commissionOrderListDo.getId(),null,false,true);
        // if (mailRemindDOS.size() > 0){
        // List<MailRemindDTO> mailRemindDTOS = new ArrayList<>();
        // mailRemindDOS.forEach(mailRemindDO ->{
        // mailRemindDTOS.add(mapper.map(mailRemindDO,MailRemindDTO.class));
        // });
        // commissionOrderListDto.setMailRemindDTOS(mailRemindDTOS);
        // }

        // 添加新学校相关
        if (commissionOrderListDo.getCourseId() > 0) {
            SchoolInstitutionListDTO schoolInstitutionInfo = schoolCourseDAO
                    .getSchoolInstitutionInfoByCourseId(commissionOrderListDo.getCourseId());
            if (commissionOrderListDo.getSchoolInstitutionLocationId() > 0 && schoolInstitutionInfo != null) {
                SchoolInstitutionLocationDO schoolInstitutionLocationDO = schoolInstitutionLocationDAO
                        .getById(commissionOrderListDo.getSchoolInstitutionLocationId());
                schoolInstitutionInfo.setSchoolInstitutionLocationDO(schoolInstitutionLocationDO);
            }
            commissionOrderListDto.setSchoolInstitutionListDTO(schoolInstitutionInfo);
        }

        // 是否退款
        RefundDO refundDo = refundDao.getRefundByCommissionOrderId(commissionOrderListDo.getId());
        commissionOrderListDto.setRefunded(refundDo != null && StringUtil.equals("PAID", refundDo.getState()));

        // 汇率币种计算金额
        Double exchangeRate = commissionOrderListDo.getExchangeRate();
        if ("AUD".equalsIgnoreCase(commissionOrderListDo.getCurrency())) {
            commissionOrderListDto.setAmountAUD(commissionOrderListDto.getAmount());
            commissionOrderListDto.setAmountCNY(roundHalfUp2(commissionOrderListDto.getAmount() * exchangeRate));
            commissionOrderListDto.setPerAmountAUD(commissionOrderListDto.getPerAmount());
            commissionOrderListDto.setPerAmountCNY(roundHalfUp2(commissionOrderListDto.getPerAmount() * exchangeRate));
            commissionOrderListDto.setTotalAmountAUD(commissionOrderListDto.getAmountAUD());
            commissionOrderListDto
                    .setTotalAmountCNY(roundHalfUp2(commissionOrderListDto.getAmountAUD() * exchangeRate));
            commissionOrderListDto.setTotalPerAmountAUD(commissionOrderListDto.getTotalPerAmount());
            commissionOrderListDto
                    .setTotalPerAmountCNY(roundHalfUp2(commissionOrderListDto.getTotalPerAmount() * exchangeRate));
            commissionOrderListDto.setExpectAmountAUD(commissionOrderListDto.getExpectAmount());
            commissionOrderListDto
                    .setExpectAmountCNY(roundHalfUp2(commissionOrderListDto.getExpectAmount() * exchangeRate));
            commissionOrderListDto.setSureExpectAmountAUD(commissionOrderListDto.getSureExpectAmount());
            commissionOrderListDto
                    .setSureExpectAmountCNY(roundHalfUp2(commissionOrderListDto.getSureExpectAmount() * exchangeRate));
            commissionOrderListDto.setDiscountAUD(commissionOrderListDto.getDiscount());
            commissionOrderListDto.setGstAUD(commissionOrderListDto.getGst());
            commissionOrderListDto.setDeductGstAUD(commissionOrderListDto.getDeductGst());
            commissionOrderListDto.setBonusAUD(commissionOrderListDto.getBonus());
        }
        if ("CNY".equalsIgnoreCase(commissionOrderListDo.getCurrency())) {
            commissionOrderListDto.setAmountAUD(roundHalfUp2(commissionOrderListDto.getAmount() / exchangeRate));
            commissionOrderListDto.setAmountCNY(commissionOrderListDto.getAmount());
            commissionOrderListDto.setPerAmountAUD(roundHalfUp2(commissionOrderListDto.getPerAmount() / exchangeRate));
            commissionOrderListDto.setPerAmountCNY(commissionOrderListDto.getPerAmount());
            commissionOrderListDto.setTotalAmountAUD(roundHalfUp2(commissionOrderListDto.getAmount() / exchangeRate));
            commissionOrderListDto.setTotalAmountCNY(commissionOrderListDto.getAmount());
            commissionOrderListDto
                    .setTotalPerAmountAUD(roundHalfUp2(commissionOrderListDto.getTotalPerAmount() / exchangeRate));
            commissionOrderListDto.setTotalPerAmountCNY(commissionOrderListDto.getTotalPerAmount());
//            commissionOrderListDto
//                    .setExpectAmountAUD(roundHalfUp2(commissionOrderListDto.getExpectAmount() / exchangeRate));
//            commissionOrderListDto.setExpectAmountCNY(commissionOrderListDto.getExpectAmount());
//			commissionOrderListDto
//					.setSureExpectAmountAUD(roundHalfUp2(commissionOrderListDto.getSureExpectAmount() / exchangeRate));
//			commissionOrderListDto.setSureExpectAmountCNY(commissionOrderListDto.getSureExpectAmount());
//            commissionOrderListDto
//			.setSureExpectAmountAUD(roundHalfUp2(commissionOrderListDto.getSureExpectAmount() / exchangeRate));
//	commissionOrderListDto.setSureExpectAmountCNY(commissionOrderListDto.getSureExpectAmount());
            // 人民币的预收业绩等于本次收款金额澳币 2023-1-27
			commissionOrderListDto.setExpectAmountAUD(commissionOrderListDto.getAmountAUD());
			commissionOrderListDto.setExpectAmountCNY(commissionOrderListDto.getAmount());
			// 留学确认预收业绩等于学校支付澳币金额? 2023-1-27
			commissionOrderListDto
					.setSureExpectAmountAUD(roundHalfUp2(commissionOrderListDto.getSureExpectAmount() / exchangeRate));
			commissionOrderListDto.setSureExpectAmountCNY(commissionOrderListDto.getSureExpectAmount());
			
            commissionOrderListDto.setDiscountAUD(roundHalfUp2(commissionOrderListDto.getDiscount() / exchangeRate));
            commissionOrderListDto.setGstAUD(roundHalfUp2(commissionOrderListDto.getGst() / exchangeRate));
            commissionOrderListDto.setDeductGstAUD(roundHalfUp2(commissionOrderListDto.getDeductGst() / exchangeRate));
            commissionOrderListDto.setBonusAUD(roundHalfUp2(commissionOrderListDto.getBonus() / exchangeRate));
        }

        return commissionOrderListDto;
    }

    @Override
    public int addComment(CommissionOrderCommentDTO commissionOrderCommentDto) throws ServiceException {
        if (commissionOrderCommentDto == null) {
            ServiceException se = new ServiceException("commissionOrderCommentDto is null !");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        try {
            CommissionOrderCommentDO commissionOrderCommentDo = mapper.map(commissionOrderCommentDto,
                    CommissionOrderCommentDO.class);
            if (commissionOrderCommentDao.add(commissionOrderCommentDo) > 0) {
                commissionOrderCommentDto.setId(commissionOrderCommentDo.getId());
                return commissionOrderCommentDo.getId();
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
    public List<CommissionOrderCommentDTO> listComment(int id) throws ServiceException {
        List<CommissionOrderCommentDTO> commissionOrderCommentDtoList = new ArrayList<>();
        List<CommissionOrderCommentDO> commissionOrderCommentDoList = new ArrayList<>();
        try {
            commissionOrderCommentDoList = commissionOrderCommentDao.list(id);
            if (commissionOrderCommentDoList == null)
                return null;
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
            throw se;
        }
        for (CommissionOrderCommentDO commissionOrderCommentDo : commissionOrderCommentDoList) {
            CommissionOrderCommentDTO commissionOrderCommentDto = mapper.map(commissionOrderCommentDo,
                    CommissionOrderCommentDTO.class);
            AdminUserDO adminUserDo = adminUserDao.getAdminUserById(commissionOrderCommentDo.getAdminUserId());
            if (adminUserDo != null)
                commissionOrderCommentDto.setAdminUserName(adminUserDo.getUsername());
            commissionOrderCommentDtoList.add(commissionOrderCommentDto);
        }
        return commissionOrderCommentDtoList;
    }

    @Override
    public int deleteComment(int id) throws ServiceException {
        if (id <= 0) {
            ServiceException se = new ServiceException("id error !");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        try {
            return commissionOrderCommentDao.delete(id);
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
	public int deleteCommissionOrder(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			CommissionOrderListDO currentCommissionOrderListDo = commissionOrderDao.getCommissionOrderById(id);
			List<CommissionOrderDO> commissionOrderList = commissionOrderDao
					.listCommissionOrderByServiceOrderId(currentCommissionOrderListDo.getServiceOrderId());
			for (CommissionOrderDO commissionOrderDo : commissionOrderList) {
				commissionOrderDo.setInstallment(commissionOrderDo.getInstallment() - 1);
				if (commissionOrderDo.getInstallmentNum() > currentCommissionOrderListDo.getInstallmentNum())
					commissionOrderDo.setInstallmentNum(commissionOrderDo.getInstallmentNum() - 1);
				commissionOrderDao.updateCommissionOrder(commissionOrderDo);
			}
			return commissionOrderDao.deleteCommissionOrderById(id);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

    @Override
    public void sendRefuseEmail(int id) {
        CommissionOrderDO commissionOrderDo = commissionOrderDao.getCommissionOrderById(id);
        AdviserDO adviserDo = adviserDao.getAdviserById(commissionOrderDo.getAdviserId());
//		OfficialDO officialDo = officialDao.getOfficialById(commissionOrderDo.getOfficialId());
        // 发送给顾问
        sendMail(adviserDo.getEmail(), "留学佣金订单驳回提醒", StringUtil.merge("亲爱的:", adviserDo.getName(), "<br/>",
                "您的订单已被驳回。<br>订单号:", commissionOrderDo.getId(), "<br/>驳回原因:", commissionOrderDo.getRefuseReason()));
        // 发送给文案
//		SendEmailUtil.send(officialDo.getEmail(), "留学佣金订单驳回提醒", StringUtil.merge("亲爱的:", officialDo.getName(), "<br/>",
//				"您的订单已被驳回。<br>订单号:", commissionOrderDo.getId(), "<br/>驳回原因:", commissionOrderDo.getRefuseReason()));
    }

    @Override
    public int updateCommissionOrderByServiceOrderId(CommissionOrderDTO commissionOrderDto) throws ServiceException {
        if (commissionOrderDto == null) {
            ServiceException se = new ServiceException("commissionOrderDto is null !");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        if (commissionOrderDto.getVerifyCode() != null) {
            List<CommissionOrderTempDO> list = commissionOrderTempDao.getCommissionOrderTempByVerifyCode(commissionOrderDto.getVerifyCode());
            List<CommissionOrderDO> commissionOrderDOS = commissionOrderDao.listCommissionOrderByVerifyCode(commissionOrderDto.getVerifyCode());
            List<VisaDO> visaDOS = visaDao.listVisaByVerifyCode(commissionOrderDto.getVerifyCode());
            if (visaDOS.size() > 0) {
                ServiceException se = new ServiceException("对账code:" + commissionOrderDto.getVerifyCode() + "已经存在,请重新创建新的code!");
                se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
                throw se;
            }
            if (commissionOrderDOS.size() > 0) {
                for (CommissionOrderDO commissionOrderDO : commissionOrderDOS) {
                    if (commissionOrderDO.getId() != commissionOrderDto.getId()) {
                        ServiceException se = new ServiceException("对账code:" + commissionOrderDto.getVerifyCode() + "已经存在,请重新创建新的code!");
                        se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
                        throw se;
                    }
                }
            }
            if (list.size() > 0) {
                for (CommissionOrderTempDO temp : list) {
                    if (temp.getServiceOrderId() != commissionOrderDto.getServiceOrderId()) {
                        ServiceException se = new ServiceException("对账code:" + commissionOrderDto.getVerifyCode() + "已经存在,请重新创建新的code!");
                        se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
                        throw se;
                    }
                }
            }
        }
        try {
            CommissionOrderDO commissionOrderDo = mapper.map(commissionOrderDto, CommissionOrderDO.class);
			/*
			// 同步修改同一批佣金订单的顾问和文案
			if (commissionOrderDo.getAdviserId() > 0 || commissionOrderDo.getOfficialId() > 0) {
				CommissionOrderListDO commissionOrderListDo = commissionOrderDao.getCommissionOrderById(commissionOrderDo.getId());
				if (commissionOrderListDo == null) {
					ServiceException se = new ServiceException("佣金订单ID:" + commissionOrderDo.getId() + ",数据不正确或不存在!");
					se.setCode(ErrorCodeEnum.DATA_ERROR.code());
					throw se;
				}
				for (CommissionOrderDO _commissionOrderDo : commissionOrderDao
						.listCommissionOrderByCode(commissionOrderListDo.getCode())) {
					if (_commissionOrderDo.getId() != commissionOrderDo.getId() && (ReviewKjStateEnum.PENDING.toString()
							.equalsIgnoreCase(_commissionOrderDo.getState())
							|| ReviewKjStateEnum.REVIEW.toString().equalsIgnoreCase(_commissionOrderDo.getState())
							|| ReviewKjStateEnum.WAIT.toString().equalsIgnoreCase(_commissionOrderDo.getState()))) {
						_commissionOrderDo.setAdviserId(commissionOrderDo.getAdviserId());
						_commissionOrderDo.setOfficialId(commissionOrderDo.getOfficialId());
						commissionOrderDao.updateCommissionOrder(_commissionOrderDo);
					}
				}
			}
			 */
            return commissionOrderDao.updateCommissionOrderByServiceOrderId(commissionOrderDo) > 0 ?
                    commissionOrderDo.getId() : 0;
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }

    // temp

    @Override
    public int addCommissionOrderTemp(CommissionOrderTempDTO commissionOrderTempDTO) throws ServiceException {
        if (commissionOrderTempDTO == null) {
            ServiceException se = new ServiceException("commissionOrderTempDTO is null !");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        if (StringUtil.isNotBlank(commissionOrderTempDTO.getVerifyCode())) {
            List<CommissionOrderTempDO> list = commissionOrderTempDao.getCommissionOrderTempByVerifyCode(commissionOrderTempDTO.getVerifyCode());
            List<CommissionOrderDO> commissionOrderDOS = commissionOrderDao.listCommissionOrderByVerifyCode(commissionOrderTempDTO.getVerifyCode());
            List<VisaDO> visaDOS = visaDao.listVisaByVerifyCode(commissionOrderTempDTO.getVerifyCode());
            if (commissionOrderDOS.size() > 0 || visaDOS.size() > 0 || list.size() > 0) {
                ServiceException se = new ServiceException("对账code:" + commissionOrderTempDTO.getVerifyCode() + "已经存在,请重新创建新的code!");
                se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
                throw se;
            }
        }
        try {
            CommissionOrderTempDO commissionOrderTempDO = mapper.map(commissionOrderTempDTO, CommissionOrderTempDO.class);
            int i = commissionOrderTempDao.addCommissionOrderTemp(commissionOrderTempDO);
            if (i > 0) {
                commissionOrderTempDTO.setId(commissionOrderTempDO.getId());
                return commissionOrderTempDTO.getId();
            }
            return 0;
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }

    @Override
    public CommissionOrderTempDTO getCommissionOrderTempByServiceOrderId(int id) throws ServiceException {
        if (id <= 0) {
            ServiceException se = new ServiceException("id is error !");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        try {
            CommissionOrderTempDO commissionOrderTempDO = commissionOrderTempDao.getCommissionOrderTempByServiceOrderId(id);
            if (commissionOrderTempDO == null)
                return null;
            CommissionOrderTempDTO commissionOrderTempDTO = mapper.map(commissionOrderTempDO, CommissionOrderTempDTO.class);

            ReceiveTypeDO receiveTypeDo = receiveTypeDao.getReceiveTypeById(commissionOrderTempDO.getReceiveTypeId());
            if (receiveTypeDo != null)
                commissionOrderTempDTO.setReceiveType(mapper.map(receiveTypeDo, ReceiveTypeDTO.class));

            return putCurrencyDataByCommissionOrderTempDTO(commissionOrderTempDTO);
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }

    @Override
    public int updateCommissionOrderTemp(CommissionOrderTempDTO tempDTO) throws ServiceException {
        if (tempDTO == null) {
            ServiceException se = new ServiceException("CommissionOrderTempDTO is null !");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        if (StringUtil.isNotBlank(tempDTO.getVerifyCode())) {
            List<CommissionOrderTempDO> list = commissionOrderTempDao.getCommissionOrderTempByVerifyCode(tempDTO.getVerifyCode());
            List<CommissionOrderDO> commissionOrderDOS = commissionOrderDao.listCommissionOrderByVerifyCode(tempDTO.getVerifyCode());
            List<VisaDO> visaDOS = visaDao.listVisaByVerifyCode(tempDTO.getVerifyCode());
            if (commissionOrderDOS.size() > 0 || visaDOS.size() > 0) {
                ServiceException se = new ServiceException("对账code:" + tempDTO.getVerifyCode() + "已经存在,请重新创建新的code!");
                se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
                throw se;
            }
            if (list.size() > 0)
                if (list.get(0).getId() != tempDTO.getId()) {//理论上如果存在的话，只会有一个
                    ServiceException se = new ServiceException("对账code:" + tempDTO.getVerifyCode() + "已经存在,请重新创建新的code!");
                    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
                    throw se;
                }

        }
        try {

            CommissionOrderTempDO commissionOrderTempDO = mapper.map(tempDTO, CommissionOrderTempDO.class);
            return commissionOrderTempDao.update(commissionOrderTempDO);
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }

    @Override
    public CommissionOrderTempDTO getCommissionOrderTempById(int id) throws ServiceException {
        if (id <= 0) {
            ServiceException se = new ServiceException("id is error !");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        try {
            CommissionOrderTempDO commissionOrderTempDO = commissionOrderTempDao.getCommissionOrderTempById(id);
            if (commissionOrderTempDO == null)
                return null;
            CommissionOrderTempDTO commissionOrderTempDTO = mapper.map(commissionOrderTempDO, CommissionOrderTempDTO.class);

            ReceiveTypeDO receiveTypeDo = receiveTypeDao.getReceiveTypeById(commissionOrderTempDO.getReceiveTypeId());
            if (receiveTypeDo != null)
                commissionOrderTempDTO.setReceiveType(mapper.map(receiveTypeDo, ReceiveTypeDTO.class));

            return putCurrencyDataByCommissionOrderTempDTO(commissionOrderTempDTO);
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }


    private CommissionOrderTempDTO putCurrencyDataByCommissionOrderTempDTO(
            CommissionOrderTempDTO commissionOrderTempDto) {
        // 汇率币种计算金额
        Double exchangeRate = commissionOrderTempDto.getExchangeRate();
        if ("AUD".equalsIgnoreCase(commissionOrderTempDto.getCurrency())) {
            commissionOrderTempDto.setAmountAUD(commissionOrderTempDto.getAmount());
            commissionOrderTempDto.setAmountCNY(roundHalfUp2(commissionOrderTempDto.getAmount() * exchangeRate));
            commissionOrderTempDto.setPerAmountAUD(commissionOrderTempDto.getPerAmount());
            commissionOrderTempDto.setPerAmountCNY(roundHalfUp2(commissionOrderTempDto.getPerAmount() * exchangeRate));
            commissionOrderTempDto.setExpectAmountAUD(commissionOrderTempDto.getExpectAmount());
            commissionOrderTempDto
                    .setExpectAmountCNY(roundHalfUp2(commissionOrderTempDto.getExpectAmount() * exchangeRate));
            commissionOrderTempDto.setDiscountAUD(commissionOrderTempDto.getDiscount());
        }
        if ("CNY".equalsIgnoreCase(commissionOrderTempDto.getCurrency())) {
            commissionOrderTempDto.setAmountAUD(roundHalfUp2(commissionOrderTempDto.getAmount() / exchangeRate));
            commissionOrderTempDto.setAmountCNY(commissionOrderTempDto.getAmount());
            commissionOrderTempDto.setPerAmountAUD(roundHalfUp2(commissionOrderTempDto.getPerAmount() / exchangeRate));
            commissionOrderTempDto.setPerAmountCNY(commissionOrderTempDto.getPerAmount());
            commissionOrderTempDto
                    .setExpectAmountAUD(roundHalfUp2(commissionOrderTempDto.getExpectAmount() / exchangeRate));
            commissionOrderTempDto.setExpectAmountCNY(commissionOrderTempDto.getExpectAmount());
            commissionOrderTempDto.setDiscountAUD(roundHalfUp2(commissionOrderTempDto.getDiscount() / exchangeRate));
        }
        return commissionOrderTempDto;
    }

    @Override
    public Integer addCommissionInfoById(int id, int installment_num) throws ServiceException {
        if (id <= 0) {
            ServiceException se = new ServiceException("id is error !");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        try {
            commissionOrderDao.addCommissionInfoById(id);
            CommissionInfoDO infoDO = commissionOrderDao.getCommissionStateById(id, installment_num);
            return commissionOrderDao.updateState(infoDO.getId());

        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }

    }

    /*
    根据服务和分期数删除
    */
    @Override
    public int deleteCommissionOrderInfoById(int serviceOrderId, int installmentNum) throws ServiceException {
        if (serviceOrderId <= 0) {
            ServiceException se = new ServiceException("id error !");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        try {
//            CommissionInfoDO infoDO = commissionOrderDao.getCommissionStateById(installmentNum, installmentNum);
//            if (infoDO.getState().equals("PENDING")) {
                return commissionOrderDao.deleteCommissionOrderInfoById(serviceOrderId, installmentNum);
//            }
//            return 1;

        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }


    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public int setinstallmentById(int id, int installment) throws ServiceException {
        if (id <= 0) {
            ServiceException se = new ServiceException("id is error !");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        try {
            return commissionOrderDao.setinstallmentById(id, installment);

        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }

    @Override
    public int setinstallmentDueDateById(int id, int installment_num, Date installmentDueDate) throws ServiceException {
        if (id <= 0) {
            ServiceException se = new ServiceException("id is error !");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        try {
            CommissionInfoDO infoDO = commissionOrderDao.getCommissionStateById(id, installment_num);
            if (infoDO.getState().equals("PENDING")) {
                return commissionOrderDao.setinstallmentDueDateById(id, installment_num, installmentDueDate);
            }
            return 1;

        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }
    
    @Override
    @Transactional(rollbackFor = ServiceException.class)
	public int confirmByInvoiceNo(String invoiceNo) throws ServiceException {
		if (StringUtil.isEmpty(invoiceNo)) {
			ServiceException se = new ServiceException("invoiceNo is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		int i = 0;
		String orderIds = invoiceDAO.getSchoolIdsByInvoiceNo(invoiceNo);
		if (StringUtil.isEmpty(orderIds)) {
			ServiceException se = new ServiceException("orderIds is null !");
			se.setCode(ErrorCodeEnum.DATA_ERROR.code());
			throw se;
		}
		String[] idArray = orderIds.split(",");
		for (String orderId : idArray)
			i += commissionOrderDao.confirm(Integer.parseInt(orderId));
		return i;

	}
}

