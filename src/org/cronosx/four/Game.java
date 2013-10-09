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
	
	public Game(int id, int width, int height, FourServer server)
	{
		this.server = server;
		users = new LinkedList<User>();
		this.width = width;
		this.height = height;
		area = new char[width][height];
		for(int x = 0; x < width; x++)
		{
			for(int y = 0; y < height; y++)
			{
				area[x][y] = (char)(int)(Math.random()*4);
			}
		}
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
				u.startGame(this);
		}
	}
	
	public void joinUser(User u)
	{
		if(!started)
		{
			users.add(u);
			u.joined(this);
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
				server.getLog().log("User tried to set whose turn it isn't");
			}
			else
			{
				int row = getLeast(column);
				area[column][row] = (char)users.indexOf(user);
				win(checkWin());
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
		if(u == null) return;
		else
		{
			server.getLog().log("Game won: " + id);
			u.win();
		}
	}
	
	private User checkWin()
	{
		for(int i = 0; i < width; i++)
		{
			for(int j = 0; j < height; j++)
			{
				char u = area[i][j];
				for(int is = -1; is <= 1; is++)
				{
					for(int js = -1; js <= 1; js++)
					{
						boolean found = true;
						int ii = i;
						int jj = j;
						for(int s = 0; s < 4; s++)
						{
							ii += is;
							jj += js;
							if(area[ii][jj] != u)
							{
								found = false;
								break;
							}
						}
						if(found)
						{
							return users.get(u);
						}
					}
				}
			}
		}
		return null;
	}
	
	private int getLeast(int column)
	{
		for(int i = height; i > 0; --i)
		{
			if(area[column][i] == 0) return i;
		}
		return -1;
	}
	
}
