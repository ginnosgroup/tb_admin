package org.zhinanzhen.b.controller;

import com.ikasoa.core.utils.StringUtil;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.service.CommissionOrderService;
import org.zhinanzhen.b.service.InvoiceService;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.pojo.*;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.ListResponse;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
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
public class InvoiceController  extends BaseController {

    private Mapper mapper = new DozerBeanMapper();

    @Resource
    private InvoiceService invoiceService;

    @Resource
    CommissionOrderService commissionOrderService;

    @Resource
    ServiceOrderService serviceOrderService;

    private SimpleDateFormat sdfNo = new SimpleDateFormat("yyyyMM");

    private SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");

    //查询invoice
    @RequestMapping(value = "/selectInvoice", method = RequestMethod.GET)
    @ResponseBody
    public ListResponse selectAllInvoice(
            @RequestParam(value = "invoiceNo", required = false) String invoiceNo,
            @RequestParam(value = "orderId", required = false) String orderId,
            @RequestParam(value = "createStart", required = false) String createStart,
            @RequestParam(value = "createEnd", required = false) String createEnd,
            @RequestParam(value = "kind", required = false) String kind,
            @RequestParam(value = "branch", required = false) String branch,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
            HttpServletRequest request, HttpServletResponse response
    ) {
        if (state == null) {
            state = "";
        }
        if (pageNum < 0)
            pageNum = 0;
        if (pageSize <= 0)
            pageNum = 10;
        state = state.toUpperCase();
        List<InvoiceDTO> invoiceDTOList = invoiceService.selectInvoice(invoiceNo, orderId, createStart, createEnd, kind, branch, pageNum, pageSize, state);
        int count = invoiceService.selectCount(invoiceNo, orderId, createStart, createEnd, kind, branch, state);
        return new ListResponse(true,pageSize, count,invoiceDTOList,"ok");
    }

    @RequestMapping(value = "/count", method = RequestMethod.GET)
    @ResponseBody
    public Response count(
            @RequestParam(value = "invoiceNo", required = false) String invoiceNo,
            @RequestParam(value = "orderId", required = false) String orderId,
            @RequestParam(value = "createStart", required = false) String createStart,
            @RequestParam(value = "createEnd", required = false) String createEnd,
            @RequestParam(value = "kind", required = false) String kind,
            @RequestParam(value = "branch", required = false) String branch,
            @RequestParam(value = "state", required = false) String state
    ) {
        if (state == null) {
            state = "";
        }
        state = state.toUpperCase();
        int count = invoiceService.selectCount(invoiceNo, orderId, createStart, createEnd, kind, branch, state);
        return new Response(0, count);
    }

    @RequestMapping(value = "/updateState", method = RequestMethod.POST)
    @ResponseBody
    public Response updateState(@RequestParam(value = "invoiceNo") String invoiceNo,
                                @RequestParam(value = "invoiceIds") String invoiceIds) {

        int result = invoiceService.updateState(invoiceNo, invoiceIds);
        String fileName = null;
//        try {
//            fileName = ResourceUtils.getURL("classpath:").getPath();
            invoiceService.pdfPrint(invoiceNo, invoiceIds, fileName, true);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            return new Response(1, "error");
//        }
        if (result > 0) {
            return new Response(0, "success");
        }

        return new Response(1, "fail");
    }

    //查询一个invoice
    @RequestMapping(value = "/selectInvoiecByNo", method = RequestMethod.GET)
    @ResponseBody
    public Response selectInvoiecByNo(
            @RequestParam(value = "invoiceNo", required = true) String invoiceNo,
            @RequestParam(value = "invoiceIds", required = true) String invoiceIds,
            @RequestParam(value = "marketing", required = false) String marketing,
            HttpServletRequest req, HttpServletResponse res
    ) {
        super.setPostHeader(res);
        Response response = invoiceService.selectInvoiceByNo(invoiceNo, invoiceIds);

        return response;
    }

    //添加ServiceFee 中的查询 companyTile
    @RequestMapping(value = "/selectCompanySF", method = RequestMethod.GET)
    @ResponseBody
    public Response selectCompanySF() {
        List<InvoiceCompanyDO> companyDOS = invoiceService.selectCompany("SF");
        List<String> companyNameList = new ArrayList<>();
        if (companyDOS != null) {
            companyDOS.forEach(company -> {
                companyNameList.add(company.getName());
            });
        }

        return new Response(0, companyNameList);
    }

