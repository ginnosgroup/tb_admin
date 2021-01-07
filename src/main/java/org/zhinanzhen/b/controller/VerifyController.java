package org.zhinanzhen.b.controller;

import com.ikasoa.core.utils.StringUtil;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zhinanzhen.b.dao.pojo.FinanceBankDO;
import org.zhinanzhen.b.dao.pojo.FinanceCodeDO;
import org.zhinanzhen.b.service.CommissionOrderService;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.VerifyService;
import org.zhinanzhen.b.service.VisaService;
import org.zhinanzhen.b.service.pojo.*;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.AdviserService;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.pojo.AdviserDTO;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: bsz
 * Date: 2020/12/18 13:30
 * Description:
 * Version: V1.0
 */
@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/verify")
public class VerifyController {

    @Resource
    private VerifyService verifyService;

    @Resource
    private CommissionOrderService commissionOrderService;

    @Resource
    private VisaService visaService;

    @Resource
    private AdviserService adviserService;

    @Resource
    ServiceOrderService serviceOrderService;

    @RequestMapping(value = "/uploadexcel",method = RequestMethod.POST)
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public Response uploadExcel(@RequestParam("file") MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename();
        List<FinanceCodeDO> financeCodeDOS = verifyService.excelToList(file.getInputStream(), fileName);

        for (FinanceCodeDO financeCodeDO : financeCodeDOS) {
            String orderId = financeCodeDO.getOrderId();
                if (orderId != null && orderId.substring(0,2).equalsIgnoreCase("CS")) {
                    CommissionOrderListDTO commissionOrderListDTO = commissionOrderService.getCommissionOrderById(Integer.parseInt(orderId.substring(2)));
                    if (commissionOrderListDTO!=null){
                        //financeCodeDO.setAdviser(commissionOrderListDTO.getAdviser().getName());
                        financeCodeDO.setAdviserId(commissionOrderListDTO.getAdviserId());
                        //financeCodeDO.setName(commissionOrderListDTO.getUser().getName());
                        financeCodeDO.setUserId(commissionOrderListDTO.getUserId());
                        financeCodeDO.setBusiness("留学-"+commissionOrderListDTO.getSchool().getName());
                        commissionOrderListDTO.setBankDate(financeCodeDO.getBankDate());
                        commissionOrderService.updateCommissionOrder(commissionOrderListDTO);
                    }
                }
                if (orderId != null && orderId.substring(0,2).equalsIgnoreCase("CV") ){
                    VisaDTO visaDTO =  visaService.getVisaById(Integer.parseInt(orderId.substring(2)));
                    if (visaDTO!=null){
                        financeCodeDO.setAdviserId(visaDTO.getAdviserId());
                        financeCodeDO.setUserId(visaDTO.getUserId());
                        ServiceOrderDTO serviceOrderDTO = serviceOrderService.getServiceOrderById(visaDTO.getServiceOrderId());
                        financeCodeDO.setBusiness(serviceOrderDTO.getService().getName()+"-"+serviceOrderDTO.getService().getCode());
                        visaDTO.setBankDate(financeCodeDO.getBankDate());
                        visaService.updateVisa(visaDTO);
                    }
                }
        }
        for (Iterator iterator = financeCodeDOS.listIterator(); iterator.hasNext();){
                FinanceCodeDO financeCodeDO = (FinanceCodeDO) iterator.next();
            if (StringUtil.isNotEmpty(financeCodeDO.getOrderId())){
                FinanceCodeDTO financeCodeDTO =  verifyService.financeCodeByOrderId(financeCodeDO.getOrderId());
                if (financeCodeDTO.getUser() != null & financeCodeDTO.getAdviser() != null){
                    iterator.remove();
                }
            }
        }
        //System.out.println(financeCodeDOS.size());
        if (verifyService.add(financeCodeDOS) > 0)
            return new Response(0,"success");
        return new Response(1,"fail");

    }

    @GetMapping(value = "/count")
    @ResponseBody
    public  Response count(@RequestParam(value = "bankDateStart",required = false) String bankDateStart,
                           @RequestParam(value = "bankDateEnd",required = false)String bankDateEnd){
        return  new Response(0,verifyService.count(bankDateStart,bankDateEnd+" 23:59:59"));
    }

    @GetMapping(value = "/list")
    @ResponseBody
    public  Response list(@RequestParam(value = "id",required = false)Integer id,
                          @RequestParam(value = "bankDateStart",required = false) String bankDateStart,
                          @RequestParam(value = "bankDateEnd",required = false)String bankDateEnd,
                          @RequestParam(value = "pageSize",required = true)Integer pageSize,
                          @RequestParam(value = "pageNum",required = true)Integer pageNumber){
        return  new Response(0,verifyService.list(bankDateStart,bankDateEnd+" 23:59:59",pageSize,pageNumber));
    }

