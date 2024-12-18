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
                                  @Param("serviceOrderType")String serviceOrderType,
                                  @Param("serviceOrderId") Integer serviceOrderId,
                                  @Param("currency") String currency);

    int count(@Param("officialId")Integer officialId,
              @Param("regionIdList")List<Integer> regionIdList,
              @Param("id")Integer id,
              @Param("startHandlingDate") String startHandlingDate, @Param("endHandlingDate") String endHandlingDate,
              @Param("state")String state,

              @Param("startDate")String startDate,
              @Param("endDate")String endDate,
              @Param("userName")String userName,
              @Param("applicantName")String applicantName,
              @Param("isMerged") Boolean isMerged,
                @Param("currency") String currency);
    VisaOfficialDO getOne(@Param("id")Integer id
    );

    List<VisaOfficialDO> listVisaByCode(@Param("code") String code);

    VisaOfficialDO getByServiceOrderId(@Param("serviceOrderId") int serviceOrderId);

    VisaOfficialDO getByServiceOrderIdOne(@Param("serviceOrderId") int serviceOrderId);

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


    @Select("SELECT * FROM b_visa_official WHERE service_order_id IN (1030522,1039680,\n" +
            "1039679,1039656,1039655,1039654,1042554,\n" +
            "1042276,1042829,1038426,1044434,1044298,\n" +
            "1039902,1044498,1044152,1045406,1045408,\n" +
            "1044840,1044805,1044964,1043105,1037639,\n" +
            "1045373,1044966,1045577,1045383,1045170,\n" +
            "1045511,1045631,1045641,1033232,1044938,\n" +
            "1045522,1045521,1044363,1045520,1046013,\n" +
            "1045407,1045958,1046087,1043186,1044385,\n" +
            "1046193,1046220,1046376,1045840,1045841,\n" +
            "1046322,1046017,1046554,1046166,1046509,\n" +
            "1046508,1046221,1046370)")
    List<VisaOfficialDO> listLinShi();


    int getCountvisaOfficialByServiceOrderPatrentId(@Param("applicantParentId")int applicantParentId);

}
