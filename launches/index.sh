#!/bin/bash
type="$1"
src="$2"
dst="$3"
commitInterval=$4
mvn exec:java -Dexec.args="$type $src $dst" -Dexec.mainClass=info.boytsov.lucene.CreateIndex

