package org.zhinanzhen.tb.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.zhinanzhen.b.dao.pojo.UserCloud;
import org.zhinanzhen.b.service.CloudDiskService;
import org.zhinanzhen.b.service.pojo.CloudDiskFile;

import javax.annotation.Resource;
import java.util.List;

@Component
@EnableScheduling
@Slf4j
public class CloudDiskCheck {
    @Resource
    private CloudDiskService cloudDiskService;

    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 4 18 * ?")
    public void checkCloudDisk() {
        List<UserCloud> userClouds = cloudDiskService.listUserCloud();
        for (UserCloud userCloud : userClouds) {
            log.info("检查同步用户" + userCloud.getEmail());
            cloudDiskService.getFileStructure("root", userCloud.getAdviserId(), userCloud.getOfficialId(), null, null, null, null, null);
        }
    }
}
