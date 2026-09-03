package org.zhinanzhen.b.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFHeaderFooter;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.zhinanzhen.b.service.MaraService;
import org.zhinanzhen.b.service.PortalDocumentService;
import org.zhinanzhen.b.service.pojo.MaraDTO;
import org.zhinanzhen.b.service.pojo.PortalDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ikasoa.core.ErrorCodeEnum;
import com.ikasoa.core.utils.StringUtil;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

@Service("PortalDocumentService")
public class PortalDocumentServiceImpl extends BaseService implements PortalDocumentService {

	private static final String CONTRACT_TEMPLATE = "Contract-CEM-SYD-Comp Final Version.pdf";
	private static final Set<String> CONTRACT_TEMPLATE_NAMES = Collections.unmodifiableSet(
			new HashSet<String>(Arrays.asList(
					"Contract-CEM-ACT-13-Sep-2022.pdf",
					"Contract-CEM-ADE-15-June-2026.pdf",
					"Contract-CEM-BNE-13-Sep-2022.pdf",
					"Contract-CEM-Mel-13-Sep-2022.pdf",
					CONTRACT_TEMPLATE,
					"Contract-CEM-TAS-13-Sep-2022.pdf")));
	private static final String[] MODERN_SIGNATURE_FIELDS = { "agent_signature_p1", "agent_signature_p2",
			"agent_signature_p3", "agent_signature_p4" };
	private static final String[] LEGACY_SIGNATURE_FIELDS = { "Signature21", "Signature22", "Signature23",
			"Signature25", "Signature38" };
	private static final String ADVICE_TEMPLATE = "MARA_Basic_Letter_of_Advice_Template.docx";
	private static final String PRACTICE_NAME = "Compass Education and Migration Pty Ltd";
	private static final String AGENT_NAME = "Tonglu Ge";
	private static final String AGENT_MARN = "1687805";
	private static final String PRACTICE_ADDRESS = "Level 36, 680 George Street, Sydney, NSW, 2000, Australia";
	private static final String PRACTICE_EMAIL = "admin@globalznz.com";
	private static final String PRACTICE_PHONE = "(02) 9283 1227";
	private static final String PENDING_AGENT_REVIEW = "To be completed by the registered migration agent after review.";
	private static final String UPLOAD_DATA_PREFIX = "/data/";
	private static final String DOCUMENT_UPLOAD_DIRECTORY = "uploads/portal_document";
	private static final String DOCUMENT_UPLOAD_PATH_PREFIX = "/uploads/portal_document/";
	private static final String TOMCAT_CONTEXT_DIRECTORY = "admin_v2.1";
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	/** 合同、建议信与普通上传文件共用 /data 下的 uploads 目录。 */
	@Value("${portal.document.output-dir:/data/uploads/portal_document}")
	private String outputDirectory;

	@Resource
	private MaraService maraService;

	@Override
	public Map<String, String> generateDocuments(PortalDTO portalDto) throws ServiceException {
		if (portalDto == null || portalDto.getId() <= 0) {
			ServiceException exception = new ServiceException("案件信息无效，无法生成合同和建议信.");
			exception.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw exception;
		}

		Path contractPath = null;
		Path advicePath = null;
		try {
			CustomerDocumentData data = buildCustomerData(portalDto);
			Path outputDir = resolveOutputDirectory();
			Files.createDirectories(outputDir);

			String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
			String customerSuffix = safeFileName(data.fullName);
			String adviserSuffix = safeFileName(firstNonEmpty(portalDto.getAdviserName(), "未分配顾问"));
			String prefix = "portal_" + portalDto.getId() + "_" + timestamp;
			contractPath = outputDir.resolve(prefix + "_Contract_" + customerSuffix + "_" + adviserSuffix + ".pdf");
			advicePath = outputDir.resolve(
					prefix + "_Letter_of_Advice_" + customerSuffix + "_" + adviserSuffix + ".docx");

			generateContractPdf(data, contractPath);
			generateAdviceDocument(data, advicePath);

			Map<String, String> paths = new LinkedHashMap<String, String>();
			// 与uploadAttachment保持一致，数据库保存访问路径而不是当前机器的物理绝对路径。
			paths.put("contractPdf", toStoredFilePath(contractPath));
			paths.put("letterOfAdviceDocx", toStoredFilePath(advicePath));
			return paths;
		} catch (Exception e) {
			deleteGeneratedFile(contractPath);
			deleteGeneratedFile(advicePath);
			ServiceException exception = new ServiceException("生成客户合同和建议信失败: " + e.getMessage(), e);
			exception.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw exception;
		}
	}

	/**
	 * 将配置的 /data/uploads/portal_document 映射到上传接口实际使用的物理目录。
	 * Linux 使用 /data；本地 Windows Tomcat 使用应用临时目录下的 data。
	 */
	private Path resolveOutputDirectory() {
		String configuredDirectory = StringUtil.isEmpty(outputDirectory)
				? UPLOAD_DATA_PREFIX + DOCUMENT_UPLOAD_DIRECTORY : outputDirectory.trim();
		String normalizedDirectory = configuredDirectory.replace('\\', '/');
		if (normalizedDirectory.equals("/data") || normalizedDirectory.startsWith(UPLOAD_DATA_PREFIX)) {
			String relativeDirectory = normalizedDirectory.equals("/data")
					? "" : normalizedDirectory.substring(UPLOAD_DATA_PREFIX.length());
			return resolveUploadDataRoot().resolve(relativeDirectory).toAbsolutePath().normalize();
		}
		// 允许通过配置继续指定自定义绝对目录。
		return Paths.get(configuredDirectory).toAbsolutePath().normalize();
	}

