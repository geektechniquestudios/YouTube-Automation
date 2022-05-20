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
        value = "crossing-over.enabled",
        havingValue = "true"
)
@RestController
@RequestMapping("/crossing-over")
public class CrossingOver extends BaseVideoController {
    public CrossingOver() {
        super(
                new Uploader(
                        new BaseConfigModel(
                                new RedisConfig(0),
                                "crossing-over",
                                "/home/pi/vid-upload/crossing-over/.client_secret.json",
                                "/home/pi/vid-upload/crossing-over/.youtube-upload-credentials.json"
                        )
                )
        );
    }

    @Scheduled(cron = cronTiming)
    private void uploadTiming() {
        super.uploader.uploadOnSchedule();
    }
}