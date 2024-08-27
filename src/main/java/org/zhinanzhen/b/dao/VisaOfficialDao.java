package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.zhinanzhen.b.dao.pojo.VisaListDO;
import org.zhinanzhen.b.dao.pojo.VisaOfficialDO;
import org.zhinanzhen.b.dao.pojo.VisaOfficialListDO;
import org.zhinanzhen.b.service.pojo.VisaOfficialDTO;

import java.util.Date;
import java.util.List;

public interface VisaOfficialDao {
    int countVisaByServiceOrderIdAndExcludeCode(@Param("serviceOrderId") Integer serviceOrderId,
                                                @Param("code") String code);

    int countVisaByServiceOrderId(@Param("serviceOrderId") Integer serviceOrderId);

    int addVisa(VisaOfficialDO visaOfficialDO);

    List<VisaOfficialListDO> list(@Param("officialId")Integer officialId,
                                 @Param("regionIdList")List<Integer> regionIdList,
                                 @Param("id")Integer id,
                                 @Param("startHandlingDate") String startHandlingDate, @Param("endHandlingDate") String endHandlingDate,
                                 @Param("state")String state,
                                 @Param("startDate")String startDate,
                                 @Param("endDate")String endDate,
                                  @Param("firstSettlementMonth")String firstSettlementMonth,@Param("lastSettlementMonth")String lastSettlementMonth,
                                  @Param("userName")String userName,
                                 @Param("applicantName")String applicantName,
                                 @Param("isMerged") Boolean isMerged,
                                 @Param("offset")Integer offset,
                                 @Param("pageSize")Integer pageSize,
                                 @Param("orderBy") String orderBy,
                                  @Param("serviceOrderType")String serviceOrderType);

    int count(@Param("officialId")Integer officialId,
              @Param("regionIdList")List<Integer> regionIdList,
              @Param("id")Integer id,
              @Param("startHandlingDate") String startHandlingDate, @Param("endHandlingDate") String endHandlingDate,
              @Param("state")String state,

              @Param("startDate")String startDate,
              @Param("endDate")String endDate,
              @Param("userName")String userName,
              @Param("applicantName")String applicantName,
              @Param("isMerged") Boolean isMerged);
    VisaOfficialDO getOne(@Param("id")Integer id
    );

    List<VisaOfficialDO> listVisaByCode(@Param("code") String code);

    VisaOfficialDO getByServiceOrderId(@Param("serviceOrderId") int serviceOrderId);

    void update(@Param("id")Integer id,
                @Param("handling_date")String handling_date,
                @Param("commissionAmount") Double commissionAmount,
                @Param("state") String state,
                @Param("serviceId") Integer serviceId);

    int updateVisaOfficial(VisaOfficialDO visaOfficialDO);

    int updateHandlingDate(@Param("id")Integer id,
                           @Param("handlingDate") Date handlingDate);
    
	int updateMerged(@Param("id") Integer id, @Param("isMerged") boolean isMerged);

    @Delete("DELETE FROM b_visa_official where service_order_id = #{id}")
    int deleteByServiceOrderId(int id);

    @Select("select * from b_visa_official where service_order_id = #{serviceOrderId}")
    List<VisaOfficialDTO> getAllvisaOfficialByServiceOrderId(Integer serviceOrderId);
}
