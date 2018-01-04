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
import org.zhinanzhen.b.service.SchoolService;
import org.zhinanzhen.b.service.pojo.SchoolDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.utils.StringUtil;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/school")
public class SchoolController extends BaseController {

	@Resource
	SchoolService schoolService;

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> addSchool(@RequestParam(value = "name") String name,
			@RequestParam(value = "subject") String subject, @RequestParam(value = "country") String country,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			List<SchoolDTO> schoolDtoList = schoolService.listSchool(name, subject, country);
			for (SchoolDTO schoolDto : schoolDtoList) {
				if (schoolDto.getName().equals(name) && schoolDto.getSubject().equals(subject)
						&& schoolDto.getCountry().equals(country)) {
					return new Response<Integer>(1, "该学校课程已存在,操作失败.", 0);
				}
			}
			SchoolDTO schoolDto = new SchoolDTO();
			schoolDto.setName(name);
			schoolDto.setSubject(subject);
			schoolDto.setCountry(country);
			if (schoolService.addSchool(schoolDto) > 0) {
				return new Response<Integer>(0, schoolDto.getId());
			} else {
				return new Response<Integer>(0, "创建失败.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public Response<SchoolDTO> updateSchool(@RequestParam(value = "id") int id,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "subject", required = false) String subject,
			@RequestParam(value = "country", required = false) String country, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			List<SchoolDTO> schoolDtoList = schoolService.listSchool(name, subject, country);
			for (SchoolDTO schoolDto : schoolDtoList) {
				if (schoolDto.getName().equals(name) && schoolDto.getSubject().equals(subject)
						&& schoolDto.getCountry().equals(country)) {
					return new Response<SchoolDTO>(1, "该学校课程已存在,操作失败.", schoolDto);
				}
			}
			SchoolDTO schoolDto = new SchoolDTO();
			schoolDto.setId(id);
			if (StringUtil.isNotEmpty(name)) {
				schoolDto.setName(name);
			}
			if (StringUtil.isNotEmpty(subject)) {
				schoolDto.setSubject(subject);
			}
			if (StringUtil.isNotEmpty(country)) {
				schoolDto.setCountry(country);
			}
			if (schoolService.updateSchool(name, subject, country) > 0) {
				return new Response<SchoolDTO>(0, schoolDto);
			} else {
				return new Response<SchoolDTO>(0, "修改失败.", null);
			}
		} catch (ServiceException e) {
			return new Response<SchoolDTO>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<SchoolDTO>> listSchool(@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "subject", required = false) String subject,
			@RequestParam(value = "country", required = false) String country, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<List<SchoolDTO>>(0, schoolService.listSchool(name, subject, country));
		} catch (ServiceException e) {
			return new Response<List<SchoolDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/get", method = RequestMethod.GET)
	@ResponseBody
	public Response<SchoolDTO> getSchool(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<SchoolDTO>(0, schoolService.getSchoolById(id));
		} catch (ServiceException e) {
			return new Response<SchoolDTO>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> deleteSchool(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, schoolService.deleteSchoolById(id));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

}
