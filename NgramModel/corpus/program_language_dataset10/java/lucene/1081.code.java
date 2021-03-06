package org.apache.lucene.search;
import java.io.IOException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.util.OpenBitSet;
public class DuplicateFilter extends Filter
{
	String fieldName;
	int keepMode=KM_USE_FIRST_OCCURRENCE;
	public static final int KM_USE_FIRST_OCCURRENCE=1;
	public static final int KM_USE_LAST_OCCURRENCE=2;
	int processingMode=PM_FULL_VALIDATION;
	public static final int PM_FULL_VALIDATION=1;
	public static final int PM_FAST_INVALIDATION=2;
	public DuplicateFilter(String fieldName)
	{
		this(fieldName, KM_USE_LAST_OCCURRENCE,PM_FULL_VALIDATION);
	}
	public DuplicateFilter(String fieldName, int keepMode, int processingMode)
	{
		this.fieldName = fieldName;
		this.keepMode = keepMode;
		this.processingMode = processingMode;
	}
  @Override
  public DocIdSet getDocIdSet(IndexReader reader) throws IOException
	{
		if(processingMode==PM_FAST_INVALIDATION)
		{
			return fastBits(reader);
		}
		else
		{
			return correctBits(reader);
		}
	}
  private OpenBitSet correctBits(IndexReader reader) throws IOException
	{
    OpenBitSet bits=new OpenBitSet(reader.maxDoc()); 
		Term startTerm=new Term(fieldName);
		TermEnum te = reader.terms(startTerm);
		if(te!=null)
		{
			Term currTerm=te.term();
			while((currTerm!=null)&&(currTerm.field()==startTerm.field())) 
			{
				int lastDoc=-1;
				TermDocs td = reader.termDocs(currTerm);
				if(td.next())
				{
					if(keepMode==KM_USE_FIRST_OCCURRENCE)
					{
						bits.set(td.doc());
					}
					else
					{
						do
						{
							lastDoc=td.doc();
						}while(td.next());
						bits.set(lastDoc);
					}
				}
				if(!te.next())
				{
					break;
				}
				currTerm=te.term();
			}
		}
		return bits;
	}
  private OpenBitSet fastBits(IndexReader reader) throws IOException
	{
    OpenBitSet bits=new OpenBitSet(reader.maxDoc());
		bits.set(0,reader.maxDoc()); 
		Term startTerm=new Term(fieldName);
		TermEnum te = reader.terms(startTerm);
		if(te!=null)
		{
			Term currTerm=te.term();
			while((currTerm!=null)&&(currTerm.field()==startTerm.field())) 
			{
				if(te.docFreq()>1)
				{
					int lastDoc=-1;
					TermDocs td = reader.termDocs(currTerm);
					td.next();
					if(keepMode==KM_USE_FIRST_OCCURRENCE)
					{
						td.next();
					}
					do
					{
						lastDoc=td.doc();
            bits.clear(lastDoc);
					}while(td.next());
					if(keepMode==KM_USE_LAST_OCCURRENCE)
					{
						bits.set(lastDoc);
					}					
				}
				if(!te.next())
				{
					break;
				}
				currTerm=te.term();
			}
		}
		return bits;
	}
	public String getFieldName()
	{
		return fieldName;
	}
	public void setFieldName(String fieldName)
	{
		this.fieldName = fieldName;
	}
	public int getKeepMode()
	{
		return keepMode;
	}
	public void setKeepMode(int keepMode)
	{
		this.keepMode = keepMode;
	}
	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if((obj == null) || (obj.getClass() != this.getClass()))
			return false;
		DuplicateFilter other = (DuplicateFilter)obj;
		return keepMode == other.keepMode &&
		processingMode == other.processingMode &&
			(fieldName == other.fieldName || (fieldName != null && fieldName.equals(other.fieldName)));
	}
	@Override
	public int hashCode()
	{
		int hash = 217;
		hash = 31 * hash + keepMode;
		hash = 31 * hash + processingMode;
		hash = 31 * hash + fieldName.hashCode();
		return hash;	
	}
	public int getProcessingMode()
	{
		return processingMode;
	}
	public void setProcessingMode(int processingMode)
	{
		this.processingMode = processingMode;
	}
}
