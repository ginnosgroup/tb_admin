package org.zhinanzhen.b.utils;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.core.io.ClassPathResource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ikasoa.core.utils.StringUtil;

/**
 * 根据案件表单数据填写 Form 956。
 *
 * Form 956 模板是扁平化 PDF，不能通过 AcroForm 字段直接赋值，因此使用 PDFBox
 * 在模板固定输入框内覆盖写入。模板只读，输出文件单独保存。
 */
public final class Form956PdfGenerator {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private Form956PdfGenerator() {
	}

	/**
	 * 从 classpath 模板生成 Form 956。
	 *
	 * @param templateResourceName classpath 下的模板名，例如 1000023_956_Allison.pdf
	 * @param outputPath 输出文件，不能与模板文件相同
	 * @param jsonStr 客户表单 JSON
	 * @param contractStr 合同表单 JSON
	 * @param fallbackMaraId 当表单 JSON 中没有 maraId 时用于填写 MARN
	 */
	public static void generateFromResource(String templateResourceName, Path outputPath, String jsonStr,
			String contractStr, int fallbackMaraId) throws IOException {
		if (StringUtil.isEmpty(templateResourceName))
			throw new IOException("Form 956模板名称为空");
		ClassPathResource resource = new ClassPathResource(templateResourceName);
		if (!resource.exists())
			throw new IOException("Form 956模板文件不存在: " + templateResourceName);
		try (InputStream inputStream = resource.getInputStream()) {
			generate(inputStream, outputPath, jsonStr, contractStr, fallbackMaraId);
		}
	}

	private static void generate(InputStream templateInputStream, Path outputPath, String jsonStr,
			String contractStr, int fallbackMaraId) throws IOException {
		if (templateInputStream == null)
			throw new IOException("Form 956模板输入流为空");
		if (outputPath == null)
			throw new IOException("Form 956输出路径为空");
		Path normalizedOutput = outputPath.toAbsolutePath().normalize();
		Path parent = normalizedOutput.getParent();
		if (parent != null)
			Files.createDirectories(parent);

		JsonNode jsonData = parseJson(jsonStr, "json_str");
		JsonNode contractData = parseJson(contractStr, "contract_str");
		Form956Data data = buildData(jsonData, contractData, fallbackMaraId);
		try (PDDocument document = PDDocument.load(templateInputStream)) {
			if (document.getNumberOfPages() < 4)
				throw new IOException("Form 956模板页数不足，无法填写第13至16题");
			PDFont font = loadFont(document);
			fillFields(document, font, data);
			document.save(normalizedOutput.toFile());
		}
	}

	private static JsonNode parseJson(String rawValue, String columnName) throws IOException {
		if (StringUtil.isEmpty(rawValue) || rawValue.trim().length() == 0)
			return null;
		String candidate = rawValue.trim();
		for (int attempt = 0; attempt < 8; attempt++) {
			try {
				JsonNode parsed = OBJECT_MAPPER.readTree(candidate);
				if (parsed != null && parsed.isTextual() && !candidate.equals(parsed.asText())) {
					candidate = parsed.asText().trim();
					continue;
				}
				if (parsed == null || parsed.isObject())
					return parsed;
			} catch (Exception ignored) {
				// 继续尝试前端重复转义后的 JSON。
			}

			String unescaped = candidate.replace("\\\"", "\"").replace("\\@", "@");
			if (candidate.equals(unescaped))
				break;
			candidate = unescaped.trim();
		}
		throw new IOException(columnName + "不是合法JSON");
	}

