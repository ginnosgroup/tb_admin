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
import lombok.extern.slf4j.Slf4j;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zhinanzhen.b.dao.OfficialDAO;
import org.zhinanzhen.b.dao.pojo.SetupExcelDO;
import org.zhinanzhen.b.service.*;
import org.zhinanzhen.b.service.pojo.*;
import org.zhinanzhen.b.service.pojo.ant.Sorter;
import org.zhinanzhen.tb.controller.ListResponse;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.AdviserService;
import org.zhinanzhen.tb.service.RegionService;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.UserService;
import org.zhinanzhen.tb.service.pojo.AdviserDTO;
import org.zhinanzhen.tb.service.pojo.RegionDTO;
import org.zhinanzhen.tb.service.pojo.UserDTO;
import org.zhinanzhen.tb.utils.CommonUtils;
import org.zhinanzhen.tb.utils.SendEmailUtil;
import org.zhinanzhen.tb.utils.WXWorkAPI;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/commissionOrder")
@Slf4j
public class CommissionOrderController extends BaseCommissionOrderController {
	
	private static final Logger LOG = LoggerFactory.getLogger(CommissionOrderController.class);
	
	protected Mapper mapper = new DozerBeanMapper();

	@Resource
	CommissionOrderService commissionOrderService;

	@Resource
	SubagencyService subagencyService;

	@Resource
	SchoolService schoolService;

	@Resource
	private OfficialDAO officialDao;

	@Resource
	UserService userService;

	@Resource
	ServiceOrderService serviceOrderService;

	@Resource
	ApplicantService applicantService;

	@Resource
	RegionService regionService;

	@Resource
	SchoolInstitutionService schoolInstitutionService;

	@Resource
	MailRemindService mailRemindService;
	
	@Resource
	ServicePackagePriceService servicePackagePriceService;
	
	@Resource
	private AdviserService adviserService;
	
	@Resource
	private KjService kjService;

	@Resource
	private SchoolCourseService schoolCourseService;

	@Resource
	WXWorkService wxWorkService;

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@RequestMapping(value = "/upload_img", method = RequestMethod.POST)
	@ResponseBody
	public Response<String> uploadImage(@RequestParam MultipartFile file, HttpServletRequest request,
			HttpServletResponse response) throws IllegalStateException, IOException {
		super.setPostHeader(response);
		return super.upload2(file, request.getSession(), "/uploads/payment_voucher_image_url_c/");
	}

