package org.zhinanzhen.tb.service.impl;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhinanzhen.b.dao.MaraDAO;
import org.zhinanzhen.b.dao.OfficialDAO;
import org.zhinanzhen.tb.dao.AdviserDAO;
import org.zhinanzhen.tb.dao.AdminUserDAO;
import org.zhinanzhen.tb.dao.AdminUserRoleDAO;
import org.zhinanzhen.tb.dao.pojo.AdminUserRoleDO;
import org.zhinanzhen.tb.service.ServiceException;

@Service
public class AdminUserRoleService {
    @Resource private AdminUserDAO adminUserDAO;
    @Resource private AdminUserRoleDAO adminUserRoleDAO;
    @Resource private AdviserDAO adviserDAO;
    @Resource private MaraDAO maraDAO;
    @Resource private OfficialDAO officialDAO;

    public List<AdminUserRoleDO> list() {
        return adminUserRoleDAO.list();
    }

    public AdminUserRoleDO get(int adminUserId) throws ServiceException {
        if (adminUserId <= 0 || adminUserDAO.getAdminUserById(adminUserId) == null)
            throw new ServiceException("账号不存在。");
        return adminUserRoleDAO.getByAdminUserId(adminUserId);
    }

    @Transactional(rollbackFor = Exception.class)
    public AdminUserRoleDO save(int adminUserId, int adviserId, int officialId, int maraId)
            throws ServiceException {
        get(adminUserId);
        validatePeople(adviserId, officialId, maraId);
        AdminUserRoleDO role = createRole(adminUserId, adviserId, officialId, maraId);
        adminUserRoleDAO.save(role);
        AdminUserRoleDO saved = adminUserRoleDAO.getByAdminUserId(adminUserId);
        if (saved == null)
            throw new ServiceException("账号角色信息保存失败。");
        return saved;
    }

    @Transactional(rollbackFor = Exception.class)
    public AdminUserRoleDO create(int adminUserId, int adviserId, int officialId, int maraId)
            throws ServiceException {
        if (get(adminUserId) != null)
            throw new ServiceException("该账号的角色信息已存在，请使用修改接口。");
        validatePeople(adviserId, officialId, maraId);
        AdminUserRoleDO role = createRole(adminUserId, adviserId, officialId, maraId);
        if (adminUserRoleDAO.create(role) <= 0)
            throw new ServiceException("账号角色信息新增失败。");
        return adminUserRoleDAO.getByAdminUserId(adminUserId);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean delete(int adminUserId) throws ServiceException {
        get(adminUserId);
        return adminUserRoleDAO.deleteByAdminUserId(adminUserId) > 0;
    }

    private void validatePeople(int adviserId, int officialId, int maraId) throws ServiceException {
        if (adviserId <= 0 || adviserDAO.getAdviserById(adviserId) == null)
            throw new ServiceException("顾问不存在:" + adviserId);
        if (officialId <= 0 || officialDAO.getOfficialById(officialId) == null)
            throw new ServiceException("文案不存在:" + officialId);
        if (maraId <= 0 || maraDAO.getMaraById(maraId) == null)
            throw new ServiceException("MARA不存在:" + maraId);
    }

    private AdminUserRoleDO createRole(int adminUserId, int adviserId, int officialId, int maraId) {
        AdminUserRoleDO role = new AdminUserRoleDO();
        role.setAdminUserId(adminUserId);
        role.setAdviserId(adviserId);
        role.setOfficialId(officialId);
        role.setMaraId(maraId);
        return role;
    }
}
