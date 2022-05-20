package com.geektechnique.knope;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.lang.reflect.Type;
import java.util.*;

import static com.geektechnique.knope.ColabOrLocal.LOCAL;


@Slf4j
@Service
public class ChannelManager {
    private final RedisConfig redis;
    private final WebClient webClient;
    Gson gson = new GsonBuilder().setPrettyPrinting().create();


    public ChannelManager(RedisConfig redisConfig, WebClient.Builder webClientBuilder) {
        this.redis = redisConfig;
        this.webClient = webClientBuilder.baseUrl("http://10.0.0.20:8080").build();
    }

    private Mono<String> getNumberInQueue(String channelName) {
        log.info("Getting number in queue for {}", channelName);
        return this.webClient.get().uri("/{channelName}/number_in_queue", channelName)
                .retrieve().bodyToMono(String.class);
    }

    private Mono<List> getFullChannelList() {
        log.info("Getting full channel list");
        return this.webClient.get().uri("/react/get_channel_list").retrieve().bodyToMono(List.class);
    }

    public String getChannelToRender(ColabOrLocal colabOrLocal, List<String> channelsToExclude) {
        log.info("Determining which channel has the shortest lifespan for {}", colabOrLocal.toString());
        List<String> channelList = getListByColabOrLocal(colabOrLocal, channelsToExclude);
        return getShortestLifespan(channelList);
    }

    public List<String> getListByColabOrLocal(ColabOrLocal colabOrLocal, List<String> channelsToExclude) {
        log.info("Getting List by ColabOrLocal");
        List<?> channels = getFullChannelList().block();
        return getRelevantList(colabOrLocal, channels, channelsToExclude);
    }

    public List<String> getRelevantList(ColabOrLocal colabOrLocal, List<?> channels, List<String> channelsToExclude) {
        log.info("Splitting list and returning {}", colabOrLocal.toString());
        List<String> relevantList = new ArrayList<>();
        List<String> disabledChannels = getDisabledChannels(colabOrLocal.toString().toLowerCase());
        try {
            channels.parallelStream().forEach(c -> {
                        //some channels will be on both lists, some on neither
                        String channel = String.valueOf(c);
                        boolean isRelevant = Boolean.parseBoolean(
                                redis.hget(
                                        channel,
                                        colabOrLocal == LOCAL ? "isLocal" : "isColab")
                        );
                        if (isRelevant && !channelsToExclude.contains(channel) && !disabledChannels.contains(channel))
                            relevantList.add(channel);
                    }
            );
        } catch (Exception e) {
            log.error("Error splitting channel list {}", e.toString());
        }
        return relevantList;
    }

    private String getShortestLifespan(List<String> channels) {
        log.info("Calculating lifespans");
        Map<String, Integer> channelMap = createChannelMap(channels);
        Map.Entry<String, Integer> shortestLifespan = channelMap.entrySet().parallelStream()
                .filter(p -> channels.contains(p.getKey()))
                .min(Comparator.comparingInt(Map.Entry::getValue)).orElse(null);//.getKey();
        if (shortestLifespan != null) return shortestLifespan.getKey();
        else return "none";
    }

    private Map<String, Integer> createChannelMap(List<?> channels) {
        log.info("Creating channel map");
        Map<String, Integer> channelMap = new HashMap<>();
        try {
            channels.parallelStream().sorted().forEach(c -> {
                        String channel = String.valueOf(c);
                        channelMap.put(
                                channel,
                                getLifespan(channel)
                        );
                    }
            );
        } catch (Exception e) {
            log.error("failed to sort channels");
        }
        log.info("Returning channel map");
        return channelMap;
    }

    private Integer getLifespan(String channel) {
        log.info("Determining lifespan for {}", channel);
        return Integer.parseInt(Objects.requireNonNull(getNumberInQueue(channel).block()))
                /
                Integer.parseInt(redis.hget(channel, "vids-per-day"));
    }


    public List<String> getDisabledChannels(String colabOrLocal) {
        log.info("Fetching disabled channels");
        Type collectionType = new TypeToken<Collection<String>>() {}.getType();
        return new ArrayList<>(
                gson.fromJson(
                        redis.get(colabOrLocal + "-disabled-channels"),
                        collectionType
                )
        );
    }

    public void backupDisabledChannels(String colabOrLocal, List<String> disabledChannels) {
        log.info("Backing up disabled channels");
        redis.set(colabOrLocal + "-disabled-channels", gson.toJson(disabledChannels));
    }

    @Synchronized
    public void disableChannel(String channel, String colabOrLocal) {
        List<String> channelsToDisable = getDisabledChannels(colabOrLocal);
        channelsToDisable.add(channel);
        backupDisabledChannels(colabOrLocal, channelsToDisable);
    }

    @Synchronized
    public void enableChannel(String channel, String colabOrLocal) {
        List<String> channelsToDisable = getDisabledChannels(colabOrLocal);
        channelsToDisable.remove(channel);
        backupDisabledChannels(colabOrLocal, channelsToDisable);
    }

    public boolean checkIfDisabled(String channel, String colabOrLocal) {
        return getDisabledChannels(colabOrLocal).contains(channel);
    }

}
