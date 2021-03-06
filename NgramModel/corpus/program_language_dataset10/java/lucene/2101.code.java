package org.apache.solr.handler.dataimport;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class TemplateTransformer extends Transformer {
  private static final Logger LOG = LoggerFactory.getLogger(TemplateTransformer.class);
  private Map<String ,List<String>> templateVsVars = new HashMap<String, List<String>>();
  @SuppressWarnings("unchecked")
  public Object transformRow(Map<String, Object> row, Context context) {
    VariableResolverImpl resolver = (VariableResolverImpl) context
            .getVariableResolver();
    for (Map<String, String> map : context.getAllEntityFields()) {
      String expr = map.get(TEMPLATE);
      if (expr == null)
        continue;
      String column = map.get(DataImporter.COLUMN);
      boolean resolvable = true;
      List<String> variables = getVars(expr);
      for (String v : variables) {
        if (resolver.resolve(v) == null) {
          LOG.warn("Unable to resolve variable: " + v
                  + " while parsing expression: " + expr);
          resolvable = false;
        }
      }
      if (!resolvable)
        continue;
      if(variables.size() == 1 && expr.startsWith("${") && expr.endsWith("}")){
        row.put(column, resolver.resolve(variables.get(0)));
      } else {
        row.put(column, resolver.replaceTokens(expr));
      }
    }
    return row;
  }
  private List<String> getVars(String expr) {
    List<String> result = this.templateVsVars.get(expr);
    if(result == null){
      result = TemplateString.getVariables(expr);
      this.templateVsVars.put(expr, result);
    }
    return result;
  }
  public static final String TEMPLATE = "template";
}
