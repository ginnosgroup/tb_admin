package org.zhinanzhen.b.controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zhinanzhen.b.dao.pojo.customer.CustomerInformationDO;
import org.zhinanzhen.b.service.CustomerInformationService;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/customerInformation")
public class CustomerInformationController extends BaseController {

    @Resource
    private CustomerInformationService customerInformationService;

    @Resource
    private ServiceOrderService serviceOrderService;

    @GetMapping("/get")
    public Response<CustomerInformationDO> get(@RequestParam(value = "id") int id, HttpServletRequest request,
                                               HttpServletResponse response) {

//        AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
//        if (adminUserLoginInfo == null) {
//            return new Response(1, "No permission !");
//        }

        try {
            CustomerInformationDO customerInformationDO = customerInformationService.get(id);
            return new Response(0, "获取成功", customerInformationDO);
        } catch (ServiceException e) {
            e.printStackTrace();
            return new Response(1, e.getMessage());
        }
    }

    @GetMapping("/getByApplicantId")
    public Response<CustomerInformationDO> getByApplicantId(@RequestParam(value = "applicantId") int applicantId, HttpServletRequest request,
                                               HttpServletResponse response) {
        try {
            CustomerInformationDO customerInformationDO = customerInformationService.getByApplicantId(applicantId);
            return new Response(0, "获取成功", customerInformationDO);
        } catch (ServiceException e) {
            e.printStackTrace();
            return new Response(1, e.getMessage());
        }
    }

    @PostMapping("/add")
    public Response<Integer> add(@RequestBody String json,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {

//    AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
//    if (adminUserLoginInfo == null ){
//        return new Response(1,"No permission !");
//    }
        try {
            super.setPostHeader(response);
            CustomerInformationDO customerInformationDO = JSONObject.parseObject(json, CustomerInformationDO.class);
            ServiceOrderDTO order = serviceOrderService.getServiceOrderById(customerInformationDO.getServiceOrderId());
            if (order == null)
                return new Response<>(1, "服务订单不存在");
            if (customerInformationService.getByServiceOrderId(customerInformationDO.getServiceOrderId()) != null)
                return new Response<>(1, "你已提交过相关信息，如需修改请联系你的顾问");
            customerInformationDO.setApplicantId(order.getApplicantId());
            customerInformationService.add(customerInformationDO);
            return new Response<>(0, "success");
        } catch (ServiceException e) {
            return new Response<>(1, e.getMessage());
        }
    }

    @PutMapping("/update")
    public Response<Integer> update(@RequestBody String json, HttpServletRequest request) {

        AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
        if (adminUserLoginInfo == null) {
            return new Response(1, "No permission !");
        }
        try {
            CustomerInformationDO customerInformationDO = JSONObject.parseObject(json, CustomerInformationDO.class);
            customerInformationService.update(customerInformationDO);
            return new Response<>(0, "success");
        } catch (ServiceException e) {
            return new Response<>(1, e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public Response<Integer> delete(@RequestParam("id") int id, HttpServletRequest request) {
        AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
        if (adminUserLoginInfo == null) {
            return new Response(1, "No permission !");
        }
        try {

            customerInformationService.delete(id);
            return new Response<>(0, "success");
        } catch (ServiceException e) {
            return new Response<>(1, e.getMessage());
        }
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public Response<String> uploadImage(@RequestParam MultipartFile file, @RequestParam("familyName") String familyName,
                                        @RequestParam("givenName") String givenName,
                                        @RequestParam ("name") String name,
                                        HttpServletRequest request,
                                        HttpServletResponse response) throws IOException {
        super.setPostHeader(response);
        try {
            String upload = customerInformationService.upload( familyName,givenName,name, file);
            return new Response<String>(0,"success",upload);
        }
        catch (ServiceException e) {
            return new Response<String>(1, e.getMessage(),null);
        }

    }

    @RequestMapping(value = "/deleteFile", method = RequestMethod.POST)
    @ResponseBody
    public Response<Integer> deleteFile(@RequestParam ("url") String url,
                                        HttpServletRequest request,
                                        HttpServletResponse response) throws IOException {
        super.setPostHeader(response);
        try {
            customerInformationService.deleteFile(url);
            return new Response<Integer>(0,"success");
        }
        catch (ServiceException e) {
            return new Response<Integer>(1, e.getMessage(),null);
        }

    }

    @RequestMapping(value = "/getMMFile", method = RequestMethod.GET)
    @ResponseBody
    public Response<List<String>> deleteFile(@RequestParam ("applicantId") int applicantId,
                                        HttpServletRequest request,
                                        HttpServletResponse response) throws IOException {
        super.setPostHeader(response);
        try {
            List<String> urlList = customerInformationService.getFileByDav(applicantId);
            return new Response<List<String>>(0,urlList);
        }
        catch (ServiceException e) {
            return new Response<List<String>>(1, e.getMessage(),null);
        }

    }


}