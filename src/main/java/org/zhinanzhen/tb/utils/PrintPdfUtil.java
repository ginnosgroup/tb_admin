package org.zhinanzhen.tb.utils;


import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.zhinanzhen.b.dao.pojo.InvoiceSchoolDescriptionDO;
import org.zhinanzhen.b.dao.pojo.InvoiceServiceFeeDescriptionDO;
import org.zhinanzhen.b.service.pojo.InvoiceSchoolDTO;
import org.zhinanzhen.b.service.pojo.InvoiceServiceFeeDTO;
import org.zhinanzhen.tb.controller.Response;
import java.io.*;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: bsz
 * Date: 2020/10/29 9:16
 * Description:
 * Version: V1.0
 */
public class PrintPdfUtil {


    static Font fontbule10 = FontFactory.getFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED,10f, Font.NORMAL, BaseColor.BLUE);
    static Font fontbule8 = FontFactory.getFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED,8f, Font.NORMAL, BaseColor.BLUE);
    static Font FontChinese12 = FontFactory.getFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED,12f, Font.NORMAL, BaseColor.BLACK);
    static Font FontChinese11Bold = FontFactory.getFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED,11f, Font.BOLD, BaseColor.BLACK);
    static Font FontChinese8 = FontFactory.getFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED,8f, Font.NORMAL, BaseColor.BLACK);
    static Font FontChinese8Bold = FontFactory.getFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED,8f, Font.BOLD, BaseColor.BLACK);
    static Font FontChinese10 = FontFactory.getFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED,10f, Font.NORMAL, BaseColor.BLACK);
    //static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
    static SimpleDateFormat dobsdf = new SimpleDateFormat("dd/MM/yyyy ");

    private static Watermark watermark = new Watermark();

    public static String pdfout(String invoiceNo ,Response response , String Model , String realPath ,boolean canceled ) {

        SimpleDateFormat sdftodocument = new SimpleDateFormat("yyyyMM");

        /*
        //得到static之后的路径
        String path = "/data/upload/pdf/"+sdftodocument.format(Calendar.getInstance().getTime());

        String staticpath = realPath+"static" ;//加上static

        staticpath = staticpath + path;
        try {
            //去掉空格  D:\Program%20Files
            staticpath = URLDecoder.decode(staticpath, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String PDFPath = staticpath.replace('/', '\\').substring(1, realPath.length());

        invoiceNo = invoiceNo + ".pdf";

        PDFPath = staticpath +File.separator+ invoiceNo;

        //创建根目录
        File file = new File(staticpath, invoiceNo);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }*/


        String uploadsPath = "/uploads/pdf/"+sdftodocument.format(Calendar.getInstance().getTime());

        String path = "/data"+uploadsPath;

        //这里是生成在/data/uploads 下面
        invoiceNo = invoiceNo + ".pdf";

        String PDFPath = path +File.separator+ invoiceNo;

        //创建根目录
        File file = new File(PDFPath);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }

        if (Model.equals("SF"))
            createServiceFeePdf((InvoiceServiceFeeDTO) response.getData() ,PDFPath ,realPath , canceled);
        else if (Model.equals("IES"))
            createIESPdf((InvoiceSchoolDTO) response.getData() ,PDFPath ,realPath , canceled);
        else if (Model.equals("M"))
            createMarkPdf((InvoiceSchoolDTO) response.getData() ,PDFPath ,realPath , canceled);
        else if (Model.equals("N"))
            createNorPdf((InvoiceSchoolDTO) response.getData() ,PDFPath ,realPath , canceled);

        return uploadsPath + "/" + invoiceNo;


    }

    /**
     * servicefee pdf 模板
     * @param invoiceServiceFeeDTO
     */
    public static void createServiceFeePdf(InvoiceServiceFeeDTO invoiceServiceFeeDTO , String pdfpath ,String realPath ,boolean canceled){


        try {
            Document document = new Document(PageSize.A4.rotate());
            //PdfWriter.getInstance(document, new FileOutputStream("E:\\Helloworld.PDF"));
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdfpath));
            PdfPCell nullcell = new PdfPCell();
            nullcell.setBorder(0);
            document.open();

            if (canceled == true)
            watermark.onEndPage(writer,document);

            //第一列
            PdfPTable table1 = new PdfPTable(3);
            //String znzimagePath = "/data/uploads/adviser_img/1505966408725_logo.png";
            //String znzimagePath = realPath + "img/znz.png";
            String znzimagePath = "/data/uploads/pdfimg/znz.png";
            Image znzlogo = Image.getInstance(URLDecoder.decode(znzimagePath, "utf-8"));
            int width1[] = {20,60,20};
            table1.setWidths(width1);
            table1.getDefaultCell().setBorder(0);
            table1.addCell(znzlogo);
            table1.addCell("");
            table1.addCell("");
            document.add(table1);

            //第二列
            PdfPTable table2 = new PdfPTable(4);
            PdfPCell cell21 = new PdfPCell(new Paragraph("Company Title: ", FontChinese12));
            cell21.setFixedHeight(25);
            PdfPCell cell22 = new PdfPCell(new Paragraph(invoiceServiceFeeDTO.getCompany(), FontChinese12));
            PdfPCell cell23 = new PdfPCell(new Paragraph("ABN: ",FontChinese10 ));
            PdfPCell cell24 = new PdfPCell(new Paragraph(invoiceServiceFeeDTO.getAbn(), FontChinese10));
            cell21.setVerticalAlignment(Element.ALIGN_MIDDLE); //中间
            cell21.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell22.setVerticalAlignment(Element.ALIGN_MIDDLE);
            //cell22.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell23.setVerticalAlignment(Element.ALIGN_BOTTOM); //垂直底部
            cell23.setHorizontalAlignment(Element.ALIGN_CENTER);//水平中间
            cell24.setVerticalAlignment(Element.ALIGN_BOTTOM);
            //cell24.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell21.setBorder(0);
            cell22.setBorder(0);
            cell23.setBorder(0);
            cell24.setBorder(0);
            //设置每列宽度比例
            int width2[] = {15,60,5,20};
            table2.setWidths(width2);
            table2.getDefaultCell().setBorder(0);
            table2.addCell(cell21);
            table2.addCell(cell22);
            table2.addCell(cell23);
            table2.addCell(cell24);
            document.add(table2);

            //第三列
            PdfPTable table3 = new PdfPTable(6);
            PdfPCell cell31 = new PdfPCell(new Paragraph("Address : ", FontChinese8));
            cell31.setFixedHeight(30);
            PdfPCell cell32 = new PdfPCell(new Paragraph(invoiceServiceFeeDTO.getAddress(), fontbule8));
            PdfPCell cell33 = new PdfPCell(new Paragraph("Tel : ",FontChinese8 ));
            PdfPCell cell34 = new PdfPCell(new Paragraph(invoiceServiceFeeDTO.getTel(), FontChinese8));
            PdfPCell cell35 = new PdfPCell(new Paragraph("E-mail : ",FontChinese8 ));
            PdfPCell cell36 = new PdfPCell(new Paragraph(invoiceServiceFeeDTO.getEmail(), FontChinese8));
            cell31.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell31.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell32.setVerticalAlignment(Element.ALIGN_MIDDLE);
            //cell32.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell33.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell33.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell34.setVerticalAlignment(Element.ALIGN_MIDDLE);
            //cell34.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell35.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell35.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell36.setVerticalAlignment(Element.ALIGN_MIDDLE);
            //cell36.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell31.setBorder(0);
            cell32.setBorder(0);
            cell33.setBorder(0);
            cell34.setBorder(0);
            cell35.setBorder(0);
            cell36.setBorder(0);
            //设置每列宽度比例
            int width3[] = {7,28,5,30,7,23};
            table3.setWidths(width3);
            table3.getDefaultCell().setBorder(0);
            table3.addCell(cell31);
            table3.addCell(cell32);
            table3.addCell(cell33);
            table3.addCell(cell34);
            table3.addCell(cell35);
            table3.addCell(cell36);
            document.add(table3);

            //第四列
            PdfPTable table4 = new PdfPTable(1);
            PdfPCell cell4 = new PdfPCell(new Paragraph(" Tax Invoice ", FontChinese12));
            cell4.setFixedHeight(30);
            cell4.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell4.setHorizontalAlignment(Element.ALIGN_CENTER);
            table4.getDefaultCell().setBorder(0);
            table4.addCell(cell4);
            document.add(table4);

            //第五列
            PdfPTable table5 = new PdfPTable(5);
            //PdfPCell cell51 = new PdfPCell(new Paragraph(" Bill to ", FontChinese10));
            PdfPCell cell52 = new PdfPCell(new Paragraph("Invoice No.: ", FontChinese10));
            PdfPCell cell53 = new PdfPCell(new Paragraph(invoiceServiceFeeDTO.getInvoiceNo(), fontbule10));
            PdfPCell cell54 = new PdfPCell(new Paragraph("Invoice Date : ", FontChinese10));
            PdfPCell cell55 = new PdfPCell(new Paragraph(dobsdf.format(invoiceServiceFeeDTO.getInvoiceDate()), fontbule10));
            cell52.setFixedHeight(20);
            cell52.setBorder(0);
            cell53.setBorder(0);
            cell54.setBorder(0);
            cell55.setBorder(0);
            int width5[] = {50,10,15,10,15};
            table5.setWidths(width5);
            table5.getDefaultCell().setBorder(0);
            table5.addCell(nullcell);
            table5.addCell(cell52);
            table5.addCell(cell53);
            table5.addCell(cell54);
            table5.addCell(cell55);
            document.add(table5);


            //增加一列  19列
            PdfPTable table19 = new PdfPTable(4);
            PdfPCell cell191 = new PdfPCell(new Paragraph(" Bill To : ", FontChinese10));
            PdfPCell cell192 = new PdfPCell(new Paragraph(invoiceServiceFeeDTO.getBillTo(), FontChinese10));
            cell191.setFixedHeight(20);
            cell191.setBorder(0);
            cell192.setBorder(0);
            int width19[] = {2,6,34,58};
            table19.setWidths(width19);
            table19.getDefaultCell().setBorder(0);
            table19.addCell(nullcell);
            table19.addCell(cell191);
            table19.addCell(cell192);
            table19.addCell(nullcell);
            document.add(table19);

            //第9列
            PdfPTable table9 = new PdfPTable(1);
            PdfPCell cell91 = new PdfPCell(new Paragraph(" Description ", FontChinese11Bold));
            cell91.setVerticalAlignment(Element.ALIGN_BOTTOM);
            cell91.setFixedHeight(70);
            cell91.setBorder(0);
            int width9[] = {10};
            table9.setWidths(width9);
            table9.getDefaultCell().setBorder(0);
            table9.addCell(cell91);
            document.add(table9);

            //第10列
            List<InvoiceServiceFeeDescriptionDO> des = invoiceServiceFeeDTO.getInvoiceServiceFeeDescriptionDOList();
            PdfPTable table10 = new PdfPTable(5);
            PdfPCell cell10 ;
            int width10[] = {20,20,20,20,20};
            for (int i = -1; i < des.size(); i++) {
                if (i == -1) {
                    cell10 = new PdfPCell(new Paragraph(" NO. ", FontChinese8));
                    cell10.setFixedHeight(15);
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(" DESCRIPTION ", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(" UNIT PRICE ", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(" QUANTITY ", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(" AMOUNT ", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    table10.setHorizontalAlignment(Element.ALIGN_CENTER);
                } else {
                    cell10 = new PdfPCell(new Paragraph(des.get(i).getId()+"", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell10.setFixedHeight(15);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(des.get(i).getDescription(), FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(des.get(i).getUnitPrice().toString(), FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(des.get(i).getQuantity()+"", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(des.get(i).getAmount().toString(), FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    table10.setHorizontalAlignment(Element.ALIGN_CENTER);
                }


            }

            table10.setWidths(width10);
            document.add(table10);

            //第11列
            PdfPTable table11 = new PdfPTable(2);
            PdfPCell cell111 = new PdfPCell(new Paragraph(" 10%GST : ", FontChinese11Bold));
            PdfPCell cell112 = new PdfPCell(new Paragraph(invoiceServiceFeeDTO.getGst().toString(), fontbule10));
            cell111.setHorizontalAlignment(Element.ALIGN_RIGHT);//设置水平在右
            cell111.setVerticalAlignment(Element.ALIGN_BOTTOM);//设置垂直在下
            cell112.setVerticalAlignment(Element.ALIGN_BOTTOM);//设置垂直在下
            cell111.setBorder(0);
            cell112.setBorder(0);
            cell111.setFixedHeight(40);
            int width11[] = {80,20};
            table11.setWidths(width11);
            table11.getDefaultCell().setBorder(0);
            table11.addCell(cell111);
            table11.addCell(cell112);
            document.add(table11);

            //第12列
            PdfPTable table12 = new PdfPTable(2);
            PdfPCell cell121 = new PdfPCell(new Paragraph(" Invoice total with GST : ", FontChinese11Bold));
            PdfPCell cell122 = new PdfPCell(new Paragraph(invoiceServiceFeeDTO.getTotalGST().toString(), fontbule10));
            cell121.setHorizontalAlignment(Element.ALIGN_RIGHT);//设置水平在右
            cell121.setBorder(0);
            cell122.setBorder(0);
            cell121.setFixedHeight(20);
            int width12[] = {80,20};
            table12.setWidths(width12);
            table12.getDefaultCell().setBorder(0);
            table12.addCell(cell121);
            table12.addCell(cell122);
            document.add(table12);

            //第13列
            PdfPTable table13 = new PdfPTable(2);
            PdfPCell cell131 = new PdfPCell(new Paragraph(" NOTE : ", FontChinese8Bold));
            String  str=invoiceServiceFeeDTO.getNote();
            PdfPCell cell132 = new PdfPCell(new Paragraph(str, fontbule10));
            cell131.setHorizontalAlignment(Element.ALIGN_CENTER);//设置水平在右
            cell131.setVerticalAlignment(Element.ALIGN_TOP);
            cell131.setBorder(0);
            cell131.setFixedHeight(40);
            int width13[] = {7,93};
            table13.setWidths(width13);
            table13.getDefaultCell().setBorder(0);
            table13.addCell(cell131);
            table13.addCell(cell132);
            document.add(table13);

            //第14列
            PdfPTable table14 = new PdfPTable(2);
            PdfPCell cell141 = new PdfPCell(new Paragraph(" Please make payment to the account below, and quote the invoice number: ", FontChinese11Bold));
            cell141.setBorder(0);
            cell141.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell141.setFixedHeight(30);
            int width14[] = {2,98};
            table14.setWidths(width14);
            table14.getDefaultCell().setBorder(0);
            table14.addCell(nullcell);
            table14.addCell(cell141);
            document.add(table14);

            //第15列
            PdfPTable table15 = new PdfPTable(4);
            PdfPCell cell151 = new PdfPCell();
            PdfPCell cell152 = new PdfPCell(new Paragraph(" Bank Name ", FontChinese8));
            PdfPCell cell153 = new PdfPCell(new Paragraph(" Commonwealth Bank ", FontChinese8));
            cell151.setBorder(0);
            cell152.setBackgroundColor(BaseColor.GRAY);
            cell153.setBackgroundColor(BaseColor.GRAY);
            cell151.setFixedHeight(20);
            int width15[] = {2,10,30,58};
            table15.setWidths(width15);
            table15.getDefaultCell().setBorder(0);
            table15.addCell(cell151);
            table15.addCell(cell152);
            table15.addCell(cell153);
            table15.addCell(cell151);
            document.add(table15);

            //第16列
            PdfPTable table16 = new PdfPTable(4);
            PdfPCell cell161 = new PdfPCell(new Paragraph(" Account Name ", FontChinese8));
            PdfPCell cell162 = new PdfPCell(new Paragraph(invoiceServiceFeeDTO.getAccountname(), FontChinese8));
            cell161.setFixedHeight(20);
            int width16[] = {2,10,30,58};
            table16.setWidths(width16);
            table16.getDefaultCell().setBorder(0);
            table16.addCell(nullcell);
            table16.addCell(cell161);
            table16.addCell(cell162);
            table16.addCell(nullcell);
            document.add(table16);


            //第17列
            PdfPTable table17 = new PdfPTable(4);
            PdfPCell cell171 = new PdfPCell(new Paragraph(" BSB ", FontChinese8));
            PdfPCell cell172 = new PdfPCell(new Paragraph(invoiceServiceFeeDTO.getBsb(), FontChinese8));
            cell171.setFixedHeight(20);
            int width17[] = {2,10,30,58};
            table17.setWidths(width17);
            table17.getDefaultCell().setBorder(0);
            table17.addCell(nullcell);
            table17.addCell(cell171);
            table17.addCell(cell172);
            table17.addCell(nullcell);
            document.add(table17);

            //第18列
            PdfPTable table18 = new PdfPTable(4);
            PdfPCell cell181 = new PdfPCell(new Paragraph(" Account No ", FontChinese8));
            PdfPCell cell182 = new PdfPCell(new Paragraph(invoiceServiceFeeDTO.getAccountno(), FontChinese8));
            cell181.setFixedHeight(20);
            int width18[] = {2,10,30,58};
            table18.setWidths(width18);
            table18.getDefaultCell().setBorder(0);
            table18.addCell(nullcell);
            table18.addCell(cell181);
            table18.addCell(cell182);
            table18.addCell(nullcell);
            document.add(table18);


            document.close();

        } catch (DocumentException e) {
            e.printStackTrace();
            System.out.println();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * IES 留学专用模板
     */
    public static void createIESPdf(InvoiceSchoolDTO invoiceSchoolDTO, String pdfpath, String realPath, boolean canceled){
        try {

            Document document = new Document(PageSize.A4.rotate());
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdfpath));

            PdfPCell nullcell = new PdfPCell();
            nullcell.setBorder(0);
            document.open();

            if (canceled == true)
                watermark.onEndPage(writer,document);

            //第二列
            PdfPTable table2 = new PdfPTable(3);
            //PdfPCell cell21 = new PdfPCell(new Paragraph("Company Title: ", FontChinese12));

            PdfPCell cell22 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getCompany(), FontChinese12));
            cell22.setFixedHeight(25);
            PdfPCell cell23 = new PdfPCell(new Paragraph("ABN: ",FontChinese10 ));
            PdfPCell cell24 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getAbn(), FontChinese10));
            //cell21.setVerticalAlignment(Element.ALIGN_MIDDLE); //中间
            //cell21.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell22.setVerticalAlignment(Element.ALIGN_MIDDLE);
            //cell22.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell23.setVerticalAlignment(Element.ALIGN_BOTTOM); //垂直底部
            cell23.setHorizontalAlignment(Element.ALIGN_CENTER);//水平中间
            cell24.setVerticalAlignment(Element.ALIGN_BOTTOM);
            //cell24.setHorizontalAlignment(Element.ALIGN_CENTER);
            //cell21.setBorder(0);
            cell22.setBorder(0);
            cell23.setBorder(0);
            cell24.setBorder(0);
            //设置每列宽度比例
            int width2[] = {70,5,25};
            table2.setWidths(width2);
            table2.getDefaultCell().setBorder(0);
            //table2.addCell(cell21);
            table2.addCell(cell22);
            table2.addCell(cell23);
            table2.addCell(cell24);
            document.add(table2);

            //第三列
            PdfPTable table3 = new PdfPTable(6);
            PdfPCell cell31 = new PdfPCell(new Paragraph("Address : ", FontChinese8));
            cell31.setFixedHeight(30);
            PdfPCell cell32 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getAddress(), fontbule8));
            PdfPCell cell33 = new PdfPCell(new Paragraph("Tel : ",FontChinese8 ));
            PdfPCell cell34 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getTel(), FontChinese8));
            PdfPCell cell35 = new PdfPCell(new Paragraph("E-mail : ",FontChinese8 ));
            PdfPCell cell36 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getEmail(), FontChinese8));
            cell31.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell31.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell32.setVerticalAlignment(Element.ALIGN_MIDDLE);
            //cell32.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell33.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell33.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell34.setVerticalAlignment(Element.ALIGN_MIDDLE);
            //cell34.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell35.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell35.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell36.setVerticalAlignment(Element.ALIGN_MIDDLE);
            //cell36.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell31.setBorder(0);
            cell32.setBorder(0);
            cell33.setBorder(0);
            cell34.setBorder(0);
            cell35.setBorder(0);
            cell36.setBorder(0);
            //设置每列宽度比例
            int width3[] = {7,28,5,30,7,23};
            table3.setWidths(width3);
            table3.getDefaultCell().setBorder(0);
            table3.addCell(cell31);
            table3.addCell(cell32);
            table3.addCell(cell33);
            table3.addCell(cell34);
            table3.addCell(cell35);
            table3.addCell(cell36);
            document.add(table3);

            //第四列
            PdfPTable table4 = new PdfPTable(1);
            PdfPCell cell4 = new PdfPCell(new Paragraph(" Tax Invoice ", FontChinese12));
            cell4.setFixedHeight(30);
            cell4.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell4.setHorizontalAlignment(Element.ALIGN_CENTER);
            table4.getDefaultCell().setBorder(0);
            table4.addCell(cell4);
            document.add(table4);

            //第五列
            PdfPTable table5 = new PdfPTable(5);
            PdfPCell cell51 = new PdfPCell(new Paragraph(" Bill to ", FontChinese10));
            PdfPCell cell52 = new PdfPCell(new Paragraph("Invoice No.: ", FontChinese10));
            PdfPCell cell53 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getInvoiceNo(), fontbule10));
            PdfPCell cell54 = new PdfPCell(new Paragraph("Invoice Date : ", FontChinese10));
            PdfPCell cell55 = new PdfPCell(new Paragraph(dobsdf.format(invoiceSchoolDTO.getInvoiceDate()), fontbule10));
            cell51.setFixedHeight(20);
            cell51.setBorder(0);
            cell52.setBorder(0);
            cell53.setBorder(0);
            cell54.setBorder(0);
            cell55.setBorder(0);
            int width5[] = {50,10,15,10,15};
            table5.setWidths(width5);
            table5.getDefaultCell().setBorder(0);
            table5.addCell(cell51);
            table5.addCell(cell52);
            table5.addCell(cell53);
            table5.addCell(cell54);
            table5.addCell(cell55);
            document.add(table5);

            //第六列
            PdfPTable table6 = new PdfPTable(2);
            PdfPCell cell61 = new PdfPCell(new Paragraph(" Company : ", FontChinese10));
            PdfPCell cell62 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getInvoiceBillToDO().getCompany(), fontbule10));
            cell61.setFixedHeight(20);
            cell61.setBorder(0);
            cell62.setBorder(0);
            int width6[] = {10,90};
            table6.setWidths(width6);
            table6.getDefaultCell().setBorder(0);
            table6.addCell(cell61);
            table6.addCell(cell62);
            document.add(table6);

            //第七列
            PdfPTable table7 = new PdfPTable(2);
            PdfPCell cell71 = new PdfPCell(new Paragraph(" ABN : ", FontChinese10));
            PdfPCell cell72 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getInvoiceBillToDO().getAbn(), fontbule10));
            cell71.setFixedHeight(20);
            cell71.setBorder(0);
            cell72.setBorder(0);
            int width7[] = {10,90};
            table7.setWidths(width7);
            table7.getDefaultCell().setBorder(0);
            table7.addCell(cell71);
            table7.addCell(cell72);
            document.add(table7);

            //第8列
            PdfPTable table8 = new PdfPTable(2);
            PdfPCell cell81 = new PdfPCell(new Paragraph(" Address : ", FontChinese10));
            PdfPCell cell82 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getInvoiceBillToDO().getAddress(), fontbule10));
            cell81.setFixedHeight(20);
            cell81.setBorder(0);
            cell82.setBorder(0);
            int width8[] = {10,90};
            table8.setWidths(width8);
            table8.getDefaultCell().setBorder(0);
            table8.addCell(cell81);
            table8.addCell(cell82);
            document.add(table8);

            //第9列
            PdfPTable table9 = new PdfPTable(1);
            PdfPCell cell91 = new PdfPCell(new Paragraph(" Description ", FontChinese11Bold));
            cell91.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell91.setFixedHeight(30);
            cell91.setBorder(0);
            int width9[] = {10};
            table9.setWidths(width9);
            table9.getDefaultCell().setBorder(0);
            table9.addCell(cell91);
            document.add(table9);

            //第10列
            PdfPTable table10 = new PdfPTable(12);
            List<InvoiceSchoolDescriptionDO> des = invoiceSchoolDTO.getInvoiceSchoolDescriptionDOS();
            PdfPCell cell10 ;
            int width10[] = {5,10,8,9,9,8,9,9,9,9,10,5};
            for (int i = -1; i < des.size() ; i++) {
                if (i == -1) {
                    cell10 = new PdfPCell(new Paragraph(" NO. ", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell10.setFixedHeight(15);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(" Student Name ", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(" DOB ", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(" Student ID ", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(" Course ", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(" Start Date ", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(" Non Tuition Fee ", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(" Tuition Fee ", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(" Commission Rate ", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(" Commission ", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(" Bonus Amount ", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(" Instalment ", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    table10.setHorizontalAlignment(Element.ALIGN_CENTER);
                } else {
                    cell10 = new PdfPCell(new Paragraph(des.get(i).getId()+"", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell10.setFixedHeight(15);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(des.get(i).getStudentname(), FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(dobsdf.format(des.get(i).getDob()), FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(des.get(i).getStudentId()+"", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(des.get(i).getCourse(), FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(dobsdf.format(des.get(i).getStartDate()), FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(des.get(i).getNonTuitionFee().toString(), FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(des.get(i).getTuitionFee().toString(), FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(des.get(i).getCommissionrate().toString(), FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(des.get(i).getCommission().toString(), FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(des.get(i).getBonus().toString(), FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(des.get(i).getInstalMent(), FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    table10.setHorizontalAlignment(Element.ALIGN_CENTER);
                }


            }

            table10.setWidths(width10);
            table8.getDefaultCell().setBorder(0);
            document.add(table10);

            //第11列
            PdfPTable table11 = new PdfPTable(2);
            PdfPCell cell111 = new PdfPCell(new Paragraph(" 10%GST : ", FontChinese11Bold));
            PdfPCell cell112 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getGst().toString(), fontbule10));
            cell111.setHorizontalAlignment(Element.ALIGN_RIGHT);//设置水平在右
            cell111.setVerticalAlignment(Element.ALIGN_BOTTOM);//设置垂直在下
            cell112.setVerticalAlignment(Element.ALIGN_BOTTOM);//设置垂直在下
            cell111.setBorder(0);
            cell112.setBorder(0);
            cell111.setFixedHeight(40);
            int width11[] = {80,20};
            table11.setWidths(width11);
            table11.getDefaultCell().setBorder(0);
            table11.addCell(cell111);
            table11.addCell(cell112);
            document.add(table11);

            //第12列
            PdfPTable table12 = new PdfPTable(2);
            PdfPCell cell121 = new PdfPCell(new Paragraph(" Invoice total with GST : ", FontChinese11Bold));
            PdfPCell cell122 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getTotalGST().toString(), fontbule10));
            cell121.setHorizontalAlignment(Element.ALIGN_RIGHT);//设置水平在右
            cell121.setBorder(0);
            cell122.setBorder(0);
            cell121.setFixedHeight(20);
            int width12[] = {80,20};
            table12.setWidths(width12);
            table12.getDefaultCell().setBorder(0);
            table12.addCell(cell121);
            table12.addCell(cell122);
            document.add(table12);

            //第13列
            PdfPTable table13 = new PdfPTable(2);
            PdfPCell cell131 = new PdfPCell(new Paragraph(" NOTE : ", FontChinese8Bold));
            String  str=invoiceSchoolDTO.getNote();
            PdfPCell cell132 = new PdfPCell(new Paragraph(str, fontbule10));
            cell131.setHorizontalAlignment(Element.ALIGN_CENTER);//设置水平在右
            cell131.setVerticalAlignment(Element.ALIGN_TOP);
            cell131.setBorder(0);
            cell131.setFixedHeight(40);
            int width13[] = {7,93};
            table13.setWidths(width13);
            table13.getDefaultCell().setBorder(0);
            table13.addCell(cell131);
            table13.addCell(cell132);
            document.add(table13);

            //第14列
            PdfPTable table14 = new PdfPTable(2);
            PdfPCell cell141 = new PdfPCell(new Paragraph(" Please make payment to the account below , and quote the invoice number : ", FontChinese11Bold));
            cell141.setBorder(0);
            cell141.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell141.setFixedHeight(30);
            int width14[] = {2,98};
            table14.setWidths(width14);
            table14.getDefaultCell().setBorder(0);
            table14.addCell(nullcell);
            table14.addCell(cell141);
            document.add(table14);

            //第15列
            PdfPTable table15 = new PdfPTable(4);
            PdfPCell cell151 = new PdfPCell();
            PdfPCell cell152 = new PdfPCell(new Paragraph(" Bank Name ", FontChinese8));
            PdfPCell cell153 = new PdfPCell(new Paragraph(" Commonwealth Bank ", FontChinese8));
            cell151.setBorder(0);
            cell152.setBackgroundColor(BaseColor.GRAY);
            cell153.setBackgroundColor(BaseColor.GRAY);
            cell151.setFixedHeight(20);
            int width15[] = {2,10,30,58};
            table15.setWidths(width15);
            table15.getDefaultCell().setBorder(0);
            table15.addCell(cell151);
            table15.addCell(cell152);
            table15.addCell(cell153);
            table15.addCell(cell151);
            document.add(table15);

            //第16列
            PdfPTable table16 = new PdfPTable(4);
            PdfPCell cell161 = new PdfPCell(new Paragraph(" Account Name ", FontChinese8));
            PdfPCell cell162 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getAccountname(), FontChinese8));
            cell161.setFixedHeight(20);
            int width16[] = {2,10,30,58};
            table16.setWidths(width16);
            table16.getDefaultCell().setBorder(0);
            table16.addCell(nullcell);
            table16.addCell(cell161);
            table16.addCell(cell162);
            table16.addCell(nullcell);
            document.add(table16);


            //第17列
            PdfPTable table17 = new PdfPTable(4);
            PdfPCell cell171 = new PdfPCell(new Paragraph(" BSB ", FontChinese8));
            PdfPCell cell172 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getBsb(), FontChinese8));
            cell171.setFixedHeight(20);
            int width17[] = {2,10,30,58};
            table17.setWidths(width17);
            table17.getDefaultCell().setBorder(0);
            table17.addCell(nullcell);
            table17.addCell(cell171);
            table17.addCell(cell172);
            table17.addCell(nullcell);
            document.add(table17);

            //第18列
            PdfPTable table18 = new PdfPTable(4);
            PdfPCell cell181 = new PdfPCell(new Paragraph(" Account No ", FontChinese8));
            PdfPCell cell182 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getAccountno(), FontChinese8));
            cell181.setFixedHeight(20);
            int width18[] = {2,10,30,58};
            table18.setWidths(width18);
            table18.getDefaultCell().setBorder(0);
            table18.addCell(nullcell);
            table18.addCell(cell181);
            table18.addCell(cell182);
            table18.addCell(nullcell);
            document.add(table18);

            document.close();


        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * mark市场模板
     */
    public static void createMarkPdf(InvoiceSchoolDTO invoiceSchoolDTO, String pdfpath, String realPath, boolean canceled){
        try {

            Document document = new Document(PageSize.A4.rotate());
            PdfWriter writer =  PdfWriter.getInstance(document, new FileOutputStream(pdfpath));

            PdfPCell nullcell = new PdfPCell();
            nullcell.setBorder(0);
            document.open();

            if (canceled == true)
                watermark.onEndPage(writer,document);

            PdfPTable table1 = new PdfPTable(3);
            //String znzimagePath = "/data/uploads/adviser_img/1505966408725_logo.png";
            //String znzimagePath = realPath + "img/znz.png";
            String znzimagePath = "/data/uploads/pdfimg/znz.png";
            Image znzlogo = Image.getInstance(URLDecoder.decode(znzimagePath, "utf-8"));
            int width1[] = {20,60,20};
            table1.setWidths(width1);
            table1.getDefaultCell().setBorder(0);
            table1.addCell(znzlogo);
            table1.addCell("");
            table1.addCell("");
            document.add(table1);

            //第二列
            PdfPTable table2 = new PdfPTable(4);
            PdfPCell cell21 = new PdfPCell(new Paragraph("Company Title: ", FontChinese12));
            cell21.setFixedHeight(25);
            PdfPCell cell22 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getCompany(), FontChinese12));
            PdfPCell cell23 = new PdfPCell(new Paragraph("ABN: ",FontChinese10 ));
            PdfPCell cell24 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getAbn(), FontChinese10));
            cell21.setVerticalAlignment(Element.ALIGN_MIDDLE); //中间
            cell21.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell22.setVerticalAlignment(Element.ALIGN_MIDDLE);
            //cell22.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell23.setVerticalAlignment(Element.ALIGN_BOTTOM); //垂直底部
            cell23.setHorizontalAlignment(Element.ALIGN_CENTER);//水平中间
            cell24.setVerticalAlignment(Element.ALIGN_BOTTOM);
            //cell24.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell21.setBorder(0);
            cell22.setBorder(0);
            cell23.setBorder(0);
            cell24.setBorder(0);
            //设置每列宽度比例
            int width2[] = {15,60,5,20};
            table2.setWidths(width2);
            table2.getDefaultCell().setBorder(0);
            table2.addCell(cell21);
            table2.addCell(cell22);
            table2.addCell(cell23);
            table2.addCell(cell24);
            document.add(table2);

            //第三列
            PdfPTable table3 = new PdfPTable(6);
            PdfPCell cell31 = new PdfPCell(new Paragraph("Address : ", FontChinese8));
            cell31.setFixedHeight(30);
            PdfPCell cell32 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getAddress(), fontbule8));
            PdfPCell cell33 = new PdfPCell(new Paragraph("Tel : ",FontChinese8 ));
            PdfPCell cell34 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getTel(), FontChinese8));
            PdfPCell cell35 = new PdfPCell(new Paragraph("E-mail : ",FontChinese8 ));
            PdfPCell cell36 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getEmail(), FontChinese8));
            cell31.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell31.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell32.setVerticalAlignment(Element.ALIGN_MIDDLE);
            //cell32.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell33.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell33.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell34.setVerticalAlignment(Element.ALIGN_MIDDLE);
            //cell34.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell35.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell35.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell36.setVerticalAlignment(Element.ALIGN_MIDDLE);
            //cell36.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell31.setBorder(0);
            cell32.setBorder(0);
            cell33.setBorder(0);
            cell34.setBorder(0);
            cell35.setBorder(0);
            cell36.setBorder(0);
            //设置每列宽度比例
            int width3[] = {7,28,5,30,7,23};
            table3.setWidths(width3);
            table3.getDefaultCell().setBorder(0);
            table3.addCell(cell31);
            table3.addCell(cell32);
            table3.addCell(cell33);
            table3.addCell(cell34);
            table3.addCell(cell35);
            table3.addCell(cell36);
            document.add(table3);

            //第四列
            PdfPTable table4 = new PdfPTable(1);
            PdfPCell cell4 = new PdfPCell(new Paragraph(" Tax Invoice ", FontChinese12));
            cell4.setFixedHeight(30);
            cell4.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell4.setHorizontalAlignment(Element.ALIGN_CENTER);
            table4.getDefaultCell().setBorder(0);
            table4.addCell(cell4);
            document.add(table4);

            //第五列
            PdfPTable table5 = new PdfPTable(5);
            PdfPCell cell51 = new PdfPCell(new Paragraph(" Bill to ", FontChinese10));
            PdfPCell cell52 = new PdfPCell(new Paragraph("Invoice No.: ", FontChinese10));
            PdfPCell cell53 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getInvoiceNo(), fontbule10));
            PdfPCell cell54 = new PdfPCell(new Paragraph("Invoice Date : ", FontChinese10));
            PdfPCell cell55 = new PdfPCell(new Paragraph(dobsdf.format(invoiceSchoolDTO.getInvoiceDate()), fontbule10));
            cell51.setFixedHeight(20);
            cell51.setBorder(0);
            cell52.setBorder(0);
            cell53.setBorder(0);
            cell54.setBorder(0);
            cell55.setBorder(0);
            int width5[] = {50,10,15,10,15};
            table5.setWidths(width5);
            table5.getDefaultCell().setBorder(0);
            table5.addCell(cell51);
            table5.addCell(cell52);
            table5.addCell(cell53);
            table5.addCell(cell54);
            table5.addCell(cell55);
            document.add(table5);

            //第六列
            PdfPTable table6 = new PdfPTable(2);
            PdfPCell cell61 = new PdfPCell(new Paragraph(" Company : ", FontChinese10));
            PdfPCell cell62 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getInvoiceBillToDO().getCompany(), fontbule10));
            cell61.setFixedHeight(20);
            cell61.setBorder(0);
            cell62.setBorder(0);
            int width6[] = {10,90};
            table6.setWidths(width6);
            table6.getDefaultCell().setBorder(0);
            table6.addCell(cell61);
            table6.addCell(cell62);
            document.add(table6);

            //第七列
            PdfPTable table7 = new PdfPTable(2);
            PdfPCell cell71 = new PdfPCell(new Paragraph(" ABN : ", FontChinese10));
            PdfPCell cell72 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getInvoiceBillToDO().getAbn(), fontbule10));
            cell71.setFixedHeight(20);
            cell71.setBorder(0);
            cell72.setBorder(0);
            int width7[] = {10,90};
            table7.setWidths(width7);
            table7.getDefaultCell().setBorder(0);
            table7.addCell(cell71);
            table7.addCell(cell72);
            document.add(table7);

            //第8列
            PdfPTable table8 = new PdfPTable(2);
            PdfPCell cell81 = new PdfPCell(new Paragraph(" Address : ", FontChinese10));
            PdfPCell cell82 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getInvoiceBillToDO().getAddress(), fontbule10));
            cell81.setFixedHeight(20);
            cell81.setBorder(0);
            cell82.setBorder(0);
            int width8[] = {10,90};
            table8.setWidths(width8);
            table8.getDefaultCell().setBorder(0);
            table8.addCell(cell81);
            table8.addCell(cell82);
            document.add(table8);

            //第9列
            PdfPTable table9 = new PdfPTable(1);
            PdfPCell cell91 = new PdfPCell(new Paragraph(" Description ", FontChinese11Bold));
            cell91.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell91.setFixedHeight(30);
            cell91.setBorder(0);
            int width9[] = {10};
            table9.setWidths(width9);
            table9.getDefaultCell().setBorder(0);
            table9.addCell(cell91);
            document.add(table9);

            //第10列
            PdfPTable table10 = new PdfPTable(9);
            List<InvoiceSchoolDescriptionDO> des = invoiceSchoolDTO.getInvoiceSchoolDescriptionDOS();
            PdfPCell cell10 ;
            int width10[] = {4,12,12,12,12,12,12,12,12};
            for (int i = -1; i < des.size(); i++) {
                if (i == -1) {
                    cell10 = new PdfPCell(new Paragraph(" NO. ", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell10.setFixedHeight(15);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(" Student Name ", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(" DOB ", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(" Student ID ", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(" Course ", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(" Start Date ", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(" Instalment ", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(" Tuition Fee ", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(" Marketing Bonus ", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    table10.setHorizontalAlignment(Element.ALIGN_CENTER);
                } else {
                    cell10 = new PdfPCell(new Paragraph(des.get(i).getId() + "", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell10.setFixedHeight(10);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(des.get(i).getStudentname(), FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(dobsdf.format(des.get(i).getDob()), FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(des.get(i).getStudentId() + "", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(des.get(i).getCourse(), FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(dobsdf.format(des.get(i).getStartDate()), FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(des.get(i).getInstalMent(), FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(des.get(i).getTuitionFee().toString(), FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(des.get(i).getMarketing().toString(), FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    table10.setHorizontalAlignment(Element.ALIGN_CENTER);
                }


            }

            table10.setWidths(width10);
            table8.getDefaultCell().setBorder(0);
            document.add(table10);

            //第11列
            PdfPTable table11 = new PdfPTable(2);
            PdfPCell cell111 = new PdfPCell(new Paragraph(" 10%GST : ", FontChinese11Bold));
            PdfPCell cell112 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getGst().toString(), fontbule10));
            cell111.setHorizontalAlignment(Element.ALIGN_RIGHT);//设置水平在右
            cell111.setVerticalAlignment(Element.ALIGN_BOTTOM);//设置垂直在下
            cell112.setVerticalAlignment(Element.ALIGN_BOTTOM);//设置垂直在下
            cell111.setBorder(0);
            cell112.setBorder(0);
            cell111.setFixedHeight(40);
            int width11[] = {80,20};
            table11.setWidths(width11);
            table11.getDefaultCell().setBorder(0);
            table11.addCell(cell111);
            table11.addCell(cell112);
            document.add(table11);

            //第12列
            PdfPTable table12 = new PdfPTable(2);
            PdfPCell cell121 = new PdfPCell(new Paragraph(" Invoice total with GST : ", FontChinese11Bold));
            PdfPCell cell122 = new PdfPCell(new Paragraph( invoiceSchoolDTO.getTotalGST().toString(), fontbule10));
            cell121.setHorizontalAlignment(Element.ALIGN_RIGHT);//设置水平在右
            cell121.setBorder(0);
            cell122.setBorder(0);
            cell121.setFixedHeight(20);
            int width12[] = {80,20};
            table12.setWidths(width12);
            table12.getDefaultCell().setBorder(0);
            table12.addCell(cell121);
            table12.addCell(cell122);
            document.add(table12);

            //第13列
            PdfPTable table13 = new PdfPTable(2);
            PdfPCell cell131 = new PdfPCell(new Paragraph(" NOTE : ", FontChinese8Bold));
            String  str=invoiceSchoolDTO.getNote();
            PdfPCell cell132 = new PdfPCell(new Paragraph(str, fontbule10));
            cell131.setHorizontalAlignment(Element.ALIGN_CENTER);//设置水平在右
            cell131.setVerticalAlignment(Element.ALIGN_TOP);
            cell131.setBorder(0);
            cell131.setFixedHeight(40);
            int width13[] = {7,93};
            table13.setWidths(width13);
            table13.getDefaultCell().setBorder(0);
            table13.addCell(cell131);
            table13.addCell(cell132);
            document.add(table13);

            //第14列
            PdfPTable table14 = new PdfPTable(2);
            PdfPCell cell141 = new PdfPCell(new Paragraph(" Please make payment to : ", FontChinese11Bold));
            cell141.setBorder(0);
            cell141.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell141.setFixedHeight(30);
            int width14[] = {2,98};
            table14.setWidths(width14);
            table14.getDefaultCell().setBorder(0);
            table14.addCell(nullcell);
            table14.addCell(cell141);
            document.add(table14);

            //第15列
            PdfPTable table15 = new PdfPTable(4);
            PdfPCell cell151 = new PdfPCell();
            PdfPCell cell152 = new PdfPCell(new Paragraph(" Bank Name ", FontChinese8));
            PdfPCell cell153 = new PdfPCell(new Paragraph(" Commonwealth  Bank ", FontChinese8));
            cell151.setBorder(0);
            cell152.setBackgroundColor(BaseColor.GRAY);
            cell153.setBackgroundColor(BaseColor.GRAY);
            cell151.setFixedHeight(20);
            int width15[] = {2,10,30,58};
            table15.setWidths(width15);
            table15.getDefaultCell().setBorder(0);
            table15.addCell(cell151);
            table15.addCell(cell152);
            table15.addCell(cell153);
            table15.addCell(cell151);
            document.add(table15);

            //第16列
            PdfPTable table16 = new PdfPTable(4);
            PdfPCell cell161 = new PdfPCell(new Paragraph(" Account Name ", FontChinese8));
            PdfPCell cell162 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getAccountname(), FontChinese8));
            cell161.setFixedHeight(20);
            int width16[] = {2,10,30,58};
            table16.setWidths(width16);
            table16.getDefaultCell().setBorder(0);
            table16.addCell(nullcell);
            table16.addCell(cell161);
            table16.addCell(cell162);
            table16.addCell(nullcell);
            document.add(table16);


            //第17列
            PdfPTable table17 = new PdfPTable(4);
            PdfPCell cell171 = new PdfPCell(new Paragraph(" BSB ", FontChinese8));
            PdfPCell cell172 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getBsb(), FontChinese8));
            cell171.setFixedHeight(20);
            int width17[] = {2,10,30,58};
            table17.setWidths(width17);
            table17.getDefaultCell().setBorder(0);
            table17.addCell(nullcell);
            table17.addCell(cell171);
            table17.addCell(cell172);
            table17.addCell(nullcell);
            document.add(table17);

            //第18列
            PdfPTable table18 = new PdfPTable(4);
            PdfPCell cell181 = new PdfPCell(new Paragraph(" Account No ", FontChinese8));
            PdfPCell cell182 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getAccountno(), FontChinese8));
            cell181.setFixedHeight(20);
            int width18[] = {2,10,30,58};
            table18.setWidths(width18);
            table18.getDefaultCell().setBorder(0);
            table18.addCell(nullcell);
            table18.addCell(cell181);
            table18.addCell(cell182);
            table18.addCell(nullcell);
            document.add(table18);

            document.close();


        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Normal 留学专用模板
     */
    public static void createNorPdf(InvoiceSchoolDTO invoiceSchoolDTO, String pdfpath, String realPath, boolean canceled){
        try {

            Document document = new Document(PageSize.A4.rotate());
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdfpath));

            PdfPCell nullcell = new PdfPCell();
            nullcell.setBorder(0);
            document.open();

            if (canceled == true)
                watermark.onEndPage(writer,document);

            //第一列
            PdfPTable table1 = new PdfPTable(3);
            //String znzimagePath = "/data/uploads/adviser_img/1505966408725_logo.png";;
            //String znzimagePath = realPath + "img/znz.png";
            String znzimagePath = "/data/uploads/pdfimg/znz.png";
            Image znzlogo = Image.getInstance(URLDecoder.decode(znzimagePath, "utf-8"));
            int width1[] = {20,60,20};
            table1.setWidths(width1);
            table1.getDefaultCell().setBorder(0);
            table1.addCell(znzlogo);
            table1.addCell("");
            table1.addCell("");
            document.add(table1);

            //第二列
            PdfPTable table2 = new PdfPTable(3);
            //PdfPCell cell21 = new PdfPCell(new Paragraph("Company Title: ", FontChinese12));

            PdfPCell cell22 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getCompany(), FontChinese12));
            cell22.setFixedHeight(25);
            PdfPCell cell23 = new PdfPCell(new Paragraph("ABN: ",FontChinese10 ));
            PdfPCell cell24 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getAbn(), FontChinese10));
            //cell21.setVerticalAlignment(Element.ALIGN_MIDDLE); //中间
            //cell21.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell22.setVerticalAlignment(Element.ALIGN_MIDDLE);
            //cell22.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell23.setVerticalAlignment(Element.ALIGN_BOTTOM); //垂直底部
            cell23.setHorizontalAlignment(Element.ALIGN_CENTER);//水平中间
            cell24.setVerticalAlignment(Element.ALIGN_BOTTOM);
            //cell24.setHorizontalAlignment(Element.ALIGN_CENTER);
            //cell21.setBorder(0);
            cell22.setBorder(0);
            cell23.setBorder(0);
            cell24.setBorder(0);
            //设置每列宽度比例
            int width2[] = {70,5,25};
            table2.setWidths(width2);
            table2.getDefaultCell().setBorder(0);
            //table2.addCell(cell21);
            table2.addCell(cell22);
            table2.addCell(cell23);
            table2.addCell(cell24);
            document.add(table2);

            //第三列
            PdfPTable table3 = new PdfPTable(6);
            PdfPCell cell31 = new PdfPCell(new Paragraph("Address : ", FontChinese8));
            cell31.setFixedHeight(30);
            PdfPCell cell32 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getAddress(), fontbule8));
            PdfPCell cell33 = new PdfPCell(new Paragraph("Tel : ",FontChinese8 ));
            PdfPCell cell34 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getTel(), FontChinese8));
            PdfPCell cell35 = new PdfPCell(new Paragraph("E-mail : ",FontChinese8 ));
            PdfPCell cell36 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getEmail(), FontChinese8));
            cell31.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell31.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell32.setVerticalAlignment(Element.ALIGN_MIDDLE);
            //cell32.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell33.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell33.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell34.setVerticalAlignment(Element.ALIGN_MIDDLE);
            //cell34.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell35.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell35.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell36.setVerticalAlignment(Element.ALIGN_MIDDLE);
            //cell36.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell31.setBorder(0);
            cell32.setBorder(0);
            cell33.setBorder(0);
            cell34.setBorder(0);
            cell35.setBorder(0);
            cell36.setBorder(0);
            //设置每列宽度比例
            int width3[] = {7,28,5,30,7,23};
            table3.setWidths(width3);
            table3.getDefaultCell().setBorder(0);
            table3.addCell(cell31);
            table3.addCell(cell32);
            table3.addCell(cell33);
            table3.addCell(cell34);
            table3.addCell(cell35);
            table3.addCell(cell36);
            document.add(table3);

            //第四列
            PdfPTable table4 = new PdfPTable(1);
            PdfPCell cell4 = new PdfPCell(new Paragraph(" Tax Invoice ", FontChinese12));
            cell4.setFixedHeight(30);
            cell4.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell4.setHorizontalAlignment(Element.ALIGN_CENTER);
            table4.getDefaultCell().setBorder(0);
            table4.addCell(cell4);
            document.add(table4);

            //第五列
            PdfPTable table5 = new PdfPTable(5);
            PdfPCell cell51 = new PdfPCell(new Paragraph(" Bill to ", FontChinese10));
            PdfPCell cell52 = new PdfPCell(new Paragraph("Invoice No.: ", FontChinese10));
            PdfPCell cell53 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getInvoiceNo(), fontbule10));
            PdfPCell cell54 = new PdfPCell(new Paragraph("Invoice Date : ", FontChinese10));
            PdfPCell cell55 = new PdfPCell(new Paragraph(dobsdf.format(invoiceSchoolDTO.getInvoiceDate()), fontbule10));
            cell51.setFixedHeight(20);
            cell51.setBorder(0);
            cell52.setBorder(0);
            cell53.setBorder(0);
            cell54.setBorder(0);
            cell55.setBorder(0);
            int width5[] = {50,10,15,10,15};
            table5.setWidths(width5);
            table5.getDefaultCell().setBorder(0);
            table5.addCell(cell51);
            table5.addCell(cell52);
            table5.addCell(cell53);
            table5.addCell(cell54);
            table5.addCell(cell55);
            document.add(table5);

            //第六列
            PdfPTable table6 = new PdfPTable(2);
            PdfPCell cell61 = new PdfPCell(new Paragraph(" Company : ", FontChinese10));
            PdfPCell cell62 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getInvoiceBillToDO().getCompany(), fontbule10));
            cell61.setFixedHeight(20);
            cell61.setBorder(0);
            cell62.setBorder(0);
            int width6[] = {10,90};
            table6.setWidths(width6);
            table6.getDefaultCell().setBorder(0);
            table6.addCell(cell61);
            table6.addCell(cell62);
            document.add(table6);

            //第七列
            PdfPTable table7 = new PdfPTable(2);
            PdfPCell cell71 = new PdfPCell(new Paragraph(" ABN : ", FontChinese10));
            PdfPCell cell72 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getInvoiceBillToDO().getAbn(), fontbule10));
            cell71.setFixedHeight(20);
            cell71.setBorder(0);
            cell72.setBorder(0);
            int width7[] = {10,90};
            table7.setWidths(width7);
            table7.getDefaultCell().setBorder(0);
            table7.addCell(cell71);
            table7.addCell(cell72);
            document.add(table7);

            //第8列
            PdfPTable table8 = new PdfPTable(2);
            PdfPCell cell81 = new PdfPCell(new Paragraph(" Address : ", FontChinese10));
            PdfPCell cell82 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getInvoiceBillToDO().getAddress(), fontbule10));
            cell81.setFixedHeight(20);
            cell81.setBorder(0);
            cell82.setBorder(0);
            int width8[] = {10,90};
            table8.setWidths(width8);
            table8.getDefaultCell().setBorder(0);
            table8.addCell(cell81);
            table8.addCell(cell82);
            document.add(table8);

            //第9列
            PdfPTable table9 = new PdfPTable(1);
            PdfPCell cell91 = new PdfPCell(new Paragraph(" Description ", FontChinese11Bold));
            cell91.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell91.setFixedHeight(30);
            cell91.setBorder(0);
            int width9[] = {10};
            table9.setWidths(width9);
            table9.getDefaultCell().setBorder(0);
            table9.addCell(cell91);
            document.add(table9);

            //第10列
            PdfPTable table10 = new PdfPTable(12);
            List<InvoiceSchoolDescriptionDO> des = invoiceSchoolDTO.getInvoiceSchoolDescriptionDOS();
            PdfPCell cell10 ;
            int width10[] = {4,10,8,8,9,8,7,9,8,11,8,10};
            for (int i = -1; i < des.size() ; i++) {
                if (i == -1) {
                    cell10 = new PdfPCell(new Paragraph("NO.", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell10.setFixedHeight(15);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(" Student Name ", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(" DOB ", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(" Student ID ", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(" Course ", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(" Start Date ", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(" Instalment ", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(" Non Tuition Fee ", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(" Tuition Fee ", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(" Commission Rate ", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(" Commission ", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(" Bonus Amount ", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    table10.setHorizontalAlignment(Element.ALIGN_CENTER);
                } else {
                    cell10 = new PdfPCell(new Paragraph(des.get(i).getId()+"", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell10.setFixedHeight(10);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(des.get(i).getStudentname(), FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(dobsdf.format(des.get(i).getDob()), FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(des.get(i).getStudentId()+"", FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(des.get(i).getCourse(), FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(dobsdf.format(des.get(i).getStartDate()), FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(des.get(i).getInstalMent(), FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(des.get(i).getNonTuitionFee().toString(), FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(des.get(i).getTuitionFee().toString(), FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(des.get(i).getCommissionrate().toString(), FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(des.get(i).getCommission().toString(), FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    cell10 = new PdfPCell(new Paragraph(des.get(i).getBonus().toString(), FontChinese8));
                    cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table10.addCell(cell10);
                    table10.setHorizontalAlignment(Element.ALIGN_CENTER);
                }


            }

            table10.setWidths(width10);
            table8.getDefaultCell().setBorder(0);
            document.add(table10);

            //第11列
            PdfPTable table11 = new PdfPTable(2);
            PdfPCell cell111 = new PdfPCell(new Paragraph(" 10%GST : ", FontChinese11Bold));
            PdfPCell cell112 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getGst().toString(), fontbule10));
            cell111.setHorizontalAlignment(Element.ALIGN_RIGHT);//设置水平在右
            cell111.setVerticalAlignment(Element.ALIGN_BOTTOM);//设置垂直在下
            cell112.setVerticalAlignment(Element.ALIGN_BOTTOM);//设置垂直在下
            cell111.setBorder(0);
            cell112.setBorder(0);
            cell111.setFixedHeight(40);
            int width11[] = {80,20};
            table11.setWidths(width11);
            table11.getDefaultCell().setBorder(0);
            table11.addCell(cell111);
            table11.addCell(cell112);
            document.add(table11);

            //第12列
            PdfPTable table12 = new PdfPTable(2);
            PdfPCell cell121 = new PdfPCell(new Paragraph(" Invoice total with GST : ", FontChinese11Bold));
            PdfPCell cell122 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getTotalGST().toString(), fontbule10));
            cell121.setHorizontalAlignment(Element.ALIGN_RIGHT);//设置水平在右
            cell121.setBorder(0);
            cell122.setBorder(0);
            cell121.setFixedHeight(20);
            int width12[] = {80,20};
            table12.setWidths(width12);
            table12.getDefaultCell().setBorder(0);
            table12.addCell(cell121);
            table12.addCell(cell122);
            document.add(table12);

            //第13列
            PdfPTable table13 = new PdfPTable(2);
            PdfPCell cell131 = new PdfPCell(new Paragraph(" NOTE : ", FontChinese8Bold));
            String  str=invoiceSchoolDTO.getNote();
            PdfPCell cell132 = new PdfPCell(new Paragraph(str, fontbule10));
            cell131.setHorizontalAlignment(Element.ALIGN_CENTER);//设置水平在右
            cell131.setVerticalAlignment(Element.ALIGN_TOP);
            cell131.setBorder(0);
            cell131.setFixedHeight(40);
            int width13[] = {7,93};
            table13.setWidths(width13);
            table13.getDefaultCell().setBorder(0);
            table13.addCell(cell131);
            table13.addCell(cell132);
            document.add(table13);

            //第14列
            PdfPTable table14 = new PdfPTable(2);
            PdfPCell cell141 = new PdfPCell(new Paragraph(" Please make payment to the account below , and quote the invoice number : ", FontChinese11Bold));
            cell141.setBorder(0);
            cell141.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell141.setFixedHeight(30);
            int width14[] = {2,98};
            table14.setWidths(width14);
            table14.getDefaultCell().setBorder(0);
            table14.addCell(nullcell);
            table14.addCell(cell141);
            document.add(table14);

            //第15列
            PdfPTable table15 = new PdfPTable(4);
            PdfPCell cell151 = new PdfPCell();
            PdfPCell cell152 = new PdfPCell(new Paragraph(" Bank Name ", FontChinese8));
            PdfPCell cell153 = new PdfPCell(new Paragraph(" Commonwealth  Bank ", FontChinese8));
            cell151.setBorder(0);
            cell152.setBackgroundColor(BaseColor.GRAY);
            cell153.setBackgroundColor(BaseColor.GRAY);
            cell151.setFixedHeight(20);
            int width15[] = {2,10,30,58};
            table15.setWidths(width15);
            table15.getDefaultCell().setBorder(0);
            table15.addCell(cell151);
            table15.addCell(cell152);
            table15.addCell(cell153);
            table15.addCell(cell151);
            document.add(table15);

            //第16列
            PdfPTable table16 = new PdfPTable(4);
            PdfPCell cell161 = new PdfPCell(new Paragraph(" Account Name ", FontChinese8));
            PdfPCell cell162 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getAccountname(), FontChinese8));
            cell161.setFixedHeight(20);
            int width16[] = {2,10,30,58};
            table16.setWidths(width16);
            table16.getDefaultCell().setBorder(0);
            table16.addCell(nullcell);
            table16.addCell(cell161);
            table16.addCell(cell162);
            table16.addCell(nullcell);
            document.add(table16);


            //第17列
            PdfPTable table17 = new PdfPTable(4);
            PdfPCell cell171 = new PdfPCell(new Paragraph(" BSB ", FontChinese8));
            PdfPCell cell172 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getBsb(), FontChinese8));
            cell171.setFixedHeight(20);
            int width17[] = {2,10,30,58};
            table17.setWidths(width17);
            table17.getDefaultCell().setBorder(0);
            table17.addCell(nullcell);
            table17.addCell(cell171);
            table17.addCell(cell172);
            table17.addCell(nullcell);
            document.add(table17);

            //第18列
            PdfPTable table18 = new PdfPTable(4);
            PdfPCell cell181 = new PdfPCell(new Paragraph(" Account No ", FontChinese8));
            PdfPCell cell182 = new PdfPCell(new Paragraph(invoiceSchoolDTO.getAccountno(), FontChinese8));
            cell181.setFixedHeight(20);
            int width18[] = {2,10,30,58};
            table18.setWidths(width18);
            table18.getDefaultCell().setBorder(0);
            table18.addCell(nullcell);
            table18.addCell(cell181);
            table18.addCell(cell182);
            table18.addCell(nullcell);
            document.add(table18);

            document.close();


        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
