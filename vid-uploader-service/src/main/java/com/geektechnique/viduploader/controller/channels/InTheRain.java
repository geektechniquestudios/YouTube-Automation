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
        value = "in-the-rain.enabled",
        havingValue = "true"
)
@RestController
@RequestMapping("/in-the-rain")
public class InTheRain extends BaseVideoController {
    public InTheRain() {
        super(
                new Uploader(
                        new BaseConfigModel(
                                new RedisConfig(0),
                                "in-the-rain",
                                "/home/pi/vid-upload/in-the-rain/.client_secret.json",
                                "/home/pi/vid-upload/in-the-rain/.youtube-upload-credentials.json"
                        )
                )
        );
    }

    @Scheduled(cron = cronTiming)
    private void uploadTiming() {
        super.uploader.uploadOnSchedule();
    }
}