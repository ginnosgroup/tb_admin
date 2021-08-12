package org.zhinanzhen.b.controller;

import com.ikasoa.core.utils.StringUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.zhinanzhen.b.service.SchoolInstitutionLocationService;
import org.zhinanzhen.b.service.pojo.SchoolInstitutionLocationDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/04/08 9:55
 * Description:
 * Version: V1.0
 */
@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping(value = "/schoolInstitutionLocation")
public class SchoolInstitutionLocationController extends BaseController {

    @Resource
    private SchoolInstitutionLocationService schoolInstitutionLocationService;

    @RequestMapping(value = "/list",method = RequestMethod.GET)
    @ResponseBody
    public Response list(@RequestParam(value = "id",required = false)Integer id,
                         @RequestParam(value = "providerId",required = false)Integer providerId,
                         @RequestParam(value = "providerCode",required = false)String providerCode){
        if (id != null && id > 0 ){
            return new Response(0,schoolInstitutionLocationService.getById(id));
        }
        return  new Response(0,"ok",schoolInstitutionLocationService.list(providerId,providerCode));
    }

    @RequestMapping(value = "/get",method = RequestMethod.GET)
    @ResponseBody
    public  Response get(@RequestParam(value = "id")int id){
        return new Response(0,schoolInstitutionLocationService.getById(id));
    }

    @RequestMapping(value = "/add",method = RequestMethod.POST)
    @ResponseBody
    public Response add(@RequestBody SchoolInstitutionLocationDTO schoolInstitutionLocationDTO, HttpServletResponse response){
        super.setPostHeader(response);
        if (schoolInstitutionLocationService.add(schoolInstitutionLocationDTO) > 0 )
            return new Response(0,"success",schoolInstitutionLocationDTO);
        else
            return new Response(1,"fail");
    }

    @RequestMapping(value = "/update",method = RequestMethod.POST)
    @ResponseBody
    public  Response update(@RequestParam("id") int id,
                            @RequestParam(value = "name",required = false)String name,
                            @RequestParam(value = "state",required = false)String state,
                            @RequestParam(value = "numberOfCourses",required = false)String numberOfCourses){
        SchoolInstitutionLocationDTO schoolInstitutionLocationDTO = schoolInstitutionLocationService.getById(id);
        if (schoolInstitutionLocationDTO == null)
            return new Response(1,"没有此记录");
        if (StringUtil.isNotEmpty(name))
            schoolInstitutionLocationDTO.setName(name);
        if (StringUtil.isNotEmpty(state))
            schoolInstitutionLocationDTO.setState(state);
        if (StringUtil.isNotEmpty(numberOfCourses))
            schoolInstitutionLocationDTO.setNumberOfCourses(StringUtil.toInt(numberOfCourses));
        if (schoolInstitutionLocationService.update(schoolInstitutionLocationDTO))
            return new Response(0,"success");
        else
            return new Response(1,"fail");
    }

    @RequestMapping(value = "/delete",method = RequestMethod.POST)
    @ResponseBody
    public Response delete(@RequestParam(value = "id")int id){
        if (schoolInstitutionLocationService.delete(id))
            return new Response(0,"success");
        else
            return new Response(1,"fail");
    }
}
