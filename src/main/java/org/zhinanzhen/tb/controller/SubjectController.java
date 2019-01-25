package org.zhinanzhen.tb.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.SubjectCategoryService;
import org.zhinanzhen.tb.service.SubjectService;
import org.zhinanzhen.tb.service.SubjectStateEnum;
import org.zhinanzhen.tb.service.SubjectTypeEnum;
import org.zhinanzhen.tb.service.pojo.SubjectCategoryDTO;
import org.zhinanzhen.tb.service.pojo.SubjectDTO;

import com.ikasoa.core.utils.StringUtil;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/subject")
public class SubjectController extends BaseController {

    @Resource
    SubjectService subjectService;
    @Resource
    SubjectCategoryService subjectCategoryService;

    @RequestMapping(value = "/upload_logo", method = RequestMethod.POST)
    @ResponseBody
    public Response<String> uploadLogo(@RequestParam MultipartFile file, HttpServletRequest request,
	    HttpServletResponse response) throws IllegalStateException, IOException {
	super.setPostHeader(response);
	return super.upload(file, request.getSession(), "/uploads/subject_logo/");
    }

    @RequestMapping(value = "/upload_img", method = RequestMethod.POST)
    @ResponseBody
    public void uploadImg(@RequestParam MultipartFile upload, HttpServletRequest request, HttpServletResponse response)
	    throws IllegalStateException, IOException {
	super.setPostHeader(response);
	Response<String> r = super.upload(upload, request.getSession(), "/uploads/subject_img/");
	response.setContentType("text/html;charset=UTF-8");
	String callback = request.getParameter("CKEditorFuncNum");
	PrintWriter out = response.getWriter();
	out.println("<script type=\"text/javascript\">");
	if (r != null && r.getCode() == 0) {
	    out.println("window.parent.CKEDITOR.tools.callFunction(" + callback + ",'"
		    + "https://tuangou.51mantuo.com/statics/" + r.getData() + "',''" + ")");
	} else {
	    out.println("window.parent.CKEDITOR.tools.callFunction(" + callback + ",'" + "上传失败.','');");
	}
	out.println("</script>");
	out.flush();
	out.close();
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public Response<Integer> addSubject(@RequestBody SubjectDTO subjectDto, HttpServletResponse response) {
	try {
	    super.setPostHeader(response);
	    if (subjectDto.getType() == null)
	    	subjectDto.setType(SubjectTypeEnum.DEFAULT);
	    if (subjectService.addSubject(subjectDto) > 0) {
		return new Response<Integer>(0, subjectDto.getId());
	    } else {
		return new Response<Integer>(2, "创建失败", 0);
	    }
	} catch (ServiceException e) {
	    return new Response<Integer>(1, e.getMessage(), 0);
	}
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public Response<Integer> updateSubject(@RequestBody SubjectDTO subjectDto, HttpServletResponse response) {
	try {
	    super.setPostHeader(response);
	    if (subjectService.updateSubject(subjectDto) > 0) {
		return new Response<Integer>(0, subjectDto.getId());
	    } else {
		return new Response<Integer>(2, "修改失败", 0);
	    }
	} catch (ServiceException e) {
	    return new Response<Integer>(1, e.getMessage(), 0);
	}
    }

    @RequestMapping(value = "/updateCategory", method = RequestMethod.POST)
    @ResponseBody
    public Response<Boolean> updateSubjectCategory(int subjectId, int categoryId, HttpServletResponse response)
	    throws ServiceException {
	super.setPostHeader(response);
	SubjectDTO subjectDto = subjectService.getSubjectById(subjectId);
	if(subjectDto == null){
	    return new Response<Boolean>(2, "课程不存在", false); 
	}
	SubjectCategoryDTO subjectCategoryDto = subjectCategoryService.getSubjectCategoryById(categoryId);
	if (subjectCategoryDto == null) {
	    return new Response<Boolean>(2, "类目不存在", false);
	}
	return new Response<Boolean>(0, subjectService.updateSubjectCategoryId(subjectId, categoryId));
    }

    @RequestMapping(value = "/stop", method = RequestMethod.GET)
    @ResponseBody
    public Response<Boolean> stopSubject(@RequestParam(value = "id") int id, HttpServletResponse response) {
	try {
	    super.setGetHeader(response);
	    return new Response<Boolean>(0, subjectService.updateSubjectState(id, SubjectStateEnum.STOP) > 0);
	} catch (ServiceException e) {
	    return new Response<Boolean>(1, e.getMessage(), false);
	}
    }

    @RequestMapping(value = "/count", method = RequestMethod.GET)
    @ResponseBody
    public Response<Integer> countSubject(@RequestParam(value = "keyword", required = false) String keyword,
	    @RequestParam(value = "categoryId", required = false) Integer categoryId,
	    @RequestParam(value = "state", required = false) String state, HttpServletResponse response) {
	try {
	    super.setGetHeader(response);
	    SubjectStateEnum stateEnum = null;
	    if (StringUtil.isNotEmpty(state)) {
		stateEnum = SubjectStateEnum.get(state);
		if (stateEnum == null) {
		    return new Response<Integer>(2, "状态参数错误.", null);
		}
	    }
	    return new Response<Integer>(0, subjectService.countSubject(keyword, categoryId, stateEnum));
	} catch (ServiceException e) {
	    return new Response<Integer>(1, e.getMessage(), null);
	}
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public Response<List<SubjectDTO>> listSubject(@RequestParam(value = "keyword", required = false) String keyword,
	    @RequestParam(value = "categoryId", required = false) Integer categoryId,
	    @RequestParam(value = "state", required = false) String state, @RequestParam(value = "pageNum") int pageNum,
	    @RequestParam(value = "pageSize") int pageSize, HttpServletResponse response) {
	try {
	    super.setGetHeader(response);
	    SubjectStateEnum stateEnum = null;
	    if (StringUtil.isNotEmpty(state)) {
		stateEnum = SubjectStateEnum.get(state);
		if (stateEnum == null) {
		    return new Response<List<SubjectDTO>>(2, "状态参数错误.", null);
		}
	    }
	    return new Response<List<SubjectDTO>>(0,
		    subjectService.listSubject(keyword, categoryId, stateEnum, pageNum, pageSize));
	} catch (ServiceException e) {
	    return new Response<List<SubjectDTO>>(1, e.getMessage(), null);
	}
    }

    @RequestMapping(value = "/sort", method = RequestMethod.GET)
    @ResponseBody
    public Response<Integer> sortSubject(@RequestParam(value = "front_id") int frontId,
	    @RequestParam(value = "id") int id, HttpServletResponse response) {
	try {
	    super.setPostHeader(response);
	    if (id > 0) {
		return new Response<Integer>(0, subjectService.sortSubject(frontId, id));
	    } else {
		return new Response<Integer>(2, "id参数不能为0!", 0);
	    }
	} catch (ServiceException e) {
	    return new Response<Integer>(1, e.getMessage(), 0);
	}
    }

    @RequestMapping(value = "/get", method = RequestMethod.GET)
    @ResponseBody
    public Response<SubjectDTO> getSubject(@RequestParam(value = "id") int id, HttpServletResponse response) {
	try {
	    super.setGetHeader(response);
	    return new Response<SubjectDTO>(0, subjectService.getSubjectById(id));
	} catch (ServiceException e) {
	    return new Response<SubjectDTO>(1, e.getMessage(), null);
	}
    }

    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    @ResponseBody
    public Response<Boolean> deleteSubject(@RequestParam(value = "id") int id, HttpServletResponse response) {
	try {
	    super.setGetHeader(response);
	    return new Response<Boolean>(0, subjectService.updateSubjectState(id, SubjectStateEnum.DELETE) > 0);
	} catch (ServiceException e) {
	    return new Response<Boolean>(1, e.getMessage(), false);
	}
    }
}