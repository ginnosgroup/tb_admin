package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.VisaDO;
import org.zhinanzhen.b.dao.pojo.VisaListDO;
import org.zhinanzhen.b.dao.pojo.VisaOfficialDO;
import org.zhinanzhen.b.dao.pojo.VisaOfficialListDO;

import java.util.List;

public interface VisaOfficialDao {
    int countVisaByServiceOrderIdAndExcludeCode(@Param("serviceOrderId") Integer serviceOrderId,
                                                @Param("code") String code);

    int addVisa(VisaOfficialDO visaOfficialDO);

    List<VisaOfficialListDO> get(@Param("officialId")Integer officialId,
                                 @Param("regionIdList")List<Integer> regionIdList,
                                 @Param("id")Integer id,
                                 @Param("startHandlingDate") String startHandlingDate, @Param("endHandlingDate") String endHandlingDate,
                                 @Param("state")String state,
                                 @Param("startDate")String startDate,
                                 @Param("endDate")String endDate,
                                 @Param("userName")String userName,
                                 @Param("applicantName")String applicantName,
                                 @Param("offset")Integer offset,
                                 @Param("pageSize")Integer pageSize,
                                 @Param("orderBy") String orderBy);

    int count(@Param("officialId")Integer officialId,
              @Param("regionIdList")List<Integer> regionIdList,
              @Param("id")Integer id,
              @Param("startHandlingDate") String startHandlingDate, @Param("endHandlingDate") String endHandlingDate,
              @Param("state")String state,

              @Param("startDate")String startDate,
              @Param("endDate")String endDate,
              @Param("userName")String userName,
              @Param("applicantName")String applicantName);
    VisaListDO getOne(@Param("id")Integer id
    );

    List<VisaOfficialDO> listVisaByCode(@Param("code") String code);

    VisaOfficialDO listByServiceOrderId(@Param("serviceOrderId") int serviceOrderId);

    void update(@Param("id")Integer id,
                @Param("handling_date")String handling_date,
                @Param("commissionAmount") Double commissionAmount,
                @Param("state") String state);
}
