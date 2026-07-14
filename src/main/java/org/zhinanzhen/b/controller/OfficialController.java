package org.zhinanzhen.b.controller;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zhinanzhen.b.dao.OfficialDAO;
import org.zhinanzhen.b.dao.pojo.OfficialDO;
import org.zhinanzhen.b.dao.pojo.OfficialEvaluate;
import org.zhinanzhen.b.dao.pojo.ServiceOrderDO;
import org.zhinanzhen.b.service.*;
import org.zhinanzhen.b.service.pojo.OfficialGradeDTO;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.b.service.pojo.WebLogDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.ListResponse;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.dao.pojo.ServiceOrderOriginallyDO;
import org.zhinanzhen.tb.service.AdviserService;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.pojo.AdminUserDTO;
import org.zhinanzhen.b.service.pojo.OfficialDTO;

import com.ikasoa.core.utils.StringUtil;
import org.zhinanzhen.tb.service.pojo.AdviserDTO;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/official")
public class OfficialController extends BaseController {

	@Resource
	OfficialService officialService;

	@Resource
	OfficialGradeService officialGradeService;

	@Resource
	ServiceOrderService serviceOrderService;

	@Resource
	private ServiceOrderOriginallyService serviceOrderOriginallyService;

	@Resource
	private WebLogService webLogService;

	@Resource
	private AdviserService adviserService;

	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

	public enum OfficialWorkStateEnum{
		NORMAL ("正常"), BUSY ("忙碌"), RESIGN("离职");
		private String comment;
		private OfficialWorkStateEnum(String comment){
			this.comment = comment;
		}
		public static OfficialWorkStateEnum get (String name){
			for(OfficialWorkStateEnum e : OfficialWorkStateEnum.values()){
				if (name.equalsIgnoreCase(e.toString())){
					return e;
				}
			}
			return OfficialWorkStateEnum.NORMAL;
		}
	}

	@RequestMapping(value = "/upload_img", method = RequestMethod.POST)
	@ResponseBody
	public Response<String> uploadLogo(@RequestParam MultipartFile file, HttpServletRequest request,
			HttpServletResponse response) throws IllegalStateException, IOException {
		super.setPostHeader(response);
		return super.upload(file, request.getSession(), "/uploads/official_img/");
	}

