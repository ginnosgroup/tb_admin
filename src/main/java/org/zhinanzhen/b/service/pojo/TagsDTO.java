package org.zhinanzhen.b.service.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class TagsDTO {
    @JsonProperty("group")
    private String group;
    @JsonProperty("name")
    private String name;
}