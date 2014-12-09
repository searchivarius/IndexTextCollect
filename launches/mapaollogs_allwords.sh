#!/bin/bash
src="$1"
log="$2"
minQuerySize=""
if [ "$3" != "" ] ; then
  minQuerySize=" -min_query_size $3 "
  echo "Min query size: $minQuerySize"
fi
export MAVEN_OPTS="-Xmx12000m -Xms8000m"
mvn exec:java -Dexec.args="$src $log $minTermFreq $maxTermQty $minQuerySize " -Dexec.mainClass=info.boytsov.lucene.MapAOLQueriesAllWords 

