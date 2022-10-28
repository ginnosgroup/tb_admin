package org.zhinanzhen.tb.controller;

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
import org.zhinanzhen.tb.service.AdminUserService;
import org.zhinanzhen.tb.service.AdviserService;
import org.zhinanzhen.tb.service.AdviserStateEnum;
import org.zhinanzhen.tb.service.RegionService;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.pojo.AdminUserDTO;
import org.zhinanzhen.tb.service.pojo.AdviserDTO;
import org.zhinanzhen.tb.service.pojo.RegionDTO;

import com.ikasoa.core.utils.ListUtil;
import com.ikasoa.core.utils.StringUtil;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/adviser")
public class AdviserController extends BaseController {

	@Resource
	AdviserService adviserService;

	@Resource
	AdminUserService adminUserService;
	
	@Resource
	RegionService regionService;

	@RequestMapping(value = "/upload_img", method = RequestMethod.POST)
	@ResponseBody
	public Response<String> uploadLogo(@RequestParam MultipartFile file, HttpServletRequest request,
			HttpServletResponse response) throws IllegalStateException, IOException {
		super.setPostHeader(response);
		return super.upload(file, request.getSession(), "/uploads/adviser_img/");
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> addAdviser(@RequestParam(value = "name") String name,
			@RequestParam(value = "phone") String phone, @RequestParam(value = "email") String email,
			@RequestParam(value = "password", required = false) String password,
			@RequestParam(value = "imageUrl", required = false) String imageUrl, @RequestParam(value = "regionId") Integer regionId,
			@RequestParam(value = "adminRegionId", required = false) Integer adminRegionId, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			List<AdviserDTO> adviserDtoList = adviserService.listAdviser(null, null, 0, 1000);
			for (AdviserDTO adviserDto : adviserDtoList) {
				if (adviserDto.getPhone().equals(phone)) {
					return new Response<Integer>(1, "该电话号已被使用,添加失败.", 0);
				}
				if (adviserDto.getEmail().equals(email)) {
					return new Response<Integer>(1, "该邮箱已被使用,添加失败.", 0);
				}
			}

			if (adminUserService.getAdminUserByUsername(email) != null)
				return new Response<Integer>(1, "该邮箱已被管理员使用,添加失败.", 0);

			AdviserDTO adviserDto = new AdviserDTO();
			adviserDto.setName(name);
			adviserDto.setPhone(phone);
			adviserDto.setEmail(email);
			adviserDto.setImageUrl(imageUrl);
			adviserDto.setRegionId(regionId);
			if (adviserService.addAdviser(adviserDto) > 0) {
				if (password == null)
					password = email; // 如果没有传入密码,则密码和email相同
				adminUserService.add(email, password, "GW", adviserDto.getId(), null, null, null, adminRegionId);
				return new Response<Integer>(0, adviserDto.getId());
			} else {
				return new Response<Integer>(0, "创建失败.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public Response<AdviserDTO> updateAdviser(@RequestParam(value = "id") int id,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "phone", required = false) String phone,
			@RequestParam(value = "email", required = false) String email,
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "imageUrl", required = false) String imageUrl,
			@RequestParam(value = "regionId", required = false) Integer regionId,
			@RequestParam(value = "adminRegionId", required = false) Integer adminRegionId, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			AdviserDTO adviserDto = adviserService.getAdviserById(id);
			if (StringUtil.isNotEmpty(name)) {
				adviserDto.setName(name);
			}
			if (StringUtil.isNotEmpty(phone)) {
				adviserDto.setPhone(phone);
			}
			if (StringUtil.isNotEmpty(email)) {
				adviserDto.setEmail(email);
			}
			if (StringUtil.isNotEmpty(state)) {
				adviserDto.setState(AdviserStateEnum.get(state));
			}
			if (StringUtil.isNotEmpty(imageUrl)) {
				adviserDto.setImageUrl(imageUrl);
			}
			if (regionId != null && regionId > 0) {
				adviserDto.setRegionId(regionId);
			}
			if (adviserService.updateAdviser(adviserDto) > 0) {
				AdminUserDTO adminUser = adminUserService.getAdminUserByUsername(adviserDto.getEmail());
				if (adminUser != null) {
					adminUserService.updateRegionId(adminUser.getId(), adminRegionId);
					if (StringUtil.isNotEmpty(email) && !email.equalsIgnoreCase(adminUser.getUsername()))
						adminUserService.updateUsername(id, email);
				} else
					return new Response<AdviserDTO>(0, "顾问修改成功,但修改顾问管理员区域失败.(没有找到管理员帐号:" + adviserDto.getEmail() + ")",
							null);
				return new Response<AdviserDTO>(0, adviserDto);
			} else {
				return new Response<AdviserDTO>(1, "修改失败.", null);
			}
		} catch (ServiceException e) {
			return new Response<AdviserDTO>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/count", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> countAdviser(@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "regionId", required = false) Integer regionId, HttpServletResponse response) {
		try {
			super.setGetHeader(response);

			List<Integer> regionIdList = null;
			if (regionId != null && regionId > 0) {
				regionIdList = ListUtil.buildArrayList(regionId);
				List<RegionDTO> regionList = regionService.listRegion(regionId);
				for (RegionDTO region : regionList)
					regionIdList.add(region.getId());
			}

			return new Response<Integer>(0, adviserService.countAdviser(name, regionIdList));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public ListResponse<List<AdviserDTO>> listAdviser(@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "regionId", required = false) Integer regionId,
			@RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);

			List<Integer> regionIdList = null;
			if (regionId != null && regionId > 0) {
				regionIdList = ListUtil.buildArrayList(regionId);
				List<RegionDTO> regionList = regionService.listRegion(regionId);
				for (RegionDTO region : regionList)
					regionIdList.add(region.getId());
			}
			int total = adviserService.countAdviser(name, regionIdList);
			List<AdviserDTO> list = adviserService.listAdviser(name, regionIdList, pageNum, pageSize);
			return new ListResponse<List<AdviserDTO>>(true, pageSize, total, list, null);
		} catch (ServiceException e) {
			return new ListResponse<List<AdviserDTO>>(false, pageSize, 0, null, e.getMessage());
		}
	}

	@RequestMapping(value = "/get", method = RequestMethod.GET)
	@ResponseBody
	public Response<AdviserDTO> getAdviser(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<AdviserDTO>(0, adviserService.getAdviserById(id));
		} catch (ServiceException e) {
			return new Response<AdviserDTO>(1, e.getMessage(), null);
		}
	}

}
