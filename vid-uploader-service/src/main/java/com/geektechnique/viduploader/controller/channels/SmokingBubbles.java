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
        value = "smoking-bubbles.enabled",
        havingValue = "true"
)
@RestController
@RequestMapping("/smoking-bubbles")
public class SmokingBubbles extends BaseVideoController {
    public SmokingBubbles() {
        super(
                new Uploader(
                        new BaseConfigModel(
                                new RedisConfig(0),
                                "smoking-bubbles",
                                "/home/pi/vid-upload/smoking-bubbles/.client_secret.json",
                                "/home/pi/vid-upload/smoking-bubbles/.youtube-upload-credentials.json"
                        )
                )
        );
    }

    @Scheduled(cron = cronTiming)
    private void uploadTiming() {
        super.uploader.uploadOnSchedule();
    }
}