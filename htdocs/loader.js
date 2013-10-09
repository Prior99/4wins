function Loader(width, height)
{
	this.width = width;
	this.height = height;
	this.mX = width/2;
	this.mY = height/2;
	this.canvas = $('<canvas width="' + width + '" height="' + height + '"></canvas>') 
	this.ctx = this.canvas[0].getContext("2d");
	this.ctx.lineWidth=2;
	this.x = 0;
	this.started = new Date().getTime();
}

Loader.prototype.start = function()
{	
	var self = this;
	this.timing = setInterval(function()
	{
		self.redraw();
	}, 30);
}


Loader.prototype.redraw = function()
{
	this.ctx.clearRect(0, 0, this.width, this.height);
	this.x = (this.x + 0.02) % 1;
	var delta = 0.05;
	for(var y = 0, a = 1; y < 1; y += 0.02)
	{	
		this.ctx.strokeStyle= "rgba(200, 200, 200, 1)";
		this.drawRadiantLine(this.mX, this.mY, this.height / 20 * 7, this.height / 2, y);
	}
	for(var y = 0, a = 1; y < 0.5; y += 0.02)
	{	
		a -= delta;
		this.ctx.strokeStyle= "rgba(0, 0, 255," + a + ")";
		this.drawRadiantLine(this.mX, this.mY, this.height / 20 * 7, this.height / 2, y - this.x);
	}
	var timeDiff = (new Date().getTime() - this.started) / 1000;
	
	var txt = timeDiff.toFixed(1) + "s";
	var fSize = Math.floor(this.width/ (txt.length * 1.2));
	this.ctx.fillStyle="#00F";
	this.ctx.font= fSize+"px Arial";
	this.ctx.textAlign = 'center';
	this.ctx.fillText(txt, this.width / 2 , this.height/2 + fSize/3);
}

Loader.prototype.stop = function()
{
	clearInterval(this.timing);
	this.canvas.remove();
}

Loader.prototype.appendTo = function(element)
{
	this.canvas.appendTo(element);
}

Loader.prototype.drawRadiantLine = function(middleX, middleY, innerRadius, outerRadius, percent)
{
	var alpha = percent * Math.PI * 2;
	var alphaSin = Math.sin(alpha);
	var alphaCos = Math.cos(alpha);
	this.ctx.beginPath();
	this.ctx.moveTo(middleX + innerRadius * alphaSin, middleY - innerRadius * alphaCos);
	this.ctx.lineTo(middleX + outerRadius * alphaSin, middleY - outerRadius * alphaCos);
	this.ctx.stroke();
}