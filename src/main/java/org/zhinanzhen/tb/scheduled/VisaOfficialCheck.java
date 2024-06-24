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
import org.zhinanzhen.tb.utils.WXWorkAPI;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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

    @Resource
    private ServiceDAO serviceDAO;

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
                ServicePackageDO byId = servicePackageDAO.getById(e.getServicePackageId());
                if ("SIV".equals(serviceParentOrderById.getType())) {
                    if (!"VA".equals(byId.getType()) && !"EOI".equals(byId.getType())) {
                        continue;
                    }
                }
                if ("TM".equals(byId.getType())) {
                    continue;
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

    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 2 1 * *")
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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

    // 每个周一第一天1点触发
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 1 * * MON")
    public void orderStatistics() {
        // 获取今天的日期
        LocalDate today = LocalDate.now();

        // 获取本周的周一日期
        LocalDate thisMonday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));

        // 获取上周的周一日期（本周周一减去一周）
        LocalDate lastMonday = thisMonday.minusWeeks(1);

        // 获取上周的周日日期（上周一加上6天）
        LocalDate lastSunday = lastMonday.plusDays(6);

        // 设置时间为00:00:00
        LocalDateTime startOfWeek = LocalDateTime.of(lastMonday, LocalTime.MIDNIGHT);

        // 设置时间为23:59:59
        LocalDateTime endOfWeek = LocalDateTime.of(lastSunday, LocalTime.of(23, 59, 59));

        // 定义日期时间格式化器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 格式化并打印上个月的第一天和最后一天的时间
        String startOfLastMonth = startOfWeek.format(formatter);
        String endOfLastMonth = endOfWeek.format(formatter);

        String orderWeekCountStr = "上周新增签证总量：";
        StringBuilder orderWeektTopStr = new StringBuilder("上周服务内容数量TOP10：");
        StringBuilder eoiWeektStr = new StringBuilder("上周eoi总数：");
        StringBuilder careerAssessmentWeektStr = new StringBuilder("上周职业评估名称：");
        List<ServiceOrderDO> serviceOrderDOS = serviceOrderDAO.listServiceOrder(startOfLastMonth, endOfLastMonth, null, null, null, null, null,
                null, null, null, null,
                null, null, null,
                null, null, null, null, null,
                null, null, null, null, null, null
                , null, 0, null, null, null
                , null, null, null, 0, 9999, null);
        Map<String, List<ServiceOrderDO>> eoiCountHashMap = new HashMap<>(); // eoi计算容器
        Map<String, List<ServiceOrderDO>> careerAssessmentHashMap = new HashMap<>(); // 职业评估计算容器
        List<ServiceOrderDO> collect = serviceOrderDOS.stream().filter(ServiceOrderDO -> !"OVST".equals(ServiceOrderDO.getType())).collect(Collectors.toList());
        // 上周EOI数量排序
        for (ServiceOrderDO a : collect) {
            String eoiCount = "";
            List<ServiceOrderDO> serviceOrderDOList = new ArrayList<>();
            ServiceDO serviceById = serviceDAO.getServiceById(a.getServiceId());
            ServicePackageDO servicePackageDO = servicePackageDAO.getById(a.getServicePackageId());
            if (ObjectUtil.isNotNull(serviceById) && serviceById.getCode().contains("EOI")) {
                eoiCount = serviceById.getName() + "-" + serviceById.getCode();
                serviceOrderDOList.add(a);
            }
            if (ObjectUtil.isNotNull(servicePackageDO) && "EOI".equals(servicePackageDO.getType())) {
                eoiCount = serviceById.getCode() + "-" + "EOI";
                serviceOrderDOList.add(a);
            }
            if (careerAssessmentHashMap.get(eoiCount) != null) {
                serviceOrderDOList.addAll(eoiCountHashMap.get(eoiCount));
            }
            eoiCountHashMap.put(eoiCount, serviceOrderDOList);
        }
        eoiCountHashMap.remove("");
        Map<String, List<ServiceOrderDO>> eoiListSize = eoiCountHashMap.entrySet().stream()
                .sorted(Map.Entry.<String, List<ServiceOrderDO>>comparingByValue(Comparator.comparingInt(List::size)).reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, // 如果出现键冲突，保留旧值（理论上不应该出现键冲突）
                        LinkedHashMap::new // 使用 LinkedHashMap 以保持插入顺序
                ));
        AtomicInteger eoiWeektCount = new AtomicInteger(1);
        eoiListSize.forEach((k, v) -> {
            eoiWeektStr.append("\n").append(eoiWeektCount.get()).append(".").append(k).append(":").append(v.size()).append("个");
        });
        // 上周职业评估情况排序
        for (ServiceOrderDO a : collect) {
            String careerAssessment = "";
            List<ServiceOrderDO> serviceOrderDOList = new ArrayList<>();
            ServiceDO serviceById = serviceDAO.getServiceById(a.getServiceId());
            ServicePackageDO servicePackageDO = servicePackageDAO.getById(a.getServicePackageId());
            if ("职评".equals(serviceById.getCode())) {
                careerAssessment = serviceById.getName() + "-" + serviceById.getCode();
                serviceOrderDOList.add(a);
            }
            if (ObjectUtil.isNotNull(servicePackageDO) && "CA".equals(servicePackageDO.getType())) {
                careerAssessment = serviceById.getCode() + "-" + "职业评估";
                serviceOrderDOList.add(a);
            }
            if (careerAssessmentHashMap.get(careerAssessment) != null) {
                serviceOrderDOList.addAll(careerAssessmentHashMap.get(careerAssessment));
            }
            careerAssessmentHashMap.put(careerAssessment, serviceOrderDOList);
        }
        careerAssessmentHashMap.remove("");
        Map<String, List<ServiceOrderDO>> careerAssessmentListSize = careerAssessmentHashMap.entrySet().stream()
                .sorted(Map.Entry.<String, List<ServiceOrderDO>>comparingByValue(Comparator.comparingInt(List::size)).reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, // 如果出现键冲突，保留旧值（理论上不应该出现键冲突）
                        LinkedHashMap::new // 使用 LinkedHashMap 以保持插入顺序
                ));
        AtomicInteger careerAssessmentWeektCount = new AtomicInteger(1);
        careerAssessmentListSize.forEach((k, v) -> {
            careerAssessmentWeektStr.append("\n").append(careerAssessmentWeektCount.get()).append(".").append(k).append(":").append(v.size()).append("个");
        });
        // 上周订单类型前十排序
        Map<Integer, List<ServiceOrderDO>> groupByService = serviceOrderDOS.stream().filter(ServiceOrderDO -> !"OVST".equals(ServiceOrderDO.getType())).collect(Collectors.groupingBy(ServiceOrderDO::getServiceId));
        groupByService.forEach((k, v) -> {
            ServiceDO serviceById = serviceDAO.getServiceById(k);
            if ("独立技术移民".equals(serviceById.getName())) {
                List<ServiceOrderDO> filteredOrders = v.stream()
                        .filter(e -> "SIV".equals(e.getType()))
                        .collect(Collectors.toList());
                groupByService.put(k, filteredOrders);
            }
        });
        // 然后将Map转换为一个Stream，并根据List的长度进行排序
        Map<Integer, List<ServiceOrderDO>> sortedByListSize = groupByService.entrySet().stream()
                .sorted(Map.Entry.<Integer, List<ServiceOrderDO>>comparingByValue(Comparator.comparingInt(List::size)).reversed())
