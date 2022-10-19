package org.zhinanzhen.b.dao.pojo.customer;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PastInformation {

    private String familyName;

    private String givenNames;

    private String dateOfBirth;

    private String usedTypeOfName;
}
