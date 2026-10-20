package org.zhinanzhen.tb.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.zhinanzhen.tb.dao.pojo.AdminUserRoleDO;

public interface AdminUserRoleDAO {
    @Select("SELECT admin_user_id AS adminUserId, adviser_id AS adviserId, "
            + "official_id AS officialId, mara_id AS maraId "
            + "FROM tb_admin_user_role WHERE admin_user_id = #{adminUserId}")
    AdminUserRoleDO getByAdminUserId(@Param("adminUserId") int adminUserId);

    @Insert("INSERT INTO tb_admin_user_role (admin_user_id, adviser_id, official_id, mara_id) "
            + "VALUES (#{adminUserId}, #{adviserId}, #{officialId}, #{maraId}) "
            + "ON DUPLICATE KEY UPDATE adviser_id = VALUES(adviser_id), "
            + "official_id = VALUES(official_id), mara_id = VALUES(mara_id)")
    int save(AdminUserRoleDO role);
}
