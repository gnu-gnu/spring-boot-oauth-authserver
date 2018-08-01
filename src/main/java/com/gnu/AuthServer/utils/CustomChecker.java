package com.gnu.AuthServer.utils;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class CustomChecker {

	public boolean isChecked(Authentication auth) {
		System.out.println(auth);
		System.out.println("checked");
		return true;
	}
}
