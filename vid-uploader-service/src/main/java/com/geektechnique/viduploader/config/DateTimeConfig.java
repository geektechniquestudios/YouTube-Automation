package com.geektechnique.viduploader.config;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Getter
@Configuration
public class DateTimeConfig {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
    ZoneId zoneId = ZoneId.of("America/New_York");
}
