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
        value = "the-domino-show.enabled",
        havingValue = "true"
)
@RestController
@RequestMapping("/the-domino-show")
public class TheDominoShow extends BaseVideoController {
    public TheDominoShow() {
        super(
                new Uploader(
                        new BaseConfigModel(
                                new RedisConfig(0),
                                "the-domino-show",
                                "/home/pi/vid-upload/the-domino-show/.client_secret.json",
                                "/home/pi/vid-upload/the-domino-show/.youtube-upload-credentials.json"
                        )
                )
        );
    }

    @Scheduled(cron = cronTiming)
    private void uploadTiming() {
        super.uploader.uploadOnSchedule();
    }
}