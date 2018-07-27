package com.gnu.AuthServer.service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Service;
/**
 * 
 * Access token 발급에 관한 서비스
 * Access token에 global로 기록될 사항들을 이 서비스에서 관리할 수 있다.
 * 
 * @author gnu-gnu(geunwoo.j.shim@gmail.com)
 *
 */
@Service
public class AuthTokenService extends DefaultTokenServices {
	Logger logger = LoggerFactory.getLogger(AuthTokenService.class);

	@Autowired
	TokenStore tokenStore;
	
	@PostConstruct
	public void init(){
		this.setRefreshTokenValiditySeconds(86400);
		this.setAccessTokenValiditySeconds(1800);
		this.setSupportRefreshToken(true);
		this.setTokenStore(tokenStore);
		/*
		 * Token 발급시 부가 정보등을 기록하기 위한 메소드
		 */
		this.setTokenEnhancer(new TokenEnhancer() {
			@SuppressWarnings("unchecked")
			@Override
			public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
				logger.info("token enhance");
				Map<String, Object> details = new HashMap<>();
				logger.info(authentication.getUserAuthentication().toString());
				details.putAll((Map<? extends String, ? extends Object>) authentication.getUserAuthentication().getDetails());
				details.put("authorities", authentication.getAuthorities().stream().map(value->value.getAuthority()).distinct().collect(Collectors.toList()));
				((DefaultOAuth2AccessToken)accessToken).setAdditionalInformation(details);
				return accessToken;
			}
		});
	}
	
	@Override
	public void setTokenEnhancer(TokenEnhancer accessTokenEnhancer) {
		super.setTokenEnhancer(accessTokenEnhancer);
	}

	@Override
	public void setAccessTokenValiditySeconds(int accessTokenValiditySeconds) {
		// TODO Auto-generated method stub
		super.setAccessTokenValiditySeconds(accessTokenValiditySeconds);
	}

	@Override
	public void setReuseRefreshToken(boolean reuseRefreshToken) {
		// TODO Auto-generated method stub
		super.setReuseRefreshToken(reuseRefreshToken);
	}

	@Override
	public void setTokenStore(TokenStore tokenStore) {
		// TODO Auto-generated method stub
		super.setTokenStore(tokenStore);
	}


}
