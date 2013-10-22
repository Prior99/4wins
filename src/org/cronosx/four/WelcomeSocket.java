package org.cronosx.four;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;

import org.cronosx.websockets.WebSocket;

public class WelcomeSocket extends Thread
{
	private ServerSocket serv;
	private List<WebSocket> clients;
	private FourServer server;
	private WelcomeWebsocketListener wListen;
	
	public WelcomeSocket(FourServer server)
	{
		this.server = server;
		wListen = new WelcomeWebsocketListener(server);
		clients = new LinkedList<WebSocket>();
		start();
	}
	
	public void run()
	{
		System.out.println("Welcomesocket opened");
		try
		{
			serv = new ServerSocket(server.getConfig().getInt("port", 2700));
			while(!isInterrupted())
			{
				Socket s = serv.accept();
				WebSocket w = new WebSocket(s);
				w.setWebSocketListener(wListen);
				clients.add(w);
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		} 
		catch (NoSuchAlgorithmException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void shutdown()
	{
		for(WebSocket sc : clients)
		{
			try 
			{
				sc.close();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		try 
		{
			serv.close();
		} 
		catch (IOException e) 
		{
		}
		interrupt();
	}
}
