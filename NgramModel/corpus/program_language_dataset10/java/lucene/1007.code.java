package org.apache.lucene.search.highlight;
public class TextFragment
{
	CharSequence markedUpText;
	int fragNum;
	int textStartPos;
	int textEndPos;
	float score;
	public TextFragment(CharSequence markedUpText,int textStartPos, int fragNum)
	{
		this.markedUpText=markedUpText;
		this.textStartPos = textStartPos;
		this.fragNum = fragNum;
	}
	@Deprecated
	public TextFragment(StringBuffer markedUpText,int textStartPos, int fragNum)
	{
		this.markedUpText=markedUpText;
		this.textStartPos = textStartPos;
		this.fragNum = fragNum;
	}
	void setScore(float score)
	{
		this.score=score;
	}
	public float getScore()
	{
		return score;
	}
  public void merge(TextFragment frag2)
  {
    textEndPos = frag2.textEndPos;
    score=Math.max(score,frag2.score);
  }
	public boolean follows(TextFragment fragment)
	{
		return textStartPos == fragment.textEndPos;
	}
	public int getFragNum()
	{
		return fragNum;
	}
	@Override
	public String toString() {
		return markedUpText.subSequence(textStartPos, textEndPos).toString();
	}
}
