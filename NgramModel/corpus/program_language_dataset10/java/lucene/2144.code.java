package org.apache.solr.handler.extraction;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.DateUtil;
import org.apache.solr.schema.DateField;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.tika.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.text.DateFormat;
import java.util.*;
public class SolrContentHandler extends DefaultHandler implements ExtractingParams {
  private transient static Logger log = LoggerFactory.getLogger(SolrContentHandler.class);
  private SolrInputDocument document;
  private Collection<String> dateFormats = DateUtil.DEFAULT_DATE_FORMATS;
  private Metadata metadata;
  private SolrParams params;
  private StringBuilder catchAllBuilder = new StringBuilder(2048);
  private IndexSchema schema;
  private Map<String, StringBuilder> fieldBuilders = Collections.emptyMap();
  private LinkedList<StringBuilder> bldrStack = new LinkedList<StringBuilder>();
  private boolean captureAttribs;
  private boolean lowerNames;
  private String contentFieldName = "content";
  private String unknownFieldPrefix = "";
  private String defaultField = "";
  public SolrContentHandler(Metadata metadata, SolrParams params, IndexSchema schema) {
    this(metadata, params, schema, DateUtil.DEFAULT_DATE_FORMATS);
  }
  public SolrContentHandler(Metadata metadata, SolrParams params,
                            IndexSchema schema, Collection<String> dateFormats) {
    document = new SolrInputDocument();
    this.metadata = metadata;
    this.params = params;
    this.schema = schema;
    this.dateFormats = dateFormats;
    this.lowerNames = params.getBool(LOWERNAMES, false);
    this.captureAttribs = params.getBool(CAPTURE_ATTRIBUTES, false);
    this.unknownFieldPrefix = params.get(UNKNOWN_FIELD_PREFIX, "");
    this.defaultField = params.get(DEFAULT_FIELD, "");
    String[] captureFields = params.getParams(CAPTURE_ELEMENTS);
    if (captureFields != null && captureFields.length > 0) {
      fieldBuilders = new HashMap<String, StringBuilder>();
      for (int i = 0; i < captureFields.length; i++) {
        fieldBuilders.put(captureFields[i], new StringBuilder());
      }
    }
    bldrStack.add(catchAllBuilder);
  }
  public SolrInputDocument newDocument() {
    float boost = 1.0f;
    for (String name : metadata.names()) {
      String[] vals = metadata.getValues(name);
      addField(name, null, vals);
    }
    Iterator<String> paramNames = params.getParameterNamesIterator();
    while (paramNames.hasNext()) {
      String pname = paramNames.next();
      if (!pname.startsWith(LITERALS_PREFIX)) continue;
      String name = pname.substring(LITERALS_PREFIX.length());
      addField(name, null, params.getParams(pname));
    }
    addField(contentFieldName, catchAllBuilder.toString(), null);
    for (Map.Entry<String, StringBuilder> entry : fieldBuilders.entrySet()) {
      if (entry.getValue().length() > 0) {
        addField(entry.getKey(), entry.getValue().toString(), null);
      }
    }
    if (log.isDebugEnabled()) {
      log.debug("Doc: " + document);
    }
    return document;
  }
  private void addField(String fname, String fval, String[] vals) {
    if (lowerNames) {
      StringBuilder sb = new StringBuilder();
      for (int i=0; i<fname.length(); i++) {
        char ch = fname.charAt(i);
        if (!Character.isLetterOrDigit(ch)) ch='_';
        else ch=Character.toLowerCase(ch);
        sb.append(ch);
      }
      fname = sb.toString();
    }    
    String name = findMappedName(fname);
    SchemaField sf = schema.getFieldOrNull(name);
    if (sf==null && unknownFieldPrefix.length() > 0) {
      name = unknownFieldPrefix + name;
      sf = schema.getFieldOrNull(name);
    } else if (sf == null && defaultField.length() > 0 && name.equals(Metadata.RESOURCE_NAME_KEY) == false ){
      name = defaultField;
      sf = schema.getFieldOrNull(name);
    }
    if (sf == null && unknownFieldPrefix.length()==0 && name == Metadata.RESOURCE_NAME_KEY) {
      return;
    }
    if (vals != null && vals.length==1) {
      fval = vals[0];
      vals = null;
    }
    if (sf != null && !sf.multiValued() && vals != null) {
      StringBuilder builder = new StringBuilder();
      boolean first=true;
      for (String val : vals) {
        if (first) {
          first=false;
        } else {
          builder.append(' ');
        }
        builder.append(val);
      }
      fval = builder.toString();
      vals=null;
    }
    float boost = getBoost(name);
    if (fval != null) {
      document.addField(name, transformValue(fval, sf), boost);
    }
    if (vals != null) {
      for (String val : vals) {
        document.addField(name, transformValue(val, sf), boost);
      }
    }
  }
  @Override
  public void startDocument() throws SAXException {
    document.clear();
    catchAllBuilder.setLength(0);
    for (StringBuilder builder : fieldBuilders.values()) {
      builder.setLength(0);
    }
    bldrStack.clear();
    bldrStack.add(catchAllBuilder);
  }
  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    StringBuilder theBldr = fieldBuilders.get(localName);
    if (theBldr != null) {
      bldrStack.add(theBldr);
    }
    if (captureAttribs == true) {
      for (int i = 0; i < attributes.getLength(); i++) {
        addField(localName, attributes.getValue(i), null);
      }
    } else {
      for (int i = 0; i < attributes.getLength(); i++) {
        bldrStack.getLast().append(attributes.getValue(i)).append(' ');
      }
    }
    bldrStack.getLast().append(' ');
  }
  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    StringBuilder theBldr = fieldBuilders.get(localName);
    if (theBldr != null) {
      bldrStack.removeLast();
      assert (bldrStack.size() >= 1);
    }
    bldrStack.getLast().append(' ');
  }
  @Override
  public void characters(char[] chars, int offset, int length) throws SAXException {
    bldrStack.getLast().append(chars, offset, length);
  }
  protected String transformValue(String val, SchemaField schFld) {
    String result = val;
    if (schFld != null && schFld.getType() instanceof DateField) {
      try {
        Date date = DateUtil.parseDate(val, dateFormats);
        DateFormat df = DateUtil.getThreadLocalDateFormat();
        result = df.format(date);
      } catch (Exception e) {
      }
    }
    return result;
  }
  protected float getBoost(String name) {
    return params.getFloat(BOOST_PREFIX + name, 1.0f);
  }
  protected String findMappedName(String name) {
    return params.get(MAP_PREFIX + name, name);
  }
}
