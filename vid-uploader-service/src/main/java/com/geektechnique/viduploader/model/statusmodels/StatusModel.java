package com.geektechnique.viduploader.model.statusmodels;

import com.geektechnique.viduploader.model.MetadataModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;


@AllArgsConstructor
@Getter
public class StatusModel {
    private final String message;
    private final int numberInQueue;
    private final String currentVideo;
    private final List<MetadataModel> videoUploadQueue;
}