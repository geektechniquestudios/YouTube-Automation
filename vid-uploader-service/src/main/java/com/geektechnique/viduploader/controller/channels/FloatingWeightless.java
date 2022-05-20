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
        value = "floating-weightless.enabled",
        havingValue = "true"
)
@RestController
@RequestMapping("/floating-weightless")
public class FloatingWeightless extends BaseVideoController {
    public FloatingWeightless() {
        super(
                new Uploader(
                        new BaseConfigModel(
                                new RedisConfig(0),
                                "floating-weightless",
                                "/home/pi/vid-upload/floating-weightless/.client_secret.json",
                                "/home/pi/vid-upload/floating-weightless/.youtube-upload-credentials.json"
                        )
                )
        );
    }

    @Scheduled(cron = cronTiming)
    private void uploadTiming() {
        super.uploader.uploadOnSchedule();
    }
}