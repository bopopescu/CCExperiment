package org.apache.solr.handler.dataimport;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.util.AbstractSolrTestCase;
import org.apache.solr.common.util.NamedList;
import java.io.IOException;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public abstract class AbstractDataImportHandlerTest extends
        AbstractSolrTestCase {
  @Override
  public void setUp() throws Exception {
    super.setUp();
  }
  @Override
  public void tearDown() throws Exception {
    File f = new File("solr/conf/dataimport.properties");
    log.info("Looking for dataimport.properties at: " + f.getAbsolutePath());
    if (f.exists()) {
      log.info("Deleting dataimport.properties");
      if (!f.delete())
        log.warn("Could not delete dataimport.properties");
    }
    super.tearDown();
  }
  protected String loadDataConfig(String dataConfigFileName) {
    try {
      SolrCore core = h.getCore();
      return SolrWriter.getResourceAsString(core.getResourceLoader()
              .openResource(dataConfigFileName));
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
  protected void runFullImport(String dataConfig) throws Exception {
    LocalSolrQueryRequest request = lrf.makeRequest("command", "full-import",
            "debug", "on", "clean", "true", "commit", "true", "dataConfig",
            dataConfig);
    h.query("/dataimport", request);
  }
  protected void runDeltaImport(String dataConfig) throws Exception {
    LocalSolrQueryRequest request = lrf.makeRequest("command", "delta-import",
            "debug", "on", "clean", "false", "commit", "true", "dataConfig",
            dataConfig);
    h.query("/dataimport", request);
  }
  protected void runFullImport(String dataConfig, Map<String, String> extraParams) throws Exception {
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("command", "full-import");
    params.put("debug", "on");
    params.put("dataConfig", dataConfig);
    params.put("clean", "true");
    params.put("commit", "true");
    params.putAll(extraParams);
    NamedList l = new NamedList();
    for (Map.Entry<String, String> e : params.entrySet()) {
      l.add(e.getKey(),e.getValue());
    }
    LocalSolrQueryRequest request = new LocalSolrQueryRequest(h.getCore(), l);  
    h.query("/dataimport", request);
  }
  @SuppressWarnings("unchecked")
  public static TestContext getContext(DataConfig.Entity parentEntity,
                                   VariableResolverImpl resolver, DataSource parentDataSource,
                                   String currProcess, final List<Map<String, String>> entityFields,
                                   final Map<String, String> entityAttrs) {
    if (resolver == null) resolver = new VariableResolverImpl();
    final Context delegate = new ContextImpl(parentEntity, resolver,
            parentDataSource, currProcess,
            new HashMap<String, Object>(), null, null);
    return new TestContext(entityAttrs, delegate, entityFields, parentEntity == null);
  }
  @SuppressWarnings("unchecked")
  public static Map createMap(Object... args) {
    Map result = new HashMap();
    if (args == null || args.length == 0)
      return result;
    for (int i = 0; i < args.length - 1; i += 2)
      result.put(args[i], args[i + 1]);
    return result;
  }
  static class TestContext extends Context {
    private final Map<String, String> entityAttrs;
    private final Context delegate;
    private final List<Map<String, String>> entityFields;
    private final boolean root;
    String script,scriptlang;
    public TestContext(Map<String, String> entityAttrs, Context delegate,
                       List<Map<String, String>> entityFields, boolean root) {
      this.entityAttrs = entityAttrs;
      this.delegate = delegate;
      this.entityFields = entityFields;
      this.root = root;
    }
    public String getEntityAttribute(String name) {
      return entityAttrs == null ? delegate.getEntityAttribute(name) : entityAttrs.get(name);
    }
    public String getResolvedEntityAttribute(String name) {
      return entityAttrs == null ? delegate.getResolvedEntityAttribute(name) :
              delegate.getVariableResolver().replaceTokens(entityAttrs.get(name));
    }
    public List<Map<String, String>> getAllEntityFields() {
      return entityFields == null ? delegate.getAllEntityFields()
              : entityFields;
    }
    public VariableResolver getVariableResolver() {
      return delegate.getVariableResolver();
    }
    public DataSource getDataSource() {
      return delegate.getDataSource();
    }
    public boolean isRootEntity() {
      return root;
    }
    public String currentProcess() {
      return delegate.currentProcess();
    }
    public Map<String, Object> getRequestParameters() {
      return delegate.getRequestParameters();
    }
    public EntityProcessor getEntityProcessor() {
      return null;
    }
    public void setSessionAttribute(String name, Object val, String scope) {
      delegate.setSessionAttribute(name, val, scope);
    }
    public Object getSessionAttribute(String name, String scope) {
      return delegate.getSessionAttribute(name, scope);
    }
    public Context getParentContext() {
      return delegate.getParentContext();
    }
    public DataSource getDataSource(String name) {
      return delegate.getDataSource(name);
    }
    public SolrCore getSolrCore() {
      return delegate.getSolrCore();
    }
    public Map<String, Object> getStats() {
      return delegate.getStats();
    }
    public String getScript() {
      return script == null ? delegate.getScript() : script;
    }
    public String getScriptLanguage() {
      return scriptlang == null ? delegate.getScriptLanguage() : scriptlang;
    }
    public void deleteDoc(String id) {
    }
    public void deleteDocByQuery(String query) {
    }
    public Object resolve(String var) {
      return delegate.resolve(var);
    }
    public String replaceTokens(String template) {
      return delegate.replaceTokens(template);
    }
  }
}
