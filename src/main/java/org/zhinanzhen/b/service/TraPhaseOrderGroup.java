package org.zhinanzhen.b.service;

import org.zhinanzhen.b.dao.pojo.ServiceOrderDO;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/** 单项目TRA的四条同级订单共享业务code，收款汇总只能计算一份业务金额。 */
public final class TraPhaseOrderGroup {
    private TraPhaseOrderGroup() { }

    public static boolean isStandalone(ServiceOrderDO order) {
        return groupCode(order.getServiceId(), order.getApplicantParentId(),
                order.getCompletionPhase(), order.getCode()) != null;
    }

    public static List<ServiceOrderDTO> financialOrders(List<ServiceOrderDTO> orders) {
        return distinct(orders, order -> groupCode(order.getServiceId(), order.getApplicantParentId(),
                order.getCompletionPhase(), order.getCode()), ServiceOrderDTO::getCompletionPhase);
    }

    public static List<ServiceOrderDO> financialOrderDOs(List<ServiceOrderDO> orders) {
        return distinct(orders, order -> groupCode(order.getServiceId(), order.getApplicantParentId(),
                order.getCompletionPhase(), order.getCode()), ServiceOrderDO::getCompletionPhase);
    }

    private static String groupCode(int serviceId, int parentId, String phase, String code) {
        return serviceId == 24 && parentId == 0 && code != null && !code.isEmpty()
                && Arrays.asList("PSA", "JRE", "JRWA", "JRFA").contains(phase) ? code : null;
    }

    private static <T> List<T> distinct(List<T> orders, Function<T, String> code, Function<T, String> phase) {
        Map<String, T> result = new LinkedHashMap<>();
        int index = 0;
        for (T order : orders) {
            String group = code.apply(order);
            String key = group == null ? "order:" + index++ : "group:" + group;
            if (!result.containsKey(key) || "PSA".equals(phase.apply(order)))
                result.put(key, order);
        }
        return new ArrayList<>(result.values());
    }
}
