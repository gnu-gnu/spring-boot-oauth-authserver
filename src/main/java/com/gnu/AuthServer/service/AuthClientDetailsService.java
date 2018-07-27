package com.gnu.AuthServer.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
/**
 * 
 * client에 관련된 기능을 처리하는 서비스
 * client_id 및 secret을 기반으로 토큰 발급에 권한 인증 처리 및
 * 콜백URL 체크, 허용할 scope 및 인층 처리 프로세스간 client에 부여할 authority등이 이 클래스에서 처리 된다.
 * 
 * @author gnu-gnu(geunwoo.j.shim@gmail.com)
 *
 */
@Service
public class AuthClientDetailsService implements ClientDetailsService {
	Logger logger = LoggerFactory.getLogger(AuthClientDetailsService.class);

	@Value("${authserver.auto.approval.scopes}")
	private String autoScopes;
	
	@Autowired
	TokenStore tokenStore;
	
	@Override
	public ClientDetails loadClientByClientId(String arg0) throws ClientRegistrationException {
		/*
		 * token 요청시 입력하는 Callback url, client id 및 secret, scope 등은 이 메소드에서 작성하는 것과 일치하여야 한다.
		 * ClientDetailsService 가 JdbcClientDetailsService 구현한다면 해당 클래스에는 Client 를 추가, 수정, 삭제할 수 있는 메소드가 구현되어 있다.
		 * 해당 클래스를 구현할 경우 Client CRUD 에 에러가 발생하면 ClientRegistrationException을 발생시키도록 한다.
		 */
		logger.info("load client from \"{}\"", arg0);
		BaseClientDetails details = new BaseClientDetails();
		Set<String> set = new HashSet<>();
		set.add("http://localhost:7077/resources/open/callback");
		details.setClientId("gnu-gnu");
		details.setAuthorizedGrantTypes(Arrays.asList(GrantTypes.AUTHORIZATION_CODE, GrantTypes.PASSWORD, GrantTypes.REFRESH_TOKEN));
		details.setClientSecret("gnu-secret");
		details.setRegisteredRedirectUri(set);
		details.setScope(StringUtils.commaDelimitedListToSet(autoScopes));
		details.setAuthorities(AuthorityUtils.commaSeparatedStringToAuthorityList("read"));
		details.setAutoApproveScopes(StringUtils.commaDelimitedListToSet(autoScopes));
		return details;
	}
}
