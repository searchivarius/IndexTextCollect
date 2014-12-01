/**
 *
 * This code is released under the
 * Apache License Version 2.0 http://www.apache.org/licenses/.
 *  
 */
package info.boytsov.lucene;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.store.FSDirectory;

import me.lemire.lucene.IntArray;

/**
 * 
 * A simple utility to compute a total number of posting lists.  
 *  
 * @author Leonid Boytsov
 */
public class GetTotPostQty {
  final public static String FIELD_NAME = "body";

  public static void main(String[] args) {
    if (args.length != 1) {
      printUsage();
      System.exit(1);      
    }
    String srcDirName = args[0];
    
    System.out.println("Source dir: "    + srcDirName);
    
    
    try {
      IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(srcDirName)));
      
      int docQty = reader.maxDoc();
      
      Fields fields = MultiFields.getFields(reader);    
      Terms  terms = fields.terms(FIELD_NAME);
      
      long totalInts = 0;
      int  termQty = 0;
      
      for (TermsEnum termIter = terms.iterator(null); termIter.next() != null; ) {
        totalInts += termIter.docFreq();
        //System.out.println(termQty + " -> " + termIter.docFreq());
        ++termQty;
        if (termQty % 1000000 == 0) System.out.println("Read " + termQty + " dictionary terms");
      }

      System.out.println(
          "Term qty: " + termQty +
          "Doc qty: " + docQty +
          " postings qty: " +
           totalInts);

    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
    
  }

  private static void printUsage() {
    System.out.println("mvn exec:java " + 
                       " -Dexec.mainClass=info.boytsov.lucene.GetTotPostQty " +
                       " -Dexec.args=\"<index dir>\"");
  }  
  
  
}
