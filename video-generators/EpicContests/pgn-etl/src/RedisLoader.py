import redis
import os


def load_redis_pgns(r: redis.Redis):
    #  r.flushdb()
    for pgn in os.listdir("../game-pgns"):
        game_num = pgn[0:-4]
        r.hset(game_num, "fetched", 0)
        r.hset(game_num, "rendered", 0)
        r.hset(game_num, "sent", 0)
        r.hset(game_num, "received", 0)
        r.hset(game_num, "uploaded", 0)
        r.hset(game_num, "failure", 0)
        r.hset(game_num, "json", "[]")
        print(pgn + " loaded")
    r.set("videoUploadQueue", "[]")
    r.lpush("failList", "?")
    r.set("validation", 1)



