package org.zhinanzhen.b.service.impl;

import com.ikasoa.core.ErrorCodeEnum;
import com.ikasoa.core.utils.ListUtil;
import com.ikasoa.core.utils.ObjectUtil;
import com.ikasoa.core.utils.StringUtil;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.zhinanzhen.b.config.GlobalThreadPool;
import org.zhinanzhen.b.controller.nodes.SONodeFactory;
import org.zhinanzhen.b.dao.*;
import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.service.ServiceOrderManageService;
import org.zhinanzhen.b.service.VisaOfficialService;
import org.zhinanzhen.b.service.pojo.*;
import org.zhinanzhen.b.service.pojo.ant.Sorter;
import org.zhinanzhen.tb.dao.AdminUserDAO;
import org.zhinanzhen.tb.dao.AdviserDAO;
import org.zhinanzhen.tb.dao.RegionDAO;
import org.zhinanzhen.tb.dao.UserDAO;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
import org.zhinanzhen.tb.dao.pojo.RegionDO;
import org.zhinanzhen.tb.dao.pojo.UserDO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;
import org.zhinanzhen.tb.service.pojo.AdviserDTO;
import org.zhinanzhen.tb.service.pojo.UserDTO;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service("ServiceOrderManageService")
public class ServiceOrderManageServiceImpl extends BaseService implements ServiceOrderManageService {

    @Resource
    private ServiceOrderManageDAO serviceOrderManageDAO;

    @Resource
    private ServiceOrderDAO serviceOrderDao;

    //	@Resource
//	private ServiceOrderReviewDAO serviceOrderReviewDao;
    @Resource
    private ServicePackagePriceDAO servicePackagePriceDAO;

    @Resource
    private ServiceOrderApplicantDAO serviceOrderApplicantDao;

    @Resource
    OfficialHandoverLogDao officialHandoverLogDao;

    @Resource
    private SchoolDAO schoolDao;

    @Resource
    private SubagencyDAO subagencyDao;

    @Resource
    private ServiceDAO serviceDao;

    @Resource
    private ReceiveTypeDAO receiveTypeDao;

    @Resource
    private UserDAO userDao;

    @Resource
    private ApplicantDAO applicantDao;

    @Resource
    private MaraDAO maraDao;

    @Resource
    private AdviserDAO adviserDao;

    @Resource
    private OfficialDAO officialDao;

    @Resource
    private KjDAO kjDao;

    @Resource
    private ServicePackageDAO servicePackageDao;

    @Resource
    private ServiceOrderCommentDAO serviceOrderCommentDao;

    @Resource
    private CommissionOrderDAO commissionOrderDao;

    @Resource
    private ServiceOrderOfficialRemarksDAO serviceOrderOfficialRemarksDao;

    @Resource
    private OfficialTagDAO officialTagDao;

    @Resource
    private VisaDAO visaDao;

    @Resource
    private AdminUserDAO adminUserDao;

    @Resource
    private ServiceAssessDao serviceAssessDao;

    @Resource
    private WXWorkDAO wxWorkDAO;

    @Resource
    private RegionDAO regionDAO;

    @Resource
    private MailRemindDAO mailRemindDAO;

    @Resource
    private CommissionOrderTempDAO commissionOrderTempDao;

    @Resource
    private SchoolCourseDAO schoolCourseDAO;

    @Resource
    private SchoolInstitutionDAO schoolInstitutionDAO;

    @Resource
    private SchoolInstitutionLocationDAO schoolInstitutionLocationDAO;

    @Resource
    private CustomerInformationDAO customerInformationDAO;

    @Resource
    private CommissionOrderTempDAO commissionOrderTempDAO;

    @Resource
    private InsuranceCompanyDAO insuranceCompanyDAO;

    @Resource
    private ServiceOrderOriginallyDAO serviceOrderOriginallyDAO;

    @Resource
    private VisaDAO visaDAO;

    @Resource
    private RefundDAO refundDAO;

    @Resource
    private CloudDiskFileDAO cloudDiskFileDAO;

    @Resource
    private VisaOfficialDao visaOfficialDao;

    @Resource
    private VisaOfficialService visaOfficialService;

    @Override
    public int addServiceOrderAndManage(ServiceOrderAndManage serviceOrderAndManage) {
        return serviceOrderManageDAO.addServiceOrderAndManage(serviceOrderAndManage);
    }

    @Override
    public int add(ServiceOrderDTO serviceOrderDto) {
        return serviceOrderManageDAO.add(serviceOrderDto);

    }

    @Override
    public int countServiceOrder(String type, List<String> excludeTypeList, String excludeState, List<String> stateList, String auditingState, List<String> reviewStateList, String urgentState, String startMaraApprovalDate, String endMaraApprovalDate, String startOfficialApprovalDate, String endOfficialApprovalDate, String startReadcommittedDate, String endReadcommittedDate, String startFinishDate, String endFinishDate, List<Integer> adviserRegionIdList, List<Integer> officialRegionIdList, Integer userId, String userName, String applicantName, Integer maraId, Integer adviserId, Integer officialId, Integer officialTagId, int parentId, int applicantParentId, boolean isNotApproved, Integer serviceId, Integer servicePackageId, Integer schoolId, Boolean isPay, Boolean isSettle, Boolean bindingList, Integer courseId, String tradingName, Integer schoolLocation) throws ServiceException {
        if (bindingList != null && bindingList) {
            if ("OVST".equals(type)) {
                type = "bindingList2";
            } else if ("SIV".equals(type)) {
                type = "bindingList3";
            } else {
                type = "bindingList";
            }
        }
        return serviceOrderManageDAO.countServiceOrder(type, excludeTypeList, excludeState, stateList, auditingState,
                reviewStateList, urgentState, theDateTo00_00_00(startMaraApprovalDate),
                theDateTo23_59_59(endMaraApprovalDate), theDateTo00_00_00(startOfficialApprovalDate),
                theDateTo23_59_59(endOfficialApprovalDate), theDateTo00_00_00(startReadcommittedDate),
                theDateTo23_59_59(endReadcommittedDate), theDateTo00_00_00(startFinishDate), theDateTo23_59_59(endFinishDate), adviserRegionIdList, officialRegionIdList, userId, userName, applicantName, maraId, adviserId, officialId,
                officialTagId, parentId, applicantParentId, isNotApproved, serviceId, servicePackageId, schoolId, isPay, isSettle, courseId, tradingName, schoolLocation);
    }

