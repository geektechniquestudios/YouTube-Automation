package com.geektechnique.notificationserivce;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {
    final private RedisClient redisClient = RedisClient.create("redis://10.0.0.20:6379/4");
    final private StatefulRedisConnection<String, String> connection = redisClient.connect();
    final private RedisCommands<String, String> syncCommands = connection.sync();


    public void set(String k, String v){
        syncCommands.set(k, v);
    }

    public String get(String k){
        return syncCommands.get(k);
    }

    public String ping(){ return syncCommands.ping();}

    public String hget(String h, String k){
        return syncCommands.hget(h, k);
    }

    public void hset(String h, String k, String v){ syncCommands.hset(h, k, v);}
}
