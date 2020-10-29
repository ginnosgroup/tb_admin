package org.zhinanzhen.tb.utils;

import ch.qos.logback.core.util.FileUtil;
import com.alibaba.fastjson.JSON;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.zhinanzhen.b.dao.pojo.InvoiceServiceFeeDescriptionDO;
import org.zhinanzhen.b.service.pojo.InvoiceServiceFeeDTO;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: bsz
 * Date: 2020/10/29 9:16
 * Description:
 * Version: V1.0
 */
public class PrintPdfUtil {

    public static String pdfout(Map map ,String path) {
        // 模板路径
        String templatePath = path ;      //"servicefee.pdf";
        // 生成的新文件路径
        String newPDFPath = "E:mytest.pdf";

        PdfReader reader;
        FileOutputStream out;
        ByteArrayOutputStream bos;
        PdfStamper stamper;
        try {
            BaseFont bf = BaseFont.createFont("c://windows//fonts//simsun.ttc,1" , BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font FontChinese = new Font(bf, 4, Font.NORMAL);
            reader = new PdfReader(templatePath);// 读取pdf模板
            out = new FileOutputStream(newPDFPath);// 输出流

            bos = new ByteArrayOutputStream();
            stamper = new PdfStamper(reader, bos);

            AcroFields form = stamper.getAcroFields();



            // 设置字体
            //BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            //com.itextpdf.text.Font thFont = new com.itextpdf.text.Font(bfChinese, 22, com.itextpdf.text.Font.BOLD);
            //com.itextpdf.text.Font nomalFont = new com.itextpdf.text.Font(bfChinese, 20, com.itextpdf.text.Font.NORMAL);



            //文字类的内容处理
            form.addSubstitutionFont(bf);
            for(Object key : map.keySet()){
                Object value = map.get(key);
                form.setField(key.toString(),value.toString());

            }
            InvoiceServiceFeeDTO invoiceServiceFeeDTO = JSON.parseObject(JSON.toJSONString(map),InvoiceServiceFeeDTO.class);
            List<InvoiceServiceFeeDescriptionDO> invoiceServiceFeeDescriptionDOList = invoiceServiceFeeDTO.getInvoiceServiceFeeDescriptionDOList();
            int index = 1 ;
            for(InvoiceServiceFeeDescriptionDO  description :  invoiceServiceFeeDescriptionDOList){
                form.setField("id"+index,description.getId()+"");
                form.setField("description"+index,description.getDescription());
                form.setField("unitPrice"+index,description.getUnitPrice().toString());
                form.setField("quantity"+index,description.getQuantity()+"");
                form.setField("amount"+index,description.getAmount().toString());
                index ++ ;
            }



            stamper.setFormFlattening(true);// 如果为false，生成的PDF文件可以编辑，如果为true，生成的PDF文件不可以编辑
            stamper.close();

            Document doc = new Document();
            Font font = new Font(bf, 10);
            PdfCopy copy = new PdfCopy(doc, out);
            doc.open();
            PdfImportedPage importPage = copy.getImportedPage(new PdfReader(bos.toByteArray()), 1);
            copy.addPage(importPage);
            doc.close();



        } catch (IOException e) {
            System.out.println(e);
        } catch (DocumentException e) {
            System.out.println(e);
        }

        return "yes";

    }
}
