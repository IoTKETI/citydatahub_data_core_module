#!/bin/bash

java -Dspring.profiles.active=local \
  -Djava.net.preferIPv4Stack=true \
	-jar data-service-broker-2.0.2.jar > /dev/null 2>&1 &

sleep 2

tail -f logs/data-service-broker.log
