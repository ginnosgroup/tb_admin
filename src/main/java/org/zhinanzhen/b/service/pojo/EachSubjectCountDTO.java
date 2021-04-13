package org.zhinanzhen.b.service.pojo;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/04/12 15:24
 * Description:
 * Version: V1.0
 */
@Data
public class EachSubjectCountDTO {



    private int total;

    private String name;

    private List<Subject> subject;

    @Data
    public  class Subject{
        private int number;
        private String subjectName;
    }
}
