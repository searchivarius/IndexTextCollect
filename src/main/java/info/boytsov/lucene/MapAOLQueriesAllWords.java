/**
 *
 * This code is released under the
 * Apache License Version 2.0 http://www.apache.org/licenses/.
 *  
 */
package info.boytsov.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.benchmark.byTask.utils.StreamUtils;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * 
 * We use the index which was previously created by the utility 
 * CreateIndex.  
 *  
 * @author Leonid Boytsov
 */
public class MapAOLQueriesAllWords {
  final public static String FIELD_NAME = "body";

  public static void main(String[] args) {
    if (args.length < 3) {
      printUsage();
      System.exit(1);      
    }
    
    String srcDirName = args[0];
    String srcFileName = args[1];
     
    int optArg = 2;
    int minQuerySize = 1;
    
     boolean   ignoreSessionDuplicates = true;
    
    for (int i = optArg; i < args.length; ++i) {
      if (args[i].equals("-permit_sess_duppl")) {
        ignoreSessionDuplicates = true;
      } else if (args[i].equals("-min_query_size")){
        minQuerySize = Integer.parseInt(args[++i]);
      } else {
        System.err.println("Wrong param: " + args[i]);
        printUsage();
        System.exit(1);
      }
    }

    
    System.out.println("Source dir: "    + srcDirName +
                       " log file: "     + srcFileName);
    System.out.println(" Min query size: " + minQuerySize);

    System.out.println("Ignore duplicates within a session: " +
                        ignoreSessionDuplicates);
    
    try {
      IndexReader              reader = 
                  DirectoryReader.open(FSDirectory.open(new File(srcDirName)));
      IndexSearcher     searcher = new IndexSearcher(reader);
      StandardAnalyzer  analyzer = new StandardAnalyzer(Version.LUCENE_46);
      QueryParser       qParser = new QueryParser(Version.LUCENE_46, 
                                                        FIELD_NAME, analyzer);
      
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
      

      int tooShortQty = 0, missQty = 0;

      int num = 0;
      for (String q: unparsedQueries) {
        ++num;
        String queryParts[] = q.split("\\s+");
        
        boolean bOk = true;
        int     querySize = 0;
        
        for (String s: queryParts) {
          // We need to ignore stop words, but not the original queries
          if (stopWords.contains(s)) {
            continue;
          } else {
            ++querySize;
          }

          Query luceneQuery = qParser.parse(QueryParserUtil.escape(s));
          
          TopDocs td = searcher.search(luceneQuery, 1);
          
          if (td.totalHits == 0) {
            System.out.println("Missing: " + s + " query num: " + num);
            bOk = false;
            break;
          } 
        }
        
        if (querySize < minQuerySize) {
          ++tooShortQty;
        } else if (!bOk) {
          missQty++;
        }
      }
      
      int usableQty = (unparsedQueries.size() - tooShortQty);
      System.out.println("totalQty - tooShortQty = " +  usableQty + 
                         " missQty = " + missQty + 
                         " tooShortQty = " + tooShortQty + 
                         " fraction of excluded: " + 
                         Math.round(missQty/(float)usableQty * 1e4)/1e4);
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
                       " <index dir> <input log file> " + 
                       " <min term frequency> <optional: max # of terms>" +
                       " -permit_sess_duppl" +
                       " -min_query_size <min # of words in a query>" +
                       "\"");
  }  
  
  
}
