package org.zhinanzhen.b.controller;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.Configuration;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.zhinanzhen.b.dao.pojo.PortalAttachmentDO;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PortalAttachmentAuditFieldsTest {

    @Test
    public void attachmentInsertAndSelectContainClientInfo() throws Exception {
        Configuration configuration = new Configuration();
        String resource = "sqlmap/BPortalAttachmentDAO.xml";
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(input);
            new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments()).parse();
        }

        String namespace = "org.zhinanzhen.b.dao.PortalAttachmentDAO.";
        PortalAttachmentDO attachment = new PortalAttachmentDO();
        attachment.setIp("203.0.113.8");
        attachment.setUserAgent("test-agent");
        BoundSql insert = configuration.getMappedStatement(namespace + "addPortalAttachment")
                .getBoundSql(attachment);
        assertTrue(insert.getSql().contains("ip"));
        assertTrue(insert.getSql().contains("user_agent"));
        assertTrue(insert.getParameterMappings().stream().anyMatch(p -> "ip".equals(p.getProperty())));
        assertTrue(insert.getParameterMappings().stream().anyMatch(p -> "userAgent".equals(p.getProperty())));

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("id", 1);
        BoundSql select = configuration.getMappedStatement(namespace + "getPortalAttachmentById")
                .getBoundSql(parameters);
        assertTrue(select.getSql().contains("ip"));
        assertTrue(select.getSql().contains("user_agent AS userAgent"));
    }

    @Test
    public void clientIpUsesSameForwardedAddressRuleAsPortalLog() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.8, 10.0.0.1");
        String ip = ReflectionTestUtils.invokeMethod(new PortalController(), "getClientIp", request);
        assertEquals("203.0.113.8", ip);
    }
}
