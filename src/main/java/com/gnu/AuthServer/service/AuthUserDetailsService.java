package com.gnu.AuthServer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.gnu.AuthServer.entity.UserEntity;
import com.gnu.AuthServer.repository.UserRepository;
import com.gnu.AuthServer.security.AuthUserDetails;

@Configuration
public class AuthUserDetailsService implements UserDetailsService{
	Logger logger = LoggerFactory.getLogger(AuthUserDetailsService.class);
	
	@Autowired
	UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		logger.info(username+"... trying to login");
		UserEntity findEntity = userRepository.findByUsername(username);
		if(null == findEntity){
			logger.info(username+"... user not found");
			throw new UsernameNotFoundException(username);
		}
		logger.info(username+"... login success");
		AuthUserDetails details = new AuthUserDetails(findEntity);
		System.out.println(details);
		return details;
	}

}
