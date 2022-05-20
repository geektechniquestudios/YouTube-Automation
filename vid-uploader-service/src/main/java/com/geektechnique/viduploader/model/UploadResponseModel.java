package com.geektechnique.viduploader.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadResponseModel {
    private boolean error;
    private String message;
}