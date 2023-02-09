package org.zhinanzhen.b.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ikasoa.core.ErrorCodeEnum;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.zhinanzhen.b.dao.CustomerInformationDAO;
import org.zhinanzhen.b.dao.pojo.customer.*;
import org.zhinanzhen.b.service.PdfGenerateService;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;
import org.zhinanzhen.tb.utils.PdfGenerateUtil;
import org.zhinanzhen.tb.utils.WebDavUtils;

import javax.annotation.Resource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service("PdfGenerateService")
public class PdfGenerateServiceImpl extends BaseService implements PdfGenerateService {
    public static final String XML1 = "/data/uploads/PdfGenerate/pdfxml/data.xml";
    public static final String XML2 = "/data/uploads/PdfGenerate/xmlout/data.xml";
    public static final String SRC = "/data/uploads/PdfGenerate/pdf/test.pdf";

    @Resource
    private CustomerInformationDAO customerInformationDAO;


    @Override
    public int generate(int id) throws ServiceException {
        try {
            CustomerInformationDO customerInformationDO = customerInformationDAO.getByServiceOrderId(id);
            fillxml(customerInformationDO);
            if (PdfGenerateUtil.manipulatePdf(SRC, XML2, id) > 0){
                webdav(id);
                return id;
            }
            else
                return 0;
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }

    private void webdav(int id) throws IOException {
        CustomerInformationDO customerInformationDO = customerInformationDAO.getByServiceOrderId(id);
        String givenName = customerInformationDO.getMainInformation().getGivenName();
        String familyName = customerInformationDO.getMainInformation().getFamilyName();
        LocalDate date = LocalDate.now(); // get the current date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String formatdate = date.format(formatter);
        String netDiskPath = "https://dav.jianguoyun.com/dav/MMpdf/"+givenName+"_"+familyName+"_"+formatdate+".pdf";
        String filePath="/data/uploads/PdfGenerate/pdfout/"+id+".pdf";
        WebDavUtils.upload(netDiskPath,filePath);
    }



    private void fillxml(CustomerInformationDO customerInformationDO) throws ServiceException, ParserConfigurationException, IOException, SAXException, TransformerException {
        DocumentBuilder newDocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        //②获取一个与磁盘文件关联的非空Document对象
        Document doc = newDocumentBuilder.parse(XML1);
        //③通过文档对象获得该文档对象的根节点
        Element root = doc.getDocumentElement();
        //MAIN APPLICANT'S DETAILS
        //Prefix/Title
        root.getElementsByTagName("ApDetails").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).setTextContent(customerInformationDO.getMainInformation().getPrefixTitle());
        //Gender
        root.getElementsByTagName("ApDetails").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).setTextContent(customerInformationDO.getMainInformation().getGender());
        //Family Name
        root.getElementsByTagName("ApDetails").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(5).setTextContent(customerInformationDO.getMainInformation().getFamilyName());
        //Given Names
        root.getElementsByTagName("ApDetails").item(0).getChildNodes().item(3).getChildNodes().item(5).getChildNodes().item(1).setTextContent(customerInformationDO.getMainInformation().getGivenName());
        //Preferred Names
        root.getElementsByTagName("ApDetails").item(0).getChildNodes().item(3).getChildNodes().item(5).getChildNodes().item(3).setTextContent(customerInformationDO.getMainInformation().getPreferredNames());
        //Date of Birth
        root.getElementsByTagName("ApDetails").item(0).getChildNodes().item(5).getChildNodes().item(3).getChildNodes().item(1).setTextContent(customerInformationDO.getMainInformation().getDateOfBirth());
        //Birth Country
        root.getElementsByTagName("ApDetails").item(0).getChildNodes().item(5).getChildNodes().item(3).getChildNodes().item(3).setTextContent(customerInformationDO.getMainInformation().getBirthCountry());
        //Birth Location
        root.getElementsByTagName("ApDetails").item(0).getChildNodes().item(5).getChildNodes().item(5).getChildNodes().item(1).setTextContent(customerInformationDO.getMainInformation().getBirthLocation().split("#")[0]);
        root.getElementsByTagName("ApDetails").item(0).getChildNodes().item(5).getChildNodes().item(5).getChildNodes().item(3).setTextContent(customerInformationDO.getMainInformation().getBirthLocation().split("#")[1]);
        //State/Province
        root.getElementsByTagName("ApDetails").item(0).getChildNodes().item(5).getChildNodes().item(5).getChildNodes().item(5).setTextContent(customerInformationDO.getMainInformation().getStateOrProvince());
        //Marital Status
        root.getElementsByTagName("MaritalStatus").item(0).setTextContent(customerInformationDO.getMainInformation().getMaritalStatus());
        //OtherNameTable
        //Question
        if (customerInformationDO.getPastInformationList() == null)
            root.getElementsByTagName("OtherName").item(0).getChildNodes().item(1).getChildNodes().item(1).setTextContent("2");
        else {
            root.getElementsByTagName("OtherName").item(0).getChildNodes().item(1).getChildNodes().item(1).setTextContent("1");
            //Other Family Name
            root.getElementsByTagName("OtherNameTable").item(0).getChildNodes().item(3).getChildNodes().item(1).setTextContent(customerInformationDO.getPastInformationList().get(0).getFamilyName());
            //Other Given Name
            root.getElementsByTagName("OtherNameTable").item(0).getChildNodes().item(3).getChildNodes().item(3).setTextContent(customerInformationDO.getPastInformationList().get(0).getGivenNames());
            //dateOfBirth
            root.getElementsByTagName("OtherNameTable").item(0).getChildNodes().item(3).getChildNodes().item(5).setTextContent(customerInformationDO.getPastInformationList().get(0).getDateOfBirth());
            //usedTypeOfName
            root.getElementsByTagName("OtherNameTable").item(0).getChildNodes().item(3).getChildNodes().item(7).setTextContent(customerInformationDO.getPastInformationList().get(0).getUsedTypeOfName());
        }

