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

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Time;
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

    public static String pdfout(String invoiceNo ,Response response , String pdfModel , String realPath ) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat sdftodocument = new SimpleDateFormat("yyyyMM");
        //得到static之后的路径
        String path = "/data/upload/pdf/"+sdftodocument.format(Calendar.getInstance().getTime());
        realPath = realPath + path;
        // 模板路径
        String templatePath = pdfModel;      //"servicefee.pdf";

        try {
            //去掉空格
            realPath = URLDecoder.decode(realPath, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String newPDFPath = realPath.replace('/', '\\').substring(1, realPath.length());

        if (pdfModel.equals("servicefee.pdf")){
            invoiceNo = invoiceNo + ".pdf";
        }
        if (pdfModel.equals("IES.pdf")){
            invoiceNo = invoiceNo + ".pdf";
        }
        newPDFPath = newPDFPath +File.separator+ invoiceNo;

        //创建根目录
        File file = new File(realPath, invoiceNo);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }

        System.out.println("newPDFPath  "+newPDFPath);//打印路径是不是正确的


        PdfReader reader;
        FileOutputStream out;
        ByteArrayOutputStream bos;
        PdfStamper stamper;
        try {
            //BaseFont bf = BaseFont.createFont("C:/WINDOWS/Fonts/SIMYOU.TTF", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
            BaseFont bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H",BaseFont.NOT_EMBEDDED);
            reader = new PdfReader(templatePath);// 读取pdf模板
            out = new FileOutputStream(newPDFPath);// 输出流
            bos = new ByteArrayOutputStream();
            stamper = new PdfStamper(reader, bos);

            AcroFields form = stamper.getAcroFields();
            form.addSubstitutionFont(bf);

            if (pdfModel.equals("servicefee.pdf")) {
                InvoiceServiceFeeDTO invoiceServiceFeeDTO = (InvoiceServiceFeeDTO) response.getData();
                Map<Object, Object> map = JSON.parseObject(JSON.toJSONString(invoiceServiceFeeDTO), Map.class);
                for (Object key : map.keySet()) {
                    form.setField(key.toString(), map.get(key).toString());
                    if (key.equals("invoiceDate")) {
                        form.setField(key.toString(), sdf.format(invoiceServiceFeeDTO.getInvoiceDate()));
                    }
                }
                List<InvoiceServiceFeeDescriptionDO> invoiceServiceFeeDescriptionDOList = invoiceServiceFeeDTO.getInvoiceServiceFeeDescriptionDOList();
                int index = 1;
                for (InvoiceServiceFeeDescriptionDO description : invoiceServiceFeeDescriptionDOList) {
                    form.setField("id" + index, description.getId() + "");
                    form.setField("description" + index, description.getDescription());
                    form.setField("unitPrice" + index, description.getUnitPrice().toString());
                    form.setField("quantity" + index, description.getQuantity() + "");
                    form.setField("amount" + index, description.getAmount().toString());
                    index++;
                }
            }
            if (pdfModel.equals("IES.pdf")) {
                //InvoiceSchoolDTO invoiceSchoolDTO = JSON.parseObject(JSON.toJSONString(map), InvoiceSchoolDTO.class);
                InvoiceSchoolDTO invoiceSchoolDTO = (InvoiceSchoolDTO) response.getData();
                System.out.println(invoiceSchoolDTO.toString());
                Map<Object, Object> map = JSON.parseObject(JSON.toJSONString(invoiceSchoolDTO), Map.class);
                for (Object key : map.keySet()) {
                    form.setField(key.toString(), map.get(key).toString());
                    if (key.equals("invoiceDate")) {
                        form.setField(key.toString(), sdf.format(invoiceSchoolDTO.getInvoiceDate()));
                    }
                    if (key.equals("invoiceDate")) {
                        form.setField(key.toString(), sdf.format(invoiceSchoolDTO.getInvoiceDate()));
                    }
                }
                form.setField("billCompany",invoiceSchoolDTO.getInvoiceBillToDO().getCompany());
                form.setField("billAbn",invoiceSchoolDTO.getInvoiceBillToDO().getAbn());
                form.setField("billAddress",invoiceSchoolDTO.getInvoiceBillToDO().getAddress());
                List<InvoiceSchoolDescriptionDO> invoiceSchoolDescriptionDOS = invoiceSchoolDTO.getInvoiceSchoolDescriptionDOS();
                int index = 1;
                for (InvoiceSchoolDescriptionDO descriptionDO : invoiceSchoolDescriptionDOS) {
                    form.setField("id" + index, descriptionDO.getId() + "");
                    form.setField("studentname" + index, descriptionDO.getStudentname());
                    form.setField("dob" + index, sdf.format(descriptionDO.getDob()));
                    form.setField("studentId" + index, descriptionDO.getStudentId() + "");
                    form.setField("course" + index, descriptionDO.getCourse());
                    form.setField("startDate" + index, sdf.format(descriptionDO.getStartDate()));
                    form.setField("tuitionFee" + index, descriptionDO.getTuitionFee().toString());
                    form.setField("commissionrate" + index, descriptionDO.getCommissionrate().toString());
                    form.setField("commission" + index, descriptionDO.getCommission().toString());
                    form.setField("bonus" + index, descriptionDO.getBonus().toString());
                    form.setField("instalMent" + index, descriptionDO.getInstalMent());
                    index ++ ;
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
            return "系统错误！";
        } catch (DocumentException e) {
            System.out.println(e);
            return "系统错误！";
        }

        return path+"/"+invoiceNo;

    }
}
