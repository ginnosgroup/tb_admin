package org.zhinanzhen.b.controller;

import java.io.IOException;
import java.util.Date;
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
import org.zhinanzhen.b.service.ApplicantService;
import org.zhinanzhen.b.service.pojo.ApplicantDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.ListResponse;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.utils.StringUtil;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/applicant")
public class ApplicantController extends BaseController {

	@Resource
	ApplicantService applicantService;
	
	@RequestMapping(value = "/upload_file", method = RequestMethod.POST)
	@ResponseBody
	public Response<String> uploadContractFile(@RequestParam MultipartFile file, HttpServletRequest request,
			HttpServletResponse response) throws IllegalStateException, IOException {
		super.setPostHeader(response);
		return super.upload2(file, request.getSession(), "/uploads/user/");
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> add(@RequestParam(value = "surname") String surname,
			@RequestParam(value = "firstname") String firstname, @RequestParam(value = "birthday") String birthday,
			@RequestParam(value = "type") String type, @RequestParam(value = "visaCode") String visaCode,
			@RequestParam(value = "visaExpirationDate") String visaExpirationDate,
			@RequestParam(value = "nutCloud", required = false) String nutCloud,
			@RequestParam(value = "fileUrl", required = false) String fileUrl,
			@RequestParam(value = "firstControllerContents", required = false) String firstControllerContents,
			@RequestParam(value = "userId") Integer userId,
			@RequestParam(value = "adviserId", required = false) Integer adviserId, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			Integer newAdviserId = getAdviserId(request);
			if (newAdviserId != null)
				adviserId = newAdviserId;
			ApplicantDTO applicantDto = new ApplicantDTO();
			applicantDto.setSurname(surname);
			applicantDto.setFirstname(firstname);
			applicantDto.setBirthday(new Date(Long.parseLong(birthday.trim())));
			applicantDto.setType(type);
			applicantDto.setVisaCode(visaCode);
			applicantDto.setVisaExpirationDate(new Date(Long.parseLong(visaExpirationDate)));
			if (StringUtil.isNotEmpty(nutCloud))
				applicantDto.setNutCloud(nutCloud);
			if (StringUtil.isNotEmpty(fileUrl))
				applicantDto.setFileUrl(fileUrl);
			if (StringUtil.isNotEmpty(firstControllerContents))
				applicantDto.setFirstControllerContents(firstControllerContents);
			applicantDto.setUserId(userId);
			applicantDto.setAdviserId(adviserId);
			if (applicantService.add(applicantDto) > 0) {
				return new Response<Integer>(0, applicantDto.getId());
			} else {
				return new Response<Integer>(1, "创建失败.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/update", method = RequestMethod.PUT)
	@ResponseBody
	public Response<Integer> update(@RequestParam(value = "id") int id,
			@RequestParam(value = "surname", required = false) String surname,
			@RequestParam(value = "firstname", required = false) String firstname,
			@RequestParam(value = "birthday", required = false) String birthday,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "visaCode", required = false) String visaCode,
			@RequestParam(value = "visaExpirationDate", required = false) String visaExpirationDate,
			@RequestParam(value = "nutCloud", required = false) String nutCloud,
			@RequestParam(value = "fileUrl", required = false) String fileUrl,
			@RequestParam(value = "firstControllerContents", required = false) String firstControllerContents,
			@RequestParam(value = "userId", required = false) Integer userId,
			@RequestParam(value = "adviserId", required = false) Integer adviserId, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			ApplicantDTO applicantDto = applicantService.getById(id);
			if (StringUtil.isNotEmpty(surname))
				applicantDto.setSurname(surname);
			if (StringUtil.isNotEmpty(firstname))
				applicantDto.setFirstname(firstname);
			if (StringUtil.isNotEmpty(birthday))
				applicantDto.setBirthday(new Date(Long.parseLong(birthday.trim())));
			if (StringUtil.isNotEmpty(type))
				applicantDto.setType(type);
			if (StringUtil.isNotEmpty(visaCode))
				applicantDto.setVisaCode(visaCode);
			if (StringUtil.isNotEmpty(visaExpirationDate))
				applicantDto.setVisaExpirationDate(new Date(Long.parseLong(visaExpirationDate)));
			if (StringUtil.isNotEmpty(nutCloud))
				applicantDto.setNutCloud(nutCloud);
			if (StringUtil.isNotEmpty(fileUrl))
				applicantDto.setFileUrl(fileUrl);
			if (StringUtil.isNotEmpty(firstControllerContents))
				applicantDto.setFirstControllerContents(firstControllerContents);
			if (userId != null && userId > 0)
				applicantDto.setUserId(userId);
			if (adviserId != null && adviserId > 0)
				applicantDto.setAdviserId(adviserId);
			if (applicantService.update(applicantDto) > 0) {
				return new Response<Integer>(0, applicantDto.getId());
			} else {
				return new Response<Integer>(1, "修改失败.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/count", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> count(@RequestParam(value = "id") Integer id, @RequestParam(value = "name") String name,
			@RequestParam(value = "userId") Integer userId,
			@RequestParam(value = "adviserId", required = false) Integer adviserId, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			Integer newAdviserId = getAdviserId(request);
			if (newAdviserId != null)
				adviserId = newAdviserId;
			return new Response<Integer>(0, applicantService.count(id, name, userId, adviserId));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public ListResponse<List<ApplicantDTO>> list(@RequestParam(value = "id") Integer id,
			@RequestParam(value = "name") String name, @RequestParam(value = "userId") Integer userId,
			@RequestParam(value = "adviserId", required = false) Integer adviserId,
			@RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			Integer newAdviserId = getAdviserId(request);
			if (newAdviserId != null)
				adviserId = newAdviserId;
			int total = applicantService.count(id, name, userId, adviserId);
			List<ApplicantDTO> list = applicantService.list(id, name, userId, adviserId, pageNum, pageSize);
			return new ListResponse<List<ApplicantDTO>>(true, pageSize, total, list, "");
		} catch (ServiceException e) {
			return new ListResponse<List<ApplicantDTO>>(false, pageSize, 0, null, e.getMessage());
		}
	}

	@RequestMapping(value = "/get", method = RequestMethod.GET)
	@ResponseBody
	public Response<ApplicantDTO> get(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<ApplicantDTO>(0, applicantService.getById(id));
		} catch (ServiceException e) {
			return new Response<ApplicantDTO>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> delete(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, applicantService.deleteById(id));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

}
