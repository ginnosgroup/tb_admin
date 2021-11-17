package org.zhinanzhen.b.controller;

import com.ikasoa.core.utils.StringUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.zhinanzhen.b.service.SchoolCourseService;
import org.zhinanzhen.b.service.pojo.SchoolCourseDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.ListResponse;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


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
                         @RequestParam(value = "isFreeze",required = false) Boolean isFreeze,
                         @RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize){
        if (id != null && id > 0 ) {
            return new ListResponse(true, pageSize, 1, schoolCourseService.schoolCourseById(id), "ok");
        }
        int total = schoolCourseService.count(providerId,providerCode,isFreeze);
        return  new ListResponse(true,pageSize,total,schoolCourseService.list(providerId,providerCode, isFreeze,
                null, null, pageNum,pageSize),"ok");
    }

    @RequestMapping(value = "/delete",method = RequestMethod.POST)
    @ResponseBody
    public Response delete(@RequestBody SchoolCourseDTO schoolCourseDTO,
                           HttpServletRequest request, HttpServletResponse response){
        super.setPostHeader(response);
        AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
        //if (adminUserLoginInfo == null ||
        //        (adminUserLoginInfo.getApList().equalsIgnoreCase("GW") && adminUserLoginInfo.getRegionId() == null))//除顾问的其他角色可以修改
        if (adminUserLoginInfo == null)
            return new Response(1,"No permission !");
        if ( schoolCourseDTO.getId() <= 0 || schoolCourseService.schoolCourseById(schoolCourseDTO.getId()) == null)
            return new Response(1,"id error");
        if (schoolCourseService.delete(schoolCourseDTO.getId()))
            return new Response(0,"success");
        else
            return new Response(1,"fail");
    }

    @RequestMapping(value = "/add",method = RequestMethod.POST)
    @ResponseBody
    public Response add(@RequestBody SchoolCourseDTO schoolCourseDTO,
                        HttpServletRequest request, HttpServletResponse response){
        super.setPostHeader(response);
        AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
        //if (adminUserLoginInfo == null ||
        //        (adminUserLoginInfo.getApList().equalsIgnoreCase("GW") && adminUserLoginInfo.getRegionId() == null))//除顾问的其他角色可以修改
        if (adminUserLoginInfo == null)
            return new Response(1,"No permission !");
        Response res = new Response(0, "success");
        if (StringUtil.isBlank(schoolCourseDTO.getCourseCode()) || StringUtil.isBlank(schoolCourseDTO.getCourseSector())
                || StringUtil.isBlank(schoolCourseDTO.getCourseName())){
            res.setCode(1);
            res.setMessage("专业编码或专业所属领域或专业名字不能为空!");
            return res;
        }
        if (schoolCourseService.list(null,null, null,
                null, schoolCourseDTO.getCourseCode(), 0,1).size() > 0){
            res.setCode(1);
            res.setMessage("专业编码已经存在!" + schoolCourseDTO.getCourseCode());
            return res;
        }
        if (schoolCourseService.add(schoolCourseDTO) > 0){ ;
            res.setData(schoolCourseDTO);
            return res;
        } else{
            res.setCode(1);
            res.setMessage("fail");
            return res;
        }
    }

    @RequestMapping(value = "/update",method = RequestMethod.POST)
    @ResponseBody
    public Response update(@RequestBody SchoolCourseDTO schoolCourseDTO,
                           HttpServletRequest request, HttpServletResponse response){
        super.setPostHeader(response);
        AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
        //if (adminUserLoginInfo == null ||
        //        (adminUserLoginInfo.getApList().equalsIgnoreCase("GW") && adminUserLoginInfo.getRegionId() == null))//除顾问的其他角色可以修改
        if (adminUserLoginInfo == null)
            return new Response(1,"No permission !");
        if (schoolCourseService.schoolCourseById(schoolCourseDTO.getId()) == null)
            return new Response(1,"没有此课程");
        if (schoolCourseService.update(schoolCourseDTO))
            return new Response(0,"success",schoolCourseDTO);
        else
            return new Response(1,"fail");

    }

    /**
     * @param providerId
     * @param request
     * @param response
     * @throws ServiceException
     * 通过学校id获取学校的专业list
     */
    @GetMapping(value = "/getCourseLevel")
    @ResponseBody
    public Response<List<String>> getCourseLevel(@RequestParam("providerId") int providerId,
                                                 HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        super.setGetHeader(response);
        if (providerId <= 0)
            return new Response<List<String>>(1,"id error",null);
        return  new Response<List<String>>(0,"success",schoolCourseService.getCourseLevelList(providerId));
    }

    /**
     * 通过学校id可以筛选查询专业,专业中携带 佣金规则
     * @param providerId
     * @param courseLevel
     * @param courseName
     * @param courseCode
     * @param pageNum
     * @param pageSize
     * @param request
     * @param response
     * @return
     */
    @GetMapping(value = "/getCourseToSetSetting")
    @ResponseBody
    public Response getCourseToSetting(@RequestParam(value = "providerId")int providerId,
                                       @RequestParam(value = "courseLevel", required = false) String courseLevel,
                                       @RequestParam(value = "courseName", required = false) String courseName,
                                       @RequestParam(value = "courseCode", required = false) String courseCode,
                                       @RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
                                       HttpServletRequest request, HttpServletResponse response){
        super.setGetHeader(response);
        if (providerId <= 0)
            return new Response(1,"providerId error !");
        List<SchoolCourseDTO> schoolCourseDTOS = schoolCourseService.getCourseToSetting(providerId,courseLevel,courseName,courseCode,
                pageNum,pageSize);
        return new Response(0,"success",schoolCourseDTOS);
    }

}
