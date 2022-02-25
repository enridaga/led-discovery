# led-discovery / CHILD


## Generate dictionaries from the word2vec model

Create/Activate venv .pyspark-venv:
```
source .pyspark/bin/activate
```
install pyspark, numpy

Generate dictionaries from the word2vec model: data/le-components/dictionary.py

$ cd data/le-components/
$ bash ../../pspark.sh
>>> exec(open('dictionary.py').read())
>>> dictionary(Child[0],Child[1],5000,"dictionary-child4.csv")

```
spark-submit dictionary.py
```

DBpedia subjects: Music, Toy, Education, Game, School

Next:

- Rebuild the child+music dictionary
- Develop a multi-entity component

