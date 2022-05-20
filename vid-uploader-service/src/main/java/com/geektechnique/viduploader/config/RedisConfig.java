package com.geektechnique.viduploader.config;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class RedisConfig {
    @NonNull int dbNum; //@todo should probably elim this and just do db 0 for all

    final private RedisClient redisClient = RedisClient.create("redis://10.0.0.20:6379/" + dbNum);
    final private StatefulRedisConnection<String, String> connection = redisClient.connect();
    @Getter
    final private RedisCommands<String, String> syncCommands = connection.sync();
    @Getter
    final private RedisAsyncCommands<String, String> asyncCommands = connection.async();
}