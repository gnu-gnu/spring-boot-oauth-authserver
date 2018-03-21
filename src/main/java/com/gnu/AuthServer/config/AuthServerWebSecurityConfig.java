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
	/** AuthenticationManagerBean을 직접 삽입
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.inMemoryAuthentication()
		.withUser("username").password("p").authorities("read", "write").and() // role과 authority의 차이는 앞에 prefix (ROLE_) 가 자동으로 붙냐 안 붙냐 차이 정도
		.withUser("read").password("p").authorities("read");
	}
	*/

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.formLogin().disable().httpBasic().and().authorizeRequests()
		.antMatchers("/oauth/authorize").authenticated()
		.antMatchers("/oauth/token").authenticated()
		.anyRequest().authenticated().and()
		.csrf().disable()
		.addFilterBefore(new AuthInnerFilter(), BasicAuthenticationFilter.class);
	}
	@Override
	@Bean
	public AuthenticationManager authenticationManagerBean() throws Exception {
		AuthenticationProvider provider = new DaoAuthenticationProvider(){

			@Override
			public Authentication authenticate(Authentication authentication) throws AuthenticationException {
					return new UsernamePasswordAuthenticationToken(authentication.getPrincipal(), authentication.getCredentials(), AuthorityUtils.createAuthorityList("read", "write"));
			}

			@Override
			public boolean supports(Class<?> authentication) {
				// TODO Auto-generated method stub
				return true;
			}
			
		};
		List<AuthenticationProvider> providers = new ArrayList<AuthenticationProvider>();
		providers.add(provider);
		return new ProviderManager(providers, new OAuth2AuthenticationManager()); 
	}

}
