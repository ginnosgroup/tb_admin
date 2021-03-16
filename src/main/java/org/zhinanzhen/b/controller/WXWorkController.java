package org.zhinanzhen.b.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.ikasoa.core.utils.StringUtil;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.zhinanzhen.b.service.WXWorkService;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.AdviserService;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.pojo.AdviserDTO;
import org.zhinanzhen.tb.service.pojo.UserDTO;
import org.zhinanzhen.tb.utils.EmojiFilter;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/01/26 13:55
 * Description:
 * Version: V1.0
 */
@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/wxwork")
public class WXWorkController extends  BaseController{

    @Resource
    private WXWorkService wxWorkService;

    @Resource
    private AdviserService adviserService;

    @GetMapping(value = "/WXWorkCode")
    @ResponseBody
    public Response getWXWorkUrl() {
        return new Response(0, "获取企业微信登录二维码成功", wxWorkService.getWXWorkUrl());
    }

    @RequestMapping(value = "/userId" , method =  RequestMethod.GET)
    @ResponseBody
    @Transactional
    public String getUserId(@RequestParam(value = "code") String code,
                                  @RequestParam(value = "state", required = false) String state,
                                  HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        super.setGetHeader(response);
        String str = "<script>setTimeout(self.close,7000)</script>";
        HttpSession session = request.getSession();
        Integer adviserId = getAdviserId(request);
        if (adviserId == null)
            return "<div style= 'color:#3c763d;'>未登录，授权失败 !</div>" + str;

        String corpToken = "";
        String customerToken = "";

        corpToken = (String) session.getAttribute("corpToken");
        customerToken = (String) session.getAttribute("customerToken");
        Map<String, Object> infoMap = wxWorkService.getUserInfo(corpToken, code);
        if ((int) infoMap.get("errcode") != 0)
            return "<div style= 'color:#3c763d;'>系统出错,授权失败 !</div>" + str;
        String userId = (String) infoMap.get("UserId");
        AdviserDTO adviserDTO =  adviserService.getAdviserById(adviserId);
        if (adviserDTO != null ){
            if (StringUtil.isNotEmpty(adviserDTO.getOperUserId()))
                return "<div style= 'color:#3c763d;'>该用户已经授权了!</div>" + str;
            if (StringUtil.isEmpty(adviserDTO.getOperUserId()))
                adviserDTO.setOperUserId(userId);
        }
        if (adviserService.updateAdviser(adviserDTO)>0){
            return "<div style= 'color:#3c763d;'>授权成功，请在客户管理页面导入并编辑客户资料!</div>" + str;
        }
        return  "<div style= 'color:#3c763d;'>授权失败!</div>" + str;
    }


    @GetMapping(value = "/getexternalcontactlist")
    @ResponseBody
    @Transactional
    public  Response getExternalContactList(HttpServletRequest request, HttpServletResponse response) throws ServiceException {

        Integer adviserId =  getAdviserId(request);
        if (adviserId == null)
            return  new Response(1,"adviserId is null");
        HttpSession session = request.getSession();
        String customerToken = (String) session.getAttribute("customerToken");
        AdviserDTO adviserDTO =  adviserService.getAdviserById(adviserId);
        if (adviserDTO == null)
            return  new Response(1,"没有此顾问");

        if (StringUtil.isEmpty(adviserDTO.getOperUserId()))
            return  new Response(1 ,"先授权登录");

        String userId = adviserDTO.getOperUserId();
        boolean flag = true ;
        String cursor = "";
        while (flag) {
            Map<String, Object> externalContactListMap = wxWorkService.getexternalContactList(customerToken, userId, cursor, 100);
            if ((int) externalContactListMap.get("errcode") != 0)
                return new Response(1,externalContactListMap.get("errmsg"));
            else {
                if (externalContactListMap.get("external_contact_list") != null) {
                    JSONArray jsonArray = JSONArray.parseArray(JSON.toJSONString(externalContactListMap.get("external_contact_list")));
                    for (int i = 0; i < jsonArray.size(); i++) {
                        Map<String, Object> externalMap = JSON.parseObject(JSON.toJSONString(jsonArray.get(i)), Map.class);
                        for (Map.Entry<String, Object> entry : externalMap.entrySet()) {
                            System.out.println(entry.getKey() + "====" + entry.getValue());
                        }
                        UserDTO userDTO = new UserDTO();
                        if (externalMap.get("follow_info") != null) {
                            Map<String, Object> follow_info_Map = JSON.parseObject(JSON.toJSONString(externalMap.get("follow_info")), Map.class);
                            String remark =  follow_info_Map.get("remark").toString();
                            userDTO.setAuthNickname(EmojiFilter.filterEmoji(remark));
                            JSONArray jsonMobiles = JSONArray.parseArray(JSON.toJSONString(follow_info_Map.get("remark_mobiles")));
                            if (jsonMobiles.size() > 0 ){
                                System.out.println(jsonMobiles.size());
                                for (int n = 0 ; n < jsonMobiles.size() ; n++){
                                    String mobiles = jsonMobiles.getString(0);
                                    userDTO.setPhone(mobiles);
                                }
                            }else
                                userDTO.setPhone("00000000000");
                            //String userid = (String) follow_info_Map.get("userid");
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
                        userDTO.setAdviserId(adviserId);
                        userDTO.setRegionId(adviserDTO.getRegionId());
                        wxWorkService.add(userDTO);
                    }
                }
                cursor = externalContactListMap.get("next_cursor").toString();
                if (StringUtil.isEmpty((String) externalContactListMap.get("next_cursor"))){
                    flag = false;
                }
            }
        }
        return  new Response(0 ,"success");
    }

}
