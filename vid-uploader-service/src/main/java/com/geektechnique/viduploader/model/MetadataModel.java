package com.geektechnique.viduploader.model;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class MetadataModel {
    private final String vidNumber;
    private final String category;//@todo change this - should be distinct from upload queue category
    private final String title;
    private final String description;
    private final String keywords;
    private final String privacyStatus;
    private final String playlist;
    private final String thumbnail;
}