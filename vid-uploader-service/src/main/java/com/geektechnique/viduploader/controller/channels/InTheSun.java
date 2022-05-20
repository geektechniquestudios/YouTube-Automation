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
        value = "in-the-sun.enabled",
        havingValue = "true"
)
@RestController
@RequestMapping("/in-the-sun")
public class InTheSun extends BaseVideoController {
    public InTheSun() {
        super(
                new Uploader(
                        new BaseConfigModel(
                                new RedisConfig(0),
                                "in-the-sun",
                                "/home/pi/vid-upload/in-the-sun/.client_secret.json",
                                "/home/pi/vid-upload/in-the-sun/.youtube-upload-credentials.json"
                        )
                )
        );
    }

    @Scheduled(cron = cronTiming)
    private void uploadTiming() {
        super.uploader.uploadOnSchedule();
    }
}