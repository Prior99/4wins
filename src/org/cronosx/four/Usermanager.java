package org.cronosx.four;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class Usermanager
{
	private FourServer server;
	private Map<String, User> users;
	public Usermanager(FourServer server)
	{
		this.server = server;
		this.users = new HashMap<String, User>();
		load();
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
				users.put(in.readUTF(), new User(in, server));
			}
			in.close();
			server.getLog().log(amount + " users successfully loaded.");
		}
		catch(Exception e)
		{
			server.getLog().error("Load failed!");
			//e.printStackTrace();
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
				return arg0.getElo() - arg1.getElo();
			}
			
		});
		return us;
	}
	
	public void loadGames()
	{
		for(String s : users.keySet())
			users.get(s).loadGames();
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
			server.getLog().log(users.size() + " users successfully saved.");
		}
		catch(Exception e)
		{
			server.getLog().error("Save failed!");
			e.printStackTrace();
		}
	}
	
	public User login(String username, String password)
	{
		password = server.getSHA1(password);
		server.getLog().log("User \"" + username + "\" attempted login with password \"" + password + "\"");
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
			password = server.getSHA1(password);
			server.getLog().log("New user \"" + username + "\" registered with password \"" + password + "\"");
			User u = new User(username, password, server);
			users.put(username, u);
			return true;
		}
	}
}