	/** 将生成文件转换为与upload2相同格式的/uploads相对路径。 */
	private String toStoredFilePath(Path path) {
		if (path == null || path.getFileName() == null)
			return null;
		Path uploadDirectory = resolveUploadDataRoot().resolve(DOCUMENT_UPLOAD_DIRECTORY).toAbsolutePath().normalize();
		Path normalizedPath = path.toAbsolutePath().normalize();
		if (normalizedPath.startsWith(uploadDirectory))
			return DOCUMENT_UPLOAD_PATH_PREFIX + path.getFileName().toString();
		// 兼容通过portal.document.output-dir配置自定义目录的情况。
		return normalizedPath.toString();
	}

	private Path resolveUploadDataRoot() {
		boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
		if (windows) {
			String catalinaBase = System.getProperty("catalina.base");
			if (StringUtil.isNotEmpty(catalinaBase)) {
				return Paths.get(catalinaBase, "work", "Tomcat", "localhost", TOMCAT_CONTEXT_DIRECTORY, "data");
			}
		}
		return Paths.get("/data");
	}

	@Override
	public void sendGeneratedDocuments(PortalDTO portalDto, Map<String, String> generatedDocumentPaths)
			throws ServiceException {
		sendGeneratedDocuments(portalDto, generatedDocumentPaths, null, null);
	}

	@Override
	public void sendGeneratedDocuments(PortalDTO portalDto, Map<String, String> generatedDocumentPaths,
			String confirmUrl, String returnUrl) throws ServiceException {
		if (portalDto == null || portalDto.getId() <= 0) {
			throw serviceException("案件信息无效，无法发送合同和建议信邮件.", ErrorCodeEnum.PARAMETER_ERROR.code(), null);
		}

		CustomerDocumentData data = buildCustomerData(portalDto);
		if (StringUtil.isEmpty(data.email)) {
			throw serviceException("客户邮箱为空，合同和建议信未发送.", ErrorCodeEnum.PARAMETER_ERROR.code(), null);
		}

		String contractFilePath = generatedDocumentPaths == null ? null : generatedDocumentPaths.get("contractPdf");
		if (StringUtil.isEmpty(contractFilePath))
			contractFilePath = portalDto.getContractFilePath();
		String letterFilePath = generatedDocumentPaths == null ? null
				: generatedDocumentPaths.get("letterOfAdviceDocx");
		if (StringUtil.isEmpty(letterFilePath))
			letterFilePath = portalDto.getLetterFilePath();
		Path contractPath = requireGeneratedFile(contractFilePath, "合同PDF");
		Path advicePath = requireGeneratedFile(letterFilePath, "建议信Word文件");
		String adviserName = firstNonEmpty(portalDto.getAdviserName(), "您的顾问");
		String title = "【指南针留学移民】485签证合同和建议信";
		String content = build485ContractEmail(data.fullName, adviserName, confirmUrl, returnUrl);
		try {
			sendMailWithAttachments(data.email, title, content, contractPath.toFile(), advicePath.toFile());
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			throw serviceException("发送合同和建议信邮件失败: " + e.getMessage(), ErrorCodeEnum.OTHER_ERROR.code(), e);
		}
	}

	private Path requireGeneratedFile(String pathValue, String description) throws ServiceException {
		if (StringUtil.isEmpty(pathValue)) {
			throw serviceException(description + "路径为空，邮件未发送.", ErrorCodeEnum.PARAMETER_ERROR.code(), null);
		}
		Path path = resolveStoredFilePath(pathValue);
		if (!Files.isRegularFile(path)) {
			throw serviceException(description + "不存在: " + path, ErrorCodeEnum.DATA_ERROR.code(), null);
		}
		return path;
	}

	/** 将数据库中的/uploads相对路径解析为当前环境的实际文件路径。 */
	private Path resolveStoredFilePath(String pathValue) {
		String normalizedPath = pathValue.trim().replace('\\', '/');
		if (normalizedPath.matches("^[A-Za-z]:/.*"))
			return Paths.get(normalizedPath).toAbsolutePath().normalize();
		if (normalizedPath.startsWith("/") && !normalizedPath.startsWith("/data/")
				&& !normalizedPath.startsWith("/uploads/"))
			return Paths.get(normalizedPath).toAbsolutePath().normalize();

		String relativePath = normalizedPath;
		if (relativePath.startsWith("/data/"))
			relativePath = relativePath.substring("/data/".length());
		else if (relativePath.startsWith("data/"))
			relativePath = relativePath.substring("data/".length());
		else if (relativePath.startsWith("/"))
			relativePath = relativePath.substring(1);

		return resolveUploadDataRoot().resolve(relativePath).toAbsolutePath().normalize();
	}

