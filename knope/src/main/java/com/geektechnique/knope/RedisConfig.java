package com.geektechnique.knope;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.Getter;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RedisConfig {
    final private RedisClient redisClient = RedisClient.create("redis://10.0.0.20:6379/4");
    final private StatefulRedisConnection<String, String> connection = redisClient.connect();
    final private RedisCommands<String, String> syncCommands = connection.sync();

    public void set(String k, String v) {
        syncCommands.set(k, v);
    }

    public String get(String k) {
        return syncCommands.get(k);
    }

    public String hget(String h, String k) {
        return syncCommands.hget(h, k);
    }
}
