#!/bin/bash
sortByURL="$1"
src="$2"
dst="$3"
minTermFreq="$4"
maxTermQty="$5"
export MAVEN_OPTS="-Xmx16000m -Xms8000m"
mvn exec:java -Dexec.args="$sortByURL $src $dst $minTermFreq $maxTermQty" -Dexec.mainClass=info.boytsov.lucene.DumpIndex

