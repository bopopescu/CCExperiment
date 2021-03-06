package org.apache.xpath.operations;
import org.apache.xpath.objects.XNumber;
import org.apache.xpath.objects.XObject;
public class Quo extends Operation
{
    static final long serialVersionUID = 693765299196169905L;
  public XObject operate(XObject left, XObject right)
          throws javax.xml.transform.TransformerException
  {
    return new XNumber((int) (left.num() / right.num()));
  }
}
