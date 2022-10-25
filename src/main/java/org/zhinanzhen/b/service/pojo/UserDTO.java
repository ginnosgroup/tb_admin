package org.zhinanzhen.b.service.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class UserDTO {

    private int id;

    private String name;

    private Date visa_expiration_date;
    
    private String visaExpirationDate;

    private Integer adviserId;

}
