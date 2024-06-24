package org.zhinanzhen.b.controller;

import com.alibaba.fastjson.JSON;
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
import org.zhinanzhen.b.dao.pojo.VisaOfficialDO;
import org.zhinanzhen.b.service.ApplicantService;
import org.zhinanzhen.b.service.OfficialService;
import org.zhinanzhen.b.service.VisaOfficialService;
import org.zhinanzhen.b.service.VisaService;
import org.zhinanzhen.b.service.pojo.*;
import org.zhinanzhen.b.service.pojo.ant.Sorter;
import org.zhinanzhen.tb.controller.ListResponse;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.RegionService;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.UserService;
import org.zhinanzhen.tb.service.pojo.RegionDTO;
import org.zhinanzhen.tb.service.pojo.UserDTO;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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
            }
            else if (visaOfficialService.addVisa(visaDto) == -2) {
//                throw new ServiceException("当前打包签证中同时包含EOI和ROI，在EOI进行结算");
                return new Response<>(-1,"当前打包签证中同时包含EOI和ROI，在EOI进行结算");
            } else {
                return new Response<>(-1,"服务订单暂未付款完成");
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
            if(StringUtil.equals(isMerged, "true"))
            	merged = true;
            if(StringUtil.equals(isMerged, "false"))
            	merged = false;
            String name = applicantName;
            if (StringUtil.isNotEmpty(applicantName)) {
                name = applicantName.replaceAll("\\s", "");
            }
            int count = visaOfficialService.count(officialId, regionIdList, id, startHandlingDate, endHandlingDate, state, startDate, endDate, userName, name, merged);
            List<VisaOfficialDTO> officialDTOList = visaOfficialService.listVisaOfficialOrder(officialId, regionIdList, id, startHandlingDate, endHandlingDate, state, startDate,
                    endDate,null, null, userName, name, merged, pageNum, pageSize, _sorter, null);


            return new ListResponse(true, pageSize, count, officialDTOList, "查询成功");
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
            List<Integer> regionList = null;
            if (regionId != null) {
                regionList = new ArrayList<>();
                regionList.add(regionId);
            }
            AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
            if ("WA".equals(adminUserLoginInfo.getApList())) {
                officialId = adminUserLoginInfo.getOfficialId();
            }
            String name = applicantName;
            if (StringUtil.isNotEmpty(applicantName)) {
                name = applicantName.replaceAll("\\s", "");
            }
            List<VisaOfficialDTO> officialList = visaOfficialService.listVisaOfficialOrder(officialId, regionList, id, startHandlingDate, endHandlingDate, state,
                    startDate, endDate,null, null, userName, name, null, null, null, null, null);
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