	private static Form956Data buildData(JsonNode jsonData, JsonNode contractData, int fallbackMaraId) {
		JsonNode[] roots = { contractData, jsonData };
		Form956Data data = new Form956Data();

		data.agentFamilyName = first(roots,
				"basicInfo.agentFamilyName", "basicInfo.migrationAgentFamilyName",
				"agent.familyName", "migrationAgent.familyName", "registeredAgent.familyName");
		data.agentGivenNames = first(roots,
				"basicInfo.agentGivenNames", "basicInfo.migrationAgentGivenNames",
				"agent.givenNames", "migrationAgent.givenNames", "registeredAgent.givenNames");
		data.agentOrganisation = first(roots,
				"basicInfo.agentOrganisation", "basicInfo.migrationAgentOrganisation",
				"agent.organisation", "migrationAgent.organisation", "registeredAgent.organisation");
		data.agentAddress = first(roots,
				"basicInfo.agentAddress", "basicInfo.migrationAgentAddress",
				"agent.address", "migrationAgent.address", "registeredAgent.address");
		data.agentCorrespondenceAddress = first(roots,
				"basicInfo.agentCorrespondenceAddress", "basicInfo.migrationAgentCorrespondenceAddress",
				"agent.correspondenceAddress", "migrationAgent.correspondenceAddress");
		data.agentOfficePhone = first(roots,
				"basicInfo.agentOfficePhone", "basicInfo.migrationAgentOfficePhone",
				"agent.officePhone", "migrationAgent.officePhone");
		data.agentMobile = first(roots,
				"basicInfo.agentMobile", "basicInfo.migrationAgentMobile",
				"agent.mobile", "migrationAgent.mobile");
		data.agentEmail = first(roots,
				"basicInfo.agentEmail", "basicInfo.migrationAgentEmail",
				"agent.email", "migrationAgent.email");
		data.marn = first(roots,
				"basicInfo.maraId", "basicInfo.marn", "maraId", "marn",
				"agent.maraId", "agent.marn", "migrationAgent.marn");
		if (isBlank(data.marn) && fallbackMaraId > 0)
			data.marn = String.valueOf(fallbackMaraId);

		data.clientFullName = first(roots,
				"basicInfo.nameOfClient", "basicInfo.name", "basicInfo.fullName",
				"basicInfo.clientName", "nameOfClient", "fullName", "clientName");
		data.clientFamilyName = first(roots,
				"basicInfo.lastName", "basicInfo.lastname", "basicInfo.surname",
				"basicInfo.familyName", "lastName", "lastname", "surname", "familyName");
		data.clientGivenNames = first(roots,
				"basicInfo.firstName", "basicInfo.firstname", "basicInfo.givenName",
				"basicInfo.givenNames", "firstName", "firstname", "givenName", "givenNames");
		if (isBlank(data.clientFamilyName) || isBlank(data.clientGivenNames))
			splitName(data);

		data.clientBirthday = date(roots,
				"basicInfo.birthday", "basicInfo.dateOfBirth", "basicInfo.dob",
				"birthday", "dateOfBirth", "dob");
		data.clientOrganisation = first(roots,
				"basicInfo.organisationName", "basicInfo.organizationName", "organisationName",
				"organizationName", "clientOrganisation");
		data.clientAddress = first(roots,
				"basicInfo.addressOfClient", "basicInfo.address", "basicInfo.residentialAddress",
				"addressOfClient", "address", "residentialAddress", "currentAddress");
		data.clientPhone = first(roots,
				"basicInfo.phone", "basicInfo.phoneNumber", "basicInfo.telephone",
				"phone", "phoneNumber", "telephone");
		data.clientMobile = first(roots,
				"basicInfo.mobileNumber", "basicInfo.mobile", "mobileNumber", "mobile");
		data.clientId = first(roots,
				"basicInfo.clientId", "basicInfo.clientIdNumber", "clientId", "clientIdNumber");
		data.rid = first(roots, "basicInfo.requestId", "basicInfo.rid", "requestId", "rid");
		data.trn = first(roots,
				"basicInfo.transactionReferenceNumber", "basicInfo.trn",
				"transactionReferenceNumber", "trn");
		return data;
	}

	private static void fillFields(PDDocument document, PDFont font, Form956Data data) throws IOException {
		// PDF 坐标原点在左下角，坐标与测试方法使用的 Form 956 模板一致。
		try (PDPageContentStream page = new PDPageContentStream(document, document.getPage(2),
				PDPageContentStream.AppendMode.APPEND, true, true)) {
			field(page, font, 96, 416, 188, 16, data.agentFamilyName, 8);
			field(page, font, 96, 396, 188, 16, data.agentGivenNames, 8);
			field(page, font, 44, 308, 240, 32, data.agentOrganisation, 7);
			field(page, font, 44, 232, 240, 48, data.agentAddress, 7);
			field(page, font, 44, 144, 240, 48, data.agentCorrespondenceAddress, 7);
			field(page, font, 96, 88, 188, 16, data.agentOfficePhone, 8);
			field(page, font, 96, 68, 188, 16, data.agentMobile, 8);
			field(page, font, 380, 636, 188, 16, data.agentEmail, 8);
			field(page, font, 456, 516, 112, 16, data.marn, 8);
		}

		try (PDPageContentStream page = new PDPageContentStream(document, document.getPage(3),
				PDPageContentStream.AppendMode.APPEND, true, true)) {
			field(page, font, 96, 600, 188, 16, data.clientFamilyName, 8);
			field(page, font, 96, 580, 188, 16, data.clientGivenNames, 8);
			field(page, font, 96, 552, 88, 16, data.clientBirthday, 8);
			field(page, font, 44, 500, 240, 32, data.clientOrganisation, 7);
			field(page, font, 44, 432, 240, 48, data.clientAddress, 7);
			field(page, font, 96, 388, 188, 16, data.clientPhone, 8);
			field(page, font, 96, 368, 188, 16, data.clientMobile, 8);
			field(page, font, 160, 344, 124, 16, data.clientId, 8);
			field(page, font, 436, 288, 132, 16, data.rid, 8);
			field(page, font, 436, 264, 132, 16, data.trn, 8);
		}
	}

