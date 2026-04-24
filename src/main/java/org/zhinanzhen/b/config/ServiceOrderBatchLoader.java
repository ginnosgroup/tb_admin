package org.zhinanzhen.b.config;

import org.zhinanzhen.b.dao.*;
import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.dao.pojo.customer.CustomerInformationDO;
import org.zhinanzhen.b.service.pojo.WebLogDTO;
import org.zhinanzhen.tb.dao.AdviserDAO;
import org.zhinanzhen.tb.dao.RegionDAO;
import org.zhinanzhen.tb.dao.UserDAO;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
import org.zhinanzhen.tb.dao.pojo.RegionDO;
import org.zhinanzhen.tb.dao.pojo.UserDO;

import java.util.*;
import java.util.stream.Collectors;

public class ServiceOrderBatchLoader {

    private final ServiceOrderDAO serviceOrderDao;
    private final SchoolDAO schoolDao;
    private final SubagencyDAO subagencyDao;
    private final ServiceDAO serviceDao;
    private final ServicePackageDAO servicePackageDao;
    private final ReceiveTypeDAO receiveTypeDao;
    private final UserDAO userDao;
    private final ApplicantDAO applicantDao;
    private final MaraDAO maraDao;
    private final AdviserDAO adviserDao;
    private final OfficialDAO officialDao;
    private final OfficialTagDAO officialTagDao;
    private final VisaDAO visaDao;
    private final ServiceAssessDao serviceAssessDao;
    private final RegionDAO regionDAO;
    private final CommissionOrderTempDAO commissionOrderTempDao;
    private final SchoolCourseDAO schoolCourseDAO;
    private final SchoolInstitutionDAO schoolInstitutionDAO;
    private final SchoolInstitutionLocationDAO schoolInstitutionLocationDAO;
    private final CustomerInformationDAO customerInformationDAO;
    private final ServiceOrderApplicantDAO serviceOrderApplicantDao;
    private final OfficialHandoverLogDao officialHandoverLogDao;
    private final ServicePackagePriceDAO servicePackagePriceDAO;
    private final ServiceOrderManageDAO serviceOrderManageDAO;
    private final WebLogDAO webLogDAO;

    public ServiceOrderBatchLoader(ServiceOrderDAO serviceOrderDao, SchoolDAO schoolDao,
                                   SubagencyDAO subagencyDao, ServiceDAO serviceDao,
                                   ServicePackageDAO servicePackageDao, ReceiveTypeDAO receiveTypeDao,
                                   UserDAO userDao, ApplicantDAO applicantDao,
                                   MaraDAO maraDao, AdviserDAO adviserDao,
                                   OfficialDAO officialDao, OfficialTagDAO officialTagDao,
                                   VisaDAO visaDao, ServiceAssessDao serviceAssessDao,
                                   RegionDAO regionDAO, CommissionOrderTempDAO commissionOrderTempDao,
                                   SchoolCourseDAO schoolCourseDAO, SchoolInstitutionDAO schoolInstitutionDAO,
                                   SchoolInstitutionLocationDAO schoolInstitutionLocationDAO,
                                   CustomerInformationDAO customerInformationDAO,
                                   ServiceOrderApplicantDAO serviceOrderApplicantDao,
                                   OfficialHandoverLogDao officialHandoverLogDao,
                                   ServicePackagePriceDAO servicePackagePriceDAO,
                                   ServiceOrderManageDAO serviceOrderManageDAO,
                                   WebLogDAO webLogDAO) {
        this.serviceOrderDao = serviceOrderDao;
        this.schoolDao = schoolDao;
        this.subagencyDao = subagencyDao;
        this.serviceDao = serviceDao;
        this.servicePackageDao = servicePackageDao;
        this.receiveTypeDao = receiveTypeDao;
        this.userDao = userDao;
        this.applicantDao = applicantDao;
        this.maraDao = maraDao;
        this.adviserDao = adviserDao;
        this.officialDao = officialDao;
        this.officialTagDao = officialTagDao;
        this.visaDao = visaDao;
        this.serviceAssessDao = serviceAssessDao;
        this.regionDAO = regionDAO;
        this.commissionOrderTempDao = commissionOrderTempDao;
        this.schoolCourseDAO = schoolCourseDAO;
        this.schoolInstitutionDAO = schoolInstitutionDAO;
        this.schoolInstitutionLocationDAO = schoolInstitutionLocationDAO;
        this.customerInformationDAO = customerInformationDAO;
        this.serviceOrderApplicantDao = serviceOrderApplicantDao;
        this.officialHandoverLogDao = officialHandoverLogDao;
        this.servicePackagePriceDAO = servicePackagePriceDAO;
        this.serviceOrderManageDAO = serviceOrderManageDAO;
        this.webLogDAO = webLogDAO;
    }

