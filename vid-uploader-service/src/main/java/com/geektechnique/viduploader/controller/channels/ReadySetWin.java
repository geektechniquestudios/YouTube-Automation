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
        value = "ready-set-win.enabled",
        havingValue = "true"
)
@RestController
@RequestMapping("/ready-set-win")
public class ReadySetWin extends BaseVideoController {
    public ReadySetWin() {
        super(
                new Uploader(
                        new BaseConfigModel(
                                new RedisConfig(0),
                                "ready-set-win",
                                "/home/pi/vid-upload/ready-set-win/.client_secret.json",
                                "/home/pi/vid-upload/ready-set-win/.youtube-upload-credentials.json"
                        )
                )
        );
    }

    @Scheduled(cron = cronTiming)
    private void uploadTiming() {
        super.uploader.uploadOnSchedule();
    }
}