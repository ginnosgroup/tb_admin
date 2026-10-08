package org.zhinanzhen.b.service;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/** 申请材料图片上传前的 PPI 校验和 AI 清晰度审查。 */
public interface PortalApplicationImageReviewService {
    ReviewResult review(byte[] imageBytes) throws IOException;

    final class ReviewResult {
        private final boolean passed;
        private final String message;
        private final Map<String, Object> details;

        public ReviewResult(boolean passed, String message, Map<String, Object> details) {
            this.passed = passed;
            this.message = message;
            this.details = new LinkedHashMap<String, Object>(details);
            this.details.put("passed", passed);
            this.details.put("message", message);
        }

        public boolean isPassed() { return passed; }
        public String getMessage() { return message; }
        public Map<String, Object> toMap() { return new LinkedHashMap<String, Object>(details); }
    }
}