//                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, // 如果出现键冲突，保留旧值（理论上不应该出现键冲突）
                        LinkedHashMap::new // 使用 LinkedHashMap 以保持插入顺序
                ));
        AtomicInteger orderWeektTopCount = new AtomicInteger(1);
        AtomicInteger orderWeektCount = new AtomicInteger(0);
        sortedByListSize.forEach((k, v) -> {
            ServiceDO serviceById = serviceDAO.getServiceById(k);
            if (orderWeektTopCount.get() <= 10) {
                orderWeektTopStr.append("\n").append(orderWeektTopCount.get()).append(".").append(serviceById.getName()).append("-").append(serviceById.getCode()).append(":").append(v.size()).append("个");
            }
            orderWeektTopCount.getAndIncrement();
            orderWeektCount.getAndAdd(v.size());
        });
        orderWeekCountStr = orderWeekCountStr + orderWeektCount.get();
//        System.out.println(orderWeekCountStr);
//        System.out.println(orderWeektTopStr);
//        System.out.println(careerAssessmentWeektStr);
//        System.out.println(eoiWeektStr);

//        WXWorkAPI.sendWecomRotMsg(orderWeekCountStr + "\n" + "\n" + orderWeektTopStr + "\n" + "\n" + careerAssessmentWeektStr + "\n" + eoiWeektStr);
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
