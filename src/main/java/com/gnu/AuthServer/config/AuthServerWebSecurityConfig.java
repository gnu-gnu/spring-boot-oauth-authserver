package com.gnu.AuthServer.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationManager;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.gnu.AuthServer.AuthInnerFilter;
import com.gnu.AuthServer.repository.UserRepository;
import com.gnu.AuthServer.security.AuthUserDetails;
import com.gnu.AuthServer.service.AuthUserDetailsService;

@Configuration
@EnableWebSecurity
public class AuthServerWebSecurityConfig extends WebSecurityConfigurerAdapter {
	Logger logger = LoggerFactory.getLogger(AuthServerWebSecurityConfig.class);
	final Marker REQUEST_MARKER = MarkerFactory.getMarker("HTTP_REQUEST");
	@Autowired
	UserRepository userRepository;
	@Autowired
	AuthUserDetailsService userDetailsService;
	
	BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
	
	/**
	 * PASSWORD GRANT는 authenticationManager를 통해서 이루어짐
	 * 
	 */
	@Override
	@Bean
	protected AuthenticationManager authenticationManager() throws Exception {

		AuthenticationProvider provider = new DaoAuthenticationProvider() {
			@Override
			public Authentication authenticate(Authentication authentication) throws AuthenticationException {
				logger.info(authentication.getName() + "... auth start");
				String username = authentication.getName();
				CharSequence password = (CharSequence) authentication.getCredentials();

				AuthUserDetails user = (AuthUserDetails) userDetailsService.loadUserByUsername(username);
				Set<GrantedAuthority> autho = (Set<GrantedAuthority>) user.getAuthorities();
				autho.add(new SimpleGrantedAuthority("ACTUATOR"));
				if (!encoder.matches(password, user.getPassword())) {
					logger.info(authentication.getName() + "... bad credential");
					throw new BadCredentialsException("bad credential");
				} else {
					return new UsernamePasswordAuthenticationToken(user.getUsername(), password.toString(),autho);
				}
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

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests().antMatchers("/h2/**").permitAll().anyRequest().authenticated().and().formLogin().and()
				.csrf().disable().addFilterBefore(new AuthInnerFilter(), BasicAuthenticationFilter.class);
		http.headers().frameOptions().disable();
	}

}
