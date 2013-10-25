package org.cronosx.four;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.cronosx.tools.*;
import org.cronosx.websockets.WebSocket;

public class FourServer
{
	private Gamemanager gameManager;
	private Usermanager userManager;
	private WelcomeSocket welcome;
	private Config config;
	private Connection conn;
	private CommandlineListener cLine;
	
	public void shutdown()
	{
		try 
		{
			conn.close();
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		welcome.shutdown();
		cLine.shutdown();
	}
	
	public FourServer()
	{
		config = new Config(new File("server.conf"));
		try
		{
			reconnectDB();
			userManager = new Usermanager(this);
			gameManager = new Gamemanager(this);
			userManager.loadGames();
			welcome = new WelcomeSocket(this);
			cLine = new CommandlineListener(this);
		}
		catch(Exception e)
		{
			System.out.println("Could not connect to database");
			e.printStackTrace();
		}
	}
	
	private void reconnectDB() throws SQLException
	{
		conn = DriverManager.getConnection("jdbc:" + config.getStr("db-server", "mysql://localhost") + "/" + config.getStr("db-database", "four")+"?autoReconnect=true", config.getStr("db-user", "root"), config.getStr("db-password", ""));
	}
	
	public Usermanager getUsermanager()
	{
		return userManager;
	}
	
	public Gamemanager getGamemanager()
	{
		return gameManager;
	}

	
	public static void main(String[] args)
	{
		new FourServer();
	}
	
	public Connection getDatabase()
	{
		try
		{
			if(conn.isClosed()) 
				reconnectDB();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return conn;
		
	}
	
	public Config getConfig()
	{
		return config;
	}
}
