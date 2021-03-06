package org.apache.lucene.search;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.util.OpenBitSet;
public class TermsFilter extends Filter
{
	Set<Term> terms=new TreeSet<Term>();
	public void addTerm(Term term)
	{
		terms.add(term);
	}
  @Override
  public DocIdSet getDocIdSet(IndexReader reader) throws IOException
	{
    OpenBitSet result=new OpenBitSet(reader.maxDoc());
        TermDocs td = reader.termDocs();
        try
        {
            for (Iterator<Term> iter = terms.iterator(); iter.hasNext();)
            {
                Term term = iter.next();
                td.seek(term);
                while (td.next())
                {
                    result.set(td.doc());
                }
            }
        }
        finally
        {
            td.close();
        }
        return result;
	}
	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if((obj == null) || (obj.getClass() != this.getClass()))
				return false;
		TermsFilter test = (TermsFilter)obj;
		return (terms == test.terms ||
					 (terms != null && terms.equals(test.terms)));
	}
	@Override
	public int hashCode()
	{
		int hash=9;
		for (Iterator<Term> iter = terms.iterator(); iter.hasNext();)
		{
			Term term = iter.next();
			hash = 31 * hash + term.hashCode();			
		}
		return hash;
	}
}
