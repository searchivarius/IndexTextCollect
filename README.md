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

Indexing:
launches/index.sh "[type: Wikipedia, trec:GOV2, trec:ClueWeb09] [location of files (a single file for Wikipedia)] [output directory for the index]"  

Dumping contents:
launches/dupm.sh [sort by url: 0,1] [index directory] [output file] [min # of documents] [max # of terms] 

If one specifies [min # of documents], we output only terms that appear at least the certain number of times.  It is also possible to limit the overall number of dumped terms. We will select most frequently occurring terms.


Postings will be written sequentially. Each posting has the format:
[N : #of postings] [docId1] [docId2] ... [docIdN]
each entry occupies 4 bytes and is stored using the little Endian format.


