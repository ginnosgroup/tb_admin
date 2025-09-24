package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.zhinanzhen.b.dao.pojo.UserCloud;
import org.zhinanzhen.b.service.pojo.CloudDiskFile;

import java.util.List;

public interface CloudDiskFileDAO {

    int add(CloudDiskFile cloudDiskFile);

    CloudDiskFile getById(@Param("id") Integer id, @Param("parentFileId") String parentFileId, @Param("fileId") String fileId, @Param("folderName") String folderName, @Param("userId") Integer userId);

    int update(CloudDiskFile cloudDiskFile);

    List<CloudDiskFile> listByParentFileId(@Param("id") Integer id, @Param("parentFileId") String parentFileId,
                                           @Param("name") String name, @Param("applicantId") Integer applicantId, @Param("userId") Integer userId,
                                           @Param("offset") int offset, @Param("rows") int rows);

    int count(@Param("id") Integer id, @Param("parentFileId") String parentFileId, @Param("name") String name,@Param("applicantId") Integer applicantId,@Param("userId") Integer userId);

    List<CloudDiskFile> listByRelativePath(@Param("oldRelativePath") String oldRelativePath);

    CloudDiskFile getCloudDisk(String relativePath);

    Long listByOfficialId(@Param("officialId")int officialId, @Param("userId") int userId);

    Long listByAdviserId(@Param("adviserId")int adviserId, @Param("userId") int userId);

    int updateofficialId(@Param("oldOfficialId") Integer oldOfficialId, @Param("newOfficialId") Integer newOfficialId);

    void addUserCloud(UserCloud userCloud);

    UserCloud getUserCloud(@Param("adviserId") Integer adviserId, @Param("officialId") Integer officialId, @Param("id") Integer id, @Param("email") String email, @Param("phone") String phone);

    List<UserCloud> listUserCloud(@Param("userName") String userName, @Param("email") String email, @Param("offset") int offset, @Param("rows") int rows);

    List<UserCloud> listUserCloudBycondition(String driveId);

    int countUserCloud(@Param("userName") String userName, @Param("email") String email);

    void deleteUserCloud(Integer id);

}
