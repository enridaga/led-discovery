#!/bin/bash
path="/Users/ed4565/.m2/repository/led/led-discovery/"
version="0.0.1-SNAPSHOT"
jar="${path}${version}/led-discovery-${version}-jar-with-dependencies.jar"
echo "$@"
java -Xmx8G -Dlog4j.configurationFile=src/main/resources/log4j2.xml -jar $jar "$@" 
