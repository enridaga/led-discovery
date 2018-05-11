#!/bin/bash
cd "${0%/*}"
mkdir -p evaluation
echo 'All Experiences: experiences.csv'
ls experiences|sed -e 's/.txt$//g'|sort -n >evaluation/experiences.csv
echo 'Experiences in the benchmark: in-benchmark.csv'
cat benchmark/data/*|grep http | sed -e 's/.$//g'|sed -e 's/"//g'|cut -d'/' -f6|sort -n >evaluation/in-benchmark.csv
echo 'Experiences not in the benchmark: for-training.csv'
comm -23 evaluation/experiences.csv evaluation/in-benchmark.csv > evaluation/for-training.csv

