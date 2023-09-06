package org.zhinanzhen.b.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ikasoa.core.utils.MapUtil;
import com.ikasoa.core.utils.ObjectUtil;
import com.ikasoa.core.utils.StringUtil;
import org.apache.commons.lang.RandomStringUtils;
import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.*;
import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.service.ServicePackageTypeEnum;
import org.zhinanzhen.b.service.WXWorkService;
import org.zhinanzhen.b.service.pojo.ChatDTO;
import org.zhinanzhen.b.service.pojo.SchoolInstitutionListDTO;
import org.zhinanzhen.tb.dao.AdminUserDAO;
import org.zhinanzhen.tb.dao.AdviserDAO;
import org.zhinanzhen.tb.dao.RegionDAO;
import org.zhinanzhen.tb.dao.UserDAO;
import org.zhinanzhen.tb.dao.pojo.AdminUserDO;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
import org.zhinanzhen.tb.dao.pojo.RegionDO;
import org.zhinanzhen.tb.dao.pojo.UserDO;
import org.zhinanzhen.tb.service.pojo.UserDTO;
import org.zhinanzhen.tb.utils.WXWorkAPI;
import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/01/26 14:07
 * Description:
 * Version: V1.0
 */
@Service
public class WXWorkServiceImpl implements WXWorkService {

    private DozerBeanMapper dozerBeanMapper = new DozerBeanMapper();

    @Resource
    private UserDAO userDAO;

    @Resource
    private ServiceOrderDAO serviceOrderDAO;

    @Resource
    private  AdviserDAO adviserDAO;

    @Resource
    private RegionDAO regionDAO;

    @Resource
    private ServiceDAO serviceDAO;

    @Resource
    ServicePackageDAO servicePackageDAO;

    @Resource
    private ServiceAssessDao serviceAssessDao;

    @Resource
    private SchoolDAO schoolDAO;

    @Resource
    private WXWorkDAO wxWorkDAO;

    @Resource
    private AdminUserDAO adminUserDao;

    @Resource
    private MaraDAO maraDao;

    @Resource
    private OfficialDAO officialDao;

    @Resource
    private SchoolCourseDAO schoolCourseDAO;

    @Value("${qywxcallBackUrl}")
    private String callBackUrl;

    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    @Override
    public String getWXWorkUrl() {
        String WXWORK_STRING_CODE = WXWorkAPI.WXWORK_STRING_CODE;
        WXWORK_STRING_CODE = WXWORK_STRING_CODE.replace("CORPID" ,WXWorkAPI .CORPID).replace("AGENTID",WXWorkAPI.AGENTID)
                .replace("REDIRECT_URI", callBackUrl + "/admin_v2.1/wxwork/userId").replace("STATE", RandomStringUtils.randomAlphanumeric(5));
        return WXWORK_STRING_CODE;
    }


    public  Map<String, Object> getToken(String SECRET) {
        JSONObject json =  WXWorkAPI.sendGet(WXWorkAPI.ACCESS_TOKEN.replace("ID",WXWorkAPI.CORPID).replace("SECRET",SECRET));
        if(ObjectUtil.isNull(json))
        	return MapUtil.newHashMap();
        Map<String, Object> access_tokenMap = JSON.parseObject(JSON.toJSONString(json), Map.class);
        return access_tokenMap;
    }

    @Override
    public Map<String, Object> getUserInfo(String token, String code) {
        JSONObject json =  WXWorkAPI.sendGet(WXWorkAPI.USERINFO.replace("ACCESS_TOKEN",token).replace("CODE",code));
        if(ObjectUtil.isNull(json))
        	return MapUtil.newHashMap();
        Map<String, Object> infoMap = JSON.parseObject(JSON.toJSONString(json), Map.class);
        return infoMap;
    }

