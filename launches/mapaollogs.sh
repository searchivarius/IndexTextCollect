#!/bin/bash
src="$1"
log="$2"
minTermFreq="$3"
maxTermQty="$4"
minQuerySize=""
if [ "$5" != "" ] ; then
  minQuerySize=" -min_query_size $5 "
  echo "Min query size: $minQuerySize"
fi
debug=""
if [ "$6" == "1" ] ; then
  echo "Debug=1"
  debug="-debug"
fi
export MAVEN_OPTS="-Xmx12000m -Xms8000m"
#echo mvn exec:java -Dexec.args="$src $log $minTermFreq $maxTermQty $minQuerySize $debug" -Dexec.mainClass=info.boytsov.lucene.MapAOLQueries
mvn exec:java -Dexec.args="$src $log $minTermFreq $maxTermQty $minQuerySize $debug" -Dexec.mainClass=info.boytsov.lucene.MapAOLQueries

