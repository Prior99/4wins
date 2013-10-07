var websocket;

var OK = 0;
var Invalid_Argument_Count = 1;
var Invalid_Argument_Type = 2;
var Argument_Error = 3;
var Unknown_Command = 4;
var Not_Allowed = 5;


window.onbeforeunload = function() 
{
	websocket.send("close");
};

$().ready(function()
{
	if(websocket === undefined) websocket = new WebsocketConnection();
	websocket.connect();
});

function getWebsocket()
{
	if(websocket == undefined) websocket = new WebsocketConnection();
	return websocket;
};

WebsocketConnection.prototype.addHandler = function(command, func)
{
	if(this.handler == null) this.handler = new Array();
	this.handler[command] = func;
};

function WebsocketConnection()
{
	var self = this;
	this.handler = new Array();
	this.bytesSent = 0;
	this.bytesReceived = 0;
	this.socket;
	this.connectionRetrys = 0;
	this.lastPong;
	this.responseQueue = new Array();
	this.opened = new Array();
	if(debug) setInterval(function() 
	{
		self.send("ping");
		self.lastPong = new Date().getTime();
	}, 3000);
	if(debug) this.addHandler("pong", function()
	{
		$('#websocketStatus').css('color', 'green').html("Websocket connected [Ping: " + (new Date().getTime() - self.lastPong) + "] [Sent: " + self.bytesSent + "] [Received: " + self.bytesReceived + "]");
	});
	this.addHandler("BEER", function()
	{
		self.socket.send("BEER?");
	});
	this.addHandler("BEER!", function()
	{
		var func;
		while((func = self.opened.pop()) !== undefined) func();
	});
};

WebsocketConnection.prototype.ready = function(func)
{
	this.opened.push(func);
};

WebsocketConnection.prototype.connect = function()
{
	if(this.connectionRetrys < 5)
	{
		$('#websocketStatus').css('color', 'yellow').html("Websocket connecting. Failed attempts: "+this.connectionRetrys);
		var me = this;
		this.socket = new WebSocket("ws://" + HOST + ":2700/"); 
		this.socket.onopen = function(evt) {  me.onOpen(evt); };
		this.socket.onclose = function(evt) { me.onClose(evt); };
		this.socket.onmessage = function(evt) { me.onMessage(evt); };
		this.socket.onerror = function(evt) { me.onError; };
		
	}
	else
	{
		$('#websocketStatus').css('color', 'red').html("Websocket disconnected");
	}
};

WebsocketConnection.prototype.onOpen = function(evt) 
{ 
	this.connectionRetrys = 0;
	if(debug) console.log("WEBSOCKET CONNECTED");
	$('#websocketStatus').css('color', 'green').html("Websocket connected");
}; 

WebsocketConnection.prototype.onClose = function(evt) 
{ 
	this.connectionRetrys++;
	this.connect();
	if(debug) console.log("WEBSOCKET DISCONNECTED"); 
};

WebsocketConnection.prototype.onMessage = function(evt) 
{ 
	var data = decompressRLE(evt.data);
	this.bytesReceived += evt.data.length;
	if(debug) console.log("Received command:\"" + data+ "\"");
	var iOf = data.indexOf(" ");
	var cmd = data.substring(0, iOf == -1 ? data.length : iOf);
	var param = parseParam(data);
	if(cmd == "Res")
	{
		var exec = this.responseQueue.shift();
		if(isFunction(exec)) exec(param);
	}
	else
	{
		if(param.length > 0) 
		{
			if(this.handler[cmd] != null) this.handler[cmd](param);
		}
		else 
		{
			if(this.handler[cmd] != null) this.handler[cmd]();
		}
	}
};

function parseParam(message)
{
	var list = new Array();
	var last = message.indexOf(" ");
	var current = 0; 
	while(current != -1)
	{
		current = message.indexOf(" ", last+1);
		if(current != -1) 
		{
			list.push(message.substring(last+1,current));
			if(message.charAt(current+1) == '"')
			{
				var i = message.indexOf('"', current+2);
				list.push(message.substring(current+2, i));
				current = i+1;
			}
			last = current;
		}
		
	}
	if(last != -1 && last < message.length) list.push(message.substring(last+1, message.length));
	return list;
}

WebsocketConnection.prototype.onError = function(evt) 
{ 
	this.connectionRetrys++;
	this.connect();
	if(debug) console.log("WEBSOCKET ERROR:"); 
	if(debug) console.log(evt); 
};

WebsocketConnection.prototype.send = function()
{
	var j = 0;
	if(isFunction(arguments[0]))
	{
		j++;
		this.responseQueue.push(arguments[0]);
	}
	else 
		this.responseQueue.push(null);
	var command = arguments[j];
	for(var i = 1 + j; i < arguments.length; i++)
		if(arguments[i] != undefined) command += " " + arguments[i];
	if(debug) console.log("Sending command:\""+command+"\"");
	var compressed = compressRLE(command);
	if(this.socket != null) this.socket.send(compressed);
	this.bytesSent += compressed.length;
};

function isFunction(x) 
{
	return Object.prototype.toString.call(x) == '[object Function]';
};

function compressRLE(s)
{
	var fin = "";
	for(var i = 0; i < s.length; i++)
	{
		var maxWidth = -1;
		var maxAmount = -1;
		var maxString = null;
		for(var width = 1; width < 127 && width < s.length / 4; width++)
		{
			if(i + width >= s.length) break;
			var part = s.substring(i, i + width); 
			var amount = 1;
			while(i + (amount + 1) * width < s.length && 
					s.substring(i + amount * width, i + (amount + 1) * width) == part && 
					amount < 127) 
				amount++;
			if(amount > maxAmount)
			{
				maxAmount = amount;
				maxWidth = width;
				maxString = part;
			}
		}
		if(maxAmount > 3)
		{
			fin += String.fromCharCode(1) + String.fromCharCode(maxAmount) + String.fromCharCode(maxWidth) + maxString;
			i += maxAmount*maxWidth-1;
		}
		else fin +=s.charAt(i);
	}
	return fin;
};

function decompressRLE(s)
{
	var fin = "";
	for(var i = 0; i < s.length; i++)
	{
		if(s.charAt(i) == String.fromCharCode(1))
		{
			var amount = s.charCodeAt(++i);
			var width = s.charCodeAt(++i);
			var part = s.substring(++i, i+width);
			for(;amount > 0;amount--) fin +=part;
			i += width -1;
		}
		else fin += s.charAt(i);
	}
	return fin;
}