	private String build485ContractEmail(String customerName, String adviserName, String confirmUrl,
			String returnUrl) {
		String safeCustomerName = htmlEscape(firstNonEmpty(customerName, "同学"));
		String safeAdviserName = htmlEscape(firstNonEmpty(adviserName, "您的顾问"));
		StringBuilder content = new StringBuilder();
		content.append("<p>亲爱的").append(safeCustomerName).append("同学，您好：</p>");
		content.append("<p>感谢您对指南针留学移民的信任。我们已根据目前系统中登记的客户信息，为您生成了485签证服务合同和建议信，并随本邮件一并发送。</p>");
		content.append("<p>请您下载并仔细核对两份附件中的姓名、联系方式、护照及学习经历等信息。如发现任何信息有误或需要补充，请先退回修改，并及时联系您的顾问 <strong>")
				.append(safeAdviserName).append("</strong>。</p>");
		if (StringUtil.isNotEmpty(confirmUrl) && StringUtil.isNotEmpty(returnUrl)) {
			content.append("<p>确认无误后，请点击下面的“确认签署”按钮；如需修改，请点击“退回修改”按钮：</p>")
					.append("<p style=\"margin:24px 0;\">")
					.append("<a href=\"").append(htmlEscape(confirmUrl))
					.append("\" style=\"display:inline-block;padding:12px 24px;margin-right:12px;"
							+ "background:#198754;color:#fff;text-decoration:none;border-radius:4px;\">确认签署</a>")
					.append("<a href=\"").append(htmlEscape(returnUrl))
					.append("\" style=\"display:inline-block;padding:12px 24px;"
							+ "background:#dc3545;color:#fff;text-decoration:none;border-radius:4px;\">退回修改</a>")
					.append("</p>");
		} else {
			content.append("<p>确认无误后，请按文件要求填写、签署并回复给我们。</p>");
		}
		content.append("<p>指南针留学移民</p>");
		return content.toString();
	}

	private String htmlEscape(String value) {
		if (value == null)
			return "";
		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
				.replace("\"", "&quot;").replace("'", "&#39;");
	}

	private ServiceException serviceException(String message, int code, Exception cause) {
		ServiceException exception = cause == null ? new ServiceException(message) : new ServiceException(message, cause);
		exception.setCode(code);
		return exception;
	}

	private void generateContractPdf(CustomerDocumentData data, Path outputPath) throws Exception {
		String contractTemplate = resolveContractTemplate(data.contractTemplate);
		ClassPathResource resource = new ClassPathResource(contractTemplate);
		try (InputStream input = resource.getInputStream();
				OutputStream output = Files.newOutputStream(outputPath, StandardOpenOption.CREATE_NEW)) {
			PdfReader reader = new PdfReader(input);
			try {
				PdfStamper stamper = new PdfStamper(reader, output);
				try {
					AcroFields form = stamper.getAcroFields();
					form.setGenerateAppearances(true);
					setPdfField(form, "Text1", data.fullName);
					setPdfField(form, "Text2", data.address);
					setPdfField(form, "Text3", data.fullName);
					setPdfField(form, "Text4", data.dateOfBirth);
					setPdfField(form, "Text5", data.passport);
					setPdfField(form, "Text6", data.address);
					setPdfField(form, "Text7", data.email);
					setPdfField(form, "Text8", data.mobile);

					setPdfField(form, "Text10", data.firstName);
					setPdfField(form, "Text11", data.lastName);
					setPdfField(form, "Text12", data.dateOfBirth);
					setPdfField(form, "Text13", data.mobile);
					setPdfField(form, "Text14", data.email);
					setPdfField(form, "Text15", data.address);
					setPdfField(form, "visa_subclass", data.matter);
					// 模板默认选中了 Diners Club；没有客户付款信息时清空该默认值。
					form.setField("Radio Button26", "Off");
					addMaraSignatures(stamper, form, data.maraId);
					stamper.setFormFlattening(false);
				} finally {
					stamper.close();
				}
			} finally {
				reader.close();
			}
		}
	}

	private String resolveContractTemplate(String requestedTemplate) throws IOException {
		String templateName = requestedTemplate == null ? null : requestedTemplate.trim();
		if (StringUtil.isEmpty(templateName))
			templateName = CONTRACT_TEMPLATE;
		if (!CONTRACT_TEMPLATE_NAMES.contains(templateName))
			throw new IOException("不支持的合同模板: " + templateName);

		ClassPathResource resource = new ClassPathResource(templateName);
		if (!resource.exists())
			throw new IOException("合同模板文件不存在: " + templateName);
		return templateName;
	}

	/**
	 * 将案件对应 Mara 的签名写入合同模板中的签名位置。
	 * 悉尼模板使用 agent_signature_p1~p4，其余旧模板使用 Signature21/22/23/25/38。
	 * 签名字段本身不能直接写入图片，因此先移除字段，再将签名图片覆盖到同一坐标。
	 */
	private void addMaraSignatures(PdfStamper stamper, AcroFields form, int maraId) throws Exception {
		if (maraId <= 0)
			throw new IOException("案件未分配 Mara，无法获取签名");
		MaraDTO maraDto = maraService.getMaraById(maraId);
		if (maraDto == null)
			throw new IOException("未找到 Mara，maraId=" + maraId);
		if (StringUtil.isEmpty(maraDto.getSignatureData()))
			throw new IOException("Mara " + maraId + " 未配置签名文件路径(signature_data)");
		byte[] signatureBytes = readSignatureFile(maraDto.getSignatureData(), maraId);
		String[] signatureFields = resolveSignatureFields(form);
		for (String signatureField : signatureFields)
			addMaraSignature(stamper, form, signatureField, signatureBytes);
	}

	private String[] resolveSignatureFields(AcroFields form) throws IOException {
		if (hasFieldPosition(form, MODERN_SIGNATURE_FIELDS[0]))
			return MODERN_SIGNATURE_FIELDS;
		if (hasFieldPosition(form, LEGACY_SIGNATURE_FIELDS[0]))
			return LEGACY_SIGNATURE_FIELDS;
		throw new IOException("合同模板缺少 Mara 签名字段");
	}

