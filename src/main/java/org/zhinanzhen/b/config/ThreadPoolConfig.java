package org.zhinanzhen.b.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class ThreadPoolConfig {

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数，线程池创建时初始化的线程数
        executor.setCorePoolSize(20);
        // 最大线程数，线程池可容纳的最大线程数
        executor.setMaxPoolSize(40);
        // 队列容量，等待执行的任务队列的容量
        executor.setQueueCapacity(200);
        // 线程池维护线程所允许的空闲时间，单位为秒
        executor.setKeepAliveSeconds(60);
        // 线程池对拒绝任务的处理策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 线程名称前缀
        executor.setThreadNamePrefix("MyThreadPool-");
        // 初始化线程池
        executor.initialize();
        return executor;
    }
}