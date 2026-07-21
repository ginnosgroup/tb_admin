package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.UserCloud;
import org.zhinanzhen.b.service.pojo.CloudDiskFile;

import java.util.Date;
import java.util.List;

public interface CloudDiskFileDAO {

    int add(CloudDiskFile cloudDiskFile);

    int batchUpsert(@Param("list") List<CloudDiskFile> list);

    CloudDiskFile getById(@Param("id") Integer id, @Param("parentFileId") String parentFileId, @Param("fileId") String fileId, @Param("folderName") String folderName, @Param("userId") Integer userId);

    CloudDiskFile getRootFolderByUserId(@Param("userId") Integer userId);

    int update(CloudDiskFile cloudDiskFile);

    int updateShareLink(@Param("id") Integer id, @Param("shareLink") String shareLink,
                        @Param("shareUrlExpiration") Date shareUrlExpiration);

    List<CloudDiskFile> listByParentFileId(@Param("id") Integer id, @Param("parentFileId") String parentFileId,
                                           @Param("name") String name, @Param("applicantId") Integer applicantId, @Param("userId") Integer userId,
                                           @Param("offset") int offset, @Param("rows") int rows);

    int count(@Param("id") Integer id, @Param("parentFileId") String parentFileId, @Param("name") String name,@Param("applicantId") Integer applicantId,@Param("userId") Integer userId);

    List<CloudDiskFile> listByRelativePath(@Param("oldRelativePath") String oldRelativePath);

    CloudDiskFile getCloudDisk(String relativePath);

    Long listByOfficialId(@Param("officialId") Integer officialId, @Param("userId") int userId);

    Long listByAdviserId(@Param("adviserId") Integer adviserId, @Param("userId") int userId);

    int updateofficialId(@Param("oldOfficialId") Integer oldOfficialId, @Param("newOfficialId") Integer newOfficialId);

    void addUserCloud(UserCloud userCloud);

    UserCloud getUserCloud(@Param("adviserId") Integer adviserId, @Param("officialId") Integer officialId, @Param("id") Integer id, @Param("email") String email, @Param("phone") String phone);

    List<UserCloud> listUserCloud(@Param("userName") String userName, @Param("email") String email, @Param("offset") int offset, @Param("rows") int rows);

    List<UserCloud> listUserCloudBycondition(String driveId);

    int countUserCloud(@Param("userName") String userName, @Param("email") String email);

    void deleteUserCloud(Integer id);

    List<CloudDiskFile> listByUserIds(@Param("userIds") List<Integer> userIds);
    List<CloudDiskFile> listByDriveId(@Param("driveId") String driveId);

    List<CloudDiskFile> listForSync(@Param("driveId") String driveId, @Param("userIds") List<Integer> userIds);

    List<CloudDiskFile> listByFileIds(@Param("fileIds") List<String> fileIds);

    /** 迁移专用：强制更新 fileId, driveId, domainId, parentFileId, fileSize */
    int updateForMigration(CloudDiskFile cloudDiskFile);

    /** 批量更新：按 id 列表批量更新 driveId, domainId, parentFileId */
    int batchUpdateForMigration(@Param("list") List<CloudDiskFile> list);
}
