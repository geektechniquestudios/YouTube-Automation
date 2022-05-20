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
        value = "tofu-go-moku.enabled",
        havingValue = "true"
)
@RestController
@RequestMapping("/tofu-go-moku")
public class TofuGoMoku extends BaseVideoController {
    public TofuGoMoku() {
        super(
                new Uploader(
                        new BaseConfigModel(
                                new RedisConfig(0),
                                "tofu-go-moku",
                                "/home/pi/vid-upload/tofu-go-moku/.client_secret.json",
                                "/home/pi/vid-upload/tofu-go-moku/.youtube-upload-credentials.json"
                        )
                )
        );
    }

    @Scheduled(cron = cronTiming)
    private void uploadTiming() {
        super.uploader.uploadOnSchedule();
    }
}