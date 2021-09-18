package org.zhinanzhen.b.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.ikasoa.core.ErrorCodeEnum;
import com.ikasoa.core.utils.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhinanzhen.b.controller.BaseCommissionOrderController;
import org.zhinanzhen.b.dao.CommissionOrderDAO;
import org.zhinanzhen.b.dao.InvoiceDAO;
import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.service.InvoiceService;
import org.zhinanzhen.b.service.pojo.InvoiceCompanyDTO;
import org.zhinanzhen.b.service.pojo.InvoiceDTO;
import org.zhinanzhen.b.service.pojo.InvoiceSchoolDTO;
import org.zhinanzhen.b.service.pojo.InvoiceServiceFeeDTO;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;
import org.zhinanzhen.tb.utils.PrintPdfUtil;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: bsz
 * Date: 2020/10/14 11:54
 * Description:
 * Version: V1.0
 */
@Service
public class InvoiceServiceImpl extends BaseService implements InvoiceService {

    @Resource
    private InvoiceDAO invoiceDAO;

    @Resource
    CommissionOrderDAO commissionOrderDAO;

    private static  SimpleDateFormat sdfdob = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
    private static  SimpleDateFormat sdfolddob = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private static  SimpleDateFormat ymdsdf = new SimpleDateFormat("yyyy-MM-dd");

    private static final String STR1900 = "1900-00-00" ;

    //查询invoice
    @Override
    public List<InvoiceDTO> selectInvoice(String invoice_no, String order_id, String create_start, String create_end, String kind, String branch, int pageNum, int pageSize,String state) {

        if(order_id == null | order_id == "" ) {
            List<InvoiceDTO> invoiceDTOList = invoiceDAO.selectScoolInvoice(invoice_no, order_id, create_start, create_end, branch,state, pageNum  * pageSize, pageSize);
            List<InvoiceDTO> invoiceServiceFeeDTOList = invoiceDAO.selectServiceFeeInvoice(invoice_no, order_id, create_start, create_end, branch, state,pageNum  * pageSize, pageSize);
            invoiceDTOList.forEach(invoice -> {
                invoice.setIds("SC" + invoice.getId());
            });
            invoiceServiceFeeDTOList.forEach(invoice -> {
                invoice.setIds("SF" + invoice.getId());
            });
            if (kind == null) {
                invoiceDTOList = invoiceDAO.selectScoolInvoice(invoice_no, order_id, create_start, create_end, branch, state,pageNum  * pageSize / 2, pageSize / 2);
                invoiceServiceFeeDTOList = invoiceDAO.selectServiceFeeInvoice(invoice_no, order_id, create_start, create_end, branch, state ,pageNum * pageSize / 2, pageSize / 2);
                invoiceDTOList.forEach(invoice -> {
                    invoice.setIds("SC" + invoice.getId());
                });
                invoiceServiceFeeDTOList.forEach(invoice -> {
                    invoice.setIds("SF" + invoice.getId());
                });
                invoiceDTOList.addAll(invoiceServiceFeeDTOList);
                return invoiceDTOList;
            }
            if (kind.equals("SC")) {
                return invoiceDTOList;
            }
            if (kind.equals("SF")) {
                return invoiceServiceFeeDTOList;
            }
        }else {
            List<InvoiceDTO> list = new ArrayList<>();
            if (kind == null) {
                InvoiceDTO invoiceSC = invoiceDAO.selectCommissionOrder(order_id);
                if(invoiceSC != null){
                    invoiceSC.setIds("SC" + invoiceSC.getId());
                    list.add(invoiceSC);
                }
                InvoiceDTO invoiceSF = invoiceDAO.selectVisaOrder(order_id);
                if(invoiceSF != null){
                    invoiceSF.setIds("SF" + invoiceSF.getId());
                    list.add(invoiceSF);
                }
                return  list;
            }
            if (kind.equals("SC")) {
                InvoiceDTO invoiceSC = invoiceDAO.selectCommissionOrder(order_id);
                if(invoiceSC != null)
                invoiceSC.setIds("SC" + invoiceSC.getId());
                list.add(invoiceSC);
                return list;
            }
            if (kind.equals("SF")) {
                InvoiceDTO invoiceSF = invoiceDAO.selectVisaOrder(order_id);
                if(invoiceSF != null)
                invoiceSF.setIds("SF" + invoiceSF.getId());
                list.add(invoiceSF);
                return list;
            }
        }
        return null;
    }