        //Chinese Question yes2 no 1
        //Chinese Commercial Code
        if (customerInformationDO.getChineseCommercialCode() == null)
            root.getElementsByTagName("Chinese").item(0).getChildNodes().item(1).getChildNodes().item(1).setTextContent("1");
        else {
            root.getElementsByTagName("Chinese").item(0).getChildNodes().item(1).getChildNodes().item(1).setTextContent("2");
            root.getElementsByTagName("Chinese").item(0).getChildNodes().item(3).getChildNodes().item(5).getChildNodes().item(1).setTextContent(customerInformationDO.getChineseCommercialCode());
        }
// ArabicRussian yes1 no 2
//        root.getElementsByTagName("ArabicRussian").item(0).getChildNodes().item(1).getChildNodes().item(1).setTextContent("2");
        //Current Contact Details
        //Country Code After Hours
        root.getElementsByTagName("ApplicantHomePhoneCountry").item(0).setTextContent(customerInformationDO.getContactDetails().getCountryCodeAfter());
        //AreaCode
        root.getElementsByTagName("ApplicantHomePhoneAreaCode").item(0).setTextContent(customerInformationDO.getContactDetails().getAreaCodeAfter());
        //Number
        root.getElementsByTagName("ApplicantHomePhoneNumber").item(0).setTextContent(customerInformationDO.getContactDetails().getNumberAfter());
        //CountryCodeOffice
        root.getElementsByTagName("ApplicantWorkPhoneCountry").item(0).setTextContent(customerInformationDO.getContactDetails().getCountryCodeOffice());
        //AreaCodeAfterOffice
        root.getElementsByTagName("ApplicantWorkPhoneAreaCode").item(0).setTextContent(customerInformationDO.getContactDetails().getAreaCodeAfterOffice());
        //NumberAfterOffice
        root.getElementsByTagName("ApplicantWorkPhoneNumber").item(0).setTextContent(customerInformationDO.getContactDetails().getNumberAfterOffice());
        //MobileOrCellCountryCode
        root.getElementsByTagName("MobileCountryCode").item(0).setTextContent(customerInformationDO.getContactDetails().getMobileOrCellCountryCode());
        //MobileOrCellCountryNumber
        root.getElementsByTagName("ApplicantMobile").item(0).setTextContent(customerInformationDO.getContactDetails().getMobileOrCellCountryNumber());
        //Email
        root.getElementsByTagName("ApplicantEmail").item(0).setTextContent(customerInformationDO.getContactDetails().getEmail());
        //todo
        //IsAnyCountry
        CitizenshipDetails citizenshipDetails = customerInformationDO.getCitizenshipDetails();
        List<Citizenship> citizenshipList = citizenshipDetails.getCitizenshipList();
        if (citizenshipDetails.getIsAnyCountry() == 0) {
            root.getElementsByTagName("CitizenshipRadio").item(0).setTextContent("2");
            //reason
            root.getElementsByTagName("StatelessReasons").item(0).setTextContent(citizenshipDetails.getNotAnyCountryInformation());
        } else {
            root.getElementsByTagName("CitizenshipRadio").item(0).setTextContent("1");
            //Country of Citizenship
            root.getElementsByTagName("ApCitizen").item(0).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(5).getChildNodes().item(3).getChildNodes().item(1).setTextContent(citizenshipList.get(0).getCountryOfCitizenship());
            //ObtainedHow
            root.getElementsByTagName("ObtainedHow").item(0).setTextContent(citizenshipList.get(0).getObtained());
            //obtainedDate
            root.getElementsByTagName("IssueDate").item(0).setTextContent(citizenshipList.get(0).getObtainedDate());
            //isHold
            if (citizenshipList.get(0).getIsHold() == 1) {
                root.getElementsByTagName("CeasedQuestion").item(0).getChildNodes().item(1).setTextContent("1");
            } else {
                root.getElementsByTagName("CeasedQuestion").item(0).getChildNodes().item(1).setTextContent("2");
                //ceasedDate
                root.getElementsByTagName("DateCeased").item(0).setTextContent(citizenshipList.get(0).getCeasedDate());
                //ceaseReason
                root.getElementsByTagName("CessationReason").item(0).setTextContent(citizenshipList.get(0).getCeaseReason());
            }
            //isHeldPassport
            if (citizenshipList.get(0).getIsHeldPassport() == 0) {
                root.getElementsByTagName("PassportQuestion").item(0).getChildNodes().item(3).getChildNodes().item(1).setTextContent("2");
            } else {
                root.getElementsByTagName("PassportQuestion").item(0).getChildNodes().item(3).getChildNodes().item(1).setTextContent("1");
                //PassportsOrTravelDocuments
                //number
                root.getElementsByTagName("PassportNumber").item(0).setTextContent(citizenshipList.get(0).getList().get(0).getNumber());
                //isShown
                root.getElementsByTagName("NameShownQ").item(0).setTextContent(String.valueOf(citizenshipList.get(0).getList().get(0).getIsShown()));
                //familyName
                root.getElementsByTagName("FamilyNameTxt").item(0).setTextContent(citizenshipList.get(0).getList().get(0).getFamilyName());
                //givenName
                root.getElementsByTagName("GivenNameTxt").item(0).setTextContent(citizenshipList.get(0).getList().get(0).getGivenName());
                //isListedOn
                root.getElementsByTagName("Country").item(2).setTextContent(citizenshipList.get(0).getList().get(0).getIsListedOn());
                //issueDate
                root.getElementsByTagName("PassportIssueDate").item(0).setTextContent(citizenshipList.get(0).getList().get(0).getIssueDate());
                //expiryDate
                root.getElementsByTagName("PassportExpiryDate").item(0).setTextContent(citizenshipList.get(0).getList().get(0).getExpiryDate());
                //Place
                root.getElementsByTagName("PassportIssuingAuthority").item(0).setTextContent(citizenshipList.get(0).getList().get(0).getPlace());
                //Status
                root.getElementsByTagName("DocStatus").item(0).setTextContent(citizenshipList.get(0).getList().get(0).getStatus());

            }


        }
        //IdentificationNumber
        ObjectMapper objectMapper = new ObjectMapper();
        List<IdentificationNumber> identificationNumberList = customerInformationDO.getIdentificationNumberList();
        List<IdentificationNumber> identificationNumberList1 = objectMapper.convertValue(identificationNumberList, new TypeReference<List<IdentificationNumber>>() {
        });
        //Question
        if (identificationNumberList == null)
            root.getElementsByTagName("ID").item(0).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent("2");
        else {
            root.getElementsByTagName("ID").item(0).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent("1");
            //type
            root.getElementsByTagName("TypeOfDoc").item(0).setTextContent(identificationNumberList1.get(0).getType());
            //identificationNumber
            root.getElementsByTagName("DocNumber").item(0).setTextContent(identificationNumberList1.get(0).getIdentificationNumber());
            //isFullName
            root.getElementsByTagName("NameShownQ").item(1).setTextContent(String.valueOf(identificationNumberList1.get(0).getIsFullName()));
            //familyName
            root.getElementsByTagName("FamilyNameTxt").item(1).setTextContent(identificationNumberList1.get(0).getFamilyName());
            //givenName
            root.getElementsByTagName("GivenNameTxt").item(1).setTextContent(identificationNumberList1.get(0).getGivenName());
            //issueDate
            root.getElementsByTagName("IDIssueDate").item(0).setTextContent(identificationNumberList1.get(0).getIssueDate());
            //expiryDate
            root.getElementsByTagName("IDExpiryDate").item(0).setTextContent(identificationNumberList1.get(0).getExpiryDate());
            //CountryOfIssue
            root.getElementsByTagName("Country").item(3).setTextContent(identificationNumberList1.get(0).getCountryOfIssue());
        }
        //Education
        //StudentQuestion


        if (customerInformationDO.getEducationList() == null) {
            root.getElementsByTagName("StudentQuestion").item(0).getChildNodes().item(3).setTextContent("2");
        } else {
            Education firstEducation = customerInformationDO.getEducationList().get(0);
            root.getElementsByTagName("StudentQuestion").item(0).getChildNodes().item(3).setTextContent("1");
            //datesFrom
            root.getElementsByTagName("DateFrom").item(1).setTextContent(firstEducation.getDatesFrom());
            //datesTo
            root.getElementsByTagName("DateTo").item(1).setTextContent(firstEducation.getDatesTo());
            //type
            root.getElementsByTagName("QualificationType").item(0).setTextContent(firstEducation.getType());
            //courseName
            root.getElementsByTagName("CourseName").item(0).setTextContent(firstEducation.getCourseName());
            //name
            root.getElementsByTagName("Institution").item(0).setTextContent(firstEducation.getName());
            //street
            root.getElementsByTagName("StreetAddress").item(1).setTextContent(firstEducation.getStreet());
            //city
            root.getElementsByTagName("Suburb").item(3).setTextContent(firstEducation.getCity());
            //state
            root.getElementsByTagName("State").item(3).setTextContent(firstEducation.getState());
            //postCode
            root.getElementsByTagName("PostCode").item(1).setTextContent(firstEducation.getPostCode());
            //country
            root.getElementsByTagName("Country").item(6).setTextContent(firstEducation.getCountry());
            //courseStatus
            root.getElementsByTagName("Status").item(2).setTextContent(firstEducation.getCourseStatus());
            //language
            root.getElementsByTagName("Language").item(0).setTextContent(firstEducation.getLanguage());
        }

        //LanguageSkills
        ObjectMapper objectMapper1 = new ObjectMapper();
        List<LanguageSkills> languageSkills = customerInformationDO.getLanguageSkills();
        List<LanguageSkills> languageSkills1 = objectMapper1.convertValue(languageSkills, new TypeReference<List<LanguageSkills>>() {
        });
        List<EnglishSkills> englishSkillsList = languageSkills1.get(0).getEnglishSkillsList();

        //isProficiencyEnglish
        if (languageSkills1.get(0).getIsProficiencyEnglish() == 0) {
            root.getElementsByTagName("YesNo").item(13).setTextContent("2");
        } else {

            root.getElementsByTagName("YesNo").item(13).setTextContent("1");
            //englishSkillsList
            EnglishSkills firstEnglishSkills = englishSkillsList.get(0);
            //type
            root.getElementsByTagName("TypeOfDoc").item(1).setTextContent(firstEnglishSkills.getType());
            //testDate
            root.getElementsByTagName("IssueDate").item(5).setTextContent(firstEnglishSkills.getTestDate());
            //location
            root.getElementsByTagName("Country").item(8).setTextContent(firstEnglishSkills.getLocation());
            //referenceNumber
            root.getElementsByTagName("ReferenceNumber").item(0).setTextContent(firstEnglishSkills.getReferenceNumber());
            //listeningScore
            root.getElementsByTagName("Listening").item(0).setTextContent(firstEnglishSkills.getListeningScore());
            //readingScore
            root.getElementsByTagName("Reading").item(0).setTextContent(firstEnglishSkills.getReadingScore());
            //writingScore
            root.getElementsByTagName("Writting").item(0).setTextContent(firstEnglishSkills.getWritingScore());
            //speakingScore
            root.getElementsByTagName("Speaking").item(0).setTextContent(firstEnglishSkills.getSpeakingScore());
            //overallScoreOrGrade
            root.getElementsByTagName("Overall").item(0).setTextContent(firstEnglishSkills.getOverallScoreOrGrade());
        }

