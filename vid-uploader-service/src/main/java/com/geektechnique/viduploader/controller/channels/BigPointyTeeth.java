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
        value = "big-pointy-teeth.enabled",
        havingValue = "true"
)
@RestController
@RequestMapping("/big-pointy-teeth")
public class BigPointyTeeth extends BaseVideoController {
    public BigPointyTeeth() {
        super(
                new Uploader(
                        new BaseConfigModel(
                                new RedisConfig(0),
                                "big-pointy-teeth",
                                "/home/pi/vid-upload/big-pointy-teeth/.client_secret.json",
                                "/home/pi/vid-upload/big-pointy-teeth/.youtube-upload-credentials.json"
                        )
                )
        );
    }

    @Scheduled(cron = cronTiming)
    private void uploadTiming() {
        super.uploader.uploadOnSchedule();
    }
}