package org.zhinanzhen.b.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 低价申请审核凭证图片分析器。
 */
public interface LowPriceApprovalImageAnalyzer {

    String NO_TEXT_MESSAGE = "未识别到文本";
    String EMPTY_IMAGE_MESSAGE = "图片为空";
    String IMAGE_TOO_LARGE_MESSAGE = "图片超过7MB";
    String UNSUPPORTED_IMAGE_MESSAGE = "文件不是支持的PNG、JPG或BMP图片";

    /**
     * 使用腾讯云通用 OCR 提取图片文字，供其他文件分析流程复用。
     */
    String extractText(byte[] imageBytes) throws IOException;

    AnalysisResult analyze(MultipartFile file, String requestSource,
                           Integer requestUserId) throws IOException;

    final class AnalysisResult {
        private final boolean approved;
        private final String reviewer;
        private final String evidence;
        private final String reason;

        private AnalysisResult(boolean approved, String reviewer, String evidence, String reason) {
            this.approved = approved;
            this.reviewer = reviewer;
            this.evidence = evidence;
            this.reason = reason;
        }

        public static AnalysisResult approved(String reviewer, String evidence, String reason) {
            return new AnalysisResult(true, reviewer, evidence, reason);
        }

        public static AnalysisResult rejected(String reason) {
            return new AnalysisResult(false, null, null, reason);
        }

        public boolean isApproved() {
            return approved;
        }

        public String getReviewer() {
            return reviewer;
        }

        public String getEvidence() {
            return evidence;
        }

        public String getReason() {
            return reason;
        }
    }
}
