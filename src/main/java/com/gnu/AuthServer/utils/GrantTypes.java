package com.gnu.AuthServer.utils;
/**
 * 
 * access_token을 발급하는데 필요한 grant type을 정의<br>
 * 각 방식의 특징은 다음과 같다<br>
 * <b>PASSWORD</b> : 사용자에게 ID와 PASSWORD를 받고 access_token을 발급한다<br>
 * <b>AUTHORIZATION_CODE</b> : ~~/authorize에서 발급된 request_token(authorization_code)을 access_token으로 교환한다<br>
 * <b>CLIENT_CREDENTIALS</b> : 신뢰하는 client에 부여된 client_credentials를 받아 access_token을 발급한다.<br>
 * <b>REFRESH_TOKEN</b> : 만료가 임박한 access_token을 새로 갱신한다<br>
 * 
 * @author Geunwoo.Shim (gflhsin@gmail.com)
 */
public interface GrantTypes {
	String PASSWORD = "password";
	String AUTHORIZATION_CODE = "authorization_code";
	String CLIENT_CREDENTIALS = "client_credentials";
	String REFRESH_TOKEN = "refresh_token";
}
