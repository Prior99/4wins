/**
 ** Class FourWins
 **
 ** 2013 By Prior (Frederick Gnodtke)
 **
 **/
function FourWins(id, width, height)
{
	var self = this; //Forward to call this from within functions
	this.canvas = document.getElementById(id); //Store our canvas
	this.canvas.addEventListener("mousemove", function(event)
	{
		self.onMouseMove(event);
	});
	this.canvas.addEventListener("mousedown", function(event)
	{
		self.onMouseDown(event);
	});
	this.ctx = this.canvas.getContext("2d"); //Store our context
	this.init(width, height); //initialize private variables
	setInterval(function() {
		self.redraw();
	}, 30);
	this.p = 0; //OBSOLETE! FOR TESTINGPURPOSES ONLY
}

/*
 *	Called when the user moved the mouse over the GameArea
 */
FourWins.prototype.onMouseMove = function(event)
{
	var x = event.offsetX / this.tileDim.width; 
	var y = event.offsetY / this.tileDim.height;
	this.selected.x = Math.floor(x);
	this.selected.y = Math.floor(y);
}

/*
 *	Called when the user clicked somewhere on the GameArea
 */
FourWins.prototype.onMouseDown = function(event)
{
	var x = event.offsetX / this.tileDim.width; 
	var y = event.offsetY / this.tileDim.height;
	y = this.lowestY(Math.floor(x));
	this.p++; //OBSOLETE! FOR TESTINGPURPOSES ONLY
	this.place(Math.floor(x), y, this.p % 4 + 1); //OBSOLETE! FOR TESTINGPURPOSES ONLY
	console.log({x: Math.floor(x), y: y, arr: this.array, p: this.p % 4 + 1});
}

FourWins.prototype.place = function(x, y, player)
{
	this.array[x][y] = player;
	this.symbols.push(new Symbol({x : x, y : y}, player, this.tileDim, this.ctx));
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
		width : this.canvas.width,
		height : this.canvas.height
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