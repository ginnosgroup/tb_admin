package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.service.pojo.InvoiceCompanyDTO;
import org.zhinanzhen.b.service.pojo.InvoiceDTO;

import java.util.List;
import java.util.Map;

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
                                        @Param("branch") String branch, @Param("state") String state,
                                        @Param("pageNum") int pageNum, @Param("pageSize") int pageSize);

    List<InvoiceDTO> selectServiceFeeInvoice(@Param("invoice_no") String invoice_no, @Param("order_id") String order_id,
                                             @Param("create_start") String create_start, @Param("create_end") String create_end,
                                             @Param("branch") String branch,@Param("state") String state,
                                             @Param("pageNum") int pageNum, @Param("pageSize") int pageSize);

    Integer selectSFCount(@Param("invoice_no")String invoice_no, @Param("order_id")String order_id, @Param("create_start")String create_start,
                      @Param("create_end")String create_end, @Param("kind")String kind,@Param("branch") String branch ,@Param("state") String state);

    Integer selectSCCount(@Param("invoice_no")String invoice_no, @Param("order_id")String order_id, @Param("create_start")String create_start,
                      @Param("create_end")String create_end, @Param("kind")String kind,@Param("branch") String branch ,@Param("state") String state);

    InvoiceDTO selectCommissionOrder(@Param("order_id") String order_id);

    int selectCommissionOrderCount(@Param("order_id") String order_id);

    InvoiceDTO selectVisaOrder(@Param("order_id")String order_id);

    int selectVisaOrderCount(@Param("order_id")String order_id);

    int updateSFState(@Param("invoiceNo") String invoiceNo);

    int updateSCState(@Param("invoiceNo")String invoiceNo);

    List<InvoiceCompanyDO> selectCompany(@Param("flag") String flag);

    List<InvoiceAddressDO> selectAddress();

    List<InvoiceBranchDO> selectBranch();

    InvoiceCompanyDTO selectCompanyByName(@Param("company") String company,@Param("flag") String flag);

    InvoiceCompanyDTO selectCompanyById(@Param("companyId") int companyId);

    InvoiceAddressDO selectAddressByBranch(@Param("branch") String branch);

    List<String> selectInvoiceBySimple(@Param("simpleBranch") String simpleBranch ,@Param("flag") String flag);

    //servicefee导入数据的时候，关联订单id
    int relationVisaOrder(@Param("idList") String[] idList, @Param("invoiceNo") String invoiceNo);
    //插入invoice 表中 的 order_id
    int insertOrderIdInInvoice(@Param("idList") String  idList, @Param("invoiceNo") String invoiceNo);

    //查询一个invoice
    InvoiceServiceFeeDO selectSFInvoiceByNo(@Param("invoiceNo") String invoiceNo ,@Param("id") String id);

    InvoiceSchoolDO selectSCInvoiceByNo(@Param("invoiceNo")String invoiceNo , @Param("id") String id);

    //school导入数据的时候，关联订单id
    int relationCommissionOrder(@Param("idList") String[] idList, @Param("invoiceNo") String invoiceNo);

    int insertCommissionOrderIdInInvoice(@Param("idList") String idList, @Param("invoiceNo") String invoiceNo);

    List<InvoiceBillToDO> billToList();

    int addBillTo(@Param("company") String company,@Param("abn") String abn,@Param("address") String address);

    int saveServiceFeeInvoice(@Param("invoiceDate")String invoiceDate, @Param("email")String email, @Param("company")String company, @Param("abn")String abn,
                              @Param("address")String address, @Param("tel")String tel, @Param("invoiceNo")String invoiceNo, @Param("note")String note,
                              @Param("accountname")String accountname, @Param("bsb")String bsb, @Param("accountno")String accountno, @Param("branch")String branch);

    int saveServiceFeeDescription(@Param("invoiceServiceFeeDescriptionDOList") List<InvoiceServiceFeeDescriptionDO> invoiceServiceFeeDescriptionDOList ,@Param("invoiceNo") String invoiceNo);

    boolean saveSchoolInvoice(Map paramMap);

    boolean saveSchoolDescription(@Param("description") List<InvoiceSchoolDescriptionDO> description,@Param("invoiceNo") Object invoiceNo);

    List<String> selectInvoiceNo(@Param("table") String table);
}
