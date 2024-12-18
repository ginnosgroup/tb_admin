package org.zhinanzhen.b.controller;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONArray;
import org.apache.poi.ss.usermodel.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zhinanzhen.b.service.ApplicantService;
import org.zhinanzhen.b.service.QywxExternalUserService;
import org.zhinanzhen.b.service.pojo.ApplicantDTO;
import org.zhinanzhen.b.service.pojo.QywxExternalUserDTO;
import org.zhinanzhen.b.service.pojo.QywxExternalUserDescriptionDTO;
import org.zhinanzhen.b.service.pojo.TagsDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.ListResponse;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.UserAuthTypeEnum;
import org.zhinanzhen.tb.service.UserService;
import org.zhinanzhen.tb.service.pojo.UserDTO;

import com.ikasoa.core.utils.ListUtil;
import com.ikasoa.core.utils.ObjectUtil;
import com.ikasoa.core.utils.StringUtil;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/qywxExternalUser")
public class QywxExternalUserController extends BaseController {
	
	private static final Logger LOG = LoggerFactory.getLogger(QywxExternalUserController.class);

	@Resource
	QywxExternalUserService qywxExternalUserService;

	@Resource
	UserService userService;
	
	@Resource
	ApplicantService applicantService;

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

	@RequestMapping(value = "/checkBOD", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<String>> checkBOD(@RequestParam(value = "id") int id,
			@RequestParam(value = "applicantId") int applicantId, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo == null || (!"GW".equalsIgnoreCase(adminUserLoginInfo.getApList())))
				return new Response<List<String>>(1, "仅限顾问操作.", null);
			ApplicantDTO applicant = applicantService.getById(applicantId);
			if (ObjectUtil.isNull(applicant.getBirthday()))
				return new Response<List<String>>(1, "申请人生日数据错误.", null);
			QywxExternalUserDTO qywxExternalUserDto = qywxExternalUserService.get(id);
			if (ObjectUtil.isNull(qywxExternalUserDto))
				return new Response<List<String>>(1, "未查到企业微信数据,请检查参数是否正确.", null);
			List<QywxExternalUserDescriptionDTO> list = qywxExternalUserService
					.listDescByExternalUserid(qywxExternalUserDto.getExternalUserid(), "_birthday");
			if (ObjectUtil.isNull(list) || list.size() == 0)
				return new Response<List<String>>(1, "未查到企业微信生日数据.", ListUtil.newArrayList());
			QywxExternalUserDescriptionDTO desc = list.get(0);
			String qywxUserBOD = desc.getQywxValue();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String applicantBOD = sdf.format(applicant.getBirthday().getTime());
			if (StringUtil.equals("_birthday", desc.getQywxKey())
					&& !StringUtil.equals(desc.getQywxValue(), sdf.format(applicant.getBirthday().getTime())))
				return new Response<List<String>>(0, null, ListUtil.buildArrayList(qywxUserBOD, applicantBOD));
			return new Response<List<String>>(1, "未查到企业微信生日数据.", ListUtil.newArrayList());
		} catch (ServiceException e) {
			return new Response<List<String>>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/bind", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> bind(@RequestParam(value = "id") int id, @RequestParam(value = "userId") int userId,
			@RequestParam(value = "applicantId") int applicantId,
			@RequestParam(value = "birthday", required = false) String birthday, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo == null || (!"GW".equalsIgnoreCase(adminUserLoginInfo.getApList())))
				return new Response<Integer>(1, "仅限顾问操作.", 0);
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
				LOG.info(StringUtil.merge("修改企业微信数据成功:", qywxExternalUserDto.getId()));
				if (StringUtil.isNotEmpty(birthday)) {
					// 修改生日数据
					ApplicantDTO applicantDto = applicantService.getById(applicantId);
					applicantDto.setBirthday(DateUtil.parseYYYYMMDDDate(birthday));
					if (applicantService.update(applicantDto) > 0)
						LOG.info(StringUtil.merge("修改申请人生日数据成功:", applicantDto.getId()));
				}
				String tags = qywxExternalUserDto.getTags();
				List<TagsDTO> tagsDTOS = JSONArray.parseArray(tags, TagsDTO.class);
				qywxExternalUserDto.setTagsDTOS(tagsDTOS);
				if (tagsDTOS != null) {
					List<String> channelSources = tagsDTOS.stream()
							.filter(TagsDTO -> TagsDTO.getGroup().equals("来源"))
							.map(TagsDTO::getName).collect(Collectors.toList());
					qywxExternalUserDto.setChannelSource(String.join(",", channelSources));
				}
				userService.update(userDto.getId(), null, null, null, null,
						null, null, null, null, null,
						null, null, qywxExternalUserDto.getStateText(), qywxExternalUserDto.getChannelSource(), null, null, null);
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
