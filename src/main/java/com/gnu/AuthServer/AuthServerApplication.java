package com.gnu.AuthServer;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@SpringBootApplication
public class AuthServerApplication extends WebMvcConfigurerAdapter{
	Logger logger = LoggerFactory.getLogger(AuthServerApplication.class);
	final Marker REQUEST_MARKER = MarkerFactory.getMarker("HTTP_REQUEST");

	public static void main(String[] args) {
		SpringApplication.run(AuthServerApplication.class, args);
	}
	@Bean
	public TokenStore tokenStore(RedisConnectionFactory factory){
		/**
		 * redis를 token 저장소로 사용하기 위해 설정
		 * 이 부분을 제거하면 inMemory 저장소를 사용함
		 */
		return new RedisTokenStore(factory);
	}
	
	// @Bean
	public FilterRegistrationBean allFilter(){
		FilterRegistrationBean bean = new FilterRegistrationBean();
		bean.setOrder(0);
		bean.addUrlPatterns("/**");
		bean.setFilter(new Filter() {
			
			@Override
			public void init(FilterConfig arg0) throws ServletException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2)
					throws IOException, ServletException {
				HttpServletRequest req = (HttpServletRequest)arg0;
				logger.info("--------- userprincipal : {}", req.getUserPrincipal());
				logger.info("--------- call for {}", req.getRequestURI());
				Enumeration<String> names = req.getHeaderNames();
				logger.info("----- headers");
				String key = "";
				while(names.hasMoreElements()){
					key = names.nextElement();
					logger.info(REQUEST_MARKER, "{} : {}", key, req.getHeader(key));
				}
				String paramKey = "";
				logger.info("----- params");
				Enumeration<String> params = req.getParameterNames();
				while(params.hasMoreElements()){
					paramKey = params.nextElement();
					logger.info(REQUEST_MARKER, "{} : {}", paramKey, req.getParameter(paramKey));
				}
				arg2.doFilter(arg0, arg1);
			}
			
			@Override
			public void destroy() {
				// TODO Auto-generated method stub
				
			}
		});
		return bean;

	}
	
	
}
