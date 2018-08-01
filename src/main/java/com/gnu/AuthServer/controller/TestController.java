package com.gnu.AuthServer.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
	Logger logger = LoggerFactory.getLogger(TestController.class);
	/**
	 * security.properties에 permitall로 등록되어 완전히 오픈된 endpoint 
	 * @return
	 */
	@RequestMapping("/open")
	public @ResponseBody String open() {
		logger.info("/open is PermitAll");
		return "open";
	}
	/**
	 * security.properties에 permitall로 web security는 오픈되어 있지만, method security가 적용된 메소드 
	 * @return
	 */
	@RequestMapping("/isok")
	@PostAuthorize("#auth.isOk()")
	public @ResponseBody String isok() {
		return "hello?";
	}
	/**
	 * web security에서 인증을 요구하는 메소드 
	 * @param auth
	 * @return
	 */
	@RequestMapping("/test")
	public @ResponseBody String test(Authentication auth) {
		return "hello?";
	}
}
