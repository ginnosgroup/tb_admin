package org.zhinanzhen.tb.utils;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.XfaForm;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class PdfGenerateUtil {
//    public static final String SRC = "C:/Users/yjt/Desktop/test/test.pdf";
//    public static final String dest = "C:/Users/yjt/Desktop/test/data/data.xml";
//    public static final String XML2 = "C:/Users/yjt/Desktop/test/data/test.xml";
    public static int manipulatePdf(String src,String xml,int id)
            throws IOException, DocumentException {
        String pdf =id+".pdf";
        String path = "/data/uploads/PdfGenerate/pdfout/"+pdf;
        PdfReader reader = new PdfReader(src);
        PdfStamper stamper = new PdfStamper(reader,
                new FileOutputStream(path));
        AcroFields form = stamper.getAcroFields();
        XfaForm xfa = form.getXfa();
        xfa.fillXfaForm(new FileInputStream(xml));
        stamper.close();
        reader.close();
        return 1;
    }
    public  void readXml(String src, String dest)
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
//    //test
//    public static void fillXml(String dest) throws IOException, SAXException, TransformerException, ParserConfigurationException {
//        DocumentBuilder newDocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//        //②获取一个与磁盘文件关联的非空Document对象
//        Document doc = newDocumentBuilder.parse(dest);
//        //③通过文档对象获得该文档对象的根节点
//        Element root = doc.getDocumentElement();
//        Node paPaDetails = root.getElementsByTagName("PASibDetails").item(0).cloneNode(true);
//        root.getElementsByTagName("Sibling").item(0).getChildNodes().item(3).appendChild(paPaDetails);
//
//        root.getElementsByTagName("PASibDetails").item(1).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent("testname");
//
//        Transformer transformer = TransformerFactory.newInstance().newTransformer();
//        //DOMSource source = new DOMSource(doc);
//        Source source = new DOMSource(doc);
//        //StreamResult result = new StreamResult();
//        Result result = new StreamResult(XML2);
//        transformer.transform(source, result);//将 XML==>Source 转换为 Result
//    }
}
