package org.cronosx.tools;

/*
 * Config-File
 * 
 * This Class Saves and Read Configfiles
 * 
 * Author: Frederick Gnodtke (Prior)
 * 
 * You are not allowed to unauthorized use this file or parts of this file, distribute it or change it.
 * 
 */

import java.io.*;
import java.util.*;

/**
 * @author prior
 * Represents a wide-featuring configuration
 */
public class Config
{
	File file; // The Configfile
	Map<String, Object> data; // This Hashmap saves the Data
	List<String> keyList;
	
	public Config()
	{

	}

	/**
	 * Create a new configuration and parses the given file
	 * <p>
	 * @param name file that should be opened to parse
	 */
	public Config(File name)
	{
		file = name;
		if(!file.exists()) 
			try
			{
				file.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		data = new HashMap<String, Object>();
		keyList = new ArrayList<String>();
		parse();
	}

	/**
	 * Changes the value assigned to the given key or creates a new key if none is existent
	 * <p>
	 * @param key key that should be changed 
	 * @param dat value
	 */
	public void setKey(String key, int dat)
	{
		data.remove(key);
		data.put(key, dat);
		if(!keyList.contains(key)) keyList.add(key);
		save();
	}

	/**
	 * Changes the value assigned to the given key or creates a new key if none is existent
	 * <p>
	 * @param key key that should be changed 
	 * @param dat value
	 */
	public void setKey(String key, boolean dat)
	{
		data.remove(key);
		data.put(key, dat);
		if(!keyList.contains(key)) keyList.add(key);
		save();
	}

	/**
	 * Changes the value assigned to the given key or creates a new key if none is existent
	 * <p>
	 * @param key key that should be changed 
	 * @param dat value
	 */
	public void setKey(String key, String dat)
	{
		data.remove(key);
		data.put(key, dat);
		if(!keyList.contains(key)) keyList.add(key);
		save();
	}

	/**
	 * Changes the value assigned to the given key or creates a new key if none is existent
	 * <p>
	 * @param key key that should be changed 
	 * @param dat value
	 */
	public void setKey(String key, float dat)
	{
		data.remove(key);
		data.put(key, dat);
		if(!keyList.contains(key)) keyList.add(key);
		save();
	}

	/**
	 * Saves the changed configuration to its assigned file.
	 * May overwrite comments etc.
	 */
	public void save()
	{
		try
		{
			FileWriter fstream = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fstream);
			for (String key : keyList)
			{
				out.write(key + " = " + data.get(key));
				out.newLine();
			}
			out.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Returns the raw mapping of the configuration
	 * <p>
	 * @return mapping
	 */
	public Map<String, Object> getData()
	{
		return data;
	}

	/**
	 * Parses one single string and adds it to the map
	 * <p>
	 * @param sz string to parse
	 */
	public void parseString(String sz)
	{
		String[] lines = sz.split("\n");
		int i = 0;
		while (i < lines.length)
		{
			parseLine(lines[i]);
			i++;
		}
	}

	/**
	 * Parses the previously defined configfile
	 */
	private void parse()
	{
		try
		{
			FileReader cfile = new FileReader(file);
			BufferedReader read = new BufferedReader(cfile);
			String line = null;
			while ((line = read.readLine()) != null)
			{
				parseLine(line);
			}
			read.close();
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace(); // TODO Auto-generated catch block
		}
		catch(IOException e)
		{
			e.printStackTrace(); // TODO Auto-generated catch block
		}

	}

	/**
	 * Parse one line
	 * <p>
	 * @param line line to parse
	 */
	private void parseLine(String line)
	{
		if(line.length() >= 3)
		{
			if(line.toCharArray()[0] != '#')
			{
				String[] args = prepare(line);
				data.put(args[0], getConverted(args[1]));
				keyList.add(args[0]);
			}
		}
	}

	/**
	 * Converts the string to an object bool > integer > double > string
	 * <p>
	 * @param s string to convert
	 * @return object
	 */
	private Object getConverted(String s)
	{
		if(s.matches("false"))
			return false;
		else if(s.matches("true")) return true;
		try
		{
			int i = Integer.parseInt(s);
			return i;
		}
		catch(Exception e1)
		{
			try
			{
				double f = Double.parseDouble(s);
				return f;
			}
			catch(Exception e2)
			{
				return s;
			}
		}

	}

	/**
	 * Clears all whitespaces, tabstops etc. and splits at "="
	 * <p>
	 * @param s string to prepare
	 * @return parts
	 */
	private String[] prepare(String s)
	{
		String[] arg = s.split("=");
		arg = Arrays.copyOf(arg, 2);
		if(arg[0] == null) arg[0] = "";
		if(arg[1] == null) arg[1] = "";
		arg[0] = clearWhitespace(arg[0]);
		arg[1] = clearWhitespace(arg[1]);
		return arg;
	}

	/**
	 * Clears all pretending whitespaces from the string
	 * <p>
	 * @param s string to clean
	 * @return cleaned string
	 */
	public static String clearWhitespace(String s)
	{
		while (s.startsWith(" ") || s.startsWith("	"))
		{
			s = s.substring(1, s.length());
		}
		while (s.endsWith(" ") || s.endsWith("	"))
		{
			s = s.substring(0, s.length() - 1);
		}
		s = s.replaceAll("\n", "");
		s = s.replaceAll("\r", "");
		return s;
	}

	/**
	 * Returns the obtained value, if no key was set so no value could be obtained, returns standard
	 * <p>
	 * @param s key to look up
	 * @param standard value to return if no value was set
	 * @return value
	 */
	public String getStr(String s, String standard)// returns the obtained value as String
	{
		if(!data.containsKey(s))
		{
			if(standard != null) 
			{
				data.put(s, standard);
				keyList.add(s);
				save();
			}
			return standard;
		}
		return (String) data.get(s);
	}

	/**
	 * Returns the obtained value, if no key was set so no value could be obtained, returns standard
	 * <p>
	 * @param s key to look up
	 * @param standard value to return if no value was set
	 * @return value
	 */
	public int getInt(String s, int standard)// returns the obtained value as Integer
	{
		if(!data.containsKey(s))
		{
			if(standard != 0) 
			{
				data.put(s, standard);
				keyList.add(s);
				save();
			}
			return standard;
		}
		return (Integer) data.get(s);
	}

	/**
	 * Returns the obtained value, if no key was set so no value could be obtained, returns standard
	 * <p>
	 * @param s key to look up
	 * @param standard value to return if no value was set
	 * @return value
	 */
	public boolean getBool(String s, boolean standard)// returns the obtained value as Boolean
	{
		if(!data.containsKey(s))
		{
			if(standard != false) 
			{
				data.put(s, standard);
				keyList.add(s);
				save();
			}
			return standard;
		}
		return (Boolean) data.get(s);
	}

	/**
	 * Returns the obtained value, if no key was set so no value could be obtained, returns standard
	 * <p>
	 * @param s key to look up
	 * @param standard value to return if no value was set
	 * @return value
	 */
	public double getDouble(String s, double standard)
	{
		if(!data.containsKey(s))
		{
			if(standard != 0) 
			{
				data.put(s, standard);
				keyList.add(s);
				save();
			}
			return standard;
		}
		return (Double) data.get(s);
	}

}