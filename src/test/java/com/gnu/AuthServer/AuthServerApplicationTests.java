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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
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
import com.gnu.AuthServer.service.AuthUserDetailsService;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@SpringBootTest(classes=AuthServerApplication.class)
public class AuthServerApplicationTests {
	Logger logger = LoggerFactory.getLogger(AuthServerApplicationTests.class);

	@Autowired
	private WebApplicationContext ctx;
	
	@Autowired
	AuthUserDetailsService userDetailsService;
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
		logger.info("----- unit test initialize");
		username = "gnu";
		password = "pass";
		this.mvc = MockMvcBuilders.webAppContextSetup(ctx).addFilters(filterChainProxy).build();
		auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
	}
	
	@Test
	public void testGetRequestToken() throws Exception {
		testFormLogin();
		String requestToken = getRequestToken();
		assertThat(requestToken).isNotEmpty();
		assertThat(getAccessToken(requestToken)).isNotNull();
	}
	
	private void testFormLogin() throws Exception {
		logger.info("----- form login test");
		ResultActions result = mvc.perform(formLogin("/login").user(username).password(password));
		assertThat(result.andReturn().getResponse().getStatus()).isNotSameAs(500);
	}
	
	private AccessToken getAccessToken(String requestToken) throws Exception{
		logger.info("----- access token test");
		MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
		request.add("grant_type", "authorization_code");
		request.add("code", requestToken);
		request.add("redirect_uri", "http://localhost:7077/resources/open/callback");
		request.add("client_id", "gnu-gnu");
		request.add("client_secret", "gnu-secret");
		/* 
		 * /oauth/token endpoint의 경우 ClientCredentialsTokenEndpointFilter가 등록되어 있음.
		 * client_id, client_secret을 이용하여 Authentication을 수행하므로 username, password 기반 토큰을 엔드포인트 인증을 위해 끼워넣을 경우 오히려 에러가 발생함 
	     */ 
		MockHttpServletRequestBuilder reqBuilder = post("/oauth/token").params(request).contentType(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.APPLICATION_JSON_UTF8).characterEncoding("UTF-8");
		ResultActions result = mvc.perform(reqBuilder).andExpect(status().isOk());
		String responseBody = result.andReturn().getResponse().getContentAsString();
		ObjectMapper mapper = new ObjectMapper();
		mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
		AccessToken accessToken = mapper.readValue(responseBody, AccessToken.class);
		logger.info("raw response : {}", responseBody);
		logger.info("Access token dto : {}", accessToken.toString());
		return accessToken;
	}
	
	private String getRequestToken() throws Exception {
		logger.info("----- request token test");
		MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
		request.add("response_type", "code");
		request.add("redirect_uri", "http://localhost:7077/resources/open/callback");
		request.add("client_id", "gnu-gnu");
		request.add("client_secret", "gnu-secret");
		request.add("scope", "read+check");
		MockHttpServletRequestBuilder reqBuilder = post("/oauth/authorize").with(authentication(auth)).params(request).contentType(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.APPLICATION_JSON_UTF8).characterEncoding("UTF-8");
		ResultActions result = mvc.perform(reqBuilder).andExpect(status().is3xxRedirection());
		return result.andReturn().getResponse().getRedirectedUrl().split("\\?code=")[1];
	}
}
