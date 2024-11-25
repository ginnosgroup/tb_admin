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

//    @Select("SELECT * from b_service_order WHERE adviser_id = 1000223 AND id NOT IN (1046351,1046518,1046519,1046527,1046597,1046617,1046635,1046636,1046649,1046699,1046784,1046844,1046866,1046867,\n" +
//            "1046868,1046869,1046913,1046914)")
    @Select("SELECT id,adviser_id,user_id FROM b_service_order WHERE id IN (1034255,1034254,1034252,1038344,1046534,1046531,1028123,1038354,1034258,1034257,1034256,1036334,1036333,1036332,1040427,1036331,1040424,1021988,1021987,1044513,1044512,1038399,1038395,1019961,1019960,1019959,1019958,1032243,1028147,1032242,1028146,1028145,1040432,1028174,1038413,1034317,1038412,1034316,1038411,1038410,1036360,1042498,1040472,1030254,1020010,1026152,1036390,1028194,1028193,1028192,1028215,1028214,1026165,1028212,1020017,1028235,1028234,1028228,1034371,1032351,1032350,1032349,1032348,1032347,1032346,1022104,1022103,1038484,1038483,1028243,1038481,1020049,1020048,1034415,1034414,1028268,1038502,1038501,1026239,1001663,1040574,1026238,1020106,1038537,1034439,1001666,1036480,1034462,1032410,1030362,1030361,1032408,1044694,1036501,1028307,1034472,1034471,1036516,1038562,1028322,1036513,1028321,1034495,1024253,1024252,1022197,1022196,1022195,1022194,1020146,1046768,1038601,1042696,1042695,1040646,1038598,1028355,1028354,1034497,1034496,1046815,1028383,1030429,1030428,1024284,1030427,1024283,1024282,1024281,1024280,1040663,1024279,1028368,1042735,1026351,1042734,1042733,1036589,1040684,1036588,1046820,1046819,1028387,1046818,1046817,1046816,1024318,1038650,1026360,1026359,1026358,1026357,1026356,1026355,1026354,1026352,1042767,1042766,1028430,1022286,1042765,1040717,1028429,1022285,1028428,1022284,1042763,1022283,1042762,1042761,1018184,1018183,1018182,1018181,1018180,1044803,1018179,1036634,1036633,1036632,1036631,1036630,1036629,1044818,1040721,1036625,1042768,1018223,1018222,1042797,1042796,1018218,1018217,1018216,1026405,1026404,1036642,1036641,1038688,1036640,1034592,1032572,1032571,1030523,1032570,1046905,1018233,1018232,1018231,1018230,1018229,1040756,1018228,1018224,1034629,1028484,1046942,1032605,1042843,1042842,1030550,1042837,1046928,1034671,1020330,1020329,1020328,1042853,1032608,1022399,1034672,1040845,1026527,1024479,1026526,1040859,1040858,1038803,1038802,1022447,1028589,1024490,1022440,1022439,1022438,1030629,1022437,1022436,1024482,1020386,1020385,1026528,1030654,1040890,1042935,1042934,1040883,1040882,1026575,1042958,1034766,1026574,1042957,1028616,1026564,1040923,1026577,1026576,1040943,1030703,1030702,1040941,1030701,1040940,1028642,1028641,1028640,1028667,1028666,1038905,1040944,1030704,1040975,1036879,1018447,1018442,1036868,1028676,1028675,1040991,1022556,1028697,1036881,1028689,1040976,1028688,1022573,1018477,1018476,1018475,1041000,1047141,1038944,1028734,1043067,1032827,1032826,1028727,1045135,1036943,1045134,1036942,1045133,1036941,1036939,1041034,1036938,1020554,1020553,1020552,1036935,1028737,1024667,1028761,1024665,1028760,1028759,1034898,1036944,1032848,1026704,1034920,1034919,1034918,1034917,1039036,1032887,1032886,1039028,1020596,1026739,1028813,1030856,1039045,1034947,1039041,1034975,1034974,1034973,1018588,1018587,1018586,1032919,1032918,1024723,1024722,1041135,1002223,1039083,1030890,1030889,1030887,1034978,1034976,1000186,1039097,1018615,1047286,1039119,1032975,1000207,1039118,1032974,1000206,1039117,1039116,1039115,1043207,1030917,1039108,1037060,1032979,1032978,1030954,1041193,1041192,1018660,1018659,1041186,1018658,1041185,1026879,1039166,1022778,1026873,1026872,1026871,1024823,1024822,1024821,1022772,1024819,1039172,1039171,1037122,1037121,1026881,1018689,1026880,1028959,1031006,1028958,1024862,1031004,1026927,1022822,1022821,1022820,1022819,1022818,1037153,1037152,1022816,1043321,1028984,1028983,1028982,1024886,1028981,1035124,1031028,1039218,1037170,1029004,1026952,1022856,1026951,1026950,1031071,1024927,1031070,1024926,1022876,1022875,1041304,1024920,1045396,1018790,1031077,1018789,1031076,1018788,1031075,1026979,1031072,1037247,1037246,1037245,1037244,1037243,1037242,1037241,1037240,1037234,1029041,1029040,1029070,1045450,1045446,1033156,1041344,1029087,1029086,1039325,1029085,1039324,1029084,1045461,1045460,1045459,1033171,1027025,1027053,1027052,1027051,1018859,1027050,1018858,1027049,1018857,1043452,1043451,1035256,1033208,1035255,1035254,1031156,1043441,1025009,1043440,1018895,1041417,1041416,1041415,1043462,1041411,1041410,1041409,1023001,1031191,1018897,1041424,1018896,1002543,1018925,1002541,1018924,1002540,1002539,1039402,1035306,1029162,1002538,1045545,1035305,1031209,1029161,1035304,1029160,1045543,1045542,1045541,1045540,1002550,1045557,1039413,1002548,1002547,1002546,1002545,1002544,1033295,1033294,1033293,1018956,1018955,1018954,1018953,1027144,1018952,1027142,1027140,1045569,1031259,1031256,1031255,1031254,1018964,1029203,1033296,1029231,1039470,1029230,1021031,1021030,1021029,1021028,1035363,1021027,1037410,1021026,1021025,1035391,1019007,1019006,1019005,1019004,1019003,1019002,1033335,1027185,1031280,1029232,1027184,1035404,1031308,1031307,1031306,1019010,1019009,1035392,1019008,1023121,1037474,1035451,1029305,1027256,1027255,1033421,1027276,1037509,1019077,1019076,1043675,1043674,1031386,1035481,1031385,1043672,1031384,1037527,1031383,1043670,1031382,1043669,1043668,1031378,1031377,1031376,1008889,1043724,1043723,1037578,1027337,1027336,1045766,1045765,1045764,1045763,1045762,1045761,1043739,1043730,1035536,1031466,1000746,1035561,1035560,1019196,1019195,1019194,1031478,1043791,1043790,1027406,1027405,1027404,1027392,1043801,1029465,1033560,1033582,1031534,1027438,1045862,1027430,1045861,1043813,1043812,1043811,1043810,1019263,1041789,1039736,1021304,1039735,1039734,1021302,1027443,1027442,1039729,1027441,1033584,1027440,1029516,1029515,1041801,1041798,1031554,1019265,1019264,1002903,1039764,1027500,1029547,1019302,1019301,1037730,1037729,1019296,1029563,1029562,1035703,1035701,1031602,1021361,1021360,1037768,1039814,1039813,1039812,1029572,1029571,1029570,1043905,1045983,1033695,1031647,1025503,1045982,1033694,1045981,1025501,1025500,1025499,1023451,1023450,1031663,1031662,1031661,1037804,1031660,1031659,1031658,1031657,1031656,1031655,1031654,1031653,1037795,1043938,1045985,1025505,1045984,1033696,1025504,1033727,1033726,1033725,1033724,1033723,1033722,1033721,1033719,1033718,1033717,1031667,1031666,1031665,1031664,1033742,1043977,1043976,1031683,1025539,1031682,1025538,1033729,1025537,1033728,1025536,1046047,1046043,1035801,1035800,1035799,1035798,1035797,1039892,1023507,1039890,1023506,1039889,1031696,1033769,1033768,1033767,1033766,1033765,1033764,1044025,1025592,1039922,1044047,1037903,1031759,1044040,1035846,1035845,1035844,1037918,1046109,1037910,1037909,1019476,1019475,1019474,1044049,1029713,1037904,1027689,1042024,1039976,1027688,1027687,1044070,1001056,1044089,1042040,1037941,1025667,1025685,1029777,1037998,1031852,1029820,1025723,1025722,1019598,1044173,1019597,1035980,1019596,1040074,1021640,1021639,1019591,1029828,1038038,1044179,1044178,1044177,1038057,1025762,1025761,1038079,1038078,1038077,1038076,1040116,1019662,1019661,1021706,1021705,1044229,1044227,1046274,1038080,1040152,1023768,1042199,1040151,1023767,1042198,1023766,1042197,1023765,1046292,1031956,1029908,1031955,1031954,1044287,1044286,1034046,1027902,1040187,1034043,1034042,1034041,1025848,1031991,1031989,1040205,1044294,1032028,1032026,1032025,1032024,1032023,1019754,1019753,1019752,1034087,1034086,1034111,1038204,1038203,1038202,1036152,1038221,1044362,1044360,1034113,1034112,1046430,1023897,1036181,1036180,1040303,1044397,1044413,1036219,1036218,1046457,1036217,1036216,1036215,1046454,1036214,1040304,1046478,1044421,1042396,1026004,1044432,1023983,1023982,1040365,1028077,1040364,1038322,1023984) AND adviser_id != 1000223")
    List<ServiceOrderDO> linshi22();

}