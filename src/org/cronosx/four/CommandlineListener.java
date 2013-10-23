package org.cronosx.four;

import java.io.File;
import java.util.Scanner;

public class CommandlineListener extends Thread
{
	private FourServer server;
	private Scanner sc;
	public CommandlineListener(FourServer server)
	{
		this.server = server;
		sc = new Scanner(System.in);
		start();
	}
	
	public void run()
	{
		while(!isInterrupted())
		{
			String line = sc.nextLine();
			String[] param = line.split(" ");
			parse(param);
		}
	}
	
	private void parse(String[] param)
	{
		if(param[0].equals("quit"))
		{
			server.shutdown();
		}
		if(param[0].equals("importOld"))
		{
			server.getUsermanager().importOld();
		}
		else
		{
			System.out.println("Unknown command");
		}
	}
	
	public void shutdown()
	{
		interrupt();
	}
	
}
