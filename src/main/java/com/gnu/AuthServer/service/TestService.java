package com.gnu.AuthServer.service;

import org.springframework.security.access.prepost.PreAuthorize;

public interface TestService {
	@PreAuthorize("#auth.isOk(#bool)")
	String hello(boolean bool);

}