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
        value = "unstoppable-force.enabled",
        havingValue = "true"
)
@RestController
@RequestMapping("/unstoppable-force")
public class UnstoppableForce extends BaseVideoController {
    public UnstoppableForce() {
        super(
                new Uploader(
                        new BaseConfigModel(
                                new RedisConfig(0),
                                "unstoppable-force",
                                "/home/pi/vid-upload/unstoppable-force/.client_secret.json",
                                "/home/pi/vid-upload/unstoppable-force/.youtube-upload-credentials.json"
                        )
                )
        );
    }

    @Scheduled(cron = cronTiming)
    private void uploadTiming() {
        super.uploader.uploadOnSchedule();
    }
}