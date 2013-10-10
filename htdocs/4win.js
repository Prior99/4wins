/**
 ** Class FourWins
 **
 ** 2013 By Prior (Frederick Gnodtke)
 **
 **/
function FourWins(width, height, gameO, parent)
{
	this.gameO = gameO;
	var self = this; //Forward to call this from within functions
	this.canvas = $("<canvas id='c' width='" + (width * 40) + "' height='" + (height * 40) + "' style='border: 1px solid #000'></canvas>").appendTo(parent);
	this.canvas[0].addEventListener("mousemove", function(event)
	{
		event.preventDefault();
		self.onMouseMove(event);
	});
	this.canvas[0].addEventListener("mousedown", function(event)
	{
		event.preventDefault();
		self.onMouseDown(event);
	});
	this.ctx = this.canvas[0].getContext("2d"); //Store our context
	this.init(width, height); //initialize private variables
	this.interval = setInterval(function() {
		self.redraw();
	}, 30);
	this.wins = {has : false};
	//this.p = 0; //OBSOLETE! FOR TESTINGPURPOSES ONLY
}

FourWins.prototype.win = function(x1, y1, x2, y2)
{
	this.wins = {
		has : true,
		x1 : x1,
		y1 : y1,
		x2 : x2,
		y2 : y2,
		a : 0
	};
}

FourWins.prototype.redrawWin = function()
{
	if(this.wins.has == true)
	{
		for(var x = 0; x < this.spread.width; x++)
		{
			for(var y = 0; y < this.spread.height; y++)
			{
				this.ctx.fillStyle = "rgba(30, 30, 30, " + this.wins.a + ")";
				this.ctx.beginPath();
				this.ctx.rect(x * this.tileDim.width, y * this.tileDim.height, this.tileDim.width, this.tileDim.height);
				this.ctx.fill();
			}
		}
		this.ctx.strokeStyle = "rgba(255, 255, 255, " + this.wins.a + ")";
		this.ctx.lineWidth = 20;
		this.ctx.beginPath();
		this.ctx.moveTo((this.wins.x1 + 0.5) * this.tileDim.width, (this.wins.y1 + 0.5) * this.tileDim.height);
		this.ctx.lineTo((this.wins.x2 + 0.5) * this.tileDim.width, (this.wins.y2 + 0.5) * this.tileDim.height);
		this.ctx.stroke();
		if(this.wins.a < 0.5) this.wins.a += 0.005;
	}
}

FourWins.prototype.setMap = function(map)
{
	for(var x = 0; x < this.spread.width; x++)
	{
		for(var y = this.spread.height - 1; y >= 0; y--)
		{
			if(map.charCodeAt(x * this.spread.height + y) != 65)
				this.place(x, y, map.charCodeAt(x * this.spread.height + y) - 65, true);
		}
	}
}

FourWins.prototype.destroy = function()
{
	this.canvas.remove();
	clearInterval(this.interval);
}

/*
 *	Called when the user moved the mouse over the GameArea
 */
FourWins.prototype.onMouseMove = function(event)
{
	var x = event.clientX / this.tileDim.width; 
	var y = event.clientY / this.tileDim.height;
	this.selected.x = Math.floor(x);
	this.selected.y = Math.floor(y);
}

/*
 *	Called when the user clicked somewhere on the GameArea
 */
FourWins.prototype.onMouseDown = function(event)
{
	var x = event.clientX / this.tileDim.width; 
	var y = event.clientY / this.tileDim.height;
	y = this.lowestY(Math.floor(x));
	//this.p++; //OBSOLETE! FOR TESTINGPURPOSES ONLY
	//this.place(, this.p % 4 + 1); //OBSOLETE! FOR TESTINGPURPOSES ONLY
	this.gameO.place(Math.floor(x), y);
}

FourWins.prototype.place = function(x, y, player, f)
{
	this.array[x][y] = player;
	this.symbols.push(new Symbol({x : x, y : y}, player, this.tileDim, this.ctx, f));
}

/*
 *	Initializes local variables such as width, height, etc...
 */
FourWins.prototype.init = function(width, height)
{
	this.selected = {
		x : -1,
		y : -1
	};
	this.dim = { //Width of the Game
		width : this.canvas[0].width,
		height : this.canvas[0].height
	};
	this.spread = { //Amount of tiles in x and y direction
		width: width,
		height: height
	};
	this.tileDim = { //Size of one tile
		width: (this.dim.width / this.spread.width),
		height: (this.dim.height / this.spread.height),
	};
	this.color = {
		gridStrokeReally : "#FFF043",
		gridFillReally : "#2CB200",
		gridStrokeSelected : "#FFF043",
		gridFillSelected : "#FFC13C",
		gridStrokeNormal : "#FFF043",
		gridFillNormal : "#FDFFAB"
	};
	this.symbols = new Array(); //Initialize symbols
	this.array = new Array(); //Initialize Game Array
	for(var x = 0; x < this.spread.width; x++)
	{
		this.array[x] = new Array();
		for(var y = 0; y < this.spread.height; y++)
		{
			this.array[x][y] = 0;
		}
	}
}

/*
 *	Will give you the lowest y coordinate to this x coordinate
 */
FourWins.prototype.lowestY = function(x)
{
	for(var y = 0; y < this.spread.height; y++)
	{
		if(this.array[x][y] != 0) break;
	}
	return y - 1;
}

/*
 *	Will redraw the whole GameArea
 */
FourWins.prototype.redraw = function()
{
	this.ctx.clearRect(0, 0, this.dim.width, this.dim.height); //Clear whole image
	this.redrawGrid();
	this.redrawSymbols();
	this.redrawWin();
};

FourWins.prototype.redrawSymbols = function()
{
	var arr = new Array();
	while(this.symbols.length != 0)
	{
		var s = this.symbols.pop();
		s.redraw();
		arr.push(s);
	}
	this.symbols = arr;
}

/*
 *	Will redraw the grid
 */
FourWins.prototype.redrawGrid = function()
{
	for(var y = 0; y < this.spread.height; y++)
	{
		for(var x = 0; x < this.spread.width; x++)
		{
			this.drawTile(x, y);
		}
	}
};

/*
 *	Will draw a single tile
 */
FourWins.prototype.drawTile = function(x, y)
{
	this.ctx.lineWidth = 1;
	var high = false, reallyHigh = false;
	if(x == this.selected.x)
		high = true;
	if(x == this.selected.x && y == this.lowestY(x))
		reallyHigh = true;
	x *= this.tileDim.width;
	y *= this.tileDim.height;
	if(reallyHigh) 
	{
		this.ctx.strokeStyle = this.color.gridStrokeReally;
		this.ctx.fillStyle = this.color.gridFillReally;
	}
	else if(high)
	{
		this.ctx.strokeStyle = this.color.gridStrokeSelected;
		this.ctx.fillStyle = this.color.gridFillSelected;
	}
	else
	{
		this.ctx.strokeStyle = this.color.gridStrokeNormal;
		this.ctx.fillStyle = this.color.gridFillNormal;
	}
	
	this.ctx.beginPath();
	this.ctx.rect(x, y, this.tileDim.width, this.tileDim.height);
	this.ctx.fill();
	this.ctx.stroke();
	
};