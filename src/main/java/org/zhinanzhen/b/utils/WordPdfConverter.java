package org.zhinanzhen.b.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.wml.Ftr;
import org.docx4j.wml.Hdr;

/**
 * Converts the filled Word document, including its tables, images and signatures, to PDF.
 * 自带 Calibri/Arial 等字体并做名称映射，避免 docx4j 因字体缺失导出空白 PDF。
 */
public final class WordPdfConverter {

	private static final String[] BUNDLED_FONTS = { "calibri.ttf", "arial.ttf", "segoeui.ttf", "simhei.ttf" };

	/** Letter 模板里常见但服务器上未必安装的 Word 字体，统一映射到内置字体。 */
	private static final String[][] FONT_ALIASES = {
			{ "Calibri", "calibri.ttf" }, { "Calibri Light", "calibri.ttf" },
			{ "Cambria", "calibri.ttf" }, { "Cambria Math", "calibri.ttf" },
			{ "Arial", "arial.ttf" }, { "Arial Narrow", "arial.ttf" },
			{ "Times New Roman", "arial.ttf" }, { "Georgia", "arial.ttf" },
			{ "Segoe UI", "segoeui.ttf" }, { "Tahoma", "segoeui.ttf" }, { "Verdana", "segoeui.ttf" },
			{ "SimHei", "simhei.ttf" }, { "SimSun", "simhei.ttf" }, { "Microsoft YaHei", "simhei.ttf" },
			{ "DengXian", "simhei.ttf" }, { "KaiTi", "simhei.ttf" },
	};

	private static volatile boolean fontsReady;

	/**
	 * 页眉/页脚里的整页水印形状（v:shape 带 width/height 的绝对定位图片）。
	 * docx4j 的 FO 导出会把页眉内容整体当作页眉高度，遇到 800pt 高的整页水印时会生成
	 * region-before/region-body margin-top=802pt，A4 只有 842pt，正文区域高度变成负数，
	 * 结果 PDF 只剩背景图、正文文字全部被裁掉（文字其实在，只是不可见）。
	 */
	private static final Pattern WATERMARK_SHAPE = Pattern.compile(
			"<w:pict\\b[^>]*>(?:(?!</w:pict>).)*?(?:Watermark|gain=|blacklevel=)(?:(?!</w:pict>).)*?</w:pict>",
			Pattern.DOTALL);

	private WordPdfConverter() {
	}

	public static void convert(Path wordPath, Path pdfPath) throws Exception {
		if (wordPath == null || !Files.isRegularFile(wordPath))
			throw new IOException("Letter Word源文件不存在: " + wordPath);
		ensureFontsRegistered();
		WordprocessingMLPackage document = WordprocessingMLPackage.load(wordPath.toFile());
		removeHeaderFooterWatermarks(document);
		IdentityPlusMapper fontMapper = new IdentityPlusMapper();
		mapAliases(fontMapper);
		document.setFontMapper(fontMapper, false);
		try (OutputStream output = Files.newOutputStream(pdfPath, StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
			Docx4J.toPDF(document, output);
		}
		if (Files.size(pdfPath) == 0)
			throw new IOException("Letter Word转PDF失败，未生成有效文件");
	}

	/**
	 * 导出PDF前去掉页眉/页脚中的整页图片水印，避免docx4j把页眉高度算成整页导致正文被裁掉。
	 * 只删除识别为水印的形状，页眉/页脚里的正常信头图片（DrawingML）不受影响。
	 */
	private static void removeHeaderFooterWatermarks(WordprocessingMLPackage document) {
		if (document == null || document.getParts() == null)
			return;
		int removed = 0;
		for (Part part : document.getParts().getParts().values()) {
			try {
				if (part instanceof HeaderPart) {
					HeaderPart header = (HeaderPart) part;
					String cleaned = stripWatermark(header.getJaxbElement());
					if (cleaned != null) {
						header.setJaxbElement((Hdr) XmlUtils.unmarshalString(cleaned));
						removed++;
					}
				} else if (part instanceof FooterPart) {
					FooterPart footer = (FooterPart) part;
					String cleaned = stripWatermark(footer.getJaxbElement());
					if (cleaned != null) {
						footer.setJaxbElement((Ftr) XmlUtils.unmarshalString(cleaned));
						removed++;
					}
				}
			} catch (Exception e) {
				// 单个页眉/页脚处理失败时不阻断转换：保留原状，正文可能仍被裁掉，由调用方从PDF内容判断。
				System.err.println("移除页眉/页脚水印失败: " + e.getMessage());
			}
		}
		if (removed > 0)
			System.out.println("Letter转PDF已移除页眉/页脚水印: " + removed + " 处");
	}

	/** 返回去掉水印形状后的页眉/页脚XML；没有水印时返回null。 */
	private static String stripWatermark(Object content) {
		if (content == null)
			return null;
		String xml = XmlUtils.marshaltoString(content);
		Matcher matcher = WATERMARK_SHAPE.matcher(xml);
		if (!matcher.find())
			return null;
		matcher.reset();
		return matcher.replaceAll("");
	}

	private static synchronized void ensureFontsRegistered() throws Exception {
		if (fontsReady)
			return;
		Path fontDir = Files.createTempDirectory("docx4j-fonts-");
		fontDir.toFile().deleteOnExit();
		for (String name : BUNDLED_FONTS) {
			Path target = fontDir.resolve(name);
			if (!Files.isRegularFile(target)) {
				try (InputStream input = WordPdfConverter.class.getResourceAsStream("/fonts/" + name)) {
					if (input == null)
						continue;
					Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
				}
				target.toFile().deleteOnExit();
			}
			PhysicalFonts.addPhysicalFonts(baseName(name), target.toUri());
		}
		fontsReady = true;
	}

	private static void mapAliases(IdentityPlusMapper fontMapper) {
		for (String[] pair : FONT_ALIASES) {
			String wordFont = pair[0];
			PhysicalFont physical = PhysicalFonts.get(pair[1].replace(".ttf", ""));
			if (physical == null)
				physical = PhysicalFonts.get(baseName(pair[1]));
			if (physical == null)
				continue;
			try {
				fontMapper.put(wordFont, physical);
			} catch (Exception ignored) {
				// 单个映射失败不影响整体转换。
			}
		}
	}

	private static String baseName(String fileName) {
		String name = fileName;
		int dot = name.lastIndexOf('.');
		if (dot > 0)
			name = name.substring(0, dot);
		return name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1);
	}
}
