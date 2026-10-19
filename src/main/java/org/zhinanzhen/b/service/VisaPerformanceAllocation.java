package org.zhinanzhen.b.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.DoubleConsumer;

import org.springframework.beans.BeanUtils;
import org.zhinanzhen.b.service.pojo.VisaDTO;

/** 每期按完整金额分配，原始输入保留给下一期的余额计算。 */
public final class VisaPerformanceAllocation {

    private static final BigDecimal RECOMMEND_RATIO = new BigDecimal("0.30");

    private VisaPerformanceAllocation() {
    }

    public static List<VisaDTO> allocate(VisaDTO source, Integer recommendAdviserId) {
        VisaDTO primary = copy(source);
        primary.setPerformanceRatio(1.00);
        // 同一顾问只计一份完整业绩。
        if (recommendAdviserId == null || recommendAdviserId == source.getAdviserId())
            return Collections.singletonList(primary);

        VisaDTO recommended = copy(source);
        recommended.setAdviserId(recommendAdviserId);
        primary.setPerformanceRatio(0.70);
        recommended.setPerformanceRatio(0.30);
        split(source.getReceivable(), primary::setReceivable, recommended::setReceivable);
        split(source.getReceived(), primary::setReceived, recommended::setReceived);
        split(source.getPerAmount(), primary::setPerAmount, recommended::setPerAmount);
        split(source.getAmount(), primary::setAmount, recommended::setAmount);
        split(source.getExpectAmount(), primary::setExpectAmount, recommended::setExpectAmount);
        split(source.getSureExpectAmount(), primary::setSureExpectAmount, recommended::setSureExpectAmount);
        split(source.getDiscount(), primary::setDiscount, recommended::setDiscount);
        split(source.getGst(), primary::setGst, recommended::setGst);
        split(source.getDeductGst(), primary::setDeductGst, recommended::setDeductGst);
        split(source.getBonus(), primary::setBonus, recommended::setBonus);
        return Arrays.asList(primary, recommended);
    }

    private static VisaDTO copy(VisaDTO source) {
        VisaDTO copy = new VisaDTO();
        BeanUtils.copyProperties(source, copy);
        copy.setId(0);
        return copy;
    }

    private static void split(double value, DoubleConsumer primary, DoubleConsumer recommended) {
        BigDecimal total = BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
        BigDecimal recommendation = total.multiply(RECOMMEND_RATIO).setScale(2, RoundingMode.HALF_UP);
        recommended.accept(recommendation.doubleValue());
        primary.accept(total.subtract(recommendation).doubleValue());
    }
}
