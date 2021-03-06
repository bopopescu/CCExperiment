package org.apache.lucene.search.highlight;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.PriorityQueue;
public class Highlighter
{
  public static final int DEFAULT_MAX_CHARS_TO_ANALYZE = 50*1024;
  private int maxDocCharsToAnalyze = DEFAULT_MAX_CHARS_TO_ANALYZE;
	private Formatter formatter;
	private Encoder encoder;
	private Fragmenter textFragmenter=new SimpleFragmenter();
	private Scorer fragmentScorer=null;
	public Highlighter(Scorer fragmentScorer)
	{
		this(new SimpleHTMLFormatter(),fragmentScorer);
	}
 	public Highlighter(Formatter formatter, Scorer fragmentScorer)
 	{
		this(formatter,new DefaultEncoder(),fragmentScorer);
	}
	public Highlighter(Formatter formatter, Encoder encoder, Scorer fragmentScorer)
	{
 		this.formatter = formatter;
		this.encoder = encoder;
 		this.fragmentScorer = fragmentScorer;
 	}
	public final String getBestFragment(Analyzer analyzer, String fieldName,String text)
		throws IOException, InvalidTokenOffsetsException
	{
		TokenStream tokenStream = analyzer.tokenStream(fieldName, new StringReader(text));
		return getBestFragment(tokenStream, text);
	}
	public final String getBestFragment(TokenStream tokenStream, String text)
		throws IOException, InvalidTokenOffsetsException
	{
		String[] results = getBestFragments(tokenStream,text, 1);
		if (results.length > 0)
		{
			return results[0];
		}
		return null;
	}
	public final String[] getBestFragments(
		Analyzer analyzer,
		String fieldName,
		String text,
		int maxNumFragments)
		throws IOException, InvalidTokenOffsetsException
	{
		TokenStream tokenStream = analyzer.tokenStream(fieldName, new StringReader(text));
		return getBestFragments(tokenStream, text, maxNumFragments);
	}
	public final String[] getBestFragments(
		TokenStream tokenStream,
		String text,
		int maxNumFragments)
		throws IOException, InvalidTokenOffsetsException
	{
		maxNumFragments = Math.max(1, maxNumFragments); 
		TextFragment[] frag =getBestTextFragments(tokenStream,text, true,maxNumFragments);
		ArrayList<String> fragTexts = new ArrayList<String>();
		for (int i = 0; i < frag.length; i++)
		{
			if ((frag[i] != null) && (frag[i].getScore() > 0))
			{
				fragTexts.add(frag[i].toString());
			}
		}
		return fragTexts.toArray(new String[0]);
	}
	public final TextFragment[] getBestTextFragments(
		TokenStream tokenStream,
		String text,
		boolean mergeContiguousFragments,
		int maxNumFragments)
		throws IOException, InvalidTokenOffsetsException
	{
		ArrayList<TextFragment> docFrags = new ArrayList<TextFragment>();
		StringBuilder newText=new StringBuilder();
	    TermAttribute termAtt = tokenStream.addAttribute(TermAttribute.class);
	    OffsetAttribute offsetAtt = tokenStream.addAttribute(OffsetAttribute.class);
	    tokenStream.addAttribute(PositionIncrementAttribute.class);
	    tokenStream.reset();
		TextFragment currentFrag =	new TextFragment(newText,newText.length(), docFrags.size());
		TokenStream newStream = fragmentScorer.init(tokenStream);
		if(newStream != null) {
		  tokenStream = newStream;
		}
		fragmentScorer.startFragment(currentFrag);
		docFrags.add(currentFrag);
		FragmentQueue fragQueue = new FragmentQueue(maxNumFragments);
		try
		{
			String tokenText;
			int startOffset;
			int endOffset;
			int lastEndOffset = 0;
			textFragmenter.start(text, tokenStream);
			TokenGroup tokenGroup=new TokenGroup(tokenStream);
			for (boolean next = tokenStream.incrementToken(); next && (offsetAtt.startOffset()< maxDocCharsToAnalyze);
			      next = tokenStream.incrementToken())
			{
				if(	(offsetAtt.endOffset()>text.length())
					||
					(offsetAtt.startOffset()>text.length())
					)						
				{
					throw new InvalidTokenOffsetsException("Token "+ termAtt.term()
							+" exceeds length of provided text sized "+text.length());
				}
				if((tokenGroup.numTokens>0)&&(tokenGroup.isDistinct()))
				{
					startOffset = tokenGroup.matchStartOffset;
					endOffset = tokenGroup.matchEndOffset;
					tokenText = text.substring(startOffset, endOffset);
					String markedUpText=formatter.highlightTerm(encoder.encodeText(tokenText), tokenGroup);
					if (startOffset > lastEndOffset)
						newText.append(encoder.encodeText(text.substring(lastEndOffset, startOffset)));
					newText.append(markedUpText);
					lastEndOffset=Math.max(endOffset, lastEndOffset);
					tokenGroup.clear();
					if(textFragmenter.isNewFragment())
					{
						currentFrag.setScore(fragmentScorer.getFragmentScore());
						currentFrag.textEndPos = newText.length();
						currentFrag =new TextFragment(newText, newText.length(), docFrags.size());
						fragmentScorer.startFragment(currentFrag);
						docFrags.add(currentFrag);
					}
				}
				tokenGroup.addToken(fragmentScorer.getTokenScore());
			}
			currentFrag.setScore(fragmentScorer.getFragmentScore());
			if(tokenGroup.numTokens>0)
			{
				startOffset = tokenGroup.matchStartOffset;
				endOffset = tokenGroup.matchEndOffset;
				tokenText = text.substring(startOffset, endOffset);
				String markedUpText=formatter.highlightTerm(encoder.encodeText(tokenText), tokenGroup);
				if (startOffset > lastEndOffset)
					newText.append(encoder.encodeText(text.substring(lastEndOffset, startOffset)));
				newText.append(markedUpText);
				lastEndOffset=Math.max(lastEndOffset,endOffset);
			}
			if (
					(lastEndOffset < text.length()) 
					&&
					(text.length()<= maxDocCharsToAnalyze)
				)				
			{
				newText.append(encoder.encodeText(text.substring(lastEndOffset)));
			}
			currentFrag.textEndPos = newText.length();
			for (Iterator<TextFragment> i = docFrags.iterator(); i.hasNext();)
			{
				currentFrag = i.next();
				fragQueue.insertWithOverflow(currentFrag);
			}
			TextFragment frag[] = new TextFragment[fragQueue.size()];
			for (int i = frag.length - 1; i >= 0; i--)
			{
				frag[i] = fragQueue.pop();
			}
			if(mergeContiguousFragments)
			{
				mergeContiguousFragments(frag);
				ArrayList<TextFragment> fragTexts = new ArrayList<TextFragment>();
				for (int i = 0; i < frag.length; i++)
				{
					if ((frag[i] != null) && (frag[i].getScore() > 0))
					{
						fragTexts.add(frag[i]);
					}
				}
				frag= fragTexts.toArray(new TextFragment[0]);
			}
			return frag;
		}
		finally
		{
			if (tokenStream != null)
			{
				try
				{
					tokenStream.close();
				}
				catch (Exception e)
				{
				}
			}
		}
	}
	private void mergeContiguousFragments(TextFragment[] frag)
	{
		boolean mergingStillBeingDone;
		if (frag.length > 1)
			do
			{
				mergingStillBeingDone = false; 
				for (int i = 0; i < frag.length; i++)
				{
					if (frag[i] == null)
					{
						continue;
					}
					for (int x = 0; x < frag.length; x++)
					{
						if (frag[x] == null)
						{
							continue;
						}
						if (frag[i] == null)
						{
							break;
						}
						TextFragment frag1 = null;
						TextFragment frag2 = null;
						int frag1Num = 0;
						int frag2Num = 0;
						int bestScoringFragNum;
						int worstScoringFragNum;
						if (frag[i].follows(frag[x]))
						{
							frag1 = frag[x];
							frag1Num = x;
							frag2 = frag[i];
							frag2Num = i;
						}
						else
							if (frag[x].follows(frag[i]))
							{
								frag1 = frag[i];
								frag1Num = i;
								frag2 = frag[x];
								frag2Num = x;
							}
						if (frag1 != null)
						{
							if (frag1.getScore() > frag2.getScore())
							{
								bestScoringFragNum = frag1Num;
								worstScoringFragNum = frag2Num;
							}
							else
							{
								bestScoringFragNum = frag2Num;
								worstScoringFragNum = frag1Num;
							}
							frag1.merge(frag2);
							frag[worstScoringFragNum] = null;
							mergingStillBeingDone = true;
							frag[bestScoringFragNum] = frag1;
						}
					}
				}
			}
			while (mergingStillBeingDone);
	}
	public final String getBestFragments(
		TokenStream tokenStream,	
		String text,
		int maxNumFragments,
		String separator)
		throws IOException, InvalidTokenOffsetsException
	{
		String sections[] =	getBestFragments(tokenStream,text, maxNumFragments);
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < sections.length; i++)
		{
			if (i > 0)
			{
				result.append(separator);
			}
			result.append(sections[i]);
		}
		return result.toString();
	}
  public int getMaxDocCharsToAnalyze() {
    return maxDocCharsToAnalyze;
  }
  public void setMaxDocCharsToAnalyze(int maxDocCharsToAnalyze) {
    this.maxDocCharsToAnalyze = maxDocCharsToAnalyze;
  }
	public Fragmenter getTextFragmenter()
	{
		return textFragmenter;
	}
	public void setTextFragmenter(Fragmenter fragmenter)
	{
		textFragmenter = fragmenter;
	}
	public Scorer getFragmentScorer()
	{
		return fragmentScorer;
	}
	public void setFragmentScorer(Scorer scorer)
	{
		fragmentScorer = scorer;
	}
    public Encoder getEncoder()
    {
        return encoder;
    }
    public void setEncoder(Encoder encoder)
    {
        this.encoder = encoder;
    }
}
class FragmentQueue extends PriorityQueue<TextFragment>
{
	public FragmentQueue(int size)
	{
		initialize(size);
	}
	@Override
	public final boolean lessThan(TextFragment fragA, TextFragment fragB)
	{
		if (fragA.getScore() == fragB.getScore())
			return fragA.fragNum > fragB.fragNum;
		else
			return fragA.getScore() < fragB.getScore();
	}
}
