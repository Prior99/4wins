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
	private User[] users;
	private double[] elo;
	private FourServer server;
	private boolean started;
	private char next;
	
	private int x1, x2, y1, y2;
	
	public Game(int id, int width, int height, FourServer server, User u1, User u2)
	{
		users = new User[2];
		users[0] = u1;
		users[1] = u2;
		u1.joined(this);
		u2.joined(this);
		this.server = server;
		elo = new double[2];
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
		users = new User[2];
		elo = new double[2];
		elo[0] = in.readDouble();
		elo[1] = in.readDouble();
		area = new char[width][height];
		for(int i = 0; i < width; i++)
		{
			for(int j = 0; j < height; j++)
			{
				area[i][j] = in.readChar();
			}
		}
		//int amount = in.readInt();
		String s = in.readUTF();
		if(s.length() != 0)
			users[0] = server.getUsermanager().getUser(s);
		s = in.readUTF();
		if(s.length() != 0)
			users[1] = server.getUsermanager().getUser(s);
		started = in.readBoolean();
	}
	
	public void save(DataOutputStream out) throws IOException
	{
		out.writeInt(id);
		out.writeInt(width);
		out.writeInt(height);
		out.writeDouble(elo[0]);
		out.writeDouble(elo[1]);
		for(int i = 0; i < width; i++)
		{
			for(int j = 0; j < height; j++)
			{
				out.writeChar(area[i][j]);
			}
		}
		if(users[0] != null) 
			out.writeUTF(users[0].getName());
		else 
			out.writeUTF("");
		if(users[1] != null) 
			out.writeUTF(users[1].getName());
		else 
			out.writeUTF("");
		out.writeBoolean(started);
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
		return users[next] == u;
	}
	
	public void start()
	{
		if(started)
		{
			server.getLog().error("Tried to start a game that was already started");
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
	
	public void joinUser(User u)
	{
		if(!started)
		{
			if(users[0] == null || users[1] == null)
			{
				if(users[0] == null)
					users[0] = u;
				else
				{
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
	
	private void calcElo()
	{
		elo[0] = 1/(1 + Math.pow(10, (users[1].getElo() - users[0].getElo()) / 400));
		elo[1] = 1/(1 + Math.pow(10, (users[0].getElo() - users[1].getElo()) / 400));
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
			if(users[next] != user)
			{
				server.getLog().log("User tried to set whose turn it isn't (" + (int)next + ")");
			}
			else
			{
				int row = getLeast(column);
				if(column >= 0 && column < width && row >= 0 && row < height)
				{
					area[column][row] = (char)(next + 1);
					checkWin(column, row);
					for(User u : users)
						u.placed(column, this, area[column][row]);
					next++;
					next = (char)(next % 2);
					users[next].nextTurn(this);
				}
			}
		}
		else
			server.getLog().error("Tried to place coin on lobby game.");
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
			if(area[column][i] == 0) return i;
		}
		return -1;
	}
	
}