    @Override
    public Map<String, Object> getexternalContactList(String token, String userId, String cursor, int limit) {
        JSONObject json =  WXWorkAPI.sendPostBody(WXWorkAPI.EXTERNAL_CONTACT_LIST.replace("ACCESS_TOKEN",token),userId, cursor ,limit);
        if(ObjectUtil.isNull(json))
        	return MapUtil.newHashMap();
        Map<String, Object> infoMap = JSON.parseObject(JSON.toJSONString(json), Map.class);
        return infoMap;
    }

    @Override
    public int add(UserDTO userDTO) {
        UserDO userDO = dozerBeanMapper.map(userDTO,UserDO.class);
        userDO.setAuthType("WECHAT_WORK");
        userDO.setSource("企业微信");
        userDO.setEmail("zhinanzhen@zhinanzhen.org");


        if (userDO.getPhone().startsWith("1"))
            userDO.setAreaCode("+86");
        else if (userDO.getPhone().startsWith("04"))
            userDO.setAreaCode("+61");
        else
            userDO.setAreaCode("+00");
        if (userDAO.getUserByAuth_openid(userDO.getAuthOpenid()).size() == 0){
            return userDAO.addUser(userDO);
        }
        return 0 ;
    }

    @Override
    public  boolean sendMsg(int id,String token){
        JSONObject parm = new JSONObject();
        JSONObject content = new JSONObject();
        String msg = "";
        ServiceOrderDO serviceOrderDO = serviceOrderDAO.getServiceOrderById(id);
        if (serviceOrderDO != null){
            msg = "\uD83D\uDCB0\uD83D\uDCB0\uD83D\uDCB0\uD83D\uDCB0\uD83D\uDCB0\uD83D\uDCB0\uD83D\uDCB0\uD83D\uDCB0\n";
            AdviserDO adviserDO = adviserDAO.getAdviserById(serviceOrderDO.getAdviserId());
            if (adviserDO != null){
                RegionDO regionDO = regionDAO.getRegionById(adviserDO.getRegionId());
                if (regionDO != null)
                    msg = msg + "恭喜 : " + regionDO.getName() + "   " + adviserDO.getName() + "  , 成功签约 ";
            }
            if (serviceOrderDO.getType().equalsIgnoreCase("VISA")) {
                ServiceDO serviceDO = serviceDAO.getServiceById(serviceOrderDO.getServiceId());
                if (serviceDO != null)
                    msg = msg
                            + "[ " + serviceDO.getName() + "-" + serviceDO.getCode();
                ServicePackageDO servicePackageDO = servicePackageDAO.getById(serviceOrderDO.getServicePackageId());
                if (servicePackageDO != null)
                    msg = msg + "-" + ServicePackageTypeEnum.getServicePackageTypeComment(servicePackageDO.getType());
                    //switch (servicePackageDO.getType()) {
                    //    case "CA":
                    //        msg = msg + "-" + "职业评估";
                    //        break;
                    //    case "EOI":
                    //        msg = msg + "-" + "EOI";
                    //        break;
                    //    case "VA":
                    //        msg = msg + "-" + "签证申请";
                    //        break;
                    //   case "ZD":
                    //        msg = msg + "-" + "州担";
                    //        break;
                    //}
                ServiceAssessDO serviceAssessDO = serviceAssessDao.seleteAssessById(serviceOrderDO.getServiceAssessId());
                if (serviceAssessDO != null)
                    msg = msg + "-" + serviceAssessDO.getName();
                msg = msg + " ] . \n";
            }
            if (serviceOrderDO.getType().equalsIgnoreCase("OVST")){
                SchoolDO schoolDO =  schoolDAO.getSchoolById(serviceOrderDO.getSchoolId());
                if (schoolDO != null ){
                    msg = msg
                            + "[ 留学 - " + schoolDO.getName() + "      专业 : " + schoolDO.getSubject() +  " ] . \n";
                } else {
                    if (serviceOrderDO.getCourseId() > 0){
                        SchoolInstitutionListDTO schoolInstitutionInfo = schoolCourseDAO.getSchoolInstitutionInfoByCourseId(serviceOrderDO.getCourseId());
                        if (schoolInstitutionInfo != null)
                            msg = msg
                                    + "[ 留学 - " + schoolInstitutionInfo.getInstitutionName() + "      专业 : "
                                    + schoolInstitutionInfo.getSchoolCourseDO().getCourseName() +  " ] . \n";
                    }
                }
            }
            if (serviceOrderDO.getType().equalsIgnoreCase("ZX")) {
                ServiceDO serviceDO = serviceDAO.getServiceById(serviceOrderDO.getServiceId());
                if (serviceDO != null)
                    msg = msg
                            + "[ " + serviceDO.getName() + "-" + serviceDO.getCode();
                ServiceAssessDO serviceAssessDO = serviceAssessDao.seleteAssessById(serviceOrderDO.getServiceAssessId());
                if (serviceAssessDO != null)
                    msg = msg + "-" + serviceAssessDO.getName();
                msg = msg + " ] . \n";
            }
            msg = msg + "各地区加油\n\uD83D\uDC4F\uD83D\uDC4F\uD83D\uDC4F\uD83D\uDC4F\uD83D\uDC4F\uD83D\uDC4F\uD83D\uDC4F\uD83D\uDC4F";
        }

        content.put("content",msg);
        parm.put("chatid",WXWorkAPI.CHATID);
        parm.put("msgtype","text");
        parm.put("text",content);
        parm.put("safe",0);

        //ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        //HttpSession session=attr.getRequest().getSession(true);
        //String token = (String) session.getAttribute("corpToken" + BaseController.VERSION); // TODO:小包  不要在Service层依赖Session，有时间把这几行代码移到Controller层

        JSONObject json =  WXWorkAPI.sendPostBody_Map(WXWorkAPI.SENDMESSAGE.replace("ACCESS_TOKEN",token),parm);
        if(ObjectUtil.isNull(json))
        	return Boolean.FALSE;
        Map<String,Object> resultMap = json.getInnerMap();//getInnerMap()方法得到Map结构
        if ((int)resultMap.get("errcode") == 0){
            return true;
        }

        return false;
    }

