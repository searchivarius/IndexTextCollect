#!/bin/bash
src="$1"
export MAVEN_OPTS="-Xmx16000m -Xms15000m"
mvn exec:java -Dexec.args="$src" -Dexec.mainClass=info.boytsov.lucene.GetTotPostQty

