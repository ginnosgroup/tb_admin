package org.zhinanzhen.b.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

/** 职评文案佣金的四个结算阶段。 */
public enum VisaCompletionPhase {
    PSA("0.10"), JRE("0.30"), JRWA("0.40"), JRFA("0.20");

    private final BigDecimal proportion;

    VisaCompletionPhase(String proportion) {
        this.proportion = new BigDecimal(proportion);
    }

    public BigDecimal allocate(BigDecimal total) {
        return total.multiply(proportion).setScale(2, RoundingMode.HALF_UP);
    }

    public double allocate(double total) {
        return allocate(BigDecimal.valueOf(total)).doubleValue();
    }

    public static VisaCompletionPhase fromValue(String value) {
        if (value == null || value.trim().isEmpty())
            return null;
        try {
            return valueOf(value.trim().toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("completionPhase只支持PSA、JRE、JRWA、JRFA。");
        }
    }
}
