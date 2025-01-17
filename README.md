# Youtube Automator
A collection of programs designed to create and streamline content for any number of youtube channels. The intent behind the software here is to fully automate the process of video production.

[Watch a video about it](https://www.youtube.com/watch?v=AjIRlGG11BY)

### Disclaimer
When I first made this, I never intended to make it open source, so there's some painfully hardcoded stuff in here along with a lot of nonsense cruft. You may have some difficulty getting things to work. That said, if anyone's interested in getting this to work, I'll help. Open an issue when you get stuck.

### How does it work?
A quick rundown of each directory: 
- knope is an actuator service that ensures all other services are operational.
- notification service is there to text me if any service goes down. If you want this to work for you, you will need a Twilio account.
- uploader react client acts as a way to interface with your video library, enabling editing of video data, deleting videos, and manual uploads on each channel you make.
- vid uploader service is a spring boot server that accepts incoming videos, ReST calls about the videos, and provides data to the uploader react client.
- video generators is a folder containing all the video generators I've used so far. You can use them as a reference point for getting started.

[![](uploader.png)](#)

This was built to run on a raspberry pi running at `10.0.0.20` in a local network. The basic idea is that you generate a video with your powerful desktop or laptop and send it to a server on the pi. That server uploads videos 5x daily to each channel you make. The way you make the video is up to you. There's also a frontend React client that runs on port 5000 that lets you modify video metadata, like the title, or even upload out of order manually. There are examples in the video generators folder for how to structure the application. The most successful channel is by far [ChessFlix](https://www.youtube.com/channel/UCLlg7famg7h-6PRsMODg3_g), with close to 2200 subscribers and 670k views. My personal favorite though is [TripCandy](https://www.youtube.com/channel/UCWoIlr_HTioFwxeES1vcZSg). I used Generative Adversarial Networks (GANs) to generate these insanely psychedelic videos, but unfortunately, it just took way too much compute power to do this constantly, so there are only a few videos. [Goflix](https://www.youtube.com/channel/UC8SpM2khnSqdF0K1OuevSTw) is another example of a channel that shows games of Go. There's also a ChessFlix clone called [ChessAfterDark](https://www.youtube.com/channel/UCSpKQBmPUm-eqJtts8cUPKA).

If you want to use this software, you'll need to get the vid-uploader-service working by installing the maven dependencies. Then you'll want to create a file in the folder `vid-uploader-service\src\main\java\com\geektechnique\viduploader\controller\channels` and call it the name of your channel. 

It should look like this. All you need to add are the 3 fields for the channel name and credentials to upload.


```java
package com.geektechnique.viduploader.controller.channels;

import com.geektechnique.viduploader.config.RedisConfig;
import com.geektechnique.viduploader.controller.BaseVideoController;
import com.geektechnique.viduploader.model.BaseConfigModel;
import com.geektechnique.viduploader.service.Uploader;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@ConditionalOnProperty(
        value = "chess-flix.enabled",
        havingValue = "true"
)
@RestController
@RequestMapping("/chess-flix")
public class ChessFlix extends BaseVideoController {
    public ChessFlix() {
        super(
                new Uploader(
                        new BaseConfigModel(
                                new RedisConfig(0),
                                "chess-flix",
                                "/home/pi/vid-upload/chess-flix/.client_secret.json",
                                "/home/pi/vid-upload/chess-flix/.youtube-upload-credentials.json"
                        )
                )
        );
    }

    @Scheduled(cron = cronTiming)
    private void uploadTiming() {
        super.uploader.uploadOnSchedule();
    }
}
```

Then go to the application.yml file and enable the channel you just made. I have chosen to leave all of my channels here for reference. You can delete the entire folder and just include what you need. You'll also need Redis running on the raspberry pi. The provided crontab will handle starting of the server on boot. You can add it to your pi by running the command `contab -e` and pasting the contents of crontab-instructions.txt. This repo is configured to use Rclone to virtually mount a google drive folder to the pi; you'll need to set that up as well, but you could easily use a normal directory. This folder holds the videos to be uploaded. You'll also want to ensure you create your own `id_rsa` and `known hosts` to scp to the pi and replace the provided one. You'll need this for your video generator to be able to send videos without entering a password.

After restarting the pi, You can visit `10.0.0.20:5000` to use the frontend webpage to control uploading and see data about the state of your uploads.


###### Please don't hesitate to open an issue. This isn't easy or very polished because who knows if anyone will use it. If you think this could help you, I'll make it more accessible.
