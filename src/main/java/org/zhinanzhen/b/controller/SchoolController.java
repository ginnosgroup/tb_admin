package org.zhinanzhen.b.controller;

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
import org.zhinanzhen.b.service.SchoolService;
import org.zhinanzhen.b.service.pojo.SchoolDTO;
import org.zhinanzhen.b.service.pojo.SchoolSettingDTO;
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
			@RequestParam(value = "subject", required = false) String subject,
			@RequestParam(value = "country") String country, HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			List<SchoolDTO> schoolDtoList = schoolService.list(name, subject, country);
			for (SchoolDTO schoolDto : schoolDtoList) {
				if (schoolDto.getName().equals(name) && (subject == null || subject.equals(schoolDto.getSubject()))
						&& schoolDto.getCountry().equals(country)) {
					return new Response<Integer>(2, "该学校课程已存在,操作失败.", 0);
				}
			}
			SchoolDTO schoolDto = new SchoolDTO();
			schoolDto.setName(name);
			schoolDto.setSubject(subject);
			schoolDto.setCountry(country);
			if (schoolService.addSchool(schoolDto) > 0) {
				return new Response<Integer>(0, schoolDto.getId());
			} else {
				return new Response<Integer>(1, "创建失败.", 0);
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
			List<SchoolDTO> schoolDtoList = schoolService.list(name, subject, country);
			for (SchoolDTO schoolDto : schoolDtoList) {
				if (schoolDto.getName().equals(name) && (subject == null || subject.equals(schoolDto.getSubject()))
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
			if (schoolService.updateSchool(id, name, subject, country) > 0) {
				return new Response<SchoolDTO>(0, schoolDto);
			} else {
				return new Response<SchoolDTO>(1, "修改失败.", null);
			}
		} catch (ServiceException e) {
			return new Response<SchoolDTO>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/updateName", method = RequestMethod.POST)
	@ResponseBody
	public Response<Boolean> updateSchoolName(@RequestParam(value = "oldName", required = false) String oldName,
			@RequestParam(value = "newName", required = false) String newName, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			List<SchoolDTO> schoolDtoList = schoolService.list(oldName);
			if (schoolDtoList == null || schoolDtoList.size() == 0)
				return new Response<Boolean>(1, "没有找到名称为'" + oldName + "'的记录!", false);
			for (SchoolDTO schoolDto : schoolDtoList)
				schoolService.updateSchool(schoolDto.getId(), newName, null, null);
			return new Response<Boolean>(0, true);
		} catch (ServiceException e) {
			return new Response<Boolean>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<SchoolDTO>> list(@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "subject", required = false) String subject,
			@RequestParam(value = "country", required = false) String country, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<List<SchoolDTO>>(0, schoolService.list(name, subject, country));
		} catch (ServiceException e) {
			return new Response<List<SchoolDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/listSchool", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<SchoolDTO>> listSchool(@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "country", required = false) String country, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<List<SchoolDTO>>(0, schoolService.listSchool(name, country));
		} catch (ServiceException e) {
			return new Response<List<SchoolDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/listSchoolSetting", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<SchoolSettingDTO>> listSchoolSetting(HttpServletRequest request,
			HttpServletResponse response) {
		if (!super.isAdminUser(request))
			return new Response<List<SchoolSettingDTO>>(1, "仅限管理员使用.", null);
		super.setGetHeader(response);
		try {
			return new Response<List<SchoolSettingDTO>>(0, schoolService.listSchoolSetting());
		} catch (ServiceException e) {
			return new Response<List<SchoolSettingDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/updateSchoolSetting0", method = RequestMethod.POST)
	@ResponseBody
	public Response<Boolean> updateSchoolSetting0(@RequestParam(value = "id") String id, HttpServletRequest request,
			HttpServletResponse response) {
		if (!super.isAdminUser(request))
			return new Response<Boolean>(1, "仅限管理员使用.", false);
		super.setPostHeader(response);
		try {
			schoolService.updateSchoolSetting(StringUtil.toInt(id), 0, new Date(), new Date(), null);
			return new Response<Boolean>(0, true);
		} catch (ServiceException e) {
			return new Response<Boolean>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/updateSchoolSetting1", method = RequestMethod.POST)
	@ResponseBody
	public Response<Boolean> updateSchoolSetting1(@RequestParam(value = "id") String id,
			@RequestParam(value = "startDate") String startDate, @RequestParam(value = "endDate") String endDate,
			@RequestParam(value = "proportion") String proportion, HttpServletRequest request,
			HttpServletResponse response) {
		if (!super.isAdminUser(request))
			return new Response<Boolean>(1, "仅限管理员使用.", false);
		super.setPostHeader(response);
		try {
			schoolService.updateSchoolSetting(StringUtil.toInt(id), 1, new Date(Long.parseLong(startDate)),
					new Date(Long.parseLong(endDate)), proportion);
			return new Response<Boolean>(0, true);
		} catch (ServiceException e) {
			return new Response<Boolean>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/updateSchoolSetting2", method = RequestMethod.POST)
	@ResponseBody
	public Response<Boolean> updateSchoolSetting2(@RequestParam(value = "id") String id,
			@RequestParam(value = "startDate") String startDate, @RequestParam(value = "endDate") String endDate,
			@RequestParam(value = "proportion") String proportion,
			@RequestParam(value = "fee1", required = false) String fee1,
			@RequestParam(value = "number1_1", required = false) String number1_1,
			@RequestParam(value = "number1_2", required = false) String number1_2,
			@RequestParam(value = "fee2", required = false) String fee2,
			@RequestParam(value = "number2_1", required = false) String number2_1,
			@RequestParam(value = "number2_2", required = false) String number2_2,
			@RequestParam(value = "fee3", required = false) String fee3,
			@RequestParam(value = "number3_1", required = false) String number3_1,
			@RequestParam(value = "number3_2", required = false) String number3_2,
			@RequestParam(value = "fee4", required = false) String fee4,
			@RequestParam(value = "number4_1", required = false) String number4_1,
			@RequestParam(value = "number4_2", required = false) String number4_2, HttpServletRequest request,
			HttpServletResponse response) {
		if (!super.isAdminUser(request))
			return new Response<Boolean>(1, "仅限管理员使用.", false);
		super.setPostHeader(response);
		String parameters = proportion;
		if (StringUtil.isNotEmpty(fee1) && StringUtil.isNotEmpty(number1_1) && StringUtil.isNotEmpty(number1_2))
			parameters = parameters + "|" + fee1 + "/" + number1_1 + "," + number1_2;
		if (StringUtil.isNotEmpty(fee2) && StringUtil.isNotEmpty(number2_1) && StringUtil.isNotEmpty(number2_2))
			parameters = parameters + "|" + fee2 + "/" + number2_1 + "," + number2_2;
		if (StringUtil.isNotEmpty(fee3) && StringUtil.isNotEmpty(number3_1) && StringUtil.isNotEmpty(number3_2))
			parameters = parameters + "|" + fee3 + "/" + number3_1 + "," + number3_2;
		if (StringUtil.isNotEmpty(fee4) && StringUtil.isNotEmpty(number4_1) && StringUtil.isNotEmpty(number4_2))
			parameters = parameters + "|" + fee4 + "/" + number4_1 + "," + number4_2;
		try {
			schoolService.updateSchoolSetting(StringUtil.toInt(id), 2, new Date(Long.parseLong(startDate)),
					new Date(Long.parseLong(endDate)), parameters);
			return new Response<Boolean>(0, true);
		} catch (ServiceException e) {
			return new Response<Boolean>(e.getCode(), e.getMessage(), null);
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

	@RequestMapping(value = "/deleteByName", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> deleteSchoolByName(@RequestParam(value = "name") String name,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, schoolService.deleteSchoolByName(name));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

}
