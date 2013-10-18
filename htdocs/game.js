function Game()
{
	var self = this;
	this.socket = new WebsocketConnection("localhost:2700");
	this.socket.connect();
	this.masks = new Array();
	var wrapper = $('<div class="wrapper"></div>"').appendTo("body");
	$('<div class="header"></div>').appendTo(wrapper);
	this.gamesw = $('<div class="games"></div>').appendTo(wrapper);
	this.parent = $("<div class='content'></div>").appendTo(wrapper);
	//$('<div class="footer">Four the lulz | 2013 by Prior (Frederick Gnodtke) | I did it for the lulz.</div>').appendTo(wrapper);
	wait("Connecting...");
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
	this.socket.send("games");
	wait("Waiting for games");
	this.socket.addHandler("games", function(param)
	{
		unwait();
		self.gamesw.html("");
		var user = $('<div class="box"></div>').append("<h1>User</h1>").appendTo(self.gamesw);
		var won = parseInt(param[1]);
		var lost = parseInt(param[2]);
		var games = won +lost;
		var wonp = (won /games) * 100;
		var lostp = (lost /games) * 100;
		var time = (new Date().getTime() - parseInt(param[3])*1000) / 1000;
		var days = time / (24 * 60 * 60);
		time = time % (24 * 60 * 60);
		var hours = time / (60 * 60);
		time = time % (60 * 60);
		var minutes = time / 60;
		minutes = minutes % 60;
		time = time % 60;
		var seconds = time;
		var since = days.toFixed(0) + " d, " + hours.toFixed(0) + " h, " + minutes.toFixed(0) + " m";
		user.append("<br>Name: " + param[4] + "<br>");
		user.append("<b>Elo: " + param[5] + "</b><br>");
		user.append("Won: " + won + " (" + wonp.toFixed(0) + "%)<br>");
		user.append("Lost: " + lost + " (" + lostp.toFixed(0) + "%)<br>");
		user.append("Games: " + games + "<br>");
		user.append("Since: " + since + "<br>");
		self.games = $('<ul></ul>').appendTo($('<div class="box"></div>').appendTo(self.gamesw).append("<h1>Games</h1>"));
		for(var i = 6; i < param.length; i+=2)
		{
			var btn = $("<a href='#'>"+param[i + 1]+"</a>").click(function()
			{
				self.showGame(this.index);
			}).appendTo($('<li></li>').appendTo(self.games))[0].index = param[i];
		}
		var join = $('<div class="box"></div>').append("<h1>Challenge Player</h1>").appendTo(self.gamesw);
		var id = $("<input type='text' />").appendTo(join);
		$("<button>Challenge!</button>").appendTo(join).click(function() {
			self.challenge(id.val());
		});
		$('<div class="box"></div>').append("<h1>Highscore</h1>").append($("<button>View Highscore</button>").click(function () {
			self.displayHighscore();
		})).appendTo(self.gamesw);
		$('<div class="box"></div>').append("<h1>Credits</h1>").append("<p>2013 by Prior <br>(Frederick Gnodtke)").appendTo(self.gamesw);
	});
};

Game.prototype.displayHighscore = function()
{
	var self = this;
	this.socket.send("highscore");
	wait();
	this.socket.addHandler("highscore", function(param) {
		unwait();
		self.clearMasks();
		var mask = $("<div class='mask highscore'></div>").appendTo(self.parent).append("<h1>Highscore</h1>");
		self.masks.push(mask);
		var table = $("<table></table>").appendTo(mask);
		$("<tr class='head'></tr>").appendTo(table)
			.append("<td>#</td>")
			.append("<td>Name</td>")
			.append("<td>Games won</td>")
			.append("<td>Games lost</td>")
			.append("<td><b>Elo</b></td>")
			.append("<td>Challenge</td>");
		for(var i = 1, j = 1; i < param.length; i += 4, j++)
		{
			function t(n) {
			$("<tr></tr>").appendTo(table)
				.append("<td>" + j + "</td>") 
				.append("<td>"+param[i]+"</td>")
				.append("<td>" + param[i + 1] + "</td>")
				.append("<td>" + param[i + 2] + "</td>")
				.append("<td><b>" + param[i + 3] + "</b></td>")
				.append($("<td></td>").append("<button>Challenge</button>").click(function(e) {
					self.challenge(n);
				}));
			};
			t(param[i]);
		}
	});
}

Game.prototype.challenge = function(player)
{
	var self = this;
	this.socket.send("challenge", player);
	this.socket.addHandler("created", function(param)
	{
		self.showGame(parseInt(param[1]));
	});
}

Game.prototype.place = function(x, y)
{
	this.socket.send("set", this.currentIndex, x);
}

