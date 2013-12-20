#!/bin/bash
src="$1"
log="$2"
dst="$3"
minTermFreq="$4"
maxTermQty="$5"
sampleQty=1000
if [ "$6" != "" ] ; then
  sampleQty="$6"
fi
debug=""
if [ "$7" == "1" ] ; then
  debug="-debug"
fi
mvn exec:java -Dexec.args="$src $log $dst $minTermFreq $maxTermQty -sample_qty $sampleQty $debug" -Dexec.mainClass=info.boytsov.lucene.MapAOLQueries

