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
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zhinanzhen.b.controller.BaseCommissionOrderController.CommissionStateEnum;
import org.zhinanzhen.b.controller.BaseCommissionOrderController.ReviewKjStateEnum;
import org.zhinanzhen.b.dao.pojo.SetupExcelDO;
import org.zhinanzhen.b.dao.pojo.VisaOfficialDO;
import org.zhinanzhen.b.service.*;
import org.zhinanzhen.b.service.pojo.*;
import org.zhinanzhen.b.service.pojo.ant.Sorter;
import org.zhinanzhen.tb.controller.ListResponse;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.RegionService;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.UserService;
import org.zhinanzhen.tb.service.pojo.RegionDTO;
import org.zhinanzhen.tb.service.pojo.UserDTO;
import org.zhinanzhen.tb.utils.WXWorkAPI;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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
    private WXWorkService wxWorkService;

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
                        return new ListResponse<>(true, pageSize, 0, null, "500");
                    }
                }
            } else {
                // 更改当前文案编号
                if (newOfficialId != null)
                    officialId = newOfficialId;
                if ("WA".equalsIgnoreCase(adminUserLoginInfo.getApList()) && officialId == null)
                    return new ListResponse<>(false, pageSize, 0, null, "无法获取文案编号，请退出重新登录后再尝试．");
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
            int count = visaOfficialService.count(officialId, regionIdList, id, startHandlingDate, endHandlingDate, state, startDate, endDate, userName, name, merged, currency);
            List<VisaOfficialDTO> officialDTOList = visaOfficialService.listVisaOfficialOrder(officialId, regionIdList, id, startHandlingDate, endHandlingDate, state, startDate,
                    endDate, null, null, userName, name, merged, pageNum, pageSize, _sorter, null, currency);
            if (officialDTOList == null) {
                officialDTOList = new ArrayList<>();
                return new ListResponse(true, pageSize, count, officialDTOList, "查询成功");
            } else {
                return new ListResponse(true, pageSize, count, officialDTOList, "查询成功");
            }
        } catch (ServiceException e) {
            return new ListResponse<>(false, pageSize, 0, null, e.getMessage());
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
            @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "applicantName", required = false) String applicantName,
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
            List<VisaOfficialDTO> officialList = visaOfficialService.listVisaOfficialOrder(officialId, regionIdList, id, startHandlingDate, endHandlingDate, state,
                    startDate, endDate, null, null, userName, name, null, null, null, null, null, null);
            response.reset();// 清空输出流
            String tableName = "official_visa_commission";
            response.setHeader("Content-disposition",
                    "attachment; filename=" + new String(tableName.getBytes("GB2312"), "8859_1") + ".xls");
            response.setContentType("application/msexcel");
            int i = 1;
            OutputStream os = response.getOutputStream();
            //获取模板
            InputStream is = this.getClass().getResourceAsStream("/officialVisa.xls");
            HSSFWorkbook wb = new HSSFWorkbook(is);
            HSSFSheet sheet = wb.getSheetAt(0);
            String servicePackageType = "";
            for (VisaOfficialDTO visaDTO : officialList) {
                HSSFRow row = sheet.createRow(i);
                row.createCell(0).setCellValue(visaDTO.getId());
                row.createCell(1).setCellValue(visaDTO.getServiceOrderId());
                row.createCell(2).setCellValue(visaDTO.getHandlingDate() == null ? "" : sdf.format(visaDTO.getHandlingDate()));
                row.createCell(3).setCellValue(sdf.format(visaDTO.getServiceOrder().getGmtCreate()));
                row.createCell(4).setCellValue(visaDTO.getUserName());
                row.createCell(5).setCellValue(StringUtil.merge(visaDTO.getApplicant().get(0).getFirstname(), " ", visaDTO.getApplicant().get(0).getSurname()));
                row.createCell(6).setCellValue(visaDTO.getReceiveDate() == null ? "" : sdf.format(visaDTO.getReceiveDate()));
                row.createCell(7).setCellValue(visaDTO.getCurrency());
                row.createCell(8).setCellValue(visaDTO.getExchangeRate());
                row.createCell(9).setCellValue(visaDTO.getReceiveTypeName());
//                if (ObjectUtil.isNotNull(visaDTO.getServiceOrder().getServicePackage()) && visaDTO.getServiceOrder().getApplicantParentId() > 0) {
//                    servicePackageType = "-" + visaDTO.getServiceOrder().getServicePackage().getType();
//                }
                System.out.println("当前id--------------------------" + visaDTO.getId());
                if (visaDTO.getServiceOrder().getApplicantParentId() > 0 && "SIV".equals(serviceOrderService.getServiceOrderById(visaDTO.getServiceOrder().getApplicantParentId()).getType())) {
                    servicePackageType = "-" + visaDTO.getServiceOrder().getServicePackage().getType();
                }
                row.createCell(10).setCellValue(StringUtil.merge(visaDTO.getServiceOrder().getService().getName(), "-", visaDTO.getServiceCode(), servicePackageType));
                servicePackageType = "";
                row.createCell(11).setCellValue(visaDTO.getAdviserName());
                row.createCell(12).setCellValue(visaDTO.getOfficialName());
                row.createCell(13).setCellValue(visaDTO.getMaraDTO() == null || visaDTO.getMaraDTO().getName() == null ? "" : visaDTO.getMaraDTO().getName());
                row.createCell(14).setCellValue(visaDTO.getTotalPerAmountAUD());
                row.createCell(15).setCellValue(visaDTO.getTotalAmountCNY());
                row.createCell(16).setCellValue(visaDTO.getPredictCommissionAmount() + "");
                row.createCell(17).setCellValue(visaDTO.getCommissionAmount() == null ? "" : visaDTO.getCommissionAmount() + "");
                row.createCell(18).setCellValue(visaDTO.getPredictCommission() == null ? "" : visaDTO.getPredictCommission() + "");
                row.createCell(19).setCellValue(visaDTO.getPredictCommissionCNY() == null ? "" : visaDTO.getPredictCommissionCNY() + "");
                row.createCell(20).setCellValue(visaDTO.isMerged() ? "是" : "否");
                String states = visaDTO.getState() == null ? "" : visaDTO.getState();
                if (states.equalsIgnoreCase("REVIEW"))
                    states = "待确认";
                row.createCell(21).setCellValue(states.equalsIgnoreCase("COMPLETE") ? "已确认" : states);
                i++;
            }
            wb.write(os);
            os.flush();
            os.close();

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