Game.prototype.showGame = function(index)
{
	var self = this;
	this.currentIndex = index;
	this.socket.send("game", index);
	wait("Waiting for game...");
	this.socket.addHandler("game", function(param)
	{
		unwait();
		self.clearMasks();
		var width = parseInt(param[1]);
		var height = parseInt(param[2]);
		console.log(param);
		if(self.gui != undefined)
			self.gui.destroy();
		var mask = $("<div class='mask game'></div>").appendTo(self.parent);
		self.masks.push(mask);
		$("<h1>Game</h1>").appendTo(mask);
		self.gui = new FourWins(width, height, self, mask);
		self.gui.setMap(param[3]);
		if(param[4] == "true") self.gui.nextTurn();
		$("<button>Delete</button>").appendTo(mask.append("<br>")).click(function()
		{
			self.socket.send("delete", index);
			self.displayHighscore();
		});
		self.socket.addHandler("placed", function(param)
		{
			if(parseInt(param[1]) == self.currentIndex)
			{
				var col = parseInt(param[2]);
				self.gui.place(col, self.gui.lowestY(col), parseInt(param[3]));
			}
		});
		self.socket.addHandler("win", function(param2)
		{
			var x1 = parseInt(param2[3]);
			var x2 = parseInt(param2[5]);
			var y1 = parseInt(param2[4]);
			var y2 = parseInt(param2[6]);
			var username = param2[1];
			self.gui.win(x1, y1, x2, y2);
			message(ok, "Game Over!", "Player " + username + " has won the game!", function() {});
			$("<button>Revanche</button>").appendTo(mask.append("<br>")).click(function()
			{
				var amnt = parseInt(param[5]);
				for(var i = 0; i < amnt; i++)
					if(param[6 + i] != self.username) self.challenge(param[6 + i]);
			});
		});
		self.socket.addHandler("turn", function(param)
		{
			if(parseInt(param[1]) == self.currentIndex)
			{
				self.gui.nextTurn();
			}
		});
	});
	this.socket.addHandler("lobby", function(param)
	{
		unwait();
		self.displayLobbyMask(param);
	});
}


Game.prototype.displayLobbyMask = function(param)
{
	this.clearMasks();
	var self = this;
	var lobby, users;
	lobby = $('<div class="lobby"></div>').appendTo(this.parent);
	this.masks.push(lobby);
	$("<h1>Lobby</h1>").appendTo(lobby);
	users = $("<div class='users'></div>").appendTo(lobby);
	var table = $("<table></table>").appendTo(lobby);
	var usersd = $("<span>0/4 users (at least 2 needed)</span>").appendTo(lobby.append("<br>"));
	$("<tr class='head'></tr>").append("<td width=300>Name</td>").append("<td>Won</td>").append("<td>Lost</td>").append("<td>Elo</td>").appendTo(table);
	var users = 0;
	var btn = $('<button>Start Game</button>').attr('disabled', true);
	for(var i = 2; i < param.length; i+=4)
	{
		$("<tr></tr>").append("<td>" + param[i] + "</td>").append("<td>" + param[i + 2] + "</td>").append("<td>" + param[i + 1] + "</td>").append("<td>" + param[i + 3] + "</td>").appendTo(table);
		users ++;
		usersd.html(users+"/2 users");
		if(users >= 2 && users <=4) btn.attr('disabled', false);
		else  btn.attr('disabled', true);
	}
	btn.appendTo(lobby.append("<br>").append("<br>")).click(function() {
		self.socket.send("start", param[1]);
	});
	$('<button>Delete Game</button>').appendTo(lobby.append("<br>")).click(function () {
		self.socket.send("delete", param[1]);
		self.displayHighscore();
	});
	this.socket.addHandler("lobbyjoin", function(param2) {
		if(parseInt(param2[1]) == parseInt(param[1]))
		{
			$("<tr></tr>").append("<td>" + param2[2] + "</td>").append("<td>" + param2[4] + "</td>").append("<td>" + param2[3] + "</td>").append("<td>" + param2[5] + "</td>").appendTo(table);
			users ++;
			if(users >= 2) btn.attr('disabled', false);
			else  btn.attr('disabled', true);
			usersd.html(users+"/2 users");
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
	this.clearMasks();
	var mask, username, password, ok, self = this;
	mask = $("<div class='mask login'></div>").appendTo(this.parent);
	this.masks.push(mask);
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
	this.clearMasks();
	var mask, username, password, repeat, ok, self = this;
	mask = $("<div class='mask register'></div>").appendTo(this.parent);
	this.masks.push(mask);
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
	wait("Waiting for login...");
	this.socket.addHandler("login", function(param)
	{
		unwait();
		if(param[1] == "ok")
		{
			self.loggedIn(username, password);
			//message(ok, "Logged in", "You successfully logged in", function() {  });
		}
		else
		{
			message(ok, "Error", "Login failed. Combination of username and password unknown.", function() { self.displayLoginMask(); });
			eraseCookie("username");
			eraseCookie("password");
		}
	});
}

Game.prototype.clearMasks = function()
{
	while(this.masks.length > 0)
	{
		var m = this.masks.pop();
		if(m != null) m.remove();
	}
}

Game.prototype.loggedIn = function(username, password)
{
	setCookie("username", username, 14);
	setCookie("password", password, 14); 	
	this.username = username;
	this.start();
}

Game.prototype.register = function(username, password)
{
	var self = this;
	console.log(username);
	console.log(password);
	this.socket.send("register", username, password);
	wait("Waiting for register...");
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