    @Override
    public List<ServiceOrderDTO> listServiceOrder(String type, List<String> excludeTypeList, String excludeState, List<String> stateList, String auditingState, List<String> reviewStateList, String urgentState, String startMaraApprovalDate, String endMaraApprovalDate, String startOfficialApprovalDate, String endOfficialApprovalDate, String startReadcommittedDate, String endReadcommittedDate, String startFinishDate, String endFinishDate, List<Integer> adviserRegionIdList, List<Integer> officialRegionIdList, Integer userId, String userName, String applicantName, Integer maraId, Integer adviserId, Integer officialId, Integer officialTagId, int parentId, int applicantParentId, boolean isNotApproved, int pageNum, int pageSize, Sorter sorter, Integer serviceId, Integer servicePackageId, Integer schoolId, Boolean isPay, Boolean isSettle, Boolean bindingList, Integer courseId, String tradingName, Integer schoolLocation) throws ServiceException {
        schoolId = null;
        List<ServiceOrderDTO> serviceOrderDtoList = new ArrayList<ServiceOrderDTO>();
        List<ServiceOrderDO> serviceOrderDoList = new ArrayList<ServiceOrderDO>();
        if (pageNum < 0)
            pageNum = DEFAULT_PAGE_NUM;
        if (pageSize < 0)
            pageSize = DEFAULT_PAGE_SIZE;
        String orderBy = "ORDER BY so.id DESC";
        if (sorter != null) {
            if (sorter.getId() != null)
                orderBy = StringUtil.merge("ORDER BY ", sorter.getOrderBy("so.id", sorter.getId()));
            if (sorter.getAdviserName() != null)
                orderBy = StringUtil.merge("ORDER BY ", sorter.getOrderBy("a.name", sorter.getAdviserName()));
        }
        try {
            if (bindingList != null && bindingList) {
                if ("OVST".equalsIgnoreCase(type)) {
                    type = "bindingList2";
                } else if ("SIV".equalsIgnoreCase(type)) {
                    type = "bindingList3";
                } else {
                    type = "bindingList";
                }
            }
            serviceOrderDoList = serviceOrderManageDAO.listServiceOrder(null, null, type, excludeTypeList, excludeState, stateList,
                    auditingState, reviewStateList, urgentState, theDateTo00_00_00(startMaraApprovalDate), theDateTo23_59_59(endMaraApprovalDate),
                    theDateTo00_00_00(startOfficialApprovalDate), theDateTo23_59_59(endOfficialApprovalDate), theDateTo00_00_00(startReadcommittedDate),
                    theDateTo23_59_59(endReadcommittedDate), theDateTo00_00_00(startFinishDate), theDateTo23_59_59(endFinishDate), adviserRegionIdList, officialRegionIdList, userId, userName, applicantName, maraId, adviserId, officialId, officialTagId,
                    parentId, applicantParentId, isNotApproved, serviceId, servicePackageId, schoolId, isPay, isSettle,null, pageNum * pageSize, pageSize, orderBy, courseId, tradingName, schoolLocation);
            if (serviceOrderDoList == null)
                return null;

            List<ServiceOrderDO> collect = new ArrayList<>();
            long count = 0L;
            if ("bindingList".equals(type)) {
                collect = serviceOrderDoList.stream().filter(ServiceOrderDO -> !"OVST".equals(ServiceOrderDO.getType())).collect(Collectors.toList());
            } else {
                collect = serviceOrderDoList;
            }
            CountDownLatch latch = new CountDownLatch(collect.size());
            for (int i = 0; i < collect.size(); i++) {
                ServiceOrderDO serviceOrderDo = collect.get(i);
                List<ServiceOrderDO> serviceOrderSubs = serviceOrderManageDAO.listSub(serviceOrderDo.getId());
                if (serviceOrderSubs != null) {
                    serviceOrderDo.setSubServiceOrders(serviceOrderSubs);
                }
                ThreadPoolExecutor executor = GlobalThreadPool.getInstance();
                executor.submit(() -> {
                    try {
                        serviceOrderDtoList.add(putServiceOrderDTO(serviceOrderDo));
                        for (ServiceOrderDO serviceOrderSub : serviceOrderSubs) {
                            putServiceOrderDTO(serviceOrderSub);
                        }
                        // 只有在成功执行了任务后才减少计数器
                        latch.countDown(); // 完成任务，计数器减一
                    } catch (Exception e) {
                        latch.countDown();
                        e.printStackTrace();
                    }
                });
            }
            latch.await();
            if (!serviceOrderDtoList.isEmpty()) {
                serviceOrderDtoList.get(0).setBindingOrderCount(count);
            }
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
            throw se;
        }
        return serviceOrderDtoList.stream().sorted(Comparator.comparing(ServiceOrderDTO::getId).reversed()).collect(Collectors.toList());
    }