    @Override
    public int selectCount(String invoice_no, String order_id, String create_start, String create_end, String kind, String branch, String state) {

        if(order_id == null | order_id == "" ) {
            int sfcount = invoiceDAO.selectSFCount(invoice_no,order_id,create_start,create_end,kind,branch ,state);
            int sccount = invoiceDAO.selectSCCount(invoice_no,order_id,create_start,create_end,kind,branch ,state);
            if(kind == null){
                return sccount+sfcount;
            }
            if (kind .equals("SF"))
                return sfcount ;
            if (kind .equals("SC"))
                return sccount ;
        }else {
            int sfcount = invoiceDAO.selectVisaOrderCount(order_id);
            int sccount = invoiceDAO.selectCommissionOrderCount(order_id);
            if(kind == null){
                return sccount+sfcount;
            }
            if (kind .equals("SF"))
                return sfcount ;
            if (kind .equals("SC"))
                return sccount ;

        }


        return 0;
    }

    //更改invoice状态
    @Override
    @Transactional
    public int updateState(String invoiceNo, String invoiceIds) {
        String flag = invoiceIds.substring(0,2);
        if (flag.equalsIgnoreCase("SF")){
            invoiceDAO.updateVisaInvoiceNumberNull(invoiceNo);
            return  invoiceDAO.updateSFState(invoiceNo);

        }if (flag.equalsIgnoreCase("SC")){
            invoiceDAO.removeInvoiceNumberInCommissionOrder(invoiceNo);
            return invoiceDAO.updateSCState(invoiceNo);
        }
        return 0;
    }

    @Override
    public List<InvoiceCompanyDO> selectCompany(String flag) {

        List<InvoiceCompanyDO>  companyDOS = invoiceDAO.selectCompany(flag);

        return companyDOS;
    }

    @Override
    public List<InvoiceAddressDO> selectAddress() {
        return invoiceDAO.selectAddress();
    }

    @Override
    public List<InvoiceBranchDO> selectBranch() {
        return invoiceDAO.selectBranch();
    }

    //增加servicefee tax invoice
    @Override
    public InvoiceCompanyDTO addServiceFeeInvoice(String branch, String company) {

        InvoiceCompanyDTO  invoiceCompanyDTO =  invoiceDAO.selectCompanyByName(company,"SF");
        if(invoiceCompanyDTO != null) {
            if (invoiceCompanyDTO.getSimple().equals("CEM")) {
                InvoiceAddressDO invoiceAddressDO = invoiceDAO.selectAddressByBranch(branch);
                if (invoiceCompanyDTO != null & invoiceAddressDO != null) {
                    invoiceCompanyDTO.setAddress(invoiceAddressDO.getAddress());
                    invoiceCompanyDTO.setAccount(invoiceAddressDO.getAccount());
                    invoiceCompanyDTO.setBsb(invoiceAddressDO.getBsb());
                    return invoiceCompanyDTO;
                }
            }
            if ("CS,CIS".contains(invoiceCompanyDTO.getSimple())) {
                InvoiceAddressDO invoiceAddressDO = invoiceDAO.selectAddressByBranch("SYD");
                if (invoiceCompanyDTO != null & invoiceAddressDO != null) {
                    invoiceCompanyDTO.setAddress(invoiceAddressDO.getAddress());
                }
                return invoiceCompanyDTO;
            }
        }
        return null;
    }

