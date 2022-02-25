from pyspark.sql.types import StructType
from pyspark.sql.types import Row
from pyspark.sql.types import StructField
from pyspark.sql.types import StringType
from pyspark.sql import SparkSession
from pyspark.sql.types import FloatType
from pyspark.mllib.feature import Word2VecModel

#s = scSparkSession.builder.appName("SimpleApp").getOrCreate()
#sc = SparkSession.builder.master('local[*]').config("spark.driver.memory", "8g").config("spark.executor.memory", "16g").config("spark.dirver.maxResultSize","8g ").appName('led-dictioanry').getOrCreate()
model = Word2VecModel.load(sc,'/Users/ed4565/Development/led-discovery/data/analysis/gutenberg2vec/word2vec.model')



first=lambda x: x[0]

def dictionaryGetMulti(concepts, size):
    keep=list()
    syns=list()
    for c in concepts:
        syn=model.findSynonyms(c,size/len(concepts))
        terms=map(first,syn)
        keep=list(set(terms) | set(keep))
        syns=list(set(syn) | set(syns))
    result=filter(lambda x: x[0] in keep,syns)
    return result

def disamb0(primary,subtract,size):
  primaryd=model.findSynonyms(primary,size)
  primaryt=map(first,primaryd)
  keep=primaryt
  for subt in subtract:
    subtd=model.findSynonyms(subt,size)
    subtt=map(first,subtd)
    keep=list(set(keep)-set(subtt)-set([subt]))
  result=filter(lambda x: x[0] in keep, primaryd)
  return result

def disamb(primarys,subtract,size):
  keep=list()
  primaryk=list()
  # Load embeddings from primary concepts
  for primary in primarys:
    #print('primary', primary)
    primaryd=list(model.findSynonyms(primary,size))
    #print('primaryd', primaryd[:10])
    primaryk = primaryk + primaryd
    #print('primaryk', primaryk[-10:])
    primaryt = list(map(first,primaryd))
    #print('prymaryt',primaryt[:10])
    # Keep a list of labels, to be used later
    keep=list(set(primaryt) | set(keep))
    #print(keep[:10])
  # Subtract embeddings from anti-concepts
  for subt in subtract:
    subtd=model.findSynonyms(subt,size)
    subtt=map(first,subtd)
    keep=list(set(keep)-set(subtt)-set([subt]))
  # Recover scores from primary concepts
  result=list(filter(lambda x: x[0] in keep, primaryk))
  # Add key concepts
  for primary in primarys:
      result.append((primary, 1.0))
  #print(result[:10])
  return result

def intersect(terms,size):
   ret=[]
   for t in terms:
     if len(ret)==0:
       ret=list(set(map(first,set(model.findSynonyms(t,size)))))
     else:
       ret=list(set(map(first,set(model.findSynonyms(t,size)))) & set(ret))
   return ret

def toCSVLine(data): 
  return ','.join(str(d) for d in data)

def dictionaryMulti(concepts,size,output):
 dict=dictionaryGetMulti(concepts,size)
 file=open(output,"w")
 for j in dict:
  file.write(j[0].encode('utf8'))
  file.write(",")
  file.write("%s" % j[1])
  file.write("\n")

def dictionary(term,antiterm,size,output):
 dict=disamb(term,antiterm,size)
 file=open(output,"wb")
 for j in dict:
  file.write(j[0].encode('utf8'))
  file.write(",".encode('utf8'))
  file.write(str(j[1]).encode('utf8'))
  file.write("\n".encode('utf8'))

Event=[
    set(["event[n]"]),
    set(["case[n]","consequence[n]","effect[n]","outcome[n]","result[n]","issue[n]","upshot[n]"])
]
MusicSimple=[
    set(["music[n]"]),
    set()
]
Music=[
    set(["music[n]","sing[v]"]),
    set(["medicine[n]","punishment[n]"])
]
Listener=[
    set(["listener[n]","hearer[n]"]),set(["auditor[n]","attender[n]"])
]
Performer=[
    set(["performer[n]","artist[n]"]),set()
]
Experience=[
    set(["experience[n]","participation[n]","observation[n]","apprehend[v]","experience[v]","feel[v]"]),
    set(["knowledge[n]","know[v]","live[v]","receive[v]"])
]

Child=[
    set(["music[n]","childhood[n]"]),
    set()
]
#dictionaryMulti(["music[n]","childhood[n]"], 10000, "dictionary-multi-music-childhood.csv")
#dictionary(Child[0],Child[1],5000,"dictionary-child4.csv")
#dictionary(Event[0],Event[1],10000,"dictionary-event-1.csv")
#dictionary(Music[0],Music[1],10000,"dictionary-music-1.csv")
#dictionary(Listener[0],Listener[1],10000,"dictionary-listener-1.csv")
#dictionary(Performer[0],Performer[1],10000,"dictionary-performer-1.csv")
#dictionary(Experience[0],Experience[1],10000,"dictionary-experience-1.csv")
