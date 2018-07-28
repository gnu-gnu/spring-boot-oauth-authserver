package com.gnu.AuthServer.dto;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class AccessToken {
//	{"access_token":"c5352e5e-e451-4047-89dc-bb607827af23","token_type":"bearer","refresh_token":"f19c036b-43aa-4d2f-b774-8a55369a0575","expires_in":1799,"scope":"check read","uid":1,"uname":"gnu","locale":"ko","authorities":["read","ACTUATOR","update","write","delete"]}
	private String accessToken;
	private String tokenType;
	private String refreshToken;
	private int expiresIn;
	private List<String> scope;
	private int uid;
	private String uname;
	private String locale;
	private Set<String> authorities;
	public String getAccessToken() {
		return accessToken;
	}
	
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	public String getTokenType() {
		return tokenType;
	}
	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}
	public String getRefreshToken() {
		return refreshToken;
	}
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	public int getExpiresIn() {
		return expiresIn;
	}
	public void setExpiresIn(int expiresIn) {
		this.expiresIn = expiresIn;
	}
	public List<String> getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = Arrays.asList(scope.split(" "));
	}
	public int getUid() {
		return uid;
	}
	public void setUid(int uid) {
		this.uid = uid;
	}
	public String getUname() {
		return uname;
	}
	public void setUname(String uname) {
		this.uname = uname;
	}
	public String getLocale() {
		return locale;
	}
	public void setLocale(String locale) {
		this.locale = locale;
	}
	public Set<String> getAuthorities() {
		return authorities;
	}
	public void setAuthorities(Set<String> authorities) {
		this.authorities = authorities;
	}

	@Override
	public String toString() {
		return "AccessToken [accessToken=" + accessToken + ", tokenType=" + tokenType + ", refreshToken=" + refreshToken
				+ ", expiresIn=" + expiresIn + ", scope=" + scope + ", uid=" + uid + ", uname=" + uname + ", locale="
				+ locale + ", authorities=" + authorities + "]";
	}
	
	
}
