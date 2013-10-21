package org.cronosx.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @author prior
 * Advanced logging
 */
public class Logger extends Thread
{
	private PrintStream file;
	private PrintStream stream;
	private Queue<String> messages;
	DateFormat dateFormat;
	private int level;
	/**
	 * 
	 * @param file file to log to
	 * @param stream outputstream to log to (You might want to use System.out)
	 * @param level log-depth. The higher the level, the more output
	 */
	public Logger(File file, PrintStream stream, int level)
	{
		dateFormat = new SimpleDateFormat("[dd.MM.yyyy HH:mm:ss] ");
		this.messages = new LinkedList<String>();
		try
		{
			this.file = new PrintStream(file);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace(stream);
		}
		this.stream = stream;
		this.level = level;
		this.start();
	}
	
	/**
	 * Logs an error
	 * <p>
	 * @param msg message to log
	 */
	public void error(String msg)
	{
		 messages.add("(ERROR  ) " + msg);
	}

	
	/**
	 * Logs a warning
	 * <p>
	 * @param msg message to log
	 */
	public void warning(String msg)
	{
		messages.add("(WARNING) " + msg);
	}
	
	/**
	 * Logs a message if the previously debuglevel is at least as high as the supplied level
	 * <p>
	 * @param msg message to log
	 * @param level minimum debuglevel
	 */
	public void log(String msg, int level)
	{
		if(level <= this.level) messages.add("(NOTICE ) " + msg);
	}
	
	/**
	 * Logs a message independent of the global logginglevel
	 * <p>
	 * @param msg message to log
	 */
	public void log(String msg)
	{
		log(msg, 0);
	}
	
	@Override
	public void run()
	{
		outer:
		while(!(this.isInterrupted() && messages.isEmpty()))
		{
			flush();
			try
			{
				Thread.sleep(300L);
			}
			catch (InterruptedException e)
			{
				break outer;
			}
		}
	}
	
	private void flush()
	{

		try
		{
			while(!messages.isEmpty())
			{
				if(!messages.isEmpty())
				{
					String msg = messages.poll();
					if(msg != null)
					{
						Date date = new Date();
						stream.println(dateFormat.format(date) + msg);
						file.println(dateFormat.format(date) + msg);
						file.flush();
						stream.flush();
					}
				}
			}
			
		}
		catch(Exception e)
		{
			//TODO: Add Handler 
		}
	}
	
	public void close()
	{
		flush();
		this.interrupt();
	}
}
