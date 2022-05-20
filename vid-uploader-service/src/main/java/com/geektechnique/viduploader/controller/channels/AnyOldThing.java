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
        value = "any-old-thing.enabled",
        havingValue = "true"
)
@RestController
@RequestMapping("/any-old-thing")
public class AnyOldThing extends BaseVideoController {
    public AnyOldThing() {
        super(
                new Uploader(
                        new BaseConfigModel(
                                new RedisConfig(0),
                                "any-old-thing",
                                "/home/pi/vid-upload/any-old-thing/.client_secret.json",
                                "/home/pi/vid-upload/any-old-thing/.youtube-upload-credentials.json"
                        )
                )
        );
    }

    @Scheduled(cron = cronTiming)
    private void uploadTiming() {
        super.uploader.uploadOnSchedule();
    }
}