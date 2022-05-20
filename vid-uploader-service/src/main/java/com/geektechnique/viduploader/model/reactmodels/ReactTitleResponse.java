package com.geektechnique.viduploader.model.reactmodels;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class ReactTitleResponse {
    private final int numInQueue;
    private final String timeOfLastAddedVid;
}
