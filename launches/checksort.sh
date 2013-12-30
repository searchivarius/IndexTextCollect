#!/bin/bash
src="$1"
dir="$2"
export MAVEN_OPTS="-Xmx16000m -Xms15000m"
mvn exec:java -Dexec.args="$src $dir" -Dexec.mainClass=info.boytsov.lucene.CheckSort

