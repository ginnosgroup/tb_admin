package org.zhinanzhen.b.service.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncBootstrapRequest {
    private String username;
    private String driveId;
    private List<Integer> userIds;
    private Integer protocolVersion;
    private Integer afterId;
    private Integer pageSize;
    private Boolean includeContext;
}
