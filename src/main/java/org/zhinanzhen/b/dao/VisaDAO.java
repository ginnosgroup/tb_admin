package org.zhinanzhen.b.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.VisaDO;
import org.zhinanzhen.b.dao.pojo.VisaListDO;
import org.zhinanzhen.b.dao.pojo.VisaReportDO;

public interface VisaDAO {

	public int addVisa(VisaDO visaDo);

	public int updateVisa(VisaDO visaDo);

	public int countVisa(@Param("keyword") String keyword, @Param("startHandlingDate") String startHandlingDate,
			@Param("endHandlingDate") String endHandlingDate, @Param("stateList") List<String> stateList,
			@Param("commissionStateList") List<String> commissionStateList,
			@Param("startKjApprovalDate") String startKjApprovalDate,
			@Param("endKjApprovalDate") String endKjApprovalDate, @Param("startDate") String startDate,
			@Param("endDate") String endDate, @Param("adviserId") Integer adviserId, @Param("userId") Integer userId);

	public int countVisaByServiceOrderIdAndExcludeCode(@Param("serviceOrderId") Integer serviceOrderId,
			@Param("code") String code);

	public List<VisaListDO> listVisa(@Param("keyword") String keyword,
			@Param("startHandlingDate") String startHandlingDate, @Param("endHandlingDate") String endHandlingDate,
			@Param("stateList") List<String> stateList, @Param("commissionStateList") List<String> commissionStateList,
			@Param("startKjApprovalDate") String startKjApprovalDate,
			@Param("endKjApprovalDate") String endKjApprovalDate, @Param("startDate") String startDate,
			@Param("endDate") String endDate, @Param("adviserId") Integer adviserId, @Param("userId") Integer userId,
			@Param("offset") int offset, @Param("rows") int rows);

	List<VisaDO> listVisaByCode(@Param("code") String code);

	List<VisaDO> listVisaByServiceOrderId(@Param("serviceOrderId") int serviceOrderId);

	List<VisaReportDO> listVisaReport(@Param("startDate") String startDate, @Param("endDate") String endDate,
			@Param("dateType") String dateType, @Param("dateMethod") String dateMethod,
			@Param("regionId") Integer regionId, @Param("adviserId") Integer adviserId);

	VisaDO getFirstVisaByServiceOrderId(int serviceOrderId);

	public double sumBonusByThisMonth();

	public VisaDO getVisaById(int id);

	public int deleteVisaById(int id);

}
