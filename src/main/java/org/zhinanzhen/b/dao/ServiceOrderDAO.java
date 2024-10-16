package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;

import java.util.List;

public interface ServiceOrderDAO {

    int addServiceOrder(ServiceOrderDO serviceOrderDo);

    int updateServiceOrder(ServiceOrderDO serviceOrderDo);

    int setCommission(@Param("id") Integer id, @Param("commissionAmount") Double commissionAmount, @Param("predictCommission") Double predictCommission, @Param("predictCommission1") Double predictCommission1);

    int updateReviewState(@Param("id") Integer id, @Param("reviewState") String reviewState);

    int updateService(@Param("id")Integer id,@Param("serviceId") Integer serviceId, @Param("serviceAssessId") Integer serviceAssessId);

    int countServiceOrder(@Param("type") String type, @Param("excludeTypeList") List<String> excludeTypeList,
                          @Param("excludeState") String excludeState, @Param("stateList") List<String> stateList,
                          @Param("auditingState") String auditingState, @Param("reviewStateList") List<String> reviewStateList,
                          @Param("urgentState") String urgentState, @Param("startMaraApprovalDate") String startMaraApprovalDate,
                          @Param("endMaraApprovalDate") String endMaraApprovalDate,
                          @Param("startOfficialApprovalDate") String startOfficialApprovalDate,
                          @Param("endOfficialApprovalDate") String endOfficialApprovalDate,
                          @Param("startReadcommittedDate") String startReadcommittedDate,
                          @Param("endReadcommittedDate") String endReadcommittedDate,
                          @Param("startFinishDate") String startFinishDate,
                          @Param("endFinishDate") String endFinishDate,
                          @Param("regionIdList") List<Integer> regionIdList, @Param("userId") Integer userId,
                          @Param("userName") String userName, @Param("applicantName") String applicantName,
                          @Param("maraId") Integer maraId, @Param("adviserId") Integer adviserId,
                          @Param("officialId") Integer officialId, @Param("officialTagId") Integer officialTagId,
                          @Param("parentId") Integer parentId, @Param("applicantParentId") Integer applicantParentId,
                          @Param("isNotApproved") Boolean isNotApproved, @Param("serviceId") Integer serviceId,
                          @Param("servicePackageId") Integer servicePackageId,
                          @Param("schoolId") Integer schoolId, @Param("isPay") Boolean isPay, @Param("isSettle") Boolean isSettle);

    List<ServiceOrderDO> listServiceOrder(@Param("startGmtCreate") String startGmtCreate,
                                          @Param("endGmtCreate") String endGmtCreate,
                                          @Param("type") String type,
                                          @Param("excludeTypeList") List<String> excludeTypeList, @Param("excludeState") String excludeState,
                                          @Param("stateList") List<String> stateList, @Param("auditingState") String auditingState,
                                          @Param("reviewStateList") List<String> reviewStateList, @Param("urgentState") String urgentState,
                                          @Param("startMaraApprovalDate") String startMaraApprovalDate,
                                          @Param("endMaraApprovalDate") String endMaraApprovalDate,
                                          @Param("startOfficialApprovalDate") String startOfficialApprovalDate,
                                          @Param("endOfficialApprovalDate") String endOfficialApprovalDate,
                                          @Param("startReadcommittedDate") String startReadcommittedDate,
                                          @Param("endReadcommittedDate") String endReadcommittedDate,
                                          @Param("startFinishDate") String startFinishDate,
                                          @Param("endFinishDate") String endFinishDate,
                                          @Param("regionIdList") List<Integer> regionIdList, @Param("userId") Integer userId,
                                          @Param("userName") String userName, @Param("applicantName") String applicantName,
                                          @Param("maraId") Integer maraId, @Param("adviserId") Integer adviserId,
                                          @Param("officialId") Integer officialId, @Param("officialTagId") Integer officialTagId,
                                          @Param("parentId") Integer parentId, @Param("applicantParentId") Integer applicantParentId,
                                          @Param("isNotApproved") Boolean isNotApproved, @Param("serviceId") Integer serviceId,
                                          @Param("servicePackageId") Integer servicePackageId,
                                          @Param("schoolId") Integer schoolId, @Param("isPay") Boolean isPay, @Param("isSettle") Boolean isSettle,
                                          @Param("offset") int offset, @Param("rows") int rows, @Param("orderBy") String orderBy);

    List<ServiceOrderDO> listByParentId(@Param("parentId") Integer parentId);
    
    List<ServiceOrderDO> listByApplicantParentId(@Param("applicantParentId") Integer applicantParentId);

    ServiceOrderDO getServiceOrderById(int id);

    int countServiceOrderByApplicantId(int applicantId);

    int deleteServiceOrderById(int id);

    int finishServiceOrder(int id);

    int ReadcommittedServiceOrder(int id);

