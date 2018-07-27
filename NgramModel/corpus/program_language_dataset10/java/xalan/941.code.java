package org.apache.xpath.operations;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.objects.XString;
public class String extends UnaryOperation
{
    static final long serialVersionUID = 2973374377453022888L;
  public XObject operate(XObject right) throws javax.xml.transform.TransformerException
  {
    return (XString)right.xstr(); 
  }
}
