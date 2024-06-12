package org.zhinanzhen.b.service.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class FollowStaffsDTO {
    @JsonProperty("staff_id")
    private String staffId;
    @JsonProperty("staff_name")
    private String staffName;
    @JsonProperty("staff_avatar")
    private String staffAvatar;
    @JsonProperty("state")
    private String state;
    @JsonProperty("state_text")
    private String stateText;
    @JsonProperty("state_type")
    private String stateType;
    @JsonProperty("remark")
    private String remark;
    @JsonProperty("remark_state")
    private String remarkState;
    @JsonProperty("description")
    private String description;
    @JsonProperty("remark_corp_name")
    private String remarkCorpName;
    @JsonProperty("remark_mobiles")
    private String remarkMobiles;
    @JsonProperty("deleted_at")
    private Integer deletedAt;
    @JsonProperty("deleted_by")
    private String deletedBy;
    @JsonProperty("deleted_each_other")
    private Boolean deletedEachOther;
    @JsonProperty("age")
    private String age;
    @JsonProperty("phone_number")
    private String phoneNumber;
    @JsonProperty("qq")
    private String qq;
    @JsonProperty("weibo")
    private String weibo;
    @JsonProperty("email")
    private String email;
    @JsonProperty("address")
    private String address;
    @JsonProperty("birthday")
    private String birthday;
    @JsonProperty("custom_fields")
    private List<CustomFieldsDTO> customFields;
    @JsonProperty("tags")
    private List<TagsDTO> tags;
    @JsonProperty("internal_tags")
    private List<InternalTagsDTO> internalTags;
    @JsonProperty("follow_time")
    private Integer followTime;
}