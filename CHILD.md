# led-discovery / CHILD


## Generate dictionaries from the word2vec model

Create/Activate venv .pyspark-venv:
```
source .pyspark-venv/bin/activate
```
install pyspark, numpy

Generate dictionaries from the word2vec model: data/le-components/dictionary.py

$ cd data/le-components/
$ bash ../../pspark.sh
>>> exec(open('dictionary.py').read())
>>> dictionary(Child[0],Child[1],5000,"dictionary-child4.csv")
dictionary(["music[n]","teach[v]"],[],10000,"dictionary-music-teach.csv")
dictionary(["music[n]","learn[v]","child[n]","lesson[n]","instrument[n]"],[],10000,"dictionary-child-many.csv")

```
spark-submit dictionary.py
```

TODO: handle terms that appear multiple times in joint dictioanries

Plan:

- More to be understood on the way multiple dictionaries can work together
- Look into examples Helen's selected from LED and see why they don't show up (Autobiography And Correspondence Of Mary Granville)
- Tune the threshold

## Build the FindLEr app
```
$ cd runnable/app
$ mvn install -Pbuild-webapp
```


## Next:

- Rebuild the child+music dictionary
- Develop a multi-entity component
- Possible DBpedia subjects: Music, Toy, Education, Game, School

