package org.apache.xalan.templates;
import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.dtm.DTM;
public class ElemApplyImport extends ElemTemplateElement
{
    static final long serialVersionUID = 3764728663373024038L;
  public int getXSLToken()
  {
    return Constants.ELEMNAME_APPLY_IMPORTS;
  }
  public String getNodeName()
  {
    return Constants.ELEMNAME_APPLY_IMPORTS_STRING;
  }
  public void execute(
          TransformerImpl transformer)
            throws TransformerException
  {
    if (transformer.currentTemplateRuleIsNull())
    {
      transformer.getMsgMgr().error(this,
        XSLTErrorResources.ER_NO_APPLY_IMPORT_IN_FOR_EACH);  
    }
    if (transformer.getDebug())
      transformer.getTraceManager().fireTraceEvent(this);
    int sourceNode = transformer.getXPathContext().getCurrentNode();
    if (DTM.NULL != sourceNode)
    {
      ElemTemplate matchTemplate = transformer.getMatchedTemplate();
      transformer.applyTemplateToNode(this, matchTemplate, sourceNode);
    }
    else  
    {
      transformer.getMsgMgr().error(this,
        XSLTErrorResources.ER_NULL_SOURCENODE_APPLYIMPORTS);  
    }
    if (transformer.getDebug())
      transformer.getTraceManager().fireTraceEndEvent(this);
  }
  public ElemTemplateElement appendChild(ElemTemplateElement newChild)
  {
    error(XSLTErrorResources.ER_CANNOT_ADD,
          new Object[]{ newChild.getNodeName(),
                        this.getNodeName() });  
    return null;
  }
}
