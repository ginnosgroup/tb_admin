package org.zhinanzhen.b.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.CloudDiskFileDAO;
import org.zhinanzhen.b.dao.OfficialDAO;
import org.zhinanzhen.b.dao.pojo.OfficialDO;
import org.zhinanzhen.b.service.ExternalInterfaceService;
import org.zhinanzhen.b.service.pojo.CloudDiskFile;
import org.zhinanzhen.b.service.pojo.UserDTO;
import org.zhinanzhen.tb.dao.AdminUserDAO;
import org.zhinanzhen.tb.dao.AdviserDAO;
import org.zhinanzhen.tb.dao.UserDAO;
import org.zhinanzhen.tb.dao.pojo.AdminUserDO;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
import org.zhinanzhen.tb.dao.pojo.UserDO;

import java.util.Collections;
import java.util.List;

@Service
public class ExternalInterfaceServiceImpl implements ExternalInterfaceService {

    @Autowired
    private CloudDiskFileDAO cloudDiskFileDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private OfficialDAO officialDAO;

    @Autowired
    private AdviserDAO adviserDAO;

    @Autowired
    private AdminUserDAO adminUserDAO;

    @Override
    public Integer addCloudDiskFile(String applicantId, String adviserId, String name, String type, String url, String parentFileId, String domainId, String driveId, String fileId, String officialId, String userId, String operator, String relativePath, String fileSize, String downloadUrl) {
        CloudDiskFile cloudDiskFile = new CloudDiskFile();
        if (applicantId != null && !"null".equals(applicantId)) {
            cloudDiskFile.setApplicantId(Integer.valueOf(applicantId));
        }
        if (adviserId != null && !"null".equals(adviserId)) {
            cloudDiskFile.setAdviserId(Integer.valueOf(adviserId));
        }
        if (name != null && !"null".equals(name)) {
            cloudDiskFile.setName(name);
        }
        if (type != null && !"null".equals(type)) {
            cloudDiskFile.setType(type);
        }
        if (url != null && !"null".equals(url)) {
            cloudDiskFile.setUrl(url);
        }
        if (parentFileId != null && !"null".equals(parentFileId)) {
            cloudDiskFile.setParentFileId(parentFileId);
        }
        if (domainId != null && !"null".equals(domainId)) {
            cloudDiskFile.setDomainId(domainId);
        }
        if (driveId != null && !"null".equals(driveId)) {
            cloudDiskFile.setDriveId(driveId);
        }
        if (fileId != null && !"null".equals(fileId)) {
            cloudDiskFile.setFileId(fileId);
        }
        if (officialId != null && !"null".equals(officialId)) {
            cloudDiskFile.setOfficialId(Integer.valueOf(officialId));
        }
        if (operator != null && !"null".equals(operator)) {
            cloudDiskFile.setOperator(operator);
        }
        if (relativePath != null && !"null".equals(relativePath)) {
            cloudDiskFile.setRelativePath(relativePath);
        }
        if (fileSize != null && !"null".equals(fileSize)) {
            cloudDiskFile.setFileSize(Long.parseLong(fileSize));
        }
        if (downloadUrl != null && !"null".equals(downloadUrl)) {
            cloudDiskFile.setDownloadUrl(downloadUrl);
        }
        if (userId != null && !"null".equals(userId)) {
            cloudDiskFile.setUserId(Integer.valueOf(userId));
        }
        return cloudDiskFileDAO.add(cloudDiskFile);
    }

    @Override
    public CloudDiskFile getCloudDiskFileById(Integer id, Integer adviserId, String parentFileId, String fileId, String folderName) {
        return cloudDiskFileDAO.getById(id, parentFileId, fileId, folderName);
    }

    @Override
    public Integer updateCloudDiskFile(String id, String isDelete, String applicantId, String adviserId, String name,
                                       String type, String url, String parentFileId, String domainId, String driveId,
                                       String fileId, String officialId, String userId, String operator, String relativePath, String fileSize, String downloadUrl) {
        CloudDiskFile cloudDiskFile = new CloudDiskFile();
        if (id != null && !"null".equals(id)) {
            cloudDiskFile.setId(Integer.valueOf(id));
        }
        if (isDelete != null && !"null".equals(isDelete)) {
            cloudDiskFile.setIsDelete("0".equals(isDelete) ? 0 : 1);
        }
        if (applicantId != null && !"null".equals(applicantId)) {
            cloudDiskFile.setApplicantId(Integer.valueOf(applicantId));
        }
        if (adviserId != null && !"null".equals(adviserId)) {
            cloudDiskFile.setAdviserId(Integer.valueOf(adviserId));
        }
        if (name != null && !"null".equals(name)) {
            cloudDiskFile.setName(name);
        }
        if (type != null && !"null".equals(type)) {
            cloudDiskFile.setType(type);
        }
        if (url != null && !"null".equals(url)) {
            cloudDiskFile.setUrl(url);
        }
        if (parentFileId != null && !"null".equals(parentFileId)) {
            cloudDiskFile.setParentFileId(parentFileId);
        }
        if (domainId != null && !"null".equals(domainId)) {
            cloudDiskFile.setDomainId(domainId);
        }
        if (driveId != null && !"null".equals(driveId)) {
            cloudDiskFile.setDriveId(driveId);
        }
        if (fileId != null && !"null".equals(fileId)) {
            cloudDiskFile.setFileId(fileId);
        }
        if (officialId != null && !"null".equals(officialId)) {
            cloudDiskFile.setOfficialId(Integer.valueOf(officialId));
        }
        if (userId != null && !"null".equals(userId)) {
            cloudDiskFile.setUserId(Integer.valueOf(userId));
        }
        if (operator != null && !"null".equals(operator)) {
            cloudDiskFile.setOperator(operator);
        }
        if (relativePath != null && !"null".equals(relativePath)) {
            cloudDiskFile.setRelativePath(relativePath);
        }
        if (fileSize != null && !"null".equals(fileSize)) {
            cloudDiskFile.setFileSize(Long.parseLong(fileSize));
        }
        if (downloadUrl != null && !"null".equals(downloadUrl)) {
            cloudDiskFile.setDownloadUrl(downloadUrl);
        }
        return cloudDiskFileDAO.update(cloudDiskFile);
    }

    @Override
    public List<CloudDiskFile> listCloudDiskFile(String parentFileId, Integer id, String name, Integer applicantId, Integer userId) {
        return cloudDiskFileDAO.listByParentFileId(id, parentFileId, name, applicantId, userId, 0, 100);
    }

    @Override
    public UserDO getUserByName(String name) {
        return userDAO.getUserByName(name);
    }

    @Override
    public AdviserDO getAdviserById(Integer id) {
        return adviserDAO.getAdviserById(id);
    }

    @Override
    public OfficialDO getOfficialById(Integer id) {
        return officialDAO.getOfficialById(id);
    }

    @Override
    public AdminUserDO getAdminuserByUserName(String username) {
        return adminUserDAO.getAdminUserByUsername(username);
    }

    @Override
    public CloudDiskFile getCloudDisk(String relativePath) {
        return cloudDiskFileDAO.getCloudDisk(relativePath);
    }
}
