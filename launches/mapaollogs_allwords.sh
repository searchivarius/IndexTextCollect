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
maxQueryQty=""
if [ "$6" != "" ] ; then
  maxQueryQty=" -max_query_qty $6 "
fi
debug=""
if [ "$7" == "1" ] ; then
  echo "Debug=1"
  debug="-debug"
fi
export MAVEN_OPTS="-Xmx12000m -Xms8000m"
mvn exec:java -Dexec.args="$src $log $minTermFreq $maxTermQty $minQuerySize $debug $maxQueryQty" -Dexec.mainClass=info.boytsov.lucene.MapAOLQueriesAllWords

