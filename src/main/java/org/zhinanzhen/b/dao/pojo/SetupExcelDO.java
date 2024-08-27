package org.zhinanzhen.b.dao.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SetupExcelDO {

    private Integer id;

    private Date gmtCreate;

    private String url;

    private String docId;

    private String sheetId;
}
