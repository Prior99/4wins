package org.cronosx.four;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class Usermanager
{
	private FourServer server;
	private Map<String, User> users;
	private User darkRoomUser;
	private MessageDigest sha1;
	
	public Usermanager(FourServer server)
	{
		try
		{
			sha1 = MessageDigest.getInstance("SHA-1");
		}
		catch(NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		this.server = server;
		this.users = new HashMap<String, User>();
		load();
	}
	
	public void removeUser(User u)
	{
		users.remove(u.getName());
	}
	
	public void attendDarkRoomGame(User u)
	{
		if(u.getActiveGames() < 10)
		{
			if(darkRoomUser == null)
			{
				darkRoomUser = u;
			}
			else
			{
				User tmp = darkRoomUser; 
				darkRoomUser = null;
				if(u != tmp) server.getGamemanager().createGame(u, tmp);
			}
		}
	}
	
	public User getUser(String user)
	{
		return users.get(user);
	}
	
	private void load()
	{
		try
		{
			DataInputStream in = new DataInputStream(new FileInputStream(new File("users.dat")));
			int amount = in.readInt();
			for(int i = 0; i < amount; i++)
			{
				String n = in.readUTF();
				User u =  new User(in, server);
				System.out.println(n + "->" + u.getName());
				users.put(n, u);
			}
			in.close();
			server.getLog().log(amount + " users successfully loaded.");
		}
		catch(Exception e)
		{
			server.getLog().error("Load failed!");
			e.printStackTrace();
		}
	}
	
	public User[] getUsersSorted()
	{
		User[] us = new User[users.size()];
		int i = 0;
		for(String s : users.keySet())
		{
			us[i++] = users.get(s);
		}
		Arrays.sort(us, new Comparator<User>()
		{

			@Override
			public int compare(User arg0, User arg1)
			{
				return arg1.getElo() - arg0.getElo();
			}
			
		});
		return us;
	}
	
	public void loadGames()
	{
		for(String s : users.keySet())
			users.get(s).loadGames();
	}
	
	public boolean isWaitingForDarkroom(User u)
	{
		return darkRoomUser == u;
	}
	
	public void save()
	{
		try
		{
			DataOutputStream out = new DataOutputStream(new FileOutputStream(new File("users.dat")));
			out.writeInt(users.size());
			for(String s : users.keySet())
			{
				out.writeUTF(s);
				users.get(s).save(out);
			}
			out.close();	
			server.getLog().log(users.size() + " users successfully saved.", 150);
		}
		catch(Exception e)
		{
			server.getLog().error("Save failed!");
			e.printStackTrace();
		}
	}
	
	public User login(String username, String password)
	{
		password = new String(sha1.digest(password.getBytes()));
		//server.getLog().log("User \"" + username + "\" attempted login with password \"" + password + "\"");
		if(!users.containsKey(username)) return null;
		else
		{
			User u = users.get(username);
			if(!u.login(password)) return null;
			return u;
		}
	}
	
	public boolean register(String username, String password)
	{
		if(users.containsKey(username)) return false;
		else
		{
			password = new String(sha1.digest(password.getBytes()));
			//server.getLog().log("New user \"" + username + "\" registered with password \"" + password + "\"");
			User u = new User(username, password, server);
			users.put(username, u);
			return true;
		}
	}
}
