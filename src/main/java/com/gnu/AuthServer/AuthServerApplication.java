package com.gnu.AuthServer;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.gnu.AuthServer.entity.UserEntity;
import com.gnu.AuthServer.repository.UserRepository;

@SpringBootApplication
public class AuthServerApplication extends WebMvcConfigurerAdapter implements CommandLineRunner{
	Logger logger = LoggerFactory.getLogger(AuthServerApplication.class);
	final Marker REQUEST_MARKER = MarkerFactory.getMarker("HTTP_REQUEST");
	@Autowired
	UserRepository userRepository;
	@Resource(name="springSecurityFilterChain")
	FilterChainProxy springSecurityFilterChain;

	public static void main(String[] args) {
		SpringApplication.run(AuthServerApplication.class, args);
	}
	@Bean
	@Profile("redis")
	public TokenStore tokenStore(RedisConnectionFactory factory){
		// Redis가 설정된 상태에서는 redis store
		return new RedisTokenStore(factory);
	}
	@Bean
	public TokenStore InMemoryTokenStore(){
		// 기본 토큰 스토어는 테스트용도로 in-memory store
		return new InMemoryTokenStore();
	}

	
	/**
	 * 초기 ID/PW 데이터 설정 및 권한(=scope로 이용) 부여
	 */
	@Override
	public void run(String... arg0) throws Exception {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		UserEntity userShim = new UserEntity("gnu", encoder.encode("pass"));
		userShim.createAuthority("read");
		userShim.createAuthority("write");
		userShim.createAuthority("delete");
		userShim.createAuthority("update");
		userShim.setLocale("ko");
		userRepository.save(userShim);
		UserEntity userNoh = new UserEntity("noh", encoder.encode("pass"));
		userNoh.createAuthority("read");
		userShim.setLocale("jp");
		userRepository.save(userNoh);
		UserEntity userJee = new UserEntity("jee", encoder.encode("pass"));
		userNoh.createAuthority("read");
		userNoh.createAuthority("write");
		userNoh.createAuthority("update");
		userShim.setLocale("en");
		userRepository.save(userJee);
		springSecurityFilterChain.getFilterChains().forEach(value->{
			value.getFilters().forEach(filter->logger.info(filter.toString()));
		});
	}
}
