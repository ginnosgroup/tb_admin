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
import org.zhinanzhen.b.service.MaraService;
import org.zhinanzhen.b.service.MaraStateEnum;
import org.zhinanzhen.b.service.pojo.MaraDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.utils.StringUtil;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/mara")
public class MaraController extends BaseController {

	@Resource
	MaraService maraService;

	@RequestMapping(value = "/upload_img", method = RequestMethod.POST)
	@ResponseBody
	public Response<String> uploadLogo(@RequestParam MultipartFile file, HttpServletRequest request,
			HttpServletResponse response) throws IllegalStateException, IOException {
		super.setPostHeader(response);
		return super.upload(file, request.getSession(), "/uploads/mara_img/");
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> addMara(@RequestParam(value = "name") String name,
			@RequestParam(value = "phone") String phone, @RequestParam(value = "email") String email,
			@RequestParam(value = "password", required = false) String password,
			@RequestParam(value = "imageUrl") String imageUrl, @RequestParam(value = "regionId") Integer regionId,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			List<MaraDTO> maraDtoList = maraService.listMara(null, null, 0, 1000);
			for (MaraDTO maraDto : maraDtoList) {
				if (maraDto.getPhone().equals(phone)) {
					return new Response<Integer>(1, "该电话号已被使用,添加失败.", 0);
				}
				if (maraDto.getEmail().equals(email)) {
					return new Response<Integer>(1, "该邮箱已被使用,添加失败.", 0);
				}
			}

			MaraDTO maraDto = new MaraDTO();
			maraDto.setName(name);
			maraDto.setPhone(phone);
			maraDto.setEmail(email);
			maraDto.setImageUrl(imageUrl);
			maraDto.setRegionId(regionId);
			if (maraService.addMara(maraDto) > 0) {
				if (password == null)
					password = email; // 如果没有传入密码,则密码和email相同
				adminUserService.add(email, password, "MA", maraDto.getId());
				return new Response<Integer>(0, maraDto.getId());
			} else {
				return new Response<Integer>(0, "创建失败.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public Response<MaraDTO> updateMara(@RequestParam(value = "id") int id,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "phone", required = false) String phone,
			@RequestParam(value = "email", required = false) String email,
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "imageUrl", required = false) String imageUrl,
			@RequestParam(value = "regionId", required = false) Integer regionId, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			MaraDTO maraDto = new MaraDTO();
			maraDto.setId(id);
			if (StringUtil.isNotEmpty(name)) {
				maraDto.setName(name);
			}
			if (StringUtil.isNotEmpty(phone)) {
				maraDto.setPhone(phone);
			}
			if (StringUtil.isNotEmpty(email)) {
				maraDto.setEmail(email);
			}
			if (StringUtil.isNotEmpty(state)) {
				maraDto.setState(MaraStateEnum.get(state));
			}
			if (StringUtil.isNotEmpty(imageUrl)) {
				maraDto.setImageUrl(imageUrl);
			}
			if (regionId != null && regionId > 0) {
				maraDto.setRegionId(regionId);
			}
			if (maraService.updateMara(maraDto) > 0) {
				return new Response<MaraDTO>(0, maraDto);
			} else {
				return new Response<MaraDTO>(0, "修改失败.", null);
			}
		} catch (ServiceException e) {
			return new Response<MaraDTO>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/count", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> countMara(@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "regionId", required = false) Integer regionId, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, maraService.countMara(name, regionId));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<MaraDTO>> listMara(@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "regionId", required = false) Integer regionId,
			@RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<List<MaraDTO>>(0, maraService.listMara(name, regionId, pageNum, pageSize));
		} catch (ServiceException e) {
			return new Response<List<MaraDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/get", method = RequestMethod.GET)
	@ResponseBody
	public Response<MaraDTO> getMara(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<MaraDTO>(0, maraService.getMaraById(id));
		} catch (ServiceException e) {
			return new Response<MaraDTO>(1, e.getMessage(), null);
		}
	}

}
