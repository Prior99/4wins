package org.cronosx.four;

import java.util.HashMap;

import org.cronosx.cgi.CGI;
import org.cronosx.cgi.DefaultPage;
import org.cronosx.cgi.Page;
import org.cronosx.cgi.PageHandler;
import org.cronosx.webserver.PostData;
import org.cronosx.webserver.Webserver;

public class PageHandlerFour implements PageHandler
{
	@Override
	public Page getPage(String request, String pageID, HashMap<String, String> params, String browserName, HashMap<String, String> cookies, String ip, Webserver webserver, CGI cgi, HashMap<String, PostData> postData)
	{
		return new DefaultPage(request, pageID, params, browserName, cookies, ip, webserver, cgi, postData);
	}	
}
