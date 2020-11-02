package org.zhinanzhen.b.service;

import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.service.pojo.InvoiceCompanyDTO;
import org.zhinanzhen.b.service.pojo.InvoiceDTO;
import org.zhinanzhen.tb.controller.Response;

import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: bsz
 * Date: 2020/10/14 11:51
 * Description:
 * Version: V1.0
 */
public interface InvoiceService {

    List<InvoiceDTO> selectInvoice(String invoice_no, String order_id, String create_start, String create_end, String kind, String branch, int pageNum, int pageSize,String state);

    int selectCount(String invoice_no, String order_id, String create_start, String create_end, String kind, String branch, String state);

    int updateState(String invoiceNo, String invoiceIds);

    List<InvoiceCompanyDO> selectCompany(String flag);

    List<InvoiceAddressDO> selectAddress();

    List<InvoiceBranchDO> selectBranch();

    InvoiceCompanyDTO addServiceFeeInvoice(String branch, String company);

    String selectInvoiceBySimple(String simpleBranch ,String flag);

    InvoiceCompanyDTO addSchoolInvoice(String branch, String company);
    //导入数据的时候关联订单Id
    int relationVisaOrder(String[] idList, String invoiceNo);
    //查询一个invoice
    Response selectInvoiceByNo(String invoiceNo, String invoiceIds, String marketing);

    int relationCommissionOrder(String[] idList, String invoiceNo);

    int addBillTo(String company, String abn, String address);

    boolean selectInvoiceNo(String invoiceNo ,String table);

    int saveServiceFeeInvoice(String invoiceDate, String email, String company, String abn, String address, String tel, String invoiceNo, String note, String accountname, String bsb, String accountno, String branch, List<InvoiceServiceFeeDescriptionDO> invoiceServiceFeeDescriptionDOList);

    int saveSchoolInvoice(Map paramMap);

    List<InvoiceBillToDO> billToList();

    Response pdfPrint(String invoiceNo, String invoiceIds, String marketing ,String realpath);


}