    //添加ServiceFee 中的查询 branch
    @RequestMapping(value = "/selectAddress", method = RequestMethod.GET)
    @ResponseBody
    public Response selectAddress() {
        List<InvoiceAddressDO> addressDOS = invoiceService.selectAddress();
        List<String> addressNameList = new ArrayList<>();
        if (addressDOS != null) {
            addressDOS.forEach(company -> {
                addressNameList.add(company.getBranch());
            });
        }

        return new Response(0, addressNameList);
    }

    //添加ServiceFeeInvoice
    @RequestMapping(value = "/addServicefee", method = RequestMethod.GET)
    @ResponseBody
    public Response addServiceFeeInvoice(
            @RequestParam(value = "branch", required = true) String branch,
            @RequestParam(value = "company", required = true) String company
    ) {
        String simpleBranch = "";
        InvoiceCompanyDTO invoiceCompanyDTO = invoiceService.addServiceFeeInvoice(branch, company);

//        HttpSession session = request.getSession();
//        List<BranchDO> branchDOS = (List<BranchDO>) session.getAttribute("branchDOS" + VERSION);
        List<InvoiceBranchDO> branchDOS = invoiceService.selectBranch();
        if (branchDOS != null) {
            for (InvoiceBranchDO branchDO : branchDOS) {
                if (branchDO.getBranch().equals(branch)) {
                    simpleBranch = branchDO.getSimple();
                }
            }
        }
        if (simpleBranch.equals(""))
            return new Response(1, "branch error!");
        if (invoiceCompanyDTO == null)
            return new Response(1, "company error!");
        String number = invoiceService.selectInvoiceBySimple(simpleBranch, "SF");
        Integer newNumber = 0;
        if (number != null) {
            newNumber = Integer.valueOf(number) + 1;
        }
        String invoiceNo = "V" + sdfNo.format(Calendar.getInstance().getTime()) + newNumber + simpleBranch;
        String invoiceDate = sdfDate.format(Calendar.getInstance().getTime());
        invoiceCompanyDTO.setInvoiceNo(invoiceNo);
        invoiceCompanyDTO.setInvoiceDate(invoiceDate);
        if (invoiceCompanyDTO != null) {
            return new Response(0, invoiceCompanyDTO);
        }

        return new Response(0, "没有数据！");
    }



    //保存serviceFee invoice
    @RequestMapping(value = "/saveServiceFeeInvoice", method = RequestMethod.POST)
    @ResponseBody
    public Response saveServiceFeeInvoice(@RequestBody Map paramMap) {
        try {
            String invoiceDate = (String) paramMap.get("invoiceDate");
            String email = (String) paramMap.get("email");
            String company = (String) paramMap.get("company");
            String abn = (String) paramMap.get("abn");
            String address = (String) paramMap.get("address");
            String tel = (String) paramMap.get("tel");
            String invoiceNo = (String) paramMap.get("invoiceNo");
            String billTo = (String) paramMap.get("billTo");
            String note = (String) paramMap.get("note");
            String accountname = (String) paramMap.get("accountname");
            String bsb = (String) paramMap.get("bsb");
            String accountno = (String) paramMap.get("accountno");
            String branch = (String) paramMap.get("branch");
            String[] idList = ((String) paramMap.get("idList")).split(",");
            List<InvoiceServiceFeeDescriptionDO> invoiceServiceFeeDescriptionDOList = (List<InvoiceServiceFeeDescriptionDO>) paramMap.get("descriptionList");

            if (invoiceService.selectInvoiceNo(invoiceNo, "b_invoice_servicefee"))
                return new Response(1, "invoiceNo repeat!");
            if (idList != null & !idList.equals("")) {
                int resultrela = invoiceService.relationVisaOrder(idList, invoiceNo, invoiceDate);
                if (resultrela > 0) {
                    return new Response(1, resultrela + "订单已经关联！");
                } else {
                    int result = invoiceService.saveServiceFeeInvoice(invoiceDate, email, company, abn, address, tel, invoiceNo,billTo, note, accountname, bsb, accountno, branch, invoiceServiceFeeDescriptionDOList);
                    resultrela = invoiceService.relationVisaOrder(idList, invoiceNo,invoiceDate);
                    if (result > 0) {
                        return new Response(0, "success");
                    }
                }
            }
            return new Response(1, "fail");
        } catch (DataAccessException ex) {
            System.out.println(ex);
            return new Response(1, "参数错误");
        } catch (Exception ex) {
            System.out.println(ex);
            return new Response(1, "系统错误，请联系管理员！");
        }

    }