    public ServiceOrderBatchContext batchLoadRelatedData(List<ServiceOrderDO> orders) {
        ServiceOrderBatchContext ctx = new ServiceOrderBatchContext();
        if (orders == null || orders.isEmpty()) return ctx;

        Set<Integer> schoolIds = new LinkedHashSet<>();
        Set<Integer> subagencyIds = new LinkedHashSet<>();
        Set<Integer> serviceIds = new LinkedHashSet<>();
        Set<Integer> servicePackageIds = new LinkedHashSet<>();
        Set<Integer> receiveTypeIds = new LinkedHashSet<>();
        Set<Integer> userIds = new LinkedHashSet<>();
        Set<Integer> applicantIds = new LinkedHashSet<>();
        Set<Integer> maraIds = new LinkedHashSet<>();
        Set<Integer> adviserIds = new LinkedHashSet<>();
        Set<Integer> officialIds = new LinkedHashSet<>();
        Set<Integer> serviceOrderIds = new LinkedHashSet<>();
        Set<Integer> schoolCourseIds = new LinkedHashSet<>();
        Set<Integer> serviceAssessIds = new LinkedHashSet<>();
        Set<Integer> parentIds = new LinkedHashSet<>();
        Set<Integer> locationIds = new LinkedHashSet<>();

        for (ServiceOrderDO order : orders) {
            serviceOrderIds.add(order.getId());
            if (order.getSchoolId() > 0) schoolIds.add(order.getSchoolId());
            if (order.getSubagencyId() > 0) subagencyIds.add(order.getSubagencyId());
            if (order.getServiceId() > 0) serviceIds.add(order.getServiceId());
            if (order.getServicePackageId() > 0) servicePackageIds.add(order.getServicePackageId());
            if (order.getReceiveTypeId() > 0) receiveTypeIds.add(order.getReceiveTypeId());
            if (order.getUserId() > 0) userIds.add(order.getUserId());
            if (order.getMaraId() > 0) maraIds.add(order.getMaraId());
            if (order.getAdviserId() > 0) adviserIds.add(order.getAdviserId());
            if (order.getOfficialId() > 0) officialIds.add(order.getOfficialId());
            if (order.getApplicantId() > 0) applicantIds.add(order.getApplicantId());
            if (order.getCourseId() > 0) schoolCourseIds.add(order.getCourseId());
            if (order.getServiceAssessId() != null && !order.getServiceAssessId().equals("0")) {
                try { serviceAssessIds.add(Integer.parseInt(order.getServiceAssessId())); } catch (NumberFormatException ignored) {}
            }
            if (order.getParentId() > 0) parentIds.add(order.getParentId());
            if (order.getSchoolInstitutionLocationId() > 0) locationIds.add(order.getSchoolInstitutionLocationId());
        }

        // 1. schools
        if (!schoolIds.isEmpty()) {
            List<SchoolDO> list = schoolDao.listByIds(new ArrayList<>(schoolIds));
            list.forEach(s -> ctx.schoolMap.put(s.getId(), s));
        }
        // 2. subagencies
        if (!subagencyIds.isEmpty()) {
            List<SubagencyDO> list = subagencyDao.listByIds(new ArrayList<>(subagencyIds));
            list.forEach(s -> ctx.subagencyMap.put(s.getId(), s));
        }
        // 3. services
        if (!serviceIds.isEmpty()) {
            List<ServiceDO> list = serviceDao.listByIds(new ArrayList<>(serviceIds));
            list.forEach(s -> ctx.serviceMap.put(s.getId(), s));
        }
        // 4. service packages
        if (!servicePackageIds.isEmpty()) {
            List<ServicePackageDO> list = servicePackageDao.listByIds(new ArrayList<>(servicePackageIds));
            list.forEach(s -> ctx.servicePackageMap.put(s.getId(), s));
        }
        // 5. receive types
        if (!receiveTypeIds.isEmpty()) {
            List<ReceiveTypeDO> list = receiveTypeDao.listByIds(new ArrayList<>(receiveTypeIds));
            list.forEach(s -> ctx.receiveTypeMap.put(s.getId(), s));
        }
        // 6. users
        if (!userIds.isEmpty()) {
            List<UserDO> list = userDao.listByIds(new ArrayList<>(userIds));
            list.forEach(s -> ctx.userMap.put(s.getId(), s));
        }
        // 7. applicants
        if (!applicantIds.isEmpty()) {
            List<ApplicantDO> list = applicantDao.listByIds(new ArrayList<>(applicantIds));
            list.forEach(s -> ctx.applicantMap.put(s.getId(), s));
        }
        // 8. maras
        if (!maraIds.isEmpty()) {
            List<MaraDO> list = maraDao.listByIds(new ArrayList<>(maraIds));
            list.forEach(s -> ctx.maraMap.put(s.getId(), s));
        }
        // 9. advisers
        if (!adviserIds.isEmpty()) {
            List<AdviserDO> list = adviserDao.listByIds(new ArrayList<>(adviserIds));
            list.forEach(s -> ctx.adviserMap.put(s.getId(), s));
        }
        // 10. officials
        if (!officialIds.isEmpty()) {
            List<OfficialDO> list = officialDao.listByIds(new ArrayList<>(officialIds));
            list.forEach(s -> ctx.officialMap.put(s.getId(), s));
        }
        // 11. official tags
        if (!serviceOrderIds.isEmpty()) {
            List<OfficialTagDO> tagList = officialTagDao.listByServiceOrderIds(new ArrayList<>(serviceOrderIds));
            tagList.forEach(t -> ctx.officialTagMap.put(t.getId(), t));
        }
        // 12. visas
        if (!serviceOrderIds.isEmpty()) {
            List<VisaDO> visaList = visaDao.listByServiceOrderIds(new ArrayList<>(serviceOrderIds));
            for (VisaDO v : visaList) {
                ctx.visaMap.computeIfAbsent(v.getServiceOrderId(), k -> new ArrayList<>()).add(v);
            }
        }
        // 13. children orders
        if (!parentIds.isEmpty()) {
            List<ServiceOrderDO> childrenList = serviceOrderDao.listByParentIds(new ArrayList<>(parentIds));
            for (ServiceOrderDO child : childrenList) {
                if (child.getParentId() > 0) {
                    ctx.childrenOrderMap.computeIfAbsent(child.getParentId(), k -> new ArrayList<>()).add(child);
                }
            }
        }
        // 14. commission order temp
        if (!serviceOrderIds.isEmpty()) {
            List<CommissionOrderTempDO> tempList = commissionOrderTempDao.listByServiceOrderIds(new ArrayList<>(serviceOrderIds));
            tempList.forEach(t -> ctx.commissionOrderTempMap.put(t.getServiceOrderId(), t));
        }
        // 15. customer information
        if (!serviceOrderIds.isEmpty()) {
            List<CustomerInformationDO> ciList = customerInformationDAO.listByServiceOrderIds(new ArrayList<>(serviceOrderIds));
            ciList.forEach(ci -> ctx.customerInformationMap.put(ci.getServiceOrderId(), ci));
        }
        // 16. web logs (contract data)
        if (!serviceOrderIds.isEmpty()) {
            List<WebLogDTO> webLogList = webLogDAO.listContractDataByOrderIds(new ArrayList<>(serviceOrderIds));
            for (WebLogDTO wl : webLogList) {
                ctx.webLogMap.computeIfAbsent(wl.getServiceOrderId(), k -> new ArrayList<>()).add(wl);
            }
        }
        // 17. service order applicants
        if (!serviceOrderIds.isEmpty()) {
            List<ServiceOrderApplicantDO> soaList = serviceOrderApplicantDao.listByServiceOrderIds(new ArrayList<>(serviceOrderIds));
            for (ServiceOrderApplicantDO soa : soaList) {
                ctx.serviceOrderApplicantMap.computeIfAbsent(soa.getServiceOrderId(), k -> new ArrayList<>()).add(soa);
            }
        }
        // 18. old officials
        if (!serviceOrderIds.isEmpty()) {
            List<Map<String, Object>> oldOfficialList = officialHandoverLogDao.listOldOfficialsByServiceOrderIds(new ArrayList<>(serviceOrderIds));
            for (Map<String, Object> row : oldOfficialList) {
                Object sid = row.get("serviceOrderId");
                Object oid = row.get("officialId");
                if (sid != null && oid != null) {
                    ctx.oldOfficialMap.put((Integer) sid, (Integer) oid);
                }
            }
        }
        // 19. service package prices
        if (!serviceIds.isEmpty()) {
            List<ServicePackagePriceDO> priceList = servicePackagePriceDAO.listByServiceIds(new ArrayList<>(serviceIds));
            priceList.forEach(p -> ctx.servicePackagePriceMap.put(p.getServiceId(), p));
        }
        // 20. service order manage
        if (!serviceOrderIds.isEmpty()) {
            List<ServiceOrderAndManage> manageList = serviceOrderManageDAO.listByServiceOrderIds(new ArrayList<>(serviceOrderIds));
            manageList.forEach(m -> ctx.serviceOrderManageMap.put(m.getServiceOrderId(), m));
        }
        // 21. school courses
        if (!schoolCourseIds.isEmpty()) {
            List<SchoolCourseDO> scList = schoolCourseDAO.listByIds(new ArrayList<>(schoolCourseIds));
            scList.forEach(sc -> ctx.schoolCourseMap.put(sc.getId(), sc));
        }
        // 22. service assess
        if (!serviceAssessIds.isEmpty()) {
            List<String> ids = serviceAssessIds.stream().map(String::valueOf).collect(Collectors.toList());
            List<ServiceAssessDO> saList = serviceAssessDao.listByIds(ids);
            saList.forEach(sa -> ctx.serviceAssessMap.put(sa.getId(), sa));
        }
        // 23. school institution locations
        if (!locationIds.isEmpty()) {
            List<SchoolInstitutionLocationDO> silList = schoolInstitutionLocationDAO.listByIds(new ArrayList<>(locationIds));
            silList.forEach(sil -> ctx.schoolInstitutionLocationMap.put(sil.getId(), sil));
        }

        // 第二阶段：依赖 advisers 结果的 regions 查询
        Set<Integer> regionIds = new LinkedHashSet<>();
        for (AdviserDO a : ctx.adviserMap.values()) {
            if (a.getRegionId() > 0) regionIds.add(a.getRegionId());
        }
        if (!regionIds.isEmpty()) {
            List<RegionDO> list = regionDAO.listByIds(new ArrayList<>(regionIds));
            list.forEach(s -> ctx.regionMap.put(s.getId(), s));
        }
        // 依赖 school courses 结果的 school institutions 查询
        Set<Integer> institutionIds = new LinkedHashSet<>();
        for (SchoolCourseDO sc : ctx.schoolCourseMap.values()) {
            institutionIds.add(sc.getProviderId());
        }
        if (!institutionIds.isEmpty()) {
            List<SchoolInstitutionDO> siList = schoolInstitutionDAO.listByIds(new ArrayList<>(institutionIds));
            siList.forEach(si -> ctx.schoolInstitutionMap.put(si.getId(), si));
        }

        return ctx;
    }
}
