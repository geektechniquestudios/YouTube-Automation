import os
import subprocess
import time
import redis
from sgfmill import sgf
import requests


HOST = "10.0.0.20"
PORT = 6379
NUM_OF_WORKERS = 0
NUM_OF_GAMES = 56313
EVENT = ""

r = redis.Redis(host=HOST, port=PORT, db=1)


def make_gif(i):
    os.system("go run ../sgf_to_gif/sgf_to_gif.go -s 56 -n -m 8 -c ../games/" + str(
        i) + ".sgf")  # go run ../sgf_to_gif/sgf_to_gif.go -s 56 -n -m 8 -c ../games/1.sgf


def make_mp4(i):
    os.system("ffmpeg -i ../games/" + str(
        i) + ".gif faststart -pix_fmt yuv420p -vf \"setpts=2.0*PTS\" ./vids_to_send/" + str(
        i) + ".mp4 -y")
    os.remove("../games/" + str(i) + ".gif")
    r.hset(str(i), "rendered", 1)


def send_metadata(i):
    global EVENT
    url = "http://10.0.0.20:8080/go-flix/add_video"

    with open("../games/" + str(i) + ".sgf", encoding="utf8") as file:
        try:
            game = sgf.Sgf_game.from_string(file.read())
        except Exception as e:
            # game = sgf.Sgf_game.from_bytes(file.read())  # @todo fix this garbage
            print(e)  # need to restart loop from here
    game_root = game.get_root()
    black_player = game_root.get("PB") if "PB" in game_root.get_raw_property_map() else "Unknown Player"
    black_rank = "(" + game_root.get("BR") + ")" if "BR" in game_root.get_raw_property_map() else ""
    komi = str(game.get_komi()) if game.get_komi() is not None else ""
    white_player = game_root.get("PW") if "PW" in game_root.get_raw_property_map() else "Unknown Player"
    white_rank = "(" + game_root.get("WR") + ")" if "WR" in game_root.get_raw_property_map() else ""
    # winner = game.get_winner()
    date = game_root.get("DT") if "DT" in game_root.get_raw_property_map() else ""
    event = game_root.get("EV") if "EV" in game_root.get_raw_property_map() else ""
    game_round = game_root.get("RO") if "RO" in game_root.get_raw_property_map() else ""
    EVENT = event

    event = " - " + event if game_round is not "" else ""
    game_round = " - round " + game_round if game_round is not "" else ""
    date = " - " + date if date is not "" else ""
    komi = " - komi: " + komi if komi is not "" else ""


    title = black_player + black_rank + " vs " + white_player + white_rank + event + game_round + date + komi

    if len(title) > 100:
        title = black_player + black_rank + " vs " + white_player + white_rank + event + game_round + date
    if len(title) > 100:
        title = black_player + black_rank + " vs " + white_player + white_rank + event + game_round
    if len(title) > 100:
        title = black_player + black_rank + " vs " + white_player + white_rank + game_round

    description = ("Enjoy a massive volume of pro Go (Baduk) games played at the highest level rendered in HD. " +
                   "No interruptions, just Go. " +
                   "Check out the rest of the channel and don't forget to like and SUBSCRIBE!"
                   )
                   #  create json dictionary
    data = {
        "vidNumber": i,
        "category": "Gaming",
        "title": title,
        "description": description,
        "keywords": "go, baduk, professional, mind sport",
        "privacyStatus": "public",
        "playlist": "Infinite Go" if EVENT == "" else EVENT,  # get event name
        "thumbnail": " "
    }

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


def send_video(i):
    file_string = str(i) + ".mp4"

    is_scp_successful = False
    request_delay = 1

    while not is_scp_successful:
        scp_val = (os.system("scp ./vids_to_send/" + file_string + " pi@10.0.0.20:/mnt/go-flix-vids-to-upload/" + file_string))
        if scp_val == 0:
            # sent == True
            r.hset(i, "sent", 1)
            print("SCP transfer completed successfully")
            os.remove("./vids_to_send/" + file_string)
            is_scp_successful = True
        else:
            print("SCP failed")
            r.hset(i, "failure", 1)
            r.lpush("failList", i)
            request_delay *= 2


def main():
    global EVENT
    recent_index = int(r.get("go-flix-recent-index"))

    for i in range(recent_index - NUM_OF_WORKERS, NUM_OF_GAMES + 1):
        print("Generating GIF for vid " + str(i))
        make_gif(i)
        print("Generating MP4 for vid " + str(i))
        make_mp4(i)
        print("Sending Video for vid " + str(i))
        send_video(i)
        print("Sending Metadata for vid " + str(i))
        send_metadata(i)
        recent_index += 1
        r.set("go-flix-recent-index", recent_index)
        EVENT = ""


main()
