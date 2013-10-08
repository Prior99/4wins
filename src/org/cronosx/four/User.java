package org.cronosx.four;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class User
{
	private String name;
	private String password;
	private int win;
	private int lose;
	private int registered;
	private int loggedIn;
	private boolean online;
	
	public User(DataInputStream in) throws IOException
	{
		name = in.readUTF();
		password = in.readUTF();
		win = in.readInt();
		lose = in.readInt();
		registered = in.readInt();
		loggedIn = in.readInt();
		online = in.readBoolean();
	}
	
	public User(String name, String password)
	{
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
	
	
}
