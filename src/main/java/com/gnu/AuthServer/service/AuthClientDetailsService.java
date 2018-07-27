package com.gnu.AuthServer.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.gnu.AuthServer.utils.GrantTypes;

@Service
public class AuthClientDetailsService implements ClientDetailsService {
	@Value("${authserver.auto.approval.scopes}")
	private String autoScopes;
	
	@Autowired
	TokenStore tokenStore;
	
	@Override
	public ClientDetails loadClientByClientId(String arg0) throws ClientRegistrationException {
		BaseClientDetails details = new BaseClientDetails();
		Set<String> set = new HashSet<>();
		set.add("http://localhost:7077/resources/open/callback");
		details.setClientId("lookin");
		details.setAuthorizedGrantTypes(Arrays.asList(GrantTypes.AUTHORIZATION_CODE, GrantTypes.PASSWORD));
		details.setClientSecret("hello");
		details.setRegisteredRedirectUri(set);
		details.setScope(StringUtils.commaDelimitedListToSet(autoScopes));
		details.setAuthorities(AuthorityUtils.commaSeparatedStringToAuthorityList("read"));
		details.setAutoApproveScopes(StringUtils.commaDelimitedListToSet(autoScopes));
		return details;
	}

}
