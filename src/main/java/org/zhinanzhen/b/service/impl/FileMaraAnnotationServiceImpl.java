package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.FileMaraAnnotationDAO;
import org.zhinanzhen.b.dao.pojo.FileMaraAnnotationDO;
import org.zhinanzhen.b.service.FileMaraAnnotationService;
import org.zhinanzhen.b.service.pojo.FileMaraAnnotationDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.ErrorCodeEnum;

@Service("FileMaraAnnotationService")
public class FileMaraAnnotationServiceImpl extends BaseService implements FileMaraAnnotationService {

    @Resource
    private FileMaraAnnotationDAO fileMaraAnnotationDao;

    @Override
    public int add(FileMaraAnnotationDTO dto) throws ServiceException {
        if (dto == null) {
            ServiceException se = new ServiceException("dto is null !");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        try {
            FileMaraAnnotationDO fileMaraAnnotationDo = mapper.map(dto, FileMaraAnnotationDO.class);
            if (fileMaraAnnotationDao.add(fileMaraAnnotationDo) > 0) {
                dto.setId(fileMaraAnnotationDo.getId());
                return fileMaraAnnotationDo.getId();
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
    public int update(FileMaraAnnotationDTO dto) throws ServiceException {
        if (dto == null) {
            ServiceException se = new ServiceException("dto is null !");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        try {
            FileMaraAnnotationDO fileMaraAnnotationDo = mapper.map(dto, FileMaraAnnotationDO.class);
            if (fileMaraAnnotationDao.update(fileMaraAnnotationDo) > 0) {
                return fileMaraAnnotationDo.getId();
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
    public FileMaraAnnotationDTO getById(int id) throws ServiceException {
        try {
            FileMaraAnnotationDO fileMaraAnnotationDo = fileMaraAnnotationDao.getById(id);
            if (fileMaraAnnotationDo != null) {
                return mapper.map(fileMaraAnnotationDo, FileMaraAnnotationDTO.class);
            }
            return null;
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }

    @Override
    public List<FileMaraAnnotationDTO> list(Integer serviceOrderId, Integer userId, Integer officialId) throws ServiceException {
        try {
            List<FileMaraAnnotationDTO> dtoList = new ArrayList<>();
            List<FileMaraAnnotationDO> doList = fileMaraAnnotationDao.list(serviceOrderId, userId, officialId);
            if (doList != null) {
                for (FileMaraAnnotationDO fileMaraAnnotationDo : doList) {
                    dtoList.add(mapper.map(fileMaraAnnotationDo, FileMaraAnnotationDTO.class));
                }
            }
            return dtoList;
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }

    @Override
    public int deleteById(int id) throws ServiceException {
        try {
            return fileMaraAnnotationDao.deleteById(id);
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }

}