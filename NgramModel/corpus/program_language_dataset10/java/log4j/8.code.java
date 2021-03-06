package org.apache.log4j;
import org.apache.log4j.RollingCalendar;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.helpers.QuietWriter;
import org.apache.log4j.helpers.CountingQuietWriter;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;
import java.util.Date;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.io.File;
import java.io.FilenameFilter;
public class CompositeRollingAppender extends org.apache.log4j.FileAppender
{
	static final int TOP_OF_TROUBLE=-1;
	static final int TOP_OF_MINUTE = 0;
	static final int TOP_OF_HOUR   = 1;
	static final int HALF_DAY      = 2;
	static final int TOP_OF_DAY    = 3;
	static final int TOP_OF_WEEK   = 4;
	static final int TOP_OF_MONTH  = 5;
	static final int BY_SIZE = 1;
	static final int BY_DATE = 2;
	static final int BY_COMPOSITE = 3;
	static final String S_BY_SIZE = "Size";
	static final String S_BY_DATE = "Date";
	static final String S_BY_COMPOSITE = "Composite";
	private String datePattern = "'.'yyyy-MM-dd";
	private String scheduledFilename = null;
	private long nextCheck = System.currentTimeMillis () - 1;
	Date now = new Date();
	SimpleDateFormat sdf;
	RollingCalendar rc = new RollingCalendar();
	int checkPeriod = TOP_OF_TROUBLE;
	protected long maxFileSize = 10*1024*1024;
	protected int maxSizeRollBackups = 0;
	protected int curSizeRollBackups = 0;
	protected int maxTimeRollBackups = -1;
	protected int curTimeRollBackups = 0;
	protected int countDirection = -1;
	protected int rollingStyle = BY_COMPOSITE;
	protected boolean rollDate = true;
	protected boolean rollSize = true;
	protected boolean staticLogFileName = true;
	protected String baseFileName;
	public CompositeRollingAppender()  {
    }
	public CompositeRollingAppender (Layout layout, String filename,
				   String datePattern) throws IOException {
	    this(layout, filename, datePattern, true);
	}
	public CompositeRollingAppender(Layout layout, String filename, boolean append)
									  throws IOException {
	    super(layout, filename, append);
	}
	public CompositeRollingAppender (Layout layout, String filename,
				   String datePattern, boolean append) throws IOException {
	    super(layout, filename, append);
	    this.datePattern = datePattern;
		activateOptions();
	}
	public CompositeRollingAppender(Layout layout, String filename) throws IOException {
	    super(layout, filename);
	}
	public void setDatePattern(String pattern) {
	    datePattern = pattern;
	}
	public String getDatePattern() {
	    return datePattern;
	}
	public int getMaxSizeRollBackups() {
	    return maxSizeRollBackups;
	}
	public long getMaximumFileSize() {
		return maxFileSize;
	}
	public void setMaxSizeRollBackups(int maxBackups) {
	    maxSizeRollBackups = maxBackups;
	}
	public void setMaxFileSize(long maxFileSize) {
	   this.maxFileSize = maxFileSize;
	}
	public void setMaximumFileSize(long maxFileSize) {
		this.maxFileSize = maxFileSize;
	}
	public void setMaxFileSize(String value) {
	    maxFileSize = OptionConverter.toFileSize(value, maxFileSize + 1);
	}
	protected void setQWForFiles(Writer writer) {
	    qw = new CountingQuietWriter(writer, errorHandler);
	}
	int computeCheckPeriod() {
		RollingCalendar c = new RollingCalendar();
		Date epoch = new Date(0);
		if(datePattern != null) {
			for(int i = TOP_OF_MINUTE; i <= TOP_OF_MONTH; i++) {
				String r0 = sdf.format(epoch);
				c.setType(i);
				Date next = new Date(c.getNextCheckMillis(epoch));
				String r1 = sdf.format(next);
				if(r0 != null && r1 != null && !r0.equals(r1)) {
					return i;
				}
			}
		}
		return TOP_OF_TROUBLE; 
	}
	protected void subAppend(LoggingEvent event) {
		if (rollDate) {
			long n = System.currentTimeMillis();
			if (n >= nextCheck) {
				now.setTime(n);
				nextCheck = rc.getNextCheckMillis(now);
				rollOverTime();
			}
		}
		if (rollSize) {
			if ((fileName != null) && ((CountingQuietWriter) qw).getCount() >= maxFileSize) {
			    rollOverSize();
			}
		}
		super.subAppend(event);
	}
	public void setFile(String file)
	{
		baseFileName = file.trim();
		fileName = file.trim();
	}
	public synchronized void setFile(String fileName, boolean append) throws IOException {
		if (!staticLogFileName) {
		    scheduledFilename = fileName = fileName.trim() + sdf.format(now);
			if (countDirection > 0) {
				scheduledFilename = fileName = fileName + '.' + (++curSizeRollBackups);
			}
		}
		super.setFile(fileName, append);
		if(append) {
		  File f = new File(fileName);
		  ((CountingQuietWriter) qw).setCount(f.length());
		}
	}
	public int getCountDirection() {
		return countDirection;
	}
	public void setCountDirection(int direction) {
		countDirection = direction;
	}
	public int getRollingStyle () {
        return rollingStyle;
	}
	public void setRollingStyle(int style) {
	    rollingStyle = style;
		switch (rollingStyle) {
			case BY_SIZE:
				 rollDate = false;
				 rollSize = true;
				 break;
			case BY_DATE:
				 rollDate = true;
				 rollSize = false;
				 break;
			case BY_COMPOSITE:
				 rollDate = true;
				 rollSize = true;
				 break;
			default:
				errorHandler.error("Invalid rolling Style, use 1 (by size only), 2 (by date only) or 3 (both)");
		}
	}
	public boolean getStaticLogFileName() {
	    return staticLogFileName;
	}
	public void setStaticLogFileName(boolean s) {
		staticLogFileName = s;
	}
	public void setStaticLogFileName(String value) {
		setStaticLogFileName(OptionConverter.toBoolean(value, true));
	}
	protected void existingInit() {
		curSizeRollBackups = 0;
		curTimeRollBackups = 0;
		String filter;
		if (staticLogFileName || !rollDate) {
			filter = baseFileName + ".*";
		}
		else {
			filter = scheduledFilename + ".*";
		}
		File f = new File(baseFileName);
		f = f.getParentFile();
		if (f == null)
		   f = new File(".");
		LogLog.debug("Searching for existing files in: " + f);
		String[] files = f.list();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				if (!files[i].startsWith(baseFileName))
				   continue;
				int index = files[i].lastIndexOf(".");
				if (staticLogFileName) {
				   int endLength = files[i].length() - index;
				   if (baseFileName.length() + endLength != files[i].length()) {
					   continue;
				   }
				}
				try {
					int backup = Integer.parseInt(files[i].substring(index + 1, files[i].length()));
					LogLog.debug("From file: " + files[i] + " -> " + backup);
					if (backup > curSizeRollBackups)
					   curSizeRollBackups = backup;
				}
				catch (Exception e) {
					LogLog.debug("Encountered a backup file not ending in .x " + files[i]);
				}
			}
		}
		LogLog.debug("curSizeRollBackups starts at: " + curSizeRollBackups);
		if (staticLogFileName && rollDate) {
			File old = new File(baseFileName);
			if (old.exists()) {
				Date last = new Date(old.lastModified());
				if (!(sdf.format(last).equals(sdf.format(now)))) {
					scheduledFilename = baseFileName + sdf.format(last);
					LogLog.debug("Initial roll over to: " + scheduledFilename);
					rollOverTime();
				}
			}
		}
		LogLog.debug("curSizeRollBackups after rollOver at: " + curSizeRollBackups);
	}
	public void activateOptions() {
		if(datePattern != null) {
			now.setTime(System.currentTimeMillis());
			sdf = new SimpleDateFormat(datePattern);
			int type = computeCheckPeriod();
			rc.setType(type);
			nextCheck = rc.getNextCheckMillis(now);
		} else {
			if (rollDate)
			    LogLog.error("Either DatePattern or rollingStyle options are not set for ["+
			      name+"].");
		}
		existingInit();
		super.activateOptions();
		if (rollDate && fileName != null && scheduledFilename == null)
			scheduledFilename = fileName + sdf.format(now);
	}
	protected void rollOverTime() {
	    curTimeRollBackups++;
		if (staticLogFileName) {
			if (datePattern == null) {
				errorHandler.error("Missing DatePattern option in rollOver().");
				return;
			}
			String dateFormat = sdf.format(now);
			if (scheduledFilename.equals(fileName + dateFormat)) {
				errorHandler.error("Compare " + scheduledFilename + " : " + fileName + dateFormat);
				return;
			}
			this.closeFile();
	        String from, to;
			for (int i = 1; i <= curSizeRollBackups; i++) {
				from = fileName + '.' + i;
				to = scheduledFilename + '.' + i;
				rollFile(from, to);
	        }
			rollFile(fileName, scheduledFilename);
		}
		try {
			curSizeRollBackups = 0; 
			scheduledFilename = fileName + sdf.format(now);
			this.setFile(baseFileName, false);
		}
		catch(IOException e) {
			errorHandler.error("setFile("+fileName+", false) call failed.");
		}
	}
	protected static void rollFile(String from, String to) {
		File target = new File(to);
		if (target.exists()) {
			LogLog.debug("deleting existing target file: " + target);
			target.delete();
		}
		File file = new File(from);
		file.renameTo(target);
		LogLog.debug(from +" -> "+ to);
	}
	protected static void deleteFile(String fileName) {
		File file = new File(fileName);
		if (file.exists()) {
		   file.delete();
		}
	}
	protected void rollOverSize() {
		File file;
		this.closeFile(); 
		LogLog.debug("rolling over count=" + ((CountingQuietWriter) qw).getCount());
		LogLog.debug("maxSizeRollBackups = " + maxSizeRollBackups);
		LogLog.debug("curSizeRollBackups = " + curSizeRollBackups);
		LogLog.debug("countDirection = " + countDirection);
		if (maxSizeRollBackups != 0) {
			if (countDirection < 0) {
				if (curSizeRollBackups == maxSizeRollBackups) {
				    deleteFile(fileName + '.' + maxSizeRollBackups);
					curSizeRollBackups--;
				}
				for (int i = curSizeRollBackups; i >= 1; i--) {
					rollFile((fileName + "." + i), (fileName + '.' + (i + 1)));
				}
				curSizeRollBackups++;
				rollFile(fileName, fileName + ".1");
			} 
			else if (countDirection == 0) {
				curSizeRollBackups++;
				now.setTime(System.currentTimeMillis());
				scheduledFilename = fileName + sdf.format(now);
				rollFile(fileName, scheduledFilename);
			}
			else { 
				if (curSizeRollBackups >= maxSizeRollBackups && maxSizeRollBackups > 0) {
					int oldestFileIndex = curSizeRollBackups - maxSizeRollBackups + 1;
					deleteFile(fileName + '.' + oldestFileIndex);
				}
				if (staticLogFileName) {
					curSizeRollBackups++;
					rollFile(fileName, fileName + '.' + curSizeRollBackups);
				}
			}
		}
		try {
			this.setFile(baseFileName, false);
		}
		catch(IOException e) {
			LogLog.error("setFile("+fileName+", false) call failed.", e);
		}
	}
}