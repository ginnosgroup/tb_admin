package org.zhinanzhen.b.service;

import org.zhinanzhen.b.dao.pojo.InvoiceBranchDO;
import org.zhinanzhen.b.dao.pojo.InvoiceAddressDO;
import org.zhinanzhen.b.dao.pojo.InvoiceCompanyDO;
import org.zhinanzhen.b.dao.pojo.InvoiceServiceFeeDO;
import org.zhinanzhen.b.service.pojo.InvoiceCompanyDTO;
import org.zhinanzhen.b.service.pojo.InvoiceDTO;
import org.zhinanzhen.tb.controller.Response;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: bsz
 * Date: 2020/10/14 11:51
 * Description:
 * Version: V1.0
 */
public interface InvoiceService {

    List<InvoiceDTO> selectInvoice(String invoice_no, String order_id, String create_start, String create_end, String kind, String branch, int pageNum, int pageSize);

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
    Response selectInvoiceByNo(String invoiceNo, String invoiceIds);

    int relationCommissionOrder(String[] idList, String invoiceNo);
}