package org.zhinanzhen.b.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.service.InvoiceService;
import org.zhinanzhen.b.service.pojo.InvoiceCompanyDTO;
import org.zhinanzhen.b.service.pojo.InvoiceDTO;
import org.zhinanzhen.tb.controller.Response;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * Created with IntelliJ IDEA.
 * User: bsz
 * Date: 2020/10/14 10:04
 * Description:
 * Version: V1.0
 */
@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/invoice")
public class InvoiceController {

    @Resource
    private InvoiceService invoiceService;

    private SimpleDateFormat sdfNo = new SimpleDateFormat("yyyyMM");

    private SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");

    //查询invoice
    @RequestMapping(value = "/selectInvoice" , method = RequestMethod.GET)
    @ResponseBody
    public Response selectAllInvoice(
            @RequestParam( value = "invoice_no" , required = false ) String invoice_no,
            @RequestParam( value = "order_no" , required =  false ) String order_id,
            @RequestParam( value = "create_start" , required = false ) String create_start,
            @RequestParam( value = "create_end",required =  false ) String create_end ,
            @RequestParam( value = "kind",required =  false ) String kind ,
            @RequestParam( value = "branch",required =  false ) String branch ,
            @RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
            HttpServletRequest request, HttpServletResponse response
    ){
        List<InvoiceDTO> invoiceDTOList = invoiceService.selectInvoice(invoice_no,order_id,create_start,create_end,kind,branch,pageNum,pageSize);
        //List<InvoiceBranchDO> branchDOS = invoiceService.selectBranch();
        //HttpSession session =  request.getSession();
        //session.setAttribute("branchDOS",branchDOS);
        return  new Response(1,invoiceDTOList);
    }


    @RequestMapping(value = "/updateState",method = RequestMethod.POST)
    @ResponseBody
    public Response updateState(@RequestParam(value = "invoiceNo")String invoiceNo,
                                @RequestParam(value = "invoiceIds")String invoiceIds ){

        int result = invoiceService.updateState(invoiceNo,invoiceIds);
        if (result>0){
            return new Response(1,"更改成功");
        }

        return new Response(1,"更改失败");
    }

    //查询一个invoice
    @RequestMapping(value = "/selectInvoiecByNo",method = RequestMethod.GET)
    @ResponseBody
    public Response selectInvoiecByNo(
            @RequestParam(value = "invoiceNo")String invoiceNo,
            @RequestParam(value = "invoiceIds")String invoiceIds
    ){

        return invoiceService.selectInvoiceByNo(invoiceNo,invoiceIds);
    }

    //添加ServiceFee 中的查询 companyTile
    @RequestMapping(value = "/selectCompanySF" , method = RequestMethod.GET )
    @ResponseBody
    public Response selectCompanySF(){
        List<InvoiceCompanyDO>  companyDOS = invoiceService.selectCompany("SF");
        List<String> companyNameList = new ArrayList<>();
        if(companyDOS != null ){
            companyDOS.forEach( company ->{
                companyNameList.add(company.getName());
            });
        }

        return  new Response(1,companyNameList);
    }

    //添加ServiceFee 中的查询 branch
    @RequestMapping(value = "/selectAddress" , method = RequestMethod.GET )
    @ResponseBody
    public Response selectAddress(){
        List<InvoiceAddressDO>  addressDOS = invoiceService.selectAddress();
        List<String> addressNameList = new ArrayList<>();
        if(addressDOS != null ){
            addressDOS.forEach( company ->{
                addressNameList.add(company.getBranch());
            });
        }

        return  new Response(1,addressNameList);
    }

    //添加ServiceFeeInvoice
    @RequestMapping(value = "/addServicefee" , method = RequestMethod.POST )
    @ResponseBody
    public Response addServiceFeeInvoice(
            @RequestParam(value = "branch",required = true) String branch,
            @RequestParam(value = "company",required = true) String company
    ){
         String simpleBranch = "";
         InvoiceCompanyDTO invoiceCompanyDTO = invoiceService.addServiceFeeInvoice(branch,company);

         //HttpSession session = request.getSession();
         //List<BranchDO> branchDOS = (List<BranchDO>) session.getAttribute("branchDOS");
        List<InvoiceBranchDO> branchDOS = invoiceService.selectBranch();
         if(branchDOS != null){
             for(InvoiceBranchDO branchDO:branchDOS){
                 if(branchDO.getBranch().equals(branch)){
                     simpleBranch = branchDO.getSimple();
                 }
             }
         }
         if(simpleBranch.equals(""))
             return new Response(1,"branch error!");
        if(invoiceCompanyDTO==null)
            return new Response(1,"company error!");
         String number =  invoiceService.selectInvoiceBySimple(simpleBranch,"SF");
         Integer newNumber = 0;
         if(number!=null){
             newNumber = Integer.valueOf(number) + 1 ;
         }
         String invoiceNo = sdfNo.format(Calendar.getInstance().getTime())+newNumber+simpleBranch;
         String invoiceDate = sdfDate.format(Calendar.getInstance().getTime());
         invoiceCompanyDTO.setInvoiceNo(invoiceNo);
         invoiceCompanyDTO.setInvoiceDate(invoiceDate);
         if(invoiceCompanyDTO != null){
             return new Response(1,invoiceCompanyDTO);
         }

        return  new Response(1,"没有数据！");
    }

