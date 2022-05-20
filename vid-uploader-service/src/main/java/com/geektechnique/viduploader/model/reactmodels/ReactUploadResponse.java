package com.geektechnique.viduploader.model.reactmodels;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class ReactUploadResponse {
    private int vidNumber;
    private boolean deleteSuccess;
}
