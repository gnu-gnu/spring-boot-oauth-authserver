package com.gnu.AuthServer.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.DefaultSecurityContextAccessor;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.SecurityContextAccessor;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.TokenStore;

import com.gnu.AuthServer.AuthInnerFilter;
import com.gnu.AuthServer.utils.GrantTypes;

@Configuration
@EnableAuthorizationServer // OAuthServer는 AuthorizationServer (권한 발급) 및 ResourceServer(보호된 자원이 위치하는 서버)가 있음
public class AuthServerConfig extends AuthorizationServerConfigurerAdapter { 
	Logger logger = LoggerFactory.getLogger(AuthServerConfig.class);
	final Marker REQUEST_MARKER = MarkerFactory.getMarker("HTTP_REQUEST");
	@Autowired
	@Qualifier("authenticationManagerBean")
	private AuthenticationManager authenticationManager;
	
	@Autowired
	ClientDetailsService clientDetailsService;
	
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
		endpoints.exceptionTranslator(new WebResponseExceptionTranslator() {
			
			@Override
			public ResponseEntity<OAuth2Exception> translate(Exception e) throws Exception {
				e.printStackTrace();
				return null; 
			}
		});
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
		SecurityContextAccessor securityContextAccessor = new DefaultSecurityContextAccessor();
		/*endpoints.userDetailsService(new UserDetailsService() {
			
			@Override
			public UserDetails loadUserByUsername(String arg0) throws UsernameNotFoundException {
				SecureRandom sRandom = new SecureRandom();
				return new User(arg0, String.valueOf(sRandom.ints(Integer.MIN_VALUE, Integer.MAX_VALUE)), securityContextAccessor.getAuthorities());
			}
		});*/
		endpoints.requestFactory(new DefaultOAuth2RequestFactory(clientDetailsService){	
			/**
			 * 
			 * ~~/oauth/token 에서 호출하는 메소드
			 * @param requestParameters 클라이언트가 보낸 요청
			 * @param authenticatedClient request_token에 부합하는 client
			 * @return
			 * @see org.springframework.security.oauth2.provider.endpoint.TokenEndpoint#postAccessToken
			 * 
			 */
			@Override
			public TokenRequest createTokenRequest(Map<String, String> requestParameters, ClientDetails authenticatedClient) {
				return super.createTokenRequest(requestParameters, authenticatedClient);
			}
			
			@Override
			public OAuth2Request createOAuth2Request(AuthorizationRequest request) {
				// request token 발급시 사용했던 basic auth에 담긴 securityContext를 accessor로 사용한다
				Collection<String> scopes = new HashSet<String>();
				// request token을 요청한 user의 Authority 기준으로 자동으로 scope를 설정한다
				securityContextAccessor.getAuthorities().forEach(value->{
					scopes.add(value.getAuthority());
				});
				request.setScope(scopes);
				return super.createOAuth2Request(request);
			}			

			/**
			 * 
			 * scope가 user의 role과 일치하는지 확인한다 (가장 마지막 단계)
			 * 
			 * @param checkUserScopes
			 */
			@Override
			public void setCheckUserScopes(boolean checkUserScopes) {
				super.setCheckUserScopes(false);
			}

			/**
			 * ~~/oauth/authorize에서 호출하는 메소드
			 * request_token을 발급 받은 후 access_token을 발급하기 위한 request를 만든다
			 * 
			 * @param authorizationParameters
			 * @return
			 * @see org.springframework.security.oauth2.provider.endpoint.AuthorizationEndpoint#authorize
			 */
			@Override
			public AuthorizationRequest createAuthorizationRequest(Map<String, String> authorizationParameters) {
				// 2
				AuthorizationRequest request = super.createAuthorizationRequest(authorizationParameters);
				 if (securityContextAccessor.isUser()) {
					 	 // 여기서 먼저 scope 설정하려 하면 에러남. access_token 발급시에 바꿔 끼우도록
						 request.setAuthorities(securityContextAccessor.getAuthorities());
			    }
				return request;
			}

			@Override
			public void setSecurityContextAccessor(SecurityContextAccessor securityContextAccessor) {
				super.setSecurityContextAccessor(securityContextAccessor);
			}
			
			
			
			
			
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
		security.addTokenEndpointAuthenticationFilter(new AuthInnerFilter()); // ~~/authorize 에 대한 필터
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
		.scopes("dummy") // 스코프가 필수값이라 넣긴하는데 OAuth2RequestFactory 에서 UserRole로 세팅하도록 설정할 것임
		/* If the client was issued a client secret, then the server must authenticate the client. One way to authenticate the client is to accept another parameter in this request, client_secret. Alternately the authorization server can use HTTP Basic Auth.
		 * secret을 발급하여 token을 발급하면 refresh 할 때도 secret을 입력해야 하는 문제가 생김. 그러므로 접근 제어는 HTTP Basic Auth에 맡기고 token 발급시에는 client_secret을 배제
		 */
		// .secret("secret")  
		.authorizedGrantTypes(GrantTypes.AUTHORIZATION_CODE, GrantTypes.REFRESH_TOKEN)
		.accessTokenValiditySeconds(600)
		.refreshTokenValiditySeconds(1800)
		.redirectUris("http://localhost:7077/resources/open/callback", "https://www.getpostman.com/oauth2/callback")
		.autoApprove("true") // 권한의 허용 여부에 대한 확인(/confirm_access)을 할지 여부
		.and()
		.withClient("resourceServer")
		.secret("resourceSecret")
		.authorities("RESOURCE"); // 해당 client에 대해 Authorities 부여, 이를 바탕으로 checkTokenAccess의 접근제어를 통과한다.
	}
}
