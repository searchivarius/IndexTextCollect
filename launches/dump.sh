#!/bin/bash
sortByURL="$1"
src="$2"
dst="$3"
minTermFreq="$4"
maxTermQty="$5"
mvn exec:java -Dexec.args="$sortByURL $src $dst $minTermFreq $maxTermQty" -Dexec.mainClass=info.boytsov.lucene.DumpIndex

