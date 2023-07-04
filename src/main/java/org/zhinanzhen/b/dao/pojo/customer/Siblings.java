package org.zhinanzhen.b.dao.pojo.customer;

import lombok.Data;

import java.util.List;

@Data
public class Siblings {
    private int isHave; //是否有兄弟姐妹

    private List<SiblingsInformation> siblingsInformationList;

}
