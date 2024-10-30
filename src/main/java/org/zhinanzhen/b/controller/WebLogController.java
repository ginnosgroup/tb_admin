package org.zhinanzhen.b.controller;

import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.zhinanzhen.b.service.pojo.WebLogDTO;
import org.zhinanzhen.b.service.WebLogService;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.ListResponse;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/webLog")
@Log4j
public class WebLogController extends BaseController {

    @Resource
    private WebLogService webLogService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public ListResponse<List<WebLogDTO>> listServiceOrder(
            @RequestParam(value = "serviceOrderId", required = false) Integer serviceOrderId,
            @RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
             HttpServletRequest request, HttpServletResponse response) {
        try {
            super.setGetHeader(response);
            Integer total = webLogService.count(serviceOrderId);
            List<WebLogDTO> webLogDTOS = webLogService.listByServiceOrderId(serviceOrderId, pageNum, pageSize);
            return new ListResponse<List<WebLogDTO>>(true, pageSize, total, webLogDTOS, "");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
