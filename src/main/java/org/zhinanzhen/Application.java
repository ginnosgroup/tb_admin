package org.zhinanzhen;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.social.config.annotation.EnableSocial;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.mem.InMemoryUsersConnectionRepository;
import org.springframework.social.connect.web.ProviderSignInController;
import org.springframework.social.wechat.autoconfigure.WecomAutoConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.ikasoa.core.utils.StringUtil;

@SpringBootApplication
@EnableAutoConfiguration
@EnableSocial
@MapperScan({ "org.zhinanzhen.tb.dao", "org.zhinanzhen.b.dao" })
@Import(WecomAutoConfiguration.class)
@EnableAsync
public class Application extends WebMvcConfigurerAdapter {

	public final static String DOMAIN = "https://yongjinbiao.zhinanzhen.org";
	
	@Value("${server.context-path}")
	private String contextPath;

	public static void main(String[] args) throws Exception {
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**").allowedOrigins("*").allowCredentials(true)
				.allowedMethods("GET", "POST", "PUT", "DELETE");
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/webroot/**").addResourceLocations("classpath:/webroot/");
	}

	@Bean
	public ProviderSignInController providerSignInController(ConnectionFactoryLocator connectionFactoryLocator,
			UsersConnectionRepository usersConnectionRepository, ScanSignInAdapter scanSignInAdapter) {
		((InMemoryUsersConnectionRepository) usersConnectionRepository)
				.setConnectionSignUp((Connection<?> connection) -> connection.getKey().getProviderUserId());
		ProviderSignInController psc = new ProviderSignInController(connectionFactoryLocator, usersConnectionRepository,
				scanSignInAdapter);
		psc.setApplicationUrl(StringUtil.merge(DOMAIN, contextPath));
		return psc;
	}

}
