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
        value = "alien-h2o.enabled",
        havingValue = "true"
)
@RestController
@RequestMapping("/alien-h2o")
public class AlienH2o extends BaseVideoController {
    public AlienH2o() {
        super(
                new Uploader(
                        new BaseConfigModel(
                                new RedisConfig(0),
                                "alien-h2o",
                                "/home/pi/vid-upload/alien-h2o/.client_secret.json",
                                "/home/pi/vid-upload/alien-h2o/.youtube-upload-credentials.json"
                        )
                )
        );
    }

    @Scheduled(cron = cronTiming)
    private void uploadTiming() {
        super.uploader.uploadOnSchedule();
    }
}