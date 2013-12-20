package info.boytsov.lucene;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
/**
*
* This code is released under the
* Apache License Version 2.0 http://www.apache.org/licenses/.
* 
*/
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

/**
 * A helper class to retrieve the list of common terms.
 * 
 * @author Leonid Boytsov
 */

public class FreqWordDict {
  private Terms                       terms;  
  private TreeMap<TermDesc, Integer>  termDescPos;
  private TreeMap<BytesRef, Integer>  termTextPos;
  
  public FreqWordDict(IndexReader reader, String fieldName,
                      int minTermFreq, int maxTermQty) 
                      throws Exception {
    
    Fields fields = MultiFields.getFields(reader);    
    terms = fields.terms(fieldName);
    
    TreeSet<TermDesc> tmpTerms = new TreeSet<TermDesc>();
    
    TermsEnum termIter = terms.iterator(null);

    for (int termId = 0; termIter.next() != null; ++termId) {
      if (termIter.docFreq() >= minTermFreq) {
        TermDesc ts = new TermDesc(fieldName, 
                                   termId,
                                   termIter.term(), 
                                   termIter.docFreq());
        
        tmpTerms.add(ts);
      }
    }
    
    termDescPos = new TreeMap<TermDesc, Integer>();
    termTextPos = new TreeMap<BytesRef, Integer>();
    
    int pos = 0;
    for (TermDesc ts: tmpTerms) {
      termDescPos.put(ts, pos);
      if (termTextPos.containsKey(ts.text)) {
        throw new Exception("Bug: the key '" + ts.getText() + 
                            "' is already in the map!");
      }
      termTextPos.put(ts.text, pos);      
      if (++pos >= maxTermQty) break;
    }
  }
  
  public Iterator<Entry<TermDesc, Integer>> getTermIterator() { 
    return termDescPos.entrySet().iterator();
  }
  
  // This function accepts non-encoded term
  public Integer getTermPos(String term) {
    BytesRef text = new BytesRef(term.getBytes());
    
    if (termTextPos.containsKey(text)) {
      return new Integer(termTextPos.get(text));
    }
    
    return null;
  }
  
  public DocsEnum getDocIterator(BytesRef text) throws IOException {
    TermsEnum termIter = terms.iterator(null);

    if (!termIter.seekExact(text)) {
      return null;
    }
    
    return termIter.docs(null, null);    
  }
  
  public int getTermCount() { return termDescPos.size(); } 
}
