package org.cronosx.four;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Gamemanager
{
	private FourServer server;
	private List<Game> games;
	private int index;
	
	public Gamemanager(FourServer server)
	{
		this.server = server;
		games = new LinkedList<Game>();
		load();
	}
	
	public boolean isGameExisting(User u1, User u2)
	{
		for(Game g : games)
		{
			User[] us = g.getUsers();
			if((us[0] == u1 && us[1] == u2) || (us[0] == u2 && us[1] == u1))
			{
				return true;
			}
		}
		return false;
	}
	
	public void removeGame(Game g)
	{
		games.remove(g);
	}
	
	public Game getGame(int id)
	{
		return games.get(id);
	}
	
	public Game createGame(User u1, User u2)
	{
		Game g = new Game(index++, 20, 15, server, u1, u2);
		games.add(g);
		return g;
	}
	
	public void load()
	{
		try
		{
			DataInputStream in = new DataInputStream(new FileInputStream(new File("games.dat")));
			index = in.readInt();
			int amt = in.readInt();
			for(int i = 0; i < amt; i++)
			{
				games.add(new Game(in, server));
			}
			in.close();
			server.getLog().log(amt + " games successfully loaded.");
		}
		catch(Exception e)
		{
			server.getLog().error("Load Failed!");
			e.printStackTrace();
		}
	}
	
	public void save()
	{
		try
		{
			DataOutputStream out = new DataOutputStream(new FileOutputStream(new File("games.dat")));
			out.writeInt(index);
			out.writeInt(games.size());
			for(Game g : games)
			{
				g.save(out);
			}
			out.close();
			server.getLog().log(games.size() + " games successfully saved.", 150);
		}
		catch(Exception e)
		{
			server.getLog().error("Save failed!");
			e.printStackTrace();
		}
	}
}
