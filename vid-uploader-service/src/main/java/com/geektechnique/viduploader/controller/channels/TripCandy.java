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
        value = "trip-candy.enabled",
        havingValue = "true"
)
@RestController
@RequestMapping("/trip-candy")
public class TripCandy extends BaseVideoController {
    public TripCandy() {
        super(
                new Uploader(
                        new BaseConfigModel(
                                new RedisConfig(0),
                                "trip-candy",
                                "/home/pi/vid-upload/trip-candy/.client_secret.json",
                                "/home/pi/vid-upload/trip-candy/.youtube-upload-credentials.json"
                        )
                )
        );
    }

    @Scheduled(cron = "0 15 10 * * ?") // 10:15 am every day
    private void uploadTiming() {
        super.uploader.uploadOnSchedule();
    }
}