package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.service.pojo.InvoiceCompanyDTO;
import org.zhinanzhen.b.service.pojo.InvoiceDTO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: bsz
 * Date: 2020/10/14 12:17
 * Description:
 * Version: V1.0
 */
public interface InvoiceDAO {

    List<InvoiceDTO> selectScoolInvoice(@Param("invoice_no") String invoice_no, @Param("order_id") String order_id,
                                        @Param("create_start") String create_start, @Param("create_end") String create_end,
                                        @Param("branch") String branch,
                                        @Param("pageNum") int pageNum, @Param("pageSize") int pageSize);

    List<InvoiceDTO> selectServiceFeeInvoice(@Param("invoice_no") String invoice_no, @Param("order_id") String order_id,
                                             @Param("create_start") String create_start, @Param("create_end") String create_end,
                                             @Param("branch") String branch,
                                             @Param("pageNum") int pageNum, @Param("pageSize") int pageSize);

    InvoiceDTO selectCommissionOrder(@Param("order_id") String order_id);

    InvoiceDTO selectVisaOrder(@Param("order_id")String order_id);

    int updateSFState(@Param("invoiceNo") String invoiceNo);

    int updateSCState(@Param("invoiceNo")String invoiceNo);

    List<InvoiceCompanyDO> selectCompany(@Param("flag") String flag);

    List<InvoiceAddressDO> selectAddress();

    List<InvoiceBranchDO> selectBranch();

    InvoiceCompanyDTO selectCompanyByName(@Param("company") String company,@Param("flag") String flag);

    InvoiceAddressDO selectAddressByBranch(@Param("branch") String branch);

    List<String> selectInvoiceBySimple(@Param("simpleBranch") String simpleBranch ,@Param("flag") String flag);

    //servicefee导入数据的时候，关联订单id
    int relationVisaOrder(@Param("idList") String[] idList, @Param("invoiceNo") String invoiceNo);

    //查询一个invoice
    InvoiceServiceFeeDO selectSFInvoiceByNo(@Param("invoiceNo") String invoiceNo);

    InvoiceSchoolDO selectSCInvoiceByNo(@Param("invoiceNo")String invoiceNo);

    //school导入数据的时候，关联订单id
    int relationCommissionOrder(@Param("idList") String[] idList, @Param("invoiceNo") String invoiceNo);
}
