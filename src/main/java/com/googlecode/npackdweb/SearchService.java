package com.googlecode.npackdweb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.sortedset.DefaultSortedSetDocValuesReaderState;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetCounts;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesReaderState;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.IOUtils;

/**
 * Search.
 */
public class SearchService {

    private static SearchService instance;

    /**
     * @return the only instance of this class
     */
    public static SearchService getInstance() {
        if (instance == null) {
            instance = new SearchService();
            try {
                instance.init();
            } catch (IOException ex) {
                NWUtils.LOG.log(Level.SEVERE, null, ex);
            }
        }

        return instance;
    }

    private IndexSearcher isearcher;
    private DirectoryReader ireader;
    private Directory directory;
    private Path indexPath;
    private Analyzer analyzer;
    private IndexWriter iwriter;

    private void init() throws IOException {
        analyzer = new StandardAnalyzer();

        Path path = Paths.get("/var/lib/npackd-web/index");
        indexPath = Files.createDirectory(path);
        directory = FSDirectory.open(indexPath);
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        iwriter = new IndexWriter(directory, config);
        iwriter.close();

        ireader = DirectoryReader.open(directory);
        isearcher = new IndexSearcher(ireader);
    }

    /**
     * Adds or updates a document.
     *
     * @param doc a document
     */
    public void addDocument(Document doc) {
        try {
            /*Document doc = new Document();
            String text = "This is the text to be indexed.";
            doc.add(new Field("fieldname", text, TextField.TYPE_STORED));*/
            iwriter.addDocument(doc);
        } catch (IOException ex) {
            throw new InternalError(ex);
        }
    }

    /**
     * Closes the index.
     *
     * @throws IOException something goes wrong
     */
    public void done() throws IOException {
        ireader.close();
        directory.close();
        IOUtils.rm(indexPath);
    }

    /**
     * Searches in the index.
     *
     * @param query query
     * @param start starting offset (0, 1, ...)
     * @param pageSize page size
     * @param sort how to sort
     * @return found documents
     * @throws ParseException error while parsing the query.
     * @throws java.io.IOException I/O error
     */
    public TopDocs search(Query query, int start, int pageSize, Sort sort)
            throws
            ParseException,
            IOException {
        TopScoreDocCollector collector = TopScoreDocCollector.create(10000,
                10000);
        isearcher.search(query, collector);
        return collector.topDocs(start, pageSize);
    }

    /**
     * Gets a document from the index.
     *
     * @param doc document index
     * @return the document
     * @throws IOException I/O error
     */
    public Document getDocument(int doc) throws IOException {
        return isearcher.doc(doc);
    }

    /**
     * Searches for the facet values
     *
     * @param query a query
     * @param fields facet fields
     * @return facets
     * @throws IOException I/O error
     */
    public List<FacetResult> getFacets(Query query, final List<String> fields)
            throws
            IOException {
        FacetsCollector fc = new FacetsCollector();
        FacetsCollector.search(isearcher, query, 10, fc);

        List<FacetResult> res = new ArrayList<>();
        for (String field : fields) {
            SortedSetDocValuesReaderState state =
                    new DefaultSortedSetDocValuesReaderState(ireader, "facet_" +
                            field);
            Facets facets = new SortedSetDocValuesFacetCounts(state, fc);
            res.add(facets.getTopChildren(100, field));
        }
        return res;
    }

    /**
     * Deletes a document.
     *
     * @param id ID of the document
     */
    public void delete(String id) {
        try {
            iwriter.deleteDocuments(new Term("id", id));
        } catch (IOException ex) {
            throw new InternalError(ex);
        }
    }
}
