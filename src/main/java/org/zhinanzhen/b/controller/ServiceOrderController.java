package org.zhinanzhen.b.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ikasoa.core.ErrorCodeEnum;
import com.ikasoa.core.utils.ListUtil;
import com.ikasoa.core.utils.ObjectUtil;
import com.ikasoa.core.utils.StringUtil;
import com.ikasoa.web.workflow.Context;
import com.ikasoa.web.workflow.Node;
import com.ikasoa.web.workflow.Workflow;
import com.ikasoa.web.workflow.WorkflowStarter;
import com.ikasoa.web.workflow.impl.WorkflowStarterImpl;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zhinanzhen.b.controller.nodes.SONodeFactory;
import org.zhinanzhen.b.dao.pojo.ServiceOrderExportDTO;
import org.zhinanzhen.b.dao.pojo.ServiceOrderReadcommittedDateDO;
import org.zhinanzhen.b.dao.pojo.VisaDO;
import org.zhinanzhen.b.service.*;
import org.zhinanzhen.b.service.pojo.*;
import org.zhinanzhen.b.service.pojo.ant.Sorter;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.ListResponse;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.RegionService;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.UserService;
import org.zhinanzhen.tb.service.pojo.AdviserDTO;
import org.zhinanzhen.tb.service.pojo.RegionDTO;
import org.zhinanzhen.tb.utils.SendEmailUtil;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/serviceOrder")
public class ServiceOrderController extends BaseController {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceOrderController.class);

    private static WorkflowStarter workflowStarter = new WorkflowStarterImpl();

    @Resource
    ServiceOrderService serviceOrderService;

    @Resource
    VisaService visaService;

    @Resource
    ApplicantService applicantService;

    @Resource
    ServiceOrderApplicantService serviceOrderApplicantService;

    @Resource
    UserService userService;

    @Resource
    ServicePackageService servicePackageService;

    @Resource
    RegionService regionService;

    @Resource
    ServiceAssessService serviceAssessService;

    @Resource
    CommissionOrderService commissionOrderService;

    @Resource
    ServiceOrderReadcommittedDateService serviceOrderReadcommittedDateService;

    @Resource
    SONodeFactory soNodeFactory;

    @Resource
    WXWorkService wxWorkService;

    @Resource
    OfficialService officialService;

    @Resource
    VisaOfficialController visaOfficialController;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public enum ReviewAdviserStateEnum {
        PENDING, REVIEW, APPLY, COMPLETE, PAID, CLOSE;

        public static ReviewAdviserStateEnum get(String name) {
            for (ReviewAdviserStateEnum e : ReviewAdviserStateEnum.values())
                if (e.toString().equals(name))
                    return e;
            return null;
        }
    }

    public enum ReviewMaraStateEnum {
        WAIT, REVIEW, FINISH;

        public static ReviewMaraStateEnum get(String name) {
            for (ReviewMaraStateEnum e : ReviewMaraStateEnum.values())
                if (e.toString().equals(name))
                    return e;
            return null;
        }
    }

    public enum ReviewOfficialStateEnum {
        PENDING, WAIT, REVIEW, FINISH, APPLY, COMPLETE, PAID, CLOSE;

        public static ReviewOfficialStateEnum get(String name) {
            for (ReviewOfficialStateEnum e : ReviewOfficialStateEnum.values())
                if (e.toString().equals(name))
                    return e;
            return null;
        }
    }

    public enum ServiceOrderReviewStateEnum {
        ADVISER, OFFICIAL, MARA, KJ;

        public static ServiceOrderReviewStateEnum get(String name) {
            for (ServiceOrderReviewStateEnum e : ServiceOrderReviewStateEnum.values())
                if (e.toString().equals(name))
                    return e;
            return null;
        }
    }

    public enum ServiceOrderTypeEnum {
        OVST("留学"), VISA("签证"), SIV("独立技术移民"), NSV("雇主担保"), ZX("咨询");

        private String meaning;

        ServiceOrderTypeEnum(String meaning) {
            this.meaning = meaning;
        }

        public static ServiceOrderTypeEnum get(String name) {
            for (ServiceOrderTypeEnum e : ServiceOrderTypeEnum.values())
                if (e.toString().equals(name))
                    return e;
            return null;
        }
    }

    @RequestMapping(value = "/upload_img", method = RequestMethod.POST)
    @ResponseBody
    public Response<String> uploadImage(@RequestParam MultipartFile file, HttpServletRequest request,
                                        HttpServletResponse response) throws IllegalStateException, IOException {
        super.setPostHeader(response);
        return super.upload2(file, request.getSession(), "/uploads/payment_voucher_image_url_s2/");
    }

    @RequestMapping(value = "/upload_visa_voucher_img", method = RequestMethod.POST)
    @ResponseBody
    public Response<String> uploadVisaVoucherImage(@RequestParam MultipartFile file, HttpServletRequest request,
                                                   HttpServletResponse response) throws IllegalStateException, IOException {
        super.setPostHeader(response);
        return super.upload2(file, request.getSession(), "/uploads/visa_voucher_image_url/");
    }

    @RequestMapping(value = "/upload_invoice_voucher_img", method = RequestMethod.POST)
    @ResponseBody
    public Response<String> uploadInvoiceVoucherImage(@RequestParam MultipartFile file, HttpServletRequest request,
                                                      HttpServletResponse response) throws IllegalStateException, IOException {
        super.setPostHeader(response);
        return super.upload2(file, request.getSession(), "/uploads/invoice_voucher_image_url/");
    }

    @RequestMapping(value = "/upload_low_price_img", method = RequestMethod.POST)
    @ResponseBody
    public Response<String> uploadLowPriceImage(@RequestParam MultipartFile file, HttpServletRequest request,
                                                HttpServletResponse response) throws IllegalStateException, IOException {
        super.setPostHeader(response);
        return super.upload2(file, request.getSession(), "/uploads/low_price_image_url/");
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public Response<Integer> addServiceOrder(@RequestParam(value = "type") String type,
                                             @RequestParam(value = "peopleNumber", required = false) Integer peopleNumber,
                                             @RequestParam(value = "peopleType", required = false) String peopleType,
                                             @RequestParam(value = "peopleRemarks", required = false) String peopleRemarks,
                                             @RequestParam(value = "serviceId") String serviceId,
                                             @RequestParam(value = "schoolId", required = false) Integer schoolId,
                                             @RequestParam(value = "schoolId2", required = false) Integer schoolId2,
                                             @RequestParam(value = "schoolId3", required = false) Integer schoolId3,
                                             @RequestParam(value = "schoolId4", required = false) Integer schoolId4,
                                             @RequestParam(value = "schoolId5", required = false) Integer schoolId5,
                                             @RequestParam(value = "servicePackageIds", required = false) String servicePackageIds,
                                             @RequestParam(value = "urgentState", required = false) String urgentState,
                                             @RequestParam(value = "isSettle", required = false) String isSettle,
                                             @RequestParam(value = "isDepositUser", required = false) String isDepositUser,
                                             @RequestParam(value = "subagencyId", required = false) String subagencyId,
                                             @RequestParam(value = "isPay") String isPay,
                                             @RequestParam(value = "receiveTypeId", required = false) String receiveTypeId,
                                             @RequestParam(value = "receiveDate", required = false) String receiveDate,
                                             @RequestParam(value = "receivable", required = false) String receivable,
                                             @RequestParam(value = "discount", required = false) String discount,
                                             @RequestParam(value = "received", required = false) String received,
                                             @RequestParam(value = "installment", required = false) Integer installment,
                                             @RequestParam(value = "paymentVoucherImageUrl1", required = false) String paymentVoucherImageUrl1,
                                             @RequestParam(value = "paymentVoucherImageUrl2", required = false) String paymentVoucherImageUrl2,
                                             @RequestParam(value = "paymentVoucherImageUrl3", required = false) String paymentVoucherImageUrl3,
                                             @RequestParam(value = "paymentVoucherImageUrl4", required = false) String paymentVoucherImageUrl4,
                                             @RequestParam(value = "paymentVoucherImageUrl5", required = false) String paymentVoucherImageUrl5,
                                             @RequestParam(value = "lowPriceImageUrl", required = false) String lowPriceImageUrl,
                                             @RequestParam(value = "perAmount", required = false) String perAmount,
                                             @RequestParam(value = "amount", required = false) String amount,
                                             @RequestParam(value = "expectAmount", required = false) String expectAmount,
                                             @RequestParam(value = "currency", required = false) String currency,
                                             @RequestParam(value = "exchangeRate", required = false) String exchangeRate,
                                             @RequestParam(value = "gst", required = false) String gst,
                                             @RequestParam(value = "deductGst", required = false) String deductGst,
                                             @RequestParam(value = "bonus", required = false) String bonus,
                                             @RequestParam(value = "userId") String userId,
                                             @RequestParam(value = "serviceOrderApplicantList", required = false) String serviceOrderApplicantListJson,
                                             @RequestParam(value = "maraId", required = false) String maraId,
                                             @RequestParam(value = "adviserId") String adviserId,
                                             @RequestParam(value = "officialId", required = false) String officialId,
                                             @RequestParam(value = "remarks", required = false) String remarks,
                                             @RequestParam(value = "closedReason", required = false) String closedReason,
                                             @RequestParam(value = "information", required = false) String information,
                                             @RequestParam(value = "isHistory", required = false) String isHistory,
                                             @RequestParam(value = "nutCloud", required = false) String nutCloud,
                                             @RequestParam(value = "serviceAssessId", required = false) String serviceAssessId,
                                             @RequestParam(value = "verifyCode", required = false) String verifyCode,
                                             @RequestParam(value = "refNo", required = false) String refNo,
                                             @RequestParam(value = "courseId", required = false) Integer courseId,
                                             @RequestParam(value = "schoolInstitutionLocationId", required = false) Integer schoolInstitutionLocationId,
                                             @RequestParam(value = "courseId2", required = false) Integer courseId2,
                                             @RequestParam(value = "schoolInstitutionLocationId2", required = false) Integer schoolInstitutionLocationId2,
                                             @RequestParam(value = "courseId3", required = false) Integer courseId3,
                                             @RequestParam(value = "schoolInstitutionLocationId3", required = false) Integer schoolInstitutionLocationId3,
                                             @RequestParam(value = "courseId4", required = false) Integer courseId4,
                                             @RequestParam(value = "schoolInstitutionLocationId4", required = false) Integer schoolInstitutionLocationId4,
                                             @RequestParam(value = "courseId5", required = false) Integer courseId5,
                                             @RequestParam(value = "schoolInstitutionLocationId5", required = false) Integer schoolInstitutionLocationId5,
                                             @RequestParam(value = "institutionTradingName", required = false) String institutionTradingName,
                                             @RequestParam(value = "institutionTradingName2", required = false) String institutionTradingName2,
                                             @RequestParam(value = "institutionTradingName3", required = false) String institutionTradingName3,
                                             @RequestParam(value = "institutionTradingName4", required = false) String institutionTradingName4,
                                             @RequestParam(value = "institutionTradingName5", required = false) String institutionTradingName5,
                                             HttpServletRequest request, HttpServletResponse response) {
        try {
            super.setPostHeader(response);
            AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
            if (adminUserLoginInfo == null || (!"SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
                    && !"GW".equalsIgnoreCase(adminUserLoginInfo.getApList())))
                return new Response<Integer>(1, "仅限顾问和超级管理员能创建服务订单.", 0);
            ServiceOrderDTO serviceOrderDto = new ServiceOrderDTO();
            serviceOrderDto.setCode(UUID.randomUUID().toString());
            if (StringUtil.isNotEmpty(type))
                serviceOrderDto.setType(type);
            serviceOrderDto.setPeopleNumber(peopleNumber != null && peopleNumber > 0 ? peopleNumber : 1);
            serviceOrderDto.setPeopleType(StringUtil.isNotEmpty(peopleType) ? peopleType : "1A");
            if (StringUtil.isNotEmpty(peopleRemarks))
                serviceOrderDto.setPeopleRemarks(peopleRemarks);
            if (StringUtil.isNotEmpty(serviceId))
                serviceOrderDto.setServiceId(StringUtil.toInt(serviceId));
            if ("OVST".equalsIgnoreCase(type)
                    && ((schoolId == null || schoolId <= 0) && (courseId == null || courseId <= 0)))
                return new Response<Integer>(1, "创建留学服务订单必须选择一个学校.", 0);
            if ("OVST".equalsIgnoreCase(type) && (schoolId == null || schoolId <= 0)
                    && (schoolInstitutionLocationId == null || schoolInstitutionLocationId <= 0))// 排除原来的留学
                return new Response<Integer>(1, "创建留学服务订单必须选择一个校区.", 0);
            if (schoolId != null && schoolId > 0)
                serviceOrderDto.setSchoolId(schoolId);
            serviceOrderDto.setState(ReviewAdviserStateEnum.PENDING.toString());
            // if (ServiceOrderTypeEnum.ZX.toString().equalsIgnoreCase(type) &&
            // StringUtil.isNotEmpty(officialId)){
            // if (StringUtil.toInt(officialId) == 0)//没有文案的咨询直接订单完成
            // serviceOrderDto.setState(ReviewAdviserStateEnum.COMPLETE.toString());
            // }
            serviceOrderDto.setSettle(isSettle != null && "true".equalsIgnoreCase(isSettle));
            serviceOrderDto.setUrgentState(urgentState);
            serviceOrderDto.setDepositUser(isDepositUser != null && "true".equalsIgnoreCase(isDepositUser));
            if (StringUtil.isNotEmpty(subagencyId))
                serviceOrderDto.setSubagencyId(StringUtil.toInt(subagencyId));
            serviceOrderDto.setPay("true".equalsIgnoreCase(isPay));
            if (StringUtil.isNotEmpty(receiveTypeId))
                serviceOrderDto.setReceiveTypeId(StringUtil.toInt(receiveTypeId));
            if (StringUtil.isNotEmpty(receiveDate))
                serviceOrderDto.setReceiveDate(new Date(Long.parseLong(receiveDate)));
            if (StringUtil.isNotEmpty(receivable))
                serviceOrderDto.setReceivable(Double.parseDouble(receivable));
            if (StringUtil.isNotEmpty(discount))
                serviceOrderDto.setDiscount(Double.parseDouble(discount));
            if (StringUtil.isNotEmpty(received))
                serviceOrderDto.setReceived(Double.parseDouble(received));
            if (installment != null && installment > 0)
                serviceOrderDto.setInstallment(installment);
            if (StringUtil.isNotEmpty(paymentVoucherImageUrl1))
                serviceOrderDto.setPaymentVoucherImageUrl1(paymentVoucherImageUrl1);
            // else if (serviceOrderDto.isPay())
            // return new Response<Integer>(1, "必须上传支付凭证!", 0);
            if (StringUtil.isNotEmpty(paymentVoucherImageUrl2))
                serviceOrderDto.setPaymentVoucherImageUrl2(paymentVoucherImageUrl2);
            if (StringUtil.isNotEmpty(paymentVoucherImageUrl3))
                serviceOrderDto.setPaymentVoucherImageUrl3(paymentVoucherImageUrl3);
            if (StringUtil.isNotEmpty(paymentVoucherImageUrl4))
                serviceOrderDto.setPaymentVoucherImageUrl4(paymentVoucherImageUrl4);
            if (StringUtil.isNotEmpty(paymentVoucherImageUrl5))
                serviceOrderDto.setPaymentVoucherImageUrl5(paymentVoucherImageUrl5);
            if (StringUtil.isNotEmpty(lowPriceImageUrl))
                serviceOrderDto.setLowPriceImageUrl(lowPriceImageUrl);
            if (StringUtil.isNotEmpty(perAmount))
                serviceOrderDto.setPerAmount(Double.parseDouble(perAmount));
            if (StringUtil.isNotEmpty(amount))
                serviceOrderDto.setAmount(Double.parseDouble(amount));
            if (StringUtil.isNotEmpty(expectAmount))
                serviceOrderDto.setExpectAmount(Double.parseDouble(expectAmount));
            if (StringUtil.isNotEmpty(currency))
                serviceOrderDto.setCurrency(currency);
            if (StringUtil.isNotEmpty(exchangeRate))
                serviceOrderDto.setExchangeRate(Double.parseDouble(exchangeRate));
            if (StringUtil.isNotEmpty(gst))
                serviceOrderDto.setGst(Double.parseDouble(gst));
            if (StringUtil.isNotEmpty(deductGst))
                serviceOrderDto.setDeductGst(Double.parseDouble(deductGst));
            if (StringUtil.isNotEmpty(bonus))
                serviceOrderDto.setBonus(Double.parseDouble(bonus));
            if (StringUtil.isNotEmpty(userId))
                if (userService.getUserById(StringUtil.toInt(userId)) == null)
                    return new Response<Integer>(1, "用户编号错误(" + userId + ")，创建失败.", 0);
                else {
                    if (userService.getUserById(StringUtil.toInt(userId)).getPhone().equalsIgnoreCase("00000000000"))
                        return new Response<Integer>(1,
                                "用户号码不合法(" + userService.getUserById(StringUtil.toInt(userId)).getPhone() + ")，创建失败.",
                                0);
                    else
                        serviceOrderDto.setUserId(StringUtil.toInt(userId));
                }
            if (StringUtil.isNotEmpty(maraId) && !"SIV".equalsIgnoreCase(serviceOrderDto.getType())
                    && !"NSV".equalsIgnoreCase(serviceOrderDto.getType())
                    && !"MT".equalsIgnoreCase(serviceOrderDto.getType())) // SIV主订单和MT主订单不需要mara
                serviceOrderDto.setMaraId(StringUtil.toInt(maraId));
            if (StringUtil.isNotEmpty(adviserId))
                serviceOrderDto.setAdviserId(StringUtil.toInt(adviserId));
            if (StringUtil.isNotEmpty(officialId) && !"SIV".equalsIgnoreCase(serviceOrderDto.getType())
                    && !"NSV".equalsIgnoreCase(serviceOrderDto.getType())
                    && !"MT".equalsIgnoreCase(serviceOrderDto.getType())) // SIV主订单和MT主订单不需要文案
                serviceOrderDto.setOfficialId(StringUtil.toInt(officialId));
            if (StringUtil.isNotEmpty(remarks))
                serviceOrderDto.setRemarks(remarks);
            if (StringUtil.isNotEmpty(closedReason))
                serviceOrderDto.setClosedReason(closedReason);
            if (StringUtil.isNotEmpty(information))
                serviceOrderDto.setInformation(information);
            serviceOrderDto.setHistory(isHistory != null && "true".equalsIgnoreCase(isHistory));
            if (StringUtil.isNotEmpty(nutCloud))
                serviceOrderDto.setNutCloud(nutCloud);
            if (StringUtil.isEmpty(serviceAssessId)
                    && serviceAssessService.seleteAssessByServiceId(serviceId).size() > 0) {
                return new Response(1, "没有选择职业!");
            }
            if (StringUtil.isNotEmpty(serviceAssessId)) {
                if ((!type.equalsIgnoreCase("SIV") && !type.equalsIgnoreCase("NSV"))
                        && serviceAssessService.seleteAssessByServiceId(serviceId).size() == 0)
                    return new Response(1, "当前服务编号不是评估(" + serviceId + ")，创建失败.", 0);
                serviceOrderDto.setServiceAssessId(serviceAssessId);
            }
            if (isHistory != null && "true".equalsIgnoreCase(isHistory))
                serviceOrderDto.setRealPeopleNumber(0);
            else
                serviceOrderDto.setRealPeopleNumber(peopleNumber != null && peopleNumber > 0 ? peopleNumber : 1);
            if (StringUtil.isNotEmpty(verifyCode))
                serviceOrderDto.setVerifyCode(verifyCode.replace("$", "").replace("#", "").replace(" ", ""));
            if (StringUtil.isNotEmpty(verifyCode))
                serviceOrderDto.setRefNo(refNo);
            if (courseId != null && courseId > 0) {
                serviceOrderDto.setCourseId(courseId);
                serviceOrderDto.setSchoolId(0);
                if (StringUtil.isNotEmpty(institutionTradingName))
                    serviceOrderDto.setInstitutionTradingName(institutionTradingName);
            }
            if (schoolInstitutionLocationId != null && schoolInstitutionLocationId > 0)
                serviceOrderDto.setSchoolInstitutionLocationId(schoolInstitutionLocationId);
            List<ServiceOrderApplicantDTO> serviceOrderApplicantList = null;
            if (StringUtil.isNotEmpty(serviceOrderApplicantListJson)) {
                serviceOrderApplicantList = JSONObject.parseArray(serviceOrderApplicantListJson,
                        ServiceOrderApplicantDTO.class);
                if (!ListUtil.isEmpty(serviceOrderApplicantList) && serviceOrderApplicantList.size() == 1)
                    serviceOrderDto.setApplicantId(serviceOrderApplicantList.get(0).getApplicantId());
            } else
                return new Response<Integer>(1, "请选择申请人.", null);
            if (serviceOrderService.addServiceOrder(serviceOrderDto) > 0) {
                int serviceOrderId = serviceOrderDto.getId();
                String msg = "";
                if (adminUserLoginInfo != null)
                    serviceOrderService.approval(serviceOrderDto.getId(), adminUserLoginInfo.getId(),
                            serviceOrderDto.getState(), null, null, null);
                // 虽然设计了可以逗号分割保存多个申请人ID，但后来讨论需求后要求如果有多个申请人则创建多条子订单
                if (!ListUtil.isEmpty(serviceOrderApplicantList) && serviceOrderApplicantList.size() >= 1) {
                    for (ServiceOrderApplicantDTO serviceOrderApplicantDto : serviceOrderApplicantList) {
//					ServiceOrderApplicantDTO serviceOrderApplicantDto = serviceOrderApplicantList.get(0);
                        serviceOrderApplicantDto.setServiceOrderId(serviceOrderDto.getId());
                        if (serviceOrderApplicantService.addServiceOrderApplicant(serviceOrderApplicantDto) <= 0) {
                            serviceOrderService.deleteServiceOrderById(serviceOrderDto.getId());
                            return new Response<Integer>(1, "申请人关联失败.", null);
                        }
                    }
                } else {
                    serviceOrderService.deleteServiceOrderById(serviceOrderDto.getId());
                    return new Response<Integer>(1, "申请人参数错误.", null);
                }
                for (ServiceOrderApplicantDTO serviceOrderApplicantDto : serviceOrderApplicantList) {
                    if (serviceOrderApplicantDto.getApplicantId() <= 0)
                        continue;
                    serviceOrderDto.setApplicantId(serviceOrderApplicantDto.getApplicantId());
                    serviceOrderDto.setApplicantParentId(serviceOrderId);
                    // 创建子服务订单
                    if (StringUtil.isNotEmpty(servicePackageIds)) {
                        List<String> servicePackageIdList = Arrays.asList(servicePackageIds.split(","));
                        serviceOrderDto.setParentId(serviceOrderDto.getId());
                        serviceOrderDto.setId(0);
                        for (String servicePackageId : servicePackageIdList) {
                            int id = StringUtil.toInt(servicePackageId);
                            if (servicePackageService.getById(id) == null) {
                                msg += "服务包不存在(" + id + "),请检查参数. ";
                                continue;
                            }
                            serviceOrderDto.setServicePackageId(id);
                            ServicePackageDTO servicePackageDto = servicePackageService.getById(id);
                            if (servicePackageDto == null)
                                return new Response<Integer>(1, "服务包不存在.", null);
                            serviceOrderDto.setServiceAssessId(
                                    "CA".equalsIgnoreCase(servicePackageDto.getType()) ? serviceAssessId : null);
                            serviceOrderDto.setType("VISA"); // 独立技术移民子订单为VISA
                            serviceOrderDto.setPay(false); // 独立技术移民子订单都未支付
                            serviceOrderDto.setVerifyCode(null); // 独立技术移民子订单都没有对账Code
                            if (StringUtil.isNotEmpty(maraId))
                                serviceOrderDto.setMaraId(StringUtil.toInt(maraId)); // 独立技术移民子订单需要mara
                            if (StringUtil.isNotEmpty(officialId))
                                serviceOrderDto.setOfficialId(StringUtil.toInt(officialId)); // 独立技术移民子订单需要文案
                            if (serviceOrderService.addServiceOrder(serviceOrderDto) > 0
                                    && adminUserLoginInfo != null) {
                                serviceOrderService.approval(serviceOrderDto.getId(), adminUserLoginInfo.getId(),
                                        ReviewAdviserStateEnum.PENDING.toString(), null, null, null);
                                serviceOrderApplicantDto.setServiceOrderId(serviceOrderDto.getId());
                                if (serviceOrderApplicantService
                                        .addServiceOrderApplicant(serviceOrderApplicantDto) == 0)
                                    msg += "申请人子服务订单创建失败(" + serviceOrderApplicantDto + "). ";
                            } else
                                msg += "子服务订单创建失败(" + serviceOrderDto + "). ";
                        }
                        if (serviceOrderApplicantList.size() == 1)
                            break;
                    } else if (serviceOrderApplicantList.size() > 1) {
                        serviceOrderDto.setId(0);
                        serviceOrderDto.setVerifyCode(null);
                        if (serviceOrderService.addServiceOrder(serviceOrderDto) > 0 && adminUserLoginInfo != null) {
                            serviceOrderService.approval(serviceOrderDto.getId(), adminUserLoginInfo.getId(),
                                    ReviewAdviserStateEnum.PENDING.toString(), null, null, null);
                            serviceOrderApplicantDto.setServiceOrderId(serviceOrderDto.getId());
                            if (serviceOrderApplicantService.addServiceOrderApplicant(serviceOrderApplicantDto) == 0)
                                msg += "申请人子服务订单创建失败(" + serviceOrderApplicantDto + "). ";
                        } else
                            msg += "服务订单创建失败(" + serviceOrderDto + "). ";
                    }
                }
                if ("OVST".equalsIgnoreCase(type) && (schoolId2 != null && schoolId2 > 0) || (courseId2 != null
                        && courseId2 > 0 && schoolInstitutionLocationId2 != null && schoolInstitutionLocationId2 > 0)) {
                    serviceOrderDto.setId(0);
                    if (schoolId2 != null && schoolId2 > 0) {
                        serviceOrderDto.setSchoolId(schoolId2);
                        serviceOrderDto.setCourseId(0);
                        serviceOrderDto.setSchoolInstitutionLocationId(0);
                    }
                    if (courseId2 != null && courseId2 > 0 && schoolInstitutionLocationId2 != null
                            && schoolInstitutionLocationId2 > 0) {
                        serviceOrderDto.setCourseId(courseId2);
                        serviceOrderDto.setSchoolInstitutionLocationId(schoolInstitutionLocationId2);
                        serviceOrderDto.setSchoolId(0);
                        if (StringUtil.isNotEmpty(institutionTradingName2))
                            serviceOrderDto.setInstitutionTradingName(institutionTradingName2);
                    }
                    if (serviceOrderService.addServiceOrder(serviceOrderDto) > 0 && adminUserLoginInfo != null) {
                        serviceOrderService.approval(serviceOrderDto.getId(), adminUserLoginInfo.getId(),
                                serviceOrderDto.getState(), null, null, null);
                        if (serviceOrderApplicantList.size() > 0) {
                            ServiceOrderApplicantDTO serviceOrderApplicantDto = serviceOrderApplicantList.get(0);
                            serviceOrderApplicantDto.setServiceOrderId(serviceOrderDto.getId());
                            if (serviceOrderApplicantService.addServiceOrderApplicant(serviceOrderApplicantDto) == 0)
                                msg += "申请人子服务订单2创建失败(" + serviceOrderApplicantDto + "). ";
                        }
                        msg += "创建第二学校服务订单成功(第二服务订单编号:" + serviceOrderDto.getId() + "). ";
                    } else
                        msg += "创建第二学校服务订单失败(第二学校编号:" + schoolId2 + "). ";
                }
                if ("OVST".equalsIgnoreCase(type) && (schoolId3 != null && schoolId3 > 0) || (courseId3 != null
                        && courseId3 > 0 && schoolInstitutionLocationId3 != null && schoolInstitutionLocationId3 > 0)) {
                    serviceOrderDto.setId(0);
                    if (schoolId3 != null && schoolId3 > 0) {
                        serviceOrderDto.setSchoolId(schoolId3);
                        serviceOrderDto.setCourseId(0);
                        serviceOrderDto.setSchoolInstitutionLocationId(0);
                    }
                    if (courseId3 != null && courseId3 > 0 && schoolInstitutionLocationId3 != null
                            && schoolInstitutionLocationId3 > 0) {
                        serviceOrderDto.setCourseId(courseId3);
                        serviceOrderDto.setSchoolInstitutionLocationId(schoolInstitutionLocationId3);
                        serviceOrderDto.setSchoolId(0);
                        if (StringUtil.isNotEmpty(institutionTradingName3))
                            serviceOrderDto.setInstitutionTradingName(institutionTradingName3);
                    }
                    if (serviceOrderService.addServiceOrder(serviceOrderDto) > 0 && adminUserLoginInfo != null) {
                        serviceOrderService.approval(serviceOrderDto.getId(), adminUserLoginInfo.getId(),
                                serviceOrderDto.getState(), null, null, null);
                        if (serviceOrderApplicantList.size() > 0) {
                            ServiceOrderApplicantDTO serviceOrderApplicantDto = serviceOrderApplicantList.get(0);
                            serviceOrderApplicantDto.setServiceOrderId(serviceOrderDto.getId());
                            if (serviceOrderApplicantService.addServiceOrderApplicant(serviceOrderApplicantDto) == 0)
                                msg += "申请人子服务订单3创建失败(" + serviceOrderApplicantDto + "). ";
                        }
                        msg += "创建第三学校服务订单成功(第三服务订单编号:" + serviceOrderDto.getId() + "). ";
                    } else
                        msg += "创建第三学校服务订单失败(第三学校编号:" + schoolId3 + "). ";
                }
                if ("OVST".equalsIgnoreCase(type) && (schoolId4 != null && schoolId4 > 0) || (courseId4 != null
                        && courseId4 > 0 && schoolInstitutionLocationId4 != null && schoolInstitutionLocationId4 > 0)) {
                    serviceOrderDto.setId(0);
                    if (schoolId4 != null && schoolId4 > 0) {
                        serviceOrderDto.setSchoolId(schoolId4);
                        serviceOrderDto.setCourseId(0);
                        serviceOrderDto.setSchoolInstitutionLocationId(0);
                    }
                    if (courseId4 != null && courseId4 > 0 && schoolInstitutionLocationId4 != null
                            && schoolInstitutionLocationId4 > 0) {
                        serviceOrderDto.setCourseId(courseId4);
                        serviceOrderDto.setSchoolInstitutionLocationId(schoolInstitutionLocationId4);
                        serviceOrderDto.setSchoolId(0);
                        if (StringUtil.isNotEmpty(institutionTradingName4))
                            serviceOrderDto.setInstitutionTradingName(institutionTradingName4);
                    }
                    if (serviceOrderService.addServiceOrder(serviceOrderDto) > 0 && adminUserLoginInfo != null) {
                        serviceOrderService.approval(serviceOrderDto.getId(), adminUserLoginInfo.getId(),
                                serviceOrderDto.getState(), null, null, null);
                        if (serviceOrderApplicantList.size() > 0) {
                            ServiceOrderApplicantDTO serviceOrderApplicantDto = serviceOrderApplicantList.get(0);
                            serviceOrderApplicantDto.setServiceOrderId(serviceOrderDto.getId());
                            if (serviceOrderApplicantService.addServiceOrderApplicant(serviceOrderApplicantDto) == 0)
                                msg += "申请人子服务订单4创建失败(" + serviceOrderApplicantDto + "). ";
                        }
                        msg += "创建第四学校服务订单成功(第四服务订单编号:" + serviceOrderDto.getId() + "). ";
                    } else
                        msg += "创建第四学校服务订单失败(第四学校编号:" + schoolId4 + "). ";
                }
                if ("OVST".equalsIgnoreCase(type) && (schoolId5 != null && schoolId5 > 0) || (courseId5 != null
                        && courseId5 > 0 && schoolInstitutionLocationId5 != null && schoolInstitutionLocationId5 > 0)) {
                    serviceOrderDto.setId(0);
                    if (schoolId5 != null && schoolId5 > 0) {
                        serviceOrderDto.setSchoolId(schoolId5);
                        serviceOrderDto.setCourseId(0);
                        serviceOrderDto.setSchoolInstitutionLocationId(0);
                    }
                    if (courseId5 != null && courseId5 > 0 && schoolInstitutionLocationId5 != null
                            && schoolInstitutionLocationId5 > 0) {
                        serviceOrderDto.setCourseId(courseId5);
                        serviceOrderDto.setSchoolInstitutionLocationId(schoolInstitutionLocationId5);
                        serviceOrderDto.setSchoolId(0);
                        if (StringUtil.isNotEmpty(institutionTradingName5))
                            serviceOrderDto.setInstitutionTradingName(institutionTradingName5);
                    }
                    if (serviceOrderService.addServiceOrder(serviceOrderDto) > 0 && adminUserLoginInfo != null) {
                        serviceOrderService.approval(serviceOrderDto.getId(), adminUserLoginInfo.getId(),
                                serviceOrderDto.getState(), null, null, null);
                        if (serviceOrderApplicantList.size() > 0) {
                            ServiceOrderApplicantDTO serviceOrderApplicantDto = serviceOrderApplicantList.get(0);
                            serviceOrderApplicantDto.setServiceOrderId(serviceOrderDto.getId());
                            if (serviceOrderApplicantService.addServiceOrderApplicant(serviceOrderApplicantDto) == 0)
                                msg += "申请人子服务订单5创建失败(" + serviceOrderApplicantDto + "). ";
                        }
                        msg += "创建第五学校服务订单成功(第五服务订单编号:" + serviceOrderDto.getId() + "). ";
                    } else
                        msg += "创建第五学校服务订单失败(第五学校编号:" + schoolId5 + "). ";
                }
                return new Response<Integer>(0, msg, serviceOrderDto.getId());
            } else
                return new Response<Integer>(1, "创建失败.", 0);
        } catch (ServiceException e) {
            return new Response<Integer>(e.getCode(), e.getMessage(), 0);
        }
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public Response<Integer> updateServiceOrder(@RequestParam(value = "id") int id,
                                                @RequestParam(value = "type", required = false) String type,
                                                @RequestParam(value = "peopleNumber", required = false) Integer peopleNumber,
                                                @RequestParam(value = "peopleType", required = false) String peopleType,
                                                @RequestParam(value = "peopleRemarks", required = false) String peopleRemarks,
                                                @RequestParam(value = "serviceId", required = false) String serviceId,
                                                @RequestParam(value = "schoolId", required = false) String schoolId,
                                                @RequestParam(value = "urgentState", required = false) String urgentState,
                                                @RequestParam(value = "isSettle", required = false) String isSettle,
                                                @RequestParam(value = "isDepositUser", required = false) String isDepositUser,
                                                @RequestParam(value = "subagencyId", required = false) String subagencyId,
                                                @RequestParam(value = "isPay", required = false) String isPay,
                                                @RequestParam(value = "receiveTypeId", required = false) String receiveTypeId,
                                                @RequestParam(value = "receiveDate", required = false) String receiveDate,
                                                @RequestParam(value = "receivable", required = false) String receivable,
                                                @RequestParam(value = "discount", required = false) String discount,
                                                @RequestParam(value = "received", required = false) String received,
                                                @RequestParam(value = "installment", required = false) Integer installment,
                                                @RequestParam(value = "paymentVoucherImageUrl1", required = false) String paymentVoucherImageUrl1,
                                                @RequestParam(value = "paymentVoucherImageUrl2", required = false) String paymentVoucherImageUrl2,
                                                @RequestParam(value = "paymentVoucherImageUrl3", required = false) String paymentVoucherImageUrl3,
                                                @RequestParam(value = "paymentVoucherImageUrl4", required = false) String paymentVoucherImageUrl4,
                                                @RequestParam(value = "paymentVoucherImageUrl5", required = false) String paymentVoucherImageUrl5,
                                                @RequestParam(value = "invoiceVoucherImageUrl1", required = false) String invoiceVoucherImageUrl1,
                                                @RequestParam(value = "invoiceVoucherImageUrl2", required = false) String invoiceVoucherImageUrl2,
                                                @RequestParam(value = "invoiceVoucherImageUrl3", required = false) String invoiceVoucherImageUrl3,
                                                @RequestParam(value = "invoiceVoucherImageUrl4", required = false) String invoiceVoucherImageUrl4,
                                                @RequestParam(value = "invoiceVoucherImageUrl5", required = false) String invoiceVoucherImageUrl5,
                                                @RequestParam(value = "kjPaymentImageUrl1", required = false) String kjPaymentImageUrl1,
                                                @RequestParam(value = "kjPaymentImageUrl2", required = false) String kjPaymentImageUrl2,
                                                @RequestParam(value = "lowPriceImageUrl", required = false) String lowPriceImageUrl,
                                                @RequestParam(value = "perAmount", required = false) String perAmount,
                                                @RequestParam(value = "amount", required = false) String amount,
                                                @RequestParam(value = "expectAmount", required = false) String expectAmount,
                                                @RequestParam(value = "currency", required = false) String currency,
                                                @RequestParam(value = "exchangeRate", required = false) String exchangeRate,
                                                @RequestParam(value = "gst", required = false) String gst,
                                                @RequestParam(value = "deductGst", required = false) String deductGst,
                                                @RequestParam(value = "bonus", required = false) String bonus,
                                                @RequestParam(value = "userId", required = false) String userId,
                                                @RequestParam(value = "applicantId", required = false) String applicantId,
                                                @RequestParam(value = "applicantBirthday", required = false) String applicantBirthday,
                                                @RequestParam(value = "serviceOrderApplicantList", required = false) String serviceOrderApplicantListJson,
                                                @RequestParam(value = "maraId", required = false) String maraId,
                                                @RequestParam(value = "adviserId", required = false) String adviserId,
                                                @RequestParam(value = "officialId", required = false) String officialId,
                                                @RequestParam(value = "remarks", required = false) String remarks,
                                                @RequestParam(value = "closedReason", required = false) String closedReason, HttpServletRequest request,
                                                @RequestParam(value = "information", required = false) String information,
                                                @RequestParam(value = "isHistory", required = false) String isHistory,
                                                @RequestParam(value = "nutCloud", required = false) String nutCloud,
                                                @RequestParam(value = "serviceAssessId", required = false) String serviceAssessId,
                                                @RequestParam(value = "verifyCode", required = false) String verifyCode,
                                                @RequestParam(value = "refNo", required = false) String refNo,
                                                @RequestParam(value = "courseId", required = false) Integer courseId,
                                                @RequestParam(value = "schoolInstitutionLocationId", required = false) Integer schoolInstitutionLocationId,
                                                @RequestParam(value = "institutionTradingName", required = false) String institutionTradingName,
                                                HttpServletResponse response) {
//		if (getOfficialAdminId(request) != null)
//			return new Response<Integer>(1, "文案管理员不可操作服务订单.", 0);
        super.setPostHeader(response);
        ServiceOrderDTO serviceOrderDto;
        try {
            serviceOrderDto = serviceOrderService.getServiceOrderById(id);
            if (serviceOrderDto == null)
                return new Response<Integer>(1, "服务订单不存在,修改失败.", 0);
            List<ServiceOrderApplicantDTO> serviceOrderApplicantList = null;
            if (StringUtil.isNotEmpty(serviceOrderApplicantListJson))
                serviceOrderApplicantList = JSONObject.parseArray(serviceOrderApplicantListJson,
                        ServiceOrderApplicantDTO.class);
            Response<Integer> res = updateOne(serviceOrderDto, type, peopleNumber, peopleType, peopleRemarks, serviceId,
                    schoolId, urgentState, isSettle, isDepositUser, subagencyId, isPay, receiveTypeId, receiveDate,
                    receivable, discount, received, installment, paymentVoucherImageUrl1, paymentVoucherImageUrl2,
                    paymentVoucherImageUrl3, paymentVoucherImageUrl4, paymentVoucherImageUrl5, invoiceVoucherImageUrl1,
                    invoiceVoucherImageUrl2, invoiceVoucherImageUrl3, invoiceVoucherImageUrl4, invoiceVoucherImageUrl5,
                    kjPaymentImageUrl1, kjPaymentImageUrl2, lowPriceImageUrl, perAmount, amount, expectAmount, currency,
                    exchangeRate, gst, deductGst, bonus, userId, applicantId, applicantBirthday,
                    serviceOrderApplicantList, maraId, adviserId, officialId, remarks, closedReason, information,
                    isHistory, nutCloud, serviceAssessId, verifyCode, refNo, courseId, schoolInstitutionLocationId,
                    institutionTradingName);
            if (res != null && res.getCode() == 0) {
				List<ServiceOrderDTO> cList = new ArrayList<>();
				if ("SIV".equalsIgnoreCase(serviceOrderDto.getType())
						|| "NSV".equalsIgnoreCase(serviceOrderDto.getType()))
					cList = serviceOrderService.listServiceOrder(serviceOrderDto.getType(), null, null, null, null,
							null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
							null, id, 0, false, 0, 100, null, null, null, null, null);
				else if ("VISA".equalsIgnoreCase(serviceOrderDto.getType()))
					cList = serviceOrderService.listServiceOrder(serviceOrderDto.getType(), null, null, null, null,
							null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
							null, 0, id, false, 0, 100, null, null, null, null, null);
				cList.forEach(cServiceOrderDto -> {
					Response<Integer> cRes = updateOne(cServiceOrderDto, null, peopleNumber, peopleType, peopleRemarks,
							serviceId, schoolId, urgentState, isSettle, isDepositUser, subagencyId, isPay,
							receiveTypeId, receiveDate, receivable, discount, received, installment,
							paymentVoucherImageUrl1, paymentVoucherImageUrl2, paymentVoucherImageUrl3,
							paymentVoucherImageUrl4, paymentVoucherImageUrl5, invoiceVoucherImageUrl1,
							invoiceVoucherImageUrl2, invoiceVoucherImageUrl3, invoiceVoucherImageUrl4,
							invoiceVoucherImageUrl5, kjPaymentImageUrl1, kjPaymentImageUrl2, lowPriceImageUrl,
							perAmount, amount, expectAmount, currency, exchangeRate, gst, deductGst, bonus, userId,
							null, null, null, maraId, adviserId, officialId, remarks, closedReason, information,
							isHistory, nutCloud, serviceAssessId, verifyCode, refNo, courseId,
							schoolInstitutionLocationId, institutionTradingName);
					if (cRes.getCode() > 0)
						res.setMessage(res.getMessage() + ";" + cRes.getMessage());
				});
            }
            return res;
        } catch (ServiceException e) {
            return new Response<Integer>(e.getCode(), e.getMessage(), null);
        }

    }

    private Response<Integer> updateOne(ServiceOrderDTO serviceOrderDto, String type, Integer peopleNumber,
                                        String peopleType, String peopleRemarks, String serviceId, String schoolId, String urgentState,
                                        String isSettle, String isDepositUser, String subagencyId, String isPay, String receiveTypeId,
                                        String receiveDate, String receivable, String discount, String received, Integer installment,
                                        String paymentVoucherImageUrl1, String paymentVoucherImageUrl2, String paymentVoucherImageUrl3,
                                        String paymentVoucherImageUrl4, String paymentVoucherImageUrl5, String invoiceVoucherImageUrl1,
                                        String invoiceVoucherImageUrl2, String invoiceVoucherImageUrl3, String invoiceVoucherImageUrl4,
                                        String invoiceVoucherImageUrl5, String kjPaymentImageUrl1, String kjPaymentImageUrl2,
                                        String lowPriceImageUrl, String perAmount, String amount, String expectAmount, String currency,
                                        String exchangeRate, String gst, String deductGst, String bonus, String userId, String applicantId,
                                        String applicantBirthday, List<ServiceOrderApplicantDTO> serviceOrderApplicantList, String maraId,
                                        String adviserId, String officialId, String remarks, String closedReason, String information,
                                        String isHistory, String nutCloud, String serviceAssessId, String verifyCode, String refNo,
                                        Integer courseId, Integer schoolInstitutionLocationId, String institutionTradingName) {
        try {
            if (StringUtil.isNotEmpty(type))
                serviceOrderDto.setType(type);
            if (peopleNumber != null && peopleNumber > 0)
                serviceOrderDto.setPeopleNumber(peopleNumber);
            if (StringUtil.isNotEmpty(peopleType))
                serviceOrderDto.setPeopleType(peopleType);
            if (StringUtil.isNotEmpty(peopleRemarks))
                serviceOrderDto.setPeopleRemarks(peopleRemarks);
            if (StringUtil.isNotEmpty(serviceId))
                serviceOrderDto.setServiceId(StringUtil.toInt(serviceId));
            if (StringUtil.isNotEmpty(schoolId))
                serviceOrderDto.setSchoolId(StringUtil.toInt(schoolId));
            serviceOrderDto.setUrgentState(urgentState);
            if (isSettle != null)
                serviceOrderDto.setSettle("true".equalsIgnoreCase(isSettle));
            if (isDepositUser != null)
                serviceOrderDto.setDepositUser("true".equalsIgnoreCase(isDepositUser));
            if (StringUtil.isNotEmpty(subagencyId))
                serviceOrderDto.setSubagencyId(StringUtil.toInt(subagencyId));
            if (isPay != null)
                serviceOrderDto.setPay("true".equalsIgnoreCase(isPay));
            if (StringUtil.isNotEmpty(receiveTypeId))
                serviceOrderDto.setReceiveTypeId(StringUtil.toInt(receiveTypeId));
            if (StringUtil.isNotEmpty(receiveDate))
                serviceOrderDto.setReceiveDate(new Date(Long.parseLong(receiveDate)));
            if (StringUtil.isNotEmpty(receivable))
                serviceOrderDto.setReceivable(Double.parseDouble(receivable));
            if (StringUtil.isNotEmpty(discount))
                serviceOrderDto.setDiscount(Double.parseDouble(discount));
            if (StringUtil.isNotEmpty(received))
                serviceOrderDto.setReceived(Double.parseDouble(received));
            if (installment != null && installment > 0)
                serviceOrderDto.setInstallment(installment);
            if (StringUtil.isNotEmpty(paymentVoucherImageUrl1))
                serviceOrderDto.setPaymentVoucherImageUrl1(paymentVoucherImageUrl1);
            // else if (serviceOrderDto.isPay())
            // return new Response<Integer>(1, "必须上传支付凭证!", 0);
            if (StringUtil.isNotEmpty(paymentVoucherImageUrl2))
                serviceOrderDto.setPaymentVoucherImageUrl2(paymentVoucherImageUrl2);
            if (StringUtil.isNotEmpty(paymentVoucherImageUrl3))
                serviceOrderDto.setPaymentVoucherImageUrl3(paymentVoucherImageUrl3);
            if (StringUtil.isNotEmpty(paymentVoucherImageUrl4))
                serviceOrderDto.setPaymentVoucherImageUrl4(paymentVoucherImageUrl4);
            if (StringUtil.isNotEmpty(paymentVoucherImageUrl5))
                serviceOrderDto.setPaymentVoucherImageUrl5(paymentVoucherImageUrl5);
            if (StringUtil.isNotEmpty(invoiceVoucherImageUrl1))
                serviceOrderDto.setInvoiceVoucherImageUrl1(invoiceVoucherImageUrl1);
            if (StringUtil.isNotEmpty(invoiceVoucherImageUrl2))
                serviceOrderDto.setInvoiceVoucherImageUrl2(invoiceVoucherImageUrl2);
            if (StringUtil.isNotEmpty(invoiceVoucherImageUrl3))
                serviceOrderDto.setInvoiceVoucherImageUrl3(invoiceVoucherImageUrl3);
            if (StringUtil.isNotEmpty(invoiceVoucherImageUrl4))
                serviceOrderDto.setInvoiceVoucherImageUrl4(invoiceVoucherImageUrl4);
            if (StringUtil.isNotEmpty(invoiceVoucherImageUrl5))
                serviceOrderDto.setInvoiceVoucherImageUrl5(invoiceVoucherImageUrl5);
            if (StringUtil.isNotEmpty(kjPaymentImageUrl1))
                serviceOrderDto.setKjPaymentImageUrl1(kjPaymentImageUrl1);
            if (StringUtil.isNotEmpty(kjPaymentImageUrl2))
                serviceOrderDto.setKjPaymentImageUrl2(kjPaymentImageUrl2);
            // if (serviceOrderDto.isSettle() &&
            // "RECEIVED".equalsIgnoreCase(serviceOrderDto.getState())){
            // if (StringUtil.isBlank(invoiceVoucherImageUrl1) ||
            // StringUtil.isBlank(invoiceVoucherImageUrl2)
            // || StringUtil.isBlank(invoiceVoucherImageUrl3) ||
            // StringUtil.isBlank(invoiceVoucherImageUrl4)
            // || StringUtil.isBlank(invoiceVoucherImageUrl5))
            // return new Response<Integer>(1, "提前扣拥必须上传发票凭证!", 0);
            // if (StringUtil.isBlank(officialPaymentImageUrl1) ||
            // StringUtil.isBlank(officialPaymentImageUrl2))
            // return new Response<Integer>(1, "文案需要上传转账凭证!", 0);
            // }
            if (StringUtil.isNotEmpty(lowPriceImageUrl))
                serviceOrderDto.setLowPriceImageUrl(lowPriceImageUrl);
            if (StringUtil.isNotEmpty(perAmount))
                serviceOrderDto.setPerAmount(Double.parseDouble(perAmount));
            if (StringUtil.isNotEmpty(amount))
                serviceOrderDto.setAmount(Double.parseDouble(amount));
            if (StringUtil.isNotEmpty(expectAmount))
                serviceOrderDto.setExpectAmount(Double.parseDouble(expectAmount));
            if (StringUtil.isNotEmpty(currency))
                serviceOrderDto.setCurrency(currency);
            if (StringUtil.isNotEmpty(exchangeRate))
                serviceOrderDto.setExchangeRate(Double.parseDouble(exchangeRate));
            if (StringUtil.isNotEmpty(gst))
                serviceOrderDto.setGst(Double.parseDouble(gst));
            if (StringUtil.isNotEmpty(deductGst))
                serviceOrderDto.setDeductGst(Double.parseDouble(deductGst));
            if (StringUtil.isNotEmpty(bonus))
                serviceOrderDto.setBonus(Double.parseDouble(bonus));
            if (StringUtil.isNotEmpty(userId))
                serviceOrderDto.setUserId(StringUtil.toInt(userId));
            if (StringUtil.isNotEmpty(applicantId))
                serviceOrderDto.setApplicantId(StringUtil.toInt(applicantId));
            if (StringUtil.isNotEmpty(maraId))
                serviceOrderDto.setMaraId(StringUtil.toInt(maraId));
            if (StringUtil.isNotEmpty(adviserId))
                serviceOrderDto.setAdviserId(StringUtil.toInt(adviserId));
            if (StringUtil.isNotEmpty(officialId))
                serviceOrderDto.setOfficialId(StringUtil.toInt(officialId));
            if (StringUtil.isNotEmpty(remarks))
                serviceOrderDto.setRemarks(remarks);
            if (StringUtil.isNotEmpty(closedReason))
                serviceOrderDto.setClosedReason(closedReason);
            if (serviceOrderDto.getServicePackageId() > 0) {
                ServicePackageDTO servicePackageDto = servicePackageService
                        .getById(serviceOrderDto.getServicePackageId());
                if (servicePackageDto == null)
                    return new Response<Integer>(1, "服务包不存在:" + serviceOrderDto.getServicePackageId(), 0);
                // if (serviceOrderDto.getOfficialId() <= 0 &&
                // "SIV".equalsIgnoreCase(serviceOrderDto.getType()))
//				if (StringUtil.isEmpty(officialId)
//						&& ("SIV".equalsIgnoreCase(serviceOrderDto.getType()) || serviceOrderDto.getParentId() > 0))
//					return new Response<Integer>(1, "必须选择文案.", 0);
                // if (serviceOrderDto.getMaraId() <= 0 &&
                // "SIV".equalsIgnoreCase(serviceOrderDto.getType())
//				if (StringUtil.isEmpty(maraId)
//						&& ("SIV".equalsIgnoreCase(serviceOrderDto.getType()) || serviceOrderDto.getParentId() > 0)
//						&& !"EOI".equalsIgnoreCase(servicePackageDto.getType()))
//					return new Response<Integer>(1, "必须选择Mara.", 0);
            }
            if (StringUtil.isNotEmpty(information))
                serviceOrderDto.setInformation(information);
            serviceOrderDto.setHistory(isHistory != null && "true".equalsIgnoreCase(isHistory));
            if (StringUtil.isNotEmpty(nutCloud))
                serviceOrderDto.setNutCloud(nutCloud);
            if (StringUtil.isNotEmpty(serviceAssessId)) {
                if (serviceAssessService.seleteAssessByServiceId(serviceId).size() == 0)
                    return new Response<Integer>(1, "当前服务编号不是评估(" + serviceId + ") .", 0);
                serviceOrderDto.setServiceAssessId(serviceAssessId);
            } else
                serviceOrderDto.setServiceAssessId(null);
            if (StringUtil.isNotEmpty(verifyCode))
                serviceOrderDto.setVerifyCode(verifyCode.replace("$", "").replace("#", "").replace(" ", ""));
            if (StringUtil.isNotEmpty(refNo))
                serviceOrderDto.setRefNo(refNo);
            if (courseId != null && courseId > 0)
                serviceOrderDto.setCourseId(courseId);
            if (schoolInstitutionLocationId != null && schoolInstitutionLocationId > 0)
                serviceOrderDto.setSchoolInstitutionLocationId(schoolInstitutionLocationId);
            if (StringUtil.isNotEmpty(institutionTradingName))
                serviceOrderDto.setInstitutionTradingName(institutionTradingName);
            if (serviceOrderApplicantList != null && serviceOrderApplicantList.size() > 0) {
                for (ServiceOrderApplicantDTO serviceOrderApplicantDto : serviceOrderApplicantList) {
                    serviceOrderApplicantDto.setServiceOrderId(serviceOrderDto.getId());
                    if (serviceOrderApplicantService.updateServiceOrderApplicant(serviceOrderApplicantDto) <= 0)
                        LOG.warn("申请人信息修改失败! (serviceOrderApplicantId:" + serviceOrderApplicantDto.getId() + ")");
                    else
                        LOG.warn("申请人信息修改成功. (serviceOrderApplicantId:" + serviceOrderApplicantDto.getId() + ")");
                }
                if (serviceOrderApplicantList.get(0) != null && StringUtil.isEmpty(applicantId))
                    serviceOrderDto.setApplicantId(serviceOrderApplicantList.get(0).getApplicantId());
            }
            int i = serviceOrderService.updateServiceOrder(serviceOrderDto);
            if (i > 0) {
                ApplicantDTO applicantDto = serviceOrderDto.getApplicant();
                if (applicantDto != null && applicantBirthday != null) {
                    applicantDto.setBirthday(new Date(Long.parseLong(applicantBirthday)));
                    if (applicantService.update(applicantDto) <= 0)
                        LOG.error("申请人生日修改失败! (serviceOrderId:" + serviceOrderDto.getId() + ", applicantId:"
                                + applicantDto.getId() + ", applicantBirthday:" + applicantDto.getBirthday() + ")");
                    else
                        LOG.info("申请人生日修改成功. (serviceOrderId:" + serviceOrderDto.getId() + ", applicantId:"
                                + applicantDto.getId() + ", applicantBirthday:" + applicantDto.getBirthday() + ")");
                }
                return new Response<Integer>(0, i);
            } else
                return new Response<Integer>(1, "修改失败.", 0);
        } catch (ServiceException e) {
            return new Response<Integer>(e.getCode(), e.getMessage(), null);
        }
    }

    @RequestMapping(value = "/updateVoucherImageUrl", method = RequestMethod.POST)
    @ResponseBody
    public Response<Integer> updateVoucherImageUrl(@RequestParam(value = "id") int id,
                                                   @RequestParam(value = "coePaymentVoucherImageUrl1", required = false) String coePaymentVoucherImageUrl1,
                                                   @RequestParam(value = "coePaymentVoucherImageUrl2", required = false) String coePaymentVoucherImageUrl2,
                                                   @RequestParam(value = "coePaymentVoucherImageUrl3", required = false) String coePaymentVoucherImageUrl3,
                                                   @RequestParam(value = "coePaymentVoucherImageUrl4", required = false) String coePaymentVoucherImageUrl4,
                                                   @RequestParam(value = "coePaymentVoucherImageUrl5", required = false) String coePaymentVoucherImageUrl5,
                                                   @RequestParam(value = "visaVoucherImageUrl", required = false) String visaVoucherImageUrl,
                                                   HttpServletRequest request, HttpServletResponse response) {
//		if (getOfficialAdminId(request) != null)
//			return new Response<Integer>(1, "文案管理员不可操作服务订单.", 0);
        try {
            super.setPostHeader(response);
            AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
            if (adminUserLoginInfo != null)
                if (adminUserLoginInfo == null || (!"SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
                        && !"WA".equalsIgnoreCase(adminUserLoginInfo.getApList())))
                    return new Response<Integer>(1, "仅限文案修改.", null);
            ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(id);
            if (serviceOrderDto == null)
                return new Response<Integer>(1, "服务订单不存在,修改失败.", 0);
//			if (getOfficialAdminId(request) != null && serviceOrderDto.getOfficialId() != getOfficialId(request))
//				return new Response<Integer>(1, "(文案管理员" + getOfficialId(request) + ")只能操作自己的服务订单,不可操作(文案"
//						+ serviceOrderDto.getOfficialId() + ")服务订单.", 0);
            if (StringUtil.isNotEmpty(coePaymentVoucherImageUrl1))
                serviceOrderDto.setCoePaymentVoucherImageUrl1(coePaymentVoucherImageUrl1);
            if (StringUtil.isNotEmpty(coePaymentVoucherImageUrl2))
                serviceOrderDto.setCoePaymentVoucherImageUrl2(coePaymentVoucherImageUrl2);
            if (StringUtil.isNotEmpty(coePaymentVoucherImageUrl3))
                serviceOrderDto.setCoePaymentVoucherImageUrl3(coePaymentVoucherImageUrl3);
            if (StringUtil.isNotEmpty(coePaymentVoucherImageUrl4))
                serviceOrderDto.setCoePaymentVoucherImageUrl4(coePaymentVoucherImageUrl4);
            if (StringUtil.isNotEmpty(coePaymentVoucherImageUrl5))
                serviceOrderDto.setCoePaymentVoucherImageUrl5(coePaymentVoucherImageUrl5);
            if (StringUtil.isNotEmpty(visaVoucherImageUrl))
                serviceOrderDto.setVisaVoucherImageUrl(visaVoucherImageUrl);
            int i = serviceOrderService.updateServiceOrder(serviceOrderDto);
            if (i > 0) {
                return new Response<Integer>(0, i);
            } else {
                return new Response<Integer>(1, "修改失败.", 0);
            }
        } catch (ServiceException e) {
            return new Response<Integer>(e.getCode(), e.getMessage(), null);
        }
    }

    @RequestMapping(value = "/updateRemarks", method = RequestMethod.POST)
    @ResponseBody
    public Response<Integer> updateRemarks(@RequestParam(value = "id") int id,
                                           @RequestParam(value = "remarks", required = false) String remarks, HttpServletRequest request,
                                           HttpServletResponse response) {
//		if (getOfficialAdminId(request) != null)
//			return new Response<Integer>(1, "文案管理员不可操作服务订单.", 0);
        try {
            super.setPostHeader(response);
            AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
            if (adminUserLoginInfo != null)
                if (adminUserLoginInfo == null || (!"SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
                        && !"WA".equalsIgnoreCase(adminUserLoginInfo.getApList())))
                    return new Response<Integer>(1, "仅限文案修改.", null);
            ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(id);
            if (serviceOrderDto == null)
                return new Response<Integer>(1, "服务订单不存在,修改失败.", 0);
//			if (getOfficialAdminId(request) != null && serviceOrderDto.getOfficialId() != getOfficialId(request))
//				return new Response<Integer>(1, "(文案管理员" + getOfficialId(request) + ")只能操作自己的服务订单,不可操作(文案"
//						+ serviceOrderDto.getOfficialId() + ")服务订单.", 0);
            if (StringUtil.isNotEmpty(remarks))
                serviceOrderDto.setRemarks(remarks);
            int i = serviceOrderService.updateServiceOrder(serviceOrderDto);
            return i > 0 ? new Response<Integer>(0, i) : new Response<Integer>(1, "修改失败.", 0);
        } catch (ServiceException e) {
            return new Response<Integer>(e.getCode(), e.getMessage(), null);
        }
    }

    @RequestMapping(value = "/updateRealPeopleNumber", method = RequestMethod.POST)
    @ResponseBody
    public Response<Integer> updateRealPeopleNumber(@RequestParam(value = "id") int id,
                                                    @RequestParam(value = "realPeopleNumber", required = false) Integer realPeopleNumber,
                                                    HttpServletRequest request, HttpServletResponse response) {
        try {
            super.setPostHeader(response);
            AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
            if (adminUserLoginInfo != null)
                if (adminUserLoginInfo == null || (!"SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
                        && !"WA".equalsIgnoreCase(adminUserLoginInfo.getApList())))
                    return new Response<Integer>(1, "仅限文案修改.", null);
            ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(id);
            if (serviceOrderDto == null)
                return new Response<Integer>(1, "服务订单不存在,修改失败.", 0);
            serviceOrderDto
                    .setRealPeopleNumber(realPeopleNumber != null && realPeopleNumber > 0 ? realPeopleNumber : 1);
            int i = serviceOrderService.updateServiceOrder(serviceOrderDto);
            return i > 0 ? new Response<Integer>(0, i) : new Response<Integer>(1, "修改失败.", 0);
        } catch (ServiceException e) {
            return new Response<Integer>(e.getCode(), e.getMessage(), null);
        }
    }

    @RequestMapping(value = "/updateOfficial", method = RequestMethod.POST)
    @ResponseBody
    public Response<Integer> updateOfficial(@RequestParam(value = "id") int id,
                                            @RequestParam(value = "officialId") Integer officialId, HttpServletRequest request,
                                            HttpServletResponse response) {
        if (getOfficialAdminId(request) == null)
            return new Response<Integer>(1, "仅限文案管理员操作.", 0);
        try {
            super.setPostHeader(response);
            ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(id);
            if (serviceOrderDto == null)
                return new Response<Integer>(1, "服务订单不存在,修改失败.", 0);
            serviceOrderDto.setOfficialId(officialId);
            int i = serviceOrderService.updateServiceOrder(serviceOrderDto);
            return i > 0 ? new Response<Integer>(0, i) : new Response<Integer>(1, "修改失败.", 0);
        } catch (ServiceException e) {
            return new Response<Integer>(e.getCode(), e.getMessage(), null);
        }
    }

    /**
     * 更新服务订单已提交申请时间,readcommitted_date字段
     *
     * @param id
     * @param readcommittedDate
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/updateReadcommittedDate", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(rollbackFor = ServiceException.class)
    public Response<Integer> updateReadcommittedDate(@RequestParam(value = "id") int id,
                                                     @RequestParam(value = "readcommittedDate") String readcommittedDate, HttpServletRequest request,
                                                     HttpServletResponse response) {
        if (getOfficialAdminId(request) == null)
            return new Response<Integer>(1, "仅限文案管理员操作.", 0);
        try {
            super.setPostHeader(response);
            ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(id);
            if (serviceOrderDto == null)
                return new Response<Integer>(1, "服务订单不存在,修改失败.", 0);
            ServiceOrderReadcommittedDateDO serviceOrderReadcommittedDateDO = new ServiceOrderReadcommittedDateDO();
            serviceOrderReadcommittedDateDO.setServiceOrderId(serviceOrderDto.getId());
            serviceOrderReadcommittedDateDO.setHistoryDate(serviceOrderDto.getReadcommittedDate());
            serviceOrderDto.setReadcommittedDate(sdf.parse(readcommittedDate));
            int i = serviceOrderService.updateServiceOrder(serviceOrderDto);
            if (i > 0) {
                serviceOrderReadcommittedDateService.add(serviceOrderReadcommittedDateDO);
                return new Response<Integer>(0, i);
            }
            return new Response<Integer>(1, "修改失败.", 0);
        } catch (ServiceException e) {
            return new Response<Integer>(e.getCode(), e.getMessage(), null);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Response<Integer>(1, e.getMessage(), null);
        }
    }

    @RequestMapping(value = "/count", method = RequestMethod.GET)
    @ResponseBody
    @Deprecated
    public Response<Integer> countServiceOrder(@RequestParam(value = "id", required = false) Integer id,
                                               @RequestParam(value = "type", required = false) String type,
                                               @RequestParam(value = "state", required = false) String state,
                                               @RequestParam(value = "auditingState", required = false) String auditingState,
                                               @RequestParam(value = "reviewState", required = false) String reviewState,
                                               @RequestParam(value = "urgentState", required = false) String urgentState,
                                               @RequestParam(value = "startMaraApprovalDate", required = false) String startMaraApprovalDate,
                                               @RequestParam(value = "endMaraApprovalDate", required = false) String endMaraApprovalDate,
                                               @RequestParam(value = "startOfficialApprovalDate", required = false) String startOfficialApprovalDate,
                                               @RequestParam(value = "endOfficialApprovalDate", required = false) String endOfficialApprovalDate,
                                               @RequestParam(value = "startReadcommittedDate", required = false) String startReadcommittedDate,
                                               @RequestParam(value = "endReadcommittedDate", required = false) String endReadcommittedDate,
                                               @RequestParam(value = "regionId", required = false) Integer regionId,
                                               @RequestParam(value = "userId", required = false) Integer userId,
                                               @RequestParam(value = "userName", required = false) String userName,
                                               @RequestParam(value = "applicantName", required = false) String applicantName,
                                               @RequestParam(value = "maraId", required = false) Integer maraId,
                                               @RequestParam(value = "adviserId", required = false) Integer adviserId,
                                               @RequestParam(value = "officialId", required = false) Integer officialId,
                                               @RequestParam(value = "officialTagId", required = false) Integer officialTagId,
                                               @RequestParam(value = "isNotApproved", required = false) Boolean isNotApproved,
                                               @RequestParam(value = "serviceId", required = false) Integer serviceId,
                                               @RequestParam(value = "schoolId", required = false) Integer schoolId, HttpServletRequest request,
                                               HttpServletResponse response) {

        String excludeState = null;
        List<String> stateList = null;
        if (state != null && !"".equals(state))
            stateList = new ArrayList<>(Arrays.asList(state.split(",")));
        List<String> reviewStateList = null;
        if (reviewState != null && !"".equals(reviewState))
            reviewStateList = new ArrayList<>(Arrays.asList(reviewState.split(",")));
        Integer newMaraId = getMaraId(request);
        if (newMaraId != null) {
            maraId = newMaraId;
            excludeState = ReviewAdviserStateEnum.PENDING.toString();
            reviewStateList = new ArrayList<>();
            reviewStateList.add(ServiceOrderReviewStateEnum.ADVISER.toString());
            reviewStateList.add(ServiceOrderReviewStateEnum.MARA.toString());
            reviewStateList.add(ServiceOrderReviewStateEnum.OFFICIAL.toString());
        }
        Integer newOfficialId = getOfficialId(request);
        if (newOfficialId != null) {
            if (getOfficialAdminId(request) == null)
                officialId = newOfficialId; // 非文案管理员就只显示自己的单子
            excludeState = ReviewAdviserStateEnum.PENDING.toString();
        }

        List<Integer> regionIdList = null;
        if (regionId != null && regionId > 0)
            regionIdList = ListUtil.buildArrayList(regionId);

        try {
            super.setGetHeader(response);

            // 处理顾问管理员
            AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
            if (adminUserLoginInfo != null && "GW".equalsIgnoreCase(adminUserLoginInfo.getApList())
                    && adminUserLoginInfo.getRegionId() != null && adminUserLoginInfo.getRegionId() > 0) {
                List<RegionDTO> regionList = regionService.listRegion(adminUserLoginInfo.getRegionId());
                regionIdList = ListUtil.buildArrayList(adminUserLoginInfo.getRegionId());
                for (RegionDTO region : regionList)
                    regionIdList.add(region.getId());
            } else {
                Integer newAdviserId = getAdviserId(request);
                if (newAdviserId != null)
                    adviserId = newAdviserId;
                if (adminUserLoginInfo == null)
                    return new Response<Integer>(0, "No permission !", 0);
            }

            if (id != null && id > 0) {
                if (serviceOrderService.getServiceOrderById(id) != null)
                    return new Response<Integer>(0, 1);
                else
                    return new Response<Integer>(0, 0);
            }

            return new Response<Integer>(0, serviceOrderService.countServiceOrder(type, null, excludeState, stateList,
                    auditingState, reviewStateList, urgentState, startMaraApprovalDate, endMaraApprovalDate,
                    startOfficialApprovalDate, endOfficialApprovalDate, startReadcommittedDate, endReadcommittedDate,
                    regionIdList, userId, userName, applicantName, maraId, adviserId, officialId, officialTagId, 0, 0,
                    isNotApproved != null ? isNotApproved : false, serviceId, schoolId, null, null));
        } catch (ServiceException e) {
            return new Response<Integer>(1, e.getMessage(), null);
        }
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public ListResponse<List<ServiceOrderDTO>> listServiceOrder(
            @RequestParam(value = "id", required = false) Integer id,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "auditingState", required = false) String auditingState,
            @RequestParam(value = "reviewState", required = false) String reviewState,
            @RequestParam(value = "urgentState", required = false) String urgentState,
            @RequestParam(value = "startMaraApprovalDate", required = false) String startMaraApprovalDate,
            @RequestParam(value = "endMaraApprovalDate", required = false) String endMaraApprovalDate,
            @RequestParam(value = "startOfficialApprovalDate", required = false) String startOfficialApprovalDate,
            @RequestParam(value = "endOfficialApprovalDate", required = false) String endOfficialApprovalDate,
            @RequestParam(value = "startReadcommittedDate", required = false) String startReadcommittedDate,
            @RequestParam(value = "endReadcommittedDate", required = false) String endReadcommittedDate,
            @RequestParam(value = "regionId", required = false) Integer regionId,
            @RequestParam(value = "userId", required = false) Integer userId,
            @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "applicantName", required = false) String applicantName,
            @RequestParam(value = "maraId", required = false) Integer maraId,
            @RequestParam(value = "adviserId", required = false) Integer adviserId,
            @RequestParam(value = "officialId", required = false) Integer officialId,
            @RequestParam(value = "officialTagId", required = false) Integer officialTagId,
            @RequestParam(value = "isNotApproved", required = false) Boolean isNotApproved,
            @RequestParam(value = "serviceId", required = false) Integer serviceId,
            @RequestParam(value = "schoolId", required = false) Integer schoolId,
            @RequestParam(value = "isSettle", required = false) Boolean isSettle,
            @RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
            @RequestParam(value = "sorter", required = false) String sorter, HttpServletRequest request,
            HttpServletResponse response) {
        List<String> excludeTypeList = null;
        String excludeState = null;
        List<String> stateList = null;
        if (state != null && !"".equals(state))
            stateList = new ArrayList<>(Arrays.asList(state.split(",")));
        List<String> reviewStateList = null;
        if (reviewState != null && !"".equals(reviewState))
            reviewStateList = new ArrayList<>(Arrays.asList(reviewState.split(",")));
        Integer newMaraId = getMaraId(request);
        if (newMaraId != null) {
            excludeTypeList = ListUtil.buildArrayList("ZX");
            maraId = newMaraId;
            excludeState = ReviewAdviserStateEnum.PENDING.toString();
            if (stateList == null)
                stateList = ListUtil.buildArrayList("WAIT", "FINISH", "APPLY", "COMPLETE", "CLOSE");
//			reviewStateList = new ArrayList<>();
//			reviewStateList.add(ServiceOrderReviewStateEnum.ADVISER.toString());
//			reviewStateList.add(ServiceOrderReviewStateEnum.MARA.toString());
//			reviewStateList.add(ServiceOrderReviewStateEnum.OFFICIAL.toString());
        }
        Integer newOfficialId = getOfficialId(request);
        if (newOfficialId != null) {
            excludeTypeList = ListUtil.buildArrayList("ZX");
            if (getOfficialAdminId(request) == null)
                officialId = newOfficialId; // 非文案管理员就只显示自己的单子
            excludeState = ReviewAdviserStateEnum.PENDING.toString();
        }

        List<Integer> regionIdList = null;
        if (regionId != null && regionId > 0)
            regionIdList = ListUtil.buildArrayList(regionId);

        Sorter _sorter = null;
        if (sorter != null)
            _sorter = JSON.parseObject(sorter.replace("adviser,name", "adviserName"), Sorter.class);

        try {
            super.setGetHeader(response);
            // 处理顾问管理员
            AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
            if (adminUserLoginInfo != null && "GW".equalsIgnoreCase(adminUserLoginInfo.getApList())
                    && adminUserLoginInfo.getRegionId() != null && adminUserLoginInfo.getRegionId() > 0) {
                List<RegionDTO> regionList = regionService.listRegion(adminUserLoginInfo.getRegionId());
                regionIdList = ListUtil.buildArrayList(adminUserLoginInfo.getRegionId());
                for (RegionDTO region : regionList)
                    regionIdList.add(region.getId());
            } else {
                Integer newAdviserId = getAdviserId(request);
                if (newAdviserId != null)
                    adviserId = newAdviserId;
                if (adminUserLoginInfo == null)
                    return new ListResponse<List<ServiceOrderDTO>>(false, pageSize, 0, null, "No permission !");
            }

            if (id != null && id > 0) {
                List<ServiceOrderDTO> list = new ArrayList<ServiceOrderDTO>();
                ServiceOrderDTO serviceOrder = serviceOrderService.getServiceOrderById(id);
                if ((serviceOrder != null && ((adviserId != null && serviceOrder.getAdviserId() == adviserId)
                        || (officialId != null && serviceOrder.getOfficialId() == officialId)))
                        || isSuperAdminUser(request) || getOfficialAdminId(request) != null || getKjId(request) != null)
                    if (serviceOrder != null)
                        list.add(serviceOrder);
                /*
                 * serviceOrder.setCommissionOrderDTOList(serviceOrderService.
                 * getCommissionOrderList(id));
                 */
                return new ListResponse<List<ServiceOrderDTO>>(true, pageSize, list.size(), list, "");
            }

            int total = serviceOrderService.countServiceOrder(type, excludeTypeList, excludeState, stateList,
                    auditingState, reviewStateList, urgentState, startMaraApprovalDate, endMaraApprovalDate,
                    startOfficialApprovalDate, endOfficialApprovalDate, startReadcommittedDate, endReadcommittedDate,
                    regionIdList, userId, userName, applicantName, maraId, adviserId, officialId, officialTagId, 0, 0,
                    isNotApproved != null ? isNotApproved : false, serviceId, schoolId, null, isSettle);
            List<ServiceOrderDTO> serviceOrderList = serviceOrderService.listServiceOrder(type, excludeTypeList,
                    excludeState, stateList, auditingState, reviewStateList, urgentState, startMaraApprovalDate,
                    endMaraApprovalDate, startOfficialApprovalDate, endOfficialApprovalDate, startReadcommittedDate,
                    endReadcommittedDate, regionIdList, userId, userName, applicantName, maraId, adviserId, officialId,
                    officialTagId, 0, 0, isNotApproved != null ? isNotApproved : false, pageNum, pageSize, _sorter,
                    serviceId, schoolId, null, isSettle);

            if (newOfficialId != null)
                for (ServiceOrderDTO so : serviceOrderList)
                    so.setOfficialNotes(serviceOrderService.listOfficialRemarks(so.getId(), newOfficialId)); // 写入note
            /*
             * if (newOfficialId != null){ for (ServiceOrderDTO so : serviceOrderList) {
             * so.setCommissionOrderDTOList(serviceOrderService.getCommissionOrderList(so.
             * getId())); } }
             */
            return new ListResponse<List<ServiceOrderDTO>>(true, pageSize, total, serviceOrderList, "");
        } catch (ServiceException e) {
            return new ListResponse<List<ServiceOrderDTO>>(false, pageSize, 0, null, e.getMessage());
        }
    }

    @RequestMapping(value = "/get", method = RequestMethod.GET)
    @ResponseBody
    public Response<ServiceOrderDTO> getServiceOrder(@RequestParam(value = "id") int id, HttpServletRequest request,
                                                     HttpServletResponse response) {
        try {
            super.setGetHeader(response);
            ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(id);
            if (getAdminUserLoginInfo(request) != null && getOfficialId(request) != null)
                serviceOrderDto.setOfficialNotes(serviceOrderService.listOfficialRemarks(id, getOfficialId(request))); // 写入文案note
            return new Response<ServiceOrderDTO>(0, serviceOrderDto);
        } catch (ServiceException e) {
            return new Response<ServiceOrderDTO>(1, e.getMessage(), null);
        }
    }

    @RequestMapping(value = "/adviserDelete", method = RequestMethod.DELETE)
    @ResponseBody
    public Response<Integer> adviserDeleteServiceOrder(@RequestParam(value = "id") int id, HttpServletRequest request,
                                                       HttpServletResponse response) {
        try {
            super.setGetHeader(response);
            Integer newAdviserId = getAdviserId(request);
            if (newAdviserId != null) {
                ServiceOrderDTO serviceOrder = serviceOrderService.getServiceOrderById(id);
                if (ObjectUtil.isNotNull(serviceOrder) && "PENDING".equalsIgnoreCase(serviceOrder.getState())) {
//                    return new Response<Integer>(0, serviceOrderService.deleteServiceOrderById(id));
                	return new Response<Integer>(1, "删除功能暂不可用！", 0);
                } else
                    return new Response<Integer>(1,
                            StringUtil.merge("服务订单", id, "的状态为", serviceOrder.getState(), "，操作失败！"), 0);
            } else
                return new Response<Integer>(1, "当前帐号不是顾问帐号，请使用其它接口操作！", 0);
        } catch (ServiceException e) {
            return new Response<Integer>(1, e.getMessage(), 0);
        }
    }

    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    @ResponseBody
    public Response<Integer> deleteServiceOrder(@RequestParam(value = "id") int id, HttpServletResponse response) {
        try {
            super.setGetHeader(response);
            return new Response<Integer>(0, serviceOrderService.deleteServiceOrderById(id));
        } catch (ServiceException e) {
            return new Response<Integer>(1, e.getMessage(), 0);
        }
    }

    @RequestMapping(value = "/finish", method = RequestMethod.GET)
    @ResponseBody
    public Response<Integer> finish(@RequestParam(value = "id") int id, HttpServletRequest request,
                                    HttpServletResponse response) {
        super.setPostHeader(response);
        try {
            return serviceOrderService.getServiceOrderById(id) != null
                    ? new Response<Integer>(0, serviceOrderService.finish(id))
                    : null;
        } catch (ServiceException e) {
            return new Response<Integer>(1, e.getMessage(), -1);
        }
    }

    @RequestMapping(value = "/approval", method = RequestMethod.POST)
    @ResponseBody
    public Response<ServiceOrderDTO> approval(@RequestParam(value = "id") int id,
                                              @RequestParam(value = "state") String state,
                                              @RequestParam(value = "subagencyId", required = false) String subagencyId,
                                              @RequestParam(value = "remarks", required = false) String remarks, HttpServletRequest request,
                                              HttpServletResponse response) {
        try {
            super.setPostHeader(response);
            if (ReviewAdviserStateEnum.CLOSE.toString().equalsIgnoreCase(state))
                return new Response<ServiceOrderDTO>(1, "关闭操作请调用'refuse'接口.", null);
            // 获取服务订单
            ServiceOrderDTO serviceOrderDto = null;
            try {
                serviceOrderDto = serviceOrderService.getServiceOrderById(id);
            } catch (ServiceException e) {
                return new Response<ServiceOrderDTO>(1, e.getMessage(), null);
            }
            if (serviceOrderDto == null) {
                ServiceException se = new ServiceException("服务订单不存在:" + id);
                se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
                throw se;
            }
            if (!"OVST".equals(serviceOrderDto.getType()) && serviceOrderDto.isPay()
                    && serviceOrderDto.getPaymentVoucherImageUrl1() == null
                    && serviceOrderDto.getPaymentVoucherImageUrl2() == null) {
                ServiceException se = new ServiceException("支付凭证不能为空!");
                se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
                throw se;
            }
            // 审核
            AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
            if (adminUserLoginInfo != null)
                if ("SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
                        || "GW".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
                    if (ReviewAdviserStateEnum.get(state) != null)
                        if (ReviewAdviserStateEnum.REVIEW.toString().equals(state.toUpperCase())) { // 顾问审核
                            if (serviceOrderDto.getParentId() == 0 && ("SIV".equalsIgnoreCase(serviceOrderDto.getType())
                                    || "NSV".equalsIgnoreCase(serviceOrderDto.getType())
                                    || "MT".equalsIgnoreCase(serviceOrderDto.getType()))) {
                                return new Response<ServiceOrderDTO>(1, "该订单不支持审核.", serviceOrderDto);
                                // List<ServiceOrderDTO> serviceOrderList =
                                // serviceOrderService.listServiceOrder(null,
                                // null, null, null, 0, 0, 0, 0,
                                // serviceOrderDto.getId(), 0, 10);
                                // for (ServiceOrderDTO so : serviceOrderList) {
                                // if (so.getServicePackage() == null)
                                // return new Response<ServiceOrderDTO>(1,
                                // "子订单没有服务包.", so);
                                // if (so.getOfficialId() <= 0)
                                // return new Response<ServiceOrderDTO>(1,
                                // "子订单必须选择文案.", so);
                                // if (so.getMaraId() <= 0
                                // &&
                                // !"EOI".equalsIgnoreCase(so.getServicePackage().getType()))
                                // return new Response<ServiceOrderDTO>(1,
                                // "子订单必须选择Mara.", so);
                                // serviceOrderService.approval(so.getId(),
                                // adminUserLoginInfo.getId(),
                                // state.toUpperCase(), null, null, null);
                                // }
                            }
                            ServiceOrderDTO serviceOrderDTO = serviceOrderService.approval(id,
                                    adminUserLoginInfo.getId(), state.toUpperCase(), null, null, null);
                            // wxWorkService.sendMsg(serviceOrderDto.getId());
                            return new Response<ServiceOrderDTO>(0, serviceOrderDTO);
                        } else if (ReviewAdviserStateEnum.PAID.toString().equals(state.toUpperCase())) { // 顾问支付同时修改文案状态
                            serviceOrderService.finish(id);
                            return new Response<ServiceOrderDTO>(0,
                                    serviceOrderService.approval(id, adminUserLoginInfo.getId(), state.toUpperCase(),
                                            null, ReviewOfficialStateEnum.PAID.toString(), null));
                        } else if (ReviewAdviserStateEnum.COMPLETE.toString().equals(state.toUpperCase())) { // 顾问完成同时修改文案和会计状态
                            serviceOrderService.finish(id);
                            return new Response<ServiceOrderDTO>(0,
                                    serviceOrderService.approval(id, adminUserLoginInfo.getId(), state.toUpperCase(),
                                            null, ReviewOfficialStateEnum.COMPLETE.toString(), null));
                        } else
                            return new Response<ServiceOrderDTO>(0, serviceOrderService.approval(id,
                                    adminUserLoginInfo.getId(), state.toUpperCase(), null, null, null));
                    else
                        return new Response<ServiceOrderDTO>(1, "state错误!(" + state + ")", null);
                } else if ("MA".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
                    if (!"VISA".equalsIgnoreCase(serviceOrderDto.getType()))
                        return new Response<ServiceOrderDTO>(1, "Mara审核仅限签证服务订单!", null);
                    if (ReviewMaraStateEnum.get(state) != null
                            && ReviewMaraStateEnum.FINISH.toString().equals(state.toUpperCase())) { // Mara审核通过同时修改状态
                        serviceOrderService.updateServiceOrderRviewState(id,
                                ServiceOrderReviewStateEnum.MARA.toString());
                        return new Response<ServiceOrderDTO>(0,
                                serviceOrderService.approval(id, adminUserLoginInfo.getId(), null, state.toUpperCase(),
                                        ReviewOfficialStateEnum.REVIEW.toString(), null));
                    }
                    if (ReviewMaraStateEnum.get(state) != null
                            && !ReviewMaraStateEnum.REVIEW.toString().equals(state.toUpperCase())) // mara调用approval方法不能驳回
                        return new Response<ServiceOrderDTO>(0, serviceOrderService.approval(id,
                                adminUserLoginInfo.getId(), null, state.toUpperCase(), null, null));
                    else
                        return new Response<ServiceOrderDTO>(1, "state错误!(" + state + ")", null);
                } else if ("WA".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
                    if (ReviewOfficialStateEnum.get(state) != null
                            && !ReviewOfficialStateEnum.CLOSE.toString().equals(state.toUpperCase())) { // 文案调用approval方法不能关闭
                        if (ReviewOfficialStateEnum.FINISH.toString().equals(state.toUpperCase())) // 文案审核通过同时修改状态
                            serviceOrderService.updateServiceOrderRviewState(id,
                                    ServiceOrderReviewStateEnum.OFFICIAL.toString());
                        if (ReviewOfficialStateEnum.WAIT.toString().equals(state.toUpperCase())) { // 文案提交mara审核
                            serviceOrderService.updateServiceOrderRviewState(id,
                                    ServiceOrderReviewStateEnum.ADVISER.toString());
                            waUpdateSubagency(serviceOrderDto, subagencyId);
                            waUpdateRemarks(serviceOrderDto, remarks);
                            return new Response<ServiceOrderDTO>(0,
                                    serviceOrderService.approval(id, adminUserLoginInfo.getId(), null,
                                            ReviewMaraStateEnum.WAIT.toString(), state.toUpperCase(), null));
                        } else if (ReviewOfficialStateEnum.APPLY.toString().equals(state.toUpperCase())) { // 文案申请同时修改顾问状态
                            // serviceOrderService.finish(id);
                            serviceOrderService.Readcommitted(id);
                            waUpdateSubagency(serviceOrderDto, subagencyId);
                            waUpdateRemarks(serviceOrderDto, remarks);
                            return new Response<ServiceOrderDTO>(0,
                                    serviceOrderService.approval(id, adminUserLoginInfo.getId(),
                                            ReviewAdviserStateEnum.APPLY.toString(), null, state.toUpperCase(), null));
                        } else if (ReviewOfficialStateEnum.PAID.toString().equals(state.toUpperCase())) { // 文案支付同时修改顾问状态
                            serviceOrderService.Readcommitted(id);
                            serviceOrderService.finish(id);
                            return new Response<ServiceOrderDTO>(0,
                                    serviceOrderService.approval(id, adminUserLoginInfo.getId(),
                                            ReviewAdviserStateEnum.PAID.toString(), null, state.toUpperCase(), null));
                        } else if (ReviewOfficialStateEnum.COMPLETE.toString().equals(state.toUpperCase())) { // 文案完成同时修改顾问和会计状态
                            // serviceOrderService.finish(id);
                            serviceOrderService.Readcommitted(id);
                            waUpdateRemarks(serviceOrderDto, remarks);
                            return new Response<ServiceOrderDTO>(0,
                                    serviceOrderService.approval(id, adminUserLoginInfo.getId(),
                                            ReviewAdviserStateEnum.COMPLETE.toString(), null, state.toUpperCase(),
                                            null));
                        } else {
                            waUpdateSubagency(serviceOrderDto, subagencyId);
                            waUpdateRemarks(serviceOrderDto, remarks);
                            return new Response<ServiceOrderDTO>(0, serviceOrderService.approval(id,
                                    adminUserLoginInfo.getId(), null, null, state.toUpperCase(), null));
                        }
                    } else
                        return new Response<ServiceOrderDTO>(1, "state错误!(" + state + ")", null);
                } else
                    return new Response<ServiceOrderDTO>(1, "该用户无审核权限!", null);
            else
                return new Response<ServiceOrderDTO>(1, "请登录!", null);
        } catch (ServiceException e) {
            return new Response<ServiceOrderDTO>(1, e.getMessage(), null);
        }
    }

    private void waUpdateSubagency(ServiceOrderDTO serviceOrderDto, String subagencyId) throws ServiceException {
        if (StringUtil.isNotEmpty(subagencyId)) {
            serviceOrderDto.setSubagencyId(StringUtil.toInt(subagencyId));
            if (serviceOrderService.updateServiceOrder(serviceOrderDto) <= 0)
                LOG.error("文案修改失败! (subagencyId:" + subagencyId + ")");
        }
    }

    private void waUpdateRemarks(ServiceOrderDTO serviceOrderDto, String remarks) throws ServiceException {
        if (StringUtil.isNotEmpty(remarks)) {
            serviceOrderDto.setRemarks(remarks);
            if (serviceOrderService.updateServiceOrder(serviceOrderDto) <= 0)
                LOG.error("文案修改失败! (remarks:" + remarks + ")");
        }
    }

    @RequestMapping(value = "/refuse", method = RequestMethod.POST)
    @ResponseBody
    public Response<ServiceOrderDTO> refuse(@RequestParam(value = "id") int id,
                                            @RequestParam(value = "state") String state,
                                            @RequestParam(value = "refuseReason", required = false) String refuseReason,
                                            @RequestParam(value = "closedReason", required = false) String closedReason, HttpServletRequest request,
                                            HttpServletResponse response) {
        try {
            super.setPostHeader(response);
            if (ReviewAdviserStateEnum.COMPLETE.toString().equalsIgnoreCase(state)
                    || ReviewOfficialStateEnum.FINISH.toString().equalsIgnoreCase(state))
                return new Response<ServiceOrderDTO>(1, "完成操作请调用'approval'接口.", null);
            // 获取服务订单
            ServiceOrderDTO serviceOrderDto = null;
            try {
                serviceOrderDto = serviceOrderService.getServiceOrderById(id);
            } catch (ServiceException e) {
                return new Response<ServiceOrderDTO>(1, e.getMessage(), null);
            }
            // 审核
            AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
            if (adminUserLoginInfo != null)
                if ("SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
                        || "GW".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
                    if (ReviewAdviserStateEnum.get(state) != null)
                        if (ReviewAdviserStateEnum.PENDING.toString().equals(state.toUpperCase())) // 顾问撤回同时修改文案和mara状态
                            return new Response<ServiceOrderDTO>(0,
                                    serviceOrderService.refuse(id, adminUserLoginInfo.getId(), state.toUpperCase(),
                                            ReviewOfficialStateEnum.PENDING.toString(),
                                            ReviewMaraStateEnum.WAIT.toString(), null));
                        else if (ReviewOfficialStateEnum.REVIEW.toString().equals(state.toUpperCase())) // 顾问驳回同时修改mara状态
                            return new Response<ServiceOrderDTO>(0,
                                    serviceOrderService.refuse(id, adminUserLoginInfo.getId(), state.toUpperCase(),
                                            ReviewMaraStateEnum.REVIEW.toString(), null, null));
                        else if (ReviewOfficialStateEnum.CLOSE.toString().equals(state.toUpperCase())) { // 顾问关闭同时修改文案状态
                            serviceOrderService.finish(id);
                            // 更新关闭原因
                            if (StringUtil.isNotEmpty(closedReason)) {
                                serviceOrderDto.setClosedReason(closedReason);
                                serviceOrderService.updateServiceOrder(serviceOrderDto);
                            }
                            return new Response<ServiceOrderDTO>(0,
                                    serviceOrderService.refuse(id, adminUserLoginInfo.getId(), state.toUpperCase(),
                                            null, ReviewOfficialStateEnum.CLOSE.toString(), null));
                        } else
                            return new Response<ServiceOrderDTO>(0, serviceOrderService.refuse(id,
                                    adminUserLoginInfo.getId(), state.toUpperCase(), null, null, null));
                    else
                        return new Response<ServiceOrderDTO>(1, "state错误!(" + state + ")", null);
                } else if ("MA".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
                    if (!"VISA".equalsIgnoreCase(serviceOrderDto.getType()))
                        return new Response<ServiceOrderDTO>(1, "Mara审核仅限签证服务订单!", null);
                    if (ReviewMaraStateEnum.get(state) != null) {
                        // 更新驳回原因
                        if (StringUtil.isNotEmpty(refuseReason)) {
                            serviceOrderDto.setRefuseReason(refuseReason);
                            serviceOrderService.updateServiceOrder(serviceOrderDto);
                        }
                        if (ReviewMaraStateEnum.REVIEW.toString().equals(state.toUpperCase())) { // mara驳回同时修改顾问状态
                            serviceOrderService.updateServiceOrderRviewState(id, null);
                            return new Response<ServiceOrderDTO>(0,
                                    serviceOrderService.refuse(id, adminUserLoginInfo.getId(),
                                            ReviewAdviserStateEnum.REVIEW.toString(), state.toUpperCase(),
                                            ReviewOfficialStateEnum.REVIEW.toString(), null));
                        } else
                            return new Response<ServiceOrderDTO>(0, serviceOrderService.refuse(id,
                                    adminUserLoginInfo.getId(), null, state.toUpperCase(), null, null));
                    } else
                        return new Response<ServiceOrderDTO>(1, "state错误!(" + state + ")", null);
                } else if ("WA".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
                    if (ReviewOfficialStateEnum.get(state) != null) {
                        // 更新驳回原因
                        if (StringUtil.isNotEmpty(refuseReason)) {
                            serviceOrderDto.setRefuseReason(refuseReason);
                            serviceOrderService.updateServiceOrder(serviceOrderDto);
                        }
                        if (ReviewOfficialStateEnum.CLOSE.toString().equals(state.toUpperCase())) { // 文案关闭同时修改顾问状态
                            serviceOrderService.finish(id);
                            // 更新关闭原因
                            if (StringUtil.isNotEmpty(closedReason)) {
                                serviceOrderDto.setClosedReason(closedReason);
                                serviceOrderService.updateServiceOrder(serviceOrderDto);
                            }
                            return new Response<ServiceOrderDTO>(0,
                                    serviceOrderService.refuse(id, adminUserLoginInfo.getId(),
                                            ReviewAdviserStateEnum.CLOSE.toString(), null, state.toUpperCase(), null));
                        } else if (ReviewOfficialStateEnum.PENDING.toString().equals(state.toUpperCase())) // 文案驳回同时修改顾问状态
                            return new Response<ServiceOrderDTO>(0,
                                    serviceOrderService.refuse(id, adminUserLoginInfo.getId(),
                                            ReviewAdviserStateEnum.PENDING.toString(), null, state.toUpperCase(),
                                            null));
                        else
                            return new Response<ServiceOrderDTO>(0, serviceOrderService.refuse(id,
                                    adminUserLoginInfo.getId(), null, null, state.toUpperCase(), null));
                    } else
                        return new Response<ServiceOrderDTO>(1, "state错误!(" + state + ")", null);
                } else
                    return new Response<ServiceOrderDTO>(1, "该用户无审核权限!", null);
            else
                return new Response<ServiceOrderDTO>(1, "请登录!", null);
        } catch (ServiceException e) {
            return new Response<ServiceOrderDTO>(1, e.getMessage(), null);
        }
    }

    @RequestMapping(value = "/reviews", method = RequestMethod.GET)
    @ResponseBody
    public Response<List<ServiceOrderReviewDTO>> reviews(@RequestParam(value = "serviceOrderId") String serviceOrderId,
                                                         HttpServletResponse response) {
        try {
            super.setGetHeader(response);
            return new Response<List<ServiceOrderReviewDTO>>(0,
                    serviceOrderService.reviews(StringUtil.toInt(serviceOrderId)));
        } catch (ServiceException e) {
            return new Response<List<ServiceOrderReviewDTO>>(1, e.getMessage(), null);
        }
    }

    @RequestMapping(value = "/addComment", method = RequestMethod.POST)
    @ResponseBody
    public Response<Integer> addComment(@RequestParam(value = "adminUserId", required = false) Integer adminUserId,
                                        @RequestParam(value = "serviceOrderId") Integer serviceOrderId,
                                        @RequestParam(value = "content") String content, HttpServletRequest request, HttpServletResponse response) {
        try {
            AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
            super.setPostHeader(response);
            ServiceOrderDTO serviceOrder = serviceOrderService.getServiceOrderById(serviceOrderId);
            if (serviceOrder == null)
                return new Response<Integer>(1, "服务订单为空.", 0);
            ServiceOrderCommentDTO serviceOrderCommentDto = new ServiceOrderCommentDTO();
            serviceOrderCommentDto
                    .setAdminUserId(adminUserLoginInfo != null ? adminUserLoginInfo.getId() : adminUserId);
            serviceOrderCommentDto.setServiceOrderId(serviceOrderId);
            serviceOrderCommentDto.setContent(content);
            if (serviceOrderService.addComment(serviceOrderCommentDto) > 0) {
                // 发送邮件
                String serviceType = "?";
                if ("VISA".equalsIgnoreCase(serviceOrder.getType()))
                    serviceType = "签证";
                else if ("OVST".equalsIgnoreCase(serviceOrder.getType()))
                    serviceType = "留学";
                else if ("SIV".equalsIgnoreCase(serviceOrder.getType()))
                    serviceType = "独立技术移民";
                else if ("NSV".equalsIgnoreCase(serviceOrder.getType()))
                    serviceType = "雇主担保";
                else if ("MT".equalsIgnoreCase(serviceOrder.getType()))
                    serviceType = "曼拓";
                String title = "您的" + serviceType + "订单" + serviceOrder.getId() + "有最新评论";
                String message = "您的服务订单有一条新的评论，请及时查看．<br/>服务订单类型:" + serviceType + "<br/>申请人:"
                        + (serviceOrder.getUser() != null ? serviceOrder.getUser().getName() : "") + "<br/>订单ID:"
                        + serviceOrder.getId() + "<br/>评论内容:" + serviceOrderCommentDto.getContent() + "<br/>评论时间:"
                        + new Date()
                        + "<br/><br/><a href='https://yongjinbiao.zhinanzhen.org/webroot_new/serviceorderdetail/id?"
                        + serviceOrder.getId() + "'>服务订单详情</a>";
                String email = "";
                if (adminUserLoginInfo != null && "GW".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
                    OfficialDTO official = serviceOrder.getOfficial();
                    if (official != null)
                        email = official.getEmail();
                    MaraDTO mara = serviceOrder.getMara();
                    if (mara != null)
                        if ("".equals(email))
                            email = mara.getEmail();
                        else
                            email = email + "," + mara.getEmail();
                } else if (adminUserLoginInfo != null && "WA".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
                    AdviserDTO adviser = serviceOrder.getAdviser();
                    if (adviser != null)
                        email = adviser.getEmail();
                    MaraDTO mara = serviceOrder.getMara();
                    if (mara != null)
                        if ("".equals(email))
                            email = mara.getEmail();
                        else
                            email = email + "," + mara.getEmail();
                } else if (adminUserLoginInfo != null && "MA".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
                    MaraDTO mara = serviceOrder.getMara();
                    if (mara != null)
                        if ("".equals(email))
                            email = mara.getEmail();
                        else
                            email = email + "," + mara.getEmail();
                } else if (adminUserLoginInfo != null && "M".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
                    OfficialDTO official = serviceOrder.getOfficial();
                    if (official != null)
                        email = official.getEmail();
                    AdviserDTO adviser = serviceOrder.getAdviser();
                    if (adviser != null)
                        if ("".equals(email))
                            email = adviser.getEmail();
                        else
                            email = email + "," + adviser.getEmail();
                }
                if (!"".equals(email))
                    SendEmailUtil.send(email, title, message);
                return new Response<Integer>(0, serviceOrderCommentDto.getId());
            } else
                return new Response<Integer>(1, "创建失败.", 0);
        } catch (ServiceException e) {
            return new Response<Integer>(e.getCode(), e.getMessage(), 0);
        }
    }

    @RequestMapping(value = "/countComment", method = RequestMethod.GET)
    @ResponseBody
    public Response<Integer> countComment(@RequestParam(value = "serviceOrderId") Integer serviceOrderId,
                                          HttpServletResponse response) {
        try {
            super.setGetHeader(response);
            return new Response<Integer>(0, serviceOrderService.listComment(serviceOrderId).size());
        } catch (ServiceException e) {
            return new Response<Integer>(1, e.getMessage(), null);
        }
    }

    @RequestMapping(value = "/listComment", method = RequestMethod.GET)
    @ResponseBody
    public Response<List<ServiceOrderCommentDTO>> listComment(
            @RequestParam(value = "serviceOrderId") Integer serviceOrderId, HttpServletResponse response) {
        try {
            super.setGetHeader(response);
            return new Response<List<ServiceOrderCommentDTO>>(0, serviceOrderService.listComment(serviceOrderId));
        } catch (ServiceException e) {
            return new Response<List<ServiceOrderCommentDTO>>(1, e.getMessage(), null);
        }
    }

    @RequestMapping(value = "/listAdviserVisa", method = RequestMethod.GET)
    @ResponseBody
    public Response<List<VisaDO>> getCommissionOrderList(@RequestParam(value = "id") int id,
                                                         HttpServletRequest request) {
        try {
            List<VisaDO> commissionOrderList = serviceOrderService.getCommissionOrderList(id);
            return new Response<>(0, commissionOrderList);
        } catch (ServiceException e) {
            return new Response<>(1, e.getMessage(), null);
        }

    }

    @RequestMapping(value = "/deleteComment", method = RequestMethod.GET)
    @ResponseBody
    public Response<Integer> deleteComment(@RequestParam(value = "id") int id, HttpServletResponse response) {
        try {
            super.setGetHeader(response);
            return new Response<Integer>(0, serviceOrderService.deleteComment(id));
        } catch (ServiceException e) {
            return new Response<Integer>(1, e.getMessage(), 0);
        }
    }

    //

    @RequestMapping(value = "/addOfficialRemarks", method = RequestMethod.POST)
    @ResponseBody
    public Response<Integer> addOfficialRemarks(@RequestParam(value = "serviceOrderId") Integer serviceOrderId,
                                                @RequestParam(value = "content") String content, HttpServletRequest request, HttpServletResponse response) {
        AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
        if (adminUserLoginInfo != null)
            if (adminUserLoginInfo == null || !"WA".equalsIgnoreCase(adminUserLoginInfo.getApList())
                    || adminUserLoginInfo.getOfficialId() == null)
                return new Response<Integer>(1, "仅限文案操作.", null);
        try {
            super.setPostHeader(response);
            ServiceOrderDTO serviceOrder = serviceOrderService.getServiceOrderById(serviceOrderId);
            if (serviceOrder == null)
                return new Response<Integer>(1, "服务订单为空.", 0);
            ServiceOrderOfficialRemarksDTO serviceOrderOfficialRemarksDto = new ServiceOrderOfficialRemarksDTO();
            serviceOrderOfficialRemarksDto.setOfficialId(adminUserLoginInfo.getOfficialId());
            serviceOrderOfficialRemarksDto.setServiceOrderId(serviceOrderId);
            serviceOrderOfficialRemarksDto.setContent(content);
            if (serviceOrderService.addOfficialRemarks(serviceOrderOfficialRemarksDto) > 0)
                return new Response<Integer>(0, serviceOrderOfficialRemarksDto.getId());
            else
                return new Response<Integer>(1, "创建失败.", 0);
        } catch (ServiceException e) {
            return new Response<Integer>(e.getCode(), e.getMessage(), 0);
        }
    }

    @RequestMapping(value = "/updateOfficialRemarks", method = RequestMethod.POST)
    @ResponseBody
    public Response<Integer> updateOfficialRemarks(@RequestParam(value = "id") Integer id,
                                                   @RequestParam(value = "content") String content, HttpServletRequest request, HttpServletResponse response) {
        AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
        if (adminUserLoginInfo != null)
            if (adminUserLoginInfo == null || !"WA".equalsIgnoreCase(adminUserLoginInfo.getApList())
                    || adminUserLoginInfo.getOfficialId() == null)
                return new Response<Integer>(1, "仅限文案操作.", null);
        try {
            super.setPostHeader(response);
            ServiceOrderOfficialRemarksDTO serviceOrderOfficialRemarksDto = new ServiceOrderOfficialRemarksDTO();
            serviceOrderOfficialRemarksDto.setId(id);
            serviceOrderOfficialRemarksDto.setOfficialId(adminUserLoginInfo.getOfficialId());
            serviceOrderOfficialRemarksDto.setContent(content);
            if (serviceOrderService.updateOfficialRemarks(serviceOrderOfficialRemarksDto) > 0)
                return new Response<Integer>(0, serviceOrderOfficialRemarksDto.getId());
            else
                return new Response<Integer>(1, "修改失败.", 0);
        } catch (ServiceException e) {
            return new Response<Integer>(e.getCode(), e.getMessage(), 0);
        }
    }

    @RequestMapping(value = "/listOfficialRemarks", method = RequestMethod.GET)
    @ResponseBody
    public Response<List<ServiceOrderOfficialRemarksDTO>> listOfficialRemarks(
            @RequestParam(value = "serviceOrderId") Integer serviceOrderId, HttpServletRequest request,
            HttpServletResponse response) {
        AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
        if (adminUserLoginInfo != null)
            if (adminUserLoginInfo == null || !"WA".equalsIgnoreCase(adminUserLoginInfo.getApList())
                    || adminUserLoginInfo.getOfficialId() == null)
                return new Response<List<ServiceOrderOfficialRemarksDTO>>(1, "仅限文案操作.", null);
        try {
            super.setGetHeader(response);
            return new Response<List<ServiceOrderOfficialRemarksDTO>>(0,
                    serviceOrderService.listOfficialRemarks(serviceOrderId, adminUserLoginInfo.getOfficialId()));
        } catch (ServiceException e) {
            return new Response<List<ServiceOrderOfficialRemarksDTO>>(1, e.getMessage(), null);
        }
    }

    @RequestMapping(value = "/deleteOfficialRemarks", method = RequestMethod.GET)
    @ResponseBody
    public Response<Integer> deleteOfficialRemarks(@RequestParam(value = "id") int id, HttpServletRequest request,
                                                   HttpServletResponse response) {
        AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
        if (adminUserLoginInfo != null)
            if (adminUserLoginInfo == null || !"WA".equalsIgnoreCase(adminUserLoginInfo.getApList())
                    || adminUserLoginInfo.getOfficialId() == null)
                return new Response<Integer>(1, "仅限文案操作.", null);
        try {
            super.setGetHeader(response);
            return new Response<Integer>(0, serviceOrderService.deleteServiceOrderOfficialRemarksDTO(id));
        } catch (ServiceException e) {
            return new Response<Integer>(1, e.getMessage(), 0);
        }
    }

    @RequestMapping(value = "/down", method = RequestMethod.GET)
    @ResponseBody
    public void down(@RequestParam(value = "id", required = false) Integer id,
                     @RequestParam(value = "type", required = false) String type,
                     @RequestParam(value = "state", required = false) String state,
                     @RequestParam(value = "auditingState", required = false) String auditingState,
                     @RequestParam(value = "reviewState", required = false) String reviewState,
                     @RequestParam(value = "urgentState", required = false) String urgentState,
                     @RequestParam(value = "startMaraApprovalDate", required = false) String startMaraApprovalDate,
                     @RequestParam(value = "endMaraApprovalDate", required = false) String endMaraApprovalDate,
                     @RequestParam(value = "startOfficialApprovalDate", required = false) String startOfficialApprovalDate,
                     @RequestParam(value = "endOfficialApprovalDate", required = false) String endOfficialApprovalDate,
                     @RequestParam(value = "startReadcommittedDate", required = false) String startReadcommittedDate,
                     @RequestParam(value = "endReadcommittedDate", required = false) String endReadcommittedDate,
                     @RequestParam(value = "regionId", required = false) Integer regionId,
                     @RequestParam(value = "userId", required = false) Integer userId,
                     @RequestParam(value = "userName", required = false) String userName,
                     @RequestParam(value = "applicantName", required = false) String applicantName,
                     @RequestParam(value = "maraId", required = false) Integer maraId,
                     @RequestParam(value = "adviserId", required = false) Integer adviserId,
                     @RequestParam(value = "officialId", required = false) Integer officialId,
                     @RequestParam(value = "officialTagId", required = false) Integer officialTagId,
                     @RequestParam(value = "isNotApproved", required = false) Boolean isNotApproved,
                     @RequestParam(value = "serviceId", required = false) Integer serviceId,
                     @RequestParam(value = "schoolId", required = false) Integer schoolId, HttpServletRequest request,
                     HttpServletResponse response) {
        List<String> excludeTypeList = null;
        String excludeState = null;
        List<String> stateList = null;
        System.out.println("是这个接口---------------------------");
        if (state != null && !"".equals(state))
            stateList = new ArrayList<>(Arrays.asList(state.split(",")));
        List<String> reviewStateList = null;
        if (reviewState != null && !"".equals(reviewState))
            reviewStateList = new ArrayList<>(Arrays.asList(reviewState.split(",")));
        Integer newMaraId = getMaraId(request);
        if (newMaraId != null) {
            excludeTypeList = ListUtil.buildArrayList("ZX");
            maraId = newMaraId;
            excludeState = ReviewAdviserStateEnum.PENDING.toString();
            reviewStateList = new ArrayList<>();
            reviewStateList.add(ServiceOrderReviewStateEnum.ADVISER.toString());
            reviewStateList.add(ServiceOrderReviewStateEnum.MARA.toString());
            reviewStateList.add(ServiceOrderReviewStateEnum.OFFICIAL.toString());
        }
        Integer newOfficialId = getOfficialId(request);
        if (newOfficialId != null) {
            excludeTypeList = ListUtil.buildArrayList("ZX");
            if (getOfficialAdminId(request) == null)
                officialId = newOfficialId; // 非文案管理员就只显示自己的单子
            excludeState = ReviewAdviserStateEnum.PENDING.toString();
        }

        List<Integer> regionIdList = null;
        if (regionId != null && regionId > 0)
            regionIdList = ListUtil.buildArrayList(regionId);

        try {
            super.setGetHeader(response);
            // 处理顾问管理员
            AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
            if (adminUserLoginInfo != null && "GW".equalsIgnoreCase(adminUserLoginInfo.getApList())
                    && adminUserLoginInfo.getRegionId() != null && adminUserLoginInfo.getRegionId() > 0) {
                List<RegionDTO> regionList = regionService.listRegion(adminUserLoginInfo.getRegionId());
                regionIdList = ListUtil.buildArrayList(adminUserLoginInfo.getRegionId());
                for (RegionDTO region : regionList)
                    regionIdList.add(region.getId());
            } else {
                Integer newAdviserId = getAdviserId(request);
                if (newAdviserId != null)
                    adviserId = newAdviserId;
            }

            List<ServiceOrderDTO> serviceOrderList = null;
            if (id != null && id > 0) {
                serviceOrderList = new ArrayList<ServiceOrderDTO>();
                ServiceOrderDTO serviceOrder = serviceOrderService.getServiceOrderById(id);
                if (serviceOrder != null)
                    serviceOrderList.add(serviceOrder);
            }
            if (id == null) {
                serviceOrderList = serviceOrderService.listServiceOrder(type, excludeTypeList, excludeState, stateList,
                        auditingState, reviewStateList, urgentState, startMaraApprovalDate, endMaraApprovalDate,
                        startOfficialApprovalDate, endOfficialApprovalDate, startReadcommittedDate,
                        endReadcommittedDate, regionIdList, userId, userName, applicantName, maraId, adviserId,
                        officialId, officialTagId, 0, 0, isNotApproved != null ? isNotApproved : false, 0, 9999, null,
                        serviceId, schoolId, null, null);

                if (newOfficialId != null)
                    for (ServiceOrderDTO so : serviceOrderList)
                        so.setOfficialNotes(serviceOrderService.listOfficialRemarks(so.getId(), newOfficialId)); // 写入note
            }

            response.reset();// 清空输出流
            String tableName = "ServiceOrderInformation";
            response.setHeader("Content-disposition",
                    "attachment; filename=" + new String(tableName.getBytes("GB2312"), "8859_1") + ".xls");
            response.setContentType("application/msexcel");

            OutputStream os = response.getOutputStream();
            jxl.Workbook wb;
            InputStream is;
            try {
                is = this.getClass().getResourceAsStream("/ServiceOrderTemplate.xls");
            } catch (Exception e) {
                throw new Exception("模版不存在");
            }
            try {
                wb = Workbook.getWorkbook(is);
            } catch (Exception e) {
                throw new Exception("模版格式不支持");
            }
            WorkbookSettings settings = new WorkbookSettings();
            settings.setWriteAccess(null);
            jxl.write.WritableWorkbook wbe = Workbook.createWorkbook(os, wb, settings);

            if (wbe == null) {
                System.out.println("wbe is null !os=" + os + ",wb" + wb);
            } else {
                System.out.println("wbe not null !os=" + os + ",wb" + wb);
            }
            WritableSheet sheet = wbe.getSheet(0);
            WritableCellFormat cellFormat = new WritableCellFormat();
            int i = 1;
            for (ServiceOrderDTO so : serviceOrderList) {
                sheet.addCell(new Label(0, i, so.getId() + "", cellFormat));
                if (so.getGmtCreate() != null)
                    sheet.addCell(new Label(1, i, sdf.format(so.getGmtCreate()), cellFormat));
                if (so.getOfficialApprovalDate() != null)
                    sheet.addCell(new Label(2, i, sdf.format(so.getOfficialApprovalDate()), cellFormat));
                if (so.getFinishDate() != null)
                    sheet.addCell(new Label(3, i, sdf.format(so.getFinishDate()), cellFormat));
                if (so.getReadcommittedDate() != null)
                    sheet.addCell(new Label(4, i, sdf.format(so.getReadcommittedDate()), cellFormat));
                sheet.addCell(new Label(5, i, so.getUserId() + "", cellFormat));
                if (so.getUser() != null) {
                    sheet.addCell(new Label(6, i, so.getUser().getName() + "", cellFormat));
                    sheet.addCell(new Label(7, i, sdf.format(so.getUser().getBirthday()), cellFormat));
                    sheet.addCell(new Label(8, i, so.getUser().getPhone(), cellFormat));
                }
                if (so.getApplicant() != null) {
                    sheet.addCell(new Label(9, i, so.getApplicantId() + "", cellFormat));
                    sheet.addCell(new Label(10, i,
                            so.getApplicant().getFirstname() + " " + so.getApplicant().getSurname(), cellFormat));
                }
                if (so.getAdviser() != null)
                    sheet.addCell(new Label(11, i, so.getAdviser().getName(), cellFormat));
                if (so.getMara() != null)
                    sheet.addCell(new Label(12, i, so.getMara().getName(), cellFormat));
                if (so.getOfficial() != null)
                    sheet.addCell(new Label(13, i, so.getOfficial().getName(), cellFormat));

                if (so.getService() != null) {
                    String servicepakageName = "";
                    String tmp = "";
                    if (so.getServicePackage() != null) {
                        String servicePackagetype = so.getServicePackage().getType();
                        servicepakageName = getTypeStrOfServicePackageDTO(servicePackagetype);
                        tmp = "-";
                    }
                    sheet.addCell(new Label(14, i, so.getService().getName(), cellFormat));
                    sheet.addCell(new Label(15, i, so.getService().getCode() + tmp + servicepakageName, cellFormat));
                    if (so.getServiceAssessDO() != null)
                        sheet.addCell(new Label(14, i,
                                so.getService().getCode() + " - " + so.getServiceAssessDO().getName(), cellFormat));
                }
                if (so.getSchool() != null) {
                    sheet.addCell(new Label(14, i, " 留学 ", cellFormat));
                    sheet.addCell(new Label(15, i, so.getSchool().getName(), cellFormat));
                } else if (so.getSchoolInstitutionListDTO() != null) {
                    sheet.addCell(new Label(14, i, " 留学 ", cellFormat));
                    sheet.addCell(new Label(15, i,
                            so.getSchoolInstitutionListDTO().getName() + "-"
                                    + so.getSchoolInstitutionListDTO().getSchoolCourseDO().getCourseName(),
                            cellFormat));
                }
                if ("ZX".equalsIgnoreCase(so.getType())) {
                    sheet.addCell(new Label(14, i, " 咨询 ", cellFormat));
                    sheet.addCell(new Label(15, i, so.getService().getCode(), cellFormat));
                }

                if (so.getState().equalsIgnoreCase("PENDING"))
                    sheet.addCell(new Label(16, i, "待提交审核", cellFormat));
                else if (so.getState().equalsIgnoreCase("REVIEW"))
                    sheet.addCell(new Label(16, i, "资料待审核", cellFormat));
                else if (so.getState().equalsIgnoreCase("OREVIEW"))
                    sheet.addCell(new Label(16, i, "资料审核中", cellFormat));
                else if (so.getState().equalsIgnoreCase("FINISH"))
                    sheet.addCell(new Label(16, i, "资料已审核", cellFormat));
                else if (so.getState().equalsIgnoreCase("APPLY"))
                    sheet.addCell(new Label(16, i, "服务申请中", cellFormat));
                else if (so.getState().equalsIgnoreCase("APPLY_FAILED"))
                    sheet.addCell(new Label(16, i, "申请失败", cellFormat));
                else if (so.getState().equalsIgnoreCase("COMPLETE")) {
                    sheet.addCell(new Label(16, i, "申请成功", cellFormat));
                    if (so.getType().equalsIgnoreCase("ZX"))
                        sheet.addCell(new Label(16, i, "订单完成", cellFormat));
                    if (so.getType().equalsIgnoreCase("OVST") && so.isSettle())
                        sheet.addCell(new Label(16, i, "等待财务转账", cellFormat));
                } else if (so.getState().equalsIgnoreCase("RECEIVED"))
                    sheet.addCell(new Label(16, i, "已收款凭证已提交", cellFormat));
                else if (so.getState().equalsIgnoreCase("COMPLETE_FD"))
                    sheet.addCell(new Label(16, i, "财务转账完成", cellFormat));
                else if (so.getState().equalsIgnoreCase("PAID"))
                    sheet.addCell(new Label(16, i, "COE已下", cellFormat));
                else if (so.getState().equalsIgnoreCase("CLOSE"))
                    sheet.addCell(new Label(16, i, "已关闭", cellFormat));
                else if (so.getState().equalsIgnoreCase("WAIT"))
                    sheet.addCell(new Label(16, i, "已提交MARA审核", cellFormat));
                /*
                 * //旧系统状态废除 if (so.getReview() != null) { if
                 * (so.getState().equalsIgnoreCase("PENDING")) sheet.addCell(new Label(15, i,
                 * "待提交审核", cellFormat)); else { if
                 * (so.getReview().getType().equalsIgnoreCase("APPROVAL")) { if
                 * (StringUtil.isEmpty(so.getReview().getOfficialState())) sheet.addCell(new
                 * Label(15, i, "资料待审核", cellFormat)); if
                 * (StringUtil.isNotEmpty(so.getReview().getOfficialState())) { if
                 * (so.getReview().getOfficialState().equalsIgnoreCase("REVIEW")) {
                 * sheet.addCell(new Label(15, i, "资料审核中", cellFormat)); if
                 * (StringUtil.isNotBlank(so.getReview().getMaraState()) &&
                 * so.getReview().getMaraState().equalsIgnoreCase("FINISH")) sheet.addCell(new
                 * Label(15, i, "资料审核完成", cellFormat)); }
                 *
                 * if (so.getReview().getOfficialState().equalsIgnoreCase("WAIT")) { if
                 * (StringUtil.isNotBlank(so.getReview().getMaraState()) &&
                 * so.getReview().getMaraState().equalsIgnoreCase("WAIT")) sheet.addCell(new
                 * Label(15, i, "已提交Mara审核", cellFormat)); } if
                 * (so.getReview().getOfficialState().equalsIgnoreCase("APPLY"))
                 * sheet.addCell(new Label(15, i, "服务申请中", cellFormat)); if
                 * (so.getReview().getOfficialState().equalsIgnoreCase("COMPLETE"))
                 * sheet.addCell(new Label(15, i, "申请成功", cellFormat)); if
                 * (so.getReview().getOfficialState().equalsIgnoreCase("PAID")) {
                 * sheet.addCell(new Label(15, i, "支付成功", cellFormat)); if
                 * (so.getType().equalsIgnoreCase("OVST")) if (so.isSubmitted())
                 * sheet.addCell(new Label(15, i, "支付成功,月奖已申请", cellFormat)); else
                 * sheet.addCell(new Label(15, i, "支付成功,月奖未申请", cellFormat)); } if
                 * (so.getReview().getOfficialState().equalsIgnoreCase("CLOSE"))
                 * sheet.addCell(new Label(15, i, "已关闭", cellFormat)); } } if
                 * (so.getReview().getType().equalsIgnoreCase("REFUSE") &
                 * StringUtil.isNotBlank(so.getReview().getOfficialState())) { if
                 * (so.getReview().getOfficialState().equalsIgnoreCase("PENDING"))
                 * sheet.addCell(new Label(15, i, "待提交审核,文案已驳回", cellFormat)); if
                 * (so.getReview().getOfficialState().equalsIgnoreCase("REVIEW"))
                 * sheet.addCell(new Label(15, i, "资料审核中,已驳回", cellFormat)); if
                 * (so.getReview().getOfficialState().equalsIgnoreCase("CLOSE"))
                 * sheet.addCell(new Label(15, i, "已关闭", cellFormat)); } }
                 *
                 * }
                 */
                sheet.addCell(new Label(17, i, so.getRealPeopleNumber() + "", cellFormat));
                sheet.addCell(new Label(18, i, so.getRemarks(), cellFormat));
                i++;
            }
            wbe.write();
            wbe.close();

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    @RequestMapping(value = "/downExcel", method = RequestMethod.GET)
    @ResponseBody
    public void downExcel(@RequestParam(value = "type") String type,
                          @RequestParam(value = "startOfficialApprovalDate", required = false) String startOfficialApprovalDate,
                          @RequestParam(value = "endOfficialApprovalDate", required = false) String endOfficialApprovalDate,
                          @RequestParam(value = "subject", required = false) String subject, HttpServletRequest request,
                          HttpServletResponse response) {

        try {
            super.setGetHeader(response);
            List<EachRegionNumberDTO> eachRegionNumberDTOS = new ArrayList<>();
            if (StringUtil.isEmpty(subject))// 导出签证/留学各个项目各个地区的个数
                eachRegionNumberDTOS = serviceOrderService.listServiceOrderGroupByForRegion(type,
                        startOfficialApprovalDate, endOfficialApprovalDate);

            if (StringUtil.isNotEmpty(subject)) {
                List<EachSubjectCountDTO> eachSubjectCountDTOS = serviceOrderService
                        .eachSubjectCount(startOfficialApprovalDate, endOfficialApprovalDate);
                response.reset();// 清空输出流
                String tableName = "Subject";
                response.setHeader("Content-disposition",
                        "attachment; filename=" + new String(tableName.getBytes("GB2312"), "8859_1") + ".xls");
                response.setContentType("application/msexcel");

                OutputStream os = response.getOutputStream();
                jxl.Workbook wb;
                InputStream is;
                try {
                    is = this.getClass().getResourceAsStream("/SubjectTemplate.xls");
                } catch (Exception e) {
                    throw new Exception("模版不存在");
                }
                try {
                    wb = Workbook.getWorkbook(is);
                } catch (Exception e) {
                    throw new Exception("模版格式不支持");
                }
                WorkbookSettings settings = new WorkbookSettings();
                settings.setWriteAccess(null);
                jxl.write.WritableWorkbook wbe = Workbook.createWorkbook(os, wb, settings);

                if (wbe == null) {
                    System.out.println("wbe is null !os=" + os + ",wb" + wb);
                } else {
                    System.out.println("wbe not null !os=" + os + ",wb" + wb);
                }
                WritableSheet sheet = wbe.getSheet(0);
                WritableCellFormat cellFormat = new WritableCellFormat();
                int i = 0;
                for (EachSubjectCountDTO each : eachSubjectCountDTOS) {
                    sheet.addCell(new Label(0, i, " 学校名称 ", cellFormat));
                    sheet.addCell(new Label(1, i, each.getName(), cellFormat));
                    sheet.addCell(new Label(0, i + 1, " 申请数量 ", cellFormat));
                    sheet.addCell(new Label(1, i + 1, each.getTotal() + "", cellFormat));
                    List<EachSubjectCountDTO.Subject> subjects = each.getSubject();
                    int n = 2;
                    for (EachSubjectCountDTO.Subject sub : subjects) {
                        sheet.addCell(new Label(n, i, sub.getSubjectName(), cellFormat));
                        sheet.addCell(new Label(n, i + 1, sub.getNumber() + "", cellFormat));
                        n++;
                    }
                    i += 2;
                }
                wbe.write();
                wbe.close();
                return;
            }

            response.reset();// 清空输出流
            String tableName = "Information";
            response.setHeader("Content-disposition",
                    "attachment; filename=" + new String(tableName.getBytes("GB2312"), "8859_1") + ".xls");
            response.setContentType("application/msexcel");

            OutputStream os = response.getOutputStream();
            jxl.Workbook wb;
            InputStream is;
            try {
                is = this.getClass().getResourceAsStream("/data.xls");
            } catch (Exception e) {
                throw new Exception("模版不存在");
            }
            try {
                wb = Workbook.getWorkbook(is);
            } catch (Exception e) {
                throw new Exception("模版格式不支持");
            }
            WorkbookSettings settings = new WorkbookSettings();
            settings.setWriteAccess(null);
            jxl.write.WritableWorkbook wbe = Workbook.createWorkbook(os, wb, settings);

            if (wbe == null) {
                System.out.println("wbe is null !os=" + os + ",wb" + wb);
            } else {
                System.out.println("wbe not null !os=" + os + ",wb" + wb);
            }
            WritableSheet sheet = wbe.getSheet(0);
            WritableCellFormat cellFormat = new WritableCellFormat();
            int i = 0;
            for (EachRegionNumberDTO eo : eachRegionNumberDTOS) {
                if (i == 0 && type.equalsIgnoreCase("VISA")) {
                    sheet.addCell(new Label(1, i, "签证项目", cellFormat));
                    sheet.addCell(new Label(11, i, "CIS", cellFormat));
                    sheet.addCell(new Label(12, i, "青岛", cellFormat));
                    sheet.addCell(new Label(13, i, "北京", cellFormat));
                    sheet.addCell(new Label(14, i, "other", cellFormat));
                    i++;
                }
                if (i == 0 && type.equalsIgnoreCase("OVST")) {
                    sheet.addCell(new Label(1, i, "Institution Trading Name", cellFormat));
                    sheet.addCell(new Label(2, i, "Institution Name", cellFormat));
                    sheet.addCell(new Label(3, i, "总数", cellFormat));
                    sheet.addCell(new Label(4, i, "Sydney", cellFormat));
                    sheet.addCell(new Label(5, i, "Melbourne", cellFormat));
                    sheet.addCell(new Label(6, i, "Brisbane", cellFormat));
                    sheet.addCell(new Label(7, i, "Adelaide", cellFormat));
                    sheet.addCell(new Label(8, i, "Hobart", cellFormat));
                    sheet.addCell(new Label(9, i, "Canberra", cellFormat));
                    sheet.addCell(new Label(10, i, "攻坚部", cellFormat));
                    sheet.addCell(new Label(11, i, "CIS", cellFormat));
                    sheet.addCell(new Label(12, i, "青岛", cellFormat));
                    sheet.addCell(new Label(13, i, "北京", cellFormat));
                    sheet.addCell(new Label(14, i, "other", cellFormat));
                    i++;
                }
                if (i == 0 && type.equalsIgnoreCase("ZX")) {
                    sheet.addCell(new Label(1, i, "咨询服务", cellFormat));
                    i++;
                }
                sheet.addCell(new Label(0, i, i + "", cellFormat));
                sheet.addCell(new Label(1, i, eo.getName(), cellFormat));
                sheet.addCell(new Label(2, i, eo.getInstitutionName(), cellFormat));
                sheet.addCell(new Label(3, i, eo.getTotal() + "", cellFormat));
                sheet.addCell(new Label(4, i, eo.getSydney() + "", cellFormat));
                sheet.addCell(new Label(5, i, eo.getMelbourne() + "", cellFormat));
                sheet.addCell(new Label(6, i, eo.getBrisbane() + "", cellFormat));
                sheet.addCell(new Label(7, i, eo.getAdelaide() + "", cellFormat));
                sheet.addCell(new Label(8, i, eo.getHobart() + "", cellFormat));
                sheet.addCell(new Label(9, i, eo.getCanberra() + "", cellFormat));
                sheet.addCell(new Label(10, i, eo.getCrucial() + "", cellFormat));
                sheet.addCell(new Label(11, i, eo.getCis() + "", cellFormat));
                sheet.addCell(new Label(12, i, eo.getQD() + "", cellFormat));
                sheet.addCell(new Label(13, i, eo.getBJ() + "", cellFormat));
                sheet.addCell(new Label(14, i, eo.getOther() + "", cellFormat));
                i++;
            }
            wbe.write();
            wbe.close();

        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {

        }
    }

    @GetMapping(value = "/down1")
    public void down1(@RequestParam(value = "type", required = false) String type,
                      @RequestParam(value = "startOfficialApprovalDate", required = false) String startOfficialApprovalDate,
                      @RequestParam(value = "endOfficialApprovalDate", required = false) String endOfficialApprovalDate,
                      @RequestParam(value = "isPay", required = false, defaultValue = "false") boolean isPay,
                      HttpServletRequest request, HttpServletResponse response) throws ServiceException {

        OutputStream os = null;
        jxl.Workbook wb = null;
        InputStream is = null;
        // jxl.write.WritableWorkbook wbe = null;

        List<ServiceOrderDTO> serviceOrderDTOS = serviceOrderService.listServiceOrder(type, null, null, null, null,
                null, null, null, null, startOfficialApprovalDate, endOfficialApprovalDate, null, null, null, null,
                null, null, null, null, null, null, 0, 0, false, 0, 9999, null, null, null, isPay, null);
        try {
            super.setGetHeader(response);
            response.reset();
            String tableName = "serviceOrder_VISA";
            response.setHeader("Content-disposition",
                    "attachment; filename=" + new String(tableName.getBytes("GB2312"), "8859_1") + ".xls");
            response.setContentType("application/msexcel");

            os = response.getOutputStream();
            try {
                is = this.getClass().getResourceAsStream("/blank.xls");
            } catch (Exception e) {
                throw new Exception("模版不存在");
            }
            try {
                wb = Workbook.getWorkbook(is);
            } catch (Exception e) {
                throw new Exception("模版格式不支持");
            }
            WorkbookSettings settings = new WorkbookSettings();
            settings.setWriteAccess(null);
            jxl.write.WritableWorkbook wbe = Workbook.createWorkbook(os, wb, settings);

            if (wbe == null) {
                System.out.println("wbe is null !os=" + os + ",wb" + wb);
            } else {
                System.out.println("wbe not null !os=" + os + ",wb" + wb);
            }
            WritableSheet sheet = wbe.getSheet(0);
            WritableCellFormat cellFormat = new WritableCellFormat();
            int i = 0;
            for (ServiceOrderDTO so : serviceOrderDTOS) {
                if (i == 0) {
                    sheet.addCell(new Label(0, i, "提交审核时间", cellFormat));
                    sheet.addCell(new Label(1, i, "服务订单ID", cellFormat));
                    sheet.addCell(new Label(2, i, "服务项目", cellFormat));
                    sheet.addCell(new Label(3, i, "顾问名称", cellFormat));
                    sheet.addCell(new Label(4, i, "顾问地区", cellFormat));
                    sheet.addCell(new Label(5, i, "文案", cellFormat));
                    sheet.addCell(new Label(6, i, "MARA", cellFormat));
                    i++;
                }
                if (so.getParentId() > 0) {
                    ServiceOrderDTO serviceOrderParent = serviceOrderService.getServiceOrderById(so.getParentId());
                    if (serviceOrderParent.isPay())
                        continue;
                }
                sheet.addCell(new Label(0, i, sdf.format(so.getOfficialApprovalDate()), cellFormat));
                sheet.addCell(new Label(1, i, so.getId() + "", cellFormat));
                String str = "";
                if ("VISA".equalsIgnoreCase(so.getType())) {
                    if (so.getService() != null)
                        str = so.getService().getName() + so.getService().getCode();
                    if (so.getServicePackage() != null)
                        str = str + "-"
                                + ServicePackageTypeEnum.getServicePackageTypeComment(so.getServicePackage().getType());
                    if (so.getServiceAssessDO() != null)
                        str = str + "-" + so.getServiceAssessDO().getName();
                }
                if ("ZX".equalsIgnoreCase(so.getType())) {
                    str = "咨询 - " + so.getService().getCode();
                }
                if ("OVST".equalsIgnoreCase(so.getType())) {
                    // str = "留学 - " + so.getSchool().getName();
                    continue;
                }
                sheet.addCell(new Label(2, i, str, cellFormat));
                if (so.getAdviser() != null) {
                    sheet.addCell(new Label(3, i, so.getAdviser().getName(), cellFormat));
                    sheet.addCell(new Label(4, i, so.getAdviser().getRegionName(), cellFormat));
                }
                if (so.getOfficial() != null)
                    sheet.addCell(new Label(5, i, so.getOfficial().getName(), cellFormat));
                if (so.getMara() != null)
                    sheet.addCell(new Label(6, i, so.getMara().getName(), cellFormat));
                i++;
            }

            wbe.write();
            wbe.close();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("os 关闭异常");
                }
            }
            if (wb != null)
                wb.close();
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("is 关闭异常");
                }
            }

        }
    }

    @RequestMapping(value = "/downAdviserOfRegionCaseCount")
    public void downAdviserOfRegionCaseCount(@RequestParam(value = "month", defaultValue = "1") int month,
                                             @RequestParam(value = "regionIds") String regionIds, HttpServletRequest request,
                                             HttpServletResponse response) {
        super.setGetHeader(response);
        List<String> typeList = ListUtil.buildArrayList("VISA", "OVST", "ZX");
        List<String> regionIdList = null;
        if (StringUtils.isNotEmpty(regionIds))
            regionIdList = Arrays.asList(regionIds.split(","));

        if (regionIdList == null)
            return;

        List<String> _regionIdList = new ArrayList<>();

        jxl.Workbook wb = null;
        InputStream is = null;
        ZipOutputStream zipos = null;
        try {
            response.reset();// 清空输出流
            String tableName = "Information";
            response.setHeader("Content-disposition",
                    "attachment; filename=" + new String(tableName.getBytes("GB2312"), "8859_1") + ".zip");
            response.setContentType("application/zip");

            zipos = new ZipOutputStream(response.getOutputStream());

            for (String regionId : regionIdList) {

                _regionIdList.add(regionId);
                List<AdviserServiceCountDTO> adviserServiceCountTs = serviceOrderService
                        .listServiceOrderToAnalysis(typeList, month, _regionIdList);

                ZipEntry zipEntryXtv = new ZipEntry(adviserServiceCountTs.get(0).getRegionName() + ".xls");
                zipos.putNextEntry(zipEntryXtv);
                try {
                    is = this.getClass().getResourceAsStream("/blank.xls");
                } catch (Exception e) {
                    throw new Exception("模版不存在");
                }
                try {
                    wb = Workbook.getWorkbook(is);
                } catch (Exception e) {
                    throw new Exception("模版格式不支持");
                }
                WorkbookSettings settings = new WorkbookSettings();
                settings.setWriteAccess(null);
                jxl.write.WritableWorkbook wbe = Workbook.createWorkbook(zipos, wb, settings);

                if (wbe == null) {
                    System.out.println("wbe is null !os=" + zipos + ",wb" + wb);
                } else {
                    System.out.println("wbe not null !os=" + zipos + ",wb" + wb);
                }
                WritableSheet sheet = wbe.getSheet(0);
                WritableCellFormat cellFormat = new WritableCellFormat();
                int i = 0;
                for (AdviserServiceCountDTO ascd : adviserServiceCountTs) {
                    sheet.addCell(new Label(0, i, ascd.getAdviserName(), cellFormat));
                    sheet.addCell(new Label(0, i + 1, "数量", cellFormat));
                    int j = 1;
                    for (AdviserServiceDetail detail : ascd.getDetails()) {
                        sheet.addCell(new Label(j, i, detail.getServiceName(), cellFormat));
                        sheet.addCell(new Label(j, i + 1, String.valueOf(detail.getCount()), cellFormat));
                        j++;
                    }
                    i += 2;
                }
                wbe.write();
                wbe.close();

                _regionIdList.clear();// 清空list
            }

            zipos.flush();

        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            if (zipos != null) {
                try {
                    zipos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("zipos 关闭异常");
                }
            }
            if (wb != null)
                wb.close();
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("is 关闭异常");
                }
            }
        }
    }

    @RequestMapping(value = "/next_flow", method = RequestMethod.POST)
    @ResponseBody
    public Response<ServiceOrderDTO> nextFlow(@RequestParam(value = "id") int id,
                                              @RequestParam(value = "state") String state,
                                              @RequestParam(value = "subagencyId", required = false) String subagencyId,
                                              @RequestParam(value = "closedReason", required = false) String closedReason,
                                              @RequestParam(value = "refuseReason", required = false) String refuseReason,
                                              @RequestParam(value = "remarks", required = false) String remarks,
                                              @RequestParam(value = "stateMark", required = false) String stateMark, HttpServletRequest request,
                                              HttpServletResponse response) {
        AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
        if (adminUserLoginInfo == null)
            return new Response<ServiceOrderDTO>(1, "请先登录.", null);
        if (id <= 0)
            return new Response<ServiceOrderDTO>(1, "id不正确:" + id, null);
        ServiceOrderDTO serviceOrderDto;
        try {
            serviceOrderDto = serviceOrderService.getServiceOrderById(id);
            if (ObjectUtil.isNull(serviceOrderDto))
                return new Response<ServiceOrderDTO>(1, "服务订单不存在:" + id, null);
            Node node = soNodeFactory.getNode(serviceOrderDto.getState());

            Context context = new Context();
            context.putParameter("serviceOrderId", id);
            context.putParameter("type", serviceOrderDto.getType());
            context.putParameter("state", state);
            context.putParameter("subagencyId", subagencyId);
            context.putParameter("closedReason", closedReason);
            context.putParameter("refuseReason", refuseReason);
            context.putParameter("remarks", remarks);
            context.putParameter("stateMark", stateMark);
            context.putParameter("ap", adminUserLoginInfo.getApList());
            context.putParameter("adminUserId", adminUserLoginInfo.getId());

            LOG.info("Flow API Log : " + context.toString());
            LOG.info("serviceOrderDto : " + serviceOrderDto);
            
            String[] nextNodeNames = node.nextNodeNames();
            if (nextNodeNames != null)
                if (Arrays.asList(nextNodeNames).contains(state))
                    node = soNodeFactory.getNode(state);
                else
                    return new Response<ServiceOrderDTO>(1,
                            StringUtil.merge("状态:", state, "不是合法状态. (", Arrays.toString(nextNodeNames), ")"), null);

            Workflow workflow = new Workflow("Service Order Work Flow", node, soNodeFactory);

            context = workflowStarter.process(workflow, context);

            // 发送消息到群聊PENGDING--->REVIEW
            if ("GW".equalsIgnoreCase(adminUserLoginInfo.getApList()) && !"ZX".equals(serviceOrderDto.getType())
                    && "REVIEW".equalsIgnoreCase(state)) {// 咨询不发群聊消息
                String token = token(request, "corp");
                LOG.info("发送群聊订单:" + serviceOrderDto.getId() + " . ACCESS_TOKEN: " + token);
                if (wxWorkService.sendMsg(serviceOrderDto.getId(), token)) {
                    LOG.info(serviceOrderDto.getId() + " 订单群聊消息发送成功 . ACCESS_TOKEN:" + token);
                }

                if ("VISA".equals(serviceOrderDto.getType())) {// 签证创建群聊
                    if (serviceOrderDto.getParentId() > 0) {
                        if (wxWorkService.ChatDOByServiceOrderId(serviceOrderDto.getParentId()) == null) {
                            // wxWorkService.createChat(id,token);//SIV，按照主订单创建
                        }
                    } else if (wxWorkService.ChatDOByServiceOrderId(id) == null) {
                        // wxWorkService.createChat(id,token);//普通VISA，直接创建
                    }
                }
            }

            if (context.getParameter("response") != null)
                return (Response<ServiceOrderDTO>) context.getParameter("response");
            else {
                if (!"ZX".equals(serviceOrderDto.getType()) || ObjectUtil.isNotNull(serviceOrderDto.getOfficial()))// 咨询服务不用发邮件提醒
                    serviceOrderService.sendRemind(id, state); // 发送提醒邮件
                return new Response<ServiceOrderDTO>(0, id + "", null);
            }
        } catch (ServiceException e) {
            return new Response<ServiceOrderDTO>(1, "异常:" + e.getMessage(), null);
        }
    }

    /**
     * MARA 批量审核 暂且只能 MARA,SUPER
     *
     * @param idList
     * @param state
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/next_flow2", method = RequestMethod.POST)
    @ResponseBody
    public Response<ServiceOrderDTO> next_flow2(@RequestParam(value = "idList") int idList[],
                                                @RequestParam(value = "state") String state,
                                                // @RequestParam(value = "subagencyId", required = false) String subagencyId,
                                                // @RequestParam(value = "closedReason", required = false) String closedReason,
                                                // @RequestParam(value = "refuseReason", required = false) String refuseReason,
                                                // @RequestParam(value = "remarks", required = false) String remarks,
                                                // @RequestParam(value = "stateMark", required = false) String stateMark,
                                                HttpServletRequest request, HttpServletResponse response) {
        AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
        if (adminUserLoginInfo == null || !"MA,SUPER".contains(adminUserLoginInfo.getApList()))
            return new Response<ServiceOrderDTO>(1, "No permission !", null);
        ServiceOrderDTO serviceOrderDto;
        try {
            for (int id : idList) {
                serviceOrderDto = serviceOrderService.getServiceOrderById(id);
                if (ObjectUtil.isNull(serviceOrderDto))
                    return new Response<ServiceOrderDTO>(1, "服务订单不存在:" + id, null);
                Node node = soNodeFactory.getNode(serviceOrderDto.getState());

                Context context = new Context();
                context.putParameter("serviceOrderId", id);
                context.putParameter("type", serviceOrderDto.getType());
                context.putParameter("state", state);
                context.putParameter("adminUserId", adminUserLoginInfo.getId());

                LOG.info("Flow API Log : " + context.toString());

                String[] nextNodeNames = node.nextNodeNames();
                if (nextNodeNames != null)
                    if (Arrays.asList(nextNodeNames).contains(state))
                        node = soNodeFactory.getNode(state);
                    else
                        return new Response<ServiceOrderDTO>(1, StringUtil.merge("佣金：", id, " ,状态:", state,
                                ",不是合法状态. (", Arrays.toString(nextNodeNames), ")"), null);

                Workflow workflow = new Workflow("Service Order Work Flow", node, soNodeFactory);

                context = workflowStarter.process(workflow, context);

                // 发送消息到群聊PENGDING--->REVIEW
                if ("GW".equalsIgnoreCase(adminUserLoginInfo.getApList()) && !"ZX".equals(serviceOrderDto.getType())
                        && "REVIEW".equalsIgnoreCase(state)) {// 咨询不发群聊消息
                    String token = token(request, "corp");
                    LOG.info("发送群聊订单:" + serviceOrderDto.getId() + " . ACCESS_TOKEN: " + token);
                    if (wxWorkService.sendMsg(serviceOrderDto.getId(), token)) {
                        LOG.info(serviceOrderDto.getId() + " 订单群聊消息发送成功 . ACCESS_TOKEN:" + token);
                    }
                }

                if (context.getParameter("response") != null)
                    return (Response<ServiceOrderDTO>) context.getParameter("response");
                else {
                    if (!"ZX".equals(serviceOrderDto.getType()))// 咨询服务不用发邮件提醒
                        serviceOrderService.sendRemind(id, state); // 发送提醒邮件
                }
            }
        } catch (ServiceException e) {
            return new Response<ServiceOrderDTO>(1, "异常:" + e.getMessage(), null);
        }
        return new Response<ServiceOrderDTO>(0, "success", null);

    }

    @RequestMapping(value = "/updateStateByList", method = RequestMethod.POST)
    @ResponseBody
    public Response<ServiceOrderDTO> updateStateByList(@RequestParam(value = "idList") int idList[],
                                                       @RequestParam(value = "state") String state,
                                                       // @RequestParam(value = "subagencyId", required = false) String subagencyId,
                                                       // @RequestParam(value = "closedReason", required = false) String closedReason,
                                                       // @RequestParam(value = "refuseReason", required = false) String refuseReason,
                                                       // @RequestParam(value = "remarks", required = false) String remarks,
                                                       // @RequestParam(value = "stateMark", required = false) String stateMark,
                                                       HttpServletRequest request, HttpServletResponse response) {
        AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
        if (adminUserLoginInfo == null || !"WA,SUPER".contains(adminUserLoginInfo.getApList()))
            return new Response<ServiceOrderDTO>(1, "No permission !", null);
        ServiceOrderDTO serviceOrderDto;
        try {
            if (state.equals("APPLY")) {
                List<Integer> list = new ArrayList<>();
                List<VisaDTO> visaList = new ArrayList<>();
                for (int id : idList) {
                    ServiceOrderDTO serviceOrder = serviceOrderService.getServiceOrderById(id);
                    if (serviceOrder.getParentId() != 0 || serviceOrder.getApplicantParentId() != 0)
                        visaList = visaService.listVisaByServiceOrderId((serviceOrder.getParentId() == 0 ? serviceOrder.getApplicantParentId() : serviceOrder.getParentId()));
                    else
                        visaList = visaService.listVisaByServiceOrderId(serviceOrder.getId());
                    for (VisaDTO visaDTO : visaList) {
                        if (visaDTO.getState().equals("PENDING")) {
                            list.add(id);
                        }
                    }
                }
                if (list.size() > 0)
                    return new Response<ServiceOrderDTO>(1, "以下订单尾款未支付请联系顾问后再继续操作:" + list, null);
            }
            for (int id : idList) {
                serviceOrderDto = serviceOrderService.getServiceOrderById(id);
                if (ObjectUtil.isNull(serviceOrderDto))
                    return new Response<ServiceOrderDTO>(1, "服务订单不存在:" + id, null);
                Node node = soNodeFactory.getNode(serviceOrderDto.getState());
                Context context = new Context();
                context.putParameter("serviceOrderId", id);
                context.putParameter("type", serviceOrderDto.getType());
                context.putParameter("state", state);
                context.putParameter("adminUserId", adminUserLoginInfo.getId());
                context.putParameter("ap", adminUserLoginInfo.getApList());

                LOG.info("Flow API Log : " + context.toString());

                String[] nextNodeNames = node.nextNodeNames();
                if (nextNodeNames != null)
                    if (Arrays.asList(nextNodeNames).contains(state))
                        node = soNodeFactory.getNode(state);
                    else
                        return new Response<ServiceOrderDTO>(1, StringUtil.merge(id, " ,状态:", state,
                                ",不是合法状态. (", Arrays.toString(nextNodeNames), ")"), null);

                Workflow workflow = new Workflow("Service Order Work Flow", node, soNodeFactory);
                context = workflowStarter.process(workflow, context);
                visaOfficialController.add(String.valueOf(serviceOrderDto.getUserId()), null, null, String.valueOf(serviceOrderDto.getReceiveTypeId()), String.valueOf(serviceOrderDto.getReceiveDate().getTime()),
                        String.valueOf(serviceOrderDto.getServiceId()), id, serviceOrderDto.getInstallment(), serviceOrderDto.getPaymentVoucherImageUrl1(), serviceOrderDto.getPaymentVoucherImageUrl2(),
                        serviceOrderDto.getPaymentVoucherImageUrl3(), serviceOrderDto.getPaymentVoucherImageUrl4(), serviceOrderDto.getPaymentVoucherImageUrl5(), serviceOrderDto.getVisaVoucherImageUrl(),
                        String.valueOf(serviceOrderDto.getReceivable()), String.valueOf(serviceOrderDto.getReceived()), String.valueOf(serviceOrderDto.getPerAmount()), String.valueOf(serviceOrderDto.getAmount()), serviceOrderDto.getCurrency(),
                        String.valueOf(serviceOrderDto.getExchangeRate()), null, String.valueOf(serviceOrderDto.getAdviserId()), String.valueOf(serviceOrderDto.getMaraId()), String.valueOf(serviceOrderDto.getOfficialId()), serviceOrderDto.getRemarks(),
                        serviceOrderDto.getVerifyCode(), request, response);

            }
        } catch (ServiceException e) {
            return new Response<ServiceOrderDTO>(1, "异常:" + e.getMessage(), null);
        }
        return new Response<ServiceOrderDTO>(0, "success", null);

    }

    @RequestMapping(value = "/updateForAD", method = RequestMethod.POST)
    @ResponseBody
    public Response<Integer> updateForAD(@RequestBody ServiceOrderDTO serviceOrderDto, HttpServletRequest request,
                                         HttpServletResponse response) {
        try {
            super.setPostHeader(response);
            AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
            if (adminUserLoginInfo == null || (!"SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
                    && !"AD".equalsIgnoreCase(adminUserLoginInfo.getApList())))
                return new Response<Integer>(1, "仅限管理员修改.", 0);
            ServiceOrderDTO orderDto = serviceOrderService.getServiceOrderById(serviceOrderDto.getId());
            if (orderDto.getState().equals("REVIEW") || orderDto.getState().equals("OREVIEW")
                    || orderDto.getState().equals("PENDING")) {
                if (serviceOrderDto.getServiceId() > 0) { // 修改服务项目
                    if (orderDto.getApplicantParentId() == 0) { // 修改子服务订单
                        List<ServiceOrderDTO> orderList = serviceOrderService
                                .listServiceOrderByApplicantParentId(serviceOrderDto.getId());
                        if (!ListUtil.isEmpty(orderList)) {
                            for (ServiceOrderDTO _cso : orderList) {
                                if (!_cso.getState().equals("REVIEW") && !_cso.getState().equals("OREVIEW")
                                        && !_cso.getState().equals("PENDING")) {
                                    return new Response<Integer>(1,
                                            StringUtil.merge("修改失败,子订单不存在资料待审核或资料审核中状态.(ID:", _cso.getId(), ")"), null);
                                }
                            }
                            orderList.forEach(cso -> {
                                try {
                                    serviceOrderService.updateServiceOrderService(cso.getId(),
                                            serviceOrderDto.getServiceId());
                                    updateVisaServiceForAD(cso, serviceOrderDto.getServiceId());
                                } catch (ServiceException e) {
                                    LOG.error(StringUtil.merge("子服务订单(", cso.getId(), ")服务项目修改失败:", e.getMessage()));
                                }
                            });
                        }
                    }
                    serviceOrderService.updateServiceOrderService(serviceOrderDto.getId(),
                            serviceOrderDto.getServiceId());
                    updateVisaServiceForAD(orderDto, serviceOrderDto.getServiceId()); // 修改佣金订单
                    return new Response<>(0, "修改成功", null);
                } else if (serviceOrderDto.getSubagencyId() > 0) { // 修改Subagency
                    orderDto.setSubagencyId(serviceOrderDto.getSubagencyId());
                    if (serviceOrderService.updateServiceOrder(orderDto) > 0)
                        return new Response<>(0, "修改成功", null);
                    else
                        return new Response<Integer>(1, "修改失败.", null);
                } else
                    return new Response<Integer>(1, "修改失败,请检查参数.", null);
            } else
                return new Response<Integer>(1, "只允许修改未申请月奖订单.", null);
        } catch (ServiceException e) {
            return new Response<Integer>(1, "异常:" + e.getMessage(), null);
        }
    }

    @RequestMapping(value = "/exportList", method = RequestMethod.GET)
    @ResponseBody
    public void exportList(@RequestParam(value = "id", required = false) Integer id,
                                                @RequestParam(value = "type", required = false) String type,
                                                @RequestParam(value = "state", required = false) String state,
                                                @RequestParam(value = "auditingState", required = false) String auditingState,
                                                @RequestParam(value = "reviewState", required = false) String reviewState,
                                                @RequestParam(value = "urgentState", required = false) String urgentState,
                                                @RequestParam(value = "startMaraApprovalDate", required = false) String startMaraApprovalDate,
                                                @RequestParam(value = "endMaraApprovalDate", required = false) String endMaraApprovalDate,
                                                @RequestParam(value = "startOfficialApprovalDate", required = false) String startOfficialApprovalDate,
                                                @RequestParam(value = "endOfficialApprovalDate", required = false) String endOfficialApprovalDate,
                                                @RequestParam(value = "startReadcommittedDate", required = false) String startReadcommittedDate,
                                                @RequestParam(value = "endReadcommittedDate", required = false) String endReadcommittedDate,
                                                @RequestParam(value = "regionId", required = false) Integer regionId,
                                                @RequestParam(value = "userId", required = false) Integer userId,
                                                @RequestParam(value = "userName", required = false) String userName,
                                                @RequestParam(value = "applicantName", required = false) String applicantName,
                                                @RequestParam(value = "maraId", required = false) Integer maraId,
                                                @RequestParam(value = "adviserId", required = false) Integer adviserId,
                                                @RequestParam(value = "officialId", required = false) Integer officialId,
                                                @RequestParam(value = "officialTagId", required = false) Integer officialTagId,
                                                @RequestParam(value = "isNotApproved", required = false) Boolean isNotApproved,
                                                @RequestParam(value = "serviceId", required = false) Integer serviceId,
                                                @RequestParam(value = "schoolId", required = false) Integer schoolId,
                                                @RequestParam(value = "isSettle", required = false) Boolean isSettle,
                                                @RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
                                                @RequestParam(value = "sorter", required = false) String sorter, HttpServletRequest request,
                                                HttpServletResponse response) {
        try {
            super.setPostHeader(response);
            // 设置响应头
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            // 设置防止中文名乱码
            Date date = new Date();
            SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd"); // 创建一个SimpleDateFormat类对象，指定日期格式为"yyyy/MM/dd HH:mm:ss"
            String formattedDateTmp = sd.format(date); // 将Date类对象转换为指定格式的字符串
            String filename = URLEncoder.encode("ServiceOrderExport-" + formattedDateTmp, "UTF-8");
            // 文件下载方式(附件下载还是在当前浏览器打开)
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-disposition", "attachment;filename=" +
                    filename + ".xlsx");
            // 查询要导出的数据
            ListResponse<List<ServiceOrderDTO>> listListResponse = this.listServiceOrder(id, type, state, auditingState, reviewState, urgentState, startMaraApprovalDate, endMaraApprovalDate,
                    startOfficialApprovalDate, endOfficialApprovalDate, startReadcommittedDate, endReadcommittedDate, regionId, userId,
                    userName, applicantName, maraId, adviserId, officialId, officialTagId, isNotApproved, serviceId, schoolId, isSettle,
                    pageNum, pageSize, sorter, request, response);
            if (listListResponse.getMessage().equals("No permission !")) {
                throw new RuntimeException("当前用户未登录");
            }
            List<ServiceOrderDTO> data = listListResponse.getData();
            // 新建表格数据容器
            List<ServiceOrderExportDTO> serviceOrderExportDTOS = new ArrayList<>();
            data.forEach(e->{
                ServiceOrderExportDTO serviceOrderExportDTO = new ServiceOrderExportDTO();
                serviceOrderExportDTO.setId(e.getId());
                if (ObjectUtil.isNotNull(e.getFinishDate())) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); // 创建一个SimpleDateFormat类对象，指定日期格式为"yyyy/MM/dd HH:mm:ss"
                    String formattedDate = sdf.format(e.getFinishDate()); // 将Date类对象转换为指定格式的字符串
                    serviceOrderExportDTO.setFinishDate(formattedDate);
                }
                if (ObjectUtil.isNotNull(e.getOfficialApprovalDate())) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); // 创建一个SimpleDateFormat类对象，指定日期格式为"yyyy/MM/dd HH:mm:ss"
                    String formattedDate = sdf.format(e.getOfficialApprovalDate()); // 将Date类对象转换为指定格式的字符串
                    serviceOrderExportDTO.setOfficialApprovalDate(formattedDate);
                }
                if (ObjectUtil.isNotNull(e.getReadcommittedDate())) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); // 创建一个SimpleDateFormat类对象，指定日期格式为"yyyy/MM/dd HH:mm:ss"
                    String formattedDate = sdf.format(e.getReadcommittedDate()); // 将Date类对象转换为指定格式的字符串
                    serviceOrderExportDTO.setReadcommittedDate(formattedDate);
                }
                if (ObjectUtil.isNotNull(e.getUser())) {
                    if (StringUtils.isNotBlank(e.getUser().getName())) {
                        serviceOrderExportDTO.setUserName(e.getUser().getName());
                    }
                }
                if (ObjectUtil.isNotNull(e.getApplicant())) {
                    if (StringUtils.isNotBlank(e.getApplicant().getSurname())) {
                        serviceOrderExportDTO.setApplicantName(e.getApplicant().getSurname());
                    }
                }
                if (ObjectUtil.isNotNull(e.getApplicant())) {
                    if (ObjectUtil.isNotNull(e.getApplicant().getBirthday())) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); // 创建一个SimpleDateFormat类对象，指定日期格式为"yyyy/MM/dd HH:mm:ss"
                        String formattedDate = sdf.format(e.getApplicant().getBirthday()); // 将Date类对象转换为指定格式的字符串
                        serviceOrderExportDTO.setApplicantBirthday(formattedDate);
                    }
                }
                if (ObjectUtil.isNotNull(e.getUser())) {
                    if (StringUtils.isNotBlank(e.getUser().getPhone())) {
                        serviceOrderExportDTO.setPhone(e.getUser().getPhone());
                    }
                }
                if (ObjectUtil.isNotNull(e.getAdviser())) {
                    if (StringUtils.isNotBlank(e.getAdviser().getName())) {
                        serviceOrderExportDTO.setAdviserName(e.getAdviser().getName());
                    }
                }
                if (ObjectUtil.isNotNull(e.getOfficial())) {
                    if (StringUtils.isNotBlank(e.getOfficial().getName())) {
                        serviceOrderExportDTO.setOfficialName(e.getOfficial().getName());
                    }
                }
                if (ObjectUtil.isNotNull(e.getMara())) {
                    if (StringUtils.isNotBlank(e.getMara().getName())) {
                        serviceOrderExportDTO.setMaraName(e.getMara().getName());
                    }
                }
                if (ObjectUtil.isNotNull(e.getService())) {
                    if (StringUtils.isNotBlank(e.getService().getName()) && StringUtils.isNotBlank(e.getService().getCode())) {
                        serviceOrderExportDTO.setServiceCodeAndName(e.getService().getName() + "-" + e.getService().getCode());
                    }
                }
                if (StringUtils.isNotBlank(e.getState())) {
                    String s = convertOrderStatus(e.getState());
                    if (s.equals("无状态")) {
                        System.out.println("无状态的数据为-------------" + e.getId());
                    }
                    serviceOrderExportDTO.setState(s);
                }
                serviceOrderExportDTOS.add(serviceOrderExportDTO);
            });
            // 写入数据到excel
            EasyExcel.write(response.getOutputStream(), ServiceOrderExportDTO.class)
                    .sheet("用户信息")
                    .doWrite(serviceOrderExportDTOS);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 转换订单状态
    private String convertOrderStatus(String state) {
        String stateName = null;
        switch (state) {
            case "PENDING":
                stateName = "待提交审核";
                break;
            case "REVIEW":
                stateName = "资料待审核";
                break;
            case "OREVIEW":
                stateName = "资料审核中";
                break;
            case "APPLY":
                stateName = "服务申请中";
                break;
            case "COMPLETE":
                stateName = "服务申请完成";
                break;
            case "FINISH":
                stateName = "完成-支付成功";
                break;
            case "CLOSE":
                stateName = "关闭";
                break;
            case "RECEIVED":
                stateName = "已收款凭证已提交";
                break;
            case "PAID":
                stateName = "COE已下";
                break;
            case "WAIT":
                stateName = "已提交Mara审核";
                break;
            case "APPLY_FAILED":
                stateName = "学校拒绝，申请失败";
                break;
            case "COMPLETE_FD":
                stateName = "财务转账完成";
                break;
            default:
                stateName = "无状态";
        }
        return stateName;
    }


    private void updateVisaServiceForAD(ServiceOrderDTO serviceOrderDto, int serviceId) throws ServiceException {
        if ("VISA".equalsIgnoreCase(serviceOrderDto.getType())) {
            List<VisaDTO> visaList = visaService.listVisaByServiceOrderId(serviceOrderDto.getId());
            visaList.forEach(visaDto -> {
                if (visaDto.getState().equals("REVIEW") || visaDto.getState().equals("PENDING")) {
                    visaDto.setServiceId(serviceId);
                    try {
                        visaService.updateVisa(visaDto);
                    } catch (ServiceException e) {
                        LOG.error(StringUtil.merge("签证订单(", visaDto.getId(), ")服务项目修改失败:", e.getMessage()));
                    }
                } else
                    LOG.error(StringUtil.merge("签证订单(", visaDto.getId(), ")服务项目修改失败:只允许修改未审核订单,而当前订单状态为",
                            visaDto.getState()));
            });
        }
    }

    private String getTypeStrOfServicePackageDTO(String type) {
        String servicepakageName;
        switch (type) {
            case "CA":
                servicepakageName = "职业评估";
                break;
            case "EOI":
                servicepakageName = "EOI";
                break;
            case "SA":
                servicepakageName = "学校申请";
                break;
            case "VA":
                servicepakageName = "签证申请";
                break;
            case "ZD":
                servicepakageName = "州担";
                break;
            case "MAT":
                servicepakageName = "Matrix";
                break;
            case "SBO":
                servicepakageName = "SBO";
                break;
            case "TM":
                servicepakageName = "提名";
                break;
            case "DB":
                servicepakageName = "担保";
                break;
            case "ROI":
                servicepakageName = "ROI";
                break;
            default:
                servicepakageName = null;
        }
        return servicepakageName;
    }
}
