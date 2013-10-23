package org.cronosx.four;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import sun.misc.BASE64Encoder;

public class Usermanager
{
	private FourServer server;
	private Map<Integer, User> users;
	private User darkRoomUser;
	private MessageDigest sha1;
	private BASE64Encoder base64Enc;
	
	public Usermanager(FourServer server)
	{
		base64Enc = new BASE64Encoder();
		try
		{
			sha1 = MessageDigest.getInstance("SHA-1");
		}
		catch(NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		this.server = server;
		this.users = new HashMap<Integer, User>();
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
	
	public User getUser(int user)
	{
		return users.get(user);
	}
	
	private void load()
	{
		try
		{
			PreparedStatement stmt = server.getDatabase().prepareStatement("SELECT id, name, password, elo, win, lose, created, last_login FROM users");
			ResultSet rs = stmt.executeQuery();
			int amount = 0;
			while(rs.next())
			{
				String name = rs.getString("name");
				String password = rs.getString("password");
				int win = rs.getInt("win");
				int lose = rs.getInt("lose");
				int registered = rs.getInt("created");
				int loggedIn = rs.getInt("last_login");
				int elo = rs.getInt("elo");
				int id = rs.getInt("id");
				users.put(id, new User(name, password, win, lose, registered, loggedIn, elo, id, server));
				amount++;
			}
			System.out.println(amount + " users loaded");
			stmt.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public User getUser(String s)
	{
		try
		{
			PreparedStatement stmt = server.getDatabase().prepareStatement("SELECT id FROM users WHERE name = ?");
			stmt.setString(1, s);
			ResultSet rs = stmt.executeQuery();
			rs.first();
			return getUser(rs.getInt("id"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public User[] getUsersSorted()
	{
		User[] us = new User[users.size()];
		int i = 0;
		for(int s : users.keySet())
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
		for(int i : users.keySet())
			users.get(i).loadGames();
	}
	
	public boolean isWaitingForDarkroom(User u)
	{
		return darkRoomUser == u;
	}
	
	public boolean isLoginValid(String username, String sha1)
	{
		try
		{
			PreparedStatement stmt = server.getDatabase().prepareStatement("SELECT id FROM users WHERE name = ? AND password = ?");
			stmt.setString(1, username);
			stmt.setString(2, sha1);
			ResultSet rs = stmt.executeQuery();
			if(rs.first()) return true;
			else return false;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	public User login(String username, String password)
	{
		password = base64Enc.encode(sha1.digest(password.getBytes()));
		if(!isLoginValid(username, password)) return null;
		else
		{
			User u = getUser(username);
			if(!u.login(password)) return null;
			return u;
		}
	}
	
	public boolean register(String username, String password)
	{
		if(users.containsKey(username)) return false;
		else
		{
			password = base64Enc.encode(sha1.digest(password.getBytes()));
			User u = new User(username, password, server);
			users.put(u.getID(), u);
			return true;
		}
	}
	
	public void importOld()
	{
		try
		{
			DataInputStream in = new DataInputStream(new FileInputStream(new File("users.dat")));
			int amount = in.readInt();
			for(int i = 0; i < amount; i++)
			{
				String name = in.readUTF();
				in.readUTF();
				String password = in.readUTF();
				byte[] passwordBin = password.getBytes();
				password = base64Enc.encode(passwordBin);
				int win = in.readInt();
				int lose = in.readInt();
				int registered = in.readInt();
				int loggedIn = in.readInt();
				in.readBoolean();
				int elo = in.readInt();
				int amnt = in.readInt();
				for(int j = 0; j < amnt; j++)
				{
					in.readInt();
				}
				PreparedStatement stmt = server.getDatabase().prepareStatement("INSERT INTO users(name, password, elo, win, lose, created, last_login) VALUES(?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
				stmt.setString(1, name);
				stmt.setString(2, password);
				stmt.setDouble(3, elo);
				stmt.setInt(4, win);
				stmt.setInt(5, lose);
				stmt.setInt(6, registered);
				stmt.setInt(7, loggedIn);
				stmt.executeUpdate();
				ResultSet rs = stmt.getGeneratedKeys();
				rs.first();
				int tmpid = rs.getInt(1);
				users.put(tmpid, new User(name, password, win, lose, registered, loggedIn, elo, tmpid, server));
				stmt.close();
			}
			System.out.println(amount + " users imported to database!");
			in.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
