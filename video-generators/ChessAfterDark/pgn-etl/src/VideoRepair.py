import redis
import time
import chess.pgn
import chess.svg
from moviepy.editor import *
from src import HandleImages

RESOLUTION = 2160
COLOR = "brown"  # brown, blue, green
FRAMERATE = 30
SECONDS_BETWEEN_MOVES = 2.7
HOST = "10.0.0.20"
PORT = 6379
NUM_OF_VIDS = 3652 + 31
VID_PATH = "//raspberrypi/NAS/mnt/vids-to-upload/"
STARTING_VID = 539

r = redis.Redis(host=HOST, port=PORT, db=0)


def get_current_place() -> int:
    print("Finding current video")
    return int(r.get("repair_index"))


def load_current_vid(current_file_string) -> VideoFileClip:
    print("Loading current vid")

    load_success = False
    request_delay = 1

    while not load_success:  # maybe use higher order func for this retry policy
        try:
            clip = VideoFileClip(VID_PATH + current_file_string)
            load_success = True
            return clip
        except Exception as e:
            print(e)
            # if no file found error, incr current_file_string
            time.sleep(request_delay)
            request_delay = request_delay * 2


def get_last_move_clip(vid_num: int):
    pgn = open("../game-pgns/" + str(vid_num) + ".pgn")
    game = chess.pgn.read_game(pgn)
    pgn.close()
    #board = game.board()

    # maybe loop through mainline if this doesn't work
    # board.set_board_fen(game.end().board().fen())
    #  board.push(game.end().move)
    return ImageClip(HandleImages.position_to_image(game.end().board(), RESOLUTION, COLOR)) \
        .set_duration(SECONDS_BETWEEN_MOVES) \
        .set_fps(FRAMERATE)


def edit_current_vid(current_clip, clip_to_add):
    print("editing current vid")
    clip_list = [current_clip, clip_to_add]
    return concatenate_videoclips(clip_list)


def write_videofile(edited_vid, file_string):
    print("Writing videofile " + file_string)
    edited_vid.write_videofile("D:" + file_string)


def send_video(file_string):
    print("Sending updated video to server")
    scp_val = 1
    request_delay = 1

    while scp_val != 0:
        scp_val = (os.system("scp D:" + file_string + " pi@10.0.0.20:/mnt/vids-to-upload/" + file_string))
        if scp_val == 0:
            # sent = True
            print("SCP transfer completed successfully")
            os.remove("D:" + file_string)
            return  # maybe scp_val = 1 is better?
        else:
            print("SCP failed")
            time.sleep(request_delay)
            request_delay = request_delay * 2


def main():
    for i in range(get_current_place() + STARTING_VID, NUM_OF_VIDS + STARTING_VID):
        print("starting main loop")
        file_string = str(i) + ".mp4"
        current_clip = load_current_vid(file_string)
        clip_to_add = get_last_move_clip(i)
        edited_vid = edit_current_vid(current_clip, clip_to_add)
        write_videofile(edited_vid, file_string)
        send_video(file_string)
        r.set("repair_index", i - STARTING_VID + 1)

    print("Video repair completed successfully")


if __name__ == "__main__":
    main()