//    @RequestMapping(value = "/downOfficialCommission_V2", method = RequestMethod.GET)
//    @ResponseBody
//    public Response<String> downOfficialCommission_V2(
//            @RequestParam(value = "id", required = false) Integer id,
//            @RequestParam(value = "state", required = false) String state,
//            @RequestParam(value = "startDate", required = false) String startDate,
//            @RequestParam(value = "endDate", required = false) String endDate,
//            @RequestParam(value = "startHandlingDate", required = false) String startHandlingDate,
//            @RequestParam(value = "endHandlingDate", required = false) String endHandlingDate,
//            @RequestParam(value = "regionId", required = false) Integer regionId,
//            @RequestParam(value = "officialId", required = false) Integer officialId,
//            @RequestParam(value = "userName", required = false) String userName,
//            @RequestParam(value = "applicantName", required = false) String applicantName,
//            HttpServletResponse response, HttpServletRequest request) {
//        try {
//            List<Integer> regionIdList = null;
//            if (regionId != null) {
//                regionIdList = new ArrayList<>();
//                regionIdList.add(regionId);
//            }
//            AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
//            Integer newOfficialId = getOfficialId(request);
////            if ("WA".equals(adminUserLoginInfo.getApList())) {
////                officialId = adminUserLoginInfo.getOfficialId();
////            }
//
//            if ("WA".equalsIgnoreCase(adminUserLoginInfo.getApList())
//                    && officialService.getOfficialById(newOfficialId).getIsOfficialAdmin()) {
//                int regionIdCurrent = officialService.getOfficialById(newOfficialId).getRegionId();
//                List<RegionDTO> regionList = regionService.listRegion(regionIdCurrent);
//                regionIdList = ListUtil.buildArrayList(regionIdCurrent);
//                for (RegionDTO region : regionList)
//                    regionIdList.add(region.getId());
//                if (officialId != null) {
//                    OfficialDTO officialById = officialService.getOfficialById(officialId);
//                    if (officialById.getRegionId() != regionIdCurrent) {
//                        String s = "该文案管理员不能查询该地区，请核验地区";
//                    }
//                }
//            } else {
//                // 更改当前文案编号
//                if (newOfficialId != null)
//                    officialId = newOfficialId;
////                if ("WA".equalsIgnoreCase(adminUserLoginInfo.getApList()) && officialId == null)
//            }
//            String name = applicantName;
//            if (StringUtil.isNotEmpty(applicantName)) {
//                name = applicantName.replaceAll("\\s", "");
//            }
//            List<VisaOfficialDTO> officialList = visaOfficialService.listVisaOfficialOrder(officialId, regionIdList, id, startHandlingDate, endHandlingDate, state,
//                    startDate, endDate, null, null, userName, name, null, null, null, null, null, null);
//
//            // 获取token
//            Map<String, Object> tokenMap = wxWorkService.getToken(WXWorkAPI.SECRET_EXCEL);
//            if ((int) tokenMap.get("errcode") != 0) {
//                throw new RuntimeException(tokenMap.get("errmsg").toString());
//            }
//            String customerToken = (String) tokenMap.get("access_token");
//
//            // 创建表格
//            String setupExcelAccessToken = WXWorkAPI.SETUP_EXCEL.replace("ACCESS_TOKEN", customerToken);
//            final JSONObject[] parm = {new JSONObject()};
//            parm[0].put("doc_type", 4);
//            parm[0].put("doc_name", "ServiceOrderTemplate-" + sdf.format(new Date()));
////            String[] userIds = {"XuShiYi"};
////            parm[0].put("admin_users", userIds);
//            JSONObject setupExcelJsonObject = WXWorkAPI.sendPostBody_Map(setupExcelAccessToken, parm[0]);
//            String url = "";
//            if ("0".equals(setupExcelJsonObject.get("errcode").toString())) {
//                url = setupExcelJsonObject.get("url").toString();
//                String docId = setupExcelJsonObject.get("docid").toString();
//                SetupExcelDO setupExcelDO = new SetupExcelDO();
//                setupExcelDO.setUrl(url);
//                setupExcelDO.setDocId(docId);
//                String informationExcelAccessToken = WXWorkAPI.INFORMATION_EXCEL.replace("ACCESS_TOKEN", customerToken);
//                parm[0] = new JSONObject();
//                parm[0].put("docid", docId);
//                JSONObject informationExcelJsonObject = WXWorkAPI.sendPostBody_Map(informationExcelAccessToken, parm[0]);
//                List<VisaOfficialDTO> finalServiceOrderList = officialList;
//                Thread thread1 = new Thread(() -> {
//                    try {
//                        // 线程1的任务
//                        if ("0".equals(informationExcelJsonObject.get("errcode").toString())) {
//                            JSONArray propertiesObjects = JSONArray.parseArray(JSONObject.toJSONString(informationExcelJsonObject.get("properties")));
//                            Iterator<Object> iterator = propertiesObjects.iterator();
//                            String sheetId = JSONObject.parseObject(iterator.next().toString()).get("sheet_id").toString();
//                            setupExcelDO.setSheetId(sheetId);
//                            int i = wxWorkService.addExcel(setupExcelDO);
//                            if (i > 0) {
//                                String redactExcelAccessToken = WXWorkAPI.REDACT_EXCEL.replace("ACCESS_TOKEN", customerToken);
//                                parm[0] = new JSONObject();
//                                parm[0].put("docid", docId);
//
//                                List<JSONObject> requests = new ArrayList<>();
//                                JSONObject requestsJson = new JSONObject();
//                                JSONObject updateRangeRequest = new JSONObject();
//                                JSONObject gridData = new JSONObject();
//                                int count = 0;
//
//                                List<String> excelTitle = new ArrayList<>();
//                                excelTitle.add("文案佣金ID");
//                                excelTitle.add("服务订单ID");
//                                excelTitle.add("提交移民局申请时间");
//                                excelTitle.add("服务订单创建日期");
//                                excelTitle.add("客户姓名");
//                                excelTitle.add("申请人姓名");
//                                excelTitle.add("客户支付日期");
//                                excelTitle.add("支付币种");
//                                excelTitle.add("创建订单时汇率");
//                                excelTitle.add("收款方式");
//                                excelTitle.add("服务项目");
//                                excelTitle.add("所属顾问");
//                                excelTitle.add("所属文案");
//                                excelTitle.add("MARA");
//                                excelTitle.add("总计应收澳币");
//                                excelTitle.add("总计应收人民币");
//                                excelTitle.add("计入佣金提点金额（预估）");
//                                excelTitle.add("计入佣金提点金额（确认）");
//                                excelTitle.add("预估佣金（澳币）");
//                                excelTitle.add("预估佣金（人民币）");
//                                excelTitle.add("是否合账");
//                                excelTitle.add("状态");
//
//                                for (VisaOfficialDTO serviceOrderDTO : finalServiceOrderList) {
//                                    if (count == 0) {
//                                        gridData.put("start_row", 0);
//                                        gridData.put("start_column", 0);
//                                        List<JSONObject> rows = new ArrayList<>();
//                                        for (String title : excelTitle) {
//                                            JSONObject jsonObject = new JSONObject();
//                                            JSONObject text = new JSONObject();
//                                            text.put("text", title);
//                                            jsonObject.put("cell_value", text);
//                                            rows.add(jsonObject);
//                                        }
//                                        List<JSONObject> objects = new ArrayList<>();
//                                        JSONObject rowsValue = new JSONObject();
//                                        rowsValue.put("values", rows);
//                                        objects.add(rowsValue);
//                                        gridData.put("rows", objects);
//                                        updateRangeRequest.put("sheet_id", sheetId);
//                                        updateRangeRequest.put("grid_data", gridData);
//                                        requestsJson.put("update_range_request", updateRangeRequest);
//                                        requests.add(requestsJson);
//                                        parm[0].put("requests", requests);
//                                        count++;
//                                        WXWorkAPI.sendPostBody_Map(redactExcelAccessToken, parm[0]);
//                                        parm[0] = new JSONObject();
//                                        requests.remove(0);
//                                    }
//                                    parm[0].put("docid", docId);
//                                    gridData.put("start_row", count);
//                                    gridData.put("start_column", 0);
//                                    List<JSONObject> rows = build(serviceOrderDTO);
//                                    List<JSONObject> objects = new ArrayList<>();
//                                    JSONObject rowsValue = new JSONObject();
//                                    rowsValue.put("values", rows);
//                                    objects.add(rowsValue);
//                                    gridData.put("rows", objects);
//                                    updateRangeRequest.put("sheet_id", sheetId);
//                                    updateRangeRequest.put("grid_data", gridData);
//                                    requestsJson.put("update_range_request", updateRangeRequest);
//                                    requests.add(requestsJson);
//                                    parm[0].put("requests", requests);
//                                    count++;
//                                    WXWorkAPI.sendPostBody_Map(redactExcelAccessToken, parm[0]);
//                                    parm[0] = new JSONObject();
//                                    requests.remove(0);
//                                }
//                            }
//                        }
//                    } catch (Exception e) {
//                        // 处理异常，例如记录日志
//                        e.printStackTrace();
//                    }
//                });
//                thread1.start();
//            }
//            // 使用StringBuilder来构建HTML字符串
//            StringBuilder htmlBuilder = new StringBuilder();
//            htmlBuilder.append("<a href=\"");
//            htmlBuilder.append(url + "\""); // 插入链接的URL
//            htmlBuilder.append(" target=\"_blank");
//            htmlBuilder.append("\">");
//            htmlBuilder.append("点击打开Excel链接"); // 插入链接的显示文本
//            htmlBuilder.append("</a>");
//            WXWorkAPI.sendShareLinkMsg(url, adminUserLoginInfo.getUsername(), "导出文案佣金订单信息");
//            return new Response<>(0, "生成Excel成功， excel链接为：" + htmlBuilder);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return new Response<>(0, "生成Excel成功， excel链接为：");
//    }

    @RequestMapping(value = "/downOfficialCommission_V2", method = RequestMethod.GET)
    @ResponseBody
    public Response<String> downOfficialCommission_V2(
            @RequestParam(value = "id", required = false) Integer id,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "startHandlingDate", required = false) String startHandlingDate,
            @RequestParam(value = "endHandlingDate", required = false) String endHandlingDate,
            @RequestParam(value = "regionId", required = false) Integer regionId,
            @RequestParam(value = "officialId", required = false) Integer officialId,
            @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "applicantName", required = false) String applicantName,
            @RequestParam(value = "currency", required = false) String currency,
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
            List<VisaOfficialDTO> officialList = visaOfficialService.listVisaOfficialOrder(officialId, regionIdList, id, startHandlingDate, endHandlingDate, state,
                    startDate, endDate, null, null, userName, name, null, null, null, null, null, currency);

            // 获取token
            Map<String, Object> tokenMap = wxWorkService.getToken(WXWorkAPI.SECRET_EXCEL);
            if ((int) tokenMap.get("errcode") != 0) {
                throw new RuntimeException(tokenMap.get("errmsg").toString());
            }
            String customerToken = (String) tokenMap.get("access_token");

            // 创建表格
            // 创建表格
            String setupExcelAccessToken = WXWorkAPI.SETUP_EXCEL.replace("ACCESS_TOKEN", customerToken);
            final JSONObject[] parm = {new JSONObject()};
            parm[0].put("doc_type", 10);
            parm[0].put("doc_name", "officialVisa-" + sdf.format(new Date()));
            log.info("parm--------------------" + Arrays.toString(parm));
            log.info("setupExcelAccessToken-------------------" + setupExcelAccessToken);

            JSONObject setupExcelJsonObject = WXWorkAPI.sendPostBody_Map(setupExcelAccessToken, parm[0]);
            log.info("setupExcelJsonObject-------------" + setupExcelJsonObject.toString());
            String docid = setupExcelJsonObject.get("docid").toString();

            // 添加子表
            String accessTokenZiBiao = WXWorkAPI.CREATE_CHILE_TABLE.replace("ACCESS_TOKEN", customerToken);
            final JSONObject[] parmZiBiao = {new JSONObject()};
            parmZiBiao[0].put("docid", docid);
            JSONObject jsonObjectProperties = new JSONObject();
            jsonObjectProperties.put("title", "文案佣金订单导出信息");
            jsonObjectProperties.put("index", 2);
            parmZiBiao[0].put("properties", jsonObjectProperties);
            JSONObject jsonObject1 = WXWorkAPI.sendPostBody_Map(accessTokenZiBiao, parmZiBiao[0]);
            log.info("setupExcelJsonObject-------------" + jsonObject1.toString());

            // 获得sheetId
            Object properties = jsonObject1.get("properties");
            JSONObject jsonObject4 = JSONObject.parseObject(properties.toString());
            String sheetId = jsonObject4.get("sheet_id").toString();
            log.info("sheet_id-------------------" + sheetId);

            // 查询默认字段id
            String accessTokenMoRen = WXWorkAPI.GET_DEFAULT_FIELD.replace("ACCESS_TOKEN", customerToken);
            final JSONObject[] parmMoRen = {new JSONObject()};
            parmMoRen[0].put("docid", docid);
            parmMoRen[0].put("sheet_id", sheetId);
            parmMoRen[0].put("offset", 0);
            parmMoRen[0].put("limit", 10);
            JSONObject jsonObject5 = WXWorkAPI.sendPostBody_Map(accessTokenMoRen, parmMoRen[0]);
            log.info("setupExcelJsonObject-------------" + jsonObject5.toString());

            // 获取默认字段id
            String fieldId = "";
            Object fields = jsonObject5.get("fields");
            JSONArray jsonArray = JSONArray.parseArray(fields.toString());
            Iterator<Object> iterator = jsonArray.iterator();
            while (iterator.hasNext()) {
                Object next = iterator.next();
                JSONObject jsonObject = JSONObject.parseObject(next.toString());
                fieldId = jsonObject.get("field_id").toString();
                log.info("字段id----------------------" + fieldId);
            }

            // 更新字段
            String accessToken2 = WXWorkAPI.UPDATE_FIELD.replace("ACCESS_TOKEN", customerToken);
            final JSONObject[] parm2 = {new JSONObject()};
            parm2[0].put("docid", docid);
            parm2[0].put("sheet_id", sheetId);
            // 添加字段标题title
            List<String> exlceTitles = buildExlceTitle(currency);
            List<JSONObject> fieldList = new ArrayList<>();
            for (String exlceTitle : exlceTitles) {
                JSONObject jsonObjectField = new JSONObject();
                jsonObjectField.put("field_title", exlceTitle);
                jsonObjectField.put("field_type", "FIELD_TYPE_TEXT");
                fieldList.add(jsonObjectField);
            }
            parm2[0].put("fields", fieldList);
            JSONObject jsonObject2 = WXWorkAPI.sendPostBody_Map(accessToken2, parm2[0]);
            log.info("setupExcelJsonObject-------------" + jsonObject2.toString());

            // 删除字段
            String accessTokenShanChu = WXWorkAPI.DELETE_FIELD.replace("ACCESS_TOKEN", customerToken);
            final JSONObject[] parmShanChu = {new JSONObject()};
            parmShanChu[0].put("docid", docid);
            parmShanChu[0].put("sheet_id", sheetId);
            List<String> fielIds = new ArrayList<>();
            fielIds.add(fieldId);
            parmShanChu[0].put("field_ids", fielIds);
            JSONObject jsonObjectShanChu = WXWorkAPI.sendPostBody_Map(accessTokenShanChu, parmShanChu[0]);
            log.info("setupExcelJsonObject-------------" + jsonObjectShanChu.toString());

            String url = "";
            if ("0".equals(jsonObject2.get("errcode").toString())) {
                url = setupExcelJsonObject.get("url").toString();
                String docId = setupExcelJsonObject.get("docid").toString();
                SetupExcelDO setupExcelDO = new SetupExcelDO();
                setupExcelDO.setUrl(url);
                setupExcelDO.setDocId(docId);
                List<VisaOfficialDTO> finalServiceOrderList = officialList;
                Thread thread1 = new Thread(() -> {
                    try {
                        // 添加行记录
                        String accessTokenJiLu = WXWorkAPI.INSERT_ROW.replace("ACCESS_TOKEN", customerToken);
                        final JSONObject[] parmJiLu = {new JSONObject()};
                        parmJiLu[0].put("docid", docid);
                        parmJiLu[0].put("sheet_id", sheetId);
                        for (VisaOfficialDTO visaOfficialDTO : finalServiceOrderList) {
                            ServiceOrderDTO serviceOrderById = serviceOrderService.getServiceOrderById(visaOfficialDTO.getServiceOrderId());
                            JSONObject jsonObjectFILEDTITLE = buileExcelJsonObject(visaOfficialDTO, currency, serviceOrderById);
                            List<JSONObject> recordsList = new ArrayList<>();
                            JSONObject jsonObjectValue = new JSONObject();
                            jsonObjectValue.put("values", jsonObjectFILEDTITLE);
                            recordsList.add(jsonObjectValue);

                            parmJiLu[0].put("records", recordsList);
                            JSONObject jsonObjectJiLu = WXWorkAPI.sendPostBody_Map(accessTokenJiLu, parmJiLu[0]);
                            log.info(accessTokenJiLu);
                            log.info("jsonObjectJiLu-------------" + jsonObjectJiLu.toString());
                        }
                    } catch (Exception e) {
                        // 处理异常，例如记录日志
                        e.printStackTrace();
                    }
                });
                thread1.start();
            }
            // 使用StringBuilder来构建HTML字符串
            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<a href=\"");
            htmlBuilder.append(url + "\""); // 插入链接的URL
            htmlBuilder.append(" target=\"_blank");
            htmlBuilder.append("\">");
            htmlBuilder.append("点击打开Excel链接"); // 插入链接的显示文本
            htmlBuilder.append("</a>");
