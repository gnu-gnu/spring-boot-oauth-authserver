package com.gnu.AuthServer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

import com.gnu.AuthServer.method.AuthServerMethodSecurityExpressionHandler;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled=true)
public class AuthServerMethodSecurityConfig extends GlobalMethodSecurityConfiguration {

	@Override
	protected MethodSecurityExpressionHandler createExpressionHandler() {
		return new AuthServerMethodSecurityExpressionHandler();
	}
	
}
