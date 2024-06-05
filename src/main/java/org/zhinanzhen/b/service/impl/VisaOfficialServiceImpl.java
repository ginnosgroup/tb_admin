package org.zhinanzhen.b.service.impl;


import com.alibaba.fastjson.JSONArray;
import com.ikasoa.core.ErrorCodeEnum;
import com.ikasoa.core.utils.ObjectUtil;
import com.ikasoa.core.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zhinanzhen.b.controller.BaseCommissionOrderController;
import org.zhinanzhen.b.dao.*;
import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.service.AbleStateEnum;
import org.zhinanzhen.b.service.ExchangeRateService;
import org.zhinanzhen.b.service.VisaOfficialService;
import org.zhinanzhen.b.service.pojo.*;
import org.zhinanzhen.b.service.pojo.ant.Sorter;
import org.zhinanzhen.tb.dao.AdviserDAO;
import org.zhinanzhen.tb.dao.RegionDAO;
import org.zhinanzhen.tb.dao.UserDAO;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
import org.zhinanzhen.tb.dao.pojo.RegionDO;
import org.zhinanzhen.tb.dao.pojo.UserDO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service("VisaOfficialService")
@Slf4j
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

    @Resource
    private ServiceAssessDao serviceAssessDao;

    @Resource
    private CustomerInformationDAO customerInformationDAO;

    @Resource
    private RegionDAO regionDAO;

    @Resource
    private ExchangeRateService exchangeRateService;

    @Resource
    private SchoolInstitutionLocationDAO schoolInstitutionLocationDAO;

    @Resource
    private SchoolInstitutionDAO schoolInstitutionDAO;

    @Resource
    private SchoolCourseDAO schoolCourseDAO;

    @Resource
    private ServiceOrderDAO serviceOrderDAO;

    public VisaOfficialDTO putVisaOfficialDTO(VisaOfficialListDO visaListDo) throws ServiceException {
        VisaOfficialDTO visaOfficialDto = putVisaOfficialDTO((VisaOfficialDO) visaListDo);
        List<ApplicantListDO> applicantListDOS = serviceOrderDao.ApplicantListByServiceOrderId(visaListDo.getServiceOrderId());
        List<ApplicantDTO> applicantDTOS = new ArrayList<>();
        for (ApplicantListDO applicantListDO : applicantListDOS) {
            if (applicantListDO.getApplicantId() > 0) {
                ApplicantDO applicantDO = applicantDao.getById(applicantListDO.getApplicantId());
                ApplicantDTO applicantDto = new ApplicantDTO();
                if (applicantDO!=null){
                    applicantDto = mapper.map(applicantDO, ApplicantDTO.class);
                }
                List<ServiceOrderApplicantDO> serviceOrderApplicantDoList = serviceOrderApplicantDao
                        .list(visaListDo.getServiceOrderId(), visaListDo.getApplicantId());
                if (serviceOrderApplicantDoList != null && serviceOrderApplicantDoList.size() > 0
                        && serviceOrderApplicantDoList.get(0) != null) {
                    applicantDto.setUrl(serviceOrderApplicantDoList.get(0).getUrl());
                    applicantDto.setContent(serviceOrderApplicantDoList.get(0).getContent());
                }
                applicantDto.setServiceOrderId(applicantListDO.getId());
                //判断是否提交mm资料
                if (customerInformationDAO.getByServiceOrderId(applicantListDO.getId()) != null) {
                    applicantDto.setSubmitMM(true);
                } else
                    applicantDto.setSubmitMM(false);
                applicantDTOS.add(applicantDto);

            }

        }
        visaOfficialDto.setApplicant(applicantDTOS);
        visaOfficialDto.setApplicantId(visaListDo.getApplicantId());
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
                ServiceOrderDO serviceOrderById = serviceOrderDao.getServiceOrderById(visaOfficialDo.getServiceOrderId());
                totalPerAmount += visaOfficialDo.getPerAmount();
                if (visaOfficialDo.getPaymentVoucherImageUrl1() != null || visaOfficialDo.getPaymentVoucherImageUrl2() != null
                        || visaOfficialDo.getPaymentVoucherImageUrl3() != null
                        || visaOfficialDo.getPaymentVoucherImageUrl4() != null
                        || visaOfficialDo.getPaymentVoucherImageUrl5() != null)
                    totalAmount += visaOfficialDo.getAmount();
                if (serviceOrderById.getServiceId() == 15) {
                    totalPerAmount = visaOfficialDo.getPerAmount();
                    totalAmount = visaOfficialDo.getAmount();
                }
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
            visaOfficialDto.setTotalAmountAUD(visaOfficialDto.getTotalAmount());
            visaOfficialDto.setTotalAmountCNY(roundHalfUp2(visaOfficialDto.getTotalAmount() * exchangeRate));
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
            visaOfficialDto.setTotalAmountAUD(roundHalfUp2(visaOfficialDto.getTotalAmount() / exchangeRate));
            visaOfficialDto.setTotalAmountCNY(visaOfficialDto.getTotalAmount());
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
        boolean suborder = false; // 判断父子订单
        int region = 0; // 判断所属地区 0：澳洲 1：中国
        int orderType = 0; // 判断订单类型： 0：打包签证 1：雇主担保 2：单一签证 3：留学服务 4：咨询服务
        boolean pay = false; // 判断是否支付
        // 判断是否是父子订单
        ServiceOrderDO serviceOrderById = serviceOrderDao.getServiceOrderById(visaOfficialDTO.getServiceOrderId());
        int applicantId = serviceOrderById.getApplicantParentId();
        ServiceOrderDO serviceOrderByApplicantId = serviceOrderDao.getServiceOrderById(applicantId);
        if (ObjectUtil.isNotNull(serviceOrderByApplicantId)) {
            suborder = true;
        }
        // 判断当前文案地区为澳洲还是中国
        RegionDO regionById = regionDAO.getRegionById(officialDAO.getOfficialById(visaOfficialDTO.getOfficialId()).getRegionId());
        String regionName = regionById.getName().replaceAll("[^\u4e00-\u9fa5]", "");
        if (StringUtil.isNotEmpty(regionName)) {
            region = 1;
        }
        // 判断当前订单类型
        String typeTmp = serviceOrderById.getType();
        if (suborder) {
            typeTmp = serviceOrderByApplicantId.getType();
        }
        switch (typeTmp) {
            case "SIV":
                orderType = 0; // 打包签证
                break;
            case "NSV":
                orderType = 1; // 雇主担保
                break;
            case "VISA":
                orderType = 2; // 单一签证
                break;
            case "OVST":
                orderType = 3;
                break;
            case "ZX":
                orderType = 4; // 咨询
                break;
            default:
                orderType = 3;
        }
        // 判断订单是否支付
        pay = serviceOrderById.isPay();
        if (suborder) {
            pay = serviceOrderByApplicantId.isPay();
        }
        String packType = "";
        VisaOfficialDO visaOfficialDO = new VisaOfficialDO();
        // 打包签证结算
        if (orderType == 0) {
            // 判断子订单中所有类型
            List<ServiceOrderDTO> deriveOrder = serviceOrderDao.getDeriveOrder(serviceOrderById.getApplicantParentId());
            List<String> deriveOrderTypes = new ArrayList<>();
            deriveOrder.forEach(e->{
                ServicePackageDO byId = servicePackageDAO.getById(e.getServicePackageId());
                deriveOrderTypes.add(byId.getType());
            });
            // 只有签证服务
            if (deriveOrderTypes.contains("VA") && !deriveOrderTypes.contains("EOI") && !deriveOrderTypes.contains("ROI")) {
                packType = "VA";
                visaOfficialDO = buildCommission(serviceOrderById, visaOfficialDTO, pay, region, packType, suborder);
            }
            // EOI、ROI、签证同时存在
            if (deriveOrderTypes.contains("VA") && deriveOrderTypes.contains("EOI") || deriveOrderTypes.contains("ROI")) {
                packType = "EOI";
                visaOfficialDO = buildCommission(serviceOrderById, visaOfficialDTO, pay, region, packType, suborder);
                if (ObjectUtil.isNull(visaOfficialDO)) {
                    return -2;
                }
            }
            // 只有ROI、签证
            if (deriveOrderTypes.contains("VA") && !deriveOrderTypes.contains("EOI") && deriveOrderTypes.contains("ROI")) {
                packType = "ROI";
                visaOfficialDO = buildCommission(serviceOrderById, visaOfficialDTO, pay, region, packType, suborder);
            }
        }
        // 雇主担保结算
        if (orderType == 1) {
            ServicePackageDO byId = servicePackageDAO.getById(serviceOrderById.getServicePackageId());
            visaOfficialDO = buildCommission(serviceOrderById, visaOfficialDTO, pay, region, typeTmp, suborder);
        }
        // 单一签证
        if (orderType == 2) {
            visaOfficialDO = buildCommission(serviceOrderById, visaOfficialDTO, pay, region, typeTmp, suborder);
        }
        // 咨询
        if (orderType == 4) {
            visaOfficialDO = buildCommission(serviceOrderById, visaOfficialDTO, pay, region, typeTmp, suborder);
        }
        visaOfficialDO.setOfficialRegion(region);
        if (visaOfficialDao.addVisa(visaOfficialDO) > 0) {
            visaOfficialDTO.setId(visaOfficialDO.getId());
            visaOfficialDTO.setCommissionAmount(visaOfficialDO.getCommissionAmount());
            visaOfficialDTO.setPredictCommission(visaOfficialDO.getPredictCommission());
            visaOfficialDTO.setCalculation(visaOfficialDO.getCalculation());
            return visaOfficialDO.getId();
        } else {
            return 0;
        }
    }

    private VisaOfficialDO buildCommission(ServiceOrderDO serviceOrderById, VisaOfficialDTO visaOfficialDTO, boolean pay, int region, String packType, boolean suborder) throws ServiceException {
        VisaOfficialDO visaOfficialDO = mapper.map(visaOfficialDTO, VisaOfficialDO.class);
        if (serviceOrderById.getBindingOrder() != null && serviceOrderById.getBindingOrder() > 0) {
            pay = true;
        }
        if (!pay) {
            ServicePackagePriceDO servicePackagePriceDO = servicePackagePriceDAO.getByServiceId(serviceOrderById.getServiceId());
            ServicePackagePriceV2DTO servicePackagePriceV2DTO = closeJugd(serviceOrderById.getOfficialId(), servicePackagePriceDO);
            if (ObjectUtil.isNotNull(servicePackagePriceDO) && servicePackagePriceV2DTO.getRuler() == 1) {
                String calculation = new String();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                calculation = "1" + "|" + servicePackagePriceDO.getThirdPrince() + "," + servicePackagePriceDO.getAmount() + "|" + dateFormat.format(servicePackagePriceDO.getGmtModify());
                visaOfficialDO.setCalculation(calculation);
                visaOfficialDO.setPredictCommission(servicePackagePriceV2DTO.getAmount());
                visaOfficialDO.setPredictCommissionCNY(visaOfficialDO.getPredictCommission() * visaOfficialDO.getExchangeRate());
                if (region == 1) {
                    visaOfficialDO.setPredictCommissionCNY(servicePackagePriceV2DTO.getAmount());
                    visaOfficialDO.setPredictCommission(visaOfficialDO.getPredictCommission() / visaOfficialDO.getExchangeRate());
                }
                visaOfficialDO.setCommissionAmount(0.00);
                visaOfficialDO.setPredictCommissionAmount(0.00);
            } else {
                visaOfficialDO.setPredictCommission(0.00);
                visaOfficialDO.setCommissionAmount(0.00);
                visaOfficialDO.setPredictCommission(0.00);
                visaOfficialDO.setCalculation(null);
                visaOfficialDO.setPredictCommissionCNY(0.00);
            }
            return visaOfficialDO;
        }
        CommissionAmountDTO commissionAmountDTO = new CommissionAmountDTO();
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
        // 判断是否为分期付款订单
        boolean installment = false;
        double refund = 0.00;
        VisaDO firstVisaByServiceOrderId = null;
        VisaDO secondVisaByServiceOrderId = null;
        firstVisaByServiceOrderId = visaDAO.getFirstVisaByServiceOrderId(serviceOrderById.getId());
        secondVisaByServiceOrderId = visaDAO.getSecondVisaByServiceOrderId(serviceOrderById.getId());
        if (suborder) {
            int id = serviceOrderDao.getServiceOrderById(serviceOrderById.getApplicantParentId()).getId();
            firstVisaByServiceOrderId = visaDAO.getFirstVisaByServiceOrderId(id);
            secondVisaByServiceOrderId = visaDAO.getSecondVisaByServiceOrderId(id);
        }
//        commissionAmountDTO.setRefund(visaDAO.getVisaById(firstVisaByServiceOrderId.getId()).getRefund()); // 设置退款金额
        // 退款查询
        RefundDO firstRefundByVisaId = refundDAO.getRefundByVisaId(firstVisaByServiceOrderId.getId());
        if (ObjectUtil.isNotNull(firstRefundByVisaId)) {
            refund = firstRefundByVisaId.getAmount();
        }
        if (ObjectUtil.isNotNull(secondVisaByServiceOrderId)) {
            installment = true;
        }
        double amount = 0.00;
        double rate = 0.00;
        OfficialDO officialById = officialDAO.getOfficialById(serviceOrderById.getOfficialId());
        OfficialGradeDO officialGradeById = officialGradeDao.getOfficialGradeById(officialById.getGradeId());
        rate = officialGradeById.getRate();
//        if (region == 0 && monthlist.contains(calendar.get(Calendar.MONTH) + 1)) {
//            rate = rate + 3;
//        }
        if (region == 0) {
            // 创建一个Calendar对象并设置时间为date对象的时间
            Calendar sss = Calendar.getInstance();
            sss.setTime(serviceOrderById.getReadcommittedDate());

            // 获取月份（注意：Calendar的月份是从0开始的，所以1代表二月，0代表一月）
            int month = sss.get(Calendar.MONTH) + 1; // 加1是因为我们需要从1开始的月份
            if (month == 1 || month == 2 || month == 3) {
                rate = rate + 3;
            }
        }
        // EOI数量判断
        int EOICount = 0;
        List<ServiceOrderDTO> deriveOrder = new ArrayList<>();
        if (suborder) {
            deriveOrder = serviceOrderDao.getDeriveOrder(serviceOrderById.getApplicantParentId());
            for (ServiceOrderDTO a : deriveOrder) {
                ServicePackageDO byId = servicePackageDAO.getById(a.getServicePackageId());
                if ("EOI".equals(byId.getType())) {
                    EOICount++;
                }
            }
        }
        if (EOICount == 0 || !"EOI".equals(servicePackageDAO.getById(serviceOrderById.getServicePackageId()).getType())) {
            EOICount = 1;
        }
//        EOICount = EOICount == 0 ? EOICount++ : EOICount;
        if ("VA".equals(packType)) {
            amount += firstVisaByServiceOrderId.getAmount();
            if (installment) {
                amount += secondVisaByServiceOrderId.getAmount();
                RefundDO secondRefundByVisaId = refundDAO.getRefundByVisaId(secondVisaByServiceOrderId.getId());
                if (ObjectUtil.isNotNull(secondRefundByVisaId)) {
                    refund += secondRefundByVisaId.getAmount();
                }
                commissionAmountDTO.setRefund(refund);
            }
        }
        if ("EOI".equals(packType)) {
            amount = firstVisaByServiceOrderId.getAmount();
            if (installment) {
                ServicePackageDO byId = servicePackageDAO.getById(serviceOrderById.getServicePackageId());
                if ("VA".equals(byId.getType())) {
                    amount = secondVisaByServiceOrderId.getAmount();
                    RefundDO secondRefundByVisaId = refundDAO.getRefundByVisaId(secondVisaByServiceOrderId.getId());
                    if (ObjectUtil.isNotNull(secondRefundByVisaId)) {
                        refund = secondRefundByVisaId.getAmount();
                    }
                    commissionAmountDTO.setRefund(refund);
                }
                if ("ROI".equals(byId.getType())) {
                    return null;
                }
            } else {
                amount = amount * 0.5;
            }
        }
        if ("ROI".equals(packType)) {
            amount += firstVisaByServiceOrderId.getAmount();
            if (installment) {
                commissionAmountDTO.setRefund(visaDAO.getVisaById(secondVisaByServiceOrderId.getId()).getRefund());
                ServicePackageDO byId = servicePackageDAO.getById(serviceOrderById.getServicePackageId());
                if ("VA".equals(byId.getType())) {
                    amount += secondVisaByServiceOrderId.getAmount();
                    RefundDO secondRefundByVisaId = refundDAO.getRefundByVisaId(secondVisaByServiceOrderId.getId());
                    if (ObjectUtil.isNotNull(secondRefundByVisaId)) {
                        refund += secondRefundByVisaId.getAmount();
                    }
                    commissionAmountDTO.setRefund(refund);
                }
            } else {
                amount = amount * 0.5;
            }
        }
        if ("NSV".equals(packType)) {
            amount += firstVisaByServiceOrderId.getAmount() * 0.5;
            if (installment) {
                ServicePackageDO byId = servicePackageDAO.getById(serviceOrderById.getServicePackageId());
                if ("TM".equals(byId.getType())) {
                    commissionAmountDTO.setRefund(visaDAO.getVisaById(firstVisaByServiceOrderId.getId()).getRefund());
                    amount = firstVisaByServiceOrderId.getAmount();
                }
                if ("VA".equals(byId.getType())) {
                    commissionAmountDTO.setRefund(visaDAO.getVisaById(secondVisaByServiceOrderId.getId()).getRefund());
                    amount = secondVisaByServiceOrderId.getAmount();
                    RefundDO secondRefundByVisaId = refundDAO.getRefundByVisaId(secondVisaByServiceOrderId.getId());
                    if (ObjectUtil.isNotNull(secondRefundByVisaId)) {
                        refund += secondRefundByVisaId.getAmount();
                    }
                    commissionAmountDTO.setRefund(refund);
                }
            }
        }
        if ("VISA".equals(packType)) {
            amount += firstVisaByServiceOrderId.getAmount();
            if (installment) {
                amount += secondVisaByServiceOrderId.getAmount();
                RefundDO refundByVisaId = refundDAO.getRefundByVisaId(secondVisaByServiceOrderId.getId());
                if (ObjectUtil.isNotNull(refundByVisaId)) {
                    refund += refundByVisaId.getAmount();
                }
            }
            if (suborder) {
                // 签证600和870计算
                ServiceDO serviceById = serviceDao.getServiceById(serviceOrderById.getServiceId());
                if ("600".equals(serviceById.getCode()) || "870".equals(serviceById.getCode())) {
                    EOICount = 0;
                    deriveOrder = serviceOrderDao.getZiOrder(serviceOrderById.getApplicantParentId());
                    for (ServiceOrderDTO a : deriveOrder) {
                        ServiceDO serviceByIdTmp = serviceDao.getServiceById(a.getServiceId());
                        if ("600".equals(serviceByIdTmp.getCode())) {
                            EOICount++;
                            refund = refund / EOICount;
                        }
                    }
                }
            }
            // 长期签证
            List<String> arrayList = new ArrayList<String>() {
                {
                    this.add("103");
                    this.add("143");
                    this.add("173");
                    this.add("864");
                    this.add("835");
                    this.add("838");
                    this.add("820境内");
                    this.add("489");
                }
            };
            String code = serviceDao.getServiceById(serviceOrderById.getServiceId()).getCode();
            String serviceType = code.replaceAll("[^\\p{L}\\p{N}\\p{Script=Han}]+", "");
            if (arrayList.contains(serviceType)) {
                if (installment) {
                    amount = firstVisaByServiceOrderId.getAmount() + secondVisaByServiceOrderId.getAmount();
                    RefundDO refundByVisaId = refundDAO.getRefundByVisaId(secondVisaByServiceOrderId.getId());
                    if (ObjectUtil.isNotNull(refundByVisaId)) {
                        refund = firstRefundByVisaId.getAmount() + refundByVisaId.getAmount();
                    }
                }
                commissionAmountDTO.setRefund(refund);
                amount = amount * 0.5;
            }
        }
        if ("ZX".equals(packType)) {
            amount = firstVisaByServiceOrderId.getAmount();
            commissionAmountDTO.setRefund(refund);
            if (installment) {
                amount = (firstVisaByServiceOrderId.getAmount() + secondVisaByServiceOrderId.getAmount());
                RefundDO refundByVisaId = refundDAO.getRefundByVisaId(secondVisaByServiceOrderId.getId());
                if (ObjectUtil.isNotNull(refundByVisaId)) {
                    refund = (refund + refundByVisaId.getAmount());
                }
                commissionAmountDTO.setRefund(refund);
            }
        }
        ServiceOrderDO serviceParentOrderById = serviceOrderDao.getServiceOrderById(serviceOrderById.getApplicantParentId());
        if ("CNY".equals(serviceOrderById.getCurrency())) {
            amount = amount / serviceOrderById.getExchangeRate();
        }
        commissionAmountDTO.setRefund(refund); // 设置退款金额
        visaOfficiaCalculate(serviceOrderById, region, commissionAmountDTO, amount, rate, EOICount, officialGradeById, visaOfficialDO, deriveOrder, serviceParentOrderById, installment);
        // EOI订单有删除情况的结算
        if (EOICount > 2) {
            List<VisaOfficialDO> visaOfficialDOS = new ArrayList<>();
            deriveOrder.forEach(e->{
                VisaOfficialDO byServiceOrderId = visaOfficialDao.getByServiceOrderId(e.getId());
                if (ObjectUtil.isNotNull(byServiceOrderId)) {
                    visaOfficialDOS.add(byServiceOrderId);
                }
            });
            if (visaOfficialDOS.size() == EOICount - 1 && EOICount < serviceParentOrderById.getEOINumber()) {
                ServicePackagePriceDO servicePackagePriceDO = servicePackagePriceDAO.getByServiceId(serviceOrderById.getServiceId());
//                double commission = 0.00;
//                VisaOfficialDO visaOfficialDO1 = visaOfficialDOS.stream().max(Comparator.comparingDouble(VisaOfficialDO::getCommissionAmount)).get();
                Double predictCommission = visaOfficialDO.getPredictCommission();
                double sumNew = predictCommission - servicePackagePriceDO.getMaxPrice() / serviceParentOrderById.getEOINumber();
                sumNew = (predictCommission + sumNew) * (EOICount - 1);
                visaOfficialDO.setPredictCommission(sumNew);
                visaOfficialDO.setPredictCommissionCNY(sumNew * visaOfficialDO.getExchangeRate());
            }
        }
        return visaOfficialDO;
    }

    private VisaOfficialDO visaOfficiaCalculate(ServiceOrderDO serviceOrderById, int region, CommissionAmountDTO commissionAmountDTO, double amount, double rate, int EOICount,
                                                OfficialGradeDO officialGradeById, VisaOfficialDO visaOfficialDO, List<ServiceOrderDTO> deriveOrder, ServiceOrderDO serviceParentOrderById, boolean installment) throws ServiceException {
        ServicePackagePriceDO servicePackagePriceDO = servicePackagePriceDAO.getByServiceId(serviceOrderById.getServiceId());

        if (region == 1) {
            double quarterExchangeRate = exchangeRateService.getQuarterExchangeRate();
            visaOfficialDO.setExchangeRate(quarterExchangeRate);
        }
        if (servicePackagePriceDO == null) {
            commissionAmountDTO.setThirdPrince(0.00);
            commissionAmountDTO.setRuler(0);
        } else {
            ServicePackagePriceV2DTO servicePackagePriceV2DTO = closeJugd(serviceOrderById.getOfficialId(), servicePackagePriceDO);
            commissionAmountDTO.setThirdPrince(servicePackagePriceDO.getThirdPrince());
            servicePackagePriceDO.setAmount(servicePackagePriceV2DTO.getAmount());
            commissionAmountDTO.setRuler(servicePackagePriceV2DTO.getRuler());
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (commissionAmountDTO.getRuler() == 0) {
            double predictCommissionAmount = 0.00;
            ServiceOrderDO serviceOrderById1 = serviceOrderDao.getServiceOrderById(serviceOrderById.getApplicantParentId());
            boolean isSIV = false;
            double bingdingOrderAmount = 0.00;
            if (ObjectUtil.isNotNull(serviceOrderById1)) {
                isSIV = "SIV".equals(serviceOrderById1.getType()) && serviceOrderById.getEOINumber() != null;
            }
            if (serviceOrderById.getBindingOrder() != null && serviceOrderById.getBindingOrder() > 0) {
                List<Integer> listbindingOrder = serviceOrderDao.listBybindingOrder(serviceOrderById.getBindingOrder());
                if (!listbindingOrder.isEmpty()) {
                    for (Integer a : listbindingOrder) {
                        bingdingOrderAmount += servicePackagePriceDAO.getByServiceId(a).getCostPrince();
                    }
                    if (installment) {
                        bingdingOrderAmount = bingdingOrderAmount * 0.5;
                    }
                }
            }
            if (isSIV) {
                List<VisaOfficialDO> visaOfficialDOS = new ArrayList<>();
                for (ServiceOrderDTO a : deriveOrder) {
                    deriveOrder.forEach(e->{
                        VisaOfficialDO byServiceOrderId = visaOfficialDao.getByServiceOrderId(a.getId());
                        if (ObjectUtil.isNotNull(byServiceOrderId)) {
                            visaOfficialDOS.add(byServiceOrderId);
                        }
                    });
                }
                ServicePackagePriceDO byServiceId = servicePackagePriceDAO.getByServiceId(25);
                if (visaOfficialDOS.isEmpty()) {
                    predictCommissionAmount = ((amount - commissionAmountDTO.getRefund()) / 1.1 - byServiceId.getMaxPrice() - bingdingOrderAmount) + byServiceId.getMaxPrice() / EOICount;
                } else {
                    predictCommissionAmount = byServiceId.getMaxPrice() / EOICount;
                }
            } else {
                predictCommissionAmount = (amount - commissionAmountDTO.getRefund()) / 1.1 - servicePackagePriceDO.getCostPrince()  - bingdingOrderAmount - servicePackagePriceDO.getThirdPrince();
            }
            commissionAmountDTO.setPredictCommissionAmount(predictCommissionAmount);
            if (commissionAmountDTO.getPredictCommissionAmount() <= 0) {
                commissionAmountDTO.setPredictCommissionAmount(0.00);
            }
            commissionAmountDTO.setCommissionAmount(commissionAmountDTO.getPredictCommissionAmount());
            commissionAmountDTO.setCommission((commissionAmountDTO.getPredictCommissionAmount() * (rate / 100)) / EOICount);
            if (isSIV) {
                commissionAmountDTO.setCommission((commissionAmountDTO.getPredictCommissionAmount() * (rate / 100)));
            }
            String calculation = new String();
            calculation = "0" + "|" + commissionAmountDTO.getThirdPrince() + "|" + dateFormat.format(servicePackagePriceDO == null ? System.currentTimeMillis() : servicePackagePriceDO.getGmtModify()) + "|" + officialGradeById.getGrade() + "," + rate + "%" + "," + dateFormat.format(officialGradeById.getGmtModify());
            commissionAmountDTO.setCalculation(calculation);
            visaOfficialDO.setPredictCommission(commissionAmountDTO.getCommission());
            visaOfficialDO.setPredictCommissionCNY(visaOfficialDO.getPredictCommission() * visaOfficialDO.getExchangeRate());
        }
        if (commissionAmountDTO.getRuler() == 1) {
            commissionAmountDTO.setCommission(servicePackagePriceDO.getAmount() / EOICount);
            commissionAmountDTO.setThirdPrince(servicePackagePriceDO.getThirdPrince());
            String calculation = new String();
            calculation = "1" + "|" + commissionAmountDTO.getThirdPrince() + "," + servicePackagePriceDO.getAmount() + "|" + dateFormat.format(servicePackagePriceDO.getGmtModify());
            commissionAmountDTO.setCalculation(calculation);
            visaOfficialDO.setPredictCommission(commissionAmountDTO.getCommission());
            visaOfficialDO.setPredictCommissionCNY(visaOfficialDO.getPredictCommission() * visaOfficialDO.getExchangeRate());
            if (region == 1) {
                visaOfficialDO.setPredictCommissionCNY(commissionAmountDTO.getCommission());
                visaOfficialDO.setPredictCommission(visaOfficialDO.getPredictCommission() / visaOfficialDO.getExchangeRate());
            }
        }
        visaOfficialDO.setPredictCommissionAmount(commissionAmountDTO.getPredictCommissionAmount() / EOICount);
        visaOfficialDO.setCommissionAmount(commissionAmountDTO.getCommissionAmount() / EOICount);
        visaOfficialDO.setCalculation(commissionAmountDTO.getCalculation());
        return visaOfficialDO;
    }




//    @Override
//    public VisaOfficialDO buildVisa(VisaOfficialDTO visaOfficialDTO) throws ServiceException {
//        if (visaOfficialDTO == null) {
//            ServiceException se = new ServiceException("visaOfficialDto is null !");
//            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
//            throw se;
//        }
//        if (visaOfficialDao.countVisaByServiceOrderIdAndExcludeCode(visaOfficialDTO.getServiceOrderId(), visaOfficialDTO.getCode()) > 0) {
//            ServiceException se = new ServiceException("已创建过佣金订单,不能重复创建!");
//            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
//            throw se;
//        }
//        boolean suborder = false; // 判断父子订单
//        int region = 0; // 判断所属地区 0：澳洲 1：中国
//        int orderType = 0; // 判断订单类型： 0：打包签证 1：雇主担保 2：单一签证 3：留学服务 4：咨询服务
//        boolean pay = false; // 判断是否支付
//        // 判断是否是父子订单
//        ServiceOrderDO serviceOrderById = serviceOrderDao.getServiceOrderById(visaOfficialDTO.getServiceOrderId());
//        int applicantId = serviceOrderById.getApplicantParentId();
//        ServiceOrderDO serviceOrderByApplicantId = serviceOrderDao.getServiceOrderById(applicantId);
//        if (ObjectUtil.isNotNull(serviceOrderByApplicantId)) {
//            suborder = true;
//        }
//        // 判断当前文案地区为澳洲还是中国
//        RegionDO regionById = regionDAO.getRegionById(officialDAO.getOfficialById(visaOfficialDTO.getOfficialId()).getRegionId());
//        String regionName = regionById.getName().replaceAll("[^\u4e00-\u9fa5]", "");
//        if (StringUtil.isNotEmpty(regionName)) {
//            region = 1;
//        }
//        // 判断当前订单类型
//        String typeTmp = serviceOrderById.getType();
//        if (suborder) {
//            typeTmp = serviceOrderByApplicantId.getType();
//        }
//        switch (typeTmp) {
//            case "SIV":
//                orderType = 0; // 打包签证
//                break;
//            case "NSV":
//                orderType = 1; // 雇主担保
//                break;
//            case "VISA":
//                orderType = 2; // 单一签证
//                break;
//            case "OVST":
//                orderType = 3;
//                break;
//            case "ZX":
//                orderType = 4; // 咨询
//                break;
//            default:
//                orderType = 3;
//        }
//        // 判断订单是否支付
//        pay = serviceOrderById.isPay();
//        if (suborder) {
//            pay = serviceOrderByApplicantId.isPay();
//        }
//        String packType = "";
//        VisaOfficialDO visaOfficialDO = new VisaOfficialDO();
//        // 打包签证结算
//        if (orderType == 0) {
//            // 判断子订单中所有类型
//            List<ServiceOrderDTO> deriveOrder = serviceOrderDao.getDeriveOrder(serviceOrderById.getApplicantParentId());
//            List<String> deriveOrderTypes = new ArrayList<>();
//            deriveOrder.forEach(e->{
//                ServicePackageDO byId = servicePackageDAO.getById(e.getServicePackageId());
//                deriveOrderTypes.add(byId.getType());
//            });
//            // 只有签证服务
//            if (deriveOrderTypes.contains("VA") && !deriveOrderTypes.contains("EOI") && !deriveOrderTypes.contains("ROI")) {
//                packType = "VA";
//                visaOfficialDO = buildCommission(serviceOrderById, visaOfficialDTO, pay, region, packType, suborder);
//            }
//            // EOI、ROI、签证同时存在
//            if (deriveOrderTypes.contains("VA") && deriveOrderTypes.contains("EOI") || deriveOrderTypes.contains("ROI")) {
//                packType = "EOI";
//                visaOfficialDO = buildCommission(serviceOrderById, visaOfficialDTO, pay, region, packType, suborder);
//                if (ObjectUtil.isNull(visaOfficialDO)) {
//                    return null;
//                }
//            }
//            // 只有ROI、签证
//            if (deriveOrderTypes.contains("VA") && !deriveOrderTypes.contains("EOI") && deriveOrderTypes.contains("ROI")) {
//                packType = "ROI";
//                visaOfficialDO = buildCommission(serviceOrderById, visaOfficialDTO, pay, region, packType, suborder);
//            }
//        }
//        // 雇主担保结算
//        if (orderType == 1) {
//            ServicePackageDO byId = servicePackageDAO.getById(serviceOrderById.getServicePackageId());
//            visaOfficialDO = buildCommission(serviceOrderById, visaOfficialDTO, pay, region, typeTmp, suborder);
//        }
//        // 单一签证
//        if (orderType == 2) {
//            visaOfficialDO = buildCommission(serviceOrderById, visaOfficialDTO, pay, region, typeTmp, suborder);
//        }
//        // 咨询
//        if (orderType == 4) {
//            visaOfficialDO = buildCommission(serviceOrderById, visaOfficialDTO, pay, region, typeTmp, suborder);
//        }
//        visaOfficialDO.setOfficialRegion(region);
//
//        return visaOfficialDO;
//    }


//    @Override
//    public int addVisa(VisaOfficialDTO visaOfficialDTO) throws ServiceException {
//        if (visaOfficialDTO == null) {
//            ServiceException se = new ServiceException("visaOfficialDto is null !");
//            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
//            throw se;
//        }
//        if (visaOfficialDao.countVisaByServiceOrderIdAndExcludeCode(visaOfficialDTO.getServiceOrderId(), visaOfficialDTO.getCode()) > 0) {
//                ServiceException se = new ServiceException("已创建过佣金订单,不能重复创建!");
//                se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
//                throw se;
//        }
//        int applicantParentId = serviceOrderDao.getServiceOrderById(visaOfficialDTO.getServiceOrderId()).getApplicantParentId();
//        List<ApplicantListDO> applicantListDOS = new ArrayList<ApplicantListDO>();
//        if (applicantParentId!=0)
//            applicantListDOS = serviceOrderDao.ApplicantListByServiceOrderId(serviceOrderDao.getServiceOrderById(visaOfficialDTO.getServiceOrderId()).getApplicantParentId());
//        else
//            applicantListDOS = serviceOrderDao.ApplicantListByServiceOrderId(visaOfficialDTO.getServiceOrderId());
//        int i=0;
//        for (ApplicantListDO applicantListDO : applicantListDOS) {
//             i =i+ visaOfficialDao.countVisaByServiceOrderId(applicantListDO.getId());
//        }
//        if (applicantParentId !=0 && "SIV".equals(serviceOrderDao.getServiceOrderById(applicantParentId).getType())) {
//            i = -1;
//        }
//        if (applicantParentId !=0 && "NSV".equals(serviceOrderDao.getServiceOrderById(applicantParentId).getType())) {
//            i = -2;
//        }
//        if (i > 0 ) {
//            return 0;
//        }
//        // 判断文案所属国家
//        RegionDO regionById = regionDAO.getRegionById(officialDAO.getOfficialById(visaOfficialDTO.getOfficialId()).getRegionId());
//        String regionName = regionById.getName().replaceAll("[^\u4e00-\u9fa5]", "");
//        try {
//            Calendar calendar = Calendar.getInstance();
//            Double rate = 0.00;
//            VisaOfficialDO visaOfficialDO = mapper.map(visaOfficialDTO, VisaOfficialDO.class);
//            ServiceOrderDO serviceOrderDO = serviceOrderDao.getServiceOrderById(visaOfficialDTO.getServiceOrderId());
//            ServiceOrderDO serviceParentOrder = serviceOrderDao.getServiceOrderById(serviceOrderDO.getParentId());
//            boolean pay = false;
//            boolean chinaFixedAmount = false;
//            ServicePackagePriceV2DTO servicePackagePriceV2DTO = new ServicePackagePriceV2DTO();
//            if (ObjectUtil.isNotNull(serviceParentOrder)) {
//                pay = !serviceParentOrder.isPay();
//            }
//            if (!serviceOrderDO.isPay() || serviceOrderDO.getReceivable() == 0) {
//                pay = !serviceOrderDO.isPay();
//            }
//            List<ServiceOrderDO> list = new ArrayList<>();
//            list = serviceOrderDao.listByParentId(serviceOrderDO.getId());
//            List<Integer> monthlist = new ArrayList<Integer>() {
//                {
//                    this.add(1);
//                    this.add(2);
//                    this.add(3);
//                    this.add(7);
//                    this.add(8);
//                    this.add(9);
//                }
//            };
//            List<String> arrayList = new ArrayList<String>() {
//                {
//                    this.add("103");
//                    this.add("143");
//                    this.add("173");
//                    this.add("864");
//                    this.add("835");
//                    this.add("838");
//                    this.add("820境内");
//                    this.add("489");
//                }
//            };
//            String code = serviceDao.getServiceById(visaOfficialDTO.getServiceId()).getCode();
//            String serviceType = code.replaceAll("\\D", "");
//            //无付费服务订单结算规则
//            if ((serviceOrderDO.getParentId()==0&&!serviceOrderDO.isPay())&&(ObjectUtil.isNotNull(servicePackagePriceDAO.getByServiceId(serviceOrderDO.getServiceId()))&&servicePackagePriceDAO.getByServiceId(serviceOrderDO.getServiceId()).getRuler()==1)){
//                ServicePackagePriceDO servicePackagePriceDO = servicePackagePriceDAO.getByServiceId(serviceOrderDO.getServiceId());
//                pay = isaBoolean(servicePackagePriceDO, pay);
//                // todo 结算方式判断
////                servicePackagePriceV2DTO = closeJugd(visaOfficialDTO.getOfficialId(), servicePackagePriceDO);
//                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                CommissionAmountDTO commissionAmountDTO = new CommissionAmountDTO();
//                commissionAmountDTO.setCommission(servicePackagePriceDO.getAmount());
//                commissionAmountDTO.setThirdPrince(servicePackagePriceDO.getThirdPrince());
//                String calculation = new String();
//                calculation = "1" + "|" + commissionAmountDTO.getThirdPrince() + "," + servicePackagePriceDO.getAmount() + "|" + dateFormat.format(servicePackagePriceDO.getGmtModify());
//                commissionAmountDTO.setCalculation(calculation);
//                visaOfficialDO.setPredictCommissionAmount(commissionAmountDTO.getPredictCommissionAmount());
//                visaOfficialDO.setCommissionAmount(commissionAmountDTO.getCommissionAmount());
//                visaOfficialDO.setPredictCommission(commissionAmountDTO.getCommission());
//                visaOfficialDO.setCalculation(commissionAmountDTO.getCalculation());
//            }
//            //长期签证计算
//            else if (arrayList.contains(serviceType)) {
//                VisaDO visaDO = new VisaDO();
//                CommissionAmountDTO commissionAmountDTO = new CommissionAmountDTO();
//                if (serviceOrderDO.getParentId() == 0) {
//                    visaDO = visaDAO.getFirstVisaByServiceOrderId(serviceOrderDO.getId());
//                } else {
//                    visaDO = visaDAO.getFirstVisaByServiceOrderId(serviceOrderDO.getParentId());
//                }
//                if (ObjectUtil.isNotNull(visaOfficialDao.getByServiceOrderId(visaOfficialDTO.getServiceOrderId()))) {
//                    visaDO = visaDAO.getSecondVisaByServiceOrderId(serviceOrderDO.getId());
//                }
//                RefundDO refund = refundDAO.getRefundByVisaId(visaDO.getId());
//                if (refund == null) {
//                    commissionAmountDTO.setRefund(0.00);
//                } else {
//                    commissionAmountDTO.setRefund(refund.getAmount());
//                }
//                OfficialDO official = officialDAO.getOfficialById(serviceOrderDO.getOfficialId());
//                OfficialGradeDO grade = officialGradeDao.getOfficialGradeById(official.getGradeId());
//                if (grade == null) {
//                    ServiceException se = new ServiceException("请绑定文案等级 !");
//                    se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
//                    throw se;
//                }
//                rate = grade.getRate();
//                if (StringUtil.isEmpty(regionName)) {
//                    if (monthlist.contains(calendar.get(Calendar.MONTH) + 1)) {
//                        rate = grade.getRate() + 3;
//                    } else {
//                        rate = grade.getRate();
//                    }
//                }
//                ServicePackagePriceDO servicePackagePriceDO = servicePackagePriceDAO.getByServiceId(serviceOrderDO.getServiceId());
//                if (servicePackagePriceDO == null) {
//                    commissionAmountDTO.setThirdPrince(0.00);
//                    commissionAmountDTO.setRuler(0);
//                } else {
//                    pay = isaBoolean(servicePackagePriceDO, pay);
//                    //  结算方式判断
//                    servicePackagePriceV2DTO = closeJugd(visaOfficialDTO.getOfficialId(), servicePackagePriceDO);
//                    commissionAmountDTO.setThirdPrince(servicePackagePriceDO.getThirdPrince());
//                    commissionAmountDTO.setRuler(servicePackagePriceV2DTO.getRuler());
//                }
//                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                if (commissionAmountDTO.getRuler() == 0) {
//                    commissionAmountDTO.setPredictCommissionAmount(((serviceOrderDO.getAmount() - commissionAmountDTO.getRefund() - commissionAmountDTO.getThirdPrince()) * 0.5)/1.1);
//                    if (commissionAmountDTO.getPredictCommissionAmount() <= 0)
//                        commissionAmountDTO.setPredictCommissionAmount(0.00);
//                    commissionAmountDTO.setCommissionAmount(commissionAmountDTO.getPredictCommissionAmount());
//                    commissionAmountDTO.setCommission(commissionAmountDTO.getPredictCommissionAmount() * (rate / 100));
//                    String calculation = new String();
//                    calculation = "0" + "|" + commissionAmountDTO.getThirdPrince() + "|" + dateFormat.format(servicePackagePriceDO == null ? System.currentTimeMillis() : servicePackagePriceDO.getGmtModify()) + "|" + grade.getGrade() + "," + rate + "%" + "," + dateFormat.format(grade.getGmtModify());
//                    commissionAmountDTO.setCalculation(calculation);
//                } else {
//                    if (StringUtil.isNotEmpty(regionName)) {
//                        chinaFixedAmount = true;
//                    }
//                    commissionAmountDTO.setCommission(servicePackagePriceV2DTO.getAmount());
//                    commissionAmountDTO.setThirdPrince(servicePackagePriceDO.getThirdPrince());
//                    String calculation = new String();
//                    calculation = "1" + "|" + commissionAmountDTO.getThirdPrince() + "," + servicePackagePriceDO.getAmount() + "|" + dateFormat.format(servicePackagePriceDO.getGmtModify());
//                    commissionAmountDTO.setCalculation(calculation);
//                }
//                visaOfficialDO.setPredictCommissionAmount(commissionAmountDTO.getPredictCommissionAmount());
//                visaOfficialDO.setCommissionAmount(commissionAmountDTO.getCommissionAmount());
//                visaOfficialDO.setPredictCommission(commissionAmountDTO.getCommission());
//                visaOfficialDO.setCalculation(commissionAmountDTO.getCalculation());
//            }
//            //常规计算
//            else if (serviceOrderDO.getParentId() == 0 && list.size() == 0) {
//                CommissionAmountDTO commissionAmountDTO = calculationCommissionAmount(serviceOrderDO.getId(), "", regionName);
//                if (commissionAmountDTO.isChinaFixedAmount()) {
//                    chinaFixedAmount = true;
//                }
//                visaOfficialDO.setPredictCommissionAmount(commissionAmountDTO.getPredictCommissionAmount());
//                visaOfficialDO.setCommissionAmount(commissionAmountDTO.getCommissionAmount());
//                visaOfficialDO.setPredictCommission(commissionAmountDTO.getCommission());
//                visaOfficialDO.setCalculation(commissionAmountDTO.getCalculation());
//            }
//            //打包服务计算
//            else if (serviceOrderDO.getServicePackageId() != 0) {
//                if (i == -1) {
//                    String situationType =  situationJudgment(serviceOrderDO);
//                    if (StringUtil.isNotEmpty(situationType)) {
//                        // 子订单中只有签证
//                        if (situationType.contains("VA") && !situationType.contains("EOI") && !situationType.contains("ROI")) {
//                            if ("VA".equals(servicePackageDAO.getById(serviceOrderDO.getServicePackageId()).getType())) {
//                                CommissionAmountDTO commissionAmountDTO = calculationCommissionAmount(serviceOrderDO.getId(), "VA", regionName);
//                                if (commissionAmountDTO.isChinaFixedAmount()) {
//                                    chinaFixedAmount = true;
//                                }
//                                visaOfficialDO.setPredictCommissionAmount(commissionAmountDTO.getPredictCommissionAmount());
//                                visaOfficialDO.setCommissionAmount(commissionAmountDTO.getCommissionAmount());
//                                visaOfficialDO.setPredictCommission(commissionAmountDTO.getCommission());
//                                visaOfficialDO.setCalculation(commissionAmountDTO.getCalculation());
//                            }
//                        }
//                        // 子订单中有EOI或者ROI没有签证
//                        if (!situationType.contains("VA") && (situationType.contains("EOI") || situationType.contains("ROI"))) {
//                            CommissionAmountDTO commissionAmountDTO = new CommissionAmountDTO();
//                            if (situationType.contains("EOI") && situationType.contains("ROI")) {
//                                if ("ROI".equals(servicePackageDAO.getById(serviceOrderDO.getServicePackageId()).getType())) {
//                                    return -2;
//                                }
//                            }
//                            if (situationType.contains("EOI") || situationType.contains("ROI")) {
//                                commissionAmountDTO = calculationCommissionAmount(serviceOrderDO.getId(), "OI", regionName);
//                                if (commissionAmountDTO.isChinaFixedAmount()) {
//                                    chinaFixedAmount = true;
//                                }
//                            }
//                            visaOfficialDO.setPredictCommissionAmount(commissionAmountDTO.getPredictCommissionAmount());
//                            visaOfficialDO.setCommissionAmount(commissionAmountDTO.getCommissionAmount());
//                            visaOfficialDO.setPredictCommission(commissionAmountDTO.getCommission());
//                            visaOfficialDO.setCalculation(commissionAmountDTO.getCalculation());
//                        }
//                        // 子订单中有签证有ROI或者有EOI
//                        if (situationType.contains("VA") && (situationType.contains("EOI") || situationType.contains("ROI"))) {
//                            CommissionAmountDTO commissionAmountDTO = new CommissionAmountDTO();
//                            if (situationType.contains("EOI") && situationType.contains("ROI")) {
//                                if ("ROI".equals(servicePackageDAO.getById(serviceOrderDO.getServicePackageId()).getType())) {
//                                    return -2;
//                                }
//                            }
//                            if (situationType.contains("EOI") || situationType.contains("ROI")) {
//                                commissionAmountDTO = calculationCommissionAmount(serviceOrderDO.getId(), "VAOI", regionName);
//                                if (commissionAmountDTO.isChinaFixedAmount()) {
//                                    chinaFixedAmount = true;
//                                }
//                            }
//                            visaOfficialDO.setPredictCommissionAmount(commissionAmountDTO.getPredictCommissionAmount());
//                            visaOfficialDO.setCommissionAmount(commissionAmountDTO.getCommissionAmount());
//                            visaOfficialDO.setPredictCommission(commissionAmountDTO.getCommission());
//                            visaOfficialDO.setCalculation(commissionAmountDTO.getCalculation());
//                        }
//                    } else {
//                        throw new ServiceException("该订单服务包错误");
//                    }
//
//                } else {
//                    ServicePackageDO packageDO = servicePackageDAO.getById(serviceOrderDO.getServicePackageId());
//                    //eoi计算
//                    if (packageDO.getType().equals("EOI")) {
//                        VisaDO visaDO = new VisaDO();
//                        RefundDO refund = new RefundDO();
//                        CommissionAmountDTO commissionAmountDTO = new CommissionAmountDTO();
//                        visaDO = visaDAO.getFirstVisaByServiceOrderId(serviceOrderDO.getParentId());
//                        if (ObjectUtil.isNotNull(visaDO) && pay) {
//                            refund = refundDAO.getRefundByVisaId(visaDO.getId());
//                        }
////                    else {
////                        return -1;
////                    }
//                        if (refund == null) {
//                            commissionAmountDTO.setRefund(0.00);
//                        } else {
//                            commissionAmountDTO.setRefund(refund.getAmount());
//                        }
//                        OfficialDO official = officialDAO.getOfficialById(serviceOrderDO.getOfficialId());
//                        OfficialGradeDO grade = officialGradeDao.getOfficialGradeById(official.getGradeId());
//                        if (grade == null) {
//                            ServiceException se = new ServiceException("请绑定文案等级 !");
//                            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
//                            throw se;
//                        }
//                        rate = grade.getRate();
//                        if (StringUtil.isEmpty(regionName)) {
//                            if (monthlist.contains(calendar.get(Calendar.MONTH) + 1)) {
//                                rate = grade.getRate() + 3;
//                            } else {
//                                rate = grade.getRate();
//                            }
//                        }
//                        ServicePackagePriceDO servicePackagePriceDO = servicePackagePriceDAO.getByServiceId(serviceOrderDO.getServiceId());
//                        if (servicePackagePriceDO == null) {
//                            commissionAmountDTO.setThirdPrince(0.00);
//                            commissionAmountDTO.setRuler(0);
//                        } else {
//                            pay = isaBoolean(servicePackagePriceDO, pay);
//                            servicePackagePriceV2DTO = closeJugd(visaOfficialDTO.getOfficialId(), servicePackagePriceDO);
//                            commissionAmountDTO.setThirdPrince(servicePackagePriceDO.getThirdPrince());
//                            commissionAmountDTO.setRuler(servicePackagePriceV2DTO.getRuler());
//                        }
//                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                        if (commissionAmountDTO.getRuler() == 0) {
//                            if (ObjectUtil.isNull(visaDO)) {
//                                visaDO = new VisaDO();
//                                visaDO.setAmount(0);
//                            }
//                            commissionAmountDTO.setPredictCommissionAmount((visaDO.getAmount() - commissionAmountDTO.getRefund() - commissionAmountDTO.getThirdPrince())/1.1);
//                            if (commissionAmountDTO.getPredictCommissionAmount() <= 0)
//                                commissionAmountDTO.setPredictCommissionAmount(0.00);
//                            commissionAmountDTO.setCommissionAmount(commissionAmountDTO.getPredictCommissionAmount());
//                            commissionAmountDTO.setCommission(commissionAmountDTO.getPredictCommissionAmount() * (rate / 100));
//                            String calculation = new String();
//                            calculation = "0" + "|" + commissionAmountDTO.getThirdPrince() + "|" + dateFormat.format(servicePackagePriceDO == null ? System.currentTimeMillis() : servicePackagePriceDO.getGmtModify()) + "|" + grade.getGrade() + "," + rate + "%" + "," + dateFormat.format(grade.getGmtModify());
//                            commissionAmountDTO.setCalculation(calculation);
//                        } else {
//                            if (StringUtil.isNotEmpty(regionName)) {
//                                chinaFixedAmount = true;
//                            }
//                            commissionAmountDTO.setCommission(servicePackagePriceV2DTO.getAmount());
//                            commissionAmountDTO.setThirdPrince(servicePackagePriceDO.getThirdPrince());
//                            String calculation = new String();
//                            calculation = "1" + "|" + commissionAmountDTO.getThirdPrince() + "," + servicePackagePriceDO.getAmount() + "|" + dateFormat.format(servicePackagePriceDO.getGmtModify());
//                            commissionAmountDTO.setCalculation(calculation);
//                        }
//                        visaOfficialDO.setPredictCommissionAmount(commissionAmountDTO.getPredictCommissionAmount());
//                        visaOfficialDO.setCommissionAmount(commissionAmountDTO.getCommissionAmount());
//                        visaOfficialDO.setPredictCommission(commissionAmountDTO.getCommission());
//                        visaOfficialDO.setCalculation(commissionAmountDTO.getCalculation());
//                    }
//                    //签证服务计算
//                    if (packageDO.getType().equals("VA") && i != -2) {
//                        if (serviceOrderDao.getServiceOrderById(serviceOrderDO.getParentId()).isPay()) {
//                            VisaDO visaDO = new VisaDO();
//                            CommissionAmountDTO commissionAmountDTO = new CommissionAmountDTO();
//                            if (serviceOrderDO.getReceivable() == serviceOrderDO.getReceived()) {
//                                visaDO = visaDAO.getVisaByServiceOrderId(serviceOrderDO.getParentId());
//                            } else {
//                                return -1;
//                            }
//                            RefundDO refund = refundDAO.getRefundByVisaId(visaDO.getId());
//                            if (refund == null) {
//                                commissionAmountDTO.setRefund(0.00);
//                            } else {
//                                commissionAmountDTO.setRefund(refund.getAmount());
//                            }
//                            OfficialDO official = officialDAO.getOfficialById(serviceOrderDO.getOfficialId());
//                            OfficialGradeDO grade = officialGradeDao.getOfficialGradeById(official.getGradeId());
//                            if (grade == null) {
//                                ServiceException se = new ServiceException("请绑定文案等级 !");
//                                se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
//                                throw se;
//                            }
//                            rate = grade.getRate();
//                            if (StringUtil.isEmpty(regionName)) {
//                                if (monthlist.contains(calendar.get(Calendar.MONTH) + 1)) {
//                                    rate = grade.getRate() + 3;
//                                } else {
//                                    rate = grade.getRate();
//                                }
//                            }
//                            ServicePackagePriceDO servicePackagePriceDO = servicePackagePriceDAO.getByServiceId(serviceOrderDO.getServiceId());
//                            if (servicePackagePriceDO == null) {
//                                commissionAmountDTO.setThirdPrince(0.00);
//                                commissionAmountDTO.setRuler(0);
//                            } else {
//                                pay = isaBoolean(servicePackagePriceDO, pay);
//                                servicePackagePriceV2DTO = closeJugd(visaOfficialDTO.getOfficialId(), servicePackagePriceDO);
//                                commissionAmountDTO.setThirdPrince(servicePackagePriceDO.getThirdPrince());
//                                commissionAmountDTO.setRuler(servicePackagePriceV2DTO.getRuler());
//                            }
//                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                            if (commissionAmountDTO.getRuler() == 0) {
//                                commissionAmountDTO.setPredictCommissionAmount((visaDO.getAmount() - commissionAmountDTO.getRefund() - commissionAmountDTO.getThirdPrince())/1.1);
//                                if (commissionAmountDTO.getPredictCommissionAmount() <= 0)
//                                    commissionAmountDTO.setPredictCommissionAmount(0.00);
//                                commissionAmountDTO.setCommissionAmount(commissionAmountDTO.getPredictCommissionAmount());
//                                commissionAmountDTO.setCommission(commissionAmountDTO.getPredictCommissionAmount() * (rate / 100));
//                                String calculation = new String();
//                                calculation = "0" + "|" + commissionAmountDTO.getThirdPrince() + "|" + dateFormat.format(servicePackagePriceDO == null ? System.currentTimeMillis() : servicePackagePriceDO.getGmtModify()) + "|" + grade.getGrade() + "," + rate + "%" + "," + dateFormat.format(grade.getGmtModify());
//                                commissionAmountDTO.setCalculation(calculation);
//                            } else {
//                                if (StringUtil.isNotEmpty(regionName)) {
//                                    chinaFixedAmount = true;
//                                }
//                                commissionAmountDTO.setCommission(servicePackagePriceV2DTO.getAmount());
//                                commissionAmountDTO.setThirdPrince(servicePackagePriceDO.getThirdPrince());
//                                String calculation = new String();
//                                calculation = "1" + "|" + commissionAmountDTO.getThirdPrince() + "," + servicePackagePriceDO.getAmount() + "|" + dateFormat.format(servicePackagePriceDO.getGmtModify());
//                                commissionAmountDTO.setCalculation(calculation);
//                            }
//                            visaOfficialDO.setPredictCommissionAmount(commissionAmountDTO.getPredictCommissionAmount());
//                            visaOfficialDO.setCommissionAmount(commissionAmountDTO.getCommissionAmount());
//                            visaOfficialDO.setPredictCommission(commissionAmountDTO.getCommission());
//                            visaOfficialDO.setCalculation(commissionAmountDTO.getCalculation());
//                        }
//
//                    }
//                    //雇主提名
//                    if (i == -2) {
//                        if (serviceOrderDao.getServiceOrderById(serviceOrderDO.getParentId()).isPay()) {
//                            VisaDO visaDO = new VisaDO();
//                            CommissionAmountDTO commissionAmountDTO = new CommissionAmountDTO();
//                            ServicePackageDO servicePackageDO = servicePackageDAO.getById(serviceOrderDO.getServicePackageId());
////                            if ("TM".equals(servicePackageDO.getType())) {
////                                visaDO = visaDAO.getFirstVisaByServiceOrderId(serviceOrderDO.getParentId());
////                            }
////                            if ("VA".equals(servicePackageDO.getType())) {
////                                visaDO = visaDAO.getSecondVisaByServiceOrderId(serviceOrderDO.getParentId());
////                            }
//                            visaDO = visaDAO.getFirstVisaByServiceOrderId(serviceOrderDO.getParentId());
//                            VisaDO secondVisa = visaDAO.getSecondVisaByServiceOrderId(serviceOrderDO.getParentId());
//                            if (ObjectUtil.isNotNull(secondVisa)) {
//                                visaDO.setAmount(visaDO.getAmount() + secondVisa.getAmount());
//                            }
//                            RefundDO refund = refundDAO.getRefundByVisaId(visaDO.getId());
//                            if (refund == null) {
//                                commissionAmountDTO.setRefund(0.00);
//                            } else {
//                                commissionAmountDTO.setRefund(refund.getAmount());
//                            }
//                            OfficialDO official = officialDAO.getOfficialById(serviceOrderDO.getOfficialId());
//                            OfficialGradeDO grade = officialGradeDao.getOfficialGradeById(official.getGradeId());
//                            if (grade == null) {
//                                ServiceException se = new ServiceException("请绑定文案等级 !");
//                                se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
//                                throw se;
//                            }
//                            rate = grade.getRate();
//                            if (StringUtil.isEmpty(regionName)) {
//                                if (monthlist.contains(calendar.get(Calendar.MONTH) + 1)) {
//                                    rate = grade.getRate() + 3;
//                                } else {
//                                    rate = grade.getRate();
//                                }
//                            }
//                            ServicePackagePriceDO servicePackagePriceDO = servicePackagePriceDAO.getByServiceId(serviceOrderDO.getServiceId());
//                            if (servicePackagePriceDO == null) {
//                                commissionAmountDTO.setThirdPrince(0.00);
//                                commissionAmountDTO.setRuler(0);
//                            } else {
//                                pay = isaBoolean(servicePackagePriceDO, pay);
//                                servicePackagePriceV2DTO = closeJugd(visaOfficialDTO.getOfficialId(), servicePackagePriceDO);
//                                commissionAmountDTO.setThirdPrince(servicePackagePriceDO.getThirdPrince());
//                                commissionAmountDTO.setRuler(servicePackagePriceV2DTO.getRuler());
//                            }
//                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                            if (commissionAmountDTO.getRuler() == 0) {
//                                commissionAmountDTO.setPredictCommissionAmount((visaDO.getAmount() - commissionAmountDTO.getRefund() - commissionAmountDTO.getThirdPrince()) / 1.1 * 0.5);
//                                if (commissionAmountDTO.getPredictCommissionAmount() <= 0)
//                                    commissionAmountDTO.setPredictCommissionAmount(0.00);
//                                commissionAmountDTO.setCommissionAmount(commissionAmountDTO.getPredictCommissionAmount());
//                                commissionAmountDTO.setCommission(commissionAmountDTO.getPredictCommissionAmount() * (rate / 100) * 0.5);
//                                String calculation = new String();
//                                calculation = "0" + "|" + commissionAmountDTO.getThirdPrince() + "|" + dateFormat.format(servicePackagePriceDO == null ? System.currentTimeMillis() : servicePackagePriceDO.getGmtModify()) + "|" + grade.getGrade() + "," + rate + "%" + "," + dateFormat.format(grade.getGmtModify());
//                                commissionAmountDTO.setCalculation(calculation);
//                            } else {
//                                if (StringUtil.isNotEmpty(regionName)) {
//                                    chinaFixedAmount = true;
//                                }
//                                commissionAmountDTO.setCommission(servicePackagePriceV2DTO.getAmount());
//                                commissionAmountDTO.setThirdPrince(servicePackagePriceDO.getThirdPrince());
//                                String calculation = new String();
//                                calculation = "1" + "|" + commissionAmountDTO.getThirdPrince() + "," + servicePackagePriceDO.getAmount() + "|" + dateFormat.format(servicePackagePriceDO.getGmtModify());
//                                commissionAmountDTO.setCalculation(calculation);
//                            }
//                            visaOfficialDO.setPredictCommissionAmount(commissionAmountDTO.getPredictCommissionAmount());
//                            visaOfficialDO.setCommissionAmount(commissionAmountDTO.getCommissionAmount());
//                            visaOfficialDO.setPredictCommission(commissionAmountDTO.getCommission());
//                            visaOfficialDO.setCalculation(commissionAmountDTO.getCalculation());
//                        }
//
//                    }
//                }
//            }
//            // 判断文案所属国家
////            RegionDO regionById = regionDAO.getRegionById(officialDAO.getOfficialById(visaOfficialDO.getOfficialId()).getRegionId());
////            String regionName = regionById.getName().replaceAll("[^\u4e00-\u9fa5]", "");
//            if (StringUtil.isNotEmpty(regionName)) {
//                visaOfficialDO.setOfficialRegion(1);
//            } else {
//                visaOfficialDO.setOfficialRegion(0);
//            }
//            //汇率
//            double exchangeRate = serviceOrderDO.getExchangeRate();
//            if ("CNY".equalsIgnoreCase(serviceOrderDO.getCurrency())) {
//                visaOfficialDO.setPredictCommissionAmount(visaOfficialDO.getPredictCommissionAmount()/exchangeRate);
//                visaOfficialDO.setCommissionAmount(visaOfficialDO.getCommissionAmount()/ exchangeRate);
//                visaOfficialDO.setPredictCommission(visaOfficialDO.getPredictCommission()/ exchangeRate);
//            }
//            if (pay) {
//                visaOfficialDO.setPredictCommissionAmount(0);
//                visaOfficialDO.setCommissionAmount(0.00);
//                visaOfficialDO.setPredictCommission(0.00);
//            }
//            // 计算预估佣金人民币
//            if (chinaFixedAmount) {
//                visaOfficialDO.setPredictCommissionCNY(visaOfficialDO.getPredictCommission());
//                visaOfficialDO.setPredictCommission(visaOfficialDO.getPredictCommissionCNY() / exchangeRate);
//            } else {
//                visaOfficialDO.setPredictCommissionCNY(visaOfficialDO.getPredictCommission() * exchangeRate);
//            }
//            if (visaOfficialDao.addVisa(visaOfficialDO) > 0) {
//                visaOfficialDTO.setId(visaOfficialDO.getId());
//                visaOfficialDTO.setCommissionAmount(visaOfficialDO.getCommissionAmount());
//                visaOfficialDTO.setPredictCommission(visaOfficialDO.getPredictCommission());
//                visaOfficialDTO.setCalculation(visaOfficialDO.getCalculation());
//                return visaOfficialDO.getId();
//            } else {
//                return 0;
//            }
//        } catch (Exception e) {
//            ServiceException se = new ServiceException(e);
//            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
//            throw se;
//        }
//    }
//
//    // 打包订单情况判断-->签证：1  EOI：2  ROI：3
//    private String situationJudgment(ServiceOrderDO serviceOrderDO) {
//        StringBuilder situationInt = new StringBuilder();
//        List<ServiceOrderDTO> deriveOrder = serviceOrderDao.getDeriveOrder(serviceOrderDO.getParentId());
//        for (ServiceOrderDTO serviceOrderDTO : deriveOrder) {
//            if ("VA".equals(serviceOrderDTO.getServicePackage().getType())) {
//                situationInt.append("VA");}
//            if ("EOI".equals(serviceOrderDTO.getServicePackage().getType())) {
//                situationInt.append("EOI");}
//            if ("ROI".equals(serviceOrderDTO.getServicePackage().getType())) {
//                situationInt.append("ROI");}
//        }
//        return situationInt.toString();
//    }

    // 文案佣金订单是否直接计算固定金额
    private static boolean isaBoolean(ServicePackagePriceDO servicePackagePriceDO, boolean pay) {
        if (servicePackagePriceDO.getRuler() == 1) {
            pay = false;
        }
        return pay;
    }

    @Override
    public List<VisaOfficialDTO> listVisaOfficialOrder(Integer officialId, List<Integer> regionIdList, Integer id, String startHandlingDate, String endHandlingDate, String state, String startDate, String endDate,String firstSettlementMonth,String lastSettlementMonth,  String userName, String applicantName, Boolean isMerged, Integer pageNum, Integer pageSize, Sorter sorter, String serviceOrderType) throws ServiceException {

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
		List<VisaOfficialListDO> list = visaOfficialDao.list(officialId, regionIdList, id,
				theDateTo00_00_00(startHandlingDate), theDateTo23_59_59(endHandlingDate), state,
				theDateTo00_00_00(startDate), theDateTo23_59_59(endDate), theDateTo00_00_00(firstSettlementMonth), theDateTo23_59_59(lastSettlementMonth), userName, applicantName, isMerged, offset,
				pageSize, orderBy, serviceOrderType);
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
            // 查询职业名称
            ServiceAssessDO serviceAssessDO = serviceAssessDao.seleteAssessById(serviceOrderDto.getServiceAssessId());
            if (serviceAssessDO != null)
                serviceOrderDto.setServiceAssessDO(serviceAssessDO);
            // 查询服务包类型
            if (serviceOrderDto.getServicePackageId() > 0) {
                ServicePackageDO servicePackageDAOById = servicePackageDAO.getById(serviceOrderDto.getServicePackageId());
                if ("EOI".equals(servicePackageDAOById.getType())) {

                    // 主订单配置EOI子订单code集合
                    if (serviceOrderDto.getServicePackageId() == 0) {
                        StringBuilder eoiList = new StringBuilder();
                        List<ServiceOrderDTO> deriveOrder = serviceOrderDao.getDeriveOrder(serviceOrderDto.getId());
                        if (deriveOrder != null && deriveOrder.size() > 0) {
                            for (ServiceOrderDTO e : deriveOrder) {
                                ServicePackageDTO eoiService = servicePackageDAO.getEOIService(e.getServicePackageId());
                                eoiList.append(eoiService.getServiceCode()).append(",");
                            }
                        }
                        serviceOrderDto.setEoiList(eoiList.substring(0, eoiList.length() - 1));
                    }

                    ServicePackageDTO servicePackageDTO = servicePackageDAO.getEOIService(serviceOrderDto.getServicePackageId());
                    if (ObjectUtil.isNotNull(servicePackageDTO)) {
                        serviceOrderDto.setServicePackage(servicePackageDTO);
                    }
                } else {
                    ServicePackageDO servicePackageDo = servicePackageDAO.getById(serviceOrderDto.getServicePackageId());
                    if (servicePackageDo != null)
                        serviceOrderDto.setServicePackage(mapper.map(servicePackageDo, ServicePackageDTO.class));
                }
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
            // 计算EOI数量及排序
            if (serviceOrderDto.getEOINumber() != null && serviceOrderDto.getApplicantParentId() > 0) {
//            Integer eoiNumber = serviceOrderDao.getServiceOrderById(serviceOrderDto.getApplicantParentId()).getEOINumber();
                List<ServiceOrderDTO> ziOrder = serviceOrderDao.getZiOrder(serviceOrderDto.getApplicantParentId());
                List<ServiceOrderDTO> collect = ziOrder.stream().filter(ServiceOrderDTO -> ServiceOrderDTO.getEOINumber() != null).collect(Collectors.toList());
                visaOfficialDto.setSortEOI(serviceOrderDto.getEOINumber() + "/" + collect.size());
            }
            visaOfficialDtoList.add(visaOfficialDto);
        }
        return visaOfficialDtoList;
    }
    
	public VisaOfficialDTO getByServiceOrderId(Integer serviceOrderId) throws ServiceException {
        VisaOfficialDO visaOfficialDo = visaOfficialDao.getByServiceOrderId(serviceOrderId);
        return ObjectUtil.isNotNull(visaOfficialDo) ? mapper.map(visaOfficialDo, VisaOfficialDTO.class) : null;
	}

    @Override
    public int count(Integer officialId, List<Integer> regionIdList, Integer id, String startHandlingDate, String endHandlingDate, String state, String startDate, String endDate, String userName, String applicantName, Boolean isMerged) throws ServiceException {
    	return visaOfficialDao.count(officialId, regionIdList, id, theDateTo00_00_00(startHandlingDate), theDateTo23_59_59(endHandlingDate), state, theDateTo00_00_00(startDate), theDateTo23_59_59(endDate), userName, applicantName, isMerged);
    }

    @Override
    public void update(Integer id, String submitIbDate, Double handling_date, String state) {
        visaOfficialDao.update(id, submitIbDate, handling_date, state);

    }
    
    @Override
	public void updateMerged(Integer id, Boolean isMerged) throws ServiceException {
        visaOfficialDao.updateMerged(id, isMerged);
    }

    @Override
    public List<VisaOfficialDO> monthlyStatement() {
        // 获取今天的日期
        LocalDate today = LocalDate.now();

        // 获取本月的第一天，然后减去一个月来获取上个月的第一天
        LocalDate firstDayOfLastMonth = today.with(TemporalAdjusters.firstDayOfMonth()).minusMonths(1);

        // 获取本月的第一天，然后减去一天来获取上个月的最后一天（因为上个月的最后一天就是本月第一天的前一天）
        LocalDate lastDayOfLastMonth = today.with(TemporalAdjusters.firstDayOfMonth()).minusDays(1);

        // 创建上个月第一天的开始时间（00:00:00）
        LocalDateTime startOfLastMonth = LocalDateTime.of(firstDayOfLastMonth, LocalTime.MIDNIGHT);

        // 创建上个月最后一天的结束时间（假设为23:59:59）
        LocalDateTime endOfLastMonth = LocalDateTime.of(lastDayOfLastMonth, LocalTime.of(23, 59, 59));

        // 定义日期时间格式化器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss");

        // 格式化并打印上个月的第一天和最后一天的时间
        String StartOfLastMonth = startOfLastMonth.format(formatter);
        String EndOfLastMonth = endOfLastMonth.format(formatter);
        System.out.println("Start of last month: " + startOfLastMonth.format(formatter));
        System.out.println("End of last month: " + endOfLastMonth.format(formatter));
        List<ServiceOrderDO> serviceOrderDOS = serviceOrderDAO.listServiceOrder(null, null, "OVST", null, null, null, null,
                null, null, null, null,
                null, null, StartOfLastMonth,
                EndOfLastMonth, null, null, null, null,
                null, null, null, null, null, null
                , null, null, null, null, null
                , null, null, null, 0, 9999, null);
        List<VisaOfficialDO> visaOfficialDOs = new ArrayList<>();
        for (ServiceOrderDO e : serviceOrderDOS) {
            try {
                if ("PAID".equals(e.getState()) || "COMPLETE".equals(e.getState()) || "CLOSE".equals(e.getState())) {
                    VisaOfficialDO visaOfficialDOTmp = visaOfficialDao.getByServiceOrderId(e.getId());
                    if (ObjectUtil.isNotNull(visaOfficialDOTmp)) {
                        log.info("当前文案佣金订单已创建------------" + e.getId());
                        visaOfficialDao.deleteByServiceOrderId(e.getId());
                    }
                    VisaOfficialDO visaOfficialDO = buildVisaOfficialDo(e);
                    SchoolInstitutionLocationDO schoolInstitutionLocationDO = schoolInstitutionLocationDAO.getById(e.getSchoolInstitutionLocationId());
                    SchoolInstitutionDO schoolInstitution = schoolInstitutionDAO.getSchoolInstitutionByCode(schoolInstitutionLocationDO.getProviderCode());
                    if ("Government".equals(schoolInstitution.getInstitutionType())) {
                        List<String> publicTafeCode = new ArrayList<String>() {
                            {
                                this.add("03020E");
                                this.add("00591E");
                                this.add("01505M");
                                this.add("00092B");
                                this.add("03041M");
                                this.add("00020G");
                                this.add("01723A");
                                this.add("00724G");
                                this.add("00012G");
                                this.add("02411J");
                                this.add("00881F");
                                this.add("01985A");
                                this.add("00011G");
                                this.add("001218G");
                                this.add("00001K");
                            }
                        };
                        visaOfficialDO.setPredictCommissionCNY(200.00);
                        visaOfficialDO.setPredictCommission(visaOfficialDO.getPredictCommissionCNY() / visaOfficialDO.getExchangeRate());
                        if (publicTafeCode.contains(schoolInstitution.getCode())) {
                            visaOfficialDO.setPredictCommissionCNY(80.00);
                            visaOfficialDO.setPredictCommission(visaOfficialDO.getPredictCommissionCNY() / visaOfficialDO.getExchangeRate());
                        }
                    }
                    if ("Private".equals(schoolInstitution.getInstitutionType())) {
                        SchoolInstitutionListDTO schoolInstitutionInfo = schoolCourseDAO.getSchoolInstitutionInfoByCourseId(e.getCourseId());
                        SchoolCourseDO schoolCourseDO = schoolInstitutionInfo.getSchoolCourseDO();
                        if ("VET".equals(schoolCourseDO.getCourseSector())) {
                            visaOfficialDO.setPredictCommissionCNY(20.00);
                            visaOfficialDO.setPredictCommission(visaOfficialDO.getPredictCommissionCNY() / visaOfficialDO.getExchangeRate());
                        }
                        if ("Higher Education".equals(schoolCourseDO.getCourseSector())) {
                            visaOfficialDO.setPredictCommissionCNY(40.00);
                            visaOfficialDO.setPredictCommission(visaOfficialDO.getPredictCommissionCNY() / visaOfficialDO.getExchangeRate());
                        }
                    }
                    visaOfficialDO.setServiceOrderDO(e);
                    visaOfficialDao.addVisa(visaOfficialDO);
                    visaOfficialDO.setGmtCreate(new Date());
                    visaOfficialDOs.add(visaOfficialDO);
                }
            } catch (Exception ex) {
                log.info("当前生成失败的订单id为---------------------" + e.getId());
                ex.printStackTrace();
            }
        }
        return visaOfficialDOs;
    }

    private VisaOfficialDO buildVisaOfficialDo(ServiceOrderDO e) throws ServiceException {
        VisaOfficialDO visaDto = new VisaOfficialDO();
        visaDto.setState(BaseCommissionOrderController.ReviewKjStateEnum.PENDING.toString());
        visaDto.setUserId(e.getUserId());
        visaDto.setCode(UUID.randomUUID().toString());
        visaDto.setHandlingDate(new Date());
        visaDto.setReceiveTypeId(e.getReceiveTypeId());
        visaDto.setReceiveDate(e.getReceiveDate());
        visaDto.setServiceId(e.getServiceId());
        visaDto.setServiceOrderId(e.getId());
        visaDto.setInstallment(e.getInstallment());
        visaDto.setPaymentVoucherImageUrl1(e.getPaymentVoucherImageUrl1());
        visaDto.setPaymentVoucherImageUrl2(e.getPaymentVoucherImageUrl2());
        visaDto.setPaymentVoucherImageUrl3(e.getPaymentVoucherImageUrl3());
        visaDto.setPaymentVoucherImageUrl4(e.getPaymentVoucherImageUrl4());
        visaDto.setPaymentVoucherImageUrl5(e.getPaymentVoucherImageUrl5());
        visaDto.setVisaVoucherImageUrl(e.getVisaVoucherImageUrl());
        visaDto.setPerAmount(e.getPerAmount());
        visaDto.setAmount(e.getAmount());
        if (visaDto.getPerAmount() < visaDto.getAmount()) {
            log.info("本次应收款(" + visaDto.getPerAmount() + ")不能小于本次已收款(" + visaDto.getAmount() + ")!");
        }
        visaDto.setCurrency(e.getCurrency());
        visaDto.setExchangeRate(e.getExchangeRate());
        visaDto.setDiscount(visaDto.getPerAmount() - visaDto.getAmount());
        visaDto.setAdviserId(e.getAdviserId());
        visaDto.setMaraId(e.getMaraId());
        visaDto.setOfficialId(e.getOfficialId());
        visaDto.setRemarks(e.getRemarks());
        double commission = visaDto.getAmount();
        if ("CNY".equals(e.getCurrency())) {
            BigDecimal bigDecimal = BigDecimal.valueOf(commission);
            BigDecimal bigDecimalExc = BigDecimal.valueOf(e.getExchangeRate());
            BigDecimal divide = bigDecimal.divide(bigDecimalExc, 4, RoundingMode.HALF_UP);
            commission = divide.doubleValue();
        }
        visaDto.setGst(commission / 11);
        visaDto.setDeductGst(commission - visaDto.getGst());
        visaDto.setBonus(visaDto.getDeductGst() * 0.1);
        visaDto.setExpectAmount(commission);
        visaDto.setExchangeRate(exchangeRateService.getQuarterExchangeRate());

        double _perAmount = 0.00;
        double _amount = 0.00;
        visaDto.setState(BaseCommissionOrderController.ReviewKjStateEnum.REVIEW.toString()); // 第一笔单子直接进入财务审核状态
        visaDto.setKjApprovalDate(e.getReadcommittedDate());
        return visaDto;
    }


    //计算
    private CommissionAmountDTO calculationCommissionAmount(int serviceOrderId, String type, String regionName) throws ServiceException {
        ServiceOrderDO serviceOrderDO = serviceOrderDao.getServiceOrderById(serviceOrderId);
        VisaDO visaDO = new VisaDO();
        VisaDO visaDO1 = new VisaDO();
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
        if (serviceOrderDO.getParentId() == 0 && serviceOrderDO.getApplicantParentId() == 0) {
            visaDO = visaDAO.getFirstVisaByServiceOrderId(serviceOrderDO.getId());
            visaDO1 = visaDAO.getSecondVisaByServiceOrderId(serviceOrderDO.getId());
            if (visaDO1 == null) {
                if (visaDO == null) {
                    return commissionAmountDTO;
                }
                amount = visaDO.getAmount();
            } else {
                amount = visaDO.getAmount();
                amount += visaDO1.getAmount();
            }
        } else {
            if (serviceOrderDO.getParentId() !=0){
            visaDO = visaDAO.getFirstVisaByServiceOrderId(serviceOrderDO.getParentId());
            visaDO1 = visaDAO.getSecondVisaByServiceOrderId(serviceOrderDO.getParentId());}
            else {
             visaDO = visaDAO.getFirstVisaByServiceOrderId(serviceOrderDO.getApplicantParentId());
             visaDO1 = visaDAO.getSecondVisaByServiceOrderId(serviceOrderDO.getApplicantParentId());
            }
            if (visaDO1 == null)
                amount = serviceOrderDO.getReceivable();
            else {
                amount = visaDO.getAmount();
                amount += visaDO1.getAmount();
            }
        }
        if (serviceOrderDO.getParentId() != 0) {
            if ("SIV".equals(serviceOrderDao.getServiceOrderById(serviceOrderDO.getParentId()).getType())) {
                visaDO = visaDAO.getFirstVisaByServiceOrderId(serviceOrderDO.getParentId());
                visaDO1 = visaDAO.getSecondVisaByServiceOrderId(serviceOrderDO.getParentId());
                if (ObjectUtil.isNull(visaDO1) && ObjectUtil.isNotNull(visaDO)) {
                    if ("VA".equals(type)) {
                        amount = visaDO.getAmount();
                    } else {
                        amount = visaDO.getAmount() * 0.5;
                    }
                }
                if (ObjectUtil.isNotNull(visaDO1)) {
                    if ("VA".equals(type)) {
                        amount = serviceOrderDO.getReceivable();
                    } else {
                        int count = 0;
                        List<ServiceOrderDTO> deriveOrder = serviceOrderDao.getDeriveOrder(serviceOrderDO.getParentId());
                        for (ServiceOrderDTO serviceOrderDTO : deriveOrder) {
                            VisaOfficialDO byServiceOrderId = visaOfficialDao.getByServiceOrderId(serviceOrderDTO.getId());
                            if (ObjectUtil.isNotNull(byServiceOrderId)) {
                                count++;
                            }
                        }
                        if (count == 0) {
                            amount = visaDO.getAmount();
                        }
                        if (count == 1) {
                            amount = visaDO1.getAmount();
                        }
                    }
                }
            }
        }
        if (serviceOrderDO.isPay()) {
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
            rate = grade.getRate();
            if (StringUtil.isEmpty(regionName)) {
                if (monthlist.contains(calendar.get(Calendar.MONTH) + 1)) {
                    rate = grade.getRate() + 3;
                } else {
                    rate = grade.getRate();
                }
            }
            ServicePackagePriceDO servicePackagePriceDO = servicePackagePriceDAO.getByServiceId(serviceOrderDO.getServiceId());
            if (servicePackagePriceDO == null) {
                commissionAmountDTO.setThirdPrince(0.00);
                commissionAmountDTO.setRuler(0);
            } else {
                ServicePackagePriceV2DTO servicePackagePriceV2DTO = closeJugd(serviceOrderDO.getOfficialId(), servicePackagePriceDO);
                commissionAmountDTO.setThirdPrince(servicePackagePriceDO.getThirdPrince());
                servicePackagePriceDO.setAmount(servicePackagePriceV2DTO.getAmount());
                commissionAmountDTO.setRuler(servicePackagePriceV2DTO.getRuler());
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (commissionAmountDTO.getRuler() == 0) {
                commissionAmountDTO.setPredictCommissionAmount((amount - commissionAmountDTO.getRefund() - commissionAmountDTO.getThirdPrince())/1.1);
                if (commissionAmountDTO.getPredictCommissionAmount() <= 0)
                    commissionAmountDTO.setPredictCommissionAmount(0.00);
                commissionAmountDTO.setCommissionAmount(commissionAmountDTO.getPredictCommissionAmount());
                commissionAmountDTO.setCommission(commissionAmountDTO.getPredictCommissionAmount() * (rate / 100));
                String calculation = new String();
                calculation = "0" + "|" + commissionAmountDTO.getThirdPrince() + "|" + dateFormat.format(servicePackagePriceDO == null ? System.currentTimeMillis() : servicePackagePriceDO.getGmtModify()) + "|" + grade.getGrade() + "," + rate + "%" + "," + dateFormat.format(grade.getGmtModify());
                commissionAmountDTO.setCalculation(calculation);
            } else {
                if (StringUtil.isNotEmpty(regionName)) {
                    commissionAmountDTO.setChinaFixedAmount(true);
                }
                commissionAmountDTO.setCommission(servicePackagePriceDO.getAmount());
                commissionAmountDTO.setThirdPrince(servicePackagePriceDO.getThirdPrince());
                String calculation = new String();
                calculation = "1" + "|" + commissionAmountDTO.getThirdPrince() + "," + servicePackagePriceDO.getAmount() + "|" + dateFormat.format(servicePackagePriceDO.getGmtModify());
                commissionAmountDTO.setCalculation(calculation);
            }
        }
        return commissionAmountDTO;
    }

    // 文案地区判断
    public ServicePackagePriceV2DTO closeJugd(Integer officialId, ServicePackagePriceDO servicePackagePriceDO) {
        // todo 判断文案结算方式
        ServicePackagePriceV2DTO servicePackagePriceV2DTO = new ServicePackagePriceV2DTO();
        OfficialDO officialById = officialDao.getOfficialById(officialId);
        RegionDO regionById = regionDAO.getRegionById(officialById.getRegionId());
        String rulerV2 = servicePackagePriceDO.getRulerV2();
        List<ServicePackagePriceV2DTO> servicePackagePriceV2DTOS = JSONArray.parseArray(rulerV2, ServicePackagePriceV2DTO.class);
        for (ServicePackagePriceV2DTO e : servicePackagePriceV2DTOS) {
            if (e.getAreaId().equals(regionById.getId())) {
                servicePackagePriceV2DTO = e;
            }
        }
        if (servicePackagePriceV2DTO.getAreaId() == null && servicePackagePriceV2DTO.getRuler() == null) {
            Map<String, ServicePackagePriceV2DTO> collect = servicePackagePriceV2DTOS.stream().collect(Collectors.toMap(ServicePackagePriceV2DTO::getCountry, each -> each));
            String s = regionById.getName().replaceAll("[^\u4e00-\u9fa5]", "");
            if (StringUtil.isNotEmpty(s)) {
                servicePackagePriceV2DTO = collect.get("China");
            } else {
                servicePackagePriceV2DTO = collect.get("Australia");
            }
        }

//
//        Map<Integer, ServicePackagePriceV2DTO> collect = servicePackagePriceV2DTOS.stream().collect(Collectors.toMap(ServicePackagePriceV2DTO::getAreaId, each -> each));
//        if (collect.get(regionById.getId()) != null) {
//            servicePackagePriceV2DTO.set(collect.get(regionById.getId()));
//        } else {
//            // 省
//            RegionDO regionById1 = regionDAO.getRegionById(regionById.getParentId());
//            // 国
//            RegionDO regionById2 = regionDAO.getRegionById(regionById1.getParentId());
//            servicePackagePriceV2DTO.set(collect.get(regionById2.getId()));
//        }
        return servicePackagePriceV2DTO;
    }
}
