package com.geektechnique.viduploader.service;

import com.geektechnique.viduploader.config.RedisConfig;
import com.geektechnique.viduploader.model.BaseConfigModel;
import com.geektechnique.viduploader.model.MetadataModel;
import com.geektechnique.viduploader.model.UploadResponseModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.util.stream.Collectors.toList;


public interface Uploadable {
    Logger log = LoggerFactory.getLogger(Uploadable.class);
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    Lock writeLock = rwLock.writeLock();

    void uploadOnSchedule();

    void addVideoToQueue(MetadataModel metadataModel);

    String skipVideo();

    default List<MetadataModel> enumerateVidQueueInterface(RedisConfig redisConfig, String category) {

        if (redisConfig.getSyncCommands().exists(category + "-videoUploadQueue") == 1L) { //@todo decouple this
            log.info("Fetching upload jobs for " + category);

            Type collectionType = new TypeToken<Collection<MetadataModel>>() {}.getType();
            return Collections.synchronizedList(
                    new ArrayList<>(
                            gson.fromJson(
                                    redisConfig.getSyncCommands().get(category + "-videoUploadQueue"),
                                    collectionType
                            )
                    )
            );
        } else {
            return Collections.synchronizedList(
                    new ArrayList<>()
            );
        }
    }

    default List<MetadataModel> addVideoToQueueInterface(
            List<MetadataModel> videoUploadQueue,
            MetadataModel vidToAdd,
            RedisConfig redisConfig,
            String category
    ) {
        writeLock.lock(); //@todo remove locks
        videoUploadQueue.add(vidToAdd);
        backupVideoQueue(redisConfig, videoUploadQueue, category);
        writeLock.unlock();
        return videoUploadQueue;
    }

    default UploadResponseModel uploadNextVidInterface(
            List<MetadataModel> videoUploadQueue,
            RedisConfig redisConfig,
            String category,
            String clientSecrets,
            String credentialsFile,
            int vidToUploadNum,
            BaseConfigModel baseConfigModel
    ) {

        //determine vidToUpload index from vidNumber
        //if (videoUploadQueue.indexOf() == -1) throw new Error(); //could be useful if I pass in entire object instead of number, def more efficient
        UploadResponseModel uploadResponseModel = new UploadResponseModel();

        if (!videoUploadQueue.isEmpty()) {

//            MetadataModel metadataModel =
//                    (vidToUploadNum == -1
//                            ? videoUploadQueue.get(0)
//                            : videoUploadQueue.stream()
//                                    .filter(vid -> Integer.parseInt(vid.getVidNumber()) == vidToUploadNum)
//                                    .collect(toList()).get(0));

            int videoIndex =
                    videoUploadQueue.parallelStream()
                            .filter(vid -> Integer.parseInt(vid.getVidNumber()) == vidToUploadNum)
                            .mapToInt(videoUploadQueue::indexOf)
                            .findAny()
                            .orElse(0);


            MetadataModel metadataModel = videoUploadQueue.get(videoIndex);
//            MetadataModel metadataModel;
//
//            if (vidToUploadNum == -1) metadataModel = videoUploadQueue.get(0);//for timed upload
//            else metadataModel = videoUploadQueue.stream()
//                    .filter(vid -> Integer.parseInt(vid.getVidNumber()) == vidToUploadNum)
//                    .collect(toList()).get(0);

//            if (metadataModel == null) throw new Error("MetaDataModel is null");

            uploadResponseModel = callApi(
                    metadataModel,
                    redisConfig,
                    videoUploadQueue,
                    category,
                    clientSecrets,
                    credentialsFile,
                    baseConfigModel
            );

            syncVidQueue(
                    uploadResponseModel.isError(),
                    redisConfig,
                    metadataModel,
                    videoUploadQueue,
                    videoIndex,
                    category
            );

        } else {
            String response = "No " + category + " videos to upload currently.";
            log.info(response);
            uploadResponseModel.setMessage(response);
            uploadResponseModel.setError(false);
        }
        return uploadResponseModel;
    }

