package org.apache.solr.client.solrj.request;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.FastInputStream;
import org.apache.solr.common.util.JavaBinCodec;
import org.apache.solr.common.util.NamedList;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
public class JavaBinUpdateRequestCodec {
  public void marshal(UpdateRequest updateRequest, OutputStream os) throws IOException {
    NamedList nl = new NamedList();
    NamedList params = solrParamsToNamedList(updateRequest.getParams());
    if (updateRequest.getCommitWithin() != -1) {
      params.add("commitWithin", updateRequest.getCommitWithin());
    }
    Iterator<SolrInputDocument> docIter = null;
    if (updateRequest.getDocuments() != null) {
      docIter = updateRequest.getDocuments().iterator();
    }
    if(updateRequest.getDocIterator() != null){
      docIter = updateRequest.getDocIterator();
    }
    nl.add("params", params);
    nl.add("delById", updateRequest.getDeleteById());
    nl.add("delByQ", updateRequest.getDeleteQuery());
    nl.add("docs", docIter);
    new JavaBinCodec(){
      public void writeMap(Map val) throws IOException {
        if (val instanceof SolrInputDocument) {
          writeVal(solrInputDocumentToList((SolrInputDocument) val));
        } else {
          super.writeMap(val);
        }
      }
    }.marshal(nl, os);
  }
  public UpdateRequest unmarshal(InputStream is, final StreamingDocumentHandler handler) throws IOException {
    final UpdateRequest updateRequest = new UpdateRequest();
    List<List<NamedList>> doclist;
    List<String> delById;
    List<String> delByQ;
    final NamedList[] namedList = new NamedList[1];
    JavaBinCodec codec = new JavaBinCodec() {
      public NamedList readNamedList(FastInputStream dis) throws IOException {
        int sz = readSize(dis);
        NamedList nl = new NamedList();
        if (namedList[0] == null) {
          namedList[0] = nl;
        }
        for (int i = 0; i < sz; i++) {
          String name = (String) readVal(dis);
          Object val = readVal(dis);
          nl.add(name, val);
        }
        return nl;
      }
      public List readIterator(FastInputStream fis) throws IOException {
        NamedList params = (NamedList) namedList[0].getVal(0);
        updateRequest.setParams(namedListToSolrParams(params));
        if (handler == null) return super.readIterator(fis);
        while (true) {
          Object o = readVal(fis);
          if (o == END_OBJ) break;
          handler.document(listToSolrInputDocument((List<NamedList>) o), updateRequest);
        }
        return Collections.EMPTY_LIST;
      }
    };
    codec.unmarshal(is);
    delById = (List<String>) namedList[0].get("delById");
    delByQ = (List<String>) namedList[0].get("delByQ");
    doclist = (List<List<NamedList>>) namedList[0].get("docs");
    if (doclist != null && !doclist.isEmpty()) {
      List<SolrInputDocument> solrInputDocs = new ArrayList<SolrInputDocument>();
      for (List<NamedList> n : doclist) {
        solrInputDocs.add(listToSolrInputDocument(n));
      }
      updateRequest.add(solrInputDocs);
    }
    if (delById != null) {
      for (String s : delById) {
        updateRequest.deleteById(s);
      }
    }
    if (delByQ != null) {
      for (String s : delByQ) {
        updateRequest.deleteByQuery(s);
      }
    }
    return updateRequest;
  }
  private List<NamedList> solrInputDocumentToList(SolrInputDocument doc) {
    List<NamedList> l = new ArrayList<NamedList>();
    NamedList nl = new NamedList();
    nl.add("boost", doc.getDocumentBoost() == 1.0f ? null : doc.getDocumentBoost());
    l.add(nl);
    Iterator<SolrInputField> it = doc.iterator();
    while (it.hasNext()) {
      nl = new NamedList();
      SolrInputField field = it.next();
      nl.add("name", field.getName());
      nl.add("val", field.getValue());
      nl.add("boost", field.getBoost() == 1.0f ? null : field.getBoost());
      l.add(nl);
    }
    return l;
  }
  private SolrInputDocument listToSolrInputDocument(List<NamedList> namedList) {
    SolrInputDocument doc = new SolrInputDocument();
    for (int i = 0; i < namedList.size(); i++) {
      NamedList nl = namedList.get(i);
      if (i == 0) {
        doc.setDocumentBoost(nl.getVal(0) == null ? 1.0f : (Float) nl.getVal(0));
      } else {
        doc.addField((String) nl.getVal(0),
                nl.getVal(1),
                nl.getVal(2) == null ? 1.0f : (Float) nl.getVal(2));
      }
    }
    return doc;
  }
  private NamedList solrParamsToNamedList(SolrParams params) {
    if (params == null) return new NamedList();
    Iterator<String> it = params.getParameterNamesIterator();
    NamedList nl = new NamedList();
    while (it.hasNext()) {
      String s = it.next();
      nl.add(s, params.getParams(s));
    }
    return nl;
  }
  private ModifiableSolrParams namedListToSolrParams(NamedList nl) {
    ModifiableSolrParams solrParams = new ModifiableSolrParams();
    for (int i = 0; i < nl.size(); i++) {
      List<String> l = (List) nl.getVal(i);
      if (l != null)
        solrParams.add(nl.getName(i),
                (String[]) l.toArray(new String[l.size()]));
    }
    return solrParams;
  }
  public static interface StreamingDocumentHandler {
    public void document(SolrInputDocument document, UpdateRequest req);
  }
}
