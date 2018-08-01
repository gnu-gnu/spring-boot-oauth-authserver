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
	/**
	 * #auth.isOk() expression을 호출할 경우 이 메소드를 call하게 된다. 이 메소드의 결과가 true, false 냐에 따라 인가  여부가 결정됨
	 * @return
	 */
	public boolean isOk(boolean bool) {
		logger.info(auth.toString());
		return bool;
	}
}