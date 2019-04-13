#!/bin/bash
####
echo "Gold Standard: ../data/evaluation/gold-standard.csv"
echo ""
echo "=========================================================================="
echo "Running Experiment: Baseline / Statistical"
echo "=========================================================================="
echo "Using Dictionary: ../data/gut_dictionary_V2.csv"
#mvn --quiet exec:exec@experiment-gs -DexId=statistical-tfidf -DexProp=../data/experiments/statistical-tfidf.properties
echo "Results in ../data/experiments/statistical-tfidf-gs.output.csv"
echo "=========================================================================="
echo ""
echo "(wait)"
sleep 3
######
echo ""
echo "=========================================================================="
echo "Running Experiment: Baseline / Forest"
echo "Using Features: ../data/analysis/features-le-neg--le1k.parquet"
echo "Using Features Dictionary: ../data/analysis/features-le-neg--le1k.parquet.vocab"
echo "Using Trained Model: ../data/analysis/model-rf-le-neg--le1k.parquet"
#mvn --quiet exec:exec@experiment-gs -DexId=random-forest -DexProp=../data/experiments/random-forest.properties
echo "Results in ../data/experiments/random-forest-gs.output.csv"
echo "=========================================================================="
echo ""
echo ""
echo "(wait)"
sleep 3
######
echo ""
echo "=========================================================================="
echo "Running Experiment: Baseline / Embeddings"
echo "Using Dictionary: ../data/embeddings-dictionary-music.csv"
#mvn --quiet exec:exec@experiment-gs -DexId=embeddings -DexProp=../data/experiments/embeddings.properties
echo "Results in ../data/experiments/embeddings-gs.output.csv"
echo "=========================================================================="
echo ""
echo ""
echo "(wait)"
sleep 3
######
echo ""
echo "=========================================================================="
echo "Running Experiment: Baseline / Entities"
echo "WARNING! This experiment relies on public APIs. Responses from previous execution are cached in folder .simple-cache."
echo "Using DBpedia Spotlight: https://api.dbpedia-spotlight.org/en/annotate"
echo "Using DBpedia SPARQL Endpoint: http://dbpedia.org/sparql"
#mvn --quiet exec:exec@experiment-gs -DexId=entities-music -DexProp=../data/experiments/entities-music.properties
echo "Results in ../data/experiments/entities-music-gs.output.csv"
echo "=========================================================================="
echo ""
echo ""
echo "(wait)"
sleep 3
######
echo ""
echo "=========================================================================="
echo "Running Experiment: Baseline / Embeddings (Filtered)"
echo "Using Dictionary: ../data/embeddings-dictionary-music.csv"
#mvn --quiet exec:exec@experiment-gs -DexId=embeddings-filtered -DexProp=../data/experiments/embeddings-filtered.properties
echo "Results in ../data/experiments/embeddings-filtered-gs.output.csv"
echo "=========================================================================="
echo ""
echo ""
echo "(wait)"
sleep 3
######
echo ""
echo "=========================================================================="
echo "Running Experiment: Baseline / Hybrid (Unfiltered)"
echo "Using Dictionary: ../data/embeddings-dictionary-music.csv"
echo "WARNING! This experiment relies on public APIs. Responses from previous execution are cached in folder .simple-cache."
echo "Using DBpedia Spotlight: https://api.dbpedia-spotlight.org/rest/annotate"
echo "Using DBpedia SPARQL Endpoint: http://dbpedia.org/sparql"
#mvn --quiet exec:exec@experiment-gs -DexId=hybrid-unfiltered -DexProp=../data/experiments/hybrid-unfiltered.properties
echo "Results in ../data/experiments/hybrid-unfiltered-gs.output.csv"
echo "=========================================================================="
echo ""
echo ""
sleep 3
######
echo ""
echo "=========================================================================="
echo "Running Experiment: Baseline / Hybrid"
echo "Using Dictionary: ../data/embeddings-dictionary-music.csv"
echo "WARNING! This experiment relies on public APIs. Responses from previous execution are cached in folder .simple-cache."
echo "Using DBpedia Spotlight: https://api.dbpedia-spotlight.org/en/annotate"
echo "Using DBpedia SPARQL Endpoint: http://dbpedia.org/sparql"
#mvn --quiet exec:exec@experiment-gs -DexId=hybrid -DexProp=../data/experiments/hybrid.properties
echo "Results in ../data/experiments/hybrid-gs.output.csv"
echo "=========================================================================="
echo ""
echo ""
echo "Analysis of results"
mvn --quiet exec:exec@analyse-results -Doutput=experiments-result.csv -Dexperiments=statistical-tfidf-gs,random-forest-gs,embeddings-gs,entities-music-gs,embeddings-filtered-gs,hybrid-unfiltered-gs,hybrid-gs
echo "see also ../data/experiments/experiments-result.csv"
echo "Finished."
