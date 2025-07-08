package org.zhinanzhen.b.service;

import org.springframework.web.multipart.MultipartFile;
import org.zhinanzhen.b.service.pojo.CloudDiskFile;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface CloudDiskService {

    int addAndUpdate(MultipartFile file, String type, Integer applicantId,Integer userId, String parentFileId, Integer adviserId, Integer id, String folderName, Integer officialId) throws ExecutionException, InterruptedException;


    int deleteById(Integer id, String fileId);

    List<CloudDiskFile> list(Integer id, String parentFileId, String name, Integer applicantId, Integer userId,int pageNum, int pageSize);

    int count(Integer id, String parentFileId, String name, Integer applicantId, Integer userId);

    String getShareUrl(String userId, String parentFileId);

    int getFileStructure(String parentFileStructures);

    List<CloudDiskFile> initializationFolder(Integer userId, Integer applicantId, Integer adviserId, Integer officialId) throws ExecutionException, InterruptedException;

    int update(String fileId, String type, Integer userId, Integer applicantId, Integer adviserId, Integer id, String name, Integer officialId);


}
