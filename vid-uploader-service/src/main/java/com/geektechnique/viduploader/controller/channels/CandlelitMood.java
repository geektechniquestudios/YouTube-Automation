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
        value = "candlelit-mood.enabled",
        havingValue = "true"
)
@RestController
@RequestMapping("/candlelit-mood")
public class CandlelitMood extends BaseVideoController {
    public CandlelitMood() {
        super(
                new Uploader(
                        new BaseConfigModel(
                                new RedisConfig(0),
                                "candlelit-mood",
                                "/home/pi/vid-upload/candlelit-mood/.client_secret.json",
                                "/home/pi/vid-upload/candlelit-mood/.youtube-upload-credentials.json"
                        )
                )
        );
    }

    @Scheduled(cron = cronTiming)
    private void uploadTiming() {
        super.uploader.uploadOnSchedule();
    }
}