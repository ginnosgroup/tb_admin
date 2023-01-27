package org.zhinanzhen.b.dao.pojo.customer;

import lombok.Data;

import java.util.List;

@Data
public class Addresses {

    private int isAll;

    private List<CurrentAddress> currentAddressList;

    private int isSamePostalAddress;

    private int isSameResidential;

}

