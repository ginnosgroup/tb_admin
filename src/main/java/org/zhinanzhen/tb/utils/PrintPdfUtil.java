package org.zhinanzhen.tb.utils;

import ch.qos.logback.core.util.FileUtil;
import com.alibaba.fastjson.JSON;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.hamcrest.Description;
import org.springframework.web.bind.annotation.RequestBody;
import org.zhinanzhen.b.dao.pojo.InvoiceSchoolDescriptionDO;
import org.zhinanzhen.b.dao.pojo.InvoiceServiceFeeDescriptionDO;
import org.zhinanzhen.b.service.pojo.InvoiceSchoolDTO;
import org.zhinanzhen.b.service.pojo.InvoiceServiceFeeDTO;
import org.zhinanzhen.tb.controller.Response;

import java.io.*;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
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

    public static String pdfout(Response response , String path) {
        // 模板路径
        String templatePath = path ;      //"servicefee.pdf";
        // 生成的新文件路径
        String newPDFPath = "E:mytest"+path;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

        PdfReader reader;
        FileOutputStream out;
        ByteArrayOutputStream bos;
        PdfStamper stamper;
        try {
            BaseFont bf = BaseFont.createFont("C:/WINDOWS/Fonts/SIMYOU.TTF", BaseFont.IDENTITY_H,BaseFont.NOT_EMBEDDED);
            reader = new PdfReader(templatePath);// 读取pdf模板
            out = new FileOutputStream(newPDFPath);// 输出流
            bos = new ByteArrayOutputStream();
            stamper = new PdfStamper(reader, bos);

            AcroFields form = stamper.getAcroFields();
            form.addSubstitutionFont(bf);
            // 设置字体



            //文字类的内容处理



            if (path.equals("servicefee.pdf")){
                //InvoiceServiceFeeDTO invoiceServiceFeeDTO = JSON.parseObject(JSON.toJSONString(map),InvoiceServiceFeeDTO.class);
                InvoiceServiceFeeDTO invoiceServiceFeeDTO = (InvoiceServiceFeeDTO) response.getData();
                Map<Object,Object> map = JSON.parseObject(JSON.toJSONString(invoiceServiceFeeDTO),Map.class);
                System.out.println(map.toString());
                for(Object key : map.keySet()){
                    form.setField(key.toString(),map.get(key).toString());
                    if (key.equals("invoiceDate")){
                        form.setField(key.toString(),sdf.format(invoiceServiceFeeDTO.getInvoiceDate()));
                    }
                }
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
            }
            if (path.equals("IES.pdf")){
                Map map =new HashMap();
                InvoiceSchoolDTO invoiceSchoolDTO = JSON.parseObject(JSON.toJSONString(map),InvoiceSchoolDTO.class);
                List<InvoiceSchoolDescriptionDO> invoiceSchoolDescriptionDOS = invoiceSchoolDTO.getInvoiceSchoolDescriptionDOS();
                int index = 1 ;
                for(InvoiceSchoolDescriptionDO descriptionDO : invoiceSchoolDescriptionDOS){
                    form.setField("id"+index,descriptionDO.getId()+"");
                    form.setField("studentname"+index,descriptionDO.getStudentname());
                    form.setField("dob"+index,sdf.format(descriptionDO.getDob()));
                    form.setField("studentId"+index,descriptionDO.getStudentId()+"");
                    form.setField("course"+index,descriptionDO.getCourse());
                    form.setField("startDate"+index,sdf.format(descriptionDO.getStartDate()));
                    form.setField("tuitionFee"+index,descriptionDO.getTuitionFee().toString());
                    form.setField("commissionrate"+index,descriptionDO.getCommissionrate().toString());
                    form.setField("commission"+index,descriptionDO.getCommission().toString());
                    form.setField("bonus"+index,descriptionDO.getBonus().toString());
                    form.setField("instalMent"+index,descriptionDO.getInstalMent());
                }
            }




            stamper.setFormFlattening(true);// 如果为false，生成的PDF文件可以编辑，如果为true，生成的PDF文件不可以编辑
            stamper.close();

            Document doc = new Document();
            Font font = new Font(bf, 4);
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
