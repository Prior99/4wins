package org.cronosx.four;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
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
	private WebSocket socket;
	private FourServer server;
	private List<Game> games;
	private int elo;
	private int id;
	
	public int getElo()
	{
		return elo;
	}
	
	public User(String name, String password, FourServer server)
	{
		games = new LinkedList<Game>();
		this.server = server;
		this.name = name;
		this.password = password;
		registered = (int)(System.currentTimeMillis()/1000);
		loggedIn = win = lose = 0;
		elo = 1000;
		try
		{
			PreparedStatement stmt = server.getDatabase().prepareStatement("INSERT INTO users(name, password, elo, win, lose, created, last_login) VALUES(?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, name);
			stmt.setString(2, password);
			stmt.setDouble(3, elo);
			stmt.setInt(4, 0);
			stmt.setInt(5, 0);
			stmt.setInt(6, registered);
			stmt.setInt(7, registered);
			stmt.executeUpdate();
			ResultSet rs = stmt.getGeneratedKeys();
			rs.first();
			this.id = rs.getInt(1);
			stmt.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public User(String name, String password, int win, int lose, int registered, int loggedIn, int elo, int id, FourServer server) throws IOException
	{
		games = new LinkedList<Game>();
		this.server = server;
		this.name = name;
		this.password = password;
		this.win = win;
		this.lose = lose;
		this.registered = registered;
		this.loggedIn = loggedIn;
		this.elo = elo;
		this.id = id;
		
	}
	
	public void loadGames()
	{
		try
		{
			PreparedStatement stmt = server.getDatabase().prepareStatement("SELECT game FROM players WHERE user = ?");
			stmt.setInt(1,  id);
			ResultSet rs = stmt.executeQuery();
			while(rs.next())
			{
				Game g = server.getGamemanager().getGame(rs.getInt("game"));
				games.add(g);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean login(String password)
	{
		if(password.equals(this.password))
		{
			loggedIn = (int)(System.currentTimeMillis()/1000);
			updateDB();
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
	}
	
	public void win(Game g, int elo)
	{
		this.elo = elo;
		games.remove(g);
		win ++;
		updateDB();
		sendGameList();
	}
	
	
	public void lose(Game g, int elo)
	{
		this.elo = elo;
		games.remove(g);
		lose ++;
		updateDB();
		sendGameList();
	}
	
	private void updateDB()
	{
		try
		{
			PreparedStatement stmt = server.getDatabase().prepareStatement("UPDATE users SET elo = ?, lose = ?, win = ?, last_login = ? WHERE id = ?");
			stmt.setInt(1, elo);
			stmt.setInt(2, lose);
			stmt.setInt(3, win);
			stmt.setInt(4, (int)(System.currentTimeMillis()/1000));
			stmt.setInt(5, id);
			stmt.executeUpdate();
			stmt.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
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
			StringBuilder sb = new StringBuilder("games;"+getWins()+";"+getLosses()+";"+this.getRegistered()+";"+this.getName()+";"+this.getElo()+";"+server.getUsermanager().isWaitingForDarkroom(this));
			for(Game g : games)
			{
				if(g != null)
				sb.append(";").append(g.getID()).append(";").append(g.getName());
			}
			send(sb.toString());
		}
	}
	
	public void placed(int col, Game g, byte u)
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
	
	public int getActiveGames()
	{
		return games.size();
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
						Game g = server.getGamemanager().createGame(this, u);
						send("created;"+g.getID());
					}
					//sendGameList();
				}
				else
				{
					send("nouser;"+param[1]);
				}
			}
			if(param[0].equals("removeuser") && param.length == 1)
			{
				while(!games.isEmpty())
				{
					Game g = games.remove(0);
					for(User u : g.getUsers())
					{
						u.games.remove(g);
						u.sendGameList();
					}
					server.getGamemanager().removeGame(g);
				}
				server.getUsermanager().removeUser(this);
			}
			if(param[0].equals("delete") && param.length ==2)
			{
				try
				{
					int id = Integer.parseInt(param[1]);
					Game g = getGame(id);
					if(g != null && g.isDeletable())
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
			if(param[0].equals("darkroom"))
			{
				server.getUsermanager().attendDarkRoomGame(this);
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
		byte[] area = g.getArea();
		User[] users = g.getUsers();
		StringBuilder sb = new StringBuilder("game;").append(g.getWidth()).append(";").append(g.getHeight()).append(";");
		for(int i = 0; i < g.getWidth(); i++)
		{
			for(int j = 0; j < g.getHeight(); j++)
			{
				sb.append((char)('A' + area[i * g.getHeight() + j])+"");
			}
		}
		sb.append(";").append(g.isNext(this));
		sb.append(";").append(users.length);
		for(User u : users)
			sb.append(";").append(u.getName());
		send(sb.toString());
	}
	
	public void sendWin(User u, byte id, int x1, int y1, int x2, int y2)
	{
		send("win;"+u.getName()+";"+id+";"+x1+";"+y1+";"+x2+";"+y2);
	}
	
	public int getID()
	{
		return id;
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
