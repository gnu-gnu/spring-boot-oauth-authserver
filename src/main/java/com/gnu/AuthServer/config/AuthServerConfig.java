package com.gnu.AuthServer.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;

import com.gnu.AuthServer.utils.GrantTypes;

@Configuration
@EnableAuthorizationServer // OAuthServer는 AuthorizationServer (권한 발급) 및 ResourceServer(보호된 자원이 위치하는 서버)가 있음
public class AuthServerConfig extends AuthorizationServerConfigurerAdapter { 
	
	@Autowired
	@Qualifier("authenticationManagerBean")
	private AuthenticationManager authenticationManager;
	
	@Autowired
	TokenStore tokenStore;
	
	/**
	 * endpoint에 대한 설정을 담당하는 메소드
	 * 기본 endpoint
	 * 1) ~~/authorize -> request token을 받는다. 나중에 access token 발급에 쓰일 수 있다. 
	 * 2) ~~/token_access -> protected resources에 엑세스하기 위한 access token을 발급한다.
	 */
	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		endpoints.tokenStore(tokenStore); // tokenStore 설정, 현재 프로젝트에서는 redis가 tokenStore bean으로 설정되어 있음
		endpoints.allowedTokenEndpointRequestMethods(HttpMethod.POST, HttpMethod.OPTIONS);
		endpoints.authenticationManager(authenticationManager);
		endpoints.tokenEnhancer((token, authentication) -> {
			/*
			 * 발급되는 토큰에 부가정보를 담아 리턴함
			 */
			;
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("hello", "world");
			((DefaultOAuth2AccessToken)token).setAdditionalInformation(map);
			return token;
		});
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
		security.checkTokenAccess("hasAuthority('RESOURCE')"); // ~~/check_token으로 remoteTokenService가 토큰의 해석을 의뢰할 경우, 해당 endpoint의 권한 설정(기본 denyAll())
		security.accessDeniedHandler((request, response, exception) -> exception.printStackTrace());
	}
	/**
	 * OAuth서버에 접근을 요청하는 Client에 관한 설정
	 */
	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients.inMemory()
		/**
		 * HTTP Basic Auth를 통해 grant_type을 client_credentials로 접근한 아래의 client에 대해 read, write 권한을 180초간 허용하는 토큰을 발급함
		 */
		.withClient("client")
		.secret("secret")
		.authorizedGrantTypes(GrantTypes.CLIENT_CREDENTIALS)
		.scopes("read")
		.accessTokenValiditySeconds(180)
		.and()
		.withClient("code")
		.secret("secret")
		.authorities("GET_AUTH_CODE")
		.authorizedGrantTypes(GrantTypes.AUTHORIZATION_CODE, GrantTypes.REFRESH_TOKEN)
		.accessTokenValiditySeconds(180)
		.refreshTokenValiditySeconds(360)
		.scopes("read","write")
		.autoApprove("true") // 권한의 허용 여부에 대한 확인(/confirm_access)을 할지 여부
		.and()
		.withClient("resourceServer")
		.secret("resourceSecret")
		.authorities("RESOURCE"); // 해당 client에 대해 Authorities 부여, 이를 바탕으로 checkTokenAccess의 접근제어를 통과한다.
	}
}
