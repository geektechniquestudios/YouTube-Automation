package com.geektechnique.viduploader.controller;

import com.geektechnique.viduploader.config.RedisConfig;
import com.geektechnique.viduploader.model.CurrentChannel;
import com.geektechnique.viduploader.model.ChannelList;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/react")
public class ReactController {

    RedisConfig redisConfig = new RedisConfig(0);

    @CrossOrigin
    @ResponseBody
    @GetMapping("/get_channel_list")
    public List<String> reactGetChannelList(){
        return ChannelList.getChannels();
    }

    @CrossOrigin
    @ResponseBody
    @GetMapping("/get_most_recent_channel")
    public CurrentChannel reactGetMostRecentChannel() {
        return new CurrentChannel(redisConfig.getSyncCommands().get("mostRecentChannel"));
    }

    @CrossOrigin
    @ResponseBody
    @PostMapping("/set_most_recent_channel/{mostRecentChannel}")
    public CurrentChannel reactSetMostRecentChannel(@PathVariable String mostRecentChannel) {
        return new CurrentChannel(redisConfig.getSyncCommands().set("mostRecentChannel", mostRecentChannel));
    }

    @CrossOrigin
    @ResponseBody
    @GetMapping("/notifier")
    public String notificationEndpoint(){
        return "";
    }

}