	private boolean hasFieldPosition(AcroFields form, String fieldName) {
		List<AcroFields.FieldPosition> positions = form.getFieldPositions(fieldName);
		return positions != null && !positions.isEmpty();
	}

	private void addMaraSignature(PdfStamper stamper, AcroFields form, String fieldName, byte[] signatureBytes)
			throws Exception {
		List<AcroFields.FieldPosition> positions = form.getFieldPositions(fieldName);
		if (positions == null || positions.isEmpty())
			throw new IOException("合同模板缺少签名字段: " + fieldName);

		AcroFields.FieldPosition fieldPosition = positions.get(0);
		Rectangle rectangle = fieldPosition.position;
		if (!form.removeField(fieldName))
			throw new IOException("无法移除合同签名字段: " + fieldName);

		Image signature = Image.getInstance(signatureBytes);
		signature.scaleToFit(rectangle.getWidth() * 0.95f, rectangle.getHeight() * 0.95f);
		signature.setAbsolutePosition(
				rectangle.getLeft() + (rectangle.getWidth() - signature.getScaledWidth()) / 2f,
				rectangle.getBottom() + (rectangle.getHeight() - signature.getScaledHeight()) / 2f);
		stamper.getOverContent(fieldPosition.page).addImage(signature);
	}

	/**
	 * signature_data 保存的是上传接口返回的路径，例如 /uploads/portal_attachment/xxx.png，
	 * 实际文件位于 /data/uploads/portal_attachment/xxx.png。
	 */
	private byte[] readSignatureFile(String signatureData, int maraId) throws IOException {
		String normalizedPath = signatureData.trim().replace('\\', '/');
		List<Path> candidatePaths = new ArrayList<Path>();
		if (normalizedPath.matches("^[A-Za-z]:/.*"))
			candidatePaths.add(Paths.get(normalizedPath));

		String relativePath = normalizedPath;
		if (relativePath.startsWith("/data/"))
			relativePath = relativePath.substring("/data/".length());
		else if (relativePath.startsWith("data/"))
			relativePath = relativePath.substring("data/".length());
		else if (relativePath.startsWith("/"))
			relativePath = relativePath.substring(1);

		// Linux/默认目录：上传接口将 /uploads/... 保存到 /data/uploads/...。
		candidatePaths.add(Paths.get("/data", relativePath));

		// 本地 Tomcat 部署时，/data 会落到 catalina.base/work/.../admin_v2.1/data 下。
		String catalinaBase = System.getProperty("catalina.base");
		if (StringUtil.isNotEmpty(catalinaBase)) {
			candidatePaths.add(Paths.get(catalinaBase, "work", "Tomcat", "localhost", "admin_v2.1", "data",
					relativePath));
		}

		// 兼容本地临时 Tomcat 实例，即使 catalina.base 没有正确设置。
		String tempDirectory = System.getProperty("java.io.tmpdir");
		if (StringUtil.isNotEmpty(tempDirectory)) {
			File[] tempDirectories = new File(tempDirectory).listFiles();
			if (tempDirectories != null) {
				for (File tempDir : tempDirectories) {
					if (tempDir.isDirectory() && tempDir.getName().startsWith("tomcat."))
						candidatePaths.add(Paths.get(tempDir.getAbsolutePath(), "work", "Tomcat", "localhost",
								"admin_v2.1", "data", relativePath));
				}
			}
		}

		// 某些嵌入式运行方式会将当前工作目录作为 /data 的相对根目录。
		String userDirectory = System.getProperty("user.dir");
		if (StringUtil.isNotEmpty(userDirectory))
			candidatePaths.add(Paths.get(userDirectory, "data", relativePath));

		for (Path candidatePath : candidatePaths) {
			Path normalizedCandidate = candidatePath.toAbsolutePath().normalize();
			if (Files.isRegularFile(normalizedCandidate))
				return Files.readAllBytes(normalizedCandidate);
		}
		throw new IOException("Mara " + maraId + " 的签名文件不存在: " + normalizedPath);
	}

	private void setPdfField(AcroFields form, String fieldName, String value) throws Exception {
		if (StringUtil.isNotEmpty(fieldName) && StringUtil.isNotEmpty(value))
			form.setField(fieldName, value);
	}

	private void generateAdviceDocument(CustomerDocumentData data, Path outputPath) throws Exception {
		ClassPathResource resource = new ClassPathResource(ADVICE_TEMPLATE);
		try (InputStream input = resource.getInputStream(); XWPFDocument document = new XWPFDocument(input)) {
			fillAdviceTables(document, data);

			Map<String, String> exactParagraphs = buildExactParagraphReplacements(data);
			Map<String, String> tokens = buildTokenReplacements(data);
			processParagraphs(document.getParagraphs(), exactParagraphs, tokens);
			processTables(document.getTables(), exactParagraphs, tokens);
			for (XWPFHeaderFooter header : document.getHeaderList()) {
				processParagraphs(header.getParagraphs(), exactParagraphs, tokens);
				processTables(header.getTables(), exactParagraphs, tokens);
			}
			for (XWPFHeaderFooter footer : document.getFooterList()) {
				processParagraphs(footer.getParagraphs(), exactParagraphs, tokens);
				processTables(footer.getTables(), exactParagraphs, tokens);
			}

			try (OutputStream output = Files.newOutputStream(outputPath, StandardOpenOption.CREATE_NEW)) {
				document.write(output);
			}
		}
	}

