package org.zhinanzhen.b.service.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceAssessAndEOI {
    private String assessName;

    private String assessId;

    private String eoiServicePackgeId;
}
