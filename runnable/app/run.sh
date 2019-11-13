#!/bin/bash

file=target/led-discovery-app-0.0.4-SNAPSHOT.jar
data="hybrid-data"
cache="hybrid-cache"
method="heat-entity-music-filtered"
input="0"
log4j2="log4j2.xml"
java -jar -Xmx4G -Dled.dataDir="$data" -Dled.userInputEnabled="$input" -Dled.method="$method" -Dled.cacheDir="$cache" -Dlog4j.configurationFile="$log4j2" $file
