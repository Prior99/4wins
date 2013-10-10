function Game()
{
	var self = this;
	this.socket = new WebsocketConnection("localhost:2700");
	this.socket.connect();
	wait();
	this.socket.openSlave = function()
	{
		unwait();
		self.waitForLogin();
	}
}
 
Game.prototype.start = function()
{
	var self = this;
	console.log("Started!");
	this.games = $('<div class="games"></div>').appendTo("body");
	this.socket.send("games");
	wait();
	this.socket.addHandler("games", function(param)
	{
		unwait();
		self.games.html("");
		for(var i = 1; i < param.length; i++)
		{
			var btn = $("<button>Game #"+param[i]+"</button>").appendTo(self.games).click(function()
			{
				self.showGame(this.index);
			})[0].index = param[i];
		}
		$("<button>Create new Game</button>").appendTo(self.games).click(function(){
			self.createGame();
		});
		var id = $("<input type='text' />").appendTo(self.games);
		$("<button>Join</button>").appendTo(self.games).click(function() {
			self.socket.send("join", id.val());
		});
	});
};

Game.prototype.place = function(x, y)
{
	this.socket.send("set", this.currentIndex, x);
}

Game.prototype.showGame = function(index)
{
	var self = this;
	this.currentIndex = index;
	this.socket.send("game", index);
	this.socket.addHandler("game", function(param)
	{
		var width = parseInt(param[1]);
		var height = parseInt(param[2]);
		console.log(param);
		if(self.gui != undefined)
			self.gui.destroy();
		self.gui = new FourWins(width, height, self);
		self.gui.setMap(param[3]);
		self.socket.addHandler("placed", function(param)
		{
			if(parseInt(param[1]) == self.currentIndex)
			{
				var col = parseInt(param[2]);
				self.gui.place(col, self.gui.lowestY(col), parseInt(param[3]));
			}
		});
	});
	this.socket.addHandler("lobby", function(param)
	{
		self.displayLobbyMask(param);
	});
}

Game.prototype.createGame = function()
{
	this.socket.send("create");
	wait();
}

Game.prototype.displayLobbyMask = function(param)
{
	var self = this;
	var lobby, users;
	lobby = $('<div class="lobby"></div>').appendTo("body");
	$("<h1>Lobby</h1>").appendTo(lobby);
	users = $("<div class='users'></div>").appendTo(lobby);
	for(var i = 2; i < param.length; i++)
	{
		$("<p>" + param[i] + "</p>").appendTo(users);
	}
	$('<button>Start Game</button>').appendTo(lobby).click(function() {
		self.socket.send("start", param[1]);
	});
	this.socket.addHandler("lobbyjoin", function(param2) {
		if(parseInt(param2[1]) == parseInt(param[1]))
		{
			$("<p>" + param2[2] + "</p>").appendTo(users);
		}
	});
}

/*
 * Loginbullshit starts here
 */

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
	mask = $("<div class='mask login'></div>").appendTo("body");
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
	mask = $("<div class='mask register'></div>").appendTo("body");
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
			message(ok, "Logged in", "You successfully logged in", function() { self.loggedIn(username, password); });
		}
		else
		{
			message(ok, "Error", "Login failed. Combination of username and password unknown.", function() { self.displayLoginMask(); });
		}
	});
}

Game.prototype.loggedIn = function(username, password)
{
	setCookie("username", username, 14);
	setCookie("password", password, 14); 	
	this.start();
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
