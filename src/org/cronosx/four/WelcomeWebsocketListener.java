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
		
	}

	@Override
	protected void parseMessage(String s, WebSocket origin)
	{
		String[] param = s.split(";");
		if(param.length > 0)
		{
			if(param[0].equals("login") && param.length == 3)
			{
				User user = server.getUsermanager().login(param[1], param[2]);
				if(user != null)
				{
					origin.send("login;ok");
					origin.setWebSocketListener(user);
				}
				else
				{
					origin.send("login;failed");
				}
			}
			else if(param[0].equals("register") && param.length == 3)
			{
				if(server.getUsermanager().register(param[1], param[2]))
				{
					origin.send("register;ok");
				}
				else
				{
					origin.send("register;failed");
				}
			}
			else
			{
				server.getLog().error("Received unknown command.");
			}
		}
	}
	
}
