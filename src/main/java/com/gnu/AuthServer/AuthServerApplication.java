package com.gnu.AuthServer;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	@Autowired
	UserRepository userRepository;
	@Resource(name="springSecurityFilterChain")
	FilterChainProxy springSecurityFilterChain;

	public static void main(String[] args) {
		SpringApplication.run(AuthServerApplication.class, args);
	}
	
	@Bean
	@Profile("local") // 기본 active profile 은 application.properties에 local로 설정되어 있다.
	public TokenStore InMemoryTokenStore(){
		logger.info("in-memory token store");
		return new InMemoryTokenStore();
	}
	
	@Bean
	@Profile("redis") // redis token store 설정을 위해서는 -Dspring.profiles.active=redis 가 필요
	public TokenStore tokenStore(RedisConnectionFactory factory){
		logger.info("redis token store");
		return new RedisTokenStore(factory);
	}

	
	/**
	 * 초기 ID/PW 데이터 설정J
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