	@RequestMapping(value = "/delete_visa_upload_img", method = RequestMethod.POST)
	@ResponseBody
	public Response<String> deleteVisaVoucherImg(@RequestParam(value = "visaUrl") String url, HttpServletRequest request,
												 HttpServletResponse response) throws IllegalStateException, IOException {
		super.setPostHeader(response);
		return super.deleteFile(url);
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<List<CommissionOrderDTO>> add(@RequestParam(value = "serviceOrderId") Integer serviceOrderId,
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "isSettle") Boolean isSettle,
			@RequestParam(value = "isDepositUser") Boolean isDepositUser,
			@RequestParam(value = "schoolId", required = false) Integer schoolId,
			@RequestParam(value = "courseId", required = false) Integer courseId,
			@RequestParam(value = "schoolInstitutionLocationId", required = false)Integer schoolInstitutionLocationId,
			@RequestParam(value = "studentCode") String studentCode,
			@RequestParam(value = "userId") Integer userId,
			@RequestParam(value = "applicantBirthday", required = false) String applicantBirthday,
			@RequestParam(value = "adviserId") Integer adviserId,
			@RequestParam(value = "officialId") Integer officialId,
			@RequestParam(value = "isStudying") Boolean isStudying,
			@RequestParam(value = "installment") Integer installment,
			@RequestParam(value = "installmentDueDate1") String installmentDueDate1,
			@RequestParam(value = "installmentDueDate2", required = false) String installmentDueDate2,
			@RequestParam(value = "installmentDueDate3", required = false) String installmentDueDate3,
			@RequestParam(value = "installmentDueDate4", required = false) String installmentDueDate4,
			@RequestParam(value = "installmentDueDate5", required = false) String installmentDueDate5,
			@RequestParam(value = "installmentDueDate6", required = false) String installmentDueDate6,
			@RequestParam(value = "installmentDueDate7", required = false) String installmentDueDate7,
			@RequestParam(value = "installmentDueDate8", required = false) String installmentDueDate8,
			@RequestParam(value = "installmentDueDate9", required = false) String installmentDueDate9,
			@RequestParam(value = "installmentDueDate10", required = false) String installmentDueDate10,
			@RequestParam(value = "installmentDueDate11", required = false) String installmentDueDate11,
			@RequestParam(value = "installmentDueDate12", required = false) String installmentDueDate12,
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
			@RequestParam(value = "dob") String dob, @RequestParam(value = "startDate") String startDate,
			@RequestParam(value = "endDate") String endDate, @RequestParam(value = "tuitionFee") String tuitionFee,
			@RequestParam(value = "perTermTuitionFee") String perTermTuitionFee,
			@RequestParam(value = "receiveTypeId") Integer receiveTypeId,
			@RequestParam(value = "receiveDate") String receiveDate,
			@RequestParam(value = "perAmount") String perAmount, @RequestParam(value = "amount") String amount,
			@RequestParam(value = "currency", required = false) String currency,
			@RequestParam(value = "exchangeRate", required = false) String exchangeRate,
			@RequestParam(value = "bonusDate", required = false) String bonusDate,
			@RequestParam(value = "zyDate", required = false) String zyDate,
			@RequestParam(value = "remarks", required = false) String remarks,
			@RequestParam(value = "visaStatus", required = false) String visaStatus,
			@RequestParam(value = "visaStatusSub", required = false) String visaStatusSub,
			@RequestParam(value = "visaCertificate", required = false) String visaCertificate,
			@RequestParam(value = "verifyCode", required = false) String verifyCode, HttpServletRequest request,
			HttpServletResponse response) {

		try {
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo == null || (!"SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
					&& !"GW".equalsIgnoreCase(adminUserLoginInfo.getApList())))
				if (!"WA".equalsIgnoreCase(adminUserLoginInfo.getApList()) || !isSettle )
					return new Response<List<CommissionOrderDTO>>(1, "仅限顾问和超级管理员能创建佣金订单.", null);
			List<CommissionOrderDTO> commissionOrderDtoList = new ArrayList<>();
			ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(serviceOrderId);
			if (serviceOrderDto == null)
				return new Response<List<CommissionOrderDTO>>(1, "服务订单(ID:" + serviceOrderId + ")不存在!", null);

			if (serviceOrderDto.getSubagencyId() <= 0)
				return new Response<List<CommissionOrderDTO>>(1,
						"SubagencyId(" + serviceOrderDto.getSubagencyId() + ")不存在!", null);
			CommissionOrderDTO commissionOrderDto = new CommissionOrderDTO();
			commissionOrderDto.setCode(UUID.randomUUID().toString());
			commissionOrderDto.setServiceOrderId(serviceOrderId);
			if (StringUtil.isNotEmpty(state))
				commissionOrderDto.setState(state);
			// else
			// commissionOrderDto.setState(ReviewKjStateEnum.PENDING.toString());
			if (isSettle)
				commissionOrderDto.setCommissionState(CommissionStateEnum.DJY.toString());
			else
				commissionOrderDto.setCommissionState(CommissionStateEnum.DZY.toString());
			commissionOrderDto.setSettle(isSettle);
			commissionOrderDto.setDepositUser(isDepositUser);
			commissionOrderDto.setSchoolId(schoolId == null ? 0 : schoolId);
			commissionOrderDto.setCourseId(courseId == null ? 0 : courseId);
			commissionOrderDto.setSchoolInstitutionLocationId(schoolInstitutionLocationId == null ? 0 : schoolInstitutionLocationId);
			commissionOrderDto.setStudentCode(studentCode);
			commissionOrderDto.setUserId(userId);
			commissionOrderDto.setAdviserId(adviserId);
			commissionOrderDto.setOfficialId(officialId);
			commissionOrderDto.setStudying(isStudying);
			commissionOrderDto.setInstallment(installment);
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl1))
				commissionOrderDto.setPaymentVoucherImageUrl1(paymentVoucherImageUrl1);
			else
				commissionOrderDto.setPaymentVoucherImageUrl1(serviceOrderDto.getPaymentVoucherImageUrl1()); // 许十一(2021-06-17)说非提前不需要引用服务订单的支付凭证!
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl2))
				commissionOrderDto.setPaymentVoucherImageUrl2(paymentVoucherImageUrl2);
			else
				commissionOrderDto.setPaymentVoucherImageUrl2(serviceOrderDto.getPaymentVoucherImageUrl2());
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl3))
				commissionOrderDto.setPaymentVoucherImageUrl3(paymentVoucherImageUrl3);
			else
				commissionOrderDto.setPaymentVoucherImageUrl3(serviceOrderDto.getPaymentVoucherImageUrl3());
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl4))
				commissionOrderDto.setPaymentVoucherImageUrl4(paymentVoucherImageUrl4);
			else
				commissionOrderDto.setPaymentVoucherImageUrl4(serviceOrderDto.getPaymentVoucherImageUrl4());
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl5))
				commissionOrderDto.setPaymentVoucherImageUrl5(paymentVoucherImageUrl5);
			else
				commissionOrderDto.setPaymentVoucherImageUrl5(serviceOrderDto.getPaymentVoucherImageUrl5());
			commissionOrderDto.setDob(new Date(Long.parseLong(dob)));
			commissionOrderDto.setStartDate(new Date(Long.parseLong(startDate)));
			commissionOrderDto.setEndDate(new Date(Long.parseLong(endDate)));
			commissionOrderDto.setTuitionFee(Double.parseDouble(tuitionFee));
			commissionOrderDto.setPerTermTuitionFee(Double.parseDouble(perTermTuitionFee));
			commissionOrderDto.setReceiveTypeId(receiveTypeId);
			commissionOrderDto.setReceiveDate(new Date(Long.parseLong(receiveDate)));
			commissionOrderDto.setPerAmount(Double.parseDouble(perAmount));
			commissionOrderDto.setAmount(Double.parseDouble(amount));
			if (commissionOrderDto.getPerAmount() < commissionOrderDto.getAmount())
				return new Response<List<CommissionOrderDTO>>(1, "本次应收款(" + commissionOrderDto.getPerAmount()
						+ ")不能小于本次已收款(" + commissionOrderDto.getAmount() + ")!", null);
			if (StringUtil.isNotEmpty(currency))
				commissionOrderDto.setCurrency(currency);
			if (StringUtil.isNotEmpty(exchangeRate))
				commissionOrderDto.setExchangeRate(Double.parseDouble(exchangeRate));
			commissionOrderDto.setDiscount(commissionOrderDto.getPerAmount() - commissionOrderDto.getAmount());
			if (StringUtil.isNotEmpty(bonusDate))
				commissionOrderDto.setBonusDate(new Date(Long.parseLong(bonusDate)));
			if (StringUtil.isNotEmpty(zyDate))
				commissionOrderDto.setZyDate(new Date(Long.parseLong(zyDate)));
			if (StringUtil.isNotEmpty(remarks))
				commissionOrderDto.setRemarks(remarks);
			if (StringUtil.isNotEmpty(visaCertificate))
				commissionOrderDto.setVisaCertificate(visaCertificate);
			if (StringUtil.isNotEmpty(visaStatus)) {
				commissionOrderDto.setVisaStatus(visaStatus);
				if ("Off shore".equals(visaStatus)) {
					if (courseId > 0) {
						SchoolCourseDTO schoolCourseDTO = schoolCourseService.schoolCourseById(courseId);
						SchoolInstitutionDTO schoolInstitutionById = schoolInstitutionService.getSchoolInstitutionById(schoolCourseDTO.getProviderId());
						if (ObjectUtil.isNotNull(schoolInstitutionById)) {
							if (!schoolInstitutionById.isCooperative() && StringUtil.isEmpty(visaCertificate)) {
								return new Response<List<CommissionOrderDTO>>(1, "当前选择为非合作院校，请上传签证信息", commissionOrderDtoList);
							}
						}
					}
				}
			}
			if (StringUtil.isNotEmpty(visaStatusSub)) {
				commissionOrderDto.setVisaStatusSub(visaStatusSub);
			}


			//if (serviceOrderDto.isSettle() == true && (
			//		StringUtil.isNotEmpty(invoiceVoucherImageUrl1) || StringUtil.isNotEmpty(invoiceVoucherImageUrl2)
			//		|| StringUtil.isNotEmpty(invoiceVoucherImageUrl3) || StringUtil.isNotEmpty(invoiceVoucherImageUrl4)
			//		|| StringUtil.isNotEmpty(invoiceVoucherImageUrl5)) ){
				/*
				杜大哥（2021-10-18）说：【提前扣拥】在上传【invoice凭证】之后，申请月奖直接将服务订单状态改成【RECEIVED】(已收款凭证已提交)
				 */
			//	serviceOrderDto.setState("RECEIVED");
			//}
			//if (StringUtil.isNotEmpty(invoiceVoucherImageUrl1))
			//	serviceOrderDto.setInvoiceVoucherImageUrl1(invoiceVoucherImageUrl1);
			//if (StringUtil.isNotEmpty(invoiceVoucherImageUrl2))
			//	serviceOrderDto.setInvoiceVoucherImageUrl2(invoiceVoucherImageUrl2);
			//if (StringUtil.isNotEmpty(invoiceVoucherImageUrl3))
			//	serviceOrderDto.setInvoiceVoucherImageUrl3(invoiceVoucherImageUrl3);
			//if (StringUtil.isNotEmpty(invoiceVoucherImageUrl4))
			//	serviceOrderDto.setInvoiceVoucherImageUrl4(invoiceVoucherImageUrl4);
			//if (StringUtil.isNotEmpty(invoiceVoucherImageUrl5))
			//	serviceOrderDto.setInvoiceVoucherImageUrl5(invoiceVoucherImageUrl5);



			// SubagencyDTO subagencyDto =
			// subagencyService.getSubagencyById(serviceOrderDto.getSubagencyId());
			// if (subagencyDto == null)
			// return new Response<List<CommissionOrderDTO>>(1,
			// "Subagency(" + serviceOrderDto.getSubagencyId() + ")不存在!", null);
			// // 佣金
			// commissionOrderDto.setCommission(commissionOrderDto.getAmount());
			// // 预收业绩
			// Double expectAmount = commissionOrderDto.getAmount() *
			// subagencyDto.getCommissionRate() * 1.1;
			// commissionOrderDto.setExpectAmount(expectAmount);
			// // GST
			// commissionOrderDto
			// .setGst(new BigDecimal(expectAmount / 11).setScale(2,
			// BigDecimal.ROUND_HALF_UP).doubleValue());
			// // Deduct GST
			// commissionOrderDto.setDeductGst(new BigDecimal(expectAmount -
			// commissionOrderDto.getGst())
			// .setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
			// // Bonus
			// commissionOrderDto.setBonus(new
			// BigDecimal(commissionOrderDto.getDeductGst() * 0.1)
			// .setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());

			String msg = "";
			for (int installmentNum = 1; installmentNum <= installment; installmentNum++) {
				commissionOrderDto.setInstallmentNum(installmentNum);
				if (installmentNum == 1 && installmentDueDate1 != null) {
					commissionOrderDto.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate1)));
					commissionOrderDto.setState(ReviewKjStateEnum.REVIEW.toString()); // 第一笔单子直接进入财务审核状态
					if (StringUtil.isNotEmpty(verifyCode))
						commissionOrderDto.setVerifyCode(verifyCode.replace("$", "").replace("#", "").replace(" ", ""));
					commissionOrderDto.setKjApprovalDate(new Date());
					//  提前扣拥会将创建的invoice发票凭证的invoiceNo暂存到佣金临时表中，创建佣金的时候写入第一笔单子
					if (isSettle){
						CommissionOrderTempDTO comTemp = commissionOrderService.getCommissionOrderTempByServiceOrderId(serviceOrderId);
						if (comTemp != null){
							commissionOrderDto.setInvoiceNumber(comTemp.getInvoiceNumber());
						}
					}
				} else {
					if (installmentNum == 2 && installmentDueDate2 != null) {
						commissionOrderDto.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate2)));
					} else if (installmentNum == 3 && installmentDueDate3 != null) {
						commissionOrderDto.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate3)));
					} else if (installmentNum == 4 && installmentDueDate4 != null) {
						commissionOrderDto.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate4)));
					} else if (installmentNum == 5 && installmentDueDate5 != null) {
						commissionOrderDto.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate5)));
					} else if (installmentNum == 6 && installmentDueDate6 != null) {
						commissionOrderDto.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate6)));
					} else if (installmentNum == 7 && installmentDueDate7 != null) {
						commissionOrderDto.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate7)));
					} else if (installmentNum == 8 && installmentDueDate8 != null) {
						commissionOrderDto.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate8)));
					} else if (installmentNum == 9 && installmentDueDate9 != null) {
						commissionOrderDto.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate9)));
					} else if (installmentNum == 10 && installmentDueDate10 != null) {
						commissionOrderDto.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate10)));
					} else if (installmentNum == 11 && installmentDueDate11 != null) {
						commissionOrderDto.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate11)));
					} else if (installmentNum == 12 && installmentDueDate12 != null) {
						commissionOrderDto.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate12)));
					} else
						break;
					commissionOrderDto.setState(ReviewKjStateEnum.PENDING.toString());
					commissionOrderDto.setVerifyCode(null);
					commissionOrderDto.setKjApprovalDate(null);
					commissionOrderDto.setReceiveDate(null);
					commissionOrderDto.setPaymentVoucherImageUrl1(null);
					commissionOrderDto.setPaymentVoucherImageUrl2(null);
					commissionOrderDto.setPaymentVoucherImageUrl3(null);
					commissionOrderDto.setPaymentVoucherImageUrl4(null);
					commissionOrderDto.setPaymentVoucherImageUrl5(null);
					commissionOrderDto.setInvoiceNumber(null);
				}
				int id = commissionOrderService.addCommissionOrder(commissionOrderDto);
				if (id > 0) {
					serviceOrderDto.setSubmitted(true);
					userService.updateDOB(new Date(Long.parseLong(dob)), userId);
					serviceOrderService.updateServiceOrder(serviceOrderDto); // 同时更改服务订单状态
					ApplicantDTO applicantDto = serviceOrderDto.getApplicant();
					if (applicantDto != null && applicantBirthday != null) {
						applicantDto.setBirthday(new Date(Long.parseLong(applicantBirthday)));
						if (applicantService.update(applicantDto) <= 0)
							msg += "申请人生日修改失败! (serviceOrderId:" + serviceOrderDto.getId() + ", applicantId:"
									+ applicantDto.getId() + ", applicantBirthday:" + applicantDto.getBirthday() + ");";
						else
							msg += "申请人生日修改成功. (serviceOrderId:" + serviceOrderDto.getId() + ", applicantId:"
									+ applicantDto.getId() + ", applicantBirthday:" + applicantDto.getBirthday() + ");";
					}
					commissionOrderDtoList.add(commissionOrderDto);
					CommissionOrderListDTO commissionOrderListDto = commissionOrderService.getCommissionOrderById(id);
					if (isSettle){

					}
					int i = 0;
					if (commissionOrderListDto.getSchoolId() > 0)
						i = schoolService.updateSchoolSetting(commissionOrderListDto); // 根据学校设置更新佣金值
					else
						i = schoolInstitutionService.updateSchoolSetting(commissionOrderListDto);
					if (i > 0) {
					} else if (i == -1)
						msg += id + "计算失败. (佣金记录不存在);";
					else if (i == -2)
						msg += id + "计算失败. (学校佣金设置不存在或不正确);";
					else if (i == -3)
						msg += id + "计算失败. (佣金办理时间不在设置合同时间范围内);";
					else
						msg += id + "计算失败. ;";
				} else
					msg += "佣金订单创建失败. (" + commissionOrderDto.toString() + ");";
			}
			return new Response<List<CommissionOrderDTO>>(0, msg, commissionOrderDtoList);
		} catch (ServiceException e) {
			return new Response<List<CommissionOrderDTO>>(e.getCode(), e.getMessage(), null);
		}
	}

	// 顾问不能修改,财务能修改
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public Response<CommissionOrderDTO> update(@RequestParam(value = "id") int id,
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "commissionState", required = false) String commissionState,
			@RequestParam(value = "isSettle", required = false) Boolean isSettle,
			@RequestParam(value = "isDepositUser", required = false) Boolean isDepositUser,
			@RequestParam(value = "schoolId", required = false) Integer schoolId,
			@RequestParam(value = "studentCode", required = false) String studentCode,
			@RequestParam(value = "adviserId", required = false) Integer adviserId,
			@RequestParam(value = "officialId", required = false) Integer officialId,
			@RequestParam(value = "isStudying", required = false) Boolean isStudying,
			@RequestParam(value = "installmentDueDate", required = false) String installmentDueDate,
			@RequestParam(value = "paymentVoucherImageUrl1", required = false) String paymentVoucherImageUrl1,
			@RequestParam(value = "paymentVoucherImageUrl2", required = false) String paymentVoucherImageUrl2,
			@RequestParam(value = "paymentVoucherImageUrl3", required = false) String paymentVoucherImageUrl3,
			@RequestParam(value = "paymentVoucherImageUrl4", required = false) String paymentVoucherImageUrl4,
			@RequestParam(value = "paymentVoucherImageUrl5", required = false) String paymentVoucherImageUrl5,
			@RequestParam(value = "dob", required = false) String dob,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "tuitionFee", required = false) String tuitionFee,
			@RequestParam(value = "perTermTuitionFee", required = false) String perTermTuitionFee,
			@RequestParam(value = "receiveTypeId", required = false) Integer receiveTypeId,
			@RequestParam(value = "receiveDate", required = false) String receiveDate,
			@RequestParam(value = "perAmount", required = false) String perAmount,
			@RequestParam(value = "amount", required = false) String amount,
			@RequestParam(value = "sureExpectAmount", required = false) Double sureExpectAmount,
			@RequestParam(value = "currency", required = false) String currency,
			@RequestParam(value = "exchangeRate", required = false) String exchangeRate,
			@RequestParam(value = "invoiceNumber", required = false) String invoiceNumber,
			@RequestParam(value = "zyDate", required = false) String zyDate,
			@RequestParam(value = "bankCheck", required = false) String bankCheck,
			@RequestParam(value = "isChecked", required = false) String isChecked,
			@RequestParam(value = "remarks", required = false) String remarks,
			@RequestParam(value = "verifyCode", required = false) String verifyCode,
			@RequestParam(value = "applicantBirthday", required = false) String applicantBirthday,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			CommissionOrderListDTO commissionOrderListDto = commissionOrderService.getCommissionOrderById(id);
			if (commissionOrderListDto == null)
				return new Response<CommissionOrderDTO>(1, "留学佣金订单订单(ID:" + id + ")不存在!", null);
			ServiceOrderDTO serviceOrderDto = serviceOrderService
					.getServiceOrderById(commissionOrderListDto.getServiceOrderId());
			if (serviceOrderDto == null)
				return new Response<CommissionOrderDTO>(1,
						"服务订单(ID:" + commissionOrderListDto.getServiceOrderId() + ")不存在!", null);
			if (serviceOrderDto.getSubagencyId() <= 0)
				return new Response<CommissionOrderDTO>(1, "SubagencyId(" + serviceOrderDto.getSubagencyId() + ")不存在!",
						null);
			CommissionOrderDTO commissionOrderDto = commissionOrderService.getCommissionOrderById(id);
			if (commissionOrderDto == null)
				return new Response<CommissionOrderDTO>(1, "佣金订单不存在,修改失败.", null);
			if (adminUserLoginInfo != null && "KJ".equalsIgnoreCase(adminUserLoginInfo.getApList())
					&& commissionState != null) { // 只有会计能修改佣金状态
				commissionOrderDto
						.setCommissionState(CommissionStateEnum.get(commissionState.toUpperCase()).toString());
				if (CommissionStateEnum.YJY.toString().equalsIgnoreCase(commissionState))
					state = ReviewKjStateEnum.COMPLETE.toString(); // 如果会计修改状态为已结佣，那么佣金订单状态需改成COMPLETE
			}
			if (StringUtil.isNotEmpty(state))
				commissionOrderDto.setState(state);
			else
				commissionOrderDto.setState(ReviewKjStateEnum.REVIEW.toString()); // 修改后重新审核
			if (isSettle != null)
				commissionOrderDto.setSettle(isSettle);
			if (isDepositUser != null)
				commissionOrderDto.setDepositUser(isDepositUser);
			if (schoolId != null)
				commissionOrderDto.setSchoolId(schoolId);
			if (StringUtil.isNotEmpty(studentCode))
				commissionOrderDto.setStudentCode(studentCode);
			if (adviserId != null)
				commissionOrderDto.setAdviserId(adviserId);
			if (officialId != null)
				commissionOrderDto.setOfficialId(officialId);
			if (isStudying != null)
				commissionOrderDto.setStudying(isStudying);
			if (installmentDueDate != null)
				commissionOrderDto.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate)));
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl1)) {
				commissionOrderDto.setPaymentVoucherImageUrl1(paymentVoucherImageUrl1);
				serviceOrderDto.setPaymentVoucherImageUrl1(paymentVoucherImageUrl1);
			}
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl2)) {
				commissionOrderDto.setPaymentVoucherImageUrl2(paymentVoucherImageUrl2);
				serviceOrderDto.setPaymentVoucherImageUrl2(paymentVoucherImageUrl2);
			}
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl3)) {
				commissionOrderDto.setPaymentVoucherImageUrl3(paymentVoucherImageUrl3);
				serviceOrderDto.setPaymentVoucherImageUrl3(paymentVoucherImageUrl3);
			}
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl4)) {
				commissionOrderDto.setPaymentVoucherImageUrl4(paymentVoucherImageUrl4);
				serviceOrderDto.setPaymentVoucherImageUrl4(paymentVoucherImageUrl4);
			}
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl5)) {
				commissionOrderDto.setPaymentVoucherImageUrl5(paymentVoucherImageUrl5);
				serviceOrderDto.setPaymentVoucherImageUrl5(paymentVoucherImageUrl5);
			}
			if (dob != null)
				commissionOrderDto.setDob(new Date(Long.parseLong(dob)));
			if (startDate != null)
				commissionOrderDto.setStartDate(new Date(Long.parseLong(startDate)));
			if (endDate != null)
				commissionOrderDto.setEndDate(new Date(Long.parseLong(endDate)));
			if (StringUtil.isNotEmpty(tuitionFee))
				commissionOrderDto.setTuitionFee(Double.parseDouble(tuitionFee));
			if (StringUtil.isNotEmpty(perTermTuitionFee))
				commissionOrderDto.setPerTermTuitionFee(Double.parseDouble(perTermTuitionFee));
			if (receiveTypeId != null) {
				commissionOrderDto.setReceiveTypeId(receiveTypeId);
				serviceOrderDto.setReceiveTypeId(receiveTypeId);
			}
			if (StringUtil.isNotEmpty(receiveDate)) {
				commissionOrderDto.setReceiveDate(new Date(Long.parseLong(receiveDate)));
				serviceOrderDto.setReceiveDate(new Date(Long.parseLong(receiveDate)));
			}
			if (StringUtil.isNotEmpty(perAmount)) {
				commissionOrderDto.setPerAmount(Double.parseDouble(perAmount));
				serviceOrderDto.setPerAmount(Double.parseDouble(perAmount));
			}
			if (StringUtil.isNotEmpty(amount)) {
				commissionOrderDto.setAmount(Double.parseDouble(amount));
				serviceOrderDto.setAmount(Double.parseDouble(amount));
			}
			double _perAmount = commissionOrderListDto.getPerAmount();
			if (commissionOrderDto.getPerAmount() > 0)
				_perAmount = commissionOrderDto.getPerAmount();
			if (_perAmount < commissionOrderDto.getAmount())
				return new Response<CommissionOrderDTO>(1,
						"本次应收款(" + _perAmount + ")不能小于本次已收款(" + commissionOrderDto.getAmount() + ")!", null);
			commissionOrderDto.setDiscount(_perAmount - commissionOrderDto.getAmount());
			if (sureExpectAmount != null)
				commissionOrderDto.setSureExpectAmount(sureExpectAmount);
			if (sureExpectAmount != null) {
				if ("CNY".equalsIgnoreCase(commissionOrderListDto.getCurrency()))
					commissionOrderDto.setSureExpectAmount(
							new BigDecimal(sureExpectAmount * commissionOrderListDto.getExchangeRate())
									.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
				else
					commissionOrderDto.setSureExpectAmount(sureExpectAmount);
			}
			if (StringUtil.isNotEmpty(currency))
				commissionOrderDto.setCurrency(currency);
			if (StringUtil.isNotEmpty(exchangeRate))
				commissionOrderDto.setExchangeRate(Double.parseDouble(exchangeRate));
			Double rate = getRate("留学", commissionOrderDto.getId(),
					regionService.isCNByAdviserId(commissionOrderDto.getAdviserId()));
			if (rate != null && rate > 0)
				commissionOrderDto.setExchangeRate(rate);
			if (StringUtil.isNotEmpty(invoiceNumber))
				commissionOrderDto.setInvoiceNumber(invoiceNumber);
			if (StringUtil.isNotEmpty(zyDate))
				commissionOrderDto.setZyDate(new Date(Long.parseLong(zyDate)));
			if (StringUtil.isNotEmpty(bankCheck))
				commissionOrderDto.setBankCheck(bankCheck);
			commissionOrderDto.setChecked(isChecked != null && "true".equalsIgnoreCase(isChecked));
			if (StringUtil.isNotEmpty(remarks))
				commissionOrderDto.setRemarks(remarks);
			if (StringUtil.isNotEmpty(verifyCode))
				commissionOrderDto.setVerifyCode(verifyCode.replace("$", "").replace("#", "").replace(" ", ""));
			if (commissionOrderDto.getKjApprovalDate() == null || commissionOrderDto.getKjApprovalDate().getTime() == 0)
				commissionOrderDto.setKjApprovalDate(new Date());

			// SubagencyDTO subagencyDto =
			// subagencyService.getSubagencyById(serviceOrderDto.getSubagencyId());
			// if (subagencyDto == null)
			// return new Response<CommissionOrderDTO>(1, "Subagency(" +
			// serviceOrderDto.getSubagencyId() + ")不存在!",
			// null);
			// // 佣金
			// commissionOrderDto.setCommission(commissionOrderDto.getAmount());
			// // 预收业绩
			// Double expectAmount = commissionOrderDto.getAmount() *
			// subagencyDto.getCommissionRate() * 1.1;
			// commissionOrderDto.setExpectAmount(expectAmount);
			// // GST
			// commissionOrderDto
			// .setGst(new BigDecimal(expectAmount / 11).setScale(2,
			// BigDecimal.ROUND_HALF_UP).doubleValue());
			// // Deduct GST
			// commissionOrderDto.setDeductGst(new BigDecimal(expectAmount -
			// commissionOrderDto.getGst())
			// .setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
			// Bonus
			// commissionOrderDto.setBonus(new
			// BigDecimal(commissionOrderDto.getDeductGst() * 0.1)
			// .setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());

			String msg = "";
			if (commissionOrderService.updateCommissionOrder(commissionOrderDto) > 0) {
				CommissionOrderListDTO _commissionOrderListDto = commissionOrderService
						.getCommissionOrderById(commissionOrderDto.getId());
				serviceOrderDto.setReceivable(_commissionOrderListDto.getTotalPerAmount());
				serviceOrderDto.setReceived(_commissionOrderListDto.getTotalAmount());
				serviceOrderService.updateServiceOrder(serviceOrderDto); // 同步修改服务订单
				ApplicantDTO applicantDto = serviceOrderDto.getApplicant();
				if (StringUtil.isEmpty(applicantBirthday))
					applicantBirthday = dob;
				if (applicantDto != null && applicantBirthday != null) {
					applicantDto.setBirthday(new Date(Long.parseLong(applicantBirthday)));
					if (applicantService.update(applicantDto) <= 0)
						msg += "申请人生日修改失败! (serviceOrderId:" + serviceOrderDto.getId() + ", applicantId:"
								+ applicantDto.getId() + ", applicantBirthday:" + applicantDto.getBirthday() + ");";
					else
						msg += "申请人生日修改成功. (serviceOrderId:" + serviceOrderDto.getId() + ", applicantId:"
								+ applicantDto.getId() + ", applicantBirthday:" + applicantDto.getBirthday() + ");";
				}
				userService.updateDOB(new Date(Long.parseLong(dob)), commissionOrderListDto.getUserId());
				int i = 0;
				if (commissionOrderListDto.getCourseId() == 0)
					i = schoolService.updateSchoolSetting(commissionOrderListDto); // 根据学校设置更新佣金值
				else
					i = schoolInstitutionService.updateSchoolSetting(_commissionOrderListDto);
				if (i > 0) {
				} else if (i == -1)
					msg += id + "计算失败. (佣金记录不存在);";
				else if (i == -2)
					msg += id + "计算失败. (学校佣金设置不存在或不正确);";
				else if (i == -3)
					msg += id + "计算失败. (佣金办理时间不在设置合同时间范围内);";
				else
					msg += id + "计算失败. ;";
				return new Response<CommissionOrderDTO>(0, msg, commissionOrderDto);
			} else
				return new Response<CommissionOrderDTO>(1, "修改失败.", null);
		} catch (ServiceException e) {
			return new Response<CommissionOrderDTO>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/kjUpdate", method = RequestMethod.POST)
	@ResponseBody
	public Response<CommissionOrderDTO> kjUpdate(@RequestParam(value = "id") int id,
			@RequestParam(value = "schoolPaymentAmount", required = false) Double schoolPaymentAmount,
			@RequestParam(value = "schoolPaymentDate", required = false) String schoolPaymentDate,
			@RequestParam(value = "invoiceNumber", required = false) String invoiceNumber,
			@RequestParam(value = "zyDate", required = false) String zyDate,
			@RequestParam(value = "sureExpectAmount", required = false) Double sureExpectAmount,
			@RequestParam(value = "bonus", required = false) Double bonus,
			@RequestParam(value = "bonusDate", required = false) String bonusDate, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo != null) {
				if (adminUserLoginInfo == null || (!"SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
						&& !"KJ".equalsIgnoreCase(adminUserLoginInfo.getApList())))
					return new Response<CommissionOrderDTO>(1, "仅限会计修改.", null);
				return updateOne(id, schoolPaymentAmount, schoolPaymentDate, invoiceNumber, zyDate, sureExpectAmount,
						bonus, bonusDate, true);
			} else
				return new Response<CommissionOrderDTO>(1, "修改失败.", null);
		} catch (ServiceException e) {
			return new Response<CommissionOrderDTO>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/updateKjApprovalDate", method = RequestMethod.POST)
	@ResponseBody
	public Response<CommissionOrderDTO> updateKjApprovalDate(@RequestParam(value = "id") int id,
			@RequestParam(value = "kjApprovalDate") String kjApprovalDate, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			CommissionOrderDTO commissionOrderDto = commissionOrderService.getCommissionOrderById(id);
			if (commissionOrderDto == null)
				return new Response<CommissionOrderDTO>(1, "佣金订单不存在,修改失败.", null);
			if (adminUserLoginInfo != null && ("KJ".equalsIgnoreCase(adminUserLoginInfo.getApList())
					|| "SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())))
				commissionOrderDto.setKjApprovalDate(new Date(Long.parseLong(kjApprovalDate)));
			else
				return new Response<CommissionOrderDTO>(1, "只有会计和超级管理员能修改会计审核日期.", null);
			if (commissionOrderService.updateCommissionOrder(commissionOrderDto) > 0) {
				LOG.info("修改留学订单提交审核日期.(username=" + adminUserLoginInfo.getUsername() + ",id=" + id + ",kjApprovalDate="
						+ kjApprovalDate);
				return new Response<CommissionOrderDTO>(0, "", commissionOrderDto);
			} else
				return new Response<CommissionOrderDTO>(1, "修改失败.", null);
		} catch (ServiceException e) {
			return new Response<CommissionOrderDTO>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/update", method = RequestMethod.PUT)
	@ResponseBody
	public Response<Integer> update(@RequestBody List<BatchUpdateCommissionOrder> batchUpdateList,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo != null)
				if (adminUserLoginInfo == null || (!"SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
						&& !"KJ".equalsIgnoreCase(adminUserLoginInfo.getApList())))
					return new Response<Integer>(1, "仅限会计修改.", 0);
			return batchUpdate(batchUpdateList, false);
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/kjUpdate", method = RequestMethod.PUT)
	@ResponseBody
	public Response<Integer> kjUpdate(@RequestBody List<BatchUpdateCommissionOrder> batchUpdateList,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo != null)
				if (adminUserLoginInfo == null || (!"SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
						&& !"KJ".equalsIgnoreCase(adminUserLoginInfo.getApList())))
					return new Response<Integer>(1, "仅限会计修改.", 0);
			return batchUpdate(batchUpdateList, true);
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	private Response<Integer> batchUpdate(List<BatchUpdateCommissionOrder> batchUpdateList, boolean isChangeState)
			throws ServiceException {
		int x = 0;
		String msg = "";
		for (BatchUpdateCommissionOrder batchUpdate : batchUpdateList) {
			Response<CommissionOrderDTO> _response = updateOne(batchUpdate.getId(),
					batchUpdate.getSchoolPaymentAmount(), batchUpdate.getSchoolPaymentDate(),
					batchUpdate.getInvoiceNumber(), batchUpdate.getZyDate(), batchUpdate.getSureExpectAmount(),
					batchUpdate.getBonus(), batchUpdate.getBonusDate(), isChangeState);
			if (_response.getCode() == 0)
				x++;
			else
				msg += _response.getMessage();
		}
		return new Response<Integer>(0, msg, x);
	}

	private Response<CommissionOrderDTO> updateOne(int id, Double schoolPaymentAmount, String schoolPaymentDate,
			String invoiceNumber, String zyDate, Double sureExpectAmount, Double bonus, String bonusDate,
			boolean isChangeState) throws ServiceException {
		CommissionOrderDTO commissionOrderDto = commissionOrderService.getCommissionOrderById(id);
		if (commissionOrderDto == null)
			return new Response<CommissionOrderDTO>(1, "留学佣金订单订单(ID:" + id + ")不存在!", null);
		if (schoolPaymentAmount != null)
			commissionOrderDto.setSchoolPaymentAmount(schoolPaymentAmount);
		if (schoolPaymentDate != null)
			commissionOrderDto.setSchoolPaymentDate(new Date(Long.parseLong(schoolPaymentDate)));
		if (StringUtil.isNotEmpty(invoiceNumber))
			commissionOrderDto.setInvoiceNumber(invoiceNumber);
		if (StringUtil.isNotEmpty(zyDate))
			commissionOrderDto.setZyDate(new Date(Long.parseLong(zyDate)));
		if (sureExpectAmount != null) {
			if ("CNY".equalsIgnoreCase(commissionOrderDto.getCurrency()))
				commissionOrderDto
						.setSureExpectAmount(new BigDecimal(sureExpectAmount * commissionOrderDto.getExchangeRate())
								.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
			else
				commissionOrderDto.setSureExpectAmount(sureExpectAmount);
		}
		if (bonus != null)
			commissionOrderDto.setBonus(bonus);
		if (bonusDate != null)
			commissionOrderDto.setBonusDate(new Date(Long.parseLong(bonusDate)));
		if (isChangeState && schoolPaymentDate != null && schoolPaymentAmount != null
				&& StringUtil.isNotEmpty(invoiceNumber))
			if (bonus != null || bonusDate != null) {
				commissionOrderDto.setState(ReviewKjStateEnum.COMPLETE.toString());
				commissionOrderDto.setCommissionState(CommissionStateEnum.YJY.toString());
			} else {
				commissionOrderDto.setState(ReviewKjStateEnum.REVIEW.toString());
				commissionOrderDto.setCommissionState(CommissionStateEnum.YZY.toString());
			}
//		return commissionOrderService.updateCommissionOrder(commissionOrderDto) > 0
//				? new Response<CommissionOrderDTO>(0, commissionOrderDto)
//				: new Response<CommissionOrderDTO>(1, "修改失败.", null);

		String msg = "";
		if (commissionOrderService.updateCommissionOrder(commissionOrderDto) > 0) {
			if (sureExpectAmount != null && sureExpectAmount > 0) {
				CommissionOrderListDTO _commissionOrderListDto = commissionOrderService
						.getCommissionOrderById(commissionOrderDto.getId());
				int i = schoolService.updateSchoolSetting(_commissionOrderListDto); // 根据学校设置更新佣金值
				if (i > 0) {
				} else if (i == -1)
					msg += id + "计算失败. (佣金记录不存在);";
				else if (i == -2)
					msg += id + "计算失败. (学校佣金设置不存在或不正确);";
				else if (i == -3)
					msg += id + "计算失败. (佣金办理时间不在设置合同时间范围内);";
				else
					msg += id + "计算失败. ;";
			}
			return new Response<CommissionOrderDTO>(0, msg, commissionOrderDto);
		} else
			return new Response<CommissionOrderDTO>(1, "修改失败.", null);
	}

	@RequestMapping(value = "/updateCommission", method = RequestMethod.POST)
	@ResponseBody
	public Response<CommissionOrderDTO> updateCommission(@RequestParam(value = "id") int id, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			CommissionOrderListDTO commissionOrderListDto = commissionOrderService.getCommissionOrderById(id);
			int i = schoolInstitutionService.updateSchoolSetting(commissionOrderListDto); // 根据学校设置更新佣金值
			if (i > 0)
				return new Response<CommissionOrderDTO>(0, "计算成功.", commissionOrderListDto);
			else if (i == -1)
				return new Response<CommissionOrderDTO>(1, "计算失败. (佣金记录不存在)", null);
			else if (i == -2)
				return new Response<CommissionOrderDTO>(2, "计算失败. (学校佣金设置不存在或不正确)", null);
			else if (i == -3)
				return new Response<CommissionOrderDTO>(3, "计算失败. (佣金办理时间不在设置合同时间范围内)", null);
			else
				return new Response<CommissionOrderDTO>(4, "计算失败.", null);
		} catch (ServiceException e) {
			return new Response<CommissionOrderDTO>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/close", method = RequestMethod.POST)
	@ResponseBody
	public Response<CommissionOrderDTO> close(@RequestParam(value = "id") int id,
			@RequestParam(value = "isStudying", required = false) Boolean isStudying,
			@RequestParam(value = "remarks", required = false) String remarks, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			CommissionOrderDTO commissionOrderDto = commissionOrderService.getCommissionOrderById(id);
			if (commissionOrderDto == null)
				return new Response<CommissionOrderDTO>(1, "佣金订单不存在,修改失败.", null);
			commissionOrderDto.setState(ReviewKjStateEnum.CLOSE.toString());
			commissionOrderDto.setClose(true);
			commissionOrderDto.setInvoiceNumber(null); // close后把invoice清空
			if (isStudying != null)
				commissionOrderDto.setStudying(isStudying);
			if (StringUtil.isNotEmpty(remarks))
				commissionOrderDto.setRemarks(remarks);
			return commissionOrderService.updateCommissionOrder(commissionOrderDto) > 0
					? new Response<CommissionOrderDTO>(0, commissionOrderDto)
					: new Response<CommissionOrderDTO>(1, "修改失败.", null);
		} catch (ServiceException e) {
			return new Response<CommissionOrderDTO>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/count", method = RequestMethod.GET)
	@ResponseBody
	@Deprecated
	public Response<Integer> count(@RequestParam(value = "id", required = false) Integer id,
			@RequestParam(value = "regionId", required = false) Integer regionId,
			@RequestParam(value = "maraId", required = false) Integer maraId,
			@RequestParam(value = "adviserId", required = false) Integer adviserId,
			@RequestParam(value = "officialId", required = false) Integer officialId,
			@RequestParam(value = "userId", required = false) Integer userId,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "applicantName", required = false) String applicantName,
			@RequestParam(value = "phone", required = false) String phone,
			@RequestParam(value = "wechatUsername", required = false) String wechatUsername,
			@RequestParam(value = "schoolId", required = false) Integer schoolId,
			@RequestParam(value = "isSettle", required = false) Boolean isSettle,
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "applyState", required = false) String applyState,
			@RequestParam(value = "commissionState", required = false) String commissionState,
			@RequestParam(value = "startKjApprovalDate", required = false) String startKjApprovalDate,
			@RequestParam(value = "endKjApprovalDate", required = false) String endKjApprovalDate,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "startInvoiceCreate", required = false) String startInvoiceCreate,
			@RequestParam(value = "endInvoiceCreate", required = false) String endInvoiceCreate,
			HttpServletRequest request, HttpServletResponse response) {

		Integer newMaraId = getMaraId(request);
		if (newMaraId != null)
			maraId = newMaraId;
		Integer newOfficialId = getOfficialId(request);
		if (newOfficialId != null)
			officialId = newOfficialId;

		List<String> commissionStateList = null;
		if (StringUtil.isNotEmpty(commissionState))
			commissionStateList = Arrays.asList(commissionState.split(","));

		List<Integer> regionIdList = null;
		if (regionId != null && regionId > 0)
			regionIdList = ListUtil.buildArrayList(regionId);

		// 会计角色过滤状态
		Boolean isYzyAndYjy = false;
		List<String> stateList = new ArrayList<>();
		if (state == null && getKjId(request) != null) {
			stateList.add(ReviewKjStateEnum.REVIEW.toString());
			stateList.add(ReviewKjStateEnum.FINISH.toString());
			stateList.add(ReviewKjStateEnum.COMPLETE.toString());
			stateList.add(ReviewKjStateEnum.CLOSE.toString());
			if (CommissionStateEnum.YZY.toString().equalsIgnoreCase(commissionState)) {
				commissionStateList = null;
				isYzyAndYjy = true;
			}
		} else if (state == null)
			stateList = null;
		else
			stateList.add(state);

//		Date _startKjApprovalDate = null;
//		if (startKjApprovalDate != null)
//			_startKjApprovalDate = new Date(Long.parseLong(startKjApprovalDate));
//		Date _endKjApprovalDate = null;
//		if (endKjApprovalDate != null)
//			_endKjApprovalDate = new Date(Long.parseLong(endKjApprovalDate));

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

			return new Response<Integer>(0,
					commissionOrderService.countCommissionOrder(id, regionIdList, maraId, adviserId, officialId, userId,
							name, applicantName, phone, wechatUsername, schoolId, isSettle, stateList, commissionStateList,
							startKjApprovalDate, endKjApprovalDate,startDate,endDate, startInvoiceCreate, endInvoiceCreate, isYzyAndYjy,
							applyState));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), null);
		}
	}


	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public ListResponse<List<CommissionOrderListDTO>> list(@RequestParam(value = "id", required = false) Integer id,
			@RequestParam(value = "regionId", required = false) Integer regionId,
			@RequestParam(value = "maraId", required = false) Integer maraId,
			@RequestParam(value = "adviserId", required = false) Integer adviserId,
			@RequestParam(value = "officialId", required = false) Integer officialId,
			@RequestParam(value = "userId", required = false) Integer userId,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "userName", required = false) String userName,
			@RequestParam(value = "applicantName", required = false) String applicantName,
			@RequestParam(value = "phone", required = false) String phone,
			@RequestParam(value = "wechatUsername", required = false) String wechatUsername,
			@RequestParam(value = "schoolId", required = false) Integer schoolId,
			@RequestParam(value = "isSettle", required = false) Boolean isSettle,
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "applyState", required = false) String applyState,
			@RequestParam(value = "commissionState", required = false) String commissionState,
			@RequestParam(value = "startKjApprovalDate", required = false) String startKjApprovalDate,
			@RequestParam(value = "endKjApprovalDate", required = false) String endKjApprovalDate,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "startInvoiceCreate", required = false) String startInvoiceCreate,
			@RequestParam(value = "endInvoiceCreate", required = false) String endInvoiceCreate,
			@RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
			@RequestParam(value = "sorter", required = false) String sorter, HttpServletRequest request,
			HttpServletResponse response) {

		Integer newMaraId = getMaraId(request);
		if (newMaraId != null)
			maraId = newMaraId;
		Integer newOfficialId = getOfficialId(request);
		if (newOfficialId != null)
			officialId = newOfficialId;

		List<String> commissionStateList = null;
		if (StringUtil.isNotEmpty(commissionState))
			commissionStateList = Arrays.asList(commissionState.split(","));

		// 会计角色过滤状态
		Boolean isYzyAndYjy = false;
		List<String> stateList = new ArrayList<>();
		if (state == null && getKjId(request) != null) {
			stateList.add(ReviewKjStateEnum.REVIEW.toString());
			stateList.add(ReviewKjStateEnum.FINISH.toString());
			stateList.add(ReviewKjStateEnum.COMPLETE.toString());
//			stateList.add(ReviewKjStateEnum.CLOSE.toString());
			if (CommissionStateEnum.YZY.toString().equalsIgnoreCase(commissionState)) {
				commissionStateList = null;
				isYzyAndYjy = true;
			}
		} else if (state == null)
			stateList = null;
		else
			stateList.add(state);

		List<Integer> regionIdList = null;
		if (regionId != null && regionId > 0)
			regionIdList = ListUtil.buildArrayList(regionId);

		Sorter _sorter = null;
		if (sorter != null)
			_sorter = JSON.parseObject(sorter.replace("adviser,name", "adviserName"), Sorter.class);

		if (StringUtil.isNotEmpty(userName))
			name = userName;

//		Date _startKjApprovalDate = null;
//		if (startKjApprovalDate != null)
//			_startKjApprovalDate = new Date(Long.parseLong(startKjApprovalDate));
//		Date _endKjApprovalDate = null;
//		if (endKjApprovalDate != null)
//			_endKjApprovalDate = new Date(Long.parseLong(endKjApprovalDate));

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
					return new ListResponse<List<CommissionOrderListDTO>>(false, pageSize, 0, null, "No permission !");
				if ("GW".equalsIgnoreCase(adminUserLoginInfo.getApList()) && adviserId == null)
					return new ListResponse<List<CommissionOrderListDTO>>(false, pageSize, 0, null,
							"无法获取顾问编号，请退出重新登录后再尝试．");
			}

			int total = commissionOrderService.countCommissionOrder(id, regionIdList, maraId, adviserId, officialId,
					userId, name, applicantName, phone, wechatUsername, schoolId, isSettle, stateList,
					commissionStateList, startKjApprovalDate, endKjApprovalDate, startDate, endDate, startInvoiceCreate,
					endInvoiceCreate, isYzyAndYjy, applyState);
			List<CommissionOrderListDTO> list = commissionOrderService.listCommissionOrder(id, regionIdList, maraId,
					adviserId, officialId, userId, name, applicantName, phone, wechatUsername, schoolId, isSettle,
					stateList, commissionStateList, startKjApprovalDate, endKjApprovalDate, startDate, endDate,
					startInvoiceCreate, endInvoiceCreate, isYzyAndYjy, applyState, pageNum, pageSize, _sorter);
			list.forEach(co -> {
				if (co.getServiceOrderId() > 0)
					try {
						ServiceOrderDTO serviceOrderDto = serviceOrderService
								.getServiceOrderById(co.getServiceOrderId());
						if (serviceOrderDto != null) {
							ApplicantDTO applicantDto = co.getApplicant();
							if (applicantDto != null) {
								if (StringUtil.isEmpty(applicantDto.getUrl()))
									applicantDto.setUrl(serviceOrderDto.getNutCloud());
								if (StringUtil.isEmpty(applicantDto.getContent()))
									applicantDto.setContent(serviceOrderDto.getInformation());
								co.setApplicant(applicantDto);
							}
						}
					} catch (ServiceException e) {
						e.printStackTrace();
					}
				try {
					List<MailRemindDTO> mailRemindDTOS = mailRemindService.list(getAdviserId(request),newOfficialId,getKjId(request),
							null,null,co.getId(),null,false,true);
					co.setMailRemindDTOS(mailRemindDTOS);
				} catch (ServiceException se) {
					se.printStackTrace();
				}
			});
			if (list == null) {
				list = new ArrayList<>();
			}
			return new ListResponse<List<CommissionOrderListDTO>>(true, pageSize, total, list, "");
		} catch (ServiceException | InterruptedException e) {
			return new ListResponse<List<CommissionOrderListDTO>>(false, pageSize, 0, null, e.getMessage());
		}
	}

	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> upload(@RequestParam MultipartFile file, HttpServletRequest request,
			HttpServletResponse response) throws IllegalStateException, IOException {
		super.setPostHeader(response);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String message = "";
		int n = 0;
		Response<String> r = super.upload2(file, request.getSession(), "/tmp/");
		try (InputStream is = new FileInputStream("/data" + r.getData())) {
			Workbook wb = Workbook.getWorkbook(is);
			Sheet sheet = wb.getSheet(0);
			for (int i = 1; i < sheet.getRows(); i++) {
				Cell[] cells = sheet.getRow(i);
				if (cells == null || cells.length == 0)
					continue;
				String _id = cells[0].getContents();
				String _schoolPaymentAmount = cells[27].getContents();
				String _schoolPaymentDate = cells[28].getContents();
				String _invoiceNumber = cells[29].getContents();
				String _zyDate = cells[30].getContents();
				String _sureExpectAmount = cells[24].getContents();
				String _bonus = cells[32].getContents();
				String _bonusDate = cells[33].getContents();
				try {
					CommissionOrderListDTO commissionOrderListDto = commissionOrderService
							.getCommissionOrderById(Integer.parseInt(_id));
					if (commissionOrderListDto == null) {
						message += "[" + _id + "]佣金订单不存在;";
						continue;
					}
					if (!CommissionStateEnum.DJY.toString()
							.equalsIgnoreCase(commissionOrderListDto.getCommissionState())
							&& !CommissionStateEnum.DZY.toString()
									.equalsIgnoreCase(commissionOrderListDto.getCommissionState())
							&& !CommissionStateEnum.YZY.toString()
									.equalsIgnoreCase(commissionOrderListDto.getCommissionState())) {
						message += "[" + _id + "]只有状态为待结佣,待追佣,已追佣允许更新佣金订单;";
						continue;
					}
					Response<CommissionOrderDTO> _r = updateOne(Integer.parseInt(_id),
							StringUtil.isEmpty(_schoolPaymentAmount) ? null
									: Double.parseDouble(_schoolPaymentAmount.trim()),
							StringUtil.isEmpty(_schoolPaymentDate) ? null
									: simpleDateFormat.parse(_schoolPaymentDate.trim()).getTime() + "",
							_invoiceNumber,
							StringUtil.isEmpty(_zyDate) ? null : simpleDateFormat.parse(_zyDate.trim()).getTime() + "",
							StringUtil.isEmpty(_sureExpectAmount) ? null : Double.parseDouble(_sureExpectAmount.trim()),
							StringUtil.isEmpty(_bonus) ? null : Double.parseDouble(_bonus.trim()),
							StringUtil.isEmpty(_bonusDate) ? null
									: simpleDateFormat.parse(_bonusDate.trim()).getTime() + "",
							true);
					if (_r.getCode() > 0)
						message += "[" + _id + "]" + _r.getMessage() + ";";
					else
						n++;
				} catch (NumberFormatException | ServiceException | ParseException e) {
					message += "[" + _id + "]" + e.getMessage() + ";";
				}
			}
		} catch (BiffException | IOException e) {
			return new Response<Integer>(1, "上传失败:" + e.getMessage(), 0);
		}
		return new Response<Integer>(0, message, n);
	}

	@RequestMapping(value = "/down", method = RequestMethod.GET)
	@ResponseBody
	public void down(@RequestParam(value = "id", required = false) Integer id,
			@RequestParam(value = "regionId", required = false) Integer regionId,
			@RequestParam(value = "maraId", required = false) Integer maraId,
			@RequestParam(value = "adviserId", required = false) Integer adviserId,
			@RequestParam(value = "officialId", required = false) Integer officialId,
			@RequestParam(value = "userId", required = false) Integer userId,
			@RequestParam(value = "userName", required = false) String name,
			@RequestParam(value = "applicantName", required = false) String applicantName,
			@RequestParam(value = "phone", required = false) String phone,
			@RequestParam(value = "wechatUsername", required = false) String wechatUsername,
			@RequestParam(value = "schoolId", required = false) Integer schoolId,
			@RequestParam(value = "isSettle", required = false) Boolean isSettle,
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "commissionState", required = false) String commissionState,
			@RequestParam(value = "startKjApprovalDate", required = false) String startKjApprovalDate,
			@RequestParam(value = "endKjApprovalDate", required = false) String endKjApprovalDate,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "startInvoiceCreate", required = false) String startInvoiceCreate,
			@RequestParam(value = "endInvoiceCreate", required = false) String endInvoiceCreate,
			HttpServletRequest request, HttpServletResponse response) {

		Integer newMaraId = getMaraId(request);
		if (newMaraId != null)
			maraId = newMaraId;
		Integer newAdviserId = getAdviserId(request);
		if (newAdviserId != null)
			adviserId = newAdviserId;
		Integer newOfficialId = getOfficialId(request);
		if (newOfficialId != null)
			officialId = newOfficialId;
		Integer kjId = getKjId(request);

		List<String> commissionStateList = null;
		if (StringUtil.isNotEmpty(commissionState))
			commissionStateList = Arrays.asList(commissionState.split(","));

		// 会计角色过滤状态
		Boolean isYzyAndYjy = false;
		List<String> stateList = new ArrayList<>();
		if (state == null && getKjId(request) != null) {
			stateList.add(ReviewKjStateEnum.REVIEW.toString());
			stateList.add(ReviewKjStateEnum.FINISH.toString());
			stateList.add(ReviewKjStateEnum.COMPLETE.toString());
//			stateList.add(ReviewKjStateEnum.CLOSE.toString());
			if (CommissionStateEnum.YZY.toString().equalsIgnoreCase(commissionState)) {
				commissionStateList = null;
				isYzyAndYjy = true;
			}
		} else if (state == null)
			stateList = null;
		else
			stateList.add(state);

		List<Integer> regionIdList = null;
		if (regionId != null && regionId > 0)
			regionIdList = ListUtil.buildArrayList(regionId);

		InputStream is = null;
		OutputStream os = null;
		Workbook wb = null;
		try {

			// 处理顾问管理员
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo != null && "GW".equalsIgnoreCase(adminUserLoginInfo.getApList())
					&& adminUserLoginInfo.getRegionId() != null && adminUserLoginInfo.getRegionId() > 0) {
				List<RegionDTO> regionList = regionService.listRegion(adminUserLoginInfo.getRegionId());
				regionIdList = ListUtil.buildArrayList(adminUserLoginInfo.getRegionId());
				for (RegionDTO region : regionList)
					regionIdList.add(region.getId());
				adviserId = null;
			}

			response.reset();// 清空输出流
			String tableName = "commission_order_information";
			response.setHeader("Content-disposition",
					"attachment; filename=" + new String(tableName.getBytes("GB2312"), "8859_1") + ".xls");
			response.setContentType("application/msexcel");

//			Date _startKjApprovalDate = null;
//			if (startKjApprovalDate != null)
//				_startKjApprovalDate = new Date(Long.parseLong(startKjApprovalDate));
//			Date _endKjApprovalDate = null;
//			if (endKjApprovalDate != null)
//				_endKjApprovalDate = new Date(Long.parseLong(endKjApprovalDate));

			List<CommissionOrderListDTO> commissionOrderList = commissionOrderService.listCommissionOrder(id,
					regionIdList, maraId, adviserId, officialId, userId, name, applicantName, phone, wechatUsername, schoolId,
					isSettle, stateList, commissionStateList, startKjApprovalDate, endKjApprovalDate,startDate,endDate,
					startInvoiceCreate, endInvoiceCreate, isYzyAndYjy, state, 0, 9999, null);
			
			if (commissionOrderList == null)
				throw new Exception("查询佣金订单数据错误!");
			System.out.println("导出佣金订单数据量:" + commissionOrderList.size());

			int _regionId = 0;
			if (ObjectUtil.isNotNull(adviserId) && adviserId > 0)
				_regionId = adviserService.getAdviserById(adviserId).getRegionId();
//			if (ObjectUtil.isNotNull(kjId) && kjId > 0)
//				_regionId = kjService.getKjById(kjId).getRegionId();
			if ("SUPERAD".equals(adminUserLoginInfo.getApList())) {
				os = response.getOutputStream();
				try {
					is = this.getClass().getResourceAsStream("/commission_order_information.xls");
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
				List<AdviserDTO> adviserDTOS = adviserService.listAdviser(null, null, 0, 1000);
				Map<Integer, String> adviserMap = adviserDTOS.stream().collect(Collectors.toMap(AdviserDTO::getId, AdviserDTO::getName, (v1, v2) -> v2));
				for (CommissionOrderListDTO commissionOrderListDto : commissionOrderList) {
					sheet.addCell(new Label(0, i, String.valueOf(commissionOrderListDto.getId()), cellFormat));
					sheet.addCell(new Label(1, i, sdf.format(commissionOrderListDto.getGmtCreate()), cellFormat));
					if (commissionOrderListDto.getReceiveDate() != null)
						sheet.addCell(new Label(2, i, sdf.format(commissionOrderListDto.getReceiveDate()), cellFormat));
					if (commissionOrderListDto.getApplicant() != null)
						sheet.addCell(new Label(3, i, commissionOrderListDto.getApplicant().getFirstname() + " "
								+ commissionOrderListDto.getApplicant().getSurname(), cellFormat));
					sheet.addCell(new Label(4, i, commissionOrderListDto.getStudentCode(), cellFormat));
					if (commissionOrderListDto.getBirthday() != null)
						sheet.addCell(new Label(5, i, sdf.format(commissionOrderListDto.getBirthday()), cellFormat));
					if (commissionOrderListDto.getReceiveType() != null)
						sheet.addCell(new Label(6, i, commissionOrderListDto.getReceiveType().getName() + "", cellFormat));
					if (commissionOrderListDto.getService() != null)
						sheet.addCell(new Label(7, i, commissionOrderListDto.getService().getName(), cellFormat));
					sheet.addCell(new Label(8, i, commissionOrderListDto.isSettle() + "", cellFormat));
					if (commissionOrderListDto.getSchool() != null) {
						sheet.addCell(new Label(9, i, commissionOrderListDto.getSchool().getName() + "", cellFormat));
						sheet.addCell(new Label(13, i, commissionOrderListDto.getSchool().getSubject() + "", cellFormat));
					}
					if (commissionOrderListDto.getSchoolInstitutionListDTO() != null){
						sheet.addCell(new Label(9, i, commissionOrderListDto.getSchoolInstitutionListDTO().getInstitutionTradingName() , cellFormat));
						//if (commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO() != null)
						sheet.addCell(new Label(10, i, commissionOrderListDto.getSchoolInstitutionListDTO().getInstitutionName(), cellFormat));
						if (commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO() != null){
							sheet.addCell(new Label(11, i, commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO().getName(), cellFormat));
							sheet.addCell(new Label(12, i, commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO().getState(), cellFormat));
						}
						if (commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolCourseDO() != null)
							sheet.addCell(new Label(13, i, commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolCourseDO().getCourseName(), cellFormat));
					}
					if (commissionOrderListDto.getStartDate() != null)
						sheet.addCell(new Label(14, i, sdf.format(commissionOrderListDto.getStartDate()), cellFormat));
					if (commissionOrderListDto.getEndDate() != null)
						sheet.addCell(new Label(15, i, sdf.format(commissionOrderListDto.getEndDate()), cellFormat));
					if (commissionOrderListDto.getInstallmentDueDate() != null)
						sheet.addCell(
								new Label(16, i, sdf.format(commissionOrderListDto.getInstallmentDueDate()), cellFormat));
					if (commissionOrderListDto.getReceiveType() != null)
						sheet.addCell(new Label(17, i, commissionOrderListDto.getReceiveType().getName() + "", cellFormat));
					sheet.addCell(new Label(18, i, commissionOrderListDto.getTuitionFee() + "", cellFormat));
					sheet.addCell(new Label(19, i, commissionOrderListDto.getPerAmount() + "", cellFormat)); // .getPerTermTuitionFee()
					sheet.addCell(new Label(20, i, commissionOrderListDto.getTotalPerAmountCNY() + "", cellFormat));
					sheet.addCell(new Label(21, i, commissionOrderListDto.getTotalPerAmountAUD() + "", cellFormat));
					sheet.addCell(new Label(22, i, commissionOrderListDto.getTotalAmountCNY() + "", cellFormat));
					sheet.addCell(new Label(23, i, commissionOrderListDto.getTotalAmountAUD() + "", cellFormat));
					sheet.addCell(new Label(24, i, commissionOrderListDto.getCurrency(), cellFormat));
					sheet.addCell(new Label(25, i, commissionOrderListDto.getExchangeRate() + "", cellFormat));
					sheet.addCell(new Label(26, i, commissionOrderListDto.getAmountCNY() + "", cellFormat));
					sheet.addCell(new Label(27, i, commissionOrderListDto.getAmountAUD() + "", cellFormat));
					sheet.addCell(new Label(28, i, commissionOrderListDto.getExpectAmountAUD() + "", cellFormat));
					if (commissionOrderListDto.isSettle())
						sheet.addCell(new Label(29, i, commissionOrderListDto.getExpectAmountAUD() + "", cellFormat));
					else
						sheet.addCell(new Label(29, i, commissionOrderListDto.getSureExpectAmountAUD() + "", cellFormat));
					sheet.addCell(new Label(30, i, commissionOrderListDto.getGst() + "", cellFormat));
					sheet.addCell(new Label(31, i, commissionOrderListDto.getDeductGst() + "", cellFormat));
					sheet.addCell(new Label(32, i, commissionOrderListDto.getSchoolPaymentAmount() + "", cellFormat));
					if (ObjectUtil.isNotNull(commissionOrderListDto.getSchoolPaymentDate()))
					sheet.addCell(new Label(33, i, sdf.format(commissionOrderListDto.getSchoolPaymentDate()) + "", cellFormat));
					sheet.addCell(new Label(34, i, commissionOrderListDto.getInvoiceNumber() + "", cellFormat));
					if (ObjectUtil.isNotNull(commissionOrderListDto.getZyDate()))
					sheet.addCell(new Label(35, i, sdf.format(commissionOrderListDto.getZyDate()) + "", cellFormat));
					// sub
					String subagencyName = subagencyService.getSubagencyByServiceOrderId(commissionOrderListDto.getServiceOrderId());
					if (StringUtil.isNotEmpty(subagencyName)) {
						sheet.addCell(new Label(36, i, subagencyName, cellFormat));
					}
					sheet.addCell(new Label(37, i, sdf.format(commissionOrderListDto.getBonus()), cellFormat));
					if (ObjectUtil.isNotNull(commissionOrderListDto.getBonusDate())) {
						sheet.addCell(new Label(38, i, sdf.format(commissionOrderListDto.getBonusDate()), cellFormat));
					}
					sheet.addCell(new Label(39, i, commissionOrderListDto.getVerifyCode(), cellFormat));
					String adviserName = adviserMap.get(commissionOrderListDto.getAdviserId());
					if (StringUtil.isNotEmpty(adviserName)) {
						sheet.addCell(new Label(41, i, adviserName + "", cellFormat));
					}
					sheet.addCell(new Label(42, i, commissionOrderListDto.getState() + "", cellFormat));
					if (ObjectUtil.isNotNull(commissionOrderListDto.getKjApprovalDate())) {
						sheet.addCell(new Label(43, i, sdf.format(commissionOrderListDto.getKjApprovalDate()) + "", cellFormat));
					}
					if (StringUtil.isNotEmpty(commissionOrderListDto.getRemarks())) {
						sheet.addCell(new Label(44, i, commissionOrderListDto.getRemarks() + "", cellFormat));
					}
					i++;
				}
				wbe.write();
				wbe.close();
			}
			if (getKjId(request) != null) {
				if (regionService.isCN(_regionId)) {
					os = response.getOutputStream();
					try {
						is = this.getClass().getResourceAsStream("/CommissionOrderTemplateCNY.xls");
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
					for (CommissionOrderListDTO commissionOrderListDto : commissionOrderList) {
						sheet.addCell(new Label(0, i, "CS" + commissionOrderListDto.getId(), cellFormat));
						sheet.addCell(new Label(1, i, sdf.format(commissionOrderListDto.getGmtCreate()), cellFormat));
						if (commissionOrderListDto.getReceiveDate() != null)
							sheet.addCell(new Label(2, i, sdf.format(commissionOrderListDto.getReceiveDate()), cellFormat));
						if (commissionOrderListDto.getApplicant() != null)
							sheet.addCell(new Label(3, i, commissionOrderListDto.getApplicant().getFirstname() + " "
									+ commissionOrderListDto.getApplicant().getSurname(), cellFormat));
						sheet.addCell(new Label(4, i, commissionOrderListDto.getStudentCode(), cellFormat));
						if (commissionOrderListDto.getBirthday() != null)
							sheet.addCell(new Label(5, i, sdf.format(commissionOrderListDto.getBirthday()), cellFormat));
						if (commissionOrderListDto.getReceiveType() != null)
							sheet.addCell(new Label(6, i, commissionOrderListDto.getReceiveType().getName() + "", cellFormat));
						if (commissionOrderListDto.getService() != null)
							sheet.addCell(new Label(7, i, commissionOrderListDto.getService().getName(), cellFormat));
						sheet.addCell(new Label(8, i, commissionOrderListDto.isSettle() + "", cellFormat));
						if (commissionOrderListDto.getSchool() != null) {
							sheet.addCell(new Label(9, i, commissionOrderListDto.getSchool().getName() + "", cellFormat));
							sheet.addCell(new Label(13, i, commissionOrderListDto.getSchool().getSubject() + "", cellFormat));
						}
						if (commissionOrderListDto.getSchoolInstitutionListDTO() != null){
							sheet.addCell(new Label(9, i, commissionOrderListDto.getSchoolInstitutionListDTO().getInstitutionTradingName() , cellFormat));
							//if (commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO() != null)
							sheet.addCell(new Label(10, i, commissionOrderListDto.getSchoolInstitutionListDTO().getInstitutionName(), cellFormat));
							if (commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO() != null){
								sheet.addCell(new Label(11, i, commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO().getName(), cellFormat));
								sheet.addCell(new Label(12, i, commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO().getState(), cellFormat));
							}
							if (commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolCourseDO() != null)
								sheet.addCell(new Label(13, i, commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolCourseDO().getCourseName(), cellFormat));
						}
						if (commissionOrderListDto.getStartDate() != null)
							sheet.addCell(new Label(14, i, sdf.format(commissionOrderListDto.getStartDate()), cellFormat));
						if (commissionOrderListDto.getEndDate() != null)
							sheet.addCell(new Label(15, i, sdf.format(commissionOrderListDto.getEndDate()), cellFormat));
						if (commissionOrderListDto.getInstallmentDueDate() != null)
							sheet.addCell(
									new Label(16, i, sdf.format(commissionOrderListDto.getInstallmentDueDate()), cellFormat));
						if (commissionOrderListDto.getReceiveType() != null)
							sheet.addCell(new Label(17, i, commissionOrderListDto.getReceiveType().getName() + "", cellFormat));
						sheet.addCell(new Label(18, i, commissionOrderListDto.getTuitionFee() + "", cellFormat));
						sheet.addCell(new Label(19, i, commissionOrderListDto.getPerAmount() + "", cellFormat)); // .getPerTermTuitionFee()
						sheet.addCell(new Label(20, i, commissionOrderListDto.getTotalPerAmountCNY() + "", cellFormat));
						sheet.addCell(new Label(21, i, commissionOrderListDto.getTotalPerAmountAUD() + "", cellFormat));
						sheet.addCell(new Label(22, i, commissionOrderListDto.getTotalAmountCNY() + "", cellFormat));
						sheet.addCell(new Label(23, i, commissionOrderListDto.getTotalAmountAUD() + "", cellFormat));
						sheet.addCell(new Label(24, i, commissionOrderListDto.getCurrency(), cellFormat));
						sheet.addCell(new Label(25, i, commissionOrderListDto.getExchangeRate() + "", cellFormat));
						sheet.addCell(new Label(26, i, commissionOrderListDto.getAmountCNY() + "", cellFormat));
						sheet.addCell(new Label(27, i, commissionOrderListDto.getAmountAUD() + "", cellFormat));
						sheet.addCell(new Label(28, i, commissionOrderListDto.getExpectAmountAUD() + "", cellFormat));
						if (commissionOrderListDto.isSettle())
							sheet.addCell(new Label(29, i, commissionOrderListDto.getExpectAmountAUD() + "", cellFormat));
						else
							sheet.addCell(new Label(29, i, commissionOrderListDto.getSureExpectAmountAUD() + "", cellFormat));
						sheet.addCell(new Label(30, i, commissionOrderListDto.getSchoolPaymentAmount() + "", cellFormat));
						//31待确定
						if (commissionOrderListDto.getSchoolPaymentDate() != null)
							sheet.addCell(
									new Label(32, i, sdf.format(commissionOrderListDto.getSchoolPaymentDate()), cellFormat));
						sheet.addCell(new Label(33, i, commissionOrderListDto.getInvoiceNumber(), cellFormat));
						if (commissionOrderListDto.getZyDate() != null)
							sheet.addCell(new Label(34, i, sdf.format(commissionOrderListDto.getZyDate()), cellFormat));
						if (commissionOrderListDto.getSubagency() != null)
							sheet.addCell(new Label(35, i, commissionOrderListDto.getSubagency().getName(), cellFormat));
						sheet.addCell(new Label(36, i, commissionOrderListDto.getBonus() + "", cellFormat));
						if (commissionOrderListDto.getBonusDate() != null)
							sheet.addCell(new Label(37, i, sdf.format(commissionOrderListDto.getBonusDate()), cellFormat));
						sheet.addCell(new Label(38, i, commissionOrderListDto.getBankCheck(), cellFormat));
						sheet.addCell(new Label(39, i, commissionOrderListDto.isChecked() + "", cellFormat));
						if (commissionOrderListDto.getAdviser() != null)
							sheet.addCell(new Label(40, i, commissionOrderListDto.getAdviser().getName(), cellFormat));
						if (commissionOrderListDto.getState() != null)
							sheet.addCell(new Label(41, i, getStateStr(commissionOrderListDto.getState()), cellFormat));
						if (commissionOrderListDto.getKjApprovalDate() != null)
							sheet.addCell(new Label(42, i, sdf.format(commissionOrderListDto.getKjApprovalDate()), cellFormat));
						sheet.addCell(new Label(43, i, commissionOrderListDto.getRemarks(), cellFormat));
						ServiceOrderDTO serviceOrderDTO = serviceOrderService
								.getServiceOrderById(commissionOrderListDto.getServiceOrderId());
						sheet.addCell(new Label(44, i,
								serviceOrderDTO != null && serviceOrderDTO.getRemarks() != null ? serviceOrderDTO.getRemarks()
										: "",
								cellFormat));
						i++;
					}
					wbe.write();
					wbe.close();

				} else {

					os = response.getOutputStream();
					try {
						is = this.getClass().getResourceAsStream("/CommissionOrderTemplate.xls");
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
					for (CommissionOrderListDTO commissionOrderListDto : commissionOrderList) {
						sheet.addCell(new Label(0, i, "CS" + commissionOrderListDto.getId(), cellFormat));
						sheet.addCell(new Label(1, i, sdf.format(commissionOrderListDto.getGmtCreate()), cellFormat));
						if (commissionOrderListDto.getReceiveDate() != null)
							sheet.addCell(new Label(2, i, sdf.format(commissionOrderListDto.getReceiveDate()), cellFormat));
						if (commissionOrderListDto.getApplicant() != null)
							sheet.addCell(new Label(3, i, commissionOrderListDto.getApplicant().getFirstname() + " "
									+ commissionOrderListDto.getApplicant().getSurname(), cellFormat));
						sheet.addCell(new Label(4, i, commissionOrderListDto.getStudentCode(), cellFormat));
						if (commissionOrderListDto.getBirthday() != null)
							sheet.addCell(new Label(5, i, sdf.format(commissionOrderListDto.getBirthday()), cellFormat));
						if (commissionOrderListDto.getReceiveType() != null)
							sheet.addCell(new Label(6, i, commissionOrderListDto.getReceiveType().getName() + "", cellFormat));
						if (commissionOrderListDto.getService() != null)
							sheet.addCell(new Label(7, i, commissionOrderListDto.getService().getName(), cellFormat));
						sheet.addCell(new Label(8, i, commissionOrderListDto.isSettle() + "", cellFormat));
						if (commissionOrderListDto.getSchool() != null) {
							sheet.addCell(new Label(9, i, commissionOrderListDto.getSchool().getName() + "", cellFormat));
							sheet.addCell(new Label(13, i, commissionOrderListDto.getSchool().getSubject() + "", cellFormat));
						}
						if (commissionOrderListDto.getSchoolInstitutionListDTO() != null){
							sheet.addCell(new Label(9, i, commissionOrderListDto.getSchoolInstitutionListDTO().getInstitutionTradingName() , cellFormat));
							//if (commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO() != null)
							sheet.addCell(new Label(10, i, commissionOrderListDto.getSchoolInstitutionListDTO().getInstitutionName(), cellFormat));
							if (commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO() != null){
								sheet.addCell(new Label(11, i, commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO().getName(), cellFormat));
								sheet.addCell(new Label(12, i, commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO().getState(), cellFormat));
							}
							if (commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolCourseDO() != null)
								sheet.addCell(new Label(13, i, commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolCourseDO().getCourseName(), cellFormat));
						}
						if (commissionOrderListDto.getStartDate() != null)
							sheet.addCell(new Label(14, i, sdf.format(commissionOrderListDto.getStartDate()), cellFormat));
						if (commissionOrderListDto.getEndDate() != null)
							sheet.addCell(new Label(15, i, sdf.format(commissionOrderListDto.getEndDate()), cellFormat));
						if (commissionOrderListDto.getInstallmentDueDate() != null)
							sheet.addCell(
									new Label(16, i, sdf.format(commissionOrderListDto.getInstallmentDueDate()), cellFormat));
						if (commissionOrderListDto.getReceiveType() != null)
							sheet.addCell(new Label(17, i, commissionOrderListDto.getReceiveType().getName() + "", cellFormat));
						sheet.addCell(new Label(18, i, commissionOrderListDto.getTuitionFee() + "", cellFormat));
						sheet.addCell(new Label(19, i, commissionOrderListDto.getPerAmount() + "", cellFormat)); // .getPerTermTuitionFee()
						sheet.addCell(new Label(20, i, commissionOrderListDto.getTotalPerAmountCNY() + "", cellFormat));
						sheet.addCell(new Label(21, i, commissionOrderListDto.getTotalPerAmountAUD() + "", cellFormat));
						sheet.addCell(new Label(22, i, commissionOrderListDto.getTotalAmountCNY() + "", cellFormat));
						sheet.addCell(new Label(23, i, commissionOrderListDto.getTotalAmountAUD() + "", cellFormat));
						sheet.addCell(new Label(24, i, commissionOrderListDto.getCurrency(), cellFormat));
						sheet.addCell(new Label(25, i, commissionOrderListDto.getExchangeRate() + "", cellFormat));
						sheet.addCell(new Label(26, i, commissionOrderListDto.getAmountCNY() + "", cellFormat));
						sheet.addCell(new Label(27, i, commissionOrderListDto.getAmountAUD() + "", cellFormat));
						sheet.addCell(new Label(28, i, commissionOrderListDto.getExpectAmountAUD() + "", cellFormat));
						if (commissionOrderListDto.isSettle())
							sheet.addCell(new Label(29, i, commissionOrderListDto.getExpectAmountAUD() + "", cellFormat));
						else
							sheet.addCell(new Label(29, i, commissionOrderListDto.getSureExpectAmountAUD() + "", cellFormat));
						sheet.addCell(new Label(30, i, commissionOrderListDto.getGst() + "", cellFormat));
						sheet.addCell(new Label(31, i, commissionOrderListDto.getDeductGst() + "", cellFormat));
						sheet.addCell(new Label(32, i, commissionOrderListDto.getSchoolPaymentAmount() + "", cellFormat));
						if (commissionOrderListDto.getSchoolPaymentDate() != null)
							sheet.addCell(
									new Label(33, i, sdf.format(commissionOrderListDto.getSchoolPaymentDate()), cellFormat));
						sheet.addCell(new Label(34, i, commissionOrderListDto.getInvoiceNumber(), cellFormat));
						if (commissionOrderListDto.getZyDate() != null)
							sheet.addCell(new Label(35, i, sdf.format(commissionOrderListDto.getZyDate()), cellFormat));
						if (commissionOrderListDto.getSubagency() != null)
							sheet.addCell(new Label(36, i, commissionOrderListDto.getSubagency().getName(), cellFormat));
						sheet.addCell(new Label(37, i, commissionOrderListDto.getBonus() + "", cellFormat));
						if (commissionOrderListDto.getBonusDate() != null)
							sheet.addCell(new Label(38, i, sdf.format(commissionOrderListDto.getBonusDate()), cellFormat));
						sheet.addCell(new Label(39, i, commissionOrderListDto.getBankCheck(), cellFormat));
						sheet.addCell(new Label(40, i, commissionOrderListDto.isChecked() + "", cellFormat));
						if (commissionOrderListDto.getAdviser() != null)
							sheet.addCell(new Label(41, i, commissionOrderListDto.getAdviser().getName(), cellFormat));
						if (commissionOrderListDto.getState() != null)
							sheet.addCell(new Label(42, i, getStateStr(commissionOrderListDto.getState()), cellFormat));
						if (commissionOrderListDto.getKjApprovalDate() != null)
							sheet.addCell(new Label(43, i, sdf.format(commissionOrderListDto.getKjApprovalDate()), cellFormat));
						sheet.addCell(new Label(44, i, commissionOrderListDto.getRemarks(), cellFormat));
						ServiceOrderDTO serviceOrderDTO = serviceOrderService
								.getServiceOrderById(commissionOrderListDto.getServiceOrderId());
						sheet.addCell(new Label(45, i,
								serviceOrderDTO != null && serviceOrderDTO.getRemarks() != null ? serviceOrderDTO.getRemarks()
										: "",
								cellFormat));
						i++;
					}
					wbe.write();
					wbe.close();

				}
			}





		} catch (Exception e) {
			e.printStackTrace();
			return;
		}finally{
			try {
				if (is != null)
					is.close();
				System.out.println("is is close");
			} catch (IOException e) {
				System.out.println("is is close 出现 异常:");
				e.printStackTrace();
			}
			try {
				if (os != null)
					os.close();
				System.out.println("os is close");
			} catch (IOException e) {
				System.out.println("os is close 出现 异常:");
				e.printStackTrace();
			}
			if (wb != null)
				wb.close();
			System.out.println("wb is close");
		}

	}

	@RequestMapping(value = "/down_V2", method = RequestMethod.GET)
	@ResponseBody
	public Response<String> down_V2(@RequestParam(value = "id", required = false) Integer id,
									@RequestParam(value = "regionId", required = false) Integer regionId,
									@RequestParam(value = "maraId", required = false) Integer maraId,
									@RequestParam(value = "adviserId", required = false) Integer adviserId,
									@RequestParam(value = "officialId", required = false) Integer officialId,
									@RequestParam(value = "userId", required = false) Integer userId,
									@RequestParam(value = "userName", required = false) String name,
									@RequestParam(value = "applicantName", required = false) String applicantName,
									@RequestParam(value = "phone", required = false) String phone,
									@RequestParam(value = "wechatUsername", required = false) String wechatUsername,
									@RequestParam(value = "schoolId", required = false) Integer schoolId,
									@RequestParam(value = "isSettle", required = false) Boolean isSettle,
									@RequestParam(value = "state", required = false) String state,
									@RequestParam(value = "commissionState", required = false) String commissionState,
									@RequestParam(value = "startKjApprovalDate", required = false) String startKjApprovalDate,
									@RequestParam(value = "endKjApprovalDate", required = false) String endKjApprovalDate,
									@RequestParam(value = "startDate", required = false) String startDate,
									@RequestParam(value = "endDate", required = false) String endDate,
									@RequestParam(value = "startInvoiceCreate", required = false) String startInvoiceCreate,
									@RequestParam(value = "endInvoiceCreate", required = false) String endInvoiceCreate,
									HttpServletRequest request, HttpServletResponse response) {

		Integer newMaraId = getMaraId(request);
		if (newMaraId != null)
			maraId = newMaraId;
		Integer newAdviserId = getAdviserId(request);
		if (newAdviserId != null)
			adviserId = newAdviserId;
		Integer newOfficialId = getOfficialId(request);
		if (newOfficialId != null)
			officialId = newOfficialId;
		Integer kjId = getKjId(request);

		List<String> commissionStateList = null;
		if (StringUtil.isNotEmpty(commissionState))
			commissionStateList = Arrays.asList(commissionState.split(","));

		// 会计角色过滤状态
		Boolean isYzyAndYjy = false;
		List<String> stateList = new ArrayList<>();
		if (state == null && getKjId(request) != null) {
			stateList.add(ReviewKjStateEnum.REVIEW.toString());
			stateList.add(ReviewKjStateEnum.FINISH.toString());
			stateList.add(ReviewKjStateEnum.COMPLETE.toString());
//			stateList.add(ReviewKjStateEnum.CLOSE.toString());
			if (CommissionStateEnum.YZY.toString().equalsIgnoreCase(commissionState)) {
				commissionStateList = null;
				isYzyAndYjy = true;
			}
		} else if (state == null)
			stateList = null;
		else
			stateList.add(state);

		List<Integer> regionIdList = null;
		if (regionId != null && regionId > 0)
			regionIdList = ListUtil.buildArrayList(regionId);

		InputStream is = null;
		OutputStream os = null;
		Workbook wb = null;
		try {

			// 处理顾问管理员
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo != null && "GW".equalsIgnoreCase(adminUserLoginInfo.getApList())
					&& adminUserLoginInfo.getRegionId() != null && adminUserLoginInfo.getRegionId() > 0) {
				List<RegionDTO> regionList = regionService.listRegion(adminUserLoginInfo.getRegionId());
				regionIdList = ListUtil.buildArrayList(adminUserLoginInfo.getRegionId());
				for (RegionDTO region : regionList)
					regionIdList.add(region.getId());
				adviserId = null;
			}

			List<CommissionOrderListDTO> commissionOrderList = commissionOrderService.listCommissionOrder(id,
					regionIdList, maraId, adviserId, officialId, userId, name, applicantName, phone, wechatUsername, schoolId,
					isSettle, stateList, commissionStateList, startKjApprovalDate, endKjApprovalDate,startDate,endDate,
					startInvoiceCreate, endInvoiceCreate, isYzyAndYjy, state, 0, 9999, null);

			if (commissionOrderList == null)
				throw new Exception("查询佣金订单数据错误!");
			System.out.println("导出佣金订单数据量:" + commissionOrderList.size());

			int _regionId;
			if (ObjectUtil.isNotNull(adviserId) && adviserId > 0)
				_regionId = adviserService.getAdviserById(adviserId).getRegionId();
			else {
				_regionId = 0;
			}
			// 获取token
			Map<String, Object> tokenMap = wxWorkService.getToken(WXWorkAPI.SECRET_EXCEL);
			if ((int)tokenMap.get("errcode") != 0){
				throw  new RuntimeException( tokenMap.get("errmsg").toString());
			}
			String customerToken = (String) tokenMap.get("access_token");

			List<AdviserDTO> adviserDTOS = adviserService.listAdviser(null, null, 0, 1000);
			Map<Integer, String> adviserMap = adviserDTOS.stream().collect(Collectors.toMap(AdviserDTO::getId, AdviserDTO::getName, (v1, v2) -> v2));

			// 创建表格
			String setupExcelAccessToken = WXWorkAPI.SETUP_EXCEL.replace("ACCESS_TOKEN", customerToken);
			final JSONObject[] parm = {new JSONObject()};
			parm[0].put("doc_type", 10);
			parm[0].put("doc_name", "commission_order_information-" + sdf.format(new Date()));
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
			jsonObjectProperties.put("title", "留学佣金订单导出信息");
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
			log.info("更新字段-------------------" + accessToken2);
			final JSONObject[] parm2 = {new JSONObject()};
			parm2[0].put("docid", docid);
			parm2[0].put("sheet_id", sheetId);
			// 添加字段标题title
			List<String> exlceTitles = buildExlceTitle(_regionId);
			List<String> exlceTitleNumberList = new ArrayList<>();
			List<JSONObject> fieldList = new ArrayList<>();
			exlceTitleNumberList.add("月奖");
			exlceTitleNumberList.add("学校支付金额");
			if (!regionService.isCN(_regionId)) {
				exlceTitleNumberList.add("Deduct GST");
				exlceTitleNumberList.add("GST");
			}
			exlceTitleNumberList.add("确认预收业绩");
			exlceTitleNumberList.add("Commission");
			exlceTitleNumberList.add("本次收款澳币");
			exlceTitleNumberList.add("本次收款人民币");
			exlceTitleNumberList.add("创建订单时汇率");
			exlceTitleNumberList.add("总计已收澳币");
			exlceTitleNumberList.add("总计已收人民币");
			exlceTitleNumberList.add("总计应收澳币");
			exlceTitleNumberList.add("总计应收人民币");
			for (String exlceTitle : exlceTitles) {
				JSONObject jsonObjectField = new JSONObject();
				jsonObjectField.put("field_title", exlceTitle);
				if (exlceTitleNumberList.contains(exlceTitle)) {
					jsonObjectField.put("field_type", "FIELD_TYPE_NUMBER");
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("decimal_places", 2);
					jsonObject.put("use_separate", false);
					jsonObjectField.put("property_number", jsonObject);
				} else {
					jsonObjectField.put("field_type", "FIELD_TYPE_TEXT");
				}
				fieldList.add(jsonObjectField);
			}
			parm2[0].put("fields", fieldList);
			JSONObject jsonObject2 = WXWorkAPI.sendPostBody_Map(accessToken2, parm2[0]);
			log.info("jsonObject2-------------" + jsonObject2.toString());

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
				List<CommissionOrderListDTO> finalServiceOrderList = commissionOrderList;
				Thread thread1 = new Thread(() -> {
					try {
						// 线程1的任务
						if ("0".equals(jsonObject1.get("errcode").toString())) {
							// 添加行记录
							String accessTokenJiLu = WXWorkAPI.INSERT_ROW.replace("ACCESS_TOKEN", customerToken);
							final JSONObject[] parmJiLu = {new JSONObject()};
							parmJiLu[0].put("docid", docid);
							parmJiLu[0].put("sheet_id", sheetId);
							for (CommissionOrderListDTO serviceOrderDTO : finalServiceOrderList) {
								JSONObject jsonObjectFILEDTITLE = buileExcelJsonObject(serviceOrderDTO, adviserMap, _regionId);
								List<JSONObject> recordsList = new ArrayList<>();
								JSONObject jsonObjectValue = new JSONObject();
								jsonObjectValue.put("values", jsonObjectFILEDTITLE);
								recordsList.add(jsonObjectValue);
								parmJiLu[0].put("records", recordsList);
								log.info("请求体--------------------------" + JSONObject.toJSONString(parmJiLu[0]));
								JSONObject jsonObjectJiLu = WXWorkAPI.sendPostBody_Map(accessTokenJiLu, parmJiLu[0]);
								log.info(accessTokenJiLu);
								log.info("setupExcelJsonObject-------------" + jsonObjectJiLu.toString());
							}
						}
					} catch (Exception e) {
						// 处理异常，例如记录日志
						e.printStackTrace();
					}
				});
				thread1.start();
			}
			StringBuilder htmlBuilder = new StringBuilder();
			htmlBuilder.append("<a href=\"");
			htmlBuilder.append(url + "\""); // 插入链接的URL
			htmlBuilder.append(" target=\"_blank");
			htmlBuilder.append("\">");
			htmlBuilder.append("点击打开Excel链接"); // 插入链接的显示文本
			htmlBuilder.append("</a>");
			WXWorkAPI.sendShareLinkMsg(url, adminUserLoginInfo.getUsername(), "导出留学佣金订单信息");
			return new Response<>(0, "生成Excel成功， excel链接为：" + htmlBuilder);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new Response<>(0, "生成Excel成功， excel链接为：");
	}




//	@RequestMapping(value = "/down_V2", method = RequestMethod.GET)
//	@ResponseBody
//	public Response<String> down_V2(@RequestParam(value = "id", required = false) Integer id,
//					 @RequestParam(value = "regionId", required = false) Integer regionId,
//					 @RequestParam(value = "maraId", required = false) Integer maraId,
//					 @RequestParam(value = "adviserId", required = false) Integer adviserId,
//					 @RequestParam(value = "officialId", required = false) Integer officialId,
//					 @RequestParam(value = "userId", required = false) Integer userId,
//					 @RequestParam(value = "userName", required = false) String name,
//					 @RequestParam(value = "applicantName", required = false) String applicantName,
//					 @RequestParam(value = "phone", required = false) String phone,
//					 @RequestParam(value = "wechatUsername", required = false) String wechatUsername,
//					 @RequestParam(value = "schoolId", required = false) Integer schoolId,
//					 @RequestParam(value = "isSettle", required = false) Boolean isSettle,
//					 @RequestParam(value = "state", required = false) String state,
//					 @RequestParam(value = "commissionState", required = false) String commissionState,
//					 @RequestParam(value = "startKjApprovalDate", required = false) String startKjApprovalDate,
//					 @RequestParam(value = "endKjApprovalDate", required = false) String endKjApprovalDate,
//					 @RequestParam(value = "startDate", required = false) String startDate,
//					 @RequestParam(value = "endDate", required = false) String endDate,
//					 @RequestParam(value = "startInvoiceCreate", required = false) String startInvoiceCreate,
//					 @RequestParam(value = "endInvoiceCreate", required = false) String endInvoiceCreate,
//					 HttpServletRequest request, HttpServletResponse response) {
//
//		Integer newMaraId = getMaraId(request);
//		if (newMaraId != null)
//			maraId = newMaraId;
//		Integer newAdviserId = getAdviserId(request);
//		if (newAdviserId != null)
//			adviserId = newAdviserId;
//		Integer newOfficialId = getOfficialId(request);
//		if (newOfficialId != null)
//			officialId = newOfficialId;
//		Integer kjId = getKjId(request);
//
//		List<String> commissionStateList = null;
//		if (StringUtil.isNotEmpty(commissionState))
//			commissionStateList = Arrays.asList(commissionState.split(","));
//
//		// 会计角色过滤状态
//		Boolean isYzyAndYjy = false;
//		List<String> stateList = new ArrayList<>();
//		if (state == null && getKjId(request) != null) {
//			stateList.add(ReviewKjStateEnum.REVIEW.toString());
//			stateList.add(ReviewKjStateEnum.FINISH.toString());
//			stateList.add(ReviewKjStateEnum.COMPLETE.toString());
////			stateList.add(ReviewKjStateEnum.CLOSE.toString());
//			if (CommissionStateEnum.YZY.toString().equalsIgnoreCase(commissionState)) {
//				commissionStateList = null;
//				isYzyAndYjy = true;
//			}
//		} else if (state == null)
//			stateList = null;
//		else
//			stateList.add(state);
//
//		List<Integer> regionIdList = null;
//		if (regionId != null && regionId > 0)
//			regionIdList = ListUtil.buildArrayList(regionId);
//
//		InputStream is = null;
//		OutputStream os = null;
//		Workbook wb = null;
//		try {
//
//			// 处理顾问管理员
//			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
//			if (adminUserLoginInfo != null && "GW".equalsIgnoreCase(adminUserLoginInfo.getApList())
//					&& adminUserLoginInfo.getRegionId() != null && adminUserLoginInfo.getRegionId() > 0) {
//				List<RegionDTO> regionList = regionService.listRegion(adminUserLoginInfo.getRegionId());
//				regionIdList = ListUtil.buildArrayList(adminUserLoginInfo.getRegionId());
//				for (RegionDTO region : regionList)
//					regionIdList.add(region.getId());
//				adviserId = null;
//			}
//
////			response.reset();// 清空输出流
////			String tableName = "commission_order_information";
////			response.setHeader("Content-disposition",
////					"attachment; filename=" + new String(tableName.getBytes("GB2312"), "8859_1") + ".xls");
////			response.setContentType("application/msexcel");
//
////			Date _startKjApprovalDate = null;
////			if (startKjApprovalDate != null)
////				_startKjApprovalDate = new Date(Long.parseLong(startKjApprovalDate));
////			Date _endKjApprovalDate = null;
////			if (endKjApprovalDate != null)
////				_endKjApprovalDate = new Date(Long.parseLong(endKjApprovalDate));
//
//			List<CommissionOrderListDTO> commissionOrderList = commissionOrderService.listCommissionOrder(id,
//					regionIdList, maraId, adviserId, officialId, userId, name, applicantName, phone, wechatUsername, schoolId,
//					isSettle, stateList, commissionStateList, startKjApprovalDate, endKjApprovalDate,startDate,endDate,
//					startInvoiceCreate, endInvoiceCreate, isYzyAndYjy, state, 0, 9999, null);
//
//			if (commissionOrderList == null)
//				throw new Exception("查询佣金订单数据错误!");
//			System.out.println("导出佣金订单数据量:" + commissionOrderList.size());
//
//			int _regionId;
//			if (ObjectUtil.isNotNull(adviserId) && adviserId > 0)
//				_regionId = adviserService.getAdviserById(adviserId).getRegionId();
//            else {
//                _regionId = 0;
//            }
//				// 获取token
//				Map<String, Object> tokenMap = wxWorkService.getToken(WXWorkAPI.SECRET_EXCEL);
//				if ((int)tokenMap.get("errcode") != 0){
//					throw  new RuntimeException( tokenMap.get("errmsg").toString());
//				}
//				String customerToken = (String) tokenMap.get("access_token");
//
//				List<AdviserDTO> adviserDTOS = adviserService.listAdviser(null, null, 0, 1000);
//				Map<Integer, String> adviserMap = adviserDTOS.stream().collect(Collectors.toMap(AdviserDTO::getId, AdviserDTO::getName, (v1, v2) -> v2));
//
//				// 创建表格
//				String setupExcelAccessToken = WXWorkAPI.SETUP_EXCEL.replace("ACCESS_TOKEN", customerToken);
//				final JSONObject[] parm = {new JSONObject()};
//				parm[0].put("doc_type", 4);
//				parm[0].put("doc_name", "ServiceOrderTemplate-" + sdf.format(new Date()));
//				String[] userIds = {"XuShiYi"};
//				parm[0].put("admin_users", userIds);
//				JSONObject setupExcelJsonObject = WXWorkAPI.sendPostBody_Map(setupExcelAccessToken, parm[0]);
//				String url = "";
//				if ("0".equals(setupExcelJsonObject.get("errcode").toString())) {
//					url = setupExcelJsonObject.get("url").toString();
//					String docId = setupExcelJsonObject.get("docid").toString();
//					SetupExcelDO setupExcelDO = new SetupExcelDO();
//					setupExcelDO.setUrl(url);
//					setupExcelDO.setDocId(docId);
//					String informationExcelAccessToken = WXWorkAPI.INFORMATION_EXCEL.replace("ACCESS_TOKEN", customerToken);
//					parm[0] = new JSONObject();
//					parm[0].put("docid", docId);
//					JSONObject informationExcelJsonObject = WXWorkAPI.sendPostBody_Map(informationExcelAccessToken, parm[0]);
//					List<CommissionOrderListDTO> finalServiceOrderList = commissionOrderList;
//					Thread thread1 = new Thread(() -> {
//						try {
//							// 线程1的任务
//							if ("0".equals(informationExcelJsonObject.get("errcode").toString())) {
//								JSONArray propertiesObjects = JSONArray.parseArray(JSONObject.toJSONString(informationExcelJsonObject.get("properties")));
//								Iterator<Object> iterator = propertiesObjects.iterator();
//								String sheetId = JSONObject.parseObject(iterator.next().toString()).get("sheet_id").toString();
//								setupExcelDO.setSheetId(sheetId);
//								int i = wxWorkService.addExcel(setupExcelDO);
//								if (i > 0) {
//									String redactExcelAccessToken = WXWorkAPI.REDACT_EXCEL.replace("ACCESS_TOKEN", customerToken);
//									parm[0] = new JSONObject();
//									parm[0].put("docid", docId);
//
//									List<JSONObject> requests = new ArrayList<>();
//									JSONObject requestsJson = new JSONObject();
//									JSONObject updateRangeRequest = new JSONObject();
//									JSONObject gridData = new JSONObject();
//									int count = 0;
//
//									List<String> excelTitle = new ArrayList<>();
//									excelTitle.add("订单ID");
//									excelTitle.add("佣金订单创建日期");
//									excelTitle.add("客户支付日期");
//									excelTitle.add("Student Name");
//									excelTitle.add("Student ID");
//									excelTitle.add("生日");
//									excelTitle.add("收款方式");
//									excelTitle.add("服务项目");
//									excelTitle.add("是否提前扣佣");
//									excelTitle.add("Institute/Institution Trading Name");
//									excelTitle.add("Institution Name");
//									excelTitle.add("Location Name");
//									excelTitle.add("State");
//									excelTitle.add("Course Name");
//									excelTitle.add("Course Start Date");
//									excelTitle.add("Course End Date");
//									excelTitle.add("Installment Due Date");
//									excelTitle.add("收款方式");
//									excelTitle.add("Total Tuition Fee");
//									excelTitle.add("Per Tuition Fee per Installment");
//									excelTitle.add("总计应收人民币");
//									excelTitle.add("总计应收澳币");
//									excelTitle.add("总计已收人民币");
//									excelTitle.add("总计已收澳币");
//									excelTitle.add("本次支付币种");
//									excelTitle.add("创建订单时汇率");
//									excelTitle.add("本次收款人民币");
//									excelTitle.add("本次收款澳币");
//									excelTitle.add("Commission");
//									excelTitle.add("确认预收业绩");
//									if (!regionService.isCN(_regionId)) {
//										excelTitle.add("GST");
//										excelTitle.add("Deduct GST");
//									}
//									excelTitle.add("学校支付金额");
//									excelTitle.add("学校支付时间");
//									excelTitle.add("Invoice NO.");
//									excelTitle.add("追佣时间");
//									excelTitle.add("Subagency");
//									excelTitle.add("月奖");
//									excelTitle.add("月奖支付时间");
//									excelTitle.add("银行对账字段");
//									excelTitle.add("顾问");
//									excelTitle.add("状态");
//									excelTitle.add("财务审核时间");
//									excelTitle.add("佣金备注");
//									excelTitle.add("服务备注");
//
//									for (CommissionOrderListDTO serviceOrderDTO : finalServiceOrderList) {
//										if (count == 0) {
//											gridData.put("start_row", 0);
//											gridData.put("start_column", 0);
//											List<JSONObject> rows = new ArrayList<>();
//											for (String title : excelTitle) {
//												JSONObject jsonObject = new JSONObject();
//												JSONObject text = new JSONObject();
//												text.put("text", title);
//												jsonObject.put("cell_value", text);
//												rows.add(jsonObject);
//											}
//											List<JSONObject> objects = new ArrayList<>();
//											JSONObject rowsValue = new JSONObject();
//											rowsValue.put("values", rows);
//											objects.add(rowsValue);
//											gridData.put("rows", objects);
//											updateRangeRequest.put("sheet_id", sheetId);
//											updateRangeRequest.put("grid_data", gridData);
//											requestsJson.put("update_range_request", updateRangeRequest);
//											requests.add(requestsJson);
//											parm[0].put("requests", requests);
//											count++;
//											WXWorkAPI.sendPostBody_Map(redactExcelAccessToken, parm[0]);
//											parm[0] = new JSONObject();
//											requests.remove(0);
//										}
//										parm[0].put("docid", docId);
//										gridData.put("start_row", count);
//										gridData.put("start_column", 0);
//										List<JSONObject> rows = build(serviceOrderDTO, adviserMap, _regionId);
//										List<JSONObject> objects = new ArrayList<>();
//										JSONObject rowsValue = new JSONObject();
//										rowsValue.put("values", rows);
//										objects.add(rowsValue);
//										gridData.put("rows", objects);
//										updateRangeRequest.put("sheet_id", sheetId);
//										updateRangeRequest.put("grid_data", gridData);
//										requestsJson.put("update_range_request", updateRangeRequest);
//										requests.add(requestsJson);
//										parm[0].put("requests", requests);
//										count++;
//										WXWorkAPI.sendPostBody_Map(redactExcelAccessToken, parm[0]);
//										parm[0] = new JSONObject();
//										requests.remove(0);
//									}
//								}
//							}
//						} catch (Exception e) {
//							// 处理异常，例如记录日志
//							e.printStackTrace();
//						}
//					});
//					thread1.start();
//				}
//				StringBuilder htmlBuilder = new StringBuilder();
//				htmlBuilder.append("<a href=\"");
//				htmlBuilder.append(url + "\""); // 插入链接的URL
//				htmlBuilder.append(" target=\"_blank");
//				htmlBuilder.append("\">");
//				htmlBuilder.append("点击打开Excel链接"); // 插入链接的显示文本
//				htmlBuilder.append("</a>");
//				WXWorkAPI.sendShareLinkMsg(url, adminUserLoginInfo.getUsername(), "导出留学佣金订单信息");
//				return new Response<>(0, "生成Excel成功， excel链接为：" + htmlBuilder);
////			}
////			if (getKjId(request) != null) {
////				if (regionService.isCN(_regionId)) {
////					os = response.getOutputStream();
////					try {
////						is = this.getClass().getResourceAsStream("/CommissionOrderTemplateCNY.xls");
////					} catch (Exception e) {
////						throw new Exception("模版不存在");
////					}
////					try {
////						wb = Workbook.getWorkbook(is);
////					} catch (Exception e) {
////						throw new Exception("模版格式不支持");
////					}
////					WorkbookSettings settings = new WorkbookSettings();
////					settings.setWriteAccess(null);
////					jxl.write.WritableWorkbook wbe = Workbook.createWorkbook(os, wb, settings);
////
////					if (wbe == null) {
////						System.out.println("wbe is null !os=" + os + ",wb" + wb);
////					} else {
////						System.out.println("wbe not null !os=" + os + ",wb" + wb);
////					}
////					WritableSheet sheet = wbe.getSheet(0);
////					WritableCellFormat cellFormat = new WritableCellFormat();
////
////					int i = 1;
////					for (CommissionOrderListDTO commissionOrderListDto : commissionOrderList) {
////						sheet.addCell(new Label(0, i, "CS" + commissionOrderListDto.getId(), cellFormat));
////						sheet.addCell(new Label(1, i, sdf.format(commissionOrderListDto.getGmtCreate()), cellFormat));
////						if (commissionOrderListDto.getReceiveDate() != null)
////							sheet.addCell(new Label(2, i, sdf.format(commissionOrderListDto.getReceiveDate()), cellFormat));
////						if (commissionOrderListDto.getApplicant() != null)
////							sheet.addCell(new Label(3, i, commissionOrderListDto.getApplicant().getFirstname() + " "
////									+ commissionOrderListDto.getApplicant().getSurname(), cellFormat));
////						sheet.addCell(new Label(4, i, commissionOrderListDto.getStudentCode(), cellFormat));
////						if (commissionOrderListDto.getBirthday() != null)
////							sheet.addCell(new Label(5, i, sdf.format(commissionOrderListDto.getBirthday()), cellFormat));
////						if (commissionOrderListDto.getReceiveType() != null)
////							sheet.addCell(new Label(6, i, commissionOrderListDto.getReceiveType().getName() + "", cellFormat));
////						if (commissionOrderListDto.getService() != null)
////							sheet.addCell(new Label(7, i, commissionOrderListDto.getService().getName(), cellFormat));
////						sheet.addCell(new Label(8, i, commissionOrderListDto.isSettle() + "", cellFormat));
////						if (commissionOrderListDto.getSchool() != null) {
////							sheet.addCell(new Label(9, i, commissionOrderListDto.getSchool().getName() + "", cellFormat));
////							sheet.addCell(new Label(13, i, commissionOrderListDto.getSchool().getSubject() + "", cellFormat));
////						}
////						if (commissionOrderListDto.getSchoolInstitutionListDTO() != null){
////							sheet.addCell(new Label(9, i, commissionOrderListDto.getSchoolInstitutionListDTO().getInstitutionTradingName() , cellFormat));
////							//if (commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO() != null)
////							sheet.addCell(new Label(10, i, commissionOrderListDto.getSchoolInstitutionListDTO().getInstitutionName(), cellFormat));
////							if (commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO() != null){
////								sheet.addCell(new Label(11, i, commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO().getName(), cellFormat));
////								sheet.addCell(new Label(12, i, commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO().getState(), cellFormat));
////							}
////							if (commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolCourseDO() != null)
////								sheet.addCell(new Label(13, i, commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolCourseDO().getCourseName(), cellFormat));
////						}
////						if (commissionOrderListDto.getStartDate() != null)
////							sheet.addCell(new Label(14, i, sdf.format(commissionOrderListDto.getStartDate()), cellFormat));
////						if (commissionOrderListDto.getEndDate() != null)
////							sheet.addCell(new Label(15, i, sdf.format(commissionOrderListDto.getEndDate()), cellFormat));
////						if (commissionOrderListDto.getInstallmentDueDate() != null)
////							sheet.addCell(
////									new Label(16, i, sdf.format(commissionOrderListDto.getInstallmentDueDate()), cellFormat));
////						if (commissionOrderListDto.getReceiveType() != null)
////							sheet.addCell(new Label(17, i, commissionOrderListDto.getReceiveType().getName() + "", cellFormat));
////						sheet.addCell(new Label(18, i, commissionOrderListDto.getTuitionFee() + "", cellFormat));
////						sheet.addCell(new Label(19, i, commissionOrderListDto.getPerAmount() + "", cellFormat)); // .getPerTermTuitionFee()
////						sheet.addCell(new Label(20, i, commissionOrderListDto.getTotalPerAmountCNY() + "", cellFormat));
////						sheet.addCell(new Label(21, i, commissionOrderListDto.getTotalPerAmountAUD() + "", cellFormat));
////						sheet.addCell(new Label(22, i, commissionOrderListDto.getTotalAmountCNY() + "", cellFormat));
////						sheet.addCell(new Label(23, i, commissionOrderListDto.getTotalAmountAUD() + "", cellFormat));
////						sheet.addCell(new Label(24, i, commissionOrderListDto.getCurrency(), cellFormat));
////						sheet.addCell(new Label(25, i, commissionOrderListDto.getExchangeRate() + "", cellFormat));
////						sheet.addCell(new Label(26, i, commissionOrderListDto.getAmountCNY() + "", cellFormat));
////						sheet.addCell(new Label(27, i, commissionOrderListDto.getAmountAUD() + "", cellFormat));
////						sheet.addCell(new Label(28, i, commissionOrderListDto.getExpectAmountAUD() + "", cellFormat));
////						if (commissionOrderListDto.isSettle())
////							sheet.addCell(new Label(29, i, commissionOrderListDto.getExpectAmountAUD() + "", cellFormat));
////						else
////							sheet.addCell(new Label(29, i, commissionOrderListDto.getSureExpectAmountAUD() + "", cellFormat));
////						sheet.addCell(new Label(30, i, commissionOrderListDto.getSchoolPaymentAmount() + "", cellFormat));
////						//31待确定
////						if (commissionOrderListDto.getSchoolPaymentDate() != null)
////							sheet.addCell(
////									new Label(32, i, sdf.format(commissionOrderListDto.getSchoolPaymentDate()), cellFormat));
////						sheet.addCell(new Label(33, i, commissionOrderListDto.getInvoiceNumber(), cellFormat));
////						if (commissionOrderListDto.getZyDate() != null)
////							sheet.addCell(new Label(34, i, sdf.format(commissionOrderListDto.getZyDate()), cellFormat));
////						if (commissionOrderListDto.getSubagency() != null)
////							sheet.addCell(new Label(35, i, commissionOrderListDto.getSubagency().getName(), cellFormat));
////						sheet.addCell(new Label(36, i, commissionOrderListDto.getBonus() + "", cellFormat));
////						if (commissionOrderListDto.getBonusDate() != null)
////							sheet.addCell(new Label(37, i, sdf.format(commissionOrderListDto.getBonusDate()), cellFormat));
////						sheet.addCell(new Label(38, i, commissionOrderListDto.getBankCheck(), cellFormat));
////						sheet.addCell(new Label(39, i, commissionOrderListDto.isChecked() + "", cellFormat));
////						if (commissionOrderListDto.getAdviser() != null)
////							sheet.addCell(new Label(40, i, commissionOrderListDto.getAdviser().getName(), cellFormat));
////						if (commissionOrderListDto.getState() != null)
////							sheet.addCell(new Label(41, i, getStateStr(commissionOrderListDto.getState()), cellFormat));
////						if (commissionOrderListDto.getKjApprovalDate() != null)
////							sheet.addCell(new Label(42, i, sdf.format(commissionOrderListDto.getKjApprovalDate()), cellFormat));
////						sheet.addCell(new Label(43, i, commissionOrderListDto.getRemarks(), cellFormat));
////						ServiceOrderDTO serviceOrderDTO = serviceOrderService
////								.getServiceOrderById(commissionOrderListDto.getServiceOrderId());
////						sheet.addCell(new Label(44, i,
////								serviceOrderDTO != null && serviceOrderDTO.getRemarks() != null ? serviceOrderDTO.getRemarks()
////										: "",
////								cellFormat));
////						i++;
////					}
////					wbe.write();
////					wbe.close();
////
////				} else {
////
////					os = response.getOutputStream();
////					try {
////						is = this.getClass().getResourceAsStream("/CommissionOrderTemplate.xls");
////					} catch (Exception e) {
////						throw new Exception("模版不存在");
////					}
////					try {
////						wb = Workbook.getWorkbook(is);
////					} catch (Exception e) {
////						throw new Exception("模版格式不支持");
////					}
////					WorkbookSettings settings = new WorkbookSettings();
////					settings.setWriteAccess(null);
////					jxl.write.WritableWorkbook wbe = Workbook.createWorkbook(os, wb, settings);
////
////					if (wbe == null) {
////						System.out.println("wbe is null !os=" + os + ",wb" + wb);
////					} else {
////						System.out.println("wbe not null !os=" + os + ",wb" + wb);
////					}
////					WritableSheet sheet = wbe.getSheet(0);
////					WritableCellFormat cellFormat = new WritableCellFormat();
////
////					int i = 1;
////					for (CommissionOrderListDTO commissionOrderListDto : commissionOrderList) {
////						sheet.addCell(new Label(0, i, "CS" + commissionOrderListDto.getId(), cellFormat));
////						sheet.addCell(new Label(1, i, sdf.format(commissionOrderListDto.getGmtCreate()), cellFormat));
////						if (commissionOrderListDto.getReceiveDate() != null)
////							sheet.addCell(new Label(2, i, sdf.format(commissionOrderListDto.getReceiveDate()), cellFormat));
////						if (commissionOrderListDto.getApplicant() != null)
////							sheet.addCell(new Label(3, i, commissionOrderListDto.getApplicant().getFirstname() + " "
////									+ commissionOrderListDto.getApplicant().getSurname(), cellFormat));
////						sheet.addCell(new Label(4, i, commissionOrderListDto.getStudentCode(), cellFormat));
////						if (commissionOrderListDto.getBirthday() != null)
////							sheet.addCell(new Label(5, i, sdf.format(commissionOrderListDto.getBirthday()), cellFormat));
////						if (commissionOrderListDto.getReceiveType() != null)
////							sheet.addCell(new Label(6, i, commissionOrderListDto.getReceiveType().getName() + "", cellFormat));
////						if (commissionOrderListDto.getService() != null)
////							sheet.addCell(new Label(7, i, commissionOrderListDto.getService().getName(), cellFormat));
////						sheet.addCell(new Label(8, i, commissionOrderListDto.isSettle() + "", cellFormat));
////						if (commissionOrderListDto.getSchool() != null) {
////							sheet.addCell(new Label(9, i, commissionOrderListDto.getSchool().getName() + "", cellFormat));
////							sheet.addCell(new Label(13, i, commissionOrderListDto.getSchool().getSubject() + "", cellFormat));
////						}
////						if (commissionOrderListDto.getSchoolInstitutionListDTO() != null){
////							sheet.addCell(new Label(9, i, commissionOrderListDto.getSchoolInstitutionListDTO().getInstitutionTradingName() , cellFormat));
////							//if (commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO() != null)
////							sheet.addCell(new Label(10, i, commissionOrderListDto.getSchoolInstitutionListDTO().getInstitutionName(), cellFormat));
////							if (commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO() != null){
////								sheet.addCell(new Label(11, i, commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO().getName(), cellFormat));
////								sheet.addCell(new Label(12, i, commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO().getState(), cellFormat));
////							}
////							if (commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolCourseDO() != null)
////								sheet.addCell(new Label(13, i, commissionOrderListDto.getSchoolInstitutionListDTO().getSchoolCourseDO().getCourseName(), cellFormat));
////						}
////						if (commissionOrderListDto.getStartDate() != null)
////							sheet.addCell(new Label(14, i, sdf.format(commissionOrderListDto.getStartDate()), cellFormat));
////						if (commissionOrderListDto.getEndDate() != null)
////							sheet.addCell(new Label(15, i, sdf.format(commissionOrderListDto.getEndDate()), cellFormat));
////						if (commissionOrderListDto.getInstallmentDueDate() != null)
////							sheet.addCell(
////									new Label(16, i, sdf.format(commissionOrderListDto.getInstallmentDueDate()), cellFormat));
////						if (commissionOrderListDto.getReceiveType() != null)
////							sheet.addCell(new Label(17, i, commissionOrderListDto.getReceiveType().getName() + "", cellFormat));
////						sheet.addCell(new Label(18, i, commissionOrderListDto.getTuitionFee() + "", cellFormat));
////						sheet.addCell(new Label(19, i, commissionOrderListDto.getPerAmount() + "", cellFormat)); // .getPerTermTuitionFee()
////						sheet.addCell(new Label(20, i, commissionOrderListDto.getTotalPerAmountCNY() + "", cellFormat));
////						sheet.addCell(new Label(21, i, commissionOrderListDto.getTotalPerAmountAUD() + "", cellFormat));
////						sheet.addCell(new Label(22, i, commissionOrderListDto.getTotalAmountCNY() + "", cellFormat));
////						sheet.addCell(new Label(23, i, commissionOrderListDto.getTotalAmountAUD() + "", cellFormat));
////						sheet.addCell(new Label(24, i, commissionOrderListDto.getCurrency(), cellFormat));
////						sheet.addCell(new Label(25, i, commissionOrderListDto.getExchangeRate() + "", cellFormat));
////						sheet.addCell(new Label(26, i, commissionOrderListDto.getAmountCNY() + "", cellFormat));
////						sheet.addCell(new Label(27, i, commissionOrderListDto.getAmountAUD() + "", cellFormat));
////						sheet.addCell(new Label(28, i, commissionOrderListDto.getExpectAmountAUD() + "", cellFormat));
////						if (commissionOrderListDto.isSettle())
////							sheet.addCell(new Label(29, i, commissionOrderListDto.getExpectAmountAUD() + "", cellFormat));
////						else
////							sheet.addCell(new Label(29, i, commissionOrderListDto.getSureExpectAmountAUD() + "", cellFormat));
////						sheet.addCell(new Label(30, i, commissionOrderListDto.getGst() + "", cellFormat));
////						sheet.addCell(new Label(31, i, commissionOrderListDto.getDeductGst() + "", cellFormat));
////						sheet.addCell(new Label(32, i, commissionOrderListDto.getSchoolPaymentAmount() + "", cellFormat));
////						if (commissionOrderListDto.getSchoolPaymentDate() != null)
////							sheet.addCell(
////									new Label(33, i, sdf.format(commissionOrderListDto.getSchoolPaymentDate()), cellFormat));
////						sheet.addCell(new Label(34, i, commissionOrderListDto.getInvoiceNumber(), cellFormat));
////						if (commissionOrderListDto.getZyDate() != null)
////							sheet.addCell(new Label(35, i, sdf.format(commissionOrderListDto.getZyDate()), cellFormat));
////						if (commissionOrderListDto.getSubagency() != null)
////							sheet.addCell(new Label(36, i, commissionOrderListDto.getSubagency().getName(), cellFormat));
////						sheet.addCell(new Label(37, i, commissionOrderListDto.getBonus() + "", cellFormat));
////						if (commissionOrderListDto.getBonusDate() != null)
////							sheet.addCell(new Label(38, i, sdf.format(commissionOrderListDto.getBonusDate()), cellFormat));
////						sheet.addCell(new Label(39, i, commissionOrderListDto.getBankCheck(), cellFormat));
////						sheet.addCell(new Label(40, i, commissionOrderListDto.isChecked() + "", cellFormat));
////						if (commissionOrderListDto.getAdviser() != null)
////							sheet.addCell(new Label(41, i, commissionOrderListDto.getAdviser().getName(), cellFormat));
////						if (commissionOrderListDto.getState() != null)
////							sheet.addCell(new Label(42, i, getStateStr(commissionOrderListDto.getState()), cellFormat));
////						if (commissionOrderListDto.getKjApprovalDate() != null)
////							sheet.addCell(new Label(43, i, sdf.format(commissionOrderListDto.getKjApprovalDate()), cellFormat));
////						sheet.addCell(new Label(44, i, commissionOrderListDto.getRemarks(), cellFormat));
////						ServiceOrderDTO serviceOrderDTO = serviceOrderService
////								.getServiceOrderById(commissionOrderListDto.getServiceOrderId());
////						sheet.addCell(new Label(45, i,
////								serviceOrderDTO != null && serviceOrderDTO.getRemarks() != null ? serviceOrderDTO.getRemarks()
////										: "",
////								cellFormat));
////						i++;
////					}
////					wbe.write();
////					wbe.close();
////
////				}
////			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
////		finally{
////			try {
////				if (is != null)
////					is.close();
////				System.out.println("is is close");
////			} catch (IOException e) {
////				System.out.println("is is close 出现 异常:");
////				e.printStackTrace();
////			}
////			try {
////				if (os != null)
////					os.close();
////				System.out.println("os is close");
////			} catch (IOException e) {
////				System.out.println("os is close 出现 异常:");
////				e.printStackTrace();
////			}
////			if (wb != null)
////				wb.close();
////			System.out.println("wb is close");
////		}
//		return new Response<>(0, "生成Excel成功， excel链接为：");
//	}


	@RequestMapping(value = "/get", method = RequestMethod.GET)
	@ResponseBody
	public Response<CommissionOrderListDTO> get(@RequestParam(value = "id") int id, HttpServletResponse response, HttpServletRequest request) {
		try {
			super.setGetHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			CommissionOrderListDTO commissionOrderById = new CommissionOrderListDTO();
			commissionOrderById = commissionOrderService.getCommissionOrderById(id);
			if ("GW".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
				if (commissionOrderById.getAdviserId() != adminUserLoginInfo.getAdviserId()) {
					commissionOrderById.setCurrentAdvisor(false);
				}
			}
			return new Response<CommissionOrderListDTO>(0, commissionOrderById);
		} catch (ServiceException e) {
			return new Response<CommissionOrderListDTO>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/get2", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<CommissionOrderListDTO>> get2(@RequestParam(value = "idList") String idList [], HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			List<CommissionOrderListDTO> commissionOrderListDTOList = new ArrayList<>();
			for (String id : idList ){
				CommissionOrderListDTO commissionOrderListDTO = commissionOrderService.getCommissionOrderById(StringUtil.toInt(id));
				if (commissionOrderListDTO == null)
					return new Response<List<CommissionOrderListDTO>>(1,"佣金订单不存在 id : " + id, null);
				commissionOrderListDTOList.add(commissionOrderListDTO);
			}
			return new Response<List<CommissionOrderListDTO>>(0, commissionOrderListDTOList );
		} catch (ServiceException e) {
			return new Response<List<CommissionOrderListDTO>>(1, e.getMessage(), null);
		}
	}
	@RequestMapping(value = "/getInfo", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<CommissionInfoDTO>> getInfo(@RequestParam(value = "id") int id,HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			Integer adviserId = getAdviserId(request);
			if (adviserId==null)
				adviserId=0;
			List<CommissionInfoDTO> commissionInfoById = commissionOrderService.getCommissionInfoById(id, adviserId);
			return new Response<List<CommissionInfoDTO>>(0, commissionInfoById);
		}
		catch (ServiceException e) {
			return new Response<List<CommissionInfoDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/approval", method = RequestMethod.POST)
	@ResponseBody
	public Response<CommissionOrderListDTO> approval(@RequestParam(value = "id") int id,
			@RequestParam(value = "state") String state, HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			if (ReviewKjStateEnum.CLOSE.toString().equalsIgnoreCase(state))
				return new Response<CommissionOrderListDTO>(1, "关闭操作请调用'refuse'接口.", null);
			// 审核
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo != null) {
				if (adminUserLoginInfo == null || (!"SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
						&& !"KJ".equalsIgnoreCase(adminUserLoginInfo.getApList())))
					return new Response<CommissionOrderListDTO>(1, "仅限会计审核佣金订单.", null);
				if ("SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
						|| "KJ".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
					if (ReviewKjStateEnum.get(state) != null) {
						CommissionOrderListDTO commissionOrderListDto = commissionOrderService
								.getCommissionOrderById(id);
						if (commissionOrderListDto == null)
							return new Response<CommissionOrderListDTO>(1, "佣金订单不存在!", null);
						serviceOrderService.approval(id, adminUserLoginInfo.getId(), null, null, null,
								state.toUpperCase());
						commissionOrderListDto.setState(state);
						if (commissionOrderService.updateCommissionOrder(commissionOrderListDto) > 0)
							return new Response<CommissionOrderListDTO>(0, commissionOrderListDto);
						else
							return new Response<CommissionOrderListDTO>(1, "修改操作异常!", null);
					} else
						return new Response<CommissionOrderListDTO>(1, "state错误!(" + state + ")", null);
				} else
					return new Response<CommissionOrderListDTO>(1, "该用户无审核权限!", null);
			} else
				return new Response<CommissionOrderListDTO>(1, "请登录!", null);
		} catch (ServiceException e) {
			return new Response<CommissionOrderListDTO>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/refuse", method = RequestMethod.POST)
	@ResponseBody
	public Response<CommissionOrderListDTO> refuse(@RequestParam(value = "id") int id,
			@RequestParam(value = "state") String state,
			@RequestParam(value = "refuseReason", required = false) String refuseReason, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			if (ReviewKjStateEnum.COMPLETE.toString().equalsIgnoreCase(state)
					|| ReviewKjStateEnum.FINISH.toString().equalsIgnoreCase(state))
				return new Response<CommissionOrderListDTO>(1, "完成操作请调用'approval'接口.", null);
			// 审核
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo != null) {
				if (adminUserLoginInfo == null || (!"SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
						&& !"KJ".equalsIgnoreCase(adminUserLoginInfo.getApList())))
					return new Response<CommissionOrderListDTO>(1, "仅限会计审核佣金订单.", null);
				if ("SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
						|| "KJ".equalsIgnoreCase(adminUserLoginInfo.getApList())) {
					if (ReviewKjStateEnum.get(state) != null) {

						CommissionOrderListDTO commissionOrderListDto = commissionOrderService
								.getCommissionOrderById(id);
						if (commissionOrderListDto == null)
							return new Response<CommissionOrderListDTO>(1, "佣金订单不存在!", null);
						// 更新驳回原因
						if (StringUtil.isNotEmpty(refuseReason))
							commissionOrderListDto.setRefuseReason(refuseReason);
						commissionOrderListDto.setState(state);
						if (commissionOrderService.updateCommissionOrder(commissionOrderListDto) > 0) {
							commissionOrderService.sendRefuseEmail(id);
							return new Response<CommissionOrderListDTO>(0, commissionOrderListDto);
						} else
							return new Response<CommissionOrderListDTO>(1, "修改操作异常!", null);
					} else
						return new Response<CommissionOrderListDTO>(1, "state错误!(" + state + ")", null);
				} else
					return new Response<CommissionOrderListDTO>(1, "该用户无审核权限!", null);
			} else
				return new Response<CommissionOrderListDTO>(1, "请登录!", null);
		} catch (ServiceException e) {
			return new Response<CommissionOrderListDTO>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/addComment", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> addComment(@RequestParam(value = "adminUserId", required = false) Integer adminUserId,
			@RequestParam(value = "commissionOrderId", required = false) Integer commissionOrderId,
			@RequestParam(value = "content") String content, HttpServletRequest request, HttpServletResponse response) {
		try {
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			super.setPostHeader(response);
			CommissionOrderCommentDTO commissionOrderCommentDto = new CommissionOrderCommentDTO();
			commissionOrderCommentDto
					.setAdminUserId(adminUserLoginInfo != null ? adminUserLoginInfo.getId() : adminUserId);
			commissionOrderCommentDto.setCommissionOrderId(commissionOrderId);
			commissionOrderCommentDto.setContent(content);
			if (commissionOrderService.addComment(commissionOrderCommentDto) > 0)
				return new Response<Integer>(0, commissionOrderCommentDto.getId());
			else
				return new Response<Integer>(1, "创建失败.", 0);
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/updateInfo",method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> updateInfo (@RequestParam(value = "serviceorderid",required = false)Integer serviceorderid,
									     @RequestParam(value = "installment",required = false)Integer installment,
										 @RequestParam(value = "installmentDueDate1",required = false) String installmentDueDate1,
										 @RequestParam(value = "installmentDueDate2", required = false) String installmentDueDate2,
										 @RequestParam(value = "installmentDueDate3", required = false) String installmentDueDate3,
										 @RequestParam(value = "installmentDueDate4", required = false) String installmentDueDate4,
										 @RequestParam(value = "installmentDueDate5", required = false) String installmentDueDate5,
										 @RequestParam(value = "installmentDueDate6", required = false) String installmentDueDate6,
										 @RequestParam(value = "installmentDueDate7", required = false) String installmentDueDate7,
										 @RequestParam(value = "installmentDueDate8", required = false) String installmentDueDate8,
										 @RequestParam(value = "installmentDueDate9", required = false) String installmentDueDate9,
										 @RequestParam(value = "installmentDueDate10", required = false) String installmentDueDate10,
										 @RequestParam(value = "installmentDueDate11", required = false) String installmentDueDate11,
										 @RequestParam(value = "installmentDueDate12", required = false) String installmentDueDate12,
										 @RequestParam(value = "installmentDueDate13", required = false) String installmentDueDate13,
										 @RequestParam(value = "installmentDueDate14", required = false) String installmentDueDate14,
										 @RequestParam(value = "installmentDueDate15", required = false) String installmentDueDate15,
										 @RequestParam(value = "installmentDueDate16", required = false) String installmentDueDate16,
										 @RequestParam(value = "installmentDueDate17", required = false) String installmentDueDate17,
										 @RequestParam(value = "installmentDueDate18", required = false) String installmentDueDate18,
									  HttpServletRequest request, HttpServletResponse response)
	{
		try {
			super.setPostHeader(response);
			Integer adviserId = getAdviserId(request);
			List<CommissionInfoDTO> info = commissionOrderService.getCommissionInfoById(serviceorderid,adviserId);
			List<Long> installmentDueDates = new ArrayList<>();
			commissionOrderService.setinstallmentById(serviceorderid,installment);
			if (installmentDueDate1!=null){installmentDueDates.add(Long.parseLong(installmentDueDate1));}
			if (installmentDueDate2!=null){installmentDueDates.add(Long.parseLong(installmentDueDate2));}
			if (installmentDueDate3!=null){installmentDueDates.add(Long.parseLong(installmentDueDate3));}
			if (installmentDueDate4!=null){installmentDueDates.add(Long.parseLong(installmentDueDate4));}
			if (installmentDueDate5!=null){installmentDueDates.add(Long.parseLong(installmentDueDate5));}
			if (installmentDueDate6!=null){installmentDueDates.add(Long.parseLong(installmentDueDate6));}
			if (installmentDueDate7!=null){installmentDueDates.add(Long.parseLong(installmentDueDate7));}
			if (installmentDueDate8!=null){installmentDueDates.add(Long.parseLong(installmentDueDate8));}
			if (installmentDueDate9!=null){installmentDueDates.add(Long.parseLong(installmentDueDate9));}
			if (installmentDueDate10!=null){installmentDueDates.add(Long.parseLong(installmentDueDate10));}
			if (installmentDueDate11!=null){installmentDueDates.add(Long.parseLong(installmentDueDate11));}
			if (installmentDueDate12!=null){installmentDueDates.add(Long.parseLong(installmentDueDate12));}
			if (installmentDueDate13!=null){installmentDueDates.add(Long.parseLong(installmentDueDate13));}
			if (installmentDueDate14!=null){installmentDueDates.add(Long.parseLong(installmentDueDate14));}
			if (installmentDueDate15!=null){installmentDueDates.add(Long.parseLong(installmentDueDate15));}
			if (installmentDueDate16!=null){installmentDueDates.add(Long.parseLong(installmentDueDate16));}
			if (installmentDueDate17!=null){installmentDueDates.add(Long.parseLong(installmentDueDate17));}
			if (installmentDueDate18!=null){installmentDueDates.add(Long.parseLong(installmentDueDate18));}
			if (info.size()>installment){
				for (CommissionInfoDTO commissionInfoDTO : info) {
					if (commissionInfoDTO.getInstallmentNum()>installment && commissionInfoDTO.getState().equals("PENDING")){
						commissionOrderService.deleteCommissionOrderInfoById(serviceorderid,commissionInfoDTO.getInstallmentNum());
					}
				}
				for (int i = 0; i < installmentDueDates.size(); i++) {
					commissionOrderService.setinstallmentDueDateById(serviceorderid,i + 1,new Date(installmentDueDates.get(i)));
				}
			}
			if (info.size()<installment){
				int num = installment-info.size();
				for (int i = 0; i < num; i++) {
					commissionOrderService.addCommissionInfoById(serviceorderid,info.size()+i+1);
				}
				commissionOrderService.setinstallmentById(serviceorderid,installment);
				for (int i = 0; i < installmentDueDates.size(); i++) {
					commissionOrderService.setinstallmentDueDateById(serviceorderid,i + 1,new Date(installmentDueDates.get(i)));
				}
			}
			if (info.size()==installment){
				for (int i = 0; i < installmentDueDates.size(); i++) {
					commissionOrderService.setinstallmentDueDateById(serviceorderid,i + 1,new Date(installmentDueDates.get(i)));
				}
//				if (installmentDueDate1!=null){
//					commissionOrderService.setinstallmentDueDateById(serviceorderid,1,new Date(Long.parseLong(installmentDueDate1)));
//				}
//				if (installmentDueDate2!=null){
//					commissionOrderService.setinstallmentDueDateById(serviceorderid,2,new Date(Long.parseLong(installmentDueDate2)));
//				}
//				if (installmentDueDate3!=null){
//					commissionOrderService.setinstallmentDueDateById(serviceorderid,3,new Date(Long.parseLong(installmentDueDate3)));
//				}
//				if (installmentDueDate4!=null){
//					commissionOrderService.setinstallmentDueDateById(serviceorderid,4,new Date(Long.parseLong(installmentDueDate4)));
//				}
//				if (installmentDueDate5!=null){
//					commissionOrderService.setinstallmentDueDateById(serviceorderid,5,new Date(Long.parseLong(installmentDueDate5)));
//				}
//				if (installmentDueDate6!=null){
//					commissionOrderService.setinstallmentDueDateById(serviceorderid,6,new Date(Long.parseLong(installmentDueDate6)));
//				}
//				if (installmentDueDate7!=null){
//					commissionOrderService.setinstallmentDueDateById(serviceorderid,7,new Date(Long.parseLong(installmentDueDate7)));
//				}
//				if (installmentDueDate8!=null){
//					commissionOrderService.setinstallmentDueDateById(serviceorderid,8,new Date(Long.parseLong(installmentDueDate8)));
//				}
//				if (installmentDueDate9!=null){
//					commissionOrderService.setinstallmentDueDateById(serviceorderid,9,new Date(Long.parseLong(installmentDueDate9)));
//				}
//				if (installmentDueDate10!=null){
//					commissionOrderService.setinstallmentDueDateById(serviceorderid,10,new Date(Long.parseLong(installmentDueDate10)));
//				}
//				if (installmentDueDate11!=null){
//					commissionOrderService.setinstallmentDueDateById(serviceorderid,11,new Date(Long.parseLong(installmentDueDate11)));
//				}
//				if (installmentDueDate12!=null){
//					commissionOrderService.setinstallmentDueDateById(serviceorderid,12,new Date(Long.parseLong(installmentDueDate12)));
//				}
//				if (installmentDueDate13!=null){
//					commissionOrderService.setinstallmentDueDateById(serviceorderid,13,new Date(Long.parseLong(installmentDueDate13)));
//				}
//				if (installmentDueDate14!=null){
//					commissionOrderService.setinstallmentDueDateById(serviceorderid,14,new Date(Long.parseLong(installmentDueDate14)));
//				}
//				if (installmentDueDate15!=null){
//					commissionOrderService.setinstallmentDueDateById(serviceorderid,15,new Date(Long.parseLong(installmentDueDate15)));
//				}
//				if (installmentDueDate16!=null){
//					commissionOrderService.setinstallmentDueDateById(serviceorderid,16,new Date(Long.parseLong(installmentDueDate16)));
//				}
//				if (installmentDueDate17!=null){
//					commissionOrderService.setinstallmentDueDateById(serviceorderid,17,new Date(Long.parseLong(installmentDueDate17)));
//				}
//				if (installmentDueDate18!=null){
//					commissionOrderService.setinstallmentDueDateById(serviceorderid,18,new Date(Long.parseLong(installmentDueDate18)));
//				}
			}
			return new Response<Integer>(0,"修改成功",0);
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}

	}

	@RequestMapping(value = "/countComment", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> countComment(@RequestParam(value = "commissionOrderId") Integer commissionOrderId,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, commissionOrderService.listComment(commissionOrderId).size());
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/listComment", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<CommissionOrderCommentDTO>> listComment(
			@RequestParam(value = "commissionOrderId") Integer commissionOrderId, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<List<CommissionOrderCommentDTO>>(0,
					commissionOrderService.listComment(commissionOrderId));
		} catch (ServiceException e) {
			return new Response<List<CommissionOrderCommentDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/deleteComment", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> deleteComment(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, commissionOrderService.deleteComment(id));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/deleteCommissionOrder", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> deleteCommissionOrder(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			LOG.info(StringUtil.merge("删除留学佣金订单:", commissionOrderService.getCommissionOrderById(id)));
			return new Response<Integer>(0, commissionOrderService.deleteCommissionOrder(id));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

	/*
	*财务驳回状态为REFERED，顾问修改佣金信息之后再提交申请月奖
	 */
	@Deprecated
	@RequestMapping(value = "/updateSubmitted", method = RequestMethod.POST)
	@ResponseBody
	public Response<List<CommissionOrderDTO>> updateSubmitted(@RequestParam(value = "serviceOrderId") Integer serviceOrderId,
					//@RequestParam(value = "state", required = false) String state,
					@RequestParam(value = "isSettle") Boolean isSettle,
					@RequestParam(value = "isDepositUser") Boolean isDepositUser,
					@RequestParam(value = "schoolId") Integer schoolId, @RequestParam(value = "studentCode") String studentCode,
					@RequestParam(value = "userId") Integer userId, @RequestParam(value = "adviserId") Integer adviserId,
					@RequestParam(value = "officialId") Integer officialId,
					@RequestParam(value = "isStudying") Boolean isStudying,
					@RequestParam(value = "installment") Integer installment,
					@RequestParam(value = "installmentDueDate1") String installmentDueDate1,
					@RequestParam(value = "installmentDueDate2", required = false) String installmentDueDate2,
					@RequestParam(value = "installmentDueDate3", required = false) String installmentDueDate3,
					@RequestParam(value = "installmentDueDate4", required = false) String installmentDueDate4,
					@RequestParam(value = "installmentDueDate5", required = false) String installmentDueDate5,
					@RequestParam(value = "installmentDueDate6", required = false) String installmentDueDate6,
					@RequestParam(value = "installmentDueDate7", required = false) String installmentDueDate7,
					@RequestParam(value = "installmentDueDate8", required = false) String installmentDueDate8,
					@RequestParam(value = "installmentDueDate9", required = false) String installmentDueDate9,
					@RequestParam(value = "installmentDueDate10", required = false) String installmentDueDate10,
					@RequestParam(value = "installmentDueDate11", required = false) String installmentDueDate11,
					@RequestParam(value = "installmentDueDate12", required = false) String installmentDueDate12,
					@RequestParam(value = "paymentVoucherImageUrl1", required = false) String paymentVoucherImageUrl1,
					@RequestParam(value = "paymentVoucherImageUrl2", required = false) String paymentVoucherImageUrl2,
					@RequestParam(value = "paymentVoucherImageUrl3", required = false) String paymentVoucherImageUrl3,
					@RequestParam(value = "paymentVoucherImageUrl4", required = false) String paymentVoucherImageUrl4,
					@RequestParam(value = "paymentVoucherImageUrl5", required = false) String paymentVoucherImageUrl5,
					@RequestParam(value = "dob") String dob, @RequestParam(value = "startDate") String startDate,
					@RequestParam(value = "endDate") String endDate, @RequestParam(value = "tuitionFee") String tuitionFee,
					@RequestParam(value = "perTermTuitionFee") String perTermTuitionFee,
					@RequestParam(value = "receiveTypeId") Integer receiveTypeId,
					@RequestParam(value = "receiveDate") String receiveDate,
					@RequestParam(value = "perAmount") String perAmount, @RequestParam(value = "amount") String amount,
					@RequestParam(value = "bonusDate", required = false) String bonusDate,
					@RequestParam(value = "zyDate", required = false) String zyDate,
					@RequestParam(value = "remarks", required = false) String remarks,
					@RequestParam(value = "verifyCode", required = false) String verifyCode,
					HttpServletRequest request, HttpServletResponse response){

		try {
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo == null || (!"SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
					&& !"GW".equalsIgnoreCase(adminUserLoginInfo.getApList())))
				return new Response<List<CommissionOrderDTO>>(1, "仅限顾问和超级管理员能修改佣金订单.", null);
			ServiceOrderDTO serviceOrderDTO = serviceOrderService.getServiceOrderById(serviceOrderId);
			if (serviceOrderDTO == null)
				return new Response<List<CommissionOrderDTO>>(1, "服务订单(ID:" + serviceOrderId + ")不存在!", null);
			if (serviceOrderDTO.getSubagencyId() <= 0)
				return new Response<List<CommissionOrderDTO>>
						(1, "SubagencyId(" + serviceOrderDTO.getSubagencyId() + ")不存在!", null);
			//List<CommissionOrderListDTO> commissionOrderListDTOS= commissionOrderService.CommissionOrderListByServiceOrderId(serviceOrderId);
			//if (commissionOrderListDTOS.size() == 0)
			CommissionOrderListDTO commissionOrderListDTO = commissionOrderService.getFirstCommissionOrderByServiceOrderId(serviceOrderId);
			if (commissionOrderListDTO == null)
				return new Response<List<CommissionOrderDTO>>(1, "还未创建服务订单为:" + serviceOrderId + "的佣金订单", null);
			//CommissionOrderDTO commissionOrderDto = new CommissionOrderDTO();
			if (isSettle)
				commissionOrderListDTO.setCommissionState(CommissionStateEnum.DJY.toString());
			else
				commissionOrderListDTO.setCommissionState(CommissionStateEnum.DZY.toString());
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl1))
				commissionOrderListDTO.setPaymentVoucherImageUrl1(paymentVoucherImageUrl1);
			else
				commissionOrderListDTO.setPaymentVoucherImageUrl1(serviceOrderDTO.getPaymentVoucherImageUrl1());
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl2))
				commissionOrderListDTO.setPaymentVoucherImageUrl2(paymentVoucherImageUrl2);
			else
				commissionOrderListDTO.setPaymentVoucherImageUrl2(serviceOrderDTO.getPaymentVoucherImageUrl2());
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl3))
				commissionOrderListDTO.setPaymentVoucherImageUrl3(paymentVoucherImageUrl3);
			else
				commissionOrderListDTO.setPaymentVoucherImageUrl3(serviceOrderDTO.getPaymentVoucherImageUrl3());
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl4))
				commissionOrderListDTO.setPaymentVoucherImageUrl4(paymentVoucherImageUrl4);
			else
				commissionOrderListDTO.setPaymentVoucherImageUrl4(serviceOrderDTO.getPaymentVoucherImageUrl4());
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl5))
				commissionOrderListDTO.setPaymentVoucherImageUrl5(paymentVoucherImageUrl5);
			else
				commissionOrderListDTO.setPaymentVoucherImageUrl5(serviceOrderDTO.getPaymentVoucherImageUrl5());
			commissionOrderListDTO.setStudentCode(studentCode);
			commissionOrderListDTO.setDob(new Date(Long.parseLong(dob)));
			commissionOrderListDTO.setStartDate(new Date(Long.parseLong(startDate)));
			commissionOrderListDTO.setEndDate(new Date(Long.parseLong(endDate)));
			commissionOrderListDTO.setTuitionFee(Double.parseDouble(tuitionFee));
			commissionOrderListDTO.setPerTermTuitionFee(Double.parseDouble(perTermTuitionFee));
			commissionOrderListDTO.setReceiveTypeId(receiveTypeId);
			commissionOrderListDTO.setReceiveDate(new Date(Long.parseLong(receiveDate)));
			commissionOrderListDTO.setPerAmount(Double.parseDouble(perAmount));
			commissionOrderListDTO.setAmount(Double.parseDouble(amount));
			if (installment == null || installment != commissionOrderListDTO.getInstallment())
				installment = commissionOrderListDTO.getInstallment();
			if (commissionOrderListDTO.getPerAmount() < commissionOrderListDTO.getAmount())
				return new Response<List<CommissionOrderDTO>>(1, "本次应收款(" + commissionOrderListDTO.getPerAmount()
						+ ")不能小于本次已收款(" + commissionOrderListDTO.getAmount() + ")!", null);
			commissionOrderListDTO.setDiscount(commissionOrderListDTO.getPerAmount() - commissionOrderListDTO.getAmount());
			if (StringUtil.isNotEmpty(bonusDate))
				commissionOrderListDTO.setBonusDate(new Date(Long.parseLong(bonusDate)));
			if (StringUtil.isNotEmpty(zyDate))
				commissionOrderListDTO.setZyDate(new Date(Long.parseLong(zyDate)));
			if (StringUtil.isNotEmpty(remarks))
				commissionOrderListDTO.setRemarks(remarks);

			String msg = "";
			for (int installmentNum = 1; installmentNum <= installment; installmentNum++) {
				commissionOrderListDTO.setInstallmentNum(installmentNum);
				if (installmentNum == 1 && installmentDueDate1 != null) {
					commissionOrderListDTO.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate1)));
					//commissionOrderListDTO.setState(ReviewKjStateEnum.REVIEW.toString()); // 第一笔单子直接进入财务审核状态
					if (StringUtil.isNotEmpty(verifyCode))
						commissionOrderListDTO.setVerifyCode(verifyCode.replace("$", "").replace("#", "").replace(" ", ""));
					//commissionOrderListDTO.setKjApprovalDate(new Date());
				} else {
					if (installmentNum == 2 && installmentDueDate2 != null) {
						commissionOrderListDTO.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate2)));
					} else if (installmentNum == 3 && installmentDueDate3 != null) {
						commissionOrderListDTO.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate3)));
					} else if (installmentNum == 4 && installmentDueDate4 != null) {
						commissionOrderListDTO.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate4)));
					} else if (installmentNum == 5 && installmentDueDate5 != null) {
						commissionOrderListDTO.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate5)));
					} else if (installmentNum == 6 && installmentDueDate6 != null) {
						commissionOrderListDTO.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate6)));
					} else if (installmentNum == 7 && installmentDueDate7 != null) {
						commissionOrderListDTO.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate7)));
					} else if (installmentNum == 8 && installmentDueDate8 != null) {
						commissionOrderListDTO.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate8)));
					} else if (installmentNum == 9 && installmentDueDate9 != null) {
						commissionOrderListDTO.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate9)));
					} else if (installmentNum == 10 && installmentDueDate10 != null) {
						commissionOrderListDTO.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate10)));
					} else if (installmentNum == 11 && installmentDueDate11 != null) {
						commissionOrderListDTO.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate11)));
					} else if (installmentNum == 12 && installmentDueDate12 != null) {
						commissionOrderListDTO.setInstallmentDueDate(new Date(Long.parseLong(installmentDueDate12)));
					} else
						break;
					//commissionOrderListDTO.setState(ReviewKjStateEnum.PENDING.toString());
					commissionOrderListDTO.setVerifyCode(null);
					commissionOrderListDTO.setKjApprovalDate(null);
					commissionOrderListDTO.setReceiveDate(null);
					commissionOrderListDTO.setPaymentVoucherImageUrl1(null);
					commissionOrderListDTO.setPaymentVoucherImageUrl2(null);
					commissionOrderListDTO.setPaymentVoucherImageUrl3(null);
					commissionOrderListDTO.setPaymentVoucherImageUrl4(null);
					commissionOrderListDTO.setPaymentVoucherImageUrl5(null);
				}
				int id = commissionOrderService.updateCommissionOrderByServiceOrderId(commissionOrderListDTO);
				if (id > 0) {
					//serviceOrderDto.setSubmitted(true);
					userService.updateDOB(new Date(Long.parseLong(dob)), userId);
					//serviceOrderService.updateServiceOrder(serviceOrderDTO); // 同时更改服务订单状态
					//commissionOrderDtoList.add(commissionOrderDto);
					CommissionOrderListDTO commissionOrderListDto = commissionOrderService.getCommissionOrderById(id);
					int i = schoolService.updateSchoolSetting(commissionOrderListDto); // 根据学校设置更新佣金值
					if (i > 0) {
					} else if (i == -1)
						msg += id + "计算失败. (佣金记录不存在);";
					else if (i == -2)
						msg += id + "计算失败. (学校佣金设置不存在或不正确);";
					else if (i == -3)
						msg += id + "计算失败. (佣金办理时间不在设置合同时间范围内);";
					else
						msg += id + "计算失败. ;";
				} else
					msg += "佣金订单创建失败. (" + commissionOrderListDTO.toString() + ");";
			}
			return new Response<List<CommissionOrderDTO>>(0, msg, null);

		} catch (ServiceException e) {
			return new Response<List<CommissionOrderDTO>>(e.getCode(), e.getMessage(), null);
		}
	}

	/**
	 * 提前扣拥留学：顾问填写申请月奖的数据，暂存到b_commission_order_temp表中，等财务通过之后，获取
	 * 数据再创建佣金订单
	 * @return
	 */
	@PostMapping(value = "/addCommissionOrderTemp")
	@ResponseBody
	public Response addCommissionOrderTemp(@RequestParam(value = "installment") Integer installment,
		   @RequestParam(value = "installmentDueDate1") String installmentDueDate1,
		   @RequestParam(value = "installmentDueDate2", required = false) String installmentDueDate2,
		   @RequestParam(value = "installmentDueDate3", required = false) String installmentDueDate3,
		   @RequestParam(value = "installmentDueDate4", required = false) String installmentDueDate4,
		   @RequestParam(value = "installmentDueDate5", required = false) String installmentDueDate5,
		   @RequestParam(value = "installmentDueDate6", required = false) String installmentDueDate6,
		   @RequestParam(value = "installmentDueDate7", required = false) String installmentDueDate7,
		   @RequestParam(value = "installmentDueDate8", required = false) String installmentDueDate8,
		   @RequestParam(value = "installmentDueDate9", required = false) String installmentDueDate9,
		   @RequestParam(value = "installmentDueDate10", required = false) String installmentDueDate10,
		   @RequestParam(value = "installmentDueDate11", required = false) String installmentDueDate11,
		   @RequestParam(value = "installmentDueDate12", required = false) String installmentDueDate12,
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
		   @RequestParam(value = "dob") String dob, @RequestParam(value = "startDate") String startDate,
		   @RequestParam(value = "endDate") String endDate, @RequestParam(value = "tuitionFee") String tuitionFee,
		   @RequestParam(value = "perTermTuitionFee") String perTermTuitionFee,
		   @RequestParam(value = "receiveTypeId") Integer receiveTypeId,
		   @RequestParam(value = "receiveDate") String receiveDate,
		   @RequestParam(value = "perAmount") String perAmount, @RequestParam(value = "amount") String amount,
		   @RequestParam(value = "remarks", required = false) String remarks,
		   @RequestParam(value = "studentCode") String studentCode,
		   @RequestParam(value = "serviceOrderId")int serviceOrderId,
		   @RequestParam(value = "expectAmount")String expectAmount,
			@RequestParam(value = "currency", required = false) String currency,
			@RequestParam(value = "exchangeRate", required = false) String exchangeRate,
		   @RequestParam(value = "verifyCode", required = false) String verifyCode,
		   @RequestParam(value = "visaStatus", required = false) String visaStatus,
		   @RequestParam(value = "visaStatusSub", required = false) String visaStatusSub,
		   @RequestParam(value = "visaCertificate", required = false) String visaCertificate,
			HttpServletRequest request, HttpServletResponse response
										   ){
		try {
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo == null || (!"SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
					&& !"GW".equalsIgnoreCase(adminUserLoginInfo.getApList())))
				return new Response(1, "仅限顾问和超级管理员能创建.", null);
			ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(serviceOrderId);
			if (serviceOrderDto == null)
				return new Response(1, "服务订单(ID:" + serviceOrderId + ")不存在!", null);

			if (serviceOrderDto.getSubagencyId() <= 0)
				return new Response(1,
						"SubagencyId(" + serviceOrderDto.getSubagencyId() + ")不存在!", null);
			CommissionOrderTempDTO tempDTO = commissionOrderService.getCommissionOrderTempByServiceOrderId(serviceOrderId);
			if (tempDTO != null) {
				return updateCommissionOrderTemp(installment,installmentDueDate1,installmentDueDate2,installmentDueDate3,installmentDueDate4,
						installmentDueDate5,installmentDueDate6,installmentDueDate7,installmentDueDate8,installmentDueDate9,installmentDueDate10,
						installmentDueDate11,installmentDueDate12,paymentVoucherImageUrl1,paymentVoucherImageUrl2,paymentVoucherImageUrl3,
						paymentVoucherImageUrl4,paymentVoucherImageUrl5,invoiceVoucherImageUrl1,invoiceVoucherImageUrl2,invoiceVoucherImageUrl3,
						invoiceVoucherImageUrl4,invoiceVoucherImageUrl5,dob, startDate,endDate, tuitionFee,perTermTuitionFee,receiveTypeId,
						receiveDate,perAmount, amount,remarks,studentCode,serviceOrderId,expectAmount, currency, exchangeRate,verifyCode,tempDTO,serviceOrderDto);
			}
			tempDTO = new CommissionOrderTempDTO();
			tempDTO.setServiceOrderId(serviceOrderId);
			tempDTO.setStudentCode(studentCode);
			tempDTO.setDob(new Date(Long.parseLong(dob)));
			tempDTO.setStartDate(new Date(Long.parseLong(startDate)));
			tempDTO.setEndDate(new Date(Long.parseLong(endDate)));
			tempDTO.setTuitionFee(Double.parseDouble(tuitionFee));
			tempDTO.setPerTermTuitionFee(Double.parseDouble(perTermTuitionFee));
			tempDTO.setReceiveTypeId(receiveTypeId);
			tempDTO.setReceiveDate(new Date(Long.parseLong(receiveDate)));
			tempDTO.setPerAmount(Double.parseDouble(perAmount));
			tempDTO.setAmount(Double.parseDouble(amount));
			tempDTO.setExpectAmount(Double.parseDouble(expectAmount));
			if (StringUtil.isNotEmpty(currency))
				tempDTO.setCurrency(currency);
			if (StringUtil.isNotEmpty(exchangeRate))
				tempDTO.setExchangeRate(Double.parseDouble(exchangeRate));
			if (tempDTO.getPerAmount() < tempDTO.getAmount())
				return new Response(1, "本次应收款(" + tempDTO.getPerAmount()
						+ ")不能小于本次已收款(" + tempDTO.getAmount() + ")!", null);
			tempDTO.setDiscount(tempDTO.getPerAmount() - tempDTO.getAmount());
			if (StringUtil.isNotEmpty(verifyCode))
				tempDTO.setVerifyCode(verifyCode);
			if (StringUtil.isNotEmpty(remarks))
				tempDTO.setRemarks(remarks);
			if (installment == null || installment <= 0 || installment > 12)
				return new Response(1,"installment = " + installment);
			tempDTO.setInstallment(installment);
			if (StringUtil.isNotEmpty(installmentDueDate1))
				tempDTO.setInstallmentDueDate1(new Date(Long.parseLong(installmentDueDate1)));
			if (StringUtil.isNotEmpty(installmentDueDate2))
				tempDTO.setInstallmentDueDate2(new Date(Long.parseLong(installmentDueDate2)));
			if (StringUtil.isNotEmpty(installmentDueDate3))
				tempDTO.setInstallmentDueDate3(new Date(Long.parseLong(installmentDueDate3)));
			if (StringUtil.isNotEmpty(installmentDueDate4))
				tempDTO.setInstallmentDueDate4(new Date(Long.parseLong(installmentDueDate4)));
			if (StringUtil.isNotEmpty(installmentDueDate5))
				tempDTO.setInstallmentDueDate5(new Date(Long.parseLong(installmentDueDate5)));
			if (StringUtil.isNotEmpty(installmentDueDate6))
				tempDTO.setInstallmentDueDate6(new Date(Long.parseLong(installmentDueDate6)));
			if (StringUtil.isNotEmpty(installmentDueDate7))
				tempDTO.setInstallmentDueDate7(new Date(Long.parseLong(installmentDueDate7)));
			if (StringUtil.isNotEmpty(installmentDueDate8))
				tempDTO.setInstallmentDueDate8(new Date(Long.parseLong(installmentDueDate8)));
			if (StringUtil.isNotEmpty(installmentDueDate9))
				tempDTO.setInstallmentDueDate9(new Date(Long.parseLong(installmentDueDate9)));
			if (StringUtil.isNotEmpty(installmentDueDate10))
				tempDTO.setInstallmentDueDate10(new Date(Long.parseLong(installmentDueDate10)));
			if (StringUtil.isNotEmpty(installmentDueDate11))
				tempDTO.setInstallmentDueDate11(new Date(Long.parseLong(installmentDueDate11)));
			if (StringUtil.isNotEmpty(installmentDueDate12))
				tempDTO.setInstallmentDueDate12(new Date(Long.parseLong(installmentDueDate12)));
			
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl1)){
				tempDTO.setPaymentVoucherImageUrl1(paymentVoucherImageUrl1);
				serviceOrderDto.setPaymentVoucherImageUrl1(paymentVoucherImageUrl1);
			}
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl2)){
				tempDTO.setPaymentVoucherImageUrl2(paymentVoucherImageUrl2);
				serviceOrderDto.setPaymentVoucherImageUrl2(paymentVoucherImageUrl2);
			}
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl3)){
				tempDTO.setPaymentVoucherImageUrl3(paymentVoucherImageUrl3);
				serviceOrderDto.setPaymentVoucherImageUrl3(paymentVoucherImageUrl3);
			}
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl4)){
				tempDTO.setPaymentVoucherImageUrl4(paymentVoucherImageUrl4);
				serviceOrderDto.setPaymentVoucherImageUrl4(paymentVoucherImageUrl4);
			}
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl5)){
				tempDTO.setPaymentVoucherImageUrl5(paymentVoucherImageUrl5);
				serviceOrderDto.setPaymentVoucherImageUrl5(paymentVoucherImageUrl5);
			}


			if (serviceOrderDto.isSettle()){
				if (StringUtil.isNotEmpty(paymentVoucherImageUrl1) || StringUtil.isNotEmpty(paymentVoucherImageUrl2)
						|| StringUtil.isNotEmpty(paymentVoucherImageUrl3) || StringUtil.isNotEmpty(paymentVoucherImageUrl4)
						|| StringUtil.isNotEmpty(paymentVoucherImageUrl5)) {
				/*
				杜大哥（2021-10-18）说：【提前扣拥】在上传【invoice凭证】之后，申请月奖直接将服务订单状态改成【RECEIVED】(已收款凭证已提交)
				 */
					serviceOrderDto.setState("RECEIVED");
				} else
					return new Response(1, "请上传支付凭证");
			}else
				return new Response(1, "服务订单不是提前扣拥:" + serviceOrderDto.getId());

			if (StringUtil.isNotEmpty(visaCertificate))
				tempDTO.setVisaCertificate(visaCertificate);
			if (StringUtil.isNotEmpty(visaStatus)) {
				tempDTO.setVisaStatus(visaStatus);
				if ("Off shore".equals(visaStatus)) {
					int courseId = serviceOrderDto.getCourseId();
					if (courseId > 0) {
						SchoolCourseDTO schoolCourseDTO = schoolCourseService.schoolCourseById(courseId);
						SchoolInstitutionDTO schoolInstitutionById = schoolInstitutionService.getSchoolInstitutionById(schoolCourseDTO.getProviderId());
						if (ObjectUtil.isNotNull(schoolInstitutionById)) {
							if (!schoolInstitutionById.isCooperative() && StringUtil.isEmpty(visaCertificate)) {
								return new Response<CommissionOrderTempDTO>(1, "当前选择为非合作院校，请上传签证信息", tempDTO);
							}
						}
					}
				}
			}
			if (StringUtil.isNotEmpty(visaStatusSub)) {
				tempDTO.setVisaStatusSub(visaStatusSub);
			}

			//顾问在留学提前扣拥状态为COMPLETE时，不用上传发票凭证
			//if (StringUtil.isNotEmpty(invoiceVoucherImageUrl1))
			//	serviceOrderDto.setInvoiceVoucherImageUrl1(invoiceVoucherImageUrl1);
			//if (StringUtil.isNotEmpty(invoiceVoucherImageUrl2))
			//	serviceOrderDto.setInvoiceVoucherImageUrl2(invoiceVoucherImageUrl2);
			//if (StringUtil.isNotEmpty(invoiceVoucherImageUrl3))
			//	serviceOrderDto.setInvoiceVoucherImageUrl3(invoiceVoucherImageUrl3);
			//if (StringUtil.isNotEmpty(invoiceVoucherImageUrl4))
			//	serviceOrderDto.setInvoiceVoucherImageUrl4(invoiceVoucherImageUrl4);
			//if (StringUtil.isNotEmpty(invoiceVoucherImageUrl5))
			//	serviceOrderDto.setInvoiceVoucherImageUrl5(invoiceVoucherImageUrl5);
			
			if(commissionOrderService.addCommissionOrderTemp(tempDTO) > 0){
				if (serviceOrderService.updateServiceOrder(serviceOrderDto) > 0) // 同时更改服务订单状态
					return new Response(0, "success", tempDTO);
			}
			return new Response(1, "失败");
		} catch (ServiceException e) {
			return new Response(e.getCode(), e.getMessage(), null);
		}
	}

	@GetMapping(value = "/getCommissionOrderTempByServiceOrderId")
	@ResponseBody
	public Response getCommissionOrderTempByServiceOrderId(int id){
		try {
			return  new Response(0,"", commissionOrderService.getCommissionOrderTempByServiceOrderId(id));
		}catch (ServiceException e){
			return new Response(e.getCode(), e.getMessage(), null);
		}
	}


	public Response updateCommissionOrderTemp(
			/*
			//@RequestParam(value = "id")int id,
			@RequestParam(value = "installment") Integer installment,
			@RequestParam(value = "installmentDueDate1") String installmentDueDate1,
			@RequestParam(value = "installmentDueDate2", required = false) String installmentDueDate2,
			@RequestParam(value = "installmentDueDate3", required = false) String installmentDueDate3,
			@RequestParam(value = "installmentDueDate4", required = false) String installmentDueDate4,
			@RequestParam(value = "installmentDueDate5", required = false) String installmentDueDate5,
			@RequestParam(value = "installmentDueDate6", required = false) String installmentDueDate6,
			@RequestParam(value = "installmentDueDate7", required = false) String installmentDueDate7,
			@RequestParam(value = "installmentDueDate8", required = false) String installmentDueDate8,
			@RequestParam(value = "installmentDueDate9", required = false) String installmentDueDate9,
			@RequestParam(value = "installmentDueDate10", required = false) String installmentDueDate10,
			@RequestParam(value = "installmentDueDate11", required = false) String installmentDueDate11,
			@RequestParam(value = "installmentDueDate12", required = false) String installmentDueDate12,
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
			@RequestParam(value = "dob",required = false) String dob, @RequestParam(value = "startDate",required = false) String startDate,
			@RequestParam(value = "endDate",required = false) String endDate, @RequestParam(value = "tuitionFee",required = false) String tuitionFee,
			@RequestParam(value = "perTermTuitionFee",required = false) String perTermTuitionFee,
			@RequestParam(value = "receiveTypeId",required = false) Integer receiveTypeId,
			@RequestParam(value = "receiveDate",required = false) String receiveDate,
			@RequestParam(value = "perAmount",required = false) String perAmount, @RequestParam(value = "amount",required = false) String amount,
			@RequestParam(value = "remarks", required = false) String remarks,
			@RequestParam(value = "studentCode",required = false) String studentCode,
			@RequestParam(value = "serviceOrderId")int serviceOrderId,
			@RequestParam(value = "expectAmount",required = false)String expectAmount,
			@RequestParam(value = "verifyCode", required = false) String verifyCode,
			HttpServletRequest request, HttpServletResponse response
			 */
			Integer installment, String installmentDueDate1, String installmentDueDate2, String installmentDueDate3,
			String installmentDueDate4, String installmentDueDate5, String installmentDueDate6, String installmentDueDate7,
			String installmentDueDate8, String installmentDueDate9, String installmentDueDate10, String installmentDueDate11,
			String installmentDueDate12, String paymentVoucherImageUrl1, String paymentVoucherImageUrl2,
			String paymentVoucherImageUrl3, String paymentVoucherImageUrl4, String paymentVoucherImageUrl5, String invoiceVoucherImageUrl1,
			String invoiceVoucherImageUrl2, String invoiceVoucherImageUrl3, String invoiceVoucherImageUrl4, String invoiceVoucherImageUrl5,
			String dob, String startDate, String endDate, String tuitionFee, String perTermTuitionFee, Integer receiveTypeId,
			String receiveDate, String perAmount, String amount, String remarks, String studentCode, int serviceOrderId,
			String expectAmount, String currency, String exchangeRate, String verifyCode,CommissionOrderTempDTO tempDTO,ServiceOrderDTO serviceOrderDto
	){
		try {
			/*
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo == null || (!"SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList())
					&& !"GW".equalsIgnoreCase(adminUserLoginInfo.getApList())))
				return new Response(1, "仅限顾问和超级管理员能修改.", null);
			ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(serviceOrderId);
			if (serviceOrderDto == null)
				return new Response(1, "服务订单(ID:" + serviceOrderId + ")不存在!", null);

			CommissionOrderTempDTO tempDTO = commissionOrderService.getCommissionOrderTempByServiceOrderId(serviceOrderId);
			if (tempDTO == null)
				return new Response(1,"此条记录不存在，修改失败");

			if (serviceOrderDto.getSubagencyId() <= 0)
				return new Response(1,
						"SubagencyId(" + serviceOrderDto.getSubagencyId() + ")不存在!", null);
			 */

			if (StringUtil.isNotEmpty(studentCode))
				tempDTO.setStudentCode(studentCode);
			if (StringUtil.isNotEmpty(dob))
				tempDTO.setDob(new Date(Long.parseLong(dob)));
			if (StringUtil.isNotEmpty(startDate))
				tempDTO.setStartDate(new Date(Long.parseLong(startDate)));
			if (StringUtil.isNotEmpty(endDate))
				tempDTO.setEndDate(new Date(Long.parseLong(endDate)));
			if (StringUtil.isNotEmpty(tuitionFee))
				tempDTO.setTuitionFee(Double.parseDouble(tuitionFee));
			if (StringUtil.isNotEmpty(perTermTuitionFee))
				tempDTO.setPerTermTuitionFee(Double.parseDouble(perTermTuitionFee));
			if (receiveTypeId != null)
				tempDTO.setReceiveTypeId(receiveTypeId);
			if (StringUtil.isNotEmpty(receiveDate))
				tempDTO.setReceiveDate(new Date(Long.parseLong(receiveDate)));
			if (StringUtil.isNotEmpty(perAmount))
				tempDTO.setPerAmount(Double.parseDouble(perAmount));
			if (StringUtil.isNotEmpty(amount))
				tempDTO.setAmount(Double.parseDouble(amount));
			if (StringUtil.isNotEmpty(expectAmount))
				tempDTO.setExpectAmount(Double.parseDouble(expectAmount));
			if (tempDTO.getPerAmount() < tempDTO.getAmount())
				return new Response(1, "本次应收款(" + tempDTO.getPerAmount()
						+ ")不能小于本次已收款(" + tempDTO.getAmount() + ")!", null);
			tempDTO.setDiscount(tempDTO.getPerAmount() - tempDTO.getAmount());
			if (StringUtil.isNotEmpty(verifyCode))
				tempDTO.setVerifyCode(verifyCode);
			if (StringUtil.isNotEmpty(remarks))
				tempDTO.setRemarks(remarks);

			if (installment != null && installment > 0)
				tempDTO.setInstallment(installment);

			if (StringUtil.isNotEmpty(installmentDueDate1))
				tempDTO.setInstallmentDueDate1(new Date(Long.parseLong(installmentDueDate1)));
			if (StringUtil.isNotEmpty(installmentDueDate2))
				tempDTO.setInstallmentDueDate2(new Date(Long.parseLong(installmentDueDate2)));
			else
				tempDTO.setInstallmentDueDate2(null);
			if (StringUtil.isNotEmpty(installmentDueDate3))
				tempDTO.setInstallmentDueDate3(new Date(Long.parseLong(installmentDueDate3)));
			else
				tempDTO.setInstallmentDueDate3(null);
			if (StringUtil.isNotEmpty(installmentDueDate4))
				tempDTO.setInstallmentDueDate4(new Date(Long.parseLong(installmentDueDate4)));
			else
				tempDTO.setInstallmentDueDate4(null);
			if (StringUtil.isNotEmpty(installmentDueDate5))
				tempDTO.setInstallmentDueDate5(new Date(Long.parseLong(installmentDueDate5)));
			else
				tempDTO.setInstallmentDueDate5(null);
			if (StringUtil.isNotEmpty(installmentDueDate6))
				tempDTO.setInstallmentDueDate6(new Date(Long.parseLong(installmentDueDate6)));
			else
				tempDTO.setInstallmentDueDate6(null);
			if (StringUtil.isNotEmpty(installmentDueDate7))
				tempDTO.setInstallmentDueDate7(new Date(Long.parseLong(installmentDueDate7)));
			else
				tempDTO.setInstallmentDueDate7(null);
			if (StringUtil.isNotEmpty(installmentDueDate8))
				tempDTO.setInstallmentDueDate8(new Date(Long.parseLong(installmentDueDate8)));
			else
				tempDTO.setInstallmentDueDate8(null);
			if (StringUtil.isNotEmpty(installmentDueDate9))
				tempDTO.setInstallmentDueDate9(new Date(Long.parseLong(installmentDueDate9)));
			else
				tempDTO.setInstallmentDueDate9(null);
			if (StringUtil.isNotEmpty(installmentDueDate10))
				tempDTO.setInstallmentDueDate10(new Date(Long.parseLong(installmentDueDate10)));
			else
				tempDTO.setInstallmentDueDate10(null);
			if (StringUtil.isNotEmpty(installmentDueDate11))
				tempDTO.setInstallmentDueDate11(new Date(Long.parseLong(installmentDueDate11)));
			else
				tempDTO.setInstallmentDueDate11(null);
			if (StringUtil.isNotEmpty(installmentDueDate12))
				tempDTO.setInstallmentDueDate12(new Date(Long.parseLong(installmentDueDate12)));
			else
				tempDTO.setInstallmentDueDate12(null);
			
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl1)){
				tempDTO.setPaymentVoucherImageUrl1(paymentVoucherImageUrl1);
				serviceOrderDto.setPaymentVoucherImageUrl1(paymentVoucherImageUrl1);
			}
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl2)){
				tempDTO.setPaymentVoucherImageUrl2(paymentVoucherImageUrl2);
				serviceOrderDto.setPaymentVoucherImageUrl2(paymentVoucherImageUrl2);
			}
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl3)){
				tempDTO.setPaymentVoucherImageUrl3(paymentVoucherImageUrl3);
				serviceOrderDto.setPaymentVoucherImageUrl3(paymentVoucherImageUrl3);
			}
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl4)){
				tempDTO.setPaymentVoucherImageUrl4(paymentVoucherImageUrl4);
				serviceOrderDto.setPaymentVoucherImageUrl4(paymentVoucherImageUrl4);
			}
			if (StringUtil.isNotEmpty(paymentVoucherImageUrl5)){
				tempDTO.setPaymentVoucherImageUrl5(paymentVoucherImageUrl5);
				serviceOrderDto.setPaymentVoucherImageUrl5(paymentVoucherImageUrl5);
			}
			if (StringUtil.isNotEmpty(tempDTO.getVisaStatus())){
				String visaStatus = tempDTO.getVisaStatus();
				String[] split = visaStatus.split("-");
				if (split.length > 1) {
					tempDTO.setVisaStatus(split[0]);
				}
			}

			if (serviceOrderDto.isSettle()){
				if (StringUtil.isNotEmpty(paymentVoucherImageUrl1) || StringUtil.isNotEmpty(paymentVoucherImageUrl2)
						|| StringUtil.isNotEmpty(paymentVoucherImageUrl3) || StringUtil.isNotEmpty(paymentVoucherImageUrl4)
						|| StringUtil.isNotEmpty(paymentVoucherImageUrl5)) {
				/*
				杜大哥（2021-10-18）说：【提前扣拥】在上传【invoice凭证】之后，申请月奖直接将服务订单状态改成【RECEIVED】(已收款凭证已提交)
				 */
					serviceOrderDto.setState("RECEIVED");
				} else
					return new Response(1, "请上传支付凭证");
			}else
				return new Response(1, "服务订单不是提前扣拥:" + serviceOrderDto.getId());

			//顾问在留学提前扣拥状态为COMPLETE时，不用上传发票凭证
			//if (StringUtil.isNotEmpty(invoiceVoucherImageUrl1))
			//	serviceOrderDto.setInvoiceVoucherImageUrl1(invoiceVoucherImageUrl1);
			//if (StringUtil.isNotEmpty(invoiceVoucherImageUrl2))
			//	serviceOrderDto.setInvoiceVoucherImageUrl2(invoiceVoucherImageUrl2);
			//if (StringUtil.isNotEmpty(invoiceVoucherImageUrl3))
			//	serviceOrderDto.setInvoiceVoucherImageUrl3(invoiceVoucherImageUrl3);
			//if (StringUtil.isNotEmpty(invoiceVoucherImageUrl4))
			//	serviceOrderDto.setInvoiceVoucherImageUrl4(invoiceVoucherImageUrl4);
			//if (StringUtil.isNotEmpty(invoiceVoucherImageUrl5))
			//	serviceOrderDto.setInvoiceVoucherImageUrl5(invoiceVoucherImageUrl5);
			
			if (commissionOrderService.updateCommissionOrderTemp(tempDTO) > 0){
				if (serviceOrderService.updateServiceOrder(serviceOrderDto) > 0 )
					return new Response(0,"success",tempDTO);
			}
			return new Response(1,"更新失败");
		}catch (ServiceException e) {
			return new Response(e.getCode(), e.getMessage(), null);
		}

	}

	public List<JSONObject> build(CommissionOrderListDTO so, Map<Integer, String> adviserMap, int _regionId) throws ServiceException {
		List<JSONObject> rows = new ArrayList<>();
		// 订单ID
		JSONObject jsonObject = new JSONObject();
		JSONObject text = new JSONObject();
		text.put("text", String.valueOf(so.getId()));
		jsonObject.put("cell_value", text);
		rows.add(jsonObject);
		// 佣金订单创建日期
		JSONObject jsonObject1 = new JSONObject();
		JSONObject text1 = new JSONObject();
		text1.put("text", sdf.format( so.getGmtCreate()));
		jsonObject1.put("cell_value", text1);
		rows.add(jsonObject1);
		// 客户支付日期
		if ( so.getReceiveDate() != null) {
			JSONObject jsonObject2 = new JSONObject();
			JSONObject text2 = new JSONObject();
			text2.put("text", sdf.format( so.getReceiveDate()));
			jsonObject2.put("cell_value", text2);
			rows.add(jsonObject2);
		} else {
			nullBuild(rows, so);
		}

		// Student Name
		if ( so.getApplicant() != null) {
			JSONObject jsonObject3 = new JSONObject();
			JSONObject text3 = new JSONObject();
			text3.put("text", so.getApplicant().getFirstname() + " " + so.getApplicant().getSurname());
			jsonObject3.put("cell_value", text3);
			rows.add(jsonObject3);
		} else {
			nullBuild(rows, so);
		}
		if ( so.getApplicant() != null) {
			JSONObject jsonObject4 = new JSONObject();
			JSONObject text4 = new JSONObject();
			text4.put("text", so.getStudentCode());
			jsonObject4.put("cell_value", text4);
			rows.add(jsonObject4);
		} else {
			JSONObject jsonObject4 = new JSONObject();
			JSONObject text4 = new JSONObject();
			text4.put("text", so.getStudentCode());
			jsonObject4.put("cell_value", text4);
			rows.add(jsonObject4);
		}
		if ( so.getBirthday() != null) {
			JSONObject jsonObject5 = new JSONObject();
			JSONObject text5 = new JSONObject();
			text5.put("text", sdf.format(so.getBirthday()));
			jsonObject5.put("cell_value", text5);
			rows.add(jsonObject5);
		} else {
			nullBuild(rows, so);
		}
		if ( so.getReceiveType() != null) {
			JSONObject jsonObject6 = new JSONObject();
			JSONObject text6 = new JSONObject();
			text6.put("text", so.getReceiveType().getName());
			jsonObject6.put("cell_value", text6);
			rows.add(jsonObject6);
		} else {
			nullBuild(rows, so);
		}
		if (so.getService() != null) {
			JSONObject jsonObject7 = new JSONObject();
			JSONObject text7 = new JSONObject();
			text7.put("text", so.getService().getName());
			jsonObject7.put("cell_value", text7);
			rows.add(jsonObject7);
		} else {
			nullBuild(rows, so);
		}
		JSONObject jsonObject8 = new JSONObject();
		JSONObject text8 = new JSONObject();
		text8.put("text", String.valueOf(so.isSettle()));
		jsonObject8.put("cell_value", text8);
		rows.add(jsonObject8);
		if ( so.getSchool() != null) {
			JSONObject jsonObject9 = new JSONObject();
			JSONObject text9 = new JSONObject();
			text9.put("text", so.getSchool().getName());
			jsonObject9.put("cell_value", text9);
			rows.add(jsonObject9);
		} else if (so.getSchoolInstitutionListDTO() != null) {
			JSONObject jsonObject9 = new JSONObject();
			JSONObject text9 = new JSONObject();
			text9.put("text", so.getSchoolInstitutionListDTO().getInstitutionTradingName());
			jsonObject9.put("cell_value", text9);
			rows.add(jsonObject9);

			JSONObject jsonObject10 = new JSONObject();
			JSONObject text10 = new JSONObject();
			text10.put("text", so.getSchoolInstitutionListDTO().getInstitutionName());
			jsonObject10.put("cell_value", text10);
			rows.add(jsonObject10);

			if ( so.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO() != null){
				JSONObject jsonObject11 = new JSONObject();
				JSONObject text11 = new JSONObject();
				text11.put("text", so.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO().getName());
				jsonObject11.put("cell_value", text11);
				rows.add(jsonObject11);

				JSONObject jsonObject12 = new JSONObject();
				JSONObject text12 = new JSONObject();
				text12.put("text", so.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO().getState());
				jsonObject12.put("cell_value", text12);
				rows.add(jsonObject12);

			} else {
				nullBuild(rows, so);
				nullBuild(rows, so);
			}
			if (so.getSchoolInstitutionListDTO().getSchoolCourseDO() != null) {
				JSONObject jsonObject13 = new JSONObject();
				JSONObject text13 = new JSONObject();
				text13.put("text", so.getSchoolInstitutionListDTO().getSchoolCourseDO().getCourseName());
				jsonObject13.put("cell_value", text13);
				rows.add(jsonObject13);
			} else {
				JSONObject jsonObject13 = new JSONObject();
				JSONObject text13 = new JSONObject();
				text13.put("text", so.getSchool().getSubject());
				jsonObject13.put("cell_value", text13);
				rows.add(jsonObject13);
			}

		} else {
			nullBuild(rows, so);
		}
		if ( so.getStartDate() != null) {
			JSONObject jsonObject14 = new JSONObject();
			JSONObject text14 = new JSONObject();
			text14.put("text", sdf.format( so.getStartDate()));
			jsonObject14.put("cell_value", text14);
			rows.add(jsonObject14);
		} else {
			nullBuild(rows, so);
		}
		if ( so.getEndDate() != null) {
			JSONObject jsonObject15 = new JSONObject();
			JSONObject text15 = new JSONObject();
			text15.put("text", sdf.format( so.getEndDate()));
			jsonObject15.put("cell_value", text15);
			rows.add(jsonObject15);
		} else {
			nullBuild(rows, so);
		}
//		if ( so.getInstallmentDueDate() != null) {
//			JSONObject jsonObject15 = new JSONObject();
//			JSONObject text15 = new JSONObject();
//			text15.put("text", sdf.format( so.getEndDate()));
//			jsonObject15.put("cell_value", text15);
//			rows.add(jsonObject15);
//		}
		JSONObject jsonObject16 = new JSONObject();
		JSONObject text16 = new JSONObject();
		text16.put("text", sdf.format( so.getInstallmentDueDate()));
		jsonObject16.put("cell_value", text16);
		rows.add(jsonObject16);
		
		if ( so.getReceiveType() != null) {
			JSONObject jsonObject17 = new JSONObject();
			JSONObject text17 = new JSONObject();
			text17.put("text", so.getReceiveType().getName());
			jsonObject17.put("cell_value", text17);
			rows.add(jsonObject17);
		} else {
			nullBuild(rows, so);
		}
		JSONObject jsonObject18 = new JSONObject();
		JSONObject text18 = new JSONObject();
		text18.put("text", String.valueOf(so.getTuitionFee()));
		jsonObject18.put("cell_value", text18);
		rows.add(jsonObject18);

		JSONObject jsonObject19 = new JSONObject();
		JSONObject text19 = new JSONObject();
		text19.put("text", String.valueOf(so.getPerAmount()));
		jsonObject19.put("cell_value", text19);
		rows.add(jsonObject19);

		JSONObject jsonObject20 = new JSONObject();
		JSONObject text20 = new JSONObject();
		text20.put("text", String.valueOf(so.getTotalPerAmountCNY()));
		jsonObject20.put("cell_value", text20);
		rows.add(jsonObject20);

		JSONObject jsonObject21 = new JSONObject();
		JSONObject text21 = new JSONObject();
		text21.put("text", String.valueOf(so.getTotalPerAmountAUD()));
		jsonObject21.put("cell_value", text21);
		rows.add(jsonObject21);

		JSONObject jsonObject22 = new JSONObject();
		JSONObject text22 = new JSONObject();
		text22.put("text", String.valueOf(so.getTotalAmountCNY()));
		jsonObject22.put("cell_value", text22);
		rows.add(jsonObject22);

		JSONObject jsonObject23 = new JSONObject();
		JSONObject text23 = new JSONObject();
		text23.put("text", String.valueOf(so.getTotalAmountAUD()));
		jsonObject23.put("cell_value", text23);
		rows.add(jsonObject23);

		JSONObject jsonObject24 = new JSONObject();
		JSONObject text24 = new JSONObject();
		text24.put("text", so.getCurrency());
		jsonObject24.put("cell_value", text24);
		rows.add(jsonObject24);

		JSONObject jsonObject25 = new JSONObject();
		JSONObject text25 = new JSONObject();
		text25.put("text", String.valueOf(so.getExchangeRate()));
		jsonObject25.put("cell_value", text25);
		rows.add(jsonObject25);

		JSONObject jsonObject26 = new JSONObject();
		JSONObject text26 = new JSONObject();
		text26.put("text", String.valueOf(so.getAmountCNY()));
		jsonObject26.put("cell_value", text26);
		rows.add(jsonObject26);

		JSONObject jsonObject27 = new JSONObject();
		JSONObject text27 = new JSONObject();
		text27.put("text", String.valueOf(so.getAmountAUD()));
		jsonObject27.put("cell_value", text27);
		rows.add(jsonObject27);

		JSONObject jsonObject28 = new JSONObject();
		JSONObject text28 = new JSONObject();
		text28.put("text", String.valueOf(so.getExpectAmountAUD()));
		jsonObject28.put("cell_value", text28);
		rows.add(jsonObject28);
		
		if ( so.isSettle()) {
			JSONObject jsonObject29 = new JSONObject();
			JSONObject text29 = new JSONObject();
			text29.put("text", String.valueOf(so.getExpectAmountAUD()));
			jsonObject29.put("cell_value", text29);
			rows.add(jsonObject29);
		} else {
			JSONObject jsonObject29 = new JSONObject();
			JSONObject text29 = new JSONObject();
			text29.put("text", String.valueOf(so.getSureExpectAmountAUD()));
			jsonObject29.put("cell_value", text29);
			rows.add(jsonObject29);
		}
		if (!regionService.isCN(_regionId)) {
			JSONObject jsonObject30 = new JSONObject();
			JSONObject text30 = new JSONObject();
			text30.put("text", String.valueOf(so.getGst()));
			jsonObject30.put("cell_value", text30);
			rows.add(jsonObject30);

			JSONObject jsonObject31 = new JSONObject();
			JSONObject text31 = new JSONObject();
			text31.put("text", String.valueOf(so.getDeductGst()));
			jsonObject31.put("cell_value", text31);
			rows.add(jsonObject31);
		}
		JSONObject jsonObject32 = new JSONObject();
		JSONObject text32 = new JSONObject();
		text32.put("text", String.valueOf(so.getSchoolPaymentAmount()));
		jsonObject32.put("cell_value", text32);
		rows.add(jsonObject32);
		if (ObjectUtil.isNotNull( so.getSchoolPaymentDate())) {
			JSONObject jsonObject33 = new JSONObject();
			JSONObject text33 = new JSONObject();
			text33.put("text", sdf.format( so.getSchoolPaymentDate()));
			jsonObject33.put("cell_value", text33);
			rows.add(jsonObject33);
		} else {
			nullBuild(rows, so);
		}

		JSONObject jsonObject34 = new JSONObject();
		JSONObject text34 = new JSONObject();
		text34.put("text", so.getInvoiceNumber());
		jsonObject34.put("cell_value", text34);
		rows.add(jsonObject34);
		if (ObjectUtil.isNotNull( so.getZyDate())) {
			JSONObject jsonObject35 = new JSONObject();
			JSONObject text35 = new JSONObject();
			text35.put("text", sdf.format( so.getZyDate()));
			jsonObject35.put("cell_value", text35);
			rows.add(jsonObject35);
		} else {
			nullBuild(rows, so);
		}
		// sub
		String subagencyName = subagencyService.getSubagencyByServiceOrderId( so.getServiceOrderId());
		if (StringUtil.isNotEmpty(subagencyName)) {
			JSONObject jsonObject36 = new JSONObject();
			JSONObject text36 = new JSONObject();
			text36.put("text", subagencyName);
			jsonObject36.put("cell_value", text36);
			rows.add(jsonObject36);
		} else {
			nullBuild(rows, so);
		}
		JSONObject jsonObject37 = new JSONObject();
		JSONObject text37 = new JSONObject();
		text37.put("text", String.valueOf(so.getBonus()));
		jsonObject37.put("cell_value", text37);
		rows.add(jsonObject37);
		if (ObjectUtil.isNotNull( so.getBonusDate())) {
			JSONObject jsonObject38 = new JSONObject();
			JSONObject text38 = new JSONObject();
			text38.put("text", sdf.format( so.getBonusDate()));
			jsonObject38.put("cell_value", text38);
			rows.add(jsonObject38);
		} else {
			nullBuild(rows, so);
		}
		JSONObject jsonObject39 = new JSONObject();
		JSONObject text39 = new JSONObject();
		text39.put("text", so.getVerifyCode());
		jsonObject39.put("cell_value", text39);
		rows.add(jsonObject39);
		String adviserName = adviserMap.get( so.getAdviserId());
		if (StringUtil.isNotEmpty(adviserName)) {
			JSONObject jsonObject40 = new JSONObject();
			JSONObject text40 = new JSONObject();
			text40.put("text", adviserName);
			jsonObject40.put("cell_value", text40);
			rows.add(jsonObject40);
		} else {
			nullBuild(rows, so);
		}
		JSONObject jsonObject41 = new JSONObject();
		JSONObject text41 = new JSONObject();
		text41.put("text", so.getState());
		jsonObject41.put("cell_value", text41);
		rows.add(jsonObject41);
		if (ObjectUtil.isNotNull( so.getKjApprovalDate())) {
			JSONObject jsonObject42 = new JSONObject();
			JSONObject text42 = new JSONObject();
			text42.put("text", sdf.format( so.getKjApprovalDate()));
			jsonObject42.put("cell_value", text42);
			rows.add(jsonObject42);
		} else {
			nullBuild(rows, so);
		}
		if (StringUtil.isNotEmpty( so.getRemarks())) {
			JSONObject jsonObject42 = new JSONObject();
			JSONObject text42 = new JSONObject();
			text42.put("text", so.getRemarks());
			jsonObject42.put("cell_value", text42);
			rows.add(jsonObject42);
		} else {
			nullBuild(rows, so);
		}
		return rows;
	}

	public void nullBuild(List<JSONObject> rows, CommissionOrderListDTO so) {
		JSONObject jsonObject9 = new JSONObject();
		JSONObject text9 = new JSONObject();
		text9.put("text", "");
		jsonObject9.put("cell_value", text9);
		rows.add(jsonObject9);
	}

	/*
	 *财务驳回状态为REFERED，顾问修改佣金信息之后再提交申请月奖
	 */
	@Deprecated
	@RequestMapping(value = "/updateSubmitted22", method = RequestMethod.POST)
	@ResponseBody
	public void updateSubmitted22(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
		CommissionOrderListDTO commissionOrderById = commissionOrderService.getCommissionOrderById(1012114);
		UserDTO userDTO = userService.getUserById(commissionOrderById.getUserId());
		AdviserDTO adviserDTO = adviserService.getAdviserById(commissionOrderById.getAdviserId());
		if (userDTO != null && adviserDTO != null){
			String message = "";
			message = adviserDTO.getName()+":"+userDTO.getName() + userDTO.getId() +","+sdf.format(commissionOrderById.getInstallmentDueDate())+ ",距离due date还有 "
					+  CommonUtils.getDateDays(commissionOrderById.getInstallmentDueDate(),new Date()) + " 天,请及时与学生沟通并申请月奖,如学生未就读请及时关闭订单,如已申请请忽略该提醒."
					+ "<br/><br/><a href='https://yongjinbiao.zhinanzhen.org/webroot_new/commissionorderdetail/ovst/id?" + commissionOrderById.getId() + "'>需要申请月奖的佣金订单链接</a>";
			SendEmailUtil.send(adviserDTO.getEmail(), userDTO.getName() + sdf.format(commissionOrderById.getInstallmentDueDate())+ " 请及时申请月奖",message);
		}
	}

	public List<String> buildExlceTitle(Integer _regionId) throws ServiceException {
		List<String> excelTitle = new ArrayList<>();
		excelTitle.add("服务备注");
		excelTitle.add("佣金备注");
		excelTitle.add("财务审核时间");
		excelTitle.add("状态");
		excelTitle.add("顾问");
		excelTitle.add("银行对账字段");
		excelTitle.add("月奖支付时间");
		excelTitle.add("月奖");
		excelTitle.add("Subagency");
		excelTitle.add("追佣时间");
		excelTitle.add("Invoice NO.");
		excelTitle.add("学校支付时间");
		excelTitle.add("学校支付金额");
		if (!regionService.isCN(_regionId)) {
			excelTitle.add("Deduct GST");
			excelTitle.add("GST");
		}
		excelTitle.add("确认预收业绩");
		excelTitle.add("Commission");
		excelTitle.add("本次收款澳币");
		excelTitle.add("本次收款人民币");
		excelTitle.add("创建订单时汇率");
		excelTitle.add("本次支付币种");
		excelTitle.add("总计已收澳币");
		excelTitle.add("总计已收人民币");
		excelTitle.add("总计应收澳币");
		excelTitle.add("总计应收人民币");
		excelTitle.add("Per Tuition Fee per Installment");
		excelTitle.add("Total Tuition Fee");
		excelTitle.add("收款 方式");
		excelTitle.add("Installment Due Date");
		excelTitle.add("Course End Date");
		excelTitle.add("Course Start Date");
		excelTitle.add("Course Name");
		excelTitle.add("State");
		excelTitle.add("Location Name");
		excelTitle.add("Institution Name");
		excelTitle.add("Institute/Institution Trading Name");
		excelTitle.add("是否提前扣佣");
		excelTitle.add("服务项目");
		excelTitle.add("收款方式");
		excelTitle.add("生日");
		excelTitle.add("Student ID");
		excelTitle.add("Student Name");
		excelTitle.add("客户支付日期");
		excelTitle.add("佣金订单创建日期");
		excelTitle.add("订单ID");
		return excelTitle;
	}

	private JSONObject buileExcelJsonObject(CommissionOrderListDTO so, Map<Integer, String> adviserMap, int regionId) throws ServiceException {
		List<JSONObject> jsonObjectFILEDTITLEList = new ArrayList<>();
		JSONObject jsonObjectFILEDTITLE = new JSONObject();
		JSONObject jsonObject = new JSONObject();
		//订单ID
		buildJsonobjectRow(String.valueOf(so.getId()), "订单ID", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		//佣金订单创建日期
		buildJsonobjectRow(sdf.format( so.getGmtCreate()), "佣金订单创建日期", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		//客户支付日期
		if ( so.getReceiveDate() != null) {
			buildJsonobjectRow(sdf.format( so.getReceiveDate()), "客户支付日期", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		} else {
			buildJsonobjectRow("", "客户支付日期", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		}
		//Student Name
		if ( so.getApplicant() != null) {
			buildJsonobjectRow(so.getApplicant().getFirstname(), "Student Name", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		} else {
			buildJsonobjectRow("", "Student Name", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		}
		//Student ID
		if ( so.getApplicant() != null) {
			buildJsonobjectRow(so.getStudentCode(), "Student ID", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		} else {
			buildJsonobjectRow("", "Student ID", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		}
		//生日
		if ( so.getBirthday() != null) {
			buildJsonobjectRow(sdf.format(so.getBirthday()), "生日", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		} else {
			buildJsonobjectRow("", "生日", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		}
		//收款方式
		if ( so.getReceiveType() != null) {
			buildJsonobjectRow(so.getReceiveType().getName(), "收款方式", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		} else {
			buildJsonobjectRow("", "收款方式", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		}
		//服务项目
		if (so.getService() != null) {
			buildJsonobjectRow(so.getService().getName(), "服务项目", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		} else {
			buildJsonobjectRow("", "服务项目", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		}
		//是否提前扣佣
		buildJsonobjectRow(String.valueOf(so.isSettle()), "是否提前扣佣", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		//Institute/Institution Trading Name
		//Institution Name
		if ( so.getSchool() != null) {
			buildJsonobjectRow(so.getSchool().getName(), "Institute/Institution Trading Name", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		} else if (so.getSchoolInstitutionListDTO() != null) {
			buildJsonobjectRow(so.getSchoolInstitutionListDTO().getInstitutionTradingName(), "Institute/Institution Trading Name", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);

			buildJsonobjectRow(so.getSchoolInstitutionListDTO().getInstitutionName(), "Institution Name", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);

			if ( so.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO() != null){
				//Location Name
				buildJsonobjectRow(so.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO().getName(), "Location Name", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
				//State
				buildJsonobjectRow(so.getSchoolInstitutionListDTO().getSchoolInstitutionLocationDO().getState(), "State", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);

			} else {
				buildJsonobjectRow("", "Location Name", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
				buildJsonobjectRow("", "State", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
			}
			if (so.getSchoolInstitutionListDTO().getSchoolCourseDO() != null) {
				//Course Name
				buildJsonobjectRow(so.getSchoolInstitutionListDTO().getSchoolCourseDO().getCourseName(), "Course Name", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
			} else {
				buildJsonobjectRow(so.getSchool().getSubject(), "Course Name", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
			}

		} else {
			buildJsonobjectRow("", "Institute/Institution Trading Name", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		}
		//Course Start Date
		if ( so.getStartDate() != null) {
			buildJsonobjectRow(sdf.format( so.getStartDate()), "Course Start Date", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		} else {
			buildJsonobjectRow("", "Course Start Date", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		}
		//Course End Date
		if ( so.getEndDate() != null) {
			buildJsonobjectRow(sdf.format( so.getEndDate()), "Course End Date", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		} else {
			buildJsonobjectRow("", "Course End Date", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		}
		//Installment Due Date
		buildJsonobjectRow(sdf.format( so.getInstallmentDueDate()), "Installment Due Date", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		//收款 方式
		if ( so.getReceiveType() != null) {
			buildJsonobjectRow(so.getReceiveType().getName(), "收款 方式", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		} else {
			buildJsonobjectRow("", "收款 方式", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		}
		//Total Tuition Fee
		buildJsonobjectRow(String.valueOf(so.getTuitionFee()), "Total Tuition Fee", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		//Per Tuition Fee per Installment
		buildJsonobjectRow(String.valueOf(so.getPerAmount()), "Per Tuition Fee per Installment", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		//总计应收人民币
		jsonObjectFILEDTITLE.put("总计应收人民币", so.getTotalPerAmountCNY());
		//总计应收澳币
		jsonObjectFILEDTITLE.put("总计应收澳币", so.getTotalPerAmountAUD());
		//总计已收人民币
		jsonObjectFILEDTITLE.put("总计已收人民币", so.getTotalAmountCNY());
		//总计已收澳币
		jsonObjectFILEDTITLE.put("总计已收澳币", so.getTotalAmountAUD());
		//本次支付币种
		buildJsonobjectRow(so.getCurrency(), "本次支付币种", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		//创建订单时汇率
		jsonObjectFILEDTITLE.put("创建订单时汇率", so.getExchangeRate());
		//本次收款人民币
		jsonObjectFILEDTITLE.put("本次收款人民币", so.getAmountCNY());
		//本次收款澳币
		jsonObjectFILEDTITLE.put("本次收款澳币", so.getAmountAUD());
		//Commission
		jsonObjectFILEDTITLE.put("Commission", so.getExpectAmountAUD());
		//确认预收业绩
		if ( so.isSettle()) {
			jsonObjectFILEDTITLE.put("确认预收业绩", so.getExpectAmountAUD());
			buildJsonobjectRow(String.valueOf(so.getExpectAmountAUD()), "确认预收业绩", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		} else {
			jsonObjectFILEDTITLE.put("确认预收业绩", so.getSureExpectAmountAUD());
		}
		if (!regionService.isCN(regionId)) {
			//GST
			jsonObjectFILEDTITLE.put("GST", so.getGst());
			//Deduct GST
			jsonObjectFILEDTITLE.put("Deduct GST", so.getDeductGst());
		}
		//学校支付金额
		jsonObjectFILEDTITLE.put("学校支付金额", so.getSchoolPaymentAmount());
		//学校支付时间
		if (ObjectUtil.isNotNull( so.getSchoolPaymentDate())) {
			buildJsonobjectRow(sdf.format( so.getSchoolPaymentDate()), "学校支付时间", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		} else {
			buildJsonobjectRow("", "学校支付时间", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		}
		//Invoice NO.
		if (so.getInvoiceNumber() != null) {
			buildJsonobjectRow(so.getInvoiceNumber(), "Invoice NO.", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		} else {
			buildJsonobjectRow("", "Invoice NO.", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		}
		//追佣时间
		if (ObjectUtil.isNotNull( so.getZyDate())) {
			buildJsonobjectRow(sdf.format( so.getZyDate()), "追佣时间", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		} else {
			buildJsonobjectRow("", "追佣时间", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		}
		//Subagency
		String subagencyName = subagencyService.getSubagencyByServiceOrderId( so.getServiceOrderId());
		if (StringUtil.isNotEmpty(subagencyName)) {
			buildJsonobjectRow(subagencyName, "Subagency", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		} else {
			buildJsonobjectRow("", "Subagency", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		}
		//月奖
		jsonObjectFILEDTITLE.put("月奖", so.getBonus());
		//月奖支付时间
		if (ObjectUtil.isNotNull( so.getBonusDate())) {
			buildJsonobjectRow(sdf.format( so.getBonusDate()), "月奖支付时间", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		} else {
			buildJsonobjectRow("", "月奖支付时间", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		}
		//银行对账字段
		if (so.getVerifyCode() != null) {
			buildJsonobjectRow(so.getVerifyCode(), "银行对账字段", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		} else {
			buildJsonobjectRow("", "银行对账字段", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		}
		//顾问
		String adviserName = adviserMap.get( so.getAdviserId());
		if (StringUtil.isNotEmpty(adviserName)) {
			buildJsonobjectRow(adviserName, "顾问", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		} else {
			buildJsonobjectRow("", "顾问", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		}
		//状态
		buildJsonobjectRow(so.getState(), "状态", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		//财务审核时间
		if (ObjectUtil.isNotNull( so.getKjApprovalDate())) {
			buildJsonobjectRow(sdf.format( so.getKjApprovalDate()), "财务审核时间", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		} else {
			buildJsonobjectRow("", "财务审核时间", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		}
		//佣金备注
		if (StringUtil.isNotEmpty( so.getRemarks())) {
			buildJsonobjectRow(so.getRemarks(), "佣金备注", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		} else {
			buildJsonobjectRow("", "佣金备注", jsonObject, jsonObjectFILEDTITLEList, jsonObjectFILEDTITLE);
		}
		//服务备注
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
}
