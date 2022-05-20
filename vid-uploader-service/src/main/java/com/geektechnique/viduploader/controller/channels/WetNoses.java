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
        value = "wet-noses.enabled",
        havingValue = "true"
)
@RestController
@RequestMapping("/wet-noses")
public class WetNoses extends BaseVideoController {
    public WetNoses() {
        super(
                new Uploader(
                        new BaseConfigModel(
                                new RedisConfig(0),
                                "wet-noses",
                                "/home/pi/vid-upload/wet-noses/.client_secret.json",
                                "/home/pi/vid-upload/wet-noses/.youtube-upload-credentials.json"
                        )
                )
        );
    }

    @Scheduled(cron = cronTiming)
    private void uploadTiming() {
        super.uploader.uploadOnSchedule();
    }
}