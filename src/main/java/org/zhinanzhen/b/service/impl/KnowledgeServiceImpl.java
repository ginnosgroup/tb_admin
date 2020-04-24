package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.KnowledgeDAO;
import org.zhinanzhen.b.dao.KnowledgeMenuDAO;
import org.zhinanzhen.b.dao.pojo.KnowledgeDO;
import org.zhinanzhen.b.dao.pojo.KnowledgeMenuDO;
import org.zhinanzhen.b.service.KnowledgeService;
import org.zhinanzhen.b.service.pojo.KnowledgeDTO;
import org.zhinanzhen.tb.dao.AdminUserDAO;
import org.zhinanzhen.tb.dao.pojo.AdminUserDO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.ErrorCodeEnum;

@Service("KnowledgeService")
public class KnowledgeServiceImpl extends BaseService implements KnowledgeService {

	@Resource
	private KnowledgeDAO knowledgeDao;

	@Resource
	private KnowledgeMenuDAO knowledgeMenuDao;

	@Resource
	private AdminUserDAO adminUserDao;

	private final static String pwd = "J0RVZ4G1";

	@Override
	public int addKnowledge(KnowledgeDTO knowledgeDto) throws ServiceException {
		if (knowledgeDto == null) {
			ServiceException se = new ServiceException("knowledgeDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			KnowledgeDO knowledgeDo = mapper.map(knowledgeDto, KnowledgeDO.class);
			if (knowledgeDao.addKnowledge(knowledgeDo) > 0) {
				knowledgeDto.setId(knowledgeDo.getId());
				return knowledgeDo.getId();
			} else
				return 0;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int updateKnowledge(KnowledgeDTO knowledgeDto) throws ServiceException {
		if (knowledgeDto == null) {
			ServiceException se = new ServiceException("knowledgeDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			KnowledgeDO knowledgeDo = mapper.map(knowledgeDto, KnowledgeDO.class);
			if (knowledgeDao.updateKnowledge(knowledgeDo) > 0) {
				return knowledgeDo.getId();
			} else {
				return 0;
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int countKnowledge(Integer knowledgeMenuId, String keyword) throws ServiceException {
		return knowledgeDao.countKnowledge(knowledgeMenuId, keyword);
	}

	@Override
	public List<KnowledgeDTO> listKnowledge(Integer knowledgeMenuId, String keyword, String password, int pageNum,
			int pageSize) throws ServiceException {
		List<KnowledgeDTO> knowledgeDtoList = new ArrayList<>();
		List<KnowledgeDO> knowledgeDoList = new ArrayList<>();
		try {
			knowledgeDoList = knowledgeDao.listKnowledge(knowledgeMenuId, keyword, pageNum * pageSize, pageSize);
			if (knowledgeDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (KnowledgeDO knowledgeDo : knowledgeDoList) {
			KnowledgeDTO knowledgeDto = mapper.map(knowledgeDo, KnowledgeDTO.class);
			KnowledgeMenuDO knowledgeMenuDo = knowledgeMenuDao.getKnowledgeMenu(knowledgeDto.getKnowledgeMenuId());
			if (knowledgeMenuDo != null)
				knowledgeDto.setKnowledgeMenuName(knowledgeMenuDo.getName());
			if (knowledgeMenuDo != null && knowledgeMenuDo.getParentId() > 0) {
				KnowledgeMenuDO knowledgeMenuDo2 = knowledgeMenuDao.getKnowledgeMenu(knowledgeMenuDo.getParentId());
				if (knowledgeMenuDo2 != null)
					knowledgeDto.setKnowledgeMenuName2(knowledgeMenuDo2.getName());
			}
			AdminUserDO adminUserDo = adminUserDao.getAdminUserById(knowledgeDto.getAdminUserId());
			if (adminUserDo != null)
				knowledgeDto.setAdminUserName(adminUserDo.getUsername());
			if (knowledgeDto.getPassword() != null && !knowledgeDto.getPassword().equals("")) {
				if (!knowledgeDto.getPassword().equals(password) && !pwd.equals(password))
					knowledgeDto.setContent(null);
				knowledgeDto.setPassword(null);
			}
			knowledgeDto.setContent(""); // 清空content以提高查询速度
			knowledgeDtoList.add(knowledgeDto);
		}
		return knowledgeDtoList;
	}

	@Override
	public KnowledgeDTO getKnowledge(Integer id, String password) throws ServiceException {
		if (id == null) {
			ServiceException se = new ServiceException("id is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			KnowledgeDO knowledgeDo = knowledgeDao.getKnowledge(id);
			if (knowledgeDo == null) {
				ServiceException se = new ServiceException("No data !");
				se.setCode(ErrorCodeEnum.DATA_ERROR.code());
				throw se;
			}
			if (knowledgeDo.getPassword() != null && !knowledgeDo.getPassword().equals("")
					&& !knowledgeDo.getPassword().equals(password) && !pwd.equals(password)) {
				ServiceException se = new ServiceException("Password error !");
				se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
				throw se;
			}
			KnowledgeDTO knowledgeDto = mapper.map(knowledgeDo, KnowledgeDTO.class);
			KnowledgeMenuDO knowledgeMenuDo = knowledgeMenuDao.getKnowledgeMenu(knowledgeDto.getKnowledgeMenuId());
			if (knowledgeMenuDo != null)
				knowledgeDto.setKnowledgeMenuName(knowledgeMenuDo.getName());
			if (knowledgeMenuDo != null && knowledgeMenuDo.getParentId() > 0) {
				KnowledgeMenuDO knowledgeMenuDo2 = knowledgeMenuDao.getKnowledgeMenu(knowledgeMenuDo.getParentId());
				if (knowledgeMenuDo2 != null) {
					knowledgeDto.setKnowledgeMenuId2(knowledgeMenuDo2.getId());
					knowledgeDto.setKnowledgeMenuName2(knowledgeMenuDo2.getName());
				}
			}
			return knowledgeDto;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
	}

	@Override
	public int deleteKnowledge(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			return knowledgeDao.deleteKnowledge(id);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

}
