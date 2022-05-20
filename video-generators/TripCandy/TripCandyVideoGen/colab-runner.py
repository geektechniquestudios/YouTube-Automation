import sys
import redis
import time
import requests
from random import randint
from selenium import webdriver
from selenium.webdriver import ActionChains
from selenium.webdriver.chrome.options import Options
from selenium.common.exceptions import NoSuchElementException
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as ec
from Metadata import Metadata

sys.path.insert(0, '/usr/lib/chromium-browser/chromedriver')

HOST = "10.0.0.20"
PORT = 6379
DB = 4
r = redis.Redis(host=HOST, port=PORT, db=DB)
isHeadless = False #todo change for pi


def random_sleep_ms(minimum_sleep, sleep_time):
    time.sleep(randint(minimum_sleep, sleep_time) / 1000)


def exists_by_text(driver, text):
    driver.implicitly_wait(2)
    try:
        driver.find_element_by_xpath("//*[contains(text(), '" + str(text) + "')]")
    except NoSuchElementException:
        driver.implicitly_wait(5)
        return False
    driver.implicitly_wait(5)
    return True


def exists_by_text2(driver, text):
    try:
        WebDriverWait(driver, 2).until(
            ec.presence_of_element_located((By.XPATH, "//*[contains(text(), '" + str(text) + "')]")))
    except Exception:
        return False
    return True


def load_channel_data(channel_name):
    print(f"loading metadata for {channel_name}")
    return Metadata(channel_name, r)


def setup_webdriver():
    chrome_options = Options()
    chrome_options.add_argument('--no-sandbox')
    if isHeadless:
        print("using headless mode")
        chrome_options.add_argument('--headless')
    else:
        print("using gui")
    chrome_options.add_argument('--disable-dev-shm-usage')
    return webdriver.Chrome('chromedriver', options=chrome_options)


def login_through_so(wd, channel_name):
    def load_page():
        print("loading stackOverflow")
        wd.get("https://stackoverflow.com/users/login?ssrc=head&returnurl=https%3a%2f%2fstackoverflow.com%2f")
        random_sleep_ms(2000, 5000)
        wd.find_element_by_class_name("s-btn__google").send_keys(Keys.ENTER)
        random_sleep_ms(500, 2000)

    def type_username():
        print("typing username")
        uname_field = wd.find_element_by_class_name("zHQkBf")
        uname = r.hget(channel_name, "uname").decode('utf-8')
        for letter in uname:
            uname_field.send_keys(letter)
            random_sleep_ms(10, 200)
        uname_field.send_keys(Keys.ENTER)
        random_sleep_ms(2000, 4000)

    def type_pw():
        print("entering password")
        pw_field = wd.find_element_by_class_name("zHQkBf")
        pw = r.hget(channel_name, "pw").decode('utf-8')
        for letter in pw:
            pw_field.send_keys(letter)
            random_sleep_ms(10, 200)
        pw_field.send_keys(Keys.ENTER)
        random_sleep_ms(2000, 4000)

    load_page()
    type_username()
    type_pw()


def go_to_colab(wd, colab_url):
    print("loading colab")
    wd.get(colab_url)
    random_sleep_ms(5000, 10000)


def connect_to_hosted_runtime(wd, colab_url):
    def check_if_runtime_disconnected():
        if exists_by_text(wd, "Runtime disconnected"):
            try:
                print("runtime disconnected")
                wd.find_element_by_id("ok").click()
            except NoSuchElementException:
                pass

    def check_for_load_error():
        if exists_by_text2(wd, "Notebook loading error"):
            print("load error")
            wd.get(colab_url)

    def check_gpu_availability():
        if exists_by_text(wd, "Cannot connect to GPU backend"):
            print("Googl  e said for now. Trying again in 12 hours.")
            SystemExit(1)

    def factory_reset_runtime():
        print("factory resetting runtime")
        wd.find_element_by_id("runtime-menu-button").click()
        random_sleep_ms(500, 3000)
        wd.find_element_by_id(":20").click()
        random_sleep_ms(1000, 2000)
        wd.find_element_by_xpath('//*[@id="ok"]').click()

    check_if_runtime_disconnected()
    random_sleep_ms(1000, 2000)
    check_for_load_error()
    random_sleep_ms(1000, 2000)
    check_gpu_availability()
    random_sleep_ms(1000, 2000)
    factory_reset_runtime()
    random_sleep_ms(8000, 12000)
    wd.get(colab_url)
    random_sleep_ms(1000, 2000)


