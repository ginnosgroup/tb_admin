package org.zhinanzhen.b.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.DoubleConsumer;

import org.zhinanzhen.b.dao.pojo.RefundDO;
import org.zhinanzhen.b.dao.pojo.VisaDO;
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
        // 推荐顾问记录只承载业绩份额，银行对账 code 只保留在原顾问记录上。
        recommended.setVerifyCode(null);
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

    /** 按当前记录的 70% 或 30% 份额反推同一期另一条记录的金额。 */
    public static double counterpartAmount(double editedShare, double editedRatio) {
        BigDecimal share = BigDecimal.valueOf(editedShare).setScale(2, RoundingMode.HALF_UP);
        BigDecimal ratio = BigDecimal.valueOf(editedRatio);
        BigDecimal total = share.divide(ratio, 2, RoundingMode.HALF_UP);
        return total.subtract(share).doubleValue();
    }

    /**
     * 返回各签证佣金单实际承担的退款额。新退款记录按比例分别保存；旧数据只有一条整额记录时，兼容性地拆分。
     */
    public static Map<Integer, Double> allocateRefundAmounts(List<VisaDO> commissionOrders,
                                                               List<RefundDO> refunds) {
        return allocateRefundAmounts(commissionOrders, refunds, null);
    }

    public static Map<Integer, Double> allocateRefundAmounts(List<VisaDO> commissionOrders,
                                                               List<RefundDO> refunds, String stateFilter) {
        Map<Integer, Double> allocated = new HashMap<>();
        if (commissionOrders == null || refunds == null || refunds.isEmpty())
            return allocated;

        Map<Integer, VisaDO> orderById = new HashMap<>();
        Map<String, List<VisaDO>> recommendationGroups = new HashMap<>();
        Set<Integer> refundedVisaIds = new HashSet<>();
        for (VisaDO order : commissionOrders) {
            if (order == null)
                continue;
            orderById.put(order.getId(), order);
            if (isRecommendationShare(order.getPerformanceRatio())) {
                recommendationGroups.computeIfAbsent(recommendationGroupKey(order), key -> new java.util.ArrayList<>())
                        .add(order);
            }
        }
        for (RefundDO refund : refunds) {
            if (refund != null && refund.getVisaId() != null) {
                refundedVisaIds.add(refund.getVisaId());
            }
        }

        for (RefundDO refund : refunds) {
            if (refund == null || refund.getVisaId() == null || "CLOSE".equalsIgnoreCase(refund.getState())
                    || (stateFilter != null && !stateFilter.equalsIgnoreCase(refund.getState())))
                continue;
            VisaDO source = orderById.get(refund.getVisaId());
            if (source == null) {
                continue;
            }

            VisaDO counterpart = null;
            if (isRecommendationShare(source.getPerformanceRatio())) {
                List<VisaDO> group = recommendationGroups.get(recommendationGroupKey(source));
                if (group != null) {
                    for (VisaDO candidate : group) {
                        if (candidate.getId() != source.getId()
                                && isRatio(candidate.getPerformanceRatio(),
                                        isRatio(source.getPerformanceRatio(), 0.70) ? 0.30 : 0.70)) {
                            counterpart = candidate;
                            break;
                        }
                    }
                }
            }

            BigDecimal amount = BigDecimal.valueOf(refund.getAmount()).setScale(2, RoundingMode.HALF_UP);
            if (counterpart != null && refundedVisaIds.contains(counterpart.getId())) {
                // 新流程已分别保存主顾问与推荐顾问的按比例退款单，直接记到对应佣金单。
                addAmount(allocated, source.getId(), amount);
            } else if (counterpart != null) {
                // 兼容旧数据：历史上只有一张完整金额退款单，需要在计算时拆分。
                BigDecimal recommendedAmount = amount.multiply(RECOMMEND_RATIO).setScale(2, RoundingMode.HALF_UP);
                BigDecimal primaryAmount = amount.subtract(recommendedAmount);
                if (isRatio(source.getPerformanceRatio(), 0.70)) {
                    addAmount(allocated, source.getId(), primaryAmount);
                    addAmount(allocated, counterpart.getId(), recommendedAmount);
                } else {
                    addAmount(allocated, source.getId(), recommendedAmount);
                    addAmount(allocated, counterpart.getId(), primaryAmount);
                }
            } else {
                // 找不到完整的70/30配对时，保持原订单承担整笔退款，避免少扣金额。
                addAmount(allocated, source.getId(), amount);
            }
        }
        return allocated;
    }

    private static void addAmount(Map<Integer, Double> allocated, int visaId, BigDecimal amount) {
        BigDecimal current = BigDecimal.valueOf(allocated.getOrDefault(visaId, 0.00));
        allocated.put(visaId, current.add(amount).setScale(2, RoundingMode.HALF_UP).doubleValue());
    }

    private static boolean isRecommendationShare(double ratio) {
        return isRatio(ratio, 0.70) || isRatio(ratio, 0.30);
    }

    private static boolean isRatio(double actual, double expected) {
        return Math.abs(actual - expected) < 0.000001;
    }

    private static String recommendationGroupKey(VisaDO order) {
        return order.getServiceOrderId() + "|" + order.getCode() + "|" + order.getInstallmentNum();
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
