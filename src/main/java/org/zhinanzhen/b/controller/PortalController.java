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
import java.util.Arrays;
import java.util.Date;
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

	@RequestMapping(value = "/attachment/upload", method = RequestMethod.POST)
	@ResponseBody
	public Response<String> uploadAttachment(@RequestParam MultipartFile file, HttpServletRequest request,
			HttpServletResponse response) throws IllegalStateException, IOException {
		super.setPostHeader(response);
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
				// 案件更新成功后，仅将更新后的 jsonStr 提交给语聚AI进行485方案咨询。
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
	 * 案件更新后调用语聚AI创建会话消息。这里重新查询一次数据库，确保使用的是更新后完整的 jsonStr，
	 * 而不是本次请求中可能未携带 jsonStr 的 portalDto。
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
				result.put("success", responseCode >= 200 && responseCode < 300);
				result.put("httpCode", responseCode);
				result.put("response", parseYujuAiResponse(responseBody));
				if (responseCode >= 200 && responseCode < 300) {
					LOG.info("语聚AI案件咨询请求成功，portalId={}，response={}", portalDto.getId(), responseBody);
				} else {
					LOG.warn("语聚AI案件咨询请求失败，portalId={}，httpCode={}，response={}", portalDto.getId(),
							responseCode, responseBody);
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
	 * 仅使用 jsonStr 的原始值作为案件资料，并追加485申请方案提问词。
	 */
	private String buildYujuAiInstructions(PortalDTO portalDto) {
		StringBuilder prompt = new StringBuilder();
		if (portalDto != null && StringUtil.isNotEmpty(portalDto.getJsonStr()))
			prompt.append(portalDto.getJsonStr().trim()).append("\n\n");
		prompt.append("请给我详细的485申请方案及材料清单。");
		return prompt.toString();
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
