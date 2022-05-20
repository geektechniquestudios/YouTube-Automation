package com.geektechnique.viduploader.service;

import com.geektechnique.viduploader.config.DateTimeConfig;
import com.geektechnique.viduploader.config.RedisConfig;
import com.geektechnique.viduploader.model.UploadResponseModel;
import com.geektechnique.viduploader.model.BaseConfigModel;
import com.geektechnique.viduploader.model.MetadataModel;
import lombok.Getter;
import lombok.Synchronized;

import javax.annotation.PostConstruct;
import java.time.ZonedDateTime;
import java.util.*;


public class Uploader implements Uploadable {
    @Getter
    private List<MetadataModel> videoUploadQueue = Collections.synchronizedList(new ArrayList<>());
    @Getter
    BaseConfigModel baseConfigModel;
    @Getter
    String timeOfLastAddedVid = "N/A";

    DateTimeConfig dateTimeConfig = new DateTimeConfig();
    RedisConfig redisConfig;
    String category;
    String clientSecrets;
    String credentialsFile;

    public Uploader(BaseConfigModel baseConfigModel) {
        this.baseConfigModel = baseConfigModel;
        this.redisConfig = baseConfigModel.getRedisConfig();
        this.category = baseConfigModel.getChannel();
        this.clientSecrets = baseConfigModel.getClientSecrets();
        this.credentialsFile = baseConfigModel.getCredentialsFile();

        timeOfLastAddedVid = redisConfig.getSyncCommands().get(baseConfigModel.getChannel() + "-last-time-rendered");

        enumerateVideoQueue();
    }

    private void enumerateVideoQueue() {
        videoUploadQueue = enumerateVidQueueInterface(redisConfig, baseConfigModel.getChannel());
    }

    @Override
    public void addVideoToQueue(MetadataModel vidToAdd) {
        videoUploadQueue = addVideoToQueueInterface(videoUploadQueue, vidToAdd, redisConfig, category);
        ZonedDateTime zonedDateTime = ZonedDateTime.now(dateTimeConfig.getZoneId());
        timeOfLastAddedVid = dateTimeConfig.getFormatter().format(zonedDateTime);
        redisConfig.getSyncCommands().set(baseConfigModel.getChannel() + "-last-time-rendered", timeOfLastAddedVid);
    }

    @Override
    @Synchronized
    public void uploadOnSchedule() { //@todo delay vs synchronized
        uploadNextVid(-1);
    }

    @Override
    public UploadResponseModel uploadNextVid(int vidToUploadNum) {
        return uploadNextVidInterface(
                videoUploadQueue,
                redisConfig,
                category,
                clientSecrets,
                credentialsFile,
                vidToUploadNum,
                baseConfigModel
        );
    }

    @Override
    public String skipVideo() {
        return skipVideoInterface(redisConfig, videoUploadQueue, baseConfigModel.getChannel());
    }
}