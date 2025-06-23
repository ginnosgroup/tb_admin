package org.zhinanzhen.b.service.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartInfo {

    private String internalUploadUrl;

    private String uploadUrl;

    private Integer partNumber;
}
