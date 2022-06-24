package org.zhinanzhen.b.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.zhinanzhen.b.dao.pojo.ServiceAssessDO;
import org.zhinanzhen.b.service.ServiceAssessService;
import org.zhinanzhen.b.service.ServiceService;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: bsz
 * Date: 2020/11/25 16:36
 * Description:
 * Version: V1.0
 */
@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/serviceAssess")
public class ServiceAssessCntroller {


    @Resource
    private ServiceAssessService serviceAssessService;

    @Resource
    private ServiceService serviceService;


    @RequestMapping(value = "/add",method = RequestMethod.POST)
    @ResponseBody
    public Response add(@RequestParam(value = "serviceId") Integer serviceId ,
                        @RequestParam(value = "name") String name) throws ServiceException {
        if (serviceService.getServiceById(serviceId) == null)
            return new Response<Integer>(1, "服务项目不存在(" + serviceId + ")!", 0);
        if ( serviceAssessService.add(name,serviceId) > 0 )
            return new Response(0,"success");
        return new Response(1,"fail");
    }

    @RequestMapping(value = "/list",method = RequestMethod.GET)
    @ResponseBody
    public Response list(@RequestParam(value = "serviceId",required = false) Integer  serviceId){
        List<ServiceAssessDO> lists = serviceAssessService.list(serviceId);
        return new Response(0,lists);
    }

    @RequestMapping(value = "/update",method = RequestMethod.POST)
    @ResponseBody
    public  Response update(@RequestParam(value = "name")String name,
                            @RequestParam(value = "id")Integer id){
        if (serviceAssessService.update(id,name ) > 0 )
            return new Response(0,"success");
        return new Response(1,"fail");
    }

    @RequestMapping(value = "delete", method = RequestMethod.POST)
    @ResponseBody
    public Response delete(@RequestParam(value = "id")Integer id){
        if (serviceAssessService.delete(id) > 0 )
            return new Response(0,"success");
        return new Response(1,"fail");
    }

}
