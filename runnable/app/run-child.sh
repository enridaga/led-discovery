#!/bin/bash

file=target/led-discovery-app-0.0.7-SNAPSHOT.jar
data="child-data"
cache="child-cache"
method="MusicEmbeddings"
input="true"
log4j2="log4j2.xml"
java -jar -Xmx4G -Dled.dataDir="$data" -Dled.userInputEnabled="$input" -Dled.method="$method" -Dled.cacheDir="$cache" -Dlog4j.configurationFile="$log4j2" $file