    @Override
	public int updateByAuthopenid(UserDTO userDTO) {
		UserDO userDO = dozerBeanMapper.map(userDTO, UserDO.class);
		if (!userDTO.getPhone().equalsIgnoreCase("00000000000")) {
			List<UserDO> userList = userDAO.listUser(null, null, null, userDTO.getPhone(), userDTO.getAreaCode(), null,
					null, null, null, null, null, null, null, 0, 1);
			if (userList.size() > 0 && userList.get(0).getId() != userDTO.getId()) { // 排除当前id
				return -1;
			}
		}
		return userDAO.updateByAuthopenid(userDO);
	}

	@Override
	public boolean updateAuthopenidByPhone(String authOpenid, String phone, String areaCode) {
		List<UserDO> userList = userDAO.listUser(null, null, null, phone, areaCode, null, null, null, null, null, null, null, null, 0,
				1);
		if (userList.size() > 0 && StringUtil.isEmpty(userList.get(0).getAuthOpenid())
				&& userDAO.getUserByAuth_openid(authOpenid).size() == 0) { // 为空的时候写入
			return userDAO.updateAuthopenidByPhone(authOpenid, phone);
        }
        return  false;
    }

    @Override
    public int addChat(ChatDTO chatDTO) {
        ChatDO chatDO = dozerBeanMapper.map(chatDTO,ChatDO.class);
        return wxWorkDAO.addChat(chatDO);
    }

    @Override
    public ChatDO ChatDOByServiceOrderId(int serviceOrderId) {
        return wxWorkDAO.ChatDOByServiceOrderId(serviceOrderId);
    }

    public Map JSONObjectToMap(JSONObject json){
        Map<String, Object> map = JSON.parseObject(JSON.toJSONString(json), Map.class);
        return map;
    }

