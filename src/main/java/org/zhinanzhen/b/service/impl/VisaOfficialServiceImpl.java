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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
            Calendar calendar = Calendar.getInstance();
            Double rate = 0.00;
            VisaOfficialDO visaOfficialDO = mapper.map(visaOfficialDTO, VisaOfficialDO.class);
            ServiceOrderDO serviceOrderDO = serviceOrderDao.getServiceOrderById(visaOfficialDTO.getServiceOrderId());
            List<ServiceOrderDO> list = new ArrayList<>();
            list = serviceOrderDao.listByParentId(serviceOrderDO.getId());
            List<Integer> monthlist = new ArrayList<Integer>() {
                {
                    this.add(1);
                    this.add(2);
                    this.add(3);
                    this.add(7);
                    this.add(8);
                    this.add(9);
                }
            };
            //长期签证计算
            List<String> arrayList = new ArrayList<String>() {
                {
                    this.add("103");
                    this.add("143");
                    this.add("173");
                    this.add("864");
                    this.add("835");
                    this.add("838");
                }
            };
            if (arrayList.contains(serviceDao.getServiceById(visaOfficialDTO.getServiceId()).getCode())) {
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
                OfficialGradeDO grade = officialGradeDao.getOfficialGradeById(official.getGradeId());
                if (grade == null) {
                    ServiceException se = new ServiceException("请绑定文案等级 !");
                    se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
                    throw se;
                }
                if (monthlist.contains(calendar.get(Calendar.MONTH) + 1)) {
                    rate = grade.getRate() + 3;
                } else {
                    rate = grade.getRate();
                }
                ServicePackagePriceDO servicePackagePriceDO = servicePackagePriceDAO.getByServiceId(serviceOrderDO.getServiceId());
                if (servicePackagePriceDO == null) {
                    commissionAmountDTO.setThirdPrince(0.00);
                    commissionAmountDTO.setRuler(0);
                } else {
                    commissionAmountDTO.setThirdPrince(servicePackagePriceDO.getThirdPrince());
                    commissionAmountDTO.setRuler(servicePackagePriceDO.getRuler());
                }
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                if (commissionAmountDTO.getRuler() == 0) {
                    commissionAmountDTO.setPredictCommissionAmount((serviceOrderDO.getAmount() - commissionAmountDTO.getRefund() - commissionAmountDTO.getThirdPrince()) * 0.5);
                    if (commissionAmountDTO.getPredictCommissionAmount() <= 0)
                        commissionAmountDTO.setPredictCommissionAmount(0.00);
                    commissionAmountDTO.setCommissionAmount(commissionAmountDTO.getPredictCommissionAmount());
                    commissionAmountDTO.setCommission(commissionAmountDTO.getPredictCommissionAmount() * (rate / 100));
                    String calculation = new String();
                    calculation = "0" + "|" + commissionAmountDTO.getThirdPrince() + "|" + dateFormat.format(servicePackagePriceDO == null ? System.currentTimeMillis() : servicePackagePriceDO.getGmtModify()) + "|" + grade.getGrade() + "," + rate + "%" + "," + dateFormat.format(grade.getGmtModify());
                    commissionAmountDTO.setCalculation(calculation);
                } else {
                    commissionAmountDTO.setCommission(servicePackagePriceDO.getAmount());
                    commissionAmountDTO.setThirdPrince(servicePackagePriceDO.getThirdPrince());
                    String calculation = new String();
                    calculation = "1" + "|" + commissionAmountDTO.getThirdPrince() + "," + servicePackagePriceDO.getAmount() + "|" + dateFormat.format(servicePackagePriceDO.getGmtModify());
                    commissionAmountDTO.setCalculation(calculation);
                }
                visaOfficialDO.setPredictCommissionAmount(commissionAmountDTO.getPredictCommissionAmount());
                visaOfficialDO.setCommissionAmount(commissionAmountDTO.getCommissionAmount());
                visaOfficialDO.setPredictCommission(commissionAmountDTO.getCommission());


                visaOfficialDO.setCalculation(commissionAmountDTO.getCalculation());
            }

            //常规计算
            else if (serviceOrderDO.getParentId() == 0 && list.size() == 0) {
                CommissionAmountDTO commissionAmountDTO = calculationCommissionAmount(serviceOrderDO.getId());
                visaOfficialDO.setPredictCommissionAmount(commissionAmountDTO.getPredictCommissionAmount());
                visaOfficialDO.setCommissionAmount(commissionAmountDTO.getCommissionAmount());
                visaOfficialDO.setPredictCommission(commissionAmountDTO.getCommission());
                visaOfficialDO.setCalculation(commissionAmountDTO.getCalculation());
            }
            //打包服务计算
            else if (serviceOrderDO.getServicePackageId() != 0) {
                ServicePackageDO packageDO = servicePackageDAO.getById(serviceOrderDO.getServicePackageId());
                //eoi计算
                if (packageDO.getType().equals("EOI")) {
                    VisaDO visaDO = new VisaDO();
                    CommissionAmountDTO commissionAmountDTO = new CommissionAmountDTO();
                    visaDO = visaDAO.getFirstVisaByServiceOrderId(serviceOrderDO.getParentId());
                    RefundDO refund = refundDAO.getRefundByVisaId(visaDO.getId());
                    if (refund == null) {
                        commissionAmountDTO.setRefund(0.00);
                    } else {
                        commissionAmountDTO.setRefund(refund.getAmount());
                    }
                    OfficialDO official = officialDAO.getOfficialById(serviceOrderDO.getOfficialId());
                    OfficialGradeDO grade = officialGradeDao.getOfficialGradeById(official.getGradeId());
                    if (grade == null) {
                        ServiceException se = new ServiceException("请绑定文案等级 !");
                        se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
                        throw se;
                    }
                    if (monthlist.contains(calendar.get(Calendar.MONTH) + 1)) {
                        rate = grade.getRate() + 3;
                    } else {
                        rate = grade.getRate();
                    }
                    ServicePackagePriceDO servicePackagePriceDO = servicePackagePriceDAO.getByServiceId(serviceOrderDO.getServiceId());
                    if (servicePackagePriceDO == null) {
                        commissionAmountDTO.setThirdPrince(0.00);
                        commissionAmountDTO.setRuler(0);
                    } else {
                        commissionAmountDTO.setThirdPrince(servicePackagePriceDO.getThirdPrince());
                        commissionAmountDTO.setRuler(servicePackagePriceDO.getRuler());
                    }
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    if (commissionAmountDTO.getRuler() == 0) {
                        commissionAmountDTO.setPredictCommissionAmount(visaDO.getAmount() - commissionAmountDTO.getRefund() - commissionAmountDTO.getThirdPrince());
                        if (commissionAmountDTO.getPredictCommissionAmount() <= 0)
                            commissionAmountDTO.setPredictCommissionAmount(0.00);
                        commissionAmountDTO.setCommissionAmount(commissionAmountDTO.getPredictCommissionAmount());
                        commissionAmountDTO.setCommission(commissionAmountDTO.getPredictCommissionAmount() * (rate / 100));
                        String calculation = new String();
                        calculation = "0" + "|" + commissionAmountDTO.getThirdPrince() + "|" + dateFormat.format(servicePackagePriceDO == null ? System.currentTimeMillis() : servicePackagePriceDO.getGmtModify()) + "|" + grade.getGrade() + "," + rate + "%" + "," + dateFormat.format(grade.getGmtModify());
                        commissionAmountDTO.setCalculation(calculation);
                    } else {
                        commissionAmountDTO.setCommission(servicePackagePriceDO.getAmount());
                        commissionAmountDTO.setThirdPrince(servicePackagePriceDO.getThirdPrince());
                        String calculation = new String();
                        calculation = "1" + "|" + commissionAmountDTO.getThirdPrince() + "," + servicePackagePriceDO.getAmount() + "|" + dateFormat.format(servicePackagePriceDO.getGmtModify());
                        commissionAmountDTO.setCalculation(calculation);
                    }
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
                        OfficialGradeDO grade = officialGradeDao.getOfficialGradeById(official.getGradeId());
                        if (grade == null) {
                            ServiceException se = new ServiceException("请绑定文案等级 !");
                            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
                            throw se;
                        }
                        if (monthlist.contains(calendar.get(Calendar.MONTH) + 1)) {
                            rate = grade.getRate() + 3;
                        } else {
                            rate = grade.getRate();
                        }
                        ServicePackagePriceDO servicePackagePriceDO = servicePackagePriceDAO.getByServiceId(serviceOrderDO.getServiceId());
                        if (servicePackagePriceDO == null) {
                            commissionAmountDTO.setThirdPrince(0.00);
                            commissionAmountDTO.setRuler(0);
                        } else {
                            commissionAmountDTO.setThirdPrince(servicePackagePriceDO.getThirdPrince());
                            commissionAmountDTO.setRuler(servicePackagePriceDO.getRuler());
                        }
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        if (commissionAmountDTO.getRuler() == 0) {
                            commissionAmountDTO.setPredictCommissionAmount(visaDO.getAmount() - commissionAmountDTO.getRefund() - commissionAmountDTO.getThirdPrince());
                            if (commissionAmountDTO.getPredictCommissionAmount() <= 0)
                                commissionAmountDTO.setPredictCommissionAmount(0.00);
                            commissionAmountDTO.setCommissionAmount(commissionAmountDTO.getPredictCommissionAmount());
                            commissionAmountDTO.setCommission(commissionAmountDTO.getPredictCommissionAmount() * (rate / 100));
                            String calculation = new String();
                            calculation = "0" + "|" + commissionAmountDTO.getThirdPrince() + "|" + dateFormat.format(servicePackagePriceDO == null ? System.currentTimeMillis() : servicePackagePriceDO.getGmtModify()) + "|" + grade.getGrade() + "," + rate + "%" + "," + dateFormat.format(grade.getGmtModify());
                            commissionAmountDTO.setCalculation(calculation);
                        } else {
                            commissionAmountDTO.setCommission(servicePackagePriceDO.getAmount());
                            commissionAmountDTO.setThirdPrince(servicePackagePriceDO.getThirdPrince());
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
                //雇主提名
                if (packageDO.getType().equals("TM")) {
                    if (serviceOrderDao.getServiceOrderById(serviceOrderDO.getParentId()).isPay()) {
                        VisaDO visaDO = new VisaDO();
                        CommissionAmountDTO commissionAmountDTO = new CommissionAmountDTO();
                        visaDO = visaDAO.getFirstVisaByServiceOrderId(serviceOrderDO.getParentId());
                        RefundDO refund = refundDAO.getRefundByVisaId(visaDO.getId());
                        if (refund == null) {
                            commissionAmountDTO.setRefund(0.00);
                        } else {
                            commissionAmountDTO.setRefund(refund.getAmount());
                        }
                        OfficialDO official = officialDAO.getOfficialById(serviceOrderDO.getOfficialId());
                        OfficialGradeDO grade = officialGradeDao.getOfficialGradeById(official.getGradeId());
                        if (grade == null) {
                            ServiceException se = new ServiceException("请绑定文案等级 !");
                            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
                            throw se;
                        }
                        if (monthlist.contains(calendar.get(Calendar.MONTH) + 1)) {
                            rate = grade.getRate() + 3;
                        } else {
                            rate = grade.getRate();
                        }
                        ServicePackagePriceDO servicePackagePriceDO = servicePackagePriceDAO.getByServiceId(serviceOrderDO.getServiceId());
                        if (servicePackagePriceDO == null) {
                            commissionAmountDTO.setThirdPrince(0.00);
                            commissionAmountDTO.setRuler(0);
                        } else {
                            commissionAmountDTO.setThirdPrince(servicePackagePriceDO.getThirdPrince());
                            commissionAmountDTO.setRuler(servicePackagePriceDO.getRuler());
                        }
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        if (commissionAmountDTO.getRuler() == 0) {
                            commissionAmountDTO.setPredictCommissionAmount(visaDO.getAmount() - commissionAmountDTO.getRefund() - commissionAmountDTO.getThirdPrince());
                            if (commissionAmountDTO.getPredictCommissionAmount() <= 0)
                                commissionAmountDTO.setPredictCommissionAmount(0.00);
                            commissionAmountDTO.setCommissionAmount(commissionAmountDTO.getPredictCommissionAmount());
                            commissionAmountDTO.setCommission(commissionAmountDTO.getPredictCommissionAmount() * (rate / 100));
                            String calculation = new String();
                            calculation = "0" + "|" + commissionAmountDTO.getThirdPrince() + "|" + dateFormat.format(servicePackagePriceDO == null ? System.currentTimeMillis() : servicePackagePriceDO.getGmtModify()) + "|" + grade.getGrade() + "," + rate + "%" + "," + dateFormat.format(grade.getGmtModify());
                            commissionAmountDTO.setCalculation(calculation);
                        } else {
                            commissionAmountDTO.setCommission(servicePackagePriceDO.getAmount());
                            commissionAmountDTO.setThirdPrince(servicePackagePriceDO.getThirdPrince());
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
    public List<VisaOfficialDTO> getVisaOfficialOrder(Integer officialId, List<Integer> regionIdList, Integer id, String startHandlingDate, String endHandlingDate, String state, String startDate, String endDate, String userName, String applicantName, Integer pageNum, Integer pageSize, Sorter sorter) throws ServiceException {

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

        List<VisaOfficialListDO> list = visaOfficialDao.get(officialId, regionIdList, id, startHandlingDate, endHandlingDate, state, theDateTo00_00_00(startDate),
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
            ServiceOrderDTO serviceOrderDto = mapper.map(serviceOrderDO, ServiceOrderDTO.class);
            ServiceDO serviceDo = serviceDao.getServiceById(serviceOrderDO.getServiceId());
            if (serviceDo != null) {
                serviceOrderDto.setService(mapper.map(serviceDo, ServiceDTO.class));
            }
            // 查询服务包类型
            if (serviceOrderDto.getServicePackageId() > 0) {
                ServicePackageDO servicePackageDo = servicePackageDAO.getById(serviceOrderDto.getServicePackageId());
                if (servicePackageDo != null)
                    serviceOrderDto.setServicePackage(mapper.map(servicePackageDo, ServicePackageDTO.class));
            }
            visaOfficialDto.setServiceOrder(serviceOrderDto);
            ServicePackagePriceDO servicePackagePriceDO = servicePackagePriceDAO.getByServiceId(visaOfficialDto.getServiceId());
            if(servicePackagePriceDO!=null) {
                visaOfficialDto.setServicePackagePriceDO(servicePackagePriceDO);
            }
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
        return visaOfficialDtoList;
    }

    @Override
    public int count(Integer officialId, List<Integer> regionIdList, Integer id, String startHandlingDate, String endHandlingDate, String state, String startDate, String endDate, String userName, String applicantName) throws ServiceException {
        return visaOfficialDao.count(officialId, regionIdList, id, startHandlingDate, endHandlingDate, state, startDate, endDate, userName, applicantName);
    }

    @Override
    public void update(Integer id, String submitIbDate, Double handling_date, String state) {
        visaOfficialDao.update(id, submitIbDate, handling_date, state);

    }

    //计算
    private CommissionAmountDTO calculationCommissionAmount(int serviceOrderId) throws ServiceException {
        ServiceOrderDO serviceOrderDO = serviceOrderDao.getServiceOrderById(serviceOrderId);
        VisaDO visaDO = new VisaDO();
        Calendar calendar = Calendar.getInstance();
        List<Integer> monthlist = new ArrayList<Integer>() {
            {
                this.add(1);
                this.add(2);
                this.add(3);
                this.add(7);
                this.add(8);
                this.add(9);
            }
        };
        CommissionAmountDTO commissionAmountDTO = new CommissionAmountDTO();
        double amount = 0.00;
        double rate = 0.00;
        if (serviceOrderDO.getParentId() == 0) {
            visaDO = visaDAO.getFirstVisaByServiceOrderId(serviceOrderDO.getId());
            VisaDO visaDO1 = visaDAO.getSecondVisaByServiceOrderId(serviceOrderDO.getId());
            if (visaDO1 == null)
                amount = visaDO.getAmount();
            else {
                amount = visaDO.getAmount();
                amount += visaDO1.getAmount();
            }
        } else {
            visaDO = visaDAO.getFirstVisaByServiceOrderId(serviceOrderDO.getParentId());
            VisaDO visaDO1 = visaDAO.getSecondVisaByServiceOrderId(serviceOrderDO.getParentId());
            if (visaDO1 == null)
                amount = visaDO.getAmount();
            else {
                amount = visaDO.getAmount();
                amount += visaDO1.getAmount();
            }
        }
        RefundDO refund = refundDAO.getRefundByVisaId(visaDO.getId());
        if (refund == null) {
            commissionAmountDTO.setRefund(0.00);
        } else {
            commissionAmountDTO.setRefund(refund.getAmount());
        }
        OfficialDO official = officialDAO.getOfficialById(serviceOrderDO.getOfficialId());
        OfficialGradeDO grade = officialGradeDao.getOfficialGradeById(official.getGradeId());
        if (grade == null) {
            ServiceException se = new ServiceException("请绑定文案等级 !");
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
        if (monthlist.contains(calendar.get(Calendar.MONTH) + 1)) {
            rate = grade.getRate() + 3;
        } else {
            rate = grade.getRate();
        }
        ServicePackagePriceDO servicePackagePriceDO = servicePackagePriceDAO.getByServiceId(serviceOrderDO.getServiceId());
        if (servicePackagePriceDO == null) {
            commissionAmountDTO.setThirdPrince(0.00);
            commissionAmountDTO.setRuler(0);
        } else {
            commissionAmountDTO.setThirdPrince(servicePackagePriceDO.getThirdPrince());
            commissionAmountDTO.setRuler(servicePackagePriceDO.getRuler());
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (commissionAmountDTO.getRuler() == 0) {
            commissionAmountDTO.setPredictCommissionAmount(amount - commissionAmountDTO.getRefund() - commissionAmountDTO.getThirdPrince());
            if (commissionAmountDTO.getPredictCommissionAmount() <= 0)
                commissionAmountDTO.setPredictCommissionAmount(0.00);
            commissionAmountDTO.setCommissionAmount(commissionAmountDTO.getPredictCommissionAmount());
            commissionAmountDTO.setCommission(commissionAmountDTO.getPredictCommissionAmount() * (rate / 100));
            String calculation = new String();
            calculation = "0" + "|" + commissionAmountDTO.getThirdPrince() + "|" + dateFormat.format(servicePackagePriceDO == null ? System.currentTimeMillis() : servicePackagePriceDO.getGmtModify()) + "|" + grade.getGrade() + "," + rate + "%" + "," + dateFormat.format(grade.getGmtModify());
            commissionAmountDTO.setCalculation(calculation);
        } else {
            commissionAmountDTO.setCommission(servicePackagePriceDO.getAmount());
            commissionAmountDTO.setThirdPrince(servicePackagePriceDO.getThirdPrince());
            String calculation = new String();
            calculation = "1" + "|" + commissionAmountDTO.getThirdPrince() + "," + servicePackagePriceDO.getAmount() + "|" + dateFormat.format(servicePackagePriceDO.getGmtModify());
            commissionAmountDTO.setCalculation(calculation);
        }

        return commissionAmountDTO;
    }


}
