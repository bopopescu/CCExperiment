package org.apache.log4j.net;
import java.net.Socket;
import java.net.ServerSocket;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.MDC;
import org.apache.log4j.helpers.LogLog;
public class ShortSocketServer  {
  static Logger cat = Logger.getLogger(ShortSocketServer.class);
  public 
  static 
  void main(String args[]) throws Exception {
    int totalTests = 0;
    String prefix = null;
    if(args.length == 2) {
      totalTests = Integer.parseInt(args[0]);
      prefix = args[1];
    } else {
      usage("Wrong number of arguments."); 
    }
      LogLog.debug("Listening on port " + SocketServerTestCase.PORT);
      ServerSocket serverSocket = new ServerSocket(SocketServerTestCase.PORT);
      MDC.put("hostID", "shortSocketServer");
      for(int i = 1; i <= totalTests; i++) {
	PropertyConfigurator.configure(prefix+i+".properties");
	LogLog.debug("Waiting to accept a new client.");
	Socket socket = serverSocket.accept();
	LogLog.debug("Connected to client at " + socket.getInetAddress());
	LogLog.debug("Starting new socket node.");	
	SocketNode sn = new SocketNode(socket, LogManager.getLoggerRepository());
	Thread t = new Thread(sn);
	t.start(); 
	t.join();
      }
  }
  static
  void usage(String msg) {
    System.err.println(msg);
    System.err.println(
      "Usage: java " +ShortSocketServer.class.getName() + " totalTests configFilePrefix");
    System.exit(1);
  }    
}
