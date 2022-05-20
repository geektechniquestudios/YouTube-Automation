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
        value = "thunderous-attack.enabled",
        havingValue = "true"
)
@RestController
@RequestMapping("/thunderous-attack")
public class ThunderousAttack extends BaseVideoController {
    public ThunderousAttack() {
        super(
                new Uploader(
                        new BaseConfigModel(
                                new RedisConfig(0),
                                "thunderous-attack",
                                "/home/pi/vid-upload/thunderous-attack/.client_secret.json",
                                "/home/pi/vid-upload/thunderous-attack/.youtube-upload-credentials.json"
                        )
                )
        );
    }

    @Scheduled(cron = cronTiming)
    private void uploadTiming() {
        super.uploader.uploadOnSchedule();
    }
}