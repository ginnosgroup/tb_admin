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
import org.zhinanzhen.b.service.pojo.QywxExternalUserDescriptionDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.ListResponse;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.UserAuthTypeEnum;
import org.zhinanzhen.tb.service.UserService;
import org.zhinanzhen.tb.service.pojo.UserDTO;

import com.ikasoa.core.utils.ObjectUtil;
import com.ikasoa.core.utils.StringUtil;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/qywxExternalUser")
public class QywxExternalUserController extends BaseController {

	@Resource
	QywxExternalUserService qywxExternalUserService;

	@Resource
	UserService userService;

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

	@RequestMapping(value = "/bind", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> bind(@RequestParam(value = "id") int id, @RequestParam(value = "userId") int userId,
			@RequestParam(value = "applicantId") int applicantId, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo == null || (!"GW".equalsIgnoreCase(adminUserLoginInfo.getApList())))
				return new Response<Integer>(1, "仅限顾问能修改.", 0);
			UserDTO userDto = userService.getUserById(userId);
			QywxExternalUserDTO qywxExternalUserDto = qywxExternalUserService.get(id);
			qywxExternalUserDto.setApplicantId(applicantId);
			if (StringUtil.isNotEmpty(userDto.getPhone()))
				qywxExternalUserDto.setPhone(StringUtil.isNotEmpty(userDto.getAreaCode())
						? StringUtil.merge(userDto.getAreaCode(), " ", userDto.getPhone())
						: userDto.getPhone());
			qywxExternalUserDto.setEmail(userDto.getEmail());
			qywxExternalUserDto.setWechatUsername(userDto.getWechatUsername());
			if (UserAuthTypeEnum.QQ.getValue().equalsIgnoreCase(userDto.getAuthType().getValue())
					&& StringUtil.isNotEmpty(userDto.getAuthUsername()))
				qywxExternalUserDto.setQq(userDto.getAuthUsername());
			if (UserAuthTypeEnum.WEIBO.getValue().equalsIgnoreCase(userDto.getAuthType().getValue())
					&& StringUtil.isNotEmpty(userDto.getAuthUsername()))
				qywxExternalUserDto.setWeiboUsername(userDto.getAuthUsername());
			qywxExternalUserDto.setState("YBD");
			if (qywxExternalUserService.update(qywxExternalUserDto) > 0) {
				return new Response<Integer>(0, qywxExternalUserDto.getId());
			} else {
				return new Response<Integer>(1, "修改失败.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/listDesc", method = RequestMethod.GET)
	@ResponseBody
	public ListResponse<List<QywxExternalUserDescriptionDTO>> listDesc(
			@RequestParam(value = "externalUserid", required = false) String externalUserid, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			if (ObjectUtil.isNotNull(getAdviserId(request)) && StringUtil.isNotEmpty(externalUserid)) {
				List<QywxExternalUserDescriptionDTO> list = qywxExternalUserService
						.listDescByExternalUserid(externalUserid, null);
				return new ListResponse<List<QywxExternalUserDescriptionDTO>>(true, 100, list.size(), list, null);
			} else
				return new ListResponse<List<QywxExternalUserDescriptionDTO>>(false, 100, 0, null, "仅顾问才有权限查看!");
		} catch (ServiceException e) {
			return new ListResponse<List<QywxExternalUserDescriptionDTO>>(false, 100, 0, null, e.getMessage());
		}
	}

	@RequestMapping(value = "/listDescByApplicantId", method = RequestMethod.GET)
	@ResponseBody
	public ListResponse<List<QywxExternalUserDescriptionDTO>> listDescByApplicantId(
			@RequestParam(value = "applicantId") Integer applicantId, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			if (ObjectUtil.isNotNull(getAdviserId(request))) {
				List<QywxExternalUserDescriptionDTO> list = qywxExternalUserService.listDescByApplicantId(applicantId);
				return new ListResponse<List<QywxExternalUserDescriptionDTO>>(true, 100, list.size(), list, null);
			} else
				return new ListResponse<List<QywxExternalUserDescriptionDTO>>(false, 100, 0, null, "仅顾问才有权限查看!");
		} catch (ServiceException e) {
			return new ListResponse<List<QywxExternalUserDescriptionDTO>>(false, 100, 0, null, e.getMessage());
		}
	}
}
