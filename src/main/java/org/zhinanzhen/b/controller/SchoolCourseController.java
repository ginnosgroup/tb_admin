package org.zhinanzhen.b.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.zhinanzhen.b.service.SchoolCourseService;
import org.zhinanzhen.b.service.pojo.SchoolCourseDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.ListResponse;
import org.zhinanzhen.tb.controller.Response;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;


/**
 * Created with IntelliJ IDEA.
 * Date: 2021/04/07 9:15
 * Description:
 * Version: V1.0
 */
@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping(value = "/schoolCourse")
public class SchoolCourseController extends BaseController {

    @Resource
    private SchoolCourseService schoolCourseService;

    @RequestMapping(value = "/list",method = RequestMethod.GET)
    @ResponseBody
    public ListResponse list(@RequestParam(value = "id",required = false) Integer id,
                         @RequestParam(value = "providerId",required = false) Integer providerId,
                         @RequestParam(value = "providerCode",required = false) String providerCode,
                         @RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize){
        if (id != null && id > 0 ) {
            return new ListResponse(true, pageSize, 1, schoolCourseService.schoolCourseById(id), "ok");
        }
        int total = schoolCourseService.count(providerId,providerCode);
        return  new ListResponse(true,pageSize,total,schoolCourseService.list(providerId,providerCode,pageNum,pageSize),"ok");
    }

    @RequestMapping(value = "/delete",method = RequestMethod.POST)
    @ResponseBody
    public Response delete(@RequestBody SchoolCourseDTO schoolCourseDTO){
        if ( schoolCourseDTO.getId() <= 0 || schoolCourseService.schoolCourseById(schoolCourseDTO.getId()) == null)
            return new Response(1,"id error");
        if (schoolCourseService.delete(schoolCourseDTO.getId()))
            return new Response(0,"success");
        else
            return new Response(1,"fail");
    }

    @RequestMapping(value = "/add",method = RequestMethod.POST)
    @ResponseBody
    public Response add(@RequestBody SchoolCourseDTO schoolCourseDTO, HttpServletResponse response){
        super.setPostHeader(response);
        if (schoolCourseService.add(schoolCourseDTO) > 0)
            return new Response(0,"success",schoolCourseDTO);
        else
            return new Response(1,"fail");
    }

    @RequestMapping(value = "/update",method = RequestMethod.POST)
    @ResponseBody
    public Response update(@RequestBody SchoolCourseDTO schoolCourseDTO, HttpServletResponse response){
        super.setPostHeader(response);
        if (schoolCourseService.schoolCourseById(schoolCourseDTO.getId()) == null)
            return new Response(1,"没有此课程");
        if (schoolCourseService.update(schoolCourseDTO))
            return new Response(0,"success",schoolCourseDTO);
        else
            return new Response(1,"fail");

    }
}