def update_colab_cell(wd, metadata):
    def highlight_text():
        print("highlighting text")
        ActionChains(wd).key_down(Keys.CONTROL).send_keys("a").key_up(Keys.CONTROL).perform()
        random_sleep_ms(200, 800)

    def enter_new_text(command_list):
        print("typing new text", end="")
        for command in command_list:
            for char in command:
                random_sleep_ms(3, 70)
                ActionChains(wd).send_keys(char).perform()
                random_sleep_ms(5, 75)
            ActionChains(wd).send_keys(Keys.SPACE).perform()
            random_sleep_ms(20, 200)
            ActionChains(wd).send_keys(Keys.ENTER).perform()
            random_sleep_ms(10, 200)
            print(".", end="")

    highlight_text()
    enter_new_text(metadata.command_list)


def run_cell(wd):
    print("running colab cell")
    random_sleep_ms(2000, 3000)
    ActionChains(wd).key_down(Keys.CONTROL).send_keys(Keys.ENTER).perform()


def wait_for_render(wd):
    print("waiting for render to complete", end="")
    while True:
        time.sleep(1800)  # 30 minutes
        print(".", end="")
        try:
            if "RenderComplete" in wd.page_source:  # works, but super weak code. can do better TODO high priority
                print("completion string found")
                wd.switch_to.default_content()
                # redis set key to rendered
                return
        except Exception:
            pass


def send_json_to_video_uploader(metadata):
    def request_until_success(url, data):
        print("sending json to video uploader")
        is_post_successful = False
        request_delay = 1

        while not is_post_successful:  # TODO after so many fails, post about failure to knope so she can request next render
            try:
                requests.post(url, json=data)
                is_post_successful = True
                print("json request sent")
            except Exception:
                print("HTTP Request Failed - sleeping for " + str(request_delay) + "seconds. \n\n")
                time.sleep(request_delay)
                request_delay = request_delay * 2

    video_json = {
        "vidNumber": metadata.video_number,
        "category": metadata.category,
        "title": metadata.title,
        "description": metadata.description,
        "keywords": metadata.keywords,
        "privacyStatus": "public",
        "playlist": metadata.playlist,
        "thumbnail": metadata.thumbnail
    }

    request_until_success(metadata.colab_url, video_json)


def send_text(channel_name):
    url = "http://10.0.0.20:8081/send-text"
    data = f"Colab rendering for {channel_name} failed"
    try:
        requests.post(url, json=data)
        print("json request sent")
    except Exception as e:
        print("Sending text failed. \n\n !Pi is probably offline!" + str(e))


def start_next_render(channel_name):
    url = f"http://10.0.0.20:8082/render-complete/{channel_name}"
    data = f"Colab rendering for {channel_name} failed"
    try:
        requests.post(url, json=data)
        print("json request sent")
    except Exception as e:
        print("Sending text failed. \n\n !Pi is probably offline! " + str(e))


def render_one(channel_name):
    try:
        metadata = load_channel_data(channel_name)
        wd = setup_webdriver()

        login_through_so(wd, channel_name)
        go_to_colab(wd, metadata.colab_url)
        connect_to_hosted_runtime(wd, metadata.colab_url)
        update_colab_cell(wd, metadata)
        run_cell(wd)
        wait_for_render(wd)
        send_json_to_video_uploader(metadata)
    except Exception as e:
        print(f"Failed for some reason => {e}")
        send_text(channel_name)
    start_next_render(channel_name)


if __name__ == '__main__':
    render_one(sys.argv[1])
