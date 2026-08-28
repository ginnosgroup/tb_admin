package org.zhinanzhen.b.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ikasoa.core.utils.ListUtil;
import com.ikasoa.core.utils.ObjectUtil;
import com.ikasoa.core.utils.StringUtil;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zhinanzhen.b.dao.InsuranceCompanyDAO;
import org.zhinanzhen.b.dao.ServiceDAO;
import org.zhinanzhen.b.dao.ServicePackageDAO;
import org.zhinanzhen.b.dao.ServicePackagePriceDAO;
import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.service.*;
import org.zhinanzhen.b.service.pojo.*;
import org.zhinanzhen.b.service.pojo.ant.Sorter;
import org.zhinanzhen.tb.controller.ListResponse;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.RegionService;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.UserService;
import org.zhinanzhen.tb.service.pojo.RegionDTO;
import org.zhinanzhen.tb.utils.SendEmailUtil;
import org.zhinanzhen.tb.utils.WXWorkAPI;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/visaOfficial")
@Log4j
public class VisaOfficialController extends BaseCommissionOrderController {

    private static final Logger LOG = LoggerFactory.getLogger(VisaOfficialController.class);

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Resource
    VisaService visaService;

    @Resource
    VisaOfficialService visaOfficialService;

    @Resource
    ApplicantService applicantService;

    @Resource
    RegionService regionService;

    @Resource
    OfficialService officialService;

    @Resource
    UserService userService;

    @Resource
    private ServiceService serviceService;

