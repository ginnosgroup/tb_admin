package org.zhinanzhen.b.dao.pojo.customer;

import lombok.Data;

import java.util.List;
@Data
public class TravelToAustralia {
    private int isIncluded;//本申请中涉及的人员目前是否在澳大利亚？

    private List<currentStayDetails> currentStayDetailsList;


}
