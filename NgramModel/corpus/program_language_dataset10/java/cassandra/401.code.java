package org.apache.cassandra.tools;
import java.io.IOException;
import org.apache.cassandra.config.ConfigurationException;
public class SchemaTool
{
    public static void main(String[] args)
    throws NumberFormatException, IOException, InterruptedException, ConfigurationException
    {
        if (args.length < 3 || args.length > 3)
            usage();
        String host = args[0];
        int port = 0;
        try
        {
            port = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException e)
        {
            System.err.println("Port must be a number.");
            System.exit(1);
        }
        if ("import".equals(args[2]))
            new NodeProbe(host, port).loadSchemaFromYAML();
        else if ("export".equals(args[2]))
            System.out.println(new NodeProbe(host, port).exportSchemaToYAML());
        else
            usage();
    }
    private static void usage()
    {
        System.err.printf("java %s <host> <port> import|export%n", SchemaTool.class.getName());
        System.exit(1);
    }
}
