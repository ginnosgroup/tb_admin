package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.VisaDO;
import org.zhinanzhen.b.dao.pojo.VisaListDO;
import org.zhinanzhen.b.dao.pojo.VisaReportDO;
import org.zhinanzhen.b.service.pojo.UserDTO;

public interface VisaDAO {

	public int addVisa(VisaDO visaDo);

	public int updateVisa(VisaDO visaDo);

	public int countVisa(@Param("id") Integer id, @Param("keyword") String keyword,
			@Param("startHandlingDate") String startHandlingDate, @Param("endHandlingDate") String endHandlingDate,
			@Param("stateList") List<String> stateList, @Param("commissionStateList") List<String> commissionStateList,
			@Param("startKjApprovalDate") String startKjApprovalDate,
			@Param("endKjApprovalDate") String endKjApprovalDate, @Param("startDate") String startDate,
			@Param("endDate") String endDate, @Param("startInvoiceCreate") String startInvoiceCreate,
			@Param("endInvoiceCreate") String endInvoiceCreate, @Param("regionIdList") List<Integer> regionIdList,
			@Param("adviserId") Integer adviserId, @Param("userId") Integer userId,
			@Param("applicantName") String applicantName, @Param("state") String state);

	public int countVisaByServiceOrderIdAndExcludeCode(@Param("serviceOrderId") Integer serviceOrderId,
			@Param("code") String code);

	public List<VisaListDO> listVisa(@Param("id") Integer id, @Param("keyword") String keyword,
			@Param("startHandlingDate") String startHandlingDate, @Param("endHandlingDate") String endHandlingDate,
			@Param("stateList") List<String> stateList, @Param("commissionStateList") List<String> commissionStateList,
			@Param("startKjApprovalDate") String startKjApprovalDate,
			@Param("endKjApprovalDate") String endKjApprovalDate, @Param("startDate") String startDate,
			@Param("endDate") String endDate, @Param("startInvoiceCreate") String startInvoiceCreate,
			@Param("endInvoiceCreate") String endInvoiceCreate, @Param("regionIdList") List<Integer> regionIdList,
			@Param("adviserId") Integer adviserId, @Param("userId") Integer userId, @Param("userName") String userName,
			@Param("applicantName") String applicantName, @Param("state") String state, @Param("offset") int offset,
			@Param("rows") int rows, @Param("orderBy") String orderBy);

	List<VisaDO> listVisaByCode(@Param("code") String code);

	List<VisaDO> listVisaByServiceOrderId(@Param("serviceOrderId") int serviceOrderId);

	List<VisaReportDO> listVisaReport(@Param("startDate") String startDate, @Param("endDate") String endDate,
			@Param("dateType") String dateType, @Param("dateMethod") String dateMethod,
			@Param("regionId") Integer regionId, @Param("adviserId") Integer adviserId,
			@Param("adviserIdList") List<String> adviserIdList);

	VisaDO getFirstVisaByServiceOrderId(int serviceOrderId);

	VisaDO getSecondVisaByServiceOrderId(int serviceOrderId);

	VisaDO getVisaByServiceOrderId(int serviceOrderId);

	public double sumBonusByThisMonth();

	public VisaDO getVisaById(int id);

	public int deleteVisaById(int id);

    List<UserDTO> listVisaRemindDateDesc(@Param("adviserId")int adviserId,@Param("pageNum") int pageNum,@Param("pageSize") int  pageSize);

	List<VisaDO> listVisaByVerifyCode(@Param("verifyCode") String verifyCode);

	boolean setBankDateNull(String substring);

	List<UserDTO> listVisaExpirationDate();
	List<VisaListDO> get(@Param("officialId")Integer officialId,
						 @Param("regionId")Integer regionId,
						 @Param("id")Integer id,
						 @Param("startHandlingDate") String startHandlingDate, @Param("endHandlingDate") String endHandlingDate,
						 @Param("commissionState")String commissionState,
						 @Param("startSubmitIbDate")String startSubmitIbDate,
						 @Param("endSubmitIbDate")String endSubmitIbDate,
						 @Param("startDate")String startDate,
						 @Param("endDate")String endDate,
						 @Param("userName")String userName,
						 @Param("applicantName")String applicantName,
						 @Param("offset")Integer offset,
						 @Param("pageSize")Integer pageSize);
	int count(@Param("officialId")Integer officialId,
			  @Param("regionId")Integer regionId,
			  @Param("id")Integer id,
			  @Param("startHandlingDate") String startHandlingDate, @Param("endHandlingDate") String endHandlingDate,
			  @Param("commissionState")String commissionState,
			  @Param("startSubmitIbDate")String startKjApprovalDate,
			  @Param("endSubmitIbDate")String endKjApprovalDate,
			  @Param("startDate")String startDate,
			  @Param("endDate")String endDate,
	          @Param("userName")String userName,
	          @Param("applicantName")String applicantName);
	VisaListDO getOne(@Param("id")Integer id
						 );

	List<VisaReportDO> listVisaReportSubtractGst(@Param("startDate") String startDate, @Param("endDate") String endDate,
												 @Param("dateType") String dateType, @Param("dateMethod") String dateMethod,
												 @Param("regionId") Integer regionId, @Param("adviserId") Integer adviserId,
												 @Param("adviserIdList") List<String> adviserIdList);
}
