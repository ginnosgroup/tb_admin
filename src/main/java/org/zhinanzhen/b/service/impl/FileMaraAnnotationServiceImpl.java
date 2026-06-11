package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.CloudDiskFileDAO;
import org.zhinanzhen.b.dao.FileMaraAnnotationDAO;
import org.zhinanzhen.b.dao.OrderMaraAnnotationDAO;
import org.zhinanzhen.b.dao.ServiceOrderDAO;
import org.zhinanzhen.b.dao.pojo.FileMaraAnnotationDO;
import org.zhinanzhen.b.dao.pojo.OrderMaraAnnotationDO;
import org.zhinanzhen.b.dao.pojo.ServiceOrderDO;
import org.zhinanzhen.b.service.FileMaraAnnotationService;
import org.zhinanzhen.b.service.pojo.CloudDiskFile;
import org.zhinanzhen.b.service.pojo.FileMaraAnnotationDTO;
import org.zhinanzhen.tb.dao.UserDAO;
import org.zhinanzhen.tb.dao.pojo.UserDO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.ErrorCodeEnum;

@Service("FileMaraAnnotationService")
public class FileMaraAnnotationServiceImpl extends BaseService implements FileMaraAnnotationService {

    @Resource
    private FileMaraAnnotationDAO fileMaraAnnotationDao;

    @Resource
    private OrderMaraAnnotationDAO orderMaraAnnotationDao;

    @Resource
    private UserDAO userDao;

    @Resource
    private CloudDiskFileDAO cloudDiskFileDao;

    @Resource
    private ServiceOrderDAO serviceOrderDao;

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
            List<FileMaraAnnotationDO> doList = fileMaraAnnotationDao.list(serviceOrderId, userId, officialId);
            List<FileMaraAnnotationDTO> result = assembleRelatedData(doList);
            // b_file_mara_annotation 没有记录，但 b_order_mara_annotation 有 → 新建空 DTO 带 orderMaraAnnotation 返回
            if (result.isEmpty() && serviceOrderId != null && serviceOrderId > 0) {
                OrderMaraAnnotationDO om = orderMaraAnnotationDao.getByServiceOrderId(serviceOrderId);
                if (om != null) {
                    FileMaraAnnotationDTO dto = new FileMaraAnnotationDTO();
                    dto.setServiceOrderId(serviceOrderId);
                    dto.setOrderMaraAnnotation(om);
                    result.add(dto);
                }
            }
            return result;
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

    @Override
    public List<FileMaraAnnotationDTO> listByCloudDiskFileIds(List<String> cloudDiskFileIds) throws ServiceException {
        try {
            List<FileMaraAnnotationDO> doList = fileMaraAnnotationDao.listByCloudDiskFileIds(cloudDiskFileIds);
            return assembleRelatedData(doList);
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }

    @Override
    public List<FileMaraAnnotationDTO> listSimple(Integer serviceOrderId, Integer userId, Integer officialId) throws ServiceException {
        try {
            List<FileMaraAnnotationDTO> dtoList = new ArrayList<>();
            List<FileMaraAnnotationDO> doList = fileMaraAnnotationDao.list(serviceOrderId, userId, officialId);
            if (doList == null) {
                return dtoList;
            }
            // 收集 cloudDiskFileId
            Set<String> fIds = new LinkedHashSet<>();
            for (FileMaraAnnotationDO d : doList) {
                if (d.getCloudDiskFileId() != null && d.getCloudDiskFileId().length() > 0) fIds.add(d.getCloudDiskFileId());
            }
            Map<String, CloudDiskFile> cdMap = new HashMap<>();
            if (!fIds.isEmpty()) {
                for (CloudDiskFile cf : cloudDiskFileDao.listByFileIds(new ArrayList<>(fIds)))
                    cdMap.put(cf.getFileId(), cf);
            }
            for (FileMaraAnnotationDO d : doList) {
                FileMaraAnnotationDTO dto = mapper.map(d, FileMaraAnnotationDTO.class);
                if (dto.getCloudDiskFileId() != null && dto.getCloudDiskFileId().length() > 0) {
                    dto.setCloudDiskFile(cdMap.get(dto.getCloudDiskFileId()));
                }
                dtoList.add(dto);
            }
            return dtoList;
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }

    private List<FileMaraAnnotationDTO> assembleRelatedData(List<FileMaraAnnotationDO> doList) {
        List<FileMaraAnnotationDTO> dtoList = new ArrayList<>();
        if (doList == null || doList.isEmpty()) {
            return dtoList;
        }
        // 用 Set 去重
        Set<Integer> userIds = new LinkedHashSet<>();
        Set<Integer> soIds = new LinkedHashSet<>();
        Set<String> fIds = new LinkedHashSet<>();
        for (FileMaraAnnotationDO d : doList) {
            if (d.getUserId() > 0) userIds.add(d.getUserId());
            if (d.getServiceOrderId() > 0) soIds.add(d.getServiceOrderId());
            if (d.getCloudDiskFileId() != null && d.getCloudDiskFileId().length() > 0) fIds.add(d.getCloudDiskFileId());
        }
        // 批量查 tb_user
        Map<Integer, UserDO> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            for (UserDO u : userDao.listByIds(new ArrayList<>(userIds))) userMap.put(u.getId(), u);
        }
        // 批量查 b_service_order
        Map<Integer, ServiceOrderDO> soMap = new HashMap<>();
        if (!soIds.isEmpty()) {
            for (ServiceOrderDO so : serviceOrderDao.listByIds(new ArrayList<>(soIds))) soMap.put(so.getId(), so);
        }
        // 批量查 b_order_mara_annotation
        Map<Integer, OrderMaraAnnotationDO> omMap = new HashMap<>();
        if (!soIds.isEmpty()) {
            for (OrderMaraAnnotationDO om : orderMaraAnnotationDao.listByServiceOrderIds(new ArrayList<>(soIds)))
                omMap.put(om.getServiceOrderId(), om);
        }
        // 批量查 b_cloud_disk_file
        Map<String, CloudDiskFile> cdMap = new HashMap<>();
        if (!fIds.isEmpty()) {
            for (CloudDiskFile cf : cloudDiskFileDao.listByFileIds(new ArrayList<>(fIds)))
                cdMap.put(cf.getFileId(), cf);
        }
        // 组装
        for (FileMaraAnnotationDO fileMaraAnnotationDo : doList) {
            FileMaraAnnotationDTO dto = mapper.map(fileMaraAnnotationDo, FileMaraAnnotationDTO.class);
            if (dto.getServiceOrderId() > 0) {
                dto.setOrderMaraAnnotation(omMap.get(dto.getServiceOrderId()));
                dto.setServiceOrder(soMap.get(dto.getServiceOrderId()));
            }
            if (dto.getUserId() > 0) {
                dto.setUser(userMap.get(dto.getUserId()));
            }
            if (dto.getCloudDiskFileId() != null && dto.getCloudDiskFileId().length() > 0) {
                dto.setCloudDiskFile(cdMap.get(dto.getCloudDiskFileId()));
            }
            dtoList.add(dto);
        }
        return dtoList;
    }

}