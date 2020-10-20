package org.zhinanzhen.b.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhinanzhen.b.dao.InvoiceDAO;
import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.service.InvoiceService;
import org.zhinanzhen.b.service.pojo.InvoiceCompanyDTO;
import org.zhinanzhen.b.service.pojo.InvoiceDTO;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.impl.BaseService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    public List<InvoiceDTO> selectInvoice(String invoice_no, String order_id, String create_start, String create_end, String kind, String branch, int pageNum, int pageSize) {

        if(order_id == null | order_id == "" ) {
            List<InvoiceDTO> invoiceDTOList = invoiceDAO.selectScoolInvoice(invoice_no, order_id, create_start, create_end, branch, (pageNum - 1) * pageSize, pageSize);
            List<InvoiceDTO> invoiceServiceFeeDTOList = invoiceDAO.selectServiceFeeInvoice(invoice_no, order_id, create_start, create_end, branch, (pageNum - 1) * pageSize, pageSize);
            invoiceDTOList.forEach(invoice -> {
                invoice.setIds("SC" + invoice.getId());
            });
            invoiceServiceFeeDTOList.forEach(invoice -> {
                invoice.setIds("SF" + invoice.getId());
            });
            if (kind == null) {
                invoiceDTOList = invoiceDAO.selectScoolInvoice(invoice_no, order_id, create_start, create_end, branch, (pageNum - 1) * pageSize / 2, pageSize / 2);
                invoiceServiceFeeDTOList = invoiceDAO.selectServiceFeeInvoice(invoice_no, order_id, create_start, create_end, branch, (pageNum - 1) * pageSize / 2, pageSize / 2);
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
                invoiceSC.setIds("SC" + invoiceSC.getId());
                InvoiceDTO invoiceSF = invoiceDAO.selectVisaOrder(order_id);
                invoiceSF.setIds("SF" + invoiceSF.getId());
                list.add(invoiceSC);
                list.add(invoiceSF);
                return  list;
            }
            if (kind.equals("SC")) {
                InvoiceDTO invoiceSC = invoiceDAO.selectCommissionOrder(order_id);
                invoiceSC.setIds("SC" + invoiceSC.getId());
                list.add(invoiceSC);
                return list;
            }
            if (kind.equals("SF")) {
                InvoiceDTO invoiceSF = invoiceDAO.selectVisaOrder(order_id);
                invoiceSF.setIds("SF" + invoiceSF.getId());
                list.add(invoiceSF);
                return list;
            }
        }
        return null;
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

        InvoiceCompanyDTO  invoiceCompanyDTO = null;

        if(company.equals("Compass Education and Migration Pty Ltd")){
            invoiceCompanyDTO =  invoiceDAO.selectCompanyByName(company,"SF");
            InvoiceAddressDO invoiceAddressDO = invoiceDAO.selectAddressByBranch(branch);
            if(invoiceCompanyDTO != null & invoiceAddressDO !=null ){
                invoiceCompanyDTO.setAddress(invoiceAddressDO.getAddress());
                invoiceCompanyDTO.setAccount(invoiceAddressDO.getAccount());
                invoiceCompanyDTO.setBsb(invoiceAddressDO.getBsb());
                return invoiceCompanyDTO;
            }
        }
        if(company.equals("COMPASS SYDNEY PTY LTD")){
            invoiceCompanyDTO =  invoiceDAO.selectCompanyByName(company,"SF");
            if( invoiceCompanyDTO != null )
            invoiceCompanyDTO.setAddress("402/630-634 George Street, Sydney NSW 2000");
            return  invoiceCompanyDTO;
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
        return invoiceDAO.relationVisaOrder(idList , invoiceNo);
    }

    //查询一个invoice
    @Override
    public Response selectInvoiceByNo(String invoiceNo, String invoiceIds) {
        if(invoiceIds.substring(0,2).equals("SF")){
            InvoiceServiceFeeDO invoiceServiceFeeDO = invoiceDAO.selectSFInvoiceByNo(invoiceNo);
            System.out.println(invoiceNo+"==="+invoiceServiceFeeDO.toString());
            return new Response(1,invoiceServiceFeeDO);
        }
        if(invoiceIds.substring(0,2).equals("SC")){
            InvoiceSchoolDO invoiceSchoolDO = invoiceDAO.selectSCInvoiceByNo(invoiceNo);
            System.out.println(invoiceNo+"==="+invoiceSchoolDO.toString());
            return new Response(1,invoiceSchoolDO);
        }
        return  null;
    }

    @Override
    public int relationCommissionOrder(String[] idList, String invoiceNo) {
        return invoiceDAO.relationCommissionOrder(idList , invoiceNo);
    }
}
