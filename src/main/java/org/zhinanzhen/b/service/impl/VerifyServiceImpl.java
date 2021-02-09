package org.zhinanzhen.b.service.impl;

import com.ikasoa.core.utils.StringUtil;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhinanzhen.b.dao.CommissionOrderDAO;
import org.zhinanzhen.b.dao.VerifyDao;
import org.zhinanzhen.b.dao.VisaDAO;
import org.zhinanzhen.b.dao.pojo.CommissionOrderDO;
import org.zhinanzhen.b.dao.pojo.FinanceBankDO;
import org.zhinanzhen.b.dao.pojo.FinanceCodeDO;
import org.zhinanzhen.b.dao.pojo.VisaDO;
import org.zhinanzhen.b.service.VerifyService;
import org.zhinanzhen.b.service.pojo.FinanceBankCodeDTO;
import org.zhinanzhen.b.service.pojo.FinanceBankDTO;
import org.zhinanzhen.b.service.pojo.FinanceCodeDTO;
import org.zhinanzhen.b.service.pojo.RegionBankDTO;
import org.zhinanzhen.tb.dao.AdminUserDAO;
import org.zhinanzhen.tb.dao.RegionDAO;
import org.zhinanzhen.tb.dao.pojo.AdminUserDO;
import org.zhinanzhen.tb.dao.pojo.RegionDO;
import org.zhinanzhen.tb.service.AdviserStateEnum;
import org.zhinanzhen.tb.service.pojo.UserDTO;
import org.zhinanzhen.tb.dao.AdviserDAO;
import org.zhinanzhen.tb.dao.UserDAO;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
import org.zhinanzhen.tb.dao.pojo.UserDO;
import org.zhinanzhen.tb.service.pojo.AdviserDTO;

import javax.annotation.Resource;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Date: 2020/12/18 13:37
 * Description:
 * Version: V1.0
 */
@Service
public class VerifyServiceImpl implements VerifyService {

    @Resource
    private  VerifyDao verifyDao;

    @Resource
    private CommissionOrderDAO commissionOrderDAO;

    private Mapper mapper = new DozerBeanMapper();

    @Resource
    private AdviserDAO adviserDao;

    @Resource
    private UserDAO userDAO;

    @Resource
    private VisaDAO visaDAO;

    @Resource
    private RegionDAO regionDAO;

    @Resource
    private AdminUserDAO adminUserDAO;


