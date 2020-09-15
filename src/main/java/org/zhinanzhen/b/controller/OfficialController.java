package org.zhinanzhen.b.controller;

import java.io.IOException;
import java.util.List;

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
import org.zhinanzhen.b.service.OfficialService;
import org.zhinanzhen.b.service.OfficialStateEnum;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.pojo.AdminUserDTO;
import org.zhinanzhen.tb.service.pojo.AdviserDTO;
import org.zhinanzhen.b.service.pojo.OfficialDTO;

import com.ikasoa.core.utils.StringUtil;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/official")
public class OfficialController extends BaseController {

	@Resource
	OfficialService officialService;

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
			HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			OfficialDTO officialDto = new OfficialDTO();
			officialDto.setId(id);
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
			if (isOfficialAdmin) {
				AdminUserDTO adminUser = adminUserService.getAdminUserByUsername(officialDto.getEmail());
				if (adminUser != null && isOfficialAdmin != null)
					adminUserService.updateOfficialAdmin(adminUser.getId(), isOfficialAdmin);
				else
					return new Response<OfficialDTO>(0, "文案管理员修改失败.", officialDto);
			} else if (officialService.updateOfficial(officialDto) > 0) {
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
	public Response<List<OfficialDTO>> listOfficial(@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "regionId", required = false) Integer regionId,
			@RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<List<OfficialDTO>>(0, officialService.listOfficial(name, regionId, pageNum, pageSize));
		} catch (ServiceException e) {
			return new Response<List<OfficialDTO>>(1, e.getMessage(), null);
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

}
