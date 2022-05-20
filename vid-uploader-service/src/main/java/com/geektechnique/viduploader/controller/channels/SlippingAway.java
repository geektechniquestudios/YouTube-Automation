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
        value = "slipping-away.enabled",
        havingValue = "true"
)
@RestController
@RequestMapping("/slipping-away")
public class SlippingAway extends BaseVideoController {
    public SlippingAway() {
        super(
                new Uploader(
                        new BaseConfigModel(
                                new RedisConfig(0),
                                "slipping-away",
                                "/home/pi/vid-upload/slipping-away/.client_secret.json",
                                "/home/pi/vid-upload/slipping-away/.youtube-upload-credentials.json"
                        )
                )
        );
    }

    @Scheduled(cron = cronTiming)
    private void uploadTiming() {
        super.uploader.uploadOnSchedule();
    }
}