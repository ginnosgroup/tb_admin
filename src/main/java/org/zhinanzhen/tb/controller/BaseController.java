package org.zhinanzhen.tb.controller;

import java.io.File;
import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.zhinanzhen.tb.service.AdminUserService;

import com.ikasoa.core.utils.StringUtil;

import lombok.Data;

public class BaseController {

	private static final Logger LOG = LoggerFactory.getLogger(BaseController.class);
	
	public static final String VERSION = "2.1"; // TODO:Larry  版本号待移动到配置文件中

	@Resource
	protected AdminUserService adminUserService;

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
		if (adminUserLoginInfo != null)
			return adminUserLoginInfo.getApList() == null;
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
		return token;
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
		private boolean isOfficialAdmin;
		private boolean isAuth;
	}
}
