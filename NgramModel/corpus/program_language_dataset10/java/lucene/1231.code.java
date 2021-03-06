package org.apache.lucene.queryParser.spans;
import javax.management.Query;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.core.nodes.OrQueryNode;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.queryParser.core.parser.SyntaxParser;
import org.apache.lucene.queryParser.core.processors.QueryNodeProcessorPipeline;
import org.apache.lucene.queryParser.standard.parser.StandardSyntaxParser;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.util.LuceneTestCase;
public class TestSpanQueryParserSimpleSample extends LuceneTestCase {
  public TestSpanQueryParserSimpleSample() {
  }
  public TestSpanQueryParserSimpleSample(String testName) {
    super(testName);
  }
  public void testBasicDemo() throws Exception {
    SyntaxParser queryParser = new StandardSyntaxParser();
    QueryNode queryTree = queryParser.parse("body:text", null);
    QueryConfigHandler spanQueryConfigHandler = new SpansQueryConfigHandler();
    UniqueFieldAttribute uniqueFieldAtt = spanQueryConfigHandler
        .getAttribute(UniqueFieldAttribute.class);
    uniqueFieldAtt.setUniqueField("index");
    QueryNodeProcessorPipeline spanProcessorPipeline = new QueryNodeProcessorPipeline(
        spanQueryConfigHandler);
    spanProcessorPipeline.addProcessor(new SpansValidatorQueryNodeProcessor());
    spanProcessorPipeline.addProcessor(new UniqueFieldQueryNodeProcessor());
    if (VERBOSE) System.out.println(queryTree);
    queryTree = spanProcessorPipeline.process(queryTree);
    if (VERBOSE) System.out.println(queryTree);
    SpansQueryTreeBuilder spansQueryTreeBuilder = new SpansQueryTreeBuilder();
    SpanQuery spanquery = spansQueryTreeBuilder.build(queryTree);
    assertTrue(spanquery instanceof SpanTermQuery);
    assertEquals(spanquery.toString(), "index:text");
  }
}
