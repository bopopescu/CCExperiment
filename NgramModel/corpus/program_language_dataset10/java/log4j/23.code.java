package com.klopotek.utils.log;
import java.sql.*;
public interface JDBCConnectionHandler
{
	Connection getConnection();
   Connection getConnection(String _url, String _username, String _password);
}