    @Override
    public String selectInvoiceBySimple(String simpleBranch ,String flag) {
        List<String> invoiceNos = invoiceDAO.selectInvoiceBySimple(simpleBranch,flag);
        List<String> _invoiceNos = new ArrayList<>();
        invoiceNos.forEach(invoiceNo -> {
            if (invoiceNo.startsWith("S") || invoiceNo.startsWith("V"))
                _invoiceNos.add(invoiceNo.substring(1));
            else
                _invoiceNos.add(invoiceNo);
        });
        List<Integer> invoiceNumber = new ArrayList<>();
        if (_invoiceNos != null & _invoiceNos.size() != 0){
            _invoiceNos.forEach(invoiceNo->{
                invoiceNumber.add(Integer.parseInt(invoiceNo.substring(0,invoiceNo.length()-1)));
            });
            String invoiceNumberMax = Collections.max(invoiceNumber).toString();
            String number = invoiceNumberMax.substring(6,invoiceNumberMax.length());
            return number;
        }
        return "0";
    }

    //添加留学invoice
    @Override
    public InvoiceCompanyDTO addSchoolInvoice(String branch, String company) {
        InvoiceCompanyDTO  invoiceCompanyDTO = null;
        InvoiceAddressDO invoiceAddressDO = null;
        invoiceCompanyDTO = invoiceDAO.selectCompanyByName(company,"SC");
        if (invoiceCompanyDTO!=null){
            if (invoiceCompanyDTO.getSimple().equals("CEM")){
                invoiceAddressDO = invoiceDAO.selectAddressByBranch(branch);
                if(invoiceAddressDO!= null)
                    invoiceCompanyDTO .setAddress(invoiceAddressDO.getAddress());
            }
            if (invoiceCompanyDTO.getSimple().equals("IES") | invoiceCompanyDTO.getSimple().equals("CS")) {
                invoiceAddressDO = invoiceDAO.selectAddressByBranch("SYD");
                if(invoiceAddressDO!= null)
                    invoiceCompanyDTO .setAddress(invoiceAddressDO.getAddress());
            }
            return invoiceCompanyDTO;
        }

        return  null;
    }

    //导入数据的时候关联签证订单id
    @Override
    @Transactional
    public int relationVisaOrder(String[] idList, String invoiceNo, String invoiceDate) {
        List<Integer> visaIds = invoiceDAO.selectVisaId(idList,"SF");
        if (visaIds.size() != 0 ){
            return visaIds.get(0);
        }
        int resulti =  invoiceDAO.insertOrderIdInInvoice(StringUtils.join(idList, ",") , invoiceNo);
        int resultv = invoiceDAO.relationVisaOrder(idList , invoiceNo);
        invoiceDAO.updateInvoiceCreate(idList,invoiceDate,"SF");
        if (resulti > 0 & resultv > 0 )
            return -1;
        else{
            rollback();
        }
        return  -2;
    }

