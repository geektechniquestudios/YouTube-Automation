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
        value = "surrender-or-die.enabled",
        havingValue = "true"
)
@RestController
@RequestMapping("/surrender-or-die")
public class SurrenderOrDie extends BaseVideoController {
    public SurrenderOrDie() {
        super(
                new Uploader(
                        new BaseConfigModel(
                                new RedisConfig(0),
                                "surrender-or-die",
                                "/home/pi/vid-upload/surrender-or-die/.client_secret.json",
                                "/home/pi/vid-upload/surrender-or-die/.youtube-upload-credentials.json"
                        )
                )
        );
    }

    @Scheduled(cron = cronTiming)
    private void uploadTiming() {
        super.uploader.uploadOnSchedule();
    }
}