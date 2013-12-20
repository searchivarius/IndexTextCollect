/**
*
* This code is released under the
* Apache License Version 2.0 http://www.apache.org/licenses/.
*  
*/
package info.boytsov.lucene;

import org.apache.lucene.util.BytesRef;

public final class TermDesc implements Comparable<TermDesc> {
  public String     field;
  public int        termId;
  public BytesRef   text;
  public int        freq;

  TermDesc(String field, int termId, BytesRef text, int df) {
    this.field  = field;
    this.termId = termId;    
    this.text   = new BytesRef();
    this.text.copyBytes(text);    
    this.freq  = df;
  }

  String getText() {
    return text.utf8ToString();
  }

  public String toString() {
    return field + ":" + getText() + " freq=" + freq + " termId=" + termId;
  }

  @Override
  public int compareTo(TermDesc o) {
    if (freq < o.freq) return 1;
    if (freq > o.freq) return -1;
    if (termId < o.termId) return -1;
    if (termId > o.termId) return 1;    
    return 0;
  }
}    
