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
	public void testGetRequestToken() throws Exception {
		testFormLogin();
		String requestToken = getRequestTokenForAuthorizationCode();
		assertThat(requestToken).isNotEmpty();
		assertThat(getAccessTokenForAuthorizationCode(requestToken)).isNotNull();
		assertThat(getAccessTokenForPassword()).isNotNull();
	}
	
	private void testFormLogin() throws Exception {
		LOG.info("----- form login test");
		ResultActions result = mvc.perform(formLogin(LOGIN_ENDPOINT).user(username).password(password));
		assertThat(result.andReturn().getResponse().getStatus()).isNotSameAs(500);
	}
	
	private AccessToken getAccessTokenForAuthorizationCode(String requestToken) throws Exception{
		LOG.info("----- authorization_code access token test");
		MultiValueMap<String, String> requestParam = new LinkedMultiValueMap<>();
		requestParam.add("grant_type", "authorization_code");
		requestParam.add("code", requestToken);
		requestParam.add("redirect_uri", CALLBACK_URL);
		requestParam.add("client_id", CLIENT_ID);
		requestParam.add("client_secret", CLIENT_SECRET);
		/* 
		 * /oauth/token endpoint의 경우 ClientCredentialsTokenEndpointFilter가 등록되어 있음.
		 * client_id, client_secret을 이용하여 Authentication을 수행하므로 username, password 기반 토큰을 엔드포인트 인증을 위해 끼워넣을 경우 오히려 에러가 발생함 
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
	
	private AccessToken getAccessTokenForPassword() throws Exception {
		LOG.info("----- password grant access token test");
		MultiValueMap<String, String> requestParam = new LinkedMultiValueMap<>();
		requestParam.add("grant_type", "password");
		requestParam.add("client_id", CLIENT_ID);
		requestParam.add("client_secret", CLIENT_SECRET);
		requestParam.add("scope", "read+check");
		requestParam.add("username", username);
		requestParam.add("password", password);
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
