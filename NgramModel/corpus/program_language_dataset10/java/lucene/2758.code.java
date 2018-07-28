package org.apache.solr.client.solrj.request;
import junit.framework.Assert;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
public class TestUpdateRequestCodec {
  @Test
  public void simple() throws IOException {
    UpdateRequest updateRequest = new UpdateRequest();
    updateRequest.deleteById("*:*");
    updateRequest.deleteById("id:5");
    updateRequest.deleteByQuery("2*");
    updateRequest.deleteByQuery("1*");
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField("id", 1);
    doc.addField("desc", "one", 2.0f);
    doc.addField("desc", "1");
    updateRequest.add(doc);
    doc = new SolrInputDocument();
    doc.addField("id", 2);
    doc.setDocumentBoost(10.0f);
    doc.addField("desc", "two", 3.0f);
    doc.addField("desc", "2");
    updateRequest.add(doc);
    doc = new SolrInputDocument();
    doc.addField("id", 3);
    doc.addField("desc", "three", 3.0f);
    doc.addField("desc", "3");
    updateRequest.add(doc);
    updateRequest.deleteById("2");
    updateRequest.deleteByQuery("id:3");
    JavaBinUpdateRequestCodec codec = new JavaBinUpdateRequestCodec();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    codec.marshal(updateRequest, baos);
    final List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
    JavaBinUpdateRequestCodec.StreamingDocumentHandler handler = new JavaBinUpdateRequestCodec.StreamingDocumentHandler() {
      public void document(SolrInputDocument document, UpdateRequest req) {
        Assert.assertNotNull(req.getParams());
        docs.add(document);
      }
    };
    UpdateRequest updateUnmarshalled = codec.unmarshal(new ByteArrayInputStream(baos.toByteArray()) ,handler);
    Assert.assertNull(updateUnmarshalled.getDocuments());
    for (SolrInputDocument document : docs) {
      updateUnmarshalled.add(document);
    }
    for (int i = 0; i < updateRequest.getDocuments().size(); i++) {
      SolrInputDocument inDoc = updateRequest.getDocuments().get(i);
      SolrInputDocument outDoc = updateUnmarshalled.getDocuments().get(i);
      compareDocs(inDoc, outDoc);
    }
    Assert.assertEquals(updateUnmarshalled.getDeleteById().get(0) , updateRequest.getDeleteById().get(0));
    Assert.assertEquals(updateUnmarshalled.getDeleteQuery().get(0) , updateRequest.getDeleteQuery().get(0));
  }
  private void compareDocs(SolrInputDocument docA, SolrInputDocument docB) {
    Assert.assertEquals(docA.getDocumentBoost(), docB.getDocumentBoost());
    for (String s : docA.getFieldNames()) {
      SolrInputField fldA = docA.getField(s);
      SolrInputField fldB = docB.getField(s);
      Assert.assertEquals(fldA.getValue(), fldB.getValue());
      Assert.assertEquals(fldA.getBoost(), fldB.getBoost());
    }
  }
}