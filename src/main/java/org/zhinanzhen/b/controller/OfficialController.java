package org.zhinanzhen.b.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.zhinanzhen.b.dao.pojo.ServiceOrderDO;
import org.zhinanzhen.b.service.OfficialGradeService;
import org.zhinanzhen.b.service.OfficialService;
import org.zhinanzhen.b.service.OfficialStateEnum;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.pojo.OfficialGradeDTO;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.ListResponse;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.pojo.AdminUserDTO;
import org.zhinanzhen.b.service.pojo.OfficialDTO;

import com.ikasoa.core.utils.StringUtil;

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

	public enum OfficialWorkStateEnum{
		NORMAL ("正常"), BUSY ("忙碌");
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
			List<OfficialDTO> officialDtoList = officialService.listOfficial(null, null, 0, 1000);
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
			return new Response<Integer>(0, officialService.countOfficial(name, regionId));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public ListResponse<List<OfficialDTO>> listOfficial(@RequestParam(value = "name", required = false) String name,
														@RequestParam(value = "regionId", required = false) Integer regionId,
														@RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
														HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			int total = officialService.countOfficial(name, regionId);
			return new ListResponse<List<OfficialDTO>>(true,pageSize, total,officialService.listOfficial(name, regionId, pageNum, pageSize), "success");
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
			List<OfficialDTO> officialDtoList = officialService.listOfficial(null, null, 0, 1000);
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
		try {
			List<ServiceOrderDTO> serviceOrderLongVisa = serviceOrderService.OfficialHandoverServiceOrder(officialId);
			for (ServiceOrderDTO s : serviceOrderLongVisa) {
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
}