package org.zhinanzhen.b.service.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceAssessAndEOI {
    private String label;

    private String value;

    private String key;

    private String eoiServicePackageId;
}
