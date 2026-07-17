package org.zhinanzhen.b.service.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.zhinanzhen.b.dao.pojo.OfficialDO;
import org.zhinanzhen.tb.dao.pojo.AdminUserDO;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
import org.zhinanzhen.tb.dao.pojo.UserDO;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncBootstrapData {
    private AdminUserDO adminUser;
    private AdviserDO adviser;
    private OfficialDO official;
    private List<UserDO> users;
    private List<CloudDiskFile> cloudDiskFiles;
}