//            WXWorkAPI.sendShareLinkMsg(url, adminUserLoginInfo.getUsername(), "导出文案佣金订单信息");
            return new Response<>(0, "生成Excel成功， excel链接为：" + htmlBuilder);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Response<>(0, "生成Excel成功， excel链接为：");
    }

    private JSONObject buileExcelJsonObject(VisaOfficialDTO so, String currency, ServiceOrderDTO serviceOrderById) throws ServiceException {
        List<JSONObject> jsonObjectFILEDTITLEList = new ArrayList<>();
        JSONObject jsonObjectFILEDTITLE = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        String peopleType = serviceOrderById.getPeopleType();
        String isInsuranceCompany = serviceOrderById.getIsInsuranceCompany();
        // 文案佣金ID
        buildJsonobjectRow(String.valueOf(so.getId()), "文案佣金ID", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
        // 服务订单ID
        buildJsonobjectRow(String.valueOf(so.getServiceOrderId()), "服务订单ID", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
        // 提交移民局申请时间
        buildJsonobjectRow(so.getHandlingDate() == null ? "" : sdf.format(so.getHandlingDate()), "提交移民局申请时间", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
        // 服务订单创建日期
        buildJsonobjectRow(sdf.format(so.getServiceOrder().getGmtCreate()), "服务订单创建日期", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
        // 客户姓名
        buildJsonobjectRow(so.getUserName(), "客户姓名", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
        // 申请人姓名
        buildJsonobjectRow(so.getApplicant().get(0).getFirstname(), "申请人姓名", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
        // 客户支付日期
        buildJsonobjectRow(so.getReceiveDate() == null ? "" : sdf.format(so.getReceiveDate()), "客户支付日期", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
        // 支付币种
        buildJsonobjectRow(so.getCurrency(), "支付币种", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
        // 创建订单时汇率
        buildJsonobjectRow(String.valueOf(so.getExchangeRate()), "创建订单时汇率", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
        // 收款方式
        buildJsonobjectRow(so.getReceiveTypeName() == null ? "" : so.getReceiveTypeName(), "收款方式", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
        // 服务项目
        String servicePackageType = "";
        if (so.getServiceOrder().getApplicantParentId() > 0 && "SIV".equals(serviceOrderService.getServiceOrderById(so.getServiceOrder().getApplicantParentId()).getType())) {
            servicePackageType = "-" + so.getServiceOrder().getServicePackage().getType();
        }
        buildJsonobjectRow(StringUtil.merge(so.getServiceOrder().getService().getName(), "-", so.getServiceCode(), servicePackageType), "服务项目", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
        // 所属顾问
        buildJsonobjectRow(so.getAdviserName(), "所属顾问", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
        // 所属文案
        buildJsonobjectRow(so.getOfficialName(), "所属文案", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
        // MARA
        buildJsonobjectRow(so.getMaraDTO() == null || so.getMaraDTO().getName() == null ? "" : so.getMaraDTO().getName(), "MARA", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
        // 总计应收澳币
        buildJsonobjectRow(String.valueOf(so.getTotalPerAmountAUD()), "总计应收澳币", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
        // 总计应收人民币
        buildJsonobjectRow(String.valueOf(so.getTotalAmountCNY()), "总计应收人民币", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
        // 计入佣金提点金额（预估）
        buildJsonobjectRow(String.valueOf(so.getPredictCommissionAmount()), "计入佣金提点金额（预估）", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
        // 计入佣金提点金额（确认）
        buildJsonobjectRow(so.getCommissionAmount() == null ? "" : String.valueOf(so.getCommissionAmount()), "计入佣金提点金额（确认）", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
        // 预估佣金（澳币）
        buildJsonobjectRow(so.getPredictCommission() == null ? "" : String.valueOf(so.getPredictCommission()), "预估佣金（澳币）", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
        // 预估佣金（人民币）
        buildJsonobjectRow(so.getPredictCommissionCNY() == null ? "" : String.valueOf(so.getPredictCommissionCNY()), "预估佣金（人民币）", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
        if ("CNY".equalsIgnoreCase(currency)) {
            buildJsonobjectRow("0", "带孩子（CNY）", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
            buildJsonobjectRow("0", "带配偶（CNY）", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
            // 带孩子（CNY）
            // 带配偶（CNY）
            if ("2A".equalsIgnoreCase(peopleType)) {
                buildJsonobjectRow("50", "带配偶（CNY）", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
            }
            if ("XA".equalsIgnoreCase(peopleType)) {
                buildJsonobjectRow("25", "带孩子（CNY）", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
            }
            if ("XB".equalsIgnoreCase(peopleType)) {
                buildJsonobjectRow("25", "带孩子（CNY）", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
                buildJsonobjectRow("50", "带配偶（CNY）", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
            }
        }
        DecimalFormat df = new DecimalFormat("#.00");
        if ("AUD".equalsIgnoreCase(currency)) {
            buildJsonobjectRow("0", "带孩子（AUD）", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
            buildJsonobjectRow("0", "带配偶（AUD）", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
            // 带孩子（AUD）
            // 带配偶（AUD）
            if ("2A".equalsIgnoreCase(peopleType)) {
                double additionalAmount = 50 / so.getExchangeRate();
                buildJsonobjectRow(df.format(additionalAmount), "带配偶（AUD）", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
            }
            if ("XA".equalsIgnoreCase(peopleType)) {
                double additionalAmount = 25 / so.getExchangeRate();
                buildJsonobjectRow(df.format(additionalAmount), "带孩子（AUD）", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
            }
            if ("XB".equalsIgnoreCase(peopleType)) {
                double additionalAmount2A = 50 / so.getExchangeRate();
                double additionalAmountXA = 25 / so.getExchangeRate();
                buildJsonobjectRow(df.format(additionalAmountXA), "带孩子（AUD）", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
                buildJsonobjectRow(df.format(additionalAmount2A), "带配偶（AUD）", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
            }
        }
        if ("ALL".equalsIgnoreCase(currency)) {
            double additionalAmount2A = 50 / so.getExchangeRate();
            double additionalAmountXA = 25 / so.getExchangeRate();
            buildJsonobjectRow("25", "带孩子（CNY）", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
            buildJsonobjectRow("50", "带配偶（CNY）", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
            buildJsonobjectRow(df.format(additionalAmountXA), "带孩子（AUD）", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
            buildJsonobjectRow(df.format(additionalAmount2A), "带配偶（AUD）", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
        }
        // 买保险
        buildJsonobjectRow(isInsuranceCompany == null ? "" : ("1".equalsIgnoreCase(isInsuranceCompany) ? "是" : "否"), "买保险", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
        // total（AUD）
        if ("AUD".equalsIgnoreCase(currency)) {
            buildJsonobjectRow(so.getPredictCommission() == null ? "" : String.valueOf(so.getPredictCommission()), "total（AUD）", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
        }
        // total（CNY）
        if ("CNY".equalsIgnoreCase(currency)) {
            buildJsonobjectRow(so.getPredictCommissionCNY() == null ? "" : String.valueOf(so.getPredictCommissionCNY()), "total（CNY）", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
        }
        if ("ALL".equalsIgnoreCase(currency)) {
            buildJsonobjectRow(so.getPredictCommission() == null ? "" : String.valueOf(so.getPredictCommission()), "total（AUD）", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
            buildJsonobjectRow(so.getPredictCommissionCNY() == null ? "" : String.valueOf(so.getPredictCommissionCNY()), "total（CNY）", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
        }
        // 是否合账
        buildJsonobjectRow(so.isMerged() ? "是" : "否", "是否合账", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
        // 状态
        String states = so.getState() == null ? "" : so.getState();
        buildJsonobjectRow(states.equalsIgnoreCase("COMPLETE") ? "已确认" : states, "状态", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
        return jsonObjectFILEDTITLE;
    }

    public void buildJsonobjectRow(String value, String title, JSONObject jsonObject, List<JSONObject> jsonObjectFILEDTITLEList, JSONObject jsonObjectFILEDTITLE) {
        jsonObject = new JSONObject();
        jsonObject.put("type", "text");
        jsonObjectFILEDTITLEList = new ArrayList<>();
        jsonObject.put("text", value);
        jsonObjectFILEDTITLEList.add(jsonObject);
        jsonObjectFILEDTITLE.put(title, jsonObjectFILEDTITLEList);
    }

    public List<String> buildExlceTitle(String currency) {
        List<String> excelTitle = new ArrayList<>();
        excelTitle.add("状态");
        excelTitle.add("是否合账");
        if ("AUD".equalsIgnoreCase(currency)) {
            excelTitle.add("total（AUD）");
        }
        if ("CNY".equalsIgnoreCase(currency)) {
            excelTitle.add("total（CNY）");
        }
        if ("ALL".equalsIgnoreCase(currency)) {
            excelTitle.add("total（AUD）");
            excelTitle.add("total（CNY）");
        }
        excelTitle.add("买保险");
        if ("AUD".equalsIgnoreCase(currency)) {
            excelTitle.add("带配偶（AUD）");
            excelTitle.add("带孩子（AUD）");
        }
        if ("CNY".equalsIgnoreCase(currency)) {
            excelTitle.add("带配偶（CNY）");
            excelTitle.add("带孩子（CNY）");
        }
        if ("ALL".equalsIgnoreCase(currency)) {
            excelTitle.add("带配偶（AUD）");
            excelTitle.add("带孩子（AUD）");
            excelTitle.add("带配偶（CNY）");
            excelTitle.add("带孩子（CNY）");
        }
        excelTitle.add("预估佣金（人民币）");
        excelTitle.add("预估佣金（澳币）");
        excelTitle.add("计入佣金提点金额（确认）");
        excelTitle.add("计入佣金提点金额（预估）");
        excelTitle.add("总计应收人民币");
        excelTitle.add("总计应收澳币");
        excelTitle.add("MARA");
        excelTitle.add("所属文案");
        excelTitle.add("所属顾问");
        excelTitle.add("服务项目");
        excelTitle.add("收款方式");
        excelTitle.add("创建订单时汇率");
        excelTitle.add("支付币种");
        excelTitle.add("客户支付日期");
        excelTitle.add("申请人姓名");
        excelTitle.add("客户姓名");
        excelTitle.add("服务订单创建日期");
        excelTitle.add("提交移民局申请时间");
        excelTitle.add("服务订单ID");
        excelTitle.add("文案佣金ID");
        return excelTitle;
    }


    private List<JSONObject> build(VisaOfficialDTO so) throws ServiceException {
        List<JSONObject> rows = new ArrayList<>();
        // 文案佣金订单
        JSONObject jsonObject = new JSONObject();
        JSONObject text = new JSONObject();
        text.put("text", String.valueOf(so.getId()));
        jsonObject.put("cell_value", text);
        rows.add(jsonObject);
        // 服务订单ID");
        JSONObject jsonObject1 = new JSONObject();
        JSONObject text1 = new JSONObject();
        text1.put("text", String.valueOf(so.getServiceOrderId()));
        jsonObject1.put("cell_value", text1);
        rows.add(jsonObject1);
        // 提交移民局申请时间");
        JSONObject jsonObject2 = new JSONObject();
        JSONObject text2 = new JSONObject();
        text2.put("text", so.getHandlingDate() == null ? "" : sdf.format(so.getHandlingDate()));
        jsonObject2.put("cell_value", text2);
        rows.add(jsonObject2);
        // 服务订单创建日期");
        JSONObject jsonObject3 = new JSONObject();
        JSONObject text3 = new JSONObject();
        text3.put("text", sdf.format(so.getServiceOrder().getGmtCreate()));
        jsonObject3.put("cell_value", text3);
        rows.add(jsonObject3);
        // 客户姓名");
        JSONObject jsonObject4 = new JSONObject();
        JSONObject text4 = new JSONObject();
        text4.put("text", so.getUserName());
        jsonObject4.put("cell_value", text4);
        rows.add(jsonObject4);
        // 申请人姓名");
        JSONObject jsonObject5 = new JSONObject();
        JSONObject text5 = new JSONObject();
        text5.put("text", StringUtil.merge(so.getApplicant().get(0).getFirstname(), " ", so.getApplicant().get(0).getSurname()));
        jsonObject5.put("cell_value", text5);
        rows.add(jsonObject5);
        // 客户支付日期");
        JSONObject jsonObject6 = new JSONObject();
        JSONObject text6 = new JSONObject();
        text6.put("text", so.getReceiveDate() == null ? "" : sdf.format(so.getReceiveDate()));
        jsonObject6.put("cell_value", text6);
        rows.add(jsonObject6);
        // 支付币种");
        JSONObject jsonObject7 = new JSONObject();
        JSONObject text7 = new JSONObject();
        text7.put("text", so.getCurrency());
        jsonObject7.put("cell_value", text7);
        rows.add(jsonObject7);
        // 创建订单时汇率");
        JSONObject jsonObject8 = new JSONObject();
        JSONObject text8 = new JSONObject();
        text8.put("text", String.valueOf(so.getExchangeRate()));
        jsonObject8.put("cell_value", text8);
        rows.add(jsonObject8);
        // 收款方式");
        JSONObject jsonObject9 = new JSONObject();
        JSONObject text9 = new JSONObject();
        text9.put("text", so.getReceiveTypeName() == null ? "" : so.getReceiveTypeName());
        jsonObject9.put("cell_value", text9);
        rows.add(jsonObject9);
        // 服务项目");
        String servicePackageType = "";
        if (so.getServiceOrder().getApplicantParentId() > 0 && "SIV".equals(serviceOrderService.getServiceOrderById(so.getServiceOrder().getApplicantParentId()).getType())) {
            servicePackageType = "-" + so.getServiceOrder().getServicePackage().getType();
        }
        JSONObject jsonObject10 = new JSONObject();
        JSONObject text10 = new JSONObject();
        text10.put("text", StringUtil.merge(so.getServiceOrder().getService().getName(), "-", so.getServiceCode(), servicePackageType));
        jsonObject10.put("cell_value", text10);
        rows.add(jsonObject10);
        // 所属顾问");
        JSONObject jsonObject11 = new JSONObject();
        JSONObject text11 = new JSONObject();
        text11.put("text", so.getAdviserName());
        jsonObject11.put("cell_value", text11);
        rows.add(jsonObject11);
        // 所属文案");
        JSONObject jsonObject12 = new JSONObject();
        JSONObject text12 = new JSONObject();
        text12.put("text", so.getOfficialName());
        jsonObject12.put("cell_value", text12);
        rows.add(jsonObject12);
        // MARA");
        JSONObject jsonObject13 = new JSONObject();
        JSONObject text13 = new JSONObject();
        text13.put("text", so.getMaraDTO() == null || so.getMaraDTO().getName() == null ? "" : so.getMaraDTO().getName());
        jsonObject13.put("cell_value", text13);
        rows.add(jsonObject13);
        // 总计应收澳币");
        JSONObject jsonObject14 = new JSONObject();
        JSONObject text14 = new JSONObject();
        text14.put("text", String.valueOf(so.getTotalPerAmountAUD()));
        jsonObject14.put("cell_value", text14);
        rows.add(jsonObject14);
        // 总计应收人民币");
        JSONObject jsonObject15 = new JSONObject();
        JSONObject text15 = new JSONObject();
        text15.put("text", String.valueOf(so.getTotalAmountCNY()));
        jsonObject15.put("cell_value", text15);
        rows.add(jsonObject15);
        // 计入佣金提点金额（预估）");
        JSONObject jsonObject16 = new JSONObject();
        JSONObject text16 = new JSONObject();
        text16.put("text", String.valueOf(so.getPredictCommissionAmount()));
        jsonObject16.put("cell_value", text16);
        rows.add(jsonObject16);
        // 计入佣金提点金额（确认）");
        JSONObject jsonObject17 = new JSONObject();
        JSONObject text17 = new JSONObject();
        text17.put("text", so.getCommissionAmount() == null ? "" : String.valueOf(so.getCommissionAmount()));
        jsonObject17.put("cell_value", text17);
        rows.add(jsonObject17);
        // 预估佣金（澳币）");
        JSONObject jsonObject18 = new JSONObject();
        JSONObject text18 = new JSONObject();
        text18.put("text", so.getPredictCommission() == null ? "" : String.valueOf(so.getPredictCommission()));
        jsonObject18.put("cell_value", text18);
        rows.add(jsonObject18);
        // 预估佣金（人民币）");
        JSONObject jsonObject19 = new JSONObject();
        JSONObject text19 = new JSONObject();
        text19.put("text", so.getPredictCommissionCNY() == null ? "" : String.valueOf(so.getPredictCommissionCNY()));
        jsonObject19.put("cell_value", text19);
        rows.add(jsonObject19);
        // 是否合账");
        JSONObject jsonObject20 = new JSONObject();
        JSONObject text20 = new JSONObject();
        text20.put("text", so.isMerged() ? "是" : "否");
        jsonObject20.put("cell_value", text20);
        rows.add(jsonObject20);
        // 状态");
        String states = so.getState() == null ? "" : so.getState();
        JSONObject jsonObject21 = new JSONObject();
        JSONObject text21 = new JSONObject();
        text21.put("text", states.equalsIgnoreCase("COMPLETE") ? "已确认" : states);
        jsonObject21.put("cell_value", text21);
        rows.add(jsonObject21);

        return rows;
    }


    @RequestMapping(value = "/uploadOfficialCommission", method = RequestMethod.POST)
    @ResponseBody
    public Response<Integer> uploadOfficialCommission(@RequestParam MultipartFile file, HttpServletRequest request,
                                                      HttpServletResponse response) throws IllegalStateException, IOException {
        super.setPostHeader(response);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String message = "";
        int n = 0;
        Response<String> r = super.upload2(file, request.getSession(), "/tmp/");
        try (InputStream is = new FileInputStream("/data" + r.getData())) {
            jxl.Workbook wb = jxl.Workbook.getWorkbook(is);
            Sheet sheet = wb.getSheet(0);
            for (int i = 1; i < sheet.getRows(); i++) {
                Cell[] cells = sheet.getRow(i);
                String _id = cells[1].getContents();
                String handling_date = cells[2].getContents();
                Double commissionAmount = Double.valueOf(cells[17].getContents());
                try {
                    ServiceOrderDTO order = serviceOrderService.getServiceOrderById(Integer.parseInt(_id));
                    if (order == null) {
                        message += "[" + _id + "]佣金订单不存在;";
                        continue;
                    }
                    try {

                        visaOfficialService.update(Integer.valueOf(_id), StringUtil.isEmpty(handling_date) ? null
                                : simpleDateFormat.parse(handling_date.trim()).getTime() + "", commissionAmount, null, null);
                    } catch (ServiceException s) {
                        message += "[" + _id + "修改失败";
                    }
                } catch (NumberFormatException | ServiceException | ParseException e) {
                    message += "[" + _id + "]" + e.getMessage() + ";";
                }
            }
        } catch (BiffException | IOException e) {
            return new Response<Integer>(1, "上传失败:" + e.getMessage(), 0);
        }
        return new Response<Integer>(0, message, n);
    }

    @RequestMapping(value = "/monthlyStatement", method = RequestMethod.GET)
    @ResponseBody
    public Response<Integer> monthlyStatement(HttpServletRequest request,
                                                      HttpServletResponse response) throws IllegalStateException, IOException {
        super.setPostHeader(response);
        try {
            List<VisaOfficialDO> visaOfficialDOs = visaOfficialService.monthlyStatement();
//            response.reset();// 清空输出流
//            String tableName = "ServiceOrderInformation";
//            response.setHeader("Content-disposition",
//                    "attachment; filename=" + new String(tableName.getBytes("GB2312"), "8859_1") + ".xls");
//            response.setContentType("application/msexcel");
//
//            OutputStream os = response.getOutputStream();
//            jxl.Workbook wb;
//            InputStream is;
//            try {
//                is = this.getClass().getResourceAsStream("/officialVisa_OVST.xls");
//            } catch (Exception e) {
//                throw new Exception("模版不存在");
//            }
//            try {
//                wb = Workbook.getWorkbook(is);
//            } catch (Exception e) {
//                e.printStackTrace();
//                throw new Exception("模版格式不支持");
//            }
//            WorkbookSettings settings = new WorkbookSettings();
//            settings.setWriteAccess(null);
//            jxl.write.WritableWorkbook wbe = Workbook.createWorkbook(os, wb, settings);
//            if (wbe == null) {
//                System.out.println("wbe is null !os=" + os + ",wb" + wb);
//            } else {
//                System.out.println("wbe not null !os=" + os + ",wb" + wb);
//            }
//            WritableSheet sheet = wbe.getSheet(0);
//            WritableCellFormat cellFormat = new WritableCellFormat();
//            int i = 1;
//            for (VisaOfficialDO e : visaOfficialDOs) {
//                UserDTO userById = userService.getUserById(e.getUserId());
//                ApplicantDTO applicantDTO = applicantService.getById(e.getServiceOrderDO().getApplicantId());
//                sheet.addCell(new Label(0, i, e.getId() + "", cellFormat));
//                // 创建一个SimpleDateFormat对象来定义日期和时间的格式
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//                // 使用SimpleDateFormat的format方法将Date对象格式化为字符串
//                String gmtCreate = sdf.format(e.getGmtCreate()); // 创建日期
//                String kjApprovalDate = sdf.format(e.getKjApprovalDate()); // 提交申请时间
//                String officialApprovalDate = sdf.format(e.getServiceOrderDO().getOfficialApprovalDate()); // 提交审核时间
//                String finishDate = sdf.format(e.getServiceOrderDO().getFinishDate()); // 办理完成时间
//                sheet.addCell(new Label(1, i, gmtCreate, cellFormat));
//                sheet.addCell(new Label(2, i, e.getServiceOrderId() + "", cellFormat));
//                sheet.addCell(new Label(3, i, kjApprovalDate, cellFormat));
//                sheet.addCell(new Label(4, i, finishDate, cellFormat));
//                sheet.addCell(new Label(5, i, officialApprovalDate, cellFormat));
//                sheet.addCell(new Label(6, i, userById.getName(), cellFormat));
//                sheet.addCell(new Label(7, i, applicantDTO.getFirstname() + applicantDTO.getSurname(), cellFormat));
//                i++;
//            }
//            wbe.write();
//            wbe.close();
            return new Response<Integer>(0, "本次结算订单" + visaOfficialDOs.size() + "条");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
}