	private void fillAdviceTables(XWPFDocument document, CustomerDocumentData data) {
		List<XWPFTable> tables = document.getTables();
		if (tables.size() > 0) {
			XWPFTable details = tables.get(0);
			setTableCell(details, 0, 1, data.generatedDate);
			setTableCell(details, 1, 1, data.fullName);
			setTableCell(details, 2, 1, valueOrNotRecorded(data.dateOfBirth));
			setTableCell(details, 3, 1, data.matter);
			setTableCell(details, 4, 1, data.reference);
		}
		if (tables.size() > 1) {
			XWPFTable acknowledgement = tables.get(1);
			setTableCell(acknowledgement, 0, 1, data.fullName);
			setTableCell(acknowledgement, 2, 1, data.generatedDate);
			setTableCell(acknowledgement, 3, 1, "Not applicable");
		}
	}

	private void setTableCell(XWPFTable table, int row, int column, String value) {
		if (row >= table.getNumberOfRows() || column >= table.getRow(row).getTableCells().size())
			return;
		XWPFTableCell cell = table.getRow(row).getCell(column);
		if (cell.getParagraphs().isEmpty())
			cell.addParagraph();
		setParagraphText(cell.getParagraphs().get(0), valueOrNotRecorded(value));
	}

	private Map<String, String> buildTokenReplacements(CustomerDocumentData data) {
		Map<String, String> replacements = new LinkedHashMap<String, String>();
		replacements.put("[PRACTICE NAME]", PRACTICE_NAME);
		replacements.put("[REGISTERED MIGRATION AGENT NAME]", AGENT_NAME);
		replacements.put("[CLIENT NAME]", data.fullName);
		replacements.put("[FULL LEGAL NAME]", data.fullName);
		replacements.put("[VISA / SPONSORSHIP / REVIEW TYPE]", data.matter);
		replacements.put("[VISA / APPLICATION TYPE]", data.matter);
		replacements.put("[FILE REFERENCE]", data.reference);
		replacements.put("[DD/MM/YYYY]", valueOrNotRecorded(data.dateOfBirth));
		replacements.put("[DATE]", data.generatedDate);
		replacements.put("[NAME]", AGENT_NAME);
		replacements.put("[NUMBER]", AGENT_MARN);
		replacements.put("[ADDRESS]", PRACTICE_ADDRESS);
		replacements.put("[EMAIL]", PRACTICE_EMAIL);
		replacements.put("[PHONE]", PRACTICE_PHONE);
		return replacements;
	}

	private Map<String, String> buildExactParagraphReplacements(CustomerDocumentData data) {
		Map<String, String> replacements = new LinkedHashMap<String, String>();
		replacements.put("[Summarise the client's current visa status, location and relevant dates.]",
				data.currentVisaSummary);
		replacements.put("[Summarise the client's objective and proposed visa pathway.]", data.objectiveSummary);
		replacements.put("[Summarise key family, employment, study, sponsorship or business circumstances.]",
				data.circumstancesSummary);
		replacements.put("[Record any previous applications, refusals, cancellations, compliance issues or other material facts.]",
				data.previousHistorySummary);
		replacements.put("[The client appears / does not appear] to meet the threshold requirements for [PATHWAY].",
				"The registered migration agent must complete the preliminary eligibility assessment for "
						+ data.matter + " after reviewing all customer information and supporting documents.");
		replacements.put("The following criteria appear capable of being satisfied: [INSERT].",
				"The criteria that appear capable of being satisfied: " + PENDING_AGENT_REVIEW);
		replacements.put("The following criteria require further evidence or clarification: [INSERT].",
				"Criteria requiring further evidence or clarification: " + PENDING_AGENT_REVIEW);
		replacements.put("The following issue may prevent or materially affect a successful outcome: [INSERT / NONE IDENTIFIED].",
				"Issues that may prevent or materially affect a successful outcome: " + PENDING_AGENT_REVIEW);
		replacements.put("Identity and civil status documents: [INSERT].",
				"Identity and civil status information currently recorded: " + data.identitySummary);
		replacements.put("Current and previous visa records: [INSERT].",
				"Current and previous visa records: " + PENDING_AGENT_REVIEW);
		replacements.put("Evidence addressing the relevant criteria: [INSERT].",
				"Evidence addressing the relevant criteria: " + PENDING_AGENT_REVIEW);
		replacements.put("Any additional documents requested after our review: [INSERT].",
				"Any additional documents requested after review: " + PENDING_AGENT_REVIEW);
		replacements.put("Current visa expiry / cessation date: [DATE / NOT APPLICABLE].",
				"Current visa expiry / cessation date: " + valueOrNotRecorded(data.visaExpiry) + ".");
		replacements.put("Application, review or response deadline: [DATE / NOT APPLICABLE].",
				"Application, review or response deadline: Not recorded in the current customer information.");
		replacements.put("Skills assessment, English test, nomination, health or character validity: [INSERT].",
				"Study and language information currently recorded: " + data.studyAndLanguageSummary);
		replacements.put("Proposed travel or other critical event: [INSERT].",
				"Proposed travel or other critical event: Not recorded in the current customer information.");
		replacements.put("Our preliminary recommendation is to [PROCEED / NOT PROCEED / OBTAIN FURTHER EVIDENCE] in relation to [MATTER], subject to the qualifications and outstanding matters in this letter. The immediate next step is [INSERT ACTION].",
				"A final recommendation regarding " + data.matter
						+ " must be completed by the registered migration agent after review. The immediate next step is to verify the customer's information and supporting documents.");
		replacements.put("Template note: This document is a general starting template only. It must be tailored to the client's facts, the relevant visa subclass and the law in force when advice is given.", "");
		return replacements;
	}

