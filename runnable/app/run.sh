#!/bin/bash

file=target/led-discovery-app-0.0.3.led-SNAPSHOT.jar
data="run-data"
cache="run-cache"
log4j2="log4j2.xml"
java -jar -Xmx4G -Dled.dataDir="$data" -Dled.cacheDir="$cache" -Dlog4j.configurationFile="$log4j2" $file
