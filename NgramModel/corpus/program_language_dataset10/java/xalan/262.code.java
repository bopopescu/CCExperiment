package org.apache.xalan.xsltc.cmdline;
import java.io.File;
import java.net.URL;
import java.util.Vector;
import org.apache.xalan.xsltc.cmdline.getopt.GetOpt;
import org.apache.xalan.xsltc.cmdline.getopt.GetOptsException;
import org.apache.xalan.xsltc.compiler.XSLTC;
import org.apache.xalan.xsltc.compiler.util.ErrorMsg;
public final class Compile {
    private static int VERSION_MAJOR = 1;
    private static int VERSION_MINOR = 4;
    private static int VERSION_DELTA = 0;
    public static void printUsage() {
        StringBuffer vers = new StringBuffer("XSLTC version " + 
	    VERSION_MAJOR + "." + VERSION_MINOR + 
	    ((VERSION_DELTA > 0) ? ("."+VERSION_DELTA) : ("")));
	System.err.println(vers + "\n" + 
		new ErrorMsg(ErrorMsg.COMPILE_USAGE_STR));
    }
    public static void main(String[] args) {
	try {
	    boolean inputIsURL = false;
	    boolean useStdIn = false;
	    boolean classNameSet = false;
	    final GetOpt getopt = new GetOpt(args, "o:d:j:p:uxhsinv");
	    if (args.length < 1) printUsage();
	    final XSLTC xsltc = new XSLTC();
	    xsltc.init();
	    int c;
	    while ((c = getopt.getNextOption()) != -1) {
		switch(c) {
		case 'i':
		    useStdIn = true;
		    break;
		case 'o':
		    xsltc.setClassName(getopt.getOptionArg());
		    classNameSet = true;
		    break;
		case 'd':
		    xsltc.setDestDirectory(getopt.getOptionArg());
		    break;
		case 'p':
		    xsltc.setPackageName(getopt.getOptionArg());
		    break;
		case 'j':  
		    xsltc.setJarFileName(getopt.getOptionArg());
		    break;
		case 'x':
		    xsltc.setDebug(true);
		    break;
		case 'u':
		    inputIsURL = true;
		    break;
		case 'n':
		    xsltc.setTemplateInlining(true);	
		    break;
		case 'v':
		case 'h':
		default:
		    printUsage();
		    break; 
		}
	    }
	    boolean compileOK;
	    if (useStdIn) {
		if (!classNameSet) {
		    System.err.println(new ErrorMsg(ErrorMsg.COMPILE_STDIN_ERR));
		}
		compileOK = xsltc.compile(System.in, xsltc.getClassName());
	    }
	    else {
		final String[] stylesheetNames = getopt.getCmdArgs();
		final Vector   stylesheetVector = new Vector();
		for (int i = 0; i < stylesheetNames.length; i++) {
		    final String name = stylesheetNames[i];
		    URL url;
		    if (inputIsURL)
			url = new URL(name);
		    else
			url = (new File(name)).toURL();
		    stylesheetVector.addElement(url);
		}
		compileOK = xsltc.compile(stylesheetVector);
	    }
	    if (compileOK) {
		xsltc.printWarnings();
		if (xsltc.getJarFileName() != null) xsltc.outputToJar();
	    }
	    else {
		xsltc.printWarnings();
		xsltc.printErrors();
	    }
	}
	catch (GetOptsException ex) {
	    System.err.println(ex);
	    printUsage(); 
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
