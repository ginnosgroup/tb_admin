package org.zhinanzhen.b.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.AdviserCommissionOrderDO;
import org.zhinanzhen.b.dao.pojo.AdviserServiceOrderDO;
import org.zhinanzhen.b.dao.pojo.AdviserUserDO;
import org.zhinanzhen.b.dao.pojo.AdviserVisaDO;

public interface AdviserDataDAO {

	List<AdviserServiceOrderDO> listServiceOrder(@Param("adviserId") Integer adviserId);

	List<AdviserVisaDO> listVisa(@Param("adviserId") Integer adviserId);

	List<AdviserCommissionOrderDO> listCommissionOrder(@Param("adviserId") Integer adviserId);

	List<AdviserUserDO> listUser(@Param("adviserId") Integer adviserId);

	int userDataMigration(@Param("newAdviserId") Integer newAdviserId, @Param("adviserId") Integer adviserId,
			@Param("userIdList") List<Integer> userIdList);

	int countUserDataMigration(@Param("newAdviserId") Integer newAdviserId, @Param("adviserId") Integer adviserId,
			@Param("userIdList") List<Integer> userIdList);

	int userAdviserDataMigration(@Param("newAdviserId") Integer newAdviserId, @Param("adviserId") Integer adviserId,
			@Param("userIdList") List<Integer> userIdList);

	int countUserAdviserDataMigration(@Param("newAdviserId") Integer newAdviserId,
			@Param("adviserId") Integer adviserId, @Param("userIdList") List<Integer> userIdList);

	int applicantDataMigration(@Param("newAdviserId") Integer newAdviserId, @Param("adviserId") Integer adviserId,
			@Param("userIdList") List<Integer> userIdList);

	int countApplicantDataMigration(@Param("newAdviserId") Integer newAdviserId, @Param("adviserId") Integer adviserId,
			@Param("userIdList") List<Integer> userIdList);

	int serviceOrderDataMigration(@Param("newAdviserId") Integer newAdviserId, @Param("adviserId") Integer adviserId,
			@Param("userIdList") List<Integer> userIdList);

	int countServiceOrderDataMigration(@Param("newAdviserId") Integer newAdviserId,
			@Param("adviserId") Integer adviserId, @Param("userIdList") List<Integer> userIdList);

	int visaDataMigration(@Param("newAdviserId") Integer newAdviserId, @Param("adviserId") Integer adviserId,
			@Param("userIdList") List<Integer> userIdList);

	int countVisaDataMigration(@Param("newAdviserId") Integer newAdviserId, @Param("adviserId") Integer adviserId,
			@Param("userIdList") List<Integer> userIdList);

	int commissionOrderDataMigration(@Param("newAdviserId") Integer newAdviserId, @Param("adviserId") Integer adviserId,
			@Param("userIdList") List<Integer> userIdList);

	int countCommissionOrderDataMigration(@Param("newAdviserId") Integer newAdviserId,
			@Param("adviserId") Integer adviserId, @Param("userIdList") List<Integer> userIdList);

}