    //查询一个invoice
    @Override
    public Response selectInvoiceByNo(String invoiceNo, String invoiceIds ) {
        if(invoiceIds.substring(0,2).equals("SF")) {
            BigDecimal totalGST = new BigDecimal("0");
            BigDecimal GST = new BigDecimal("0");
            InvoiceServiceFeeDO invoiceServiceFeeDO = invoiceDAO.selectSFInvoiceByNo(invoiceNo);
            if (invoiceServiceFeeDO != null) {
                InvoiceServiceFeeDTO invoiceServiceFeeDTO = mapper.map(invoiceServiceFeeDO, InvoiceServiceFeeDTO.class);
                List<InvoiceServiceFeeDescriptionDO> descriptions = invoiceServiceFeeDTO.getInvoiceServiceFeeDescriptionDOList();
                for (InvoiceServiceFeeDescriptionDO description : descriptions) {
                    totalGST = totalGST.add(description.getAmount());
                }
                GST = totalGST.divide(new BigDecimal("11"), 2, BigDecimal.ROUND_HALF_UP);
                invoiceServiceFeeDTO.setSubtotal(totalGST.subtract(GST));
                invoiceServiceFeeDTO.setGst(GST);
                invoiceServiceFeeDTO.setTotalGST(totalGST);
                return new Response(0, invoiceServiceFeeDTO);
            }
        }
        if(invoiceIds.substring(0,2).equals("SC")){
            BigDecimal totalGST = new BigDecimal("0");
            BigDecimal GST = new BigDecimal("0");
            InvoiceSchoolDO invoiceSchoolDO = invoiceDAO.selectSCInvoiceByNo(invoiceNo);
            /*
            if ( marketing == null | marketing == ""){
                if ( invoiceSchoolDO != null ){
                    InvoiceSchoolDTO invoiceSchoolDTO = mapper.map(invoiceSchoolDO, InvoiceSchoolDTO.class);
                    List<InvoiceSchoolDescriptionDO> descriptionDOS = invoiceSchoolDO.getInvoiceSchoolDescriptionDOS();
                    for(InvoiceSchoolDescriptionDO description : descriptionDOS){
                        totalGST = totalGST.add(description.getBonus());
                        totalGST = totalGST.add(description.getCommission());
                    }
                    GST = totalGST.divide(new BigDecimal("11"), 2, BigDecimal.ROUND_HALF_UP);
                    invoiceSchoolDTO.setTotalGST(totalGST);
                    invoiceSchoolDTO.setGst(GST);
                    return new Response(0, invoiceSchoolDTO);
                }

            }if (marketing != null && marketing .equalsIgnoreCase("marketing")){
                if ( invoiceSchoolDO != null ){
                    InvoiceSchoolDTO invoiceSchoolDTO = mapper.map(invoiceSchoolDO, InvoiceSchoolDTO.class);
                    List<InvoiceSchoolDescriptionDO> descriptionDOS = invoiceSchoolDO.getInvoiceSchoolDescriptionDOS();
                    for(InvoiceSchoolDescriptionDO description : descriptionDOS){
                        totalGST = totalGST.add(description.getMarketing());
                    }
                    GST = totalGST.divide(new BigDecimal("11"), 2, BigDecimal.ROUND_HALF_UP);
                    invoiceSchoolDTO.setTotalGST(totalGST);
                    invoiceSchoolDTO.setGst(GST);
                    return new Response(0, invoiceSchoolDTO);
                }
            } */
            if (invoiceSchoolDO!= null){
                InvoiceSchoolDTO invoiceSchoolDTO = mapper.map(invoiceSchoolDO, InvoiceSchoolDTO.class);
                List<InvoiceSchoolDescriptionDO> descriptionDOS = invoiceSchoolDO.getInvoiceSchoolDescriptionDOS();
                if (invoiceSchoolDO.getFlag().equals("N")){
                    for(InvoiceSchoolDescriptionDO description : descriptionDOS){
                        totalGST = totalGST.add(description.getBonus());
                        totalGST = totalGST.add(description.getCommission());
                    }
                    GST = totalGST.divide(new BigDecimal("11"), 2, BigDecimal.ROUND_HALF_UP);
                    invoiceSchoolDTO.setTotalGST(totalGST);
                    invoiceSchoolDTO.setGst(GST);
                    return new Response(0, invoiceSchoolDTO);
                }
                if (invoiceSchoolDO.getFlag().equals("M")){
                    for(InvoiceSchoolDescriptionDO description : descriptionDOS){
                        totalGST = totalGST.add(description.getMarketing());
                    }
                    GST = totalGST.divide(new BigDecimal("11"), 2, BigDecimal.ROUND_HALF_UP);
                    invoiceSchoolDTO.setTotalGST(totalGST);
                    invoiceSchoolDTO.setGst(GST);
                    return new Response(0, invoiceSchoolDTO);
                }
            }

        }
        return  null;
    }

    @Override
    public int selectReaplceOrderId(String[] idList, String invoiceNo) {
        List<Integer> visaIds = invoiceDAO.selectVisaId(idList,"SC");
        if (visaIds.size() != 0 ){
            //return visaIds.get(0);
        }
        return 0;
    }


