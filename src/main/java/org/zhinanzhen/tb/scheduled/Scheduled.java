package org.zhinanzhen.tb.scheduled;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonParser;
import com.ikasoa.core.utils.ObjectUtil;
import com.ikasoa.core.utils.StringUtil;

import com.lark.oapi.Client;
import com.lark.oapi.core.request.RequestOptions;
import com.lark.oapi.core.utils.Jsons;
import com.lark.oapi.service.bitable.v1.model.*;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.zhinanzhen.b.dao.*;
import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.WXWorkService;
import org.zhinanzhen.b.service.impl.VerifyServiceImpl;
import org.zhinanzhen.b.service.pojo.DataDTO;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.tb.dao.AdviserDAO;
import org.zhinanzhen.tb.dao.RegionDAO;
import org.zhinanzhen.tb.dao.UserDAO;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
import org.zhinanzhen.tb.dao.pojo.RegionDO;
import org.zhinanzhen.tb.dao.pojo.UserDO;
import org.zhinanzhen.tb.service.AdviserService;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.UserService;
import org.zhinanzhen.tb.service.pojo.AdviserDTO;
import org.zhinanzhen.tb.service.pojo.UserDTO;
import org.zhinanzhen.tb.utils.*;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: bsz
 * Date: 2020/09/27 16:48
 * Description:
 * Version: V1.0
 */
/*
boss邮件组：paul@zhinanzhen.org;elvin@zhinanzhen.org;jiaheng.xu@zhinanzhen.org
Sydney：juntao@zhinanzhen.org;jiaheng.xu@zhinanzhen.org
Canberra ：juntao@zhinanzhen.org;jiaheng.xu@zhinanzhen.org
sydeny2:kevin@zhinanzhen.org;jiaheng.xu@zhinanzhen.org
Melbourne:lisa@zhinanzhen.org;jiaheng.xu@zhinanzhen.org
Brisbane：vicky@zhinanzhen.org;jiaheng.xu@zhinanzhen.org
Adelaide:caroline.wang@zhinanzhen.org;jiaheng.xu@zhinanzhen.org
Hobart:lorrain.pan@zhinanzhen.org;jiaheng.xu@zhinanzhen.org
 */
@Component
@EnableScheduling
@Slf4j
@Lazy(false)
public class Scheduled {

    private static final Logger LOG = LoggerFactory.getLogger(Scheduled.class);

    private final Executor executor;

    @Autowired
    Data data;

    @Autowired
    private VerifyDao verifyDao;

    @Autowired
    private VisaDAO visaDAO;

    @Autowired
    private CommissionOrderDAO commissionOrderDAO;

    @Autowired
    private ServiceOrderService serviceOrderService;

    @Autowired
    private SchoolDAO schoolDAO;

    @Autowired
    private AdviserDAO adviserDAO;

    @Autowired
    private AdviserService adviserService;

    @Autowired
    private WXWorkService wxWorkService;

    @Autowired
    private UserService userService;

    @Autowired
    MailRemindDAO mailRemindDAO;
    
    @Autowired
	private EverydayExchangeRateDAO everydayExchangeRateDao;
    
    @Autowired
    private SchoolInstitutionDAO schoolInstitutionDao;

    @Autowired
    private ServiceOrderDAO serviceOrderDAO;

    @Autowired
    private OfficialDAO officialDao;

    @Autowired
    private RegionDAO regionDAO;

    @Autowired
    private ServiceDAO serviceDAO;

    @Autowired
    private ServicePackageDAO servicePackageDAO;

    @Autowired
    private UserDAO userDao;

    @Autowired
    private ServiceAssessDao serviceAssessDao;

    @Autowired
    private SchoolInstitutionDAO schoolInstitutionDAO;


    private Calendar calendar ;

    private StringBuilder content = null;

    private  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private SimpleDateFormat sdfT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Value("${feishu.ACCESSKEYID}")
    private String ACCESS_KEY_ID;

    @Value("${feishu.ACCESSKEYSECRET}")
    private String ACCESS_KEY_SECRET;

    @Value("${feishu.APPTOKEN}")
    private String APP_TOKEN;

    @Value("${feishu.EXCELIDVISA}")
    private String EXCEL_ID_VISA;

    @Value("${feishu.EXCELIDOVST}")
    private String EXCEL_ID_OVST;

    @Autowired
    public Scheduled(Executor executor) {
        this.executor = executor;
    }

    //String startDate = "2020-09-01";
    //String lastSaturdayDate = "2020-09-19";
    //String endDate = "2020-09-25";
    
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 8 ? * MON")
	public void everyWeekMonday() {
		executor.execute(() -> {
            List<SchoolInstitutionCountDO> schoolWeekList = schoolInstitutionDao.countSchoolWeek(10);
            List<SchoolInstitutionCountDO> _schoolWeekList = schoolInstitutionDao.countSchoolWeek(999);
            List<SchoolInstitutionCountDO> courseWeekList = schoolInstitutionDao.countCourseWeek();
            String schoolWeekStr = "";
            if (schoolWeekList.size() > 0) {
                schoolWeekStr = "Top10申请学校列表:\n";
                for (int i = 0; i < schoolWeekList.size(); i++) {
                    SchoolInstitutionCountDO schoolInstitutionCountDo = schoolWeekList.get(i);
                    schoolWeekStr += StringUtil.merge(i + 1, ".", schoolInstitutionCountDo.getName(), ":",
                            schoolInstitutionCountDo.getCount(), "\n");
                }
            }
            String courseWeekStr = "";
            if (courseWeekList.size() > 0) {
                courseWeekStr = "Top10申请专业列表:\n";
                for (int i = 0; i < courseWeekList.size(); i++) {
                    SchoolInstitutionCountDO schoolInstitutionCountDo = courseWeekList.get(i);
                    courseWeekStr += StringUtil.merge(i + 1, ".", schoolInstitutionCountDo.getCourseName(), "(",
                            schoolInstitutionCountDo.getName(), "):", schoolInstitutionCountDo.getCount(), "\n");
                }
            }
            LOG.info(StringUtil.merge("_schoolWeekList.size=", _schoolWeekList.size()));
            if (_schoolWeekList.size() > 0)
                WXWorkAPI.sendWecomRotMsg(StringUtil.merge("各位顾问：上周新增学校服务订单", _schoolWeekList.size(), "个。仅供参考。\n\n",
                        schoolWeekStr, "\n\n", courseWeekStr));
        });
	}

