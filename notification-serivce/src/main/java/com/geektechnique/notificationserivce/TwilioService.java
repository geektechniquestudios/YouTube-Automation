package com.geektechnique.notificationserivce;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.stereotype.Service;

@Service
public class TwilioService {

    //Must use this once a month or number will be removed

    public TwilioService() {
        String AUTH_TOKEN = "";
        String ACCOUNT_SID = "";
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    public void sendMessage(String message){
        Message.creator(
                new PhoneNumber(""),
                new PhoneNumber(""),
                message
        ).create();
    }

}

