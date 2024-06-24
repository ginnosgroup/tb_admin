package org.zhinanzhen.tb.scheduled;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ikasoa.core.ErrorCodeEnum;
import com.ikasoa.core.utils.ObjectUtil;
import com.ikasoa.core.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.zhinanzhen.b.controller.BaseCommissionOrderController;
import org.zhinanzhen.b.dao.*;
import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.service.VisaOfficialService;
import org.zhinanzhen.b.service.pojo.*;
import org.zhinanzhen.tb.dao.AdminUserDAO;
import org.zhinanzhen.tb.dao.AdviserDAO;
import org.zhinanzhen.tb.dao.pojo.AdminUserDO;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.utils.WXWorkAPI;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
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

    @Resource
    private AdminUserDAO adminUserDAO;

    @Resource
    private AdviserDAO adviserDAO;

    @Value("${weiban.crop_id}")
    private String weibanCropId;

    @Value("${weiban.secret}")
    private String weibanSecret;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private QywxExternalUserDAO qywxExternalUserDAO;

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

    // 每个周一第一天1点触发
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 1 * * *")
    public void externalContactImport() {
// 获取当前日期
        LocalDate now = LocalDate.now();

        // 获取上个月的第一天
        LocalDate firstDayOfLastMonth = now.with(TemporalAdjusters.firstDayOfMonth()).minusMonths(1);
        // 设置时间为0点0分0秒
        ZonedDateTime firstDayMidnight = firstDayOfLastMonth.atStartOfDay(ZoneId.systemDefault());
        // 转换为毫秒级时间戳
        long firstDayTimestamp = firstDayMidnight.toInstant().toEpochMilli() / 1000;

        // 获取上个月的最后一天
        LocalDate lastDayOfLastMonth = now.with(TemporalAdjusters.lastDayOfMonth()).minusMonths(1);
        // 设置时间为23点59分59秒
        ZonedDateTime lastDayEnd = lastDayOfLastMonth.atTime(LocalTime.of(23, 59, 59)).atZone(ZoneId.systemDefault());
        // 转换为毫秒级时间戳
        long lastDayTimestamp = lastDayEnd.toInstant().toEpochMilli() / 1000;

        // 打印结果
        System.out.println("上个月1号0点0分的时间戳: " + firstDayTimestamp);
        System.out.println("上个月最后一天23点59分59秒的时间戳: " + lastDayTimestamp);

        List<AdviserDO> adviserDOS = adviserDAO.listAdviser(null, null, 0, 10000);
        List<AdviserDO> newAdviserDOs = new ArrayList<>();
        for (AdviserDO a : adviserDOS) {
            AdminUserDO userDO = adminUserDAO.getUserByAdviserId(a.getId());
            if (ObjectUtil.isNotNull(userDO) && StringUtil.isNotEmpty(userDO.getOperUserId()) && "ENABLED".equals(a.getState())) {
                a.setOperUserId(userDO.getOperUserId());
                newAdviserDOs.add(a);
            }
        }
        Map<Integer, String> collect = newAdviserDOs.stream().collect(Collectors.toMap(AdviserDO::getId, AdviserDO::getOperUserId));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US); // HH表示24小时制
        // 获取token
        String urlToken = "https://open.weibanzhushou.com/open-api/access_token/get";
        HashMap<String, String> uriVariablesMap = new HashMap<>();
        uriVariablesMap.put("corp_id", weibanCropId);
        uriVariablesMap.put("secret", weibanSecret);
        log.info("uriVariablesMap : " + uriVariablesMap);
        JSONObject weibanTokenJsonObject = restTemplate.postForObject(urlToken, uriVariablesMap, JSONObject.class);
        if ((int) weibanTokenJsonObject.get("errcode") != 0) {
            ServiceException se = new ServiceException(weibanTokenJsonObject.get("errmsg").toString());
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
        }
        log.info(weibanTokenJsonObject.get("access_token").toString());
        AtomicInteger length = new AtomicInteger(0);
        JSONArray jsonArray = new JSONArray();
        Map<Integer, JSONArray> jsonArrayHashMap = new HashMap<>();