    //添加School 中的查询 companyTile
    @RequestMapping(value = "/selectCompanySC", method = RequestMethod.GET)
    @ResponseBody
    public Response selectCompanySC() {
        List<InvoiceCompanyDO> companyDOS = invoiceService.selectCompany("SC");
        List<InvoiceCompanyIdNameDTO> invoiceCompanyIdNameDTOList = new ArrayList<>();
        companyDOS.forEach(company -> {
            invoiceCompanyIdNameDTOList.add(mapper.map(company, InvoiceCompanyIdNameDTO.class));
        });

        return new Response(0, invoiceCompanyIdNameDTOList);
    }

    /**
     * 查询billto公司list
     *
     * @return
     */
    @RequestMapping(value = "/selectBillTo", method = RequestMethod.GET)
    @ResponseBody
    public Response billToList(@RequestParam(value = "name",required = false)String name) {
        return new Response(0, invoiceService.billToList(name));
    }

    /**
     * 添加billto公司返回id
     *
     * @param company
     * @param abn
     * @param address
     * @return
     */
    @RequestMapping(value = "/addBillTo", method = RequestMethod.POST)
    @ResponseBody
    public Response addBillTo(@RequestParam(value = "company", required = true) String company,
                              @RequestParam(value = "abn", required = false) String abn,
                              @RequestParam(value = "address", required = true) String address) {
        int result = invoiceService.addBillTo(company, abn, address);
        if (result == -1) {
            return new Response(1, "this company has been added ！");
        }
        if (result > 0) {
            int billId = invoiceService.selectLastBillTo();
            return new Response(0, "success", billId);
        }


        return new Response(1, "fail");
    }

    //添加schoolInvoice 返回数据
    @RequestMapping(value = "/addSchool", method = RequestMethod.GET)
    @ResponseBody
    public Response addSchoolInvoice(
            @RequestParam(value = "branch", required = true) String branch,
            @RequestParam(value = "company", required = true) String company
    ) {
        String simpleBranch = "";
        InvoiceCompanyDTO invoiceCompanyDTO = invoiceService.addSchoolInvoice(branch, company);

        List<InvoiceBranchDO> branchDOS = invoiceService.selectBranch();
        if (branchDOS != null) {
            for (InvoiceBranchDO branchDO : branchDOS) {
                if (branchDO.getBranch().equals(branch)) {
                    simpleBranch = branchDO.getSimple();
                }
            }
        }
        if (simpleBranch.equals(""))
            return new Response(1, "branch error!");
        if (invoiceCompanyDTO == null)
            return new Response(1, "company error or NO data!");

		String number = invoiceService.selectInvoiceBySimple(simpleBranch.substring(simpleBranch.length() - 1), "SC"); // 2022-07-25 bugfix
        Integer newNumber = 0;
        if (number != null) {
            newNumber = Integer.valueOf(number) + 1;
        }
        String invoiceNo = "S" + sdfNo.format(Calendar.getInstance().getTime()) + newNumber + simpleBranch;
        String invoiceDate = sdfDate.format(Calendar.getInstance().getTime());

        invoiceCompanyDTO.setInvoiceNo(invoiceNo);
        invoiceCompanyDTO.setInvoiceDate(invoiceDate);
        if (invoiceCompanyDTO != null) {
            return new Response(0, invoiceCompanyDTO);
        }

        return new Response(0, "没有数据！");
    }


