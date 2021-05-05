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
import org.zhinanzhen.b.service.MailLogService;
import org.zhinanzhen.b.service.pojo.MailLogDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.ListResponse;
import org.zhinanzhen.tb.service.ServiceException;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/mailLog")
public class MailLogController extends BaseController {

	@Resource
	MailLogService mailLogService;

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public ListResponse<List<MailLogDTO>> list(@RequestParam(value = "pageNum") int pageNum,
			@RequestParam(value = "pageSize") int pageSize, HttpServletRequest request, HttpServletResponse response) {
		AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
		if (adminUserLoginInfo == null || !"SUPERAD".equalsIgnoreCase(adminUserLoginInfo.getApList()))
			return new ListResponse<List<MailLogDTO>>(false, pageSize, 0, null, "仅限超级管理员查阅!");
		try {
			super.setGetHeader(response);
			return new ListResponse<List<MailLogDTO>>(true, pageSize, mailLogService.countMailLog(),
					mailLogService.listMailLog(pageNum, pageSize), "");
		} catch (ServiceException e) {
			return new ListResponse<List<MailLogDTO>>(false, pageSize, 0, null, e.getMessage());
		}
	}

}
