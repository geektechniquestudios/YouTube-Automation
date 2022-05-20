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
        value = "fire-inside.enabled",
        havingValue = "true"
)
@RestController
@RequestMapping("/fire-inside")
public class FireInside extends BaseVideoController {
    public FireInside() {
        super(
                new Uploader(
                        new BaseConfigModel(
                                new RedisConfig(0),
                                "fire-inside",
                                "/home/pi/vid-upload/fire-inside/.client_secret.json",
                                "/home/pi/vid-upload/fire-inside/.youtube-upload-credentials.json"
                        )
                )
        );
    }

    @Scheduled(cron = cronTiming)
    private void uploadTiming() {
        super.uploader.uploadOnSchedule();
    }
}