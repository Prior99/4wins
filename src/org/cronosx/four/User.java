package org.cronosx.four;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.cronosx.websockets.WebSocket;
import org.cronosx.websockets.WebSocketListener;

public class User implements WebSocketListener
{
	private String name;
	private String password;
	private int win;
	private int lose;
	private int registered;
	private int loggedIn;
	private boolean online;
	private WebSocket socket;
	private FourServer server;
	private List<Game> games;
	
	public User(DataInputStream in, FourServer server) throws IOException
	{
		games = new LinkedList<Game>();
		this.server = server;
		name = in.readUTF();
		password = in.readUTF();
		win = in.readInt();
		lose = in.readInt();
		registered = in.readInt();
		loggedIn = in.readInt();
		online = in.readBoolean();
		int amount = in.readInt();
		for(int i = 0; i < amount; i++)
		{
			Game g = server.getGamemanager().getGame(in.readInt());
			games.add(g);
		}
	}
	
	
	
	public User(String name, String password, FourServer server)
	{
		this.server = server;
		this.name = name;
		this.password = password;
		registered = (int)(System.currentTimeMillis()/1000);
		loggedIn = win = lose = 0;
		online = false;
	}
	
	public void save(DataOutputStream out) throws IOException
	{
		out.writeUTF(name);
		out.writeUTF(password);
		out.writeInt(win);
		out.writeInt(lose);
		out.writeInt(registered);
		out.writeInt(loggedIn);
		out.writeBoolean(online);
		out.writeInt(games.size());
		for(Game game : games)
			out.writeInt(game.getID());
	}
	
	public boolean login(String password)
	{
		if(password.equals(this.password))
		{
			online = true;
			loggedIn = (int)(System.currentTimeMillis()/1000);
			return true;
		}
		else
			return false;
	}
	
	public String getName()
	{
		return name;
	}
	
	public int getWins()
	{
		return win;
	}
	
	public int getLosses()
	{
		return lose;
	}
	
	public boolean isOnline()
	{
		return online;
	}
	
	public int getRegistered()
	{
		return registered;
	}
	
	public int getLoggedIn()
	{
		return loggedIn;
	}
	
	public int getGames()
	{
		return win + lose;
	}
	
	public void logout()
	{
		online = false;
	}
	
	public void win()
	{
		win ++;
	}
	
	public void lose()
	{
		lose ++;
	}

	public void joined(Game game)
	{
		games.add(game);
		sendGameList();
	}
	
	private void sendGameList()
	{
		if(socket != null)
		{
			StringBuilder sb = new StringBuilder("games");
			for(Game g : games)
			{
				sb.append(";").append(g.getID());
			}
			socket.send(sb.toString());
		}
	}
	
	public void placed(int col, Game g, char u)
	{
		socket.send("placed;"+g.getID()+";"+col+";"+(int)u);
	}
	
	@Override
	public void onMessage(String s, WebSocket origin)
	{
		socket = origin;
		String[] param = s.split(";");
		if(param.length > 0)
		{
			if(param[0].equals("create") && param.length == 1)
			{
				Game game = server.getGamemanager().createGame();
				game.joinUser(this);
			}
			if(param[0].equals("set") && param.length == 3)
			{
				try
				{
					int id = Integer.parseInt(param[1]);
					int col = Integer.parseInt(param[2]);
					server.getGamemanager().getGame(id).place(col, this);
				}
				catch(Exception e)
				{
					server.getLog().error("This is not a number");
				}
			}
			if(param[0].equals("game") && param.length == 2)
			{
				try
				{
					int id = Integer.parseInt(param[1]);
					Game g = server.getGamemanager().getGame(id);
					char[][] area = g.getArea();
					User[] users = g.getUsers();
					StringBuilder sb = new StringBuilder("game;").append(g.getWidth()).append(";").append(g.getHeight()).append(";");
					for(int i = 0; i < g.getWidth(); i++)
					{
						for(int j = 0; j < g.getHeight(); j++)
						{
							sb.append('A' + area[i][j]);
						}
					}
					sb.append(";").append(users.length);
					for(User u : users)
						sb.append(";").append(u.getName());
					
				}
				catch(Exception e)
				{
					server.getLog().error("This is not a number");
				}
			}
		}
	}
	
	public void nextTurn(Game g)
	{
		socket.send("turn;"+g.getID());
	}

	@Override
	public void onOpen(WebSocket origin) {}

	@Override
	public void onHandshake(WebSocket origin){}

	@Override
	public void onHandshakeSuccessfull(WebSocket origin){}

	@Override
	public void onClose(WebSocket origin)
	{
		socket = null;
		logout();
	}
	
}
