package org.zhinanzhen.b.dao.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceOrderInsuranceDO implements Serializable {
    private Integer id;

    private Integer serviceOrderId;

    private Integer insuranceCompanyId;
}
