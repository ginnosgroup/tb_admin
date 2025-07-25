package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.service.pojo.CloudDiskFile;

import java.util.List;

public interface CloudDiskFileDAO {

    int add(CloudDiskFile cloudDiskFile);

    CloudDiskFile getById(@Param("id") Integer id, @Param("parentFileId") String parentFileId, @Param("fileId") String fileId, @Param("folderName") String folderName);

    int update(CloudDiskFile cloudDiskFile);

    List<CloudDiskFile> listByParentFileId(@Param("id") Integer id, @Param("parentFileId") String parentFileId,
                                           @Param("name") String name, @Param("applicantId") Integer applicantId, @Param("userId") Integer userId,
                                           @Param("offset") int offset, @Param("rows") int rows);

    int count(@Param("id") Integer id, @Param("parentFileId") String parentFileId, @Param("name") String name,@Param("applicantId") Integer applicantId,@Param("userId") Integer userId);

    List<CloudDiskFile> listByRelativePath(@Param("oldRelativePath") String oldRelativePath);

    CloudDiskFile getCloudDisk(String relativePath);

}
