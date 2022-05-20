import time
import chess.pgn
import chess.svg
import redis
import requests
import json
import atexit
import itertools
from moviepy.editor import *
from src import HandleImages, RedisLoader

"""
    @Author: Terry Dorsey 
    !!!!WARNING!!!!
        !Running this program flushes the redisdb running on 10.0.0.20! 
    !!!!WARNING!!!!
"""

COLOR_ITER = itertools.cycle(iter(["green", "blue", "brown"]))

RESOLUTION = 2160
COLOR = next(COLOR_ITER)  # brown, blue, green
FRAMERATE = 30
MOVE_TIME = 1.4  # seconds between moves
HOST = "10.0.0.20"  # "localhost"
PORT = 6379
NUM_OF_WORKERS = 1
EVENT = ""

r = redis.Redis(host=HOST, port=PORT, db=0)

y = 0  # keeps value of current loop outside of the loop, for keyboard exit


def save_state():
    print("Process interrupted, state reverted to last successful transfer")
    r.hset(y, "fetched", 0)
    r.hset(y, "rendered", 0)


atexit.register(save_state)  # runs this on exit, except for when running in IDE for some reason


def validate_redis():
    is_redis_valid = str(r.get("validation"))
    if is_redis_valid == "None":  # is_redis_valid != "1": ????? need to try
        print("Setting up database")
        # RedisLoader.load_redis_pgns(r)

    print("Database validated")


def render_video():
    print("Starting render for video " + file_string)

    pgn = open("./game-pgns/" + str(x) + ".pgn")
    #pgn = open("//10.0.0.20:~/startup.sh")

    game = chess.pgn.read_game(pgn)
    pgn.close()
    board = game.board()

    clips_list = []

    for move in game.mainline_moves():
        clip = ImageClip(
            HandleImages.position_to_image(board, RESOLUTION, COLOR)) \
            .set_duration(MOVE_TIME) \
            .set_fps(FRAMERATE)
        clips_list.append(clip)
        clip.close()
        board.push(move)
        print(board, end="\n\n")

    #consider instead of imageclip, using ImageSequenceClip
    clip = ImageClip(  # yeah I did it. easier to read this way
        HandleImages.position_to_image(board, RESOLUTION, COLOR)) \
        .set_duration(MOVE_TIME) \
        .set_fps(FRAMERATE)
    clips_list.append(clip)
    clip.close()

    # add music here

    clips_list.extend([clips_list[-1], clips_list[-1]])

    vid_wo_text = concatenate_videoclips(clips_list)

    # txt_clip = TextClip("Please fucking work", fontsize=12, color='white')
    # txt_clip.set_position(("right", "bottom"))
    # txt_clip.set_duration(vid_wo_text.duration)
    #
    # burn_video = CompositeVideoClip([vid_wo_text, txt_clip])

    # result_text = TextClip(game.headers["Result"],
    #                        color="white",
    #                        font="Amiri-Bold",
    #                        kerning=5,
    #                        fontsize=100)\
    #     .set_position("center") \
    #     .set_duration(5) \

    vid_wo_text.write_videofile("./vids-to-send/" + file_string)

    r.hset(str(x), "rendered", 1)

    return game


def generate_title(_game):
    global EVENT
    white_elo = _game.headers["WhiteElo"]
    if white_elo == "0":
        white_elo = ""
    else:
        white_elo = "(" + white_elo + ")"
    black_elo = _game.headers["BlackElo"]
    if black_elo == "0":
        black_elo = ""
    else:
        black_elo = "(" + black_elo + ")"

    white_name_temp = _game.headers["White"].split(",")
    white_name_temp.reverse()
    white_name = " ".join(map(str, white_name_temp))[1:]

    black_name_temp = _game.headers["Black"].split(",")
    black_name_temp.reverse()
    black_name = " ".join(map(str, black_name_temp))[1:]

    event = _game.headers["Event"]

    if event == "?":
        event = ""
    else:
        EVENT = event
        event = "Event: " + event

    date = _game.headers["Date"]

    if date == "?":
        date = ""

    # title must be under 100 chars
    _title = (
            white_name + white_elo +
            " vs " +
            black_name + black_elo + " | " +
            event + " | " +
            date
    )

    if len(_title) > 100:
        _title = (
                white_name + white_elo +
                " vs " +
                black_name + black_elo
        )

        if len(_title) > 100:
            _title = "GM Chess"

    return _title


def send_video():
    scp_val = (os.system("scp ./vids-to-send/" + file_string + " pi@10.0.0.20:/mnt/epic-contests-vids-to-upload/" + file_string))
    if scp_val == 0:
        # sent = True
        r.hset(x, "sent", 1)
        print("SCP transfer completed successfully")
        os.remove("./vids-to-send/" + file_string)
        #  Store var in redis called "sent", catch keyboard interrupt, if sent == 0 and keyboard interrupted, add to fail list,
    else:
        print("SCP failed")
        r.hset(x, "failure", 1)
        r.lpush("failList", x)
    # for some reason, this makes os.replace fail

    # just for testing on windows
    # os.replace("./vids-to-send/" + file_string,
    #            "/Users/nope/Desktop/GitRepos/chess-uploader/yt-uploader-api/vids-to-upload/" + file_string)


def send_json():
    global EVENT
    pgn = open("./game-pgns/" + str(x) + ".pgn")
    game_text = pgn.read()
    pgn.close()  # leaving the file open until here will make it fail

    description = ("Enjoy a massive volume of GM chess games played at the highest level rendered in 4k. " +
                   "No interruptions, just chess. " +
                   "Check out the rest of the channel and don't forget to like and SUBSCRIBE! See the game pgn below." +
                   "\n\n" +
                   game_text)

    keywords = "chess, gm, grandmaster"

    # url = "http://localhost:8080/upload_service/"  # Dev
    url = "http://10.0.0.20:8080/epic-contests/add_video"  # Prod @todo move outside of loop, improve efficiency

    data = {
        "vidNumber": x,
        "category": "Gaming",
        "title": title,
        "description": description,
        "keywords": keywords,
        "privacyStatus": "public",
        "playlist": "Chess Forever" if EVENT == "" else EVENT,  # get event name
        "thumbnail": " "
    }

    r.hset(x, "json", json.dumps(data))

    is_post_successful = False
    request_delay = 1

    while not is_post_successful:
        try:
            requests.post(url, json=data)
            is_post_successful = True
        except Exception as e:
            print("HTTP Request Failed - sleeping for " + str(request_delay) + "seconds.")
            time.sleep(request_delay)
            request_delay = request_delay * 2
            # print(e)
            # r.hset(x, "failure", 1)
            # r.lpush("failList", x)
            # sys.exit(1)


# validate_redis()
recent_idx = int(r.get("epic-contests-recent-index"))  # 275474 total videos
for x in range(recent_idx - NUM_OF_WORKERS, 275474):
    file_string = str(x) + ".mp4"
    if int(r.hget(str(x), "fetched")) == 1:
        continue
    else:
        r.hset(str(x), "fetched", 1)

    y = x  # making loop value global #omg I can just use the global keyword @todo

    _game = render_video()
    title = generate_title(_game)
    send_video()
    send_json()

    COLOR = next(COLOR_ITER)

    r.decr("epic-contests-recent-index")  # make method for resetting stuff?
    EVENT = ""


# @todo move render onto usb drive