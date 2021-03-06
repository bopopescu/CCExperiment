package org.apache.lucene.swing.models;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
public class ListSearcher extends AbstractListModel {
    private ListModel listModel;
    private ArrayList<Integer> rowToModelIndex = new ArrayList<Integer>();
    private RAMDirectory directory;
    private Analyzer analyzer;
    private static final String ROW_NUMBER = "ROW_NUMBER";
    private static final String FIELD_NAME = "FIELD_NAME";
    private String searchString = null;
    private ListDataListener listModelListener;
    public ListSearcher(ListModel newModel) {
        analyzer = new WhitespaceAnalyzer(Version.LUCENE_CURRENT);
        setListModel(newModel);
        listModelListener = new ListModelHandler();
        newModel.addListDataListener(listModelListener);
        clearSearchingState();
    }
    private void setListModel(ListModel newModel) {
        if (newModel != null) {
            newModel.removeListDataListener(listModelListener);
        }
        listModel = newModel;
        if (listModel != null) {
            listModel.addListDataListener(listModelListener);
        }
        reindex();
        fireContentsChanged(this, 0, getSize());
    }
    private void reindex() {
        try {
            directory = new RAMDirectory();
            IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(Version.LUCENE_CURRENT, analyzer));
            for (int row=0; row < listModel.getSize(); row++){
                Document document = new Document();
                document.add(new Field(ROW_NUMBER, "" + row, Field.Store.YES, Field.Index.ANALYZED));
                document.add(new Field(FIELD_NAME, String.valueOf(listModel.getElementAt(row)).toLowerCase(), Field.Store.YES, Field.Index.ANALYZED));
                writer.addDocument(document);
            }
            writer.optimize();
            writer.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public void search(String searchString){
        if (searchString == null || searchString.equals("")){
            clearSearchingState();
            fireContentsChanged(this, 0, getSize());
            return;
        }
        try {
            this.searchString = searchString;
            IndexSearcher is = new IndexSearcher(directory, true);
            String[] fields = {FIELD_NAME};
            MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LUCENE_CURRENT, fields, analyzer);
            Query query =parser.parse(searchString);
            resetSearchResults(is, query);
        } catch (Exception e){
            e.printStackTrace();
        }
        fireContentsChanged(this, 0, getSize());
    }
    final static class CountingCollector extends Collector {
      public int numHits = 0;
      @Override
      public void setScorer(Scorer scorer) throws IOException {}
      @Override
      public void collect(int doc) throws IOException {
        numHits++;
      }
      @Override
      public void setNextReader(IndexReader reader, int docBase) {}
      @Override
      public boolean acceptsDocsOutOfOrder() {
        return true;
      }    
    }
    private void resetSearchResults(IndexSearcher searcher, Query query) {
        try {
            rowToModelIndex.clear();
            CountingCollector countingCollector = new CountingCollector();
            searcher.search(query, countingCollector);
            ScoreDoc[] hits = searcher.search(query, countingCollector.numHits).scoreDocs;
            for (int t=0; t<hits.length; t++){
                Document document = searcher.doc(hits[t].doc);
                Fieldable field = document.getField(ROW_NUMBER);
                rowToModelIndex.add(Integer.valueOf(field.stringValue()));
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public Analyzer getAnalyzer() {
        return analyzer;
    }
    public void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
        reindex();
        if (isSearching()){
            search(searchString);
        }
    }
    private boolean isSearching() {
        return searchString != null;
    }
    private void clearSearchingState() {
        searchString = null;
        rowToModelIndex.clear();
        for (int t=0; t<listModel.getSize(); t++){
            rowToModelIndex.add(t);
        }
    }
    private int getModelRow(int row){
        return rowToModelIndex.get(row);
    }
    public int getSize() {
        return (listModel == null) ? 0 : rowToModelIndex.size();
    }
    public Object getElementAt(int index) {
        return listModel.getElementAt(getModelRow(index));
    }
    class ListModelHandler implements ListDataListener {
        public void contentsChanged(ListDataEvent e) {
            somethingChanged();
        }
        public void intervalAdded(ListDataEvent e) {
            somethingChanged();
        }
        public void intervalRemoved(ListDataEvent e) {
            somethingChanged();
        }
        private void somethingChanged(){
            if (!isSearching()) {
                clearSearchingState();
                reindex();
                fireContentsChanged(ListSearcher.this, 0, getSize());
                return;
            }
            reindex();
            search(searchString);
            fireContentsChanged(ListSearcher.this, 0, getSize());
            return;
        }
    }
}
