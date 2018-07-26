package com.gnu.AuthServer.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.stereotype.Service;

import com.gnu.AuthServer.utils.GrantTypes;

@Service
public class AuthClientDetailsService implements ClientDetailsService {

	@Override
	public ClientDetails loadClientByClientId(String arg0) throws ClientRegistrationException {
		System.out.println("clientId check...");
		Set<String> set = new HashSet<>();
		set.add("http://localhost:7077/resources/open/callback");
		BaseClientDetails details = new BaseClientDetails();
		details.setClientId("lookin");
		details.setAuthorizedGrantTypes(Arrays.asList(GrantTypes.AUTHORIZATION_CODE, GrantTypes.PASSWORD));
		details.setClientSecret("hello");
		details.setRegisteredRedirectUri(set);
		details.setScope(Arrays.asList("check"));
		details.setAuthorities(AuthorityUtils.commaSeparatedStringToAuthorityList("read"));
		return details;
	}

}
