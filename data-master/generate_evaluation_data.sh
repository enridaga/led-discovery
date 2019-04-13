#!/bin/bash
cd "${0%/*}"
mkdir -p evaluation
echo 'All Experiences: experiences.csv'
ls experiences|sed -e 's/.txt$//g'|sort -n >evaluation/experiences.csv
echo 'Experiences in the benchmark: in-benchmark.csv'
cat benchmark/data/*|grep http | sed -e 's/.$//g'|sed -e 's/"//g'|cut -d'/' -f6|sed 's/^[ \t]*//;s/[ \t]*$//'|sort -n >evaluation/in-benchmark.csv
echo 'Experiences not in the benchmark: for-training.csv'
comm -23 <(sort evaluation/experiences.csv) <(sort evaluation/in-benchmark.csv) > evaluation/for-training.csv
echo 'Sources: sources.csv'
ls benchmark/sources>evaluation/sources.csv
echo 'Benchmark: benchmark.csv'
cat benchmark/generated-benchmark.csv |grep -i ".txt,1"|cut -d',' -f1-5>evaluation/benchmark.csv
echo 'Negative examples from Reuters dataset (same number as training)'
ls -U negatives/|head -9059>evaluation/negatives.csv

