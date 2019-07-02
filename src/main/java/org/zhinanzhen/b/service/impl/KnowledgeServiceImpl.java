package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.KnowledgeDAO;
import org.zhinanzhen.b.dao.pojo.KnowledgeDO;
import org.zhinanzhen.b.service.KnowledgeService;
import org.zhinanzhen.b.service.pojo.KnowledgeDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.thrift.ErrorCodeEnum;

@Service("KnowledgeService")
public class KnowledgeServiceImpl extends BaseService implements KnowledgeService {

	@Resource
	private KnowledgeDAO knowledgeDao;

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
	public List<KnowledgeDTO> listKnowledge(Integer knowledgeMenuId, String keyword) throws ServiceException {
		List<KnowledgeDTO> knowledgeDtoList = new ArrayList<>();
		List<KnowledgeDO> knowledgeDoList = new ArrayList<>();
		try {
			knowledgeDoList = knowledgeDao.listKnowledge(knowledgeMenuId, keyword);
			if (knowledgeDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (KnowledgeDO knowledgeDo : knowledgeDoList) {
			KnowledgeDTO knowledgeDto = mapper.map(knowledgeDo, KnowledgeDTO.class);
			knowledgeDtoList.add(knowledgeDto);
		}
		return knowledgeDtoList;
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
