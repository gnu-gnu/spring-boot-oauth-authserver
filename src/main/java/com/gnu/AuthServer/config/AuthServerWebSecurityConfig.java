package com.gnu.AuthServer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class AuthServerWebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		// TODO Auto-generated method stub
		auth.inMemoryAuthentication().withUser("u").password("p").authorities("UserGrant");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.formLogin().disable();
		http.httpBasic();
		http.authorizeRequests().antMatchers("/oauth/authorize").authenticated().antMatchers("/oauth/token").access("hasAuthority('GET_AUTH_CODE')").and().authorizeRequests().anyRequest().authenticated();
		http.exceptionHandling().accessDeniedHandler((arg0, arg1, arg2) -> {
			System.out.println(arg0.getRequestURL().toString());
			arg2.printStackTrace();	
		});
		http.csrf().disable();
	}

	@Override
	@Bean
	public AuthenticationManager authenticationManagerBean() throws Exception {
		// TODO Auto-generated method stub
		return super.authenticationManagerBean();
	}

}
