package org.cronosx.four;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;

public class Gamemanager
{
	private FourServer server;
	private List<Game> games;
	
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
		try
		{
			PreparedStatement stmt = server.getDatabase().prepareStatement("DELETE FROM players WHERE game = ?");
			stmt.setInt(1, g.getID());
			stmt.executeUpdate();
			stmt.close();
			stmt = server.getDatabase().prepareStatement("DELETE FROM games WHERE id = ?");
			stmt.setInt(1, g.getID());
			stmt.executeUpdate();
			stmt.close();	
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public Game createGame(User u1, User u2)
	{
		Game g = new Game(20, 15, server, u1, u2);
		games.add(g);
		return g;
	}
	
	public Game getGame(int id)
	{
		for(Game g : games)
		{
			if(g.getID() == id) return g;
		}
		return null;
	}
	
	public void load()
	{
		try
		{
			PreparedStatement stmt = server.getDatabase().prepareStatement("SELECT id FROM games");
			ResultSet rs = stmt.executeQuery();
			int amount = 0;
			while(rs.next())
			{
				int id = rs.getInt("id");
				games.add(new Game(id, server));
				amount++;
			}
			System.out.println(amount + " games loaded");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
