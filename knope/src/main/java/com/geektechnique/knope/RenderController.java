package com.geektechnique.knope;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static com.geektechnique.knope.ColabOrLocal.LOCAL;

import java.util.ArrayList;


@Slf4j
@RestController
public class RenderController {
    final RenderService renderService;
    final RedisConfig redis;

    public RenderController(RenderService renderService, RedisConfig redisConfig) {
        this.renderService = renderService;
        this.redis = redisConfig;
    }

    @CrossOrigin
    @ResponseBody
    @PostMapping("/get-channel-to-render")
    public String getLocalChannelToRender(@RequestBody ArrayList<String> channelsToExclude) {
        log.info("Determining local channel to render");
        return renderService.channelManager.getChannelToRender(LOCAL, channelsToExclude);
    }

    @CrossOrigin
    @ResponseBody
    @GetMapping("/get-channel-to-render")
    public String getLocalChannelToRender() {
        return getLocalChannelToRender(new ArrayList<>());
    }

    @CrossOrigin
    @ResponseBody
    @GetMapping("/enable-colab-render")
    public String enableColabRender() {
        log.info("Colab render will start at 12");
        redis.set("should-colab-render", "true");
        return "Colab service started";
    }

    @CrossOrigin
    @ResponseBody
    @GetMapping("/disable-colab-render")
    public String disableColabRender() {
        log.info("Disabling colab rendering");
        redis.set("should-colab-render", "false");
        return "Disabling colab rendering";
    }

    @CrossOrigin
    @ResponseBody
    @GetMapping("/start-colab-render")
    public String startColabRender() {
        log.info("Off-schedule colab render starting");
        return renderService.colabRender();
    }

    @CrossOrigin
    @ResponseBody
    @GetMapping("/disable/{colabOrLocal}/{channel}")
    public String disableChannel(@PathVariable String colabOrLocal, @PathVariable String channel) {
        if (!(colabOrLocal.equals("colab") || colabOrLocal.equals("local")))
            return "Path variable must be colab or local";
        log.info("Disabling {} renders for {}", colabOrLocal, channel);
        if (renderService.channelManager.checkIfDisabled(channel, colabOrLocal)) return channel + " already disabled";
        renderService.channelManager.disableChannel(channel, colabOrLocal);
        return channel + " now has renders disabled for " + colabOrLocal;
    }

    @CrossOrigin
    @ResponseBody
    @GetMapping("/enable/{colabOrLocal}/{channel}")
    public String enableChannel(@PathVariable String colabOrLocal, @PathVariable String channel) {
        if (!(colabOrLocal.equals("colab") || colabOrLocal.equals("local")))
            return "Path variable must be colab or local";
        log.info("Enabling {} renders for {}", colabOrLocal, channel);
        if (!renderService.channelManager.checkIfDisabled(channel, colabOrLocal)) return channel + " wasn't disabled";
        renderService.channelManager.enableChannel(channel, colabOrLocal);
        return channel + " now has " + colabOrLocal + " renders enabled";
    }

    @CrossOrigin
    @GetMapping("/notifier")
    public String notifier() {
        log.info("Haveford notifer checking in");
        return "";
    }
}
