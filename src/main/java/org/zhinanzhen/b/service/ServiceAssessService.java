package org.zhinanzhen.b.service;

import org.zhinanzhen.b.dao.pojo.ServiceAssessDO;
import org.zhinanzhen.b.dao.pojo.ServiceCategory;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: bsz
 * Date: 2020/11/25 16:50
 * Description:
 * Version: V1.0
 */
public interface ServiceAssessService {

    List<ServiceAssessDO> list(Integer serviceId);

    int add(String name, Integer serviceId);

    int addCategory(String name, String fixPrice);

    int update(Integer id, String name);

    int delete(Integer id);

    ServiceAssessDO seleteAssessById(String serviceAssessId);

    List<ServiceAssessDO> seleteAssessByServiceId(String serviceId);

    List<ServiceCategory> listCategory(int pageNum,int pageSize);

    int updateCategory(Integer id, String name, String fixPrice);

    int deleteCategory(Integer id);

    int countCategory();
}