    //关联留学订单id
    @Override
    @Transactional
    public int relationCommissionOrder(String[] idList, String invoiceNo, String invoiceDate) {
        //b_invoce_school中插入order_id
        int resulti =  invoiceDAO.insertCommissionOrderIdInInvoice(StringUtils.join(idList, ",") , invoiceNo);
        //int resultin =  invoiceDAO.insertCommissionOrderIdInInvoice(StringUtils.join(idList, ",") , newInvoiceNo);
        //b_commission_order插入invoice_no
        int resultc = invoiceDAO.relationCommissionOrder(idList , invoiceNo);
        invoiceDAO.updateInvoiceCreate(idList,invoiceDate,"SC");

        List<String> stateList = new ArrayList<>();
        stateList.add(BaseCommissionOrderController.ReviewKjStateEnum.REVIEW.toString());
        stateList.add(BaseCommissionOrderController.ReviewKjStateEnum.FINISH.toString());
        stateList.add(BaseCommissionOrderController.ReviewKjStateEnum.COMPLETE.toString());
        stateList.add(BaseCommissionOrderController.ReviewKjStateEnum.CLOSE.toString());
        int resultzydate = invoiceDAO.updateCommissionOrderZyDate(stateList,idList,null);
        if ( resulti > 0 & resultc > 0 ){
            return  -1 ;
        }
        else {
            rollback();
        }
        return -2;
    }

    @Override
    public List<InvoiceBillToDO> billToList() {
        return invoiceDAO.billToList();
    }


    @Override
    public int addBillTo(String company, String abn, String address) {
        List<InvoiceBillToDO> billToDOS = invoiceDAO.billToList();
        for (InvoiceBillToDO billTo : billToDOS){
            if (billTo.getCompany().equals(company))
                return  -1;
        }

        return invoiceDAO.addBillTo(company,abn,address);
    }

    @Override
    public int selectLastBillTo() {
        return invoiceDAO.selectLastBillTo();
    }

    @Override
    public boolean selectInvoiceNo(String invoiceNo ,String table) {
        List<String> invoiceNoList = invoiceDAO.selectInvoiceNo(table,invoiceNo);
        if ( invoiceNoList.size() > 0 )
            return true;
        return false;
    }

    //保存servicefee
    @Override
    @Transactional
    public int saveServiceFeeInvoice( String invoiceDate, String email, String company, String abn, String address, String tel, String invoiceNo,
                                      String billTo,String note, String accountname, String bsb, String accountno, String branch,
                                     List<InvoiceServiceFeeDescriptionDO> invoiceServiceFeeDescriptionDOList) {

        int resultsavein = invoiceDAO.saveServiceFeeInvoice(invoiceDate,email,company,abn,address,tel,invoiceNo,billTo,note,accountname,bsb,accountno,branch);
        int resultsavedes = invoiceDAO.saveServiceFeeDescription(invoiceServiceFeeDescriptionDOList,invoiceNo);
        if(resultsavein > 0 && resultsavedes > 0)
            return 1 ;
        else {
            rollback();
        }
        return 0;
    }

    /**
     * 暂时弃用
     * @param paramMap
     * @return
     */
    @Override
    @Deprecated
    @Transactional
    public int saveSchoolInvoice(Map paramMap) {
        List<InvoiceSchoolDescriptionDO> description = (List<InvoiceSchoolDescriptionDO>) paramMap .get("description");
        if(invoiceDAO.saveSchoolInvoice(paramMap)  && invoiceDAO.saveSchoolDescription(description, paramMap.get("invoiceNo")) )
            return 1 ;
        else{
            rollback();
        }
        return 0;
    }

    @Override
    @Transactional
    public int saveSchoolInvoice(Map paramMap, List<InvoiceSchoolDescriptionDO> des) throws ServiceException {
        des = checkSchoolDescriptionInstallmentDueDate(des);
        if(  invoiceDAO.saveSchoolInvoice(paramMap) && invoiceDAO.saveSchoolDescription(des, paramMap.get("invoiceNo"))) {
            return 1;
        } else{
            rollback();
        }
        return 0;
    }

