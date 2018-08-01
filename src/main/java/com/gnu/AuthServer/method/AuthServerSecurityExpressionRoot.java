package com.gnu.AuthServer.method;

import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.core.Authentication;

/**
 * 
 * MethodSecurityExpressionRoot는 modifier가 왜 public이 아닌지 알 수가 없음.
 * 그냥 바로 써도 될 것 같은데 인스턴스 생성이 불가능하므로 SecurityExpressionRoot를 상속하여 구현 
 * 아래에 위 클래스의 modifier 관련 이슈가 있음
 * @see https://github.com/spring-projects/spring-security/pull/4266 
 *  
 * @author gnu-gnu(geunwoo.j.shim@gmail.com)
 *
 */
public class AuthServerSecurityExpressionRoot extends SecurityExpressionRoot {
	public AuthServerSecurityExpressionRoot(Authentication authentication) {
		super(authentication);
	}
}
