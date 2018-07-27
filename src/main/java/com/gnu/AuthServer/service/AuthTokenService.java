package com.gnu.AuthServer.service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Service;

@Service
public class AuthTokenService extends DefaultTokenServices {
	@Autowired
	TokenStore tokenStore;
	
	@PostConstruct
	public void init(){
		this.setRefreshTokenValiditySeconds(86400);
		this.setAccessTokenValiditySeconds(1800);
		this.setSupportRefreshToken(true);
		this.setTokenStore(tokenStore);
		this.setTokenEnhancer(new TokenEnhancer() {
			@SuppressWarnings("unchecked")
			@Override
			public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
				System.out.println("token enhance");
				Map<String, Object> details = new HashMap<>();
				System.out.println(authentication.getUserAuthentication());
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
