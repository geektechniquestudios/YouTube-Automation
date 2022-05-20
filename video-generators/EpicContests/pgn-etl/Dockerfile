FROM python:3
#FROM python:3.7.7-buster

#ARG rsakey
#RUN test -n "${rsakey}" && { \
#    mkdir -p -m 700 /root/.ssh; \
#    echo "${rsakey}" > /root/.ssh/id_rsa; \
#    chmod 600 /root/.ssh/id_rsa; \
#} || :

RUN mkdir -p /root/.ssh
ADD id_rsa /root/.ssh/id_rsa
ADD known_hosts /root/.ssh/known_hosts
RUN chmod 700 /root/.ssh/id_rsa
RUN chmod 700 /root/.ssh/known_hosts

#WORKDIR /usr/src/app
COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt
COPY . .
CMD ["python", "PgnRenderer.py"]
