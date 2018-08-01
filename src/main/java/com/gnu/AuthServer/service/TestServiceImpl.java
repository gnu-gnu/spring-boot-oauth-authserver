package com.gnu.AuthServer.service;

import org.springframework.stereotype.Service;
/**
 * 
 * 서비스 레이어에 메소드 표현식 기반 인가 프로세스 적용
 * 인터페이스에 적용 해 두었기 때문에 서비스 레이어는 구현만 하면 됨
 * 
 * @author gnu-gnu(geunwoo.j.shim@gmail.com)
 *
 */
@Service
public class TestServiceImpl implements TestService {
	
	@Override
	public String hello(boolean bool) {
		return "Hello?";
	}
}
