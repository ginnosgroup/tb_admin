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

    @Select("SELECT * FROM b_commission_order WHERE id IN (1000079,1000080,1000081,1000082,1001483,1001484,1001485,1001486,1001487,1001488,\n" +
            "1001489,1001490,1001491,1001492,1001493,1001494,1001495,1001499,\n" +
            "1001500,1001501,1001502,1001503,1001504,\n" +
            "1001505,\n" +
            "1001506,\n" +
            "1001512,\n" +
            "1001513,\n" +
            "1001514,\n" +
            "1001515,\n" +
            "1001516,\n" +
            "1001517,\n" +
            "1001525,\n" +
            "1001528,\n" +
            "1001530,\n" +
            "1001532,\n" +
            "1001534,\n" +
            "1001536,\n" +
            "1001538,\n" +
            "1001540,\n" +
            "1001543,\n" +
            "1001544,\n" +
            "1001545,\n" +
            "1001546,\n" +
            "1001551,\n" +
            "1001552,\n" +
            "1001553,\n" +
            "1001555,\n" +
            "1001557,\n" +
            "1001558,\n" +
            "1001560,\n" +
            "1001562,\n" +
            "1001563,\n" +
            "1001564,\n" +
            "1001565,\n" +
            "1001571,\n" +
            "1001573,\n" +
            "1001574,\n" +
            "1001576,\n" +
            "1001577,\n" +
            "1001578,\n" +
            "1001579,\n" +
            "1001596,\n" +
            "1001597,\n" +
            "1001598,\n" +
            "1001599,\n" +
            "1001611,\n" +
            "1007533,\n" +
            "1007534,\n" +
            "1007535,\n" +
            "1007536,\n" +
            "1007537,\n" +
            "1007538,\n" +
            "1007863,\n" +
            "1007865,\n" +
            "1007866,\n" +
            "1007867,\n" +
            "1007868,\n" +
            "1007869,\n" +
            "1007870,\n" +
            "1007871,\n" +
            "1007872,\n" +
            "1008046,\n" +
            "1008048,\n" +
            "1008061,\n" +
            "1008062,\n" +
            "1008063,\n" +
            "1008064,\n" +
            "1008136,\n" +
            "1008137,\n" +
            "1008138,\n" +
            "1008139,\n" +
            "1008140,\n" +
            "1008141,\n" +
            "1008142,\n" +
            "1008143,\n" +
            "1008144,\n" +
            "1008222,\n" +
            "1008223,\n" +
            "1008224,\n" +
            "1008225,\n" +
            "1008226,\n" +
            "1008227,\n" +
            "1008228,\n" +
            "1008229,\n" +
            "1008230,\n" +
            "1008237,\n" +
            "1008238,\n" +
            "1008239,\n" +
            "1008240,\n" +
            "1008241,\n" +
            "1008242,\n" +
            "1008243,\n" +
            "1008244,\n" +
            "1008363,\n" +
            "1008414,\n" +
            "1008415,\n" +
            "1008416,\n" +
            "1008417,\n" +
            "1008464,\n" +
            "1008465,\n" +
            "1008466,\n" +
            "1008467,\n" +
            "1008557,\n" +
            "1008559,\n" +
            "1008582,\n" +
            "1008583,\n" +
            "1008584,\n" +
            "1008585,\n" +
            "1008618,\n" +
            "1008619,\n" +
            "1008620,\n" +
            "1008621,\n" +
            "1008622,\n" +
            "1008623,\n" +
            "1008625,\n" +
            "1008632,\n" +
            "1008633,\n" +
            "1008634,\n" +
            "1008635,\n" +
            "1008636,\n" +
            "1008637,\n" +
            "1008638,\n" +
            "1008639,\n" +
            "1008640,\n" +
            "1008810,\n" +
            "1008811,\n" +
            "1008812,\n" +
            "1008813,\n" +
            "1008814,\n" +
            "1008815,\n" +
            "1008816,\n" +
            "1008817,\n" +
            "1008818,\n" +
            "1008837,\n" +
            "1008838,\n" +
            "1008839,\n" +
            "1008840,\n" +
            "1008846,\n" +
            "1008847,\n" +
            "1008848,\n" +
            "1008849,\n" +
            "1008850,\n" +
            "1008851,\n" +
            "1008852,\n" +
            "1008853,\n" +
            "1008933,\n" +
            "1008934,\n" +
            "1008935,\n" +
            "1008936,\n" +
            "1009094,\n" +
            "1009095,\n" +
            "1009100,\n" +
            "1009101,\n" +
            "1009102,\n" +
            "1009103,\n" +
            "1009104,\n" +
            "1009105,\n" +
            "1009106,\n" +
            "1009107,\n" +
            "1009108,\n" +
            "1009109,\n" +
            "1009117,\n" +
            "1009118,\n" +
            "1009119,\n" +
            "1009120,\n" +
            "1009121,\n" +
            "1009122,\n" +
            "1009123,\n" +
            "1009124,\n" +
            "1009125,\n" +
            "1009126,\n" +
            "1009127,\n" +
            "1009128,\n" +
            "1009129,\n" +
            "1009130,\n" +
            "1009131,\n" +
            "1009132,\n" +
            "1009133,\n" +
            "1009134,\n" +
            "1009135,\n" +
            "1009136,\n" +
            "1009137,\n" +
            "1009138,\n" +
            "1009193,\n" +
            "1009194,\n" +
            "1009197,\n" +
            "1009198,\n" +
            "1009199,\n" +
            "1009200,\n" +
            "1009201,\n" +
            "1009202,\n" +
            "1009203,\n" +
            "1009204,\n" +
            "1009217,\n" +
            "1009218,\n" +
            "1009219,\n" +
            "1009220,\n" +
            "1009221,\n" +
            "1009248,\n" +
            "1009249,\n" +
            "1009250,\n" +
            "1009251,\n" +
            "1009252,\n" +
            "1009260,\n" +
            "1009261,\n" +
            "1009262,\n" +
            "1009263,\n" +
            "1009264,\n" +
            "1009265,\n" +
            "1009266,\n" +
            "1009267,\n" +
            "1009268,\n" +
            "1009269,\n" +
            "1009270,\n" +
            "1009271,\n" +
            "1009272,\n" +
            "1009273,\n" +
            "1009294,\n" +
            "1009295,\n" +
            "1009296,\n" +
            "1009297,\n" +
            "1009298,\n" +
            "1009299,\n" +
            "1009300,\n" +
            "1009301,\n" +
            "1009374,\n" +
            "1009375,\n" +
            "1009376,\n" +
            "1009377,\n" +
            "1009378,\n" +
            "1009379,\n" +
            "1009380,\n" +
            "1009381,\n" +
            "1009415,\n" +
            "1009416,\n" +
            "1009417,\n" +
            "1009418,\n" +
            "1009431,\n" +
            "1009432,\n" +
            "1009433,\n" +
            "1009478,\n" +
            "1009479,\n" +
            "1009480,\n" +
            "1009481,\n" +
            "1009484,\n" +
            "1009485,\n" +
            "1009574,\n" +
            "1009575,\n" +
            "1009576,\n" +
            "1009577,\n" +
            "1009578,\n" +
            "1009579,\n" +
            "1009580,\n" +
            "1009581,\n" +
            "1009582,\n" +
            "1009583,\n" +
            "1009586,\n" +
            "1009607,\n" +
            "1009608,\n" +
            "1009710,\n" +
            "1009711,\n" +
            "1009712,\n" +
            "1009713,\n" +
            "1009714,\n" +
            "1009715,\n" +
            "1009722,\n" +
            "1009723,\n" +
            "1009726,\n" +
            "1009729,\n" +
            "1009730,\n" +
            "1009731,\n" +
            "1009732,\n" +
            "1009733,\n" +
            "1009734,\n" +
            "1009735,\n" +
            "1009736,\n" +
            "1009737,\n" +
            "1009738,\n" +
            "1009740,\n" +
            "1009800,\n" +
            "1009898,\n" +
            "1009899,\n" +
            "1009900,\n" +
            "1009901,\n" +
            "1009902,\n" +
            "1009903,\n" +
            "1009904,\n" +
            "1009905,\n" +
            "1009910,\n" +
            "1009911,\n" +
            "1009912,\n" +
            "1009913,\n" +
            "1009914,\n" +
            "1009915,\n" +
            "1009916,\n" +
            "1009917,\n" +
            "1009918,\n" +
            "1009919,\n" +
            "1009920,\n" +
            "1009921,\n" +
            "1009922,\n" +
            "1009923,\n" +
            "1009924,\n" +
            "1009962,\n" +
            "1009963,\n" +
            "1009964,\n" +
            "1009965,\n" +
            "1010062,\n" +
            "1010063,\n" +
            "1010064,\n" +
            "1010065,\n" +
            "1010066,\n" +
            "1010067,\n" +
            "1010068,\n" +
            "1010069,\n" +
            "1010070,\n" +
            "1010071,\n" +
            "1010072,\n" +
            "1010073,\n" +
            "1010075,\n" +
            "1010078,\n" +
            "1010079,\n" +
            "1010080,\n" +
            "1010081,\n" +
            "1010082,\n" +
            "1010083,\n" +
            "1010084,\n" +
            "1010085,\n" +
            "1010086,\n" +
            "1010087,\n" +
            "1010088,\n" +
            "1010089,\n" +
            "1010090,\n" +
            "1010091,\n" +
            "1010092,\n" +
            "1010093,\n" +
            "1010094,\n" +
            "1010095,\n" +
            "1010096,\n" +
            "1010097,\n" +
            "1010179,\n" +
            "1010184,\n" +
            "1010185,\n" +
            "1010201,\n" +
            "1010202,\n" +
            "1010203,\n" +
            "1010204,\n" +
            "1010205,\n" +
            "1010208,\n" +
            "1010209,\n" +
            "1010210,\n" +
            "1010211,\n" +
            "1010212,\n" +
            "1010261,\n" +
            "1010262,\n" +
            "1010263,\n" +
            "1010264,\n" +
            "1010265,\n" +
            "1010266,\n" +
            "1010267,\n" +
            "1010268,\n" +
            "1010269,\n" +
            "1010270,\n" +
            "1010273,\n" +
            "1010301,\n" +
            "1010302,\n" +
            "1010303,\n" +
            "1010308,\n" +
            "1010323,\n" +
            "1010324,\n" +
            "1010325,\n" +
            "1010326,\n" +
            "1010408,\n" +
            "1010409,\n" +
            "1010410,\n" +
            "1010411,\n" +
            "1010412,\n" +
            "1010413,\n" +
            "1010414,\n" +
            "1010415,\n" +
            "1010416,\n" +
            "1010417,\n" +
            "1010418,\n" +
            "1010419,\n" +
            "1010420,\n" +
            "1010421,\n" +
            "1010422,\n" +
            "1010423,\n" +
            "1010424,\n" +
            "1010425,\n" +
            "1010426,\n" +
            "1010427,\n" +
            "1010467,\n" +
            "1010468,\n" +
            "1010554,\n" +
            "1010562,\n" +
            "1010649,\n" +
            "1010650,\n" +
            "1010651,\n" +
            "1010652,\n" +
            "1010653,\n" +
            "1010654,\n" +
            "1010655,\n" +
            "1010660,\n" +
            "1010661,\n" +
            "1010662,\n" +
            "1010663,\n" +
            "1010664,\n" +
            "1010665,\n" +
            "1010666,\n" +
            "1010674,\n" +
            "1010675,\n" +
            "1010676,\n" +
            "1010677,\n" +
            "1010678,\n" +
            "1010679,\n" +
            "1010680,\n" +
            "1010681,\n" +
            "1010682,\n" +
            "1010683,\n" +
            "1010684,\n" +
            "1010685,\n" +
            "1010686,\n" +
            "1010687,\n" +
            "1010688,\n" +
            "1010718,\n" +
            "1010782,\n" +
            "1010783,\n" +
            "1010784,\n" +
            "1010785,\n" +
            "1010786,\n" +
            "1010787,\n" +
            "1010788,\n" +
            "1010789,\n" +
            "1010840,\n" +
            "1010841,\n" +
            "1010842,\n" +
            "1010862,\n" +
            "1010863,\n" +
            "1010864,\n" +
            "1010865,\n" +
            "1010866,\n" +
            "1010867,\n" +
            "1010868,\n" +
            "1010869,\n" +
            "1010870,\n" +
            "1010871,\n" +
            "1010872,\n" +
            "1010873,\n" +
            "1010874,\n" +
            "1010875,\n" +
            "1010881,\n" +
            "1010882,\n" +
            "1010890,\n" +
            "1010891,\n" +
            "1010892,\n" +
            "1010893,\n" +
            "1010894,\n" +
            "1010895,\n" +
            "1010896,\n" +
            "1010897,\n" +
            "1010940,\n" +
            "1010965,\n" +
            "1010974,\n" +
            "1011050,\n" +
            "1011051,\n" +
            "1011052,\n" +
            "1011053,\n" +
            "1011054,\n" +
            "1011055,\n" +
            "1011062,\n" +
            "1011063,\n" +
            "1011064,\n" +
            "1011065,\n" +
            "1011099,\n" +
            "1011100,\n" +
            "1011101,\n" +
            "1011102,\n" +
            "1011103,\n" +
            "1011104,\n" +
            "1011107,\n" +
            "1011108,\n" +
            "1011109,\n" +
            "1011110,\n" +
            "1011111,\n" +
            "1011112,\n" +
            "1011113,\n" +
            "1011114,\n" +
            "1011116,\n" +
            "1011270,\n" +
            "1011402,\n" +
            "1011421,\n" +
            "1011422,\n" +
            "1011423,\n" +
            "1011483,\n" +
            "1011484,\n" +
            "1011485,\n" +
            "1011486,\n" +
            "1011487,\n" +
            "1011488,\n" +
            "1011489,\n" +
            "1011490,\n" +
            "1011491,\n" +
            "1011492,\n" +
            "1011511,\n" +
            "1011536,\n" +
            "1011559,\n" +
            "1011601,\n" +
            "1011602,\n" +
            "1011654,\n" +
            "1011655,\n" +
            "1011656,\n" +
            "1011657,\n" +
            "1011658,\n" +
            "1011659,\n" +
            "1011660,\n" +
            "1011661,\n" +
            "1011662,\n" +
            "1011721,\n" +
            "1011762,\n" +
            "1012058,\n" +
            "1012060,\n" +
            "1012099,\n" +
            "1012100,\n" +
            "1012101,\n" +
            "1012102,\n" +
            "1012103,\n" +
            "1012104,\n" +
            "1012118,\n" +
            "1012119,\n" +
            "1012171,\n" +
            "1012175,\n" +
            "1012176,\n" +
            "1012206,\n" +
            "1012207,\n" +
            "1012208,\n" +
            "1012257,\n" +
            "1012258,\n" +
            "1012312,\n" +
            "1012313,\n" +
            "1012314,\n" +
            "1012315,\n" +
            "1012335,\n" +
            "1012336,\n" +
            "1012357,\n" +
            "1012415,\n" +
            "1012416,\n" +
            "1012417,\n" +
            "1012418,\n" +
            "1012419,\n" +
            "1012435,\n" +
            "1012436,\n" +
            "1012485,\n" +
            "1012486,\n" +
            "1012487,\n" +
            "1012488,\n" +
            "1012557) AND adviser_id != 1000223")
    List<ServiceOrderDO> linshi22();

}