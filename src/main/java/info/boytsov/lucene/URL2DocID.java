package info.boytsov.lucene;
/**
*
* This code is released under the
* Apache License Version 2.0 http://www.apache.org/licenses/.
*  
*/

class URL2DocID implements Comparable<URL2DocID> {
  public URL2DocID(String url, int docID) {
    super();
    this.url = url;
    this.docID = docID;
  }

  public int compareTo(URL2DocID o) {
    return url.compareTo(o.url);
  }

  public String url;
  public int    docID;
}