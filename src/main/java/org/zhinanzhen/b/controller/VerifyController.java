package org.zhinanzhen.b.controller;

import com.ikasoa.core.utils.StringUtil;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    private  SimpleDateFormat  sdfbankDatein = new SimpleDateFormat("dd/MM/yyyy");

    private  SimpleDateFormat  sdfbankDateout = new SimpleDateFormat("yyyy-MM-dd");

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
                        if (commissionOrderListDTO.getBankDate()==null)
                            commissionOrderListDTO.setBankDate(financeCodeDO.getBankDate());
                        if (!commissionOrderListDTO.isChecked())
                            commissionOrderListDTO.setChecked(true);
                        if (StringUtil.isEmpty(commissionOrderListDTO.getBankCheck()))
                            commissionOrderListDTO.setBankCheck("手工");
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
                        if (visaDTO.getBankDate()==null)
                            visaDTO.setBankDate(financeCodeDO.getBankDate());
                        if (!visaDTO.isChecked())
                            visaDTO.setChecked(true);
                        if (StringUtil.isEmpty(visaDTO.getBankCheck()))
                            visaDTO.setBankCheck("手工");
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

    @GetMapping(value = "/down")
    @ResponseBody
    public  void down(@RequestParam(value = "bankDateStart",required = false) String bankDateStart,
                      @RequestParam(value = "bankDateEnd",required = false)String bankDateEnd,
                      HttpServletRequest request, HttpServletResponse response){

        try {
            if (StringUtil.isNotEmpty(bankDateStart))
                bankDateStart = sdfbankDateout.format(sdfbankDatein.parse(bankDateStart));
            if (StringUtil.isNotEmpty(bankDateEnd))
                bankDateEnd = sdfbankDateout.format(sdfbankDatein.parse(bankDateEnd));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        List<FinanceCodeDTO> financeCodeDTOS = verifyService.list(bankDateStart,bankDateEnd+" 23:59:59",9999,0);

        try {
            response.reset();// 清空输出流
            String tableName = "bankstatement";
            response.setHeader("Content-disposition",
                    "attachment; filename=" + new String(tableName.getBytes("GB2312"), "8859_1") + ".xls");
            response.setContentType("application/msexcel");

            OutputStream os = response.getOutputStream();
            jxl.Workbook wb;
            InputStream is;
            try {
                is = this.getClass().getResourceAsStream("/bankstatement.xls");
            } catch (Exception e) {
                throw new Exception("模版不存在");
            }
            try {
                wb = Workbook.getWorkbook(is);
            } catch (Exception e) {
                throw new Exception("模版格式不支持");
            }
            WorkbookSettings settings = new WorkbookSettings();
            settings.setWriteAccess(null);
            jxl.write.WritableWorkbook wbe = Workbook.createWorkbook(os, wb, settings);

            if (wbe == null) {
                System.out.println("wbe is null !os=" + os + ",wb" + wb);
            } else {
                System.out.println("wbe not null !os=" + os + ",wb" + wb);
            }
            WritableSheet sheet = wbe.getSheet(0);
            WritableCellFormat cellFormat = new WritableCellFormat();
            int i = 1;
            for (FinanceCodeDTO financeCodeDTO:financeCodeDTOS){
                if (financeCodeDTO.getOrderId() !=null)
                    sheet.addCell(new Label(0, i, financeCodeDTO.getOrderId() + "", cellFormat));
                if (financeCodeDTO.getBankDate()!=null)
                    sheet.addCell(new Label(1, i, sdfbankDatein.format(financeCodeDTO.getBankDate()), cellFormat));
                if (financeCodeDTO.getUser()!=null)
                    sheet.addCell(new Label(2, i, financeCodeDTO.getUser().getName(), cellFormat));
                if (financeCodeDTO.getBusiness()!=null)
                    sheet.addCell(new Label(3, i, financeCodeDTO.getBusiness() + "", cellFormat));
                if (financeCodeDTO.getComment() !=null)
                    sheet.addCell(new Label(4, i, financeCodeDTO.getComment() + "", cellFormat));
                if (financeCodeDTO.isIncome())
                    sheet.addCell(new Label(5, i, "收入", cellFormat)) ;
                else
                    sheet.addCell(new Label(5, i, "支出", cellFormat));
                sheet.addCell(new Label(6, i, financeCodeDTO.getMoney() + "", cellFormat));
                sheet.addCell(new Label(7, i, financeCodeDTO.getBalance() + "", cellFormat));
                if (financeCodeDTO.getAdviser()!=null){
                    sheet.addCell(new Label(8, i, financeCodeDTO.getAdviser().getRegionName() + "", cellFormat));
                    sheet.addCell(new Label(9, i, financeCodeDTO.getAdviser().getName() + "", cellFormat));
                }




                i++;
            }

            wbe.write();
            wbe.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @GetMapping(value = "/count")
    @ResponseBody
    public  Response count(@RequestParam(value = "bankDateStart",required = false) String bankDateStart,
                           @RequestParam(value = "bankDateEnd",required = false)String bankDateEnd){
        try {
            if (StringUtil.isNotEmpty(bankDateStart))
                bankDateStart = sdfbankDateout.format(sdfbankDatein.parse(bankDateStart));
            if (StringUtil.isNotEmpty(bankDateEnd))
                bankDateEnd = sdfbankDateout.format(sdfbankDatein.parse(bankDateEnd));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return  new Response(0,verifyService.count(bankDateStart,bankDateEnd+" 23:59:59"));
    }

    @GetMapping(value = "/list")
    @ResponseBody
    public  Response list(@RequestParam(value = "id",required = false)Integer id,
                          @RequestParam(value = "bankDateStart",required = false) String bankDateStart,
                          @RequestParam(value = "bankDateEnd",required = false)String bankDateEnd,
                          @RequestParam(value = "pageSize",required = true)Integer pageSize,
                          @RequestParam(value = "pageNum",required = true)Integer pageNumber){
        try {
            if (StringUtil.isNotEmpty(bankDateStart))
                bankDateStart = sdfbankDateout.format(sdfbankDatein.parse(bankDateStart));
            if (StringUtil.isNotEmpty(bankDateEnd))
                bankDateEnd = sdfbankDateout.format(sdfbankDatein.parse(bankDateEnd));
        } catch (ParseException e) {
            e.printStackTrace();
        }
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
            commissionOrderListDTO.setChecked(true);
            commissionOrderListDTO.setBankCheck("手工");
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
            visaDTO.setChecked(true);
            visaDTO.setBankCheck("手工");
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
