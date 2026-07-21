package org.zhinanzhen.tb.utils;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Logs slow MyBatis statements without printing parameters or credentials.
 */
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {
                MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class
        })
})
public class SlowSqlInterceptor implements Interceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlowSqlInterceptor.class);

    private long thresholdMillis = 1000L;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return invocation.proceed();
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            if (elapsed >= thresholdMillis) {
                Object[] args = invocation.getArgs();
                String statementId = args.length > 0 && args[0] instanceof MappedStatement
                        ? ((MappedStatement) args[0]).getId()
                        : "unknown";
                LOGGER.warn("Slow SQL detected: statement={}, elapsedMs={}", statementId, elapsed);
            }
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        String configured = properties.getProperty("thresholdMillis");
        if (configured != null && !configured.trim().isEmpty()) {
            thresholdMillis = Long.parseLong(configured.trim());
        }
    }

    public void setThresholdMillis(long thresholdMillis) {
        this.thresholdMillis = thresholdMillis;
    }
}
