package com.yaoyao.online.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/7 09:41
 * @Description:session会话保存
 */
@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 86400)
public class RedisSessionConfig {

    @Bean
    public RedisTemplate<String,String> redisTemplate(RedisConnectionFactory factory){
        return new StringRedisTemplate(factory);
    }
}
