/**
 * This code is released under the
 * Apache License Version 2.0 http://www.apache.org/licenses/.
 * 
 * It is based on the Daniel Lemire's code in:
 * https://github.com/lemire/IndexWikipedia
 */
package info.boytsov.lucene;

import java.io.File;
import java.util.Properties;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.benchmark.byTask.feeds.DocMaker;
import org.apache.lucene.benchmark.byTask.feeds.EnwikiContentSource;
import org.apache.lucene.benchmark.byTask.utils.Config;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;


/**
 * A simple utility to index files in different formats using Lucene.
 * Supported formats:
 *    
 *    1) Gov2
 *    2) ClueWeb09
 * 
 * @author Leonid Boytsov
 * @author Daniel Lemire
 * 
 */
public class CreateLuceneIndex {

  public static void main(String[] args) throws Exception {
    if (args.length != 3) {
      printUsage();
      return;
    }
    String indexType = args[0];
    String indexSource = args[1];

    File outputDir = new File(args[2]);
    if (!outputDir.exists()) {
      if (!outputDir.mkdirs()) {
        System.out.println("couldn't create " + outputDir.getAbsolutePath());
        return;
      }
    }
    if (!outputDir.isDirectory()) {
      System.out.println(outputDir.getAbsolutePath() + " is not a directory!");
      return;
    }
    if (!outputDir.canWrite()) {
      System.out.println("Can't write to " + outputDir.getAbsolutePath());
      return;
    }

    // we should be "ok" now

    FSDirectory dir = FSDirectory.open(outputDir);

    StandardAnalyzer analyzer = new StandardAnalyzer(
            Version.LUCENE_46);// default
                               // stop
                               // words
    IndexWriterConfig config = new IndexWriterConfig(
            Version.LUCENE_46, analyzer);
    config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);// overwrites
                                                          // if
                                                          // needed
    IndexWriter indexWriter = new IndexWriter(dir, config);

    DocMaker docMaker = new DocMaker();
    Properties properties = new Properties();
    properties.setProperty("content.source.forever", "false"); // will
                                                               // parse
                                                               // each
                                                               // document
                                                               // only
                                                               // once
/*  
    
    File wikipediafile = new File(args[1]);
    if (!wikipediafile.exists()) {
      System.out.println("Can't find " + wikipediafile.getAbsolutePath());
      return;
    }
    if (!wikipediafile.canRead()) {
      System.out.println("Can't read " + wikipediafile.getAbsolutePath());
      return;
    }
   
    properties.setProperty("docs.file", wikipediafile.getAbsolutePath());
    properties.setProperty("keep.image.only.docs", "false");

    new EnwikiContentSource();
*/    
    EnwikiContentSource source = CreateSource(indexType,
                                              indexSource,
                                              properties);
    
    Config c = new Config(properties);    
    source.setConfig(c);
    source.resetInputs();// though this does not seem needed, it is
                         // (gets the file opened?)
    docMaker.setConfig(c, source);
    int count = 0;
    System.out.println("Starting Indexing of " + indexType + " source " + indexSource);
    
    long start = System.currentTimeMillis();
    Document doc;
    try {
      while ((doc = docMaker.makeDocument()) != null) {
        indexWriter.addDocument(doc);
        ++count;
        if (count % 1000 == 0)
          System.out.println("Indexed " + count + " documents in "
                              + (System.currentTimeMillis() - start) + " ms");
      }
    } catch (org.apache.lucene.benchmark.byTask.feeds.NoMoreDataException nmd) {
            nmd.printStackTrace();
    }
    long finish = System.currentTimeMillis();
    System.out.println("Indexing " + count + " documents took "
            + (finish - start) + " ms");
    System.out.println("Total data processed: "
            + source.getTotalBytesCount() + " bytes");
    System.out.println("Index should be located at "
            + dir.getDirectory().getAbsolutePath());
    docMaker.close();
    indexWriter.close();
  }

  private static void printUsage() {
    System.out.println("Usage: java -cp <...> " + 
                       "info.boytsov.lucene.CreateIndex " + 
                       "<index type> <input dir/file> <output dir>");
  }
}
