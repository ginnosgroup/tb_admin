package org.zhinanzhen.b.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ikasoa.core.utils.MapUtil;
import com.ikasoa.core.utils.ObjectUtil;
import com.ikasoa.core.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.zhinanzhen.b.service.WXWorkService;
import org.zhinanzhen.b.service.pojo.BehaviorDataDTO;
import org.zhinanzhen.b.service.pojo.ExternalUserDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.ListResponse;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.AdviserService;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.UserService;
import org.zhinanzhen.tb.service.pojo.AdminUserDTO;
import org.zhinanzhen.tb.service.pojo.AdviserDTO;
import org.zhinanzhen.tb.service.pojo.UserDTO;
import org.zhinanzhen.tb.utils.EmojiFilter;
import org.zhinanzhen.tb.utils.WXWorkAPI;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

    private static final Logger LOG = LoggerFactory.getLogger(WXWorkController.class);

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Resource
    private WXWorkService wxWorkService;

    @Resource
    private AdviserService adviserService;

    @Resource
    private UserService userService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${weiban.crop_id}")
    private String weibanCropId;

    @Value("${weiban.secret}")
    private String weibanSecret;

    @Value("${qywxcallBackUrl}")
    private String callBackUrl;

    public enum AccessTokenType{
        corp("企微自建应用SECRET"), cust("企微客户联系SECRET");
        private String val;
        AccessTokenType(String val){
            this.val = val;
        }
    }

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
        AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
        if (adminUserLoginInfo == null)
            return "<div style= 'color:#3c763d;'>未登录，授权失败 !</div>" + str;

        String corpToken = "";
        String customerToken = "";

        corpToken = (String) session.getAttribute("corpToken" + VERSION);
        customerToken = (String) session.getAttribute("customerToken" + VERSION);
        Map<String, Object> infoMap = wxWorkService.getUserInfo(corpToken, code);
        if ((int) infoMap.get("errcode") != 0)
            return "<div style= 'color:#3c763d;'>系统出错,授权失败 !</div>" + str;
        String userId = (String) infoMap.get("UserId");
        AdminUserDTO adminUser = adminUserService.getAdminUserByUsername(adminUserLoginInfo.getUsername());
        if ( adminUser != null){
            if (StringUtil.isNotEmpty(adminUser.getOperUserId()))
                return "<div style= 'color:#3c763d;'>该用户已经授权了!</div>" + str;
            if (adminUserService.updateOperUserId(adminUser.getId(),userId)){
                AdminUserLoginInfo loginInfo = (AdminUserLoginInfo) session.getAttribute("AdminUserLoginInfo" + VERSION);
                if (loginInfo != null)
                    loginInfo.setAuth(true);
                session.removeAttribute("AdminUserLoginInfo" + VERSION);
                session.setAttribute("AdminUserLoginInfo" + VERSION, loginInfo);
                if (adminUserLoginInfo.getApList().equalsIgnoreCase("GW"))
                    return "<div style= 'color:#3c763d;text-align:center;margin-top:15%;'>授权成功，请在客户管理页面导入并编辑客户资!3秒以后自动跳转...</div>" +
                            "<script>setTimeout('gotoUrl()', 3000);function gotoUrl(){window.location='" + callBackUrl +
                            "/webroot_new/weibanzhushou/list/ContactWay'}</script>";
                else
                    return "<div style= 'color:#3c763d;'>授权成功!</div>" + str;
            }
        }
        return   "<div style= 'color:#3c763d;'>授权失败!</div>" + str;
    }

    @GetMapping(value = "/getExternalUserList")
    @ResponseBody
	public ListResponse<List<ExternalUserDTO>> getExternalUserList(HttpServletRequest request, HttpServletResponse response)
			throws ServiceException {
    	List<ExternalUserDTO> externalUserList = new ArrayList<ExternalUserDTO>();
		AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
		if (ObjectUtil.isNull(adminUserLoginInfo))
			return new ListResponse(false, 0, 0, null, "未登录!");
		AdminUserDTO adminUser = adminUserService.getAdminUserByUsername(adminUserLoginInfo.getUsername());
		if (ObjectUtil.isNull(adminUser))
			return new ListResponse(false, 0, 0, null, "帐号异常!");
		Integer adviserId = getAdviserId(request);
		if (ObjectUtil.isNull(adviserId))
			return new ListResponse(false, 0, 0, null, "仅限顾问调用!");
		String customerToken = token(request, AccessTokenType.cust.toString());
		if (StringUtil.isEmpty(customerToken))
			return new ListResponse(false, 0, 0, null, "Token获取失败!");
		String userId = adminUser.getOperUserId();
		if (StringUtil.isEmpty(userId))
			return new ListResponse(false, 0, 0, null, "OperUserId为空,请扫码授权!");
		Map<String, Object> externalContactListMap = wxWorkService.getexternalContactList(customerToken, userId, "",
				1000);
		if (MapUtil.isEmpty(externalContactListMap))
			return new ListResponse(false, 0, 0, null, "调用企业微信API异常!");
		if ((int) externalContactListMap.get("errcode") != 0)
			return new ListResponse(false, 0, 0, null,
					StringUtil.merge("调用企业微信API异常:", externalContactListMap.get("errmsg")));
		if (externalContactListMap.get("external_contact_list") != null) {
			JSONArray jsonArray = JSONArray.parseArray(JSON.toJSONString(externalContactListMap.get("external_contact_list")));
			for (int i = 0; i < jsonArray.size(); i++) {
				Map<String, Object> externalMap = JSON.parseObject(JSON.toJSONString(jsonArray.get(i)), Map.class);
				ExternalUserDTO externalUserDto = new ExternalUserDTO();
				if (externalMap.get("follow_info") != null) {
					Map<String, Object> followInfoMap = JSON.parseObject(JSON.toJSONString(externalMap.get("follow_info")), Map.class);
					// userid
					externalUserDto.setUserId(followInfoMap.get("userid").toString());
					// remark
					String remark =  followInfoMap.get("remark").toString();
					externalUserDto.setRemark(EmojiFilter.filterEmoji(remark));
					// description
					externalUserDto.setDescription(followInfoMap.get("description").toString());
					// createtime
					externalUserDto.setCreatetime(new Date(Long.parseLong(followInfoMap.get("createtime").toString())));
					// tag id
					// remark mobiles
					// addWay
					externalUserDto.setAddWay((int)followInfoMap.get("add_way"));
					// operUserid
					externalUserDto.setOperUserid(followInfoMap.get("oper_userid").toString());
				}
				if (externalMap.get("external_contact") != null) {
					Map<String, Object> externalContactMap = JSON
							.parseObject(JSON.toJSONString(externalMap.get("external_contact")), Map.class);
					// externalUserid
					externalUserDto.setExternalUserid(externalContactMap.get("external_userid").toString());
					// name
					externalUserDto.setName(externalContactMap.get("name").toString());
					// type
					externalUserDto.setType((int) externalContactMap.get("type"));
					// avatar
					externalUserDto.setAvatar(externalContactMap.get("avatar").toString());
					// gender
					externalUserDto.setGender((int) externalContactMap.get("gender"));
					// unionid
					externalUserDto.setUnionid(externalContactMap.get("unionid").toString());
				}
				externalUserList.add(externalUserDto);
			}
		}
		return new ListResponse<List<ExternalUserDTO>>(true, 1, externalUserList.size(), externalUserList, "");
	}

    @GetMapping(value = "/getexternalcontactlist")
    @ResponseBody
    @Transactional
    public  Response getExternalContactList(HttpServletRequest request, HttpServletResponse response) throws ServiceException {

        AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
        if (adminUserLoginInfo == null)
            return  new Response(1,"未登录");
        AdminUserDTO adminUser = adminUserService.getAdminUserByUsername(adminUserLoginInfo.getUsername());
        Integer adviserId =  getAdviserId(request);
        if (adviserId == null)
            return  new Response(1,"不是顾问!");
        HttpSession session = request.getSession();
        String customerToken = (String) session.getAttribute("customerToken" + VERSION);
        AdviserDTO adviserDTO =  adviserService.getAdviserById(adviserId);
        if (adviserDTO == null)
            return  new Response(1,"没有此顾问");
        if (StringUtil.isEmpty(adminUser.getOperUserId()))
            return  new Response(1 ,"先授权登录");

        String userId = adminUser.getOperUserId();
        ArrayList phoneContainList = new ArrayList();
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
									if (StringUtil.isNotEmpty(mobiles) && userService.countUser(null, null, null,
											mobiles, null, null, 0, null, null, null) > 0) {
										isContain = true;
										break;
                                    }
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
                        if (isContain){
                            wxWorkService.updateAuthopenidByPhone(userDTO.getAuthOpenid(),userDTO.getPhone(), userDTO.getAreaCode());
                        }
//                        userDTO.setAdviserId(adviserId); // TODO: 小包
                        userDTO.setRegionId(adviserDTO.getRegionId());
                        UserDTO userDTOByAuthOpenid = userService.getUserByOpenId("WECHAT_WORK",userDTO.getAuthOpenid());
                        if (userDTOByAuthOpenid != null){
                            userDTO.setId(userDTOByAuthOpenid.getId());
                            int result = wxWorkService.updateByAuthopenid(userDTO);
                            if (result == -1)
                                phoneContainList.add(userDTO.getPhone());
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
        return  new Response(0 ,"success",phoneContainList);
    }

    @GetMapping(value = "/customerstatistics")
    @ResponseBody
    public Response customerStatistics(//@RequestParam(value = "id")int id,
            HttpServletRequest request, HttpServletResponse response) throws ServiceException, UnsupportedEncodingException {
        super.setGetHeader(response);

        AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
        if (adminUserLoginInfo == null)
            return  new Response(1,"未登录");
        AdminUserDTO adminUser = adminUserService.getAdminUserByUsername(adminUserLoginInfo.getUsername());
        Integer adviserId =  getAdviserId(request);
        if (adviserId == null)
            return  new Response(1,"不是顾问!");
        HttpSession session = request.getSession();
        String customerToken = (String) session.getAttribute("customerToken" + VERSION);
        AdviserDTO adviserDTO =  adviserService.getAdviserById(adviserId);
        if (adviserDTO == null)
            return  new Response(1,"没有此顾问");
        if (StringUtil.isEmpty(adminUser.getOperUserId()))
            return  new Response(1 ,"先授权登录");


        //HttpHeaders httpHeaders = new HttpHeaders();
        //MediaType type=MediaType.parseMediaType("application/json;charset=UTF-8");
        //httpHeaders.setContentType(type);

        int customerTotal = 0, todayAddCustomer = 0, todayLoseCustomer = 0, LoseCustomerTotal = 0, sendMsgTotal = 0 ;

        Map uriVariablesMap = new HashMap<String, Object>();
        Calendar calendar = Calendar.getInstance();
        JSONObject jsonObject = null;
        uriVariablesMap.put("userid", Arrays.asList(adminUser.getOperUserId()));
        //uriVariablesMap.put("partyid");
        for (int i = 0 ; i < 6; i ++){
            long endTime = calendar.getTimeInMillis() / 1000;
            uriVariablesMap.put("end_time", endTime);
            calendar.add(Calendar.DAY_OF_MONTH, -29);
            long startTime = calendar.getTimeInMillis() / 1000;
            uriVariablesMap.put("start_time", startTime);
            //开始时间和结束时间跨度最大为30天
            jsonObject =  restTemplate.postForObject(StringUtil.merge(WXWorkAPI.BEHAVIOR_DATA, customerToken), uriVariablesMap, JSONObject.class);

            JSONArray jsonList =  jsonObject.getJSONArray("behavior_data");
            int size = jsonList.size();
            for (int j = 0 ; j < size ; j ++) {
                BehaviorDataDTO behaviorDataDto = JSON.parseObject(JSON.toJSONString(jsonList.get(j)), BehaviorDataDTO.class);
                if ( i == 0 && j == 29){
                    todayAddCustomer = behaviorDataDto.getNewContactCnt();
                    todayLoseCustomer = behaviorDataDto.getNegativeFeedbackCnt();
                }
                LoseCustomerTotal = behaviorDataDto.getNegativeFeedbackCnt() + LoseCustomerTotal;
                sendMsgTotal = behaviorDataDto.getMessageCnt() + sendMsgTotal;
            }
            //System.out.println(jsonObject.toJSONString());
        }
        String url = WXWorkAPI.CUSTOMERLIST.replace("ACCESS_TOKEN", customerToken)
                .replace("USERID", URLEncoder.encode(adminUser.getOperUserId(),"UTF-8"));
        URI uri = URI.create(url);
        JSONObject customerListJson = restTemplate.getForObject(uri, JSONObject.class);

        if ((int)customerListJson.get("errcode") == 0){
            customerTotal = customerListJson.getJSONArray("external_userid").size();
        }
        //System.out.println(customerListJson.toJSONString());

        Map responseMap = new HashMap();
        responseMap.put("customerTotal", customerTotal);
        responseMap.put("todayAddCustomer", todayAddCustomer);
        responseMap.put("todayLoseCustomer", todayLoseCustomer);
        responseMap.put("LoseCustomerTotal", LoseCustomerTotal);
        responseMap.put("sendMsgTotal", sendMsgTotal);

        return new Response(0, responseMap);
    }

    @GetMapping(value = "/customerstatisticslist")
    @ResponseBody
    public Response customerStatisticsList(
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            HttpServletRequest request, HttpServletResponse response) throws ServiceException, ParseException {
        super.setGetHeader(response);
        AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
        if (adminUserLoginInfo == null)
            return  new Response(1,"未登录");
        AdminUserDTO adminUser = adminUserService.getAdminUserByUsername(adminUserLoginInfo.getUsername());
        Integer adviserId =  getAdviserId(request);
        if (adviserId == null)
            return  new Response(1,"不是顾问!");
        AdviserDTO adviserDTO =  adviserService.getAdviserById(adviserId);
        if (adviserDTO == null)
            return  new Response(1,"没有此顾问");
        if (StringUtil.isEmpty(adminUser.getOperUserId()))
            return  new Response(1 ,"先授权登录");

        if (StringUtil.isEmpty(startTime) || StringUtil.isEmpty(endTime)){
            Calendar calendar = Calendar.getInstance();
            endTime = calendar.getTimeInMillis() / 1000 + "";
            calendar.add(Calendar.DAY_OF_MONTH, -10);
            startTime = calendar.getTimeInMillis() / 1000 + "";
        }else {
            startTime = sdf.parse(startTime).getTime() / 1000 + "";
            endTime = sdf.parse(endTime).getTime() / 1000 + "";
        }

        Map uriVariablesMap = new HashMap<String, Object>();
        uriVariablesMap.put("userid", Arrays.asList(adminUser.getOperUserId()));
        //uriVariablesMap.put("partyid"); //这个参数可以不用要
        uriVariablesMap.put("start_time", startTime);
        uriVariablesMap.put("end_time", endTime);

        LOG.info("startTime / endTime = " + startTime + "/" + endTime);
        LOG.info(" userid : " + Arrays.asList(adminUser.getOperUserId()));

        String customerToken = token(request, AccessTokenType.cust.toString());
        JSONObject jsonObject =
                restTemplate.postForObject(StringUtil.merge(WXWorkAPI.BEHAVIOR_DATA, customerToken), uriVariablesMap, JSONObject.class);

        List<BehaviorDataDTO> behaviors = new ArrayList<>();
        if ((int)jsonObject.get("errcode") == 0){
            JSONArray jsonList =  jsonObject.getJSONArray("behavior_data");
            int size = jsonList.size();
            for (int index = size - 1 ; index >= 0 ; index --) {
                BehaviorDataDTO behaviorDataDto = JSON.parseObject(JSON.toJSONString(jsonList.get(index)), BehaviorDataDTO.class);
                behaviors.add(behaviorDataDto);
            }
            return new Response(0, "ok", behaviors);
        }
        LOG.info(" get_user_behavior_data api errmsg :" + jsonObject.get("errmsg"));
        return new Response(1, jsonObject.get("errmsg").toString(), behaviors);
    }

    @GetMapping(value = "/contactwaylist")
    @ResponseBody
    public Response contactWayList(
            @RequestParam(value = "pageSize", required = false)int pageSize,
            @RequestParam(value = "pageNum", required = false)int pageNum,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            HttpServletRequest request, HttpServletResponse response) throws ServiceException, ParseException {
        super.setGetHeader(response);
        AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
        if (adminUserLoginInfo == null)
            return  new Response(1,"未登录");
        AdminUserDTO adminUser = adminUserService.getAdminUserByUsername(adminUserLoginInfo.getUsername());
        Integer adviserId =  getAdviserId(request);
        if (adviserId == null)
            return  new Response(1,"不是顾问!");
        AdviserDTO adviserDTO =  adviserService.getAdviserById(adviserId);
        if (adviserDTO == null)
            return  new Response(1,"没有此顾问");
        if (StringUtil.isEmpty(adminUser.getOperUserId()))
            return  new Response(1 ,"先授权登录");



        String url = "https://open.weibanzhushou.com/open-api/access_token/get";
        HashMap uriVariablesMap = new HashMap();
        uriVariablesMap.put("corp_id", weibanCropId);
        uriVariablesMap.put("secret", weibanSecret);
        JSONObject weibanTokenJsonObject = restTemplate.postForObject(url, uriVariablesMap, JSONObject.class);
        if ((int)weibanTokenJsonObject.get("errcode") != 0){
            return new Response(1, weibanTokenJsonObject.get("errmsg"));
        }

        String url2 = StringUtil.merge("https://open.weibanzhushou.com/open-api/contact_way/list?", "access_token={access_token}"
                , "&staff_id={staff_id}", "&limit={limit}&offset={offset}");
        uriVariablesMap.put("access_token", weibanTokenJsonObject.get("access_token"));
        uriVariablesMap.put("limit", pageSize);
        uriVariablesMap.put("offset", pageNum);
        uriVariablesMap.put("staff_id", adminUser.getOperUserId());//  这里是企业微信的userid
        JSONObject contactWayListJsonObject = restTemplate.getForObject(url2, JSONObject.class, uriVariablesMap);
        //System.out.println(weibanTokenJsonObject);
        System.out.println(contactWayListJsonObject);
        if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)){
            Date _start = sdf.parse(startTime);
            Date _end = sdf.parse(endTime);
            Date _createAt = null;
            JSONArray _jsonArray = new JSONArray();
            for ( ; contactWayListJsonObject.getJSONArray("contact_way_list").size() > 0 ; ){
                JSONArray jsonArray = contactWayListJsonObject.getJSONArray("contact_way_list");
                int size = jsonArray.size();
                for (int i = 0; i < size ; i++){
                    String createdAt =  JSON.toJSONString(((JSONObject) jsonArray.get(i)).get("created_at"));
                    _createAt = new Date(Long.parseLong(createdAt));
                    if (_start.before(_createAt) &&_end.after(_createAt))
                        _jsonArray.add(jsonArray.get(i));
                }
                uriVariablesMap.put("offset", ++pageNum * pageSize);
                contactWayListJsonObject = restTemplate.getForObject(url2, JSONObject.class, uriVariablesMap);
            }
            return new Response(0, "success", _jsonArray);
        }
        if((int)contactWayListJsonObject.get("errcode") == 0){
            return new Response(0, "success", contactWayListJsonObject.get("contact_way_list"));
        }
        return new Response(1, contactWayListJsonObject.get("errmsg"));
    }

}
