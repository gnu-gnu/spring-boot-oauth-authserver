package com.gnu.AuthServer.config;

import java.util.Arrays;
import java.util.Collection;import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.DefaultSecurityContextAccessor;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.SecurityContextAccessor;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenStore;

import com.gnu.AuthServer.AuthInnerFilter;
import com.gnu.AuthServer.utils.GrantTypes;

import ch.qos.logback.core.net.SyslogOutputStream;

@Configuration
@EnableAuthorizationServer // OAuthServer는 AuthorizationServer (권한 발급) 및 ResourceServer(보호된 자원이 위치하는 서버)가 있음
public class AuthServerConfig extends AuthorizationServerConfigurerAdapter { 
	Logger logger = LoggerFactory.getLogger(AuthServerConfig.class);
	final Marker REQUEST_MARKER = MarkerFactory.getMarker("HTTP_REQUEST");
	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	TokenStore tokenStore;
	
	public ClientDetailsService ClientDetailsService() {
			return new ClientDetailsService() {
				@Override
				public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
					System.out.println("clientId check...");
					Set<String> set = new HashSet<>();
					set.add("http://localhost:7077/resources/open/callback");
					BaseClientDetails details = new BaseClientDetails();
					details.setClientId("lookin");
					details.setAuthorizedGrantTypes(Arrays.asList(GrantTypes.AUTHORIZATION_CODE));
					details.setRegisteredRedirectUri(set);
					details.setScope(Arrays.asList("check"));
					return details;
				}
			};
		};
	

	/**
	 * endpoint에 대한 설정을 담당하는 메소드
	 * 기본 endpoint
	 * 1) ~~/authorize -> request token을 받는다. 나중에 access token 발급에 쓰일 수 있다. 이 단계에서는 httpBasic의 인증에 설정 해 놓은 id, pw를 basic auth로 사용한다
	 * 2) ~~/token_access -> protected resources에 엑세스하기 위한 access token을 발급한다. 이 단계에서는 client id, secret을 basic auth에 사용한다 (secret 생략 가능)
	 */
	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		endpoints.tokenStore(tokenStore); // tokenStore 설정, 현재 프로젝트에서는 redis가 tokenStore bean으로 설정되어 있음
		endpoints.allowedTokenEndpointRequestMethods(HttpMethod.POST, HttpMethod.OPTIONS);
		endpoints.authenticationManager(authenticationManager);
		
	}
	/**
	 * 보안에 관련된 설정
	 * 권한, 접근제어등은 여기서 설정한다.
	 * 
	 * 보안이 요구되는 endpoints (기본은 denyAll() 이므로 적절히 고쳐서 사용한다)
	 * 1) ~~/check_token (resource server가 rest로 token의 검증을 요청할 때 사용하는 endpoint, checkTokenAcess 로 조절)
	 * 2) ~~/token_key (JWT 사용시, 토큰 검증을 위한 공개키를 노출하는 endpoint, tokenKeyAccess로 조절)
	 */
	@Override
	public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
		security.addTokenEndpointAuthenticationFilter(new AuthInnerFilter()); // ~~/authorize 에 대한 필터
		security.checkTokenAccess("hasAuthority('RESOURCE')"); // ~~/check_token으로 remoteTokenService가 토큰의 해석을 의뢰할 경우, 해당 endpoint의 권한 설정(기본 denyAll())
		security.accessDeniedHandler((request, response, exception) -> exception.printStackTrace());
	}
	/**
	 * OAuth서버에 접근을 요청하는 Client에 관한 설정
	 */
	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients.withClientDetails(ClientDetailsService());
	}
}
