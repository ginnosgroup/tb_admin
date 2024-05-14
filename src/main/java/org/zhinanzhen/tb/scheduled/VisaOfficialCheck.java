package org.zhinanzhen.tb.scheduled;


import com.ikasoa.core.utils.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.zhinanzhen.b.controller.BaseCommissionOrderController;
import org.zhinanzhen.b.dao.*;
import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.service.VisaOfficialService;
import org.zhinanzhen.b.service.pojo.SchoolInstitutionListDTO;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.b.service.pojo.VisaOfficialDTO;
import org.zhinanzhen.tb.service.ServiceException;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
@EnableScheduling
@Slf4j
public class VisaOfficialCheck {

    @Resource
    private ServiceOrderDAO serviceOrderDAO;

    @Resource
    private VisaOfficialDao visaOfficialDao;

    @Resource
    private ServicePackageDAO servicePackageDAO;

    @Resource
    private VisaOfficialService visaOfficialService;

    @Resource
    private SchoolInstitutionLocationDAO schoolInstitutionLocationDAO;

    @Resource
    private SchoolInstitutionDAO schoolInstitutionDAO;

    @Resource
    private SchoolCourseDAO schoolCourseDAO;
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 3 * * ?")
    public void visaOfficialCheckEverDay() throws ServiceException {
        // 获取当前日期
        LocalDate currentDate = LocalDate.now();
        // 计算三天前的日期
        LocalDate threeDaysAgo = currentDate.minusDays(3);
        LocalDate localDate = currentDate.plusDays(1);
        // 创建一个 DateTimeFormatter 对象来格式化日期
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // 格式化日期并输出
        String beforeFormat = threeDaysAgo.format(formatter);
        String nowDate = localDate.format(formatter);
        List<ServiceOrderDO> tmpServiceOrder = serviceOrderDAO.getTmpServiceOrder(beforeFormat, nowDate);
        for (ServiceOrderDO e : tmpServiceOrder) {
            if (e.getApplicantParentId() != 0 && e.getServicePackageId() != 0) {
                ServiceOrderDO serviceParentOrderById = serviceOrderDAO.getServiceOrderById(e.getApplicantParentId());
                if ("SIV".equals(serviceParentOrderById.getType())) {
                    ServicePackageDO byId = servicePackageDAO.getById(e.getServicePackageId());
                    if (!"VA".equals(byId.getType()) && !"EOI".equals(byId.getType())) {
                        continue;
                    }
                }
            }
            VisaOfficialDO visaOfficialDO = visaOfficialDao.getByServiceOrderId(e.getId());
            if (ObjectUtil.isNull(visaOfficialDO)) {
                ServiceOrderDO serviceOrderById = serviceOrderDAO.getServiceOrderById(e.getId());
                VisaOfficialDTO visaDto = new VisaOfficialDTO();
                visaDto.setState(BaseCommissionOrderController.ReviewKjStateEnum.PENDING.toString());
                visaDto.setUserId(serviceOrderById.getUserId());
                visaDto.setCode(UUID.randomUUID().toString());
                visaDto.setHandlingDate(new Date());
                visaDto.setReceiveTypeId(serviceOrderById.getReceiveTypeId());
                visaDto.setReceiveDate(serviceOrderById.getReceiveDate());
                visaDto.setServiceId(serviceOrderById.getServiceId());
                visaDto.setServiceOrderId(serviceOrderById.getId());
                visaDto.setInstallment(serviceOrderById.getInstallment());
                visaDto.setPaymentVoucherImageUrl1(serviceOrderById.getPaymentVoucherImageUrl1());
                visaDto.setPaymentVoucherImageUrl2(serviceOrderById.getPaymentVoucherImageUrl2());
                visaDto.setPaymentVoucherImageUrl3(serviceOrderById.getPaymentVoucherImageUrl3());
                visaDto.setPaymentVoucherImageUrl4(serviceOrderById.getPaymentVoucherImageUrl4());
                visaDto.setPaymentVoucherImageUrl5(serviceOrderById.getPaymentVoucherImageUrl5());
                visaDto.setVisaVoucherImageUrl(serviceOrderById.getVisaVoucherImageUrl());
                visaDto.setPerAmount(serviceOrderById.getPerAmount());
                visaDto.setAmount(serviceOrderById.getAmount());
                if (visaDto.getPerAmount() < visaDto.getAmount()) {
                    log.info("本次应收款(" + visaDto.getPerAmount() + ")不能小于本次已收款(" + visaDto.getAmount() + ")!");
                }
                visaDto.setCurrency(serviceOrderById.getCurrency());
                visaDto.setExchangeRate(serviceOrderById.getExchangeRate());
                visaDto.setDiscount(visaDto.getPerAmount() - visaDto.getAmount());
//                visaDto.setInvoiceNumber(serviceOrderById.getInvoiceNumber());
                visaDto.setAdviserId(serviceOrderById.getAdviserId());
                visaDto.setMaraId(serviceOrderById.getMaraId());
                visaDto.setOfficialId(serviceOrderById.getOfficialId());
                visaDto.setRemarks(serviceOrderById.getRemarks());
                double commission = visaDto.getAmount();
                if ("CNY".equals(serviceOrderById.getCurrency())) {
                    BigDecimal bigDecimal = BigDecimal.valueOf(commission);
                    BigDecimal bigDecimalExc = BigDecimal.valueOf(serviceOrderById.getExchangeRate());
                    BigDecimal divide = bigDecimal.divide(bigDecimalExc, 4, RoundingMode.HALF_UP);
                    commission = divide.doubleValue();
                }
                visaDto.setGst(commission / 11);
                visaDto.setDeductGst(commission - visaDto.getGst());
                visaDto.setBonus(visaDto.getDeductGst() * 0.1);
                visaDto.setExpectAmount(commission);

                double _perAmount = 0.00;
                double _amount = 0.00;
                visaDto.setState(BaseCommissionOrderController.ReviewKjStateEnum.REVIEW.toString()); // 第一笔单子直接进入财务审核状态
//                if (StringUtil.isNotEmpty(verifyCode))// 只给第一笔赋值verifyCode
//                    visaDto.setVerifyCode(verifyCode.replace("$", "").replace("#", "").replace(" ", ""));
                visaDto.setKjApprovalDate(new Date());
                visaOfficialService.addVisa(visaDto);
                log.info("当前未生成文案佣金订单的订单编号为：------------------------------{}", e.getId());
            }
        }
    }

    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 3 1 * *")
    public void visaOfficialOVSTCreate() {
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
        List<ServiceOrderDO> serviceOrderDOS = serviceOrderDAO.listServiceOrder("OVST", null, null, null, null,
                null, null, null, null,
                null, null, StartOfLastMonth,
                EndOfLastMonth, null, null, null, null,
                null, null, null, null, null, null
                , null, null, null, null, null
                , null, null, null, 0, 9999, null);
        for (ServiceOrderDO e : serviceOrderDOS) {
            try {
                if ("PAID".equals(e.getState()) || "COMPLETE".equals(e.getState()) || "CLOSE".equals(e.getState())) {
                    VisaOfficialDO visaOfficialDOTmp = visaOfficialDao.getByServiceOrderId(e.getId());
                    if (ObjectUtil.isNotNull(visaOfficialDOTmp)) {
                        log.info("当前文案佣金订单已创建------------" + e.getId());
                        continue;
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
                    visaOfficialDao.addVisa(visaOfficialDO);
                }
            } catch (Exception ex) {
                log.info("当前生成失败的订单id为---------------------" + e.getId());
                ex.printStackTrace();
            }
        }
    }

    private static VisaOfficialDO buildVisaOfficialDo(ServiceOrderDO e) {
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

        double _perAmount = 0.00;
        double _amount = 0.00;
        visaDto.setState(BaseCommissionOrderController.ReviewKjStateEnum.REVIEW.toString()); // 第一笔单子直接进入财务审核状态
        visaDto.setKjApprovalDate(new Date());
        return visaDto;
    }
}