    //保存school invoice
    @RequestMapping(value = "/saveSchoolInvoice", method = RequestMethod.POST)
    @ResponseBody
    public Response saveSchoolInvoice(@RequestBody Map paramMap) {
        try {
            if (paramMap.get("idList") == null)
                return new Response(1, "idList is null");
            if (paramMap.get("invoiceNo") == null)
                return new Response(1, "invoiceNo is null");
            //String [] idstr = ((String)paramMap.get("idList")).split("#");
            String newInvoiceNo = "";
            String str = ((String) paramMap.get("idList"));
            String invoiceNo = (String) paramMap.get("invoiceNo");
            String flag = (String) paramMap.get("flag");
            String invoiceDate = (String) paramMap.get("invoiceDate");
            String commissionOrderTempId = (String) paramMap.get("commissionOrderTempId");

            String arr1 = str.substring(0, str.indexOf('#'));
            String arr2 = str.substring(str.indexOf('#') + 1);


            List<String> list = new ArrayList<String>();
            Collections.addAll(list, arr1.split(","));
            Collections.addAll(list, arr2.split(","));
            String[] idList = list.toArray(new String[list.size()]);


            if (invoiceService.selectInvoiceNo(invoiceNo, "b_invoice_school"))
                return new Response(1, "invoiceNo repeat!");
            if (idList != null & !idList.equals("")) {
                int resultrela = invoiceService.selectReaplceOrderId(idList, invoiceNo);
                if (resultrela > 0)
                    return new Response(1, resultrela + "  订单已经关联了！");
                else {
                    int result = 0;
                    List<InvoiceSchoolDescriptionDO> descriptionNormal = (List<InvoiceSchoolDescriptionDO>) paramMap.get("normal");
                    List<InvoiceSchoolDescriptionDO> descriptionMarketing = (List<InvoiceSchoolDescriptionDO>) paramMap.get("marketing");
                    if (descriptionNormal.size() == 0) {
                        paramMap.put("flag", "M");
                        result = invoiceService.saveSchoolInvoice(paramMap, descriptionMarketing);
                        resultrela = invoiceService.relationCommissionOrder(arr2.split(","), invoiceNo,invoiceDate);

                    } else if (descriptionMarketing.size() == 0) {
                        paramMap.put("flag", "N");
                        result = invoiceService.saveSchoolInvoice(paramMap, descriptionNormal);
                        resultrela = invoiceService.relationCommissionOrder(arr1.split(","), invoiceNo,invoiceDate);

                    } else if (descriptionNormal.size() != 0 | descriptionMarketing.size() != 0) {
                        Integer newNum = Integer.parseInt(invoiceNo.substring(6, invoiceNo.length() - 1)) + 1;
                        newInvoiceNo = invoiceNo.substring(0, 6) + newNum + invoiceNo.substring(invoiceNo.length() - 1, invoiceNo.length());
                        paramMap.put("flag", "N");
                        result = invoiceService.saveSchoolInvoice(paramMap, descriptionNormal);
                        paramMap.put("invoiceNo", newInvoiceNo);
                        paramMap.put("flag", "M");
                        invoiceService.saveSchoolInvoice(paramMap, descriptionMarketing);
                        resultrela = invoiceService.relationCommissionOrder(arr1.split(","), invoiceNo ,invoiceDate);
                        invoiceService.relationCommissionOrder(arr2.split(","), newInvoiceNo,invoiceDate);
                    }
                    if (StringUtil.isNotEmpty(commissionOrderTempId)){//提前扣拥会在没创建佣金订单的时候创建发票，先把invoiceNo写在临时表上
                        CommissionOrderTempDTO comtemp = commissionOrderService.getCommissionOrderTempById(StringUtil.toInt(commissionOrderTempId));
                        if (comtemp != null)
                            comtemp.setInvoiceNumber(invoiceNo);
                        commissionOrderService.updateCommissionOrderTemp(comtemp);
                    }
                    if (result > 0) {
                        return new Response(0, "success",
                                invoiceNo + (StringUtil.isEmpty(newInvoiceNo) ? "" : "," + newInvoiceNo));
                    }
                }
            }
            return new Response(1, "fail");
        } catch (DataAccessException ex) {
            ex.printStackTrace();
            return new Response(1, "参数错误",ex.getMessage());
        } catch (ServiceException ex) {
            ex.printStackTrace();
            return new Response(1,ex.getMessage());
        }catch (Exception ex) {
            ex.printStackTrace();
            return new Response(1, "系统错误，请联系管理员！",ex.getMessage());
        }
    }


    //打印pdf
    @RequestMapping(value = "/pdfPrint", method = RequestMethod.GET)
    @ResponseBody
    public Response pdfPrint(
            @RequestParam(value = "invoiceNo", required = true) String invoiceNo,
            @RequestParam(value = "invoiceIds", required = true) String invoiceIds,
            @RequestParam(value = "marketing", required = false) String marketing,
            HttpServletRequest req, HttpServletResponse resp) throws FileNotFoundException, ServiceException {

        super.setGetHeader(resp);
//        String fileName = ResourceUtils.getURL("classpath:").getPath();
        String fileName = null;
        Response response = invoiceService.pdfPrint(invoiceNo, invoiceIds, fileName, false);
        if (StringUtil.isNotEmpty(response.getMessage()))
            response.setMessage("/statics" + response.getMessage());
        return response;
    }

    //更改invoice
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public Response update(@RequestBody Map paramMap) {
        try {
            if (StringUtil.isNotEmpty((String) paramMap.get("billto_id"))) {
                //System.out.println("SC");
                return new Response(0, invoiceService.updateSCInvoice(paramMap));

            } else if (StringUtil.isEmpty((String) paramMap.get("billto_id"))) {
                //System.out.println("SF");
                return new Response(0, invoiceService.updateSFInvoice(paramMap));
            }
            return new Response(0, "fail");
        }catch (ServiceException e){
            return new Response(1, e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return new Response(1, "系统错误！");
        }
    }


}