    @Resource
    private WXWorkService wxWorkService;
    @Autowired
    private ServiceDAO serviceDAO;
    @Autowired
    private ServicePackagePriceDAO servicePackagePriceDAO;
    @Autowired
    private ServicePackageDAO servicePackageDao;
    @Autowired
    private org.zhinanzhen.b.dao.ServiceOrderDAO serviceOrderDAO;
    @Autowired
    private InsuranceCompanyDAO insuranceCompanyDAO;

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public Response<List<VisaOfficialDTO>> add(
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "applicantBirthday", required = false) String applicantBirthday,
            @RequestParam(value = "handlingDate") String handlingDate,
            @RequestParam(value = "receiveTypeId") String receiveTypeId,
            @RequestParam(value = "receiveDate") String receiveDate,
            @RequestParam(value = "serviceId") String serviceId,
            @RequestParam(value = "serviceOrderId") Integer serviceOrderId,
            @RequestParam(value = "installment") Integer installment,
            @RequestParam(value = "paymentVoucherImageUrl1", required = false) String paymentVoucherImageUrl1,
            @RequestParam(value = "paymentVoucherImageUrl2", required = false) String paymentVoucherImageUrl2,
            @RequestParam(value = "paymentVoucherImageUrl3", required = false) String paymentVoucherImageUrl3,
            @RequestParam(value = "paymentVoucherImageUrl4", required = false) String paymentVoucherImageUrl4,
            @RequestParam(value = "paymentVoucherImageUrl5", required = false) String paymentVoucherImageUrl5,
            @RequestParam(value = "visaVoucherImageUrl", required = false) String visaVoucherImageUrl,
            @RequestParam(value = "receivable") String receivable,
            @RequestParam(value = "received", required = false) String received,
            @RequestParam(value = "perAmount") String perAmount, @RequestParam(value = "amount") String amount,
            @RequestParam(value = "currency", required = false) String currency,
            @RequestParam(value = "exchangeRate", required = false) String exchangeRate,
            @RequestParam(value = "invoiceNumber", required = false) String invoiceNumber,
            @RequestParam(value = "adviserId") String adviserId, @RequestParam(value = "maraId") String maraId,
            @RequestParam(value = "officialId") String officialId,
            @RequestParam(value = "remarks", required = false) String remarks,
            @RequestParam(value = "verifyCode", required = false) String verifyCode, HttpServletRequest request,
            HttpServletResponse response) {

        // 更改当前顾问编号
        Integer newAdviserId = getAdviserId(request);
        if (newAdviserId != null)
            adviserId = newAdviserId + "";

        try {
            super.setPostHeader(response);
            ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(serviceOrderId);
            if (serviceOrderDto == null)
                return new Response<>(1, "服务订单(ID:" + serviceOrderId + ")不存在!", null);
            List<VisaOfficialDTO> visaOfficialDTOList = new ArrayList<>();
            VisaOfficialDTO visaDto = new VisaOfficialDTO();
            double _receivable = 0.00;
            if (StringUtil.isNotEmpty(receivable))
                _receivable = Double.parseDouble(receivable);
            double _received = 0.00;
            if (StringUtil.isNotEmpty(received))
                _received = Double.parseDouble(received);
            visaDto.setState(ReviewKjStateEnum.PENDING.toString());
            if (StringUtil.isNotEmpty(userId))
                visaDto.setUserId(Integer.parseInt(userId));
            visaDto.setCode(UUID.randomUUID().toString());
            if (StringUtil.isNotEmpty(handlingDate))
                visaDto.setHandlingDate(new Date(Long.parseLong(handlingDate)));
            if (StringUtil.isNotEmpty(receiveTypeId))
                visaDto.setReceiveTypeId(Integer.parseInt(receiveTypeId));
            if (StringUtil.isNotEmpty(receiveDate))
                visaDto.setReceiveDate(new Date(Long.parseLong(receiveDate)));
            if (StringUtil.isNotEmpty(serviceId))
                visaDto.setServiceId(Integer.parseInt(serviceId));
            if (serviceOrderId != null && serviceOrderId > 0)
                visaDto.setServiceOrderId(serviceOrderId);
            if (installment != null)
                visaDto.setInstallment(installment);
            if (StringUtil.isNotEmpty(paymentVoucherImageUrl1))
                visaDto.setPaymentVoucherImageUrl1(paymentVoucherImageUrl1);
            else
                visaDto.setPaymentVoucherImageUrl1(serviceOrderDto.getPaymentVoucherImageUrl1());
            if (StringUtil.isNotEmpty(paymentVoucherImageUrl2))
                visaDto.setPaymentVoucherImageUrl2(paymentVoucherImageUrl2);
            else
                visaDto.setPaymentVoucherImageUrl2(serviceOrderDto.getPaymentVoucherImageUrl2());
            if (StringUtil.isNotEmpty(paymentVoucherImageUrl3))
                visaDto.setPaymentVoucherImageUrl3(paymentVoucherImageUrl3);
            else
                visaDto.setPaymentVoucherImageUrl3(serviceOrderDto.getPaymentVoucherImageUrl3());
            if (StringUtil.isNotEmpty(paymentVoucherImageUrl4))
                visaDto.setPaymentVoucherImageUrl4(paymentVoucherImageUrl4);
            else
                visaDto.setPaymentVoucherImageUrl4(serviceOrderDto.getPaymentVoucherImageUrl4());
            if (StringUtil.isNotEmpty(paymentVoucherImageUrl5))
                visaDto.setPaymentVoucherImageUrl5(paymentVoucherImageUrl5);
            else
                visaDto.setPaymentVoucherImageUrl5(serviceOrderDto.getPaymentVoucherImageUrl5());
            if (StringUtil.isNotEmpty(visaVoucherImageUrl))
                visaDto.setVisaVoucherImageUrl(visaVoucherImageUrl);
            else
                visaDto.setVisaVoucherImageUrl(serviceOrderDto.getVisaVoucherImageUrl());
            if (StringUtil.isNotEmpty(perAmount))
                visaDto.setPerAmount(Double.parseDouble(perAmount));
            if (StringUtil.isNotEmpty(amount))
                visaDto.setAmount(Double.parseDouble(amount));
            if (visaDto.getPerAmount() < visaDto.getAmount())
                return new Response<List<VisaOfficialDTO>>(1,
                        "本次应收款(" + visaDto.getPerAmount() + ")不能小于本次已收款(" + visaDto.getAmount() + ")!", null);
            if (StringUtil.isNotEmpty(currency))
                visaDto.setCurrency(currency);
            if (StringUtil.isNotEmpty(exchangeRate))
                visaDto.setExchangeRate(Double.parseDouble(exchangeRate));
            visaDto.setDiscount(visaDto.getPerAmount() - visaDto.getAmount());
            if (StringUtil.isNotEmpty(invoiceNumber))
                visaDto.setInvoiceNumber(invoiceNumber);
            if (StringUtil.isNotEmpty(adviserId)) {
                visaDto.setAdviserId(StringUtil.toInt(adviserId));
            }
            if (StringUtil.isNotEmpty(maraId))
                visaDto.setMaraId(StringUtil.toInt(maraId));
            if (StringUtil.isNotEmpty(officialId)) {
                visaDto.setOfficialId(StringUtil.toInt(officialId));
            }
            if (StringUtil.isNotEmpty(remarks))
                visaDto.setRemarks(remarks);
            double commission = visaDto.getAmount();
            if ("CNY".equals(currency)) {
                BigDecimal bigDecimal = BigDecimal.valueOf(commission);
                BigDecimal bigDecimalExc = new BigDecimal(exchangeRate);
                BigDecimal divide = bigDecimal.divide(bigDecimalExc, 4, RoundingMode.HALF_UP);
                commission = divide.doubleValue();
            }
            visaDto.setGst(commission / 11);
            visaDto.setDeductGst(commission - visaDto.getGst());
            visaDto.setBonus(visaDto.getDeductGst() * 0.1);
            visaDto.setExpectAmount(commission);

            VisaDTO _visaDto = visaService.getFirstVisaByServiceOrderId(serviceOrderId);
            if (_visaDto != null && ReviewKjStateEnum.COMPLETE.toString().equalsIgnoreCase(_visaDto.getState())
                    && CommissionStateEnum.YJY.toString().equalsIgnoreCase(_visaDto.getCommissionState())) {
                LOG.info(StringUtil.merge("佣金订单(", _visaDto.getId(), ")对应文案佣金订单合账."));
                visaDto.setMerged(Boolean.TRUE);
            }

            double _perAmount = 0.00;
            double _amount = 0.00;
            visaDto.setState(ReviewKjStateEnum.REVIEW.toString()); // 第一笔单子直接进入财务审核状态
            if (StringUtil.isNotEmpty(verifyCode))// 只给第一笔赋值verifyCode
                visaDto.setVerifyCode(verifyCode.replace("$", "").replace("#", "").replace(" ", ""));
            visaDto.setKjApprovalDate(new Date());

            if (visaOfficialService.addVisa(visaDto) > 0) {
                visaOfficialDTOList.add(visaDto);
            } else if (visaOfficialService.addVisa(visaDto) == -2) {
//                throw new ServiceException("当前打包签证中同时包含EOI和ROI，在EOI进行结算");
                return new Response<>(-1, "当前打包签证中同时包含EOI和ROI，在EOI进行结算");
            } else {
                return new Response<>(-1, "服务订单暂未付款完成");
            }
            _perAmount += visaDto.getPerAmount();
            _amount += visaDto.getAmount();
            return new Response<>(0, "", visaOfficialDTOList);
        } catch (ServiceException e) {
            log.info("当前错误订单为：" + serviceOrderId);
            log.info(e.getMessage());
            return new Response<>(e.getCode(), e.getMessage(), null);
        }
    }

    @RequestMapping(value = "/listVisaOfficial", method = RequestMethod.GET)
    @ResponseBody
    public ListResponse<List<VisaOfficialDTO>> listVisaOrder(
            @RequestParam(value = "id", required = false) Integer id,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "startHandlingDate", required = false) String startHandlingDate,
            @RequestParam(value = "endHandlingDate", required = false) String endHandlingDate,
            @RequestParam(value = "regionId", required = false) Integer regionId,
            @RequestParam(value = "officialId", required = false) Integer officialId,
            @RequestParam(value = "maraId", required = false) Integer maraId,
            @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "applicantName", required = false) String applicantName,
            @RequestParam(value = "isMerged", required = false) String isMerged,
            @RequestParam(value = "pageNum") Integer pageNum,
            @RequestParam(value = "pageSize") Integer pageSize,
            @RequestParam(value = "currency", required = false) String currency,
            @RequestParam(value = "sorter", required = false) String sorter, HttpServletResponse response,
            HttpServletRequest request) {
        super.setGetHeader(response);

        List<Integer> regionIdList = null;
        if (regionId != null && regionId > 0)
            regionIdList = ListUtil.buildArrayList(regionId);
        Sorter _sorter = null;
        if (sorter != null)
            _sorter = JSON.parseObject(sorter, Sorter.class);
        try {
            Integer newOfficialId = getOfficialId(request);
            super.setGetHeader(response);
            // 处理文案管理员
            AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
            if (adminUserLoginInfo == null)
                return new ListResponse<>(false, pageSize, 0, null, "No permission !");
            if ("WA".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
                if (newOfficialId == null)
                    return new ListResponse<>(false, pageSize, 0, null, "无法获取文案编号，请退出重新登录后再尝试．");
                OfficialDTO currentOfficial = officialService.getOfficialById(newOfficialId);
                if (currentOfficial.getIsOfficialAdmin()) {
                    int regionIdCurrent = currentOfficial.getRegionId();
                    List<RegionDTO> regionList = regionService.listRegion(regionIdCurrent);
                    regionIdList = ListUtil.buildArrayList(regionIdCurrent);
                    for (RegionDTO region : regionList)
                        regionIdList.add(region.getId());
                    if (officialId != null) {
                        OfficialDTO officialById = officialService.getOfficialById(officialId);
                        if (officialById.getRegionId() != regionIdCurrent) {
                            return new ListResponse<>(true, pageSize, 0, null, "500");
                        }
                    }
                }
                // 更改当前文案编号
                officialId = newOfficialId;
            } else {
                // 更改当前文案编号
                if (newOfficialId != null)
                    officialId = newOfficialId;
            }
            Boolean merged = null;
            if (StringUtil.equals(isMerged, "true"))
                merged = true;
            if (StringUtil.equals(isMerged, "false"))
                merged = false;
            String name = applicantName;
            if (StringUtil.isNotEmpty(applicantName)) {
                name = applicantName.replaceAll("\\s", "");
            }
            int count = visaOfficialService.count(officialId, regionIdList, id, startHandlingDate, endHandlingDate, state, startDate, endDate, userName, name, merged, currency, maraId);
            List<VisaOfficialDTO> officialDTOList = visaOfficialService.listVisaOfficialOrder(officialId, regionIdList, id, startHandlingDate, endHandlingDate, state, startDate,
                    endDate, null, null, userName, name, merged, pageNum, pageSize, _sorter, null, currency, maraId);
            if (officialDTOList == null) {
                officialDTOList = new ArrayList<>();
                return new ListResponse(true, pageSize, count, officialDTOList, "查询成功");
            } else {
                return new ListResponse(true, pageSize, count, officialDTOList, "查询成功");
            }
        } catch (ServiceException e) {
            return new ListResponse<>(false, pageSize, 0, null, e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @RequestMapping(value = "/updateOfficialVisa", method = RequestMethod.PUT)
    @ResponseBody
    public Response<String> update(
            @RequestParam(value = "id") Integer id,
            @RequestParam(value = "handlingDate", required = false) String handlingDate,
            @RequestParam(value = "commissionAmount", required = false) Double commissionAmount,
            @RequestParam(value = "state", required = false) String state,
            HttpServletResponse response, HttpServletRequest request) {
        super.setGetHeader(response);
        // 获取用户信息
        try {
            AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
            if (adminUserLoginInfo != null) {
                if (adminUserLoginInfo.getApList().equalsIgnoreCase("KJ")) {
                    visaOfficialService.update(id, handlingDate, commissionAmount, state, null);
                    return new Response<>(0, "修改成功");
                }
                if (adminUserLoginInfo.getApList().equalsIgnoreCase("WA") && officialService.getOfficialById(getOfficialId(request)).getIsOfficialAdmin()) {
                    if (commissionAmount != null || StringUtil.isNotEmpty(state)) {
                        return new Response<>(1, "修改失败没有权限");
                    }
                    visaOfficialService.update(id, handlingDate, commissionAmount, state, null);
                    return new Response<>(0, "修改成功");
                }
            }
            return new Response<>(1, "修改失败,请登录");
        } catch (ServiceException e) {
            return new Response<>(1, "修改失败" + e.getMessage());
        }
    }

    @RequestMapping(value = "/downOfficialCommission", method = RequestMethod.GET)
    @ResponseBody
    public void downOfficialCommission(
            @RequestParam(value = "id", required = false) Integer id,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "startHandlingDate", required = false) String startHandlingDate,
            @RequestParam(value = "endHandlingDate", required = false) String endHandlingDate,
            @RequestParam(value = "regionId", required = false) Integer regionId,
            @RequestParam(value = "officialId", required = false) Integer officialId,
            @RequestParam(value = "maraId", required = false) Integer maraId,
            @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "applicantName", required = false) String applicantName,
            @RequestParam(value = "serviceId", required = false) Integer serviceId,
            HttpServletResponse response, HttpServletRequest request) {
        try {
            List<Integer> regionIdList = null;
            if (regionId != null) {
                regionIdList = new ArrayList<>();
                regionIdList.add(regionId);
            }
            AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
            Integer newOfficialId = getOfficialId(request);
//            if ("WA".equals(adminUserLoginInfo.getApList())) {
//                officialId = adminUserLoginInfo.getOfficialId();
//            }

            if ("WA".equalsIgnoreCase(adminUserLoginInfo.getApList())
                    && officialService.getOfficialById(newOfficialId).getIsOfficialAdmin()) {
                int regionIdCurrent = officialService.getOfficialById(newOfficialId).getRegionId();
                List<RegionDTO> regionList = regionService.listRegion(regionIdCurrent);
                regionIdList = ListUtil.buildArrayList(regionIdCurrent);
                for (RegionDTO region : regionList)
                    regionIdList.add(region.getId());
                if (officialId != null) {
                    OfficialDTO officialById = officialService.getOfficialById(officialId);
                    if (officialById.getRegionId() != regionIdCurrent) {
                        String s = "该文案管理员不能查询该地区，请核验地区";
                    }
                }
            } else {
                // 更改当前文案编号
                if (newOfficialId != null)
                    officialId = newOfficialId;
//                if ("WA".equalsIgnoreCase(adminUserLoginInfo.getApList()) && officialId == null)
            }

            String name = applicantName;
            if (StringUtil.isNotEmpty(applicantName)) {
                name = applicantName.replaceAll("\\s", "");
            }
//            List<VisaOfficialDTO> officialList = visaOfficialService.listVisaOfficialOrder(officialId, regionIdList, id, startHandlingDate, endHandlingDate, state,
//                    startDate, endDate, null, null, userName, name, null, null, null, null, null, null);
            List<VisaOfficialDTO> officialList = visaOfficialService.listVisaForDown(officialId, regionIdList, id, startHandlingDate, endHandlingDate, state,
                    startDate, endDate, userName, name, maraId);
            if (officialList != null && serviceId != null) {
                officialList = officialList.stream()
                        .filter(item -> item != null && item.getServiceId() == serviceId)
                        .collect(Collectors.toList());
            }
            if (officialList == null || officialList.isEmpty()) {
                return;
            }
            response.reset();// 清空输出流
            String tableName = "official_visa_commission";
            response.setHeader("Content-disposition",
                    "attachment; filename=" + new String(tableName.getBytes("GB2312"), "8859_1") + ".xlsx");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            int i = 1;
            OutputStream os = response.getOutputStream();

            // 读取模板获取表头
            InputStream is = this.getClass().getResourceAsStream("/officialVisa.xls");
            HSSFWorkbook templateWb = new HSSFWorkbook(is);
            HSSFSheet templateSheet = templateWb.getSheetAt(0);
            HSSFRow templateHeaderRow = templateSheet.getRow(0);

            // 创建 SXSSFWorkbook(.xlsx)，流式写入，内存中只保留100行
            SXSSFWorkbook wb = new SXSSFWorkbook(100);
            org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet("Sheet1");

            // 复制表头，并在“是否保险公司”后插入“保险公司名称”
            Row headerRow = sheet.createRow(0);
            short lastCellNum = templateHeaderRow.getLastCellNum();
            for (int c = 0; c < lastCellNum; c++) {
                int targetColumn = c < 29 ? c : c + 1;
                headerRow.createCell(targetColumn).setCellValue(templateHeaderRow.getCell(c).getStringCellValue());
            }
            headerRow.createCell(29).setCellValue("保险公司名称");

            String servicePackageType = "";
            List<ServicePackagePriceDO> servicePackagePriceDOS = servicePackagePriceDAO.list(null, null, 0, 999);
            Map<Integer, ServicePackagePriceDO> servicePackagePriceDOMap = servicePackagePriceDOS.stream().collect(Collectors.toMap(ServicePackagePriceDO::getServiceId, Function.identity()));

            // 预加载数据，消除 N+1 查询
            Set<Integer> parentOrderIds = new HashSet<>();
            Set<Integer> bindingOrderIds = new HashSet<>();
            Set<Integer> servicePackageIds = new HashSet<>();
            Set<Integer> serviceIds = new HashSet<>();
            Set<Integer> serviceOrderIds = new HashSet<>();
            for (VisaOfficialDTO vd : officialList) {
                if (vd.getServiceOrderId() > 0) {
                    serviceOrderIds.add(vd.getServiceOrderId());
                }
                ServiceOrderDTO so = vd.getServiceOrder();
                if (so != null) {
                    if (so.getServiceId() > 0) serviceIds.add(so.getServiceId());
                    if (so.getApplicantParentId() > 0) parentOrderIds.add(so.getApplicantParentId());
                    if (so.getServicePackageId() > 0) servicePackageIds.add(so.getServicePackageId());
                    if (so.getBindingOrder() != null && so.getBindingOrder() > 0)
                        bindingOrderIds.add(so.getBindingOrder());
                }
            }
            Map<Integer, ServiceDO> serviceMap = serviceIds.isEmpty() ? Collections.emptyMap() :
                    serviceDAO.listByIds(new ArrayList<>(serviceIds)).stream().collect(Collectors.toMap(ServiceDO::getId, Function.identity()));
            Map<Integer, ServiceOrderDO> parentOrderMap = parentOrderIds.isEmpty() ? Collections.emptyMap() :
                    serviceOrderDAO.listByIds(new ArrayList<>(parentOrderIds)).stream().collect(Collectors.toMap(ServiceOrderDO::getId, Function.identity()));
            Map<Integer, ServicePackageDO> servicePackageMap = servicePackageIds.isEmpty() ? Collections.emptyMap() :
                    servicePackageDao.listByIds(new ArrayList<>(servicePackageIds)).stream().collect(Collectors.toMap(ServicePackageDO::getId, Function.identity()));
            Map<Integer, ServiceOrderDO> bindingOrderMap = bindingOrderIds.isEmpty() ? Collections.emptyMap() :
                    serviceOrderDAO.listByIds(new ArrayList<>(bindingOrderIds)).stream().collect(Collectors.toMap(ServiceOrderDO::getId, Function.identity()));
            // 补充绑定订单的 serviceId 到 serviceMap
            for (ServiceOrderDO bOrder : bindingOrderMap.values()) {
                if (bOrder.getServiceId() > 0) {
                    serviceIds.add(bOrder.getServiceId());
                }
            }
            if (serviceIds.size() > serviceMap.size()) {
                serviceMap = serviceDAO.listByIds(new ArrayList<>(serviceIds)).stream().collect(Collectors.toMap(ServiceDO::getId, Function.identity()));
            }

            Map<Integer, String> insuranceCompanyNameMap = Collections.emptyMap();
            if (!serviceOrderIds.isEmpty()) {
                List<ServiceOrderInsuranceDO> serviceOrderInsuranceList = insuranceCompanyDAO.listByServiceOrderIds(new ArrayList<>(serviceOrderIds));
                Set<Integer> insuranceCompanyIds = serviceOrderInsuranceList.stream()
                        .map(ServiceOrderInsuranceDO::getInsuranceCompanyId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                if (!insuranceCompanyIds.isEmpty()) {
                    Map<Integer, String> insuranceCompanyNameById = insuranceCompanyDAO
                            .listByIds(new ArrayList<>(insuranceCompanyIds)).stream()
                            .collect(Collectors.toMap(InsuranceCompanyDO::getId,
                                    company -> company.getName() == null ? "" : company.getName(),
                                    (first, second) -> first));
                    insuranceCompanyNameMap = serviceOrderInsuranceList.stream()
                            .filter(item -> item.getServiceOrderId() != null && item.getInsuranceCompanyId() != null)
                            .collect(Collectors.toMap(ServiceOrderInsuranceDO::getServiceOrderId,
                                    item -> insuranceCompanyNameById.getOrDefault(item.getInsuranceCompanyId(), ""),
                                    (first, second) -> first));
                }
            }

            for (VisaOfficialDTO visaDTO : officialList) {
                ServiceOrderDTO so = visaDTO.getServiceOrder();
                Row row = sheet.createRow(i);
                row.createCell(0).setCellValue(visaDTO.getId());
                row.createCell(1).setCellValue(visaDTO.getServiceOrderId());
                if (visaDTO.getParentIdNew() != null) {
                    row.createCell(2).setCellValue(visaDTO.getParentIdNew());
                }
                row.createCell(3).setCellValue(visaDTO.getHandlingDate() == null ? "" : sdf.format(visaDTO.getHandlingDate()));
                row.createCell(4).setCellValue(sdf.format(so.getGmtCreate()));
                row.createCell(5).setCellValue(visaDTO.getUserName());
                String applicantNameValue = "";
                if (visaDTO.getApplicant() != null && !visaDTO.getApplicant().isEmpty()) {
                    applicantNameValue = StringUtil.merge(visaDTO.getApplicant().get(0).getFirstname(), " ", visaDTO.getApplicant().get(0).getSurname());
                }
                row.createCell(6).setCellValue(applicantNameValue);
                row.createCell(7).setCellValue(visaDTO.getReceiveDate() == null ? "" : sdf.format(visaDTO.getReceiveDate()));
                row.createCell(8).setCellValue(visaDTO.getCurrency());
                row.createCell(9).setCellValue(visaDTO.getExchangeRate());
                row.createCell(10).setCellValue(visaDTO.getReceiveTypeName());

                // SIV 父订单类型判断 → 使用 parentOrderMap
                if (so.getApplicantParentId() > 0) {
                    ServiceOrderDO parentOrder = parentOrderMap.get(so.getApplicantParentId());
                    if (parentOrder != null && "SIV".equals(parentOrder.getType())) {
                        String type = so.getServicePackage().getType();
                        switch (type) {
                            case "CA":
                                type = "职业评估";
                                break;
                            case "EOI":
                                type = "EOI";
                                break;
                            case "VA":
                                type = "签证申请";
                                break;
                            case "TM":
                                type = "提名";
                                break;
                            case "ZD":
                                type = "州担";
                                break;
                            default:
                                type = type;
                        }
                        if ("EOI".equalsIgnoreCase(type)) {
                            ServiceDO spService = serviceMap.get(so.getServicePackage().getServiceId());
                            if (spService != null) {
                                type = type + "-" + spService.getCode();
                            }
                        }
                        servicePackageType = "-" + type;
                    }
                }

                // 绑定订单特殊处理（serviceId==25）→ 使用 servicePackageMap 和 bindingOrderMap
                if (so.getService().getId() == 25 && so.getBindingOrder() != null) {
                    ServicePackageDO servicePackageDo = servicePackageMap.get(so.getServicePackageId());
                    if (ObjectUtil.isNotNull(servicePackageDo)) {
                        visaDTO.setServiceCode(servicePackageDo.getType() + "-" + visaDTO.getServiceCode());
                    }
                    ServiceOrderDO bindingOrder = bindingOrderMap.get(so.getBindingOrder());
                    if (bindingOrder != null) {
                        ServiceDO bindingService = serviceMap.get(bindingOrder.getServiceId());
                        if (bindingService != null) {
                            so.getService().setName(bindingService.getName());
                        }
                    }
                }

                row.createCell(11).setCellValue(StringUtil.merge(so.getService().getName(), "-", visaDTO.getServiceCode(), servicePackageType));
                servicePackageType = "";
                ServicePackagePriceDO servicePackagePriceDO = servicePackagePriceDOMap.get(visaDTO.getServiceId());
                if (ObjectUtil.isNotNull(servicePackagePriceDO)) {
                    row.createCell(12).setCellValue(servicePackagePriceDO.getMaxPrice());
                }
                row.createCell(13).setCellValue(visaDTO.getAdviserName());
                row.createCell(14).setCellValue(visaDTO.getOfficialName());
                row.createCell(15).setCellValue(visaDTO.getMaraName() == null ? "" : visaDTO.getMaraName());
                row.createCell(16).setCellValue(visaDTO.getTotalPerAmountAUD());
                row.createCell(17).setCellValue(visaDTO.getTotalAmountCNY());
                row.createCell(18).setCellValue(visaDTO.getPredictCommissionAmount() + "");
                row.createCell(19).setCellValue(visaDTO.getCommissionAmount() == null ? "" : visaDTO.getCommissionAmount() + "");
                row.createCell(20).setCellValue(visaDTO.getPredictCommission() == null ? "" : visaDTO.getPredictCommission() + "");
                row.createCell(21).setCellValue(visaDTO.getPredictCommissionCNY() == null ? "" : visaDTO.getPredictCommissionCNY() + "");
                double extraAmount = visaDTO.getExtraAmount() == null ? 0 : visaDTO.getExtraAmount();
                row.createCell(22).setCellValue(extraAmount);
                if (extraAmount == 0) {
                    row.createCell(23).setCellValue(0);
                } else {
                    double basicAmount = visaDTO.getCommissionAmount() - visaDTO.getExtraAmount();
                    if (basicAmount < 0) {
                        basicAmount = 0.00;
                    }
                    row.createCell(23).setCellValue(basicAmount);
                }

                // 500签证附加费 → 使用 so 和 serviceMap
                double additionalAmount2A = 0.00;
                double additionalAmountXA = 0.00;
                ServiceDO curService = serviceMap.get(so.getServiceId());
                if (ObjectUtil.isNotNull(curService) && curService.getCode() != null && curService.getCode().contains("500")) {
                    if ("2A".equalsIgnoreCase(so.getPeopleType())) {
                        additionalAmount2A = 50.00;
                    }
                    if ("XA".equalsIgnoreCase(so.getPeopleType())) {
                        additionalAmountXA = 25.00;
                    }
                    if ("XB".equalsIgnoreCase(so.getPeopleType())) {
                        additionalAmount2A = 50.00;
                        additionalAmountXA = 25.00;
                    }
                }
                row.createCell(24).setCellValue(additionalAmountXA);
                row.createCell(25).setCellValue(additionalAmount2A);
                row.createCell(26).setCellValue(additionalAmountXA / visaDTO.getExchangeRate());
                row.createCell(27).setCellValue(additionalAmount2A / visaDTO.getExchangeRate());
                String isInsuranceCompany = so.getIsInsuranceCompany();
                row.createCell(28).setCellValue(isInsuranceCompany == null ? "" : ("1".equalsIgnoreCase(isInsuranceCompany) ? "是" : "否"));
                row.createCell(29).setCellValue(insuranceCompanyNameMap.getOrDefault(visaDTO.getServiceOrderId(), ""));
                row.createCell(30).setCellValue(visaDTO.getPredictCommissionCNY() == null ? 0 : visaDTO.getPredictCommissionCNY());
                row.createCell(31).setCellValue(visaDTO.getPredictCommission() == null ? 0 : visaDTO.getPredictCommission());
                row.createCell(32).setCellValue(visaDTO.getRefundAmount());
                row.createCell(33).setCellValue(visaDTO.getBingDingAmount());
                row.createCell(34).setCellValue(visaDTO.isMerged() ? "是" : "否");
                String states = visaDTO.getState() == null ? "" : visaDTO.getState();
                if (states.equalsIgnoreCase("REVIEW"))
                    states = "待确认";
                row.createCell(35).setCellValue(states.equalsIgnoreCase("COMPLETE") ? "已确认" : states);
                row.createCell(36).setCellValue(visaDTO.getStage() == null ? "" : visaDTO.getStage());
                i++;
            }
            wb.write(os);
            os.flush();
            os.close();
            templateWb.close();
            wb.dispose();

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }
}
