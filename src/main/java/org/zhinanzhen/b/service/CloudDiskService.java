package org.zhinanzhen.b.service;

import org.springframework.web.multipart.MultipartFile;
import org.zhinanzhen.b.dao.pojo.UserCloud;
import org.zhinanzhen.b.service.pojo.CloudDiskFile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface CloudDiskService {

    int addAndUpdate(MultipartFile file, String type, Integer applicantId,Integer userId, String parentFileId, Integer adviserId, Integer id, String folderName, Integer officialId, String relativePath) throws ExecutionException, InterruptedException;


    int deleteById(Integer id, String fileId);

    List<CloudDiskFile> list(Integer id, String parentFileId, String name, Integer applicantId, Integer userId,int pageNum, int pageSize);

    int count(Integer id, String parentFileId, String name, Integer applicantId, Integer userId);

    String getShareUrl(String userId, String parentFileId);

    int getFileStructure(String parentFileStructures, Integer adviserId, Integer officialId, Map<String, String> belongFolderMap, Map<String, Integer> addCountMap, String folderName, Integer userId, String synchronizeName);

    List<CloudDiskFile> initializationFolder(Integer userId, Integer applicantId, Integer adviserId, Integer officialId) throws ExecutionException, InterruptedException;

    int update(String fileId, String type, Integer userId, Integer applicantId, Integer adviserId, Integer id, String name, Integer officialId, String relativePath);


    int addAndUpdate(MultipartFile file, Integer userId, String parentFileId, Integer adviserId, Integer officialId, String relativePath);

    String getDownLink(Integer id, String fileId);

    int updateofficialId(Integer officialId1, Integer integer);

    List<UserCloud> listUserCloud(String userName, String email, int pageNum, int pageSize);

    void synchronizeUserCloud();

    int countUserCloud(String userName, String email);

    void deleteUserCloud(Integer id);

}
