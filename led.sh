#!/bin/bash
home=$(cd && pwd)
path="$home/.m2/repository/led-discovery/led-tools"
version="0.0.1-SNAPSHOT"
jar="${path}/${version}/led-tools-${version}-jar-with-dependencies.jar"
echo "$@"
java -Xmx8G -Dlog4j.configurationFile=log4j2.xml -jar $jar "$@" 
