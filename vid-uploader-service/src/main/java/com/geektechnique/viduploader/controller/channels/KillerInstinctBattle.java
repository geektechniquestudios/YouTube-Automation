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
        value = "killer-instinct-battle.enabled",
        havingValue = "true"
)
@RestController
@RequestMapping("/killer-instinct-battle")
public class KillerInstinctBattle extends BaseVideoController {
    public KillerInstinctBattle() {
        super(
                new Uploader(
                        new BaseConfigModel(
                                new RedisConfig(0),
                                "killer-instinct-battle",
                                "/home/pi/vid-upload/killer-instinct-battle/.client_secret.json",
                                "/home/pi/vid-upload/killer-instinct-battle/.youtube-upload-credentials.json"
                        )
                )
        );
    }

    @Scheduled(cron = cronTiming)
    private void uploadTiming() {
        super.uploader.uploadOnSchedule();
    }
}