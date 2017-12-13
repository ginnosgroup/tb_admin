package org.zhinanzhen.tb.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.zhinanzhen.tb.service.pojo.UserDTO;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class PrintExcelUtil {
    /**
     * 输出excel工具
     * 
     * @throws Exception
     */
    public static void getUserExcel(List<UserDTO> userDtoList, String fileName) throws Exception {
	WritableWorkbook book = Workbook.createWorkbook(new File(fileName));
	WritableSheet sheet = book.createSheet("第一页", 0);
	String[] title = { "创建时间", "客户ID", "客户真名", "微信昵称", "电话号码", "用户填写的微信号", "邮箱", "余额", "顾问" };
	for (int i = 0; i < title.length; i++) {
	    sheet.addCell(new Label(i, 0, title[i]));
	}
	book.write();
	book.close();
    }
    public static void main(String[] args) throws Exception {
	List<UserDTO>userDtoList = new ArrayList<UserDTO>();
	getUserExcel(userDtoList, "test.xls");
    }
}
