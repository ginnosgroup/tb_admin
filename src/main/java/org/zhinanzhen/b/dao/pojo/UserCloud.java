package org.zhinanzhen.b.dao.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCloud implements Serializable {
    private Integer id;

    private String userName;

    private String email;

    private Integer adviserId;

    private Integer officialId;

    private String driveId;

    private String userId;

    private String phone;
}
