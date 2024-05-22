package org.zhinanzhen.b.controller;


import com.ikasoa.core.utils.ListUtil;
import com.ikasoa.core.utils.StringUtil;
import jxl.write.Label;
import lombok.extern.log4j.Log4j;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.zhinanzhen.b.service.OfficialService;
import org.zhinanzhen.b.service.VisaOfficialService;
import org.zhinanzhen.b.service.pojo.OfficialDTO;
import org.zhinanzhen.b.service.pojo.VisaOfficialDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.service.RegionService;
import org.zhinanzhen.tb.service.pojo.RegionDTO;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/ovstOfficial")
@Log4j
public class OvstOfficialController extends BaseController {

    @Resource
    private VisaOfficialService visaOfficialService;

    @Resource
    private RegionService regionService;

    @Resource
    private OfficialService officialService;

    @RequestMapping(value = "/down", method = RequestMethod.GET)
    @ResponseBody
    public void down(@RequestParam(value = "id", required = false) Integer id,
                     @RequestParam(value = "state", required = false) String state,
                     @RequestParam(value = "startDate", required = false) String startDate,
                     @RequestParam(value = "endDate", required = false) String endDate,
                     @RequestParam(value = "startHandlingDate", required = false) String startHandlingDate,
                     @RequestParam(value = "endHandlingDate", required = false) String endHandlingDate,
                     @RequestParam(value = "regionId", required = false) Integer regionId,
                     @RequestParam(value = "officialId", required = false) Integer officialId,
                     @RequestParam(value = "userName", required = false) String userName,
                     @RequestParam(value = "applicantName", required = false) String applicantName,
                     @RequestParam(value = "settlementMonth", required = false) Integer settlementMonth,
                    HttpServletRequest request,HttpServletResponse response) {
        try {
            List<Integer> regionList = null;
            if (regionId != null && regionId > 0) {
                List<RegionDTO> regionDTOS = regionService.listRegion(regionId);
                if (!regionDTOS.isEmpty()) {
                    regionList = new ArrayList<>();
                    regionList.add(regionId);
                }
            }
            AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
            if ("WA".equals(adminUserLoginInfo.getApList())) {
                if (officialId != null && adminUserLoginInfo.isOfficialAdmin()) {
                    OfficialDTO officialById = officialService.getOfficialById(officialId);
                    if (officialById.getRegionId() != adminUserLoginInfo.getOfficialId()) {
                        String s = "该文案管理员不能查询其他地区文案佣金订单，请核验地区";
                        throw new RuntimeException(s);
                    }
                    if (regionId != null && regionId > 0 && adminUserLoginInfo.getRegionId() != regionId) {
                        String s = "该文案管理员不能查询其他地区文案佣金订单，请核验地区";
                        throw new RuntimeException(s);
                    }
                }
                officialId = adminUserLoginInfo.getOfficialId();
            }
            String name = applicantName;
            if (StringUtil.isNotEmpty(applicantName)) {
                name = applicantName.replaceAll("\\s", "");
            }
            List<VisaOfficialDTO> officialList = new ArrayList<>();
            if (settlementMonth != null) {
                // 获取当前日期
                LocalDate currentDate = LocalDate.now();
                // 获取年份
                int year = currentDate.getYear();

                // 生成这个月的第一天 00:00:00
                LocalDate firstDayOfMonth = LocalDate.of(year, settlementMonth, 1);
                LocalDateTime firstDayOfMonthDateTime = LocalDateTime.of(firstDayOfMonth, LocalTime.MIDNIGHT);

                // 生成这个月的最后一天 23:59:59
                LocalDate lastDayOfMonth = firstDayOfMonth.with(java.time.temporal.TemporalAdjusters.lastDayOfMonth());
                LocalDateTime lastDayOfMonthDateTime = LocalDateTime.of(lastDayOfMonth, LocalTime.of(23, 59, 59));

                // 格式化日期时间为 yyyy-MM-dd hh:mm:ss
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");

                String firstSettlementMonth = firstDayOfMonthDateTime.format(formatter);
                String lastSettlementMonth = lastDayOfMonthDateTime.format(formatter);

                officialList = visaOfficialService.listVisaOfficialOrder(officialId, regionList, id, startHandlingDate, endHandlingDate, state,
                        startDate, endDate, firstSettlementMonth, lastSettlementMonth, userName, name, null, null, null, null, "OVST");
            } else {
                officialList = visaOfficialService.listVisaOfficialOrder(officialId, regionList, id, startHandlingDate, endHandlingDate, state,
                        startDate, endDate, null, null, userName, name, null, null, null, null, "OVST");
            }
            response.reset();// 清空输出流
            String tableName = "official_visa_commission";
            response.setHeader("Content-disposition",
                    "attachment; filename=" + new String(tableName.getBytes("GB2312"), "8859_1") + ".xls");
            response.setContentType("application/msexcel");
            int i = 1;
            OutputStream os = response.getOutputStream();
            //获取模板
            InputStream is = this.getClass().getResourceAsStream("/officialVisa_OVST.xls");
            HSSFWorkbook wb = new HSSFWorkbook(is);
            HSSFSheet sheet = wb.getSheetAt(0);
            for (VisaOfficialDTO e : officialList) {
                HSSFRow row = sheet.createRow(i);
                // 创建一个SimpleDateFormat对象来定义日期和时间的格式
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                // 使用SimpleDateFormat的format方法将Date对象格式化为字符串
                String gmtCreate = sdf.format(e.getGmtCreate()); // 创建日期
                String kjApprovalDate = sdf.format(e.getKjApprovalDate()); // 提交申请时间
                String officialApprovalDate = sdf.format(e.getServiceOrder().getOfficialApprovalDate()); // 提交审核时间
                String finishDate = sdf.format(e.getServiceOrder().getFinishDate()); // 办理完成时间
                row.createCell(0).setCellValue(e.getId());
                row.createCell(1).setCellValue(gmtCreate);
                row.createCell(2).setCellValue(e.getServiceOrderId());
                row.createCell(3).setCellValue(kjApprovalDate);
                row.createCell(4).setCellValue(finishDate);
                row.createCell(5).setCellValue(officialApprovalDate);
                row.createCell(6).setCellValue(e.getUser().getName());
                row.createCell(7).setCellValue(e.getApplicant().get(0).getFirstname() + e.getApplicant().get(0).getSurname());
                i++;
            }
            wb.write(os);
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
}
