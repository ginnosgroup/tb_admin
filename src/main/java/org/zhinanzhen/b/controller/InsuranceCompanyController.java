package org.zhinanzhen.b.controller;

import com.ikasoa.core.utils.StringUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.zhinanzhen.b.dao.pojo.InsuranceCompanyDO;
import org.zhinanzhen.b.service.InsuranceCompanyService;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.ListResponse;
import org.zhinanzhen.tb.controller.Response;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/insuranceCompany")
public class InsuranceCompanyController extends BaseController {

    @Resource
    private InsuranceCompanyService insuranceCompanyService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public ListResponse<List<InsuranceCompanyDO>> listServiceOrder(
            @RequestParam(value = "id", required = false) Integer id,
            @RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
            HttpServletRequest request,
            HttpServletResponse response) {
        try {
            super.setGetHeader(response);
            AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
            boolean isSUPERAD = false;
            if ("SUPERAD".equals(adminUserLoginInfo.getApList())) {
                isSUPERAD = true;
            }
            Integer count = insuranceCompanyService.count(id, isSUPERAD);
            List<InsuranceCompanyDO> list = insuranceCompanyService.list(id, isSUPERAD, pageNum, pageSize);
            return new ListResponse<List<InsuranceCompanyDO>>(true, pageSize, count, list, "");
        } catch (Exception e) {
            e.printStackTrace();
            return new ListResponse<List<InsuranceCompanyDO>>(false, pageSize, 0, null, e.getMessage());
        }
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public Response<Integer> add(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "marks", required = false) String marks,
            @RequestParam(value = "isRecommend", required = false) boolean isRecommend,
            HttpServletRequest request, HttpServletResponse response) {
        try {
            super.setGetHeader(response);
            AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
            if (!"SUPERAD".equals(adminUserLoginInfo.getApList())) {
                return new Response<Integer>(1, "仅限超级管理员能创建保险公司信息.", 0);
            }
            InsuranceCompanyDO insuranceCompanyDO = new InsuranceCompanyDO();
            if (StringUtil.isNotEmpty(name)) {
                insuranceCompanyDO.setName(name);
            }
            if (StringUtil.isNotEmpty(marks)) {
                insuranceCompanyDO.setMarks(marks);
            }
            insuranceCompanyDO.setRecommend(isRecommend);
            Integer i = insuranceCompanyService.add(insuranceCompanyDO);
            return new Response<Integer>(0, "添加成功", i);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<Integer>(0, "添加失败" + e.getMessage(), 0);
        }
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public Response<Integer> update(
            @RequestParam(value = "id", required = false) Integer id,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "marks", required = false) String marks,
            @RequestParam(value = "isRecommend", required = false) boolean isRecommend,
            HttpServletRequest request, HttpServletResponse response) {
        try {
            super.setGetHeader(response);
            AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
            if (!"SUPERAD".equals(adminUserLoginInfo.getApList())) {
                return new Response<Integer>(1, "仅限超级管理员能修改保险公司信息.", 0);
            }
            InsuranceCompanyDO insuranceCompanyDO = new InsuranceCompanyDO();
            insuranceCompanyDO.setId(id);
            if (StringUtil.isNotEmpty(name)) {
                insuranceCompanyDO.setName(name);
            }
            if (StringUtil.isNotEmpty(marks)) {
                insuranceCompanyDO.setMarks(marks);
            }
            insuranceCompanyDO.setRecommend(isRecommend);
            Integer i = insuranceCompanyService.update(insuranceCompanyDO);
            return new Response<Integer>(0, "修改成功", i);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<Integer>(0, "修改失败" + e.getMessage(), 0);
        }
    }

}
