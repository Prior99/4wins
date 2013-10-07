package org.cronosx.four;

import org.cronosx.server.DefaultWebSocketListener;
import org.cronosx.websockets.WebSocket;

public class WelcomeWebsocketListener extends DefaultWebSocketListener
{
	private FourServer server;
	public WelcomeWebsocketListener(FourServer server)
	{
		super(server);
		this.server = server;
	}

	@Override
	public void onHandshakeSuccessfull(WebSocket origin)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void parseMessage(String s, WebSocket origin)
	{
		// TODO Auto-generated method stub
		
	}
	
}
