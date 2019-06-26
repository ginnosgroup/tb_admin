package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhinanzhen.b.dao.KnowledgeDAO;
import org.zhinanzhen.b.dao.KnowledgeMenuDAO;
import org.zhinanzhen.b.dao.pojo.KnowledgeMenuDO;
import org.zhinanzhen.b.service.KnowledgeMenuService;
import org.zhinanzhen.b.service.pojo.KnowledgeMenuDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.thrift.ErrorCodeEnum;

@Service("KnowledgeMenuService")
public class KnowledgeMenuServiceImpl extends BaseService implements KnowledgeMenuService {

	@Resource
	private KnowledgeMenuDAO knowledgeMenuDao;

	@Resource
	private KnowledgeDAO knowledgeDao;

	@Override
	public int addKnowledgeMenu(KnowledgeMenuDTO knowledgeMenuDto) throws ServiceException {
		if (knowledgeMenuDto == null) {
			ServiceException se = new ServiceException("knowledgeMenuDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			KnowledgeMenuDO knowledgeMenuDo = mapper.map(knowledgeMenuDto, KnowledgeMenuDO.class);
			if (knowledgeMenuDao.addKnowledgeMenu(knowledgeMenuDo) > 0) {
				knowledgeMenuDto.setId(knowledgeMenuDo.getId());
				return knowledgeMenuDo.getId();
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
	public int updateKnowledgeMenu(KnowledgeMenuDTO knowledgeMenuDto) throws ServiceException {
		if (knowledgeMenuDto == null) {
			ServiceException se = new ServiceException("knowledgeMenuDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			KnowledgeMenuDO knowledgeMenuDo = mapper.map(knowledgeMenuDto, KnowledgeMenuDO.class);
			if (knowledgeMenuDao.updateSourceRegion(knowledgeMenuDo) > 0) {
				return knowledgeMenuDo.getId();
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
	public List<KnowledgeMenuDTO> listKnowledgeMenu(Integer parentId) throws ServiceException {
		List<KnowledgeMenuDTO> knowledgeMenuDtoList = new ArrayList<KnowledgeMenuDTO>();
		List<KnowledgeMenuDO> knowledgeMenuDoList = new ArrayList<KnowledgeMenuDO>();
		try {
			knowledgeMenuDoList = knowledgeMenuDao.listKnowledgeMenu(parentId);
			if (knowledgeMenuDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (KnowledgeMenuDO knowledgeMenuDo : knowledgeMenuDoList) {
			KnowledgeMenuDTO knowledgeMenuDto = mapper.map(knowledgeMenuDo, KnowledgeMenuDTO.class);
			knowledgeMenuDtoList.add(knowledgeMenuDto);
		}
		return knowledgeMenuDtoList;
	}

	@Override
	@Transactional
	public int deleteKnowledgeMenu(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			knowledgeDao.listKnowledge(id).forEach(knowledge -> knowledgeDao.deleteKnowledge(knowledge.getId()));
			return knowledgeMenuDao.deleteKnowledgeMenu(id);
		} catch (Exception e) {
			rollback();
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

}
