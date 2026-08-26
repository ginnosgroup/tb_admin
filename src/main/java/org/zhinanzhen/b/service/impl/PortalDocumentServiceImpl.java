package org.zhinanzhen.b.service.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFHeaderFooter;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.zhinanzhen.b.service.PortalDocumentService;
import org.zhinanzhen.b.service.pojo.PortalDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ikasoa.core.ErrorCodeEnum;
import com.ikasoa.core.utils.StringUtil;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

@Service("PortalDocumentService")
public class PortalDocumentServiceImpl extends BaseService implements PortalDocumentService {

	private static final String CONTRACT_TEMPLATE = "Contract-CEM-SYD-Comp Final Version.pdf";
	private static final String ADVICE_TEMPLATE = "MARA_Basic_Letter_of_Advice_Template.docx";
	private static final String PRACTICE_NAME = "Compass Education and Migration Pty Ltd";
	private static final String AGENT_NAME = "Tonglu Ge";
	private static final String AGENT_MARN = "1687805";
	private static final String PRACTICE_ADDRESS = "Level 36, 680 George Street, Sydney, NSW, 2000, Australia";
	private static final String PRACTICE_EMAIL = "admin@globalznz.com";
	private static final String PRACTICE_PHONE = "(02) 9283 1227";
	private static final String PENDING_AGENT_REVIEW = "To be completed by the registered migration agent after review.";
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Value("${portal.document.output-dir:C:/Users/Admin/Desktop/dataT/xian/user}")
	private String outputDirectory;

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
			Path outputDir = Paths.get(outputDirectory).toAbsolutePath().normalize();
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
			paths.put("contractPdf", contractPath.toString());
			paths.put("letterOfAdviceDocx", advicePath.toString());
			return paths;
		} catch (Exception e) {
			deleteGeneratedFile(contractPath);
			deleteGeneratedFile(advicePath);
			ServiceException exception = new ServiceException("生成客户合同和建议信失败: " + e.getMessage(), e);
			exception.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw exception;
		}
	}

	@Override
	public void sendGeneratedDocuments(PortalDTO portalDto, Map<String, String> generatedDocumentPaths)
			throws ServiceException {
		if (portalDto == null || portalDto.getId() <= 0) {
			throw serviceException("案件信息无效，无法发送合同和建议信邮件.", ErrorCodeEnum.PARAMETER_ERROR.code(), null);
		}

		CustomerDocumentData data = buildCustomerData(portalDto);
		if (StringUtil.isEmpty(data.email)) {
			throw serviceException("客户邮箱为空，合同和建议信未发送.", ErrorCodeEnum.PARAMETER_ERROR.code(), null);
		}

		Path contractPath = requireGeneratedFile(generatedDocumentPaths, "contractPdf", "合同PDF");
		Path advicePath = requireGeneratedFile(generatedDocumentPaths, "letterOfAdviceDocx", "建议信Word文件");
		String adviserName = firstNonEmpty(portalDto.getAdviserName(), "您的顾问");
		String title = "【指南针留学移民】485签证合同、建议信及材料准备清单";
		String content = build485PreparationEmail(data.fullName, adviserName);
		try {
			sendMailWithAttachments(data.email, title, content, contractPath.toFile(), advicePath.toFile());
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			throw serviceException("发送合同和建议信邮件失败: " + e.getMessage(), ErrorCodeEnum.OTHER_ERROR.code(), e);
		}
	}

	private Path requireGeneratedFile(Map<String, String> generatedDocumentPaths, String key, String description)
			throws ServiceException {
		String pathValue = generatedDocumentPaths == null ? null : generatedDocumentPaths.get(key);
		if (StringUtil.isEmpty(pathValue)) {
			throw serviceException(description + "路径为空，邮件未发送.", ErrorCodeEnum.PARAMETER_ERROR.code(), null);
		}
		Path path = Paths.get(pathValue).toAbsolutePath().normalize();
		if (!Files.isRegularFile(path)) {
			throw serviceException(description + "不存在: " + path, ErrorCodeEnum.DATA_ERROR.code(), null);
		}
		return path;
	}

	private String build485PreparationEmail(String customerName, String adviserName) {
		String safeCustomerName = htmlEscape(firstNonEmpty(customerName, "同学"));
		String safeAdviserName = htmlEscape(firstNonEmpty(adviserName, "您的顾问"));
		StringBuilder content = new StringBuilder();
		content.append("<p>亲爱的").append(safeCustomerName).append("同学，您好：</p>");
		content.append("<p>感谢您对指南针留学移民的信任。我们已根据目前系统中登记的客户信息，为您生成了485签证服务合同和建议信，并随本邮件一并发送。</p>");
		content.append("<p>请您下载并仔细核对两份附件中的姓名、联系方式、护照及学习经历等信息。确认无误后，请按文件要求填写、签署并回复给我们；如发现任何信息有误或需要补充，请先不要签署，并及时联系您的顾问 <strong>")
				.append(safeAdviserName).append("</strong>。</p>");
		content.append("<p>为了便于后续评估和递交，请您同步开始准备以下材料。每位申请人的情况和所适用的485分支可能不同，最终材料以顾问审核及澳洲内政部递交时的最新要求为准。</p>");
		content.append("<hr/>");
		content.append("<h3>485签证材料准备清单</h3>");
		content.append("<ol>");
		content.append("<li><strong>身份材料：</strong>当前护照个人信息页，以及包含签发日期、有效期的页面；如有身份证、曾用名或改名情况，请一并提供身份证及改名证明。</li>");
		content.append("<li><strong>澳洲学习材料：</strong>学校完成信（Completion Letter）、最终成绩单、毕业证或学位证，以及课程和入学确认相关材料（如CoE）。</li>");
		content.append("<li><strong>英语能力材料：</strong>符合当前485要求的有效英语考试成绩单或可核验的考试信息。</li>");
		content.append("<li><strong>品行材料：</strong>澳大利亚联邦警察（AFP）无犯罪记录证明或申请凭证；如顾问要求，请补充其他国家或地区的无犯罪证明及相关品行表格。</li>");
		content.append("<li><strong>健康保险材料：</strong>覆盖申请人及随行家庭成员的有效澳洲健康保险证明；如适用，也可提供Medicare相关证明供顾问审核。</li>");
		content.append("<li><strong>职业评估材料（如适用）：</strong>如果申请Post-Vocational Education Work stream，请准备提名职业及相关职业评估申请或结果材料。</li>");
		content.append("<li><strong>签证与出入境材料：</strong>当前及以往澳洲签证批准信、签证申请记录，以及曾发生拒签、取消签证或其他移民事项的说明和文件（如有）。</li>");
		content.append("<li><strong>家庭成员材料（如适用）：</strong>配偶或子女的护照、出生证明、结婚证、同居关系证明及其他身份、健康、品行材料。</li>");
		content.append("<li><strong>文件整理要求：</strong>请提供清晰的彩色扫描件；非英文文件同时提供原件和英文翻译件；多页文件尽量合并为一个完整文件。</li>");
		content.append("<li><strong>其他补充材料：</strong>体检、Form 80、Form 1221或其他证明材料请在顾问确认适用或移民局要求后准备。</li>");
		content.append("</ol>");
		content.append("<p>请先按上述清单整理现有材料，并将缺失或仍在办理中的项目告知您的顾问，方便我们为您安排下一步工作。感谢您的配合！</p>");
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
		ClassPathResource resource = new ClassPathResource(CONTRACT_TEMPLATE);
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
					stamper.setFormFlattening(false);
				} finally {
					stamper.close();
				}
			} finally {
				reader.close();
			}
		}
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
		CustomerDocumentData data = new CustomerDocumentData();

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
