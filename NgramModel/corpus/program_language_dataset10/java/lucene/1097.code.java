package org.apache.lucene.queryParser.core;
import org.apache.lucene.queryParser.core.builders.QueryBuilder;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.queryParser.core.parser.SyntaxParser;
import org.apache.lucene.queryParser.core.processors.QueryNodeProcessor;
public class QueryParserHelper {
  private QueryNodeProcessor processor;
  private SyntaxParser syntaxParser;
  private QueryBuilder builder;
  private QueryConfigHandler config;
  public QueryParserHelper(QueryConfigHandler queryConfigHandler, SyntaxParser syntaxParser, QueryNodeProcessor processor,
      QueryBuilder builder) {
    this.syntaxParser = syntaxParser;
    this.config = queryConfigHandler;
    this.processor = processor;
    this.builder = builder;
    if (processor != null) {
      processor.setQueryConfigHandler(queryConfigHandler);
    }
  }
  public QueryNodeProcessor getQueryNodeProcessor() {
    return processor;
  }
  public void setQueryNodeProcessor(QueryNodeProcessor processor) {
    this.processor = processor;
    this.processor.setQueryConfigHandler(getQueryConfigHandler());
  }
  public void setSyntaxParser(SyntaxParser syntaxParser) {
    if (syntaxParser == null) {
      throw new IllegalArgumentException("textParser should not be null!");
    }
    this.syntaxParser = syntaxParser;
  }
  public void setQueryBuilder(QueryBuilder queryBuilder) {
    if (queryBuilder == null) {
      throw new IllegalArgumentException("queryBuilder should not be null!");
    }
    this.builder = queryBuilder;
  }
  public QueryConfigHandler getQueryConfigHandler() {
    return config;
  }
  public QueryBuilder getQueryBuilder() {
    return this.builder;
  }
  public SyntaxParser getSyntaxParser() {
    return this.syntaxParser;
  }
  public void setQueryConfigHandler(QueryConfigHandler config) {
    this.config = config;
    QueryNodeProcessor processor = getQueryNodeProcessor();
    if (processor != null) {
      processor.setQueryConfigHandler(config);
    }
  }
  public Object parse(String query, String defaultField)
      throws QueryNodeException {
    QueryNode queryTree = getSyntaxParser().parse(query, defaultField);
    QueryNodeProcessor processor = getQueryNodeProcessor();
    if (processor != null) {
      queryTree = processor.process(queryTree);
    }
    return getQueryBuilder().build(queryTree);
  }
}
