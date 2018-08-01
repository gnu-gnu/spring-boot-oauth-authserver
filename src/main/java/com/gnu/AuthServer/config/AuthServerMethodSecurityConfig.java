package com.gnu.AuthServer.config;

import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

import com.gnu.AuthServer.method.AuthServerMethodSecurityExpressionHandler;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled=true,mode=AdviceMode.PROXY,proxyTargetClass=false)
public class AuthServerMethodSecurityConfig extends GlobalMethodSecurityConfiguration {
	/**
	 * 아래 부분을 주석처리하면 기본 핸들러를 사용하고, Bean 클래스의 메소드 표현식을 사용할 수 있다 
	 */
	@Override
	protected MethodSecurityExpressionHandler createExpressionHandler() {
		return new AuthServerMethodSecurityExpressionHandler();
	}
	
}
