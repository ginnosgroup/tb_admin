package org.zhinanzhen.b.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.zhinanzhen.b.service.PdfGenerateService;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/pdfGenerate")
public class PdfGenerateController extends BaseController{
    @Resource
    private PdfGenerateService pdfGenerateService;

    @RequestMapping(value = "/generate", method = RequestMethod.POST)
    @ResponseBody
    public Response generate(@RequestParam(value = "id" ) Integer id,
     HttpServletResponse response){
        try {
            super.setPostHeader(response);

            int generate = pdfGenerateService.generate(id);
            if (generate>0)
                return new Response(0, "生成成功",generate);
            else
                return new Response(1, "生成失败",0);
        }catch (ServiceException e){
            return new Response(1, e.getMessage(),0);
        }
    }
}
