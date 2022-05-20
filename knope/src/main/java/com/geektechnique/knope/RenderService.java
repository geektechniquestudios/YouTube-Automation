package com.geektechnique.knope;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import static com.geektechnique.knope.ColabOrLocal.COLAB;


@Slf4j
@Service
public class RenderService {
    final ChannelManager channelManager;
    final RedisConfig redis;

    public RenderService(ChannelManager channelManager, RedisConfig redisConfig) {
        this.channelManager = channelManager;
        this.redis = redisConfig;
    }

    @Scheduled(cron = "0 */12 * * ?") //every 12 hours
    public String colabRender() {
        if (!Boolean.parseBoolean(redis.get("should-colab-render"))) {
            log.info("Colab renders are set to disabled right now");
            return "Colab renders are set to disabled right now";
        }
        log.info("Running scheduled colab renders");
        List<String> colabList = channelManager.getListByColabOrLocal(COLAB, channelManager.getDisabledChannels("colab"));
        colabList.parallelStream().forEach(this::runProcessWithOutput);
        return "Rendering started";
    }

    private void runProcessWithOutput(String channel) {
        try {
            log.info("Running render for {}" + channel);
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "/usr/bin/python3",
                    "/home/pi/TripCandyVideoGen/colab-runner.py",
                    channel
            );
            processBuilder.redirectErrorStream(true);
            final Process process = processBuilder.start();
            final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            process.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) builder.append(line);

            String result = builder.toString();
            log.info(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
