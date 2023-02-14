package org.zhinanzhen.b.controller;

import com.ikasoa.core.utils.ObjectUtil;
import com.ikasoa.core.utils.StringUtil;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WriteException;

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
import org.zhinanzhen.tb.controller.ListResponse;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.AdviserService;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.pojo.AdviserDTO;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    private  SimpleDateFormat  sdfbankDatein = new SimpleDateFormat("dd/MM/yyyy");

    private  SimpleDateFormat  sdfbankDateout = new SimpleDateFormat("yyyy-MM-dd");

    @RequestMapping(value = "/uploadexcel",method = RequestMethod.POST)
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public Response uploadExcel(@RequestParam("file") MultipartFile file,
                                @RequestParam("regionId")Integer regionId) throws Exception {
        String fileName = file.getOriginalFilename();
        List<FinanceCodeDO> financeCodeDOS = verifyService.excelToList(file.getInputStream(), fileName);
        for (FinanceCodeDO financeCodeDO : financeCodeDOS) {
            String orderId = financeCodeDO.getOrderId();
System.out.println("[对账debug] orderId: " + orderId);
                if (StringUtil.isNotBlank(orderId)  && orderId.substring(0,2).equalsIgnoreCase("CS")) {
                    CommissionOrderListDTO commissionOrderListDTO = commissionOrderService.getCommissionOrderById(Integer.parseInt(orderId.substring(2)));
                    if (commissionOrderListDTO!=null){
                        //financeCodeDO.setAdviser(commissionOrderListDTO.getAdviser().getName());
                        financeCodeDO.setAdviserId(commissionOrderListDTO.getAdviserId());
                        //financeCodeDO.setName(commissionOrderListDTO.getUser().getName());
                        financeCodeDO.setUserId(commissionOrderListDTO.getUserId());
                        financeCodeDO.setAmount(commissionOrderListDTO.getAmount());
                        if (commissionOrderListDTO.getSchool() != null)
                            financeCodeDO.setBusiness("留学-"+commissionOrderListDTO.getSchool().getName());
                        else if (commissionOrderListDTO.getSchoolInstitutionListDTO() != null)
                            financeCodeDO.setBusiness("留学-"+commissionOrderListDTO.getSchoolInstitutionListDTO().getInstitutionName());
                        //if (commissionOrderListDTO.getBankDate()==null)
                        commissionOrderListDTO.setBankDate(financeCodeDO.getBankDate());
                        if (commissionOrderListDTO.getAmount() == financeCodeDO.getMoney())
                            commissionOrderListDTO.setChecked(true);
                        if (StringUtil.isEmpty(commissionOrderListDTO.getBankCheck()))
                            commissionOrderListDTO.setBankCheck("手工");
                        commissionOrderService.updateCommissionOrder(commissionOrderListDTO);
                    }

                }
                if (StringUtil.isNotBlank(orderId) && orderId.substring(0,2).equalsIgnoreCase("CV") ){
                    VisaDTO visaDTO =  visaService.getVisaById(Integer.parseInt(orderId.substring(2)));
                    if (visaDTO!=null){
                        financeCodeDO.setAdviserId(visaDTO.getAdviserId());
                        financeCodeDO.setUserId(visaDTO.getUserId());
                        financeCodeDO.setAmount(visaDTO.getAmount());
                        ServiceOrderDTO serviceOrderDTO = serviceOrderService.getServiceOrderById(visaDTO.getServiceOrderId());
                        financeCodeDO.setBusiness(serviceOrderDTO.getService().getName()+"-"+serviceOrderDTO.getService().getCode());
                        //if (visaDTO.getBankDate()==null)
                        visaDTO.setBankDate(financeCodeDO.getBankDate());
                        if (visaDTO.getAmount() == financeCodeDO.getMoney())
                            visaDTO.setChecked(true);
                        if (StringUtil.isEmpty(visaDTO.getBankCheck()))
                            visaDTO.setBankCheck("手工");
                        visaService.updateVisa(visaDTO);
                    }
                }
            financeCodeDO.setRegionId(regionId);
        }

        for (Iterator iterator = financeCodeDOS.listIterator(); iterator.hasNext();){
            FinanceCodeDO financeCodeDO = (FinanceCodeDO) iterator.next();
            FinanceCodeDTO financeCodeDTO =  verifyService.financeDTOByCode(financeCodeDO.getCode());
            if (financeCodeDTO != null){
                if (StringUtil.isNotEmpty(financeCodeDO.getOrderId()) && StringUtil.isEmpty(financeCodeDTO.getOrderId())){
                    financeCodeDO.setId(financeCodeDTO.getId());
                    verifyService.update(financeCodeDO);
                }
                iterator.remove();
            }
        }
        if (verifyService.add(financeCodeDOS) > 0)
            return new Response(0,"success");
        return new Response(1,"fail");

    }

    @GetMapping(value = "/down")
    @ResponseBody
    public  void down(@RequestParam(value = "bankDateStart",required = false) String bankDateStart,
                      @RequestParam(value = "bankDateEnd",required = false)String bankDateEnd,
                      @RequestParam(value = "regionId",required = false) Integer regionId,
                      HttpServletRequest request, HttpServletResponse response){

        try {
            if (StringUtil.isNotEmpty(bankDateStart))
                bankDateStart = sdfbankDateout.format(sdfbankDateout.parse(bankDateStart));
            if (StringUtil.isNotEmpty(bankDateEnd))
                bankDateEnd = sdfbankDateout.format(sdfbankDateout.parse(bankDateEnd));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        List<FinanceCodeDTO> financeCodeDTOS = verifyService.list(bankDateStart,bankDateEnd+" 23:59:59", regionId, 9999,0);

        jxl.Workbook wb = null;
        InputStream is = null;
        OutputStream os = null;
        jxl.write.WritableWorkbook wbe = null;
        try {
            response.reset();// 清空输出流
            String tableName = "bankstatement";
            response.setHeader("Content-disposition",
                    "attachment; filename=" + new String(tableName.getBytes("GB2312"), "8859_1") + ".xls");
            response.setContentType("application/msexcel");

            os = response.getOutputStream();
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
            wbe = Workbook.createWorkbook(os, wb, settings);

            if (wbe == null) {
                System.out.println("wbe is null !os=" + os + ",wb" + wb);
            } else {
                System.out.println("wbe not null !os=" + os + ",wb" + wb);
            }
            WritableSheet sheet = wbe.getSheet(0);
            WritableCellFormat cellFormat = new WritableCellFormat();
            int i = 1;
            for (FinanceCodeDTO financeCodeDTO:financeCodeDTOS){
                if (financeCodeDTO.getOrderId() !=null){
                    sheet.addCell(new Label(0, i, financeCodeDTO.getOrderId() + "", cellFormat));
                    if (financeCodeDTO.getOrderId().startsWith("CV")){
                        VisaDTO visaDTO =  visaService.getVisaById(Integer.parseInt(financeCodeDTO.getOrderId().substring(2)));
                        if ( visaDTO != null ){
                            sheet.addCell(new Label(8, i, visaDTO.getPerAmount() + "", cellFormat));
                            sheet.addCell(new Label(9, i, visaDTO.getAmount() + "", cellFormat));
                        }
                    }
                    if (financeCodeDTO.getOrderId().startsWith("CS")){
                        CommissionOrderListDTO commissionOrderListDTO = commissionOrderService.getCommissionOrderById(Integer.parseInt(financeCodeDTO.getOrderId().substring(2)));
                        if (commissionOrderListDTO != null){
                            sheet.addCell(new Label(8, i, commissionOrderListDTO.getPerAmount() + "", cellFormat));
                            sheet.addCell(new Label(9, i, commissionOrderListDTO.getAmount() + "", cellFormat));
                        }
                    }
                }
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
                    sheet.addCell(new Label(10, i, financeCodeDTO.getAdviser().getRegionName() + "", cellFormat));
                    sheet.addCell(new Label(11, i, financeCodeDTO.getAdviser().getName() + "", cellFormat));
                }




                i++;
            }

            wbe.write();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
			try {
				if (wbe != null)
					wbe.close();
			} catch (WriteException|IOException e) {
				e.printStackTrace();
			}
            try {
                if (is != null)
                    is.close();
                System.out.println("is is close");
            } catch (IOException e) {
                System.out.println("is is close 出现 异常:");
                e.printStackTrace();
            }
            try {
                if (os != null)
                    os.close();
                System.out.println("os is close");
            } catch (IOException e) {
                System.out.println("os is close 出现 异常:");
                e.printStackTrace();
            }
            if (wb != null)
                wb.close();
            System.out.println("wb is close");
        }

    }

    @GetMapping(value = "/count")
    @ResponseBody
    public  Response count(@RequestParam(value = "bankDateStart",required = false) String bankDateStart,
                           @RequestParam(value = "bankDateEnd",required = false)String bankDateEnd,
                           @RequestParam(value = "regionId",required = false) Integer regionId){
        try {
            if (StringUtil.isNotEmpty(bankDateStart))
                bankDateStart = sdfbankDateout.format(sdfbankDateout.parse(bankDateStart));
            if (StringUtil.isNotEmpty(bankDateEnd))
                bankDateEnd = sdfbankDateout.format(sdfbankDateout.parse(bankDateEnd));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return  new Response(0,verifyService.count(bankDateStart,bankDateEnd+" 23:59:59",regionId));
    }

    @GetMapping(value = "/list")
    @ResponseBody
    public ListResponse list(@RequestParam(value = "id",required = false)Integer id,
                             @RequestParam(value = "bankDateStart",required = false) String bankDateStart,
                             @RequestParam(value = "bankDateEnd",required = false)String bankDateEnd,
                             @RequestParam(value = "regionId",required = false)Integer regionId,
                             @RequestParam(value = "pageSize",required = true)Integer pageSize,
                             @RequestParam(value = "pageNum",required = true)Integer pageNumber){
        try {
            if (StringUtil.isNotEmpty(bankDateStart))
                bankDateStart = sdfbankDateout.format(sdfbankDateout.parse(bankDateStart));
            if (StringUtil.isNotEmpty(bankDateEnd))
                bankDateEnd = sdfbankDateout.format(sdfbankDateout.parse(bankDateEnd));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (ObjectUtil.isNotNull(id) && id > 0 ){
            List<FinanceCodeDTO> list = new ArrayList<>();
            FinanceCodeDTO financeCodeDTO;
            if ( (financeCodeDTO = verifyService.financeCodeById(id)) != null){
                list.add(financeCodeDTO);
            }
            return new ListResponse(true,pageSize,list.size(),list,"");
        }
        int total = verifyService.count(bankDateStart,bankDateEnd+" 23:59:59",regionId);
        return  new ListResponse(true,pageSize,total,verifyService.list(bankDateStart,bankDateEnd+" 23:59:59",regionId,pageSize,pageNumber),"");
    }

    @PostMapping(value = "/update")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public  Response update(@RequestParam(value = "orderId",required = true)String orderId,
                            @RequestParam(value = "id") Integer id) throws Exception {

        if (id <= 0 )
            throw new Exception("id  error !");
        FinanceCodeDO financeCodeDO = verifyService.financeCodeById(id);
        if (financeCodeDO==null)
            return  new Response(1,"id error");
        if (StringUtil.isEmpty(orderId)){
            if (verifyService.deleteOrderId(financeCodeDO))
                return new Response(0,"success");
            return new Response(1,"删除失败!");
        }
        String order = "";
        Integer number = 0;
        if (StringUtil.isNotEmpty(orderId)){
            try {
                order = orderId.substring(0, 2);
                number = Integer.parseInt(orderId.substring(2));
            } catch (Exception e) {
                throw new Exception("orderId error");
            }
            if (number <= 0)
                throw new Exception("orderId error");
            if (!orderId.equalsIgnoreCase(financeCodeDO.getOrderId())){
                verifyService.deleteOrderId(financeCodeDO);
            }
        }
        financeCodeDO.setOrderId(orderId);
        if (order.equalsIgnoreCase("CS")){
            CommissionOrderListDTO commissionOrderListDTO = commissionOrderService.getCommissionOrderById(number);
            if (commissionOrderListDTO == null)
                throw new Exception(" 没有此佣金订单:" + orderId +"!");
            financeCodeDO.setAdviserId(commissionOrderListDTO.getAdviserId());
            financeCodeDO.setUserId(commissionOrderListDTO.getUserId());
            financeCodeDO.setAmount(commissionOrderListDTO.getAmount());
            if (commissionOrderListDTO.getSchool() != null)
                financeCodeDO.setBusiness("留学-"+commissionOrderListDTO.getSchool().getName());
            else if (commissionOrderListDTO.getSchoolInstitutionListDTO() != null)
                financeCodeDO.setBusiness("留学-"+commissionOrderListDTO.getSchoolInstitutionListDTO().getInstitutionName());
            commissionOrderListDTO.setBankDate(financeCodeDO.getBankDate());
            if (financeCodeDO.getMoney() == commissionOrderListDTO.getAmount())
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
            financeCodeDO.setAmount(visaDTO.getAmount());
            ServiceOrderDTO serviceOrderDTO = serviceOrderService.getServiceOrderById(visaDTO.getServiceOrderId());
            financeCodeDO.setBusiness(serviceOrderDTO.getService().getName()+"-"+serviceOrderDTO.getService().getCode());
            visaDTO.setBankDate(financeCodeDO.getBankDate());
            if (visaDTO.getAmount() == financeCodeDO.getMoney())
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
