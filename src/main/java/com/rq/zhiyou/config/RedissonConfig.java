package com.rq.zhiyou.config;

import io.lettuce.core.RedisClient;
import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 若倾
 * @version 1.0
 * @description TODO
 * @date 2023/5/20 17:21
 */
@ConfigurationProperties(prefix = "spring.redis")
@Configuration
@Data
public class RedissonConfig {

    private String host;

    private String port;

    private String password;

    @Bean
    public RedissonClient redissonClient(){
        // 1. Create config object
        Config config = new Config();
        String redisAddress=String.format("redis://%s:%s",host,port);
        config.useSingleServer().setAddress(redisAddress).setDatabase(3).setPassword(password);
        // 2. Create Redisson instance
        // Sync and Async API
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}
