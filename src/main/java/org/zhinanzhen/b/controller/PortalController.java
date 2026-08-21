package org.zhinanzhen.b.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.zhinanzhen.b.service.PortalAttachmentService;
import org.zhinanzhen.b.service.PortalLogService;
import org.zhinanzhen.b.service.PortalService;
import org.zhinanzhen.b.service.PortalTypeService;
import org.zhinanzhen.b.service.pojo.PortalAttachmentDTO;
import org.zhinanzhen.b.service.pojo.PortalDTO;
import org.zhinanzhen.b.service.pojo.PortalLogDTO;
import org.zhinanzhen.b.service.pojo.PortalTypeDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.ListResponse;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.ErrorCodeEnum;
import com.ikasoa.core.utils.StringUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/portal")
public class PortalController extends BaseController {

	private static final Logger LOG = LoggerFactory.getLogger(PortalController.class);

	/**
	 * 语聚 AI 会话配置。API Key 在 application.properties 的 yuju.ai.api-key 中填写，
	 * ibotID 使用语聚 AI 助手 ID。
	 */
	@Value("${yuju.ai.ibot-id:2133_2622_jjyibotID_c67b59bc9fed4c4e9c6cf3a950b84f8e}")
	private String yujuAiIbotId;

	@Value("${yuju.ai.api-key:}")
	private String yujuAiApiKey;

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Resource
	PortalService portalService;

	@Resource
	PortalTypeService portalTypeService;

	@Resource
	PortalAttachmentService portalAttachmentService;

	@Resource
	PortalLogService portalLogService;

	@Resource
	ChartForAI chartForAI;

	@RequestMapping(value = "/attachment/upload", method = RequestMethod.POST)
	@ResponseBody
	public Response<String> uploadAttachment(@RequestParam MultipartFile file, HttpServletRequest request,
			HttpServletResponse response) throws IllegalStateException, IOException {
		super.setPostHeader(response);
		// 必须先读取文件字节：upload2 内部 transferTo 会移动 MultipartFile 的临时文件，
		// 之后再调 file.getBytes() 会因临时文件不存在而报错（AI提取要用原始字节）。
		byte[] fileBytes = file.getBytes();
		Response<String> uploadResp = super.upload2(file, request.getSession(), "/uploads/portal_attachment/");
		if (uploadResp.getCode() != 0) {
			return uploadResp;
		}
		// 在upload接口里就新增 b_portal_attachment 的数据
		PortalAttachmentDTO portalAttachmentDto = new PortalAttachmentDTO();
		portalAttachmentDto.setFileName(file.getOriginalFilename());
		portalAttachmentDto.setFilePath(uploadResp.getData());
		portalAttachmentDto.setFileSize(file.getSize());
		portalAttachmentDto.setFileType(file.getContentType());
		String originalName = file.getOriginalFilename();
		if (StringUtil.isNotEmpty(originalName) && originalName.contains("."))
			portalAttachmentDto.setFileExt(originalName.substring(originalName.lastIndexOf(".") + 1));
		portalAttachmentDto.setStage("apply");
		// 文件上传成功后调取 DeepSeek 提取附件文字并随附件入库（AI失败不影响上传主流程）
		portalAttachmentDto.setAiText(extractAttachmentText(fileBytes, file.getOriginalFilename()));
		try {
			if (portalAttachmentService.addPortalAttachment(portalAttachmentDto) <= 0) {
				super.deleteFile(uploadResp.getData()); // 入库失败则删除已上传文件
				return new Response<String>(1, "附件信息保存失败.", null);
			}
		} catch (ServiceException e) {
			super.deleteFile(uploadResp.getData());
			return new Response<String>(e.getCode(), e.getMessage(), null);
		}
		return new Response<String>(0, "", uploadResp.getData());
	}

	/**
	 * 调取 DeepSeek 提取附件（图片/PDF）中的文字。失败返回null，不影响附件上传主流程。
	 */
	private String extractAttachmentText(byte[] fileBytes, String filename) {
		try {
			Response<String> aiResponse = chartForAI.analyzeFile(fileBytes, filename, null);
			if (aiResponse != null && aiResponse.getCode() == 0) {
				return aiResponse.getData();
			}
			LOG.warn("附件AI文字提取失败，file={}，原因：{}", filename,
					aiResponse == null ? "无响应" : aiResponse.getMessage());
		} catch (Exception e) {
			LOG.error("附件AI文字提取异常，file={}", filename, e);
		}
		return null;
	}

