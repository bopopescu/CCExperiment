package org.apache.wml;
public interface WMLSelectElement extends WMLElement {
    public void setTabIndex(int newValue);
    public int getTabIndex();
    public void setMultiple(boolean newValue);
    public boolean getMultiple();
    public void setName(String newValue);
    public String getName();
    public void setValue(String newValue);
    public String getValue();
    public void setTitle(String newValue);
    public String getTitle();
    public void setIName(String newValue);
    public String getIName();
    public void setIValue(String newValue);
    public String getIValue();
    public void setXmlLang(String newValue);
    public String getXmlLang();
}