	private void processParagraphs(List<XWPFParagraph> paragraphs, Map<String, String> exactParagraphs,
			Map<String, String> tokens) {
		for (XWPFParagraph paragraph : paragraphs) {
			String exactReplacement = exactParagraphs.get(paragraph.getText());
			if (exactReplacement != null || exactParagraphs.containsKey(paragraph.getText())) {
				setParagraphText(paragraph, exactReplacement == null ? "" : exactReplacement);
				continue;
			}
			for (XWPFRun run : paragraph.getRuns()) {
				for (int i = 0; i < run.getCTR().sizeOfTArray(); i++) {
					String text = run.getText(i);
					if (text == null)
						continue;
					String replaced = replaceTokens(text, tokens);
					if (!text.equals(replaced))
						run.setText(replaced, i);
				}
			}
		}
	}

	private void processTables(List<XWPFTable> tables, Map<String, String> exactParagraphs,
			Map<String, String> tokens) {
		for (XWPFTable table : tables) {
			for (org.apache.poi.xwpf.usermodel.XWPFTableRow row : table.getRows()) {
				for (XWPFTableCell cell : row.getTableCells()) {
					processParagraphs(cell.getParagraphs(), exactParagraphs, tokens);
					processTables(cell.getTables(), exactParagraphs, tokens);
				}
			}
		}
	}

	private String replaceTokens(String text, Map<String, String> replacements) {
		String result = text;
		for (Map.Entry<String, String> entry : replacements.entrySet())
			result = result.replace(entry.getKey(), valueOrNotRecorded(entry.getValue()));
		return result;
	}

	private void setParagraphText(XWPFParagraph paragraph, String value) {
		List<XWPFRun> runs = paragraph.getRuns();
		if (runs.isEmpty()) {
			paragraph.createRun().setText(value);
			return;
		}
		XWPFRun firstRun = runs.get(0);
		for (int i = runs.size() - 1; i > 0; i--)
			paragraph.removeRun(i);
		if (firstRun.getCTR().sizeOfTArray() == 0) {
			firstRun.setText(value);
		} else {
			firstRun.setText(value, 0);
			while (firstRun.getCTR().sizeOfTArray() > 1)
				firstRun.getCTR().removeT(1);
		}
	}

	private CustomerDocumentData buildCustomerData(PortalDTO portalDto) {
		JsonNode formData = parseFormData(portalDto.getJsonStr());
		JsonNode contractData = parseFormData(portalDto.getContractStr());
		CustomerDocumentData data = new CustomerDocumentData();
		data.contractTemplate = findContractTemplateName(contractData);

		data.firstName = findText(formData, "firstName", "firstname", "givenName", "givenNames");
		data.lastName = findText(formData, "lastName", "lastname", "surname", "familyName");
		data.fullName = firstNonEmpty(portalDto.getName(),
				findText(formData, "fullName", "fullLegalName", "clientName", "applicantName"),
				joinName(data.firstName, data.lastName));
		fillNamePartsFromFullName(data);
		data.email = findText(formData, "email", "emailAddress", "userEmail");
		data.mobile = findText(formData, "mobile", "mobilePhone", "phone", "phoneNumber", "telephone");
		data.address = findText(formData, "residentialAddress", "currentAddress", "homeAddress", "address");
		data.passport = firstNonEmpty(portalDto.getPassport(),
				findText(formData, "passport", "passportNumber", "passportNo"));
		data.dateOfBirth = portalDto.getBirthday() == null
				? findDate(formData, "birthday", "dateOfBirth", "dob")
				: formatDate(portalDto.getBirthday());
		data.birthCountry = findText(formData, "birthCountry", "countryOfBirth");
		data.nationality = findText(formData, "nationality", "citizenship");
		data.maritalStatus = findText(formData, "maritalStatus", "relationshipStatus");
		data.location = findText(formData, "currentLocation", "location", "countryOfResidence");
		data.visaStatus = findText(formData, "currentVisaStatus", "visaStatus", "currentVisa");
		data.visaExpiry = firstNonEmpty(
				portalDto.getStudentVisaExpirationDate() == null ? null : formatDate(portalDto.getStudentVisaExpirationDate()),
				portalDto.getVisaExpirationDate() == null ? null : formatDate(portalDto.getVisaExpirationDate()),
				findDate(formData, "studentVisaExpirationDate", "visaExpirationDate", "visaExpiryDate"));
		data.maraId = portalDto.getMaraId();
		data.matter = firstNonEmpty(portalDto.getPortalTypeName(),
				portalDto.getPortalType() == null ? null : portalDto.getPortalType().getName(),
				findText(formData, "visaSubclass", "visaType", "applicationType", "matter"),
				"Australian immigration matter");
		data.reference = "Portal-" + portalDto.getId();
		data.generatedDate = new SimpleDateFormat("dd/MM/yyyy").format(new Date());

		String education = buildEducationSummary(formData);
		String language = buildLanguageSummary(formData);
		data.currentVisaSummary = buildCurrentVisaSummary(data);
		data.objectiveSummary = "The matter currently recorded for the customer is " + data.matter + ".";
		data.circumstancesSummary = buildCircumstancesSummary(data, education, language);
		String previousHistory = findText(formData, "previousVisaHistory", "visaHistory", "refusalHistory",
				"cancellationHistory", "complianceHistory");
		data.previousHistorySummary = StringUtil.isNotEmpty(previousHistory) ? previousHistory
				: "No previous application, refusal, cancellation or compliance history is recorded in the current customer information.";
		data.identitySummary = buildIdentitySummary(data);
		data.studyAndLanguageSummary = joinNonEmpty("; ", education, language);
		if (StringUtil.isEmpty(data.studyAndLanguageSummary))
			data.studyAndLanguageSummary = "Not recorded in the current customer information.";
		return data;
	}

