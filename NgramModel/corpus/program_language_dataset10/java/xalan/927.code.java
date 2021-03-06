package org.apache.xpath.operations;
import org.apache.xpath.objects.XBoolean;
import org.apache.xpath.objects.XObject;
public class Gt extends Operation
{
    static final long serialVersionUID = 8927078751014375950L;
  public XObject operate(XObject left, XObject right)
          throws javax.xml.transform.TransformerException
  {
    return left.greaterThan(right) ? XBoolean.S_TRUE : XBoolean.S_FALSE;
  }
}
