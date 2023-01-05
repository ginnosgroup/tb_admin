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
import org.zhinanzhen.b.service.QywxExternalUserService;
import org.zhinanzhen.b.service.pojo.QywxExternalUserDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.ListResponse;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.utils.ObjectUtil;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/qywxExternalUser")
public class QywxExternalUserController extends BaseController {

	@Resource
	QywxExternalUserService qywxExternalUserService;

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public ListResponse<List<QywxExternalUserDTO>> list(@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			Integer adviserId = getAdviserId(request);
			if (ObjectUtil.isNotNull(adviserId))
				return new ListResponse<List<QywxExternalUserDTO>>(true, pageSize,
						qywxExternalUserService.count(adviserId, state, startDate, endDate),
						qywxExternalUserService.list(adviserId, state, startDate, endDate, pageNum, pageSize), null);
			else
				return new ListResponse<List<QywxExternalUserDTO>>(false, pageSize, 0, null, "仅顾问才有权限查看!");
		} catch (ServiceException e) {
			return new ListResponse<List<QywxExternalUserDTO>>(false, pageSize, 0, null, e.getMessage());
		}
	}

}
