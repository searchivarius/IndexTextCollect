/**
 *
 * This code is released under the
 * Apache License Version 2.0 http://www.apache.org/licenses/.
 *  
 */
package info.boytsov.lucene;

import java.io.File;
import java.util.Arrays;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

public class CheckSort {
  public static void main(String[] args) {
    if (args.length != 2) {
      printUsage();
      System.exit(1);      
    }
    int dir = 1;

    String srcDirName = args[0];
    System.out.println("Source dir: "    + srcDirName);
    if (args[1].equals("forward")) dir = 1; 
    else if (args[1].equals("backward")) dir = -1;
    else {
      System.err.println("Invalid direction: " + args[1]);
      printUsage();
      System.exit(1);
    }
    
    try {
      IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(srcDirName)));
      
      int docQty = reader.maxDoc();
      int sortTable[] = new int[docQty];

      
      Arrays.fill(sortTable, -1);
      
      int sortedQty = 0;
      
      double sortedStreak = 0;
      int    sortedStreakQty = 0;
      
      URL2DocID remap[] = new URL2DocID[docQty];
      
      String prevURL = "";
      
      int prevSorted = 0;
      
      for (int docID = 0; docID < docQty; ++docID) {
        Document doc = reader.document(docID);
        String url = doc.get("url");
        if (dir > 0) {
          remap[docID] = new URL2DocID(url, docID);
        } else {
          remap[docQty - 1 - docID] = new URL2DocID(url, docID);
        }
        if (docID % 100000 == 0) {
          System.out.println("Collected " + (docID + 1) + 
             " URLs, sorted so far, direct " + sortedQty + 
             " avg. sorted streak QTY: " + (sortedStreak/sortedStreakQty) + 
             " sortedStreakQty: " + sortedStreakQty);
        }
        // Assuming the increasing order
        if (dir * url.compareTo(prevURL) >= 0) {
          ++sortedQty;
        } else {
          sortedStreak += docID - prevSorted - 1;
          sortedStreakQty++;

          prevSorted = docID;
        }
        prevURL = url;
      }
      
      System.out.println("Collected " + docQty + 
          " URLs, sorted so far, direct " + sortedQty + 
          " avg. sorted streak QTY: " + (sortedStreak/sortedStreakQty) + 
          " sortedStreakQty: " + sortedStreakQty);
     
      double invQty = Inversions.count(remap);
      System.out.println("A total number of inversions: " + invQty + 
                         " relative to n*(n-1)/2: " + 
                          (invQty * 2.0 / docQty / (docQty + 1)));

    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
    
  }


  private static void printUsage() {
    System.out.println("mvn exec:java " + 
                       " -Dexec.mainClass=info.boytsov.lucene.CheckSort " +
                       " -Dexec.args=\"" +  
                       " <index dir> <direction forward:backward>\"");
  }  

}
