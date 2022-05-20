package com.geektechnique.viduploader.controller;

import com.geektechnique.viduploader.config.RedisConfig;
import com.geektechnique.viduploader.model.reactmodels.ReactTitleResponse;
import com.geektechnique.viduploader.model.reactmodels.ReactResponse;
import com.geektechnique.viduploader.model.ChannelList;
import com.geektechnique.viduploader.service.Uploader;
import com.geektechnique.viduploader.model.statusmodels.StatusModel;
import com.geektechnique.viduploader.model.MetadataModel;

import java.util.List;


public class BaseVideoController implements VideoControllerInterface {
    protected Uploader uploader;
    private final RedisConfig redisConfig;

    //post construct, add 

    public BaseVideoController(Uploader uploader) {
        this.redisConfig = uploader.getBaseConfigModel().getRedisConfig();
        this.uploader = uploader;

        ChannelList.addChannel(uploader.getBaseConfigModel().getChannel());
    }

    @Override
    public String intakeVideo(MetadataModel metadataModel) {
        return intakeVideoInterface(
                metadataModel,
                redisConfig,
                uploader
        );
    }

    @Override
    public StatusModel showNumberInQueue(int numToShow) {
        return showNumberInQueueInterface(numToShow, uploader);
    }

    @Override
    public ReactResponse manuallyUploadOne() { //@todo remove
        return manuallyUploadOneInterface(uploader, -1);
    }

    @Override
    public String skipVideo() {
        return uploader.skipVideo();
    }

    @Override
    public List<MetadataModel> reactListVideos() { //@todo need to handle edge case of no videos
        log.info("Listing react videos for {}", uploader.getBaseConfigModel().getChannel());
        return uploader.getVideoUploadQueue().subList(0, Math.min(50, uploader.getVideoUploadQueue().size()));
    }

    @Override
    public ReactTitleResponse reactTitle() {
        return new ReactTitleResponse(
                uploader.getVideoUploadQueue().size(),
                uploader.getTimeOfLastAddedVid() == null ? "None on record" : uploader.getTimeOfLastAddedVid()
        );
    }

    @Override
    public ReactResponse reactUpload(int vidNumber) {
        log.info("Uploading video: {}", vidNumber);
        return manuallyUploadOneInterface(uploader, vidNumber);
    }

    @Override
    public ReactResponse reactDelete(int vidNumber) {

        uploader.getVideoUploadQueue().remove(
                uploader.getVideoUploadQueue().parallelStream()
                        .filter(vid -> Integer.parseInt(vid.getVidNumber()) == vidNumber) //@todo change to find first
                        .findFirst().orElse(null)
        );

        uploader.backupVideoQueue(redisConfig, uploader.getVideoUploadQueue(), uploader.getBaseConfigModel().getChannel());

        log.info("Video {} deleted", vidNumber);
        return new ReactResponse(vidNumber, true, null);
    }

    @Override
    public ReactResponse reactEdit(MetadataModel metadataModel) {

        MetadataModel derivedMDM = uploader.getVideoUploadQueue().parallelStream()
                .filter(vid -> vid.getVidNumber().equals(metadataModel.getVidNumber()))
                .findFirst().orElse(null);

        if (derivedMDM == null) return new ReactResponse(Integer.parseInt(metadataModel.getVidNumber()), false, "Couldn't find video to save edits");

        uploader.getVideoUploadQueue().set(
                uploader.getVideoUploadQueue().indexOf(derivedMDM),
                metadataModel
        );

        uploader.backupVideoQueue(redisConfig, uploader.getVideoUploadQueue(), metadataModel.getCategory());

        log.info("Video {} edited", metadataModel.getVidNumber());
        return new ReactResponse(Integer.parseInt(metadataModel.getVidNumber()), true, null);
    }

    @Override
    public int numberInQueue() {
        log.info("Fetching num in queue for {}", uploader.getBaseConfigModel().getChannel());
        return uploader.getVideoUploadQueue().size();
    }
}