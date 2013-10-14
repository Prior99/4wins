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
	private List<Integer> tmpGameIDs;
	private int elo;
	
	public int getElo()
	{
		return elo;
	}
	
	public User(DataInputStream in, FourServer server) throws IOException
	{
		tmpGameIDs = new LinkedList<Integer>();
		games = new LinkedList<Game>();
		this.server = server;
		name = in.readUTF();
		password = in.readUTF();
		win = in.readInt();
		lose = in.readInt();
		registered = in.readInt();
		loggedIn = in.readInt();
		online = in.readBoolean();
		elo = in.readInt();
		int amount = in.readInt();
		for(int i = 0; i < amount; i++)
		{
			tmpGameIDs.add(in.readInt());
		}
	}
	
	public void loadGames()
	{
		for(Integer i : tmpGameIDs)
		{
			Game g = server.getGamemanager().getGame(i);
			games.add(g);
		}
	}
	
	public User(String name, String password, FourServer server)
	{
		games = new LinkedList<Game>();
		this.server = server;
		this.name = name;
		this.password = password;
		registered = (int)(System.currentTimeMillis()/1000);
		loggedIn = win = lose = 0;
		online = false;
		elo = 1000;
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
		out.writeInt(elo);
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
	
	public void win(Game g, int elo)
	{
		this.elo = elo;
		games.remove(g);
		win ++;
		sendGameList();
	}
	
	
	public void lose(Game g, int elo)
	{
		this.elo = elo;
		games.remove(g);
		lose ++;
		sendGameList();
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
			StringBuilder sb = new StringBuilder("games;"+getWins()+";"+getLosses()+";"+this.getRegistered()+";"+this.getName()+";"+this.getElo());
			for(Game g : games)
			{
				if(g != null)
				sb.append(";").append(g.getID()).append(";").append(g.getName());
			}
			send(sb.toString());
		}
	}
	
	public void placed(int col, Game g, char u)
	{
		send("placed;"+g.getID()+";"+col+";"+(int)u);
	}
	
	private Game getGame(int id)
	{
		for(Game g : games)
		{
			if(g.getID() == id)
			{
				return g;
			}
		}
		return null;
	}
	
	
	@Override
	public void onMessage(String s, WebSocket origin)
	{
		socket = origin;
		String[] param = s.split(";");
		if(param.length > 0)
		{
			if(param[0].equals("challenge") && param.length == 2)
			{
				User u = server.getUsermanager().getUser(param[1]);
				if(u != null && u != this)
				{
					if(games.size() < 10 && !server.getGamemanager().isGameExisting(u, this))
					{
						server.getGamemanager().createGame(this, u);
					}
					sendGameList();
				}
				else
				{
					send("nouser;"+param[1]);
				}
			}
			if(param[0].equals("delete") && param.length ==2)
			{
				try
				{
					int id = Integer.parseInt(param[1]);
					Game g = getGame(id);
					if(g != null)
					{
						for(User u : g.getUsers())
						{
							u.games.remove(g);
							u.sendGameList();
						}
						server.getGamemanager().removeGame(g);
					}
				}
				catch(Exception e)
				{
					server.getLog().error("This is not a number");
				}
			}
			if(param[0].equals("highscore") && param.length == 1)
			{
				User[] us = server.getUsermanager().getUsersSorted();
				StringBuilder sb = new StringBuilder("highscore");
				for(User u:us)
				{
					sb.append(";").append(u.getName()).append(";").append(u.getWins()).append(";").append(u.getLosses()).append(";").append(u.getElo());
				}
				send(sb.toString());
			}
			if(param[0].equals("start") && param.length == 2)
			{
				try
				{
					int id = Integer.parseInt(param[1]);
					Game g = getGame(id);
					if(g != null) g.start();
					else sendGameList();
				}
				catch(Exception e)
				{
					server.getLog().error("This is not a number");
				}
			}
			if(param[0].equals("set") && param.length == 3)
			{
				try
				{
					int id = Integer.parseInt(param[1]);
					int col = Integer.parseInt(param[2]);
					Game g = getGame(id);
					if(g != null) g.place(col, this);
					else sendGameList();
				}
				catch(Exception e)
				{
					server.getLog().error("This is not a number");
					e.printStackTrace();
				}
			}
			if(param[0].equals("games") && param.length == 1)
			{
				sendGameList();
			}
			if(param[0].equals("game") && param.length == 2)
			{
				try
				{
					int id = Integer.parseInt(param[1]);
					Game g = getGame(id);
					if(g != null)
					{
						if(g.isStarted())
							sendGame(g);
						else
							sendLobby(g);
					}
					else sendGameList();
				}
				catch(Exception e)
				{
					server.getLog().error("This is not a number");
					e.printStackTrace();
				}
			}
		}
	}

	public void sendLobby(Game g)
	{
		StringBuilder sb = new StringBuilder("lobby;"+g.getID());
		for(User u : g.getUsers())
		{
			if(u != null)
			sb.append(";").append(u.getName()).append(";").append(u.getLosses()).append(";").append(u.getWins()).append(";").append(u.getElo());
		}
		send(sb.toString());
	}
	
	public void sendLobbyJoin(Game g, User u)
	{
		send("lobbyjoin;"+g.getID()+";"+u.getName()+";"+u.getLosses()+";"+u.getWins()+";"+u.getElo());
	}
	
	public void sendGame(Game g)
	{
		char[][] area = g.getArea();
		User[] users = g.getUsers();
		StringBuilder sb = new StringBuilder("game;").append(g.getWidth()).append(";").append(g.getHeight()).append(";");
		for(int i = 0; i < g.getWidth(); i++)
		{
			for(int j = 0; j < g.getHeight(); j++)
			{
				sb.append((char)('A' + area[i][j])+"");
			}
		}
		sb.append(";").append(g.isNext(this));
		sb.append(";").append(users.length);
		for(User u : users)
			sb.append(";").append(u.getName());
		send(sb.toString());
	}
	
	public void sendWin(User u, char id, int x1, int y1, int x2, int y2)
	{
		send("win;"+u.getName()+";"+id+";"+x1+";"+y1+";"+x2+";"+y2);
	}
	
	public void nextTurn(Game g)
	{
		send("turn;"+g.getID());
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
	public void send(String s)
	{
		if(socket != null)
			socket.send(s);
	}
	
}
