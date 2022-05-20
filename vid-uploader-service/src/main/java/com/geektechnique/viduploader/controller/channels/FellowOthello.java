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
        value = "fellow-othello.enabled",
        havingValue = "true"
)
@RestController
@RequestMapping("/fellow-othello")
public class FellowOthello extends BaseVideoController {
    public FellowOthello() {
        super(
                new Uploader(
                        new BaseConfigModel(
                                new RedisConfig(0),
                                "fellow-othello",
                                "/home/pi/vid-upload/fellow-othello/.client_secret.json",
                                "/home/pi/vid-upload/fellow-othello/.youtube-upload-credentials.json"
                        )
                )
        );
    }

    @Scheduled(cron = cronTiming)
    private void uploadTiming() {
        super.uploader.uploadOnSchedule();
    }
}