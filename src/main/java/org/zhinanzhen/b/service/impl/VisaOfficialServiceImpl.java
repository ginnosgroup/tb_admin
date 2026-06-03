package org.zhinanzhen.b.service.impl;


import cn.hutool.log.Log;
import com.alibaba.fastjson.JSONArray;
import com.ikasoa.core.ErrorCodeEnum;
import com.ikasoa.core.utils.ObjectUtil;
import com.ikasoa.core.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zhinanzhen.b.config.GlobalThreadPool;
import org.zhinanzhen.b.controller.BaseCommissionOrderController;
import org.zhinanzhen.b.dao.*;
import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.dao.pojo.customer.CustomerInformationDO;
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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service("VisaOfficialService")
@Slf4j
public class VisaOfficialServiceImpl extends BaseService implements VisaOfficialService {

    private static Double thresholdsAmount = 3000.00;

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

    @Resource
    private SchoolDAO schoolDAO;

    @Resource
    private InsuranceCompanyDAO insuranceCompanyDAO;

    @Resource
    private ServiceOrderManageDAO serviceOrderManageDAO;

    // ========== Batch context for preloaded data ==========
    @lombok.Data
    @lombok.NoArgsConstructor
    static class VisaOfficialBatchContext {
        Map<Integer, UserDO> userMap = new HashMap<>();
        Map<Integer, AdviserDO> adviserMap = new HashMap<>();
        Map<Integer, OfficialDO> officialMap = new HashMap<>();
        Map<Integer, ReceiveTypeDO> receiveTypeMap = new HashMap<>();
        Map<Integer, ServiceDO> serviceMap = new HashMap<>();
        Map<Integer, MaraDO> maraMap = new HashMap<>();
        Map<Integer, SchoolDO> schoolMap = new HashMap<>();
        Map<Integer, ApplicantDO> applicantMap = new HashMap<>();
        Map<Integer, ServiceOrderDO> serviceOrderMap = new HashMap<>();
        Map<Integer, ServiceAssessDO> serviceAssessMap = new HashMap<>();
        Map<Integer, ServicePackageDO> servicePackageMap = new HashMap<>();
        Map<Integer, ServicePackagePriceDO> servicePackagePriceMap = new HashMap<>();
        Map<Integer, EoiServiceInfo> eoiServiceMap = new HashMap<>();
        Map<Integer, SchoolCourseDO> schoolCourseMap = new HashMap<>();
        Map<Integer, SchoolInstitutionDO> schoolInstitutionMap = new HashMap<>();
        Map<Integer, ServiceOrderAndManage> serviceOrderManageMap = new HashMap<>();
        Map<Integer, ServiceOrderInsuranceDO> insuranceMap = new HashMap<>();
        Map<Integer, InsuranceCompanyDO> insuranceCompanyMap = new HashMap<>();
        Map<Integer, List<RemindDO>> remindMap = new HashMap<>();
        Map<Integer, List<VisaDO>> visaByServiceOrderIdMap = new HashMap<>();
        Map<Integer, List<ServiceOrderDO>> childrenByParentIdMap = new HashMap<>();
        Map<Integer, List<ServiceOrderApplicantDO>> serviceOrderApplicantMap = new HashMap<>();
        Map<Integer, List<ApplicantListDO>> applicantListMap = new HashMap<>();
        Map<Integer, CustomerInformationDO> customerInformationMap = new HashMap<>();
        Map<Integer, List<RefundDO>> refundByVisaIdMap = new HashMap<>();
        Map<String, List<VisaOfficialDO>> visaOfficialByCodeMap = new HashMap<>();
        List<String> longTermVisaCodes = new ArrayList<>();
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    static class EoiServiceInfo {
        private String serviceCode;
        private ServicePackageDTO servicePackage;
    }

    private VisaOfficialBatchContext preloadBatchData(List<VisaOfficialListDO> list) {
        long _startPreload = System.currentTimeMillis();
        VisaOfficialBatchContext ctx = new VisaOfficialBatchContext();
        if (list == null || list.isEmpty()) return ctx;

        Set<Integer> serviceOrderIds = new LinkedHashSet<>();
        Set<Integer> userIds = new LinkedHashSet<>();
        Set<Integer> adviserIds = new LinkedHashSet<>();
        Set<Integer> officialIds = new LinkedHashSet<>();
        Set<Integer> receiveTypeIds = new LinkedHashSet<>();
        Set<Integer> serviceIds = new LinkedHashSet<>();
        Set<Integer> maraIds = new LinkedHashSet<>();
        Set<String> codes = new LinkedHashSet<>();
        Set<Integer> visaOfficialIds = new LinkedHashSet<>();

        for (VisaOfficialListDO vo : list) {
            visaOfficialIds.add(vo.getId());
            serviceOrderIds.add(vo.getServiceOrderId());
            if (vo.getUserId() > 0) userIds.add(vo.getUserId());
            if (vo.getAdviserId() > 0) adviserIds.add(vo.getAdviserId());
            if (vo.getOfficialId() > 0) officialIds.add(vo.getOfficialId());
            if (vo.getReceiveTypeId() > 0) receiveTypeIds.add(vo.getReceiveTypeId());
            if (vo.getServiceId() > 0) serviceIds.add(vo.getServiceId());
            if (vo.getMaraId() > 0) maraIds.add(vo.getMaraId());
            if (StringUtil.isNotEmpty(vo.getCode())) codes.add(vo.getCode());
        }

        // Batch-load basic entities
        long _p0 = System.currentTimeMillis();
        if (!userIds.isEmpty()) userDao.listByIds(new ArrayList<>(userIds)).forEach(u -> ctx.userMap.put(u.getId(), u));
        if (!adviserIds.isEmpty()) adviserDao.listByIds(new ArrayList<>(adviserIds)).forEach(a -> ctx.adviserMap.put(a.getId(), a));
        if (!officialIds.isEmpty()) officialDao.listByIds(new ArrayList<>(officialIds)).forEach(o -> ctx.officialMap.put(o.getId(), o));
        if (!receiveTypeIds.isEmpty()) receiveTypeDao.listByIds(new ArrayList<>(receiveTypeIds)).forEach(r -> ctx.receiveTypeMap.put(r.getId(), r));
        if (!serviceIds.isEmpty()) serviceDao.listByIds(new ArrayList<>(serviceIds)).forEach(s -> ctx.serviceMap.put(s.getId(), s));
        if (!maraIds.isEmpty()) maraDAO.listByIds(new ArrayList<>(maraIds)).forEach(m -> ctx.maraMap.put(m.getId(), m));
        long _p1 = System.currentTimeMillis();

        // Batch-load ServiceOrderDOs
        if (!serviceOrderIds.isEmpty()) {
            List<ServiceOrderDO> orders = serviceOrderDao.listByIds(new ArrayList<>(serviceOrderIds));
            orders.forEach(o -> ctx.serviceOrderMap.put(o.getId(), o));
            long _p2a = System.currentTimeMillis();

            Set<Integer> schoolIds = new LinkedHashSet<>();
            Set<Integer> soServiceIds = new LinkedHashSet<>();
            Set<Integer> servicePackageIds = new LinkedHashSet<>();
            Set<Integer> soReceiveTypeIds = new LinkedHashSet<>();
            Set<Integer> soUserIds = new LinkedHashSet<>();
            Set<Integer> applicantIds = new LinkedHashSet<>();
            Set<Integer> soMaraIds = new LinkedHashSet<>();
            Set<Integer> soAdviserIds = new LinkedHashSet<>();
            Set<Integer> soOfficialIds = new LinkedHashSet<>();
            Set<Integer> courseIds = new LinkedHashSet<>();
            Set<Integer> serviceAssessIdInts = new LinkedHashSet<>();
            Set<Integer> parentIds = new LinkedHashSet<>();
            Set<Integer> applicantParentIds = new LinkedHashSet<>();

            for (ServiceOrderDO so : orders) {
                if (so.getSchoolId() > 0) schoolIds.add(so.getSchoolId());
                if (so.getServiceId() > 0) soServiceIds.add(so.getServiceId());
                if (so.getServicePackageId() > 0) servicePackageIds.add(so.getServicePackageId());
                if (so.getReceiveTypeId() > 0) soReceiveTypeIds.add(so.getReceiveTypeId());
                if (so.getUserId() > 0) soUserIds.add(so.getUserId());
                if (so.getApplicantId() > 0) applicantIds.add(so.getApplicantId());
                if (so.getMaraId() > 0) soMaraIds.add(so.getMaraId());
                if (so.getAdviserId() > 0) soAdviserIds.add(so.getAdviserId());
                if (so.getOfficialId() > 0) soOfficialIds.add(so.getOfficialId());
                if (so.getCourseId() > 0) courseIds.add(so.getCourseId());
                if (so.getServiceAssessId() != null && !"0".equals(so.getServiceAssessId())) {
                    try { serviceAssessIdInts.add(Integer.parseInt(so.getServiceAssessId())); } catch (NumberFormatException ignored) {}
                }
                if (so.getParentId() > 0) parentIds.add(so.getParentId());
                if (so.getApplicantParentId() > 0) applicantParentIds.add(so.getApplicantParentId());
            }

            // Batch-load remaining entities from ServiceOrder
            if (!schoolIds.isEmpty()) schoolDAO.listByIds(new ArrayList<>(schoolIds)).forEach(s -> ctx.schoolMap.put(s.getId(), s));
            if (!soServiceIds.isEmpty()) serviceDao.listByIds(new ArrayList<>(soServiceIds)).forEach(s -> ctx.serviceMap.put(s.getId(), s));
            if (!servicePackageIds.isEmpty()) servicePackageDAO.listByIds(new ArrayList<>(servicePackageIds)).forEach(s -> ctx.servicePackageMap.put(s.getId(), s));
            if (!soReceiveTypeIds.isEmpty()) receiveTypeDao.listByIds(new ArrayList<>(soReceiveTypeIds)).forEach(r -> ctx.receiveTypeMap.put(r.getId(), r));
            if (!soUserIds.isEmpty()) userDao.listByIds(new ArrayList<>(soUserIds)).forEach(u -> ctx.userMap.put(u.getId(), u));
            if (!applicantIds.isEmpty()) applicantDao.listByIds(new ArrayList<>(applicantIds)).forEach(a -> ctx.applicantMap.put(a.getId(), a));
            if (!soMaraIds.isEmpty()) maraDAO.listByIds(new ArrayList<>(soMaraIds)).forEach(m -> ctx.maraMap.put(m.getId(), m));
            if (!soAdviserIds.isEmpty()) adviserDao.listByIds(new ArrayList<>(soAdviserIds)).forEach(a -> ctx.adviserMap.put(a.getId(), a));
            if (!soOfficialIds.isEmpty()) officialDao.listByIds(new ArrayList<>(soOfficialIds)).forEach(o -> ctx.officialMap.put(o.getId(), o));
            if (!courseIds.isEmpty()) schoolCourseDAO.listByIds(new ArrayList<>(courseIds)).forEach(sc -> ctx.schoolCourseMap.put(sc.getId(), sc));
            if (!serviceAssessIdInts.isEmpty()) {
                List<String> ids = serviceAssessIdInts.stream().map(String::valueOf).collect(Collectors.toList());
                serviceAssessDao.listByIds(ids).forEach(sa -> ctx.serviceAssessMap.put(sa.getId(), sa));
            }
            if (!serviceOrderIds.isEmpty()) {
                serviceOrderManageDAO.listByServiceOrderIds(new ArrayList<>(serviceOrderIds)).forEach(m -> ctx.serviceOrderManageMap.put(m.getServiceOrderId(), m));
                List<ServiceOrderInsuranceDO> insuranceList = insuranceCompanyDAO.listByServiceOrderIds(new ArrayList<>(serviceOrderIds));
                for (ServiceOrderInsuranceDO ins : insuranceList) ctx.insuranceMap.put(ins.getServiceOrderId(), ins);
                visaDAO.listByServiceOrderIds(new ArrayList<>(serviceOrderIds)).forEach(v -> {
                    ctx.visaByServiceOrderIdMap.computeIfAbsent(v.getServiceOrderId(), k -> new ArrayList<>()).add(v);
                });
                customerInformationDAO.listByServiceOrderIds(new ArrayList<>(serviceOrderIds)).forEach(ci -> ctx.customerInformationMap.put(ci.getServiceOrderId(), ci));
                serviceOrderApplicantDao.listByServiceOrderIds(new ArrayList<>(serviceOrderIds)).forEach(soa -> {
                    ctx.serviceOrderApplicantMap.computeIfAbsent(soa.getServiceOrderId(), k -> new ArrayList<>()).add(soa);
                });
            }
            // Batch-load servicePackagePrices by service IDs from both main list and service orders
            Set<Integer> allSvcIdsForPrice = new LinkedHashSet<>();
            allSvcIdsForPrice.addAll(serviceIds);
            allSvcIdsForPrice.addAll(soServiceIds);
            // Load children orders and collect their IDs for price lookups
            if (!parentIds.isEmpty()) {
                serviceOrderDao.listByParentIds(new ArrayList<>(parentIds)).forEach(child -> {
                    ctx.childrenByParentIdMap.computeIfAbsent(child.getParentId(), k -> new ArrayList<>()).add(child);
                    if (child.getServiceId() > 0) allSvcIdsForPrice.add(child.getServiceId());
                    if (child.getServicePackageId() > 0) allSvcIdsForPrice.add(child.getServicePackageId());
                });
            }
            if (!allSvcIdsForPrice.isEmpty()) {
                servicePackagePriceDAO.listByServiceIds(new ArrayList<>(allSvcIdsForPrice)).forEach(p -> ctx.servicePackagePriceMap.put(p.getServiceId(), p));
            }
            long _p2b = System.currentTimeMillis();

            // Load insurance companies (batch)
            Set<Integer> insCompanyIds = new LinkedHashSet<>();
            for (ServiceOrderInsuranceDO ins : ctx.insuranceMap.values()) {
                if (ins.getInsuranceCompanyId() > 0) insCompanyIds.add(ins.getInsuranceCompanyId());
            }
            if (!insCompanyIds.isEmpty()) {
                insuranceCompanyDAO.listByIds(new ArrayList<>(insCompanyIds))
                    .forEach(ic -> ctx.insuranceCompanyMap.put(ic.getId(), ic));
            }

            // Load school institutions from courses
            Set<Integer> institutionIds = new LinkedHashSet<>();
            for (SchoolCourseDO sc : ctx.schoolCourseMap.values()) {
                institutionIds.add(sc.getProviderId());
            }
            if (!institutionIds.isEmpty()) {
                schoolInstitutionDAO.listByIds(new ArrayList<>(institutionIds)).forEach(si -> ctx.schoolInstitutionMap.put(si.getId(), si));
            }

            // Load applicant lists (batch)
            if (!serviceOrderIds.isEmpty()) {
                List<ApplicantListDO> allApplicantLists = serviceOrderDao.ApplicantListByServiceOrderIds(new ArrayList<>(serviceOrderIds));
                if (allApplicantLists != null) {
                    // 用于去重：每个 serviceOrderId 下的 applicant_id 只留一条（对应原 SQL 的 GROUP BY applicant_id）
                    Map<Integer, Set<Integer>> seenApplicants = new HashMap<>();
                    for (ApplicantListDO al : allApplicantLists) {
                        Integer key = al.getApplicantParentId() != null && al.getApplicantParentId() > 0
                            && serviceOrderIds.contains(al.getApplicantParentId())
                            ? al.getApplicantParentId() : al.getId();
                        if (key != null && key > 0) {
                            seenApplicants.computeIfAbsent(key, k -> new HashSet<>());
                            if (seenApplicants.get(key).add(al.getApplicantId())) {
                                ctx.applicantListMap.computeIfAbsent(key, k -> new ArrayList<>()).add(al);
                            }
                        }
                    }
                }
            }
        }
        long _p2 = System.currentTimeMillis();

        // Batch-load visa official by codes
        long _p3 = System.currentTimeMillis();
        if (!codes.isEmpty()) {
            visaOfficialDao.listByCodes(new ArrayList<>(codes)).forEach(vo -> {
                ctx.visaOfficialByCodeMap.computeIfAbsent(vo.getCode(), k -> new ArrayList<>()).add(vo);
            });
        }

        // Batch-load reminds
        long _p4 = System.currentTimeMillis();
        if (!visaOfficialIds.isEmpty()) {
            remindDao.listByVisaOfficialIds(new ArrayList<>(visaOfficialIds), AbleStateEnum.ENABLED.toString()).forEach(r -> {
                ctx.remindMap.computeIfAbsent(r.getVisaId(), k -> new ArrayList<>()).add(r);
            });
        }

        // Batch-load refunds for all visaIds from visaByServiceOrderIdMap
        Set<Integer> allVisaIds = new LinkedHashSet<>();
        for (List<VisaDO> visas : ctx.visaByServiceOrderIdMap.values()) {
            for (VisaDO v : visas) allVisaIds.add(v.getId());
        }
        // Also add visaOfficialIds themselves (used as visa_id in refund table)
        allVisaIds.addAll(visaOfficialIds);
        long _p5 = System.currentTimeMillis();
        if (!allVisaIds.isEmpty()) {
            refundDAO.listByVisaIds(new ArrayList<>(allVisaIds)).forEach(r -> {
                ctx.refundByVisaIdMap.computeIfAbsent(r.getVisaId(), k -> new ArrayList<>()).add(r);
            });
        }

        // Load long-term visa codes
        long _p6 = System.currentTimeMillis();
        ctx.longTermVisaCodes = serviceDao.listLongTimeVisa();

        return ctx;
    }