	private static void field(PDPageContentStream page, PDFont font, float x, float y, float width,
			float height, String value, float fontSize) throws IOException {
		page.setNonStrokingColor(Color.WHITE);
		page.addRect(x + 1, y + 1, width - 2, height - 2);
		page.fill();
		if (isBlank(value))
			return;

		String safeValue = safeText(font, value);
		float actualSize = fontSize;
		while (actualSize > 5 && font.getStringWidth(safeValue) / 1000f * actualSize > width - 6)
			actualSize -= 0.5f;
		if (font.getStringWidth(safeValue) / 1000f * actualSize > width - 6)
			safeValue = trimToWidth(font, safeValue, actualSize, width - 6);

		page.setNonStrokingColor(Color.BLACK);
		page.beginText();
		page.setFont(font, actualSize);
		page.newLineAtOffset(x + 3, y + Math.max(3, (height - actualSize) / 2));
		page.showText(safeValue);
		page.endText();
	}

	private static String trimToWidth(PDFont font, String value, float fontSize, float maxWidth)
			throws IOException {
		String result = value;
		while (result.length() > 1 && font.getStringWidth(result) / 1000f * fontSize > maxWidth)
			result = result.substring(0, result.length() - 1);
		return result;
	}

	private static String safeText(PDFont font, String value) {
		String normalized = value == null ? "" : value.replace('\r', ' ').replace('\n', ' ').trim();
		if (font instanceof PDType0Font)
			return normalized;
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < normalized.length(); i++) {
			char character = normalized.charAt(i);
			result.append(character >= 32 && character <= 126 ? character : '?');
		}
		return result.toString();
	}

	private static PDFont loadFont(PDDocument document) throws IOException {
		String[] fontPaths = {
				"C:\\Windows\\Fonts\\simhei.ttf",
				"C:\\Windows\\Fonts\\arial.ttf",
				"C:\\Windows\\Fonts\\calibri.ttf",
				"C:\\Windows\\Fonts\\segoeui.ttf" };
		for (String fontPath : fontPaths) {
			Path path = Paths.get(fontPath);
			if (Files.isRegularFile(path)) {
				try {
					return PDType0Font.load(document, path.toFile());
				} catch (IOException ignored) {
					// 当前机器字体不可嵌入时使用 PDF 内置字体继续生成。
				}
			}
		}
		return PDType1Font.HELVETICA;
	}

	private static String first(JsonNode[] roots, String... paths) {
		if (roots == null || paths == null)
			return null;
		for (JsonNode root : roots) {
			if (root == null)
				continue;
			for (String path : paths) {
				String value = text(get(root, path));
				if (!isBlank(value))
					return value;
			}
		}
		return null;
	}

	private static JsonNode get(JsonNode root, String path) {
		if (root == null || !root.isObject() || isBlank(path))
			return null;
		JsonNode current = root;
		for (String part : path.split("\\.")) {
			if (current == null || !current.isObject())
				return null;
			current = current.get(part);
		}
		return current;
	}

	private static String text(JsonNode value) {
		if (value == null || value.isNull() || !value.isValueNode())
			return null;
		String result = value.asText();
		return isBlank(result) || "null".equalsIgnoreCase(result) ? null : result.trim();
	}

	private static String date(JsonNode[] roots, String... paths) {
		String value = first(roots, paths);
		if (isBlank(value))
			return null;
		try {
			long timestamp = Long.parseLong(value);
			if (timestamp > 0) {
				if (timestamp < 100000000000L)
					timestamp *= 1000L;
				return new SimpleDateFormat("dd/MM/yyyy").format(new Date(timestamp));
			}
		} catch (NumberFormatException ignored) {
			// 不是时间戳时按常见日期字符串处理。
		}
		for (String pattern : new String[] { "yyyy-MM-dd", "yyyy/MM/dd", "dd/MM/yyyy" }) {
			try {
				Date parsed = new SimpleDateFormat(pattern).parse(value);
				return new SimpleDateFormat("dd/MM/yyyy").format(parsed);
			} catch (Exception ignored) {
				// 尝试下一个日期格式。
			}
		}
		return value;
	}

	private static void splitName(Form956Data data) {
		if (isBlank(data.clientFullName))
			return;
		String[] parts = data.clientFullName.trim().split("\\s+", 2);
		// Form 956 的字段顺序是 Family name 在前、Given names 在后。
		if (parts.length > 1) {
			if (isBlank(data.clientFamilyName))
				data.clientFamilyName = parts[0];
			if (isBlank(data.clientGivenNames))
				data.clientGivenNames = parts[1];
		} else if (isBlank(data.clientFamilyName) && isBlank(data.clientGivenNames)) {
			data.clientFamilyName = parts[0];
		}
	}

	private static boolean isBlank(String value) {
		return value == null || value.trim().length() == 0 || "null".equalsIgnoreCase(value.trim());
	}

	private static class Form956Data {
		private String agentFamilyName;
		private String agentGivenNames;
		private String agentOrganisation;
		private String agentAddress;
		private String agentCorrespondenceAddress;
		private String agentOfficePhone;
		private String agentMobile;
		private String agentEmail;
		private String marn;
		private String clientFullName;
		private String clientFamilyName;
		private String clientGivenNames;
		private String clientBirthday;
		private String clientOrganisation;
		private String clientAddress;
		private String clientPhone;
		private String clientMobile;
		private String clientId;
		private String rid;
		private String trn;
	}
}
