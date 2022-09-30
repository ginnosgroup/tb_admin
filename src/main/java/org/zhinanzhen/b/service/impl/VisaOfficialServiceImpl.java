package org.zhinanzhen.b.service.impl;


import com.ikasoa.core.ErrorCodeEnum;
import com.ikasoa.core.utils.StringUtil;
import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.*;
import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.service.AbleStateEnum;
import org.zhinanzhen.b.service.VisaOfficialService;
import org.zhinanzhen.b.service.pojo.*;
import org.zhinanzhen.b.service.pojo.ant.Sorter;
import org.zhinanzhen.tb.dao.AdviserDAO;
import org.zhinanzhen.tb.dao.UserDAO;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
import org.zhinanzhen.tb.dao.pojo.UserDO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service("VisaOfficialService")
public class VisaOfficialServiceImpl extends BaseService implements VisaOfficialService {
    @Resource
    VisaOfficialDao visaOfficialDao;

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

    @Resource
    private ServicePackagePriceDAO servicePackagePriceDAO;

    @Resource
    private MaraDAO maraDAO;


    @Resource
    private RefundDAO refundDAO;

    @Resource
    private OfficialDAO officialDAO;

    @Resource
    private OfficialGradeDao officialGradeDao;

    @Resource
    private VisaDAO visaDAO;

    @Resource
    private ServiceOrderOfficialRemarksDAO serviceOrderOfficialRemarksDAO;

    @Resource
    private ServicePackageDAO servicePackageDAO;

