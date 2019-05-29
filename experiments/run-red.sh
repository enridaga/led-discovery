#!/bin/bash

# mvn exec:exec@experiment-red -DexId=hybrid-book-1 -DexProp=hybrid-book-1
mvn exec:exec@red-analyse-results -Doutput=results -Dexperiments=hybrid-book-1