    //addservicefee导入数据，关联订单id
    @RequestMapping(value = "/relationVisaOrder" , method = RequestMethod.POST )
    @ResponseBody
    public Response relationVisaOrder(@RequestParam (value = "idList" ,required = true) String [] idList ,
                                  @RequestParam(value = "invoiceNo" ,required = true) String invoiceNo){
        int result = invoiceService.relationVisaOrder(idList,invoiceNo);
        if( result > 0 ){
            return new Response(1,"成功");
        }
        return   new Response(0,"失败");
    }

    //!!!!!!!!!!!!!
    //保存serviceFee invoice
    @RequestMapping(value = "/saveServiceFeeInvoice" , method = RequestMethod.POST )
    @ResponseBody
    public Response saveServiceFeeInvoice(InvoiceServiceFeeDO invoiceServiceFeeDO){
        System.out.println(invoiceServiceFeeDO.toString());
        return null;
    }




    //添加School 中的查询 companyTile
    @RequestMapping(value = "/selectCompanySC" , method = RequestMethod.GET )
    @ResponseBody
    public Response selectCompanySC(){
        List<InvoiceCompanyDO>  companyDOS = invoiceService.selectCompany("SC");
        List<String> companyNameList = new ArrayList<>();
        if(companyDOS != null ){
            companyDOS.forEach( company ->{
                companyNameList.add(company.getName());
            });
        }

        return  new Response(1,companyNameList);
    }

    //添加schoolInvoice
    @RequestMapping(value = "/addSchool" , method = RequestMethod.POST )
    @ResponseBody
    public Response addSchoolInvoice(
            @RequestParam(value = "branch",required = true) String branch,
            @RequestParam(value = "company",required = true) String company
    ){
        String simpleBranch = "";
        InvoiceCompanyDTO invoiceCompanyDTO = invoiceService.addSchoolInvoice(branch,company);

        List<InvoiceBranchDO> branchDOS = invoiceService.selectBranch();
        if(branchDOS != null){
            for(InvoiceBranchDO branchDO:branchDOS){
                if(branchDO.getBranch().equals(branch)){
                    simpleBranch = branchDO.getSimple();
                }
            }
        }
        if(simpleBranch.equals(""))
            return new Response(1,"branch error!");
        if(invoiceCompanyDTO==null)
            return new Response(1,"company error or NO data!");

        String number =  invoiceService.selectInvoiceBySimple(simpleBranch,"SC");
        Integer newNumber = 0;
        if(number!=null){
            newNumber = Integer.valueOf(number) + 1 ;
        }
        String invoiceNo = sdfNo.format(Calendar.getInstance().getTime())+newNumber+simpleBranch;
        String invoiceDate = sdfDate.format(Calendar.getInstance().getTime());
        invoiceCompanyDTO.setInvoiceNo(invoiceNo);
        invoiceCompanyDTO.setInvoiceDate(invoiceDate);
        if(invoiceCompanyDTO != null){
            return new Response(1,invoiceCompanyDTO);
        }

        return  new Response(1,"没有数据！");
    }

    //addschool导入数据，关联订单id
    @RequestMapping(value = "/relationCommissionOrder" , method = RequestMethod.POST )
    @ResponseBody
    public Response relationCommissionOrder(@RequestParam (value = "idList" ,required = true) String [] idList ,
                                      @RequestParam(value = "invoiceNo" ,required = true) String invoiceNo){
        int result = invoiceService.relationCommissionOrder(idList,invoiceNo);
        if( result > 0 ){
            return new Response(1,"成功");
        }
        return   new Response(0,"失败");
    }

}