    public VisaOfficialDTO putVisaOfficialDTO(VisaOfficialListDO visaListDo) throws ServiceException {
        VisaOfficialDTO visaOfficialDto = putVisaOfficialDTO((VisaOfficialDO) visaListDo);
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

            visaOfficialDto.setApplicant(applicantDto);
            visaOfficialDto.setApplicantId(visaListDo.getApplicantId());
        }
        return visaOfficialDto;
    }

    public VisaOfficialDTO putVisaOfficialDTO(VisaOfficialDO visaListDo) throws ServiceException {
        VisaOfficialDTO visaOfficialDto = mapper.map(visaListDo, VisaOfficialDTO.class);
        if (visaOfficialDto.getUserId() > 0) {
            UserDO userDo = userDao.getUserById(visaOfficialDto.getUserId());
            visaOfficialDto.setUserName(userDo.getName());
            visaOfficialDto.setPhone(userDo.getPhone());
            visaOfficialDto.setBirthday(userDo.getBirthday());
            visaOfficialDto.setUser(mapper.map(userDo, UserDTO.class));
        }
        AdviserDO adviserDo = adviserDao.getAdviserById(visaListDo.getAdviserId());
        if (adviserDo != null) {
            visaOfficialDto.setAdviserName(adviserDo.getName());
        }
        OfficialDO officialDo = officialDao.getOfficialById(visaListDo.getOfficialId());
        if (officialDo != null) {
            visaOfficialDto.setOfficialName(officialDo.getName());
        }
        ReceiveTypeDO receiveTypeDo = receiveTypeDao.getReceiveTypeById(visaListDo.getReceiveTypeId());
        if (receiveTypeDo != null) {
            visaOfficialDto.setReceiveTypeName(receiveTypeDo.getName());
        }
        ServiceDO serviceDo = serviceDao.getServiceById(visaListDo.getServiceId());
        if (serviceDo != null) {
            visaOfficialDto.setServiceCode(serviceDo.getCode());
        }
        List<VisaOfficialDO> list = visaOfficialDao.listVisaByCode(visaOfficialDto.getCode());
        if (list != null) {
            double totalPerAmount = 0.00;
            double totalAmount = 0.00;
            for (VisaOfficialDO visaOfficialDo : list) {
                totalPerAmount += visaOfficialDo.getPerAmount();
                if (visaOfficialDo.getPaymentVoucherImageUrl1() != null || visaOfficialDo.getPaymentVoucherImageUrl2() != null
                        || visaOfficialDo.getPaymentVoucherImageUrl3() != null
                        || visaOfficialDo.getPaymentVoucherImageUrl4() != null
                        || visaOfficialDo.getPaymentVoucherImageUrl5() != null)
                    totalAmount += visaOfficialDo.getAmount();
            }
            visaOfficialDto.setTotalPerAmount(totalPerAmount);
            visaOfficialDto.setTotalAmount(totalAmount);
        }

        // 是否退款
        RefundDO refundDo = refundDao.getRefundByVisaId(visaListDo.getId());
        visaOfficialDto.setRefunded(refundDo != null && StringUtil.equals("PAID", refundDo.getState()));

        // 汇率币种计算金额
        Double exchangeRate = visaOfficialDto.getExchangeRate();
        if ("AUD".equalsIgnoreCase(visaOfficialDto.getCurrency())) {
            visaOfficialDto.setAmountAUD(visaOfficialDto.getAmount());
            visaOfficialDto.setAmountCNY(roundHalfUp2(visaOfficialDto.getAmount() * exchangeRate));
            visaOfficialDto.setPerAmountAUD(visaOfficialDto.getPerAmount());
            visaOfficialDto.setPerAmountCNY(roundHalfUp2(visaOfficialDto.getPerAmount() * exchangeRate));
            visaOfficialDto.setTotalAmountAUD(visaOfficialDto.getAmountAUD());
            visaOfficialDto.setTotalAmountCNY(roundHalfUp2(visaOfficialDto.getAmountAUD() * exchangeRate));
            visaOfficialDto.setTotalPerAmountAUD(visaOfficialDto.getTotalPerAmount());
            visaOfficialDto.setTotalPerAmountCNY(roundHalfUp2(visaOfficialDto.getTotalPerAmount() * exchangeRate));
            visaOfficialDto.setExpectAmountAUD(visaOfficialDto.getExpectAmount());
            visaOfficialDto.setExpectAmountCNY(roundHalfUp2(visaOfficialDto.getExpectAmount() * exchangeRate));
            visaOfficialDto.setSureExpectAmountAUD(visaOfficialDto.getSureExpectAmount());
            visaOfficialDto.setSureExpectAmountCNY(roundHalfUp2(visaOfficialDto.getSureExpectAmount() * exchangeRate));
            visaOfficialDto.setDiscountAUD(visaOfficialDto.getDiscount());
            visaOfficialDto.setGstAUD(visaOfficialDto.getGst());
            visaOfficialDto.setDeductGstAUD(visaOfficialDto.getDeductGst());
            visaOfficialDto.setBonusAUD(visaOfficialDto.getBonus());
        }
        if ("CNY".equalsIgnoreCase(visaOfficialDto.getCurrency())) {
            visaOfficialDto.setAmountAUD(roundHalfUp2(visaOfficialDto.getAmount() / exchangeRate));
            visaOfficialDto.setAmountCNY(visaOfficialDto.getAmount());
            visaOfficialDto.setPerAmountAUD(roundHalfUp2(visaOfficialDto.getPerAmount() / exchangeRate));
            visaOfficialDto.setPerAmountCNY(visaOfficialDto.getPerAmount());
            visaOfficialDto.setTotalAmountAUD(roundHalfUp2(visaOfficialDto.getAmount() / exchangeRate));
            visaOfficialDto.setTotalAmountCNY(visaOfficialDto.getAmount());
            visaOfficialDto.setTotalPerAmountAUD(roundHalfUp2(visaOfficialDto.getTotalPerAmount() / exchangeRate));
            visaOfficialDto.setTotalPerAmountCNY(visaOfficialDto.getTotalPerAmount());
            visaOfficialDto.setExpectAmountAUD(roundHalfUp2(visaOfficialDto.getExpectAmount() / exchangeRate));
            visaOfficialDto.setExpectAmountCNY(visaOfficialDto.getExpectAmount());
            visaOfficialDto.setSureExpectAmountAUD(roundHalfUp2(visaOfficialDto.getSureExpectAmount() / exchangeRate));
            visaOfficialDto.setSureExpectAmountCNY(visaOfficialDto.getSureExpectAmount());
            visaOfficialDto.setDiscountAUD(roundHalfUp2(visaOfficialDto.getDiscount() / exchangeRate));
            visaOfficialDto.setGstAUD(roundHalfUp2(visaOfficialDto.getGst() / exchangeRate));
            visaOfficialDto.setDeductGstAUD(roundHalfUp2(visaOfficialDto.getDeductGst() / exchangeRate));
            visaOfficialDto.setBonusAUD(roundHalfUp2(visaOfficialDto.getBonus() / exchangeRate));
        }

        return visaOfficialDto;
    }

    @Override
    public int addVisa(VisaOfficialDTO visaOfficialDTO) throws ServiceException {
        if (visaOfficialDTO == null) {
            ServiceException se = new ServiceException("visaOfficialDto is null !");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        if (visaOfficialDao.countVisaByServiceOrderIdAndExcludeCode(visaOfficialDTO.getServiceOrderId(), visaOfficialDTO.getCode()) > 0) {
            ServiceException se = new ServiceException("已创建过佣金订单,不能重复创建!");
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
        try {
            VisaOfficialDO visaOfficialDO = mapper.map(visaOfficialDTO, VisaOfficialDO.class);
//            //长期签证计算
//            String a []={"103","143","173","864","835","838"};//todo
//            for (String s : a) {
//                if (s.equals(serviceDAO.getServiceById(visaOfficialDTO.getServiceId()).getCode())){
//                    VisaDO visaDO = new VisaDO();
//                    CommissionAmountDTO commissionAmountDTO = new CommissionAmountDTO();
//                    visaDO = visaDAO.getFirstVisaByServiceOrderId(visaOfficialDTO.getId());
//                    RefundDO refund = refundDAO.getRefundByVisaId(visaDO.getId());
//                    if (refund==null){
//                        commissionAmountDTO.setRefund(0.00);
//                    }
//                    else{
//                        commissionAmountDTO.setRefund(refund.getAmount());
//                    }
//                    ServiceOrderOfficialRemarksDO serviceOrderOfficialRemarksDO = serviceOrderOfficialRemarksDAO.getByServiceOrderId(visaOfficialDTO.getId());
//                    OfficialDO official = officialDAO.getOfficialById(serviceOrderOfficialRemarksDO.getOfficialId());
//                    OfficialGradeDO grade = officialGradeDao.getOfficialGradeById(official.getGradeId());
//                    ServicePackagePriceDO servicePackagePriceDO = servicePackagePriceDAO.getByServiceId(visaOfficialDTO.getServiceId());
//                    commissionAmountDTO.setThirdPrince(servicePackagePriceDO.getThirdPrince());
//                    commissionAmountDTO.setCommissionAmount(visaOfficialDTO.getAmount()-commissionAmountDTO.getRefund()-commissionAmountDTO.getThirdPrince());
//                    if (commissionAmountDTO.getCommissionAmount()<=0)
//                        commissionAmountDTO.setCommissionAmount(0.00);
//                    commissionAmountDTO.setCommission(commissionAmountDTO.getCommissionAmount()*grade.getRate()*0.5);
//                    serviceOrderDao.setCommission(visaOfficialDTO.getId(),commissionAmountDTO.getCommissionAmount(),commissionAmountDTO.getCommission(),0.00);
//                }
//            }
            //常规计算
            ServiceOrderDO serviceOrderDO = serviceOrderDao.getServiceOrderById(visaOfficialDTO.getServiceOrderId());
            List<ServiceOrderDO> listByParentId = serviceOrderDao.listByParentId(serviceOrderDO.getId());
            if (serviceOrderDO.getParentId() == 0 && listByParentId ==null) {
                CommissionAmountDTO commissionAmountDTO = calculationCommissionAmount(serviceOrderDO.getId());
                visaOfficialDO.setPredictCommissionAmount(commissionAmountDTO.getPredictCommissionAmount());
                visaOfficialDO.setCommissionAmount(commissionAmountDTO.getCommissionAmount());
                visaOfficialDO.setPredictCommission(commissionAmountDTO.getCommission());
                visaOfficialDO.setCalculation(commissionAmountDTO.getCalculation());
            }
            //打包服务计算
            if (serviceOrderDO.getServicePackageId() != 0) {
                ServicePackageDO packageDO = servicePackageDAO.getById(serviceOrderDO.getId());
                //eoi计算
                if (packageDO.getType().equals("EOI")) {
                    CommissionAmountDTO commissionAmountDTO = calculationCommissionAmount(serviceOrderDO.getId());
                    visaOfficialDO.setPredictCommissionAmount(commissionAmountDTO.getPredictCommissionAmount());
                    visaOfficialDO.setCommissionAmount(commissionAmountDTO.getCommissionAmount());
                    visaOfficialDO.setPredictCommission(commissionAmountDTO.getCommission());
                    visaOfficialDO.setCalculation(commissionAmountDTO.getCalculation());
                }
                //签证服务计算
                if (packageDO.getType().equals("VA")) {
                    if (serviceOrderDao.getServiceOrderById(serviceOrderDO.getParentId()).isPay()) {
                        VisaDO visaDO = new VisaDO();
                        CommissionAmountDTO commissionAmountDTO = new CommissionAmountDTO();
                        visaDO = visaDAO.getSecondVisaByServiceOrderId(serviceOrderDO.getParentId());
                        RefundDO refund = refundDAO.getRefundByVisaId(visaDO.getId());
                        if (refund == null) {
                            commissionAmountDTO.setRefund(0.00);
                        } else {
                            commissionAmountDTO.setRefund(refund.getAmount());
                        }
                        OfficialDO official = officialDAO.getOfficialById(serviceOrderDO.getOfficialId());
                        if (official.getGradeId() == 0) {
                            ServiceException se = new ServiceException("文案未绑定等级");
                            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
                            throw se;
                        }
                        OfficialGradeDO grade = officialGradeDao.getOfficialGradeById(official.getGradeId());
                        ServicePackagePriceDO servicePackagePriceDO = servicePackagePriceDAO.getByServiceId(serviceOrderDO.getServiceId());
                        if (servicePackagePriceDO==null){
                            ServiceException se = new ServiceException("服务未绑定规则");
                            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
                            throw se;
                        }
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        if (servicePackagePriceDO.getRuler() == 0) {
                            commissionAmountDTO.setThirdPrince(servicePackagePriceDO.getThirdPrince());
                            commissionAmountDTO.setPredictCommissionAmount(serviceOrderDO.getAmount() - commissionAmountDTO.getRefund() - commissionAmountDTO.getThirdPrince());
                            if (commissionAmountDTO.getPredictCommissionAmount() <= 0)
                                commissionAmountDTO.setPredictCommissionAmount(0.00);
                            commissionAmountDTO.setCommissionAmount(commissionAmountDTO.getPredictCommissionAmount());
                            commissionAmountDTO.setCommission(commissionAmountDTO.getPredictCommissionAmount() * grade.getRate());
                            String calculation = new String();
                            calculation = "0" + "|" + commissionAmountDTO.getThirdPrince() + "|" + dateFormat.format(servicePackagePriceDO.getGmtModify()) + "|" + grade.getGrade() + "," + grade.getRate() + "," + dateFormat.format(grade.getGmt_modify());
                            commissionAmountDTO.setCalculation(calculation);
                        } else {
                            commissionAmountDTO.setCommission(servicePackagePriceDO.getAmount());
                            String calculation = new String();
                            calculation = "1" + "|" + commissionAmountDTO.getThirdPrince() + "," + servicePackagePriceDO.getAmount() + "|" + dateFormat.format(servicePackagePriceDO.getGmtModify());
                            commissionAmountDTO.setCalculation(calculation);
                        }
                        visaOfficialDO.setPredictCommissionAmount(commissionAmountDTO.getPredictCommissionAmount());
                        visaOfficialDO.setCommissionAmount(commissionAmountDTO.getCommissionAmount());
                        visaOfficialDO.setPredictCommission(commissionAmountDTO.getCommission());
                        visaOfficialDO.setCalculation(commissionAmountDTO.getCalculation());
                    }
                }
            }
            if (visaOfficialDao.addVisa(visaOfficialDO) > 0) {
                visaOfficialDTO.setId(visaOfficialDO.getId());
                visaOfficialDTO.setPredictCommissionAmount(visaOfficialDO.getPredictCommissionAmount());
                visaOfficialDTO.setCommissionAmount(visaOfficialDO.getCommissionAmount());
                visaOfficialDTO.setPredictCommission(visaOfficialDO.getPredictCommission());
                visaOfficialDTO.setCalculation(visaOfficialDO.getCalculation());
                return visaOfficialDO.getId();
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
    public List<VisaOfficialDTO> getVisaOfficialOrder(Integer officialId, List<Integer> regionIdList, Integer id, String startHandlingDate, String endHandlingDate, String state, String startSubmitIbDate, String endSubmitIbDate, String startDate, String endDate, String userName, String applicantName, Integer pageNum, Integer pageSize, Sorter sorter) throws ServiceException {

        if (pageNum != null && pageNum < 0) {
            pageNum = DEFAULT_PAGE_NUM;
        }
        if (pageSize != null && pageSize < 0) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        Integer offset = null;
        if (pageNum != null && pageSize != null) {
            offset = pageNum * pageSize;
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

        List<VisaOfficialListDO> list = visaOfficialDao.get(officialId, regionIdList, id, startHandlingDate, endHandlingDate, state, theDateTo00_00_00(startSubmitIbDate), theDateTo23_59_59(endSubmitIbDate), theDateTo00_00_00(startDate),
                theDateTo23_59_59(endDate), userName, applicantName, offset, pageSize, orderBy);
        List<VisaOfficialDTO> visaOfficialDtoList = new ArrayList<>();
        if (list == null || list.size() == 0) {
            return null;
        }
        for (VisaOfficialListDO visaListDo : list) {
            VisaOfficialDTO visaOfficialDto = null;
            try {
                visaOfficialDto = putVisaOfficialDTO(visaListDo);
            } catch (ServiceException e) {
                e.printStackTrace();
            }

            List<Date> remindDateList = new ArrayList<>();
            List<RemindDO> remindDoList = remindDao.listRemindByVisaOfficialId(visaOfficialDto.getId(), officialId,
                    AbleStateEnum.ENABLED.toString());
            ServiceOrderDO serviceOrderDO = serviceOrderDao.getServiceOrderById(visaOfficialDto.getServiceOrderId());
            serviceOrderDO.setService(mapper.map(serviceDao.getServiceById(serviceOrderDO.getServiceId()), ServiceDTO.class));
            visaOfficialDto.setServiceOrder(mapper.map(serviceOrderDO, ServiceOrderDTO.class));
            visaOfficialDto.setServicePackagePriceDO(servicePackagePriceDAO.getByServiceId(visaOfficialDto.getServiceId()));
            MaraDO mara = maraDAO.getMaraById(visaOfficialDto.getMaraId());
            if (mara != null) {
                visaOfficialDto.setMaraDTO(mapper.map(mara, MaraDTO.class));
            }
            for (RemindDO remindDo : remindDoList) {
                remindDateList.add(remindDo.getRemindDate());
            }
            visaOfficialDto.setRemindDateList(remindDateList);
            visaOfficialDtoList.add(visaOfficialDto);
        }
        for (VisaOfficialDTO adviserRateCommissionOrderDO : visaOfficialDtoList) {
            BigDecimal receivedAUD = BigDecimal.valueOf(adviserRateCommissionOrderDO.getTotalAmountAUD());//总计实收澳币
            BigDecimal refund = BigDecimal.valueOf(adviserRateCommissionOrderDO.getRefund());//退款
            ServicePackagePriceDO servicePackagePriceDO = adviserRateCommissionOrderDO.getServicePackagePriceDO();
            if (servicePackagePriceDO != null) {
                //第三方费用
                double third_prince1 = servicePackagePriceDO.getThirdPrince();
                BigDecimal third_prince = BigDecimal.valueOf(third_prince1);
                //预计计入佣金金额 总计实收-退款-第三方费用
                BigDecimal amount = receivedAUD.subtract(refund).subtract(third_prince);
                double receivedAUDDouble = receivedAUD.doubleValue();
                double amountDouble = amount.doubleValue();
                if (receivedAUDDouble == 0 || amountDouble < 0) {
                    amount = new BigDecimal(adviserRateCommissionOrderDO.getServicePackagePriceDO().getMinPrice()).
                            subtract(new BigDecimal(adviserRateCommissionOrderDO.getServicePackagePriceDO().getCostPrince()));
                    amountDouble = amount.doubleValue();
                }
                adviserRateCommissionOrderDO.setExpectCommissionAmount(amountDouble);
            }

        }

        return visaOfficialDtoList;
    }

    @Override
    public int count(Integer officialId, List<Integer> regionIdList, Integer id, String startHandlingDate, String endHandlingDate, String state, String startSubmitIbDate, String endSubmitIbDate, String startDate, String endDate, String userName, String applicantName) throws ServiceException {
        return visaOfficialDao.count(officialId, regionIdList, id, startHandlingDate, endHandlingDate, state, startSubmitIbDate, endSubmitIbDate, startDate, endDate, userName, applicantName);
    }

    @Override
    public void update(Integer id, String submitIbDate, Double commissionAmount, String state) {
        visaOfficialDao.update(id, submitIbDate, commissionAmount, state);

    }

    //计算
    private CommissionAmountDTO calculationCommissionAmount(int serviceOrderId) throws ServiceException {
        ServiceOrderDO serviceOrderDO = serviceOrderDao.getServiceOrderById(serviceOrderId);
        VisaDO visaDO = new VisaDO();
        CommissionAmountDTO commissionAmountDTO = new CommissionAmountDTO();
        if (serviceOrderDO.getParentId() == 0) {
            visaDO = visaDAO.getFirstVisaByServiceOrderId(serviceOrderDO.getId());
        } else {
            visaDO = visaDAO.getFirstVisaByServiceOrderId(serviceOrderDO.getParentId());
        }
        RefundDO refund = refundDAO.getRefundByVisaId(visaDO.getId());
        if (refund == null) {
            commissionAmountDTO.setRefund(0.00);
        } else {
            commissionAmountDTO.setRefund(refund.getAmount());
        }
        OfficialDO official = officialDAO.getOfficialById(serviceOrderDO.getOfficialId());
        if (official.getGradeId() == 0) {
            ServiceException se = new ServiceException("文案未绑定等级");
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
        OfficialGradeDO grade = officialGradeDao.getOfficialGradeById(official.getGradeId());
        ServicePackagePriceDO servicePackagePriceDO = servicePackagePriceDAO.getByServiceId(serviceOrderDO.getServiceId());
        if (servicePackagePriceDO==null){
            ServiceException se = new ServiceException("服务未绑定规则");
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (servicePackagePriceDO.getRuler() == 0) {
            commissionAmountDTO.setThirdPrince(servicePackagePriceDO.getThirdPrince());
            commissionAmountDTO.setPredictCommissionAmount(serviceOrderDO.getAmount() - commissionAmountDTO.getRefund() - commissionAmountDTO.getThirdPrince());
            if (commissionAmountDTO.getPredictCommissionAmount() <= 0)
                commissionAmountDTO.setPredictCommissionAmount(0.00);
            commissionAmountDTO.setCommissionAmount(commissionAmountDTO.getPredictCommissionAmount());
            commissionAmountDTO.setCommission(commissionAmountDTO.getPredictCommissionAmount() * grade.getRate());
            String calculation = new String();
            calculation = "0" + "|" + commissionAmountDTO.getThirdPrince() + "|" + dateFormat.format(servicePackagePriceDO.getGmtModify()) + "|" + grade.getGrade() + "," + grade.getRate() + "," + dateFormat.format(grade.getGmt_modify());
            commissionAmountDTO.setCalculation(calculation);
        } else {
            commissionAmountDTO.setCommission(servicePackagePriceDO.getAmount());
            String calculation = new String();
            calculation = "1" + "|" + commissionAmountDTO.getThirdPrince() + "," + servicePackagePriceDO.getAmount() + "|" + dateFormat.format(servicePackagePriceDO.getGmtModify());
            commissionAmountDTO.setCalculation(calculation);
        }

        return commissionAmountDTO;
    }


}