    /**
     * 这个方法
     * @param serviceOrderId
     */
    public  void createChat(int serviceOrderId , String token) {
        List<String> userList = new ArrayList<>();
        JSONObject parm = new JSONObject();
        ChatDO chatDO = new ChatDO();
        ServiceOrderDO serviceOrderDO = serviceOrderDAO.getServiceOrderById(serviceOrderId);
        if (serviceOrderDO != null){
            chatDO.setServiceOrderId(serviceOrderDO.getParentId() > 0 ? serviceOrderDO.getParentId() : serviceOrderId);//SIV子订单，存SIV的id
            chatDO.setUserId(serviceOrderDO.getUserId());
            AdviserDO adviserDO =  adviserDAO.getAdviserById(serviceOrderDO.getAdviserId());
            if (adviserDO != null){
                chatDO.setAdviserId(adviserDO.getId());
                AdminUserDO adminUserDO =  adminUserDao.getAdminUserByUsername(adviserDO.getEmail());
                if (adminUserDO != null)
                    if ( StringUtil.isNotEmpty(adminUserDO.getOperUserId())){
                        userList.add(adminUserDO.getOperUserId());
                        parm.put("owner",adminUserDO.getOperUserId());
                    }
            }
            OfficialDO officialDO =  officialDao.getOfficialById(serviceOrderDO.getOfficialId());
            if (officialDO != null){
                chatDO.setOfficialId(officialDO.getId());
                AdminUserDO adminUserDO =  adminUserDao.getAdminUserByUsername(officialDO.getEmail());
                if (adminUserDO != null)
                    if ( StringUtil.isNotEmpty(adminUserDO.getOperUserId()))
                        userList.add(adminUserDO.getOperUserId());
            }
            MaraDO maraDO =  maraDao.getMaraById(serviceOrderDO.getMaraId());
            if (maraDO != null){
                chatDO.setMaraId(maraDO.getId());
                AdminUserDO adminUserDO =  adminUserDao.getAdminUserByUsername(maraDO.getEmail());
                if (adminUserDO != null)
                    if ( StringUtil.isNotEmpty(adminUserDO.getOperUserId()) & serviceOrderDO.getType().equalsIgnoreCase("VISA"))
                        userList.add(adminUserDO.getOperUserId());
            }
            ServiceDO serviceDO = serviceDAO.getServiceById(serviceOrderDO.getServiceId());
            if (serviceDO != null)
                parm.put("name",StringUtil.merge("服务订单",chatDO.getServiceOrderId(),"服务项目:",serviceDO.getName(),"-",serviceDO.getCode()) );
            userList.add("elvinhe");
            userList.add("paul");
            parm.put("userlist",userList);
            parm.put("chatid","ZNZ"+serviceOrderDO.getId());
        }
        JSONObject json =  WXWorkAPI.sendPostBody_Map(WXWorkAPI.CREATECHAT.replace("ACCESS_TOKEN",token),parm);
        if(ObjectUtil.isNull(json))
        	return;
        System.out.println(parm.get("chatid") + "群聊创建返回信息：" + json);
        Map<String,Object> result = JSON.parseObject(JSON.toJSONString(json), Map.class);
        if ((int)result.get("errcode") == 0){	//群聊创建成功之后，发送第一条消息
            chatDO.setChatId("ZNZ" + serviceOrderDO.getId());
            int re = wxWorkDAO.addChat(chatDO);
            JSONObject msgParm = new JSONObject();
            JSONObject content = new JSONObject();
            msgParm.put("chatid",result.get("chatid"));
            msgParm.put("msgtype","text");
            content.put("content","这里是订单编号为:" + chatDO.getServiceOrderId() + "的群聊,请顾问拉客户进群，进群后接下来请文案对接资料。");
            msgParm.put("text",content);
            msgParm.put("safe",0);
            JSONObject sendFirstMsgResultJson =  WXWorkAPI.sendPostBody_Map(WXWorkAPI.SENDMESSAGE.replace("ACCESS_TOKEN",token),msgParm);
            System.out.println(chatDO.getChatId() + "发送第一条消息返回信息:" + sendFirstMsgResultJson);
        }
    }

}