	// curl -X POST -d
	// 'name=sulei&phone=0404987526&email=leisu@zhinanzhen.org&imageUrl=/logo.jpg&regionId=10000000'
	// "http://localhost:8080/admin/official/add"
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> addOfficial(@RequestParam(value = "name") String name,
			@RequestParam(value = "phone") String phone, @RequestParam(value = "email") String email,
			@RequestParam(value = "password", required = false) String password,
			@RequestParam(value = "imageUrl") String imageUrl, @RequestParam(value = "regionId") Integer regionId,
			@RequestParam(value = "specialty",required = false) String specialty,
			@RequestParam(value = "grade",required = false)String grade,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			List<OfficialDTO> officialDtoList = officialService.listOfficial(null, null, null, false,0, 1000);
			for (OfficialDTO officialDto : officialDtoList) {
				if (officialDto.getPhone().equals(phone)) {
					return new Response<Integer>(1, "该电话号已被使用,添加失败.", 0);
				}
				if (officialDto.getEmail().equals(email)) {
					return new Response<Integer>(1, "该邮箱已被使用,添加失败.", 0);
				}
			}
			if (adminUserService.getAdminUserByUsername(email) != null)
				return new Response<Integer>(1, "该邮箱已被管理员使用,添加失败.", 0);
			OfficialDTO officialDto = new OfficialDTO();
			officialDto.setName(name);
			officialDto.setPhone(phone);
			officialDto.setEmail(email);
			officialDto.setImageUrl(imageUrl);
			officialDto.setRegionId(regionId);
			officialDto.setSpecialty(specialty);
			officialDto.setWorkState(OfficialWorkStateEnum.NORMAL.toString());
			OfficialGradeDTO officialGradeDTO = officialGradeService.getOfficialGradeByGrade(grade);
			if (officialGradeDTO!=null){
				int gradeId = officialGradeDTO.getId();
				officialDto.setGradeId(gradeId);
			}
			else
				return new Response<Integer>(0,"没有找到对应等级",0);
			if (officialService.addOfficial(officialDto) > 0) {
				if (password == null)
					password = email; // 如果没有传入密码,则密码和email相同
				adminUserService.add(email, password, "WA", null, null, officialDto.getId(), null, null);
				return new Response<Integer>(0, officialDto.getId());
			} else {
				return new Response<Integer>(0, "创建失败.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public Response<OfficialDTO> updateOfficial(@RequestParam(value = "id") int id,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "phone", required = false) String phone,
			@RequestParam(value = "email", required = false) String email,
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "imageUrl", required = false) String imageUrl,
			@RequestParam(value = "regionId", required = false) Integer regionId,
			@RequestParam(value = "isOfficialAdmin", required = false) Boolean isOfficialAdmin,
			@RequestParam(value = "specialty", required = false) String specialty,
			@RequestParam(value = "workState", required = false) String workState,
			@RequestParam(value = "grade",required = false)String grade,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			if (id <= 0)
				return new Response<OfficialDTO>(1, "请输入有效id.", null);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo == null || (adminUserLoginInfo != null && !
					(adminUserLoginInfo.isOfficialAdmin() || "SUPERAD".equals(adminUserLoginInfo.getApList()))) )
				return new Response(1,"No permission !");
			OfficialDTO officialDto = officialService.getOfficialById(id);
			String _email = officialDto.getEmail();
			if (StringUtil.isNotEmpty(name)) {
				officialDto.setName(name);
			}
			if (StringUtil.isNotEmpty(phone)) {
				officialDto.setPhone(phone);
			}
			if (StringUtil.isNotEmpty(email)) {
				officialDto.setEmail(email);
			}
			if (StringUtil.isNotEmpty(state)) {
				officialDto.setState(OfficialStateEnum.get(state));
			}
			if (StringUtil.isNotEmpty(imageUrl)) {
				officialDto.setImageUrl(imageUrl);
			}
			if (regionId != null && regionId > 0) {
				officialDto.setRegionId(regionId);
			}
			if (StringUtil.isNotEmpty(specialty)) {
				officialDto.setSpecialty(specialty);
			}
			if (StringUtil.isNotEmpty(grade)) {
				OfficialGradeDTO officialGradeDTO = officialGradeService.getOfficialGradeByGrade(grade);
				if (officialGradeDTO!=null){
				int gradeId = officialGradeDTO.getId();
				officialDto.setGradeId(gradeId);
				}
				else
					return new Response<OfficialDTO>(0, "没有找到对应等级", null);
			}
			AdminUserDTO adminUser = adminUserService.getAdminUserByUsername(_email);
			if (isOfficialAdmin != null) {
				if (adminUser != null && isOfficialAdmin != null)
					adminUserService.updateOfficialAdmin(adminUser.getId(), isOfficialAdmin);
				else
					return new Response<OfficialDTO>(0, "文案管理员修改失败.", officialDto);
			}
			if (StringUtil.isNotEmpty(workState) && workState.equals(OfficialWorkStateEnum.get(workState).toString()))
				officialDto.setWorkState(OfficialWorkStateEnum.get(workState).toString());
			if (officialService.updateOfficial(officialDto) > 0) {
				if (StringUtil.isNotEmpty(email) && !email.equalsIgnoreCase(adminUser.getUsername()))
					adminUserService.updateUsername(adminUser.getId(), email);
				return new Response<OfficialDTO>(0, officialDto);
			} else
				return new Response<OfficialDTO>(1, "修改失败.", null);
		} catch (ServiceException e) {
			return new Response<OfficialDTO>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/count", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> countOfficial(@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "regionId", required = false) Integer regionId, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, officialService.countOfficial(name, regionId, null));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public ListResponse<List<OfficialDTO>> listOfficial(@RequestParam(value = "name", required = false) String name,
														@RequestParam(value = "regionId", required = false) Integer regionId,
														@RequestParam(value = "gradeId", required = false) Integer gradeId,
														@RequestParam(value = "isbuiltOrder", required = false) boolean isbuiltOrder,
														@RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
														HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			int total = officialService.countOfficial(name, regionId, gradeId);
			return new ListResponse<List<OfficialDTO>>(true,pageSize, total,officialService.listOfficial(name, regionId, gradeId, isbuiltOrder, pageNum, pageSize), "success");
		} catch (ServiceException e) {
			return new ListResponse<List<OfficialDTO>>(false, pageSize, 0, null, e.getMessage());
		}
	}

	@RequestMapping(value = "/get", method = RequestMethod.GET)
	@ResponseBody
	public Response<OfficialDTO> getOfficial(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<OfficialDTO>(0, officialService.getOfficialById(id));
		} catch (ServiceException e) {
			return new Response<OfficialDTO>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/syncAdminUser", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> syncAdminUser(HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			int num = 0;
			List<OfficialDTO> officialDtoList = officialService.listOfficial(null, null, null, false,0, 1000);
			for (OfficialDTO officialDto : officialDtoList) {
				AdminUserDTO adminUser = adminUserService.getAdminUserByUsername(officialDto.getEmail());
				if (adminUser == null) {
					adminUserService.add(officialDto.getEmail(), officialDto.getEmail(), "WA", null, null,
							officialDto.getId(), null, null);
					num++;
				} else
					adminUserService.updateOfficialId(adminUser.getId(), officialDto.getId());
			}
			return new Response<Integer>(0, num);
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), null);
		}
	}

	/**
	 * 文案管理员 可以修改文案的工作状态 忙碌/正常
	 * @return
	 */
	@RequestMapping(value = "/updateWorkState" , method = RequestMethod.POST)
	@ResponseBody
	public Response<String> updateWorkState(@RequestParam(value = "id") int id,
											@RequestParam(value = "workState") String workState,
											HttpServletRequest request, HttpServletResponse response) throws ServiceException {
		super.setPostHeader(response);
		AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
		if (adminUserLoginInfo == null || (adminUserLoginInfo != null && ! adminUserLoginInfo.isOfficialAdmin()) )
			return new Response(1,"No permission !");
		OfficialDTO officialDTO = officialService.getOfficialById(id);
		if (officialDTO == null)
			return new Response<>(0,"修改工作状态失败!");
		if (StringUtil.isNotEmpty(workState) && ! workState.equals(OfficialWorkStateEnum.get(workState).toString()))
			return new Response(1,"状态参数: workState error : " + workState);
		officialDTO.setWorkState(OfficialWorkStateEnum.get(workState).toString());
		if (officialService.updateWorkState(officialDTO) > 0)
			return new Response<>(0,"success");
		return  new Response<>(0,"fail");
	}

	//文案交接
	@RequestMapping(value = "/officialHandover", method = RequestMethod.PUT)
	@ResponseBody
	public Response<String> officialHandover(@RequestParam(value = "officialId") Integer officialId,
											 @RequestParam(value = "newOfficialId") Integer newOfficialId,
											 HttpServletRequest request) {
		if(officialId.equals(newOfficialId)){
			return new Response<>(0, "交接文案不能相同");
		}
		AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
		if (adminUserLoginInfo == null ){
			return new Response(1,"No permission !");
		}
		String apList = adminUserLoginInfo.getApList();
		switch (apList) {
			case "GW":
				apList = "顾问";
				break;
			case "WA":
				apList = "文案";
				break;
			case "KJ":
				apList = "会计";
				break;
			case "SUPERAD":
				apList = "超级管理员";
				break;
			default: apList = apList;
		}

		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			List<ServiceOrderDTO> serviceOrderLongVisa = serviceOrderService.OfficialHandoverServiceOrder(officialId);
			for (ServiceOrderDTO s : serviceOrderLongVisa) {
				WebLogDTO webLogDTO = new WebLogDTO();
				webLogDTO.setUserId(adminUserLoginInfo.getId());
				webLogDTO.setRole(apList);
				webLogDTO.setServiceOrderId(s.getId());
				webLogDTO.setStartTime(sdf.format(new Date()));
				webLogDTO.setUri("/admin_v2.1/adviserData/officialHandover");
				webLogService.addWebLogs(webLogDTO);

				ServiceOrderOriginallyDO serviceOrderOriginallyDO = new ServiceOrderOriginallyDO();
				serviceOrderOriginallyDO.setServiceOrderId(s.getId());
				serviceOrderOriginallyDO.setOfficialId(s.getOfficialId());
				serviceOrderOriginallyDO.setNewOfficialId(newOfficialId);
				serviceOrderOriginallyDO.setWebLogId(webLogDTO.getId());
				serviceOrderOriginallyService.addServiceOrderOriginallyDO(serviceOrderOriginallyDO);

				s.setOfficialId(newOfficialId);
				serviceOrderService.updateOfficial(s.getId(),officialId,newOfficialId);
			}
			OfficialDTO officialDTO = new OfficialDTO();
			officialDTO.setWorkState("BUSY");
			officialDTO.setSpecialty("已离职交接给"+officialService.getOfficialById(newOfficialId).getName());
			officialDTO.setId(officialId);
			officialService.updateOfficial(officialDTO);
		} catch (ServiceException e) {
			return new Response<>(0, "fail");
		}
		return new Response<>(0, "success");
	}

	// 添加文案评分
	@RequestMapping(value = "/addOfficialEvaluate", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> addOfficialEvaluate(@RequestBody List<OfficialEvaluate> officialEvaluateList,

//			@RequestParam(value = "officialId") Integer officialId,
//											 @RequestParam(value = "adviserId") Integer adviserId,
//											 @RequestParam(value = "professionalism") String professionalism,
//											 @RequestParam(value = "accuracy") String accuracy,
//											 @RequestParam(value = "timelyCommunication") String timelyCommunication,
//											 @RequestParam(value = "collaborationTime") String collaborationTime,
//											 @RequestParam(value = "reasonLowScore", required = false) String reasonLowScore,
//											 @RequestParam(value = "remark", required = false) String remark,
											 HttpServletRequest request) {
		OfficialEvaluate officialEvaluate = new OfficialEvaluate();
//		if (officialId != null) {
//			officialEvaluate.setOfficialId(officialId);
//		}
//		if (adviserId != null) {
//			officialEvaluate.setAdviserId(adviserId);
//		}
//		if (professionalism != null) {
//			officialEvaluate.setProfessionalism(professionalism);
//		}
//		if (accuracy != null) {
//			officialEvaluate.setAccuracy(accuracy);
//		}
//		if (timelyCommunication != null) {
//			officialEvaluate.setTimelyCommunication(timelyCommunication);
//		}
//		if (collaborationTime != null) {
//			officialEvaluate.setCollaborationTime(collaborationTime);
//		}
//		if (reasonLowScore != null) {
//			officialEvaluate.setReasonLowScore(reasonLowScore);
//		}
//		if (remark != null) {
//			officialEvaluate.setRemark(remark);
//		}
		for (OfficialEvaluate evaluate : officialEvaluateList) {
			int i = officialService.addOfficialEvaluate(evaluate);
			if (i == evaluate.getOfficialId()) {
				try {
					OfficialDTO officialById = officialService.getOfficialById(evaluate.getOfficialId());
					return new Response<Integer>(1, "该月 " + officialById.getName() + " 已添加评分.", officialEvaluate.getId());
				} catch (ServiceException e) {
                    throw new RuntimeException(e);
                }
            }
		}
		return new Response<Integer>(0, "添加成功", officialEvaluate.getId());
//		if (addRe > 0) {
//			return new Response<Integer>(0, "添加成功", officialEvaluate.getId());
//		} else if (addRe == -1) {
//			return new Response<Integer>(1, "该月已添加评分.");
//		} else {
//			return new Response<Integer>(1, "添加失败.");
//		}
	}


	// 修改文案评分
	@RequestMapping(value = "/updateOfficialEvaluate", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> updateOfficialEvaluate(@RequestParam(value = "id") Integer id,
			@RequestParam(value = "officialEvaluate", required = false) String officialEvaluateT,
//			@RequestParam(value = "officialId") Integer officialId,
//												 @RequestParam(value = "adviserId") Integer adviserId,
//												 @RequestParam(value = "professionalism") String professionalism,
//												 @RequestParam(value = "accuracy") String accuracy,
//												 @RequestParam(value = "timelyCommunication") String timelyCommunication,
//												 @RequestParam(value = "collaborationTime") String collaborationTime,
//												 @RequestParam(value = "remark", required = false) String remark,
												 HttpServletRequest request) {

		OfficialEvaluate officialEvaluateN = officialService.getOfficialEvaluate(null, null, null, null, id);
		if (officialEvaluateN == null) {
			return new Response<Integer>(1, "该评分不存在.");
		}
		try {
			JSONObject jsonObject = JSONObject.parseObject(officialEvaluateT);
			OfficialEvaluate officialEvaluate = new OfficialEvaluate();
			officialEvaluate.setId(id);
			officialEvaluate.setOfficialId(jsonObject.getInteger("officialId"));
			officialEvaluate.setAdviserId(jsonObject.getInteger("adviserId"));
			officialEvaluate.setProfessionalism(jsonObject.getString("professionalism"));
			officialEvaluate.setAccuracy(jsonObject.getString("accuracy"));
			officialEvaluate.setTimelyCommunication(jsonObject.getString("timelyCommunication"));
			officialEvaluate.setCollaborationTime(jsonObject.getString("collaborationTime") + "-15 12:00:00");
			officialEvaluate.setRemark(jsonObject.getString("remark"));
			int addRe = officialService.updateOfficialEvaluate(officialEvaluate);
			if (addRe > 0) {
				return new Response<Integer>(0, "修改成功", officialEvaluate.getId());
			} else {
				return new Response<Integer>(1, "修改失败.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	@RequestMapping(value = "/listCooperationEvaluate", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<OfficialDTO>> listcooperationEvaluate(@RequestParam(value = "adviserId", required = false) Integer adviserId,
																  @RequestParam(value = "collaborationTime", required = false) String collaborationTime,
																  HttpServletRequest request) {
		try {
			String isAllCooperation = "false";
			int count = 0;
			// 解析年月字符串
			YearMonth yearMonth = YearMonth.parse(collaborationTime, DateTimeFormatter.ofPattern("yyyy-MM"));

			// 获取月初第一天 00:00:00
			LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();

			// 获取月末最后一天 23:59:59
			LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

			// 创建格式化器
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

			// 格式化为字符串
			String startCollaborationTime = startOfMonth.format(formatter);
			String endCollaborationTime = endOfMonth.format(formatter);

			List<OfficialDTO> officialDOS = new ArrayList<>();
			List<Integer> officials = serviceOrderService.listCooperationOfficial(adviserId, startCollaborationTime, endCollaborationTime);
			if (officials != null) {
				for (Integer integer : officials) {
					if (integer == 0) {
						continue;
					}
					OfficialEvaluate officialEvaluate = officialService.getOfficialEvaluate(integer, adviserId, startCollaborationTime, endCollaborationTime, null);
					if (officialEvaluate != null) {
						count++;
						continue;
					}
					OfficialDTO officialById = officialService.getOfficialById(integer);
					if (officialById != null) {
						officialDOS.add(officialById);
					}
				}
				if (count == officials.size() && count != 0) {
					isAllCooperation = "true";
				}
			}

			return new Response<List<OfficialDTO>>(0, isAllCooperation, officialDOS);
		} catch (ServiceException e) {
            throw new RuntimeException(e);
        }

    }

	@RequestMapping(value = "/listOfficialEvaluate", method = RequestMethod.GET)
	@ResponseBody
	public ListResponse<List<OfficialEvaluate>> listOfficialEvaluate(@RequestParam(value = "officialId", required = false) Integer officialId,
												 @RequestParam(value = "adviserId", required = false) Integer adviserId,
												 @RequestParam(value = "startCollaborationTime", required = false) String startCollaborationTime,
												 @RequestParam(value = "endCollaborationTime", required = false) String endCollaborationTime,
												 @RequestParam(value = "collaborationTime", required = false) String collaborationTime,
												 @RequestParam(value = "pageNum") Integer pageNum, @RequestParam(value = "pageSize") Integer pageSize,
												 HttpServletRequest request) throws ServiceException {
		Integer adviserId1 = getAdviserId(request);
		if (adviserId1 != null) {
			adviserId = adviserId1;
		}
		List<Integer> officialIds = new ArrayList<>();
		if (officialId == null) {
			officialIds = serviceOrderService.listCooperationOfficial(adviserId, startCollaborationTime, endCollaborationTime);
		} else {
			officialIds.add(officialId);
		}
		if (officialIds.isEmpty()) {
			officialIds = null;
		}
		if (collaborationTime != null) {
			// 解析年月字符串
			YearMonth yearMonth = YearMonth.parse(collaborationTime, DateTimeFormatter.ofPattern("yyyy-MM"));

			// 获取月初第一天 00:00:00
			LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();

			// 获取月末最后一天 23:59:59
			LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

			// 创建格式化器
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

			// 格式化为字符串
			startCollaborationTime = startOfMonth.format(formatter);
			endCollaborationTime = endOfMonth.format(formatter);
		}
		int total = officialService.countOfficialEvaluate(officialIds, adviserId, startCollaborationTime, endCollaborationTime);
		List<OfficialEvaluate> officialEvaluates = officialService.listOfficialEvaluate(officialIds, adviserId, startCollaborationTime, endCollaborationTime, pageNum, pageSize);
		if (officialEvaluates != null) {
			for (OfficialEvaluate officialEvaluate : officialEvaluates) {
				AdviserDTO adviserById = adviserService.getAdviserById(officialEvaluate.getAdviserId());
				if (adviserById != null) {
					officialEvaluate.setEvaluateAdviser(adviserById.getName());
				}
				OfficialDTO officialById = officialService.getOfficialById(officialEvaluate.getOfficialId());
				officialEvaluate.setEvaluateOfficial(officialById.getName());
                Double averageScore = officialService.getAverageScore(officialEvaluate, collaborationTime, 1, true);
				officialEvaluate.setAverageScore(DECIMAL_FORMAT.format(averageScore));
                officialById.setOfficialEvaluate(officialEvaluate);
			}
			String isSuccess = "false";
			if (officialIds != null && officialIds.size() == officialEvaluates.size()) {
				isSuccess = "true";
			}
			return new ListResponse<List<OfficialEvaluate>>(true, pageSize, total, officialEvaluates, isSuccess);
		} else {
			return new ListResponse<List<OfficialEvaluate>>(false, pageSize, total, null, "");
		}
	}
}
