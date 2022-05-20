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
        value = "renju-flix.enabled",
        havingValue = "true"
)
@RestController
@RequestMapping("/renju-flix")
public class RenjuFlix extends BaseVideoController {
    public RenjuFlix() {
        super(
                new Uploader(
                        new BaseConfigModel(
                                new RedisConfig(0),
                                "renju-flix",
                                "/home/pi/vid-upload/renju-flix/.client_secret.json",
                                "/home/pi/vid-upload/renju-flix/.youtube-upload-credentials.json"
                        )
                )
        );
    }

    @Scheduled(cron = cronTiming)
    private void uploadTiming() {
        super.uploader.uploadOnSchedule();
    }
}