function WebsocketConnection(host)
{
	this.host = host;
	var self = this;
	this.handler = new Array();
	this.socket;
	this.opened = new Array();
};

WebsocketConnection.prototype.addHandler = function(command, func)
{
	if(this.handler == null) this.handler = new Array();
	this.handler[command] = func;
};

WebsocketConnection.prototype.ready = function(func)
{
	this.opened.push(func);
};

WebsocketConnection.prototype.connect = function()
{
	var me = this;
	this.socket = new WebSocket("ws://" + this.host + "/"); 
	this.socket.onopen = function(evt) {  me.onOpen(evt); };
	this.socket.onclose = function(evt) { me.onClose(evt); };
	this.socket.onmessage = function(evt) { me.onMessage(evt); };
	this.socket.onerror = function(evt) { me.onError; };
};

WebsocketConnection.prototype.onOpen = function(evt) 
{ 
	console.log("Websocket connected");
}; 

WebsocketConnection.prototype.onClose = function(evt) 
{ 
	console.log("Websocket lost connection");
	this.connect(); 
};

WebsocketConnection.prototype.onMessage = function(evt) 
{ 
	var data = evt.data;
	console.log("Received:\"" + data+ "\"");
	var param = data.split(";");
	var cmd = param[0];
	if(param.length > 0) 
	{
		if(this.handler[cmd] != null) this.handler[cmd](param);
	}
	else 
	{
		if(this.handler[cmd] != null) this.handler[cmd]();
	}
};

WebsocketConnection.prototype.onError = function(evt) 
{ 
	this.connectionRetrys++;
	console.log("Websocket error:" + evt);
	this.connect();
};

WebsocketConnection.prototype.send = function()
{
	var command = arguments[0];
	for(var i = 1; i < arguments.length; i++)
		if(arguments[i] != undefined) command += ";" + arguments[i];
	if(debug) console.log("Sending:\""+command+"\"");
	if(this.socket != null) this.socket.send(command);
};
