package com.geektechnique.viduploader.model.statusmodels;

import java.util.ArrayList;


public class NoVidsStatusModel extends StatusModel {
    public NoVidsStatusModel() {
        super(
                "There are no videos in queue",
                0,
                "",
                new ArrayList<>()
        );
    }
}