    List<EachRegionNumberDO> listServiceOrderGroupByForRegion(@Param("type") String type,
                                                              @Param("startOfficialApprovalDate") String startOfficialApprovalDate,
                                                              @Param("endOfficialApprovalDate") String endOfficialApprovalDate,@Param("regionId") Integer regionId);

    List<ServiceOrderDO> listByVerifyCode(@Param("verifyCode") String verifyCode);

    List<EachSubjectCountDO> eachSubjectCount(@Param("startOfficialApprovalDate") String startOfficialApprovalDate,
                                              @Param("endOfficialApprovalDate") String endOfficialApprovalDate);

    List<ServiceOrderDO> NotReviewedServiceOrder(@Param("officialId") Integer officialId,
                                                 @Param("thisMonth") boolean thisMonth);

    Integer caseCount(@Param("officialId") Integer officialId, @Param("days") String days,
                      @Param("state") String state);

    List<AdviserServiceCountDO> listServiceOrderToAnalysis(@Param("typeList") List<String> typeList, @Param("month") int month,
                                                           @Param("regionIdList") List<String> regionIdList);

    /**
     * 服务订单按照顾问地区,新学校库专业分组
     */
    List<EachRegionNumberDO> listOvstServiceOrderGroupByForRegion(@Param("startOfficialApprovalDate") String startOfficialApprovalDate,
                                                                  @Param("endOfficialApprovalDate") String endOfficialApprovalDate,@Param("regionId") Integer regionId);

    //查看服务订单对应佣金信息
    List<VisaDO> getCommissionOrderList(@Param("id") int id
    );

    //查看离职文案需交接订单
    List<ServiceOrderDO> OfficialHandoverServiceOrder(@Param("officialId") Integer officialId
    );

    int updateOffice(@Param("officialId") Integer officialId,
                     @Param("id") Integer id);
    //获取多个申请人列表
    List<ApplicantListDO> ApplicantListByServiceOrderId(@Param("serviceOrderId") int serviceOrderId);

    // 根据父订单获取子订单
    List<ServiceOrderDTO> getDeriveOrder(@Param("id") int id);

    @Select("SELECT * FROM b_service_order WHERE applicant_parent_id = #{id}")
    List<ServiceOrderDTO> getZiOrder(int id);

    @Select("SELECT service_id AS serviceId FROM b_service_order WHERE binding_order = #{id} AND applicant_parent_id = 0")
    List<Integer> listBybindingOrder(int id);

    List<ServiceOrderDO> getTmpServiceOrder(@Param("beforeFormat")String beforeFormat, @Param("nowDate")String nowDate);

