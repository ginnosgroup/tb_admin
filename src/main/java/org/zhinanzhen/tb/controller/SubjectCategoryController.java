package org.zhinanzhen.tb.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.SubjectCategoryService;
import org.zhinanzhen.tb.service.SubjectCategoryStateEnum;
import org.zhinanzhen.tb.service.SubjectService;
import org.zhinanzhen.tb.service.pojo.SubjectCategoryDTO;

import com.ikasoa.core.utils.StringUtil;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/subject_category")
public class SubjectCategoryController extends BaseController {

	@Resource
	SubjectCategoryService subjectCategoryService;
	
	@Resource
	SubjectService subjectService;

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<Boolean> addSubjectCategory(@RequestParam(value = "name") String name,
			@RequestParam(value = "state") String state, @RequestParam(value = "weight") int weight,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			SubjectCategoryStateEnum stateEnum = null;
			if (StringUtil.isNotEmpty(state)) {
				stateEnum = SubjectCategoryStateEnum.get(state);
				if (stateEnum == null) {
					return new Response<Boolean>(3, "状态参数错误.", false);
				}
			}
			if (weight <= 0) {
				return new Response<Boolean>(4, "排序值必须大于0.", false);
			}
			SubjectCategoryDTO subjectCategoryDto = new SubjectCategoryDTO();
			subjectCategoryDto.setName(name);
			subjectCategoryDto.setState(stateEnum);
			subjectCategoryDto.setWeight(weight);
			return new Response<Boolean>(0, subjectCategoryService.addSubjectCategory(subjectCategoryDto) > 0);
		} catch (ServiceException e) {
			return new Response<Boolean>(1, e.getMessage(), false);
		}
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public Response<Boolean> updateSubjectCategory(@RequestParam(value = "id") int id,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "weight", required = false) int weight, HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			SubjectCategoryStateEnum stateEnum = null;
			if (StringUtil.isNotEmpty(state)) {
				stateEnum = SubjectCategoryStateEnum.get(state);
				if (stateEnum == null) {
					return new Response<Boolean>(2, "状态参数错误.", false);
				}
			}
			SubjectCategoryDTO subjectCategoryDto = subjectCategoryService.getSubjectCategoryById(id);
			if (StringUtil.isNotEmpty(name)) {
				subjectCategoryDto.setName(name);
			}
			if (stateEnum != null) {
				subjectCategoryDto.setState(stateEnum);
			}
			if (weight > 0) {
				subjectCategoryDto.setWeight(weight);
			}
			return new Response<Boolean>(0, subjectCategoryService.updateSubjectCategory(subjectCategoryDto) > 0);
		} catch (ServiceException e) {
			return new Response<Boolean>(1, e.getMessage(), false);
		}
	}

	@RequestMapping(value = "/count", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> countSubjectCategory(@RequestParam(value = "state", required = false) String state,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			SubjectCategoryStateEnum stateEnum = null;
			if (StringUtil.isNotEmpty(state)) {
				stateEnum = SubjectCategoryStateEnum.get(state);
				if (stateEnum == null) {
					return new Response<Integer>(2, "状态参数错误.", null);
				}
			}
			int count = subjectCategoryService.countSubjectCategory(stateEnum);
			return new Response<Integer>(0, count);
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), -1);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<SubjectCategoryDTO>> listSubjectCategory(
			@RequestParam(value = "state", required = false) String state, @RequestParam(value = "pageNum") int pageNum,
			@RequestParam(value = "pageSize") int pageSize, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			SubjectCategoryStateEnum stateEnum = null;
			if (StringUtil.isNotEmpty(state)) {
				stateEnum = SubjectCategoryStateEnum.get(state);
				if (stateEnum == null) {
					return new Response<List<SubjectCategoryDTO>>(2, "状态参数错误.", null);
				}
			}
			List<SubjectCategoryDTO> list = subjectCategoryService.listSubjectCategory(stateEnum, pageNum, pageSize);
			return new Response<List<SubjectCategoryDTO>>(0, list);
		} catch (ServiceException e) {
			return new Response<List<SubjectCategoryDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	@ResponseBody
	public Response<Boolean> deleteSubjectCategory(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			if (subjectService.countSubject(null, id, null) > 0) {
				return new Response<Boolean>(2, "have subject for the category !", false);
			}
			return new Response<Boolean>(0,
					subjectCategoryService.updateSubjectCategoryState(id, SubjectCategoryStateEnum.DELETE) > 0);
		} catch (ServiceException e) {
			return new Response<Boolean>(1, e.getMessage(), false);
		}
	}

}
