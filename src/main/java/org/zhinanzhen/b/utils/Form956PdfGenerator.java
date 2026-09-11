package org.zhinanzhen.b.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.springframework.core.io.ClassPathResource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ikasoa.core.utils.StringUtil;

/**
 * 根据案件表单数据填写 Form 956。
 *
 * 按 AcroForm 字段名称填写，并生成字段外观，兼容不同页数及缺失字段目录的模板。
 * 同时校验字段值和页面 Widget，避免页面上有文字而浏览器中的表单仍为空。
 * 模板只读，代理人预填信息及第15题不会因客户信息缺失而被清空。
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
	 * @param fallbackMaraId 保留旧接口兼容；数据库MARA主键不能作为注册号MARN填写
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

	/**
	 * 从文件系统模板生成 Form 956。模板路径可以是绝对路径，也可以是已解析到当前运行环境的存储路径。
	 */
	public static void generateFromPath(Path templatePath, Path outputPath, String jsonStr,
			String contractStr, int fallbackMaraId) throws IOException {
		if (templatePath == null)
			throw new IOException("Form 956模板路径为空");
		Path normalizedTemplate = templatePath.toAbsolutePath().normalize();
		if (!Files.isRegularFile(normalizedTemplate))
			throw new IOException("Form 956模板文件不存在: " + normalizedTemplate);
		if (outputPath != null && (normalizedTemplate.equals(outputPath.toAbsolutePath().normalize())
				|| (Files.exists(outputPath) && Files.isSameFile(normalizedTemplate, outputPath))))
			throw new IOException("Form 956输出文件不能覆盖模板");
		try (InputStream inputStream = Files.newInputStream(normalizedTemplate)) {
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
		if (isBlank(data.clientFamilyName) && isBlank(data.clientGivenNames))
			throw new IOException("Form 956客户姓名为空，请检查案件json_str和contract_str中的basicInfo");
		Map<String, String> values = fieldValues(data);
		try (PDDocument document = PDDocument.load(templateInputStream)) {
			PDAcroForm form = prepareForm(document, values);
			PDFont font = loadFont(document, values);
			PDResources resources = form.getDefaultResources();
			if (resources == null) {
				resources = new PDResources();
				form.setDefaultResources(resources);
			}
			COSName fontName = resources.add(font);
			form.setNeedAppearances(false);
			Map<String, PDField> fields = collectFields(form);
			for (Map.Entry<String, String> entry : values.entrySet()) {
				PDTextField field = requireTextField(fields, entry.getKey());
				field.setDefaultAppearance("/" + fontName.getName() + " 0 Tf 0 g");
				field.setValue(entry.getValue());
			}
			// 先保存到临时文件并重新打开验证，只有值和外观都完整时才交给邮件/数据库流程。
			Path temporary = Files.createTempFile(parent, "form956-", ".pdf");
			try {
				document.save(temporary.toFile());
				try (PDDocument written = PDDocument.load(temporary.toFile())) {
					verifyFields(written, values);
				}
				Files.move(temporary, normalizedOutput, StandardCopyOption.REPLACE_EXISTING);
			} finally {
				Files.deleteIfExists(temporary);
			}
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
				"basicInfo.marn", "marn", "agent.marn", "migrationAgent.marn");

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

	private static Map<String, String> fieldValues(Form956Data data) {
		Map<String, String> values = new LinkedHashMap<String, String>();
		put(values, "cc.name fam", data.clientFamilyName);
		put(values, "cc.name giv", data.clientGivenNames);
		put(values, "cc.dob", data.clientBirthday);
		put(values, "cc.org name", data.clientOrganisation);
		put(values, "cc.resadd str", data.clientAddress);
		put(values, "cc.off ph", data.clientPhone);
		put(values, "cc.mob", data.clientMobile);
		put(values, "cc.diac id", data.clientId);
		// 第16题的RID/TRN与第15题共用ta前缀，只更新这两个字段。
		put(values, "ta.diac request id", data.rid);
		put(values, "ta.diac trans id", data.trn);
		put(values, "mg.name fam", data.agentFamilyName);
		put(values, "mg.name giv", data.agentGivenNames);
		put(values, "mg.org name", data.agentOrganisation);
		put(values, "mg.resadd str", data.agentAddress);
		put(values, "mg.postal str", data.agentCorrespondenceAddress);
		put(values, "mg.off ph", data.agentOfficePhone);
		put(values, "mg.mob", data.agentMobile);
		put(values, "mg.email", data.agentEmail);
		put(values, "mg.marn", data.marn);
		return values;
	}

	private static void put(Map<String, String> values, String field, String value) {
		if (!isBlank(value))
			values.put(field, value.replaceAll("[\\r\\n\\t]+", " ").trim());
	}

	/**
	 * 直接使用原有Widget，恢复需要填写的孤立字段，不能新建同名控件覆盖在旧控件下面。
	 * 不重建无关的复选框/签名字段，保留模板已经填写的内容及外观。
	 */
	private static PDAcroForm prepareForm(PDDocument document, Map<String, String> values) throws IOException {
		PDAcroForm form = document.getDocumentCatalog().getAcroForm(null);
		if (form == null) {
			form = new PDAcroForm(document);
			document.getDocumentCatalog().setAcroForm(form);
		}
		Map<String, PDField> fields = collectFields(form);
		COSArray roots = form.getCOSObject().getCOSArray(COSName.FIELDS);
		if (roots == null) {
			roots = new COSArray();
			form.getCOSObject().setItem(COSName.FIELDS, roots);
		}
		for (PDPage page : document.getPages()) {
			for (PDAnnotation annotation : page.getAnnotations()) {
				if (!(annotation instanceof PDAnnotationWidget))
					continue;
				COSDictionary widget = annotation.getCOSObject();
				String name = fieldName(widget);
				if (!values.containsKey(name))
					continue;
				PDField field = fields.get(name);
				if (field == null) {
					COSDictionary root = widget;
					while (root.getCOSDictionary(COSName.PARENT) != null)
						root = root.getCOSDictionary(COSName.PARENT);
					if (!contains(roots, root)) {
						roots.add(root);
						fields = collectFields(form);
					}
					field = fields.get(name);
				}
				if (field == null || !ownsWidget(field, widget))
					throw new IOException("Form 956字段目录与页面控件冲突: " + name);
				annotation.setPage(page);
			}
		}
		validateWidgets(document, fields, values, false);
		return form;
	}

	private static boolean contains(COSArray array, COSDictionary dictionary) {
		for (int i = 0; i < array.size(); i++)
			if (array.getObject(i) == dictionary)
				return true;
		return false;
	}

	private static String fieldName(COSDictionary widget) throws IOException {
		String name = "";
		Set<COSDictionary> visited = Collections.newSetFromMap(new IdentityHashMap<COSDictionary, Boolean>());
		for (COSDictionary current = widget; current != null; current = current.getCOSDictionary(COSName.PARENT)) {
			if (!visited.add(current))
				throw new IOException("Form 956模板字段存在循环引用");
			String part = current.getString(COSName.T);
			if (!isBlank(part))
				name = part + (name.length() == 0 ? "" : "." + name);
		}
		return name;
	}

	private static Map<String, PDField> collectFields(PDAcroForm form) throws IOException {
		Map<String, PDField> fields = new LinkedHashMap<String, PDField>();
		for (PDField field : form.getFieldTree()) {
			PDField previous = fields.put(field.getFullyQualifiedName(), field);
			if (previous != null && previous.getCOSObject() != field.getCOSObject())
				throw new IOException("Form 956模板存在重复字段: " + field.getFullyQualifiedName());
		}
		return fields;
	}

	private static PDTextField requireTextField(Map<String, PDField> fields, String name) throws IOException {
		PDField field = fields.get(name);
		if (!(field instanceof PDTextField))
			throw new IOException("Form 956模板缺少可填写的文本字段: " + name);
		return (PDTextField) field;
	}

	private static boolean ownsWidget(PDField field, COSDictionary dictionary) {
		for (PDAnnotationWidget widget : field.getWidgets())
			if (widget.getCOSObject() == dictionary)
				return true;
		return false;
	}

	private static void verifyFields(PDDocument document, Map<String, String> values) throws IOException {
		PDAcroForm form = document.getDocumentCatalog().getAcroForm(null);
		if (form == null)
			throw new IOException("Form 956生成后丢失表单字段");
		Map<String, PDField> fields = collectFields(form);
		for (Map.Entry<String, String> entry : values.entrySet()) {
			PDTextField field = requireTextField(fields, entry.getKey());
			if (!entry.getValue().equals(field.getValue()))
				throw new IOException("Form 956字段值保存失败: " + entry.getKey());
		}
		validateWidgets(document, fields, values, true);
	}

	private static void validateWidgets(PDDocument document, Map<String, PDField> fields,
			Map<String, String> values, boolean verifyAppearance) throws IOException {
		Map<String, Integer> found = new LinkedHashMap<String, Integer>();
		for (PDPage page : document.getPages()) {
			for (PDAnnotation annotation : page.getAnnotations()) {
				if (!(annotation instanceof PDAnnotationWidget))
					continue;
				String name = fieldName(annotation.getCOSObject());
				if (!values.containsKey(name))
					continue;
				PDTextField field = requireTextField(fields, name);
				if (!ownsWidget(field, annotation.getCOSObject()) || annotation.getRectangle() == null
						|| annotation.getRectangle().getWidth() <= 0 || annotation.getRectangle().getHeight() <= 0)
					throw new IOException("Form 956字段没有有效页面控件: " + name);
				if (verifyAppearance && (annotation.getNormalAppearanceStream() == null
						|| annotation.getNormalAppearanceStream().getCOSObject().getLength() == 0))
					throw new IOException("Form 956字段显示内容为空: " + name);
				found.put(name, found.containsKey(name) ? found.get(name) + 1 : 1);
			}
		}
		for (String name : values.keySet()) {
			PDTextField field = requireTextField(fields, name);
			if (!found.containsKey(name) || found.get(name) != field.getWidgets().size())
				throw new IOException("Form 956字段未关联到页面: " + name);
		}
	}

	private static PDFont loadFont(PDDocument document, Map<String, String> values) throws IOException {
		try {
			for (String value : values.values())
				PDType1Font.HELVETICA.getStringWidth(value);
			return PDType1Font.HELVETICA;
		} catch (IllegalArgumentException ignored) {
			// 需要中文等字符时嵌入完整字体，便于浏览器重新编辑表单。
		}
		String[] fontPaths = {
				"C:\\Windows\\Fonts\\simhei.ttf",
				"C:\\Windows\\Fonts\\arial.ttf",
				"C:\\Windows\\Fonts\\calibri.ttf",
				"C:\\Windows\\Fonts\\segoeui.ttf" };
		for (String fontPath : fontPaths) {
			Path path = Paths.get(fontPath);
			if (Files.isRegularFile(path)) {
				try (InputStream input = Files.newInputStream(path)) {
					PDFont font = PDType0Font.load(document, input, false);
					for (String value : values.values())
						font.getStringWidth(value);
					return font;
				} catch (IllegalArgumentException ignored) {
					// 当前字体缺少所需字符时尝试下一个字体。
				}
			}
		}
		throw new IOException("Form 956缺少可显示客户信息的字体，请配置支持相应字符的字体");
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
