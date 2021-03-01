package org.zhinanzhen.b.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.RandomStringUtils;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.zhinanzhen.b.dao.ServiceOrderDAO;
import org.zhinanzhen.b.dao.pojo.ServiceOrderDO;
import org.zhinanzhen.b.service.WXWorkService;
import org.zhinanzhen.tb.dao.UserDAO;
import org.zhinanzhen.tb.dao.pojo.UserDO;
import org.zhinanzhen.tb.service.pojo.UserDTO;
import org.zhinanzhen.tb.utils.WXWorkAPI;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
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
        if (serviceOrderDO!=null){
            UserDO userDO = userDAO.getUserById(serviceOrderDO.getId());
            if (userDO!=null)
                msg = "ID:"+serviceOrderDO.getId()+"\n"
                    +"姓名:"+userDO.getName()+"\n"
                    +"备注:"+serviceOrderDO.getRemarks()+"\n"
                    +"信息:"+serviceOrderDO.getInformation()+"\n";
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

    //if (type.equalsIgnoreCase("VISA")){
    //    wxWorkService.sendMsg(serviceOrderDto.getId());
    //}
}