    @Override
    @Transactional
    public List<FinanceCodeDO> excelToList(InputStream inputStream, String fileName) throws Exception {
        List<FinanceCodeDO> financeCodeDOS = new ArrayList<>();
        //double money = 0.00;
        //double balance = 0.00;
        Workbook workbook = null;
        try {
            boolean isExcel2003 = true;
            if (fileName.matches("^.+\\.(?i)(xlsx)$")) {
                isExcel2003 = false;
            }
            if (isExcel2003) {
                workbook = new HSSFWorkbook(inputStream);
                //System.out.println("HSSFWorkbook");
            } else {
                workbook = new XSSFWorkbook(inputStream);
                //System.out.println("XSSFWorkbook");
            }
            //关闭 Stream
            inputStream.close();
            //工作表对象
            Sheet sheet = workbook.getSheetAt(0);

            //总行数
            int rowLength = sheet.getLastRowNum();
            //System.out.println(rowLength);
            //工作表的列
            Row row = sheet.getRow(0);
            //总列数
            int colLength = row.getLastCellNum();
            //System.out.println("colLength"+colLength);
            //得到指定的单元格
            Cell cell = null;
            //从第二行的数据开始
            for (int r = 1; r <= rowLength; r++) {
                FinanceCodeDO financeCodeDO = new FinanceCodeDO();
                row = sheet.getRow(r);
                Object columnValue = null;

                DecimalFormat df = new DecimalFormat("0.00");// 格式化 number
                SimpleDateFormat sdfParse = new SimpleDateFormat("dd/MM/yyyy");// 格式化日期字符串

                if (row.getCell(0) == null){
                    throw new Exception("入账日期有误,行数:"+r+1);
                }

                financeCodeDO.setBankDate(sdfParse.parse(row.getCell(0).getStringCellValue()));
                financeCodeDO.setIncome(row.getCell(1).getNumericCellValue() > 0);
                financeCodeDO.setMoney(Double.parseDouble(df.format(row.getCell(1).getNumericCellValue())));
                financeCodeDO.setComment(row.getCell(2).getStringCellValue());
                financeCodeDO.setBalance(Double.parseDouble(df.format(row.getCell(3).getNumericCellValue())));

                if (row.getCell(4) != null && StringUtil.isNotEmpty(row.getCell(4).getStringCellValue())) {
                    String orderId = row.getCell(4).getStringCellValue();
                    //if (id.substring(0,2).equalsIgnoreCase("CV")){
                    //   financeCodeDO.setVisaId(Integer.parseInt(id.substring(2)));
                    //}
                    //if (id.substring(0,2).equalsIgnoreCase("CS")){
                    //    financeCodeDO.setCommissionOrderId(Integer.parseInt(id.substring(2)));
                    //}
                    financeCodeDO.setOrderId(orderId);
                }else if (StringUtil.isNotEmpty(checkVerifyCode(financeCodeDO.getComment()))){
                    String comment = financeCodeDO.getComment();
                    //得到 verifyCode 并且字符全部转换成大写
                    String verifyCode = checkVerifyCode(comment).toUpperCase();

                    List<VisaDO> visaDOS = visaDAO.listVisaByVerifyCode(verifyCode);
                    List<CommissionOrderDO> commissionOrderDOS = commissionOrderDAO.listCommissionOrderByVerifyCode(verifyCode);
                    if (visaDOS.size() > 1 | commissionOrderDOS.size() > 1)
                        throw new Exception("存在两个佣金订单对账code为:" + verifyCode + "!");
                    if (visaDOS.size() > 0) { //visaDOS 判断list是否有数据
                        VisaDO visaDO = visaDOS.get(0);
                        if (visaDO != null) {
                            visaDO.setBankDate(financeCodeDO.getBankDate());
                            if (visaDO.getAmount()==financeCodeDO.getMoney())
                                visaDO.setChecked(true);
                            visaDO.setBankCheck("Code");
                            if (visaDAO.updateVisa(visaDO) > 0)
                                financeCodeDO.setOrderId("CV" + visaDO.getId());
                        }
                    }
                    if (commissionOrderDOS.size() > 0) { //commissionOrderDOS 判断list是否有数据
                        CommissionOrderDO commissionOrderDO = commissionOrderDOS.get(0);
                        if (commissionOrderDO != null) {
                            commissionOrderDO.setBankDate(financeCodeDO.getBankDate());
                            if (commissionOrderDO.getAmount()==financeCodeDO.getMoney())
                                commissionOrderDO.setChecked(true);
                            commissionOrderDO.setBankCheck("Code");
                            if (commissionOrderDAO.updateCommissionOrder(commissionOrderDO) > 0)
                                financeCodeDO.setOrderId("CS" + commissionOrderDO.getId());
                        }
                    }
                }
                //balance = financeCodeDO.getBalance()+balance;
                //money = financeCodeDO.getMoney() + money;
                financeCodeDOS.add(financeCodeDO);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return financeCodeDOS;
    }

    public  String checkVerifyCode(String str){
        if (StringUtil.isEmpty(str))
            return null;
        for (regionEnum e : regionEnum.values()){
            if (str.contains(e.toString()) && str.contains("ZNZ")){
                return str.substring(str.indexOf(e.toString()),str.lastIndexOf("ZNZ")+3);
            }
        }
        return  null;
    }

    @Override
    @Transactional
    public int add(List<FinanceCodeDO> financeCodeDOS) {
        if (financeCodeDOS.size()>0)
            verifyDao.add(financeCodeDOS);
        return 1;
    }

    @Override
    public int count(String bankDateStart, String bankDateEnd) {
        return verifyDao.count(bankDateStart,bankDateEnd);
    }

    @Override
    public List<FinanceCodeDTO> list(String bankDateStart, String bankDateEnd, Integer pageSize, Integer pageNumber) {
        List<FinanceCodeDO> financeCodeDOS = verifyDao.list(bankDateStart,bankDateEnd,pageSize,pageNumber * pageSize);
        List<FinanceCodeDTO> financeCodeDTOS  = new ArrayList<>();
        if ( financeCodeDOS != null){
            financeCodeDOS.forEach(financeCodeDO -> {
                financeCodeDTOS.add(mapper.map(financeCodeDO,FinanceCodeDTO.class));
            });
        }
        if (financeCodeDTOS!= null){
            financeCodeDTOS.forEach(financeCodeDTO -> {
                if (financeCodeDTO.getAdviserId() > 0 ){
                    AdviserDO adviserDO = adviserDao.getAdviserById(financeCodeDTO.getAdviserId());
                    if (adviserDO!= null){
                        financeCodeDTO.setAdviser(mapper.map(adviserDO,AdviserDTO.class));
                        RegionDO regionDO = regionDAO.getRegionById(adviserDO.getRegionId());
                        if (regionDO!=null)
                            financeCodeDTO.getAdviser().setRegionName(regionDO.getName());
                    }
                }
                if (financeCodeDTO.getUserId() > 0 ){
                    UserDO userDO = userDAO.getUserById(financeCodeDTO.getUserId());
                    if (userDO!=null)
                        financeCodeDTO.setUser(mapper.map(userDO,UserDTO.class));
                }
            });
        }
        return financeCodeDTOS;
    }

    @Override
    public int update(FinanceCodeDO financeCodeDO) {
        return verifyDao.update(financeCodeDO);
    }

    @Override
    public List<RegionBankDTO> regionList() {
        List<RegionDO> regionDOS = regionDAO.regionList();
        List<RegionBankDTO> regionBankDTOS = new ArrayList<>();
        regionDOS.forEach(regionDO -> {
            regionBankDTOS.add(mapper.map(regionDO,RegionBankDTO.class));
        });
        regionBankDTOS.forEach(regionBankDTO -> {
            regionBankDTO.setFinanceBankDO(verifyDao.getFinanceBankById(regionBankDTO.getFinanceBankId()));
        });
        return regionBankDTOS;
    }

    @Override
    public RegionDO regionById(int id) {
        return verifyDao.regionById(id);
    }

    @Override
    public List<FinanceBankDTO> bankList(Integer pageNumber, Integer pageSize) {
        List<FinanceBankDO> financeBankDOS = verifyDao.bankList(pageNumber*pageSize,pageSize);
        List<FinanceBankDTO> financeBankDTOS = new ArrayList<>();
        if (financeBankDOS!= null)
            financeBankDOS.forEach(financeBankDO -> {
                financeBankDTOS.add(mapper.map(financeBankDO,FinanceBankDTO.class));
            });
        return financeBankDTOS;
    }

    @Override
    public int bankCount() {
        return verifyDao.bankCount();
    }

    @Override
    public FinanceBankCodeDTO getPaymentCode(Integer adviserId) {
        boolean flag = true;
        FinanceBankCodeDTO financeBankCodeDTO = new FinanceBankCodeDTO();
        List<RegionDO> regionDOParentList = regionDAO.selectByParent();
        AdviserDO adviserDO = adviserDao.getAdviserById(adviserId);
        if (adviserDO!=null){
            if (adviserDO.getRegionId()>0){
                RegionDO regionDO =  regionDAO.getRegionById(adviserDO.getRegionId());
                if (regionDO!=null){
                    FinanceBankDO financeBankDO = verifyDao.getFinanceBankById(regionDO.getFinanceBankId());
                    if (financeBankDO!=null)
                    financeBankCodeDTO=mapper.map(financeBankDO,FinanceBankCodeDTO.class);
                    String code = "";
                    while (flag){
                        for (RegionDO regionParent : regionDOParentList){
                            if (regionParent.getId() == regionDO.getId() || (regionDO.getParentId()== null ? 0 : regionDO.getParentId()) == regionParent.getId()){
                                if (regionParent.getName().equalsIgnoreCase(regionEnum.SYD.value)){
                                    code = regionEnum.SYD.toString();
                                    break;
                                }
                                if (regionParent.getName().equalsIgnoreCase(regionEnum.ADE.value)) {
                                    code = regionEnum.ADE.toString();
                                    break;
                                }
                                if (regionParent.getName().equalsIgnoreCase(regionEnum.TAS.value)){
                                    code = regionEnum.TAS.toString();
                                    break;
                                }
                                if (regionParent.getName().equalsIgnoreCase(regionEnum.BNE.value)){
                                    code = regionEnum.BNE.toString();
                                    break;
                                }
                                if (regionParent.getName().equalsIgnoreCase(regionEnum.BJ.value)){
                                    code = regionEnum.BJ.toString();
                                    break;
                                }
                                if (regionParent.getName().equalsIgnoreCase(regionEnum.MEL.value)){
                                    code = regionEnum.MEL.toString();
                                    break;
                                }
                            }
                        }
                        String adviserName = adviserDO.getName();
                        code= code + (adviserName.contains(".")? adviserName.substring(0,adviserName.indexOf(".")) : adviserName ) +  RandomStringUtils.randomAlphanumeric(6) + "ZNZ";
                        code = code.toUpperCase();
                        if (commissionOrderDAO.listCommissionOrderByVerifyCode(code).size()==0 && visaDAO.listVisaByVerifyCode(code).size()==0){
                            flag = false;
                        }
                    }
                    financeBankCodeDTO.setCode(code.replaceAll(" ",""));
                    financeBankCodeDTO.setRegionDO(regionDO);
                }
            }
        }
        return financeBankCodeDTO;
    }

    @Override
    public int bankUpdate(FinanceBankDO financeBankDO) {
        return verifyDao.bankUpdate(financeBankDO);
    }

    @Override
    public FinanceCodeDTO financeCodeByOrderId(String orderId) {
        List<FinanceCodeDO> financeCodeDOS = verifyDao.financeCodeByOrderId(orderId);
        FinanceCodeDTO financeCodeDTO = new FinanceCodeDTO();
        if (financeCodeDOS .size() > 0){
            FinanceCodeDO  financeCodeDO = financeCodeDOS.get(0);
            financeCodeDTO = mapper.map(financeCodeDO,FinanceCodeDTO.class);
            if (financeCodeDO != null) {
                if (financeCodeDO.getAdviserId() > 0) {
                    AdviserDO adviserDO = adviserDao.getAdviserById(financeCodeDTO.getAdviserId());
                    if (adviserDO != null)
                        financeCodeDTO.setAdviser(mapper.map(adviserDO, AdviserDTO.class));
                }
                if (financeCodeDO.getUserId() > 0) {
                    UserDO userDO = userDAO.getUserById(financeCodeDTO.getUserId());
                    if (userDO != null)
                        financeCodeDTO.setUser(mapper.map(userDO, UserDTO.class));
                }
            }
        }
        return financeCodeDTO;
    }

    @Override
    public List<AdviserDTO> adviserList(Integer id) {
        List<AdviserDO> adviserDOS = adviserDao.listAdviserByRegionId(id, AdviserStateEnum.ENABLED.toString());
        List<AdviserDTO> adviserDTOS = new ArrayList<>();
        if (adviserDOS !=null ){
            for (AdviserDO adviserDo : adviserDOS) {
                AdviserDTO adviserDTO = mapper.map(adviserDo, AdviserDTO.class);
                if (StringUtil.isNotEmpty(adviserDo.getState())) {
                    adviserDTO.setState(AdviserStateEnum.get(adviserDo.getState()));
                }
                RegionDO regionDo = regionDAO.getRegionById(adviserDo.getRegionId());
                if (regionDo != null) {
                    adviserDTO.setRegionName(regionDo.getName());
                    adviserDTO.setRegionDo(regionDo);
                }
                AdminUserDO adminUserDo = adminUserDAO.getAdminUserByAdviserId(adviserDo.getId());
                if (adminUserDo != null && adminUserDo.getRegionId() != null) {
                    adviserDTO.setAdminRegionId(adminUserDo.getRegionId());
                    RegionDO adminRegionDo = regionDAO.getRegionById(adminUserDo.getRegionId());
                    if (adminRegionDo != null)
                        adviserDTO.setAdminRegionName(adminRegionDo.getName());
                }
                adviserDTOS.add(adviserDTO);
            }
        }
        return adviserDTOS;
    }

    @Override
    public int updateFinanceBankId(Integer id, Integer financeBankId) {

        return regionDAO.updateFinanceBankId(id,financeBankId);
    }

    @Override
    public int addBank(FinanceBankDO financeBankDO) {
        return verifyDao.addBank(financeBankDO);
    }

    @Override
    public FinanceCodeDO financeCodeById(Integer id) {
        return verifyDao.financeCodeById(id);
    }

    public enum regionEnum {
        SYD("NSW & ACT"),BNE("QLD"),TAS("TAS"),MEL("VIC"),ADE("SA"),BJ("北京");

        private String value;
        private regionEnum (String value) {
            this.value = value;
        }

        public static regionEnum get(String name) {
            for (regionEnum e : regionEnum.values())
                if (e.toString().equals(name))
                    return e;
            return null;
        }
    }
}