	private String findContractTemplateName(JsonNode contractData) {
		if (contractData == null || !contractData.isObject())
			return null;
		JsonNode contract = contractData.get("contract");
		if (contract == null || !contract.isObject())
			return null;
		return firstNonEmpty(nodeToText(contract.get("contractfileName")),
				nodeToText(contract.get("contractFileName")));
	}

	/**
	 * PDF最后一页需要分别填写First Name和Last Name。当前端只传客户全名时，
	 * 按第一个空白字符分成两段；已有独立姓名字段时保留原值，仅补充缺失部分。
	 */
	private void fillNamePartsFromFullName(CustomerDocumentData data) {
		if (data == null || StringUtil.isEmpty(data.fullName))
			return;
		String[] nameParts = data.fullName.trim().split("\\s+", 2);
		if (StringUtil.isEmpty(data.firstName) && nameParts.length > 0)
			data.firstName = nameParts[0];
		if (StringUtil.isEmpty(data.lastName) && nameParts.length > 1)
			data.lastName = nameParts[1];
	}

	private String buildCurrentVisaSummary(CustomerDocumentData data) {
		List<String> values = new ArrayList<String>();
		addLabelValue(values, "Current visa/status", data.visaStatus);
		addLabelValue(values, "Current location", data.location);
		addLabelValue(values, "Visa expiry", data.visaExpiry);
		return values.isEmpty() ? "No current visa status, location or expiry information is recorded in the current customer information."
				: joinNonEmpty("; ", values.toArray(new String[values.size()])) + ".";
	}

	private String buildCircumstancesSummary(CustomerDocumentData data, String education, String language) {
		List<String> values = new ArrayList<String>();
		addLabelValue(values, "Country of birth", data.birthCountry);
		addLabelValue(values, "Nationality", data.nationality);
		addLabelValue(values, "Marital status", data.maritalStatus);
		addLabelValue(values, "Residential address", data.address);
		addLabelValue(values, "Email", data.email);
		addLabelValue(values, "Mobile", data.mobile);
		addLabelValue(values, "Study", education);
		addLabelValue(values, "Language", language);
		return values.isEmpty() ? "No family, employment, study, sponsorship or business circumstances are recorded in the current customer information."
				: joinNonEmpty("; ", values.toArray(new String[values.size()])) + ".";
	}

	private String buildIdentitySummary(CustomerDocumentData data) {
		List<String> values = new ArrayList<String>();
		addLabelValue(values, "Full legal name", data.fullName);
		addLabelValue(values, "Date of birth", data.dateOfBirth);
		addLabelValue(values, "Passport number", data.passport);
		addLabelValue(values, "Nationality", data.nationality);
		addLabelValue(values, "Marital status", data.maritalStatus);
		return joinNonEmpty("; ", values.toArray(new String[values.size()]));
	}

	private String buildEducationSummary(JsonNode root) {
		JsonNode education = findNode(root, "education");
		if (education == null)
			return buildEducationItem(root);
		List<String> summaries = new ArrayList<String>();
		if (education.isArray()) {
			for (JsonNode item : education)
				addIfNotEmpty(summaries, buildEducationItem(item));
		} else {
			addIfNotEmpty(summaries, buildEducationItem(education));
		}
		return joinNonEmpty(" | ", summaries.toArray(new String[summaries.size()]));
	}

	private String buildEducationItem(JsonNode item) {
		List<String> values = new ArrayList<String>();
		addLabelValue(values, "Institution", findText(item, "schoolName", "cricosName", "institutionName"));
		addLabelValue(values, "Course", findText(item, "courseName", "eduCourseType", "courseType"));
		addLabelValue(values, "Start", findDate(item, "startDate"));
		addLabelValue(values, "End", findDate(item, "endDate"));
		addLabelValue(values, "Completion", findDate(item, "eduCourseCompletionDate", "completionDate"));
		return joinNonEmpty(", ", values.toArray(new String[values.size()]));
	}

	private String buildLanguageSummary(JsonNode root) {
		JsonNode language = findNode(root, "language");
		if (language == null)
			return findText(root, "langType", "languageTestType", "testType");
		List<String> values = new ArrayList<String>();
		if (language.isArray()) {
			for (JsonNode item : language)
				addIfNotEmpty(values, findText(item, "langType", "languageTestType", "testType"));
		} else {
			addIfNotEmpty(values, findText(language, "langType", "languageTestType", "testType"));
		}
		return joinNonEmpty(", ", values.toArray(new String[values.size()]));
	}

	private JsonNode parseFormData(String jsonStr) {
		if (StringUtil.isEmpty(jsonStr))
			return null;
		try {
			JsonNode node = OBJECT_MAPPER.readTree(jsonStr.trim());
			if (node != null && node.isTextual() && StringUtil.isNotEmpty(node.asText()))
				node = OBJECT_MAPPER.readTree(node.asText());
			return node;
		} catch (Exception firstException) {
			try {
				return OBJECT_MAPPER.readTree(jsonStr.replace("\\\"", "\"").trim());
			} catch (Exception ignored) {
				return null;
			}
		}
	}

