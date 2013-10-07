function Symbol(pos, player, dim, ctx)
{
	pos.x *= dim.width;
	pos.y *= dim.height;
	this.pos = pos;
	this.realPos = {
		x : pos.x,
		y : -dim.height
	};
	this.player = player;
	this.dim = dim;
	this.ctx = ctx;
	     if(player == 1)
	{
		this.color = "#4353FF";
		this.color2 = "#0015FF";
	}
	else if(player == 2)
	{
		this.color = "#A424FF";
		this.color2 = "#6200A9";
	}
	else if(player == 3)
	{
		this.color = "#FF2525";
		this.color2 = "#B30000";
	}
	else if(player == 4)
	{
		this.color = "#0BF254";
		this.color2 = "#00892B";
	}
}

Symbol.prototype.redraw = function()
{
	if(this.realPos.y < this.pos.y) this.realPos.y += 40;
	this.drawCircleSymbol();
}

Symbol.prototype.drawBlockSymbol = function()
{
	this.ctx.lineWidth = 2;
	this.ctx.fillStyle = this.color;
	this.ctx.strokeStyle = this.color2;
	this.ctx.beginPath();
	this.ctx.rect(this.realPos.x + 2, this.realPos.y + 2, this.dim.width - 4, this.dim.height - 4);
	this.ctx.fill();
	this.ctx.stroke();
}

Symbol.prototype.drawCircleSymbol = function()
{
	this.ctx.lineWidth = 2;
	this.ctx.fillStyle = this.color;
	this.ctx.strokeStyle = this.color2;
	this.ctx.beginPath();
	this.ctx.arc(this.realPos.x + this.dim.width / 2, this.realPos.y + this.dim.height / 2, this.dim.width / 2 - 4, 0, Math.PI * 2);
	this.ctx.fill();
	this.ctx.stroke();
}

Symbol.prototype.drawCrossSymbol = function()
{
	this.ctx.strokeStyle = this.color;
	this.ctx.lineWidth = 5;
	this.ctx.beginPath();
	this.ctx.moveTo(this.realPos.x + 2, this.realPos.y + 2);
	this.ctx.lineTo(this.realPos.x + this.dim.width - 2, this.realPos.y + this.dim.height - 2);
	this.ctx.stroke();
	this.ctx.beginPath();
	this.ctx.moveTo(this.realPos.x + 2, this.realPos.y - 2 + this.dim.height);
	this.ctx.lineTo(this.realPos.x + this.dim.width - 2, this.realPos.y + 2);
	this.ctx.stroke();
}