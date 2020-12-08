package org.zhinanzhen;

import java.io.IOException;

import javax.sql.DataSource;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ActivitiConfig {

	private final DataSource dataSource;

	private final PlatformTransactionManager platformTransactionManager;

	@Autowired
	public ActivitiConfig(DataSource dataSource, PlatformTransactionManager platformTransactionManager) {
		this.dataSource = dataSource;
		this.platformTransactionManager = platformTransactionManager;
	}

	/*
	 * 自动扫描process包下的bpmn(流程定义文件)的设置，这样就可以省去了部署
	 */
	@Bean
	public SpringProcessEngineConfiguration springProcessEngineConfiguration() {
		SpringProcessEngineConfiguration spec = new SpringProcessEngineConfiguration();
		spec.setDataSource(dataSource);
		spec.setTransactionManager(platformTransactionManager);
		spec.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
		Resource[] resources = null;
		// 启动自动部署流程
		try {
			// 注意：下面那个路径是你resource文件夹下存放流程图的路径
			resources = new PathMatchingResourcePatternResolver().getResources("classpath*:processes/*.bpmn");
		} catch (IOException e) {
			e.printStackTrace();
		}
		spec.setDeploymentResources(resources);
		return spec;
	}

	@Bean
	public ProcessEngineFactoryBean processEngine() {
		ProcessEngineFactoryBean processEngineFactoryBean = new ProcessEngineFactoryBean();
		processEngineFactoryBean.setProcessEngineConfiguration(springProcessEngineConfiguration());
		return processEngineFactoryBean;
	}

	@Bean
	public RepositoryService repositoryService() throws Exception {
		return processEngine().getObject().getRepositoryService();
	}

	@Bean
	public RuntimeService runtimeService() throws Exception {
		return processEngine().getObject().getRuntimeService();
	}

	@Bean
	public TaskService taskService() throws Exception {
		return processEngine().getObject().getTaskService();
	}

	@Bean
	public HistoryService historyService() throws Exception {
		return processEngine().getObject().getHistoryService();
	}

}