	private String findText(JsonNode root, String... fieldNames) {
		JsonNode node = findNode(root, fieldNames);
		return nodeToText(node);
	}

	private String findDate(JsonNode root, String... fieldNames) {
		JsonNode node = findNode(root, fieldNames);
		if (node == null || node.isNull())
			return null;
		if (node.isNumber())
			return formatTimestamp(node.asLong());
		String value = node.asText();
		if (StringUtil.isEmpty(value))
			return null;
		if (value.matches("-?\\d{10,13}"))
			return formatTimestamp(Long.parseLong(value));
		Date parsed = parseDate(value);
		return parsed == null ? value : formatDate(parsed);
	}

	private JsonNode findNode(JsonNode root, String... fieldNames) {
		if (root == null || fieldNames == null)
			return null;
		for (String fieldName : fieldNames) {
			JsonNode found = findNodeByField(root, fieldName, 0);
			if (found != null && !found.isNull())
				return found;
		}
		return null;
	}

	private JsonNode findNodeByField(JsonNode node, String fieldName, int depth) {
		if (node == null || node.isNull() || depth > 8)
			return null;
		if (node.isObject()) {
			JsonNode direct = node.get(fieldName);
			if (direct != null && !direct.isNull() && !(direct.isTextual() && StringUtil.isEmpty(direct.asText())))
				return direct;
			java.util.Iterator<JsonNode> children = node.elements();
			while (children.hasNext()) {
				JsonNode found = findNodeByField(children.next(), fieldName, depth + 1);
				if (found != null)
					return found;
			}
		} else if (node.isArray()) {
			for (JsonNode child : node) {
				JsonNode found = findNodeByField(child, fieldName, depth + 1);
				if (found != null)
					return found;
			}
		}
		return null;
	}

	private String nodeToText(JsonNode node) {
		if (node == null || node.isNull())
			return null;
		if (node.isValueNode())
			return node.asText().trim();
		List<String> values = new ArrayList<String>();
		collectScalarValues(node, values, 0);
		return joinNonEmpty(", ", values.toArray(new String[values.size()]));
	}

	private void collectScalarValues(JsonNode node, List<String> values, int depth) {
		if (node == null || node.isNull() || depth > 4)
			return;
		if (node.isValueNode()) {
			addIfNotEmpty(values, node.asText());
			return;
		}
		for (JsonNode child : node)
			collectScalarValues(child, values, depth + 1);
	}

	private Date parseDate(String value) {
		String[] patterns = { "yyyy-MM-dd", "dd/MM/yyyy", "yyyy-MM-dd'T'HH:mm:ss.SSSX", "yyyy-MM-dd'T'HH:mm:ssX" };
		for (String pattern : patterns) {
			try {
				SimpleDateFormat format = new SimpleDateFormat(pattern);
				format.setLenient(false);
				return format.parse(value);
			} catch (ParseException ignored) {
			}
		}
		return null;
	}

	private String formatTimestamp(long timestamp) {
		if (timestamp <= 0)
			return null;
		if (timestamp < 100000000000L)
			timestamp *= 1000L;
		return formatDate(new Date(timestamp));
	}

	private String formatDate(Date date) {
		return date == null ? null : new SimpleDateFormat("dd/MM/yyyy").format(date);
	}

	private String joinName(String firstName, String lastName) {
		return joinNonEmpty(" ", firstName, lastName);
	}

	private String firstNonEmpty(String... values) {
		if (values != null) {
			for (String value : values) {
				if (StringUtil.isNotEmpty(value))
					return value.trim();
			}
		}
		return null;
	}

	private String joinNonEmpty(String separator, String... values) {
		List<String> nonEmpty = new ArrayList<String>();
		if (values != null) {
			for (String value : values)
				addIfNotEmpty(nonEmpty, value);
		}
		return nonEmpty.isEmpty() ? null : String.join(separator, nonEmpty);
	}

	private void addLabelValue(List<String> values, String label, String value) {
		if (StringUtil.isNotEmpty(value))
			values.add(label + ": " + value.trim());
	}

	private void addIfNotEmpty(List<String> values, String value) {
		if (StringUtil.isNotEmpty(value))
			values.add(value.trim());
	}

	private String valueOrNotRecorded(String value) {
		return StringUtil.isEmpty(value) ? "Not recorded" : value;
	}

	private String safeFileName(String value) {
		String safe = StringUtil.isEmpty(value) ? "client" : value.trim();
		safe = safe.replaceAll("[^\\p{L}\\p{N}._-]+", "_");
		if (safe.length() > 60)
			safe = safe.substring(0, 60);
		return StringUtil.isEmpty(safe) ? "client" : safe;
	}

	private void deleteGeneratedFile(Path path) {
		if (path == null)
			return;
		try {
			Files.deleteIfExists(path);
		} catch (Exception ignored) {
		}
	}

	private static class CustomerDocumentData {
		private String contractTemplate;
		private String fullName;
		private String firstName;
		private String lastName;
		private String dateOfBirth;
		private String passport;
		private String address;
		private String email;
		private String mobile;
		private String birthCountry;
		private String nationality;
		private String maritalStatus;
		private String location;
		private String visaStatus;
		private String visaExpiry;
		private int maraId;
		private String matter;
		private String reference;
		private String generatedDate;
		private String currentVisaSummary;
		private String objectiveSummary;
		private String circumstancesSummary;
		private String previousHistorySummary;
		private String identitySummary;
		private String studyAndLanguageSummary;
	}
}
