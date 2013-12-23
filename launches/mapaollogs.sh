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
minQuerySize=""
if [ "$7" != "" ] ; then
  minQuerySize=" -min_query_size $7 "
fi
debug=""
if [ "$8" == "1" ] ; then
  debug="-debug"
fi
mvn exec:java -Dexec.args="$src $log $dst $minTermFreq $maxTermQty -sample_qty $sampleQty $minQuerySize $debug" -Dexec.mainClass=info.boytsov.lucene.MapAOLQueries

