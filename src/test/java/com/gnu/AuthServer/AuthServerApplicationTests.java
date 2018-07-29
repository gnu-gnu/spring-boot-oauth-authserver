package com.gnu.AuthServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.gnu.AuthServer.dto.AccessToken;
import com.gnu.AuthServer.utils.GrantTypes;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT, classes=AuthServerApplication.class)
public class AuthServerApplicationTests {
	private static final Logger LOG = LoggerFactory.getLogger(AuthServerApplicationTests.class);
	private static final String CLIENT_SECRET = "gnu-secret";
	private static final String CLIENT_ID = "gnu-gnu";
	private static final String CALLBACK_URL = "http://localhost:7077/resources/open/callback";
	private static final String REQUEST_TOKEN_ENDPOINT = "/oauth/authorize";
	private static final String ACCESS_TOKEN_ENDPOINT = "/oauth/token";
	private static final String LOGIN_ENDPOINT = "/login";


	@Autowired
	private WebApplicationContext ctx;
	
	@Autowired
	AuthenticationManager authenticationManager;
	@Autowired
	FilterChainProxy filterChainProxy;
	
	private MockMvc mvc;
	private String username;
	private String password;
	private Authentication auth;
	
	@Before
	public void init() throws Exception {
		LOG.info("----- unit test initialize");
		username = "gnu";
		password = "pass";
		this.mvc = MockMvcBuilders.webAppContextSetup(ctx).addFilters(filterChainProxy).build();
		auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
	}
	
