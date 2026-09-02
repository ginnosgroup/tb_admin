package org.zhinanzhen.b.service.impl;

import com.ikasoa.core.ErrorCodeEnum;
import com.ikasoa.core.utils.ListUtil;
import com.ikasoa.core.utils.ObjectUtil;
import com.ikasoa.core.utils.StringUtil;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.zhinanzhen.b.config.GlobalThreadPool;
import org.zhinanzhen.b.config.ServiceOrderBatchContext;
import org.zhinanzhen.b.config.ServiceOrderBatchLoader;
import org.zhinanzhen.b.dao.*;
import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.service.ServiceOrderManageService;
import org.zhinanzhen.b.service.VisaOfficialService;
import org.zhinanzhen.b.service.pojo.*;
import org.zhinanzhen.b.service.pojo.ant.Sorter;
import org.zhinanzhen.tb.controller.Response;
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
import java.util.function.Function;
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
    private ReviewAIDAO reviewAIDAO;

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

    @Resource
    private WebLogDAO webLogDAO;

    private ServiceOrderBatchLoader batchLoader;

    private ServiceOrderBatchLoader getBatchLoader() {
        if (batchLoader == null) {
            batchLoader = new ServiceOrderBatchLoader(
                    serviceOrderDao, schoolDao, subagencyDao, serviceDao,
                    servicePackageDao, receiveTypeDao, userDao, applicantDao,
                    maraDao, adviserDao, officialDao, officialTagDao,
                    visaDao, serviceAssessDao, regionDAO, commissionOrderTempDao,
                    schoolCourseDAO, schoolInstitutionDAO, schoolInstitutionLocationDAO,
                    customerInformationDAO, serviceOrderApplicantDao, officialHandoverLogDao,
                    servicePackagePriceDAO, serviceOrderManageDAO, webLogDAO, reviewAIDAO,
                    mailRemindDAO
            );
        }
        return batchLoader;
    }

    private ServiceOrderBatchContext batchLoadRelatedData(List<ServiceOrderDO> orders) {
        return getBatchLoader().batchLoadRelatedData(orders);
    }

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
            List<Integer> serviceOrderManageIds = serviceOrderManageDAO.listserviceOrderManage(theDateTo00_00_00(startMaraApprovalDate), theDateTo23_59_59(endMaraApprovalDate), type, excludeTypeList, excludeState, stateList,
                    auditingState, reviewStateList, urgentState, null, null,
                    theDateTo00_00_00(startOfficialApprovalDate), theDateTo23_59_59(endOfficialApprovalDate), theDateTo00_00_00(startReadcommittedDate),
                    theDateTo23_59_59(endReadcommittedDate), theDateTo00_00_00(startFinishDate), theDateTo23_59_59(endFinishDate), adviserRegionIdList, officialRegionIdList, userId, userName, applicantName, maraId, adviserId, officialId, officialTagId,
                    parentId, applicantParentId, isNotApproved, serviceId, servicePackageId, schoolId, isPay, isSettle,null, pageNum * pageSize, pageSize, orderBy, courseId, tradingName, schoolLocation);

            for (Integer serviceOrderManageId : serviceOrderManageIds) {
                ServiceOrderDO serviceOrderById = serviceOrderManageDAO.getServiceOrderById(serviceOrderManageId);
                UserDO userById = userDao.getUserById(serviceOrderById.getUserId());
                if (userById != null) {
                    serviceOrderById.setUserDO(userById);
                }
                AdviserDO adviserById = adviserDao.getAdviserById(serviceOrderById.getAdviserId());
                if (adviserById != null) {
                    serviceOrderById.setAdviserDO(adviserById);
                }
                serviceOrderDoList.add(serviceOrderById);
            }
            long count = 0L;
            CountDownLatch latch = new CountDownLatch(serviceOrderDoList.size());
            // 批量预加载关联数据
            ServiceOrderBatchContext batchContextParent = batchLoadRelatedData(serviceOrderDoList);
            for (int i = 0; i < serviceOrderDoList.size(); i++) {
                ServiceOrderDO serviceOrderDo = serviceOrderDoList.get(i);
                List<ServiceOrderDO> serviceOrderSubs = serviceOrderManageDAO.listSub(serviceOrderDo.getId());
                ThreadPoolExecutor executor = GlobalThreadPool.getInstance();
                executor.submit(() -> {
                    try {
                        ServiceOrderDTO serviceOrderDTO = putServiceOrderDTO(serviceOrderDo, batchContextParent);
                        if (serviceOrderSubs != null) {
                            List<ServiceOrderDTO> serviceOrderSubsT = new ArrayList<>();
                            // 批量预加载关联数据
                            ServiceOrderBatchContext batchContext = batchLoadRelatedData(serviceOrderSubs);
                            for (ServiceOrderDO serviceOrderSub : serviceOrderSubs) {
                                setSubOrderDistributableAmount(serviceOrderSub, batchContext);
                                ServiceOrderDTO serviceOrderDTO1 = putServiceOrderDTO(serviceOrderSub, batchContext);
                                VisaDO visaByServiceOrderId = visaDAO.getFirstVisaByServiceOrderId(serviceOrderSub.getId());
                                if (visaByServiceOrderId != null || !"PENDING".equalsIgnoreCase(serviceOrderSub.getState())) {
                                    serviceOrderDTO.setVisaBuild(true);
                                }
                                serviceOrderSubsT.add(serviceOrderDTO1);
                            }
                            serviceOrderDTO.setSubServiceOrders(serviceOrderSubsT);
                        }
                        serviceOrderDtoList.add(serviceOrderDTO);
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
            ServiceOrderDO serviceOrderDo = null;
            if (i == 1) {
                ServiceOrderAndManage serviceOrderAndManageById = serviceOrderManageDAO.getServiceOrderAndManageById(id);
                if (serviceOrderAndManageById != null) {
                    serviceOrderDo = serviceOrderManageDAO.getServiceOrderById(serviceOrderAndManageById.getServiceOrderManageId());
                }
            } else {
                serviceOrderDo = serviceOrderManageDAO.getServiceOrderById(id);
            }
            if (serviceOrderDo == null)
                return null;
            List<ServiceOrderDO> serviceOrderDOS = serviceOrderManageDAO.listSub(serviceOrderDo.getId());
//            serviceOrderDOS.add(serviceOrderDo);
            // 批量预加载关联数据
            ServiceOrderBatchContext batchContext = batchLoadRelatedData(serviceOrderDOS);
            if (serviceOrderDOS != null) {
                for (ServiceOrderDO serviceOrderDO : serviceOrderDOS) {
                    subServiceOrderDtos.add(putServiceOrderDTO(serviceOrderDO, batchContext));
                }
            }
            serviceOrderDo.setDistributableAmount(serviceOrderDo.getReceivable());
            serviceOrderDto = putServiceOrderDTO(serviceOrderDo, batchContext);
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

    /**
     * 计算子订单可分配金额：子订单 receivable * 0.6 - 服务成本价 - 绑定订单 receivable。
     * 服务成本价和绑定订单金额均由批量上下文预加载，避免在子订单循环中产生 N+1 查询。
     */
    private void setSubOrderDistributableAmount(ServiceOrderDO serviceOrderSub, ServiceOrderBatchContext batchContext) {
        ServicePackagePriceDO servicePackagePrice = batchContext.servicePackagePriceMap.get(serviceOrderSub.getServiceId());
        double costPrice = servicePackagePrice == null ? 0D : servicePackagePrice.getCostPrince();
        double bindingOrderReceivable = batchContext.bindingOrderReceivableMap
                .getOrDefault(serviceOrderSub.getId(), 0D);
        double distributableAmount = serviceOrderSub.getReceivable() * 0.6D - costPrice - bindingOrderReceivable;
        serviceOrderSub.setDistributableAmount(roundHalfUp2(distributableAmount));
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
        return serviceOrderManageDAO.updateServiceOrder(mapper.map(serviceorderManageDto, ServiceOrderDO.class));
    }

    @Override
    public ServiceOrderDTO getserviceOrderManageByServiceOrderId(int id) {
        ServiceOrderAndManage serviceOrderAndManageById = serviceOrderManageDAO.getServiceOrderAndManageById(id);
        if (serviceOrderAndManageById != null) {
            return mapper.map(serviceOrderManageDAO.getServiceOrderById(serviceOrderAndManageById.getServiceOrderManageId()), ServiceOrderDTO.class);
        }
        return null;
    }

    @Override
    public List<ServiceOrderDTO> listChildrenServiceOrder(int id) {
        return serviceOrderManageDAO.listChildrenServiceOrder(id);
    }

    @Override
    public ServiceOrderDTO getServiceOrderBySubId(Integer subId) throws ServiceException {
        if (subId <= 0) {
            ServiceException se = new ServiceException("service order id error !");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        ServiceOrderDTO serviceOrderDto = null;
        List<ServiceOrderDTO> subServiceOrderDtos = new ArrayList<>();
        try {
            ServiceOrderAndManage serviceOrderAndManageById = serviceOrderManageDAO.getServiceOrderAndManageById(subId);
            if (serviceOrderAndManageById != null) {
                ServiceOrderDO serviceOrderDo = serviceOrderManageDAO.getServiceOrderById(serviceOrderAndManageById.getServiceOrderManageId());
                List<ServiceOrderDO> serviceOrderDOS = serviceOrderManageDAO.listSub(serviceOrderDo.getId());
                serviceOrderDOS.add(serviceOrderDo);
                // 批量预加载关联数据
                ServiceOrderBatchContext batchContext = batchLoadRelatedData(serviceOrderDOS);
                if (serviceOrderDOS != null) {
                    for (ServiceOrderDO serviceOrderDO : serviceOrderDOS) {
                        subServiceOrderDtos.add(putServiceOrderDTO(serviceOrderDO, batchContext));
                    }
                }
                if (serviceOrderDo == null)
                    return null;
                serviceOrderDo.setDistributableAmount(serviceOrderDo.getReceivable());
                serviceOrderDto = putServiceOrderDTO(serviceOrderDo, batchContext);
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
            }
//            if (serviceOrderAndManageById != null) {
//                ServiceOrderDO serviceOrderById = serviceOrderManageDAO.getServiceOrderById(serviceOrderAndManageById.getServiceOrderManageId());
//                List<ServiceOrderDTO> serviceOrderDTOS = serviceOrderManageDAO.listChildrenServiceOrder(serviceOrderById.getId());
//                if (serviceOrderDTOS != null && !serviceOrderDTOS.isEmpty()) {
//                    List<ServiceOrderDO> serviceOrderDOS = serviceOrderDTOS.stream().map(dto -> {
//                        ServiceOrderDO serviceOrderDO = new ServiceOrderDO();
//                        BeanUtils.copyProperties(dto, serviceOrderDO);
//                        return serviceOrderDO;
//                    }).collect(Collectors.toList());
//                    serviceOrderById.setSubServiceOrders(serviceOrderDOS);
//                }
//            }

        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
        return serviceOrderDto;

    }

    @Override
    public String deleteServiceOrderById(List<Integer> idList) {
        if (idList == null || idList.isEmpty()) {
            return "参数错误";
        }
        for (Integer id : idList) {
            Integer i1 = firstPlace(id);
            if (i1 > 1) {
                List<ServiceOrderDTO> serviceOrderDTOS = serviceOrderManageDAO.listChildrenServiceOrder(id);
                for (ServiceOrderDTO serviceOrderDTO : serviceOrderDTOS) {
                    VisaDO firstVisaByServiceOrderId = visaDAO.getFirstVisaByServiceOrderId(serviceOrderDTO.getId());
                    if (!"PENDING".equalsIgnoreCase(serviceOrderDTO.getState()) || firstVisaByServiceOrderId != null) {
                        return"该订单的子订单已有流程在进行中，不允许删除主订单操作。";
                    }
                }
                serviceOrderManageDAO.deleteServiceOrderById(id);
            }
            if (i1 == 1) {
                ServiceOrderAndManage serviceOrderAndManageById = serviceOrderManageDAO.getServiceOrderAndManageById(id);
                if (serviceOrderAndManageById == null) {
                    return "该订单不是多订单类型，请核实";
                }
                ServiceOrderDO serviceOrderManageDAOServiceOrderById = serviceOrderManageDAO.getServiceOrderById(serviceOrderAndManageById.getServiceOrderManageId());
                ServiceOrderDO serviceOrderById = serviceOrderDao.getServiceOrderById(id);
                VisaDO visaByServiceOrderId = visaDao.getVisaByServiceOrderId(serviceOrderById.getId());
                if (visaByServiceOrderId != null || !"PENDING".equalsIgnoreCase(serviceOrderById.getState())) {
                    return "该订单已生成佣金订单，不能删除，请核实";
                }
                if (serviceOrderById.getApplicantParentId() == 0) {
                    serviceOrderManageDAOServiceOrderById.setReceivable(serviceOrderManageDAOServiceOrderById.getReceivable() - serviceOrderById.getReceivable());
                    serviceOrderManageDAOServiceOrderById.setReceived(serviceOrderManageDAOServiceOrderById.getReceived() - serviceOrderById.getReceived());
                    serviceOrderManageDAOServiceOrderById.setAmount(serviceOrderManageDAOServiceOrderById.getAmount() - serviceOrderById.getAmount());
                    serviceOrderManageDAOServiceOrderById.setGst(serviceOrderManageDAOServiceOrderById.getGst() - serviceOrderById.getGst());
                    serviceOrderManageDAOServiceOrderById.setDeductGst(serviceOrderManageDAOServiceOrderById.getDeductGst() - serviceOrderById.getDeductGst());
                    serviceOrderManageDAOServiceOrderById.setBonus(serviceOrderManageDAOServiceOrderById.getBonus() - serviceOrderById.getBonus());
                    serviceOrderManageDAOServiceOrderById.setExpectAmount(serviceOrderManageDAOServiceOrderById.getExpectAmount() - serviceOrderById.getExpectAmount());
                    serviceOrderManageDAOServiceOrderById.setPerAmount(serviceOrderManageDAOServiceOrderById.getPerAmount() - serviceOrderById.getPerAmount());
                    serviceOrderManageDAO.updateServiceOrder(serviceOrderManageDAOServiceOrderById);
                }
                serviceOrderDao.deleteServiceOrderById(id);
                if (ObjectUtil.isNotNull(serviceOrderById) && serviceOrderById.getEOINumber() != null) {
                    List<ServiceOrderDTO> ziOrder = serviceOrderDao.getDeriveOrder(serviceOrderById.getApplicantParentId());
                    List<ServiceOrderDTO> collect = ziOrder.stream()
                            .filter(order -> order.getEOINumber() != null) // 过滤掉EOINumber为null的对象
                            .sorted(Comparator.comparing(ServiceOrderDTO::getEOINumber)) // 对剩余对象进行排序
                            .collect(Collectors.toList()); // 收集结果
                    for (int i = 0; i < collect.size(); i++) {
                        collect.get(i).setEOINumber(i + 1);
                        ServiceOrderDO map = mapper.map(collect.get(i), ServiceOrderDO.class);
                        serviceOrderDao.updateServiceOrder(map);
                    }
                    // 删除订单如果是打包EOI最后一个订单
                    int EOICount = 0;
                    List<VisaOfficialDO> visaOfficialDOS = new ArrayList<>();
                    ServiceOrderDO serviceOrderParentById = serviceOrderDao.getServiceOrderById(serviceOrderById.getApplicantParentId());
                    for (ServiceOrderDTO e : collect) {
                        VisaOfficialDO byServiceOrderId = visaOfficialDao.getByServiceOrderId(e.getId());
                        visaOfficialDOS.add(byServiceOrderId);
                        if (ObjectUtil.isNotNull(byServiceOrderId)) {
                            EOICount++;
                        }
                    }
                    if (ObjectUtil.isNotNull(serviceOrderParentById) && EOICount == (serviceOrderParentById.getEOINumber() - 1)) {
                        ServicePackagePriceDO byServiceId = servicePackagePriceDAO.getByServiceId(25);
                        VisaOfficialDO visaOfficialDO = visaOfficialDOS.stream().max(Comparator.comparing(VisaOfficialDO::getPredictCommission)).get();
                        visaOfficialDOS.remove(visaOfficialDO);
                        double pre = 0.00;
                        for (VisaOfficialDO e : visaOfficialDOS) {
                            Double predictCommission = e.getPredictCommissionAmount();
                            pre += (byServiceId.getMaxPrice() / EOICount) - predictCommission;
                        }
                        double rate = visaOfficialDO.getPredictCommission() / visaOfficialDO.getPredictCommissionAmount();
//                    double sum = visaOfficialDOS.stream().mapToDouble(VisaOfficialDO::getPredictCommissionAmount).sum();
                        double sum = 0.00;
                        double predictCommissionAmount = visaOfficialDO.getPredictCommissionAmount();
                        sum = predictCommissionAmount - (byServiceId.getMaxPrice() / serviceOrderParentById.getEOINumber()) + (byServiceId.getMaxPrice() / collect.size()) + pre;
                        visaOfficialDO.setPredictCommissionAmount(sum);
                        visaOfficialDO.setCommissionAmount(sum);
                        visaOfficialDO.setPredictCommission(sum * rate);
                        visaOfficialDO.setPredictCommissionCNY(visaOfficialDO.getPredictCommission() * visaOfficialDO.getExchangeRate());
                        visaOfficialDao.updateVisaOfficial(visaOfficialDO);
                    }
                }
            }
        }
        return "删除成功";
    }

    @Override
    public ServiceOrderAndManage getServiceOrderAndManageById(int id) {
        return serviceOrderManageDAO.getServiceOrderAndManageById(id);
    }

    public ServiceOrderDTO putServiceOrderDTO(ServiceOrderDO serviceOrderDO, ServiceOrderBatchContext ctx) {
        ServiceOrderDTO serviceOrderDto = mapper.map(serviceOrderDO, ServiceOrderDTO.class);
        //获取旧文案信息
        Integer oldOfficialId = ctx.oldOfficialMap.get(serviceOrderDto.getId());
        if (oldOfficialId != null) {
            serviceOrderDto.setOldOfficial(ctx.officialMap.get(oldOfficialId));
        }
        // 查询学校课程
        if (serviceOrderDto.getSchoolId() > 0) {
            SchoolDO schoolDo = ctx.schoolMap.get(serviceOrderDto.getSchoolId());
            if (schoolDo != null)
                serviceOrderDto.setSchool(mapper.map(schoolDo, SchoolDTO.class));
        }
        // 查询Subagency
        if (serviceOrderDto.getSubagencyId() > 0) {
            SubagencyDO subagencyDo = ctx.subagencyMap.get(serviceOrderDto.getSubagencyId());
            if (subagencyDo != null)
                serviceOrderDto.setSubagency(mapper.map(subagencyDo, SubagencyDTO.class));
        }
        // 查询服务
        ServiceDO serviceDo = ctx.serviceMap.get(serviceOrderDto.getServiceId());
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
                            ServiceAssessDO serviceAssessDO = ctx.serviceAssessMap.get(Integer.parseInt(k));
                            if (serviceAssessDO == null) {
                                serviceAssessDO = serviceAssessDao.seleteAssessById(k);
                            }
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
            ServicePackageDO servicePackageDAOById = ctx.servicePackageMap.get(serviceOrderDto.getServicePackageId());
            if (servicePackageDAOById == null) {
                servicePackageDAOById = servicePackageDao.getById(serviceOrderDto.getServicePackageId());
            }
            if (servicePackageDAOById != null && "EOI".equals(servicePackageDAOById.getType())) {
                ServicePackageDTO servicePackageDTO = servicePackageDao.getEOIService(serviceOrderDto.getServicePackageId());
                if (ObjectUtil.isNotNull(servicePackageDTO)) {
                    serviceOrderDto.setServicePackage(servicePackageDTO);
                }
            } else if (servicePackageDAOById != null) {
                serviceOrderDto.setServicePackage(mapper.map(servicePackageDAOById, ServicePackageDTO.class));
            }
        }
        // 查询收款方式
        ReceiveTypeDO receiveTypeDo = ctx.receiveTypeMap.get(serviceOrderDto.getReceiveTypeId());
        if (receiveTypeDo != null)
            serviceOrderDto.setReceiveType(mapper.map(receiveTypeDo, ReceiveTypeDTO.class));
        // 查询用户
        UserDO userDo = ctx.userMap.get(serviceOrderDto.getUserId());
        if (userDo != null) {
            UserDTO userDto = mapper.map(userDo, UserDTO.class);
            if (serviceOrderDto.getUserId() > 0 && serviceOrderDto.getApplicantId() >= 0) {
                List<ServiceOrderApplicantDO> serviceOrderApplicantList = ctx.serviceOrderApplicantMap.getOrDefault(serviceOrderDto.getId(), new ArrayList<>());
                List<ApplicantDTO> applicantDtoList = new ArrayList<>();
                serviceOrderApplicantList.forEach(serviceOrderApplicant -> {
                    ApplicantDO applicantDo = ctx.applicantMap.get(serviceOrderApplicant.getApplicantId());
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
            ApplicantDO applicantDo = ctx.applicantMap.get(serviceOrderDto.getApplicantId());
            if (applicantDo != null) {
                ApplicantDTO applicantDto = mapper.map(applicantDo, ApplicantDTO.class);
                applicantDto = buildApplicant(applicantDto, serviceOrderDO.getId(), serviceOrderDto.getNutCloud(),
                        serviceOrderDto.getInformation());
                serviceOrderDto.setApplicantId(applicantDto.getId());
                serviceOrderDto.setApplicant(applicantDto);
            }
        } else {
            // 当 applicantId 为 0 时（如父订单/管理订单），返回空对象避免前端空指针
            serviceOrderDto.setApplicant(new ApplicantDTO());
        }
        // 查询Mara
        MaraDO maraDo = ctx.maraMap.get(serviceOrderDto.getMaraId());
        if (maraDo != null)
            serviceOrderDto.setMara(mapper.map(maraDo, MaraDTO.class));
        // 查询顾问
        AdviserDO adviserDo = ctx.adviserMap.get(serviceOrderDto.getAdviserId());
        if (adviserDo != null) {
            RegionDO regionDO = ctx.regionMap.get(adviserDo.getRegionId());
            serviceOrderDto.setAdviser(mapper.map(adviserDo, AdviserDTO.class));
            if (regionDO != null)
                serviceOrderDto.getAdviser().setRegionName(regionDO.getName());
            serviceOrderDto.getAdviser().setRegionDo(regionDO);
        }
        // 查询顾问2
        if (serviceOrderDto.getAdviserId2() > 0) {
            AdviserDO adviserDo2 = ctx.adviserMap.get(serviceOrderDto.getAdviserId2());
            if (adviserDo2 != null)
                serviceOrderDto.setAdviser2(mapper.map(adviserDo2, AdviserDTO.class));
        }
        // 查询文案
        OfficialDO officialDo = ctx.officialMap.get(serviceOrderDto.getOfficialId());
        if (officialDo != null)
            serviceOrderDto.setOfficial(mapper.map(officialDo, OfficialDTO.class));
        // 查询文案Tag
        OfficialTagDO officialTagDo = ctx.officialTagMap.get(serviceOrderDto.getId());
        if (officialTagDo != null)
            serviceOrderDto.setOfficialTag(mapper.map(officialTagDo, OfficialTagDTO.class));
        // 查询子服务
        if (serviceOrderDto.getParentId() <= 0) {
            List<ChildrenServiceOrderDTO> childrenServiceOrderList = new ArrayList<>();
            List<ServiceOrderDO> list = ctx.childrenOrderMap.getOrDefault(serviceOrderDto.getId(), new ArrayList<>());
            list.forEach(serviceOrder -> {
                ChildrenServiceOrderDTO childrenServiceOrderDto = mapper.map(serviceOrder,
                        ChildrenServiceOrderDTO.class);
                ServicePackageDO servicePackageDo = ctx.servicePackageMap.get(childrenServiceOrderDto.getServicePackageId());
                if (servicePackageDo != null)
                    childrenServiceOrderDto.setServicePackageType(servicePackageDo.getType());
                childrenServiceOrderList.add(childrenServiceOrderDto);
            });
            serviceOrderDto.setChildrenServiceOrders(childrenServiceOrderList);
        } else {
            // 子订单没有下级服务，返回空数组而不是 null，避免后续 SIV 分支 add 时空指针
            serviceOrderDto.setChildrenServiceOrders(new ArrayList<>());
        }

        List<Integer> cIds = new ArrayList<>();
        List<VisaDO> visaList = new ArrayList<>();
        int visaLookupId = serviceOrderDO.getParentId() == 0 ? serviceOrderDO.getApplicantParentId() : serviceOrderDO.getParentId();
        if (serviceOrderDO.getParentId() != 0 || serviceOrderDO.getApplicantParentId() != 0)
            visaList = ctx.visaMap.getOrDefault(visaLookupId, new ArrayList<>());
        else
            visaList = ctx.visaMap.getOrDefault(serviceOrderDO.getId(), new ArrayList<>());
        if (visaList != null && visaList.size() > 0) {
            for (VisaDO visaDo : visaList)
                cIds.add(visaDo.getId());
        }
        serviceOrderDto.setVisaDOList(visaList);
        serviceOrderDto.setCIds(cIds);

        // 查询职业名称
        ServiceAssessDO serviceAssessDO = null;
        if (serviceOrderDto.getServiceAssessId() != null) {
            try {
                serviceAssessDO = ctx.serviceAssessMap.get(Integer.parseInt(serviceOrderDto.getServiceAssessId()));
            } catch (NumberFormatException ignored) {}
        }
        if (serviceAssessDO == null && serviceOrderDto.getServiceAssessId() != null) {
            serviceAssessDO = serviceAssessDao.seleteAssessById(serviceOrderDto.getServiceAssessId());
        }
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
        List<MailRemindDO> mailRemindDOS = ctx.mailRemindMap.getOrDefault(serviceOrderDO.getId(), new ArrayList<>());
        if (mailRemindDOS.size() > 0) {
            List<MailRemindDTO> mailRemindDTOS = new ArrayList<>();
            mailRemindDOS.forEach(mailRemindDO -> {
                MailRemindDTO map = mapper.map(mailRemindDO, MailRemindDTO.class);
                Integer adviserId = map.getAdviserId();
                Integer offcialId = map.getOffcialId();
                Integer kjId = map.getKjId();
                if (adviserId != null) {
                    AdviserDO adviserById = ctx.adviserMap.get(adviserId);
                    if (adviserById != null) map.setUserName(adviserById.getName());
                }
                if (offcialId != null) {
                    OfficialDO officialById = ctx.officialMap.get(offcialId);
                    if (officialById != null) map.setUserName(officialById.getName());
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
            SchoolCourseDO schoolCourseDO = ctx.schoolCourseMap.get(serviceOrderDto.getCourseId());
            if (schoolCourseDO != null) {
                SchoolInstitutionDO schoolInstitutionDO = ctx.schoolInstitutionMap.get(schoolCourseDO.getProviderId());
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
                if (serviceOrderDto.getSchoolInstitutionListDTO() != null) {
                    serviceOrderDto.getSchoolInstitutionListDTO().setSchoolCourseDO(schoolCourseDO);
                }
                if (serviceOrderDto.getSchoolInstitutionLocationId() > 0) {
                    SchoolInstitutionLocationDO schoolInstitutionLocationDO = ctx.schoolInstitutionLocationMap.get(serviceOrderDto.getSchoolInstitutionLocationId());
                    if (serviceOrderDto.getSchoolInstitutionListDTO() != null && schoolInstitutionLocationDO != null) {
                        serviceOrderDto.getSchoolInstitutionListDTO().setSchoolInstitutionLocationDO(schoolInstitutionLocationDO);
                    }
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
        if (!serviceOrderDO.isPay() && (ctx.servicePackagePriceMap.get(serviceOrderDO.getServiceId()) != null && ctx.servicePackagePriceMap.get(serviceOrderDO.getServiceId()).getRuler() == 1) || serviceOrderDO.isPay()) {
            serviceOrderDto.setCreateVisaOffice(true);
        } else
            serviceOrderDto.setCreateVisaOffice(false);
        ServiceOrderDO parentServiceOrder = ctx.childrenOrderMap.containsKey(serviceOrderDO.getParentId()) ?
                null : serviceOrderDao.getServiceOrderById(serviceOrderDO.getParentId());
        if (parentServiceOrder == null && serviceOrderDO.getParentId() > 0) {
            parentServiceOrder = serviceOrderDao.getServiceOrderById(serviceOrderDO.getParentId());
        }
        if (ObjectUtil.isNotNull(parentServiceOrder)) {
            if ("SIV".equals(parentServiceOrder.getType())) {
                ServicePackageDO spDo = ctx.servicePackageMap.get(serviceOrderDto.getServicePackageId());
                if (spDo == null) spDo = servicePackageDao.getById(serviceOrderDto.getServicePackageId());
                String type = spDo != null ? spDo.getType() : null;
                if ("ROI".equals(type) || "EOI".equals(type) || "VA".equals(type)) {
                    serviceOrderDto.setCreateVisaOffice(true);
                } else {
                    serviceOrderDto.setCreateVisaOffice(false);
                }
            }
        }
        //判断是否提交mm资料
        if (ctx.customerInformationMap.get(serviceOrderDO.getId()) != null) {
            serviceOrderDto.setSubmitMM(true);
        } else
            serviceOrderDto.setSubmitMM(false);
        // EOI数量排序
        if (serviceOrderDto.getEOINumber() != null && serviceOrderDto.getApplicantParentId() > 0) {
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
                    if (childrenServiceOrders == null)
                        childrenServiceOrders = new ArrayList<>();
                    ChildrenServiceOrderDTO childrenServiceOrderDTO = new ChildrenServiceOrderDTO();
                    childrenServiceOrderDTO.setServicePackageId(e.getId());
                    childrenServiceOrderDTO.setServicePackageType(e.getType());
                    if (childrenServiceOrderDTO.getId() != 0) {
                        childrenServiceOrders.add(childrenServiceOrderDTO);
                    }
                    serviceOrderDto.setChildrenServiceOrders(childrenServiceOrders);
                }
            });
            // 查询打包订单绑定的职评订单信息
            Integer bingDingAssOrderId = serviceOrderDao.getBingDingAssOrderId(serviceOrderDto.getId());
            if (bingDingAssOrderId != null) {
                serviceOrderDto.setBingdingAssessOrder(bingDingAssOrderId);
            }
        }
        // 判断offer文件路径是否为多个
        String offerUrl = serviceOrderDto.getOfferUrl();
        if (StringUtil.isNotEmpty(offerUrl)) {
            List<String> list = Arrays.asList(offerUrl.split(","));
            serviceOrderDto.setOfferUrls(list);
        }
        // 留学订单添加签证信息
        if ("OVST".equals(serviceOrderDto.getType())) {
            CommissionOrderTempDO commissionOrderTempByServiceOrderId = ctx.commissionOrderTempMap.get(serviceOrderDto.getId());
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
                List<InsuranceCompanyDO> insList = insuranceCompanyDAO.list(serviceOrderInsuranceDO.getInsuranceCompanyId(), true, 0, 1);
                if (insList != null && insList.size() > 0) {
                    serviceOrderDto.setInsuranceCompanyDO(insList.get(0));
                }
            }
        } else if ("0".equals(serviceOrderDto.getIsInsuranceCompany())) {
            serviceOrderDto.setIsInsuranceCompany("0");
        } else {
            serviceOrderDto.setIsInsuranceCompany("");
        }
        // 顾问以及文案资料大小合计
        Long officialDataSize =  cloudDiskFileDAO.listByOfficialId(null, serviceOrderDto.getUserId());
        Long adviserDataSize = cloudDiskFileDAO.listByAdviserId(serviceOrderDto.getAdviserId(), serviceOrderDto.getUserId());

        serviceOrderDto.setAdviserDataSize(adviserDataSize);
        serviceOrderDto.setOfficialDataSize(officialDataSize);

        ServiceOrderAndManage serviceOrderAndManage = ctx.serviceOrderManageMap.get(serviceOrderDto.getId());
        if (serviceOrderAndManage != null) {
            serviceOrderDto.setManageOrder(true);
            serviceOrderDto.setParentIdNew(serviceOrderAndManage.getServiceOrderManageId());
        }
        // 获取服务订单上传合同日志信息
        List<WebLogDTO> webLogDTOList = ctx.webLogMap.getOrDefault(serviceOrderDto.getId(), new ArrayList<>());
        if (!webLogDTOList.isEmpty()) {
            serviceOrderDto.setContractDataList(webLogDTOList);
        }
        // 获取AI审核数据
        List<ReviewAIDO> reviewAIList = ctx.reviewAIMap.getOrDefault(serviceOrderDto.getId(), new ArrayList<>());
        if (!reviewAIList.isEmpty()) {
            serviceOrderDto.setReviewAIList(reviewAIList);
        }
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
                            getPeopleTypeStr(serviceOrderDo.getPeopleType()), "<br/>客户的资料地址: https://yongjinbiao.zhinanzhen.org/webroot_new/attachments/serviceOrder/get?id=", serviceOrderDo.getId(),
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
                                "<br/>属性:", getPeopleTypeStr(serviceOrderDo.getPeopleType()), "<br/>客户的资料地址: https://yongjinbiao.zhinanzhen.org/webroot_new/attachments/serviceOrder/get?id=", serviceOrderDo.getId(), "<br/>客户基本信息:", applicantDto.getContent(), "<br/>备注:",

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
                                "<br/>客户的资料地址: https://yongjinbiao.zhinanzhen.org/webroot_new/attachments/serviceOrder/get?id=", serviceOrderDo.getId(), "<br/>申请人基本信息:", applicantDto.getContent(),
                                "<br/>备注:", serviceOrderDo.getRemarks(), "<br/>驳回原因:", serviceOrderDo.getRefuseReason(),
                                "<br/>创建时间:", date, "<br/>", serviceOrderMailDetail.getServiceOrderUrl()));
            if (StringUtil.equals("Retracted", serviceOrderDo.getStateMark2())) // 顾问提交审核后又撤回，提醒文案不同
                sendMail(_officialDo.getEmail() + ",maggie@zhinanzhen.org", "服务被撤回提醒:",
                        StringUtil.merge("亲爱的", _officialDo.getName(), ":<br/>您有一条服务订单已被撤回,如有服务相关问题请及时与顾问沟通<br/>订单号:",
                                serviceOrderDo.getId(), "<br/>服务类型:", serviceOrderMailDetail.getType(),
                                serviceOrderMailDetail.getDetail(), "<br/>顾问:", adviserDo.getName(), "<br/>申请人基本信息:",
                                applicantDto.getContent(), "<br/>客户的资料地址: https://yongjinbiao.zhinanzhen.org/webroot_new/attachments/serviceOrder/get?id=", serviceOrderDo.getId(), "<br/>备注:",
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
                                                adviserDo.getName(), "<br/>文案:", _officialDo.getName(), "<br/>客户的资料地址: https://yongjinbiao.zhinanzhen.org/webroot_new/attachments/serviceOrder/get?id=", serviceOrderDo.getId(), "<br/>备注:", serviceOrderDo.getRemarks(),

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
                                        _officialDo.getName(), "<br/>客户的资料地址: https://yongjinbiao.zhinanzhen.org/webroot_new/attachments/serviceOrder/get?id=", serviceOrderDo.getId(), "<br/>备注:",
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

    @Override
    public int addReviewAI(ReviewAIDO reviewAIDo) {
        return reviewAIDAO.addReviewAI(reviewAIDo);
    }

    @Override
    public int updateReviewAI(ReviewAIDO reviewAIDo) {
        return reviewAIDAO.updateReviewAI(reviewAIDo);
    }

    @Override
    public ReviewAIDO getReviewAIById(int id) {
        return reviewAIDAO.getReviewAIById(id);
    }

    @Override
    public List<ReviewAIDO> listReviewAI(Integer serviceOrderId, Integer adminUserId, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return reviewAIDAO.listReviewAI(serviceOrderId, adminUserId, offset, pageSize);
    }

    @Override
    public int countReviewAI(Integer serviceOrderId, Integer adminUserId) {
        return reviewAIDAO.countReviewAI(serviceOrderId, adminUserId);
    }

    @Override
    public int deleteReviewAIById(int id) {
        return reviewAIDAO.deleteReviewAIById(id);
    }
}