    @Select("SELECT * from b_service_order WHERE id IN (1020948,1025098,1025510,1025557,1025749,1025629,1025887,1014941,1025854,1026057,1020894,1026758,1025455,1027139,\n" +
            "1026150,1025287,1014441,1026098,1026643,1025181,1025754,1026075,1020937,1026895,1027868,1027757,1009154,1027403,1026375,1028253,\n" +
            "1028448,1028239,1018910,1025390,1027464,1026926,1028231,1028237,1027917,1027679,1028693,1027853,1029123,1028450,1028627,1027260,\n" +
            "1026769,1029400,1029393,1027976,1029860,1029194,1028367,1030073,1030253,1030203,1029857,1031673,1029251,1027123,1027750,1026088,\n" +
            "1030597,1026052,1031446,1031670,1031719,1031720,1026804,1031802,1028289,1031516,1031913,1029500,1030939,1010214,1026665,1032694,\n" +
            "1032894,1027006,1018622,1033621,1033055,1030502,1031603,1031406,1033891,1033493,1016494,1032066,1033921,1028327,1029039,\n" +
            "1034212,1033636,1034570,1034937,1031592,1035002,1034791,1035464,1027760,1034585,1035920,1035240,1036199,1035417,1035950,1035296,\n" +
            "1031650,1036385,1030139,1036799,1035922,1037130,1037472,1037404,1032616,1036298,1036883,1037434,1033658,1037577,1036591,\n" +
            "1030621,1037581,1036667,1037524,1033865,1036492,1039056,1037833,1031054,1030208,1035070,1036547,1037676,1039284,1039460,1038343,\n" +
            "1036297,1039459,1039502,1039512,1038528,1039449,1038691,1029105,1039727,1038187,1036103,1037107,1039652,1039768,1039216,1039022,\n" +
            "1028473,1040336,1040449,1039286,1038755,1040586,1038349,1040039,1040038,1040746,1040678,1037854,1040446,1040926,1038058,1028064,\n" +
            "1040521,1038708,1041058,1039737,1039630,1041347,1041425,1040496,1041761,1039770,1041342,1030199,1041387,1041743,1041696,1043092,\n" +
            "1036166,1043338,1043230,1043063,1043533,1039519,1044072,1030500,1043412,1042432,1044153,1043494,1044682,1040897,1036326,1038648,\n" +
            "1038649,1044708,1027334,1032304,1045122,1039513,1044736,1043780,1036318,1039636,1038245,1038247,1018093,1018094,1024898,1024875,\n" +
            "1024141,1025010,1025056,1024520,1027590,1024761,1024925,1025592,1025461,1025133,1023635,1025134,1025555,1025143,1024137,1025374,1025136,\n" +
            "1025511,1025675,1025731,1025748,1021306,1026092,1039364,1039252,1025875,1026158,1026078,1025921,1025796,1026090,1025756,1025624,\n" +
            "1026326,1033134,1026230,1026174,1024634,1024889,1026126,1026529,1026627,1026285,1026666,1026841,1025738,1027413,1025730,1024780,\n" +
            "1024982,1025653,1025838,1026100,1026588,1026361,1018538,1026924,1025129,1027525,1025766,1024944,1026739,1024924,1027398,1024924,\n" +
            "1027618,1027504,1027962,1028039,1027831,1027934,1027933,1027817,1028284,1028474,1028206,1028207,1028422,1026532,1027138,1020117,\n" +
            "1027320,1027986,1029441,1027892,1028578,1028267,1028536,1028800,1029062,1027888,1028849,1028989,1027865,1025315,1029538,1029227,\n" +
            "1028670,1029015,1028696,1029651,1030148,1027029,1029582,1027368,1027368,1024991,1024968,1028446,1029166,1029330,1030614,1030562,\n" +
            "1021438,1024737,1020782,1026742,1026741,1027133,1028466,1023989,1028332,1028532,1028447,1027017,1030823,1030830,1031229,1031436,\n" +
            "1026397,1031091,1029588,1031438,1031523,1031536,1031527,1031593,1031590,1031594,1031597,1031616,1031618,1031598,1031550,1031489,\n" +
            "1031551,1031648,1030854,1024682,1030457,1030275,1030462,1032907,1023515,1025552,1026844,1026727,1034607,1028451,1030569,1027236,\n" +
            "1028560,1030028,1030066,1029091,1030504,1029380,1030750,1030893,1031148,1031053,1031340,1031341,1031428,1031440,1031528,1031826,\n" +
            "1030920,1031333,1031334,1032269,1031917,1032343,1032620,1032336,1030703,1031882,1031881,1028704,1030612,1029481,1031769,1031325,\n" +
            "1025094,1032921,1032922,1032924,1033111,1025346,1028305,1033245,1032019,1024887,1023906,1030631,1031915,1032411,1031145,1032908,\n" +
            "1033306,1021982,1030535,1030772,1030949,1031139,1031111,1032618,1031952,1032210,1032153,1030922,1031930,1033115,1033638,1033211,\n" +
            "1033365,1033459,1033301,1033423,1033591,1031931,1033313,1034124,1034146,1026807,1032582,1033316,1032756,1032757,1033466,1028646,\n" +
            "1028408,1026650,1030955,1029509,1031400,1031635,1030745,1025995,1032501,1034452,1034767,1032507,1033585,1033980,1034949,1030220,\n" +
            "1031502,1028106,1032809,1033770,1033509,1033330,1034645,1034771,1035181,1023815,1034779,1033961,1035456,1028242,1033604,1033781,\n" +
            "1034629,1035277,1036003,1035343,1036228,1020700,1032996,1026545,1031922,1031691,1033811,1034021,1033880,1035592,1036484,1036523,\n" +
            "1033205,1033206,1033207,1035081,1036619,1025045,1025223,1016372,1005527,1033643,1034834,1035398,1034591,1035520,1035955,1035218,\n" +
            "1038385,1026342,1032709,1029792,1035689,1036689,1036690,1036997,1037734,1020843,1039615,1029038,1030008,1029903,1034473,1033843,\n" +
            "1034333,1035128,1035170,1035150,1033405,1034722,1036205,1035253,1033116,1035171,1036542,1036268,1036477,1036983,1036990,1037859,\n" +
            "1038770,1037885,1038271,1038550,1038548,1038108,1039151,1035125,1035308,1035300,1034639,1036012,1034763,1039152,1039563,1040336,\n" +
            "1031392,1035418,1037834,1036754,1026748,1039875,1040468,1035301,1036487,1040909,1040977,1039289,1041044,1035146,1035312,1031058,\n" +
            "1034577,1037312,1036483,1037852,1038134,1038135,1038647,1040214,1040817,1040620,1041495,1041792,1027756,1041412,1036974,1035065,\n" +
            "1040470,1040471,1041087,1032394,1039640,1042451,1041075,1025176,1035678,1038050,1039129,1039283,1036207,1042248,1042751,1030953,\n" +
            "1040580,1039551,1017790,1025069,1035765,1036786,1040979,1043336,1012085,1017789,1032962,1036363,1039644,1037741,1041408,1035676,\n" +
            "1041629,1041375,1043311,1043499,1043781,1043779,1044455,1031532,1033613,1032246,1042516,1043774,1044053,1044183,1044186,1044716,\n" +
            "1044676,1040841)")
    List<ServiceOrderDO> linshi();

}