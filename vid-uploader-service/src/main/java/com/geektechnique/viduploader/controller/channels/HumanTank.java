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
        value = "human-tank.enabled",
        havingValue = "true"
)
@RestController
@RequestMapping("/human-tank")
public class HumanTank extends BaseVideoController {
    public HumanTank() {
        super(
                new Uploader(
                        new BaseConfigModel(
                                new RedisConfig(0),
                                "human-tank",
                                "/home/pi/vid-upload/human-tank/.client_secret.json",
                                "/home/pi/vid-upload/human-tank/.youtube-upload-credentials.json"
                        )
                )
        );
    }

    @Scheduled(cron = cronTiming)
    private void uploadTiming() {
        super.uploader.uploadOnSchedule();
    }
}