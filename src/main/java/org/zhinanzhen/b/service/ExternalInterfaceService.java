package org.zhinanzhen.b.service;


import org.zhinanzhen.b.dao.pojo.OfficialDO;
import org.zhinanzhen.b.service.pojo.CloudDiskFile;
import org.zhinanzhen.b.service.pojo.SyncBootstrapData;
import org.zhinanzhen.b.service.pojo.UserDTO;
import org.zhinanzhen.tb.dao.pojo.AdminUserDO;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
import org.zhinanzhen.tb.dao.pojo.UserDO;

import java.util.List;

public interface ExternalInterfaceService {

    Integer addCloudDiskFile(String applicantId, String adviserId, String name,
                             String type, String url, String parentFileId, String domainId,
                             String driveId, String fileId, String officialId, String userId,
                             String operator, String relativePath, String fileSize, String downloadUrl, String hashCode);

    Integer batchUpsertCloudDiskFiles(List<CloudDiskFile> cloudDiskFiles);

    SyncBootstrapData getSyncBootstrap(String username, String driveId, List<Integer> userIds);

    CloudDiskFile getCloudDiskFileById(Integer id, Integer adviserId, String parentFileId, String fileId, String folderName, Integer userId);

    Integer updateCloudDiskFile(String id, String isDelete, String applicantId, String adviserId, String name,
                                String type, String url, String parentFileId, String domainId, String driveId,
                                String fileId, String officialId, String userId, String operator, String relativePath, String fileSize, String downloadUrl, String hashCode, String oldRelativePath, String oldPart);

    List<CloudDiskFile> listCloudDiskFile(String parentFileId, Integer id, String name, Integer applicantId, Integer userId);

    UserDO getUserByName(String name, String id);

    AdviserDO getAdviserById(Integer id);

    OfficialDO getOfficialById(Integer id);

    AdminUserDO getAdminuserByUserName(String username);

    CloudDiskFile getCloudDisk(String relativePath);

    List<CloudDiskFile> listByRelativePath(String relativePath);

}
