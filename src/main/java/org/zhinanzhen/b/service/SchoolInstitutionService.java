package org.zhinanzhen.b.service;

import org.zhinanzhen.b.service.pojo.CommissionOrderListDTO;
import org.zhinanzhen.b.service.pojo.SchoolInstitutionCommentDTO;
import org.zhinanzhen.b.service.pojo.SchoolInstitutionDTO;
import org.zhinanzhen.b.service.pojo.SchoolSettingNewDTO;
import org.zhinanzhen.tb.service.ServiceException;

import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/04/01 17:48
 * Description:
 * Version: V1.0
 */
public interface SchoolInstitutionService {

    List<SchoolInstitutionDTO> listSchoolInstitutionDTO(String name, String type, String code, Boolean isFreeze, int pageNum, int pageSize,
                                                        String orderBy, String keyword);

    SchoolInstitutionDTO getSchoolInstitutionById(Integer id);

    List<String> getTradingNamesById(Integer id);

    SchoolInstitutionDTO getSchoolInstitutionByCode(String code);

    int count(String name, String type, String code, Boolean isFreeze);

    boolean update(SchoolInstitutionDTO schoolInstitutionDTO) throws ServiceException;

    int add(SchoolInstitutionDTO schoolInstitutionDTO) throws ServiceException;

    boolean delete(int id);

    int addSetting(SchoolSettingNewDTO schoolSettingNewDTO);

    SchoolSettingNewDTO getByProviderIdAndLevel(int providerId, Integer level, String courseLevel, Integer courseId);

    int updateSetting(SchoolSettingNewDTO schoolSettingNewDTO) throws ServiceException;

    int deleteSetting(int id) throws ServiceException;

    int updateSchoolAttachments(int providerId, String contractFile1, String contractFile2, String contractFile3, String remarks) throws ServiceException;

    public int updateSchoolSetting(CommissionOrderListDTO commissionOrderListDto) throws ServiceException;

    SchoolSettingNewDTO getSchoolSettingNewById(int id);

    int deleteSchoolAttachments(int providerId, boolean isDeleteFile1, boolean isDeleteFile2, boolean isDeleteFile3) throws ServiceException;

    int addComment(SchoolInstitutionCommentDTO commentDTO) throws ServiceException;

    List<SchoolInstitutionCommentDTO> listComment(int schoolInstitutionId) throws ServiceException;

    int deleteComment(int id) throws ServiceException;

    SchoolInstitutionCommentDTO getCommentById(int id) throws ServiceException;
}
