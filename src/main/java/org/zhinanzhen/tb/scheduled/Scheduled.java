package org.zhinanzhen.tb.scheduled;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.ikasoa.core.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
import org.zhinanzhen.tb.service.AdviserService;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.UserService;
import org.zhinanzhen.tb.service.pojo.AdviserDTO;
import org.zhinanzhen.tb.service.pojo.UserDTO;
import org.zhinanzhen.tb.utils.CommonUtils;
import org.zhinanzhen.tb.utils.EmojiFilter;
import org.zhinanzhen.tb.utils.SendEmailUtil;
import org.zhinanzhen.tb.utils.WXWorkAPI;
import java.text.SimpleDateFormat;
import java.util.*;

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
@Lazy(false)
public class Scheduled {

    private static final Logger LOG = LoggerFactory.getLogger(Scheduled.class);

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

    private Calendar calendar ;

    private StringBuilder content = null;

    private  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    //String startDate = "2020-09-01";
    //String lastSaturdayDate = "2020-09-19";
    //String endDate = "2020-09-25";

    //本月1号-本周五   上周六-本周五
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 9 ?  *  SAT")
    public void everyWeek(){


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
            SendEmailUtil.send("jiaheng.xu@zhinanzhen.org","全澳的 Data Report",content.toString());
            //SendEmailUtil.send("815124560@qq.com","全澳的 Data Report", content.toString());

        }

        {
            List<ServiceOrderDTO> serviceOrderDTOS = null;
			try {
				serviceOrderDTOS = serviceOrderService.listServiceOrder(null, null, null, null, null, null, null, null,
						null, lastSaturdayDate, endDate, null, null, null, null, null, null, null, null, null, null, 0,
						0, false, 0, 9999, null, null, null, false, null);
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
            SendEmailUtil.send("jiaheng.xu@zhinanzhen.org","未支付的服务订单",content.toString());
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
                    SendEmailUtil.send("jiaheng.xu@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                }
                if (area.getArea() .equals("Canberra")){
                    SendEmailUtil.send("juntao@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                    SendEmailUtil.send("jiaheng.xu@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
                }
                if (area.getArea() .equalsIgnoreCase("攻坚部")){
                    SendEmailUtil.send("kevin.fan@zhinanzhen.org",area.getArea()+" Data Report",content.toString());
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
            }
        }

    }

    //上月一号到上月末(每月触发)
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 15  2 * ?")
    public void everyMonth(){

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

            }
        }


    }

    //每天凌晨触发
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 0 * * ? ")
    //@org.springframework.scheduling.annotation.Scheduled(cron = "0 27 16 * * ? ")
    public void verifyCodeEveryDay(){
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
    }

    //每天凌晨触发
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 0 * * ? ")
    public void updateCustomerEveryDay() throws ServiceException {
        Map<String, Object> tokenMap = wxWorkService.getToken(WXWorkAPI.SECRET_CUSTOMER);
        if ((int)tokenMap.get("errcode") != 0){
            LOG.error(tokenMap.get("errmsg").toString());
            throw  new RuntimeException( tokenMap.get("errmsg").toString());
        }
        String customerToken = (String) tokenMap.get("access_token");
        List<AdviserDO> adviserDOList = adviserDAO.listAdviserOperUserIdIsNull();
        for (AdviserDO adviserDO : adviserDOList){
            AdviserDTO adviserDTO =  adviserService.getAdviserById(adviserDO.getId());
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
                                        if (StringUtil.isNotEmpty(mobiles) && userService.countUser(null, null, null, mobiles, null, 0, null, null) > 0){
                                            isContain = true;
                                            break;
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
                                wxWorkService.updateAuthopenidByPhone(userDTO.getAuthOpenid(),userDTO.getPhone());
                            }
//                            userDTO.setAdviserId(adviserDO.getId()); // TODO: 小包
                            userDTO.setRegionId(adviserDTO.getRegionId());
                            UserDTO userDTOByAuthOpenid = userService.getUserByOpenId("WECHAT_WORK",userDTO.getAuthOpenid());
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
    }

    /*
    *每天10点发送签证或者留学的提醒邮件
     */
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 10 * * ? ")
    public void autoSendMailRemind(){

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

    }

    /**
     * 每小时触发一次(设置提醒)
     */
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 10 * * * ? ")
    public void sendSetRemindMail(){
        List<MailRemindDO> mailRemindDOS = mailRemindDAO.listBySendDate("H");
        for (MailRemindDO mailRemindDO : mailRemindDOS){
            String sendMsg = mailRemindDO.getContent() + " 请及时处理。如已处理完成请及时关闭提醒。" ;
            if (mailRemindDO.getUserId() != null && mailRemindDO.getAdviserId() != null)
                StringUtil.merge(sendMsg,"<br/><a href='https://yongjinbiao.zhinanzhen.org/webroot_new/userdetail/id/?" + mailRemindDO.getUserId() + "'>点击即可进入客户详情页</a>");
            SendEmailUtil.send(mailRemindDO.getMail(),mailRemindDO.getTitle(),sendMsg);
            mailRemindDO.setSend(true);
            mailRemindDAO.update(mailRemindDO);
        }
    }

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

    public enum regionEnum{
        Sydney, Canberra, 攻坚部, Melbourne, Brisbane, Adelaide, Hobart ;

        public static regionEnum get(String name){
            for (regionEnum e : regionEnum.values())
                if (e.toString().equals(name))
                    return e;
            return null;
        }
    }

}
