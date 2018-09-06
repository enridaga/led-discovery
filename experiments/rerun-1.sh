#!/bin/bash
mvn exec:exec@experiment-gs -DexId=led-components-1t-all -DexProp=../data/experiments/led-components-1t-all.properties
mvn exec:exec@experiment-gs -DexId=led-components-1t-min3 -DexProp=../data/experiments/led-components-1t-min3.properties
mvn exec:exec@experiment-gs -DexId=led-components-1t-min4 -DexProp=../data/experiments/led-components-1t-min4.properties
mvn exec:exec@experiment-gs -DexId=led-components-1a-all -DexProp=../data/experiments/led-components-1a-all.properties
mvn exec:exec@experiment-gs -DexId=led-components-1a-min3 -DexProp=../data/experiments/led-components-1a-min3.properties
mvn exec:exec@experiment-gs -DexId=led-components-1a-min4 -DexProp=../data/experiments/led-components-1a-min4.properties

mvn exec:exec@analyse-results -Doutput=experiments-components-1.result.csv -Dexperiments=led-components-1t-all-gs,led-components-1t-min3-gs,led-components-1t-min4-gs,led-components-1a-all-gs,led-components-1a-min3-gs,led-components-1a-min4-gs,rf--le-reu-red--le10k-gs,rf--le-neg--le1k-gs,rf--le-neg--gut5k-gs,rf--le-neg--gut10k-gs,heat-gutenberg2-00005-20-gs
