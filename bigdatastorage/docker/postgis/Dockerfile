FROM postgres

ENV DEBIAN_FRONTEND=noninteractive

ENV LANG en_US.utf8
ENV LC_ALL en_US.utf8
RUN apt-get update && apt-get install -y locales && \
    echo "en_US.UTF-8 UTF-8" > /etc/locale.gen && \
    locale-gen


RUN apt-get update && \
    apt-get -y upgrade && \
    apt-get -y install \
            postgis \
            dos2unix && \
    apt-get -y autoremove && \
    apt-get autoclean

WORKDIR /docker-entrypoint-initdb.d

ADD ./init .

RUN dos2unix 01-createdb.sh
