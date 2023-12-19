#!/bin/sh

hadoop namenode -format

wait

/usr/local/hadoop/sbin/start-all.sh