    public VisaOfficialDTO putVisaOfficialDTO(VisaOfficialListDO visaListDo, VisaOfficialBatchContext ctx) throws ServiceException {
        VisaOfficialDTO visaOfficialDto = putVisaOfficialDTO((VisaOfficialDO) visaListDo, ctx);
        List<ApplicantListDO> applicantListDOS = ctx.applicantListMap.get(visaListDo.getServiceOrderId());
        List<ApplicantDTO> applicantDTOS = new ArrayList<>();
        if (applicantListDOS != null) {
            for (ApplicantListDO applicantListDO : applicantListDOS) {
                if (applicantListDO.getApplicantId() > 0) {
                    ApplicantDO applicantDO = ctx.applicantMap.get(applicantListDO.getApplicantId());
                    ApplicantDTO applicantDto = new ApplicantDTO();
                    if (applicantDO != null) {
                        applicantDto = mapper.map(applicantDO, ApplicantDTO.class);
                    }
                    List<ServiceOrderApplicantDO> soaList = ctx.serviceOrderApplicantMap.get(visaListDo.getServiceOrderId());
                    if (soaList != null) {
                        for (ServiceOrderApplicantDO soa : soaList) {
                            if (Objects.equals(soa.getApplicantId(), applicantListDO.getApplicantId())) {
                                applicantDto.setUrl(soa.getUrl());
                                applicantDto.setContent(soa.getContent());
                                break;
                            }
                        }
                    }
                    applicantDto.setServiceOrderId(applicantListDO.getId());
                    applicantDto.setSubmitMM(ctx.customerInformationMap.get(applicantListDO.getId()) != null);
                    applicantDTOS.add(applicantDto);
                }
            }
        }
        visaOfficialDto.setApplicant(applicantDTOS);
        visaOfficialDto.setApplicantId(visaListDo.getApplicantId());
        return visaOfficialDto;
    }

