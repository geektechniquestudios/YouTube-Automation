package com.geektechnique.notificationserivce;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PollingService {

    RestTemplate restTemplate = new RestTemplate();
    ArrayList<LocalService> localServices = new ArrayList<>();

    final String piUrl = "http://10.0.0.20/";

    final TwilioService twilioService;
    final RedisConfig redisConfig;


    public PollingService(TwilioService twilioService, RedisConfig redisConfig) {
        this.twilioService = twilioService;
        this.redisConfig = redisConfig;

        localServices.add(new LocalService("uploader", piUrl + ":8080/notfier"));
        localServices.add(new LocalService("react", piUrl + ":5000"));// maybe I'll add / notifier, but I'm just checking status code so ...
        localServices.add(new LocalService("knope", piUrl + "8082/notifier"));
    }

    @Scheduled(cron = "0 30 10 * * ?")
    private void ensureVideoNumbers() {
        List<?> channels = restTemplate.getForEntity(piUrl, List.class).getBody();
        if (channels != null) {
            channels.parallelStream().forEach(channel -> {
                        Integer numberInQueue = restTemplate.getForObject(piUrl + channel + "/number_in_queue", Integer.class);
                        if (numberInQueue == null) return;
                        //checkIfNotificationNeeded() bleh, should imply that it changes values if ...
                        if (numberInQueue < 100){
                            if (redisConfig.hget(String.valueOf(channel), "has-been-over-100").equals("true"))
                                twilioService.sendMessage(channel + " has less than 100 videos in queue!");
                        } else redisConfig.hset(String.valueOf(channel),
                                "has-been-over-100",
                                "true");
                    }
            );
        }
    }

    @Scheduled(cron = "0 15 10 1 * ?") //1st of every month at 10:15am
    @Scheduled(cron = "0 15 10 15 * ?") //15th of every month at 10:15am
    private void keepNumberAlive() {
        twilioService.sendMessage("scheduled ping");
    }

    @Scheduled(cron = "0 0 */2 * * *") //every 2 hours
    private void poll() {
        checkMicroservices();
        checkRedis();
    }

    private void checkMicroservices() {
        localServices.parallelStream().forEach(service -> {
                    if (!restTemplate.getForEntity(service.getUrl(), String.class).getStatusCode().equals(HttpStatus.OK)) {
                        // @TODO also check for if message has been sent within the past day to not spam
                        twilioService.sendMessage(service + " is offline");
                    }
                }
        );
    }

    private void checkRedis() {
        try {
            if (!redisConfig.ping().equals("PONG")) {
                twilioService.sendMessage("Redis ping failed");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            twilioService.sendMessage("Redis ping failed");
        }
    }


}
