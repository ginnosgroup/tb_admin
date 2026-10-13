package org.zhinanzhen.b.service;

import java.io.IOException;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * PDF内容提取组件：优先读取PDF文字层，页面没有文字或页面含图片时把页面渲染成图片交给视觉模型识别。
 *
 * application附件内容提取与护照、CoE、完成信、语言/课程成绩单等AI识别共用本组件，
 * 让扫描件PDF（没有文字层）也能识别出内容，避免各业务流程各写一套PDF解析逻辑。
 * 返回结构：{@code pageCount/formFields/extractionStatus/fullText/visionLimitReached/pages}，
 * 其中{@code pages}的每个元素为{@code pageNumber/method/text/fields/tables}，method取值为pdfText或deepseek。
 */
public interface PdfContentExtractor {

    /** 提取全部页面内容，页面需要时调用视觉模型。 */
    ObjectNode extract(byte[] pdfBytes) throws IOException;

    /**
     * 提取页面内容，并限制调用视觉模型的页数，用于上传接口内的同步AI识别，避免超时和费用失控。
     * 超出页数的页面只保留文字层，结果中visionLimitReached为true。
     */
    ObjectNode extract(byte[] pdfBytes, int maxVisionPages) throws IOException;

    /** 只读取PDF文字层（不调用视觉模型），供已有文字层的PDF走快速通道。 */
    String extractNativeText(byte[] pdfBytes) throws IOException;
}
