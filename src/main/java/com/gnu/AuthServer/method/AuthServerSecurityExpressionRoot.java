package com.gnu.AuthServer.method;

import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.core.Authentication;

/**
 * 
 * 
 * 
 * @author gnu-gnu(geunwoo.j.shim@gmail.com)
 *
 */
public class AuthServerSecurityExpressionRoot extends SecurityExpressionRoot {

	public AuthServerSecurityExpressionRoot(Authentication authentication) {
		super(authentication);
	}

}
