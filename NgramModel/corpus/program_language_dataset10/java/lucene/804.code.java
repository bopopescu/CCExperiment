package org.apache.lucene.analysis.snowball;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.zip.ZipFile;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.KeywordTokenizer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
public class TestSnowballVocab extends BaseTokenStreamTestCase {
  private Tokenizer tokenizer = new KeywordTokenizer(new StringReader(""));
  ZipFile zipFile = null;
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    this.zipFile = new ZipFile(getDataFile("TestSnowballVocabData.zip"));
  }
  @Override
  protected void tearDown() throws Exception {
    this.zipFile.close();
    this.zipFile = null;
    super.tearDown();
  }
  public void testStemmers() throws IOException {
    assertCorrectOutput("Danish", "danish");
    assertCorrectOutput("Dutch", "dutch");
    assertCorrectOutput("English", "english");
    assertCorrectOutput("French", "french");
    assertCorrectOutput("German", "german");
    assertCorrectOutput("German2", "german2");
    assertCorrectOutput("Hungarian", "hungarian");
    assertCorrectOutput("Italian", "italian");
    assertCorrectOutput("Kp", "kraaij_pohlmann");
    assertCorrectOutput("Norwegian", "norwegian");
    assertCorrectOutput("Porter", "porter");
    assertCorrectOutput("Portuguese", "portuguese");
    assertCorrectOutput("Romanian", "romanian");
    assertCorrectOutput("Russian", "russian");
    assertCorrectOutput("Spanish", "spanish");
    assertCorrectOutput("Swedish", "swedish");
    assertCorrectOutput("Turkish", "turkish");
  }
  private void assertCorrectOutput(String snowballLanguage, String dataDirectory)
      throws IOException {
    if (VERBOSE) System.out.println("checking snowball language: " + snowballLanguage);
    TokenStream filter = new SnowballFilter(tokenizer, snowballLanguage);
    InputStream voc = zipFile.getInputStream(zipFile.getEntry(dataDirectory + "/voc.txt"));
    InputStream out = zipFile.getInputStream(zipFile.getEntry(dataDirectory + "/output.txt"));
    BufferedReader vocReader = new BufferedReader(new InputStreamReader(
        voc, "UTF-8"));
    BufferedReader outputReader = new BufferedReader(new InputStreamReader(
        out, "UTF-8"));
    String inputWord = null;
    while ((inputWord = vocReader.readLine()) != null) {
      String expectedWord = outputReader.readLine();
      assertNotNull(expectedWord);
      tokenizer.reset(new StringReader(inputWord));
      filter.reset();
      assertTokenStreamContents(filter, new String[] {expectedWord});
    }
    vocReader.close();
    outputReader.close();
  }
}
