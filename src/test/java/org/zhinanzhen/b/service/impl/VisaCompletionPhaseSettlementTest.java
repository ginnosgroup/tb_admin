package org.zhinanzhen.b.service.impl;

import org.dozer.DozerBeanMapper;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.zhinanzhen.b.dao.*;
import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.service.VisaCompletionPhase;
import org.zhinanzhen.b.service.ExchangeRateService;
import org.zhinanzhen.b.service.PortalFollowUpState;
import org.zhinanzhen.b.service.pojo.VisaOfficialDTO;
import org.zhinanzhen.tb.dao.RegionDAO;
import org.zhinanzhen.tb.dao.pojo.RegionDO;
import org.zhinanzhen.tb.service.ServiceException;

import java.util.*;
import java.io.InputStream;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class VisaCompletionPhaseSettlementTest {
    private VisaOfficialServiceImpl service;
    private VisaOfficialDao officialVisaDao;
    private VisaDAO visaDao;
    private ServiceOrderDAO orders;
    private final Map<String, VisaOfficialDO> saved = new LinkedHashMap<>();

    @Before
    public void setUp() {
        service = new VisaOfficialServiceImpl();
        officialVisaDao = mock(VisaOfficialDao.class);
        visaDao = mock(VisaDAO.class);
        orders = mock(ServiceOrderDAO.class);
        OfficialDAO officials = mock(OfficialDAO.class);
        RegionDAO regions = mock(RegionDAO.class);
        OfficialGradeDao grades = mock(OfficialGradeDao.class);
        ServicePackagePriceDAO prices = mock(ServicePackagePriceDAO.class);
        ServiceDAO services = mock(ServiceDAO.class);
        ReflectionTestUtils.setField(service, "mapper", new DozerBeanMapper());
        ReflectionTestUtils.setField(service, "visaOfficialDao", officialVisaDao);
        ReflectionTestUtils.setField(service, "visaDAO", visaDao);
        ReflectionTestUtils.setField(service, "serviceOrderDao", orders);
        ReflectionTestUtils.setField(service, "serviceOrderDAO", orders);
        ReflectionTestUtils.setField(service, "officialDao", officials);
        ReflectionTestUtils.setField(service, "officialDAO", officials);
        ReflectionTestUtils.setField(service, "regionDAO", regions);
        ReflectionTestUtils.setField(service, "officialGradeDao", grades);
        ReflectionTestUtils.setField(service, "servicePackagePriceDAO", prices);
        ReflectionTestUtils.setField(service, "serviceDao", services);
        ReflectionTestUtils.setField(service, "servicePackageDAO", mock(ServicePackageDAO.class));
        ReflectionTestUtils.setField(service, "refundDAO", mock(RefundDAO.class));
        ReflectionTestUtils.setField(service, "serviceOrderManageDAO", mock(ServiceOrderManageDAO.class));

        ServiceOrderDO order = new ServiceOrderDO();
        order.setId(81);
        order.setServiceId(20);
        order.setOfficialId(12);
        order.setCurrency("AUD");
        order.setExchangeRate(5);
        order.setType("VISA");
        order.setCompletionPhase("PSA");
        when(orders.getServiceOrderById(81)).thenReturn(order);
        when(orders.updateCompletionPhase(anyInt(), anyString())).thenAnswer(invocation -> {
            int serviceOrderId = (Integer) invocation.getArguments()[0];
            ServiceOrderDO target = orders.getServiceOrderById(serviceOrderId);
            if (target == null)
                return 0;
            target.setCompletionPhase((String) invocation.getArguments()[1]);
            return 1;
        });
        when(orders.listBybindingOrder(anyInt())).thenReturn(Collections.<Integer>emptyList());
        when(officialVisaDao.lockCompletionPhaseOrder(81)).thenReturn(81);
        OfficialDO official = new OfficialDO();
        official.setId(12);
        official.setGradeId(100001);
        official.setRegionId(9);
        when(officials.getOfficialById(12)).thenReturn(official);
        RegionDO region = new RegionDO();
        region.setName("Sydney");
        when(regions.getRegionById(9)).thenReturn(region);
        OfficialGradeDO grade = new OfficialGradeDO();
        grade.setId(100001);
        grade.setGrade("资深");
        grade.setGmtModify(new Date());
        when(grades.getOfficialGradeById(100001)).thenReturn(grade);
        ServicePackagePriceDO price = new ServicePackagePriceDO();
        price.setRulerV2("[{\"officialGrades\":\"100001\",\"ruler\":0,\"rate\":10}]");
        price.setGmtModify(new Date());
        when(prices.getByServiceId(20)).thenReturn(price);
        ServiceDO assessment = new ServiceDO();
        assessment.setCode("职评");
        when(services.getServiceById(20)).thenReturn(assessment);
        when(visaDao.listVisaByServiceOrderId(81)).thenReturn(Arrays.asList(
                receipt(1, 550, "AUD", 5), receipt(2, 2750, "CNY", 5)));

        for (VisaCompletionPhase phase : VisaCompletionPhase.values()) {
            int orderId = 81 + phase.ordinal();
            ServiceOrderDO phaseOrder = new DozerBeanMapper().map(order, ServiceOrderDO.class);
            phaseOrder.setId(orderId);
            phaseOrder.setCompletionPhase(phase.name());
            when(orders.getServiceOrderById(orderId)).thenReturn(phaseOrder);
            when(officialVisaDao.lockCompletionPhaseOrder(orderId)).thenReturn(orderId);
            when(visaDao.listVisaByServiceOrderId(orderId)).thenReturn(Arrays.asList(
                    receipt(orderId * 2, 550, "AUD", 5), receipt(orderId * 2 + 1, 2750, "CNY", 5)));
        }
        when(officialVisaDao.countVisaForOrderAndStageExcludingId(anyInt(), anyString(), anyInt())).thenAnswer(invocation -> {
            String key = invocation.getArguments()[0] + ":" + invocation.getArguments()[1];
            VisaOfficialDO existing = saved.get(key);
            return existing != null && existing.getId() != (Integer) invocation.getArguments()[2] ? 1 : 0;
        });
        when(officialVisaDao.addVisa(any(VisaOfficialDO.class))).thenAnswer(invocation -> {
            VisaOfficialDO value = (VisaOfficialDO) invocation.getArguments()[0];
            value.setId(100 + saved.size());
            saved.put(value.getServiceOrderId() + ":" + value.getStage(), value);
            return 1;
        });
    }

    @Test
    public void addInterfaceUpdatesServiceOrderAndSettlesEveryPhase() throws Exception {
        double[] gross = {110, 330, 440, 220};
        double fullReceipt = 1100;
        double fullCommissionBase = 1000;
        double[] base = {100, 300, 400, 200};
        double[] commission = {10, 30, 40, 20};
        for (VisaCompletionPhase phase : VisaCompletionPhase.values()) {
            int i = phase.ordinal();
            VisaOfficialDTO dto = request(81);
            dto.setStage("IGNORED"); // 佣金请求不能覆盖服务订单的结算阶段。
            assertTrue(service.addVisa(dto, phase.name().toLowerCase(Locale.ENGLISH)) > 0);
            assertEquals(81, dto.getServiceOrderId());
            assertEquals(phase.name(), dto.getStage());
            assertEquals(fullReceipt, dto.getAmount(), 0.001);
            assertEquals(fullReceipt, dto.getPerAmount(), 0.001);
            assertEquals(fullCommissionBase, dto.getCommissionAmount(), 0.001);
            assertEquals(fullCommissionBase, dto.getPredictCommissionAmount(), 0.001);
            assertEquals(commission[i], dto.getPredictCommission(), 0.001);
            assertEquals(gross[i], dto.getExpectAmount(), 0.001);
        }
        assertEquals(4, saved.size());
        verify(orders, times(4)).updateCompletionPhase(eq(81), anyString());
    }

    @Test
    public void samePhaseCannotRepeatButNextPhaseCanSettle() throws Exception {
        service.addVisa(request(81), "PSA");
        try {
            service.addVisa(request(81), "PSA");
            fail("same phase must not generate another commission");
        } catch (ServiceException expected) {
            assertTrue(expected.getMessage().contains("不能重复生成"));
        }
        assertTrue(service.addVisa(request(81), "JRE") > 0);
        verify(officialVisaDao, times(2)).addVisa(any(VisaOfficialDO.class));
    }

    @Test
    public void invalidStoredPhaseAndInvalidRateDoNotWriteCommission() throws Exception {
        try {
            service.addVisa(request(81), "INVALID");
            fail("invalid request phase");
        } catch (ServiceException expected) {
            assertTrue(expected.getMessage().contains("completionPhase"));
        }
        when(visaDao.listVisaByServiceOrderId(81)).thenReturn(Collections.singletonList(receipt(1, 500, "CNY", 0)));
        try {
            service.addVisa(request(81), "PSA");
            fail("zero receipt exchange rate");
        } catch (ServiceException expected) {
            assertTrue(expected.getMessage().contains("汇率"));
        }
        verify(officialVisaDao, never()).addVisa(any(VisaOfficialDO.class));
    }

    @Test
    public void nullStoredPhaseKeepsLegacyDuplicateRule() throws Exception {
        orders.getServiceOrderById(81).setCompletionPhase(null);
        when(officialVisaDao.countVisaByServiceOrderIdAndExcludeCode(eq(81), anyString())).thenReturn(1);
        try {
            service.addVisa(request(81));
            fail("legacy duplicate rule");
        } catch (ServiceException expected) {
            assertTrue(expected.getMessage().contains("已创建过佣金订单"));
        }
        verify(officialVisaDao, never()).lockCompletionPhaseOrder(anyInt());
        verify(officialVisaDao, never()).addVisa(any(VisaOfficialDO.class));
    }

    @Test
    public void refundRecalculationUsesFullBaseOnlyOnce() throws Exception {
        VisaOfficialDTO dto = request(82);
        service.addVisa(dto, "JRE");
        when(officialVisaDao.getOne(dto.getId())).thenReturn(saved.get("82:JRE"));
        when(officialVisaDao.updateVisaOfficial(any(VisaOfficialDO.class))).thenReturn(1);
        dto.setIsRefund(true);
        service.addVisa(dto);
        assertEquals(1000, dto.getCommissionAmount(), 0.001);
        assertEquals(1000, dto.getPredictCommissionAmount(), 0.001);
        assertEquals(30, dto.getPredictCommission(), 0.001);
        verify(officialVisaDao, times(1)).addVisa(any(VisaOfficialDO.class));
        verify(officialVisaDao, times(1)).updateVisaOfficial(any(VisaOfficialDO.class));
    }

    @Test
    public void fixedRuleIsAllocatedByServiceOrderPhase() throws Exception {
        ServicePackagePriceDAO prices = (ServicePackagePriceDAO) ReflectionTestUtils.getField(service, "servicePackagePriceDAO");
        prices.getByServiceId(20).setRulerV2("[{\"officialGrades\":\"100001\",\"ruler\":1,\"amount\":200}]");
        VisaOfficialDTO dto = request(83);
        service.addVisa(dto, "JRWA");
        assertEquals(80, dto.getPredictCommission(), 0.001);
    }

    @Test
    public void domesticCnyAmountUsesSavedCommissionExchangeRate() throws Exception {
        RegionDAO regions = (RegionDAO) ReflectionTestUtils.getField(service, "regionDAO");
        orders.getServiceOrderById(81).setCurrency("CNY");
        regions.getRegionById(9).setName("成都");
        ExchangeRateService exchange = mock(ExchangeRateService.class);
        when(exchange.getQuarterExchangeRate()).thenReturn(4.8);
        ReflectionTestUtils.setField(service, "exchangeRateService", exchange);
        VisaOfficialDTO dto = request(81);
        service.addVisa(dto, "PSA");
        assertEquals(4.8, dto.getExchangeRate(), 0.001);
        assertEquals(5280, dto.getAmount(), 0.001);
        assertEquals(1100, dto.getAmount() / dto.getExchangeRate(), 0.001);
        assertEquals(10, dto.getPredictCommission(), 0.001);
    }

    @Test
    public void exportUsesServiceOrderHierarchyAndStoredPhase() {
        VisaOfficialExportServiceDO item = new VisaOfficialExportServiceDO();
        item.setServiceName("签证");
        item.setServiceCode("职评");
        item.setCategoryName("职业分类");
        item.setAssessName("评估名称");
        item.setCompletionPhase("JRWA");
        assertEquals("签证-职评-职业分类-评估名称-JRWA", item.serviceItem());
        item.setCompletionPhase(null);
        assertEquals("签证-职评-职业分类-评估名称", item.serviceItem());
    }

    @Test
    public void myBatisPersistsPhaseOnlyOnServiceOrderAndExportsThroughJoin() throws Exception {
        Configuration configuration = new Configuration();
        for (String resource : Arrays.asList("sqlmap/BVisaOfficialDAO.xml", "sqlmap/BServiceOrderDAO.xml")) {
            try (InputStream input = getClass().getClassLoader().getResourceAsStream(resource)) {
                assertNotNull(input);
                new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments()).parse();
            }
        }
        String visaNamespace = "org.zhinanzhen.b.dao.VisaOfficialDao.";
        String orderNamespace = "org.zhinanzhen.b.dao.ServiceOrderDAO.";
        ServiceOrderDO order = orders.getServiceOrderById(81);
        BoundSql orderInsert = configuration.getMappedStatement(orderNamespace + "addServiceOrder").getBoundSql(order);
        assertFalse(orderInsert.getSql().contains("completion_phase"));
        assertFalse(orderInsert.getParameterMappings().stream().anyMatch(p -> "completionPhase".equals(p.getProperty())));
        BoundSql orderUpdate = configuration.getMappedStatement(orderNamespace + "updateServiceOrder").getBoundSql(order);
        assertFalse(orderUpdate.getSql().contains("completion_phase = ?"));
        Map<String, Object> phaseParameters = new HashMap<>();
        phaseParameters.put("serviceOrderId", 81);
        phaseParameters.put("completionPhase", "PSA");
        BoundSql phaseUpdate = configuration.getMappedStatement(orderNamespace + "updateCompletionPhase")
                .getBoundSql(phaseParameters);
        assertTrue(phaseUpdate.getSql().contains("completion_phase = ?"));
        assertEquals(2, phaseUpdate.getParameterMappings().size());
        BoundSql visaInsert = configuration.getMappedStatement(visaNamespace + "addVisa").getBoundSql(new VisaOfficialDO());
        assertFalse(visaInsert.getSql().contains("completion_phase"));
        BoundSql visaUpdate = configuration.getMappedStatement(visaNamespace + "updateVisaOfficial").getBoundSql(new VisaOfficialDO());
        assertFalse(visaUpdate.getSql().contains("completion_phase"));
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("serviceOrderIds", Arrays.asList(81, 82));
        parameters.put("ids", Arrays.asList(81, 82));
        BoundSql export = configuration.getMappedStatement(visaNamespace + "listAssessmentExportServices").getBoundSql(parameters);
        assertTrue(export.getSql().contains("so.completion_phase"));
        assertEquals(2, export.getParameterMappings().size());
        for (String statement : Arrays.asList("listServiceOrder", "getServiceOrderById", "listByIds", "getDeriveOrder")) {
            assertTrue(statement, configuration.getMappedStatement(orderNamespace + statement)
                    .getBoundSql(parameters).getSql().contains("completion_phase"));
        }
    }

    @Test
    public void invalidAddPhaseDoesNotModifyServiceOrder() throws Exception {
        try {
            service.addVisa(request(81), "INVALID");
            fail("invalid phase on add");
        } catch (ServiceException expected) {
            assertTrue(expected.getMessage().contains("completionPhase"));
        }
        verify(orders, never()).updateCompletionPhase(anyInt(), anyString());
    }

    @Test
    public void portalStateLabelsMatchRequestedLogContent() {
        String[][] states = {
                {"01", "客户待提交资料"}, {"02", "客户已提交资料"}, {"02A", "升级案件MARA处理中"},
                {"02B", "已生成合同预览"}, {"02C", "客户签约退回"}, {"02D", "MARA签约审核驳回"},
                {"03", "已提交给MARA签约审核"}, {"03A", "合同审核通过待客户确认"}, {"04", "客户签约已确认"},
                {"05", "顾问已下服务订单"}, {"06", "准备申请材料中"}, {"06A", "客户退回申请材料"},
                {"06B", "申请材料待客户确认"}, {"06C", "客户已上传申请材料"}, {"07", "客户已确认申请材料"},
                {"07A", "申请材料MARA正在审核"}, {"07B", "申请材料MARA审核驳回"}, {"08", "申请材料MARA审核通过"},
                {"09", "文案已正式提交申请"}, {"010", "已通知客户补料"}, {"010A", "补料MARA正在审核"},
                {"010B", "补料MARA审核驳回"}, {"010C", "补料MARA审核通过"}, {"010D", "客户已上传补充材料"},
                {"010E", "客户退回补充材料"}, {"010F", "补充材料待客户确认"}, {"010G", "客户已确认补充材料"},
                {"011", "等待最终决定"}, {"012", "申请结果已通知客户"}, {"013", "案件已归档结案"}
        };
        for (String[] state : states)
            assertEquals(state[0], state[1], PortalFollowUpState.fromCode(state[0]).getLabel());
    }


    private VisaOfficialDTO request(int orderId) {
        VisaOfficialDTO dto = new VisaOfficialDTO();
        dto.setServiceOrderId(orderId);
        dto.setOfficialId(12);
        dto.setCode(UUID.randomUUID().toString());
        dto.setAmount(99999); // 请求金额不能覆盖数据库visa总收款。
        return dto;
    }

    private VisaDO receipt(int id, double amount, String currency, double rate) {
        VisaDO receipt = new VisaDO();
        receipt.setId(id);
        receipt.setAmount(amount);
        receipt.setPerAmount(amount);
        receipt.setCurrency(currency);
        receipt.setExchangeRate(rate);
        return receipt;
    }
}
