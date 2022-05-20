import redis
from src import RedisLoader

"""
    !!!WARNING!!! This deletes everything in the db
    Everything is commented out to prevent accidental dbflush
"""

DB = 0
HOST = "10.0.0.20"
PORT = 6379

r = redis.Redis(host=HOST, port=PORT, db=DB)
r.flushdb()

RedisLoader.load_redis_pgns(r)
print("Database load complete")
