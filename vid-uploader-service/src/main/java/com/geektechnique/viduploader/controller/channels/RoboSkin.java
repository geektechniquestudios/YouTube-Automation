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
        value = "robo-skin.enabled",
        havingValue = "true"
)
@RestController
@RequestMapping("/robo-skin")
public class RoboSkin extends BaseVideoController {
    public RoboSkin() {
        super(
                new Uploader(
                        new BaseConfigModel(
                                new RedisConfig(0),
                                "robo-skin",
                                "/home/pi/vid-upload/robo-skin/.client_secret.json",
                                "/home/pi/vid-upload/robo-skin/.youtube-upload-credentials.json"
                        )
                )
        );
    }

    @Scheduled(cron = cronTiming)
    private void uploadTiming() {
        super.uploader.uploadOnSchedule();
    }
}