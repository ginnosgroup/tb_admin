package org.zhinanzhen.b.controller;

import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;
import org.springframework.dao.DataAccessException;
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
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.SimpleDateFormat;
import java.util.*;


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
            @RequestParam( value = "state",required =  false ) String state ,
            @RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
            HttpServletRequest request, HttpServletResponse response
    ){
        if (state==null){
            state = "";
        }
        if (pageNum < 0 )
            pageNum = 0 ;
        if (pageSize <= 0 )
            pageNum = 10 ;
        state = state.toUpperCase();
        List<InvoiceDTO> invoiceDTOList = invoiceService.selectInvoice(invoice_no,order_id,create_start,create_end,kind,branch,pageNum,pageSize,state);
        return  new Response(1,invoiceDTOList);
    }

    @RequestMapping(value = "/count" , method = RequestMethod.GET)
    @ResponseBody
    public Response count(
            @RequestParam( value = "invoice_no" , required = false ) String invoice_no,
            @RequestParam( value = "order_no" , required =  false ) String order_id,
            @RequestParam( value = "create_start" , required = false ) String create_start,
            @RequestParam( value = "create_end",required =  false ) String create_end ,
            @RequestParam( value = "kind",required =  false ) String kind ,
            @RequestParam( value = "branch",required =  false ) String branch ,
            @RequestParam( value = "state",required =  false ) String state
    ){
        if (state==null){
            state = "";
        }
        state = state.toUpperCase();
        int count = invoiceService.selectCount(invoice_no,order_id,create_start,create_end,kind,branch,state);
        return new Response(1,count);
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
            @RequestParam(value = "invoiceNo" ,required = true)String invoiceNo,
            @RequestParam(value = "invoiceIds" , required = true)String invoiceIds ,
            @RequestParam(value = "marketing" ,required = false) String marketing
    ){

        return invoiceService.selectInvoiceByNo(invoiceNo,invoiceIds,marketing);
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
    @RequestMapping(value = "/addServicefee" , method = RequestMethod.GET )
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

    //addservicefee导入数据，关联订单id (已弃用)
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


    //保存serviceFee invoice
    @RequestMapping(value = "/saveServiceFeeInvoice" , method = RequestMethod.POST )
    @ResponseBody
    public Response saveServiceFeeInvoice(@RequestBody Map paramMap){

       try {
            String invoiceDate = (String) paramMap.get("invoiceDate");
            String email = (String) paramMap.get("email");
            String company = (String) paramMap.get("company");
            String abn = (String) paramMap.get("abn");
            String address = (String) paramMap.get("address");
            String tel = (String) paramMap.get("tel");
            String invoiceNo = (String) paramMap.get("invoiceNo");
            String note = (String) paramMap.get("note");
            String accountname = (String) paramMap.get("accountname");
            String bsb = (String) paramMap.get("bsb");
            String accountno = (String) paramMap.get("accountno");
            String branch = (String) paramMap.get("branch");
            String [] idList = ((String)paramMap.get("idList")).split(",");
            List<InvoiceServiceFeeDescriptionDO> invoiceServiceFeeDescriptionDOList = (List<InvoiceServiceFeeDescriptionDO>) paramMap.get("descriptionList");
            int result = invoiceService.saveServiceFeeInvoice(invoiceDate, email, company, abn, address, tel, invoiceNo, note, accountname, bsb, accountno, branch, invoiceServiceFeeDescriptionDOList);
            int resultrela = invoiceService.relationVisaOrder(idList,invoiceNo);
            if (resultrela > 0) {
                return new Response(1, "success");
            }
            return new Response(1,"fail");
        }catch (DataAccessException ex){
            return new Response(1 ,"参数错误" );
        }

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

    @RequestMapping(value = "/selectBillTo" ,method =  RequestMethod.GET )
    @ResponseBody
    public Response billToList(){
        return  new Response(1,invoiceService.billToList());
    }

    @RequestMapping(value = "/addBillTo" ,method =  RequestMethod.POST )
    @ResponseBody
    public Response addBillTo(@RequestParam(value = "company" ,required =  true)String company ,
                              @RequestParam(value = "abn", required = true )String abn ,
                              @RequestParam(value = "address", required =  true) String address ){
        int result = invoiceService.addBillTo(company,abn,address);
        if( result > 0 )
            return  new Response(1,"success");
        return  new Response(1,"fail");
    }

    //添加schoolInvoice 返回数据
    @RequestMapping(value = "/addSchool" , method = RequestMethod.GET )
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

    //addschool导入数据，关联订单id (已弃用)
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

    //保存school invoice
    @RequestMapping( value = "/saveSchoolInvoice" ,method =  RequestMethod.POST)
    @ResponseBody
    public  Response saveSchoolInvoice(@RequestBody Map paramMap){
        try {
            if (paramMap.get("idList") == null)
                return  new Response(1,"idList is null");
            if (paramMap.get("invoiceNo") == null)
                return  new Response(1,"invoiceNo is null");
            String [] idList = ((String)paramMap.get("idList")).split(",");
            String invoiceNo = (String) paramMap.get("invoiceNo");
            int resultrela = invoiceService.relationCommissionOrder(idList,invoiceNo);

            int result = invoiceService.saveSchoolInvoice(paramMap);
            if ( result > 0 ){
                return  new Response(1 ,"success" );
            }
            return new Response(1 ,"fail" );
        }catch (DataAccessException ex){
            return new Response(1 ,"参数错误" );
        }



    }

}
