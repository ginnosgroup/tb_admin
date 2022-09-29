package org.zhinanzhen.b.controller;

import com.alibaba.fastjson.JSON;
import com.ikasoa.core.utils.ListUtil;
import com.ikasoa.core.utils.StringUtil;
import jxl.Cell;
import jxl.Sheet;
import jxl.read.biff.BiffException;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zhinanzhen.b.service.ApplicantService;
import org.zhinanzhen.b.service.OfficialService;
import org.zhinanzhen.b.service.VisaOfficialService;
import org.zhinanzhen.b.service.pojo.ApplicantDTO;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.b.service.pojo.VisaDTO;
import org.zhinanzhen.b.service.pojo.VisaOfficialDTO;
import org.zhinanzhen.b.service.pojo.ant.Sorter;
import org.zhinanzhen.tb.controller.ListResponse;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.RegionService;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.pojo.RegionDTO;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/visaOfficial")
public class VisaOfficialController extends BaseCommissionOrderController {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Resource
    VisaOfficialService visaOfficialService;

    @Resource
    ApplicantService applicantService;

    @Resource
    RegionService regionService;

    @Resource
    OfficialService officialService;
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
            visaDto.setGst(commission / 11);
            visaDto.setDeductGst(commission - visaDto.getGst());
            visaDto.setBonus(visaDto.getDeductGst() * 0.1);
            visaDto.setExpectAmount(commission);

