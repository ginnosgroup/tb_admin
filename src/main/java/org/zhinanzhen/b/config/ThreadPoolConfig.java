package org.zhinanzhen.b.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;  
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;  
  
import java.util.concurrent.Executor;  
  
@Configuration  
public class ThreadPoolConfig {  
  
    @Bean(name = "taskExecutor")  
    public Executor taskExecutor() {  
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();  
        executor.setCorePoolSize(10); // 核心线程数
        executor.setMaxPoolSize(20); // 最大线程数
        executor.setQueueCapacity(25); // 队列容量  
        executor.setThreadNamePrefix("GlobalThreadPool-"); // 线程名前缀  
        executor.initialize();  
        return executor;  
    }  
}