package com.geektechnique.viduploader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class VidUploaderApplication {
    public static void main(String[] args) {
        SpringApplication.run(VidUploaderApplication.class, args);
    }
}
