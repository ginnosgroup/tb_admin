package org.zhinanzhen.b.service;

import org.springframework.web.multipart.MultipartFile;
import org.zhinanzhen.b.service.pojo.CloudDiskFile;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface CloudDiskService {

    int addAndUpdate(MultipartFile file, String type, int applicantId, String parentFileId, Integer adviserId, Integer id) throws ExecutionException, InterruptedException;


    int deleteById(Integer id, String fileId);

    List<CloudDiskFile> list(Integer id, String parentFileId, String name, int pageNum, int pageSize);

    int count(Integer id, String parentFileId, String name);

    String getShareUrl(String userId, String parentFileId);

    int getFileStructure(String parentFileStructures);

}
