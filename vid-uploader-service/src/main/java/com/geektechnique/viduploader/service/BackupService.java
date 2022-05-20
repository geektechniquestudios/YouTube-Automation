package com.geektechnique.viduploader.service;

import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;


@Service
@Slf4j
public class BackupService {

    @Synchronized
    public static void backupDatabase() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String currentTime = formatter.format(now);

        CompletableFuture.runAsync(() -> {
            log.info("Creating backup 1: {}", currentTime);
            ProcessBuilder backupProcess1 = new ProcessBuilder(
                    "sudo",
                    "cp",
                    "/var/lib/redis/pgn-etl-dump.rdb",
                    "/ace/everything-else/Terry/redis-backup/pgn-etl-dump.rdb"
            );

            try {
                backupProcess1.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        CompletableFuture.runAsync(() -> {
            log.info("Creating backup 2: {}", currentTime);
            ProcessBuilder backupProcess2 = new ProcessBuilder(
                    "sudo",
                    "cp",
                    "/var/lib/redis/pgn-etl-dump.rdb",
                    "/mnt/redis-data/pgn-etl-dump.rdb"
            );

            try {
                backupProcess2.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
