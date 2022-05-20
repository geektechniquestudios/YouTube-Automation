package com.geektechnique.notificationserivce;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class NotifierController {

    final TwilioService twilioService;

    public NotifierController(TwilioService twilioService) {
        this.twilioService = twilioService;
    }

    @CrossOrigin
    @PostMapping("/send-text")
    public void sendText(@RequestBody String messageToSend){
        twilioService.sendMessage(messageToSend);
        log.info("Sending message: {}", messageToSend);
    }

}
