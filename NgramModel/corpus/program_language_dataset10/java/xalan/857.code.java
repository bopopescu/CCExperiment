package org.apache.xpath.functions;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XBoolean;
import org.apache.xpath.objects.XObject;
public class FuncBoolean extends FunctionOneArg
{
    static final long serialVersionUID = 4328660760070034592L;
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
  {
    return m_arg0.execute(xctxt).bool() ? XBoolean.S_TRUE : XBoolean.S_FALSE;
  }
}
