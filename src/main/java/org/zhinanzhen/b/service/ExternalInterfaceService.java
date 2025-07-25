package org.zhinanzhen.b.service;


import org.zhinanzhen.b.dao.pojo.OfficialDO;
import org.zhinanzhen.b.service.pojo.CloudDiskFile;
import org.zhinanzhen.b.service.pojo.UserDTO;
import org.zhinanzhen.tb.dao.pojo.AdminUserDO;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
import org.zhinanzhen.tb.dao.pojo.UserDO;

import java.util.List;

public interface ExternalInterfaceService {

    Integer addCloudDiskFile(Integer applicantId, Integer adviserId, String name,
                             String type, String url, String parentFileId, String domainId,
                             String driveId, String fileId, Integer officialId, Integer userId,
                             String operator, String relativePath, String fileSize, String downloadUrl);

    CloudDiskFile getCloudDiskFileById(Integer id, Integer adviserId, String parentFileId, String fileId, String folderName);

    Integer updateCloudDiskFile(Integer applicantId, Integer adviserId, String name,
                                String type, String url, String parentFileId, String domainId, String driveId,
                                String fileId, Integer officialId, Integer userId, String operator, String relativePath, String fileSize, String downloadUrl);

    List<CloudDiskFile> listCloudDiskFile(String parentFileId, Integer id, String name, Integer applicantId, Integer userId);

    UserDO getUserByName(String name);

    AdviserDO getAdviserById(Integer id);

    OfficialDO getOfficialById(Integer id);

    AdminUserDO getAdminuserByUserName(String username);

    CloudDiskFile getCloudDisk(String relativePath);

}
