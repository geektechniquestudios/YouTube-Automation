package com.geektechnique.viduploader.controller;

import com.geektechnique.viduploader.config.RedisConfig;
import com.geektechnique.viduploader.model.reactmodels.ReactTitleResponse;
import com.geektechnique.viduploader.model.UploadResponseModel;
import com.geektechnique.viduploader.model.reactmodels.ReactResponse;
import com.geektechnique.viduploader.model.statusmodels.NoVidsStatusModel;
import com.geektechnique.viduploader.model.statusmodels.StatusModel;
import com.geektechnique.viduploader.model.MetadataModel;
import com.geektechnique.viduploader.service.Uploader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;


public interface VideoControllerInterface {
    Logger log = LoggerFactory.getLogger(VideoControllerInterface.class);
    String cronTiming = "0 1 0-20/4 * * *";

    @ResponseBody
    @PostMapping(value = "/add_video", consumes = MediaType.APPLICATION_JSON_VALUE)
    String intakeVideo(@RequestBody MetadataModel metadataModel);

    @ResponseBody
    @GetMapping("/number_in_queue/{numToShow}")
    StatusModel showNumberInQueue(@PathVariable int numToShow);

    @ResponseBody
    @PostMapping("/manual_upload/")
    ReactResponse manuallyUploadOne();

    @ResponseBody
    @GetMapping("/skip_video")
    String skipVideo();

    @CrossOrigin
    @ResponseBody
    @GetMapping("/react/videos")
    List<MetadataModel> reactListVideos();

    @CrossOrigin
    @ResponseBody
    @GetMapping("/react/title")
    ReactTitleResponse reactTitle();

    @CrossOrigin
    @ResponseBody
    @PostMapping("/react/upload/{vidNumber}")
    ReactResponse reactUpload(@PathVariable int vidNumber);

    @CrossOrigin
    @ResponseBody
    @DeleteMapping("/react/delete/{vidNumber}")
    ReactResponse reactDelete(@PathVariable int vidNumber);

    @CrossOrigin
    @ResponseBody
    @PostMapping("/react/edit")
    ReactResponse reactEdit(@RequestBody MetadataModel metadataModel);

    @CrossOrigin
    @ResponseBody
    @GetMapping("/number_in_queue")
    int numberInQueue();


    default String intakeVideoInterface(MetadataModel metadataModel, RedisConfig redisConfig, Uploader uploader) {
        redisConfig.getAsyncCommands().hset(metadataModel.getVidNumber(), "sent", "1");
        uploader.addVideoToQueue(metadataModel);
        String response = "video " + metadataModel.getVidNumber() + ".mp4 added to " + uploader.getBaseConfigModel().getChannel() + " queue";
        log.info(response);
        return response;

    }

    default StatusModel showNumberInQueueInterface(int numToShow, Uploader uploader) {
        log.info("Retrieving queue for " + uploader.getBaseConfigModel().getChannel());
        return formResponse(uploader, numToShow, null);
    }

    default ReactResponse manuallyUploadOneInterface(Uploader uploader, int vidToUploadNum) {
        log.info("Attempting manual upload on " + uploader.getBaseConfigModel().getChannel());

        UploadResponseModel uploadResponseModel = uploader.uploadNextVid(vidToUploadNum);

        return new ReactResponse(
                vidToUploadNum,
                !uploadResponseModel.isError(),
                uploadResponseModel.getMessage()
        );
    }

    default StatusModel formResponse(
            Uploader uploader,
            int numToShow,
            String message
    ) {
        if (uploader.getVideoUploadQueue().size() > 0) {
            if (numToShow > uploader.getVideoUploadQueue().size()) {
                numToShow = uploader.getVideoUploadQueue().size();
            }

            return new StatusModel(
                    message,
                    uploader.getVideoUploadQueue().size(),
                    uploader.getVideoUploadQueue().get(0).getVidNumber(),
                    uploader.getVideoUploadQueue().subList(0, numToShow)
            );
        } else {
            return new NoVidsStatusModel();
        }
    }
}