//        for (AdviserDO e : newAdviserDOs) {
        for (AdviserDO e : newAdviserDOs) {
            // 获取列表
            String url = StringUtil.merge("https://open.weibanzhushou.com/open-api/external_user/list?",
                    "access_token={accessToken}", "&staff_id={staffId}", "&limit={limit}&offset={offset}",
                    "&start_time={startTime}&end_time={endTime}");
            HashMap<String, Object> paramMap = new HashMap<>();
            Integer offset = 0;
            paramMap.put("accessToken", weibanTokenJsonObject.get("access_token").toString());
            paramMap.put("limit", 100);
            paramMap.put("offset", offset);
            paramMap.put("staffId", e.getOperUserId());
//            paramMap.put("staffId", "ZhiNanZhenemily");
            paramMap.put("startTime", firstDayTimestamp);
            paramMap.put("endTime", lastDayTimestamp);
            log.info("URL : " + url);
            log.info("Params : " + paramMap);
            JSONObject weibanUserListJsonObject = restTemplate.getForObject(url, JSONObject.class, paramMap);
            if (ObjectUtil.isNull(weibanUserListJsonObject)) {
                log.warn("'weibanUserListJsonObject' not exist !");
                return;
            }
            log.info("weibanUserListJsonObject : " + weibanUserListJsonObject.toString());
            if ((int) weibanUserListJsonObject.get("errcode") != 0) {
                log.warn("调用微伴API异常!");
                return;
            }
            if (!weibanUserListJsonObject.containsKey("external_user_list")) {
                log.warn("'external_user_list' not exist by Json !");
                return;
            }
            // 获取列表数据
//            jsonArray = weibanUserListJsonObject.getJSONArray("external_user_list");
//            if (ObjectUtil.isNull(jsonArray)) {
//                log.warn("'jsonArray' is null : " + weibanUserListJsonObject.toString());
//                return;
//            }
            Integer total = (Integer) weibanUserListJsonObject.get("total");
            length.getAndAdd(total);
            for (int i = 0; i < total; i = i + 100) {
                paramMap.put("accessToken", weibanTokenJsonObject.get("access_token").toString());
                paramMap.put("limit", 100);
                paramMap.put("offset", i);
                paramMap.put("staffId", e.getOperUserId());
//            paramMap.put("staffId", "ZhiNanZhenemily");
                paramMap.put("startTime", firstDayTimestamp);
                paramMap.put("endTime", lastDayTimestamp);
                log.info("URL : " + url);
                log.info("Params : " + paramMap);
                JSONObject weibanUserListJsonObjectTmp = restTemplate.getForObject(url, JSONObject.class, paramMap);
                if (ObjectUtil.isNull(weibanUserListJsonObjectTmp)) {
                    log.warn("'weibanUserListJsonObject' not exist !");
                    return;
                }
                log.info("weibanUserListJsonObject : " + weibanUserListJsonObjectTmp.toString());
                if ((int) weibanUserListJsonObjectTmp.get("errcode") != 0) {
                    log.warn("调用微伴API异常!");
                    return;
                }
                if (!weibanUserListJsonObjectTmp.containsKey("external_user_list")) {
                    log.warn("'external_user_list' not exist by Json !");
                    return;
                }
                // 获取列表数据
                JSONArray jsonArray2 = weibanUserListJsonObjectTmp.getJSONArray("external_user_list");
                if (ObjectUtil.isNull(jsonArray2)) {
                    log.warn("'jsonArray' is null : " + weibanUserListJsonObjectTmp.toString());
                    return;
                }
                jsonArray.addAll(jsonArray2);
                if (jsonArrayHashMap.get(e.getId()) == null) {
                    jsonArrayHashMap.put(e.getId(), jsonArray2);
                } else {
                    JSONArray objects = jsonArrayHashMap.get(e.getId());
                    objects.addAll(jsonArray2);
                    jsonArrayHashMap.put(e.getId(), objects);
                }

            }
        }
        List<QywxExternalUserDTO> qywxExternalUserDTOS = new ArrayList<>();
        jsonArrayHashMap.forEach((k, v) -> {
            List<WeBanUserDTO> weBanUserDTOS = JSONArray.parseArray(v.toJSONString(), WeBanUserDTO.class);
            try {
                for (WeBanUserDTO weBanUserDTO : weBanUserDTOS) {
                    String externalUserid = weBanUserDTO.getId();
                    QywxExternalUserDTO qywxExternalUserDto = new QywxExternalUserDTO();
                    // createtime
                    if (weBanUserDTO.getCreatedAt() != null)
                        qywxExternalUserDto.setCreateTime(
                                new Date(Long.parseLong(String.valueOf(weBanUserDTO.getCreatedAt())) * 1000));
                    // adviserId
                    qywxExternalUserDto.setAdviserId(k);
                    // externalUserid
                    qywxExternalUserDto.setExternalUserid(externalUserid);
                    // name
                    if (weBanUserDTO.getName() != null) {
                        String s = new String(weBanUserDTO.getName().getBytes(), StandardCharsets.UTF_8);
                        qywxExternalUserDto.setName(s);
                    }

                    // type
                    if (weBanUserDTO.getType() != null)
                        qywxExternalUserDto.setType(weBanUserDTO.getType());
                    // avatar
                    if (weBanUserDTO.getAvatar() != null)
                        qywxExternalUserDto.setAvatar(weBanUserDTO.getAvatar());
                    // gender
                    if (weBanUserDTO.getGender() != null)
                        qywxExternalUserDto.setGender(weBanUserDTO.getGender());
                    // unionid
                    if (weBanUserDTO.getUnionid() != null)
                        qywxExternalUserDto.setUnionId(weBanUserDTO.getUnionid());
                    // state
                    qywxExternalUserDto.setState("WCZ");
                    qywxExternalUserDTOS.add(qywxExternalUserDto);
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
        });
        Map<String, QywxExternalUserDTO> qywxExternalUserDTOMap = qywxExternalUserDTOS.stream().collect(Collectors.toMap(QywxExternalUserDTO::getExternalUserid, Function.identity(), (v1, v2) -> v2));
        List<String> userExternalUserids = qywxExternalUserDTOS.stream().map(QywxExternalUserDTO::getExternalUserid).collect(Collectors.toList());
        List<List<String>> result = new ArrayList<>();
        int currentIndex = 0;
        while (currentIndex < userExternalUserids.size()) {
            int endIndex = Math.min(currentIndex + 20, userExternalUserids.size());
            List<String> subList = userExternalUserids.subList(currentIndex, endIndex);
            result.add(new ArrayList<>(subList)); // 创建一个新的ArrayList来避免对原始列表的修改影响子列表
            currentIndex += 20;
        }
        // 客户详情
        String urlDetail = StringUtil.merge("https://open.weibanzhushou.com/open-api/external_user/batch_get?",
                "access_token={accessToken}");
        HashMap<String, Object> paramMapDetail = new HashMap<>();
        for (List<String> a : result) {
            // 顾问id
            paramMapDetail.put("accessToken", weibanTokenJsonObject.get("access_token").toString());
//            JSONObject weibanUserDetailJsonObject = restTemplate.getForObject(urlDetail, JSONObject.class, paramMapDetail);
            JSONObject jsonObject = new JSONObject();
            List<String> objects = new ArrayList<>();
            log.info("当前请求用户组--------------------------" + a);
            objects.addAll(a);
            jsonObject.put("id_list", objects);
            JSONObject weibanUserDetailJsonObject = restTemplate.postForObject(urlDetail, jsonObject, JSONObject.class, paramMapDetail);
            if ((Integer)weibanUserDetailJsonObject.get("errcode") == 0) {
                List<WeBanUserDTO> weBanUserDTOS = JSONArray.parseArray(weibanUserDetailJsonObject.getJSONArray("external_user").toJSONString(), WeBanUserDTO.class);
                Map<String, List<FollowStaffsDTO>> followStaffsDTOs = weBanUserDTOS.stream().collect(Collectors.toMap(WeBanUserDTO::getId, WeBanUserDTO::getFollowStaffs));
                followStaffsDTOs.forEach((k, v) -> {
                    QywxExternalUserDTO qywxExternalUserDTO = qywxExternalUserDTOMap.get(k);
                    List<FollowStaffsDTO> collect1 = v.stream().filter(FollowStaffsDTO -> collect.get(qywxExternalUserDTO.getAdviserId()).equals(FollowStaffsDTO.getStaffId())).collect(Collectors.toList());
                    if (!collect1.isEmpty()) {
                        FollowStaffsDTO followStaffsDTO = collect1.get(0);
//                        Integer followTime = followStaffsDTO.getFollowTime();
//                        Date date = new Date(followTime);
//                        String format = sdf.format(date);
                        qywxExternalUserDTO.setTagsDTOS(followStaffsDTO.getTags());
                        String jsonString = JSONObject.toJSONString(followStaffsDTO.getTags());
                        qywxExternalUserDTO.setTags(jsonString);
//                        qywxExternalUserDTO.setCreateTime(format);
                    }
                    qywxExternalUserDTOMap.put(k, qywxExternalUserDTO);
                });
            }
        }
        List<QywxExternalUserDTO> collect1 = qywxExternalUserDTOMap.values().stream().collect(Collectors.toList());
        qywxExternalUserDAO.bacthAdd(collect1);
        log.info("总数量为-------------------------" + length.get());
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
