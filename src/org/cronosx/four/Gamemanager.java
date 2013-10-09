package org.cronosx.four;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

public class Gamemanager
{
	private FourServer server;
	private Map<Integer, Game> games;
	private int amount;
	
	public Gamemanager(FourServer server)
	{
		this.server = server;
		games = new HashMap<Integer, Game>();
		load();
	}
	
	public Game getGame(int id)
	{
		return games.get(id);
	}
	
	public Game createGame()
	{
		Game g = new Game(amount, 20, 15, server);
		games.put(amount++, g);
		new Thread()
		{
			public void run()
			{
				save();
				server.getUsermanager().save();
			}
		}.start();
		return g;
	}
	
	public void load()
	{
		try
		{
			DataInputStream in = new DataInputStream(new FileInputStream(new File("games.dat")));
			amount = in.readInt();
			for(int i = 0; i < amount; i++)
			{
				games.put(in.readInt(), new Game(in, server));
			}
			in.close();
			server.getLog().log(amount + " games successfully loaded.");
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
			out.writeInt(amount);
			for(int i : games.keySet())
			{
				out.writeInt(i);
				games.get(i).save(out);
			}
			out.close();
			server.getLog().log(games.size() + " games successfully saved.");
		}
		catch(Exception e)
		{
			server.getLog().error("Save failed!");
			e.printStackTrace();
		}
	}
}
