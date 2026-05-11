package org.zhinanzhen.b.dao;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.ReviewAIDO;

public interface ReviewAIDAO {

    int addReviewAI(ReviewAIDO reviewAIDo);

    int updateReviewAI(ReviewAIDO reviewAIDo);

    ReviewAIDO getReviewAIById(@Param("id") int id);

    List<ReviewAIDO> listReviewAI(@Param("serviceOrderId") Integer serviceOrderId,
                                  @Param("adminUserId") Integer adminUserId,
                                  @Param("offset") int offset,
                                  @Param("pageSize") int pageSize);

    int countReviewAI(@Param("serviceOrderId") Integer serviceOrderId,
                      @Param("adminUserId") Integer adminUserId);

    int deleteReviewAIById(@Param("id") int id);

    List<ReviewAIDO> listByServiceOrderIds(@Param("serviceOrderIds") List<Integer> serviceOrderIds);
}
