package org.cronosx.four;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Game
{
	private int id;
	private int width;
	private int height;
	private char[][] area;
	private List<User> users;
	private FourServer server;
	private boolean started;
	private char next;
	
	private int x1, x2, y1, y2;
	
	public Game(int id, int width, int height, FourServer server)
	{
		this.server = server;
		users = new LinkedList<User>();
		this.width = width;
		this.height = height;
		area = new char[width][height];
		this.id = id;
		started = false;
		next = 0;
	}

	public Game(DataInputStream in, FourServer server) throws IOException
	{
		this.server = server;
		id = in.readInt();
		width = in.readInt();
		height = in.readInt();
		users = new LinkedList<User>();
		area = new char[width][height];
		for(int i = 0; i < width; i++)
		{
			for(int j = 0; j < height; j++)
			{
				area[i][j] = in.readChar();
			}
		}
		int amount = in.readInt();
		for(int i = 0; i < amount; i++)
		{
			users.add(server.getUsermanager().getUser(in.readUTF()));
		}
		started = in.readBoolean();
	}
	
	public boolean isStarted()
	{
		return started;
	}
	
	public void save(DataOutputStream out) throws IOException
	{
		out.writeInt(id);
		out.writeInt(width);
		out.writeInt(height);
		for(int i = 0; i < width; i++)
		{
			for(int j = 0; j < height; j++)
			{
				out.writeChar(area[i][j]);
			}
		}
		out.writeInt(users.size());
		for(User u:users)
		{
			out.writeUTF(u.getName());
		}
		out.writeBoolean(started);
	}
	
	public void start()
	{
		if(started)
		{
			server.getLog().error("Tried to start a game that was already started");
		}
		else
		{
			started = true;
			for(User u : users)
				u.sendGame(this);
		}
	}
	
	public void joinUser(User u)
	{
		if(!started)
		{
			users.add(u);
			u.joined(this);
			for(User u2: users)
				u2.sendLobbyJoin(this, u);
		}
		else
			server.getLog().error("Tried to join user on started game.");
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public char[][] getArea()
	{
		return area;
	}
	
	public User[] getUsers()
	{
		User[] us = new User[users.size()];
		users.toArray(us);
		return us;
	}
	
	public int getID()
	{
		return id;
	}
	
	public void place(int column, User user)
	{
		if(started)
		{
			if(users.indexOf(user) != next)
			{
				server.getLog().log("User (" + users.indexOf(user) + ") tried to set whose turn it isn't (" + (int)next + ")");
			}
			else
			{
				int row = getLeast(column);
				area[column][row] = (char)(users.indexOf(user) + 1);
				checkWin(column, row);
				for(User u : users)
					u.placed(column, this, area[column][row]);
				next++;
				next = (char)(next % users.size());
				users.get(next).nextTurn(this);
			}
		}
		else
			server.getLog().error("Tried to place coin on lobby game.");
	}
	
	private void win(User u)
	{
	}
	
	private boolean checkLine(int x, int y, int deltax, int deltay, char c)
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
				area[x + i * deltax][y + i * deltay] == c; i++) 
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
				area[x - i * deltax][y - i *deltay] == c; i++) 
		{
			x1 = x - i * deltax;
			y1 = y - i * deltay;
			r++;
		}
		return r >= 4;
	}
	
	private void checkWin(int x, int y)
	{
		char orig = area[x][y];
		if(checkLine(x, y, 1, 0, orig) ||
				checkLine(x, y, 0, 1, orig) ||
				checkLine(x, y, 1, 1, orig) ||
				checkLine(x, y, -1, 1, orig))
		{
			User u = users.get(orig - 1);
			server.getLog().log(u.getName() + " HAS WON!");
			server.getLog().log("Game won: " + id);
			u.win();
			for(User u2 : users) u2.sendWin(u, orig, x1, y1, x2, y2);
		}

	}
	
	private int getLeast(int column)
	{
		for(int i = height -1; i >= 0; --i)
		{
			if(area[column][i] == 0) return i;
		}
		return -1;
	}
	
}
