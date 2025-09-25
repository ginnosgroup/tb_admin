package org.zhinanzhen.b.dao.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceOrderAndManage {

    private Integer id;

    private Integer serviceOrderId;

    private Integer serviceOrderManageId;
}
