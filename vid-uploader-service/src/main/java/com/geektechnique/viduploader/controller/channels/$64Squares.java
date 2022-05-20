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
        value = "64-squares.enabled",
        havingValue = "true"
)
@RestController
@RequestMapping("/64-squares")
public class $64Squares extends BaseVideoController {
    public $64Squares() {
        super(
                new Uploader(
                        new BaseConfigModel(
                                new RedisConfig(0),
                                "64-squares",
                                "/home/pi/vid-upload/64-squares/.client_secret.json",
                                "/home/pi/vid-upload/64-squares/.youtube-upload-credentials.json"
                        )
                )
        );
    }

    @Scheduled(cron = cronTiming)
    private void uploadTiming() {
        super.uploader.uploadOnSchedule();
    }
}