    @Override
    public ServiceOrderDTO getServiceOrderById(Integer id) throws ServiceException {
        if (id <= 0) {
            ServiceException se = new ServiceException("service order id error !");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        ServiceOrderDTO serviceOrderDto = null;
        List<ServiceOrderDTO> subServiceOrderDtos = new ArrayList<>();
        try {
            Integer i = firstPlace(id);
            if (i == 1) {
                return null;
            }
            ServiceOrderDO serviceOrderDo = serviceOrderManageDAO.getServiceOrderById(id);
            List<ServiceOrderDO> serviceOrderDOS = serviceOrderManageDAO.listSub(serviceOrderDo.getId());
            if (serviceOrderDOS != null) {
                for (ServiceOrderDO serviceOrderDO : serviceOrderDOS) {
                    subServiceOrderDtos.add(putServiceOrderDTO(serviceOrderDO));
                }
            }
            if (serviceOrderDo == null)
                return null;
            serviceOrderDo.setDistributableAmount(serviceOrderDo.getReceivable());
            serviceOrderDto = putServiceOrderDTO(serviceOrderDo);
            serviceOrderDto.setSubServiceOrders(subServiceOrderDtos);
            // 是否有创建过佣金订单
            if ("OVST".equalsIgnoreCase(serviceOrderDto.getType()))
                serviceOrderDto.setHasCommissionOrder(commissionOrderDao
                        .countCommissionOrderByServiceOrderIdAndExcludeCode(serviceOrderDto.getId(), null) > 0);
            else if ("VISA".equalsIgnoreCase(serviceOrderDto.getType()))
                serviceOrderDto.setHasCommissionOrder(
                        visaDao.countVisaByServiceOrderIdAndExcludeCode(serviceOrderDto.getId(), null) > 0);
            else
                serviceOrderDto.setHasCommissionOrder(false);
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
        return serviceOrderDto;

    }

    @Override
    public int updateServiceOrderManage(ServiceOrderDTO serviceorderManageDto) throws ServiceException {
        if (serviceorderManageDto == null) {
            ServiceException se = new ServiceException("serviceOrderDto is null !");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        if (serviceorderManageDto.getId() <= 0) {
            ServiceException se = new ServiceException("id is null !");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        if (serviceorderManageDto.getVerifyCode() != null) {
            List<CommissionOrderDO> commissionOrderDOS = commissionOrderDao
                    .listCommissionOrderByVerifyCode(serviceorderManageDto.getVerifyCode());
            List<VisaDO> visaDOS = visaDao.listVisaByVerifyCode(serviceorderManageDto.getVerifyCode());
            List<CommissionOrderTempDO> list = commissionOrderTempDao.getCommissionOrderTempByVerifyCode(serviceorderManageDto.getVerifyCode());
            if ((commissionOrderDOS.size() > 0 || list.size() > 0) && !serviceorderManageDto.getIsInsertEoi()) {
                ServiceException se = new ServiceException(
                        "对账code:" + serviceorderManageDto.getVerifyCode() + "已经存在,请重新创建新的code!");
                se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
                throw se;
            }
            for (VisaDO visaDO : visaDOS) {
                if ((visaDO.getServiceOrderId() != serviceorderManageDto.getId() && visaDO.getServiceOrderId() != serviceorderManageDto.getApplicantParentId()) && !serviceorderManageDto.getIsInsertEoi()) {
                    ServiceException se = new ServiceException(
                            "对账code:" + serviceorderManageDto.getVerifyCode() + "已经存在,请重新创建新的code!");
                    se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
                    throw se;
                }
            }
        }
        String offerType1 = serviceorderManageDto.getOfferType();
        long time = serviceorderManageDto.getGmtCreate().getTime();
        long timeTmp = 1721577600000L;
        if ("COMPLETE".equals(serviceorderManageDto.getState()) && "OVST".equals(serviceorderManageDto.getType()) && (timeTmp < time)) {
            String offerType = serviceorderManageDto.getOfferType();
            if (StringUtil.isEmpty(offerType) && StringUtil.isEmpty(offerType1)) {
                ServiceException se = new ServiceException(
                        "当前留学订单" + serviceorderManageDto.getId() + "没有设置offer类型，请核实");
                se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
                throw se;
            }
        }
        try {
            ServiceOrderDO _serviceOrderDo = serviceOrderManageDAO.getServiceOrderById(serviceorderManageDto.getId());
            ServiceOrderDO serviceOrderDo = mapper.map(serviceorderManageDto, ServiceOrderDO.class);
            if (serviceorderManageDto.isSettle() != _serviceOrderDo.isSettle() && serviceorderManageDto.isSettle() && _serviceOrderDo.getState().equalsIgnoreCase("PAID")) {
                serviceOrderDo.setState("COMPLETE");
            }
            LOG.info("修改服务订单(serviceOrderDo=" + serviceOrderDo + ").");
            int i = serviceOrderManageDAO.updateServiceOrder(serviceOrderDo);
            if (i > 0
                    && ((_serviceOrderDo.getMaraId() > 0 && serviceOrderDo.getMaraId() > 0
                    && _serviceOrderDo.getMaraId() != serviceOrderDo.getMaraId())
                    || (_serviceOrderDo.getOfficialId() > 0 && serviceOrderDo.getOfficialId() > 0
                    && _serviceOrderDo.getOfficialId() != serviceOrderDo.getOfficialId()))
                    && (!"PENDING".equalsIgnoreCase(serviceOrderDo.getState())
                    || StringUtil.equals("Retracted", serviceOrderDo.getStateMark2())))
                sendEmailOfUpdateOfficial(serviceOrderDo, _serviceOrderDo);
            if (i > 0
                    && ((_serviceOrderDo.getServiceId() > 0 && serviceOrderDo.getServiceId() > 0
                    && _serviceOrderDo.getServiceId() != serviceOrderDo.getServiceId()))
                    && (!"PENDING".equalsIgnoreCase(serviceOrderDo.getState())
                    || StringUtil.equals("Retracted", serviceOrderDo.getStateMark2())))
                sendEmailOfUpdateServiceId(serviceOrderDo, _serviceOrderDo);
            if (i > 0 && "WAIT".equalsIgnoreCase(serviceOrderDo.getState())) {
                ServiceOrderManageServiceImpl.ServiceOrderMailDetail serviceOrderMailDetail = getServiceOrderMailDetail(serviceOrderDo, "任务提醒:");
                AdviserDO adviserDo = adviserDao.getAdviserById(serviceOrderDo.getAdviserId());
                OfficialDO officialDo = officialDao.getOfficialById(serviceOrderDo.getOfficialId());
                MaraDO maraDo = maraDao.getMaraById(serviceOrderDo.getMaraId());
                ApplicantDTO applicantDto = new ApplicantDTO();
                if (serviceOrderDo.getApplicantId() > 0)
                    applicantDto = mapper.map(applicantDao.getById(serviceOrderDo.getApplicantId()), ApplicantDTO.class);
                applicantDto = buildApplicant(applicantDto, serviceOrderDo.getId(), serviceOrderDo.getNutCloud(),
                        serviceOrderDo.getInformation());
                Date date = serviceOrderDo.getGmtCreate();
                String email = maraDo.getEmail();
                if (maraDo.getId() == 1000017) {
                    email = "maggie@zhinanzhen.org";
                }
                sendMail(email, "新任务提醒:",
                        StringUtil.merge("亲爱的mara:", maraDo.getName(), "<br/>", "您有一条新的服务订单任务请及时处理。", "<br>订单号:",
                                serviceOrderDo.getId(), "<br/>服务类型:签证/申请人名称:", getApplicantName(applicantDto), "/顾问:",
                                adviserDo.getName(), "/文案:", officialDo.getName(), "/MARA:", maraDo.getName(),
                                "<br/>属性:", getPeopleTypeStr(serviceOrderDo.getPeopleType()), "<br/>坚果云资料地址:",
                                applicantDto.getUrl(), "<br/>客户基本信息:", applicantDto.getContent(), "<br/>备注:",
                                serviceOrderDo.getRemarks(),"<br/>创建时间:", date, "<br/>", serviceOrderMailDetail.getServiceOrderUrl()));
            }
            return i;
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }

    }

    public ServiceOrderDTO putServiceOrderDTO(ServiceOrderDO serviceOrderDO) {
        ServiceOrderDTO serviceOrderDto = mapper.map(serviceOrderDO, ServiceOrderDTO.class);
        //获取旧文案信息
        Integer oldOfficialId = officialHandoverLogDao.getOldOfficial(serviceOrderDto.getId());
        if (oldOfficialId != null) {
            serviceOrderDto.setOldOfficial(officialDao.getOfficialById(oldOfficialId));
        }
        // 查询学校课程
        if (serviceOrderDto.getSchoolId() > 0) {
            SchoolDO schoolDo = schoolDao.getSchoolById(serviceOrderDto.getSchoolId());
            if (schoolDo != null)
                serviceOrderDto.setSchool(mapper.map(schoolDo, SchoolDTO.class));
        }
        // 查询Subagency
        if (serviceOrderDto.getSubagencyId() > 0) {
            SubagencyDO subagencyDo = subagencyDao.getSubagencyById(serviceOrderDto.getSubagencyId());
            if (subagencyDo != null)
                serviceOrderDto.setSubagency(mapper.map(subagencyDo, SubagencyDTO.class));
        }
        // 查询服务
        ServiceDO serviceDo = serviceDao.getServiceById(serviceOrderDto.getServiceId());
        if (serviceDo != null) {
            if (serviceDo.getCode().contains("EOI")) {
                if (serviceOrderDto.getServicePackageId() == 0) {
                    StringBuilder eoiList = new StringBuilder();
                    List<ServiceOrderDTO> deriveOrder = serviceOrderDao.getDeriveOrder(serviceOrderDto.getId());
                    if (deriveOrder != null && !deriveOrder.isEmpty()) {
                        for (ServiceOrderDTO e : deriveOrder) {
                            ServicePackageDTO eoiService = servicePackageDao.getEOIService(e.getServicePackageId());
                            eoiList.append(eoiService.getServiceCode()).append(",");
                        }
                    }
                    if (StringUtils.isNotBlank(String.valueOf(eoiList))) {
                        serviceOrderDto.setEoiList(eoiList.substring(0, eoiList.length() - 1));
                    }
                }
                if (serviceOrderDto.getApplicantId() == 0 && serviceOrderDto.getApplicantParentId() == 0) {
                    List<ServiceAssessAndEOI> serviceAssessAndEOIList = new ArrayList<>();
                    List<ServiceOrderDTO> deriveOrder = serviceOrderDao.getDeriveOrder(serviceOrderDto.getId());
                    if (deriveOrder != null && !deriveOrder.isEmpty() && deriveOrder.get(0).getServiceAssessId() != null) {
                        deriveOrder.stream().collect(Collectors.groupingBy(ServiceOrderDTO::getServiceAssessId)).forEach((k, v) -> {
                            ServiceAssessAndEOI serviceAssessAndEOI = new ServiceAssessAndEOI();
                            ServiceAssessDO serviceAssessDO = serviceAssessDao.seleteAssessById(k);
                            if (serviceAssessDO != null) {
                                StringBuilder eoiNum = new StringBuilder();
                                serviceAssessAndEOI.setLabel(serviceAssessDO.getName());
                                serviceAssessAndEOI.setValue(k);
                                serviceAssessAndEOI.setKey(k);
                                for (ServiceOrderDTO serviceOrderDTO : v) {
                                    eoiNum.append(",").append(serviceOrderDTO.getServicePackageId());
                                }
                                serviceAssessAndEOI.setEoiServicePackageId(eoiNum.substring(1, eoiNum.length()));
                                serviceAssessAndEOIList.add(serviceAssessAndEOI);
                            }
                        });
                        serviceOrderDto.setServiceAssessAndEOIList(serviceAssessAndEOIList);
                    }
                }
                ServicePackageDO servicePackageDo = servicePackageDao.getEOIServiceCode(serviceOrderDto.getServicePackageId());
                if (ObjectUtil.isNotNull(servicePackageDo)) {
                    serviceDo.setCode(servicePackageDo.getType());
                }
            }
            serviceOrderDto.setService(mapper.map(serviceDo, ServiceDTO.class));
        }
        // 查询服务包类型
        if (serviceOrderDto.getServicePackageId() > 0) {
            ServicePackageDO servicePackageDAOById = servicePackageDao.getById(serviceOrderDto.getServicePackageId());
            if ("EOI".equals(servicePackageDAOById.getType())) {
                ServicePackageDTO servicePackageDTO = servicePackageDao.getEOIService(serviceOrderDto.getServicePackageId());
                if (ObjectUtil.isNotNull(servicePackageDTO)) {
                    serviceOrderDto.setServicePackage(servicePackageDTO);
                }
            } else {
                ServicePackageDO servicePackageDo = servicePackageDao.getById(serviceOrderDto.getServicePackageId());
                if (servicePackageDo != null)
                    serviceOrderDto.setServicePackage(mapper.map(servicePackageDo, ServicePackageDTO.class));
            }
        }
        // 查询收款方式
        ReceiveTypeDO receiveTypeDo = receiveTypeDao.getReceiveTypeById(serviceOrderDto.getReceiveTypeId());
        if (receiveTypeDo != null)
            serviceOrderDto.setReceiveType(mapper.map(receiveTypeDo, ReceiveTypeDTO.class));
        // 查询用户
        UserDO userDo = userDao.getUserById(serviceOrderDto.getUserId());
        if (userDo != null) {
            org.zhinanzhen.tb.service.pojo.UserDTO userDto = mapper.map(userDo, UserDTO.class);
            if (serviceOrderDto.getUserId() > 0 && serviceOrderDto.getApplicantId() >= 0) {
                List<ServiceOrderApplicantDO> serviceOrderApplicantList = serviceOrderApplicantDao.list(serviceOrderDto.getId(), null);
                List<ApplicantDTO> applicantDtoList = new ArrayList<>();
                serviceOrderApplicantList.forEach(serviceOrderApplicant -> {
                    ApplicantDO applicantDo = applicantDao.getById(serviceOrderApplicant.getApplicantId());
                    if (applicantDo != null) {
                        if (applicantDo.getFileUrl() == null) {
                            List<ServiceOrderApplicantDO> list = serviceOrderApplicantDao.list(serviceOrderDO.getId(), applicantDo.getId());
                            if (list != null) {
                                applicantDo.setFileUrl(list.get(0).getUrl());
                                applicantDo.setFirstControllerContents(list.get(0).getContent());
                            }
                        }
                        applicantDtoList.add(mapper.map(applicantDo, ApplicantDTO.class));
                    }
                });
                if (applicantDtoList.size() == 0 && serviceOrderDto.getApplicant() != null)
                    applicantDtoList.add(serviceOrderDto.getApplicant());
                userDto.setApplicantList(applicantDtoList);
            }
            serviceOrderDto.setUser(userDto);
        }
        if (serviceOrderDto.getApplicantId() > 0) {
            ApplicantDO applicantDo = applicantDao.getById(serviceOrderDto.getApplicantId());
            if (applicantDo != null) {
                ApplicantDTO applicantDto = mapper.map(applicantDo, ApplicantDTO.class);
                applicantDto = buildApplicant(applicantDto, serviceOrderDO.getId(), serviceOrderDto.getNutCloud(),
                        serviceOrderDto.getInformation());
                serviceOrderDto.setApplicantId(applicantDto.getId());
                serviceOrderDto.setApplicant(applicantDto);
            }
        }
        // 查询Mara
        MaraDO maraDo = maraDao.getMaraById(serviceOrderDto.getMaraId());
        if (maraDo != null)
            serviceOrderDto.setMara(mapper.map(maraDo, MaraDTO.class));
        // 查询顾问
        AdviserDO adviserDo = adviserDao.getAdviserById(serviceOrderDto.getAdviserId());
        if (adviserDo != null) {
            RegionDO regionDO = regionDAO.getRegionById(adviserDo.getRegionId());
            serviceOrderDto.setAdviser(mapper.map(adviserDo, AdviserDTO.class));
            if (regionDO != null)
                serviceOrderDto.getAdviser().setRegionName(regionDO.getName());
            serviceOrderDto.getAdviser().setRegionDo(regionDO);
        }
        // 查询顾问2
        if (serviceOrderDto.getAdviserId2() > 0) {
            AdviserDO adviserDo2 = adviserDao.getAdviserById(serviceOrderDto.getAdviserId2());
            if (adviserDo2 != null)
                serviceOrderDto.setAdviser2(mapper.map(adviserDo2, AdviserDTO.class));
        }
        // 查询文案
        OfficialDO officialDo = officialDao.getOfficialById(serviceOrderDto.getOfficialId());
        if (officialDo != null)
            serviceOrderDto.setOfficial(mapper.map(officialDo, OfficialDTO.class));
        // 查询文案Tag
        OfficialTagDO officialTagDo = officialTagDao.getOfficialTagByServiceOrderId(serviceOrderDto.getId());
        if (officialTagDo != null)
            serviceOrderDto.setOfficialTag(mapper.map(officialTagDo, OfficialTagDTO.class));
        // 查询子服务
        if (serviceOrderDto.getParentId() <= 0) {
            List<ChildrenServiceOrderDTO> childrenServiceOrderList = new ArrayList<>();
            List<ServiceOrderDO> list = serviceOrderDao.listByParentId(serviceOrderDto.getId());
            list.forEach(serviceOrder -> {
                ChildrenServiceOrderDTO childrenServiceOrderDto = mapper.map(serviceOrder,
                        ChildrenServiceOrderDTO.class);
                ServicePackageDO servicePackageDo = servicePackageDao
                        .getById(childrenServiceOrderDto.getServicePackageId()); // TODO:
                if (servicePackageDo != null)
                    childrenServiceOrderDto.setServicePackageType(servicePackageDo.getType());
                childrenServiceOrderList.add(childrenServiceOrderDto);
            });
            serviceOrderDto.setChildrenServiceOrders(childrenServiceOrderList);
        }

        List<Integer> cIds = new ArrayList<>();
        List<VisaDO> visaList = new ArrayList<>();
        if (serviceOrderDO.getParentId() != 0 || serviceOrderDO.getApplicantParentId() != 0)
            visaList = visaDao.listVisaByServiceOrderId((serviceOrderDO.getParentId()==0?serviceOrderDO.getApplicantParentId():serviceOrderDO.getParentId()));
        else
            visaList = visaDao.listVisaByServiceOrderId(serviceOrderDO.getId());
        if (visaList != null && visaList.size() > 0) {
            for (VisaDO visaDo : visaList)
                cIds.add(visaDo.getId());
        }
        serviceOrderDto.setVisaDOList(visaList);
        serviceOrderDto.setCIds(cIds);

        // 查询职业名称
        ServiceAssessDO serviceAssessDO = serviceAssessDao.seleteAssessById(serviceOrderDto.getServiceAssessId());
        if (serviceAssessDO != null) {
            serviceOrderDto.setServiceAssessDO(serviceAssessDO);
            ServiceCategory categoryIdByServiceOrderId = serviceAssessDao.getCategoryIdByServiceOrderId(serviceOrderDto.getId());
            if (categoryIdByServiceOrderId != null) {
                serviceOrderDto.setServiceCategory(categoryIdByServiceOrderId);
            }
        }
        if (serviceOrderDto.getServiceAssessId() != null && "0".equalsIgnoreCase(serviceOrderDto.getServiceAssessId())) {
            serviceOrderDto.setServiceAssessDO(new ServiceAssessDO());
        }
        List<MailRemindDO> mailRemindDOS = mailRemindDAO.list(null, null, null, serviceOrderDO.getId(), null, null, null, false, true);
        if (mailRemindDOS.size() > 0) {
            List<MailRemindDTO> mailRemindDTOS = new ArrayList<>();
            mailRemindDOS.forEach(mailRemindDO -> {
                MailRemindDTO map = mapper.map(mailRemindDO, MailRemindDTO.class);
                Integer adviserId = map.getAdviserId();
                Integer offcialId = map.getOffcialId();
                Integer kjId = map.getKjId();
                if (adviserId != null) {
                    AdviserDO adviserById = adviserDao.getAdviserById(adviserId);
                    map.setUserName(adviserById.getName());
                }
                if (offcialId != null) {
                    OfficialDO officialById = officialDao.getOfficialById(offcialId);
                    map.setUserName(officialById.getName());
                }
                if (kjId != null) {
                    KjDO kjById = kjDao.getKjById(kjId);
                    map.setUserName(kjById.getName());
                }
                mailRemindDTOS.add(map);
            });
            serviceOrderDto.setMailRemindDTOS(mailRemindDTOS);
        }

        //添加新学校相关
        if (serviceOrderDto.getCourseId() > 0) {
            SchoolCourseDO schoolCourseDO = schoolCourseDAO.schoolCourseById(serviceOrderDto.getCourseId());
            if (schoolCourseDO != null) {
                SchoolInstitutionDO schoolInstitutionDO = schoolInstitutionDAO.getSchoolInstitutionById(schoolCourseDO.getProviderId());
                if (schoolInstitutionDO != null) {
                    String tradingName = schoolInstitutionDO.getInstitutionTradingName();
                    if (StringUtil.isNotEmpty(tradingName) && tradingName.contains(";")) {
                        List<String> tradingNames = ListUtil.buildArrayList(tradingName.split(";"));
                        if (tradingNames.stream().anyMatch(a -> a.equalsIgnoreCase(serviceOrderDto.getInstitutionTradingName()))) {
                            schoolInstitutionDO.setInstitutionTradingName(serviceOrderDto.getInstitutionTradingName());
                        }
                    }
                    serviceOrderDto.setSchoolInstitutionListDTO(mapper.map(schoolInstitutionDO, SchoolInstitutionListDTO.class));
                }
                serviceOrderDto.getSchoolInstitutionListDTO().setSchoolCourseDO(schoolCourseDO);
                if (serviceOrderDto.getSchoolInstitutionLocationId() > 0) {
                    SchoolInstitutionLocationDO schoolInstitutionLocationDO = schoolInstitutionLocationDAO.getById(serviceOrderDto.getSchoolInstitutionLocationId());
                    serviceOrderDto.getSchoolInstitutionListDTO().setSchoolInstitutionLocationDO(schoolInstitutionLocationDO);
                }
            }
        }

        // 汇率币种计算金额
        Double exchangeRate = serviceOrderDto.getExchangeRate();
        if ("AUD".equalsIgnoreCase(serviceOrderDto.getCurrency())) {
            serviceOrderDto.setAmountAUD(serviceOrderDto.getAmount());
            serviceOrderDto.setAmountCNY(roundHalfUp2(serviceOrderDto.getAmount() * exchangeRate));
            serviceOrderDto.setPerAmountAUD(serviceOrderDto.getPerAmount());
            serviceOrderDto.setPerAmountCNY(roundHalfUp2(serviceOrderDto.getPerAmount() * exchangeRate));
            serviceOrderDto.setExpectAmountAUD(serviceOrderDto.getExpectAmount());
            serviceOrderDto.setExpectAmountCNY(roundHalfUp2(serviceOrderDto.getExpectAmount() * exchangeRate));
            serviceOrderDto.setReceivableAUD(serviceOrderDto.getReceivable());
            serviceOrderDto.setReceivableCNY(roundHalfUp2(serviceOrderDto.getReceivable() * exchangeRate));
            serviceOrderDto.setDiscountAUD(serviceOrderDto.getDiscount());
            serviceOrderDto.setGstAUD(serviceOrderDto.getGst());
            serviceOrderDto.setDeductGstAUD(serviceOrderDto.getDeductGst());
            serviceOrderDto.setBonusAUD(serviceOrderDto.getBonus());
        }
        if ("CNY".equalsIgnoreCase(serviceOrderDto.getCurrency())) {
            serviceOrderDto.setAmountAUD(roundHalfUp2(serviceOrderDto.getAmount() / exchangeRate));
            serviceOrderDto.setAmountCNY(serviceOrderDto.getAmount());
            serviceOrderDto.setPerAmountAUD(roundHalfUp2(serviceOrderDto.getPerAmount() / exchangeRate));
            serviceOrderDto.setPerAmountCNY(serviceOrderDto.getPerAmount());
            serviceOrderDto.setExpectAmountAUD(roundHalfUp2(serviceOrderDto.getExpectAmount() / exchangeRate));
            serviceOrderDto.setExpectAmountCNY(serviceOrderDto.getExpectAmount());
            serviceOrderDto.setReceivableAUD(roundHalfUp2(serviceOrderDto.getReceivable() / exchangeRate));
            serviceOrderDto.setReceivableCNY(serviceOrderDto.getReceivable());
            serviceOrderDto.setDiscountAUD(roundHalfUp2(serviceOrderDto.getDiscount() / exchangeRate));
            serviceOrderDto.setGstAUD(roundHalfUp2(serviceOrderDto.getGst() / exchangeRate));
            serviceOrderDto.setDeductGstAUD(roundHalfUp2(serviceOrderDto.getDeductGst() / exchangeRate));
            serviceOrderDto.setBonusAUD(roundHalfUp2(serviceOrderDto.getBonus() / exchangeRate));
        }
        //判断是否生成文案佣金
        if (!serviceOrderDO.isPay() && (servicePackagePriceDAO.getByServiceId(serviceOrderDO.getServiceId()) != null && servicePackagePriceDAO.getByServiceId(serviceOrderDO.getServiceId()).getRuler() == 1) || serviceOrderDO.isPay()) {
            serviceOrderDto.setCreateVisaOffice(true);
        } else
            serviceOrderDto.setCreateVisaOffice(false);
        ServiceOrderDO parentServiceOrder = serviceOrderDao.getServiceOrderById(serviceOrderDO.getParentId());
        if (ObjectUtil.isNotNull(parentServiceOrder)) {
            if ("SIV".equals(parentServiceOrder.getType())) {
                String type = servicePackageDao.getById(serviceOrderDto.getServicePackageId()).getType();
                if ("ROI".equals(type) || "EOI".equals(type) || "VA".equals(type)) {
                    serviceOrderDto.setCreateVisaOffice(true);
                } else {
                    serviceOrderDto.setCreateVisaOffice(false);
                }
            }
        }
        //判断是否提交mm资料
        if (customerInformationDAO.getByServiceOrderId(serviceOrderDO.getId()) != null) {
            serviceOrderDto.setSubmitMM(true);
        } else
            serviceOrderDto.setSubmitMM(false);
        // EOI数量排序
        if (serviceOrderDto.getEOINumber() != null && serviceOrderDto.getApplicantParentId() > 0) {
//            Integer eoiNumber = serviceOrderDao.getServiceOrderById(serviceOrderDto.getApplicantParentId()).getEOINumber();
            List<ServiceOrderDTO> ziOrder = serviceOrderDao.getZiOrder(serviceOrderDto.getApplicantParentId());
            List<ServiceOrderDTO> collect = ziOrder.stream().filter(ServiceOrderDTO -> ServiceOrderDTO.getEOINumber() != null).collect(Collectors.toList());
            serviceOrderDto.setSortEOI(serviceOrderDto.getEOINumber() + "/" + collect.size());
        }
        // 添加父订单EOI绑定标识
        if ("SIV".equals(serviceOrderDto.getType())) {
            List<ServicePackageListDO> list = servicePackageDao.list(serviceOrderDto.getServiceId(), 0, 200);
            list.forEach(e->{
                if ("EOI".equals(e.getType())) {
                    List<ChildrenServiceOrderDTO> childrenServiceOrders = serviceOrderDto.getChildrenServiceOrders();
                    ChildrenServiceOrderDTO childrenServiceOrderDTO = new ChildrenServiceOrderDTO();
                    childrenServiceOrderDTO.setServicePackageId(e.getId());
                    childrenServiceOrderDTO.setServicePackageType(e.getType());
                    if (childrenServiceOrderDTO.getId() != 0) {
                        childrenServiceOrders.add(childrenServiceOrderDTO);
                    }
                    serviceOrderDto.setChildrenServiceOrders(childrenServiceOrders);
                }
            });
        }
        // 判断offer文件路径是否为多个
        String offerUrl = serviceOrderDto.getOfferUrl();
        if (StringUtil.isNotEmpty(offerUrl)) {
            List<String> list = Arrays.asList(offerUrl.split(","));
            serviceOrderDto.setOfferUrls(list);
        }
        // 留学订单添加签证信息
        if ("OVST".equals(serviceOrderDto.getType())) {
            CommissionOrderTempDO commissionOrderTempByServiceOrderId = commissionOrderTempDAO.getCommissionOrderTempByServiceOrderId(serviceOrderDto.getId());
            if (ObjectUtil.isNotNull(commissionOrderTempByServiceOrderId)) {
                serviceOrderDto.setVisaStatus(commissionOrderTempByServiceOrderId.getVisaStatus());
                serviceOrderDto.setVisaCertificate(commissionOrderTempByServiceOrderId.getVisaCertificate());
                serviceOrderDto.setVisaStatusSub(commissionOrderTempByServiceOrderId.getVisaStatusSub());
            }
        }
        // 是否购买过保险
        if ("1".equals(serviceOrderDto.getIsInsuranceCompany())) {
            ServiceOrderInsuranceDO serviceOrderInsuranceDO = insuranceCompanyDAO.listServiceOrderInsuranceDOByServiceOrderId(serviceOrderDto.getId());
            if (ObjectUtil.isNotNull(serviceOrderInsuranceDO)) {
                List<InsuranceCompanyDO> list = insuranceCompanyDAO.list(serviceOrderInsuranceDO.getInsuranceCompanyId(), true, 0, 1);
                if (list != null && list.size() > 0) {
                    serviceOrderDto.setInsuranceCompanyDO(list.get(0));
                }
            }
        } else if ("0".equals(serviceOrderDto.getIsInsuranceCompany())) {
            serviceOrderDto.setIsInsuranceCompany("0");
        } else {
            serviceOrderDto.setIsInsuranceCompany("");
        }
        // 顾问以及文案资料大小合计
        Long officialDataSize =  cloudDiskFileDAO.listByOfficialId(serviceOrderDto.getOfficialId(), serviceOrderDto.getUserId());
        Long adviserDataSize = cloudDiskFileDAO.listByAdviserId(serviceOrderDto.getAdviserId(), serviceOrderDto.getUserId());

        serviceOrderDto.setAdviserDataSize(adviserDataSize);
        serviceOrderDto.setOfficialDataSize(officialDataSize);

        return serviceOrderDto;
    }

    private ApplicantDTO buildApplicant(ApplicantDTO applicantDto, Integer serviceOrderId, String notCloud,
                                        String information) {
        if (applicantDto == null)
            return applicantDto;
        List<ServiceOrderApplicantDO> serviceOrderApplicantDoList = serviceOrderApplicantDao.list(serviceOrderId,
                applicantDto.getId());
        if (serviceOrderApplicantDoList != null && serviceOrderApplicantDoList.size() > 0
                && serviceOrderApplicantDoList.get(0) != null) {
            applicantDto.setUrl(serviceOrderApplicantDoList.get(0).getUrl());
            applicantDto.setContent(serviceOrderApplicantDoList.get(0).getContent());
        }
        if (StringUtil.isEmpty(applicantDto.getUrl()))
            applicantDto.setUrl(notCloud);
        if (StringUtil.isEmpty(applicantDto.getContent()))
            applicantDto.setContent(information);
        return applicantDto;
    }

    private int stateDecision(String state) {
        int stateValue = 0;
        switch (state) {
            case "OREVIEW":
                stateValue = 1;
                break;
            case "APPLY":
                stateValue = 1;
                break;
            case "COMPLETE":
                stateValue = 1;
                break;
            case "FINISH":
                stateValue = 1;
                break;
            case "CLOSE":
                stateValue = 1;
                break;
            case "RECEIVED":
                stateValue = 1;
                break;
            case "PAID":
                stateValue = 1;
                break;
            case "WAIT":
                stateValue = 1;
                break;
            case "APPLY_FAILED":
                stateValue = 1;
                break;
            case "COMPLETE_FD":
                stateValue = 1;
                break;
            default:
                stateValue = 0;
        }
        return stateValue;
    }

    private void sendEmailOfUpdateOfficial(ServiceOrderDO serviceOrderDo, ServiceOrderDO _serviceOrderDo) {
        ServiceOrderManageServiceImpl.ServiceOrderMailDetail serviceOrderMailDetail = getServiceOrderMailDetail(serviceOrderDo, "任务提醒:");
        AdviserDO adviserDo = adviserDao.getAdviserById(serviceOrderDo.getAdviserId());
        OfficialDO officialDo = officialDao.getOfficialById(serviceOrderDo.getOfficialId());
        OfficialDO _officialDo = officialDao.getOfficialById(_serviceOrderDo.getOfficialId());
        if (ObjectUtil.orIsNull(adviserDo, officialDo, _officialDo))
            return;
        ApplicantDTO applicantDto = null;
        if (serviceOrderDo.getApplicantId() > 0)
            applicantDto = mapper.map(applicantDao.getById(serviceOrderDo.getApplicantId()), ApplicantDTO.class);
        applicantDto = buildApplicant(applicantDto, serviceOrderDo.getId(), serviceOrderDo.getNutCloud(),
                serviceOrderDo.getInformation());
        Date date = serviceOrderDo.getGmtCreate();
        if (!"PENDING".equalsIgnoreCase(serviceOrderDo.getState()))
            sendMail(adviserDo.getEmail(), "变更任务提醒:",
                    StringUtil.merge("亲爱的:", adviserDo.getName(), "<br/>", "您的订单已经变更。", "<br>订单号:",
                            serviceOrderDo.getId(), "<br/>申请人名称:", getApplicantName(applicantDto), "<br/>顾问:",
                            adviserDo.getName(), "<br/>文案:", officialDo.getName(), "<br/>属性:",
                            getPeopleTypeStr(serviceOrderDo.getPeopleType()), "<br/>坚果云资料地址:", applicantDto.getUrl(),
                            "<br/>客户基本信息:", applicantDto.getContent(), "<br/>备注:", serviceOrderDo.getRemarks(),
                            "<br/>驳回原因:", serviceOrderDo.getRefuseReason(), "<br/>创建时间:", date, "<br/>",
                            serviceOrderMailDetail.getServiceOrderUrl()));
        if ("VISA".equalsIgnoreCase(serviceOrderDo.getType())
                && !"PENDING".equalsIgnoreCase(serviceOrderDo.getState())) {
            if (_serviceOrderDo.getMaraId() > 0 && serviceOrderDo.getMaraId() > 0
                    && _serviceOrderDo.getMaraId() != serviceOrderDo.getMaraId()) {
                MaraDO maraDo = maraDao.getMaraById(serviceOrderDo.getMaraId());
                MaraDO _maraDo = maraDao.getMaraById(_serviceOrderDo.getMaraId());
                sendMail(maraDo.getEmail(), "新任务提醒:",
                        StringUtil.merge("亲爱的:", maraDo.getName(), "<br/>", "您有一条新的服务订单任务请及时处理。", "<br>订单号:",
                                serviceOrderDo.getId(), "<br/>服务类型:签证/申请人名称:", getApplicantName(applicantDto), "/顾问:",
                                adviserDo.getName(), "/文案:", officialDo.getName(), "/MARA:", maraDo.getName(),
                                "<br/>属性:", getPeopleTypeStr(serviceOrderDo.getPeopleType()), "<br/>坚果云资料地址:",
                                applicantDto.getUrl(), "<br/>客户基本信息:", applicantDto.getContent(), "<br/>备注:",
                                serviceOrderDo.getRemarks(), "<br/>驳回原因:", serviceOrderDo.getRefuseReason(),
                                "<br/>创建时间:", date, "<br/>", serviceOrderMailDetail.getServiceOrderUrl()));
                sendMail(_maraDo.getEmail(), "变更任务提醒:", StringUtil.merge("亲爱的", _maraDo.getName(), ":<br/>", "您有的订单号:",
                        serviceOrderDo.getId(), "已从您这更改为Mara:", maraDo.getName()));
            }
        }
        if (_serviceOrderDo.getOfficialId() > 0 && serviceOrderDo.getOfficialId() > 0
                && _serviceOrderDo.getOfficialId() != serviceOrderDo.getOfficialId()) {
            if (!"PENDING".equalsIgnoreCase(serviceOrderDo.getState()))
                sendMail(officialDo.getEmail() + ",maggie@zhinanzhen.org", "新任务提醒:",
                        StringUtil.merge("亲爱的", officialDo.getName(), ":<br/>", "您有一条新的服务订单任务请及时处理。", "<br/>订单号:",
                                serviceOrderDo.getId(), "<br/>服务类型:", serviceOrderMailDetail.getType(),
                                serviceOrderMailDetail.getDetail(), "/顾问:", adviserDo.getName(), "/文案:",
                                officialDo.getName(), "<br/>属性:", getPeopleTypeStr(serviceOrderDo.getPeopleType()),
                                "<br/>坚果云资料地址:", applicantDto.getUrl(), "<br/>申请人基本信息:", applicantDto.getContent(),
                                "<br/>备注:", serviceOrderDo.getRemarks(), "<br/>驳回原因:", serviceOrderDo.getRefuseReason(),
                                "<br/>创建时间:", date, "<br/>", serviceOrderMailDetail.getServiceOrderUrl()));
            if (StringUtil.equals("Retracted", serviceOrderDo.getStateMark2())) // 顾问提交审核后又撤回，提醒文案不同
                sendMail(_officialDo.getEmail() + ",maggie@zhinanzhen.org", "服务被撤回提醒:",
                        StringUtil.merge("亲爱的", _officialDo.getName(), ":<br/>您有一条服务订单已被撤回,如有服务相关问题请及时与顾问沟通<br/>订单号:",
                                serviceOrderDo.getId(), "<br/>服务类型:", serviceOrderMailDetail.getType(),
                                serviceOrderMailDetail.getDetail(), "<br/>顾问:", adviserDo.getName(), "<br/>申请人基本信息:",
                                applicantDto.getContent(), "<br/>坚果云资料地址:", applicantDto.getUrl(), "<br/>备注:",
                                serviceOrderDo.getRemarks(), "<br/>驳回原因:", serviceOrderDo.getRefuseReason(),
                                "<br/>创建时间:", date, "<br/>", serviceOrderMailDetail.getServiceOrderUrl()));
            else if (!"PENDING".equalsIgnoreCase(serviceOrderDo.getState()))
                sendMail(_officialDo.getEmail() + ",maggie@zhinanzhen.org", "变更任务提醒:",
                        StringUtil.merge("亲爱的", _officialDo.getName(), ":<br/>", "您有的订单号:", serviceOrderDo.getId(),
                                "已从您这更改为文案:", officialDo.getName()));
        }
    }

    private void sendEmailOfUpdateServiceId(ServiceOrderDO serviceOrderDo, ServiceOrderDO _serviceOrderDo) {
        ServiceOrderManageServiceImpl.ServiceOrderMailDetail serviceOrderMailDetail = getServiceOrderMailDetail(serviceOrderDo, "任务提醒:");
        AdviserDO adviserDo = adviserDao.getAdviserById(serviceOrderDo.getAdviserId());
        OfficialDO _officialDo = officialDao.getOfficialById(_serviceOrderDo.getOfficialId());
        ApplicantDTO applicantDto = null;
        if (serviceOrderDo.getApplicantId() > 0)
            applicantDto = mapper.map(applicantDao.getById(serviceOrderDo.getApplicantId()), ApplicantDTO.class);
        applicantDto = buildApplicant(applicantDto, serviceOrderDo.getId(), serviceOrderDo.getNutCloud(),
                serviceOrderDo.getInformation());
        Date date = serviceOrderDo.getGmtCreate();
        if (_serviceOrderDo.getServiceId() > 0 && serviceOrderDo.getServiceId() > 0
                && _serviceOrderDo.getServiceId() != serviceOrderDo.getServiceId()) {
            if (StringUtil.equals("Retracted", serviceOrderDo.getStateMark2()) && serviceOrderDo.isSubmitted()) { // 给会计发邮件
                int regionId = adviserDo.getRegionId();
                if (regionId > 0) {
                    List<KjDO> kjList = kjDao.listKjByRegionId(regionId);
                    if (kjList != null && kjList.size() > 0) {
                        String kjEmails = "candice.huang@zhinanzhen.org,"; //　所有会计邮件都同时发给Candice
                        for (KjDO kjDo : kjList)
                            kjEmails += kjDo.getEmail() + ",";
                        if ("VISA".equalsIgnoreCase(serviceOrderDo.getType())) {
                            List<VisaDO> visaList = visaDao.listVisaByServiceOrderId(serviceOrderDo.getId());
                            if (visaList != null && visaList.size() > 0) {
                                String visaIds = "";
                                for (VisaDO visaDo : visaList)
                                    visaIds += visaDo.getId() + ",";
                                sendMail(kjEmails, "服务项目变更提醒:签证",
                                        StringUtil.merge("亲爱的会计", ":<br/>", "佣金订单关联服务项目已发生变更，如有问题请与顾问联系．<br/>佣金订单编号:",
                                                visaIds, "<br/>申请人基本信息:", applicantDto.getContent(), "<br/>顾问:",
                                                adviserDo.getName(), "<br/>文案:", _officialDo.getName(), "<br/>坚果云资料地址:",
                                                applicantDto.getUrl(), "<br/>备注:", serviceOrderDo.getRemarks(),
                                                "<br/>驳回原因:", serviceOrderDo.getRefuseReason(), "<br/>创建时间:", date,
                                                "<br/>", serviceOrderMailDetail.getServiceOrderUrl()));
                            }
                        }
                        if ("OVST".equalsIgnoreCase(serviceOrderDo.getType())) {
                            List<CommissionOrderDO> commissionOrderlist = commissionOrderDao
                                    .listCommissionOrderByServiceOrderId(serviceOrderDo.getId());
                            if (commissionOrderlist != null && commissionOrderlist.size() > 0) {
                                String commissionOrderIds = "";
                                for (CommissionOrderDO commissionOrderDo : commissionOrderlist)
                                    commissionOrderIds += commissionOrderDo.getId() + ",";
                                sendMail(kjEmails, "服务项目变更提醒:留学", StringUtil.merge("亲爱的会计", ":<br/>",
                                        "佣金订单关联服务项目已发生变更，如有问题请与顾问联系．<br/>佣金订单编号:", commissionOrderIds, "<br/>申请人基本信息:",
                                        applicantDto.getContent(), "<br/>顾问:", adviserDo.getName(), "<br/>文案:",
                                        _officialDo.getName(), "<br/>坚果云资料地址:", applicantDto.getUrl(), "<br/>备注:",
                                        serviceOrderDo.getRemarks(), "<br/>驳回原因:", serviceOrderDo.getRefuseReason(),
                                        "<br/>创建时间:", date, "<br/>", serviceOrderMailDetail.getServiceOrderUrl()));
                            }
                        }
                    }
                }
            }
        }
    }

    private String getPeopleTypeStr(String peopleType) {
        if ("1A".equalsIgnoreCase(peopleType))
            return "单人";
        else if ("1B".equalsIgnoreCase(peopleType))
            return "单人提配偶";
        else if ("2A".equalsIgnoreCase(peopleType))
            return "带配偶";
        else if ("XA".equalsIgnoreCase(peopleType))
            return "带孩子";
        else if ("XB".equalsIgnoreCase(peopleType))
            return "带配偶孩子";
        else if ("XC".equalsIgnoreCase(peopleType))
            return "其它";
        else
            return "未知";
    }

    @Data
    class ServiceOrderMailDetail {
        String title = "";
        String type = "";
        String detail = "";
        String serviceOrderUrl = "";
        UserDO user;
    }

    private ServiceOrderManageServiceImpl.ServiceOrderMailDetail getServiceOrderMailDetail(ServiceOrderDO serviceOrderDo, String title) {
        ServiceOrderManageServiceImpl.ServiceOrderMailDetail serviceOrderMailDetail = new ServiceOrderManageServiceImpl.ServiceOrderMailDetail();
        String type = "";
        String detail = "";
        serviceOrderMailDetail.setServiceOrderUrl(
                "<br/><a href='https://yongjinbiao.zhinanzhen.org/webroot_new/serviceorderdetail/id?"
                        + serviceOrderDo.getId() + "'>服务订单详情</a>");
        UserDO user = userDao.getUserById(serviceOrderDo.getUserId());
        ApplicantDTO applicantDto = null;
        if (serviceOrderDo.getApplicantId() > 0)
            applicantDto = mapper.map(applicantDao.getById(serviceOrderDo.getApplicantId()), ApplicantDTO.class);
        if ("VISA".equalsIgnoreCase(serviceOrderDo.getType())) {
            type = "签证";
            if (applicantDto != null)
                detail += "/申请人名称:" + getApplicantName(applicantDto);
            ServiceDO service = serviceDao.getServiceById(serviceOrderDo.getServiceId());
            if (service != null) {
                detail += "/类型:" + service.getName() + "(" + service.getCode() + ")";
                String servicePackageType = "";
                if (serviceOrderDo.getServicePackageId() != 0 && serviceOrderDo.getServicePackageId() > 0) {
                    ServicePackageDO servicePackageDO = servicePackageDao.getById(serviceOrderDo.getServicePackageId());
                    String typeTmp = servicePackageDO.getType();
                    servicePackageType = "-" + typeTmp;
                    if ("独立技术移民".equalsIgnoreCase(service.getName()) && "EOI".equals(typeTmp)) {
                        ServiceDO serviceById = serviceDao.getServiceById(servicePackageDO.getServiceId());
                        servicePackageType += "-" + serviceById.getCode();
                    }
                }
                type += "(" + service.getCode() + servicePackageType + ")";

                if (serviceOrderDo.getServicePackageId() > 0) {
                    ServicePackageDO servicePackageDo = servicePackageDao.getById(serviceOrderDo.getServicePackageId());
                    if (servicePackageDo != null) {
                        String _type = servicePackageDo.getType();
                        if ("CA".equals(_type))
                            detail += " - 职业评估";
                        if ("EOI".equals(_type)) {
                            detail += " - EOI";
                            if ("独立技术移民".equalsIgnoreCase(service.getName())) {
                                ServiceDO serviceById = serviceDao.getServiceById(servicePackageDo.getServiceId());
                                detail += " - " + serviceById.getCode();
                            }
                        }
                        if ("SA".equals(_type))
                            detail += " - 学校申请";
                        if ("VA".equals(_type))
                            detail += " - 签证申请";
                        if ("ZD".equals(_type))
                            detail += " - 州担";
                        if ("TM".equals(_type))
                            detail += " - 提名";
                        if ("DB".equals(_type))
                            detail += " - 担保";
                    }
                }
                if (StringUtil.isNotEmpty(serviceOrderDo.getServiceAssessId())) {
                    ServiceAssessDO serviceAssessDo = serviceAssessDao
                            .seleteAssessById(serviceOrderDo.getServiceAssessId());
                    if (serviceAssessDo != null)
                        detail = StringUtil.merge(detail, " - ", serviceAssessDo.getName());
                }
            }
            title += getApplicantName(applicantDto) + "/" + type;
            if (StringUtil.isNotEmpty(serviceOrderDo.getUrgentState()))
                title += StringUtil.merge("[", getUrgentStateName(serviceOrderDo.getUrgentState()), "]");
        } else if ("OVST".equalsIgnoreCase(serviceOrderDo.getType())) {
            type = "留学";
            if (applicantDto != null)
                detail += "/申请人名称:" + getApplicantName(applicantDto);
            SchoolDO school = schoolDao.getSchoolById(serviceOrderDo.getSchoolId());
            if (school != null) {
                detail += "/学校:" + school.getName();
                detail += "/专业:" + school.getSubject();
            }
            title += getApplicantName(applicantDto) + "/" + type;
        } else if ("SIV".equalsIgnoreCase(serviceOrderDo.getType())) {
            type = "独立技术移民";
            title += getApplicantName(applicantDto) + "/" + type;
        } else if ("NSV".equalsIgnoreCase(serviceOrderDo.getType())) {
            type = "雇主担保";
            title += getApplicantName(applicantDto) + "/" + type;
        } else if ("MT".equalsIgnoreCase(serviceOrderDo.getType())) {
            type = "曼拓";
            title += getApplicantName(applicantDto) + "/" + type;
        } else if ("ZX".equalsIgnoreCase(serviceOrderDo.getType())) {
            type = "咨询";
            title += getApplicantName(applicantDto) + "/" + type;
        }
        serviceOrderMailDetail.setTitle(title);
        serviceOrderMailDetail.setType(type);
        serviceOrderMailDetail.setDetail(detail);
        serviceOrderMailDetail.setUser(user);
        return serviceOrderMailDetail;
    }

    private String getUrgentStateName(String urgentState) {
        if ("JJ".equalsIgnoreCase(urgentState))
            return "加急";
        if ("TSJJ".equalsIgnoreCase(urgentState))
            return "特殊加急";
        return "";
    }

    private Integer firstPlace(Integer id) {
        // 1. 将int转换为String
        String numberStr = String.valueOf(id);

        // 2. 找到第一个不是负号的字符（即第一个数字）
        char firstChar = numberStr.charAt(0);
        int firstDigit;

        if (firstChar == '-') {
            // 如果是负数，则取第二个字符
            firstDigit = Character.getNumericValue(numberStr.charAt(1));
        } else {
            // 如果是正数，直接取第一个字符
            firstDigit = Character.getNumericValue(firstChar);
        }
        return firstDigit;
    }
}
