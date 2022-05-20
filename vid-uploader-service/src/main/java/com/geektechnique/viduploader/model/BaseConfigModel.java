package com.geektechnique.viduploader.model;

import com.geektechnique.viduploader.config.RedisConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class BaseConfigModel {
    private final RedisConfig redisConfig;
    private final String channel;
    private final String clientSecrets;
    private final String credentialsFile;
}