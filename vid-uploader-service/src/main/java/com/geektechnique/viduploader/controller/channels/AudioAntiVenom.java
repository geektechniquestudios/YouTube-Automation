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
        value = "audio-anti-venom.enabled",
        havingValue = "true"
)
@RestController
@RequestMapping("/audio-anti-venom")
public class AudioAntiVenom extends BaseVideoController {
    public AudioAntiVenom() {
        super(
                new Uploader(
                        new BaseConfigModel(
                                new RedisConfig(0),
                                "audio-anti-venom",
                                "/home/pi/vid-upload/audio-anti-venom/.client_secret.json",
                                "/home/pi/vid-upload/audio-anti-venom/.youtube-upload-credentials.json"
                        )
                )
        );
    }

    @Scheduled(cron = cronTiming)
    private void uploadTiming() {
        super.uploader.uploadOnSchedule();
    }
}