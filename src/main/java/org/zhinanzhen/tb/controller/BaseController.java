package org.zhinanzhen.tb.controller;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.zhinanzhen.b.service.QywxExternalUserService;
import org.zhinanzhen.b.service.pojo.QywxExternalUserDTO;
import org.zhinanzhen.b.service.pojo.QywxExternalUserDescriptionDTO;
import org.zhinanzhen.tb.controller.BaseController.AdminUserLoginInfo;
import org.zhinanzhen.tb.service.AdminUserService;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.pojo.AdminUserDTO;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ikasoa.core.ErrorCodeEnum;
import com.ikasoa.core.utils.ObjectUtil;
import com.ikasoa.core.utils.StringUtil;

import lombok.Data;

public class BaseController {

	private static final Logger LOG = LoggerFactory.getLogger(BaseController.class);
	
	public static final String VERSION = "2.1"; // TODO:Larry  版本号待移动到配置文件中

	@Resource
	protected AdminUserService adminUserService;
	
	@Resource
	private QywxExternalUserService qywxExternalUserService;
	
	@Autowired
    private RestTemplate restTemplate;

    @Value("${weiban.crop_id}")
    private String weibanCropId;

    @Value("${weiban.secret}")
    private String weibanSecret;

	@ExceptionHandler(Exception.class)
	@ResponseBody
	public Response<Integer> exp(Exception e) {
		LOG.error("error ! message:" + e.getMessage());
		e.printStackTrace();
		return new Response<Integer>(1, e.getMessage(), 0);
	}

	private void initHeader(HttpServletResponse response) {
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Headers", "Content-Type");
		response.setHeader("Access-Control-Allow-Credentials", "true");
	}

	protected void setHeader(HttpServletResponse response) {
		initHeader(response);
		response.setHeader("Access-Control-Allow-Methods", "GET,POST");
		response.setHeader("Allow", "GET,POST");
	}

	protected void setGetHeader(HttpServletResponse response) {
		initHeader(response);
		response.setHeader("Access-Control-Allow-Methods", "GET");
		response.setHeader("Allow", "GET");
	}

	protected void setPostHeader(HttpServletResponse response) {
		initHeader(response);
		response.setHeader("Access-Control-Allow-Methods", "POST");
		response.setHeader("Allow", "POST");
	}

