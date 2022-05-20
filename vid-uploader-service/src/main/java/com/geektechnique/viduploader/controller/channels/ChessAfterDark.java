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
        value = "chess-after-dark.enabled",
        havingValue = "true"
)
@RestController
@RequestMapping("/chess-after-dark")
public class ChessAfterDark extends BaseVideoController {
    public ChessAfterDark() {
        super(
                new Uploader(
                        new BaseConfigModel(
                                new RedisConfig(0),
                                "chess-after-dark",
                                "/home/pi/vid-upload/chess-after-dark/.client_secret.json",
                                "/home/pi/vid-upload/chess-after-dark/.youtube-upload-credentials.json"
                        )
                )
        );
    }

    @Scheduled(cron = cronTiming)
    private void uploadTiming() {
        super.uploader.uploadOnSchedule();
    }
}