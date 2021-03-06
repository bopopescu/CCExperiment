package org.apache.cassandra.cli;
import jline.SimpleCompletor;
public class CliCompleter extends SimpleCompletor
{
    private static String[] commands = {
            "connect",
            "describe keyspace",
            "exit",
            "help",
            "quit",
            "show cluster name",
            "show keyspaces",
            "show api version",
            "create keyspace",
            "create column family",
            "drop keyspace",
            "drop column family",
            "rename keyspace",
            "rename column family",
            "help connect",
            "help describe keyspace",
            "help exit",
            "help help",
            "help quit",
            "help show cluster name",
            "help show keyspaces",
            "help show api version",
            "help create keyspace",
            "help create column family",
            "help drop keyspace",
            "help drop column family",
            "help rename keyspace",
            "help rename column family",
            "help get",
            "help set",
            "help del",
            "help count",
            "help list",
            "help truncate"
    };
    private static String[] keyspaceCommands = {
            "get",
            "set",
            "count",
            "del",
            "list",
            "truncate"
    };
    public CliCompleter()
    {
        super(commands);
    }
    String[] getKeyspaceCommands()
    {
        return keyspaceCommands;
    }
}
