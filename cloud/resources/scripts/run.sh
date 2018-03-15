#!/bin/bash


java \
-Dlog4j.configurationFile=./resources/config/log4j-prod.xml \
-Duser.timezone=UTC \
-jar ./target/pisecurity-pi-0.1-SNAPSHOT-jar-with-dependencies.jar \
$*
