package com.geektechnique.viduploader.model;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;


@Slf4j
@Repository
public class ChannelList {
    @Getter
    private static ArrayList<String> channels = new ArrayList<>(); //Don't make final
    public static void addChannel(String channelName){
        channels.add(channelName);
    }

    // after initialization alphabetize channel list
    @EventListener(ContextRefreshedEvent.class)
    public void contextRefreshedEvent() {
        channels.sort(String::compareTo);
        log.info("Sorting channel list {}", channels);
    }
}
