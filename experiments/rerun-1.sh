#!/bin/bash
mvn exec:exec@experiment-gs -DexId=led-components-1t-all -DexProp=../data/experiments/led-components-1t-all.properties 
mvn exec:exec@experiment-gs -DexId=led-components-1t-min3 -DexProp=../data/experiments/led-components-1t-min3.properties
mvn exec:exec@experiment-gs -DexId=led-components-1t-min4 -DexProp=../data/experiments/led-components-1t-min4.properties
mvn exec:exec@experiment-gs -DexId=led-components-1a-all -DexProp=../data/experiments/led-components-1a-all.properties
mvn exec:exec@experiment-gs -DexId=led-components-1a-min3 -DexProp=../data/experiments/led-components-1a-min3.properties
mvn exec:exec@experiment-gs -DexId=led-components-1a-min4 -DexProp=../data/experiments/led-components-1a-min4.properties

mvn exec:exec@experiment-gs -DexId=heat-gutenberg2-000112 -DexProp=../data/experiments/heat-gutenberg2-000112.properties
mvn exec:exec@experiment-gs -DexId=stacked-compo-heat-1 -DexProp=../data/experiments/stacked-compo-heat-1.properties
mvn exec:exec@experiment-gs -DexId=stacked-compo-heat-2 -DexProp=../data/experiments/stacked-compo-heat-2.properties
mvn exec:exec@experiment-gs -DexId=stacked-forest-heat-3 -DexProp=../data/experiments/stacked-forest-heat-3.properties
mvn exec:exec@experiment-gs -DexId=stacked-compo-forest-heat-4 -DexProp=../data/experiments/stacked-compo-forest-heat-4.properties
mvn exec:exec@experiment-gs -DexId=stacked-heat-compo-5 -DexProp=../data/experiments/stacked-heat-compo-5.properties
mvn exec:exec@experiment-gs -DexId=stacked-forest-compo-heat-6 -DexProp=../data/experiments/stacked-forest-compo-heat-6.properties
mvn exec:exec@experiment-gs -DexId=stacked-forest-compo-heat-7 -DexProp=../data/experiments/stacked-forest-compo-heat-7.properties
mvn exec:exec@experiment-gs -DexId=stacked-compo-heat-8 -DexProp=../data/experiments/stacked-compo-heat-8.properties

mvn exec:exec@analyse-results -Doutput=experiments-components-1.result.csv -Dexperiments=led-components-1t-all-gs,led-components-1t-min3-gs,led-components-1t-min4-gs,led-components-1a-all-gs,led-components-1a-min3-gs,led-components-1a-min4-gs,rf--le-reu-red--le10k-gs,rf--le-neg--le1k-gs,rf--le-neg--gut5k-gs,rf--le-neg--gut10k-gs,heat-gutenberg2-000112-gs,stacked-compo-heat-1-gs,stacked-compo-heat-2-gs,stacked-forest-heat-3-gs,stacked-compo-forest-heat-4-gs,stacked-heat-compo-5-gs,stacked-forest-compo-heat-6-gs,stacked-compo-heat-8-gs