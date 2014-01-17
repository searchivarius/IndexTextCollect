IndexTextCollect
==============

A simple utility to index several important text collections using Lucene and extract postings (if necessary). It was created to produce datasets to test algorithms for fast retrieval: https://github.com/lemire/SIMDCompressionAndIntersection  


Postings are extracted without terms: You can load the postings of the 2d term, but you don't known what the term was. This was done on purpose: TREC collection licenses allow us to post derived data only if it is not possible to restore originals from the derived data. To test the software, we also take query log files and convert them. Namely, we replace terms with posting ids.

It is possible to extract both unmodified postings and postings for documents sorted by URLs. 

Currently supported collections are:

1. Wikipedia (grab a dump of WebPages from https://en.wikipedia.org/wiki/Wikipedia:Database_download#English-language_Wikipedia)
2. Gov2 (http://ir.dcs.gla.ac.uk/test_collections/gov2-summary.htm)
3. ClueWeb09 (http://lemurproject.org/clueweb09/)

Usage:
------------------------

install java (JDK) if needed
install maven if needed 

mvn compile  

Indexing:
------------------------

launches/index.sh "[type: Wikipedia, trec:GOV2, trec:ClueWeb09] [location of files (a single file for Wikipedia)] [output directory for the index]"  

Dumping contents:
------------------------

launches/dump.sh [sort by url: 0,1] [index directory] [output file] [min # of documents] [max # of terms] 

If one specifies [min # of documents], we output only terms that appear at least the certain number of times.  It is also possible to limit the overall number of dumped terms. If you specify, say, one million as a parameter value for [max # of terms], software selects only one million most frequently occurring terms.

Postings will be written sequentially. Each posting has the format:
[N : #of postings] [docId1] [docId2] ... [docIdN]
each entry occupies 4 bytes and is stored using the little Endian format.

NOTE that dumping is a very slow process, which is IO-bound. Be prepared that it takes much longer than indexing.


Mapping logs:
------------------------

This was originally created to map AOL logs as follows:

launches/mapaollogs.sh [index directory] [log file] [output file] [min # of documents] [max # of terms]  [optional max # of queries to process]

You can also supply some other log file given that it has the following format:

[session id] [tab] [space-separated query words]

In particular, we also used 1 Million TREC query files.



Checking the "sortedness" of the collection
--------------------------

Some collections (in particular, ClueWeb09) are partially sorted. To collected "sortedness" statistics you can do:

launches/checksort.sh [index directory] [forward|backward]

The program will print the overall number of inversions (also relative to n*(n-1)/2) and the average length of the sorted run.
