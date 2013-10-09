var _waitLoader, _waitDiv;
var fail = false;
var ok = true;

function wait()
{
	_waitDiv = $("<div class='wait'></div>").appendTo("body");
	_waitLoader = new Loader(200, 200);
	_waitLoader.appendTo(_waitDiv);
	_waitLoader.start();
}

function unwait()
{
	_waitLoader.stop();
	_waitDiv.remove();
}

function message(type, head, text, func)
{
	var msg =  $("<div class='msg'></div>").appendTo("body");
	if(type)
		msg.addClass("ok");
	else
		msg.addClass("fail");
	$("<h1>" + head + "</h1>").appendTo(msg);
	$("<p>" + text + "</p>").appendTo(msg);
	var button = $("<button>ok</button>").appendTo(msg);
	button.click(function()
	{
		msg.remove();
		func();
	});
}