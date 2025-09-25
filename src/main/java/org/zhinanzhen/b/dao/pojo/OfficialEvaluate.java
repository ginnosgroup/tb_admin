package org.zhinanzhen.b.dao.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OfficialEvaluate implements Serializable {
    private Integer id;

    private Integer officialId;

    private Integer adviserId;

    private String professionalism;

    private String accuracy;

    private String timelyCommunication;

    private String collaborationTime;

    private String remark;

    private String averageScore;

    private String evaluateAdviser;

    private String evaluateOfficial;

    private String threeMonthsAverageScore;
}
