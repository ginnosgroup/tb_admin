package org.zhinanzhen.b.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhinanzhen.b.dao.InvoiceDAO;
import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.service.InvoiceService;
import org.zhinanzhen.b.service.pojo.InvoiceCompanyDTO;
import org.zhinanzhen.b.service.pojo.InvoiceDTO;
import org.zhinanzhen.b.service.pojo.InvoiceServiceFeeDTO;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.impl.BaseService;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    //查询invoice
    @Override
    public List<InvoiceDTO> selectInvoice(String invoice_no, String order_id, String create_start, String create_end, String kind, String branch, int pageNum, int pageSize,String state) {

        if(order_id == null | order_id == "" ) {
            List<InvoiceDTO> invoiceDTOList = invoiceDAO.selectScoolInvoice(invoice_no, order_id, create_start, create_end, branch,state, (pageNum - 1) * pageSize, pageSize);
            List<InvoiceDTO> invoiceServiceFeeDTOList = invoiceDAO.selectServiceFeeInvoice(invoice_no, order_id, create_start, create_end, branch, state,(pageNum - 1) * pageSize, pageSize);
            invoiceDTOList.forEach(invoice -> {
                invoice.setIds("SC" + invoice.getId());
            });
            invoiceServiceFeeDTOList.forEach(invoice -> {
                invoice.setIds("SF" + invoice.getId());
            });
            if (kind == null) {
                invoiceDTOList = invoiceDAO.selectScoolInvoice(invoice_no, order_id, create_start, create_end, branch, state,(pageNum - 1) * pageSize / 2, pageSize / 2);
                invoiceServiceFeeDTOList = invoiceDAO.selectServiceFeeInvoice(invoice_no, order_id, create_start, create_end, branch, state ,(pageNum - 1) * pageSize / 2, pageSize / 2);
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
    public int updateState(String invoiceNo, String invoiceIds) {
        String flag = invoiceIds.substring(0,2);
        if (flag.equalsIgnoreCase("SF")){
            return  invoiceDAO.updateSFState(invoiceNo);

        }if (flag.equalsIgnoreCase("SC")){
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
            if (invoiceCompanyDTO.getSimple().equals("CS")) {
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
        List<Integer> invoiceNumber = new ArrayList<>();
        if (invoiceNos != null & invoiceNos.size() != 0){
            invoiceNos.forEach(invoiceNo->{
                invoiceNumber.add(Integer.parseInt(invoiceNo.substring(0,invoiceNo.length()-1)));
            });
            String invoiceNumberMax = Collections.max(invoiceNumber).toString();
            String number = invoiceNumberMax.substring(invoiceNumberMax.length()-2,invoiceNumberMax.length());
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

    //导入数据的时候关联订单id
    @Override
    @Transactional
    public int relationVisaOrder(String[] idList, String invoiceNo) {
        int resulti =  invoiceDAO.insertOrderIdInInvoice(idList , invoiceNo);
        //int resultv = invoiceDAO.relationVisaOrder(idList , invoiceNo);
        if ( resulti > 0  )
            return 1;
        return  0;
    }

    //查询一个invoice
    @Override
    public Response selectInvoiceByNo(String invoiceNo, String invoiceIds) {
        if(invoiceIds.substring(0,2).equals("SF")) {
            BigDecimal totalGST = new BigDecimal("0");
            BigDecimal GST = new BigDecimal("0");
            InvoiceServiceFeeDO invoiceServiceFeeDO = invoiceDAO.selectSFInvoiceByNo(invoiceNo,invoiceIds.substring(2,invoiceIds.length()));
            if (invoiceServiceFeeDO != null) {
                InvoiceServiceFeeDTO invoiceServiceFeeDTO = mapper.map(invoiceServiceFeeDO, InvoiceServiceFeeDTO.class);
                List<InvoiceServiceFeeDescriptionDO> descriptions = invoiceServiceFeeDTO.getInvoiceServiceFeeDescriptionDOList();
                for (InvoiceServiceFeeDescriptionDO description : descriptions) {
                    totalGST = totalGST.add(description.getAmount());
                }
                GST = totalGST.divide(new BigDecimal("11"), 2, BigDecimal.ROUND_HALF_UP);
                invoiceServiceFeeDTO.setSubtotal(totalGST.subtract(GST));
                invoiceServiceFeeDTO.setGST(GST);
                invoiceServiceFeeDTO.setTotalGST(totalGST);
                return new Response(1, invoiceServiceFeeDTO);
            }
        }
        if(invoiceIds.substring(0,2).equals("SC")){
            InvoiceSchoolDO invoiceSchoolDO = invoiceDAO.selectSCInvoiceByNo(invoiceNo);
            return new Response(1,invoiceSchoolDO);
        }
        return  null;
    }

    @Override
    public int relationCommissionOrder(String[] idList, String invoiceNo) {
        return invoiceDAO.relationCommissionOrder(idList , invoiceNo);
    }

    @Override
    public List<InvoiceBillToDO> billToList() {
        return invoiceDAO.billToList();
    }

    @Override
    public int addBillTo(String company, String abn, String address) {
        return invoiceDAO.addBillTo(company,abn,address);
    }

    //保存servicefee
    @Override
    @Transactional
    public int saveServiceFeeInvoice(String invoiceDate, String email, String company, String abn, String address, String tel, String invoiceNo,
                                     String note, String accountname, String bsb, String accountno, String branch,
                                     List<InvoiceServiceFeeDescriptionDO> invoiceServiceFeeDescriptionDOList) {

        int resultsavein = invoiceDAO.saveServiceFeeInvoice(invoiceDate,email,company,abn,address,tel,invoiceNo,note,accountname,bsb,accountno,branch);
        int resultsavedes = invoiceDAO.saveServiceFeeDescription(invoiceServiceFeeDescriptionDOList,invoiceNo);
        if(resultsavein > 0 && resultsavedes > 0)
            return 1 ;
        return 0;
    }

    @Override
    @Transactional
    public int saveSchoolInvoice(Map paramMap) {
        List<InvoiceSchoolDescriptionDO> description = (List<InvoiceSchoolDescriptionDO>) paramMap .get("description");
        if(invoiceDAO.saveSchoolInvoice(paramMap)  && invoiceDAO.saveSchoolDescription(description, paramMap.get("invoiceNo")) )
            return 1 ;
        return 0;
    }


}
