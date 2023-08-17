package org.zhinanzhen.b.controller;

import com.ikasoa.core.utils.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.zhinanzhen.b.service.SchoolInstitutionLocationService;
import org.zhinanzhen.b.service.pojo.SchoolInstitutionLocationDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

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
	
	private static final Logger LOG = LoggerFactory.getLogger(SchoolInstitutionLocationController.class);

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
    public Response add(@RequestBody SchoolInstitutionLocationDTO schoolInstitutionLocationDTO,
                        HttpServletRequest request, HttpServletResponse response){
        super.setPostHeader(response);
        AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
        if (adminUserLoginInfo == null ||
                (adminUserLoginInfo.getApList().equalsIgnoreCase("GW") && adminUserLoginInfo.getRegionId() == null))//除顾问的其他角色可以修改
            return new Response(1,"No permission !");
        if (schoolInstitutionLocationService.add(schoolInstitutionLocationDTO) > 0 )
            return new Response(0,"success",schoolInstitutionLocationDTO);
        else
            return new Response(1,"fail");
    }

    @RequestMapping(value = "/update",method = RequestMethod.POST)
    @ResponseBody
    public  Response update(@RequestBody SchoolInstitutionLocationDTO _schoolInstitutionLocationDTO,
                            HttpServletRequest request, HttpServletResponse response){
        super.setPostHeader(response);
        AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
        if (adminUserLoginInfo == null ||
                (adminUserLoginInfo.getApList().equalsIgnoreCase("GW") && adminUserLoginInfo.getRegionId() == null))//除顾问的其他角色可以修改
            return new Response(1,"No permission !");
        SchoolInstitutionLocationDTO schoolInstitutionLocationDTO = schoolInstitutionLocationService.getById(_schoolInstitutionLocationDTO.getId());
        if (schoolInstitutionLocationDTO == null)
            return new Response(1,"没有此记录");
        if (StringUtil.isNotEmpty(_schoolInstitutionLocationDTO.getName()))
            schoolInstitutionLocationDTO.setName(_schoolInstitutionLocationDTO.getName());
        if (StringUtil.isNotEmpty(_schoolInstitutionLocationDTO.getState()))
            schoolInstitutionLocationDTO.setState(_schoolInstitutionLocationDTO.getState());
        if (_schoolInstitutionLocationDTO.getNumberOfCourses() > 0)
            schoolInstitutionLocationDTO.setNumberOfCourses(_schoolInstitutionLocationDTO.getNumberOfCourses());
        if (StringUtil.isNotEmpty(_schoolInstitutionLocationDTO.getPhone()))
            schoolInstitutionLocationDTO.setPhone(_schoolInstitutionLocationDTO.getPhone());
        if (StringUtil.isNotEmpty(_schoolInstitutionLocationDTO.getAddress()))
            schoolInstitutionLocationDTO.setAddress(_schoolInstitutionLocationDTO.getAddress());
        if (schoolInstitutionLocationService.update(schoolInstitutionLocationDTO))
            return new Response(0,"success");
        else
            return new Response(1,"fail");
    }

    @RequestMapping(value = "/delete",method = RequestMethod.POST)
    @ResponseBody
    public Response delete(@RequestParam(value = "id")int id,
                           HttpServletRequest request, HttpServletResponse response){
        super.setPostHeader(response);
        AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
        if (adminUserLoginInfo == null ||
                (adminUserLoginInfo.getApList().equalsIgnoreCase("GW") && adminUserLoginInfo.getRegionId() == null))//除顾问的其他角色可以修改
            return new Response(1,"No permission !");
        LOG.info(StringUtil.merge("删除校区:", schoolInstitutionLocationService.getById(id)));
        if (schoolInstitutionLocationService.delete(id))
            return new Response(0,"success");
        else
            return new Response(1,"fail");
    }

    @GetMapping(value = "/getState")
    @ResponseBody
    public Response<List<String>> getState(HttpServletRequest request,
                                           HttpServletResponse response){
        super.setGetHeader(response);
        return new Response<List<String>>(0,schoolInstitutionLocationService.getState());
    }

}
