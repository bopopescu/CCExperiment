package org.apache.lucene.search;
import org.apache.lucene.search.BooleanClause.Occur;
public class FilterClause implements java.io.Serializable
{
	Occur occur = null;
	Filter filter = null;
	public FilterClause( Filter filter,Occur occur)
	{
		this.occur = occur;
		this.filter = filter;
	}
	public Filter getFilter()
	{
		return filter;
	}
	public Occur getOccur()
	{
		return occur;
	}
}
