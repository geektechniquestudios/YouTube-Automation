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
        value = "xiangqi-flix.enabled",
        havingValue = "true"
)
@RestController
@RequestMapping("/xiangqi-flix")
public class XiangqiFlix extends BaseVideoController {
    public XiangqiFlix() {
        super(
                new Uploader(
                        new BaseConfigModel(
                                new RedisConfig(0),
                                "xiangqi-flix",
                                "/home/pi/vid-upload/xiangqi-flix/.client_secret.json",
                                "/home/pi/vid-upload/xiangqi-flix/.youtube-upload-credentials.json"
                        )
                )
        );
    }

    @Scheduled(cron = cronTiming)
    private void uploadTiming() {
        super.uploader.uploadOnSchedule();
    }
}