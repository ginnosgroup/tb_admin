//package org.zhinanzhen.tb.util;
//
//import com.ikasoa.core.utils.ListUtil;
//import com.ikasoa.core.utils.ObjectUtil;
//import com.ikasoa.core.utils.StringUtil;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.poi.hssf.usermodel.HSSFRow;
//import org.apache.poi.hssf.usermodel.HSSFSheet;
//import org.apache.poi.hssf.usermodel.HSSFWorkbook;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.zhinanzhen.b.dao.ServiceDAO;
//import org.zhinanzhen.b.dao.ServicePackageDAO;
//import org.zhinanzhen.b.dao.ServicePackagePriceDAO;
//import org.zhinanzhen.b.dao.pojo.ServiceDO;
//import org.zhinanzhen.b.dao.pojo.ServicePackageDO;
//import org.zhinanzhen.b.dao.pojo.ServicePackagePriceDO;
//import org.zhinanzhen.b.service.ServiceOrderService;
//import org.zhinanzhen.b.service.ServiceService;
//import org.zhinanzhen.b.service.VisaOfficialService;
//import org.zhinanzhen.b.service.pojo.OfficialDTO;
//import org.zhinanzhen.b.service.pojo.ServiceDTO;
//import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
//import org.zhinanzhen.b.service.pojo.VisaOfficialDTO;
//import org.zhinanzhen.tb.controller.BaseController;
//import org.zhinanzhen.tb.service.pojo.RegionDTO;
//import org.zhinanzhen.tb.utils.MD5Util;
//
//import junit.framework.TestCase;
//
//import javax.annotation.Resource;
//import java.io.FileOutputStream;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.text.SimpleDateFormat;
//import java.util.*;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//
//@SpringBootTest
//@Slf4j
//@RunWith(SpringRunner.class)
//public class MD5UtilTest extends TestCase {
//
//	@Resource
//	private VisaOfficialService visaOfficialService;
//
//	@Resource
//	private ServicePackagePriceDAO servicePackagePriceDAO;
//
//	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//	@Resource
//	private ServicePackageDAO servicePackageDao;
//
//	@Resource
//	private ServiceOrderService serviceOrderService;
//
//	@Resource
//	private ServiceDAO serviceDAO;
//
//	@Resource
//	private ServiceService serviceService;
//
//	@Test
//	public void testToMD5() throws Exception {
//		System.out.println(MD5Util.getMD5("1111"));
////		assertEquals(MD5Util.getMD5("hello1234"), "9a1996efc97181f0aee18321aa3b3b12");
//	}
//
//	public static void main(String[] args) {
//		String path = StringUtil.merge("C:\\Users\\yjt\\Desktop\\pdf\\data\\uploads\\payment_voucher_image_url_s2\\", "newFileName", ".", "type");
//		System.out.println(path);
//	}
//
//	@Test
//	public void visaOfficialDown() throws Exception {
//		try {
//			List<Integer> regionIdList = null;
//			List<VisaOfficialDTO> officialList = visaOfficialService.listVisaForDown(null, regionIdList, null, "2025-06-01 00:00:00", "2025-06-30 23:59:59", null,
//					null, null, null, null);
//			int i = 1;
//			//获取模板
//			InputStream is = this.getClass().getResourceAsStream("/officialVisa.xls");
//			HSSFWorkbook wb = new HSSFWorkbook(is);
//			HSSFSheet sheet = wb.getSheetAt(0);
//			String servicePackageType = "";
//			List<ServicePackagePriceDO> servicePackagePriceDOS = servicePackagePriceDAO.list(null, null, 0, 999);
//			Map<Integer, ServicePackagePriceDO> servicePackagePriceDOMap = servicePackagePriceDOS.stream().collect(Collectors.toMap(ServicePackagePriceDO::getServiceId, Function.identity()));
//			for (VisaOfficialDTO visaDTO : officialList) {
//				HSSFRow row = sheet.createRow(i);
//				row.createCell(0).setCellValue(visaDTO.getId());
//				row.createCell(1).setCellValue(visaDTO.getServiceOrderId());
//				if (visaDTO.getParentIdNew() != null) {
//					row.createCell(2).setCellValue(visaDTO.getParentIdNew());
//				}
//				row.createCell(3).setCellValue(visaDTO.getHandlingDate() == null ? "" : sdf.format(visaDTO.getHandlingDate()));
//				row.createCell(4).setCellValue(sdf.format(visaDTO.getServiceOrder().getGmtCreate()));
//				row.createCell(5).setCellValue(visaDTO.getUserName());
//				row.createCell(6).setCellValue(StringUtil.merge(visaDTO.getApplicant().get(0).getFirstname(), " ", visaDTO.getApplicant().get(0).getSurname()));
//				row.createCell(7).setCellValue(visaDTO.getReceiveDate() == null ? "" : sdf.format(visaDTO.getReceiveDate()));
//				row.createCell(8).setCellValue(visaDTO.getCurrency());
//				row.createCell(9).setCellValue(visaDTO.getExchangeRate());
//				row.createCell(10).setCellValue(visaDTO.getReceiveTypeName());
//				System.out.println("当前id--------------------------" + visaDTO.getId());
//				if (visaDTO.getServiceOrder().getApplicantParentId() > 0 && "SIV".equals(serviceOrderService.getServiceOrderById(visaDTO.getServiceOrder().getApplicantParentId()).getType())) {
//					String type = visaDTO.getServiceOrder().getServicePackage().getType();
//					switch (type) {
//						case "CA":
//							type = "职业评估";
//							break;
//						case "EOI":
//							type = "EOI";
//							break;
//						case "VA":
//							type = "签证申请";
//							break;
//						case "TM":
//							type = "提名";
//							break;
//						case "ZD":
//							type = "州担";
//							break;
//						default:
//							type = type;
//					}
//					if ("EOI".equalsIgnoreCase(type)) {
//						ServiceDO serviceById = serviceDAO.getServiceById(visaDTO.getServiceOrder().getServicePackage().getServiceId());
//						type = type + "-" + serviceById.getCode();
//					}
//					servicePackageType = "-" + type;
////                    servicePackageType = "-" + visaDTO.getServiceOrder().getServicePackage().getType();
//				}
//				String firstServiceName = "";
//				if (visaDTO.getServiceOrder().getService().getId() == 25 && visaDTO.getServiceOrder().getBindingOrder() != null) {
//					int servicePackageId = visaDTO.getServiceOrder().getServicePackageId();
//					ServicePackageDO servicePackageDo = servicePackageDao.getEOIServiceCode(servicePackageId);
//					if (ObjectUtil.isNotNull(servicePackageDo)) {
//						visaDTO.setServiceCode(servicePackageDo.getType() + "-" + visaDTO.getServiceCode());
//					}
//					ServiceOrderDTO serviceOrderById = serviceOrderService.getServiceOrderById(visaDTO.getServiceOrder().getBindingOrder());
//					ServiceDTO serviceById = serviceService.getServiceById(serviceOrderById.getServiceId());
//					visaDTO.getServiceOrder().getService().setName(serviceById.getName());
//				}
//				row.createCell(11).setCellValue(StringUtil.merge(visaDTO.getServiceOrder().getService().getName(), "-", visaDTO.getServiceCode(), servicePackageType));
//				servicePackageType = "";
//				ServicePackagePriceDO servicePackagePriceDO = servicePackagePriceDOMap.get(visaDTO.getServiceId());
//				if (ObjectUtil.isNotNull(servicePackagePriceDO)) {
//					row.createCell(12).setCellValue(servicePackagePriceDO.getMaxPrice());
//				}
//				row.createCell(13).setCellValue(visaDTO.getAdviserName());
//				row.createCell(14).setCellValue(visaDTO.getOfficialName());
//				row.createCell(15).setCellValue(visaDTO.getMaraName() == null ? "" : visaDTO.getMaraName());
//				row.createCell(16).setCellValue(visaDTO.getTotalPerAmountAUD());
//				row.createCell(17).setCellValue(visaDTO.getTotalAmountCNY());
//				row.createCell(18).setCellValue(visaDTO.getPredictCommissionAmount() + "");
//				row.createCell(19).setCellValue(visaDTO.getCommissionAmount() == null ? "" : visaDTO.getCommissionAmount() + "");
//				row.createCell(20).setCellValue(visaDTO.getPredictCommission() == null ? "" : visaDTO.getPredictCommission() + "");
//				row.createCell(21).setCellValue(visaDTO.getPredictCommissionCNY() == null ? "" : visaDTO.getPredictCommissionCNY() + "");
//				double extraAmount = 0.00;
//				extraAmount = visaDTO.getExtraAmount() == null ? 0 : visaDTO.getExtraAmount();
//				row.createCell(22).setCellValue(extraAmount);
//				if (extraAmount == 0) {
//					row.createCell(23).setCellValue(0);
//				} else {
//					double basicAmount = 0.00;
//					basicAmount = visaDTO.getCommissionAmount() - visaDTO.getExtraAmount();
//					if (basicAmount < 0) {
//						basicAmount = 0.00;
//					}
//					row.createCell(23).setCellValue(basicAmount);
//				}
//				ServiceOrderDTO serviceOrderById = serviceOrderService.getServiceOrderById(visaDTO.getServiceOrderId());
//				double additionalAmount2A = 0.00; // 带配偶
//				double additionalAmountXA = 0.00; // 带孩子
//				ServiceDO serviceById = serviceDAO.getServiceById(serviceOrderById.getServiceId());
//				if (ObjectUtil.isNotNull(serviceById) && serviceById.getCode().contains("500")) {
//					if ("2A".equalsIgnoreCase(serviceOrderById.getPeopleType())) {
//						additionalAmount2A = 50.00;
//					}
//					if ("XA".equalsIgnoreCase(serviceOrderById.getPeopleType())) {
//						additionalAmountXA = 25.00;
//					}
//					if ("XB".equalsIgnoreCase(serviceOrderById.getPeopleType())) {
//						additionalAmount2A = 50.00;
//						additionalAmountXA = 25.00;
//					}
//				}
//				row.createCell(24).setCellValue(additionalAmountXA);
//				row.createCell(25).setCellValue(additionalAmount2A);
//				row.createCell(26).setCellValue(additionalAmountXA / visaDTO.getExchangeRate());
//				row.createCell(27).setCellValue(additionalAmount2A / visaDTO.getExchangeRate());
//				String isInsuranceCompany = serviceOrderById.getIsInsuranceCompany();
//				row.createCell(28).setCellValue(isInsuranceCompany == null ? "" : ("1".equalsIgnoreCase(isInsuranceCompany) ? "是" : "否"));
//				row.createCell(29).setCellValue(visaDTO.getPredictCommissionCNY() == null ? 0 : visaDTO.getPredictCommissionCNY());
//				row.createCell(30).setCellValue(visaDTO.getPredictCommission() == null ? 0 : visaDTO.getPredictCommission());
//				row.createCell(31).setCellValue(visaDTO.getRefundAmount());
//				row.createCell(32).setCellValue(visaDTO.getBingDingAmount());
//				row.createCell(33).setCellValue(visaDTO.isMerged() ? "是" : "否");
//				String states = visaDTO.getState() == null ? "" : visaDTO.getState();
//				if (states.equalsIgnoreCase("REVIEW"))
//					states = "待确认";
//				row.createCell(34).setCellValue(states.equalsIgnoreCase("COMPLETE") ? "已确认" : states);
//				row.createCell(35).setCellValue(visaDTO.getStage() == null ? "" : visaDTO.getStage());
//				i++;
//			}
//
//			FileOutputStream fileOutputStream = new FileOutputStream("C:\\Users\\znz\\Desktop\\pdf\\666.xls");
//			wb.write(fileOutputStream);
//			fileOutputStream.flush();
//			fileOutputStream.close();
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			return;
//		}
//	}
//
//	@Test
//	public void visaOfficialDown2() throws Exception {
//		try {
//			// 获取数据
//			List<Integer> regionIdList = null;
//			List<VisaOfficialDTO> officialList = visaOfficialService.listVisaForDown(
//					null, regionIdList, null,
//					"2025-05-01 00:00:00", "2025-12-31 23:59:59",
//					null, null, null, null, null);
//
//			// 预加载所有需要的ServiceOrder数据
//			Set<Integer> serviceOrderIds = officialList.stream()
//					.map(VisaOfficialDTO::getServiceOrderId)
//					.collect(Collectors.toSet());
//
//			Set<Integer> parentOrderIds = officialList.stream()
//					.filter(dto -> dto.getServiceOrder() != null && dto.getServiceOrder().getApplicantParentId() > 0)
//					.map(dto -> dto.getServiceOrder().getApplicantParentId())
//					.collect(Collectors.toSet());
//
//			Set<Integer> bindingOrderIds = officialList.stream()
//					.filter(dto -> dto.getServiceOrder() != null && dto.getServiceOrder().getBindingOrder() != null)
//					.map(dto -> dto.getServiceOrder().getBindingOrder())
//					.collect(Collectors.toSet());
//
//			// 批量查询所有需要的ServiceOrder
//			Set<Integer> allOrderIds = new HashSet<>();
//			allOrderIds.addAll(serviceOrderIds);
//			allOrderIds.addAll(parentOrderIds);
//			allOrderIds.addAll(bindingOrderIds);
//
//			Map<Integer, ServiceOrderDTO> serviceOrderCache = new HashMap<>();
//			for (Integer orderId : allOrderIds) {
//				if (orderId != null && orderId > 0) {
//					serviceOrderCache.put(orderId, serviceOrderService.getServiceOrderById(orderId));
//				}
//			}
//
//			// 预加载Service数据
//			Set<Integer> serviceIds = officialList.stream()
//					.filter(dto -> dto.getServiceOrder() != null && dto.getServiceOrder().getService() != null)
//					.map(dto -> dto.getServiceOrder().getService().getId())
//					.collect(Collectors.toSet());
//
//			Map<Integer, ServiceDO> serviceCache = new HashMap<>();
//			for (Integer serviceId : serviceIds) {
//				if (serviceId != null && serviceId > 0) {
//					serviceCache.put(serviceId, serviceDAO.getServiceById(serviceId));
//				}
//			}
//
//			// Excel导出部分
//			String tableName = "official_visa_commission";
//			int i = 1;
//
//			// 获取模板
//			InputStream is = this.getClass().getResourceAsStream("/officialVisa.xls");
//			HSSFWorkbook wb = new HSSFWorkbook(is);
//			HSSFSheet sheet = wb.getSheetAt(0);
//
//			String servicePackageType = "";
//			List<ServicePackagePriceDO> servicePackagePriceDOS = servicePackagePriceDAO.list(null, null, 0, 999);
//			Map<Integer, ServicePackagePriceDO> servicePackagePriceDOMap = servicePackagePriceDOS.stream()
//					.collect(Collectors.toMap(ServicePackagePriceDO::getServiceId, Function.identity()));
//
//			// 分批处理，避免内存溢出
//			int batchSize = 1000;
//			int totalSize = officialList.size();
//
//			for (int batchStart = 0; batchStart < totalSize; batchStart += batchSize) {
//				int batchEnd = Math.min(batchStart + batchSize, totalSize);
//				List<VisaOfficialDTO> batchList = officialList.subList(batchStart, batchEnd);
//
//				for (VisaOfficialDTO visaDTO : batchList) {
//					HSSFRow row = sheet.createRow(i);
//					row.createCell(0).setCellValue(visaDTO.getId());
//					row.createCell(1).setCellValue(visaDTO.getServiceOrderId());
//					if (visaDTO.getParentIdNew() != null) {
//						row.createCell(2).setCellValue(visaDTO.getParentIdNew());
//					}
//					row.createCell(3).setCellValue(visaDTO.getHandlingDate() == null ? "" : sdf.format(visaDTO.getHandlingDate()));
//					row.createCell(4).setCellValue(sdf.format(visaDTO.getServiceOrder().getGmtCreate()));
//					row.createCell(5).setCellValue(visaDTO.getUserName());
//					row.createCell(6).setCellValue(StringUtil.merge(visaDTO.getApplicant().get(0).getFirstname(), " ", visaDTO.getApplicant().get(0).getSurname()));
//					row.createCell(7).setCellValue(visaDTO.getReceiveDate() == null ? "" : sdf.format(visaDTO.getReceiveDate()));
//					row.createCell(8).setCellValue(visaDTO.getCurrency());
//					row.createCell(9).setCellValue(visaDTO.getExchangeRate());
//					row.createCell(10).setCellValue(visaDTO.getReceiveTypeName());
////                if (ObjectUtil.isNotNull(visaDTO.getServiceOrder().getServicePackage()) && visaDTO.getServiceOrder().getApplicantParentId() > 0) {
////                    servicePackageType = "-" + visaDTO.getServiceOrder().getServicePackage().getType();
////                }
//					System.out.println("当前id--------------------------" + visaDTO.getId());
//					if (visaDTO.getServiceOrder().getApplicantParentId() > 0 && "SIV".equals(serviceOrderService.getServiceOrderById(visaDTO.getServiceOrder().getApplicantParentId()).getType())) {
//						String type = visaDTO.getServiceOrder().getServicePackage().getType();
//						switch (type) {
//							case "CA":
//								type = "职业评估";
//								break;
//							case "EOI":
//								type = "EOI";
//								break;
//							case "VA":
//								type = "签证申请";
//								break;
//							case "TM":
//								type = "提名";
//								break;
//							case "ZD":
//								type = "州担";
//								break;
//							default:
//								type = type;
//						}
//						if ("EOI".equalsIgnoreCase(type)) {
//							ServiceDO serviceById = serviceDAO.getServiceById(visaDTO.getServiceOrder().getServicePackage().getServiceId());
//							type = type + "-" + serviceById.getCode();
//						}
//						servicePackageType = "-" + type;
////                    servicePackageType = "-" + visaDTO.getServiceOrder().getServicePackage().getType();
//					}
//					String firstServiceName = "";
//					if (visaDTO.getServiceOrder().getService().getId() == 25 && visaDTO.getServiceOrder().getBindingOrder() != null) {
//						int servicePackageId = visaDTO.getServiceOrder().getServicePackageId();
//						ServicePackageDO servicePackageDo = servicePackageDao.getEOIServiceCode(servicePackageId);
//						if (ObjectUtil.isNotNull(servicePackageDo)) {
//							visaDTO.setServiceCode(servicePackageDo.getType() + "-" + visaDTO.getServiceCode());
//						}
//						ServiceOrderDTO serviceOrderById = serviceOrderService.getServiceOrderById(visaDTO.getServiceOrder().getBindingOrder());
//						ServiceDTO serviceById = serviceService.getServiceById(serviceOrderById.getServiceId());
//						visaDTO.getServiceOrder().getService().setName(serviceById.getName());
//					}
//					row.createCell(11).setCellValue(StringUtil.merge(visaDTO.getServiceOrder().getService().getName(), "-", visaDTO.getServiceCode(), servicePackageType));
//					servicePackageType = "";
//					ServicePackagePriceDO servicePackagePriceDO = servicePackagePriceDOMap.get(visaDTO.getServiceId());
//					if (ObjectUtil.isNotNull(servicePackagePriceDO)) {
//						row.createCell(12).setCellValue(servicePackagePriceDO.getMaxPrice());
//					}
//					row.createCell(13).setCellValue(visaDTO.getAdviserName());
//					row.createCell(14).setCellValue(visaDTO.getOfficialName());
//					row.createCell(15).setCellValue(visaDTO.getMaraName() == null ? "" : visaDTO.getMaraName());
//					row.createCell(16).setCellValue(visaDTO.getTotalPerAmountAUD());
//					row.createCell(17).setCellValue(visaDTO.getTotalAmountCNY());
//					row.createCell(18).setCellValue(visaDTO.getPredictCommissionAmount() + "");
//					row.createCell(19).setCellValue(visaDTO.getCommissionAmount() == null ? "" : visaDTO.getCommissionAmount() + "");
//					row.createCell(20).setCellValue(visaDTO.getPredictCommission() == null ? "" : visaDTO.getPredictCommission() + "");
//					row.createCell(21).setCellValue(visaDTO.getPredictCommissionCNY() == null ? "" : visaDTO.getPredictCommissionCNY() + "");
//					double extraAmount = 0.00;
//					extraAmount = visaDTO.getExtraAmount() == null ? 0 : visaDTO.getExtraAmount();
//					row.createCell(22).setCellValue(extraAmount);
//					if (extraAmount == 0) {
//						row.createCell(23).setCellValue(0);
//					} else {
//						double basicAmount = 0.00;
//						basicAmount = visaDTO.getCommissionAmount() - visaDTO.getExtraAmount();
//						if (basicAmount < 0) {
//							basicAmount = 0.00;
//						}
//						row.createCell(23).setCellValue(basicAmount);
//					}
//					ServiceOrderDTO serviceOrderById = serviceOrderService.getServiceOrderById(visaDTO.getServiceOrderId());
//					double additionalAmount2A = 0.00; // 带配偶
//					double additionalAmountXA = 0.00; // 带孩子
//					ServiceDO serviceById = serviceDAO.getServiceById(serviceOrderById.getServiceId());
//					if (ObjectUtil.isNotNull(serviceById) && serviceById.getCode().contains("500")) {
//						if ("2A".equalsIgnoreCase(serviceOrderById.getPeopleType())) {
//							additionalAmount2A = 50.00;
//						}
//						if ("XA".equalsIgnoreCase(serviceOrderById.getPeopleType())) {
//							additionalAmountXA = 25.00;
//						}
//						if ("XB".equalsIgnoreCase(serviceOrderById.getPeopleType())) {
//							additionalAmount2A = 50.00;
//							additionalAmountXA = 25.00;
//						}
//					}
//					row.createCell(24).setCellValue(additionalAmountXA);
//					row.createCell(25).setCellValue(additionalAmount2A);
//					row.createCell(26).setCellValue(additionalAmountXA / visaDTO.getExchangeRate());
//					row.createCell(27).setCellValue(additionalAmount2A / visaDTO.getExchangeRate());
//					String isInsuranceCompany = serviceOrderById.getIsInsuranceCompany();
//					row.createCell(28).setCellValue(isInsuranceCompany == null ? "" : ("1".equalsIgnoreCase(isInsuranceCompany) ? "是" : "否"));
//					row.createCell(29).setCellValue(visaDTO.getPredictCommissionCNY() == null ? 0 : visaDTO.getPredictCommissionCNY());
//					row.createCell(30).setCellValue(visaDTO.getPredictCommission() == null ? 0 : visaDTO.getPredictCommission());
//					row.createCell(31).setCellValue(visaDTO.getRefundAmount());
//					row.createCell(32).setCellValue(visaDTO.getBingDingAmount());
//					row.createCell(33).setCellValue(visaDTO.isMerged() ? "是" : "否");
//					String states = visaDTO.getState() == null ? "" : visaDTO.getState();
//					if (states.equalsIgnoreCase("REVIEW"))
//						states = "待确认";
//					row.createCell(34).setCellValue(states.equalsIgnoreCase("COMPLETE") ? "已确认" : states);
//					row.createCell(35).setCellValue(visaDTO.getStage() == null ? "" : visaDTO.getStage());
//
//					// 使用缓存的数据
//					ServiceOrderDTO currentOrder = serviceOrderCache.get(visaDTO.getServiceOrderId());
//					if (currentOrder != null) {
//						visaDTO.setServiceOrder(currentOrder);
//					}
//
//					// 处理parent order
//					if (visaDTO.getServiceOrder() != null && visaDTO.getServiceOrder().getApplicantParentId() > 0) {
//						ServiceOrderDTO parentOrder = serviceOrderCache.get(visaDTO.getServiceOrder().getApplicantParentId());
//						if (parentOrder != null && "SIV".equals(parentOrder.getType())) {
//							// ... 处理逻辑
//						}
//					}
//
//					i++;
//
//					// 每处理1000条打印一次进度
//					if (i % 1000 == 0) {
//						System.out.println("已处理 " + i + " 条数据...");
//					}
//				}
//			}
//
//			// 使用SXSSFWorkbook处理大文件（如果数据量很大）
//			FileOutputStream fileOutputStream = new FileOutputStream("C:\\Users\\znz\\Desktop\\pdf\\666.xls");
//			wb.write(fileOutputStream);
//			fileOutputStream.flush();
//			fileOutputStream.close();
//
//			System.out.println("导出完成，总行数：" + i);
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//}