	public static Response<String> upload(MultipartFile file, HttpSession session, String dir)
			throws IllegalStateException, IOException {
		if (file != null) {
			String path = "/root/tmp/";// 文件路径
			String type = "JPG";// 文件类型
			String fileName = file.getOriginalFilename().replace(" ", "_").replace("%20", "_");// 文件原名称
			LOG.info("上传的文件原名称:" + fileName);
			// 判断文件类型
			type = fileName.indexOf(".") != -1 ? fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length())
					: null;
			if (type != null) {// 判断文件类型是否为空
				if ("PNG".equals(type.toUpperCase()) || "JPG".equals(type.toUpperCase())) {
					String realPath = "/data" + dir;
					// 创建目录
					File folder = new File(realPath);
					if (!folder.isDirectory()) {
						folder.mkdirs();
					}
					// 自定义的文件名称
//					String newFileName = String.valueOf(System.currentTimeMillis()) + "_" + fileName.toLowerCase();
					String newFileName = String.valueOf(System.currentTimeMillis());
					// 设置存放图片文件的路径
					path = realPath + newFileName;
					LOG.info("存放图片文件的路径:" + path);
					// 转存文件到指定的路径
					file.transferTo(new File(path));
					return new Response<String>(0, "", dir + newFileName);
				} else {
					return new Response<String>(1, "文件类型只能是.png或.jpg.", null);
				}
			} else {
				return new Response<String>(2, "文件类型为空.", null);
			}
		} else {
			return new Response<String>(3, "文件为空.", null);
		}
	}

	public static Response<String> upload2(MultipartFile file, HttpSession session, String dir)
			throws IllegalStateException, IOException {
		if (file != null) {
			String fileName = file.getOriginalFilename().replace(" ", "_").replace("%20", "_");// 文件原名称
			LOG.info("上传的文件原名称:" + fileName);
			// 判断文件类型
			String type = fileName.indexOf(".") != -1
					? fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length())
					: null;
			String realPath = StringUtil.merge("/data", dir);
			// 创建目录
			File folder = new File(realPath);
			if (!folder.isDirectory())
				folder.mkdirs();
			// 自定义的文件名称
//			String newFileName = String.valueOf(System.currentTimeMillis()) + "_" + fileName.toLowerCase();
			String newFileName = String.valueOf(System.currentTimeMillis());
			// 设置存放文件的路径
			String path = StringUtil.merge(realPath, newFileName, ".", type);
			LOG.info("存放文件的路径:" + path);
			// 转存文件到指定的路径
			file.transferTo(new File(path));
			return new Response<String>(0, "", StringUtil.merge(dir, newFileName, ".", type));
		} else {
			return new Response<String>(3, "文件为空.", null);
		}
	}
	
	public static Response<String> uploadPdf(MultipartFile file, HttpSession session, String dir)
			throws IllegalStateException, IOException {
		if (file != null) {
			String path = "/root/tmp/";// 文件路径
			String type = "PDF";// 文件类型
			String fileName = file.getOriginalFilename().replace(" ", "_").replace("%20", "_");// 文件原名称
			LOG.info("上传的文件原名称:" + fileName);
			// 判断文件类型
			type = fileName.indexOf(".") != -1 ? fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length())
					: null;
			if (type != null) {// 判断文件类型是否为空
				if ("PDF".equals(type.toUpperCase())) {
					String realPath = "/data" + dir;
					// 创建目录
					File folder = new File(realPath);
					if (!folder.isDirectory()) {
						folder.mkdirs();
					}
					// 自定义的文件名称
//					String newFileName = String.valueOf(System.currentTimeMillis()) + "_" + fileName.toLowerCase();
					String newFileName = String.valueOf(System.currentTimeMillis());
					// 设置存放图片文件的路径
					path = realPath + newFileName + "." + type;
					LOG.info("存放PDF文件的路径:" + path);
					// 转存文件到指定的路径
					file.transferTo(new File(path));
					return new Response<String>(0, "", dir + newFileName + "." + type);
				} else {
					return new Response<String>(1, "文件类型只能是pdf.", null);
				}
			} else {
				return new Response<String>(2, "文件类型为空.", null);
			}
		} else {
			return new Response<String>(3, "文件为空.", null);
		}
	}
	
	protected AdminUserLoginInfo getLoginInfoAndUpdateSession(HttpSession session, int id) throws ServiceException {
		String sessionId = session.getId();
		if (id > 0 && adminUserService.updateSessionId(id, sessionId))
			return getLoginInfoAndUpdateSession(session, adminUserService.getAdminUserById(id));
		else
			return null;
	}
	
	protected AdminUserLoginInfo getLoginInfoAndUpdateSession(HttpSession session, AdminUserDTO adminUser) throws ServiceException {
		String sessionId = session.getId();
			AdminUserLoginInfo loginInfo = new AdminUserLoginInfo();
			loginInfo.setId(adminUser.getId());
			loginInfo.setUsername(adminUser.getUsername());
			loginInfo.setOperUserid(adminUser.getOperUserId());
			loginInfo.setSessionId(sessionId);
			if (adminUser != null) {
				String ap = adminUser.getApList();
				if (ap != null) {
					loginInfo.setApList(ap);
					if (ap.contains("GW"))
						loginInfo.setAdviserId(adminUser.getAdviserId());
					if (ap.contains("MA"))
						loginInfo.setMaraId(adminUser.getMaraId());
					if (ap.contains("WA"))
						loginInfo.setOfficialId(adminUser.getOfficialId());
					if (ap.contains("KJ"))
						loginInfo.setKjId(adminUser.getKjId());
				}
				loginInfo.setRegionId(adminUser.getRegionId());
				loginInfo.setOfficialAdmin(adminUser.isOfficialAdmin());
				if (StringUtil.isNotEmpty(adminUser.getOperUserId()))
					loginInfo.setAuth(true);
			}
			session.removeAttribute("AdminUserLoginInfo" + VERSION);
			session.setAttribute("AdminUserLoginInfo" + VERSION, loginInfo);
			session.setMaxInactiveInterval(4 * 60 * 60);
			return loginInfo;
	}

	// 获取当前用户
	protected AdminUserLoginInfo getAdminUserLoginInfo(HttpServletRequest request) {
		HttpSession session = request.getSession();
		if (session.getAttribute("AdminUserLoginInfo" + VERSION) != null) {
			AdminUserLoginInfo adminUserLoginInfo = (AdminUserLoginInfo) session.getAttribute("AdminUserLoginInfo" + VERSION);
			if (adminUserLoginInfo != null)
				return adminUserLoginInfo;
		}
		return null;
	}

	// 获取当前顾问编号
	protected Integer getAdviserId(HttpServletRequest request) {
		AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
		if (adminUserLoginInfo != null) {
			String ap = adminUserLoginInfo.getApList();
			if (ap != null && ap.contains("GW"))
				return adminUserLoginInfo.getAdviserId();
		}
		return null;
	}

	// 获取当前Mara编号
	protected Integer getMaraId(HttpServletRequest request) {
		AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
		if (adminUserLoginInfo != null) {
			String ap = adminUserLoginInfo.getApList();
			if (ap != null && ap.contains("MA"))
				return adminUserLoginInfo.getMaraId();
		}
		return null;
	}

	// 获取当前Official编号
	protected Integer getOfficialId(HttpServletRequest request) {
		AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
		if (adminUserLoginInfo != null) {
			String ap = adminUserLoginInfo.getApList();
			if (ap != null && ap.contains("WA"))
				return adminUserLoginInfo.getOfficialId();
		}
		return null;
	}
	
	// 获取当前Official管理员编号
	protected Integer getOfficialAdminId(HttpServletRequest request) {
		AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
		if (adminUserLoginInfo != null) {
			String ap = adminUserLoginInfo.getApList();
			if (ap != null && ap.contains("WA") && adminUserLoginInfo.isOfficialAdmin())
				return adminUserLoginInfo.getOfficialId();
		}
		return null;
	}

	// 获取当前KJ编号
	protected Integer getKjId(HttpServletRequest request) {
		AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
		if (adminUserLoginInfo != null) {
			String ap = adminUserLoginInfo.getApList();
			if (ap != null && ap.contains("KJ"))
				return adminUserLoginInfo.getKjId();
		}
		return null;
	}

	// 获取用户是不是管理员
	protected Boolean isAdminUser(HttpServletRequest request) {
		AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
		if (adminUserLoginInfo != null) {
			String ap = adminUserLoginInfo.getApList();
			return ap == null || (ap != null && !ap.contains("GW"));
		}
		return Boolean.FALSE;
	}

	// 获取用户是不是超级管理员
	protected Boolean isSuperAdminUser(HttpServletRequest request) {
		AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
		if (adminUserLoginInfo != null) {
			String ap = adminUserLoginInfo.getApList();
			return ap == null || (ap != null && ap.contains("SUPERAD"));
		}
		return Boolean.FALSE;
	}

	//获取session中的token
	protected String token(HttpServletRequest request , String type){
		HttpSession session = request.getSession();
		String token = "";
		if (type.equalsIgnoreCase("corp")){
			token = (String) session.getAttribute("corpToken" + BaseController.VERSION);
		}
		if (type.equalsIgnoreCase("cust")){
			token = (String) session.getAttribute("customerToken" + BaseController.VERSION);
		}
		LOG.info("Token : " + token);
		return token;
	}
	
	protected String getWeibanToken() throws ServiceException {
		String url = "https://open.weibanzhushou.com/open-api/access_token/get";
		HashMap<String, String> uriVariablesMap = new HashMap<>();
		uriVariablesMap.put("corp_id", weibanCropId);
		uriVariablesMap.put("secret", weibanSecret);
		JSONObject weibanTokenJsonObject = restTemplate.postForObject(url, uriVariablesMap, JSONObject.class);
		if ((int) weibanTokenJsonObject.get("errcode") != 0) {
			ServiceException se = new ServiceException(weibanTokenJsonObject.get("errmsg").toString());
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		return weibanTokenJsonObject.get("access_token").toString();
	}
	
	protected boolean isCN(Integer regionId) {
		return ObjectUtil.isNotNull(regionId)
				&& (regionId == 52000 || regionId == 1000025 || regionId == 1000030 || regionId == 1000032);
	}
	
	protected boolean syncWeibanData(AdminUserLoginInfo loginInfo) throws ServiceException {
		// 同步企业微信客户数据
		if (loginInfo.getApList() != null && loginInfo.getApList().contains("GW")
				&& StringUtil.isNotEmpty(loginInfo.getOperUserid()) && loginInfo.getAdviserId() != null) {
			String url = StringUtil.merge("https://open.weibanzhushou.com/open-api/external_user/list?",
					"access_token={accessToken}", "&staff_id={staffId}", "&limit={limit}&offset={offset}",
					"&start_time={startTime}&end_time={endTime}");
			HashMap<String, Object> paramMap = new HashMap<>();
			paramMap.put("accessToken", getWeibanToken());
			paramMap.put("limit", 100);
			paramMap.put("offset", 0);
			paramMap.put("staffId", loginInfo.getOperUserid());
			paramMap.put("startTime", 1);
			paramMap.put("endTime", new Date().getTime() / 1000); // 毫秒转秒
			LOG.info("URL : " + url);
			LOG.info("Params : " + paramMap);
			JSONObject weibanUserListJsonObject = restTemplate.getForObject(url, JSONObject.class, paramMap);
			if (ObjectUtil.isNull(weibanUserListJsonObject)) {
				LOG.warn("'weibanUserListJsonObject' not exist !");
				return false;
			}
			LOG.info("weibanUserListJsonObject : " + weibanUserListJsonObject.toString());
			if ((int) weibanUserListJsonObject.get("errcode") != 0) {
				LOG.warn("调用微伴API异常!");
				return false;
			}
			if (!weibanUserListJsonObject.containsKey("external_user_list")) {
				LOG.warn("'external_user_list' not exist by Json !");
				return false;
			}
			JSONArray jsonArray = weibanUserListJsonObject.getJSONArray("external_user_list");
			if (ObjectUtil.isNull(jsonArray)) {
				LOG.warn("'jsonArray' is null : " + weibanUserListJsonObject.toString());
				return false;
			}
			for (int i = 0; i < jsonArray.size(); i++) {
				Map<String, Object> externalMap = JSON.parseObject(JSON.toJSONString(jsonArray.get(i)), Map.class);
				if (ObjectUtil.isNull(externalMap)) {
					LOG.warn("'externalMap' is null : " + jsonArray.get(i));
					continue;
				}
				if (!externalMap.containsKey("id")) {
					LOG.warn("'id' not exist : " + jsonArray.get(i));
					continue;
				}
				String externalUserid = externalMap.get("id").toString();
				QywxExternalUserDTO qywxExternalUserDto = qywxExternalUserService.getByExternalUserid(externalUserid);
				if (ObjectUtil.isNull(qywxExternalUserDto))
					qywxExternalUserDto = new QywxExternalUserDTO();
				// createtime
				if (externalMap.containsKey("created_at"))
					qywxExternalUserDto.setCreateTime((int) externalMap.get("created_at") * 1000);
				// adviserId
				qywxExternalUserDto.setAdviserId(loginInfo.getAdviserId());
				// externalUserid
				qywxExternalUserDto.setExternalUserid(externalUserid);
				// name
				if (externalMap.containsKey("name"))
					qywxExternalUserDto.setName(externalMap.get("name").toString());
				// type
				if (externalMap.containsKey("type"))
					qywxExternalUserDto.setType((int) externalMap.get("type"));
				// avatar
				if (externalMap.containsKey("avatar"))
					qywxExternalUserDto.setAvatar(externalMap.get("avatar").toString());
				// gender
				if (externalMap.containsKey("gender"))
					qywxExternalUserDto.setGender((int) externalMap.get("gender"));
				// unionid
				if (externalMap.containsKey("unionid"))
					qywxExternalUserDto.setUnionId(externalMap.get("unionid").toString());
				// state
				qywxExternalUserDto.setState("WCZ");
				QywxExternalUserDTO _qywxExternalUserDto = qywxExternalUserService
						.getByExternalUserid(qywxExternalUserDto.getExternalUserid());
				if (ObjectUtil.isNull(_qywxExternalUserDto)) {
					int qywxExtId = qywxExternalUserService.add(qywxExternalUserDto);
					if (qywxExtId > 0) {
						LOG.info(StringUtil.merge("New External User : ", qywxExternalUserDto.toString()));
						// 详情
						String url2 = StringUtil.merge("https://open.weibanzhushou.com/open-api/external_user/get?",
								"access_token={accessToken}", "&id={id}", "&unionid={unionid}");
						HashMap<String, Object> paramMap2 = new HashMap<>();
						paramMap2.put("accessToken", getWeibanToken());
						paramMap2.put("id", qywxExternalUserDto.getExternalUserid());
						paramMap2.put("unionid", qywxExternalUserDto.getUnionId());
						LOG.info("URL2 : " + url2);
						LOG.info("Params2 : " + paramMap2);
						JSONObject weibanUserJsonObject = restTemplate.getForObject(url2, JSONObject.class, paramMap2);
						if ((int) weibanUserJsonObject.get("errcode") != 0) {
							LOG.warn("调用微伴API异常!");
							return false;
						}
						LOG.info("weibanUserJsonObject : " + weibanUserJsonObject.toString());
						if (weibanUserJsonObject.containsKey("external_user")) {
							JSONObject externalUserJsonObject = weibanUserJsonObject.getJSONObject("external_user");
							if (externalUserJsonObject.containsKey("follow_staffs")) {
								JSONArray followStaffs = externalUserJsonObject.getJSONArray("follow_staffs");
								LOG.info(StringUtil.merge("followStaffs : ", followStaffs));
								for (int j = 0; j < followStaffs.size(); j++) {
									JSONObject staffJsonObject = followStaffs.getJSONObject(j);
									if (ObjectUtil.isNotNull(staffJsonObject)) {
										JSONArray customFields = staffJsonObject.getJSONArray("custom_fields");
										for (int k = 0; k < customFields.size(); k++) {
											JSONObject customField = customFields.getJSONObject(k);
											if (customField.containsKey("key")) {
												List<QywxExternalUserDescriptionDTO> descList = qywxExternalUserService
														.listDesc(qywxExternalUserDto.getExternalUserid(),
																customField.getString("name"));
												if (descList.size() > 0) {
													QywxExternalUserDescriptionDTO qywxExternalUserDescriptionDto = descList
															.get(0);
													qywxExternalUserDescriptionDto
															.setQywxValue(customField.getString("field_value"));
													if (qywxExternalUserService
															.updateDesc(qywxExternalUserDescriptionDto) > 0)
														LOG.info(StringUtil.merge("Update External User Desception : ",
																qywxExternalUserDescriptionDto.toString()));
												} else {
													QywxExternalUserDescriptionDTO qywxExternalUserDescriptionDto = new QywxExternalUserDescriptionDTO();
													qywxExternalUserDescriptionDto.setQywxExternalUserId(
															qywxExternalUserDto.getExternalUserid());
													qywxExternalUserDescriptionDto
															.setQywxKey(customField.getString("name"));
													qywxExternalUserDescriptionDto
															.setQywxValue(customField.getString("field_value"));
													if (qywxExternalUserService
															.addDesc(qywxExternalUserDescriptionDto) > 0)
														LOG.info(StringUtil.merge("New External User Desception : ",
																qywxExternalUserDescriptionDto.toString()));
												}
											} else
												LOG.error(StringUtil.merge("Custom Field Error : ",
														customField.toString()));
										}
									} else
										LOG.error("Staff Json Object is null !");
								}
							} else
								LOG.error("'follow_staffs' not exist !");
						} else
							LOG.error("'external_user' not exist !");
					} else
						LOG.error(
								StringUtil.merge("'qywxExternalUserDto' add error : ", qywxExternalUserDto.toString()));
				} else {
					qywxExternalUserDto.setId(_qywxExternalUserDto.getId());
					qywxExternalUserService.update(qywxExternalUserDto);
				}
			}
		}
		return true;
	}

	@Data
	protected class AdminUserLoginInfo {
		private int id;
		private String username;
		private String sessionId;
		private String apList;
		private Integer adviserId;
		private Integer maraId;
		private Integer officialId;
		private Integer kjId;
		private Integer regionId;
		private String operUserid;
		private String country; // CN or AU
		private boolean isOfficialAdmin;
		private boolean isAuth;
	}
}
