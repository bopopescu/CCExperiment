package org.apache.solr.handler.admin;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.ContentStreamBase;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.handler.RequestHandlerUtils;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.RawResponseWriter;
import org.apache.solr.response.SolrQueryResponse;
public class ShowFileRequestHandler extends RequestHandlerBase
{
  public static final String HIDDEN = "hidden";
  public static final String USE_CONTENT_TYPE = "contentType";
  protected Set<String> hiddenFiles;
  private static ShowFileRequestHandler instance;
  public ShowFileRequestHandler()
  {
    super();
    instance = this; 
  }
  @Override
  public void init(NamedList args) {
    super.init( args );
    ModifiableSolrParams params = new ModifiableSolrParams( invariants );
    if( params.get( CommonParams.WT ) == null ) {
      params.set( CommonParams.WT, "raw" );
    }
    this.invariants = params;
    hiddenFiles = new HashSet<String>();
    if( invariants != null ) {
      String[] hidden = invariants.getParams( HIDDEN );
      if( hidden != null ) {
        for( String s : hidden ) {
          hiddenFiles.add( s.toUpperCase() );
        }
      }
    }
  }
  public Set<String> getHiddenFiles()
  {
    return hiddenFiles;
  }
  @Override
  public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws IOException 
  {
    File adminFile = null;
    final SolrResourceLoader loader = req.getCore().getResourceLoader();
    File configdir = new File( loader.getConfigDir() ); 
    String fname = req.getParams().get("file", null);
    if( fname == null ) {
      adminFile = configdir;
    }
    else {
      fname = fname.replace( '\\', '/' ); 
      if( hiddenFiles.contains( fname.toUpperCase() ) ) {
        throw new SolrException( ErrorCode.FORBIDDEN, "Can not access: "+fname );
      }
      if( fname.indexOf( ".." ) >= 0 ) {
        throw new SolrException( ErrorCode.FORBIDDEN, "Invalid path: "+fname );  
      }
      adminFile = new File( configdir, fname );
    }
    if( !adminFile.exists() ) {
      throw new SolrException( ErrorCode.BAD_REQUEST, "Can not find: "+adminFile.getName() 
          + " ["+adminFile.getAbsolutePath()+"]" );
    }
    if( !adminFile.canRead() || adminFile.isHidden() ) {
      throw new SolrException( ErrorCode.BAD_REQUEST, "Can not show: "+adminFile.getName() 
          + " ["+adminFile.getAbsolutePath()+"]" );
    }
    if( adminFile.isDirectory() ) {
      int basePath = configdir.getAbsolutePath().length() + 1;
      NamedList<SimpleOrderedMap<Object>> files = new SimpleOrderedMap<SimpleOrderedMap<Object>>();
      for( File f : adminFile.listFiles() ) {
        String path = f.getAbsolutePath().substring( basePath );
        path = path.replace( '\\', '/' ); 
        if( hiddenFiles.contains( path.toUpperCase() ) ) {
          continue; 
        }
        if( f.isHidden() || f.getName().startsWith( "." ) ) {
          continue; 
        }
        SimpleOrderedMap<Object> fileInfo = new SimpleOrderedMap<Object>();
        files.add( path, fileInfo );
        if( f.isDirectory() ) {
          fileInfo.add( "directory", true ); 
        }
        else {
          fileInfo.add( "size", f.length() );
        }
        fileInfo.add( "modified", new Date( f.lastModified() ) );
      }
      rsp.add( "files", files );
    }
    else {
      ContentStreamBase content = new ContentStreamBase.FileStream( adminFile );
      content.setContentType( req.getParams().get( USE_CONTENT_TYPE ) );
      rsp.add( RawResponseWriter.CONTENT, content );
    }
    rsp.setHttpCaching(false);
  }
  @Deprecated
  public static String getFileContents( String path )
  {
    if( instance != null && instance.hiddenFiles != null ) {
      if( instance.hiddenFiles.contains( path ) ) {
        return ""; 
      }
    }
    try {
      SolrCore core = SolrCore.getSolrCore();
      InputStream input = core.getResourceLoader().openResource(path);
      return IOUtils.toString( input );
    }
    catch( Exception ex ) {} 
    return "";
  }
  @Override
  public String getDescription() {
    return "Admin Get File -- view config files directly";
  }
  @Override
  public String getVersion() {
      return "$Revision: 898152 $";
  }
  @Override
  public String getSourceId() {
    return "$Id: ShowFileRequestHandler.java 898152 2010-01-12 02:19:56Z ryan $";
  }
  @Override
  public String getSource() {
    return "$URL: http://svn.apache.org/repos/asf/lucene/solr/branches/newtrunk/solr/src/java/org/apache/solr/handler/admin/ShowFileRequestHandler.java $";
  }
}
