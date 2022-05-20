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
        value = "forest-for-the-trees.enabled",
        havingValue = "true"
)
@RestController
@RequestMapping("/forest-for-the-trees")
public class ForestForTheTrees extends BaseVideoController {
    public ForestForTheTrees() {
        super(
                new Uploader(
                        new BaseConfigModel(
                                new RedisConfig(0),
                                "forest-for-the-trees",
                                "/home/pi/vid-upload/forest-for-the-trees/.client_secret.json",
                                "/home/pi/vid-upload/forest-for-the-trees/.youtube-upload-credentials.json"
                        )
                )
        );
    }

    @Scheduled(cron = cronTiming)
    private void uploadTiming() {
        super.uploader.uploadOnSchedule();
    }
}