package org.zhinanzhen.b.controller;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.zhinanzhen.b.service.OfficialGradeService;
import org.zhinanzhen.b.service.pojo.OfficialGradeDTO;
import org.zhinanzhen.b.service.pojo.OfficialTagDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/officialGrade")
public class OfficialGradeController extends BaseController {

    @Resource
    OfficialGradeService officialGradeService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public Response<List<OfficialGradeDTO>> get(@RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
                                                HttpServletRequest request, HttpServletResponse response) {
        AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
        if (adminUserLoginInfo != null)
            if (adminUserLoginInfo == null || !isSuperAdminUser(request) && !adminUserLoginInfo.isOfficialAdmin())
                return new Response<List<OfficialGradeDTO>>(1, "No permission !", null);
        try {
            super.setPostHeader(response);
            return new Response<List<OfficialGradeDTO>>(0, officialGradeService.listOfficialGrade(pageNum, pageSize));
        } catch (ServiceException e) {
            return new Response<List<OfficialGradeDTO>>(e.getCode(), e.getMessage(), null);
        }
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public Response<Integer> addGrade(@RequestParam(value = "grade") String grade,
                                      @RequestParam(value = "rate") String rate,
                                      @RequestParam(value = "ruler") Integer ruler,
                                      HttpServletRequest request, HttpServletResponse response) {
        AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
        if (adminUserLoginInfo != null)
            if (adminUserLoginInfo == null || !isSuperAdminUser(request))
                return new Response<Integer>(1, "仅限管理员操作", null);
        try {
            super.setPostHeader(response);
            OfficialGradeDTO officialGradeDTO = new OfficialGradeDTO();
            officialGradeDTO.setGrade(grade);
            officialGradeDTO.setRate(Double.parseDouble(rate));
            officialGradeDTO.setRuler(ruler);
            if (officialGradeService.addOfficialGrade(officialGradeDTO) > 0)
                return new Response<Integer>(0, "添加成功", 0);
            else
                return new Response<Integer>(1, "添加失败", 0);

        } catch (ServiceException e) {
            return new Response<Integer>(e.getCode(), e.getMessage(), null);
        }
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public Response<Integer> updateGrade(
            @RequestParam(value = "id") Integer id,
            @RequestParam(value = "grade") String grade,
            @RequestParam(value = "rate") String rate,
            @RequestParam(value = "ruler") Integer ruler,
            HttpServletRequest request, HttpServletResponse response) {
        AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
        if (adminUserLoginInfo != null)
            if (adminUserLoginInfo == null || !isSuperAdminUser(request))
                return new Response<Integer>(1, "仅限管理员操作", null);
        try {
            super.setPostHeader(response);
            OfficialGradeDTO officialGradeDTO = new OfficialGradeDTO();
            officialGradeDTO.setId(id);
            officialGradeDTO.setGrade(grade);
            officialGradeDTO.setRate(Double.parseDouble(rate));
            officialGradeDTO.setRuler(ruler);
            if (officialGradeService.updateOfficialGradeById(officialGradeDTO) > 0)
                return new Response<Integer>(0, "修改成功", 0);
            else
                return new Response<Integer>(1, "修改失败", 0);

        } catch (ServiceException e) {
            return new Response<Integer>(e.getCode(), e.getMessage(), null);
        }
    }

    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    @ResponseBody
    public Response<Boolean> deleteOfficialGrade(@RequestParam(value = "id") Integer id, HttpServletRequest request,
                                                 HttpServletResponse response) {
        AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
        if (adminUserLoginInfo != null)
            if (adminUserLoginInfo == null || !isSuperAdminUser(request))
                return new Response<Boolean>(1, "仅限管理员操作", null);
        try {
            super.setPostHeader(response);
            if (officialGradeService.deleteOfficialGradeById(id) > 0)
                return new Response<Boolean>(0, "删除成功", true);
            else
                return new Response<Boolean>(1, "删除失败", false);
        } catch (ServiceException e) {
            return new Response<Boolean>(e.getCode(), e.getMessage(), false);
        }
    }


}