    @PostMapping(value = "/update")
    @ResponseBody
    @Transactional
    public  Response update(@RequestParam(value = "orderId",required = true)String orderId,
                            @RequestParam(value = "id") Integer id) throws Exception {
        String order = orderId.substring(0,2);
        Integer number = Integer.parseInt(orderId.substring(2));
        if (number <= 0 | id <= 0 )
            throw new Exception("id or orderId error !");
        FinanceCodeDO financeCodeDO = verifyService.financeCodeById(id);
        if (financeCodeDO==null)
            return  new Response(0,"id is error");
        financeCodeDO.setOrderId(orderId);
        if (order.equalsIgnoreCase("CS")){
            CommissionOrderListDTO commissionOrderListDTO = commissionOrderService.getCommissionOrderById(number);
            if (commissionOrderListDTO == null)
                throw new Exception(" 没有此佣金订单:" + orderId +"!");
            financeCodeDO.setAdviserId(commissionOrderListDTO.getAdviserId());
            financeCodeDO.setUserId(commissionOrderListDTO.getUserId());
            financeCodeDO.setBusiness("留学-"+commissionOrderListDTO.getSchool().getName());
            commissionOrderListDTO.setBankDate(financeCodeDO.getBankDate());
            commissionOrderService.updateCommissionOrder(commissionOrderListDTO);
        }
        if (order.equalsIgnoreCase("CV")){
            VisaDTO visaDTO =  visaService.getVisaById(number);
            if (visaDTO == null)
                throw new Exception(" 没有此佣金订单:" + orderId +"!");
            financeCodeDO.setAdviserId(visaDTO.getAdviserId());
            financeCodeDO.setUserId(visaDTO.getUserId());
            ServiceOrderDTO serviceOrderDTO = serviceOrderService.getServiceOrderById(visaDTO.getServiceOrderId());
            financeCodeDO.setBusiness(serviceOrderDTO.getService().getName()+"-"+serviceOrderDTO.getService().getCode());
            visaDTO.setBankDate(financeCodeDO.getBankDate());
            visaService.updateVisa(visaDTO);
        }
        if( verifyService.update(financeCodeDO) > 0 ){
            return new Response(0,"success");
        }
        return  new Response(1,"fail");
    }


    @GetMapping(value = "/regionlist")
    @ResponseBody
    public Response regionlist(){
        return new Response(0,verifyService.regionList());
    }


    @GetMapping(value = "/adviserlist")
    @ResponseBody
    public Response adviserList(@RequestParam(value = "id",required = true)Integer id) throws ServiceException {
        return new Response<List<AdviserDTO>>(0, verifyService.adviserList(id));
    }

    @GetMapping(value = "/paymentcode")
    @ResponseBody
    public  Response getPaymentCode(@RequestParam(value = "adviserId")Integer adviserId) throws Exception {
        if (adviserId< 0)
            throw  new Exception("id error");
        return new Response(0,verifyService.getPaymentCode(adviserId));
    }

    @PostMapping(value = "/addbank")
    @ResponseBody
    public Response addBank(FinanceBankDO financeBankDO){
        if (verifyService.addBank(financeBankDO)>0)
            return new Response(0,"success");
        return new Response(1,"fail");
    }

    @GetMapping(value = "/banklist")
    @ResponseBody
    public Response bankList(@RequestParam(value = "pageSize",required = true)Integer pageSize,
                             @RequestParam(value = "pageNum",required = true)Integer pageNumber) throws Exception {
        if (pageNumber < 0 | pageSize <0)
            throw  new Exception("参数错误!");
        return new Response(0,verifyService.bankList(pageNumber,pageSize));
    }

    @GetMapping(value = "/bankcount")
    @ResponseBody
    public Response bankCount(){
        return new Response(0,verifyService.bankCount());
    }

    @PostMapping(value = "/bankupdate")
    @ResponseBody
    public Response bankUpdate(FinanceBankDO financeBankDO) throws Exception {

        if (financeBankDO.getId() <= 0 )
            throw  new Exception(" id error !");
        if (verifyService.bankUpdate(financeBankDO)>0)
            return new Response(0,"success");
        return new Response(1,"fail");
    }

    @PostMapping(value = "/updatefinancebankid")
    @ResponseBody
    public Response updateFinanceBankId(@RequestParam(value = "id") Integer id ,
                                        @RequestParam(value = "financeBankId")Integer financeBankId) throws Exception {
        try {
            if (verifyService.updateFinanceBankId(id,financeBankId)>0)
                return new Response(0,"sucess");
        }catch (Exception e){
            throw new Exception("系统出现异常，请联系管理员");
        }
        return new Response(1,"fail");
    }

}