    public VisaOfficialDTO putVisaOfficialDTO(VisaOfficialDO visaListDo, VisaOfficialBatchContext ctx) throws ServiceException {
        VisaOfficialDTO visaOfficialDto = mapper.map(visaListDo, VisaOfficialDTO.class);
        if (visaOfficialDto.getUserId() > 0) {
            UserDO userDo = ctx.userMap.get(visaOfficialDto.getUserId());
            if (userDo != null) {
                visaOfficialDto.setUserName(userDo.getName());
                visaOfficialDto.setPhone(userDo.getPhone());
                visaOfficialDto.setBirthday(userDo.getBirthday());
                visaOfficialDto.setUser(mapper.map(userDo, UserDTO.class));
            }
        }
        AdviserDO adviserDo = ctx.adviserMap.get(visaListDo.getAdviserId());
        if (adviserDo != null) {
            visaOfficialDto.setAdviserName(adviserDo.getName());
        }
        OfficialDO officialDo = ctx.officialMap.get(visaListDo.getOfficialId());
        if (officialDo != null) {
            visaOfficialDto.setOfficialName(officialDo.getName());
        }
        ReceiveTypeDO receiveTypeDo = ctx.receiveTypeMap.get(visaListDo.getReceiveTypeId());
        if (receiveTypeDo != null) {
            visaOfficialDto.setReceiveTypeName(receiveTypeDo.getName());
        }
        ServiceDO serviceDo = ctx.serviceMap.get(visaListDo.getServiceId());
        if (serviceDo != null) {
            visaOfficialDto.setServiceCode(serviceDo.getCode());
        }
        List<VisaOfficialDO> codeList = ctx.visaOfficialByCodeMap.get(visaOfficialDto.getCode());
        if (codeList != null) {
            boolean longTermVisa = false;
            ServiceDO svc = ctx.serviceMap.get(visaOfficialDto.getServiceId());
            if (svc != null && ctx.longTermVisaCodes.contains(svc.getCode())) {
                longTermVisa = true;
            }
            double totalPerAmount = 0.00;
            double totalAmount = 0.00;
            for (VisaOfficialDO vo : codeList) {
                if (longTermVisa) {
                    totalPerAmount = vo.getPerAmount();
                } else {
                    totalPerAmount += vo.getPerAmount();
                }
                if (vo.getPaymentVoucherImageUrl1() != null || vo.getPaymentVoucherImageUrl2() != null
                        || vo.getPaymentVoucherImageUrl3() != null || vo.getPaymentVoucherImageUrl4() != null
                        || vo.getPaymentVoucherImageUrl5() != null)
                    totalAmount += vo.getAmount();
            }
            visaOfficialDto.setTotalPerAmount(totalPerAmount);
            visaOfficialDto.setTotalAmount(totalAmount);
        }

        List<RefundDO> refunds = ctx.refundByVisaIdMap.get(visaListDo.getId());
        if (refunds != null) {
            for (RefundDO r : refunds) {
                if (StringUtil.equals("PAID", r.getState())) {
                    visaOfficialDto.setRefunded(true);
                    break;
                }
            }
        }

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

    public VisaOfficialDTO putVisaOfficialDTOV2(VisaOfficialDTO visaOfficialDto) throws ServiceException {
        if (visaOfficialDto.getUserId() > 0) {
            UserDO userDo = userDao.getUserById(visaOfficialDto.getUserId());
            visaOfficialDto.setUserName(userDo.getName());
            visaOfficialDto.setPhone(userDo.getPhone());
            visaOfficialDto.setBirthday(userDo.getBirthday());
            visaOfficialDto.setUser(mapper.map(userDo, UserDTO.class));
        }
        AdviserDO adviserDo = adviserDao.getAdviserById(visaOfficialDto.getAdviserId());
        if (adviserDo != null) {
            visaOfficialDto.setAdviserName(adviserDo.getName());
        }
        OfficialDO officialDo = officialDao.getOfficialById(visaOfficialDto.getOfficialId());
        if (officialDo != null) {
            visaOfficialDto.setOfficialName(officialDo.getName());
        }
        ReceiveTypeDO receiveTypeDo = receiveTypeDao.getReceiveTypeById(visaOfficialDto.getReceiveTypeId());
        if (receiveTypeDo != null) {
            visaOfficialDto.setReceiveTypeName(receiveTypeDo.getName());
        }
        ServiceDO serviceDo = serviceDao.getServiceById(visaOfficialDto.getServiceId());
        if (serviceDo != null) {
            visaOfficialDto.setServiceCode(serviceDo.getCode());
        }
        List<VisaOfficialDO> list = visaOfficialDao.listVisaByCode(visaOfficialDto.getCode());
        if (list != null) {
            boolean longTermVisa = false;
            List<String> arrayList = serviceDao.listLongTimeVisa();
            String serviceType = serviceDao.getServiceById(visaOfficialDto.getServiceId()).getCode();
            if (arrayList.contains(serviceType)) {
                longTermVisa = true;
            }
            double totalPerAmount = 0.00;
            double totalAmount = 0.00;
            for (VisaOfficialDO visaOfficialDo : list) {
                if (longTermVisa) {
                    totalPerAmount = visaOfficialDo.getPerAmount();
                } else {
                    totalPerAmount += visaOfficialDo.getPerAmount();
                }
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
        RefundDO refundDo = refundDao.getRefundByVisaId(visaOfficialDto.getId());
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
            ServiceOrderDO serviceOrderById = serviceOrderDAO.getServiceOrderById(visaOfficialDTO.getServiceOrderId());
            if ("COMPLETE".equalsIgnoreCase(serviceOrderById.getState())) {
                return serviceOrderById.getId();
            }
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

//        OfficialDO officialById = officialDao.getOfficialById(serviceOrderById.getOfficialId());

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
        if (visaOfficialDTO.getIsRefund()) {
            visaOfficialDao.updateVisaOfficial(visaOfficialDO);
        } else {
            if (visaOfficialDao.addVisa(visaOfficialDO) > 0) {
                visaOfficialDTO.setId(visaOfficialDO.getId());
                visaOfficialDTO.setCommissionAmount(visaOfficialDO.getCommissionAmount());
                visaOfficialDTO.setPredictCommission(visaOfficialDO.getPredictCommission());
                visaOfficialDTO.setCalculation(visaOfficialDO.getCalculation());
            } else {
                return 0;
            }
        }
        return visaOfficialDO.getId();
    }

    @Override
    public int addVisaTmp(VisaOfficialDO visaOfficialDO) throws ServiceException {
        return visaOfficialDao.addVisa(visaOfficialDO);
    }

    private VisaOfficialDO buildCommission(ServiceOrderDO serviceOrderById, VisaOfficialDTO visaOfficialDTO, boolean pay, int region, String packType, boolean suborder) throws ServiceException {
        VisaOfficialDO visaOfficialDO = mapper.map(visaOfficialDTO, VisaOfficialDO.class);
        if (serviceOrderById.getBindingOrder() != null && serviceOrderById.getBindingOrder() > 0) {
            pay = true;
        }
//        if (!pay) {
//            ServicePackagePriceDO servicePackagePriceDO = servicePackagePriceDAO.getByServiceId(serviceOrderById.getServiceId());
//            ServicePackagePriceV2DTO servicePackagePriceV2DTO = closeJugd(serviceOrderById.getOfficialId(), servicePackagePriceDO);
//            if (ObjectUtil.isNotNull(servicePackagePriceDO) && servicePackagePriceV2DTO.getRuler() == 1) {
//                String calculation = new String();
//                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                calculation = "1" + "|" + servicePackagePriceDO.getThirdPrince() + "," + servicePackagePriceDO.getAmount() + "|" + dateFormat.format(servicePackagePriceDO.getGmtModify());
//                visaOfficialDO.setCalculation(calculation);
//                visaOfficialDO.setPredictCommission(servicePackagePriceV2DTO.getAmount());
//                visaOfficialDO.setPredictCommissionCNY(visaOfficialDO.getPredictCommission() * visaOfficialDO.getExchangeRate());
//                if (region == 1) {
//                    visaOfficialDO.setPredictCommissionCNY(servicePackagePriceV2DTO.getAmount());
//                    visaOfficialDO.setPredictCommission(visaOfficialDO.getPredictCommission() / visaOfficialDO.getExchangeRate());
//                }
//                visaOfficialDO.setCommissionAmount(0.00);
//                visaOfficialDO.setPredictCommissionAmount(0.00);
//            } else {
//                visaOfficialDO.setPredictCommission(0.00);
//                visaOfficialDO.setCommissionAmount(0.00);
//                visaOfficialDO.setPredictCommission(0.00);
//                visaOfficialDO.setCalculation(null);
//                visaOfficialDO.setPredictCommissionCNY(0.00);
//            }
//            return visaOfficialDO;
//        }
        CommissionAmountDTO commissionAmountDTO = new CommissionAmountDTO();
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
        double amount = 0.00;
        double rate = 0.00;
        boolean longTermVisa = false;
        double calculateProportion = 0.00; // 计算比例
        // 查询所有佣金订单并计算总金额
        List<VisaDO> visaDOS = new ArrayList<>();
        // 父子订单金额计算
        visaDOS = visaDAO.listVisaByServiceOrderId(serviceOrderById.getId());
        ServiceOrderDO serviceOrderByParentId = new ServiceOrderDO();
        if (suborder) {
            serviceOrderByParentId = serviceOrderDao.getServiceOrderById(serviceOrderById.getApplicantParentId());
            int id = serviceOrderDao.getServiceOrderById(serviceOrderById.getApplicantParentId()).getId();
            visaDOS = visaDAO.listVisaByServiceOrderId(id);
        }
        double proportion = 0.00;
        if (visaDOS.isEmpty()) {
            ServiceOrderAndManage serviceOrderAndManageById = serviceOrderManageDAO.getServiceOrderAndManageById(serviceOrderById.getId());
            if (serviceOrderAndManageById != null) {
                ServiceOrderDO serviceOrderManage = serviceOrderManageDAO.getServiceOrderById(serviceOrderAndManageById.getServiceOrderManageId());
                proportion = serviceOrderById.getAmount() / serviceOrderManage.getAmount();
                visaDOS = visaDAO.listVisaByServiceOrderId(serviceOrderAndManageById.getServiceOrderManageId());
                for (VisaDO visaDO : visaDOS) {
                    visaDO.setReceivable(visaDO.getReceivable() * proportion);
                    visaDO.setReceived(visaDO.getReceived() * proportion);
                    visaDO.setAmount(visaDO.getAmount() * proportion);
                    visaDO.setPerAmount(visaDO.getPerAmount() * proportion);
                    visaDO.setExpectAmount(visaDO.getExpectAmount() * proportion);
                }
            }
        }
        // 设置amount金额
        List<RefundDO> refundDOS = new ArrayList<>();
        boolean isSIV = false;
        boolean isNSV = false;
        String code = serviceDao.getServiceById(serviceOrderById.getServiceId()).getCode();
        String serviceType = code.replaceAll("[^\\p{L}\\p{N}\\p{Script=Han}]+", "");
        if (ObjectUtil.isNotNull(serviceOrderByParentId)) {
            isSIV = "SIV".equals(serviceOrderByParentId.getType());
            isNSV = "NSV".equals(serviceOrderByParentId.getType());
        }
        double extraAmount = 0.00;
        List<String> serviceList = new ArrayList<>();
        // 判断是否需要计算extra金额
        if (!isSIV && !isNSV) {
            ServiceDO serviceById = serviceDao.getServiceById(serviceOrderById.getServiceId());
            if (ObjectUtil.isNotNull(serviceById)) {
                serviceList = serviceDao.listExtraAmount();
            }
        }
        // 计算退款
        for (VisaDO visaDO : visaDOS) {
            RefundDO refundByVisaId = refundDAO.getRefundByVisaId(visaDO.getId());
            if (ObjectUtil.isNotNull(refundByVisaId)) {
                visaDO.setAmount(visaDO.getAmount() - refundByVisaId.getAmount() * proportion);
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
        if ("VISA".equals(packType)) {
            if (suborder) {
                // 签证600和870计算
                ServiceDO serviceById = serviceDao.getServiceById(serviceOrderById.getServiceId());
                if ("600".equals(serviceById.getCode()) || "870".equals(serviceById.getCode())) {
                    EOICount = 0;
                    deriveOrder = serviceOrderDao.getZiOrder(serviceOrderById.getApplicantParentId());
                    for (ServiceOrderDTO a : deriveOrder) {
                        ServiceDO serviceByIdTmp = serviceDao.getServiceById(a.getServiceId());
                        if ("600".equals(serviceByIdTmp.getCode()) || "870".equals(serviceById.getCode())) {
                            EOICount++;
                        }
                    }
                }
            }
            List<String> arrayList = serviceDao.listLongTimeVisa();
            if (arrayList.contains(serviceType)) {
                longTermVisa = true;
            }
        }
        OfficialDO officialById = officialDAO.getOfficialById(serviceOrderById.getOfficialId());
        if (region == 1) {
            // 计算extra金额
            amount = visaDOS.stream().mapToDouble(VisaDO::getAmount).sum(); // 收款总额
            if (isSIV || isNSV) {
//            if (isNSV) {
                ServicePackagePriceDO packagePriceDAOByServiceId = servicePackagePriceDAO.getByServiceId(serviceOrderByParentId.getServiceId());
//                if (amount > packagePriceDAOByServiceId.getMaxPrice()) {
//                    extraAmount = amount - packagePriceDAOByServiceId.getMaxPrice();
//                }
//            } else if (isSIV) {
                double serviceOrderByParentIdAmount = packagePriceDAOByServiceId.getMaxPrice();
                if (serviceOrderByParentIdAmount < serviceOrderById.getReceivable()) {
                    extraAmount = serviceOrderById.getReceivable() - serviceOrderByParentIdAmount;
                }
            } else if (serviceList.contains(serviceType)) {
                ServicePackagePriceDO byServiceId = servicePackagePriceDAO.getByServiceId(serviceOrderById.getServiceId());
                if (serviceType.contains("600")) {
                    amount = amount / EOICount;
                }
                if (byServiceId.getMaxPrice() < amount) {
                    extraAmount = amount - byServiceId.getMaxPrice();
                }
            }
        }
        if (!visaDOS.isEmpty()) {
            if (visaDOS.size() == 1) {
                amount = visaDOS.get(0).getAmount(); // 收款总额
                calculateProportion = 1.00;
                amount = amount - extraAmount;
            }
            if (visaDOS.size() > 1) {
                installment = true;
                amount = visaDOS.stream().mapToDouble(VisaDO::getAmount).sum(); // 收款总额
                if (!longTermVisa && (visaOfficialDO.getPerAmount() != amount)) {
                    visaOfficialDO.setPerAmount(amount);
                }
                if (suborder && (isSIV || isNSV)) {
                    if (visaDOS.stream().mapToDouble(VisaDO::getAmount).sum() != visaOfficialDO.getPerAmount()) {
                        visaOfficialDO.setPerAmount(serviceOrderByParentId.getPerAmount());
                    }
                    List<VisaOfficialDO> countvisaOfficialByServiceOrderPatrentId = visaOfficialDao.getCountvisaOfficialByServiceOrderPatrentId(serviceOrderById.getApplicantParentId());
                    if (visaOfficialDTO.getIsRefund()) {
                        if (!CollectionUtils.isEmpty(countvisaOfficialByServiceOrderPatrentId)) {
                            List<VisaOfficialDO> collect = countvisaOfficialByServiceOrderPatrentId.stream().sorted(Comparator.comparing(VisaOfficialDO::getGmtCreate)).collect(Collectors.toList());
                            amount = visaDOS.stream().mapToDouble(VisaDO::getAmount).sum() - extraAmount;
//                            if (visaOfficialDTO.getGmtCreate() == collect.get(0).getGmtCreate()) {
//                                calculateProportion = visaDOS.get(0).getAmount() / amount;
//                            } else {
//                                calculateProportion = visaDOS.stream().mapToDouble(VisaDO::getAmount).sum() / amount;
//                            }
                        }
                    } else {
                        amount = visaDOS.stream().mapToDouble(VisaDO::getAmount).sum() - extraAmount;
                        if (CollectionUtils.isEmpty(countvisaOfficialByServiceOrderPatrentId)) {
                            calculateProportion = visaDOS.get(0).getAmount() / amount;
                        } else {
                            calculateProportion = visaDOS.stream().mapToDouble(VisaDO::getAmount).sum() / amount;
                        }
                    }
                } else {
                    amount = visaDOS.stream().mapToDouble(VisaDO::getAmount).sum() - extraAmount; // 收款总额
                }
            }
        }
        // 免费绑定订单金额计算
        if (serviceOrderById.getBindingOrder() != null &&serviceOrderById.getBindingOrder() > 0) {
            ServicePackagePriceDO byId = servicePackagePriceDAO.getByServiceId(serviceOrderById.getServiceId());
            amount = byId.getCostPrince();
        }

        // 澳洲地区高峰月份rate处理
        OfficialGradeDO officialGradeById = officialGradeDao.getOfficialGradeById(officialById.getGradeId());
//        rate = officialGradeById.getRate();
        if (region == 0 && officialGradeById.getId() != 100001) {
            // 创建一个Calendar对象并设置时间为date对象的时间
            Calendar sss = Calendar.getInstance();
            sss.setTime(serviceOrderById.getReadcommittedDate());

            // 获取月份（注意：Calendar的月份是从0开始的，所以1代表二月，0代表一月）
            int month = sss.get(Calendar.MONTH) + 1; // 加1是因为我们需要从1开始的月份

            if (monthlist.contains(month)) {
//                rate = rate + 3;
                rate = 3;
            }
        }
        ServiceOrderDO serviceParentOrderById = serviceOrderDao.getServiceOrderById(serviceOrderById.getApplicantParentId());
        if ("CNY".equals(serviceOrderById.getCurrency())) {
            amount = amount / serviceOrderById.getExchangeRate();
            extraAmount = extraAmount / serviceOrderById.getExchangeRate();
        }
//        commissionAmountDTO.setRefund(refund); // 设置退款金额
        visaOfficiaCalculate(serviceOrderById, region, commissionAmountDTO, amount, rate, EOICount, officialGradeById,
                visaOfficialDO, deriveOrder, serviceParentOrderById, installment, longTermVisa, officialById,
                calculateProportion, isSIV, isNSV, extraAmount, serviceOrderByParentId);
        // EOI订单有删除情况的结算
        if (EOICount > 2) {
            List<VisaOfficialDO> visaOfficialDOS = new ArrayList<>();
            deriveOrder.forEach(e->{
                VisaOfficialDO byServiceOrderId = visaOfficialDao.getByServiceOrderIdOne(e.getId());
                if (ObjectUtil.isNotNull(byServiceOrderId)) {
                    visaOfficialDOS.add(byServiceOrderId);
                }
            });
            log.info("当前订单id-------------------" + visaOfficialDO.getServiceOrderId());
            if (visaOfficialDOS.size() == EOICount - 1 && EOICount < serviceParentOrderById.getEOINumber()) {
                ServicePackagePriceDO servicePackagePriceDO = servicePackagePriceDAO.getByServiceId(serviceOrderById.getServiceId());
                Double predictCommission = visaOfficialDO.getPredictCommission();
                double sumNew = predictCommission - servicePackagePriceDO.getMaxPrice() / serviceParentOrderById.getEOINumber();
                sumNew = (predictCommission + sumNew) * (EOICount - 1);
                visaOfficialDO.setPredictCommission(sumNew);
                visaOfficialDO.setPredictCommissionCNY(sumNew * visaOfficialDO.getExchangeRate());
            }
        }
        return visaOfficialDO;
    }


//    private VisaOfficialDO buildCommission(ServiceOrderDO serviceOrderById, VisaOfficialDTO visaOfficialDTO, boolean pay, int region, String packType, boolean suborder) throws ServiceException {
//        VisaOfficialDO visaOfficialDO = mapper.map(visaOfficialDTO, VisaOfficialDO.class);
//        if (serviceOrderById.getBindingOrder() != null && serviceOrderById.getBindingOrder() > 0) {
//            pay = true;
//        }
//        if (!pay) {
//            ServicePackagePriceDO servicePackagePriceDO = servicePackagePriceDAO.getByServiceId(serviceOrderById.getServiceId());
//            ServicePackagePriceV2DTO servicePackagePriceV2DTO = closeJugd(serviceOrderById.getOfficialId(), servicePackagePriceDO);
//            if (ObjectUtil.isNotNull(servicePackagePriceDO) && servicePackagePriceV2DTO.getRuler() == 1) {
//                String calculation = new String();
//                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                calculation = "1" + "|" + servicePackagePriceDO.getThirdPrince() + "," + servicePackagePriceDO.getAmount() + "|" + dateFormat.format(servicePackagePriceDO.getGmtModify());
//                visaOfficialDO.setCalculation(calculation);
//                visaOfficialDO.setPredictCommission(servicePackagePriceV2DTO.getAmount());
//                visaOfficialDO.setPredictCommissionCNY(visaOfficialDO.getPredictCommission() * visaOfficialDO.getExchangeRate());
//                if (region == 1) {
//                    visaOfficialDO.setPredictCommissionCNY(servicePackagePriceV2DTO.getAmount());
//                    visaOfficialDO.setPredictCommission(visaOfficialDO.getPredictCommission() / visaOfficialDO.getExchangeRate());
//                }
//                visaOfficialDO.setCommissionAmount(0.00);
//                visaOfficialDO.setPredictCommissionAmount(0.00);
//            } else {
//                visaOfficialDO.setPredictCommission(0.00);
//                visaOfficialDO.setCommissionAmount(0.00);
//                visaOfficialDO.setPredictCommission(0.00);
//                visaOfficialDO.setCalculation(null);
//                visaOfficialDO.setPredictCommissionCNY(0.00);
//            }
//            return visaOfficialDO;
//        }
//        CommissionAmountDTO commissionAmountDTO = new CommissionAmountDTO();
//        Calendar calendar = Calendar.getInstance();
//        List<Integer> monthlist = new ArrayList<Integer>() {
//            {
//                this.add(1);
//                this.add(2);
//                this.add(3);
//                this.add(7);
//                this.add(8);
//                this.add(9);
//            }
//        };
//        // 判断是否为分期付款订单
//        boolean installment = false;
//        double refund = 0.00;
//        VisaDO firstVisaByServiceOrderId;
//        VisaDO secondVisaByServiceOrderId;
//        firstVisaByServiceOrderId = visaDAO.getFirstVisaByServiceOrderId(serviceOrderById.getId());
//        secondVisaByServiceOrderId = visaDAO.getSecondVisaByServiceOrderId(serviceOrderById.getId());
//        if (suborder) {
//            int id = serviceOrderDao.getServiceOrderById(serviceOrderById.getApplicantParentId()).getId();
//            firstVisaByServiceOrderId = visaDAO.getFirstVisaByServiceOrderId(id);
//            secondVisaByServiceOrderId = visaDAO.getSecondVisaByServiceOrderId(id);
//        }
//        if (serviceOrderById.getBindingOrder() != null &&serviceOrderById.getBindingOrder() > 0) {
//            if (firstVisaByServiceOrderId == null) {
//                firstVisaByServiceOrderId = new VisaDO();
//            }
//            ServicePackagePriceDO byId = servicePackagePriceDAO.getByServiceId(serviceOrderById.getServiceId());
//            firstVisaByServiceOrderId.setAmount(byId.getCostPrince());
//            firstVisaByServiceOrderId.setId(0);
//        }
////        commissionAmountDTO.setRefund(visaDAO.getVisaById(firstVisaByServiceOrderId.getId()).getRefund()); // 设置退款金额
//        // 退款查询
//        RefundDO firstRefundByVisaId = refundDAO.getRefundByVisaId(firstVisaByServiceOrderId.getId());
//        if (ObjectUtil.isNotNull(firstRefundByVisaId)) {
//            refund = firstRefundByVisaId.getAmount();
//        }
//        if (ObjectUtil.isNotNull(secondVisaByServiceOrderId)) {
//            installment = true;
//        }
//        double amount = 0.00;
//        double rate = 0.00;
//        boolean longTermVisa = false;
//        OfficialDO officialById = officialDAO.getOfficialById(serviceOrderById.getOfficialId());
//        OfficialGradeDO officialGradeById = officialGradeDao.getOfficialGradeById(officialById.getGradeId());
//        rate = officialGradeById.getRate();
//        if (region == 0 && !"资深".equals(officialGradeById.getGrade())) {
//            // 创建一个Calendar对象并设置时间为date对象的时间
//            Calendar sss = Calendar.getInstance();
//            sss.setTime(serviceOrderById.getReadcommittedDate());
//
//            // 获取月份（注意：Calendar的月份是从0开始的，所以1代表二月，0代表一月）
//            int month = sss.get(Calendar.MONTH) + 1; // 加1是因为我们需要从1开始的月份
//
//            if (monthlist.contains(month)) {
//                rate = rate + 3;
//            }
//        }
//        // EOI数量判断
//        int EOICount = 0;
//        List<ServiceOrderDTO> deriveOrder = new ArrayList<>();
//        if (suborder) {
//            deriveOrder = serviceOrderDao.getDeriveOrder(serviceOrderById.getApplicantParentId());
//            for (ServiceOrderDTO a : deriveOrder) {
//                ServicePackageDO byId = servicePackageDAO.getById(a.getServicePackageId());
//                if ("EOI".equals(byId.getType())) {
//                    EOICount++;
//                }
//            }
//        }
//        if (EOICount == 0 || !"EOI".equals(servicePackageDAO.getById(serviceOrderById.getServicePackageId()).getType())) {
//            EOICount = 1;
//        }
////        EOICount = EOICount == 0 ? EOICount++ : EOICount;
//        if ("VA".equals(packType)) {
//            amount += firstVisaByServiceOrderId.getAmount();
//            if (installment) {
//                amount += secondVisaByServiceOrderId.getAmount();
//                RefundDO secondRefundByVisaId = refundDAO.getRefundByVisaId(secondVisaByServiceOrderId.getId());
//                if (ObjectUtil.isNotNull(secondRefundByVisaId)) {
//                    refund += secondRefundByVisaId.getAmount();
//                }
//                commissionAmountDTO.setRefund(refund);
//            }
//        }
//        if ("EOI".equals(packType)) {
//            amount = firstVisaByServiceOrderId.getAmount();
//            if (installment) {
//                ServicePackageDO byId = servicePackageDAO.getById(serviceOrderById.getServicePackageId());
//                if ("VA".equalsIgnoreCase(byId.getType()) || "EOI".equalsIgnoreCase(byId.getType())) {
//                    amount += secondVisaByServiceOrderId.getAmount();
//                    RefundDO secondRefundByVisaId = refundDAO.getRefundByVisaId(secondVisaByServiceOrderId.getId());
//                    if (ObjectUtil.isNotNull(secondRefundByVisaId)) {
//                        refund += secondRefundByVisaId.getAmount();
//                    }
//                    commissionAmountDTO.setRefund(refund);
//                }
//                if ("ROI".equals(byId.getType())) {
//                    return null;
//                }
//            }
//            amount = amount * 0.5;
//        }
//        if ("ROI".equals(packType)) {
//            amount += firstVisaByServiceOrderId.getAmount();
//            if (installment) {
//                commissionAmountDTO.setRefund(visaDAO.getVisaById(secondVisaByServiceOrderId.getId()).getRefund());
//                ServicePackageDO byId = servicePackageDAO.getById(serviceOrderById.getServicePackageId());
//                if ("VA".equals(byId.getType())) {
//                    amount += secondVisaByServiceOrderId.getAmount();
//                    RefundDO secondRefundByVisaId = refundDAO.getRefundByVisaId(secondVisaByServiceOrderId.getId());
//                    if (ObjectUtil.isNotNull(secondRefundByVisaId)) {
//                        refund += secondRefundByVisaId.getAmount();
//                    }
//                    commissionAmountDTO.setRefund(refund);
//                }
//            } else {
//                amount = amount * 0.5;
//            }
//        }
//        if ("NSV".equals(packType)) {
//            amount += firstVisaByServiceOrderId.getAmount() * 0.5;
//            if (installment) {
//                ServicePackageDO byId = servicePackageDAO.getById(serviceOrderById.getServicePackageId());
//                if ("TM".equals(byId.getType())) {
//                    commissionAmountDTO.setRefund(visaDAO.getVisaById(firstVisaByServiceOrderId.getId()).getRefund());
//                    amount = firstVisaByServiceOrderId.getAmount();
//                }
//                if ("VA".equals(byId.getType())) {
//                    commissionAmountDTO.setRefund(visaDAO.getVisaById(secondVisaByServiceOrderId.getId()).getRefund());
//                    amount = secondVisaByServiceOrderId.getAmount();
//                    RefundDO secondRefundByVisaId = refundDAO.getRefundByVisaId(secondVisaByServiceOrderId.getId());
//                    if (ObjectUtil.isNotNull(secondRefundByVisaId)) {
//                        refund += secondRefundByVisaId.getAmount();
//                    }
//                    commissionAmountDTO.setRefund(refund);
//                }
//            }
//        }
//        if ("VISA".equals(packType)) {
//            amount += firstVisaByServiceOrderId.getAmount();
//            commissionAmountDTO.setRefund(refund); // 设置退款金额
//            if (installment) {
//                amount += secondVisaByServiceOrderId.getAmount();
//                RefundDO refundByVisaId = refundDAO.getRefundByVisaId(secondVisaByServiceOrderId.getId());
//                if (ObjectUtil.isNotNull(refundByVisaId)) {
//                    refund += refundByVisaId.getAmount();
//                }
//            }
//            if (suborder) {
//                // 签证600和870计算
//                ServiceDO serviceById = serviceDao.getServiceById(serviceOrderById.getServiceId());
//                if ("600".equals(serviceById.getCode()) || "870".equals(serviceById.getCode())) {
//                    EOICount = 0;
//                    deriveOrder = serviceOrderDao.getZiOrder(serviceOrderById.getApplicantParentId());
//                    for (ServiceOrderDTO a : deriveOrder) {
//                        ServiceDO serviceByIdTmp = serviceDao.getServiceById(a.getServiceId());
//                        if ("600".equals(serviceByIdTmp.getCode())) {
//                            EOICount++;
//                            refund = refund / EOICount;
//                        }
//                    }
//                }
//            }
//            List<String> arrayList = serviceDao.listLongTimeVisa();
//            String code = serviceDao.getServiceById(serviceOrderById.getServiceId()).getCode();
//            String serviceType = code.replaceAll("[^\\p{L}\\p{N}\\p{Script=Han}]+", "");
//            if (arrayList.contains(serviceType)) {
//                longTermVisa = true;
//                if (installment) {
//                    amount = firstVisaByServiceOrderId.getAmount() + secondVisaByServiceOrderId.getAmount();
//                    RefundDO refundByVisaId = refundDAO.getRefundByVisaId(secondVisaByServiceOrderId.getId());
//                    if (ObjectUtil.isNotNull(refundByVisaId)) {
//                        refund = firstRefundByVisaId.getAmount() + refundByVisaId.getAmount();
//                    }
//                }
//                refund = refund * 0.5;
//                amount = amount * 0.5;
//            }
//        }
//        if ("ZX".equals(packType)) {
//            amount = firstVisaByServiceOrderId.getAmount();
//            commissionAmountDTO.setRefund(refund);
//            if (installment) {
//                amount = (firstVisaByServiceOrderId.getAmount() + secondVisaByServiceOrderId.getAmount());
//                RefundDO refundByVisaId = refundDAO.getRefundByVisaId(secondVisaByServiceOrderId.getId());
//                if (ObjectUtil.isNotNull(refundByVisaId)) {
//                    refund = (refund + refundByVisaId.getAmount());
//                }
//                commissionAmountDTO.setRefund(refund);
//            }
//        }
//        ServiceOrderDO serviceParentOrderById = serviceOrderDao.getServiceOrderById(serviceOrderById.getApplicantParentId());
//        if ("CNY".equals(serviceOrderById.getCurrency())) {
////        if ("CNY".equals(serviceOrderById.getCurrency()) && (serviceOrderById.getBindingOrder() != null && serviceOrderById.getBindingOrder() == 0)) {
//            amount = amount / serviceOrderById.getExchangeRate();
//        }
//        commissionAmountDTO.setRefund(refund); // 设置退款金额
//        visaOfficiaCalculate(serviceOrderById, region, commissionAmountDTO, amount, rate, EOICount, officialGradeById, visaOfficialDO, deriveOrder, serviceParentOrderById, installment, longTermVisa, officialById);
//        // EOI订单有删除情况的结算
//        if (EOICount > 2) {
//            List<VisaOfficialDO> visaOfficialDOS = new ArrayList<>();
//            deriveOrder.forEach(e->{
//                VisaOfficialDO byServiceOrderId = visaOfficialDao.getByServiceOrderIdOne(e.getId());
//                if (ObjectUtil.isNotNull(byServiceOrderId)) {
//                    visaOfficialDOS.add(byServiceOrderId);
//                }
//            });
//            if (visaOfficialDOS.size() == EOICount - 1 && EOICount < serviceParentOrderById.getEOINumber()) {
//                ServicePackagePriceDO servicePackagePriceDO = servicePackagePriceDAO.getByServiceId(serviceOrderById.getServiceId());
////                double commission = 0.00;
////                VisaOfficialDO visaOfficialDO1 = visaOfficialDOS.stream().max(Comparator.comparingDouble(VisaOfficialDO::getCommissionAmount)).get();
//                Double predictCommission = visaOfficialDO.getPredictCommission();
//                double sumNew = predictCommission - servicePackagePriceDO.getMaxPrice() / serviceParentOrderById.getEOINumber();
//                sumNew = (predictCommission + sumNew) * (EOICount - 1);
//                visaOfficialDO.setPredictCommission(sumNew);
//                visaOfficialDO.setPredictCommissionCNY(sumNew * visaOfficialDO.getExchangeRate());
//            }
//        }
//        return visaOfficialDO;
//    }

    private VisaOfficialDO visaOfficiaCalculate(ServiceOrderDO serviceOrderById, int region, CommissionAmountDTO commissionAmountDTO, double amount, double rate, int EOICount,
                                                OfficialGradeDO officialGradeById, VisaOfficialDO visaOfficialDO, List<ServiceOrderDTO> deriveOrder, ServiceOrderDO serviceParentOrderById, boolean installment, boolean longTermVisa, OfficialDO officialById,
                                                double calculateProportion, boolean isSIV, boolean isNSV, double extraAmount, ServiceOrderDO serviceOrderByParentId) throws ServiceException {
        ServicePackagePriceDO servicePackagePriceDO = servicePackagePriceDAO.getByServiceId(serviceOrderById.getServiceId());
        ServiceDO serviceDO = serviceDao.getServiceById(serviceOrderById.getServiceId());
        ServicePackagePriceV2DTO servicePackagePriceV2DTO = new ServicePackagePriceV2DTO();
        if (region == 1) {
            double quarterExchangeRate = exchangeRateService.getQuarterExchangeRate();
            visaOfficialDO.setExchangeRate(quarterExchangeRate);
        }
        if (servicePackagePriceDO == null) {
            commissionAmountDTO.setThirdPrince(0.00);
            commissionAmountDTO.setRuler(0);
        } else if (region == 1) { // 新版结算
            servicePackagePriceV2DTO = closeJugdNew(serviceOrderById.getOfficialId(), servicePackagePriceDO);
            servicePackagePriceV2DTO.setRate(servicePackagePriceV2DTO.getRate());
            log.info("当前服务计算规则-----------------------" + servicePackagePriceV2DTO);
            commissionAmountDTO.setThirdPrince(servicePackagePriceDO.getThirdPrince());
            Double priceV2DTOAmount = servicePackagePriceV2DTO.getAmount();
            if (priceV2DTOAmount != null) {servicePackagePriceDO.setAmount(servicePackagePriceV2DTO.getAmount());}
            commissionAmountDTO.setRuler(3);
        } else {
            ServicePackagePriceV2DTO servicePackagePriceV2DTO1 = closeJugdNew(serviceOrderById.getOfficialId(), servicePackagePriceDO);
            if (ObjectUtil.isNotNull(servicePackagePriceV2DTO1) && servicePackagePriceV2DTO1.getRuler() == 0) {
                rate = rate + servicePackagePriceV2DTO1.getRate();
            }
//            servicePackagePriceV2DTO = closeJugd(serviceOrderById.getOfficialId(), servicePackagePriceDO);
            if (servicePackagePriceV2DTO1.getRuler() == 1) {
                servicePackagePriceDO.setAmount(servicePackagePriceV2DTO1.getAmount());
            }
            commissionAmountDTO.setRuler(servicePackagePriceV2DTO1.getRuler());
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (commissionAmountDTO.getRuler() == 0) {
            double predictCommissionAmount = 0.00;
            double bingdingOrderAmount = 0.00;
            Integer getBindingOrderId = -1;
            getBindingOrderId = serviceOrderById.getId();
            if (ObjectUtil.isNotNull(serviceOrderByParentId) && serviceOrderByParentId.getId() > 0) {
                getBindingOrderId = serviceOrderByParentId.getId();
            }
            double countB = 1;
            if (isSIV || isNSV) {
                getBindingOrderId = serviceOrderByParentId.getId();
                amount = amount * 0.5;
                countB = 0.5;
            }
            if (longTermVisa) {
                amount = amount * 0.5;
            }
            bingdingOrderAmount = getBingdingOrderAmount(serviceOrderById, installment, longTermVisa, getBindingOrderId, bingdingOrderAmount, isSIV, isNSV, calculateProportion, 1000034 == officialById.getRegionId()) * countB;
            if (isSIV && "EOI".equalsIgnoreCase(servicePackageDAO.getById(serviceOrderById.getServicePackageId()).getType())) {
                List<VisaOfficialDO> visaOfficialDOS = new ArrayList<>();
                for (ServiceOrderDTO a : deriveOrder) {
                    ServicePackageDO servicePackageDO = servicePackageDAO.getById(a.getServicePackageId());
                    if ("VA".equalsIgnoreCase(servicePackageDO.getType())) {
                        continue;
                    }
                    VisaOfficialDO byServiceOrderId = visaOfficialDao.getByServiceOrderId(a.getId());
                    if (ObjectUtil.isNotNull(byServiceOrderId)) {
                        visaOfficialDOS.add(byServiceOrderId);
                    }
                }
//                EOICount = 6;
                ServicePackagePriceDO byServiceId = servicePackagePriceDAO.getByServiceId(25);
                if (visaOfficialDOS.isEmpty()) {
//                    predictCommissionAmount = ((amount - commissionAmountDTO.getRefund()) / 1.1 - byServiceId.getMaxPrice() - bingdingOrderAmount) + byServiceId.getMaxPrice() / EOICount;
                    predictCommissionAmount = ((amount - bingdingOrderAmount) / 1.1 - byServiceId.getMaxPrice()) + byServiceId.getMaxPrice() / EOICount;
                } else {
                    predictCommissionAmount = byServiceId.getMaxPrice() / EOICount;
                }
            } else {
//                predictCommissionAmount = (amount - commissionAmountDTO.getRefund()) / 1.1 - bingdingOrderAmount - servicePackagePriceDO.getThirdPrince();
                predictCommissionAmount = (amount - bingdingOrderAmount) / 1.1 - servicePackagePriceDO.getThirdPrince();
                if (serviceOrderById.getBindingOrder() != null && serviceOrderById.getBindingOrder() > 0) {
//                    predictCommissionAmount = (amount - commissionAmountDTO.getRefund()) / 1.1 - servicePackagePriceDO.getThirdPrince();
                    predictCommissionAmount = amount / 1.1 - servicePackagePriceDO.getThirdPrince();
                }
            }
            commissionAmountDTO.setPredictCommissionAmount(predictCommissionAmount);
            if (commissionAmountDTO.getPredictCommissionAmount() <= 0) {
                commissionAmountDTO.setPredictCommissionAmount(0.00);
            }
            commissionAmountDTO.setCommissionAmount(commissionAmountDTO.getPredictCommissionAmount());
            if (visaOfficialDO.getCommissionAmount() != null && visaOfficialDO.getCommissionAmount() > 0 && visaOfficialDO.getCommissionAmount() != visaOfficialDO.getPredictCommissionAmount()) {
                commissionAmountDTO.setPredictCommissionAmount(visaOfficialDO.getPredictCommissionAmount());
                commissionAmountDTO.setCommissionAmount(visaOfficialDO.getCommissionAmount());
            }
            commissionAmountDTO.setCommission((commissionAmountDTO.getCommissionAmount() * (rate / 100)) / EOICount);
            if (isSIV) {
//                Integer VACount = 0;
//                Integer nowEOICount = 0;
//                String eoiStrage = "";
//                List<VisaOfficialDO> countvisaOfficialByServiceOrderPatrentId = visaOfficialDao.getCountvisaOfficialByServiceOrderPatrentId(serviceOrderById.getApplicantParentId());
//                Map<Integer, Integer> collect = deriveOrder.stream().collect(Collectors.toMap(ServiceOrderDTO::getId, ServiceOrderDTO::getServicePackageId));
//                if (CollectionUtils.isNotEmpty(countvisaOfficialByServiceOrderPatrentId)) {
//                    for (VisaOfficialDO officialDO : countvisaOfficialByServiceOrderPatrentId) {
//                        Integer i = collect.get(officialDO.getServiceOrderId());
//                        ServicePackageDO byId = servicePackageDAO.getById(i);
//                        if ("VA".equalsIgnoreCase(byId.getType())) {
//                            VACount++;
//                        }
//                        if ("EOI".equalsIgnoreCase(byId.getType())) {
//                            eoiStrage = officialDO.getStage();
//                            nowEOICount++;
//                        }
//                    }
//                    Integer i = collect.get(visaOfficialDO.getServiceOrderId());
//                    ServicePackageDO byId = servicePackageDAO.getById(i);
//                    if (VACount == 0) {
//                        if ("VA".equalsIgnoreCase(byId.getType())) {
//                            visaOfficialDO.setStage("2");
//                        }
//                        if ("EOI".equalsIgnoreCase(byId.getType())) {
//                            visaOfficialDO.setStage("2-" + (nowEOICount + 1));
//                        }
//                    }
//                    if (nowEOICount == 0) {
//                        if ("EOI".equalsIgnoreCase(byId.getType())) {
//                            visaOfficialDO.setStage("2-" + (nowEOICount + 1));
//                        }
//                    }
//                    if (VACount > 0 && nowEOICount > 0) {
//                        if ("EOI".equalsIgnoreCase(byId.getType())) {
//                            visaOfficialDO.setStage(eoiStrage.substring(0,2) + (nowEOICount + 1));
//                        }
//                    }
//                } else {
//                    Integer i = collect.get(visaOfficialDO.getServiceOrderId());
//                    ServicePackageDO byId = servicePackageDAO.getById(i);
//                    if ("VA".equalsIgnoreCase(byId.getType())) {
//                        visaOfficialDO.setStage("1");
//                    }
//                    if ("EOI".equalsIgnoreCase(byId.getType())) {
//                        visaOfficialDO.setStage("1-1");
//                    }
//                }
                commissionAmountDTO.setCommission((commissionAmountDTO.getCommissionAmount() * (rate / 100)));
            }
            String calculation = new String();
            calculation = "0" + "|" + commissionAmountDTO.getThirdPrince() + "|" + dateFormat.format(servicePackagePriceDO == null ? System.currentTimeMillis() : servicePackagePriceDO.getGmtModify()) + "|" + officialGradeById.getGrade() + "," + rate + "%" + "," + dateFormat.format(officialGradeById.getGmtModify());
            commissionAmountDTO.setCalculation(calculation);
            if ("CNY".equals(serviceOrderById.getCurrency()) && (serviceOrderById.getBindingOrder() != null && serviceOrderById.getBindingOrder() > 0)) {
                visaOfficialDO.setPerAmount(visaOfficialDO.getPerAmount() * serviceOrderById.getExchangeRate() / EOICount);
            }
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
        if (commissionAmountDTO.getRuler() == 3) { // 新版结算
            boolean isBound = false;
            double predictCommissionAmount = amount;
            List<Integer> integers = new ArrayList<>();
            if (serviceOrderById.getApplicantParentId() > 0) {
                integers = serviceOrderDAO.listBybindingOrder(serviceOrderById.getApplicantParentId());
                isBound = !integers.isEmpty();
            }
            Integer getBindingOrderId = 0;
            double bingdingOrderAmount = 0.00;
            getBindingOrderId = serviceOrderById.getId();
            if (ObjectUtil.isNotNull(serviceOrderByParentId) && serviceOrderByParentId.getId() > 0) {
                getBindingOrderId = serviceOrderByParentId.getId();
            }
            bingdingOrderAmount = getBingdingOrderAmount(serviceOrderById, installment, longTermVisa, getBindingOrderId, bingdingOrderAmount, isSIV, isNSV, calculateProportion, 1000034 == officialById.getRegionId());
            predictCommissionAmount = predictCommissionAmount - bingdingOrderAmount;
            predictCommissionAmount = predictCommissionAmount / 1.1;
            // 500新版结算
            double additionalAmount = 0.00;
            if (ObjectUtil.isNotNull(serviceDO) && serviceDO.getCode().contains("500")) {
                if ("2A".equalsIgnoreCase(serviceOrderById.getPeopleType())) {
                    additionalAmount = 50;
                }
                if ("XA".equalsIgnoreCase(serviceOrderById.getPeopleType())) {
                    additionalAmount = 25;
                }
                if ("XB".equalsIgnoreCase(serviceOrderById.getPeopleType())) {
                    additionalAmount = 75;
                }
                if (extraAmount == 0) {
                    commissionAmountDTO.setCommission(servicePackagePriceV2DTO.getAmount() + additionalAmount);
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
                if (extraAmount > 0) {
                    commissionAmountDTO.setCommissionAmount(commissionAmountDTO.getPredictCommissionAmount());
                    commissionAmountDTO.setCommission(((servicePackagePriceV2DTO.getAmount() + additionalAmount) / visaOfficialDO.getExchangeRate() + extraAmount / 1.1 * 1.4 / 100) / EOICount);
                    commissionAmountDTO.setCommissionAmount((commissionAmountDTO.getPredictCommissionAmount() + extraAmount));
                    commissionAmountDTO.setPredictCommissionAmount(commissionAmountDTO.getPredictCommissionAmount() + extraAmount);
                    String calculation = new String();
                    calculation = "0" + "|" + commissionAmountDTO.getThirdPrince() + "|" + dateFormat.format(servicePackagePriceDO == null ? System.currentTimeMillis() : servicePackagePriceDO.getGmtModify()) + "|" + officialGradeById.getGrade() + "," + rate + "%" + "," + dateFormat.format(officialGradeById.getGmtModify());
                    commissionAmountDTO.setCalculation(calculation);
                    if ("CNY".equals(serviceOrderById.getCurrency()) && (serviceOrderById.getBindingOrder() != null && serviceOrderById.getBindingOrder() > 0)) {
                        visaOfficialDO.setPerAmount(visaOfficialDO.getPerAmount() * serviceOrderById.getExchangeRate() / EOICount);
                    }
                    visaOfficialDO.setPredictCommission(commissionAmountDTO.getCommission());
                    visaOfficialDO.setPredictCommissionCNY(visaOfficialDO.getPredictCommission() * visaOfficialDO.getExchangeRate());
                }
            } else if (isSIV) { // 新版独立技术移民结算
                List<VisaOfficialDO> visaOfficialDOS = new ArrayList<>();
                ServicePackageDO servicePackageDO = servicePackageDAO.getById(serviceOrderById.getServicePackageId());

                List<VisaOfficialDO> countvisaOfficialByServiceOrderPatrentId = visaOfficialDao.getCountvisaOfficialByServiceOrderPatrentId(serviceOrderById.getApplicantParentId());
                List<VisaOfficialDO> collect = countvisaOfficialByServiceOrderPatrentId.stream().sorted(Comparator.comparing(VisaOfficialDO::getGmtCreate)).collect(Collectors.toList());

//                if (CollectionUtils.isEmpty(countvisaOfficialByServiceOrderPatrentId) || visaOfficialDO.getGmtCreate() == collect.get(0).getGmtCreate()) {
//                    predictCommissionAmount = predictCommissionAmount * 0.6;
//                    extraAmount = extraAmount * 0;
//                }
                // 区分阶段结算
                if (ObjectUtil.isNotNull(servicePackageDO) && ("EOI".equalsIgnoreCase(servicePackageDO.getType()) || "ROI".equalsIgnoreCase(servicePackageDO.getType()))) {
                    predictCommissionAmount = predictCommissionAmount * 0.6;
                    extraAmount = extraAmount * 0.6;
                    for (ServiceOrderDTO a : deriveOrder) {
                        ServicePackageDO servicePackageDO1 = servicePackageDAO.getById(a.getServicePackageId());
                        if ("VA".equalsIgnoreCase(servicePackageDO1.getType())) {
                            countvisaOfficialByServiceOrderPatrentId = countvisaOfficialByServiceOrderPatrentId.stream().filter(e -> e.getServiceOrderId() != a.getId()).collect(Collectors.toList());
                            continue;
                        }
                        VisaOfficialDO byServiceOrderId = visaOfficialDao.getByServiceOrderIdOne(a.getId());
                        if (ObjectUtil.isNotNull(byServiceOrderId)) {
                            visaOfficialDOS.add(byServiceOrderId);
                        }
                    }
                    visaOfficialDO.setStage("1-" + (visaOfficialDOS.size() + 1));
                } else {
                    visaOfficialDO.setStage("2");
                    extraAmount = extraAmount * 0.4;
                    predictCommissionAmount = predictCommissionAmount * 0.4;
                }
                // 修改专用结算stage
                if (visaOfficialDO.getIsRefund()) {
                    countvisaOfficialByServiceOrderPatrentId = countvisaOfficialByServiceOrderPatrentId.stream().sorted(Comparator.comparing(VisaOfficialDO::getGmtCreate)).collect(Collectors.toList());
                    if (countvisaOfficialByServiceOrderPatrentId.get(0).getServiceOrderId() == visaOfficialDO.getServiceOrderId()) {
                        visaOfficialDOS = new ArrayList<>();
                    }
                    for (int i = 0; i < countvisaOfficialByServiceOrderPatrentId.size(); i++) {
                        if (visaOfficialDO.getServiceOrderId() == countvisaOfficialByServiceOrderPatrentId.get(i).getServiceOrderId()) {
                            String type = servicePackageDAO.getById(serviceOrderById.getServicePackageId()).getType();
                            if ("VA".equalsIgnoreCase(type)) {
                                visaOfficialDO.setStage("2");
                            } else {
                                visaOfficialDO.setStage("1-" + (i + 1));
                            }
                            break;
                        }
                    }
                }
                extraAmount = extraAmount / 1.1; // extraAmount计算比例
                commissionAmountDTO.setPredictCommissionAmount(predictCommissionAmount);
                if (commissionAmountDTO.getPredictCommissionAmount() <= 0) {
                    commissionAmountDTO.setPredictCommissionAmount(0.00);
                }
                if (!visaOfficialDOS.isEmpty()) {
                    commissionAmountDTO.setCommissionAmount(0);
                    commissionAmountDTO.setCommission(0);
                    String calculation = new String();
                    calculation = "0" + "|" + commissionAmountDTO.getThirdPrince() + "|" + dateFormat.format(servicePackagePriceDO == null ? System.currentTimeMillis() : servicePackagePriceDO.getGmtModify()) + "|" + officialGradeById.getGrade() + "," + rate + "%" + "," + dateFormat.format(officialGradeById.getGmtModify());
                    commissionAmountDTO.setCalculation(calculation);
                    if ("CNY".equals(serviceOrderById.getCurrency()) && (serviceOrderById.getBindingOrder() != null && serviceOrderById.getBindingOrder() > 0)) {
                        visaOfficialDO.setPerAmount(visaOfficialDO.getPerAmount() * serviceOrderById.getExchangeRate() / EOICount);
                    }
                } else {
                    commissionAmountDTO.setCommissionAmount(commissionAmountDTO.getPredictCommissionAmount());
                    commissionAmountDTO.setCommission((commissionAmountDTO.getCommissionAmount() * (servicePackagePriceV2DTO.getRate() / 100)  + extraAmount * 1.4 / 100));
                    commissionAmountDTO.setCommissionAmount(commissionAmountDTO.getPredictCommissionAmount() + extraAmount);
                    commissionAmountDTO.setPredictCommissionAmount(commissionAmountDTO.getPredictCommissionAmount() + extraAmount);
                    String calculation = new String();
                    calculation = "0" + "|" + commissionAmountDTO.getThirdPrince() + "|" + dateFormat.format(servicePackagePriceDO == null ? System.currentTimeMillis() : servicePackagePriceDO.getGmtModify()) + "|" + officialGradeById.getGrade() + "," + rate + "%" + "," + dateFormat.format(officialGradeById.getGmtModify());
                    commissionAmountDTO.setCalculation(calculation);
                    if ("CNY".equals(serviceOrderById.getCurrency()) && (serviceOrderById.getBindingOrder() != null && serviceOrderById.getBindingOrder() > 0)) {
                        visaOfficialDO.setPerAmount(visaOfficialDO.getPerAmount() * serviceOrderById.getExchangeRate() / EOICount);
                    }
                }
                visaOfficialDO.setPredictCommission(commissionAmountDTO.getCommission());
                visaOfficialDO.setPredictCommissionCNY(visaOfficialDO.getPredictCommission() * visaOfficialDO.getExchangeRate());
//                extraAmount = extraAmount / 2; // 独立技术移民文案佣金订单存入时额外金额除以2
            } else {
                extraAmount = extraAmount / 1.1;
                if (isNSV) {
                    LocalDateTime localDateTime = LocalDateTime.of(2025, 9, 1, 0, 0, 0);
                    Date from = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
                    if (serviceOrderById.getServiceId() == 1000105 && serviceOrderById.getGmtCreate().getTime() > from.getTime()) {
                        ServicePackageDO servicePackageDO = servicePackageDAO.getById(serviceOrderById.getServicePackageId());
                        if ("VA".equalsIgnoreCase(servicePackageDO.getType())) {
                            predictCommissionAmount = predictCommissionAmount * 0.5;
                        } else {
                            predictCommissionAmount = predictCommissionAmount * 0.25;
                        }
                    } else {
                        predictCommissionAmount = predictCommissionAmount * 0.5;
                    }
                }
                if (longTermVisa && !serviceDO.getCode().contains("820") && !serviceDO.getCode().contains("309")) {
                    predictCommissionAmount = predictCommissionAmount * 0.4;
                }
                if (servicePackagePriceV2DTO.getRuler() == 0) {
                    commissionAmountDTO.setPredictCommissionAmount(predictCommissionAmount);
                    if (commissionAmountDTO.getPredictCommissionAmount() <= 0) {
                        commissionAmountDTO.setPredictCommissionAmount(0.00);
                    }
                    commissionAmountDTO.setCommissionAmount(commissionAmountDTO.getPredictCommissionAmount());
                    commissionAmountDTO.setCommission(((commissionAmountDTO.getCommissionAmount() * (servicePackagePriceV2DTO.getRate() / 100)) + extraAmount * 1.4 / 100) / EOICount);
                    commissionAmountDTO.setCommissionAmount((commissionAmountDTO.getPredictCommissionAmount() + extraAmount));
                    commissionAmountDTO.setPredictCommissionAmount(commissionAmountDTO.getPredictCommissionAmount() + extraAmount);
                    String calculation = new String();
                    calculation = "0" + "|" + commissionAmountDTO.getThirdPrince() + "|" + dateFormat.format(servicePackagePriceDO == null ? System.currentTimeMillis() : servicePackagePriceDO.getGmtModify()) + "|" + officialGradeById.getGrade() + "," + rate + "%" + "," + dateFormat.format(officialGradeById.getGmtModify());
                    commissionAmountDTO.setCalculation(calculation);
                    if ("CNY".equals(serviceOrderById.getCurrency()) && (serviceOrderById.getBindingOrder() != null && serviceOrderById.getBindingOrder() > 0)) {
                        visaOfficialDO.setPerAmount(visaOfficialDO.getPerAmount() * serviceOrderById.getExchangeRate() / EOICount);
                    }
                    visaOfficialDO.setPredictCommission(commissionAmountDTO.getCommission());
                    visaOfficialDO.setPredictCommissionCNY(visaOfficialDO.getPredictCommission() * visaOfficialDO.getExchangeRate());
                }
                if (servicePackagePriceV2DTO.getRuler() == 1) {
                    extraAmount = extraAmount / 1.1;
                    predictCommissionAmount = servicePackagePriceV2DTO.getAmount();
                    if (serviceDO.getCode().contains("485") && "1".equalsIgnoreCase(serviceOrderById.getIsInsuranceCompany())) {
                        predictCommissionAmount = predictCommissionAmount + 43;
                    }
                    if (extraAmount == 0) {
                        commissionAmountDTO.setPredictCommissionAmount(0);
                    }
                    if (commissionAmountDTO.getPredictCommissionAmount() < 0) {
                        commissionAmountDTO.setPredictCommissionAmount(0);
                    }
                    commissionAmountDTO.setCommissionAmount(commissionAmountDTO.getPredictCommissionAmount());
                    visaOfficialDO.setPredictCommission(predictCommissionAmount / visaOfficialDO.getExchangeRate() + extraAmount * 1.4 / 100 + additionalAmount / visaOfficialDO.getExchangeRate());
//                    visaOfficialDO.setPredictCommissionCNY(predictCommissionAmount + extraAmount * 1.4 / 100 * visaOfficialDO.getExchangeRate());
                    visaOfficialDO.setPredictCommissionCNY(visaOfficialDO.getPredictCommission() * visaOfficialDO.getExchangeRate());
                    if (extraAmount > 0) {
                        commissionAmountDTO.setCommissionAmount(commissionAmountDTO.getPredictCommissionAmount() + extraAmount);
                        commissionAmountDTO.setPredictCommissionAmount(commissionAmountDTO.getPredictCommissionAmount() + extraAmount);
                    }
                    String calculation = "1" + "|" + commissionAmountDTO.getThirdPrince() + "," + servicePackagePriceDO.getAmount() + "|" + dateFormat.format(servicePackagePriceDO.getGmtModify());
                    commissionAmountDTO.setCalculation(calculation);
                }
            }
        }
        visaOfficialDO.setExtraAmount(extraAmount / EOICount);
        visaOfficialDO.setPredictCommissionAmount(commissionAmountDTO.getPredictCommissionAmount() / EOICount);
        visaOfficialDO.setCommissionAmount(commissionAmountDTO.getCommissionAmount() / EOICount);
        if (isSIV) {
            visaOfficialDO.setPredictCommissionAmount(commissionAmountDTO.getPredictCommissionAmount());
            visaOfficialDO.setCommissionAmount(commissionAmountDTO.getCommissionAmount());
        }
        visaOfficialDO.setCalculation(commissionAmountDTO.getCalculation());
        return visaOfficialDO;
    }

    private double getBingdingOrderAmount(ServiceOrderDO serviceOrderById, boolean installment, boolean longTermVisa, Integer getBindingOrderId, double bingdingOrderAmount, boolean isSIV, boolean isNSV, double calculateProportion, boolean isChengDu) {
        List<Integer> integers = serviceOrderDao.listBybindingOrder(getBindingOrderId);
        if ((serviceOrderById.getBindingOrder() != null && serviceOrderById.getBindingOrder() > 0) || !integers.isEmpty()) {
            if (!integers.isEmpty()) {
                for (Integer a : integers) {
                    bingdingOrderAmount += servicePackagePriceDAO.getByServiceId(a).getCostPrince();
                }
            }
        }
        return bingdingOrderAmount;
    }

    // 文案佣金订单是否直接计算固定金额
    private static boolean isaBoolean(ServicePackagePriceDO servicePackagePriceDO, boolean pay) {
        if (servicePackagePriceDO.getRuler() == 1) {
            pay = false;
        }
        return pay;
    }

    @Override
    public List<VisaOfficialDTO> listVisaOfficialOrder(Integer officialId, List<Integer> regionIdList, Integer id, String startHandlingDate, String endHandlingDate, String state, String startDate, String endDate,String firstSettlementMonth,String lastSettlementMonth,  String userName, String applicantName, Boolean isMerged, Integer pageNum, Integer pageSize, Sorter sorter, String serviceOrderType, String currency) throws ServiceException, InterruptedException {

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
        if ("ALL".equalsIgnoreCase(currency)) {
            currency = null;
        }
		long _t0_main = System.currentTimeMillis();
		List<VisaOfficialListDO> list = visaOfficialDao.list(officialId, regionIdList, id,
				theDateTo00_00_00(startHandlingDate), theDateTo23_59_59(endHandlingDate), state,
				theDateTo00_00_00(startDate), theDateTo23_59_59(endDate), theDateTo00_00_00(firstSettlementMonth), theDateTo23_59_59(lastSettlementMonth), userName, applicantName, isMerged, offset,
				pageSize, orderBy, serviceOrderType, null, currency);
		long _t1_main = System.currentTimeMillis();
        List<VisaOfficialDTO> visaOfficialDtoList = new ArrayList<>();
        if (list == null || list.size() == 0) {
            return null;
        }
        // 批量预加载所有关联数据
        VisaOfficialBatchContext ctx = preloadBatchData(list);
        long _t2_preload = System.currentTimeMillis();
        long _t3_loop = System.currentTimeMillis();
        int _n1_deriveOrder = 0, _n1_eoiService = 0, _n1_ziOrder = 0;
        for (VisaOfficialListDO visaListDo : list) {
            try {
                VisaOfficialDTO visaOfficialDto = putVisaOfficialDTO(visaListDo, ctx);

                List<Date> remindDateList = new ArrayList<>();
                List<RemindDO> remindDoList = ctx.remindMap.get(visaOfficialDto.getId());
                if (remindDoList != null) {
                    for (RemindDO remindDo : remindDoList) {
                        remindDateList.add(remindDo.getRemindDate());
                    }
                }
                visaOfficialDto.setRemindDateList(remindDateList);

                ServiceOrderDO serviceOrderDO = ctx.serviceOrderMap.get(visaOfficialDto.getServiceOrderId());
                ServiceOrderDTO serviceOrderDto = null;
                if (serviceOrderDO != null) {
                    serviceOrderDto = mapper.map(serviceOrderDO, ServiceOrderDTO.class);
                    ServiceDO serviceDo = ctx.serviceMap.get(serviceOrderDO.getServiceId());
                    if (serviceDo != null) {
                        serviceOrderDto.setService(mapper.map(serviceDo, ServiceDTO.class));
                    }
                    if (serviceOrderDto.getServiceAssessId() != null && !"0".equals(serviceOrderDto.getServiceAssessId())) {
                        try {
                            Integer saId = Integer.parseInt(serviceOrderDto.getServiceAssessId());
                            ServiceAssessDO sa = ctx.serviceAssessMap.get(saId);
                            if (sa != null) serviceOrderDto.setServiceAssessDO(sa);
                        } catch (NumberFormatException ignored) {}
                    }
                    if (serviceOrderDto.getServicePackageId() > 0) {
                        ServicePackageDO sp = ctx.servicePackageMap.get(serviceOrderDto.getServicePackageId());
                        if (sp != null && "EOI".equals(sp.getType())) {
                            if (serviceOrderDto.getServicePackageId() == 0) {
                                StringBuilder eoiList = new StringBuilder();
                                _n1_deriveOrder++;
                                List<ServiceOrderDTO> deriveOrder = serviceOrderDao.getDeriveOrder(serviceOrderDto.getId());
                                if (deriveOrder != null && deriveOrder.size() > 0) {
                                    for (ServiceOrderDTO e : deriveOrder) {
                                        _n1_eoiService++;
                                        ServicePackageDTO eoiService = servicePackageDAO.getEOIService(e.getServicePackageId());
                                        eoiList.append(eoiService.getServiceCode()).append(",");
                                    }
                                }
                                if (eoiList.length() > 0)
                                    serviceOrderDto.setEoiList(eoiList.substring(0, eoiList.length() - 1));
                            }
                            _n1_eoiService++;
                            ServicePackageDTO eoiSp = servicePackageDAO.getEOIService(serviceOrderDto.getServicePackageId());
                            if (ObjectUtil.isNotNull(eoiSp)) serviceOrderDto.setServicePackage(eoiSp);
                        } else {
                            if (sp != null)
                                serviceOrderDto.setServicePackage(mapper.map(sp, ServicePackageDTO.class));
                        }
                    }
                    if (serviceOrderDto.getSchoolId() > 0) {
                        SchoolDO school = ctx.schoolMap.get(serviceOrderDto.getSchoolId());
                        if (school != null) serviceOrderDto.setSchool(mapper.map(school, SchoolDTO.class));
                    }
                    if (serviceOrderDto.getCourseId() > 0) {
                        SchoolCourseDO sc = ctx.schoolCourseMap.get(serviceOrderDto.getCourseId());
                        if (sc != null) {
                            SchoolInstitutionListDTO silDto = new SchoolInstitutionListDTO();
                            SchoolInstitutionDO si = ctx.schoolInstitutionMap.get(sc.getProviderId());
                            if (si != null) {
                                silDto.setId(si.getId());
                                silDto.setName(si.getName());
                            }
                            serviceOrderDto.setSchoolInstitutionListDTO(silDto);
                        }
                    }
                    if ("1".equals(serviceOrderDto.getIsInsuranceCompany())) {
                        ServiceOrderInsuranceDO ins = ctx.insuranceMap.get(serviceOrderDto.getId());
                        if (ins != null) {
                            InsuranceCompanyDO ic = ctx.insuranceCompanyMap.get(ins.getInsuranceCompanyId());
                            if (ic != null) serviceOrderDto.setInsuranceCompanyDO(ic);
                        }
                    }
                    visaOfficialDto.setServiceOrder(serviceOrderDto);
                }

                ServicePackagePriceDO spp = ctx.servicePackagePriceMap.get(visaOfficialDto.getServiceId());
                if (spp != null) visaOfficialDto.setServicePackagePriceDO(spp);

                MaraDO mara = ctx.maraMap.get(visaOfficialDto.getMaraId());
                if (mara != null) visaOfficialDto.setMaraDTO(mapper.map(mara, MaraDTO.class));

                if (serviceOrderDto != null && serviceOrderDto.getEOINumber() != null && serviceOrderDto.getApplicantParentId() > 0) {
                    _n1_ziOrder++;
                    List<ServiceOrderDTO> ziOrder = serviceOrderDao.getZiOrder(serviceOrderDto.getApplicantParentId());
                    List<ServiceOrderDTO> collect = ziOrder.stream().filter(s -> s.getEOINumber() != null).collect(Collectors.toList());
                    visaOfficialDto.setSortEOI(serviceOrderDto.getEOINumber() + "/" + collect.size());
                }

                ServiceOrderAndManage som = ctx.serviceOrderManageMap.get(visaOfficialDto.getServiceOrderId());
                if (som != null && som.getServiceOrderManageId() != null) {
                    visaOfficialDto.setParentIdNew(som.getServiceOrderManageId());
                }

                visaOfficialDto.setRefundAmount(0.00);
                visaOfficialDto.setBingDingAmount(0.00);
                if (serviceOrderDto != null && serviceOrderDto.getApplicantParentId() > 0) {
                    ServiceOrderDO parentOrder = ctx.serviceOrderMap.get(serviceOrderDto.getApplicantParentId());
                    if (parentOrder != null) {
                        List<VisaDO> visas = ctx.visaByServiceOrderIdMap.get(parentOrder.getId());
                        if (visas != null) {
                            for (VisaDO v : visas) {
                                List<RefundDO> refunds = ctx.refundByVisaIdMap.get(v.getId());
                                if (refunds != null) {
                                    for (RefundDO r : refunds) {
                                        visaOfficialDto.setRefundAmount(visaOfficialDto.getRefundAmount() + r.getAmount());
                                    }
                                }
                            }
                        }
                        List<ServiceOrderDO> children = ctx.childrenByParentIdMap.get(parentOrder.getId());
                        if (children != null) {
                            for (ServiceOrderDO child : children) {
                                ServicePackagePriceDO childSpp = ctx.servicePackagePriceMap.get(child.getServiceId());
                                if (childSpp != null) {
                                    visaOfficialDto.setBingDingAmount(visaOfficialDto.getBingDingAmount() + childSpp.getCostPrince());
                                }
                            }
                        }
                    }
                } else if (serviceOrderDO != null) {
                    List<VisaDO> visas = ctx.visaByServiceOrderIdMap.get(serviceOrderDO.getId());
                    if (visas != null) {
                        for (VisaDO v : visas) {
                            List<RefundDO> refunds = ctx.refundByVisaIdMap.get(v.getId());
                            if (refunds != null) {
                                for (RefundDO r : refunds) {
                                    visaOfficialDto.setRefundAmount(r.getAmount());
                                }
                            }
                        }
                    }
                    List<ServiceOrderDO> children = ctx.childrenByParentIdMap.get(visaOfficialDto.getServiceOrderId());
                    if (children != null) {
                        for (ServiceOrderDO child : children) {
                            ServicePackagePriceDO childSpp = ctx.servicePackagePriceMap.get(child.getServicePackageId());
                            if (childSpp != null) {
                                visaOfficialDto.setBingDingAmount(visaOfficialDto.getBingDingAmount() + childSpp.getCostPrince());
                            }
                        }
                    }
                }

                visaOfficialDtoList.add(visaOfficialDto);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return visaOfficialDtoList.stream().sorted(Comparator.comparing(VisaOfficialDTO::getId).reversed()).collect(Collectors.toList());
    }
    
	public VisaOfficialDTO getByServiceOrderId(Integer serviceOrderId) throws ServiceException {
        VisaOfficialDO visaOfficialDo = visaOfficialDao.getByServiceOrderId(serviceOrderId);
        return ObjectUtil.isNotNull(visaOfficialDo) ? mapper.map(visaOfficialDo, VisaOfficialDTO.class) : null;
	}

    @Override
    public int count(Integer officialId, List<Integer> regionIdList, Integer id, String startHandlingDate, String endHandlingDate, String state, String startDate, String endDate, String userName, String applicantName, Boolean isMerged, String currency) throws ServiceException {
        if ("ALL".equalsIgnoreCase(currency)) {
            currency = null;
        }
    	return visaOfficialDao.count(officialId, regionIdList, id, theDateTo00_00_00(startHandlingDate), theDateTo23_59_59(endHandlingDate), state, theDateTo00_00_00(startDate), theDateTo23_59_59(endDate), userName, applicantName, isMerged, currency);
    }

    @Override
    public void update(Integer id, String submitIbDate, Double commissionAmount, String state, Integer serviceId) throws ServiceException {
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
        Integer region = 0;
        VisaOfficialDO one = visaOfficialDao.getOne(id);
        VisaOfficialDO byServiceOrderId = visaOfficialDao.getByServiceOrderId(one.getServiceOrderId());
        ServicePackagePriceDO byServiceId = servicePackagePriceDAO.getByServiceId(byServiceOrderId.getServiceId());

        if (StringUtil.isEmpty(submitIbDate)) {
            OfficialDO officialById = officialDao.getOfficialById(byServiceOrderId.getOfficialId());
            ServicePackagePriceV2DTO servicePackagePriceV2DTO = closeJugdNew(officialById.getId(), byServiceId);
            OfficialGradeDO officialGradeById = officialGradeDao.getOfficialGradeById(officialById.getGradeId());
            Double rate = servicePackagePriceV2DTO.getRate();
            // 判断当前文案地区为澳洲还是中国
            RegionDO regionById = regionDAO.getRegionById(officialById.getRegionId());
            String regionName = regionById.getName().replaceAll("[^\u4e00-\u9fa5]", "");
            if (StringUtil.isNotEmpty(regionName)) {
                region = 1;
            }
            if (region == 0 && !"资深".equals(officialGradeById.getGrade())) {
                // 创建一个Calendar对象并设置时间为date对象的时间
                Calendar sss = Calendar.getInstance();
                sss.setTime(byServiceOrderId.getGmtCreate());

                // 获取月份（注意：Calendar的月份是从0开始的，所以1代表二月，0代表一月）
                int month = sss.get(Calendar.MONTH) + 1; // 加1是因为我们需要从1开始的月份

                if (monthlist.contains(month)) {
                    rate = rate + 3;
                }
            }
            byServiceOrderId.setCommissionAmount(commissionAmount);
            byServiceOrderId.setPredictCommission(commissionAmount * rate / 100);
            byServiceOrderId.setPredictCommissionCNY(byServiceOrderId.getPredictCommission() * byServiceOrderId.getExchangeRate());
            visaOfficialDao.updateVisaOfficial(byServiceOrderId);
        } else {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                Date parse = sdf.parse(submitIbDate);
                visaOfficialDao.updateHandlingDate(id, parse);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
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
                EndOfLastMonth, null, null, null, null, null,
                null, null, null, null, null, null
                , null, null, null, null, null
                , null, null, null, null, 0, 9999, null, null, null, null, null, null, false);
        List<VisaOfficialDO> visaOfficialDOs = new ArrayList<>();
        int count = 0;
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
                } else {
                    count++;
                }
            } catch (Exception ex) {
                log.info("当前生成失败的订单id为---------------------" + e.getId());
                ex.printStackTrace();
            }
            log.info("总订单数：-----------------" + serviceOrderDOS.size() + "当前未生成订单状态：--------------" + e.getState() + count);
        }
        return visaOfficialDOs;
    }

    @Override
    public int deleteById(Integer id) {
        return visaOfficialDao.deleteByServiceOrderId(id);
    }

    @Override
    public List<VisaOfficialDTO> getAllvisaOfficialByServiceOrderId(Integer serviceOrderId) {

        return visaOfficialDao.getAllvisaOfficialByServiceOrderId(serviceOrderId);
    }

    @Override
    public VisaOfficialDO getByServiceOrderIdOne(int id) {
        return visaOfficialDao.getByServiceOrderIdOne(id);
    }

    @Override
    public void visaServiceupdateHandlingDate(int id, Date handlingDate) {
        visaOfficialDao.updateHandlingDate(id, handlingDate);
    }

    @Override
    public void visaServiceupdateVisaOfficial(VisaOfficialDO visaOfficialDO1) {
        visaOfficialDao.updateVisaOfficial(visaOfficialDO1);
    }

    @Override
    public List<VisaOfficialDTO> listVisaForDown(Integer officialId, List<Integer> regionIdList, Integer id, String startHandlingDate, String endHandlingDate, String state, String startDate, String endDate, String userName, String applicantName) throws InterruptedException {
        // 使用 listVisaOfficialOrder 相同的批量预载方式，避免 N+1 查询
        List<VisaOfficialListDO> list = visaOfficialDao.list(officialId, regionIdList, id,
                theDateTo00_00_00(startHandlingDate), theDateTo23_59_59(endHandlingDate), state,
                theDateTo00_00_00(startDate), theDateTo23_59_59(endDate), null, null, userName, applicantName,
                null, null, null, null, null, null, null);
        if (list == null || list.isEmpty()) {
            return null;
        }
        // 批量预加载所有关联数据
        VisaOfficialBatchContext ctx = preloadBatchData(list);
        List<VisaOfficialDTO> visaOfficialDTOList = new ArrayList<>();
        for (VisaOfficialListDO visaListDo : list) {
            try {
                VisaOfficialDTO visaOfficialDto = putVisaOfficialDTO(visaListDo, ctx);

                // 构建 ServiceOrder 并注入关联对象
                ServiceOrderDO serviceOrderDO = ctx.serviceOrderMap.get(visaOfficialDto.getServiceOrderId());
                ServiceOrderDTO serviceOrderDto = null;
                if (serviceOrderDO != null) {
                    serviceOrderDto = mapper.map(serviceOrderDO, ServiceOrderDTO.class);
                    ServiceDO serviceDo = ctx.serviceMap.get(serviceOrderDO.getServiceId());
                    if (serviceDo != null) {
                        serviceOrderDto.setService(mapper.map(serviceDo, ServiceDTO.class));
                    }
                    if (serviceOrderDto.getServicePackageId() > 0) {
                        ServicePackageDO sp = ctx.servicePackageMap.get(serviceOrderDto.getServicePackageId());
                        if (sp != null) {
                            serviceOrderDto.setServicePackage(mapper.map(sp, ServicePackageDTO.class));
                        }
                    }
                    visaOfficialDto.setServiceOrder(serviceOrderDto);
                }

                // 计算退款金额和绑定订单金额（与 listVisaOfficialOrder 一致）
                visaOfficialDto.setRefundAmount(0.00);
                visaOfficialDto.setBingDingAmount(0.00);
                if (serviceOrderDto != null && serviceOrderDto.getApplicantParentId() > 0) {
                    ServiceOrderDO parentOrder = ctx.serviceOrderMap.get(serviceOrderDto.getApplicantParentId());
                    if (parentOrder != null) {
                        List<VisaDO> visas = ctx.visaByServiceOrderIdMap.get(parentOrder.getId());
                        if (visas != null) {
                            for (VisaDO v : visas) {
                                List<RefundDO> refunds = ctx.refundByVisaIdMap.get(v.getId());
                                if (refunds != null) {
                                    for (RefundDO r : refunds) {
                                        visaOfficialDto.setRefundAmount(visaOfficialDto.getRefundAmount() + r.getAmount());
                                    }
                                }
                            }
                        }
                        List<ServiceOrderDO> children = ctx.childrenByParentIdMap.get(parentOrder.getId());
                        if (children != null) {
                            for (ServiceOrderDO child : children) {
                                ServicePackagePriceDO childSpp = ctx.servicePackagePriceMap.get(child.getServiceId());
                                if (childSpp != null) {
                                    visaOfficialDto.setBingDingAmount(visaOfficialDto.getBingDingAmount() + childSpp.getCostPrince());
                                }
                            }
                        }
                    }
                } else if (serviceOrderDO != null) {
                    List<VisaDO> visas = ctx.visaByServiceOrderIdMap.get(serviceOrderDO.getId());
                    if (visas != null) {
                        for (VisaDO v : visas) {
                            List<RefundDO> refunds = ctx.refundByVisaIdMap.get(v.getId());
                            if (refunds != null) {
                                for (RefundDO r : refunds) {
                                    visaOfficialDto.setRefundAmount(r.getAmount());
                                }
                            }
                        }
                    }
                    List<ServiceOrderDO> children = ctx.childrenByParentIdMap.get(visaOfficialDto.getServiceOrderId());
                    if (children != null) {
                        for (ServiceOrderDO child : children) {
                            ServicePackagePriceDO childSpp = ctx.servicePackagePriceMap.get(child.getServicePackageId());
                            if (childSpp != null) {
                                visaOfficialDto.setBingDingAmount(visaOfficialDto.getBingDingAmount() + childSpp.getCostPrince());
                            }
                        }
                    }
                }

                visaOfficialDTOList.add(visaOfficialDto);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return visaOfficialDTOList.stream().sorted(Comparator.comparing(VisaOfficialDTO::getId).reversed()).collect(Collectors.toList());
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
            if (e.getAreaId() != null && e.getAreaId().equals(regionById.getId())) {
                servicePackagePriceV2DTO = e;
            }
        }
        if (servicePackagePriceV2DTO.getAreaId() == null && servicePackagePriceV2DTO.getRuler() == null) {
            Map<String, ServicePackagePriceV2DTO> collect = servicePackagePriceV2DTOS.stream().collect(Collectors.toMap(ServicePackagePriceV2DTO::getCountry, Function.identity(), (oldValue, newValue) -> newValue));
            String s = regionById.getName().replaceAll("[^\u4e00-\u9fa5]", "");
            if (StringUtil.isNotEmpty(s)) {
                servicePackagePriceV2DTO = collect.get("China");
            } else {
                servicePackagePriceV2DTO = collect.get("Australia");
            }
        }
        return servicePackagePriceV2DTO;
    }

    // 文案地区判断
    public ServicePackagePriceV2DTO closeJugdNew(Integer officialId, ServicePackagePriceDO servicePackagePriceDO) {
        // 判断文案结算方式
        ServicePackagePriceV2DTO servicePackagePriceV2DTO = new ServicePackagePriceV2DTO();
        OfficialDO officialDO = officialDao.getOfficialById(officialId);
        String rulerV2 = servicePackagePriceDO.getRulerV2();
        List<ServicePackagePriceV2DTO> servicePackagePriceV2DTOS = JSONArray.parseArray(rulerV2, ServicePackagePriceV2DTO.class);
        for (ServicePackagePriceV2DTO packagePriceV2DTO : servicePackagePriceV2DTOS) {
            String officialGrades = packagePriceV2DTO.getOfficialGrades();
            if (StringUtil.isNotEmpty(officialGrades)) {
                String[] split = officialGrades.split(",");
                if (Arrays.asList(split).contains(String.valueOf(officialDO.getGradeId()))) {
                    servicePackagePriceV2DTO = packagePriceV2DTO;
                }
            }
        }
        return servicePackagePriceV2DTO;
    }

}
