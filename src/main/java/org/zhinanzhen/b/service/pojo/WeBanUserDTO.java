package org.zhinanzhen.b.service.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class WeBanUserDTO {
    @JsonProperty("id")
    private String id;
    @JsonProperty("type")
    private Integer type;
    @JsonProperty("unionid")
    private String unionid;
    @JsonProperty("created_at")
    private Integer createdAt;
    @JsonProperty("name")
    private String name;
    @JsonProperty("avatar")
    private String avatar;
    @JsonProperty("corp_full_name")
    private String corpFullName;
    @JsonProperty("corp_name")
    private String corpName;
    @JsonProperty("gender")
    private Integer gender;
    @JsonProperty("position")
    private String position;
    @JsonProperty("external_profile")
    private Object externalProfile;
    @JsonProperty("follow_staffs")
    private List<FollowStaffsDTO> followStaffs;
}