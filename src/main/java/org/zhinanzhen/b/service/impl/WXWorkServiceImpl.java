package org.zhinanzhen.b.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.RandomStringUtils;
import org.aspectj.org.eclipse.jdt.core.IField;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.zhinanzhen.b.dao.*;
import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.service.WXWorkService;
import org.zhinanzhen.tb.dao.AdviserDAO;
import org.zhinanzhen.tb.dao.RegionDAO;
import org.zhinanzhen.tb.dao.UserDAO;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
import org.zhinanzhen.tb.dao.pojo.RegionDO;
import org.zhinanzhen.tb.dao.pojo.UserDO;
import org.zhinanzhen.tb.service.pojo.UserDTO;
import org.zhinanzhen.tb.utils.WXWorkAPI;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
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

    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    @Override
    public String getWXWorkUrl() {
        String WXWORK_STRING_CODE = WXWorkAPI.WXWORK_STRING_CODE;
        WXWORK_STRING_CODE = WXWORK_STRING_CODE.replace("CORPID" ,WXWorkAPI .CORPID).replace("AGENTID",WXWorkAPI.AGENTID)
                .replace("REDIRECT_URI","http://127.0.0.1:8080/admin/wxwork/userId").replace("STATE", RandomStringUtils.randomAlphanumeric(5));
        return WXWORK_STRING_CODE;
    }


    public  Map<String, Object> getToken(String SECRET) {
        JSONObject json =  WXWorkAPI.sendGet(WXWorkAPI.ACCESS_TOKEN.replace("ID",WXWorkAPI.CORPID).replace("SECRET",SECRET));
        Map<String, Object> access_tokenMap = JSON.parseObject(JSON.toJSONString(json), Map.class);
        return access_tokenMap;
    }

    @Override
    public Map<String, Object> getUserInfo(String token, String code) {
        JSONObject json =  WXWorkAPI.sendGet(WXWorkAPI.USERINFO.replace("ACCESS_TOKEN",token).replace("CODE",code));
        Map<String, Object> infoMap = JSON.parseObject(JSON.toJSONString(json), Map.class);
        return infoMap;
    }

    @Override
    public Map<String, Object> getexternalContactList(String token, String userId, String cursor, int limit) {
        JSONObject json =  WXWorkAPI.sendPostBody(WXWorkAPI.EXTERNAL_CONTACT_LIST.replace("ACCESS_TOKEN",token),userId, cursor ,limit);
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
    public  void sendMsg(int id){
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
                    switch (servicePackageDO.getType()) {
                        case "CA":
                            msg = msg + "-" + "职业评估";
                            break;
                        case "EOI":
                            msg = msg + "-" + "EOI";
                            break;
                        case "VA":
                            msg = msg + "-" + "签证申请";
                            break;
                    }
                ServiceAssessDO serviceAssessDO = serviceAssessDao.seleteAssessById(serviceOrderDO.getServiceAssessId());
                if (serviceAssessDO != null)
                    msg = msg + "-" + serviceAssessDO.getName();
                msg = msg + " ] . \n";
            }
            if (serviceOrderDO.getType().equalsIgnoreCase("OVST")){
                SchoolDO schoolDO =  schoolDAO.getSchoolById(serviceOrderDO.getSchoolId());
                if (schoolDO != null )
                    msg = msg
                            + "[ 留学 - " + schoolDO.getName() + "      专业 : " + schoolDO.getSubject() +  " ] . \n";
            }
            msg = msg + "各地区加油\n\uD83D\uDC4F\uD83D\uDC4F\uD83D\uDC4F\uD83D\uDC4F\uD83D\uDC4F\uD83D\uDC4F\uD83D\uDC4F\uD83D\uDC4F";
        }

        content.put("content",msg);
        parm.put("chatid",WXWorkAPI.CHATID);
        parm.put("msgtype","text");
        parm.put("text",content);
        parm.put("safe",0);

        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session=attr.getRequest().getSession(true);
        String token = (String) session.getAttribute("corpToken");
        JSONObject json =  WXWorkAPI.sendPostBody_Map(WXWorkAPI.SENDMESSAGE.replace("ACCESS_TOKEN",token),parm);
    }
}
