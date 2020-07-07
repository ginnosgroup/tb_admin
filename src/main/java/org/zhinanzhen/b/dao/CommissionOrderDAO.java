package org.zhinanzhen.b.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.CommissionOrderDO;
import org.zhinanzhen.b.dao.pojo.CommissionOrderListDO;

public interface CommissionOrderDAO {

	int addCommissionOrder(CommissionOrderDO commissionOrderDo);

	public int countCommissionOrder(@Param("maraId") Integer maraId, @Param("adviserId") Integer adviserId,
			@Param("officialId") Integer officialId, @Param("name") String name, @Param("phone") String phone,
			@Param("wechatUsername") String wechatUsername, @Param("schoolId") Integer schoolId,
			@Param("isSettle") Boolean isSettle, @Param("stateList") List<String> stateList,
			@Param("commissionStateList") List<String> commissionStateList,
			@Param("startKjApprovalDate") String startKjApprovalDate,
			@Param("endKjApprovalDate") String endKjApprovalDate, @Param("isYzyAndYjy") Boolean isYzyAndYjy);

	int countCommissionOrderBySchoolId(@Param("schoolId") Integer schoolId);

	int countCommissionOrderByServiceOrderIdAndExcludeCode(@Param("serviceOrderId") Integer serviceOrderId,
			@Param("code") String code);

	public List<CommissionOrderListDO> listCommissionOrder(@Param("maraId") Integer maraId,
			@Param("adviserId") Integer adviserId, @Param("officialId") Integer officialId, @Param("name") String name,
			@Param("phone") String phone, @Param("wechatUsername") String wechatUsername,
			@Param("schoolId") Integer schoolId, @Param("isSettle") Boolean isSettle,
			@Param("stateList") List<String> stateList, @Param("commissionStateList") List<String> commissionStateList,
			@Param("startKjApprovalDate") String startKjApprovalDate,
			@Param("endKjApprovalDate") String endKjApprovalDate, @Param("isYzyAndYjy") Boolean isYzyAndYjy,
			@Param("offset") int offset, @Param("rows") int rows);

	public List<CommissionOrderDO> listCommissionOrderByCode(String code);

	public List<CommissionOrderDO> listCommissionOrderByServiceOrderId(Integer serviceOrderId);

	List<CommissionOrderListDO> listCommissionOrderBySchool(@Param("startDate") Date startDate,
			@Param("endDate") Date endDate, @Param("schoolName") String schoolName);

	public List<CommissionOrderListDO> listThisMonthCommissionOrderAtDashboard(@Param("adviserId") Integer adviserId,
			@Param("officialId") Integer officialId);

	int updateCommissionOrder(CommissionOrderDO commissionOrderDo);

	CommissionOrderListDO getCommissionOrderById(int id);
	
	List<CommissionOrderListDO> listCommissionOrderByInvoiceNumber(String invoiceNumber);

	CommissionOrderListDO getFirstCommissionOrderByServiceOrderId(int serviceOrderId);

	Double sumTuitionFeeBySchoolId(@Param("schoolId") Integer schoolId);

}
