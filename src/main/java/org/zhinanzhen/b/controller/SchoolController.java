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
import org.zhinanzhen.b.service.pojo.SubjectSettingDTO;
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

	@RequestMapping(value = "/hasSchoolSetting", method = RequestMethod.GET)
	@ResponseBody
	public Response<Boolean> hasSchoolSetting(@RequestParam(value = "schoolName") String schoolName,
			HttpServletRequest request, HttpServletResponse response) {
		super.setGetHeader(response);
		try {
			for (SchoolSettingDTO schoolSettingDto : schoolService.listSchoolSetting())
				if (schoolSettingDto.getType() > 0 && schoolName.equals(schoolSettingDto.getSchoolName()))
					return new Response<Boolean>(0, "", true);
			return new Response<Boolean>(0, "", false);
		} catch (ServiceException e) {
			return new Response<Boolean>(1, e.getMessage(), null);
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

	// 固定比例-无额外补贴
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

	// 固定比例-每人补贴
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
			parameters = parameters + "|" + fee1 + "/" + number1_1 + "/" + number1_2;
		if (StringUtil.isNotEmpty(fee2) && StringUtil.isNotEmpty(number2_1) && StringUtil.isNotEmpty(number2_2))
			parameters = parameters + "|" + fee2 + "/" + number2_1 + "/" + number2_2;
		if (StringUtil.isNotEmpty(fee3) && StringUtil.isNotEmpty(number3_1) && StringUtil.isNotEmpty(number3_2))
			parameters = parameters + "|" + fee3 + "/" + number3_1 + "/" + number3_2;
		if (StringUtil.isNotEmpty(fee4) && StringUtil.isNotEmpty(number4_1) && StringUtil.isNotEmpty(number4_2))
			parameters = parameters + "|" + fee4 + "/" + number4_1 + "/" + number4_2;
		try {
			schoolService.updateSchoolSetting(StringUtil.toInt(id), 2, new Date(Long.parseLong(startDate)),
					new Date(Long.parseLong(endDate)), parameters);
			return new Response<Boolean>(0, true);
		} catch (ServiceException e) {
			return new Response<Boolean>(e.getCode(), e.getMessage(), null);
		}
	}

	// 固定比例-固定一次性补贴
	@RequestMapping(value = "/updateSchoolSetting3", method = RequestMethod.POST)
	@ResponseBody
	public Response<Boolean> updateSchoolSetting3(@RequestParam(value = "id") String id,
			@RequestParam(value = "startDate") String startDate, @RequestParam(value = "endDate") String endDate,
			@RequestParam(value = "proportion") String proportion, @RequestParam(value = "fee") String fee,
			@RequestParam(value = "number") String number, HttpServletRequest request, HttpServletResponse response) {
		if (!super.isAdminUser(request))
			return new Response<Boolean>(1, "仅限管理员使用.", false);
		super.setPostHeader(response);
		String parameters = proportion + "|" + fee + "/" + number;
		try {
			schoolService.updateSchoolSetting(StringUtil.toInt(id), 3, new Date(Long.parseLong(startDate)),
					new Date(Long.parseLong(endDate)), parameters);
			return new Response<Boolean>(0, true);
		} catch (ServiceException e) {
			return new Response<Boolean>(e.getCode(), e.getMessage(), null);
		}
	}

	// 变动比例
	@RequestMapping(value = "/updateSchoolSetting4", method = RequestMethod.POST)
	@ResponseBody
	public Response<Boolean> updateSchoolSetting4(@RequestParam(value = "id") String id,
			@RequestParam(value = "startDate") String startDate, @RequestParam(value = "endDate") String endDate,
			@RequestParam(value = "proportion1", required = false) String proportion1,
			@RequestParam(value = "number1", required = false) String number1,
			@RequestParam(value = "proportion2", required = false) String proportion2,
			@RequestParam(value = "number2", required = false) String number2,
			@RequestParam(value = "proportion3", required = false) String proportion3,
			@RequestParam(value = "number3", required = false) String number3,
			@RequestParam(value = "proportion4", required = false) String proportion4,
			@RequestParam(value = "number4", required = false) String number4, HttpServletRequest request,
			HttpServletResponse response) {
		if (!super.isAdminUser(request))
			return new Response<Boolean>(1, "仅限管理员使用.", false);
		super.setPostHeader(response);
		String parameters = "";
		if (StringUtil.isNotEmpty(proportion1) && StringUtil.isNotEmpty(number1))
			parameters = parameters + "|" + proportion1 + "/" + number1;
		if (StringUtil.isNotEmpty(proportion2) && StringUtil.isNotEmpty(number2))
			parameters = parameters + "|" + proportion2 + "/" + number2;
		if (StringUtil.isNotEmpty(proportion3) && StringUtil.isNotEmpty(number3))
			parameters = parameters + "|" + proportion3 + "/" + number3;
		if (StringUtil.isNotEmpty(proportion4) && StringUtil.isNotEmpty(number4))
			parameters = parameters + "|" + proportion4 + "/" + number4;
		try {
			schoolService.updateSchoolSetting(StringUtil.toInt(id), 4, new Date(Long.parseLong(startDate)),
					new Date(Long.parseLong(endDate)), parameters);
			return new Response<Boolean>(0, true);
		} catch (ServiceException e) {
			return new Response<Boolean>(e.getCode(), e.getMessage(), null);
		}
	}

	// 固定底价-每人补贴
	@RequestMapping(value = "/updateSchoolSetting5", method = RequestMethod.POST)
	@ResponseBody
	public Response<Boolean> updateSchoolSetting5(@RequestParam(value = "id") String id,
			@RequestParam(value = "startDate") String startDate, @RequestParam(value = "endDate") String endDate,
			@RequestParam(value = "fee1", required = false) String fee1,
			@RequestParam(value = "number1", required = false) String number1,
			@RequestParam(value = "fee2", required = false) String fee2,
			@RequestParam(value = "number2", required = false) String number2,
			@RequestParam(value = "fee3", required = false) String fee3,
			@RequestParam(value = "number3", required = false) String number3,
			@RequestParam(value = "fee4", required = false) String fee4,
			@RequestParam(value = "number4", required = false) String number4, HttpServletRequest request,
			HttpServletResponse response) {
		if (!super.isAdminUser(request))
			return new Response<Boolean>(1, "仅限管理员使用.", false);
		super.setPostHeader(response);
		String parameters = "";
		if (StringUtil.isNotEmpty(fee1) && StringUtil.isNotEmpty(number1))
			parameters = parameters + "|" + fee1 + "/" + number1;
		if (StringUtil.isNotEmpty(fee2) && StringUtil.isNotEmpty(number2))
			parameters = parameters + "|" + fee2 + "/" + number2;
		if (StringUtil.isNotEmpty(fee3) && StringUtil.isNotEmpty(number3))
			parameters = parameters + "|" + fee3 + "/" + number3;
		if (StringUtil.isNotEmpty(fee4) && StringUtil.isNotEmpty(number4))
			parameters = parameters + "|" + fee4 + "/" + number4;
		try {
			schoolService.updateSchoolSetting(StringUtil.toInt(id), 5, new Date(Long.parseLong(startDate)),
					new Date(Long.parseLong(endDate)), parameters);
			return new Response<Boolean>(0, true);
		} catch (ServiceException e) {
			return new Response<Boolean>(e.getCode(), e.getMessage(), null);
		}
	}

	// 固定底价-固定一次性补贴
	@RequestMapping(value = "/updateSchoolSetting6", method = RequestMethod.POST)
	@ResponseBody
	public Response<Boolean> updateSchoolSetting6(@RequestParam(value = "id") String id,
			@RequestParam(value = "startDate") String startDate, @RequestParam(value = "endDate") String endDate,
			@RequestParam(value = "fee") String fee, @RequestParam(value = "number") String number,
			HttpServletRequest request, HttpServletResponse response) {
		if (!super.isAdminUser(request))
			return new Response<Boolean>(1, "仅限管理员使用.", false);
		super.setPostHeader(response);
		String parameters = fee + "/" + number;
		try {
			schoolService.updateSchoolSetting(StringUtil.toInt(id), 6, new Date(Long.parseLong(startDate)),
					new Date(Long.parseLong(endDate)), parameters);
			return new Response<Boolean>(0, true);
		} catch (ServiceException e) {
			return new Response<Boolean>(e.getCode(), e.getMessage(), null);
		}
	}

	// 固定底价-无额外补贴
	@RequestMapping(value = "/updateSchoolSetting7", method = RequestMethod.POST)
	@ResponseBody
	public Response<Boolean> updateSchoolSetting7(@RequestParam(value = "id") String id,
			@RequestParam(value = "startDate") String startDate, @RequestParam(value = "endDate") String endDate,
			HttpServletRequest request, HttpServletResponse response) {
		if (!super.isAdminUser(request))
			return new Response<Boolean>(1, "仅限管理员使用.", false);
		super.setPostHeader(response);
		try {
			schoolService.updateSchoolSetting(StringUtil.toInt(id), 7, new Date(Long.parseLong(startDate)),
					new Date(Long.parseLong(endDate)), null);
			return new Response<Boolean>(0, true);
		} catch (ServiceException e) {
			return new Response<Boolean>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/listSubjectSetting", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<SubjectSettingDTO>> listSubjectSetting(
			@RequestParam(value = "schoolSettingId") String schoolSettingId, HttpServletRequest request,
			HttpServletResponse response) {
		if (!super.isAdminUser(request))
			return new Response<List<SubjectSettingDTO>>(1, "仅限管理员使用.", null);
		super.setGetHeader(response);
		try {
			return new Response<List<SubjectSettingDTO>>(0,
					schoolService.listSubjectSetting(StringUtil.toInt(schoolSettingId)));
		} catch (ServiceException e) {
			return new Response<List<SubjectSettingDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/updateSubjectSetting", method = RequestMethod.POST)
	@ResponseBody
	public Response<Boolean> updateSubjectSetting(@RequestParam(value = "subjectSettingId") String subjectSettingId,
			@RequestParam(value = "price") String price, HttpServletRequest request, HttpServletResponse response) {
		if (!super.isAdminUser(request))
			return new Response<Boolean>(1, "仅限管理员使用.", false);
		super.setPostHeader(response);
		try {
			return new Response<Boolean>(0, schoolService.updateSubjectSetting(StringUtil.toInt(subjectSettingId),
					Double.parseDouble(price)) > -1);
		} catch (ServiceException e) {
			return new Response<Boolean>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/updateSubjectSettings", method = RequestMethod.POST)
	@ResponseBody
	public Response<Boolean> updateSubjectSettings(@RequestParam(value = "parameters") String parameters,
			HttpServletRequest request, HttpServletResponse response) {
		if (!super.isAdminUser(request))
			return new Response<Boolean>(1, "仅限管理员使用.", false);
		super.setPostHeader(response);
		if (StringUtil.isEmpty(parameters))
			return new Response<Boolean>(1, "参数错误.", false);
		boolean b = true;
		String msg = "";
		String[] _parameters = parameters.split("[|]");
		for (String parameter : _parameters) {
			if (StringUtil.isNotEmpty(parameter)) {
				String[] _parameter = parameter.split(":");
				if (_parameter.length == 2)
					try {
						if (schoolService.updateSubjectSetting(StringUtil.toInt(_parameter[0]),
								Double.parseDouble(_parameter[1])) <= -1) {
							b = false;
							msg += _parameter[0] + ":" + _parameter[1] + ";";
						}
					} catch (ServiceException e) {
						return new Response<Boolean>(e.getCode(), e.getMessage(), null);
					}
			}
		}
		return new Response<Boolean>(b ? 0 : 1, msg, b);
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
