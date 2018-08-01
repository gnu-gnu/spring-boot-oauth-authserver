package com.gnu.AuthServer.method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

import com.gnu.AuthServer.config.AuthServerWebSecurityConfig;

/**
 * 
 * 이 클래스의 public boolean 메소드들이 MethodSecurityExpression 으로 쓰임(eg : PreAuthorize, PostAuthorize)
 * 
 * @author gnu-gnu(geunwoo.j.shim@gmail.com)
 *
 */
public class AuthServerMethodSecurityExpression {
	
	private Authentication auth;
	private static final Logger logger = LoggerFactory.getLogger(AuthServerWebSecurityConfig.class);
	
	public AuthServerMethodSecurityExpression(Authentication auth) {
		this.auth = auth;
	}

	public boolean isOk() {
		logger.info(auth.toString());
		return true;
	}
}
