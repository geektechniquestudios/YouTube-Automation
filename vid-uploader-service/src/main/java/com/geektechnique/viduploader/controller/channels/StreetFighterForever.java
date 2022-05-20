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
        value = "street-fighter-forever.enabled",
        havingValue = "true"
)
@RestController
@RequestMapping("/street-fighter-forever")
public class StreetFighterForever extends BaseVideoController {
    public StreetFighterForever() {
        super(
                new Uploader(
                        new BaseConfigModel(
                                new RedisConfig(0),
                                "street-fighter-forever",
                                "/home/pi/vid-upload/street-fighter-forever/.client_secret.json",
                                "/home/pi/vid-upload/street-fighter-forever/.youtube-upload-credentials.json"
                        )
                )
        );
    }

    @Scheduled(cron = cronTiming)
    private void uploadTiming() {
        super.uploader.uploadOnSchedule();
    }
}