            double _perAmount = 0.00;
            double _amount = 0.00;
            for (int installmentNum = 1; installmentNum <= installment; installmentNum++) {
                visaDto.setInstallmentNum(installmentNum);
                if (installmentNum > 1) { // 只给第一个添加支付凭证
                    visaDto.setPaymentVoucherImageUrl1(null);
                    visaDto.setPaymentVoucherImageUrl2(null);
                    visaDto.setPaymentVoucherImageUrl3(null);
                    visaDto.setPaymentVoucherImageUrl4(null);
                    visaDto.setPaymentVoucherImageUrl5(null);
                    visaDto.setState(ReviewKjStateEnum.PENDING.toString());
                    visaDto.setVerifyCode(null);// 只给第一笔对账verifyCode
                    visaDto.setKjApprovalDate(null);
                    visaDto.setReceiveDate(null);
                    visaDto.setPerAmount(_receivable > _perAmount ? _receivable - _perAmount : 0.00); // 第二笔单子修改本次应收款
                    visaDto.setAmount(visaDto.getPerAmount());
                    visaDto.setDiscount(0.00);
                } else {
                    visaDto.setState(ReviewKjStateEnum.REVIEW.toString()); // 第一笔单子直接进入财务审核状态
                    if (StringUtil.isNotEmpty(verifyCode))// 只给第一笔赋值verifyCode
                        visaDto.setVerifyCode(verifyCode.replace("$", "").replace("#", "").replace(" ", ""));
                    visaDto.setKjApprovalDate(new Date());
                }
                if (visaOfficialService.addVisa(visaDto) > 0)
                    visaOfficialDTOList.add(visaDto);
                _perAmount += visaDto.getPerAmount();
                _amount += visaDto.getAmount();
            }
            serviceOrderDto.setOfficialApprovalDate(new Date());
            serviceOrderDto.setSubmitted(true);
            serviceOrderService.updateServiceOrder(serviceOrderDto);
            if (serviceOrderDto.getParentId() > 0) {
                ServiceOrderDTO _serviceOrderDto = serviceOrderService
                        .getServiceOrderById(serviceOrderDto.getParentId());
                if (_serviceOrderDto != null && !_serviceOrderDto.isSubmitted()) {
                    _serviceOrderDto.setSubmitted(true);
                    serviceOrderService.updateServiceOrder(_serviceOrderDto);
                }
            }
            ApplicantDTO applicantDto = serviceOrderDto.getApplicant();
            String msg = "";
            if (applicantDto != null && applicantBirthday != null) {
                applicantDto.setBirthday(new Date(Long.parseLong(applicantBirthday)));
                if (applicantService.update(applicantDto) <= 0)
                    msg += "申请人生日修改失败! (serviceOrderId:" + serviceOrderDto.getId() + ", applicantId:"
                            + applicantDto.getId() + ", applicantBirthday:" + applicantDto.getBirthday() + ");";
                else
                    msg += "申请人生日修改成功. (serviceOrderId:" + serviceOrderDto.getId() + ", applicantId:"
                            + applicantDto.getId() + ", applicantBirthday:" + applicantDto.getBirthday() + ");";
            }
            return new Response<>(0, msg, visaOfficialDTOList);
        } catch (ServiceException e) {
            return new Response<>(e.getCode(), e.getMessage(), null);
        }
    }

    @RequestMapping(value = "/listVisaOfficial", method = RequestMethod.GET)
    @ResponseBody
    public ListResponse<List<VisaOfficialDTO>> listVisaOrder(
            @RequestParam(value = "id", required = false) Integer id,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "startSubmitIbDate", required = false) String startSubmitIbDate,
            @RequestParam(value = "endSubmitIbDate", required = false) String endSubmitIbDate,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "startHandlingDate", required = false) String startHandlingDate,
            @RequestParam(value = "endHandlingDate", required = false) String endHandlingDate,
            @RequestParam(value = "regionId", required = false) Integer regionId,
            @RequestParam(value = "officialId" ,required = false) Integer officialId,
            @RequestParam(value ="userName" ,required = false) String userName,
            @RequestParam(value ="applicantName" ,required = false) String applicantName,
            @RequestParam(value = "pageNum") Integer pageNum,
            @RequestParam(value = "pageSize") Integer pageSize,
            @RequestParam(value = "sorter", required = false) String sorter,HttpServletResponse response,
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
            if ("WA".equalsIgnoreCase(adminUserLoginInfo.getApList())
                    &&officialService.getOfficialById(newOfficialId).getIsOfficialAdmin() ) {
                List<RegionDTO> regionList = regionService.listRegion(officialService.getOfficialById(newOfficialId).getRegionId());
                regionIdList = ListUtil.buildArrayList(officialService.getOfficialById(newOfficialId).getRegionId());
                for (RegionDTO region : regionList)
                    regionIdList.add(region.getId());
            } else {
                // 更改当前文案编号
                if (newOfficialId != null)
                    officialId = newOfficialId;
                if (adminUserLoginInfo == null)
                    return new ListResponse<>(false, pageSize, 0, null, "No permission !");
                if ("WA".equalsIgnoreCase(adminUserLoginInfo.getApList()) && officialId == null)
                    return new ListResponse<>(false, pageSize, 0, null, "无法获取文案编号，请退出重新登录后再尝试．");
            }
            int count = visaOfficialService.count(officialId,regionIdList,id,startHandlingDate,endHandlingDate,state,startSubmitIbDate,endSubmitIbDate,startDate,endDate, userName, applicantName);
            final List<VisaOfficialDTO> officialDTOList = visaOfficialService.getVisaOfficialOrder(officialId, regionIdList, id, startHandlingDate, endHandlingDate, state, startSubmitIbDate, endSubmitIbDate, startDate,
                    endDate, userName, applicantName, pageNum, pageSize, _sorter);


            return new ListResponse(true, pageSize, count, officialDTOList, "查询成功");
        }catch (ServiceException e) {
            return new ListResponse<>(false, pageSize, 0, null, e.getMessage());
        }
    }

    @RequestMapping(value = "/updateOfficialVisa", method = RequestMethod.PUT)
    @ResponseBody
    public Response<String> update(
            @RequestParam(value = "id") Integer id,
            @RequestParam(value = "submitIbDate", required = false) String submitIbDate,
            @RequestParam(value = "commissionAmount", required = false) Double commissionAmount,
            @RequestParam(value="state",required = false) String state,
            HttpServletResponse response,HttpServletRequest request){
        super.setGetHeader(response);
        // 获取用户信息
        try {
            AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
            if(adminUserLoginInfo!=null){
                if(adminUserLoginInfo.getApList().equalsIgnoreCase("KJ")){
                    visaOfficialService.update(id,submitIbDate,commissionAmount,state);
                    return new Response<>(0,"修改成功");
                }
                if(adminUserLoginInfo.getApList().equalsIgnoreCase("WA")){
                    if(commissionAmount!=null||StringUtil.isNotEmpty(state)){
                        return new Response<>(1,"修改失败仅限财务修改");
                    }
                    visaOfficialService.update(id,submitIbDate,commissionAmount,state);
                    return new Response<>(0,"修改成功");
                }
            }
            return new Response<>(1,"修改失败,请登录");
        }catch (ServiceException e){
            return new Response<>(1,"修改失败"+e.getMessage());
        }
    }


}
