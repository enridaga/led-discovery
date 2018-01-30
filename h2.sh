#!/bin/bash
path="/Users/ed4565/.m2/repository/led/led-discovery/"
version="0.0.1-SNAPSHOT"
jar="${path}${version}/led-discovery-${version}-jar-with-dependencies.jar"
echo "$@"
java -cp "$jar" org.h2.tools.Shell
 