    //本月1号-本周五   上周六-本周五
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 9 ?  *  SAT")
    public void everyWeek(){
        executor.execute(() -> {


            String startDate = DateClass.thisMonthFirstDay(Calendar.getInstance());   //本月一号的时间
            String lastSaturdayDate = DateClass.lastSaturday(Calendar.getInstance()); //上周六
            String endDate = DateClass.thisMonthFriday(Calendar.getInstance());            //也就是当前时间


            //月初截止到本周五的
            List<DataDTO> areaTodayDataList = data.dataReport(startDate,endDate,"A",null); //   A  全area地区的area数据   数据
            List<DataDTO> dataTOdayDTOList = data.dataReport(startDate,endDate,"R",null); //  R 全area顾问倒序排名的数据  顾问

            //上周六截止到到本周五的数据
            List<DataDTO> lastWeekAreaDataList = data.dataReport(lastSaturdayDate,endDate,"A",null); //   A  地区的数据

            //所有顾问按照地区分类（已经排好名次）
            List<List<DataDTO>> regionList = RegionClassification.classification(dataTOdayDTOList);//  按照地区将顾问进行分组(顾问排名是1-当前日期)

            {
                content = EmailModel.start();

                content.append("全澳截止到本周五："+startDate+"  至  "+endDate+"号 Data Report");
                content.append(EmailModel.areaModelHaveDate(areaTodayDataList));//area各地区的数据

                content.append("全澳本周数据:"+lastSaturdayDate+"  至  "+endDate+"号 Data Report");
                content.append(EmailModel.areaModelNoDate(lastWeekAreaDataList));

                content.append("全澳"+startDate+"  至  "+endDate+"号的顾问业绩排名");
                content.append(EmailModel.rankModelHave(dataTOdayDTOList)); //顾问业绩排名

                content.append(EmailModel.end());
                SendEmailUtil.send("paul@zhinanzhen.org","全澳的 Data Report",content.toString());
                SendEmailUtil.send("elvin@zhinanzhen.org","全澳的 Data Report",content.toString());
//                SendEmailUtil.send("jiaheng.xu@zhinanzhen.org","全澳的 Data Report",content.toString());
                //SendEmailUtil.send("815124560@qq.com","全澳的 Data Report", content.toString());

            }

            {
                List<ServiceOrderDTO> serviceOrderDTOS = null;
                try {
                    serviceOrderDTOS = serviceOrderService.listServiceOrder(null, null, null, null, null, null, null, null,
                            null, lastSaturdayDate, endDate, null, null, null, null, null, null, null, null, null, null, null, null, null, 0,
                            0, false, 0, 9999, null, null, null, null, false, null, null, null, null, null, null, null);
                    for (Iterator iterator = serviceOrderDTOS.iterator() ; iterator.hasNext() ; ){
                        ServiceOrderDTO so = (ServiceOrderDTO) iterator.next();
                        if (so.getParentId() > 0){
                            ServiceOrderDTO serviceOrderParent =  serviceOrderService.getServiceOrderById(so.getParentId());
                            if (serviceOrderParent.isPay())
                                iterator.remove();
                        }
                    }
                } catch (ServiceException e) {
                    e.printStackTrace();
                    return;
                }
                content = EmailModel.start();

                content.append( lastSaturdayDate + "  至  " + endDate + " 的未支付的服务订单服务订单");
                content .append(EmailModel.officialApprovalServicecAndIsPayModule(serviceOrderDTOS));

                content .append( EmailModel.end());

                SendEmailUtil.send("paul@zhinanzhen.org","未支付的服务订单",content.toString());
                SendEmailUtil.send("elvin@zhinanzhen.org","未支付的服务订单",content.toString());
//                SendEmailUtil.send("jiaheng.xu@zhinanzhen.org","未支付的服务订单",content.toString());
                //SendEmailUtil.send("815124560@qq.com","数据", content.toString());
            }



            {   //发送给每个Manager的总数据
                for(DataDTO area : areaTodayDataList){
                    content = EmailModel.start();

                    content.append(area.getArea()+"截止到本周五:"+startDate+"  至  "+endDate+"号  Data Report");
                    content.append(EmailModel.areaModelHaveDate(area));         //一号到本周五的 Manager 地区的数据

                    for(DataDTO lastWeekArea : lastWeekAreaDataList){
                        if(area.getArea() .equals(lastWeekArea.getArea()) ) {
                            content.append(area.getArea() + " 本周数据:" + lastSaturdayDate + "  至  " + endDate + "号 Data Report");
                            content.append(EmailModel.areaModelNoDate(lastWeekArea));   // 19-25号Manager该地区的总数据
                        }
                    }

                    for(List<DataDTO> regionDataList:regionList){
                        if(area.getArea() .equals(regionDataList.get(0).getArea()) ){
                            content.append(area.getArea()+" 截止到本周五:"+startDate+"  至  "+endDate+"的顾问业绩排名");
                            content.append(EmailModel.rankModelHave(regionDataList)); //一号到本周五Manager地区的排名
                        }
                    }

                    content.append("全澳截止到本周五:"+startDate+"  至  "+endDate+"号的顾问业绩排名");
                    content.append(EmailModel.rankModelNo(dataTOdayDTOList)); // 全澳顾问业绩排名(No money)

                    content.append(EmailModel.end());

                    if (area.getArea() .equals("Sydney")){
                        SendEmailUtil.send("juntao@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
//                        SendEmailUtil.send("jiaheng.xu@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                    }
                    if (area.getArea() .equals("Canberra")){
                        SendEmailUtil.send("juntao@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
//                        SendEmailUtil.send("jiaheng.xu@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                    }
                    if (area.getArea() .equalsIgnoreCase("攻坚部")){
                        SendEmailUtil.send("kevin.fan@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
//                        SendEmailUtil.send("jiaheng.xu@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                    }
                    if (area.getArea() .equals("Melbourne")){
                        SendEmailUtil.send("lisa@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
//                        SendEmailUtil.send("jiaheng.xu@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                    }
                    if (area.getArea() .equals("Brisbane")){
                        SendEmailUtil.send("vicky@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
//                        SendEmailUtil.send("jiaheng.xu@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                    }
                    if (area.getArea() .equals("Adelaide")){
                        SendEmailUtil.send("caroline.wang@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
//                        SendEmailUtil.send("jiaheng.xu@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                    }
                    if (area.getArea() .equals("Hobart")){
                        SendEmailUtil.send("lorrain.pan@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
//                        SendEmailUtil.send("jiaheng.xu@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                    }
                    if (area.getArea().equals("CIS")){
                        SendEmailUtil.send("kevin@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
//                        SendEmailUtil.send("jiaheng.xu@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                    }
                }
            }
        });
    }

    //上月一号到上月末(每月触发)
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 15  2 * ?")
    public void everyMonth(){
        executor.execute(() -> {

            String startDate = DateClass.lastMonthFirstDay(Calendar.getInstance());   //就是上个月一号的时间
            String endDate = DateClass.lastMonthLastDay(Calendar.getInstance());     //上个月最后一天

            //下面的数据是上月初到上月末的
            List<DataDTO> areaTodayDataList = data.dataReport(startDate,endDate,"A",null); //   A  全area地区的area数据   数据
            List<DataDTO> dataTOdayDTOList = data.dataReport(startDate,endDate,"R",null); //  R 全area顾问倒序排名的数据  顾问
            List<List<DataDTO>> regionList = RegionClassification.classification(dataTOdayDTOList);//  按照地区将顾问进行分组

            {
                System.out.println(content+"content");
                content = EmailModel.start();

                content.append("上月全澳的 Data Report，截止日期:"+startDate+"  至  "+endDate);
                content.append(EmailModel.areaModelHaveDate(areaTodayDataList));//area各地区的数据

                content.append("上月全澳的顾问业绩排名，截止日期:"+startDate+"  至  "+endDate);
                content.append(EmailModel.rankModelHave(dataTOdayDTOList)); //顾问业绩排名

                content.append(EmailModel.end());
                SendEmailUtil.send("paul@zhinanzhen.org","全澳的 Data Report",content.toString());
                SendEmailUtil.send("elvin@zhinanzhen.org","全澳的 Data Report",content.toString());
                SendEmailUtil.send("jiaheng.xu@zhinanzhen.org","全澳的 Data Report",content.toString());
            }
            {   //发送给每个Manager的总数据
                for(DataDTO area : areaTodayDataList){
                    content = EmailModel.start();

                    content.append(area.getArea()+"上个月的  Data Report，截止日期:"+startDate+"  至  "+endDate);
                    content.append(EmailModel.areaModelHaveDate(area));         //一号到本周五的 Manager 地区的数据

                    for(List<DataDTO> regionDataList:regionList){
                        if(area.getArea() .equals(regionDataList.get(0).getArea()) ){
                            content.append(area.getArea()+"的顾问业绩排名");
                            content.append(EmailModel.rankModelHave(regionDataList)); //一号到本周五Manager地区的排名
                        }
                    }

                    content.append("上月全澳顾问业绩排名，截止日期："+startDate+"  至  "+endDate);
                    content.append(EmailModel.rankModelNo(dataTOdayDTOList)); // 全澳顾问业绩排名(No money)

                    content.append(EmailModel.end());
                    if (area.getArea() .equals("Sydney")){
                        SendEmailUtil.send("juntao@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                        SendEmailUtil.send("jiaheng.xu@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                    }
                    if (area.getArea() .equals("Canberra")){
                        SendEmailUtil.send("juntao@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                        SendEmailUtil.send("jiaheng.xu@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                    }
                    if (area.getArea() .equalsIgnoreCase("攻坚部")){
                        SendEmailUtil.send("kevin@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                        SendEmailUtil.send("jiaheng.xu@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                    }
                    if (area.getArea() .equals("Melbourne")){
                        SendEmailUtil.send("lisa@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                        SendEmailUtil.send("jiaheng.xu@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                    }
                    if (area.getArea() .equals("Brisbane")){
                        SendEmailUtil.send("vicky@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                        SendEmailUtil.send("jiaheng.xu@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                    }
                    if (area.getArea() .equals("Adelaide")){
                        SendEmailUtil.send("caroline.wang@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                        SendEmailUtil.send("jiaheng.xu@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                    }
                    if (area.getArea() .equals("Hobart")){
                        SendEmailUtil.send("lorrain.pan@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                        SendEmailUtil.send("jiaheng.xu@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                    }
                    if (area.getArea().equals("CIS")){
                        SendEmailUtil.send("kevin@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                        SendEmailUtil.send("jiaheng.xu@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                    }

                }
            }
        });
    }

    //每天凌晨触发
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 0 * * ? ")
    //@org.springframework.scheduling.annotation.Scheduled(cron = "0 27 16 * * ? ")
    public void verifyCodeEveryDay(){
        executor.execute(() -> {
            List<FinanceCodeDO> financeCodeDOS = verifyDao.getFinanceCodeOrderIdIsNull();
            for (FinanceCodeDO financeCodeDO : financeCodeDOS){
                String comment = StringUtil.isNotEmpty(financeCodeDO.getComment()) ? financeCodeDO.getComment() : "";
                LOG.info(" FinanceCode ID : " + financeCodeDO.getId() + " COMMENT : " + comment );
                if (StringUtil.isNotEmpty(VerifyServiceImpl.checkVerifyCode(comment.toUpperCase()))){
                    //得到 verifyCode 并且字符全部转换成大写
                    String verifyCode = VerifyServiceImpl.checkVerifyCode(comment.toUpperCase());
                    LOG.info(" FinanceCode ID : " + financeCodeDO.getId() + " VerifyCode : " + verifyCode );

                    List<VisaDO> visaDOS = visaDAO.listVisaByVerifyCode(verifyCode);
                    List<CommissionOrderDO> commissionOrderDOS = commissionOrderDAO.listCommissionOrderByVerifyCode(verifyCode);
                    if (visaDOS.size() > 0) { //visaDOS 判断list是否有数据
                        VisaDO visaDO = visaDOS.get(0);
                        if (visaDO != null) {
                            visaDO.setBankDate(financeCodeDO.getBankDate());
                            if (visaDO.getAmount()==financeCodeDO.getMoney())
                                visaDO.setChecked(true);
                            visaDO.setBankCheck("Code");
                            if (visaDAO.updateVisa(visaDO) > 0){
                                LOG.info(" FinanceCode  ID : " + financeCodeDO.getId() + " VISA ID : " + visaDO.getId() + " OK ! " );
                                financeCodeDO.setOrderId("CV" + visaDO.getId());
                                financeCodeDO.setAdviserId(visaDO.getAdviserId());
                                financeCodeDO.setUserId(visaDO.getUserId());
                                financeCodeDO.setAmount(visaDO.getAmount());
                                if (visaDO.getServiceOrderId() > 0){
                                    try {
                                        ServiceOrderDTO serviceOrderDTO = serviceOrderService.getServiceOrderById(visaDO.getServiceOrderId());
                                        financeCodeDO.setBusiness(serviceOrderDTO.getService().getName()+"-"+serviceOrderDTO.getService().getCode());
                                    } catch (ServiceException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                    if (commissionOrderDOS.size() > 0) { //commissionOrderDOS 判断list是否有数据
                        CommissionOrderDO commissionOrderDO = commissionOrderDOS.get(0);
                        if (commissionOrderDO != null) {
                            commissionOrderDO.setBankDate(financeCodeDO.getBankDate());
                            if (commissionOrderDO.getAmount()==financeCodeDO.getMoney())
                                commissionOrderDO.setChecked(true);
                            commissionOrderDO.setBankCheck("Code");
                            if (commissionOrderDAO.updateCommissionOrder(commissionOrderDO) > 0){
                                financeCodeDO.setOrderId("CS" + commissionOrderDO.getId());
                                financeCodeDO.setAdviserId(commissionOrderDO.getAdviserId());
                                financeCodeDO.setUserId(commissionOrderDO.getUserId());
                                financeCodeDO.setAmount(commissionOrderDO.getAmount());
                                if (commissionOrderDO.getSchoolId()>0) {
                                    SchoolDO schoolDO = schoolDAO.getSchoolById(commissionOrderDO.getSchoolId());
                                    if (schoolDO != null)
                                        //commissionOrderListDto.setSchool(mapper.map(schoolDo, SchoolDTO.class));
                                        financeCodeDO.setBusiness("留学-"+schoolDO.getName());
                                }
                            }
                        }
                    }
                }
                if (verifyDao.update(financeCodeDO) > 0 ){
                    LOG.info(" FinanceCode ID : " + financeCodeDO.getId() + " UPDATE order_id SUCCESS : " + financeCodeDO.getOrderId());
                }
            }
        });
    }

    //每天凌晨触发
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 0 * * * ")
    public void updateCustomerEveryDay() throws ServiceException {
        executor.execute(() -> {
            Map<String, Object> tokenMap = wxWorkService.getToken(WXWorkAPI.SECRET_CUSTOMER);
            if ((int)tokenMap.get("errcode") != 0){
                LOG.error(tokenMap.get("errmsg").toString());
                throw  new RuntimeException( tokenMap.get("errmsg").toString());
            }
            String customerToken = (String) tokenMap.get("access_token");
            List<AdviserDO> adviserDOList = adviserDAO.listAdviserOperUserIdIsNull();
            for (AdviserDO adviserDO : adviserDOList){
                AdviserDTO adviserDTO = null;
                try {
                    adviserDTO = adviserService.getAdviserById(adviserDO.getId());
                } catch (ServiceException e) {
                    throw new RuntimeException(e);
                }
                if (adviserDTO == null)
                    continue;
                if (StringUtil.isEmpty(adviserDTO.getOperUserId()))
                    continue;
                String userId = adviserDTO.getOperUserId();
                boolean flag = true ;
                String cursor = "";
                while (flag) {
                    Map<String, Object> externalContactListMap = wxWorkService.getexternalContactList(customerToken, userId, cursor, 100);
                    if ((int) externalContactListMap.get("errcode") != 0)
                        break;
                    else {
                        if (externalContactListMap.get("external_contact_list") != null) {
                            JSONArray jsonArray = JSONArray.parseArray(JSON.toJSONString(externalContactListMap.get("external_contact_list")));
                            for (int i = 0; i < jsonArray.size(); i++) {
                                Map<String, Object> externalMap = JSON.parseObject(JSON.toJSONString(jsonArray.get(i)), Map.class);
                                UserDTO userDTO = new UserDTO();
                                boolean isContain = false;
                                if (externalMap.get("follow_info") != null) {
                                    Map<String, Object> follow_info_Map = JSON.parseObject(JSON.toJSONString(externalMap.get("follow_info")), Map.class);
                                    String remark =  follow_info_Map.get("remark").toString();
                                    userDTO.setAuthNickname(EmojiFilter.filterEmoji(remark));
                                    JSONArray jsonMobiles = JSONArray.parseArray(JSON.toJSONString(follow_info_Map.get("remark_mobiles")));
                                    if (jsonMobiles.size() > 0 ){
                                        for (int n = 0 ; n < jsonMobiles.size() ; n++){
                                            String mobiles = jsonMobiles.getString(n);
                                            userDTO.setPhone(mobiles);
                                            try {
                                                if (StringUtil.isNotEmpty(mobiles) && userService.countUser(null, null, null,
                                                        mobiles, null, null, 0, null, null, null) > 0) {
                                                    isContain = true;
                                                    break;
                                                }
                                            } catch (ServiceException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                    }else
                                        userDTO.setPhone("00000000000");
                                }
                                if (externalMap.get("external_contact") != null) {
                                    Map<String, Object> external_contact_Map = JSON.parseObject(JSON.toJSONString(externalMap.get("external_contact")), Map.class);
                                    String name = external_contact_Map.get("name").toString();
                                    userDTO.setName(EmojiFilter.filterEmoji(name));
                                    userDTO.setWechatUsername(EmojiFilter.filterEmoji(name));
                                    String external_userid = external_contact_Map.get("external_userid").toString();
                                    userDTO.setAuthOpenid(external_userid);
                                    String avatar =  external_contact_Map.get("avatar").toString();
                                    userDTO.setAuthLogo(avatar);
                                }
                                if (isContain){
                                    wxWorkService.updateAuthopenidByPhone(userDTO.getAuthOpenid(),userDTO.getPhone(),userDTO.getAreaCode());
                                }
//                            userDTO.setAdviserId(adviserDO.getId()); // TODO: 小包
                                userDTO.setRegionId(adviserDTO.getRegionId());
                                UserDTO userDTOByAuthOpenid = null;
                                try {
                                    userDTOByAuthOpenid = userService.getUserByOpenId("WECHAT_WORK",userDTO.getAuthOpenid());
                                } catch (ServiceException e) {
                                    throw new RuntimeException(e);
                                }
                                if (userDTOByAuthOpenid != null){
                                    userDTO.setId(userDTOByAuthOpenid.getId());
                                    wxWorkService.updateByAuthopenid(userDTO);
                                }
                                if (userDTOByAuthOpenid == null)
                                    wxWorkService.add(userDTO);
                            }
                        }
                        cursor = externalContactListMap.get("next_cursor").toString();
                        if (StringUtil.isEmpty((String) externalContactListMap.get("next_cursor"))){
                            flag = false;
                        }
                    }
                }
            }
        });
    }

    /*
    *每天10点发送签证或者留学的提醒邮件
     */
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 10 * * ? ")
    public void autoSendMailRemind(){
        executor.execute(() -> {

            //签证日期到期前提醒（自动提醒)
            List<org.zhinanzhen.b.service.pojo.UserDTO> userList = visaDAO.listVisaExpirationDate();
            userList.forEach(userDTO -> {
                LOG.info(userDTO.toString());
                try {
                    if (userDTO.getAdviserId() != null){
                        AdviserDTO adviserDTO = adviserService.getAdviserById(userDTO.getAdviserId());
                        if (adviserDTO != null)
                            SendEmailUtil.send(adviserDTO.getEmail(),
                                    userDTO.getName() + sdf.format(userDTO.getVisa_expiration_date()) + " visa 即将到期提醒 !",
                                    adviserDTO.getName() + ": " + userDTO.getName() + userDTO.getId() + "," + sdf.format(userDTO.getVisa_expiration_date()) + ",7天内到期请注意提醒客户，如签证日期有变化请及时更新，如已更新请忽略该提醒.");

                    }else {
                        UserDTO user = userService.getUserById(userDTO.getId());
                        user.getUserAdviserList().forEach(adviser -> {
                            AdviserDTO ad = adviser.getAdviserDto();
                            if (ad != null)
                                SendEmailUtil.send(ad.getEmail(),
                                        userDTO.getName() + sdf.format(userDTO.getVisa_expiration_date()) + " visa 即将到期提醒 !",
                                        ad.getName() + ": " + userDTO.getName() + userDTO.getId() + "," + sdf.format(userDTO.getVisa_expiration_date()) + ",7天内到期请注意提醒客户，如签证日期有变化请及时更新，如已更新请忽略该提醒.");
                        });
                    }

                } catch (ServiceException e) {
                    e.printStackTrace();
                }

            });

            //留学 due date日期提醒（自动提醒）
            List<CommissionOrderDO> orderDOS = commissionOrderDAO.listCommissionOrderInstallmentDueDate();
            orderDOS.forEach(commissionOrderDO -> {
                try {
                    UserDTO userDTO = userService.getUserById(commissionOrderDO.getUserId());
                    AdviserDTO adviserDTO = adviserService.getAdviserById(commissionOrderDO.getAdviserId());
                    if (userDTO != null && adviserDTO != null){
                        String message = "";
                        message = adviserDTO.getName()+":"+userDTO.getName() + userDTO.getId() +","+sdf.format(commissionOrderDO.getInstallmentDueDate())+ ",距离due date还有 "
                                +  CommonUtils.getDateDays(commissionOrderDO.getInstallmentDueDate(),new Date()) + " 天,请及时与学生沟通并申请月奖,如学生未就读请及时关闭订单,如已申请请忽略该提醒."
                                + "<br/><br/><a href='https://yongjinbiao.zhinanzhen.org/webroot_new/commissionorderdetail/ovst/id?" + commissionOrderDO.getId() + "'>需要申请月奖的佣金订单链接</a>";
                        SendEmailUtil.send(adviserDTO.getEmail(), userDTO.getName() + sdf.format(commissionOrderDO.getInstallmentDueDate())+ " 请及时申请月奖",message);
                    }
                } catch (ServiceException e) {
                    e.printStackTrace();
                }
            });
        });

    }

    /**
     * 每小时触发一次(设置提醒)
     */
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 10 * * * ? ")
    public void sendSetRemindMail(){
        executor.execute(() -> {
            List<MailRemindDO> mailRemindDOS = mailRemindDAO.listBySendDate("H");
            for (MailRemindDO mailRemindDO : mailRemindDOS){
                String sendMsg = mailRemindDO.getContent() + " 请及时处理。如已处理完成请及时关闭提醒。" ;
                if (mailRemindDO.getUserId() != null && mailRemindDO.getAdviserId() != null)
                    StringUtil.merge(sendMsg,"<br/><a href='https://yongjinbiao.zhinanzhen.org/webroot_new/userdetail/id/?" + mailRemindDO.getUserId() + "'>点击即可进入客户详情页</a>");
                if (mailRemindDO.getNeedRemind() && !mailRemindDO.isSend()) {
                    SendEmailUtil.send(mailRemindDO.getMail(),mailRemindDO.getTitle(),sendMsg);
                    mailRemindDO.setSend(true);
                    mailRemindDAO.update(mailRemindDO);
                }
            }
        });
    }

//    /**
//     * 每小时触发一次(设置提醒)
//     */
//    @org.springframework.scheduling.annotation.Scheduled(cron = "0 * * * * ? ")
//    public void sout(){
//        log.info("定时任务测试-------------------");
//    }

    /*
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 56 * * * ? ")
    public void t() throws Exception {
        String startDate = DateClass.thisMonthFirstDay(Calendar.getInstance());   //本月一号的时间
        String lastSaturdayDate = DateClass.lastSaturday(Calendar.getInstance()); //上周六
        String endDate = DateClass.thisMonthFriday(Calendar.getInstance());            //也就是当前时间

        List<ServiceOrderDTO> serviceOrderDTOS = serviceOrderService.listServiceOrder(null, null, null, null, null,
                null, null, null, null, "2021-06-1 20:50:58", "2021-08-29 20:50:58", null, null, null, null,
                null, null, null, null, null, 0, false,
                0, 9999, null, null, null,false);
        for (Iterator iterator = serviceOrderDTOS.iterator() ; iterator.hasNext() ; ){
            ServiceOrderDTO so = (ServiceOrderDTO) iterator.next();
            if (so.getParentId() > 0){
                ServiceOrderDTO serviceOrderParent =  serviceOrderService.getServiceOrderById(so.getParentId());
                if (serviceOrderParent.isPay())
                    iterator.remove();
            }
        }

        content = EmailModel.start();
        content .append(EmailModel.officialApprovalServicecAndIsPayModule(serviceOrderDTOS));
        content .append( EmailModel.end());
        
        SendEmailUtil.send("815124560@qq.com","数据", content.toString());
    }
    */
    
    // 每日更新汇率
    @org.springframework.scheduling.annotation.Scheduled(cron = "20 33 5 * * ?")
	public void updateRateEveryDay(){
		executor.execute(() -> {
            try {
                JSONObject jsonObject = getJsonObject("http://web.juhe.cn/finance/exchange/rmbquot?key=459f1492038689af44230eb125de38c7");
                JSONArray resultArray = jsonObject.getJSONArray("result");
                JSONObject result = (JSONObject) resultArray.get(0);
                JSONObject data6 = (JSONObject) result.get("data6");
                Double fSellPri = data6.getDoubleValue("fSellPri") / 100;

                EverydayExchangeRateDO everydayExchangeRateDo = new EverydayExchangeRateDO();
                everydayExchangeRateDo.setCurrency("CNY");
                everydayExchangeRateDo.setOriginalExchangeRate(fSellPri);
                everydayExchangeRateDo.setZnzExchangeRate(fSellPri + 0.15); // ZNZ汇率差
                everydayExchangeRateDo.setUpdateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .parse(StringUtil.merge(data6.getString("date"), " ", data6.getString("time"))));
                log.info("获取实时汇率:" + everydayExchangeRateDo);
                everydayExchangeRateDao.add(everydayExchangeRateDo);
            } catch (Exception e) {
                log.error("获取实时汇率异常:" + e.getMessage());
            }
        });
	}

    // 成都文案订单所属情况（每日更新）
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 30 4 * * ?")
    private void updateUserData() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String format = yesterday.toString();
        String startTime = format + " 00:00:00";
        String endTime = format + " 23:59:59";

        List<Integer> officialIds = new ArrayList<>();
        officialIds.add(1000034);
        // 获取当天数据
        List<ServiceOrderDO> remainingOrders = serviceOrderDAO.listServiceOrder(null, null, null, null, null, null,
                null, null, null, null, null,
                null, null, null,
                null, startTime, endTime, null, officialIds, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, 0, 1000, null, null, null, null, null, null);
        List<ServiceOrderDO> visaList = new ArrayList<>();
        List<ServiceOrderDO> ovstList = new ArrayList<>();
        for (ServiceOrderDO serviceOrderDO : remainingOrders) {
            int officialId = serviceOrderDO.getOfficialId();
            if (officialId == 1000044 || officialId == 1000053 || officialId == 1000056 || officialId == 1000057) {
                ovstList.add(serviceOrderDO);
            } else {
                visaList.add(serviceOrderDO);
            }
        }
        // 获取表格内容
        try {
            // 构建client
            Client client = Client.newBuilder(ACCESS_KEY_ID, ACCESS_KEY_SECRET).build();

            // 创建请求对象
            SearchAppTableRecordReq searchAppTableRecordReq = SearchAppTableRecordReq.newBuilder()
                    .appToken("ZY4CbtJIRaxykks0HNScbdomnmb")
                    .tableId("tblYdUL0ajKR1Qb5")
                    .pageSize(1000)
                    .searchAppTableRecordReqBody(SearchAppTableRecordReqBody.newBuilder()
                            .build())
                    .build();

            // 发起请求
            SearchAppTableRecordResp searchAppTableRecordResp = client.bitable().v1().appTableRecord().search(searchAppTableRecordReq);

            // 处理服务端错误
            if(!searchAppTableRecordResp.success()) {
                System.out.println(String.format("code:%s,msg:%s,reqId:%s, resp:%s",
                        searchAppTableRecordResp.getCode(), searchAppTableRecordResp.getMsg(), searchAppTableRecordResp.getRequestId(), Jsons.createGSON(true, false).toJson(JsonParser.parseString(new String(searchAppTableRecordResp.getRawResponse().getBody(), StandardCharsets.UTF_8)))));
                return;
            }

            // 判断是否删除上上月数据
            LocalDate today = LocalDate.now();
            // 方法1：获取当月的第一天并比较
            LocalDate firstDay = today.withDayOfMonth(1);
            boolean isFirstDay = today.equals(firstDay);

            AppTableRecord[] items = searchAppTableRecordResp.getData().getItems();
            List<String> toTimeList = new ArrayList<>();
            for (AppTableRecord item : items) {
                Map<String, Object> fields = item.getFields();
                String finishTime = (String) ((Map) ((List) fields.get("完成时间")).get(0)).get("text");
                if (isFirstDay) {
                    if (TimeUtil.isBetweenLastLastMonth(finishTime)) {
                        toTimeList.add(item.getRecordId());
                    }
                } else {
                    if (!TimeUtil.isBetweenCurrentAndLastMonth(finishTime)) {
                        toTimeList.add(item.getRecordId());
                    }
                }
            }
            String[] array = toTimeList.stream().toArray(String[]::new);
            // 删除过期数据
            // 创建请求对象
            if (array.length > 0) {
                BatchDeleteAppTableRecordReq deleteAppTableRecordReq = BatchDeleteAppTableRecordReq.newBuilder()
                        .appToken("ZY4CbtJIRaxykks0HNScbdomnmb")
                        .tableId("tblYdUL0ajKR1Qb5")
                        .batchDeleteAppTableRecordReqBody(BatchDeleteAppTableRecordReqBody.newBuilder()
                                .records(array)
                                .build())
                        .build();

                // 发起请求
                BatchDeleteAppTableRecordResp deleteAppTableRecordResp = client.bitable().v1().appTableRecord().batchDelete(deleteAppTableRecordReq, RequestOptions.newBuilder()
                        .build());

                // 处理服务端错误
                if(!deleteAppTableRecordResp.success()) {
                    System.out.println(String.format("code:%s,msg:%s,reqId:%s, resp:%s",
                            deleteAppTableRecordResp.getCode(), deleteAppTableRecordResp.getMsg(), deleteAppTableRecordResp.getRequestId(), Jsons.createGSON(true, false).toJson(JsonParser.parseString(new String(deleteAppTableRecordResp.getRawResponse().getBody(), StandardCharsets.UTF_8)))));
                }
            } else {
                log.info(format + "没有需删除数据");
            }
            // 构建查询数据映射
            List<OfficialDO> officialDOS = officialDao.listOfficial(null, null, null, 0, 1000);
            Map<Integer, OfficialDO> officialDOMap = officialDOS.stream().collect(Collectors.toMap(OfficialDO::getId, Function.identity(), (v1, v2) -> v2));
            List<RegionDO> regionDOS = regionDAO.listAllRegion();
            Map<Integer, RegionDO> regionDOMap = regionDOS.stream().collect(Collectors.toMap(RegionDO::getId, Function.identity(), (v1, v2) -> v2));
            List<ServiceDO> serviceDOS = serviceDAO.listService(null, null, false, 0, 1000);
            Map<Integer, ServiceDO> serviceDOMap = serviceDOS.stream().collect(Collectors.toMap(ServiceDO::getId, Function.identity(), (v1, v2) -> v2));
            List<ServicePackageDO> servicePackageListDOS = servicePackageDAO.listAll();
            Map<Integer, ServicePackageDO> servicePackageListDOMap = servicePackageListDOS.stream().collect(Collectors.toMap(ServicePackageDO::getId, Function.identity(), (v1, v2) -> v2));


            // 更新新数据到签证表格
            AppTableRecord[] recordsToCreate = new AppTableRecord[visaList.size()];
            if (visaList != null && visaList.size() > 0) {
                for (int i = 0; i < visaList.size(); i++) {
                    ServiceOrderDO serviceOrderDO = visaList.get(i);
                    Map<String, Object> fields = buildRecordFields(serviceOrderDO, officialDOMap, regionDOMap, serviceDOMap, servicePackageListDOMap, true);
                    recordsToCreate[i] = AppTableRecord.newBuilder().fields(fields).build();
                }
                BatchCreateAppTableRecordReq createReq = BatchCreateAppTableRecordReq.newBuilder()
                        .tableId(EXCEL_ID_VISA)
                        .appToken(APP_TOKEN)
                        .batchCreateAppTableRecordReqBody(BatchCreateAppTableRecordReqBody.newBuilder()
                                .records(recordsToCreate)
                                .build())
                        .build();
                BatchCreateAppTableRecordResp createResp = client.bitable().v1().appTableRecord().batchCreate(createReq, RequestOptions.newBuilder().build());
                if (!createResp.success()) {
                    System.out.println("添加记录失败: " + createResp.getMsg());
                }
            } else {
                log.info(format + "没有新增数据，未进行添加");
            }

            // 更新新数据到留学表格
            AppTableRecord[] recordsToCreateOVST = new AppTableRecord[ovstList.size()];
            if (ovstList != null && ovstList.size() > 0) {
                for (int i = 0; i < ovstList.size(); i++) {
                    ServiceOrderDO serviceOrderDO = ovstList.get(i);
                    Map<String, Object> fields = buildRecordFields(serviceOrderDO, officialDOMap, regionDOMap, serviceDOMap, servicePackageListDOMap, true);
                    recordsToCreateOVST[i] = AppTableRecord.newBuilder().fields(fields).build();
                }
                BatchCreateAppTableRecordReq createReq = BatchCreateAppTableRecordReq.newBuilder()
                        .tableId(EXCEL_ID_OVST)
                        .appToken(APP_TOKEN)
                        .batchCreateAppTableRecordReqBody(BatchCreateAppTableRecordReqBody.newBuilder()
                                .records(recordsToCreateOVST)
                                .build())
                        .build();
                BatchCreateAppTableRecordResp createResp = client.bitable().v1().appTableRecord().batchCreate(createReq, RequestOptions.newBuilder().build());
                if (!createResp.success()) {
                    System.out.println("添加记录失败: " + createResp.getMsg());
                }
            } else {
                log.info(format + "没有新增数据，未进行添加");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



	private static JSONObject getJsonObject(String url) {
		JSONObject json = null;
		BufferedReader in = null;
		try {
			URL realUrl = new URL(url);
			URLConnection connection = realUrl.openConnection();
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.connect();
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			while ((line = in.readLine()) != null)
				json = com.alibaba.fastjson.JSONObject.parseObject(line);
		} catch (Exception e) {
			log.error("发送GET请求出现异常！" + e.getMessage());
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return json;
	}

    public enum regionEnum{
        Sydney, Canberra, 攻坚部, Melbourne, Brisbane, Adelaide, Hobart, CIS ;

        public static regionEnum get(String name){
            for (regionEnum e : regionEnum.values())
                if (e.toString().equals(name))
                    return e;
            return null;
        }
    }

    private Map<String, Object> buildRecordFields(ServiceOrderDO serviceOrderDO,
                                                  Map<Integer, OfficialDO> officialDOMap,
                                                  Map<Integer, RegionDO> regionDOMap,
                                                  Map<Integer, ServiceDO> serviceDOMap,
                                                  Map<Integer, ServicePackageDO> servicePackageListDOMap,
                                                  boolean convertStatus) {
        Map<String, Object> fields = new HashMap<>();
        int officialId = serviceOrderDO.getOfficialId();
        if (officialId != 0) {
            OfficialDO officialDO = officialDOMap.get(officialId);
            if (officialDO != null) {
                RegionDO regionDO = regionDOMap.get(officialDO.getRegionId());
                fields.put("所属文案", officialDO.getName());
                if (regionDO != null) {
                    fields.put("所属部门", regionDO.getName());
                }
            }
        }
        String serviceName = buildServiceName(serviceOrderDO, serviceDOMap, servicePackageListDOMap);
        UserDO userById = userDao.getUserById(serviceOrderDO.getUserId());
        fields.put("完成时间", sdfT.format(serviceOrderDO.getFinishDate()));
        fields.put("客户", userById != null ? userById.getName() : "");
        fields.put("订单", String.valueOf(serviceOrderDO.getId()));
        fields.put("签证类别", serviceName);
        fields.put("AI初审结果", "这是初始写入的内容");
        fields.put("订单状态", convertStatus ? convertOrderStatus(serviceOrderDO.getState()) : serviceOrderDO.getState());
        return fields;
    }

    private String buildServiceName(ServiceOrderDO serviceOrderDO,
                                    Map<Integer, ServiceDO> serviceDOMap,
                                    Map<Integer, ServicePackageDO> servicePackageListDOMap) {
        String serviceName = "";
        String ass = "";
        if (serviceOrderDO.getServiceAssessId() != null) {
            ServiceAssessDO serviceAssessDO = serviceAssessDao.seleteAssessById(serviceOrderDO.getServiceAssessId());
            if (serviceAssessDO != null) {
                ass = serviceAssessDO.getName();
            }
        }
        if (serviceOrderDO.getServiceId() != 0) {
            ServiceDO serviceDO = serviceDOMap.get(serviceOrderDO.getServiceId());
            if (serviceOrderDO.getServicePackageId() > 0) {
                String servicepakageName = "";
                String tmp = "";
                ServicePackageDO servicePackageListDO = servicePackageListDOMap.get(serviceOrderDO.getServicePackageId());
                if (servicePackageListDO != null) {
                    String servicePackagetype = servicePackageListDO.getType();
                    servicepakageName = getTypeStrOfServicePackageDTO(servicePackagetype);
                }
                tmp = "-";
                if ("雇主担保".equalsIgnoreCase(serviceDO.getName()) || "独立技术移民".equalsIgnoreCase(serviceDO.getName())) {
                    serviceName = serviceDO.getName() + "-" + serviceDO.getCode() + tmp + ass + servicepakageName;
                }
            } else {
                ServiceDO service = serviceDOMap.get(serviceOrderDO.getServiceId());
                if (service != null) {
                    serviceName = service.getCode();
                }
            }
        } else {
            SchoolInstitutionDO schoolInstitutionDO = schoolInstitutionDAO.getSchoolInstitutionByCourseId(serviceOrderDO.getCourseId());
            if (ObjectUtil.isNotNull(schoolInstitutionDO)) {
                serviceName = schoolInstitutionDO.getName();
            }
        }
        return serviceName;
    }


    private String getTypeStrOfServicePackageDTO(String type) {
        String servicepakageName;
        switch (type) {
            case "CA":
                servicepakageName = "职业评估";
                break;
            case "EOI":
                servicepakageName = "EOI";
                break;
            case "SA":
                servicepakageName = "学校申请";
                break;
            case "VA":
                servicepakageName = "签证申请";
                break;
            case "ZD":
                servicepakageName = "州担";
                break;
            case "MAT":
                servicepakageName = "Matrix";
                break;
            case "SBO":
                servicepakageName = "SBO";
                break;
            case "TM":
                servicepakageName = "提名";
                break;
            case "DB":
                servicepakageName = "担保";
                break;
            case "ROI":
                servicepakageName = "ROI";
                break;
            default:
                servicepakageName = null;
        }
        return servicepakageName;
    }
    // 转换订单状态
    private String convertOrderStatus(String state) {
        String stateName = null;
        switch (state) {
            case "PENDING":
                stateName = "待提交审核";
                break;
            case "REVIEW":
                stateName = "资料待审核";
                break;
            case "OREVIEW":
                stateName = "资料审核中";
                break;
            case "APPLY":
                stateName = "服务申请中";
                break;
            case "COMPLETE":
                stateName = "服务申请完成";
                break;
            case "FINISH":
                stateName = "资料审核完成";
                break;
            case "CLOSE":
                stateName = "关闭";
                break;
            case "RECEIVED":
                stateName = "已收款凭证已提交";
                break;
            case "PAID":
                stateName = "COE已下";
                break;
            case "WAIT":
                stateName = "已提交Mara审核";
                break;
            case "APPLY_FAILED":
                stateName = "申请失败";
                break;
            case "COMPLETE_FD":
                stateName = "财务转账完成";
                break;
            default:
                stateName = "无状态";
        }
        return stateName;
    }


}
