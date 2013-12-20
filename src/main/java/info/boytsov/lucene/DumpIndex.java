/**
 *
 * This code is released under the
 * Apache License Version 2.0 http://www.apache.org/licenses/.
 *  
 */
package info.boytsov.lucene;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.store.FSDirectory;

/**
 * 
 * A simple utility to dump posting lists from the Lucene index,
 * which was previously created by the utility CreateIndex.
 * 
 *  We process only the field "body". 
 *  For each keyword, we first store the number of postings. 
 *  Next, we store the postings (each using 4 bytes). 
 *  
 * @author Leonid Boytsov
 */
public class DumpIndex {
  final public static String FIELD_NAME = "body";
  final public static int MIN_TERM_FREQ = 3;
  final public static int MAX_TERM_QTY = 10000000; 

  public static void main(String[] args) {
    if (args.length < 3 || args.length > 8) {
      printUsage();
      System.exit(1);      
    }
    boolean sortByURL = Integer.parseInt(args[0]) != 0;
    
    String srcDirName = args[1];
    String dstFileName = args[2];
    
    int minTermFreq = MIN_TERM_FREQ;
    
    if (args.length >= 4) minTermFreq = Integer.parseInt(args[3]);
    
    int maxTermQty  = MAX_TERM_QTY; 
    
    if (args.length >= 5) maxTermQty = Integer.parseInt(args[4]);
    
    System.out.println("Source dir: "    + srcDirName + 
                       " target dir: " + dstFileName);
    System.out.println("Min term freq: " + minTermFreq + 
                       " Max # of terms: " + maxTermQty);
    
    
    try {
      IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(srcDirName)));
      
      int docQty = reader.maxDoc();
      int sortTable[] = new int[docQty];
      
      Arrays.fill(sortTable, -1);
      
      if (sortByURL) {
        System.out.println("Re-sorting documents by URL!");

        URL2DocID remap[] = new URL2DocID[docQty];
        
        for (int docID = 0; docID < docQty; ++docID) {
          Document doc = reader.document(docID);
          String url = doc.get("url");
          remap[docID] = new URL2DocID(url, docID);
          if (docID % 100000 == 0) {
            System.out.println("Collected " + (docID + 1) + " URLs for re-sorting");
          }
        }
        
        Arrays.sort(remap);
        
        System.out.println("Collected and sorted all URLs for resoring, " + 
                           "filling out the sort table.");
                        
        for (int newDocID = 0; newDocID < docQty; ++newDocID) {
          sortTable[remap[newDocID].docID] = newDocID;
        }
        
        System.out.println("Sort table is filled up!");

        for (int i = 0; i < docQty; ++i) remap[i] = null;
        remap = null;        
        System.gc(); // Let's try to free some memory
        
        /*
         *  Paranoid check: did we change all the -1 to non-negative numbers.
         *  Turned out, it wasn't that paranoid. You may have repeating URLs.
         *  Then, some elements in sortTable remain unset.
         */
        for (int i = 0; i < sortTable.length; ++i) {
          if (sortTable[i] == -1) {
            throw new Exception("Bug: element " + i + " in sort table is not set");
          }
        }
      } else {
        System.out.println("Keeping the original document order!");
        
        for (int i = 0; i < sortTable.length; ++i) {
          sortTable[i] = i; // Identity transformation
        }
      }

      FreqWordDict  dict = new FreqWordDict(reader, FIELD_NAME,
                                            minTermFreq, maxTermQty);

      File dstFile = new File(dstFileName);
      
      FileOutputStream outData = new FileOutputStream(dstFile);
      
      Iterator<Entry<TermDesc, Integer>> iter = dict.getTermIterator();
      
      long totalWritten = 0;
      long totalInts = 0;
      
      int termId = 0;
      
      while (iter.hasNext()) {
        Entry<TermDesc, Integer> e = iter.next();
      
        TermDesc ts = e.getKey();
        /*
        System.out.println(e.getValue() + 
                           "@@" + dict.getTermPos(ts.getText()) 
                           + " ---> " + ts);
        */
        DocsEnum docIter = dict.getDocIterator(ts.text);
        
        int postQty = ts.freq;
        
        ByteBuffer buffer = ByteBuffer.allocate(4 * (1 + postQty));        
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        buffer.putInt(postQty);
        
        int tmpDocId[] = new int[postQty];
        int qty = 0, prevDocID = -1;
        
        for (int i = 0; 
             docIter.nextDoc() != DocIdSetIterator.NO_MORE_DOCS; 
             ++i, ++qty) {
          if (i >= postQty) {
            throw new Exception("Bug: more postings than expected for term: "
                                + ts.getText());
          }
          int currDocID = docIter.docID();
          if (currDocID >= docQty) {
            throw new Exception("Bug: a document ID " + currDocID + 
                                " is out of bounds, total # of docs: " + docQty);  
          }
          tmpDocId[i] = sortTable[currDocID];
          if (prevDocID >= docIter.docID()) {
            throw new Exception("Bug: unsorted doc ids for term: " 
                                + ts.getText());  
          }
          prevDocID = currDocID;
        }
        if (qty != postQty) {
          throw new Exception("Bug: fewer postings than expected for term: " 
                              + ts.getText());
        }
        // Now let's resort docIds and write them
        Arrays.sort(tmpDocId);
        for (int docId : tmpDocId) buffer.putInt(docId);
        // Finally, we can write the buffer!
        outData.write(buffer.array());
        totalWritten += buffer.array().length;
        totalInts += postQty;
        System.out.println(termId + ":" + ts.getText() + " \t postQty=" + postQty + 
                           " overall written: " + totalWritten/1e6 + " Mbs " +
                            totalInts/1e6 + " Millions of Ints");
        ++termId;
      }     
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
    
  }

  private static void printUsage() {
    System.out.println("mvn exec:java " + 
                       " -Dexec.mainClass=info.boytsov.lucene.DumpIndex " +
                       " -Dexec.args=\"" + 
                       " <sort by URL: 1:0>" + 
                       " <index dir> <output file>" + 
                       " <optional: min term frequency> <optional: max # of terms>\"");
  }  
  
  
}
