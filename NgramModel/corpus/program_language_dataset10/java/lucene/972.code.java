package org.apache.lucene.search.vectorhighlight;
import java.io.IOException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
public class FastVectorHighlighter {
  public static final boolean DEFAULT_PHRASE_HIGHLIGHT = true;
  public static final boolean DEFAULT_FIELD_MATCH = true;
  private final boolean phraseHighlight;
  private final boolean fieldMatch;
  private final FragListBuilder fragListBuilder;
  private final FragmentsBuilder fragmentsBuilder;
  public FastVectorHighlighter(){
    this( DEFAULT_PHRASE_HIGHLIGHT, DEFAULT_FIELD_MATCH );
  }
  public FastVectorHighlighter( boolean phraseHighlight, boolean fieldMatch ){
    this( phraseHighlight, fieldMatch, new SimpleFragListBuilder(), new ScoreOrderFragmentsBuilder() );
  }
  public FastVectorHighlighter( boolean phraseHighlight, boolean fieldMatch,
      FragListBuilder fragListBuilder, FragmentsBuilder fragmentsBuilder ){
    this.phraseHighlight = phraseHighlight;
    this.fieldMatch = fieldMatch;
    this.fragListBuilder = fragListBuilder;
    this.fragmentsBuilder = fragmentsBuilder;
  }
  public FieldQuery getFieldQuery( Query query ){
    return new FieldQuery( query, phraseHighlight, fieldMatch );
  }
  public final String getBestFragment( final FieldQuery fieldQuery, IndexReader reader, int docId,
      String fieldName, int fragCharSize ) throws IOException {
    FieldFragList fieldFragList = getFieldFragList( fieldQuery, reader, docId, fieldName, fragCharSize );
    return fragmentsBuilder.createFragment( reader, docId, fieldName, fieldFragList );
  }
  public final String[] getBestFragments( final FieldQuery fieldQuery, IndexReader reader, int docId,
      String fieldName, int fragCharSize, int maxNumFragments ) throws IOException {
    FieldFragList fieldFragList = getFieldFragList( fieldQuery, reader, docId, fieldName, fragCharSize );
    return fragmentsBuilder.createFragments( reader, docId, fieldName, fieldFragList, maxNumFragments );
  }
  private FieldFragList getFieldFragList( final FieldQuery fieldQuery, IndexReader reader, int docId,
      String fieldName, int fragCharSize ) throws IOException {
    FieldTermStack fieldTermStack = new FieldTermStack( reader, docId, fieldName, fieldQuery );
    FieldPhraseList fieldPhraseList = new FieldPhraseList( fieldTermStack, fieldQuery );
    return fragListBuilder.createFieldFragList( fieldPhraseList, fragCharSize );
  }
  public boolean isPhraseHighlight(){ return phraseHighlight; }
  public boolean isFieldMatch(){ return fieldMatch; }
}
