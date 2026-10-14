package org.zhinanzhen.b.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/** Converts the filled Word document, including its tables, images and signatures, to PDF. */
public final class WordPdfConverter {

	private WordPdfConverter() {
	}

	public static void convert(Path wordPath, Path pdfPath) throws Exception {
		if (wordPath == null || !Files.isRegularFile(wordPath))
			throw new IOException("Letter Word源文件不存在: " + wordPath);
		WordprocessingMLPackage document = WordprocessingMLPackage.load(wordPath.toFile());
		try (OutputStream output = Files.newOutputStream(pdfPath, StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
			Docx4J.toPDF(document, output);
		}
		if (Files.size(pdfPath) == 0)
			throw new IOException("Letter Word转PDF失败，未生成有效文件");
	}
}