	@Test
	public void testGetToken() throws Exception {
		testFormLogin();
		String requestToken = getRequestTokenForAuthorizationCode();
		assertThat(requestToken).isNotEmpty();
		AccessToken accessToken = getAccessTokenForAuthorizationCode(requestToken);
		assertThat(accessToken).isNotNull();
		String refreshToken = accessToken.getRefreshToken();
		assertThat(getAccessTokenByRefreshToken(refreshToken)).isNotNull();
		assertThat(getAccessTokenForPassword()).isNotNull();
	}
	/**
	 * Form 기반 로그인이 성공하는지 테스트
	 * 
	 * @throws Exception
	 */
	private void testFormLogin() throws Exception {
		LOG.info("----- form login test");
		ResultActions result = mvc.perform(formLogin(LOGIN_ENDPOINT).user(username).password(password));
		assertThat(result.andReturn().getResponse().getStatus()).isNotSameAs(500);
	}
	/**
	 * 
	 * Access 토큰 교환을 위해 사용하는 공통 메소드
	 * 
	 * @return Access_token
	 * @throws Exception
	 */
	private AccessToken getAccessToken(MultiValueMap<String, String> requestParam) throws Exception {
		/* 
		 * /oauth/token endpoint의 경우 ClientCredentialsTokenEndpointFilter가 등록되어 있음.
		 * client_id, client_secret을 이용하여 Authentication을 수행하므로 username, password 기반 인증을 엔드포인트 인증을 위해 끼워넣을 경우 오히려 에러가 발생함
		 * eg) request token 요청시에는 아래와 같이 .with(authentication(auth))를 삽입하여 Endpoint 인증에 Spring security의 AuthenticationManager를 사용하였으나
		 * post(REQUEST_TOKEN_ENDPOINT).with(authentication(auth)).params(requestParam).contentType(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.APPLICATION_JSON_UTF8).characterEncoding("UTF-8"); 
		 * access token 교환시 해당 인증을 삽입하면 client_id, secret 기반의 ClientCredentialsTokenEndpointFilter와 충돌을 일으켜 client_id를 AuthenticationManger에 등록된 id,pw로 인식하게 됨
		 * 그러므로 Access token 교환을 위한 인증시에는 client_id, secret 기반의 인증을 사용하도록 해당 부분을 제거
	     */ 
		MockHttpServletRequestBuilder reqBuilder = post(ACCESS_TOKEN_ENDPOINT).params(requestParam).contentType(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.APPLICATION_JSON_UTF8).characterEncoding("UTF-8");
		ResultActions result = mvc.perform(reqBuilder).andExpect(status().isOk());
		String responseBody = result.andReturn().getResponse().getContentAsString();
		ObjectMapper mapper = new ObjectMapper();
		mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
		AccessToken accessToken = mapper.readValue(responseBody, AccessToken.class);
		LOG.info("raw response : {}", responseBody);
		LOG.info("Access token dto : {}", accessToken.toString());
		return accessToken;
	}
	/**
	 * authorization_code grant 에서 requestToken을 access token으로 교환하기 위한 메소드
	 * 
	 * @param requestToken 
	 * @return
	 * @throws Exception
	 */
	private AccessToken getAccessTokenForAuthorizationCode(String requestToken) throws Exception{
		LOG.info("----- authorization_code access token test");
		MultiValueMap<String, String> requestParam = new LinkedMultiValueMap<>();
		requestParam.add("grant_type", GrantTypes.AUTHORIZATION_CODE);
		requestParam.add("code", requestToken);
		requestParam.add("redirect_uri", CALLBACK_URL);
		requestParam.add("client_id", CLIENT_ID);
		requestParam.add("client_secret", CLIENT_SECRET);
		return getAccessToken(requestParam);
	}
	/**
	 * 
	 * refresh_token을 이용하여 access_token을 교환하는 메소드
	 * access token이 유효하여야 한다.
	 * auth server의 endpoint에 대한 security 인증은 client_id, client_password로 이루어진다. 이 부분이 없다면 401 Unauthorized 에러가 발생한다.
	 * 
	 * @param refreshToken access_token 발급시 부여받은 refresh_token
	 * @return 갱신된 access token
	 * @throws Exception
	 */
	private AccessToken getAccessTokenByRefreshToken(String refreshToken) throws Exception {
		LOG.info("----- refresh token test");
		MultiValueMap<String, String> requestParam = new LinkedMultiValueMap<>();
		requestParam.add("refresh_token", refreshToken);
		requestParam.add("grant_type", GrantTypes.REFRESH_TOKEN);
		requestParam.add("client_id", CLIENT_ID);
		requestParam.add("client_secret", CLIENT_SECRET);
		return getAccessToken(requestParam);
	}
	/**
	 * AuthenticationManager에 등록된 id와 password를 통해 access token을 부여하는 password grant 방식으로 access token을 받는다.
	 * 이 방식은 third-party에게 제공하기는 대단히 부적합하기 때문에 유의해야 한다.  
	 * 
	 * @return access token
	 * @throws Exception
	 */
	private AccessToken getAccessTokenForPassword() throws Exception {
		LOG.info("----- password grant access token test");
		MultiValueMap<String, String> requestParam = new LinkedMultiValueMap<>();
		requestParam.add("grant_type", "password");
		requestParam.add("client_id", CLIENT_ID);
		requestParam.add("client_secret", CLIENT_SECRET);
		requestParam.add("scope", "read+check");
		requestParam.add("username", username);
		requestParam.add("password", password);
		return getAccessToken(requestParam);
	}
	/**
	 * 
	 * authorization_code grant_type에서 access_token 교환에 사용할 request_token을 받는다.
	 * 이 flow는 일반적으로 client가 redirect_uri로 redirect하면서 해당 주소에 parameter로 code를 전달하는 방식이다.
	 * 그러므로 단위테스트에서는 redirect 되는 주소의 queryString을 파싱한다.
	 * 
	 * @return request_token 문자열, 이 request_token을 access_token 교환시 사용한다.
	 * @throws Exception
	 */
	private String getRequestTokenForAuthorizationCode() throws Exception {
		LOG.info("----- request token test");
		MultiValueMap<String, String> requestParam = new LinkedMultiValueMap<>();
		requestParam.add("response_type", "code");
		requestParam.add("redirect_uri", CALLBACK_URL);
		requestParam.add("client_id", CLIENT_ID);
		requestParam.add("client_secret", CLIENT_SECRET);
		requestParam.add("scope", "read+check");
		MockHttpServletRequestBuilder reqBuilder = post(REQUEST_TOKEN_ENDPOINT).with(authentication(auth)).params(requestParam).contentType(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.APPLICATION_JSON_UTF8).characterEncoding("UTF-8");
		ResultActions result = mvc.perform(reqBuilder).andExpect(status().is3xxRedirection());
		return result.andReturn().getResponse().getRedirectedUrl().split("\\?code=")[1];
	}
}