    //保存pdf到文件夹中
    @Override
    public Response pdfPrint(String invoiceNo, String invoiceIds, String realpath ,boolean canceled) {

        Response response = selectInvoiceByNo(invoiceNo, invoiceIds);
        String result = "";
        if (response != null) {
            Map<String,String> map = new HashMap<>();
            map.put("invoiceNo",invoiceNo);
            if (invoiceIds.substring(0, 2).equals("SF")) {
                InvoiceServiceFeeDTO invoiceServiceFeeDTO = (InvoiceServiceFeeDTO) response.getData();
                if (invoiceServiceFeeDTO != null) {
                    //Map<String, Object> servicefeepdfMap = JSON.parseObject(JSON.toJSONString(invoiceServiceFeeDTO), Map.class);
                    //result = PrintPdfUtil.pdfout(invoiceNo + "_SF" + invoiceServiceFeeDTO.getId(), response, "SF", realpath ,canceled);
                    result = PrintPdfUtil.pdfout("Tax Invoice " + invoiceNo , response, "SF", realpath ,canceled);
                    map.put("type","SF");
                    map.put("pdfUrl",result);
                }
            }
            if (invoiceIds.substring(0, 2).equals("SC")) {
                InvoiceSchoolDTO invoiceSchoolDTO = (InvoiceSchoolDTO) response.getData();
                //通过companyid查询是不是IES公司
                if (invoiceSchoolDTO == null)
                    return null;
                int companyId = invoiceSchoolDTO.getCompanyId();
                InvoiceCompanyDTO invoiceCompanyDTO = invoiceDAO.selectCompanyById(companyId);
                InvoiceBillToDO billToDO = invoiceSchoolDTO.getInvoiceBillToDO();
                if (billToDO != null & invoiceCompanyDTO != null)
                    if (invoiceCompanyDTO.getSimple().equals("IES")) {
                        //result = PrintPdfUtil.pdfout(invoiceNo + "_SC" + invoiceSchoolDTO.getId(), response, "IES", realpath ,canceled);
                        result = PrintPdfUtil.pdfout(billToDO.getCompany() + " " + invoiceNo, response, "IES", realpath, canceled);
                        map.put("pdfUrl", result);
                    } else {
                        if (invoiceSchoolDTO.getFlag().equals("M")) {
                            result = PrintPdfUtil.pdfout(billToDO.getCompany() + " " + invoiceNo, response, "M", realpath, canceled);
                            map.put("pdfUrl", result);
                        }
                        if (invoiceSchoolDTO.getFlag().equals("N")) {
                            result = PrintPdfUtil.pdfout(billToDO.getCompany() + " " + invoiceNo, response, "N", realpath, canceled);
                            map.put("pdfUrl", result);
                        }
                    }
                map.put("type","SC");
            }
            if (invoiceDAO.updatePdfUrl(map))
                return new Response(0, "/statics/" + result);
        }
        return new Response(0, result);
    }

    @Override
    @Transactional
    public String updateSFInvoice(Map paramMap) {
        String invoiceNo = (String) paramMap.get("invoiceNo");
        String idList [] =  ((String) paramMap.get("idList")).split(",");
        List<InvoiceServiceFeeDescriptionDO> description = (List<InvoiceServiceFeeDescriptionDO>) paramMap.get("description");

        invoiceDAO.updateVisaInvoiceNumberNull(invoiceNo);
        List<Integer> visaIds = invoiceDAO.selectVisaId(idList,"SF");
        if (visaIds.size() != 0 ){
            rollback();
            return visaIds.get(0) + " 已经被关联了 ！";
        }

        int resulti =  invoiceDAO.insertOrderIdInInvoice(StringUtils.join(idList, ",") , invoiceNo);
        int resultv = invoiceDAO.relationVisaOrder(idList , invoiceNo);

        invoiceDAO.deleteDesc(invoiceNo,"SF");
        invoiceDAO.saveServiceFeeDescription(description,invoiceNo);

        if (invoiceDAO.updateSFInvoice(paramMap)>0)
            return "success";
        return "";
    }

