package com.gnu.AuthServer.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.gnu.AuthServer.service.AuthUserDetails;
import com.gnu.AuthServer.service.AuthUserDetailsService;
/**
 * 
 * 기본적인 Web Security를 설정하는 Bean
 * 웹 프로젝트의 Spring security 설정과 크게 다르지 않다.
 * Password Grant 기반의 인증을 수행할 경우 ClientDetailsService를 거치지 않고 이 서비스의 AuthenticationManager만으로 인증 과정이 수행되고 Token이 발급된다
 * 
 * @author gnu-gnu(geunwoo.j.shim@gmail.com)
 *
 */
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
	
	@Override
	@Bean
	protected AuthenticationManager authenticationManager() throws Exception {

		AuthenticationProvider provider = new DaoAuthenticationProvider() {
			@Override
			public Authentication authenticate(Authentication authentication) throws AuthenticationException {
				logger.info(authentication.getName() + "... auth start");
				String username = authentication.getName();
				CharSequence password = (CharSequence) authentication.getCredentials();
				Map<String, Object> details = new HashMap<>();
				AuthUserDetails user = (AuthUserDetails) userDetailsService.loadUserByUsername(username);
				@SuppressWarnings("unchecked")
				Set<GrantedAuthority> autho = (Set<GrantedAuthority>) user.getAuthorities();
				autho.add(new SimpleGrantedAuthority("ACTUATOR"));
				if (!encoder.matches(password, user.getPassword())) {
					logger.info(authentication.getName() + "... bad credential");
					throw new BadCredentialsException("bad credential");
				} else {
					UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user.getUsername(), password.toString(),autho);
					details.put("uname", user.getEntity().getUsername());
					details.put("uid", user.getEntity().getId());
					details.put("locale", user.getEntity().getLocale());
					token.setDetails(details);
					return token;
				}
			}

			@Override
			public boolean supports(Class<?> authentication) {
				return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
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
		http.headers().frameOptions().disable(); // UI redressing attack을 방지하기 위해 X-Frame-Options를 검증하는 부분, 편의상 disable
	}

}
