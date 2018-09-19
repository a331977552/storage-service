package com.storage.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@PropertySource("classpath:myapp.properties")
public class RedisConfig {
	@Value("${redis.address}")
	String address;
	@Bean
	public RedisConnectionFactory
	getJedisFactory(){
		RedisStandaloneConfiguration  con=new RedisStandaloneConfiguration(address, 6379);
		JedisConnectionFactory jedisConFactory
		= new JedisConnectionFactory(con);
		return jedisConFactory;
		
	}
	
	@Bean
	public StringRedisTemplate getRedisTemplate() {
		StringRedisTemplate redisTemplate=new StringRedisTemplate();
		redisTemplate.setConnectionFactory(getJedisFactory());
		return redisTemplate;
	}
	
}
