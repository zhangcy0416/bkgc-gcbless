package com.bkgc;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;


@SpringBootApplication
@MapperScan("com.bkgc.*.mapper")
@EnableScheduling
@EnableFeignClients
@EnableEurekaClient
@EnableDiscoveryClient
@EnableHystrix
public class BkgcBlessApplication {
    public static void main(String[] args) {
        SpringApplication.run(BkgcBlessApplication.class, args);
    }

	/*@Bean
	public FilterRegistrationBean accessTokenFilterRegistration() {
		FilterRegistrationBean registration = new FilterRegistrationBean(new AccessTokenFilter());
		registration.addUrlPatterns("*//*");
		registration.addInitParameter("noCheck",
				"/blessenvelope/getBlessEnvelope,/auth/login,/blessenvelope/getAdurlByMachineId,"
						+ "/views/,/css/,/js/,/favicon.ico,/images/,/fonts/,/auth/getImage,"
						+ "/auth/loginByWeiXin,/auth/registerWithPhone,/auth/checkSmsCodeGetUser,"
						+ "/mapp/mu,/auth/sendMessage,/blessenvelope/BlessReciverTop,/blessenvelope/getDefault20MachinesByCurrentLoaction,"
						+ "/auth/loginByCode,/apk/,/bless/,/message/,/account,/authMemCom,/wx,/envelope,/q/m,/index.html,index.html,/game,/getBlessAndAwad,/activity,/auth/provider/getUserInfo"
		);
		return registration;
	}


	@Bean
	public FilterRegistrationBean checkSignFilterRegistration() {
		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(new CheckSignFilter());
		registration.addUrlPatterns("*//*");
		registration.addInitParameter("noCheck",
				"/envelope/getBlessEnvelope,/auth/login,/blessenvelope/getAdurlByMachineId,"
						+ "/views/,/css/,/js/,/favicon.ico,/images/,/fonts/,/auth/getImage,/envelope,"
						+ "/auth/loginByWeiXin,/auth/registerWithPhone,/auth/checkSmsCodeGetUser,"
						+ "/mapp/mu,/auth/sendMessage,/blessenvelope/BlessReciverTop,/blessenvelope/getDefault20MachinesByCurrentLoaction,"
						+ "/auth/loginByCode,/apk/,/bless/,/message/,/account,/authMemCom,/auth/getUserInfo,/auth/batchTransfer,/wx,/q/m,/index.html,index.html,/game,/getBlessAndAwad,/activity,/auth/provider/getUserInfo"
		);
		registration.setOrder(2);
		return registration;
	}*/



   /* @Bean
    public ServletListenerRegistrationBean<ServletContextListener> servletListenerRegistrationBean() {
        ServletListenerRegistrationBean<ServletContextListener> servletListenerRegistrationBean = new ServletListenerRegistrationBean<ServletContextListener>();
        servletListenerRegistrationBean.setListener(new LoadClientConfig());
        return servletListenerRegistrationBean;
    }*/


	@Bean
	@LoadBalanced
	RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
