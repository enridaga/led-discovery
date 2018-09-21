#!/bin/bash
mvn exec:exec@analyse-components-coverage -Dinput=../data/experiences/ -Doutput=../data/analysis/components-$1-experiences.csv -Dlimit=$1
mvn exec:exec@analyse-components-coverage -Dinput=../data/negatives-reuters/ -Doutput=../data/analysis/components-$1-reuters.csv -Dlimit=$1
mvn exec:exec@analyse-components-coverage -Dinput=../data/negatives-red/ -Doutput=../data/analysis/components-$1-red.csv -Dlimit=$1