        //AllLanguages
        //isFirstLanguage
        if (languageSkills1.get(0).getIsFirstLanguage() == 0) {
            root.getElementsByTagName("MainLangQuestion").item(0).getChildNodes().item(1).setTextContent("2");
        } else {
            root.getElementsByTagName("MainLangQuestion").item(0).getChildNodes().item(1).setTextContent("1");
        }
        List<AllLanguages> allLanguagesList = languageSkills1.get(0).getAllLanguagesList();
        //todo 列表
        int size = languageSkills1.get(0).getAllLanguagesList().size();
        for (int i = 0; i < size; i++) {
            //Language
            root.getElementsByTagName("Language").item(3).setTextContent(allLanguagesList.get(i).getLanguage());
            //LevelOfProficiency
            root.getElementsByTagName("Proficiency").item(0).setTextContent(allLanguagesList.get(i).getLevelOfProficiency());
            //isMainLanguages
            root.getElementsByTagName("Main").item(0).setTextContent(allLanguagesList.get(i).getIsMainLanguages());
        }

        //Addresses
        //isAll
        if (customerInformationDO.getAddresses().getIsAll() == 1) {
            root.getElementsByTagName("YesNoQ").item(40).setTextContent("1");
        } else {
            root.getElementsByTagName("YesNoQ").item(40).setTextContent("2");
        }
        //currentAddressList
        //todo
        List<CurrentAddress> currentAddressList = customerInformationDO.getAddresses().getCurrentAddressList();
        int addressSize = currentAddressList.size();
        for (int i = 0; i < addressSize; i++) {
            //country
            root.getElementsByTagName("AddressInformation").item(0).getChildNodes().item(1).getChildNodes().item(1).setTextContent(currentAddressList.get(i).getCountry());
            //addresses
            root.getElementsByTagName("AddressInformation").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).setTextContent(currentAddressList.get(i).getAddresses());
            //streetLine2
            root.getElementsByTagName("AddressInformation").item(0).getChildNodes().item(3).getChildNodes().item(5).getChildNodes().item(1).setTextContent(currentAddressList.get(i).getStreetLine2());
            //suburb
            root.getElementsByTagName("AddressInformation").item(0).getChildNodes().item(3).getChildNodes().item(7).getChildNodes().item(3).getChildNodes().item(1).setTextContent(currentAddressList.get(i).getSuburb());
            //state
            root.getElementsByTagName("AddressInformation").item(0).getChildNodes().item(3).getChildNodes().item(7).getChildNodes().item(5).getChildNodes().item(1).setTextContent(currentAddressList.get(i).getState());
            //postCode
            root.getElementsByTagName("AddressInformation").item(0).getChildNodes().item(3).getChildNodes().item(7).getChildNodes().item(7).getChildNodes().item(1).setTextContent(currentAddressList.get(i).getPostCode());
            //applicantList
            CurrentAddress address = currentAddressList.get(i);
            int applicantListSize = address.getApplicantList().size();
            for (int a = 0; a < applicantListSize; a++) {
                //names
                root.getElementsByTagName("PersonName").item(2).setTextContent(address.getApplicantList().get(a).getNames());
                //dataFrom
                root.getElementsByTagName("DateFr").item(0).getChildNodes().item(1).setTextContent(address.getApplicantList().get(a).getDataFrom());
                //dataTo
                root.getElementsByTagName("DateTo").item(18).setTextContent(address.getApplicantList().get(a).getDataTo());
                //legalStatus
                root.getElementsByTagName("ResStatus").item(0).setTextContent(address.getApplicantList().get(a).getLegalStatus());
            }

        }
        //isSamePostalAddress
        List<PostalAddress> postalAddressList = customerInformationDO.getPostalAddressList();
        if (customerInformationDO.getAddresses().getIsSamePostalAddress() == 1) {
            root.getElementsByTagName("Postal").item(0).getChildNodes().item(1).getChildNodes().item(1).setTextContent("1");
            if (customerInformationDO.getAddresses().getIsSameResidential() == 1) {
                root.getElementsByTagName("SameAsResQ").item(0).getChildNodes().item(1).setTextContent("1");
            } else {
                root.getElementsByTagName("SameAsResQ").item(0).getChildNodes().item(1).setTextContent("2");
                //PostalAddress
                int listSize = postalAddressList.size();
                for (int i = 0; i < listSize; i++) {
                    //line1
                    root.getElementsByTagName("POAddress1").item(0).setTextContent(postalAddressList.get(i).getLine1());
                    //line2
                    root.getElementsByTagName("POAddress2").item(0).setTextContent(postalAddressList.get(i).getLine2());
                    //townOrCity
                    root.getElementsByTagName("Locality").item(0).getChildNodes().item(3).getChildNodes().item(1).setTextContent(postalAddressList.get(i).getTownOrCity());
                    //postCode
                    root.getElementsByTagName("Locality").item(0).getChildNodes().item(5).getChildNodes().item(1).setTextContent(postalAddressList.get(i).getPostCode());
                    //state
                    root.getElementsByTagName("StateCountry").item(0).getChildNodes().item(1).getChildNodes().item(1).setTextContent(postalAddressList.get(i).getState());
                    //country
                    root.getElementsByTagName("StateCountry").item(0).getChildNodes().item(3).getChildNodes().item(1).setTextContent(postalAddressList.get(i).getCountry());
                    //names
                    root.getElementsByTagName("PersonName").item(2).setTextContent(postalAddressList.get(i).getNames());
                    //names2
                    //todo 判断有第二个名字直接
                    root.getElementsByTagName("PersonName").item(2).setTextContent(postalAddressList.get(i).getNames2());
                }

            }
        } else {
            root.getElementsByTagName("Postal").item(0).getChildNodes().item(1).getChildNodes().item(1).setTextContent("2");
            int aList = postalAddressList.size();
            for (int i = 0; i < aList; i++) {
                //line1
                root.getElementsByTagName("POAddress1").item(0).setTextContent(postalAddressList.get(i).getLine1());
                //line2
                root.getElementsByTagName("POAddress2").item(0).setTextContent(postalAddressList.get(i).getLine2());
                //townOrCity
                root.getElementsByTagName("Locality").item(0).getChildNodes().item(3).getChildNodes().item(1).setTextContent(postalAddressList.get(i).getTownOrCity());
                //postCode
                root.getElementsByTagName("Locality").item(0).getChildNodes().item(5).getChildNodes().item(1).setTextContent(postalAddressList.get(i).getPostCode());
                //state
                root.getElementsByTagName("StateCountry").item(0).getChildNodes().item(1).getChildNodes().item(1).setTextContent(postalAddressList.get(i).getState());
                //country
                root.getElementsByTagName("StateCountry").item(0).getChildNodes().item(3).getChildNodes().item(1).setTextContent(postalAddressList.get(i).getCountry());
                //names
                root.getElementsByTagName("PersonName").item(2).setTextContent(postalAddressList.get(i).getNames());
                //names2
                //todo 判断有第二个名字直接
                root.getElementsByTagName("PersonName").item(2).setTextContent(postalAddressList.get(i).getNames2());

            }

        }

        //HealthQuestions
        HealthQuestions healthQuestions = customerInformationDO.getHealthQuestions();
        //HealthExamination
        //HealthExaminationQuestion
        if (healthQuestions.getLista() == null) {
            root.getElementsByTagName("Exam").item(0).getChildNodes().item(1).getChildNodes().item(1).setTextContent("2");
        } else {
            root.getElementsByTagName("Exam").item(0).getChildNodes().item(1).getChildNodes().item(1).setTextContent("1");
            int hSize = healthQuestions.getLista().size();
            List<HealthExamination> lista = healthQuestions.getLista();
            for (int i = 0; i < hSize; i++) {
                //name
                root.getElementsByTagName("Exam").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(lista.get(i).getName());
                //date
                root.getElementsByTagName("Exam").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(String.valueOf(lista.get(i).getDate()));
                //country
                root.getElementsByTagName("Exam").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(5).setTextContent(lista.get(i).getCountry());
                //hapId
                root.getElementsByTagName("Exam").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(7).setTextContent(String.valueOf(lista.get(i).getHapId()));
            }

        }

        //listb
        if (healthQuestions.getListb() == null) {
            root.getElementsByTagName("HealthCare").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("2");
        } else {
            root.getElementsByTagName("HealthCare").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("1");
            List<HealthTemplate> listb = healthQuestions.getListb();
            for (int i = 0; i < listb.size(); i++) {
                //name
                root.getElementsByTagName("HealthCare").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(listb.get(i).getName());
                //reason
                root.getElementsByTagName("HealthCare").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(listb.get(i).getReason());
                //details
                root.getElementsByTagName("HealthCare").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(5).setTextContent(listb.get(i).getDetails());
            }
        }

        //listc
        if (healthQuestions.getListc() == null) {
            root.getElementsByTagName("Doctor").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("2");

        } else {
            root.getElementsByTagName("Doctor").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("1");
            List<HealthTemplate> listc = healthQuestions.getListc();
            for (int i = 0; i < listc.size(); i++) {
                //name
                root.getElementsByTagName("Doctor").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(listc.get(i).getName());
                //reason
                //details
                root.getElementsByTagName("Doctor").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(listc.get(i).getDetails());

            }


        }

        //listd
        if (healthQuestions.getListd() == null) {
            root.getElementsByTagName("ChildCare").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("2");
        } else {
            root.getElementsByTagName("ChildCare").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("1");
            List<HealthTemplate> listd = healthQuestions.getListd();
            for (int i = 0; i < listd.size(); i++) {
                //name
                root.getElementsByTagName("ChildCare").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(listd.get(i).getName());
                //reason
                //details
                root.getElementsByTagName("ChildCare").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(listd.get(i).getDetails());

            }


        }

        //liste
        if (healthQuestions.getListe() == null) {
            root.getElementsByTagName("School").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("2");
        } else {
            root.getElementsByTagName("School").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("1");
            List<HealthTemplate> liste = healthQuestions.getListe();
            for (int i = 0; i < liste.size(); i++) {
                //name
                root.getElementsByTagName("School").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(liste.get(i).getName());
                //reason
                //details
                root.getElementsByTagName("School").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(liste.get(i).getDetails());

            }


        }


        //listf
        if (healthQuestions.getListf() == null) {
            root.getElementsByTagName("TB").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("2");

        } else {
            root.getElementsByTagName("TB").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("1");

            List<HealthTemplate> listf = healthQuestions.getListf();
            for (int i = 0; i < listf.size(); i++) {
                //name
                root.getElementsByTagName("TB").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(listf.get(i).getName());
                //reason
                //details
                root.getElementsByTagName("TB").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(listf.get(i).getDetails());

            }

        }

        //listg
        if (healthQuestions.getListg() == null) {
            root.getElementsByTagName("TBContact").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("2");

        } else {
            root.getElementsByTagName("TBContact").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("1");
            List<HealthTemplate> listg = healthQuestions.getListg();
            for (int i = 0; i < listg.size(); i++) {
                //name
                root.getElementsByTagName("TBContact").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(listg.get(i).getName());
                //reason
                //details
                root.getElementsByTagName("TBContact").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(listg.get(i).getDetails());
            }
        }


        //listh1
        if (healthQuestions.getListh1() == null) {
            root.getElementsByTagName("Blood").item(0).getChildNodes().item(3).getChildNodes().item(1).setTextContent("2");
        } else {
            root.getElementsByTagName("Blood").item(0).getChildNodes().item(3).getChildNodes().item(1).setTextContent("1");
            List<HealthTemplate> listh1 = healthQuestions.getListh1();
            for (int i = 0; i < listh1.size(); i++) {
                //name
                root.getElementsByTagName("Blood").item(0).getChildNodes().item(5).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(listh1.get(i).getName());
                //reason
                //details
                root.getElementsByTagName("Blood").item(0).getChildNodes().item(5).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(listh1.get(i).getDetails());
            }
        }

        //listh2
        if (healthQuestions.getListh2() == null) {
            root.getElementsByTagName("Cancer").item(0).getChildNodes().item(1).getChildNodes().item(1).setTextContent("2");

        } else {
            root.getElementsByTagName("Cancer").item(0).getChildNodes().item(1).getChildNodes().item(1).setTextContent("1");
            List<HealthTemplate> listh2 = healthQuestions.getListh2();
            for (int i = 0; i < listh2.size(); i++) {
                //name
                root.getElementsByTagName("Cancer").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(listh2.get(i).getName());
                //reason
                //details
                root.getElementsByTagName("Cancer").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(listh2.get(i).getDetails());
            }
        }


        //listh3
        if (healthQuestions.getListh3() == null) {
            root.getElementsByTagName("Heart").item(0).getChildNodes().item(1).getChildNodes().item(1).setTextContent("2");

        } else {
            root.getElementsByTagName("Heart").item(0).getChildNodes().item(1).getChildNodes().item(1).setTextContent("1");
            List<HealthTemplate> listh3 = healthQuestions.getListh3();
            for (int i = 0; i < listh3.size(); i++) {
                //name
                root.getElementsByTagName("Heart").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(listh3.get(i).getName());
                //reason
                //details
                root.getElementsByTagName("Heart").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(listh3.get(i).getDetails());
            }
        }

        //listh4
        if (healthQuestions.getListh4() == null) {
            root.getElementsByTagName("Hepatitis").item(0).getChildNodes().item(1).getChildNodes().item(1).setTextContent("2");
        } else {
            root.getElementsByTagName("Hepatitis").item(0).getChildNodes().item(1).getChildNodes().item(1).setTextContent("1");
            List<HealthTemplate> listh4 = healthQuestions.getListh4();
            for (int i = 0; i < listh4.size(); i++) {
                //name
                root.getElementsByTagName("Hepatitis").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(listh4.get(i).getName());

                //reason
                //details
                root.getElementsByTagName("Hepatitis").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(listh4.get(i).getDetails());

            }


        }

        //listh5
        if (healthQuestions.getListh5() == null) {
            root.getElementsByTagName("Liver").item(0).getChildNodes().item(1).getChildNodes().item(1).setTextContent("2");
        } else {
            root.getElementsByTagName("Liver").item(0).getChildNodes().item(1).getChildNodes().item(1).setTextContent("1");
            List<HealthTemplate> listh5 = healthQuestions.getListh5();
            for (int i = 0; i < listh5.size(); i++) {
                //name
                root.getElementsByTagName("Liver").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(listh5.get(i).getName());
                //reason
                //details
                root.getElementsByTagName("Liver").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(listh5.get(i).getDetails());
            }

        }

        //listh6
        if (healthQuestions.getListh6() == null) {
            root.getElementsByTagName("Kidney").item(0).getChildNodes().item(1).getChildNodes().item(1).setTextContent("2");

        } else {
            root.getElementsByTagName("Kidney").item(0).getChildNodes().item(1).getChildNodes().item(1).setTextContent("1");
            List<HealthTemplate> listh6 = healthQuestions.getListh6();
            for (int i = 0; i < listh6.size(); i++) {
                //name
                root.getElementsByTagName("Kidney").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(listh6.get(i).getName());
                //reason
                //details
                root.getElementsByTagName("Kidney").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(listh6.get(i).getDetails());
            }
        }


        //listh7
        if (healthQuestions.getListh7() == null) {
            root.getElementsByTagName("Mental").item(0).getChildNodes().item(1).getChildNodes().item(1).setTextContent("2");

        } else {
            root.getElementsByTagName("Mental").item(0).getChildNodes().item(1).getChildNodes().item(1).setTextContent("1");
            List<HealthTemplate> listh7 = healthQuestions.getListh7();
            for (int i = 0; i < listh7.size(); i++) {
                //name
                root.getElementsByTagName("Mental").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(listh7.get(i).getName());
                //reason
                //details
                root.getElementsByTagName("Mental").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(listh7.get(i).getDetails());

            }
        }
        //listh8
        if (healthQuestions.getListh8() == null) {
            root.getElementsByTagName("Pregnancy").item(0).getChildNodes().item(1).getChildNodes().item(1).setTextContent("2");

        } else {
            root.getElementsByTagName("Pregnancy").item(0).getChildNodes().item(1).getChildNodes().item(1).setTextContent("1");
            List<HealthTemplate> listh8 = healthQuestions.getListh8();
            for (int i = 0; i < listh8.size(); i++) {
                //name
                root.getElementsByTagName("Pregnancy").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(listh8.get(i).getName());
                //reason
                //details
                root.getElementsByTagName("Pregnancy").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(listh8.get(i).getDetails());
            }
        }

        //listh9
        if (healthQuestions.getListh9() == null) {
            root.getElementsByTagName("Respiratory").item(0).getChildNodes().item(1).getChildNodes().item(1).setTextContent("2");

        } else {
            root.getElementsByTagName("Respiratory").item(0).getChildNodes().item(1).getChildNodes().item(1).setTextContent("1");
            List<HealthTemplate> listh9 = healthQuestions.getListh9();
            for (int i = 0; i < listh9.size(); i++) {
                //name
                root.getElementsByTagName("Respiratory").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(listh9.get(i).getName());
                //reason
                //details
                root.getElementsByTagName("Respiratory").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(listh9.get(i).getDetails());

            }


        }

        //listh10
        if (healthQuestions.getListh10() == null) {
            root.getElementsByTagName("HIV").item(0).getChildNodes().item(1).getChildNodes().item(1).setTextContent("2");

        } else {
            root.getElementsByTagName("HIV").item(0).getChildNodes().item(1).getChildNodes().item(1).setTextContent("1");
            List<HealthTemplate> listh10 = healthQuestions.getListh10();
            for (int i = 0; i < listh10.size(); i++) {
                //name
                root.getElementsByTagName("HIV").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(listh10.get(i).getName());
                //reason
                //details
                root.getElementsByTagName("HIV").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(listh10.get(i).getDetails());
            }
        }

        //listh11
        if (healthQuestions.getListh11() == null) {
            root.getElementsByTagName("Diabetes").item(0).getChildNodes().item(1).getChildNodes().item(1).setTextContent("2");

        } else {
            root.getElementsByTagName("Diabetes").item(0).getChildNodes().item(1).getChildNodes().item(1).setTextContent("1");
            List<HealthTemplate> listh11 = healthQuestions.getListh11();
            for (int i = 0; i < listh11.size(); i++) {
                //name
                root.getElementsByTagName("Diabetes").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(listh11.get(i).getName());
                //reason
                //details
                root.getElementsByTagName("Diabetes").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(listh11.get(i).getDetails());
            }


        }

        //listh12
        if (healthQuestions.getListh12() == null) {
            root.getElementsByTagName("Disability").item(0).getChildNodes().item(1).getChildNodes().item(1).setTextContent("2");

        } else {
            root.getElementsByTagName("Disability").item(0).getChildNodes().item(1).getChildNodes().item(1).setTextContent("1");
            List<HealthTemplate> listh12 = healthQuestions.getListh12();
            for (int i = 0; i < listh12.size(); i++) {
                //name
                root.getElementsByTagName("Disability").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(listh12.get(i).getName());
                //reason
                //details
                root.getElementsByTagName("Disability").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(listh12.get(i).getDetails());
            }
        }

        //listh13
        if (healthQuestions.getListh13() == null) {
            root.getElementsByTagName("Other").item(21).getChildNodes().item(1).getChildNodes().item(1).setTextContent("2");


        } else {
            root.getElementsByTagName("Other").item(21).getChildNodes().item(1).getChildNodes().item(1).setTextContent("1");
            List<HealthTemplate> listh13 = healthQuestions.getListh13();
            for (int i = 0; i < listh13.size(); i++) {
                //name
                root.getElementsByTagName("Other").item(21).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(listh13.get(i).getName());
                //reason
                //details
                root.getElementsByTagName("Other").item(21).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(listh13.get(i).getDetails());
            }
        }

        //listi
        if (healthQuestions.getListi() == null) {
            root.getElementsByTagName("Care").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("2");

        } else {
            root.getElementsByTagName("Care").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("1");
            List<HealthTemplate> listi = healthQuestions.getListi();
            for (int i = 0; i < listi.size(); i++) {
                //name
                root.getElementsByTagName("Care").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(listi.get(i).getName());
                //reason
                //details
                root.getElementsByTagName("Care").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(listi.get(i).getDetails());
            }
        }


        //CharacterIssues

        //1
        if (customerInformationDO.getCharacterIssues() == null || customerInformationDO.getCharacterIssues().getCl1() == null) {
            root.getElementsByTagName("AFP").item(0).getChildNodes().item(1).getChildNodes().item(1).setTextContent("2");

        } else {
            root.getElementsByTagName("AFP").item(0).getChildNodes().item(1).getChildNodes().item(1).setTextContent("1");
            List<PoliceClearance> cl1 = customerInformationDO.getCharacterIssues().getCl1();
            for (int i = 0; i < cl1.size(); i++) {
                //name
                root.getElementsByTagName("AFP").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(cl1.get(i).getName());
                //country
                root.getElementsByTagName("AFP").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(cl1.get(i).getCountry());
                //applicationDate
                root.getElementsByTagName("AFP").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(5).setTextContent(String.valueOf(cl1.get(i).getApplicationDate()));
                //dateOfIssue
                root.getElementsByTagName("AFP").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(7).setTextContent(String.valueOf(cl1.get(i).getDateOfIssue()));
                //referenceNumber
                root.getElementsByTagName("AFP").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(9).setTextContent(cl1.get(i).getReferenceNumber());
            }
        }

        //C2
        if (customerInformationDO.getCharacterIssues() == null || customerInformationDO.getCharacterIssues().getCl2() == null) {
            root.getElementsByTagName("Detention").item(0).getChildNodes().item(1).getChildNodes().item(1).setTextContent("2");

        } else {
            root.getElementsByTagName("Detention").item(0).getChildNodes().item(1).getChildNodes().item(1).setTextContent("1");
            List<Detention> cl2 = customerInformationDO.getCharacterIssues().getCl2();
            for (int i = 0; i < cl2.size(); i++) {
                //name
                root.getElementsByTagName("Detention").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(cl2.get(i).getName());
                //dateForm
                root.getElementsByTagName("Detention").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(String.valueOf(cl2.get(i).getDateForm()));
                //dateTo
                root.getElementsByTagName("Detention").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(5).setTextContent(String.valueOf(cl2.get(i).getDateTo()));

                //country
                root.getElementsByTagName("Detention").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(7).setTextContent(cl2.get(i).getCountry());
                //details
                root.getElementsByTagName("Detention").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(9).setTextContent(cl2.get(i).getDetails());
            }


        }


        //3
        if (customerInformationDO.getCharacterIssues() == null || customerInformationDO.getCharacterIssues().getCl3() == null) {
            root.getElementsByTagName("Offence").item(1).getChildNodes().item(1).getChildNodes().item(3).setTextContent("2");

        } else {
            root.getElementsByTagName("Offence").item(1).getChildNodes().item(1).getChildNodes().item(3).setTextContent("1");
            List<Template> cl3 = customerInformationDO.getCharacterIssues().getCl3();
            for (int i = 0; i < cl3.size(); i++) {
                //name
                root.getElementsByTagName("ConvictDetail").item(0).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(cl3.get(i).getName());
                //country.
                root.getElementsByTagName("ConvictDetail").item(0).getChildNodes().item(1).getChildNodes().item(5).getChildNodes().item(1).setTextContent(cl3.get(i).getCountry());
                //date
                root.getElementsByTagName("ConvictDetail").item(0).getChildNodes().item(1).getChildNodes().item(7).getChildNodes().item(1).setTextContent(String.valueOf(cl3.get(i).getDate()));
                //type
                //todo 判断类型
                root.getElementsByTagName("ConvictDetail").item(0).getChildNodes().item(1).getChildNodes().item(9).getChildNodes().item(1).setTextContent(type(cl3.get(i).getType()));
                //details
                root.getElementsByTagName("ConvictDetail").item(0).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(1).setTextContent(cl3.get(i).getDetails());
            }

        }

        //4
        if (customerInformationDO.getCharacterIssues() == null || customerInformationDO.getCharacterIssues().getCl4() == null) {
            root.getElementsByTagName("Charged").item(1).getChildNodes().item(1).getChildNodes().item(3).setTextContent("2");
        } else {
            root.getElementsByTagName("Charged").item(1).getChildNodes().item(1).getChildNodes().item(3).setTextContent("1");
            List<Template> cl4 = customerInformationDO.getCharacterIssues().getCl4();
            for (int i = 0; i < cl4.size(); i++) {
                //name
                root.getElementsByTagName("ChargeDetail").item(0).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(cl4.get(i).getName());
                //country
                root.getElementsByTagName("ChargeDetail").item(0).getChildNodes().item(1).getChildNodes().item(5).getChildNodes().item(1).setTextContent(cl4.get(i).getCountry());
                //date
                root.getElementsByTagName("ChargeDetail").item(0).getChildNodes().item(1).getChildNodes().item(7).getChildNodes().item(1).setTextContent(String.valueOf(cl4.get(i).getDate()));
                //type
                //todo 判断类型
                root.getElementsByTagName("ChargeDetail").item(0).getChildNodes().item(1).getChildNodes().item(9).getChildNodes().item(1).setTextContent(type(cl4.get(i).getType()));
                //details
                root.getElementsByTagName("ChargeDetail").item(0).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(1).setTextContent(cl4.get(i).getDetails());
            }


        }


        //5
        if (customerInformationDO.getCharacterIssues() == null || customerInformationDO.getCharacterIssues().getCl5() == null) {
            root.getElementsByTagName("FamilyViolence").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("2");

        } else {

            root.getElementsByTagName("FamilyViolence").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("1");
            List<Template> cl5 = customerInformationDO.getCharacterIssues().getCl5();
            for (int i = 0; i < cl5.size(); i++) {
                //name
                root.getElementsByTagName("OrderDetail").item(0).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(cl5.get(i).getName());
                //country
                root.getElementsByTagName("OrderDetail").item(0).getChildNodes().item(1).getChildNodes().item(5).getChildNodes().item(1).setTextContent(cl5.get(i).getCountry());
                //date
                root.getElementsByTagName("OrderDetail").item(0).getChildNodes().item(1).getChildNodes().item(7).getChildNodes().item(1).setTextContent(String.valueOf(cl5.get(i).getDate()));

                //type
                //details
                root.getElementsByTagName("OrderDetail").item(0).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(1).setTextContent(cl5.get(i).getDetails());
            }

        }


        //6
        if (customerInformationDO.getCharacterIssues() == null || customerInformationDO.getCharacterIssues().getCl6() == null) {
            root.getElementsByTagName("ArrestWarrant").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("2");

        } else {
            root.getElementsByTagName("ArrestWarrant").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("1");
            List<Template> cl6 = customerInformationDO.getCharacterIssues().getCl6();
            for (int i = 0; i < cl6.size(); i++) {
                //name
                root.getElementsByTagName("ArrestWarrant").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(cl6.get(i).getName());
                //country
                root.getElementsByTagName("ArrestWarrant").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(cl6.get(i).getCountry());
                //date
                //type
                //details
                root.getElementsByTagName("ArrestWarrant").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(5).setTextContent(cl6.get(i).getDetails());
            }
        }

        //7
        if (customerInformationDO.getCharacterIssues() == null || customerInformationDO.getCharacterIssues().getCl7() == null) {
            root.getElementsByTagName("ChildCrime").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("2");

        } else {
            root.getElementsByTagName("ChildCrime").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("1");
            List<Template> cl7 = customerInformationDO.getCharacterIssues().getCl7();
            for (int i = 0; i < cl7.size(); i++) {
                //name
                root.getElementsByTagName("ChildCrime").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(cl7.get(i).getName());
                //country
                root.getElementsByTagName("ChildCrime").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(cl7.get(i).getCountry());
                //date
                //type
                //details
                root.getElementsByTagName("ChildCrime").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(5).setTextContent(cl7.get(i).getDetails());


            }


        }


        //8
        if (customerInformationDO.getCharacterIssues() == null || customerInformationDO.getCharacterIssues().getCl8() == null) {
            root.getElementsByTagName("SexOffender").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("2");
        } else {
            root.getElementsByTagName("SexOffender").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("1");
            List<Template> cl8 = customerInformationDO.getCharacterIssues().getCl8();
            for (int i = 0; i < cl8.size(); i++) {
                //name
                root.getElementsByTagName("SexOffender").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(cl8.get(i).getName());
                //country
                root.getElementsByTagName("SexOffender").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(cl8.get(i).getCountry());
                //date
                //type
                //details
                root.getElementsByTagName("SexOffender").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(5).setTextContent(cl8.get(i).getDetails());
            }
        }


        //9
        if (customerInformationDO.getCharacterIssues() == null || customerInformationDO.getCharacterIssues().getCl9() == null) {
            root.getElementsByTagName("Prison").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("2");

        } else {
            root.getElementsByTagName("Prison").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("1");
            List<Template> cl9 = customerInformationDO.getCharacterIssues().getCl9();
            for (int i = 0; i < cl9.size(); i++) {
                //name
                root.getElementsByTagName("Prison").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(cl9.get(i).getName());
                //country
                root.getElementsByTagName("Prison").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(cl9.get(i).getCountry());
                //date
                //type
                //details
                root.getElementsByTagName("Prison").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(5).setTextContent(cl9.get(i).getDetails());

            }


        }


        //10
        if (customerInformationDO.getCharacterIssues() == null || customerInformationDO.getCharacterIssues().getCl10() == null) {
            root.getElementsByTagName("Insanity").item(1).getChildNodes().item(1).getChildNodes().item(3).setTextContent("2");
        } else {
            root.getElementsByTagName("Insanity").item(1).getChildNodes().item(1).getChildNodes().item(3).setTextContent("1");
            List<Template> cl10 = customerInformationDO.getCharacterIssues().getCl10();
            for (int i = 0; i < cl10.size(); i++) {
                //name
                root.getElementsByTagName("Insanity").item(1).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(cl10.get(i).getName());
                //country
                root.getElementsByTagName("Insanity").item(1).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(cl10.get(i).getCountry());
                //date
                //type
                //details
                root.getElementsByTagName("Insanity").item(1).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(5).setTextContent(cl10.get(i).getDetails());
            }
        }

        //11
        if (customerInformationDO.getCharacterIssues() == null || customerInformationDO.getCharacterIssues().getCl11() == null) {
            root.getElementsByTagName("NotFit").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("2");

        } else {
            root.getElementsByTagName("NotFit").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("1");
            List<Template> cl11 = customerInformationDO.getCharacterIssues().getCl11();
            for (int i = 0; i < cl11.size(); i++) {
                //name
                root.getElementsByTagName("NotFit").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(cl11.get(i).getName());
                //country
                root.getElementsByTagName("NotFit").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(cl11.get(i).getCountry());
                //date
                //type
                //details
                root.getElementsByTagName("NotFit").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(5).setTextContent(cl11.get(i).getDetails());

            }

        }


        //12
        if (customerInformationDO.getCharacterIssues() == null || customerInformationDO.getCharacterIssues().getCl12() == null) {
            root.getElementsByTagName("Misleading").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("2");

        } else {
            root.getElementsByTagName("Misleading").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("1");
            List<Template> cl12 = customerInformationDO.getCharacterIssues().getCl12();
            for (int i = 0; i < cl12.size(); i++) {
                //name
                root.getElementsByTagName("Misleading").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(cl12.get(i).getName());
                //country
                //date
                //type
                //details
                root.getElementsByTagName("Misleading").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(cl12.get(i).getDetails());

            }

        }


        //13
        if (customerInformationDO.getCharacterIssues() == null || customerInformationDO.getCharacterIssues().getCl3() == null) {
            root.getElementsByTagName("Visa").item(2).getChildNodes().item(1).getChildNodes().item(3).setTextContent("2");

        } else {
            root.getElementsByTagName("Visa").item(2).getChildNodes().item(1).getChildNodes().item(3).setTextContent("1");
            List<Template> cl3 = customerInformationDO.getCharacterIssues().getCl3();
            for (int i = 0; i < cl3.size(); i++) {
                //name
                root.getElementsByTagName("Visa").item(2).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(cl3.get(i).getName());
                //country
                root.getElementsByTagName("Visa").item(2).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(cl3.get(i).getCountry());
                //date
                //type
                //details
                root.getElementsByTagName("Visa").item(2).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(5).setTextContent(cl3.get(i).getDetails());
            }
        }


        //14
        if (customerInformationDO.getCharacterIssues() == null || customerInformationDO.getCharacterIssues().getCl4() == null) {
            root.getElementsByTagName("VisaOverstay").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("2");
        } else {
            root.getElementsByTagName("VisaOverstay").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("1");
            List<Template> cl14 = customerInformationDO.getCharacterIssues().getCl14();
            for (int i = 0; i < cl14.size(); i++) {
                //name
                root.getElementsByTagName("VisaOverstay").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(cl14.get(i).getName());
                //country
                root.getElementsByTagName("VisaOverstay").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(cl14.get(i).getCountry());
                //date
                //type
                //details
                root.getElementsByTagName("VisaOverstay").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(5).setTextContent(cl14.get(i).getDetails());

            }
        }

        //15
        if (customerInformationDO.getCharacterIssues() == null || customerInformationDO.getCharacterIssues().getCl15() == null) {
            root.getElementsByTagName("Removed").item(1).getChildNodes().item(1).getChildNodes().item(3).setTextContent("2");

        } else {
            root.getElementsByTagName("Removed").item(1).getChildNodes().item(1).getChildNodes().item(3).setTextContent("1");
            List<Template> cl15 = customerInformationDO.getCharacterIssues().getCl15();
            for (int i = 0; i < cl15.size(); i++) {
                //name
                root.getElementsByTagName("Removed").item(1).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(cl15.get(i).getName());
                //country
                root.getElementsByTagName("Removed").item(1).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(cl15.get(i).getCountry());
                //date
                //type
                //details
                root.getElementsByTagName("Removed").item(1).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(5).setTextContent(cl15.get(i).getDetails());
            }
        }


        //16
        if (customerInformationDO.getCharacterIssues() == null || customerInformationDO.getCharacterIssues().getCl16() == null) {
            root.getElementsByTagName("Avoid").item(1).getChildNodes().item(1).getChildNodes().item(3).setTextContent("2");

        } else {
            root.getElementsByTagName("Avoid").item(1).getChildNodes().item(1).getChildNodes().item(3).setTextContent("1");
            List<Template> cl16 = customerInformationDO.getCharacterIssues().getCl16();
            for (int i = 0; i < cl16.size(); i++) {
                //name
                root.getElementsByTagName("Avoid").item(1).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(cl16.get(i).getName());
                //country
                root.getElementsByTagName("Avoid").item(1).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(cl16.get(i).getCountry());
                //date
                //type
                //details
                root.getElementsByTagName("Avoid").item(1).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(5).setTextContent(cl16.get(i).getDetails());

            }


        }


        //17
        if (customerInformationDO.getCharacterIssues() == null || customerInformationDO.getCharacterIssues().getCl17() == null) {
            root.getElementsByTagName("Excluded").item(1).getChildNodes().item(1).getChildNodes().item(3).setTextContent("2");

        } else {
            root.getElementsByTagName("Excluded").item(1).getChildNodes().item(1).getChildNodes().item(3).setTextContent("1");
            List<Template> cl17 = customerInformationDO.getCharacterIssues().getCl17();
            for (int i = 0; i < cl17.size(); i++) {

                //name
                root.getElementsByTagName("Excluded").item(1).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(cl17.get(i).getName());
                //country
                root.getElementsByTagName("Excluded").item(1).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(cl17.get(i).getCountry());
                //date
                //type
                //details
                root.getElementsByTagName("Excluded").item(1).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(5).setTextContent(cl17.get(i).getDetails());

            }

        }


        //18
        if (customerInformationDO.getCharacterIssues() == null || customerInformationDO.getCharacterIssues().getCl18() == null) {
            root.getElementsByTagName("RefusedCitz").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("2");

        } else {
            root.getElementsByTagName("RefusedCitz").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("1");
            List<Template> cl18 = customerInformationDO.getCharacterIssues().getCl18();
            for (int i = 0; i < cl18.size(); i++) {
                //name
                root.getElementsByTagName("RefusedCitz").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(cl18.get(i).getName());
                //country
                root.getElementsByTagName("RefusedCitz").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(cl18.get(i).getCountry());
                //date
                //type
                //details
                root.getElementsByTagName("RefusedCitz").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(5).setTextContent(cl18.get(i).getDetails());

            }


        }

        //19
        if (customerInformationDO.getCharacterIssues() == null || customerInformationDO.getCharacterIssues().getCl19() == null) {
            root.getElementsByTagName("WarCrimes").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("2");

        } else {
            root.getElementsByTagName("WarCrimes").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("1");
            List<Template> cl19 = customerInformationDO.getCharacterIssues().getCl19();
            for (int i = 0; i < cl19.size(); i++) {
                //name
                root.getElementsByTagName("WarCrimes").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(cl19.get(i).getName());
                //country
                root.getElementsByTagName("WarCrimes").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(cl19.get(i).getCountry());
                //date
                //type
                //details
                root.getElementsByTagName("WarCrimes").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(5).setTextContent(cl19.get(i).getDetails());

            }


        }


        //20
        if (customerInformationDO.getCharacterIssues() == null || customerInformationDO.getCharacterIssues().getCl20() == null) {
            root.getElementsByTagName("Activities").item(1).getChildNodes().item(1).getChildNodes().item(3).setTextContent("2");

        } else {
            root.getElementsByTagName("Activities").item(1).getChildNodes().item(1).getChildNodes().item(3).setTextContent("1");
            List<Template> cl20 = customerInformationDO.getCharacterIssues().getCl20();
            for (int i = 0; i < cl20.size(); i++) {
                //name
                root.getElementsByTagName("Activities").item(1).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(cl20.get(i).getName());
                //country
                root.getElementsByTagName("Activities").item(1).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(cl20.get(i).getCountry());
                //date
                //type
                //details
                root.getElementsByTagName("Activities").item(1).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(5).setTextContent(cl20.get(i).getDetails());

            }

        }


        //21
        if (customerInformationDO.getCharacterIssues() == null || customerInformationDO.getCharacterIssues().getCl21() == null) {
            root.getElementsByTagName("Debts").item(1).getChildNodes().item(1).getChildNodes().item(3).setTextContent("2");

        } else {
            root.getElementsByTagName("Debts").item(1).getChildNodes().item(1).getChildNodes().item(3).setTextContent("1");
            List<Template> cl21 = customerInformationDO.getCharacterIssues().getCl21();
            for (int i = 0; i < cl21.size(); i++) {
                //name
                root.getElementsByTagName("Debts").item(1).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(cl21.get(i).getName());
                //country
                root.getElementsByTagName("Debts").item(1).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(cl21.get(i).getCountry());
                //date
                //type
                //details
                root.getElementsByTagName("Debts").item(1).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(5).setTextContent(cl21.get(i).getDetails());
            }
        }

        //22
        if (customerInformationDO.getCharacterIssues() == null || customerInformationDO.getCharacterIssues().getCl22() == null) {
            root.getElementsByTagName("People").item(1).getChildNodes().item(1).getChildNodes().item(3).setTextContent("2");

        } else {
            root.getElementsByTagName("People").item(1).getChildNodes().item(1).getChildNodes().item(3).setTextContent("1");
            List<Template> cl22 = customerInformationDO.getCharacterIssues().getCl22();
            for (int i = 0; i < cl22.size(); i++) {
                //name
                root.getElementsByTagName("People").item(1).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(cl22.get(i).getName());
                //country
                root.getElementsByTagName("People").item(1).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(cl22.get(i).getCountry());
                //date
                //type
                //details
                root.getElementsByTagName("People").item(1).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(5).setTextContent(cl22.get(i).getDetails());

            }


        }

        //23
        if (customerInformationDO.getCharacterIssues() == null || customerInformationDO.getCharacterIssues().getCl23() == null) {
            root.getElementsByTagName("CriminalAssociation").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("2");

        } else {
            root.getElementsByTagName("CriminalAssociation").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("1");
            List<Template> cl23 = customerInformationDO.getCharacterIssues().getCl23();
            for (int i = 0; i < cl23.size(); i++) {

                //name
                root.getElementsByTagName("CriminalAssociation").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(cl23.get(i).getName());
                //country
                root.getElementsByTagName("CriminalAssociation").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(cl23.get(i).getCountry());
                //date
                //type
                //details
                root.getElementsByTagName("CriminalAssociation").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(5).setTextContent(cl23.get(i).getDetails());

            }


        }

        //C24
        if (customerInformationDO.getCharacterIssues() == null || customerInformationDO.getCharacterIssues().getCl24() == null) {
            root.getElementsByTagName("Violence").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("2");

        } else {
            root.getElementsByTagName("Violence").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("1");
            List<Template> cl24 = customerInformationDO.getCharacterIssues().getCl24();
            for (int i = 0; i < cl24.size(); i++) {

                //name
                root.getElementsByTagName("Violence").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(cl24.get(i).getName());
                //country
                root.getElementsByTagName("Violence").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(cl24.get(i).getCountry());
                //date
                //type
                //details
                root.getElementsByTagName("Violence").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(5).setTextContent(cl24.get(i).getDetails());
            }
        }


        //C25
        if (customerInformationDO.getCharacterIssues() == null || customerInformationDO.getCharacterIssues().getCl25() == null) {
            root.getElementsByTagName("Training").item(1).getChildNodes().item(1).getChildNodes().item(3).setTextContent("2");

        } else {
            root.getElementsByTagName("Training").item(1).getChildNodes().item(1).getChildNodes().item(3).setTextContent("1");
            List<Military> cl25 = customerInformationDO.getCharacterIssues().getCl25();
            for (int i = 0; i < cl25.size(); i++) {
                //name
                root.getElementsByTagName("TrainingDetail").item(1).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(cl25.get(i).getName());
                //country.
                root.getElementsByTagName("TrainingDetail").item(1).getChildNodes().item(1).getChildNodes().item(5).getChildNodes().item(1).setTextContent(cl25.get(i).getCountry());
                //date
                root.getElementsByTagName("TrainingDetail").item(1).getChildNodes().item(1).getChildNodes().item(7).getChildNodes().item(1).setTextContent(String.valueOf(cl25.get(i).getDateForm()));
                //type
                //todo 判断类型
                root.getElementsByTagName("TrainingDetail").item(1).getChildNodes().item(1).getChildNodes().item(9).getChildNodes().item(1).setTextContent(type25(cl25.get(i).getType()));
                //details
                root.getElementsByTagName("TrainingDetail").item(1).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(1).setTextContent(cl25.get(i).getDetails());

            }


        }


        //C26
        if (customerInformationDO.getCharacterIssues() == null || customerInformationDO.getCharacterIssues().getCl26() == null) {
            root.getElementsByTagName("Military").item(1).getChildNodes().item(1).getChildNodes().item(1).setTextContent("2");

        } else {
            root.getElementsByTagName("Military").item(1).getChildNodes().item(1).getChildNodes().item(1).setTextContent("1");
            List<MilitaryForce> cl26 = customerInformationDO.getCharacterIssues().getCl26();
            for (int i = 0; i < cl26.size(); i++) {
                //name;
                root.getElementsByTagName("MilitaryDetail").item(1).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(cl26.get(i).getName());

                // dateForm;
                root.getElementsByTagName("MilitaryDetail").item(1).getChildNodes().item(1).getChildNodes().item(5).getChildNodes().item(1).setTextContent(String.valueOf(cl26.get(i).getDateForm()));


                // dateTo;
                root.getElementsByTagName("MilitaryDetail").item(1).getChildNodes().item(1).getChildNodes().item(7).getChildNodes().item(1).setTextContent(String.valueOf(cl26.get(i).getDateTo()));

                // country;
                root.getElementsByTagName("MilitaryDetail").item(1).getChildNodes().item(1).getChildNodes().item(9).getChildNodes().item(1).setTextContent(cl26.get(i).getCountry());


                // type;
                root.getElementsByTagName("MilitaryDetail").item(1).getChildNodes().item(1).getChildNodes().item(11).getChildNodes().item(1).setTextContent(type26(cl26.get(i).getType()));


                // nameOfOrganisation;
                root.getElementsByTagName("MilitaryDetail").item(1).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(1).setTextContent(cl26.get(i).getNameOfOrganisation());


                // position;
                root.getElementsByTagName("MilitaryDetail").item(1).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).setTextContent(cl26.get(i).getPosition());

                // countryOfDeployment;
                root.getElementsByTagName("MilitaryDetail").item(1).getChildNodes().item(3).getChildNodes().item(5).getChildNodes().item(1).setTextContent(cl26.get(i).getCountryOfDeployment());

                // Duties;.
                root.getElementsByTagName("MilitaryDetail").item(1).getChildNodes().item(3).getChildNodes().item(7).getChildNodes().item(1).setTextContent(cl26.get(i).getDuties());

            }


        }

        //C27
        if (customerInformationDO.getCharacterIssues() == null || customerInformationDO.getCharacterIssues().getCl27() == null) {
            root.getElementsByTagName("PayForSpon").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("2");

        } else {
            root.getElementsByTagName("PayForSpon").item(0).getChildNodes().item(1).getChildNodes().item(3).setTextContent("1");
            List<Template> cl27 = customerInformationDO.getCharacterIssues().getCl27();
            for (int i = 0; i < cl27.size(); i++) {
                //name
                root.getElementsByTagName("PayForSpon").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(1).setTextContent(cl27.get(i).getName());
                //country
                //date
                //type
                //details
                root.getElementsByTagName("PayForSpon").item(0).getChildNodes().item(3).getChildNodes().item(3).getChildNodes().item(1).getChildNodes().item(3).getChildNodes().item(3).setTextContent(cl27.get(i).getDetails());

            }

        }


        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        //DOMSource source = new DOMSource(doc);
        Source source = new DOMSource(doc);
        //StreamResult result = new StreamResult();
        Result result = new StreamResult(XML2);
        transformer.transform(source, result);//将 XML==>Source 转换为 Result

    }

    private String type(String type) {
        if (type.equals("Armed Robbery"))
            return "13";
        if (type.equals("Assault"))
            return "14";
        if (type.equals("Child Pornography"))
            return "15";
        if (type.equals("Child Sex Offences"))
            return "16";
        if (type.equals("Domestic or Family Violence"))
            return "17";
        if (type.equals("Drug Offences"))
            return "18";
        if (type.equals("Fraud, Deception, White Collar Crime"))
            return "19";
        if (type.equals("Grievous Bodily Harm, Reckless Injury"))
            return "20";
        if (type.equals("Kidnapping"))
            return "21";
        if (type.equals("Manslaughter"))
            return "22";
        if (type.equals("Murder"))
            return "23";
        if (type.equals("People Smuggling"))
            return "24";
        if (type.equals("Rape, Sexual Offences"))
            return "25";
        if (type.equals("Theft, Robery, Break and Enter"))
            return "26";
        if (type.equals("Threat Involving Weapons Use"))
            return "27";
        if (type.equals("Traffic and Driving Offences"))
            return "28";
        if (type.equals("Other Offence"))
            return "29";
        return "1";
    }

    private String type26(String type) {
        if (type.equals("Intelligence"))
            return "10";
        if (type.equals("Military - Voluntary Service"))
            return "2";
        if (type.equals("Military - Compulsory National Service"))
            return "3";
        if (type.equals("Military - Conscription"))
            return "7";
        if (type.equals("Military - Reserve"))
            return "4";
        if (type.equals("National Guard"))
            return "5";
        if (type.equals("Militia"))
            return "8";
        if (type.equals("Paramilitary"))
            return "9";
        if (type.equals("Police"))
            return "12";
        if (type.equals("Secret Police"))
            return "11";
        return "1";
    }

    private String type25(String type) {
        if (type.equals("Military"))
            return "11";
        if (type.equals("Paramilitary"))
            return "9";
        if (type.equals("Weapons/Explosives"))
            return "12";
        if (type.equals("Biological Products Manufacture"))
            return "14";
        if (type.equals("Chemical Products Manufacture"))
            return "13";
        if (type.equals("Manufacture of Weapons"))
            return "15";
        return "1";
    }
}
