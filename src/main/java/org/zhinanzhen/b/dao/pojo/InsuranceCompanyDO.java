package org.zhinanzhen.b.dao.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InsuranceCompanyDO implements Serializable {

    private int id;

    private Date gmtCreate;

    private Date gmtModify;

    private String name;

    private String marks;

    private boolean isRecommend; // 是否为推荐保险
}
