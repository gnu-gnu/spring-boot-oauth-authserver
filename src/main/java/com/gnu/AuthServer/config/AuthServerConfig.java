package com.gnu.AuthServer.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.approval.TokenApprovalStore;
import org.springframework.security.oauth2.provider.token.TokenStore;

import com.gnu.AuthServer.AuthInnerFilter;
import com.gnu.AuthServer.service.AuthClientDetailsService;
import com.gnu.AuthServer.service.AuthTokenService;

@Configuration
@EnableAuthorizationServer // OAuthServer는 AuthorizationServer (권한 발급) 및 ResourceServer(보호된 자원이 위치하는 서버)가 있음
public class AuthServerConfig extends AuthorizationServerConfigurerAdapter { 
	Logger logger = LoggerFactory.getLogger(AuthServerConfig.class);
	final Marker REQUEST_MARKER = MarkerFactory.getMarker("HTTP_REQUEST");
	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	AuthClientDetailsService clientDetailsService;
	@Autowired
	AuthTokenService tokenService;
	@Autowired
	TokenStore tokenStore;
	

	/**
	 * endpoint에 대한 설정을 담당하는 메소드
	 * 기본 endpoint
	 * 1) ~~/authorize -> request token을 받는다. 나중에 access token 발급에 쓰일 수 있다. 이 단계에서는 httpBasic의 인증에 설정 해 놓은 id, pw를 basic auth로 사용한다
	 * 2) ~~/token_access -> protected resources에 엑세스하기 위한 access token을 발급한다. 이 단계에서는 client id, secret을 basic auth에 사용한다 (secret 생략 가능)
	 */
	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		endpoints.allowedTokenEndpointRequestMethods(HttpMethod.POST, HttpMethod.OPTIONS);
		endpoints.authenticationManager(authenticationManager);
		endpoints.tokenServices(tokenService);
		TokenApprovalStore approvalStore = new TokenApprovalStore();
		approvalStore.setTokenStore(tokenStore);
		endpoints.approvalStore(approvalStore);
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
		security.allowFormAuthenticationForClients(); // 기본적으로 HTTP HEADER AUTH가 적용되므로, FORM 전송으로 AUTH하기 위해서 적용
		security.checkTokenAccess("hasAuthority('RESOURCE')"); // ~~/check_token으로 remoteTokenService가 토큰의 해석을 의뢰할 경우, 해당 endpoint의 권한 설정(기본 denyAll())
		security.accessDeniedHandler((request, response, exception) -> logger.error(exception.getMessage()));
	}
	/**
	 * OAuth서버에 접근을 요청하는 Client에 관한 설정
	 */
	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients.withClientDetails(clientDetailsService);
	}
}
