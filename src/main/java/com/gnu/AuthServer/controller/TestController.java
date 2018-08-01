package com.gnu.AuthServer.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
	 * 이 endpoint는 websecurity에서 permitAll로 오픈되어 있지만, method security가 적용된 메소드
	 * 현재 isOk(boolean bool)는 bool= 값으로 들어온 true/false 에 따라 인증 성공 / 실패를 보여준다.
	 * @return 인증이 성공할 경우 hello? 라는 문자열 출력
	 */
	@RequestMapping("/isok")
	@PreAuthorize("#auth.isOk(#bool)")
	public @ResponseBody String isok(boolean bool) {
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
