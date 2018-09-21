from pyspark.sql.types import StructType
from pyspark.sql.types import Row
from pyspark.sql.types import StructField
from pyspark.sql.types import StringType
from pyspark.sql.types import FloatType
from pyspark.mllib.feature import Word2VecModel
model = Word2VecModel.load(sc,'/Users/ed4565/Development/led-discovery/data/analysis/gutenberg2vec/word2vec.model')



first=lambda x: x[0]

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
  for primary in primarys:
    primaryd=model.findSynonyms(primary,size)
    primaryt=map(first,primaryd)
    keep=list(set(primaryt) | set(keep))
  for subt in subtract:
    subtd=model.findSynonyms(subt,size)
    subtt=map(first,subtd)
    keep=list(set(keep)-set(subtt)-set([subt]))
  result=filter(lambda x: x[0] in keep, primaryd)
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

def dictionary(term,antiterm,size,output):
 dict=disamb(term,antiterm,size)
 file=open(output,"w")
 for j in dict:
  file.write(j[0].encode('utf8'))
  file.write(",")
  file.write("%s" % j[1])
  file.write("\n")

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

dictionary(Event[0],Event[1],10000,"dictionary-event-1.csv")
dictionary(Music[0],Music[1],10000,"dictionary-music-1.csv")
dictionary(Listener[0],Listener[1],10000,"dictionary-listener-1.csv")
dictionary(Performer[0],Performer[1],10000,"dictionary-performer-1.csv")
dictionary(Experience[0],Experience[1],10000,"dictionary-experience-1.csv")
