package com.geektechnique.viduploader.model.reactmodels;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReactResponse {
    private int vidNumber;
    private boolean operationSuccess;
    private String message;
}
