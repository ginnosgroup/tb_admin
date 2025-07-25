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
    public Integer addCloudDiskFile(Integer applicantId, Integer adviserId, String name, String type, String url, String parentFileId, String domainId, String driveId, String fileId, Integer officialId, Integer userId, String operator, String relativePath, String fileSize, String downloadUrl) {
        CloudDiskFile cloudDiskFile = new CloudDiskFile();
        cloudDiskFile.setApplicantId(applicantId);
        cloudDiskFile.setAdviserId(adviserId);
        cloudDiskFile.setName(name);
        cloudDiskFile.setType(type);
        cloudDiskFile.setUrl(url);
        cloudDiskFile.setParentFileId(parentFileId);
        cloudDiskFile.setDomainId(domainId);
        cloudDiskFile.setDriveId(driveId);
        cloudDiskFile.setFileId(fileId);
        cloudDiskFile.setOfficialId(officialId);
        cloudDiskFile.setUserId(userId);
        return cloudDiskFileDAO.add(cloudDiskFile);
    }

    @Override
    public CloudDiskFile getCloudDiskFileById(Integer id, Integer adviserId, String parentFileId, String fileId, String folderName) {
        return cloudDiskFileDAO.getById(id, parentFileId, fileId, folderName);
    }

    @Override
    public Integer updateCloudDiskFile(Integer applicantId, Integer adviserId, String name, String type, String url,
                                       String parentFileId, String domainId, String driveId, String fileId, Integer officialId,
                                       Integer userId, String operator, String relativePath, String fileSize, String downloadUrl) {
        CloudDiskFile cloudDiskFile = new CloudDiskFile();
        if (applicantId != null) {
            cloudDiskFile.setApplicantId(applicantId);
        }
        if (adviserId != null) {
            cloudDiskFile.setAdviserId(adviserId);
        }
        if (name != null) {
            cloudDiskFile.setName(name);
        }
        if (type != null) {
            cloudDiskFile.setType(type);
        }
        if (url != null) {
            cloudDiskFile.setUrl(url);
        }
        if (parentFileId != null) {
            cloudDiskFile.setParentFileId(parentFileId);
        }
        if (domainId != null) {
            cloudDiskFile.setDomainId(domainId);
        }
        if (driveId != null) {
            cloudDiskFile.setDriveId(driveId);
        }
        if (fileId != null) {
            cloudDiskFile.setFileId(fileId);
        }
        if (officialId != null) {
            cloudDiskFile.setOfficialId(officialId);
        }
        if (userId != null) {
            cloudDiskFile.setUserId(userId);
        }
        if (operator != null) {
            cloudDiskFile.setOperator(operator);
        }
        if (relativePath != null) {
            cloudDiskFile.setRelativePath(relativePath);
        }
        if (fileSize != null) {
            cloudDiskFile.setFileSize(Long.parseLong(fileSize));
        }
        if (downloadUrl != null) {
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
