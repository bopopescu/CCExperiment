package org.apache.lucene.search.highlight;
public class DefaultEncoder implements Encoder
{
	public DefaultEncoder()
	{
	}
	public String encodeText(String originalText)
	{
		return originalText;
	}
}