    @Override
    @Transactional
    public String updateSCInvoice(Map paramMap) throws ServiceException {
        String invoiceNo = (String) paramMap.get("invoiceNo");
        String idList [] =  ((String) paramMap.get("idList")).split(",");
        List<InvoiceSchoolDescriptionDO> description = (List<InvoiceSchoolDescriptionDO>) paramMap.get("description");
        checkSchoolDescriptionInstallmentDueDate(description);
        String invoiceDate = (String) paramMap.get("invoiceDate");
        boolean isContainsCommissionOrder = true;


        //验证留学佣金订单是否已经绑定过发票
        //List<Integer> visaIds = invoiceDAO.selectVisaId(idList,"SC");
        //if (visaIds.size() != 0 ){
        //    rollback();
        //    return visaIds.get(0) + " 佣金订单已经关联！";
        //}

        for (String id : idList){
            if (commissionOrderDAO.getCommissionOrderById(StringUtil.toInt(id)) == null){
                isContainsCommissionOrder = false;
                break;
            }
        }
        if (isContainsCommissionOrder){
            invoiceDAO.removeInvoiceNumberInCommissionOrder(invoiceNo);

            int resulti =  invoiceDAO.insertCommissionOrderIdInInvoice(StringUtils.join(idList, ",") , invoiceNo);
            int resultc = invoiceDAO.relationCommissionOrder(idList , invoiceNo);

            List<String> stateList = new ArrayList<>();
            stateList.add(BaseCommissionOrderController.ReviewKjStateEnum.REVIEW.toString());
            stateList.add(BaseCommissionOrderController.ReviewKjStateEnum.FINISH.toString());
            stateList.add(BaseCommissionOrderController.ReviewKjStateEnum.COMPLETE.toString());
            stateList.add(BaseCommissionOrderController.ReviewKjStateEnum.CLOSE.toString());
            int resultzydate = invoiceDAO.updateCommissionOrderZyDate(stateList,idList ,invoiceDate);

            invoiceDAO.deleteDesc(invoiceNo,"SC");
            invoiceDAO.saveSchoolDescription(description,invoiceNo);
            if (invoiceDAO.updateSCInvoice(paramMap)>0)
                return "sucess";
        }

        return "fail";
    }

    private List<InvoiceSchoolDescriptionDO> checkSchoolDescriptionInstallmentDueDate(List<InvoiceSchoolDescriptionDO> description) throws ServiceException {
        JSONArray jsonArray = JSONArray.parseArray(JSON.toJSONString(description));
        List<InvoiceSchoolDescriptionDO> _description = new ArrayList<>();
        int num = jsonArray.size();
        for (int i = 0 ; i < num ; i++ ){
            JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(jsonArray.get(i)));
            //if (String.valueOf(jsonObject.get("dob")).equals(" 00:00:00"))
            //    throw  new ServiceException("dob 时间错误," + jsonObject.get("dob")) ;
            try {
                Date dateDob = ymdsdf.parse(String.valueOf(jsonObject.get("dob")));
            } catch (ParseException e) {
                jsonObject.put("dob",new Date());
            }
            try {
                Date dateStartDate = ymdsdf.parse(String.valueOf(jsonObject.get("startDate")));
            } catch (ParseException e) {
                jsonObject.put("startDate",new Date());
            }
            try {
                Date dateInstallmentDueDate = ymdsdf.parse(String.valueOf(jsonObject.get("installmentDueDate")));
            } catch (ParseException e) {
                jsonObject.put("installmentDueDate",new Date());
            }
            try {
                _description.add(JSON.parseObject(JSON.toJSONString(jsonObject),InvoiceSchoolDescriptionDO.class));
            }catch (JSONException e){
                ServiceException se = new ServiceException("Description 栏 时间输入错误:" + e.getMessage());
                se.setCode(ErrorCodeEnum.DATA_ERROR.code());
                throw  se ;
            }
        }
        for (InvoiceSchoolDescriptionDO invoiceSchoolDescriptionDO : _description)  {
            if (invoiceSchoolDescriptionDO.getInstallmentDueDate() == null || ymdsdf.format(invoiceSchoolDescriptionDO.getInstallmentDueDate()).equalsIgnoreCase(STR1900)){
                //throw  new ServiceException("installment due date 时间错误!") ;
            }
        }
        return _description;
    }

    public enum typeEnum{
        SC,SF;
        public static typeEnum get(String name){
            for (typeEnum e : typeEnum.values())
                if (e.toString().equals(name))
                    return e;
            return null;
        }
    }
}
