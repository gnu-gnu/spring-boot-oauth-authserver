package com.gnu.AuthServer.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.gnu.AuthServer.service.TestService;

@RestController
public class TestController {
	@Autowired
	TestService service;
	Logger logger = LoggerFactory.getLogger(TestController.class);
	/**
	 * AuthServerMethodSecurityConfig의 createExpressionHandler 메소드 override를 주석처리하고 기본 Handler를 타게 한 후 
	 * /open에 접속 해 보면 bean의 메소드 기반으로 타는 것을 확인 가능
	 * @return
	 */
	@RequestMapping("/open")
	@PreAuthorize("@customChecker.isChecked(#auth)")
	public @ResponseBody String open(Authentication auth) {
		logger.info("/open is PermitAll");
		return "open";
	}
	/**
	 * 이 endpoint는 websecurity에서 permitAll로 오픈되어 있지만, method security가 적용된 메소드
	 * 현재 isOk(boolean bool)는 bool= 값으로 들어온 true/false 에 따라 인증 성공 / 실패를 보여준다.
	 * @return 인증이 성공할 경우 hello? 라는 문자열 출력
	 */
	@RequestMapping("/isok")
	public @ResponseBody String isok(boolean bool) {
		return service.hello(bool);
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
