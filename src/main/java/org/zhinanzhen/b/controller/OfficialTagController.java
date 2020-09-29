package org.zhinanzhen.b.controller;

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
			if (officialTagService.addOfficialTag(officialTagDto) > 0)
				return new Response<Integer>(0, officialTagDto.getId());
			else
				return new Response<Integer>(1, "创建失败.", 0);
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

}
