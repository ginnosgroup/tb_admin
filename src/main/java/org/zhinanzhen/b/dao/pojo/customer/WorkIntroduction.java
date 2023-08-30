package org.zhinanzhen.b.dao.pojo.customer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

//todo 工作介绍
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkIntroduction {
    private int isWorkIntroduction;

    private List<EmploymentDetails> workIntroductionList;//工作介绍详情




}
