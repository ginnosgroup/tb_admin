package org.zhinanzhen.b.service.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class CustomFieldsDTO {
    @JsonProperty("corp_id")
    private String corpId;
    @JsonProperty("created_at")
    private Integer createdAt;
    @JsonProperty("deleted")
    private Boolean deleted;
    @JsonProperty("field_type")
    private String fieldType;
    @JsonProperty("field_value")
    private String fieldValue;
    @JsonProperty("has_staff_used")
    private Boolean hasStaffUsed;
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("rank_num")
    private Integer rankNum;
}