	@RequestMapping(value = "/attachment/delete", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> deleteAttachment(@RequestParam(value = "id", required = false) Integer id,
			@RequestParam(value = "filePath", required = false) String filePath, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			if (id != null && id > 0) {
				// 已入库：按附件id删除
				PortalAttachmentDTO portalAttachmentDto = portalAttachmentService.getPortalAttachment(id);
				if (portalAttachmentDto == null) {
					return new Response<Integer>(1, "附件不存在.", 0);
				}
				Response<String> deleteResp = super
						.deleteFile(normalizeAttachmentFilePath(portalAttachmentDto.getFilePath()));
				portalAttachmentService.deletePortalAttachmentById(id);
				if (deleteResp != null && deleteResp.getCode() != 0) {
					return new Response<Integer>(1, "附件文件删除失败：" + deleteResp.getMessage() + "（已删除数据库记录）", 0);
				}
				return new Response<Integer>(0, id);
			} else if (StringUtil.isNotEmpty(filePath)) {
				// 已上传但未入库：按服务器上的路径删除文件
				Response<String> deleteResp = super.deleteFile(normalizeAttachmentFilePath(filePath));
				portalAttachmentService.deletePortalAttachmentByPath(filePath);
				if (deleteResp != null && deleteResp.getCode() != 0) {
					return new Response<Integer>(1, "附件文件删除失败：" + deleteResp.getMessage() + "（已删除数据库记录）", 0);
				}
				return new Response<Integer>(0, 0);
			} else {
				return new Response<Integer>(1, "参数错误：id和filePath至少传一个.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	/**
	 * 解析前端传的 hasCompletionLetter：兼容 "true"/"false" 和 "0"/"1" 两种写法，
	 * 空值/其他值一律按 false 处理。
	 */
	private static boolean parseHasCompletionLetter(String value) {
		if (StringUtil.isEmpty(value))
			return false;
		String v = value.trim();
		return "true".equalsIgnoreCase(v) || "1".equals(v);
	}

	/**
	 * 规范化附件文件路径后再交给 deleteFile 拼接 /data 前缀：
	 * 去掉 http(s)://域名 前缀、应用 context path（/admin_v2.1）前缀，
	 * 以及重复的 /data 前缀，避免路径对不上导致文件删不掉。
	 */
	private static String normalizeAttachmentFilePath(String filePath) {
		if (filePath == null) {
			return null;
		}
		// 统一把反斜杠转成正斜杠，便于后续判断前缀（\\data\\uploads\\... -> /data/uploads/...）
		String path = filePath.trim().replace('\\', '/');
		// 去掉 http(s)://host 前缀，只保留路径部分
		int schemeIndex = path.indexOf("://");
		if (schemeIndex >= 0) {
			int slashIndex = path.indexOf('/', schemeIndex + 3);
			if (slashIndex < 0) {
				return null; // 只有域名没有路径，无法定位文件
			}
			path = path.substring(slashIndex);
		}
		// 去掉应用 context path 前缀（如 /admin_v2.1/uploads/... -> /uploads/...）
		if (path.startsWith("/admin_v2.1/")) {
			path = path.substring("/admin_v2.1".length());
		}
		// 保留 /data 前缀：deleteFile 内部会判断是否重复拼接，并兜底按 Tomcat 工作目录查找
		return path;
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> addPortal(@RequestParam(value = "typeId") String typeId,
			@RequestParam(value = "name") String name,
			@RequestParam(value = "gender", required = false) String gender,
			@RequestParam(value = "birthday", required = false) String birthday,
			@RequestParam(value = "passport", required = false) String passport,
			@RequestParam(value = "englishScore", required = false) String englishScore,
			@RequestParam(value = "completionDate", required = false) String completionDate,
			@RequestParam(value = "visaExpirationDate", required = false) String visaExpirationDate,
			@RequestParam(value = "examResultsDate", required = false) String examResultsDate,
			@RequestParam(value = "studentVisaExpirationDate", required = false) String studentVisaExpirationDate,
			@RequestParam(value = "hasCompletionLetter", required = false) String hasCompletionLetter,
			@RequestParam(value = "jsonStr", required = false) String jsonStr,
			@RequestParam(value = "adviserId", required = false) String adviserId,
			@RequestParam(value = "officialId", required = false) String officialId,
			@RequestParam(value = "maraId", required = false) String maraId,
			@RequestParam(value = "serviceOrderId", required = false) String serviceOrderId,
			@RequestParam(value = "strState", required = false) String strState,
			@RequestParam(value = "filePath", required = false) String filePath, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			// 拦截：必须登录且是顾问（或超管）才能创建案件
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			if (adminUserLoginInfo == null)
				return new Response<Integer>(1, "请先登录.", 0);
			String apList = adminUserLoginInfo.getApList() == null ? ""
					: adminUserLoginInfo.getApList().toUpperCase(Locale.ENGLISH);
			if (!apList.contains("GW") && !apList.contains("SUPERAD"))
				return new Response<Integer>(1, "仅限顾问和超级管理员能创建案件.", 0);
			PortalDTO portalDto = new PortalDTO();
			portalDto.setTypeId(StringUtil.toInt(typeId));
			portalDto.setName(name);
			if (StringUtil.isNotEmpty(gender))
				portalDto.setGender(gender);
			if (StringUtil.isNotEmpty(birthday))
				portalDto.setBirthday(new Date(Long.parseLong(birthday.trim())));
			if (StringUtil.isNotEmpty(passport))
				portalDto.setPassport(passport);
			if (StringUtil.isNotEmpty(englishScore))
				portalDto.setEnglishScore(englishScore);
			if (StringUtil.isNotEmpty(completionDate))
				portalDto.setCompletionDate(new Date(Long.parseLong(completionDate.trim())));
			if (StringUtil.isNotEmpty(visaExpirationDate))
				portalDto.setVisaExpirationDate(new Date(Long.parseLong(visaExpirationDate.trim())));
			if (StringUtil.isNotEmpty(examResultsDate))
				portalDto.setExamResultsDate(new Date(Long.parseLong(examResultsDate.trim())));
			if (StringUtil.isNotEmpty(studentVisaExpirationDate))
				portalDto.setStudentVisaExpirationDate(new Date(Long.parseLong(studentVisaExpirationDate.trim())));
			// 未传默认false（没有完成信），避免insert时写入null违反非空约束
			portalDto.setHasCompletionLetter(parseHasCompletionLetter(hasCompletionLetter));
			if (StringUtil.isNotEmpty(jsonStr))
				portalDto.setJsonStr(jsonStr);
			// 顾问添加时取登录顾问的顾问id；超管添加时用前端传的adviserId
			if (apList.contains("GW") && adminUserLoginInfo.getAdviserId() != null
					&& adminUserLoginInfo.getAdviserId() > 0) {
				portalDto.setAdviserId(adminUserLoginInfo.getAdviserId());
			} else if (StringUtil.isNotEmpty(adviserId)) {
				portalDto.setAdviserId(StringUtil.toInt(adviserId));
			} else {
				return new Response<Integer>(1, "顾问信息缺失，无法创建案件.", 0);
			}
			if (StringUtil.isNotEmpty(officialId))
				portalDto.setOfficialId(StringUtil.toInt(officialId));
			if (StringUtil.isNotEmpty(maraId))
				portalDto.setMaraId(StringUtil.toInt(maraId));
			if (StringUtil.isNotEmpty(serviceOrderId))
				portalDto.setServiceOrderId(StringUtil.toInt(serviceOrderId));
			// 未传strState时默认"01"
			portalDto.setStrState(StringUtil.isNotEmpty(strState) ? strState : "01");
			if (portalService.addPortal(portalDto) > 0) {
				// 同步附件：根据addPortal传过来的路径，把已上传附件的portalId更新为新创建的案件ID
				syncPortalAttachments(filePath, portalDto.getId());
				// 操作日志：客户第一步入库
				savePortalLog(portalDto.getId(), "customer_first_submit", null, portalDto.getStrState(),
						"客户第一步提交案件", request);
				return new Response<Integer>(0, portalDto.getId());
			} else {
				return new Response<Integer>(1, "创建失败.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public Response<PortalDTO> updatePortal(@RequestParam(value = "id") int id,
			@RequestParam(value = "typeId", required = false) String typeId,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "gender", required = false) String gender,
			@RequestParam(value = "birthday", required = false) String birthday,
			@RequestParam(value = "passport", required = false) String passport,
			@RequestParam(value = "englishScore", required = false) String englishScore,
			@RequestParam(value = "completionDate", required = false) String completionDate,
			@RequestParam(value = "visaExpirationDate", required = false) String visaExpirationDate,
			@RequestParam(value = "examResultsDate", required = false) String examResultsDate,
			@RequestParam(value = "studentVisaExpirationDate", required = false) String studentVisaExpirationDate,
			@RequestParam(value = "hasCompletionLetter", required = false) String hasCompletionLetter,
			@RequestParam(value = "jsonStr", required = false) String jsonStr,
			@RequestParam(value = "adviserId", required = false) String adviserId,
			@RequestParam(value = "officialId", required = false) String officialId,
			@RequestParam(value = "maraId", required = false) String maraId,
			@RequestParam(value = "serviceOrderId", required = false) String serviceOrderId,
			@RequestParam(value = "strState", required = false) String strState,
			@RequestParam(value = "filePath", required = false) String filePath, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			PortalDTO portalDto = new PortalDTO();
			portalDto.setId(id);
			// 记录操作前状态（查不到时忽略，不影响主流程）
			String fromState = null;
			try {
				PortalDTO oldPortalDto = portalService.getPortal(id, null, null, null, null, null);
				if (oldPortalDto != null)
					fromState = oldPortalDto.getStrState();
			} catch (ServiceException ignored) {
			}
			if (StringUtil.isNotEmpty(typeId))
				portalDto.setTypeId(StringUtil.toInt(typeId));
			if (StringUtil.isNotEmpty(name))
				portalDto.setName(name);
			if (StringUtil.isNotEmpty(gender))
				portalDto.setGender(gender);
			if (StringUtil.isNotEmpty(birthday))
				portalDto.setBirthday(new Date(Long.parseLong(birthday.trim())));
			if (StringUtil.isNotEmpty(passport))
				portalDto.setPassport(passport);
			if (StringUtil.isNotEmpty(englishScore))
				portalDto.setEnglishScore(englishScore);
			if (StringUtil.isNotEmpty(completionDate))
				portalDto.setCompletionDate(new Date(Long.parseLong(completionDate.trim())));
			if (StringUtil.isNotEmpty(visaExpirationDate))
				portalDto.setVisaExpirationDate(new Date(Long.parseLong(visaExpirationDate.trim())));
			if (StringUtil.isNotEmpty(examResultsDate))
				portalDto.setExamResultsDate(new Date(Long.parseLong(examResultsDate.trim())));
			if (StringUtil.isNotEmpty(studentVisaExpirationDate))
				portalDto.setStudentVisaExpirationDate(new Date(Long.parseLong(studentVisaExpirationDate.trim())));
			if (StringUtil.isNotEmpty(hasCompletionLetter))
				portalDto.setHasCompletionLetter(parseHasCompletionLetter(hasCompletionLetter));
			if (StringUtil.isNotEmpty(jsonStr))
				portalDto.setJsonStr(jsonStr);
			if (StringUtil.isNotEmpty(adviserId))
				portalDto.setAdviserId(StringUtil.toInt(adviserId));
			if (StringUtil.isNotEmpty(officialId))
				portalDto.setOfficialId(StringUtil.toInt(officialId));
			if (StringUtil.isNotEmpty(maraId))
				portalDto.setMaraId(StringUtil.toInt(maraId));
			if (StringUtil.isNotEmpty(serviceOrderId))
				portalDto.setServiceOrderId(StringUtil.toInt(serviceOrderId));
			if (StringUtil.isNotEmpty(strState))
				portalDto.setStrState(strState);
			if (portalService.updatePortal(portalDto) > 0) {
				// 同步附件：根据updatePortal传过来的路径，把已上传附件的portalId更新为当前案件ID
				syncPortalAttachments(filePath, portalDto.getId());
				// 操作日志：更新案件
				String toState = StringUtil.isNotEmpty(strState) ? strState : fromState;
				savePortalLog(portalDto.getId(), "update", fromState, toState, "更新案件信息", request);
				// 案件更新成功后，按前端字段语义整理案件资料并提交给语聚AI进行485方案咨询。
				// AI调用失败不影响案件主流程，调用结果随案件数据一起返回。
				portalDto.setYujuAiResult(requestYujuAiAfterPortalUpdate(portalDto));
				return new Response<PortalDTO>(0, portalDto);
			} else {
				return new Response<PortalDTO>(1, "修改失败.", null);
			}
		} catch (ServiceException e) {
			return new Response<PortalDTO>(e.getCode(), e.getMessage(), null);
		}
	}

	/**
	 * 案件更新后调用语聚AI创建会话消息。这里重新查询一次数据库，确保使用的是更新后的完整案件资料，
	 * 而不是本次请求中可能只携带部分字段的 portalDto。
	 */
	private Map<String, Object> requestYujuAiAfterPortalUpdate(PortalDTO updatedPortalDto) {
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		if (updatedPortalDto == null || updatedPortalDto.getId() <= 0) {
			result.put("success", false);
			result.put("error", "案件信息无效，未调用语聚AI");
			return result;
		}
		try {
			PortalDTO portalDto = updatedPortalDto;
			try {
				PortalDTO savedPortalDto = portalService.getPortal(updatedPortalDto.getId(), null, null, null, null, null);
				if (savedPortalDto != null)
					portalDto = savedPortalDto;
			} catch (Exception e) {
				LOG.warn("查询更新后的案件资料失败，将使用本次更新字段继续请求语聚AI，portalId={}",
						updatedPortalDto.getId(), e);
			}
			if (StringUtil.isEmpty(yujuAiApiKey)) {
				LOG.warn("未配置 yuju.ai.api-key，跳过语聚AI请求，portalId={}", portalDto.getId());
				result.put("success", false);
				result.put("error", "未配置语聚AI API Key");
				return result;
			}
			if (StringUtil.isEmpty(yujuAiIbotId)) {
				LOG.warn("未配置 yuju.ai.ibot-id，跳过语聚AI请求，portalId={}", portalDto.getId());
				result.put("success", false);
				result.put("error", "未配置语聚AI助手ID");
				return result;
			}

			String instructions = buildYujuAiInstructions(portalDto);
			// 组装好的语聚AI请求content打印到控制台，便于联调核对
			System.out.println("语聚AI请求content: " + instructions);
			Map<String, Object> requestBody = new LinkedHashMap<String, Object>();
			requestBody.put("content", instructions);
			requestBody.put("ibotID", yujuAiIbotId.trim());
			requestBody.put("stream", false);
			String apiUrl = "https://chat.jijyun.cn/v1/openapi/chat?apiKey="
					+ URLEncoder.encode(yujuAiApiKey.trim(), "UTF-8");

			HttpURLConnection connection = null;
			try {
				connection = (HttpURLConnection) new URL(apiUrl).openConnection();
				connection.setRequestMethod("POST");
				connection.setConnectTimeout(15000);
				connection.setReadTimeout(30000);
				connection.setDoOutput(true);
				connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
				byte[] body = OBJECT_MAPPER.writeValueAsBytes(requestBody);
				LOG.info("语聚AI案件咨询请求，portalId={}，method=POST，requestHeaders={}，requestBody={}",
						portalDto.getId(), connection.getRequestProperties(),
						new String(body, StandardCharsets.UTF_8));
				OutputStream outputStream = null;
				try {
					outputStream = connection.getOutputStream();
					outputStream.write(body);
					outputStream.flush();
				} finally {
					if (outputStream != null)
						outputStream.close();
				}

				int responseCode = connection.getResponseCode();
				InputStream inputStream = responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream();
				String responseBody = readYujuAiResponse(inputStream);
				Object parsedResponse = parseYujuAiResponse(responseBody);
				result.put("success", responseCode >= 200 && responseCode < 300);
				result.put("httpCode", responseCode);
				result.put("response", parsedResponse);
				// 提取返回结果中的 content 存入数据库（保存失败不影响主流程）
				String aiContent = extractYujuAiContent(parsedResponse);
				if (StringUtil.isNotEmpty(aiContent)) {
					try {
						portalService.updateAiConsultContent(portalDto.getId(), aiContent);
						result.put("contentSaved", true);
					} catch (Exception e) {
						LOG.warn("保存语聚AI返回内容失败，portalId={}", portalDto.getId(), e);
						result.put("contentSaved", false);
					}
				} else {
					result.put("contentSaved", false);
				}
				if (responseCode >= 200 && responseCode < 300) {
					LOG.info("语聚AI案件咨询请求成功，portalId={}，httpCode={}，responseHeaders={}，response={}",
							portalDto.getId(), responseCode, connection.getHeaderFields(), responseBody);
				} else {
					LOG.warn("语聚AI案件咨询请求失败，portalId={}，httpCode={}，responseHeaders={}，response={}",
							portalDto.getId(), responseCode, connection.getHeaderFields(), responseBody);
				}
			} finally {
				if (connection != null)
					connection.disconnect();
			}
		} catch (Exception e) {
			LOG.error("调用语聚AI案件咨询接口异常，portalId={}", updatedPortalDto.getId(), e);
			result.put("success", false);
			result.put("error", "调用语聚AI案件咨询接口异常");
		}
		return result;
	}

	/**
	 * 从案件资料（PortalDTO + 前端提交的 jsonStr）中提取关键字段，
	 * 拼成"字段：值"形式的文本作为语聚AI的提问内容，例如：
	 * 姓名：DU MINGHUA，性别：Male，出生日期：23/06/1984，出生国家/地区：China，国籍：China，
	 * 婚姻状况：Married，签证到期日期：25/12/2027，是否有完成信：是，英语考试类型：IELTS。
	 * 请给我详细的485申请方案及材料清单。
	 */
	private String buildYujuAiInstructions(PortalDTO portalDto) {
		JsonNode formData = parsePortalFormData(portalDto);
		StringBuilder text = new StringBuilder();

//		appendAiField(text, "姓名", portalDto == null ? null : portalDto.getName());
//		appendAiField(text, "性别", portalDto == null ? null : portalDto.getGender());
		appendAiField(text, "出生日期", portalDto == null ? null : formatAiDate(portalDto.getBirthday()));
		appendAiField(text, "出生国家/地区", getJsonValue(formData, "basicInfo", "birthCountry"));
		appendAiField(text, "国籍", getJsonValue(formData, "basicInfo", "citCountry", "citiCountry", "nationality"));
		appendAiField(text, "婚姻状况", getJsonValue(formData, "basicInfo", "maritalStatus"));
		appendAiField(text, "签证到期日期", resolveVisaExpirationDate(formData, portalDto));
		appendAiField(text, "是否有完成信", hasEducationData(formData) ? "是" : "否");
		// 护照信息
		appendAiField(text, "护照号码", portalDto == null ? null : portalDto.getPassport());
		appendAiField(text, "护照签发国家", getJsonValue(formData, "passportInfo", "issueCountry"));
		appendAiField(text, "护照签发日期", aiDateText(getJsonNode(formData, "passportInfo", "issueDate")));
		appendAiField(text, "护照到期日期", aiDateText(getJsonNode(formData, "passportInfo", "expiryDate")));
		// 学习经历（多段，逐条转中文）
		appendEducationFields(text, getSectionNode(formData, "education"));
		// 语言考试
		appendAiField(text, "英语考试类型", joinLangTypes(getSectionNode(formData, "language")));
		appendAiField(text, "英语考试成绩", buildExamDetails(getSectionNode(formData, "language")));

		// jsonStr 解析失败时，兜底把原始 jsonStr 附上，避免信息丢失
		if (formData == null && portalDto != null && StringUtil.isNotEmpty(portalDto.getJsonStr()))
			appendAiField(text, "原始表单数据", portalDto.getJsonStr().trim());

		String content = text.toString().trim();
		if (content.isEmpty())
			return "请给我详细的485申请方案及材料清单。";
		return content + "。请给我详细的485申请方案及材料清单。";
	}

	/**
	 * 签证到期日期：优先取 jsonStr.basicInfo.visaExpirationDate（时间戳/日期字符串），
	 * 取不到再回退到 PortalDTO 的签证到期日期。
	 */
	private String resolveVisaExpirationDate(JsonNode formData, PortalDTO portalDto) {
		Date date = dateFromNode(getJsonNode(formData, "basicInfo", "visaExpirationDate"));
		if (date == null && portalDto != null)
			date = portalDto.getVisaExpirationDate();
		return formatAiDate(date);
	}

	/** 是否有完成信：jsonStr.education 有数据（数组非空/对象有字段）即为"有"。 */
	private boolean hasEducationData(JsonNode formData) {
		JsonNode node = getSectionNode(formData, "education");
		if (node == null)
			return false;
		if (node.isArray())
			return node.size() > 0;
		if (node.isObject())
			return node.size() > 0;
		return StringUtil.isNotEmpty(node.asText());
	}

	/** 取 jsonStr 的顶层分组节点（如 basicInfo / education / language）。 */
	private JsonNode getSectionNode(JsonNode formData, String section) {
		if (formData == null || !formData.isObject())
			return null;
		JsonNode node = formData.get(section);
		return (node == null || node.isNull()) ? null : node;
	}

	/** 英语考试类型：多门用顿号连接，如 "IELTS、OET"。 */
	private String joinLangTypes(JsonNode languageNode) {
		if (languageNode == null)
			return null;
		List<String> types = new ArrayList<String>();
		if (languageNode.isArray()) {
			for (JsonNode item : languageNode) {
				String type = getText(item, "langType");
				if (StringUtil.isNotEmpty(type))
					types.add(type);
			}
		} else {
			String type = getText(languageNode, "langType");
			if (StringUtil.isNotEmpty(type))
				types.add(type);
		}
		return types.isEmpty() ? null : String.join("、", types);
	}

	/** 每门语言考试的成绩明细，如 "IELTS（听力gg/阅读ss/写作ww/口语hh/总分ccc，考试日期25/06/2026）"。 */
	private String buildExamDetails(JsonNode languageNode) {
		if (languageNode == null)
			return null;
		StringBuilder result = new StringBuilder();
		boolean isArray = languageNode.isArray();
		int count = isArray ? languageNode.size() : 1;
		for (int i = 0; i < count; i++) {
			JsonNode item = isArray ? languageNode.get(i) : languageNode;
			if (item == null || !item.isObject())
				continue;
			String type = getText(item, "langType");
			if (StringUtil.isEmpty(type))
				continue;
			StringBuilder detail = new StringBuilder();
			appendExamDetail(detail, "听力", getText(item, "listening"));
			appendExamDetail(detail, "阅读", getText(item, "reading"));
			appendExamDetail(detail, "写作", getText(item, "writing"));
			appendExamDetail(detail, "口语", getText(item, "speaking"));
			appendExamDetail(detail, "总分", getText(item, "overall"));
			String testDateText = formatAiDate(dateFromNode(item.get("testDate")));
			if (testDateText != null)
				appendExamDetail(detail, "考试日期", testDateText);
			String hkPassport = getText(item, "hkPassport");
			if (StringUtil.isNotEmpty(hkPassport))
				appendExamDetail(detail, "香港护照", ("1".equals(hkPassport) || "true".equalsIgnoreCase(hkPassport)) ? "是" : "否");
			if (result.length() > 0)
				result.append("；");
			result.append(type);
			if (detail.length() > 0)
				result.append("（").append(detail).append("）");
		}
		return result.length() == 0 ? null : result.toString();
	}

	private void appendExamDetail(StringBuilder detail, String label, String value) {
		if (StringUtil.isEmpty(value))
			return;
		if (detail.length() > 0)
			detail.append("/");
		detail.append(label).append(value);
	}

	/** 学习经历：jsonStr.education 数组逐条转成中文文本，如 "学习经历1：学校名称fgff/CRICOS课程fff/学历类型Senior High School/开始日期18/08/2026"。 */
	private void appendEducationFields(StringBuilder text, JsonNode educationNode) {
		if (educationNode == null)
			return;
		boolean isArray = educationNode.isArray();
		int count = isArray ? educationNode.size() : 1;
		for (int i = 0; i < count; i++) {
			JsonNode item = isArray ? educationNode.get(i) : educationNode;
			if (item == null || !item.isObject())
				continue;
			StringBuilder edu = new StringBuilder();
			appendExamDetail(edu, "学校名称", getText(item, "auSchoolName"));
			appendExamDetail(edu, "CRICOS课程", getText(item, "cricosName"));
			appendExamDetail(edu, "学历类型", getText(item, "eduCourseType"));
			appendExamDetail(edu, "开始日期", aiDateText(item.get("eduStartDate")));
			appendExamDetail(edu, "完成日期", aiDateText(item.get("eduEndDate")));
			appendExamDetail(edu, "课程完成信日期", aiDateText(item.get("eduCourseCompletionDate")));
			appendExamDetail(edu, "技能评估", getText(item, "skillAssessment"));
			if (edu.length() == 0)
				continue;
			String label = count > 1 ? "学习经历" + (i + 1) : "学习经历";
			if (text.length() > 0)
				text.append("，");
			text.append(label).append("：").append(edu);
		}
	}

	/** jsonStr 里的日期节点转成 AI 提问用的 dd/MM/yyyy 文本（无效值返回 null）。 */
	private String aiDateText(JsonNode node) {
		return formatAiDate(dateFromNode(node));
	}

	/** 取节点文本值（字符串/数字/布尔），null 或空返回 null。 */
	private String getText(JsonNode node, String field) {
		JsonNode value = node == null ? null : node.get(field);
		if (value == null || value.isNull())
			return null;
		if (value.isTextual())
			return value.asText();
		if (value.isNumber() || value.isBoolean())
			return value.asText();
		return null;
	}

	/** 把 jsonStr 里的日期节点（毫秒/秒时间戳、yyyy-MM-dd、dd/MM/yyyy）转成 Date。 */
	private Date dateFromNode(JsonNode node) {
		if (node == null || node.isNull())
			return null;
		if (node.isNumber()) {
			long timestamp = node.asLong();
			if (timestamp <= 0)
				return null; // 0/负数视为未填，避免解析成 1970-01-01
			if (timestamp < 100000000000L)
				timestamp *= 1000L;
			return new Date(timestamp);
		}
		if (node.isTextual()) {
			String text = node.asText().trim();
			if (text.matches("-?\\d{10,13}")) {
				long timestamp = Long.parseLong(text);
				if (timestamp <= 0)
					return null; // 0/负数视为未填
				if (text.replace("-", "").length() <= 10)
					timestamp *= 1000L;
				return new Date(timestamp);
			}
			return tryParseDateText(text);
		}
		return null;
	}

	/** 简单兼容 yyyy-MM-dd / dd/MM/yyyy 两种文本日期。 */
	private Date tryParseDateText(String text) {
		try {
			if (text.matches("\\d{4}-\\d{2}-\\d{2}"))
				return new java.text.SimpleDateFormat("yyyy-MM-dd").parse(text);
			if (text.matches("\\d{2}/\\d{2}/\\d{4}"))
				return new java.text.SimpleDateFormat("dd/MM/yyyy").parse(text);
		} catch (Exception e) {
			// 忽略无法解析的文本日期
		}
		return null;
	}

	private JsonNode parsePortalFormData(PortalDTO portalDto) {
		if (portalDto == null || StringUtil.isEmpty(portalDto.getJsonStr()))
			return null;
		String jsonStr = portalDto.getJsonStr().trim();
		try {
			return readPortalFormData(jsonStr);
		} catch (Exception firstException) {
			// 兼容前端把整个 JSON 再次转义后提交的 {\"field\":\"value\"} 格式。
			String unescapedJson = jsonStr.replace("\\\"", "\"");
			if (!jsonStr.equals(unescapedJson)) {
				try {
					return readPortalFormData(unescapedJson);
				} catch (Exception secondException) {
					LOG.warn("解析案件jsonStr失败，将原始数据提交给语聚AI，portalId={}", portalDto.getId(),
							secondException);
					return null;
				}
			}
			LOG.warn("解析案件jsonStr失败，将原始数据提交给语聚AI，portalId={}", portalDto.getId(),
					firstException);
			return null;
		}
	}

	private JsonNode readPortalFormData(String jsonStr) throws IOException {
		JsonNode node = OBJECT_MAPPER.readTree(jsonStr);
		if (node != null && node.isTextual() && StringUtil.isNotEmpty(node.asText()))
			return OBJECT_MAPPER.readTree(node.asText());
		return node;
	}

	private Object getJsonValue(JsonNode root, String section, String field, String... flatAliases) {
		JsonNode node = getJsonNode(root, section, field, flatAliases);
		if (node == null || node.isNull())
			return null;
		if (node.isTextual())
			return node.asText();
		if (node.isNumber())
			return node.numberValue();
		if (node.isBoolean())
			return node.asBoolean();
		return node.toString();
	}

	private String getJsonDateValue(JsonNode root, String section, String field, String... flatAliases) {
		JsonNode node = getJsonNode(root, section, field, flatAliases);
		if (node == null || node.isNull())
			return null;
		String value = node.asText();
		if (value != null && value.matches("-?\\d{10,13}")) {
			try {
				long timestamp = Long.parseLong(value);
				if (value.replace("-", "").length() <= 10)
					timestamp *= 1000L;
				return formatDate(new Date(timestamp));
			} catch (NumberFormatException e) {
				// 非时间戳格式时保留前端提交的原值。
			}
		}
		return value;
	}

	private JsonNode getJsonNode(JsonNode root, String section, String field, String... flatAliases) {
		if (root == null)
			return null;
		JsonNode sectionNode = root.get(section);
		if (sectionNode != null && !sectionNode.isNull()) {
			JsonNode nestedNode = sectionNode.get(field);
			if (nestedNode != null && !nestedNode.isNull())
				return nestedNode;
		}
		JsonNode flatNode = root.get(field);
		if (flatNode != null && !flatNode.isNull())
			return flatNode;
		if (flatAliases != null) {
			for (String alias : flatAliases) {
				JsonNode aliasNode = root.get(alias);
				if (aliasNode != null && !aliasNode.isNull())
					return aliasNode;
			}
		}
		return null;
	}

	private String formatDate(Date date) {
		return date == null ? null : new java.text.SimpleDateFormat("yyyy-MM-dd").format(date);
	}

	/** 语聚AI提问内容里的日期格式，与示例保持一致。 */
	private String formatAiDate(Date date) {
		return date == null ? null : new java.text.SimpleDateFormat("dd/MM/yyyy").format(date);
	}

	/** 拼接"字段：值"，多个字段用中文逗号分隔。 */
	private void appendAiField(StringBuilder text, String label, Object value) {
		if (value == null || StringUtil.isEmpty(String.valueOf(value)))
			return;
		if (text.length() > 0)
			text.append("，");
		text.append(label).append("：").append(value);
	}

	/**
	 * 从语聚AI返回结果中提取 content 字段（兼容 {"content":...}、{"data":{"content":...}}、
	 * {"choices":[{"message":{"content":...}}]} 等结构，递归查找第一个文本 content）。
	 */
	private String extractYujuAiContent(Object response) {
		if (response == null)
			return null;
		try {
			JsonNode node = OBJECT_MAPPER.valueToTree(response);
			return findFirstContentText(node);
		} catch (Exception e) {
			LOG.warn("解析语聚AI返回的content失败", e);
			return null;
		}
	}

	/** 递归查找第一个文本类型的 content 字段值。 */
	private String findFirstContentText(JsonNode node) {
		if (node == null || node.isNull())
			return null;
		if (node.isObject()) {
			Iterator<Map.Entry<String, JsonNode>> iterator = node.fields();
			while (iterator.hasNext()) {
				Map.Entry<String, JsonNode> entry = iterator.next();
				if ("content".equals(entry.getKey()) && entry.getValue() != null
						&& entry.getValue().isTextual()
						&& StringUtil.isNotEmpty(entry.getValue().asText()))
					return entry.getValue().asText();
			}
			iterator = node.fields();
			while (iterator.hasNext()) {
				Map.Entry<String, JsonNode> entry = iterator.next();
				String found = findFirstContentText(entry.getValue());
				if (found != null)
					return found;
			}
		} else if (node.isArray()) {
			for (JsonNode item : node) {
				String found = findFirstContentText(item);
				if (found != null)
					return found;
			}
		}
		return null;
	}

	private Object parseYujuAiResponse(String responseBody) {
		if (StringUtil.isEmpty(responseBody))
			return "";
		try {
			return OBJECT_MAPPER.readValue(responseBody, Object.class);
		} catch (Exception e) {
			return responseBody;
		}
	}

	private String readYujuAiResponse(InputStream inputStream) throws IOException {
		if (inputStream == null)
			return "";
		StringBuilder response = new StringBuilder();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
			String line;
			while ((line = reader.readLine()) != null)
				response.append(line);
		} finally {
			if (reader != null)
				reader.close();
		}
		return response.toString();
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public ListResponse<List<PortalDTO>> listPortal(@RequestParam(value = "typeId", required = false) Integer typeId,
			@RequestParam(value = "strState", required = false) String strState,
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			// strState=ALL 表示查询全部案件，转成null不按状态过滤
			if ("ALL".equalsIgnoreCase(strState))
				strState = null;
			// 数据权限过滤：顾问查自己名下，顾问管理员查同地区所有顾问，文案同理，mara查自己名下，超管查全部
			PortalAccessFilter filter = buildAccessFilter(request);
			int total = portalService.countPortal(typeId, strState, keyword, filter.adviserId, filter.adviserRegionId,
					filter.officialId, filter.officialRegionId, filter.maraId);
			List<PortalDTO> portalDtoList = portalService.listPortal(typeId, strState, keyword, pageNum, pageSize,
					filter.adviserId, filter.adviserRegionId, filter.officialId, filter.officialRegionId,
					filter.maraId);
			// 与 /get 保持一致：按 portal_id 关联查询附件列表，组装进每个案件一起返回
			if (portalDtoList != null) {
				for (PortalDTO portalDto : portalDtoList) {
					portalDto.setPortalAttachmentList(
							portalAttachmentService.listPortalAttachmentByPortalId(portalDto.getId()));
				}
			}
			return new ListResponse<List<PortalDTO>>(true, pageSize, total, portalDtoList, "");
		} catch (ServiceException e) {
			return new ListResponse<List<PortalDTO>>(false, pageSize, 0, null, e.getMessage());
		}
	}

	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> deletePortal(@RequestParam(value = "id") int id, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			// 记录操作前状态（查不到时忽略，不影响主流程）
			String fromState = null;
			try {
				PortalDTO oldPortalDto = portalService.getPortal(id, null, null, null, null, null);
				if (oldPortalDto != null)
					fromState = oldPortalDto.getStrState();
			} catch (ServiceException ignored) {
			}
			if (portalService.deletePortal(id) > 0) {
				// 操作日志：删除案件
				savePortalLog(id, "delete", fromState, null, "删除案件", request);
				return new Response<Integer>(0, id);
			} else {
				return new Response<Integer>(1, "删除失败.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/log/list", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<PortalLogDTO>> listPortalLog(
			@RequestParam(value = "portalId", required = false) Integer portalId,
			@RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<List<PortalLogDTO>>(0, portalLogService.listPortalLog(portalId, pageNum, pageSize));
		} catch (ServiceException e) {
			return new Response<List<PortalLogDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/log/count", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> countPortalLog(@RequestParam(value = "portalId", required = false) Integer portalId,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, portalLogService.countPortalLog(portalId));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), null);
		}
	}

	/**
	 * 保存案件操作日志（日志记录失败不影响主流程）
	 */
	private void savePortalLog(int portalId, String action, String fromState, String toState, String content,
			HttpServletRequest request) {
		try {
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			PortalLogDTO portalLogDto = new PortalLogDTO();
			portalLogDto.setPortalId(portalId);
			portalLogDto.setAction(action);
			portalLogDto.setFromState(fromState);
			portalLogDto.setToState(toState);
			portalLogDto.setContent(content);
			portalLogDto.setIp(request.getRemoteAddr());
			portalLogDto.setUserAgent(request.getHeader("User-Agent"));
			if (adminUserLoginInfo != null) {
				portalLogDto.setOperatorId(adminUserLoginInfo.getId());
				portalLogDto.setOperatorName(adminUserLoginInfo.getUsername());
				portalLogDto.setRole(getRoleName(adminUserLoginInfo.getApList()));
			} else {
				// 未登录的操作视为客户
				portalLogDto.setRole("客户");
			}
			portalLogService.addPortalLog(portalLogDto);
		} catch (Exception e) {
			LOG.error("保存案件操作日志失败, portalId=" + portalId + ", action=" + action, e);
		}
	}

	@RequestMapping(value = "/get", method = RequestMethod.GET)
	@ResponseBody
	public Response<PortalDTO> getPortal(@RequestParam(value = "id") Integer id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			// 此接口不需要验证登录，不做数据权限过滤
			PortalDTO portalDto = portalService.getPortal(id, null, null, null, null, null);
			if (portalDto != null) {
				// 按 portal_id 关联查询附件列表，组装进 PortalDTO 一起返回
				portalDto.setPortalAttachmentList(
						portalAttachmentService.listPortalAttachmentByPortalId(portalDto.getId()));
			}
			return new Response<PortalDTO>(0, "", portalDto);
		} catch (ServiceException e) {
			// 没查询到数据时直接返回空message，不返回"No data"
			if (ErrorCodeEnum.DATA_ERROR.code() == e.getCode())
				return new Response<PortalDTO>(0, "", null);
			return new Response<PortalDTO>(1, e.getMessage(), null);
		}
	}

	/**
	 * 案件查询数据权限过滤器：顾问查自己名下；顾问管理员(regionId>0)查同地区所有顾问；
	 * 文案查自己名下；文案管理员(isOfficialAdmin)查同地区所有文案；mara查自己名下；超管查全部。
	 * 未登录直接拒绝（抛出ServiceException）。
	 */
	private static class PortalAccessFilter {
		Integer adviserId;
		Integer adviserRegionId;
		Integer officialId;
		Integer officialRegionId;
		Integer maraId;
	}

	private PortalAccessFilter buildAccessFilter(HttpServletRequest request) throws ServiceException {
		PortalAccessFilter filter = new PortalAccessFilter();
		AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
		if (adminUserLoginInfo == null) {
			ServiceException se = new ServiceException("请先登录.");
			se.setCode(1);
			throw se;
		}
		String apList = adminUserLoginInfo.getApList() == null ? ""
				: adminUserLoginInfo.getApList().toUpperCase(Locale.ENGLISH);
		if (apList.contains("SUPERAD")) {
			// 超管查全部，不加过滤
			return filter;
		}
		if (apList.contains("GW")) {
			if (adminUserLoginInfo.getRegionId() != null && adminUserLoginInfo.getRegionId() > 0) {
				// 顾问管理员：查同地区（含子地区）所有顾问的记录
				filter.adviserRegionId = adminUserLoginInfo.getRegionId();
			} else {
				// 普通顾问：查自己名下
				filter.adviserId = adminUserLoginInfo.getAdviserId();
			}
		}
		if (apList.contains("WA")) {
			if (adminUserLoginInfo.isOfficialAdmin()) {
				// 文案管理员：查同地区（含子地区）所有文案的记录
				filter.officialRegionId = adminUserLoginInfo.getRegionId();
			} else {
				// 普通文案：查自己名下
				filter.officialId = adminUserLoginInfo.getOfficialId();
			}
		}
		if (apList.contains("MA")) {
			// mara：查自己名下
			filter.maraId = adminUserLoginInfo.getMaraId();
		}
		return filter;
	}

	/**
	 * 根据apList转换为操作人角色：GW=顾问, WA=文案, MA=mara, SUPERAD=超管
	 */
	private String getRoleName(String apList) {
		if (StringUtil.isEmpty(apList))
			return null;
		String ap = apList.toUpperCase();
		if (ap.contains("SUPERAD"))
			return "超管";
		if (ap.contains("MA"))
			return "mara";
		if (ap.contains("WA"))
			return "文案";
		if (ap.contains("GW"))
			return "顾问";
		return null;
	}

	/**
	 * 同步案件附件：把已上传附件（portalId为空）按路径批量挂到案件上。
	 * filePath支持多个路径，用英文逗号","或中文逗号"，"连接（一个案件可能有多个附件），
	 * 内部做去空格、去空串、去重。
	 */
	private void syncPortalAttachments(String filePath, int portalId) throws ServiceException {
		if (StringUtil.isEmpty(filePath))
			return;
		List<String> filePathList = Arrays.stream(filePath.split("[,，]")).map(String::trim)
				.filter(StringUtil::isNotEmpty).distinct().collect(Collectors.toList());
		if (!filePathList.isEmpty())
			portalAttachmentService.updatePortalIdByPathList(filePathList, portalId);
	}

	@RequestMapping(value = "/type/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> addPortalType(@RequestParam(value = "name") String name,
			@RequestParam(value = "description", required = false) String description,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "isDelete", required = false) String isDelete, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			PortalTypeDTO portalTypeDto = new PortalTypeDTO();
			portalTypeDto.setName(name);
			if (StringUtil.isNotEmpty(description))
				portalTypeDto.setDescription(description);
			if (StringUtil.isNotEmpty(sort))
				portalTypeDto.setSort(StringUtil.toInt(sort));
			// 未传默认0（未删除），避免insert时写入null违反非空约束
			portalTypeDto.setIsDelete("1".equals(isDelete) ? 1 : 0);
			if (portalTypeService.addPortalType(portalTypeDto) > 0) {
				return new Response<Integer>(0, portalTypeDto.getId());
			} else {
				return new Response<Integer>(1, "创建失败.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/type/update", method = RequestMethod.POST)
	@ResponseBody
	public Response<PortalTypeDTO> updatePortalType(@RequestParam(value = "id") int id,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "description", required = false) String description,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "isDelete", required = false) String isDelete, HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			PortalTypeDTO portalTypeDto = new PortalTypeDTO();
			portalTypeDto.setId(id);
			if (StringUtil.isNotEmpty(name))
				portalTypeDto.setName(name);
			if (StringUtil.isNotEmpty(description))
				portalTypeDto.setDescription(description);
			if (StringUtil.isNotEmpty(sort))
				portalTypeDto.setSort(StringUtil.toInt(sort));
			if (StringUtil.isNotEmpty(isDelete))
				portalTypeDto.setIsDelete("1".equals(isDelete) ? 1 : 0);
			if (portalTypeService.updatePortalType(portalTypeDto) > 0) {
				return new Response<PortalTypeDTO>(0, portalTypeDto);
			} else {
				return new Response<PortalTypeDTO>(1, "修改失败.", null);
			}
		} catch (ServiceException e) {
			return new Response<PortalTypeDTO>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/type/list", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<PortalTypeDTO>> listPortalType(
			@RequestParam(value = "isDelete", required = false) Integer isDelete,
			@RequestParam(value = "keyword", required = false) String keyword, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<List<PortalTypeDTO>>(0, portalTypeService.listPortalType(isDelete, keyword));
		} catch (ServiceException e) {
			return new Response<List<PortalTypeDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/type/get", method = RequestMethod.GET)
	@ResponseBody
	public Response<PortalTypeDTO> getPortalType(@RequestParam(value = "id") Integer id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<PortalTypeDTO>(0, portalTypeService.getPortalType(id));
		} catch (ServiceException e) {
			return new Response<PortalTypeDTO>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/type/delete", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> deletePortalType(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, portalTypeService.deletePortalType(id));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

}
