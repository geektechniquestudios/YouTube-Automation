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
        value = "cacti-armor.enabled",
        havingValue = "true"
)
@RestController
@RequestMapping("/cacti-armor")
public class CactiArmor extends BaseVideoController {
    public CactiArmor() {
        super(
                new Uploader(
                        new BaseConfigModel(
                                new RedisConfig(0),
                                "cacti-armor",
                                "/home/pi/vid-upload/cacti-armor/.client_secret.json",
                                "/home/pi/vid-upload/cacti-armor/.youtube-upload-credentials.json"
                        )
                )
        );
    }

    @Scheduled(cron = cronTiming)
    private void uploadTiming() {
        super.uploader.uploadOnSchedule();
    }
}