    default UploadResponseModel callApi(
            MetadataModel metadataModel,
            RedisConfig redisConfig,
            List<MetadataModel> videoUploadQueue,
            String category,
            String clientSecrets,
            String credentialsFile,
            BaseConfigModel baseConfigModel
    ) {
        if (metadataModel.getTitle().length() > 100) {
            String message = category + " video " + metadataModel.getVidNumber() + ".mp4 failed because the title is too long. Should be < 100 characters";
            log.error(message);

            redisConfig.getSyncCommands().hset(metadataModel.getVidNumber(), "failure", "1");
            redisConfig.getSyncCommands().lpush("failList", metadataModel.getVidNumber());
            return new UploadResponseModel(true, message);
        }

        try {
            log.info("Video {} is uploading, {} are in queue", metadataModel.getVidNumber(), videoUploadQueue.size());

            ProcessBuilder processBuilder = constructProcessBuilder(
                    clientSecrets,
                    credentialsFile,
                    metadataModel,
                    baseConfigModel
            );

            //processbuilder.command.{arrayListCommands}

            processBuilder.redirectErrorStream(true);
            final Process process = processBuilder.start();
            final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            process.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) builder.append(line);

            String result = builder.toString();
            log.info(result);

            boolean isUploadResponseError = (
                    result.matches(".*\\berror\\b.*") ||
                            result.matches(".*\\bexception\\b.*")
            );
            //@todo need to improve error handling, consider deserialization, bc this is disgusting

            return new UploadResponseModel(
                    isUploadResponseError,
                    result
            );
        } catch (Exception e) {
            e.printStackTrace();
            return new UploadResponseModel(true, e.toString());
        }

    }

    default ProcessBuilder constructProcessBuilder(
            String clientSecrets,
            String credentialsFile,
            MetadataModel metadataModel,
            BaseConfigModel baseConfigModel
    ) {//tb private

        ProcessBuilder processBuilder = new ProcessBuilder(
                "youtube-upload",
                "--title=" + metadataModel.getTitle(),
                "--category=" + metadataModel.getCategory(),
                "--description=" + metadataModel.getDescription(),
                "--tags=" + metadataModel.getKeywords(),
                "--privacy=" + metadataModel.getPrivacyStatus(),
                "--playlist=" + metadataModel.getPlaylist(),
                "--thumbnail=" + metadataModel.getThumbnail(),
                "--client-secrets=" + clientSecrets,
                "--credentials-file=" + credentialsFile,
                "/mnt/" + baseConfigModel.getChannel() + "-vids-to-upload/" + metadataModel.getVidNumber() + ".mp4"
        );

        //"/mnt/vids-to-upload/"

        if (metadataModel.getThumbnail().equals(" ")) processBuilder.command().remove(7);
        if (metadataModel.getPlaylist().equals(" ")) processBuilder.command().remove(6);

        return processBuilder;
    }

    default void syncVidQueue(
            boolean e,
            RedisConfig redisConfig,
            MetadataModel metadataModel,
            List<MetadataModel> videoUploadQueue,
            int videoIndex,
            String category
    ) {
        if (e) {
            log.error("Error uploading {}", metadataModel.getVidNumber() + ".mp4");
            redisConfig.getSyncCommands().hset(metadataModel.getVidNumber(), "failure", "1");
            redisConfig.getSyncCommands().lpush("failList", metadataModel.getVidNumber());
        } else {
            log.info("Video {} uploaded", metadataModel.getVidNumber() + ".mp4");
            redisConfig.getSyncCommands().hset(metadataModel.getVidNumber(), "uploaded", "1");

            writeLock.lock();
            try {
                videoUploadQueue.remove(videoIndex); //change index to what's passed in @todo
                backupVideoQueue(redisConfig, videoUploadQueue, category);
                redisConfig.getSyncCommands().hset(
                        String.valueOf(videoUploadQueue.get(0).getVidNumber()),
                        "uploaded",
                        "1");
            } catch (Exception ex) {ex.printStackTrace();};
            writeLock.unlock();

            BackupService.backupDatabase();
            //deleteVidAfterUpload();
        }
    }

    default String skipVideoInterface(RedisConfig redisConfig, List<MetadataModel> videoUploadQueue, String category) { //@todo could break, hondle ex
        writeLock.lock();
        videoUploadQueue.remove(0);
        backupVideoQueue(redisConfig, videoUploadQueue, category);
        writeLock.unlock();

        return "Video " + videoUploadQueue.get(0).getVidNumber() + " skipped.";
    }

    default void backupVideoQueue(RedisConfig redisConfig, List<MetadataModel> videoUploadQueue, String category) {
        redisConfig.getSyncCommands().set(category + "-videoUploadQueue", gson.toJson(videoUploadQueue));
    }

//    default void delay(
//            List<MetadataModel> videoUploadQueue,
//            String category
//    ) {
//        try {
//            TimeUnit.MINUTES.sleep(
//                    AntiSyncDelay.getNumberOfMinutes(
//                            videoUploadQueue.get(0).getVidNumber(),
//                            category
//                    )
//            );
//            log.info("delay complete");
//        } catch (Exception e) {
//            e.printStackTrace();
//            log.error("AntiSyncDelay failure");
//        }
//    }

    UploadResponseModel uploadNextVid(int vidToUpload);
}
