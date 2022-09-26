package org.zhinanzhen.b.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.CommissionInfoDO;
import org.zhinanzhen.b.dao.pojo.CommissionOrderDO;
import org.zhinanzhen.b.dao.pojo.CommissionOrderListDO;
import org.zhinanzhen.b.dao.pojo.CommissionOrderReportDO;

public interface CommissionOrderDAO {

	int addCommissionOrder(CommissionOrderDO commissionOrderDo);

	public int countCommissionOrder(@Param("id") Integer id, @Param("regionIdList") List<Integer> regionIdList,
			@Param("maraId") Integer maraId, @Param("adviserId") Integer adviserId,
			@Param("officialId") Integer officialId, @Param("userId") Integer userId, @Param("name") String name,
			@Param("applicantName") String applicantName, @Param("phone") String phone,
			@Param("wechatUsername") String wechatUsername, @Param("schoolId") Integer schoolId,
			@Param("isSettle") Boolean isSettle, @Param("stateList") List<String> stateList,
			@Param("commissionStateList") List<String> commissionStateList,
			@Param("startKjApprovalDate") String startKjApprovalDate,
			@Param("endKjApprovalDate") String endKjApprovalDate, @Param("startDate") String startDate,
			@Param("endDate") String endDate, @Param("startInvoiceCreate") String startInvoiceCreate,
			@Param("endInvoiceCreate") String endInvoiceCreate, @Param("isYzyAndYjy") Boolean isYzyAndYjy,
			@Param("applyState") String applyState);

	int countCommissionOrderBySchoolId(@Param("schoolId") Integer schoolId);

	int countCommissionOrderByServiceOrderIdAndExcludeCode(@Param("serviceOrderId") Integer serviceOrderId,
			@Param("code") String code);

	public List<CommissionOrderListDO> listCommissionOrder(@Param("id") Integer id,
			@Param("regionIdList") List<Integer> regionIdList, @Param("maraId") Integer maraId,
			@Param("adviserId") Integer adviserId, @Param("officialId") Integer officialId,
			@Param("userId") Integer userId, @Param("name") String name, @Param("applicantName") String applicantName,
			@Param("phone") String phone, @Param("wechatUsername") String wechatUsername,
			@Param("schoolId") Integer schoolId, @Param("isSettle") Boolean isSettle,
			@Param("stateList") List<String> stateList, @Param("commissionStateList") List<String> commissionStateList,
			@Param("startKjApprovalDate") String startKjApprovalDate,
			@Param("endKjApprovalDate") String endKjApprovalDate, @Param("startDate") String startDate,
			@Param("endDate") String endDate, @Param("startInvoiceCreate") String startInvoiceCreate,
			@Param("endInvoiceCreate") String endInvoiceCreate, @Param("isYzyAndYjy") Boolean isYzyAndYjy,
			@Param("applyState") String applyState, @Param("offset") int offset, @Param("rows") int rows,
			@Param("orderBy") String orderBy);

	public List<CommissionOrderDO> listCommissionOrderByCode(String code);

	public List<CommissionOrderDO> listCommissionOrderBySchoolId(Integer schoolId);

	public List<CommissionOrderDO> listCommissionOrderByServiceOrderId(Integer serviceOrderId);

	List<CommissionOrderListDO> listCommissionOrderBySchool(@Param("startDate") Date startDate,
			@Param("endDate") Date endDate, @Param("schoolName") String schoolName);

	public List<CommissionOrderListDO> listThisMonthCommissionOrderAtDashboard(@Param("adviserId") Integer adviserId,
			@Param("officialId") Integer officialId);

	List<CommissionOrderReportDO> listCommissionOrderReport(@Param("startDate") String startDate,
			@Param("endDate") String endDate, @Param("dateType") String dateType,
			@Param("dateMethod") String dateMethod, @Param("regionId") Integer regionId,
			@Param("adviserId") Integer adviserId,@Param("adviserIdList") List<String> adviserIdList);

	int updateCommissionOrder(CommissionOrderDO commissionOrderDo);

	CommissionOrderListDO getCommissionOrderById(int id);

	List<CommissionInfoDO> getCommissionInfoById(@Param("id") Integer id,
												 @Param("adviserId") Integer adviserId);

	List<CommissionOrderListDO> listCommissionOrderByInvoiceNumber(String invoiceNumber);

	CommissionOrderListDO getFirstCommissionOrderByServiceOrderId(int serviceOrderId);

	Double sumTuitionFeeBySchoolId(@Param("schoolId") Integer schoolId);

	int deleteCommissionOrderById(int id);

	public List<CommissionOrderDO> listCommissionOrderByState(String state);

	List<CommissionOrderDO> listCommissionOrderByVerifyCode(String verifyCode);

    boolean setBankDateNull(String substring);

	List<CommissionOrderDO> listCommissionOrderInstallmentDueDate();

	int updateCommissionOrderByServiceOrderId(CommissionOrderDO commissionOrderDo);

	List<CommissionOrderListDO> listCommissionOrderByCourse(@Param("providerId") Integer providerId, @Param("courseLevel") String courseLevel ,
														@Param("courseId") Integer courseId,
														@Param("startDate") Date startDate, @Param("endDate") Date endDate);

	int deleteCommissionOrderInfoById(@Param("id")int serviceOrderId ,@Param("installmentNum") int installmentNum) ;

	int setinstallmentById(@Param("id") Integer serviceOrderId,
						   @Param("installment") Integer installment);

	int setinstallmentDueDateById(@Param("id") Integer serviceOrderId,
								  @Param("installmentNum") Integer installmentNum,
								  @Param("installmentDueDate") Date installmentDueDate);

	int addCommissionInfoById(int id);

	CommissionInfoDO getCommissionStateById(@Param("id") Integer serviceOrderId,
								  @Param("installmentNum") Integer installmentNum);
	int updateState(int id);

	List<CommissionOrderListDO> get(@Param("officialId")Integer officialId,
									@Param("regionId")Integer regionId,
									@Param("id")Integer id,
									@Param("commissionState")String commissionState,
									@Param("startSubmitIbDate")String startSubmitIbDate,
									@Param("endSubmitIbDate")String endSubmitIbDate,
									@Param("startDate")String startDate,
									@Param("endDate")String endDate,
									@Param("offset")int offset,
									@Param("pageSize")int pageSize);
	int count(@Param("officialId")Integer officialId,
			  @Param("regionId")Integer regionId,
			  @Param("id")Integer id,
			  @Param("commissionState")String commissionState,
			  @Param("startSubmitIbDate")String startKjApprovalDate,
			  @Param("endSubmitIbDate")String endKjApprovalDate,
			  @Param("startDate")String startDate,
			  @Param("endDate")String endDate);
}
