import redis


class Metadata:
    def __init__(self, channel_name, r: redis.Redis):
        self.channel_name = channel_name
        self.colab_url = r.hget(channel_name, "colab-url").decode('utf-8') # do I really need this? think about if I should just use the same page all the time... me later - might need other notebooks for different google accounts if I decide to grab more power
        self.description = r.hget(channel_name, "description").decode('utf-8')
        self.pkl = r.hget(channel_name, "pkl").decode('utf-8')
        self.multiplier = r.hget(channel_name, "multiplier").decode('utf-8')
        self.frames = r.hget(channel_name, "frames").decode('utf-8')
        self.truncation_psi = r.hget(channel_name, "truncation-psi").decode('utf-8')
        self.video_number = r.hget(channel_name, "video-number").decode('utf-8')
        self.title = "[" + self.video_number + "] " + r.hget(channel_name, "title").decode('utf-8')
        self.keywords = r.hget(channel_name, "keywords").decode('utf-8')
        self.playlist = r.hget(channel_name, "playlist").decode('utf-8')
        self.thumbnail = " "
        self.category = r.hget(channel_name, "category").decode('utf-8')

        self.seed = r.hget(channel_name, "seed").decode('utf-8')
        self.seed_spacing = r.hget(channel_name, "seed-spacing").decode('utf-8')

        r.hset(channel_name, "seed", str(int(self.seed) + int(self.seed_spacing) + 1))
        r.hset(channel_name, "video-number", str(int(self.video_number) + 1))

        self.command_list = [
            "%tensorflow_version 1.x ",
            "!git clone https://github.com/k4yt3x/video2x.git",
            "!git clone https://github.com/geektechniquestudios/stylegan2",
            "!pip install opensimplex",
            "!pip install -U PyYAML",
            "!apt install ffmpeg",
            "!apt install libmagic1 python3-yaml",
            "!apt install libvulkan-dev",
            "%mkdir stylegan2/datasets",
            "import os",
            "os.chdir('video2x/src')",
            "!git checkout 4.7.0",
            "!pip install -r requirements.txt",
            "!rm -rf video2x.yaml",
            "!wget -O video2x.yaml http://akas.io/v2xcolab",
            "os.chdir('../..')",
            "!wget https://github.com/nihui/realsr-ncnn-vulkan/releases/download/20200818/realsr-ncnn-vulkan-20200818-linux.zip",
            "!7z x realsr-ncnn-vulkan-20200818-linux.zip",
            "!wget https://github.com/nihui/waifu2x-ncnn-vulkan/releases/download/20200818/waifu2x-ncnn-vulkan-20200818-linux.zip",
            "!7z x waifu2x-ncnn-vulkan-20200818-linux.zip",
            "!wget https://github.com/nihui/srmd-ncnn-vulkan/releases/download/20200818/srmd-ncnn-vulkan-20200818-linux.zip",
            "!7z x srmd-ncnn-vulkan-20200818-linux",
            f"!python stylegan2/run_generator.py generate-latent-walk --network=/content/drive/My\ Drive/freedom/pkl-files/{self.pkl} --seeds={self.seed}-{str(int(self.seed) + int(self.seed_spacing))} --frames {self.frames} --truncation-psi={self.truncation_psi}",
            f"!ffmpeg -r 60 -i /content/results/00000-generate-latent-walk/frame%05d.png -vcodec libx264 -pix_fmt yuv420p /content/{self.video_number}.mp4 -loglevel warning -y",
            "%rm -rf /content/results/00000-generate-latent-walk",
            f"!python ./video2x/src/video2x.py -i /content/{self.video_number}.mp4 -o /content/drive/My\ Drive/freedom/{channel_name}/{self.video_number}.mp4 -d waifu2x_ncnn_vulkan -r {self.multiplier}",
            "print(\"Render\" + \"Complete\")"
        ]
