#!/bin/bash

git pull && mvn clean install && mvn exec:exec >eeanalysis.log 2>&1 && git add ../data/eeanalysis/analysis.csv ../data/eeanalysis/sources/ && git commit -m "Updated analysis" && git push
