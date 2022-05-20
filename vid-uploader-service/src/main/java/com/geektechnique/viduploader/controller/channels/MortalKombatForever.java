package com.geektechnique.viduploader.controller.channels;

import com.geektechnique.viduploader.config.RedisConfig;
import com.geektechnique.viduploader.controller.BaseVideoController;
import com.geektechnique.viduploader.model.BaseConfigModel;
import com.geektechnique.viduploader.service.Uploader;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@ConditionalOnProperty(
        value = "mortal-kombat-forever.enabled",
        havingValue = "true"
)
@RestController
@RequestMapping("/mortal-kombat-forever")
public class MortalKombatForever extends BaseVideoController {
    public MortalKombatForever() {
        super(
                new Uploader(
                        new BaseConfigModel(
                                new RedisConfig(0),
                                "mortal-kombat-forever",
                                "/home/pi/vid-upload/mortal-kombat-forever/.client_secret.json",
                                "/home/pi/vid-upload/mortal-kombat-forever/.youtube-upload-credentials.json"
                        )
                )
        );
    }

    @Scheduled(cron = cronTiming)
    private void uploadTiming() {
        super.uploader.uploadOnSchedule();
    }
}