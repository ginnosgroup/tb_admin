package org.zhinanzhen.b.controller;

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
import org.zhinanzhen.b.service.OfficialTagService;
import org.zhinanzhen.b.service.pojo.OfficialTagDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/officialTag")
public class OfficialTagController extends BaseController {

	@Resource
	OfficialTagService officialTagService;

	@RequestMapping(value = "/addOfficialTag", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> addOfficialTag(@RequestParam(value = "name") String name,
			@RequestParam(value = "colour") String colour, HttpServletRequest request, HttpServletResponse response) {
		AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
		if (adminUserLoginInfo != null)
			if (adminUserLoginInfo == null || !"WA".equalsIgnoreCase(adminUserLoginInfo.getApList())
					|| adminUserLoginInfo.getOfficialId() == null)
				return new Response<Integer>(1, "仅限文案操作.", null);
		try {
			super.setPostHeader(response);
			OfficialTagDTO officialTagDto = new OfficialTagDTO();
			officialTagDto.setName(name);
			officialTagDto.setColour(colour);
			if (officialTagService.add(officialTagDto) > 0)
				return new Response<Integer>(0, officialTagDto.getId());
			else
				return new Response<Integer>(1, "创建失败.", 0);
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/updateOfficialTag", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> updateOfficialTag(@RequestParam(value = "id") Integer id,
			@RequestParam(value = "name") String name, @RequestParam(value = "colour") String colour,
			HttpServletRequest request, HttpServletResponse response) {
		AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
		if (adminUserLoginInfo != null)
			if (adminUserLoginInfo == null || !"WA".equalsIgnoreCase(adminUserLoginInfo.getApList())
					|| adminUserLoginInfo.getOfficialId() == null)
				return new Response<Integer>(1, "仅限文案操作.", null);
		try {
			super.setPostHeader(response);
			OfficialTagDTO officialTagDto = officialTagService.get(id);
			if (officialTagDto == null)
				return new Response<Integer>(1, "没有找到该标签:" + id, null);
			officialTagDto.setName(name);
			officialTagDto.setColour(colour);
			if (officialTagService.update(officialTagDto) > 0)
				return new Response<Integer>(0, officialTagDto.getId());
			else
				return new Response<Integer>(1, "修改失败.", 0);
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.POST)
	@ResponseBody
	public Response<List<OfficialTagDTO>> updateOfficialTag(HttpServletRequest request, HttpServletResponse response) {
		AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
		if (adminUserLoginInfo != null)
			if (adminUserLoginInfo == null || !"WA".equalsIgnoreCase(adminUserLoginInfo.getApList())
					|| adminUserLoginInfo.getOfficialId() == null)
				return new Response<List<OfficialTagDTO>>(1, "仅限文案操作.", null);
		try {
			super.setPostHeader(response);
			return new Response<List<OfficialTagDTO>>(0, officialTagService.list());
		} catch (ServiceException e) {
			return new Response<List<OfficialTagDTO>>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/deleteOfficialTag", method = RequestMethod.POST)
	@ResponseBody
	public Response<Boolean> deleteOfficialTag(@RequestParam(value = "id") Integer id,
			@RequestParam(value = "name") String name, @RequestParam(value = "colour") String colour,
			HttpServletRequest request, HttpServletResponse response) {
		AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
		if (adminUserLoginInfo != null)
			if (adminUserLoginInfo == null || !"WA".equalsIgnoreCase(adminUserLoginInfo.getApList())
					|| adminUserLoginInfo.getOfficialId() == null)
				return new Response<Boolean>(1, "仅限文案操作.", false);
		try {
			super.setPostHeader(response);
			if (officialTagService.get(id) == null)
				return new Response<Boolean>(1, "没有找到该标签:" + id, false);
			if (officialTagService.delete(id) > 0)
				return new Response<Boolean>(0, true);
			else
				return new Response<Boolean>(1, "删除失败.", false);
		} catch (ServiceException e) {
			return new Response<Boolean>(e.getCode(), e.getMessage(), false);
		}
	}

	@RequestMapping(value = "/addServiceOrderOfficialTag", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> addServiceOrderOfficialTag(@RequestParam(value = "id") Integer id,
			@RequestParam(value = "serviceOrderId") Integer serviceOrderId, HttpServletRequest request,
			HttpServletResponse response) {
		AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
		if (adminUserLoginInfo != null)
			if (adminUserLoginInfo == null || !"WA".equalsIgnoreCase(adminUserLoginInfo.getApList())
					|| adminUserLoginInfo.getOfficialId() == null)
				return new Response<Integer>(1, "仅限文案操作.", null);
		try {
			super.setPostHeader(response);
			if (officialTagService.addServiceOrderOfficialTag(id, serviceOrderId) > 0)
				return new Response<Integer>(0, id);
			else
				return new Response<Integer>(1, "创建失败.", 0);
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/deleteServiceOrderOfficialTagById", method = RequestMethod.POST)
	@ResponseBody
	public Response<Boolean> deleteServiceOrderOfficialTagById(@RequestParam(value = "id") Integer id,
			@RequestParam(value = "serviceOrderId") Integer serviceOrderId, HttpServletRequest request,
			HttpServletResponse response) {
		AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
		if (adminUserLoginInfo != null)
			if (adminUserLoginInfo == null || !"WA".equalsIgnoreCase(adminUserLoginInfo.getApList())
					|| adminUserLoginInfo.getOfficialId() == null)
				return new Response<Boolean>(1, "仅限文案操作.", false);
		try {
			super.setPostHeader(response);
			if (officialTagService.deleteServiceOrderOfficialTagByTagIdAndServiceOrderId(id, serviceOrderId) > 0)
				return new Response<Boolean>(0, true);
			else
				return new Response<Boolean>(1, "删除失败.", false);
		} catch (ServiceException e) {
			return new Response<Boolean>(e.getCode(), e.getMessage(), false);
		}
	}

}
