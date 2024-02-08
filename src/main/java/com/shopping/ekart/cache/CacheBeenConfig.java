package com.shopping.ekart.cache;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.shopping.ekart.entity.User;

@Configuration
public class CacheBeenConfig {
	
	
	@Bean
	public CacheStore<User> userCacheStrore(){
		return new CacheStore<User>(Duration.ofMinutes(5));
	}
	
	@Bean
	public CacheStore<String> otpCacheStore(){
		return new CacheStore<String>(Duration.ofMinutes(1));
	}

}
