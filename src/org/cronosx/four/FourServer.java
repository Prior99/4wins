package org.cronosx.four;

import java.sql.Statement;

import org.cronosx.cgi.CGI;
import org.cronosx.cgi.PageHandler;
import org.cronosx.server.DefaultWebSocketListener;
import org.cronosx.server.Server;
import org.cronosx.webserver.Webserver;

public class FourServer extends Server
{
	private Gamemanager gameManager;
	private Usermanager userManager;
	
	public FourServer()
	{
		gameManager = new Gamemanager(this);
		userManager = new Usermanager(this);
	}
	
	@Override
	protected CGI getDefaultCGIHandler()
	{
		return new CGI(webserver, new PageHandlerFour());
	}
	
	public Usermanager getUsermanager()
	{
		return userManager;
	}
	
	public Gamemanager getGamemanager()
	{
		return gameManager;
	}

	@Override
	public DefaultWebSocketListener getDefaultWebSocketListener()
	{
		return new WelcomeWebsocketListener(this);
	}

	@Override
	protected boolean isDatabaseEnabled()
	{
		return false;
	}

	@Override
	public Webserver getDefaultWebserver()
	{
		return new Webserver(getLog(), getConfig(), this);
	}

	@Override
	public void createDatabase(Statement stmt)
	{
		return;
	}
	
	public static void main(String[] args)
	{
		FourServer s = new FourServer();
		s.start();
	}
}
