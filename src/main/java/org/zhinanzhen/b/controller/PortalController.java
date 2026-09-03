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
import java.security.MessageDigest;
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
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
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
import org.zhinanzhen.b.service.PortalDocumentService;
import org.zhinanzhen.b.service.PortalLogService;
import org.zhinanzhen.b.service.PortalService;
import org.zhinanzhen.b.service.PortalTypeService;
import org.zhinanzhen.b.service.MaraService;
import org.zhinanzhen.b.service.pojo.PortalAttachmentDTO;
import org.zhinanzhen.b.service.pojo.PortalDTO;
import org.zhinanzhen.b.service.pojo.PortalLogDTO;
import org.zhinanzhen.b.service.pojo.PortalTypeDTO;
import org.zhinanzhen.b.service.pojo.MaraDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.ListResponse;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.dao.AdviserDAO;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.ErrorCodeEnum;
import com.ikasoa.core.utils.StringUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

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

	/** 客户合同确认链接签名密钥；生产环境建议通过 portal.customer-action-secret 单独配置。 */
	@Value("${portal.customer-action-secret:}")
	private String portalCustomerActionSecret;

	/** 客户合同按钮调用的基础地址；本地测试使用 http://localhost:8081/admin_v2.1。 */
	@Value("${portal.customer-action-base-url:}")
	private String portalCustomerActionBaseUrl;

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final String PASSPORT_JSON_PROMPT =
			"请识别并解析这份护照，只返回一个合法的JSON对象。\n"
					+ "不要返回Markdown代码块、解释、原始全文或JSON之外的任何内容。\n"
					+ "必须严格使用下面的字段名称和层级：\n"
					+ "{"
					+ "\"documentType\":\"Passport\","
					+ "\"passportDetails\":{"
					+ "\"passportNumber\":null,\"passportType\":null,\"countryCode\":null,"
					+ "\"issuingCountry\":null,\"familyName\":null,\"givenNames\":null,\"fullName\":null,"
					+ "\"nationality\":null,\"dateOfBirth\":null,\"sex\":null,\"placeOfBirth\":null,"
					+ "\"dateOfIssue\":null,\"dateOfExpiry\":null,\"issuingAuthority\":null,\"personalNumber\":null},"
					+ "\"machineReadableZone\":{\"line1\":null,\"line2\":null}}\n"
					+ "规则：\n"
					+ "1. 只能根据护照可见内容和机读区提取，不要猜测；缺失字段返回null。\n"
					+ "2. 日期统一为yyyy-MM-dd。\n"
					+ "3. fullName按givenNames在前、familyName在后的顺序生成。\n"
					+ "4. 护照号码、姓名、出生日期和有效期必须优先核对机读区，避免字母和数字混淆。";

	private static final String COMPLETION_JSON_PROMPT =
			"请识别并解析这份课程完成信（Completion Letter），只提取完成日期并返回一个合法的JSON对象。\n"
					+ "不要返回Markdown代码块、解释、原始全文或JSON之外的任何内容。\n"
					+ "必须严格返回下面的结构，禁止增加其他字段：\n"
					+ "{\"completionDate\":null}\n"
					+ "规则：\n"
					+ "1. completionDate必须始终出现在JSON中。\n"
					+ "2. completionDate应取学生正式完成课程或满足课程要求的日期；不要用信件签发日期代替。\n"
					+ "3. 如果文件同时出现课程结束日期和明确的完成日期，优先使用明确的完成日期。\n"
					+ "4. 日期统一为yyyy-MM-dd；只能根据文件内容提取，不要猜测，确实未找到时返回null。";

	private static final String COE_JSON_PROMPT =
			"请识别并解析这份澳大利亚海外学生入学确认书（CoE），只返回一个合法的JSON对象。\n"
					+ "不要返回Markdown代码块、解释、原始全文或JSON之外的任何内容。\n"
					+ "必须严格使用下面的字段名称和层级：\n"
					+ "{"
					+ "\"coeNumber\":null,"
					+ "\"documentType\":null,"
					+ "\"courseDetails\":{"
					+ "\"provider\":null,\"providerCode\":null,\"tradingAs\":[],"
					+ "\"telephone\":null,\"fax\":null,\"email\":null,"
					+ "\"courseName\":null,\"courseCode\":null,\"courseLevel\":null,"
					+ "\"courseStartDate\":null,\"courseEndDate\":null,"
					+ "\"initialPrePaidTuitionFee\":{\"currency\":null,\"amount\":null,\"fromDate\":null,\"toDate\":null},"
					+ "\"otherPrePaidNonTuitionFee\":{\"currency\":null,\"amount\":null},"
					+ "\"totalTuitionFee\":{\"currency\":null,\"amount\":null}},"
					+ "\"studentDetails\":{"
					+ "\"providerStudentId\":null,\"courtesyTitle\":null,\"familyName\":null,"
					+ "\"givenNames\":null,\"fullName\":null,\"gender\":null,\"dateOfBirth\":null,"
					+ "\"countryOfBirth\":null,\"nationality\":null,\"providerArrangedOSHC\":null,"
					+ "\"englishTest\":{\"type\":null,\"score\":null,\"testDate\":null},\"comments\":null},"
					+ "\"importantInformation\":{"
					+ "\"isVisa\":false,\"visaExtension\":false,\"reminders\":[],"
					+ "\"links\":{\"vevo\":null,\"studentVisa\":null,\"cricos\":null,\"studyAustralia\":null}},"
					+ "\"createdAt\":null,\"updatedAt\":null}\n"
					+ "规则：\n"
					+ "1. 只能根据文件内容提取，不要猜测；缺失的普通字段返回null，缺失的数组返回空数组。\n"
					+ "2. 日期统一为yyyy-MM-dd；createdAt和updatedAt使用yyyy-MM-dd HH:mm:ss。\n"
					+ "3. 金额和考试成绩必须是JSON数字，不要包含币种符号、逗号或单位。\n"
					+ "4. providerArrangedOSHC、isVisa和visaExtension必须是JSON布尔值。\n"
					+ "5. currency统一使用三位币种代码，例如AUD。\n"
					+ "6. links中的网址必须是普通URL字符串，禁止返回Markdown链接、反斜杠或转义后的括号。\n"
					+ "7. fullName按givenNames在前、familyName在后的顺序生成。";

	@Resource
	PortalService portalService;

	@Resource
	PortalTypeService portalTypeService;

	@Resource
	PortalAttachmentService portalAttachmentService;

	@Resource
	PortalLogService portalLogService;

	@Resource
	PortalDocumentService portalDocumentService;

	@Resource
	ChartForAI chartForAI;

	@Resource
	MaraService maraService;

	@Resource
	AdviserDAO adviserDao;

	@RequestMapping(value = "/attachment/upload", method = RequestMethod.POST)
	@ResponseBody
	public Response<Map<String, Object>> uploadAttachment(@RequestParam MultipartFile file,
			@RequestParam(value = "aiText", required = false) String aiText,
			@RequestParam(value = "fileType", required = false) String fileType,
			@RequestParam(value = "maraId", required = false) String maraId,
			HttpServletRequest request, HttpServletResponse response) throws IllegalStateException, IOException {
		super.setPostHeader(response);
		boolean signatureUpload = aiText == null && fileType != null
				&& "signature".equalsIgnoreCase(fileType.trim());
		MaraDTO maraDto = null;
		String oldSignatureData = null;
		if (signatureUpload) {
			Integer targetMaraId = StringUtil.isNotEmpty(maraId) ? StringUtil.toInt(maraId.trim()) : null;
			if (targetMaraId == null || targetMaraId <= 0)
				return new Response<Map<String, Object>>(1, "maraId不能为空且必须是有效数字，无法上传签名文件.", null);
			try {
				maraDto = maraService.getMaraById(targetMaraId);
			} catch (ServiceException e) {
				return new Response<Map<String, Object>>(e.getCode(), e.getMessage(), null);
			}
			if (maraDto == null) {
				return new Response<Map<String, Object>>(1, "MARA不存在，无法保存签名文件.", null);
			}
			oldSignatureData = maraDto.getSignatureData();
		}
		String normalizedFileType = null;
		if (aiText != null) {
			normalizedFileType = normalizeAttachmentFileType(fileType);
			if (normalizedFileType == null) {
				return new Response<Map<String, Object>>(1,
						"调用AI识别时fileType必须是passport、completion或coe.", null);
			}
		}
		// 只有传入aiText参数时才读取原始文件并调用AI。必须在upload2之前读取，
		// 因为upload2内部transferTo会移动MultipartFile的临时文件。
		byte[] fileBytes = aiText == null ? null : file.getBytes();
		Response<String> uploadResp = super.upload2(file, request.getSession(), "/uploads/portal_attachment/");
		if (uploadResp == null) {
			return new Response<Map<String, Object>>(1, "附件上传失败.", null);
		}
		if (uploadResp.getCode() != 0) {
			return new Response<Map<String, Object>>(uploadResp.getCode(), uploadResp.getMessage(), null);
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
		// 传入aiText参数时才提取附件文字并随附件入库（AI失败不影响上传主流程）。
		if (aiText != null) {
			portalAttachmentDto
					.setAiText(extractAttachmentText(fileBytes, file.getOriginalFilename(), normalizedFileType));
		}
		int attachmentId = 0;
		try {
			attachmentId = portalAttachmentService.addPortalAttachment(portalAttachmentDto);
			if (attachmentId <= 0) {
				super.deleteFile(uploadResp.getData()); // 入库失败则删除已上传文件
				return new Response<Map<String, Object>>(1, "附件信息保存失败.", null);
			}
			if (signatureUpload) {
				maraDto.setSignatureData(uploadResp.getData());
				if (maraService.updateMara(maraDto) <= 0) {
					cleanupUploadedAttachment(uploadResp.getData(), attachmentId);
					return new Response<Map<String, Object>>(1, "MARA签名文件路径保存失败.", null);
				}
				if (StringUtil.isNotEmpty(oldSignatureData)
						&& !oldSignatureData.equals(uploadResp.getData())) {
					super.deleteFile(oldSignatureData);
				}
			}
		} catch (ServiceException e) {
			cleanupUploadedAttachment(uploadResp.getData(), attachmentId);
			return new Response<Map<String, Object>>(e.getCode(), e.getMessage(), null);
		}
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		result.put("filePath", uploadResp.getData());
		if (aiText != null) {
			result.put("aiText", portalAttachmentDto.getAiText());
		}
		return new Response<Map<String, Object>>(0, "", result);
	}

	/** 上传后的附件入库或 Mara 更新失败时，清理文件和附件记录。 */
	private void cleanupUploadedAttachment(String filePath, int attachmentId) {
		if (attachmentId > 0) {
			try {
				portalAttachmentService.deletePortalAttachmentById(attachmentId);
			} catch (ServiceException e) {
				LOG.error("清理上传附件数据库记录失败，attachmentId={}", attachmentId, e);
			}
		}
		if (StringUtil.isNotEmpty(filePath)) {
			super.deleteFile(filePath);
		}
	}

	/**
	 * 根据附件类型调取 DeepSeek 解析图片/PDF，并规范为结构化JSON字符串。
	 * 失败返回null，不影响附件上传主流程。
	 */
	private String extractAttachmentText(byte[] fileBytes, String filename, String fileType) {
		try {
			Response<String> aiResponse = chartForAI.analyzeFile(fileBytes, filename,
					resolveAttachmentAiPrompt(fileType), true);
			if (aiResponse != null && aiResponse.getCode() == 0) {
				return normalizeAttachmentTextJson(aiResponse.getData(), fileType);
			}
			LOG.warn("附件AI文字提取失败，file={}，原因：{}", filename,
					aiResponse == null ? "无响应" : aiResponse.getMessage());
		} catch (Exception e) {
			LOG.error("附件AI文字提取异常，file={}", filename, e);
		}
		return null;
	}

	/** 规范化并校验附件类型；普通上传不调用本方法。 */
	private String normalizeAttachmentFileType(String fileType) {
		String value = fileType == null ? "" : fileType.trim().toLowerCase(Locale.ENGLISH);
		if ("passport".equalsIgnoreCase(value) || "completion".equalsIgnoreCase(value) || "coe".equalsIgnoreCase(value)) {
			return value;
		}
		return null;
	}

	/** 按附件类型选择独立的AI提示词。 */
	private String resolveAttachmentAiPrompt(String fileType) {
		if ("passport".equals(fileType)) {
			return PASSPORT_JSON_PROMPT;
		}
		if ("completion".equals(fileType)) {
			return COMPLETION_JSON_PROMPT;
		}
		return COE_JSON_PROMPT;
	}

	/** 校验AI结果是JSON对象，并序列化成格式稳定的JSON字符串。 */
	private String normalizeAttachmentTextJson(String aiText, String fileType) throws IOException {
		JsonNode jsonNode = OBJECT_MAPPER.readTree(aiText);
		if (jsonNode == null || !jsonNode.isObject()) {
			throw new IOException("AI返回结果不是合法的JSON对象");
		}
		// 完成信只允许返回completionDate；即使AI增加其他字段，也不返回给前端。
		if ("completion".equals(fileType)) {
			ObjectNode completionJson = OBJECT_MAPPER.createObjectNode();
			if (jsonNode.has("completionDate")) {
				completionJson.set("completionDate", jsonNode.get("completionDate"));
			} else {
				completionJson.putNull("completionDate");
			}
			return OBJECT_MAPPER.writeValueAsString(completionJson);
		}
		return OBJECT_MAPPER.writeValueAsString(jsonNode);
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
	 * 从合同表单JSON的 basicInfo.officialId 读取文案ID。
	 * 同时兼容数字和数字字符串，避免状态02B时只更新合同表单而没有更新案件文案。
	 */
	private Integer extractContractOfficialId(String contractStr) {
		if (StringUtil.isEmpty(contractStr))
			return null;
		try {
			JsonNode root = readContractFormData(contractStr);
			if (root == null || !root.isObject())
				return null;
			JsonNode basicInfo = root.get("basicInfo");
			if (basicInfo == null || !basicInfo.isObject())
				return null;
			JsonNode officialIdNode = basicInfo.get("officialId");
			if (officialIdNode == null || officialIdNode.isNull())
				return null;
			String officialIdValue = officialIdNode.asText();
			if (StringUtil.isEmpty(officialIdValue))
				return null;
			int parsedOfficialId = Integer.parseInt(officialIdValue.trim());
			return parsedOfficialId > 0 ? parsedOfficialId : null;
		} catch (Exception e) {
			LOG.warn("解析合同表单中的officialId失败", e);
			return null;
		}
	}

	/**
	 * 从合同表单JSON的 basicInfo.maraId 读取MARA ID。
	 * 仅用于案件从02直接进入02B时确定本次合同对应的MARA。
	 */
	private Integer extractContractMaraId(String contractStr) {
		if (StringUtil.isEmpty(contractStr))
			return null;
		try {
			JsonNode root = readContractFormData(contractStr);
			if (root == null || !root.isObject())
				return null;
			JsonNode basicInfo = root.get("basicInfo");
			if (basicInfo == null || !basicInfo.isObject())
				return null;
			JsonNode maraIdNode = basicInfo.get("maraId");
			if (maraIdNode == null || maraIdNode.isNull())
				return null;
			String maraIdValue = maraIdNode.asText();
			if (StringUtil.isEmpty(maraIdValue))
				return null;
			int parsedMaraId = Integer.parseInt(maraIdValue.trim());
			return parsedMaraId > 0 ? parsedMaraId : null;
		} catch (Exception e) {
			LOG.warn("解析合同表单中的maraId失败", e);
			return null;
		}
	}

	/** 解析合同表单JSON，并兼容前端提交的转义JSON字符串。 */
	private JsonNode readContractFormData(String contractStr) throws IOException {
		try {
			return readPortalFormData(contractStr);
		} catch (IOException firstException) {
			String unescapedJson = contractStr.replace("\\\"", "\"");
			if (!contractStr.equals(unescapedJson))
				return readPortalFormData(unescapedJson);
			throw firstException;
		}
	}

	/** 删除案件已生成的合同和Letter文件；文件不存在时视为已清理。 */
	private void deleteGeneratedPortalDocuments(PortalDTO portalDto) {
		if (portalDto == null)
			return;
		deleteGeneratedPortalDocument("合同", portalDto.getContractFilePath());
		deleteGeneratedPortalDocument("Letter", portalDto.getLetterFilePath());
	}

	private void deleteGeneratedPortalDocument(String documentName, String filePath) {
		if (StringUtil.isEmpty(filePath))
			return;
		Response<String> deleteResp = super.deleteFile(normalizeAttachmentFilePath(filePath));
		if (deleteResp != null && deleteResp.getCode() != 0)
			LOG.warn("删除案件{}文件失败，filePath={}，原因：{}", documentName, filePath, deleteResp.getMessage());
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
			@RequestParam(value = "caseType", required = false) String caseType,
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
			@RequestParam(value = "contractStr", required = false) String contractStr,
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
			portalDto.setCaseType(caseType);
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
			// 合同表单由顾问或MARA填写，保留前端传入的JSON字符串。
			if (contractStr != null)
				portalDto.setContractStr(contractStr);
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

	@RequestMapping(value = "/update", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public Object updatePortal(@RequestParam(value = "id") int id,
			@RequestParam(value = "typeId", required = false) String typeId,
			@RequestParam(value = "caseType", required = false) String caseType,
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
			@RequestParam(value = "contractStr", required = false) String contractStr,
			@RequestParam(value = "adviserId", required = false) String adviserId,
			@RequestParam(value = "officialId", required = false) String officialId,
			@RequestParam(value = "maraId", required = false) String maraId,
			@RequestParam(value = "serviceOrderId", required = false) String serviceOrderId,
			@RequestParam(value = "strState", required = false) String strState,
			@RequestParam(value = "result", required = false) String result,
			@RequestParam(value = "remark", required = false) String remark,
			@RequestParam(value = "filePath", required = false) String filePath, HttpServletRequest request,
			HttpServletResponse response) {
		String normalizedResult = result == null ? null : result.trim().toLowerCase(Locale.ENGLISH);
		boolean customerResultRequest = "confirmed".equals(normalizedResult) || "returned".equals(normalizedResult);
		try {
			super.setPostHeader(response);
			if (customerResultRequest)
				prepareCustomerActionResponse(response);
			PortalDTO portalDto = new PortalDTO();
			portalDto.setId(id);
			// 记录操作前状态（查不到时忽略，不影响主流程）
			String fromState = null;
			PortalDTO oldPortalDto = null;
			try {
				oldPortalDto = portalService.getPortal(id, null, null, null, null, null);
				if (oldPortalDto != null)
					fromState = oldPortalDto.getStrState();
			} catch (ServiceException ignored) {
			}
			if (StringUtil.isNotEmpty(normalizedResult)
					&& !("confirmed".equals(normalizedResult) || "returned".equals(normalizedResult)))
				return new Response<PortalDTO>(1, "result参数只能是confirmed或returned.", null);
			if ("confirmed".equals(normalizedResult) && !"04".equals(strState))
				return customerResultPage(null, normalizedResult, false,
						"确认签署链接参数不完整，请联系您的顾问。", response);
			if ("returned".equals(normalizedResult) && !"02C".equals(strState))
				return customerResultPage(null, normalizedResult, false,
						"退回修改链接参数不完整，请联系您的顾问。", response);
			String adviserRemark = remark == null ? null : remark.trim();
			if ("02A".equals(strState) && StringUtil.isEmpty(adviserRemark))
				adviserRemark = "通知mara处理案件";
			if (StringUtil.isNotEmpty(typeId))
				portalDto.setTypeId(StringUtil.toInt(typeId));
			// 未传caseType时保持原值；传空字符串时允许清空案件类型标识。
			if (caseType != null)
				portalDto.setCaseType(caseType);
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
			// contractStr != null 才更新，允许前端传空字符串清空合同表单数据。
			if (contractStr != null)
				portalDto.setContractStr(contractStr);
			if (StringUtil.isNotEmpty(adviserId))
				portalDto.setAdviserId(StringUtil.toInt(adviserId));
			if (StringUtil.isNotEmpty(officialId))
				portalDto.setOfficialId(StringUtil.toInt(officialId));
			// 状态转为02B时，合同表单中的文案才是最终归属文案；以 contractStr.basicInfo.officialId 为准。
			// 这样后续重新查询案件发送邮件时，会从 b_portal_list.official_id 获取正确的文案。
			if ("02B".equals(strState)) {
				Integer contractOfficialId = extractContractOfficialId(contractStr);
				if (contractOfficialId == null) {
					return new Response<PortalDTO>(1,
							"案件状态为02B时，contractStr.basicInfo.officialId不能为空且必须是有效数字.", null);
				}
				portalDto.setOfficialId(contractOfficialId);
			}
			if (StringUtil.isNotEmpty(maraId))
				portalDto.setMaraId(StringUtil.toInt(maraId));
			// 案件从02直接进入02B时，以合同表单中的MARA为准，更新案件的mara_id。
			if ("02".equals(fromState) && "02B".equals(strState)) {
				Integer contractMaraId = extractContractMaraId(contractStr);
				if (contractMaraId == null) {
					return new Response<PortalDTO>(1,
							"案件从02转为02B时，contractStr.basicInfo.maraId不能为空且必须是有效数字.", null);
				}
				portalDto.setMaraId(contractMaraId);
			}
			if (StringUtil.isNotEmpty(serviceOrderId))
				portalDto.setServiceOrderId(StringUtil.toInt(serviceOrderId));
			if (StringUtil.isNotEmpty(strState))
				portalDto.setStrState(strState);
			String customerConfirmUrl = null;
			String customerReturnUrl = null;
			if ("03A".equals(strState)) {
				try {
					customerConfirmUrl = buildPortalCustomerActionUrl(request, id, "confirm");
					customerReturnUrl = buildPortalCustomerActionUrl(request, id, "return");
				} catch (IllegalStateException actionUrlException) {
					LOG.error("客户合同确认链接生成失败，portalId={}", id, actionUrlException);
					return new Response<PortalDTO>(1, actionUrlException.getMessage(), null);
				}
			}
			if (portalService.updatePortal(portalDto) > 0) {
				// 同步附件：根据updatePortal传过来的路径，把已上传附件的portalId更新为当前案件ID
				syncPortalAttachments(filePath, portalDto.getId());
            // 状态首次转为02B时，使用更新后的完整客户资料生成合同和建议信，但不发送客户邮件。
            if ("02B".equals(strState) && !"02B".equals(fromState)) {
                try {
                    PortalDTO savedPortalDto = portalService.getPortal(id, null, null, null, null, null);
                    Map<String, String> generatedDocumentPaths = portalDocumentService.generateDocuments(savedPortalDto);
                    PortalDTO documentPathDto = new PortalDTO();
                    documentPathDto.setId(id);
                    documentPathDto.setContractFilePath(generatedDocumentPaths.get("contractPdf"));
                    documentPathDto.setLetterFilePath(generatedDocumentPaths.get("letterOfAdviceDocx"));
                    portalService.updatePortal(documentPathDto);
                    portalDto.setContractFilePath(documentPathDto.getContractFilePath());
                    portalDto.setLetterFilePath(documentPathDto.getLetterFilePath());
                    portalDto.setGeneratedDocumentPaths(generatedDocumentPaths);
					} catch (ServiceException documentException) {
						// 文件生成失败时只回退状态，客户本次填写的其他资料仍然保留，便于修复后再次提交02B。
						if (StringUtil.isNotEmpty(fromState)) {
							try {
								PortalDTO rollbackPortalDto = new PortalDTO();
								rollbackPortalDto.setId(id);
								rollbackPortalDto.setStrState(fromState);
								portalService.updatePortal(rollbackPortalDto);
							} catch (ServiceException rollbackException) {
								LOG.error("合同和建议信生成失败后回退案件状态失败，portalId={}", id, rollbackException);
							}
						}
                    LOG.error("案件状态转为02B后生成合同和建议信失败，portalId={}", id, documentException);
						if (customerResultRequest)
							return customerResultPage(null, normalizedResult, false,
									"合同和建议信生成失败，请联系您的顾问。", response);
						return new Response<PortalDTO>(documentException.getCode(), documentException.getMessage(), portalDto);
					}
				}
				// MARA退回顾问修改时，删除已经生成的合同和Letter文件，并清空数据库中的路径。
				// 02A的重复提交也执行检查，避免旧文件和旧路径残留。
				if ("02A".equals(strState)) {
					try {
						PortalDTO documentPortalDto = oldPortalDto;
						if (documentPortalDto == null)
							documentPortalDto = portalService.getPortal(id, null, null, null, null, null);
						deleteGeneratedPortalDocuments(documentPortalDto);
						if (portalService.clearGeneratedDocumentPaths(id) <= 0)
							LOG.warn("合同和Letter文件路径清空失败，portalId={}", id);
					} catch (ServiceException documentCleanupException) {
						// 状态更新和日志仍保留，清理失败写日志便于后续补偿处理。
						LOG.error("案件状态转为02A后清理合同和Letter文件失败，portalId={}", id,
								documentCleanupException);
					}
				}
				// 操作日志：更新案件
				String toState = StringUtil.isNotEmpty(strState) ? strState : fromState;
				String logAction = "update";
				String logContent = "02A".equals(strState) ? adviserRemark : "更新案件信息";
				if ("confirmed".equals(normalizedResult)) {
					logAction = "customer_confirm_sign";
					logContent = "客户点击确认签署按钮";
				} else if ("returned".equals(normalizedResult)) {
					logAction = "customer_return_modify";
					logContent = "客户点击退回修改按钮";
				} else if ("02B".equals(fromState) && "02A".equals(strState)) {
					logAction = "mara_return_modify";
					logContent = "mara返回修改";
				}
				savePortalLog(portalDto.getId(), logAction, fromState, toState, logContent, request);
				if ("03A".equals(strState)) {
					try {
						PortalDTO savedPortalDto = portalService.getPortal(id, null, null, null, null, null);
						portalDocumentService.sendGeneratedDocuments(savedPortalDto, null, customerConfirmUrl,
								customerReturnUrl);
					} catch (ServiceException confirmationMailException) {
						LOG.error("案件已更新为03A，但客户合同确认邮件发送失败，portalId={}", id,
								confirmationMailException);
						return new Response<PortalDTO>(confirmationMailException.getCode(),
								"案件已更新为03A，但客户确认邮件发送失败：" + confirmationMailException.getMessage(), portalDto);
					}
				}
				if ("confirmed".equals(normalizedResult) && "04".equals(strState) && "03A".equals(fromState)) {
					try {
						PortalDTO savedPortalDto = portalService.getPortal(id, null, null, null, null, null);
						portalService.sendOfficialPortalNotification(savedPortalDto,
								buildPortalCaseUrl(request, id));
					} catch (ServiceException notificationException) {
						LOG.error("客户已确认签署，但文案通知邮件发送失败，portalId={}", id,
								notificationException);
						if (customerResultRequest)
							return customerResultPage(portalDto, normalizedResult, false,
									"您已完成确认，案件状态已更新为04，但系统暂时未能发送文案通知，请联系工作人员。",
									response);
						return new Response<PortalDTO>(notificationException.getCode(),
								"案件已更新为04，但文案通知邮件发送失败：" + notificationException.getMessage(), portalDto);
					}
				}
				// 普通进入02A时通知MARA；02B->02A是MARA退回顾问修改，不再重复通知MARA。
				if ("02A".equals(strState) && !"02B".equals(fromState)) {
					try {
						PortalDTO savedPortalDto = portalService.getPortal(id, null, null, null, null, null);
						portalService.sendMaraPortalNotification(savedPortalDto, adviserRemark,
								buildPortalCaseUrl(request, id));
					} catch (ServiceException notificationException) {
						LOG.error("案件已更新，但MARA通知邮件发送失败，portalId={}", id, notificationException);
						return new Response<PortalDTO>(notificationException.getCode(),
								"案件已更新并记录备注，但MARA通知邮件发送失败：" + notificationException.getMessage(),
								portalDto);
					}
				}
				// 案件进入03时通知对应MARA进行案件审核；重复提交03不重复发送通知。
				if ("03".equals(strState) && !"03".equals(fromState)) {
					try {
						PortalDTO savedPortalDto = portalService.getPortal(id, null, null, null, null, null);
						portalService.sendMaraPortalReviewNotification(savedPortalDto,
								buildPortalCaseUrl(request, id));
					} catch (ServiceException notificationException) {
						LOG.error("案件已更新为03，但MARA审核通知邮件发送失败，portalId={}", id,
								notificationException);
						return new Response<PortalDTO>(notificationException.getCode(),
								"案件已更新为03，但MARA审核通知邮件发送失败：" + notificationException.getMessage(),
								portalDto);
					}
				}
				// 只有本次请求明确将状态更新为02时，才调用语聚AI进行485方案咨询。
				// AI调用失败不影响案件主流程，调用结果随案件数据一起返回。
				if ("02".equals(strState))
					portalDto.setYujuAiResult(requestYujuAiAfterPortalUpdate(portalDto));
				if (customerResultRequest) {
					PortalDTO resultPortalDto = portalService.getPortal(id, null, null, null, null, null);
					String message = "confirmed".equals(normalizedResult)
							? "您已确认签署合同，感谢您选择指南针。请将签署完成的合同文件电邮给您的顾问，"
									+ "顾问邮箱地址是：" + adviserEmail(resultPortalDto)
									+ "。系统已自动通知文案开始准备申请，感谢您的配合。"
							: "我们已收到您的退回修改请求，感谢您的反馈。案件已退回给顾问进一步检查和修改，"
									+ "顾问邮箱地址是：" + adviserEmail(resultPortalDto)
									+ "。顾问会尽快与您联系，感谢您的理解与耐心。";
					return customerResultPage(resultPortalDto, normalizedResult, true, message, response);
				}
				return new Response<PortalDTO>(0, portalDto);
			} else {
				if (customerResultRequest)
					return customerResultPage(portalDto, normalizedResult, false,
							"案件状态暂未更新成功，请稍后重试或联系您的顾问。", response);
				return new Response<PortalDTO>(1, "修改失败.", null);
			}
		} catch (ServiceException e) {
			if (customerResultRequest)
				return customerResultPage(null, normalizedResult, false,
						"系统暂时无法处理该操作，请稍后重试或联系您的顾问。", response);
			return new Response<PortalDTO>(e.getCode(), e.getMessage(), null);
		} catch (Exception e) {
			LOG.error("更新案件发生异常，portalId={}", id, e);
			if (customerResultRequest)
				return customerResultPage(null, normalizedResult, false,
						"系统暂时无法处理该操作，请稍后重试或联系您的顾问。", response);
			return new Response<PortalDTO>(1, e.getMessage(), null);
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
	 * 出生：23/06/1984，出生国家：China，婚姻：Married，学签到期：25/12/2027，完成信：是，语言考试：IELTS。
	 * 请给我485申请方案及材料清单。
	 */
	private String buildYujuAiInstructions(PortalDTO portalDto) {
		JsonNode formData = parsePortalFormData(portalDto);
		StringBuilder text = new StringBuilder();

//		appendAiField(text, "姓名", portalDto == null ? null : portalDto.getName());
//		appendAiField(text, "性别", portalDto == null ? null : portalDto.getGender());
		appendAiField(text, "出生", portalDto == null ? null : formatAiDate(portalDto.getBirthday()));
		appendAiField(text, "出生国家", getJsonValue(formData, "basicInfo", "birthCountry"));
		appendAiField(text, "婚姻", getJsonValue(formData, "basicInfo", "maritalStatus"));
		appendAiField(text, "学签到期", resolveStudentVisaExpirationDate(formData, portalDto));
		appendAiField(text, "完成信", hasEducationData(formData) ? "是" : "否");
		appendAiField(text, "语言考试", joinLangTypes(getSectionNode(formData, "language")));

		// jsonStr 解析失败时，兜底把原始 jsonStr 附上，避免信息丢失
		if (formData == null && portalDto != null && StringUtil.isNotEmpty(portalDto.getJsonStr()))
			appendAiField(text, "原始表单数据", portalDto.getJsonStr().trim());

		String content = text.toString().trim();
		if (content.isEmpty())
			return "请给我485申请方案及材料清单。";
		return content + "。请给我485申请方案及材料清单。";
	}

	/**
	 * 学签到期时间：优先取 jsonStr.basicInfo.studentVisaExpirationDate（时间戳/日期字符串），
	 * 取不到再回退到 PortalDTO 的学签到期时间。
	 */
	private String resolveStudentVisaExpirationDate(JsonNode formData, PortalDTO portalDto) {
		Date date = dateFromNode(getJsonNode(formData, "basicInfo", "studentVisaExpirationDate"));
		if (date == null && portalDto != null)
			date = portalDto.getStudentVisaExpirationDate();
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

	/** 语言考试：取 language[].langType，多门用顿号连接，如 "ESOL、AAA"。 */
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
			@RequestParam(value = "caseType", required = false) String caseType,
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
			int total = portalService.countPortal(typeId, caseType, strState, keyword, filter.adviserId,
					filter.adviserRegionId, filter.officialId, filter.officialRegionId, filter.maraId);
			List<PortalDTO> portalDtoList = portalService.listPortal(typeId, caseType, strState, keyword, pageNum,
					pageSize, filter.adviserId, filter.adviserRegionId, filter.officialId, filter.officialRegionId,
					filter.maraId);
			// 与 /get 保持一致：按 portal_id 关联查询附件列表和操作日志，组装进每个案件一起返回
			if (portalDtoList != null) {
				for (PortalDTO portalDto : portalDtoList) {
					portalDto.setPortalAttachmentList(
							portalAttachmentService.listPortalAttachmentByPortalId(portalDto.getId()));
					portalDto.setPortalLogList(portalLogService.listPortalLog(portalDto.getId(), 0, 1000));
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
	 * 客户邮件中的合同操作链接。链接本身不依赖登录态，使用HMAC令牌校验，且只允许案件当前处于03A时变更状态。
	 */
	@RequestMapping(value = "/customer-action", method = RequestMethod.GET, produces = "text/html;charset=UTF-8")
	@ResponseBody
	public String handleCustomerAction(@RequestParam(value = "portalId") Integer portalId,
			@RequestParam(value = "action") String action, @RequestParam(value = "token") String token,
			HttpServletRequest request, HttpServletResponse response) {
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=UTF-8");
		response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
		try {
			super.setGetHeader(response);
			String normalizedAction = action == null ? "" : action.trim().toLowerCase(Locale.ENGLISH);
			if (portalId == null || portalId <= 0
					|| !("confirm".equals(normalizedAction) || "return".equals(normalizedAction))
					|| !isValidPortalActionToken(portalId, normalizedAction, token)) {
				return customerActionPage(false, "链接无效", "该合同操作链接无效或已失效，请联系您的顾问。");
			}

			PortalDTO portalDto = portalService.getPortal(portalId, null, null, null, null, null);
			if (portalDto == null)
				return customerActionPage(false, "案件不存在", "未找到对应案件，请联系您的顾问。");

			boolean confirm = "confirm".equals(normalizedAction);
			String targetState = confirm ? "04" : "02C";
			String logAction = confirm ? "customer_confirm_sign" : "customer_return_modify";
			String logContent = confirm ? "客户点击确认签署按钮" : "客户点击退回修改按钮";
			String currentState = portalDto.getStrState();
			if (!"03A".equals(currentState)) {
				savePortalLog(portalId, logAction + "_ignored", currentState, currentState,
						logContent + "，当前状态不允许重复处理", request);
				if (targetState.equals(currentState))
					return customerActionPage(true, "操作已完成", "该案件已经处理过，无需重复操作。");
				return customerActionPage(false, "操作未执行", "该案件当前状态已发生变化，请联系您的顾问。");
			}

			int updated = portalService.updatePortalStateIfCurrent(portalId, "03A", targetState);
			if (updated <= 0) {
				PortalDTO latestPortalDto = portalService.getPortal(portalId, null, null, null, null, null);
				String latestState = latestPortalDto == null ? null : latestPortalDto.getStrState();
				savePortalLog(portalId, logAction + "_ignored", latestState, latestState,
						logContent + "，案件状态已被其他请求处理", request);
				return customerActionPage(false, "操作未执行", "该案件已经被处理或状态已发生变化，请联系您的顾问。");
			}

			savePortalLog(portalId, logAction, "03A", targetState, logContent, request);
			if (confirm) {
				try {
					PortalDTO confirmedPortalDto = portalService.getPortal(portalId, null, null, null, null, null);
					portalService.sendOfficialPortalNotification(confirmedPortalDto,
							buildPortalCaseUrl(request, portalId));
				} catch (ServiceException notificationException) {
					LOG.error("客户已确认签署，但文案通知邮件发送失败，portalId={}", portalId,
							notificationException);
					return customerActionPage(true, "确认签署成功",
							"案件状态已更新为04（客户确认签署），但文案通知邮件发送失败，请联系工作人员。");
				}
				return customerActionPage(true, "确认签署成功", "案件状态已更新为04（客户确认签署），文案已收到开始准备申请的通知。");
			}
			return customerActionPage(true, "已退回修改", "案件状态已退回02C，您的顾问会根据反馈修改合同和Letter文件。");
		} catch (ServiceException e) {
			LOG.error("处理客户合同操作链接失败，portalId={}", portalId, e);
			return customerActionPage(false, "处理失败", "系统暂时无法处理该操作，请稍后重试或联系您的顾问。");
		} catch (Exception e) {
			LOG.error("处理客户合同操作链接发生异常，portalId={}", portalId, e);
			return customerActionPage(false, "处理失败", "系统暂时无法处理该操作，请稍后重试或联系您的顾问。");
		}
	}

	private void prepareCustomerActionResponse(HttpServletResponse response) {
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=UTF-8");
		response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
	}

	/**
	 * 从案件关联的顾问中读取邮箱，供客户操作完成页面显示。
	 */
	private String adviserEmail(PortalDTO portalDto) {
		if (portalDto == null || portalDto.getAdviserId() <= 0)
			return "暂未配置，请联系工作人员获取";
		try {
			AdviserDO adviserDo = adviserDao.getAdviserById(portalDto.getAdviserId());
			if (adviserDo != null && StringUtil.isNotEmpty(adviserDo.getEmail()))
				return adviserDo.getEmail().trim();
		} catch (Exception e) {
			LOG.warn("查询案件顾问邮箱失败，adviserId={}", portalDto.getAdviserId(), e);
		}
		return "暂未配置，请联系工作人员获取";
	}

	private String customerResultPage(PortalDTO portalDto, String result, boolean success, String message,
			HttpServletResponse response) {
		prepareCustomerActionResponse(response);
		String title;
		if (!success) {
			title = "操作未完成";
		} else if ("confirmed".equals(result)) {
			title = "确认签署成功";
		} else {
			title = "已退回修改";
		}
		return customerActionPage(success, title, message);
	}

	private String customerActionPage(boolean success, String title, String message) {
		String color = success ? "#198754" : "#dc3545";
		return "<!doctype html><html lang=\"zh-CN\"><head><meta charset=\"UTF-8\"><meta name=\"viewport\" "
				+ "content=\"width=device-width,initial-scale=1\"><title>" + escapeHtml(title)
				+ "</title></head><body style=\"margin:0;background:#f5f7fa;font-family:Arial,'Microsoft YaHei',sans-serif;\">"
				+ "<div style=\"max-width:560px;margin:12vh auto;padding:36px 28px;background:#fff;border-radius:8px;"
				+ "box-shadow:0 2px 12px rgba(0,0,0,.08);text-align:center;\"><h2 style=\"color:" + color
				+ ";margin:0 0 20px;\">" + escapeHtml(title) + "</h2><p style=\"color:#555;line-height:1.8;\">"
				+ escapeHtml(message) + "</p></div></body></html>";
	}

	private String buildPortalCustomerActionUrl(HttpServletRequest request, int portalId, String action) {
		String normalizedAction = action == null ? "" : action.trim().toLowerCase(Locale.ENGLISH);
		boolean confirmed = "confirm".equals(normalizedAction);
		String targetState = confirmed ? "04" : "02C";
		String result = confirmed ? "confirmed" : "returned";
		String baseUrl = StringUtil.isNotEmpty(portalCustomerActionBaseUrl)
				? portalCustomerActionBaseUrl.trim().replaceAll("/+$", "")
				: buildPortalPublicBaseUrl(request);
		// 本地测试时固定调用 http://localhost:8081/admin_v2.1/portal/update。
		return baseUrl + "/portal/update?id=" + portalId + "&strState=" + targetState + "&result=" + result;
	}

	private boolean isValidPortalActionToken(int portalId, String action, String token) {
		if (StringUtil.isEmpty(token))
			return false;
		try {
			byte[] expected = signPortalAction(portalId, action).getBytes(StandardCharsets.US_ASCII);
			byte[] actual = token.trim().getBytes(StandardCharsets.US_ASCII);
			return MessageDigest.isEqual(expected, actual);
		} catch (Exception e) {
			LOG.warn("校验客户合同操作链接失败，portalId={}", portalId, e);
			return false;
		}
	}

	private String signPortalAction(int portalId, String action) {
		String secret = portalActionSecret();
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			byte[] digest = mac.doFinal((portalId + ":" + action).getBytes(StandardCharsets.UTF_8));
			StringBuilder token = new StringBuilder(digest.length * 2);
			for (byte item : digest) {
				int value = item & 0xff;
				if (value < 16)
					token.append('0');
				token.append(Integer.toHexString(value));
			}
			return token.toString();
		} catch (Exception e) {
			throw new IllegalStateException("客户确认链接签名生成失败.", e);
		}
	}

	private String portalActionSecret() {
		if (StringUtil.isNotEmpty(portalCustomerActionSecret))
			return portalCustomerActionSecret.trim();
		// 兼容现有部署：未单独配置时暂使用已有的语聚AI密钥作为签名密钥；生产环境建议单独配置。
		if (StringUtil.isNotEmpty(yujuAiApiKey))
			return yujuAiApiKey.trim();
		throw new IllegalStateException("未配置客户确认链接密钥，请设置 portal.customer-action-secret.");
	}

	private String buildPortalPublicBaseUrl(HttpServletRequest request) {
		String scheme = firstHeaderValue(request.getHeader("X-Forwarded-Proto"));
		if (StringUtil.isEmpty(scheme))
			scheme = request.getScheme();
		String forwardedHost = firstHeaderValue(request.getHeader("X-Forwarded-Host"));
		String host = StringUtil.isNotEmpty(forwardedHost) ? forwardedHost : request.getServerName();
		if (StringUtil.isEmpty(forwardedHost)) {
			int port = request.getServerPort();
			boolean defaultPort = ("http".equalsIgnoreCase(scheme) && port == 80)
					|| ("https".equalsIgnoreCase(scheme) && port == 443);
			if (!defaultPort)
				host += ":" + port;
		}
		String contextPath = request.getContextPath();
		return scheme + "://" + host + (StringUtil.isEmpty(contextPath) ? "" : contextPath);
	}

	private String firstHeaderValue(String value) {
		if (StringUtil.isEmpty(value))
			return null;
		int commaIndex = value.indexOf(',');
		return (commaIndex >= 0 ? value.substring(0, commaIndex) : value).trim();
	}

	private String escapeHtml(String value) {
		if (value == null)
			return "";
		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
				.replace("\"", "&quot;").replace("'", "&#39;");
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
			portalLogDto.setIp(getClientIp(request));
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

	private String buildPortalCaseUrl(HttpServletRequest request, int portalId) {
		return buildPortalPublicBaseUrl(request) + "/webroot_new/portal/list/ALL?id=" + portalId;
	}

	private String getClientIp(HttpServletRequest request) {
		String forwarded = firstHeaderValue(request.getHeader("X-Forwarded-For"));
		if (StringUtil.isNotEmpty(forwarded) && !"unknown".equalsIgnoreCase(forwarded))
			return forwarded;
		String realIp = firstHeaderValue(request.getHeader("X-Real-IP"));
		return StringUtil.isNotEmpty(realIp) && !"unknown".equalsIgnoreCase(realIp) ? realIp
				: request.getRemoteAddr();
	}

	@RequestMapping(value = "/get", method = RequestMethod.GET)
	@ResponseBody
	public Response<PortalDTO> getPortal(@RequestParam(value = "id") Integer id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			// 此接口不需要验证登录，不做数据权限过滤
			PortalDTO portalDto = portalService.getPortal(id, null, null, null, null, null);
			if (portalDto != null) {
				// 按 portal_id 关联查询附件列表和操作日志，组装进 PortalDTO 一起返回
				portalDto.setPortalAttachmentList(
						portalAttachmentService.listPortalAttachmentByPortalId(portalDto.getId()));
				portalDto.setPortalLogList(portalLogService.listPortalLog(portalDto.getId(), 0, 1000));
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
