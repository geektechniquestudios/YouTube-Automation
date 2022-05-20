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
        value = "gooey-is-better.enabled",
        havingValue = "true"
)
@RestController
@RequestMapping("/gooey-is-better")
public class GooeyIsBetter extends BaseVideoController {
    public GooeyIsBetter() {
        super(
                new Uploader(
                        new BaseConfigModel(
                                new RedisConfig(0),
                                "gooey-is-better",
                                "/home/pi/vid-upload/gooey-is-better/.client_secret.json",
                                "/home/pi/vid-upload/gooey-is-better/.youtube-upload-credentials.json"
                        )
                )
        );
    }

    @Scheduled(cron = cronTiming)
    private void uploadTiming() {
        super.uploader.uploadOnSchedule();
    }
}