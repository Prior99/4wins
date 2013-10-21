package org.cronosx.four;

import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import org.cronosx.tools.*;
import org.cronosx.websockets.WebSocket;

public class FourServer
{
	private Gamemanager gameManager;
	private Usermanager userManager;
	private Logger log;
	private Thread saver, welcome;
	private Config config;
	private List<WebSocket> clients;
	private WelcomeWebsocketListener wListen;
	
	public FourServer()
	{
		config = new Config(new File("server.conf"));
		log = new Logger(new File("server.log"), System.out, config.getInt("loglevel", 0));
		wListen = new WelcomeWebsocketListener(this);
		clients = new LinkedList<WebSocket>();
		userManager = new Usermanager(this);
		gameManager = new Gamemanager(this);
		userManager.loadGames();
		saver = new Thread("Saver")
		{
			public void run()
			{
				while(!isInterrupted())
				{
					try
					{
						Thread.sleep(20000);
					}
					catch(InterruptedException e)
					{
						return;
					}
					getGamemanager().save();
					getUsermanager().save();
				}
			}
		};
		saver.start();
		welcome = new Thread("Websocketwelcome")
		{
			private ServerSocket serv;
			public void run()
			{
				try
				{
					serv = new ServerSocket(config.getInt("port", 2700));
					while(!isInterrupted())
					{
						Socket s = serv.accept();
						WebSocket w = new WebSocket(s);
						w.setWebSocketListener(wListen);
						clients.add(w);
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		};
		welcome.start();
	}

	public Logger getLog()
	{
		return log;
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
		FourServer s = new FourServer();
	}
}
