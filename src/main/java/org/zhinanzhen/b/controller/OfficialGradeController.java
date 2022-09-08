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

    @RequestMapping(value = "/get", method = RequestMethod.GET)
    @ResponseBody
    public Response<List<OfficialGradeDTO>> get(HttpServletRequest request, HttpServletResponse response){
        AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
        if (adminUserLoginInfo!=null)
            if (adminUserLoginInfo == null || !isSuperAdminUser(request))
                return new Response<List<OfficialGradeDTO>>(1, "仅限管理员操作.", null);
        try {
            super.setPostHeader(response);
            return new Response<List<OfficialGradeDTO>>(0,officialGradeService.getOfficialGrade());
        }catch (ServiceException e) {
            return new Response<List<OfficialGradeDTO>>(e.getCode(), e.getMessage(), null);
        }
    }
    @RequestMapping(value = "/add",method = RequestMethod.POST)
    @ResponseBody
    public Response<Integer> addGrade(@RequestParam(value = "grade") Integer grade,
            @RequestParam(value = "rate") String rate,
            @RequestParam(value = "ruler") Integer ruler,
            HttpServletRequest request, HttpServletResponse response) {
        AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
        if (adminUserLoginInfo!=null)
            if (adminUserLoginInfo == null || !isSuperAdminUser(request))
                return new Response<Integer>(1, "仅限管理员操作.", null);
        try {
            super.setPostHeader(response);
            OfficialGradeDTO officialGradeDTO = new OfficialGradeDTO();
            officialGradeDTO.setGrade(grade);
            officialGradeDTO.setRate(Double.parseDouble(rate));
            officialGradeDTO.setRuler(ruler);
            if (officialGradeService.addOfficialGrade(officialGradeDTO) > 0)
                return new Response<Integer>(0, "修改成功",0);
            else
                return new Response<Integer>(1, "修改失败.", 0);

        }catch (ServiceException e) {
            return new Response<Integer>(e.getCode(), e.getMessage(), null);
        }
    }
}

