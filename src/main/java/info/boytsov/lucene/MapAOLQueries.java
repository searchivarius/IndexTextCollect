/**
 *
 * This code is released under the
 * Apache License Version 2.0 http://www.apache.org/licenses/.
 *  
 */
package info.boytsov.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.BitSet;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.benchmark.byTask.utils.StreamUtils;
import org.apache.lucene.index.*;
import org.apache.lucene.store.FSDirectory;

/**
 * 
 * A simple utility to map queries in the AOL log format to
 * numeric representation. In this representation, each word
 * is replaced by a number. Additionally, we randomly re-hash
 * each query making it hard to link transformed queries to 
 * originals.
 * 
 * We use the index which was previously created by the utility 
 * CreateIndex. One needs to use the same values
 * for minTermFreq and maxTermQty as for the utility DumpIndex. 
 *  
 * @author Leonid Boytsov
 */
public class MapAOLQueries {
  final public static String FIELD_NAME = "body";
  private static FileOutputStream outWriter;

  public static void main(String[] args) {
    if (args.length < 5 || args.length > 8) {
      printUsage();
      System.exit(1);      
    }
    
    boolean DEBUG = false;
    
    String srcDirName = args[0];
    String srcFileName = args[1];
    String dstFileName = args[2];
    
    // using the same default value, so that we get consistent results
    int minTermFreq = DumpIndex.MIN_TERM_FREQ;    
    int maxTermQty  = DumpIndex.MAX_TERM_QTY; 
    int optArg = 3;
    int minQuerySize = 2;
    
    if (args.length >= 4 && !args[3].startsWith("-")) {
      minTermFreq = Integer.parseInt(args[3]);
      optArg++;

      // using the same default value, so that we get consistent results
      if (args.length >= 5 && !args[4].startsWith("-")) { 
        maxTermQty = Integer.parseInt(args[4]);
        optArg++;
      }

    }
    

    
    int       sampleQty = -1;
    boolean   ignoreSessionDuplicates = true;
    
    for (int i = optArg; i < args.length; ++i) {
      if (args[i].equals("-permit_sess_duppl")) {
        ignoreSessionDuplicates = true;
      } else if (args[i].equals("-sample_qty")) {
        sampleQty = Integer.parseInt(args[++i]);
      } else if (args[i].equals("-min_query_size")){
        minQuerySize = Integer.parseInt(args[++i]);
      } else {
        System.err.println("Wrong param: " + args[i]);
        printUsage();
        System.exit(1);
      }
    }

    
    System.out.println("Source dir: "    + srcDirName +
                       " log file: "     + srcFileName +
                       " target dir: " + dstFileName);
    System.out.println("Min term freq: " + minTermFreq + 
                       " Max # of terms: " + maxTermQty);
    System.out.println("SampleQty:" + sampleQty);
    System.out.println("Ignore duplicates within a session: " +
                        ignoreSessionDuplicates);
    
    try {
      IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(srcDirName)));
      
      FreqWordDict  dict = new FreqWordDict(reader, FIELD_NAME,
                                            minTermFreq, maxTermQty);
      
      if (DEBUG) {
        Iterator<Entry<TermDesc, Integer>> iter = dict.getTermIterator();

        int termId = 0;
        
        while (iter.hasNext()) {
          Entry<TermDesc, Integer> e = iter.next();
        
          TermDesc ts = e.getKey();
          System.out.println(termId + ":" + ts.getText());
          ++termId;
        }
      }

      File srcFile = new File(srcFileName);

      // supports either gzip, bzip2, or regular text file, 
      // detects type by extension
      InputStream inputStream = StreamUtils.inputStream(srcFile);
 
      BufferedReader logReader = new BufferedReader(new 
                      InputStreamReader(inputStream), StreamUtils.BUFFER_SIZE);
      
      
      String line;
      String prevSessID = "";
      
      HashSet<String> sessQueries = new HashSet<String>();
            
      ArrayList<String> unparsedQueries = new ArrayList<String>();
      
      CharArraySet stopWords = StopAnalyzer.ENGLISH_STOP_WORDS_SET;
      
      while ((line = logReader.readLine()) != null) {
        String parts[] = line.split("\t");
        
        String sessID = parts[0];
        // Ignore the header line
        if (sessID.equals("AnonID")) continue;
        
        String q = parts[1];        
        
        if (!sessID.equals(prevSessID)) {
          prevSessID = sessID;
          sessQueries.clear();
        }
        
        if (ignoreSessionDuplicates && sessQueries.contains(q)) continue;
        
        if (ignoreSessionDuplicates) sessQueries.add(q);
      
        unparsedQueries.add(q);
      }
      
      logReader.close();
      
      if (sampleQty == -1) 
        sampleQty = unparsedQueries.size();
      
      sampleQty = Math.min(sampleQty, unparsedQueries.size());

      int unusedQty = unparsedQueries.size();      
      BitSet usedQueries = new BitSet(unusedQty);      

      outWriter = new FileOutputStream(new File(dstFileName));

      while (sampleQty > 0 && unusedQty > 0 && sampleQty <= unusedQty) {
        int qr = (int)Math.floor(Math.random() * unusedQty);
        int qn = 0;
        
        for (int i = 0; i < usedQueries.cardinality(); ++i) {         
          if (usedQueries.get(i)) {
            ++qn;
            continue;
          }
          if (--qr < 0) { qn = i; break; }
        }
          
        if (usedQueries.get(qn)) {
          throw new Exception("Bug: selected an already used query!");
        }
        
        String q = unparsedQueries.get(qn);
        
        String queryParts[] = q.split("\\s+");
        
        String  res = "";
        int     querySize = 0;
        
        for (String s: queryParts) {
          // We need to ignore stop words, but not the original queries
          if (stopWords.contains(s)) {
            continue;
          } else {
            ++querySize;
          }

          Integer pos = dict.getTermPos(s);
          if (pos == null) {res = ""; break;}
          String posStr = DEBUG ? s + ":" + pos : pos.toString();
          res = res.isEmpty() ? posStr : res + " " + posStr; 
        }
        
        if (!res.isEmpty() && querySize >= minQuerySize) {
          outWriter.write(res.getBytes());
          outWriter.write('\n');
          sampleQty--;
        }

        usedQueries.set(qn);
        unusedQty--;
      }
      
      if (sampleQty > 0) {
        throw new Exception("Failed to obtained a required number of queries, "
                            + sampleQty + " are not found." +
                            " Please, ask for fewer queries to be converted."); 
      }
      
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
    
  }

  private static void printUsage() {
    System.out.println("mvn exec:java " + 
                       " -Dexec.mainClass=info.boytsov.lucene.MapAOLQueries " +
                       " -Dexec.args=\"" +  
                       " <index dir> <input log file> <output file> " + 
                       " <min term frequency> <optional: max # of terms>" +
                       " -permit_sess_duppl" +
                       " -sample_qty <# of queries to sample> " +
                       " -min_query_size <min # of words in a query> " +
                       "\"");
  }  
  
  
}
