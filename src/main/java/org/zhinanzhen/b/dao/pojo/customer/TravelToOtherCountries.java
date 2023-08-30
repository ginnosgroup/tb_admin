package org.zhinanzhen.b.dao.pojo.customer;

import lombok.Data;

import java.util.List;
@Data
public class TravelToOtherCountries {
    private int isTravelToOtherCountries;//此申请中是否有任何人在过去 10 年内去过澳大利亚以外的任何国家？

    private List<previousStayDetails> previousStayDetailsList;

}
