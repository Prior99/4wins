package org.cronosx.four;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class Game
{
	private int width;
	private int height;
	private byte[] area;
	private User[] users;
	private double[] elo;
	private FourServer server;
	private boolean started;
	private int next;
	private int id;
	
	private int x1, x2, y1, y2;
	
	public Game(int width, int height, FourServer server, User u1, User u2)
	{
		users = new User[2];
		users[0] = u1;
		users[1] = u2;
		this.server = server;
		elo = new double[2];
		this.width = width;
		this.height = height;
		area = new byte[width * height];
		started = false;
		next = 0;
		try
		{
			PreparedStatement stmt = server.getDatabase().prepareStatement("INSERT INTO games(width, height, next_turn, area, started, created, last_update) VALUES (?, ?, 0, ?, 0, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			stmt.setInt(1, width);
			stmt.setInt(2, height);
			stmt.setBytes(3, area);
			stmt.setInt(4, (int)(System.currentTimeMillis()/1000));
			stmt.setInt(5, (int)(System.currentTimeMillis()/1000));
			stmt.executeUpdate();
			ResultSet keys = stmt.getGeneratedKeys();
			keys.next();
			id = keys.getInt(1);
			stmt.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		u1.joined(this);
		u2.joined(this);
		for(int i = 0; i <= 1; i++)
		{
			try
			{
				PreparedStatement stmt = server.getDatabase().prepareStatement("INSERT INTO players(user, game, elo, number) VALUES (?, ?, ?, ?)");
				stmt.setInt(1, users[i].getID());
				stmt.setInt(2, getID());
				stmt.setInt(3, 0);
				stmt.setInt(4, i);
				stmt.executeUpdate();
				stmt.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public boolean isDeletable()
	{
		return next < 4; 
	}
	
	public Game(int id, FourServer server) throws IOException
	{
		this.id = id;
		this.server = server;
		try
		{
			PreparedStatement stmt = server.getDatabase().prepareStatement("SELECT width, height, next_turn, area, started, created, last_update FROM games WHERE id = ?");
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			rs.first();
			width = rs.getInt("width");
			height = rs.getInt("height");
			next = rs.getInt("next_turn");
			area = rs.getBytes("area");
			started = rs.getBoolean("started");
			//created = rs.getInt("created");
			//lastUpdate = rs.getInt("last_update");
			
			stmt.close();
			
			users = new User[2];
			elo = new double[2];
			
			stmt = server.getDatabase().prepareStatement("SELECT user, elo FROM players WHERE game = ? ORDER BY number ASC");
			stmt.setInt(1, id);
			rs = stmt.executeQuery();
			if(rs.first())
			{
				elo[0] = rs.getDouble("elo");
				users[0] = server.getUsermanager().getUser(rs.getInt("user"));
				if(rs.next())
				{
					elo[1] = rs.getDouble("elo");
					users[1] = server.getUsermanager().getUser(rs.getInt("user"));
				}
			}
			stmt.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void updateDB()
	{
		try
		{
			PreparedStatement stmt = server.getDatabase().prepareStatement("UPDATE games SET next_turn = ?, area = ?, last_update = ?, started = ? WHERE id = ?");
			stmt.setInt(1, next);
			stmt.setBytes(2, area);
			stmt.setInt(3, (int)(System.currentTimeMillis()/1000));
			stmt.setBoolean(4, started);
			stmt.setInt(5, id);
			stmt.executeUpdate();
			stmt.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	public boolean isStarted()
	{
		return started;
	}
	
	
	public String getName()
	{
		return users[0].getName() + " - " + users[1].getName();
	}
	
	public boolean isNext(User u)
	{
		return users[next%2] == u;
	}
	
	public void start()
	{
		if(started)
		{
			
		}
		else
		{
			calcElo();
			started = true;
			for(User u : users)
				u.sendGame(this);
			users[0].nextTurn(this);
		}
	}
	
	/*public void joinUser(User u)
	{
		if(!started)
		{
			if(users[0] == null || users[1] == null)
			{
				int num;
				if(users[0] == null)
				{
					num = 0;
					users[0] = u;
				}
				else
				{
					num = 1;
					users[1] = u;
				}
				u.joined(this);
				for(User u2: users)
					if(u2 != null)u2.sendLobbyJoin(this, u);
			}
			else
				server.getLog().error("Tried to join user on a full lobby");
		}
		else
			server.getLog().error("Tried to join user on started game.");
	}
	*/
	private void calcElo()
	{
		elo[0] = 1/(1 + Math.pow(10, (users[1].getElo() - users[0].getElo()) / 400));
		elo[1] = 1/(1 + Math.pow(10, (users[0].getElo() - users[1].getElo()) / 400));
		try
		{
			for(int i = 0; i <= 1; i++)
			{
				PreparedStatement stmt = server.getDatabase().prepareStatement("UPDATE players SET elo = ? WHERE game = ? AND number = ?");
				stmt.setDouble(1, elo[i]);
				stmt.setInt(2, getID());
				stmt.setInt(3, i);
				stmt.executeUpdate();
				stmt.close();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public byte[] getArea()
	{
		return area;
	}
	
	public User[] getUsers()
	{
		return users;
	}
	
	public int getID()
	{
		return id;
	}
	
	public void place(int column, User user)
	{
		if(started)
		{
			if(users[(next % 2)] != user)
			{
				
			}
			else
			{
				int row = getLeast(column);
				if(column >= 0 && column < width && row >= 0 && row < height)
				{
					area[column * height + row] = (byte)((next % 2) + 1);
					checkWin(column, row);
					for(User u : users)
						u.placed(column, this, area[column * height + row]);
					next++;
					//next = (char)(next % 2);
					users[(next % 2)].nextTurn(this);
					updateDB();
				}
			}
		}
	}
	
	private boolean checkLine(int x, int y, int deltax, int deltay, byte c)
	{
		int r;
		x1 = x; x2 = x;
		y1 = y; y2 = y;
		r = 1;
		for(int i = 1; 
				x + i * deltax < width && 
				x + i * deltax >= 0 && 
				y + i * deltay < height && 
				y + i * deltay >= 0 && 
				area[(x + i * deltax) * height  + (y + i * deltay)] == c; i++)
		{
			x2 = x + i * deltax;
			y2 = y + i * deltay;
			r++;
		}
		for(int i = 1; 
				x - i * deltax < width && 
				x - i * deltax >= 0 && 
				y - i * deltay < height && 
				y - i * deltay >= 0 && 
				area[(x - i * deltax) * height + (y - i *deltay)] == c; i++) 
		{
			x1 = x - i * deltax;
			y1 = y - i * deltay;
			r++;
		}
		return r >= 4;
	}
	
	private void checkWin(int x, int y)
	{
		byte orig = area[x * height + y];
		if(checkLine(x, y, 1, 0, orig) ||
				checkLine(x, y, 0, 1, orig) ||
				checkLine(x, y, 1, 1, orig) ||
				checkLine(x, y, -1, 1, orig))
		{
			server.getGamemanager().removeGame(this);

			int elo1, elo2;
			
			if(orig - 1 == 0)
			{
				elo1 = (int)Math.round(users[0].getElo() + 15 * (1 - elo[0]));
				elo2 = (int)Math.round(users[1].getElo() + 15 * (0 - elo[1]));
				users[0].win(this, elo1);
				users[1].lose(this, elo2);
				users[0].sendWin(users[0], orig, x1, y1, x2, y2);
				users[1].sendWin(users[0], orig, x1, y1, x2, y2);
			}
			else
			{
				elo1 = (int)Math.round(users[0].getElo() + 15 * (0 - elo[0]));
				elo2 = (int)Math.round(users[1].getElo() + 15 * (1 - elo[1]));
				users[0].lose(this, elo1);
				users[1].win(this, elo2);
				users[0].sendWin(users[1], orig, x1, y1, x2, y2);
				users[1].sendWin(users[1], orig, x1, y1, x2, y2);
			}
		}
	}
	
	private int getLeast(int column)
	{
		for(int i = height -1; i >= 0; --i)
		{
			if(area[column * height + i] == 0) return i;
		}
		return -1;
	}
	
}