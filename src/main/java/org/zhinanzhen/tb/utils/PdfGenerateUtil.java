package org.zhinanzhen.tb.utils;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.XfaForm;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class PdfGenerateUtil {
    public static final String SRC = "C:/Users/yjt/Desktop/pdftest/nulltest.pdf";
    public static final String XML = "C:/Users/yjt/Desktop/pdftest/data1.xml";
    public static final String XML2 = "C:/Users/yjt/Desktop/pdftest/data2.xml";
    public static final String DEST = "C:/Users/yjt/Desktop/pdfout/completed.pdf";

    public static void main(String[] args) throws IOException, DocumentException, TransformerException, SAXException, ParserConfigurationException {
        //readXfa(SRC, XML);
      new PdfGenerateUtil().writeXml(XML,XML2);
       //new PdfGenerateUtil().readXml(SRC,XML);
    }




    public static void readXfa(String src, String dest)
            throws IOException, ParserConfigurationException, SAXException,
            TransformerFactoryConfigurationError, TransformerException {
        PdfReader reader = new PdfReader(src);
        XfaForm xfa = new XfaForm(reader);
        Document doc = xfa.getDomDocument();

        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty(OutputKeys.INDENT, "yes");

        FileOutputStream os = new FileOutputStream(dest);
        tf.transform(new DOMSource(doc), new StreamResult(os));

        reader.close();
    }

    public static int manipulatePdf(String src, String dest, String xml)
            throws IOException, DocumentException {
        PdfReader reader = new PdfReader(src);
        PdfStamper stamper = new PdfStamper(reader,
                new FileOutputStream(dest));
        AcroFields form = stamper.getAcroFields();
        XfaForm xfa = form.getXfa();
        xfa.fillXfaForm(new FileInputStream(xml));
        stamper.close();
        reader.close();
        return 1;
    }
    public void readXml(String src, String dest)
            throws IOException, DocumentException, TransformerException {
        PdfReader reader = new PdfReader(src);
        AcroFields form = reader.getAcroFields();
        XfaForm xfa = form.getXfa();
        Node node = xfa.getDatasetsNode();
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            if("data".equals(list.item(i).getLocalName())) {
                node = list.item(i);
                break;
            }
        }
        list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            if("movies".equals(list.item(i).getLocalName())) {
                node = list.item(i);
                break;
            }
        }
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        FileOutputStream os = new FileOutputStream(dest);
        tf.transform(new DOMSource(node), new StreamResult(os));
        reader.close();
    }
    public void writeXml(String src, String dest) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        //①获得解析器DocumentBuilder的工厂实例DocumentBuilderFactory  然后拿到DocumentBuilder对象
        DocumentBuilder newDocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        //②获取一个与磁盘文件关联的非空Document对象
        Document doc = newDocumentBuilder.parse(XML);
        //③通过文档对象获得该文档对象的根节点
        Element root = doc.getDocumentElement();
        root.getElementsByTagName("FamilyName").item(0).setTextContent("bbc");
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        //DOMSource source = new DOMSource(doc);
        Source source = new DOMSource(doc);
        //StreamResult result = new StreamResult();
        Result result = new StreamResult(XML2);
        transformer.transform(source, result);//将 XML==>Source 转换为 Result

    }
}
