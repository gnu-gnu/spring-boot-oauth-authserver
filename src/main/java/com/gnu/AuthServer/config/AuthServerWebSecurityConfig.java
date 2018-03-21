package com.gnu.AuthServer.config;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationManager;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.gnu.AuthServer.AuthInnerFilter;

@Configuration
@EnableWebSecurity
public class AuthServerWebSecurityConfig extends WebSecurityConfigurerAdapter {
	Logger logger = LoggerFactory.getLogger(AuthServerWebSecurityConfig.class);
	final Marker REQUEST_MARKER = MarkerFactory.getMarker("HTTP_REQUEST");

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.formLogin().disable().httpBasic().and().authorizeRequests()
		.antMatchers("/oauth/authorize").authenticated()
		.antMatchers("/oauth/token").authenticated()
		.anyRequest().authenticated().and()
		.csrf().disable()
		.addFilterBefore(new AuthInnerFilter(), BasicAuthenticationFilter.class);
	}
	
	/**
	 * inMemory 기반의 기본 인증을 AuthenticationManager로 대체
	 */
	@Override
	@Bean(name="customAuthManager")
	public AuthenticationManager authenticationManagerBean() throws Exception {
		AuthenticationProvider provider = new DaoAuthenticationProvider(){
			@Override
			public Authentication authenticate(Authentication authentication) throws AuthenticationException {
					// TODO 현재는 입력한 아이디 기준으로 무조건 권한 발급, DB 기준으로 고치도록
					return new UsernamePasswordAuthenticationToken(authentication.getPrincipal(), authentication.getCredentials(), AuthorityUtils.createAuthorityList("read", "write"));
			}
			@Override
			public boolean supports(Class<?> authentication) {
				return true;
			}
		};
		List<AuthenticationProvider> providers = new ArrayList<AuthenticationProvider>();
		providers.add(provider);
		return new ProviderManager(providers, new OAuth2AuthenticationManager()); 
	}

}
