package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.FileMaraAnnotationDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface FileMaraAnnotationService {

    int add(FileMaraAnnotationDTO dto) throws ServiceException;

    int update(FileMaraAnnotationDTO dto) throws ServiceException;

    FileMaraAnnotationDTO getById(int id) throws ServiceException;

    List<FileMaraAnnotationDTO> list(Integer serviceOrderId, Integer userId, Integer officialId) throws ServiceException;

    int deleteById(int id) throws ServiceException;

    int updateIsCheckByServiceOrderId(int serviceOrderId) throws ServiceException;

}