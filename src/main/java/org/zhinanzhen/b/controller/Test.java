package org.zhinanzhen.b.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import jxl.Cell;
import jxl.Sheet;
import jxl.read.biff.BiffException;

public class Test {

	public static void main(String[] args) {
		try (InputStream is = new FileInputStream("/home/larry/下载/commission_order_information (3).xls")) {
			jxl.Workbook wb = jxl.Workbook.getWorkbook(is);
			Sheet sheet = wb.getSheet(0);
			for (int i = 1; i < sheet.getRows(); i++) {
				Cell[] cells = sheet.getRow(i);
				String _id = cells[0].getContents();
				String _schoolPaymentAmount = cells[21].getContents();
				String _schoolPaymentDate = cells[22].getContents();
				String _invoiceNumber = cells[23].getContents();
				String _zyDate = cells[23].getContents();
				String _bonus = cells[27].getContents();
				String _bonusDate = cells[28].getContents();
				System.out.println("id=" + _id);
				System.out.println("schoolPaymentAmount=" + _schoolPaymentAmount);
			}
		} catch (BiffException | IOException e) {
		}
	}

}
