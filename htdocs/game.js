function Game()
{
	this.socket = new WebsocketConnection("localhost:2700");
	this.socket.connect();
	this.waitForLogin();
}

Game.prototype.waitForLogin = function()
{
	var self = this;
	var username = getCookie("username");
	var password = getCookie("password"); 
	if(username != null && password != null)
	{
		this.login(username, password);
	}
	else
	{
		this.displayLoginMask();
	}
}

Game.prototype.displayLoginMask = function()
{
	var mask, username, password, ok, self = this;
	mask = $("<div class='login'></div>").appendTo("body");
	$("<h1>Login</h1>").appendTo(mask);
	username = $("<p></p>").appendTo(mask );
	$("<label>Username:</label>").appendTo(username);
	username = $("<input type='text' />").appendTo(username);
	password = $("<p></p>").appendTo(mask );
	$("<label>Password:</label>").appendTo(password);
	password = $("<input type='password' />").appendTo(password);
	ok = $("<p></p>").appendTo(mask );
	$("<label>Login:</label>").appendTo(ok);
	ok = $("<button>OK</button>").appendTo(ok);
	ok.click(function()
	{
		mask.remove();
		self.login(username.val(), password.val());
	});
	reg = $("<p>Don't have an account? Create one </p>").appendTo(mask );
	reg = $("<a href='#'>here</a>").appendTo(reg);
	reg.click(function()
	{
		mask.remove();
		self.displayRegisterMask();
	});
}

Game.prototype.displayRegisterMask = function()
{
	var mask, username, password, repeat, ok, self = this;
	mask = $("<div class='register'></div>").appendTo("body");
	$("<h1>Register</h1>").appendTo(mask);
	username = $("<p></p>").appendTo(mask );
	$("<label>Username:</label>").appendTo(username);
	username = $("<input type='text' />").appendTo(username);
	password = $("<p></p>").appendTo(mask );
	$("<label>Password:</label>").appendTo(password);
	password = $("<input type='password' />").appendTo(password);
	repeat = $("<p></p>").appendTo(mask );
	$("<label>Repeat:</label>").appendTo(repeat);
	repeat = $("<input type='password' />").appendTo(repeat);
	ok = $("<p></p>").appendTo(mask );
	$("<label>Login:</label>").appendTo(ok);
	ok = $("<button>OK</button>").appendTo(ok);
	ok.click(function()
	{
		mask.remove();
		self.register(username.val(), password.val());
	});
	reg = $("<p>Don't have an account? Create one </p>").appendTo(mask );
	reg = $("<a href='#'>here</a>").appendTo(reg);
	reg.click(function()
	{
		self.displayRegisterMask();
		mask.remove();
	});
	function test()
	{
		if(repeat.val() != password.val())
		{
			repeat.css({background: "#FAA"});
			password.css({background: "#FAA"});
			ok.attr({disabled: true});
		}
		else
		{
			repeat.css({background: "#FFF"});
			password.css({background: "#FFF"});
			ok.attr({disabled: false});
		}
	};
	repeat.keyup(test);
	password.keyup(test);
}

Game.prototype.login = function(username, password)
{
	var self = this;
	console.log(username);
	console.log(password);
	this.socket.send("login", username, password);
	wait();
	this.socket.addHandler("login", function(param)
	{
		unwait();
		if(param[1] == "ok")
		{
			message(ok, "Logged in", "You successfully logged in", null);
		}
		else
		{
			message(ok, "Error", "Login failed. Combination of username and password unknown.", null);
		}
	});
}

Game.prototype.register = function(username, password)
{
	var self = this;
	console.log(username);
	console.log(password);
	this.socket.send("register", username, password);
	wait();
	this.socket.addHandler("register", function(param)
	{
		unwait();
		if(param[1] == "ok")
		{
			message(ok, "Registered", "Registration was successfull. you may now login.", function() { self.displayLoginMask(); });
		}
		else
		{
			message(fail, "Error", "Unable to register. Maybe this username was already taken.", function() { self.displayRegisterMask(); });
		}
	});
}