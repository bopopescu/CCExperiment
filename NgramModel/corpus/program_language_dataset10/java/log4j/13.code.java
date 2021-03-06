 package org.apache.log4j;
import java.io.File;
import java.io.Writer;
import java.io.FileWriter;
import java.io.BufferedWriter;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.spi.ErrorHandler;
public class TempFileAppender extends AppenderSkeleton {
  static final public String PATH_OPTION = "Path";
  protected String path = null;
  static final public String PREFIX_OPTION = "Prefix";
  protected String prefix = "l4j_";
  static final public String SUFFIX_OPTION = "Suffix";
  protected String suffix = ".tmp";
  protected File dir = null;
  public TempFileAppender() {
      super();
  }
  public String[] getOptionStrings() {
      return OptionConverter.concatanateArrays(super.getOptionStrings(),
                 new String[] {PATH_OPTION,PREFIX_OPTION,SUFFIX_OPTION});
  }  
  public void setOption(String key, String value) {
      super.setOption(key, value);
      if(key.equalsIgnoreCase(PATH_OPTION)) {
	  path = value;
	  if(path==null) {
              errorHandler.error("Path cannot be empty!",null,0);
	  }
	  dir = new File(path);
	  if(!(dir.exists() && dir.isDirectory() && dir.canWrite())) {
              errorHandler.error("Cannot write to directory " + path + "!",null,0);
	  }
      }
      else if(key.equalsIgnoreCase(PREFIX_OPTION)) {
          if(value!=null && value.length()>=3) {
	      prefix = value;
	  } else {
              errorHandler.error("Prefix cannot be shorter than 3 characters!",
	                         null,0);
	  }
      }
      else if(key.equalsIgnoreCase(SUFFIX_OPTION)) {
          if(value!=null && value.length()>=1) {
	      suffix = value;
	  } else {
              errorHandler.error("Suffix cannot be empty!",null,0);
	  }
      }
  }
  public void append(LoggingEvent event) { 
      if(!checkEntryConditions()) {
          return;
      }
      subAppend(event);
  }
  protected boolean checkEntryConditions() {
      return true;
  }   
  protected void subAppend(LoggingEvent event) {
      try {
          File tmp = File.createTempFile(prefix,suffix,dir);
	  Writer out = new BufferedWriter(new FileWriter(tmp));
	  out.write(event.message);
	  out.close();
      } catch (Exception e) {
          errorHandler.error("Error during creation of temporary File!",e,1);
      }
  }
  public boolean requiresLayout() {
      return false;
  }
  public void close() {
  }
} 
