IndexTextCollect
==============

A simple utility to index several important text collections using Lucene.
Currently supported collections are:

1. Wikipedia (grab a dump of WebPages from https://en.wikipedia.org/wiki/Wikipedia:Database_download#English-language_Wikipedia)
2. Gov2 (http://ir.dcs.gla.ac.uk/test_collections/gov2-summary.htm)
3. ClueWeb09 (http://lemurproject.org/clueweb09/)


Usage:

install java (JDK) if needed
install maven if needed
mvn compile
mvn exec:java -Dexec.args="<type: Wikipedia, trec:GOV2"> <location of files, e.g., gov2> <output directory for the index>"


