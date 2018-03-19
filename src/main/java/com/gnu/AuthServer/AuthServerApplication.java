package com.gnu.AuthServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@SpringBootApplication
public class AuthServerApplication extends WebMvcConfigurerAdapter{
	Logger logger = LoggerFactory.getLogger(AuthServerApplication.class);
	final Marker REQUEST_MARKER = MarkerFactory.getMarker("HTTP_REQUEST");

	public static void main(String[] args) {
		SpringApplication.run(AuthServerApplication.class, args);
	}
	@Bean
	public TokenStore tokenStore(RedisConnectionFactory factory){
		/**
		 * redis를 token 저장소로 사용하기 위해 설정
		 * 이 부분을 제거하면 inMemory 저장소를 사용함
		 */
		return new RedisTokenStore(factory);
	}
}
