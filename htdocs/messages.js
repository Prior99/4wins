var _waitLoader, _waitDiv, _waitDark;
var fail = false;
var ok = true;

function wait(msg)
{
	_waitDark =  $("<div class='dark'></div>").appendTo("body").hide().fadeIn(200);
	_waitDiv = $("<div class='wait'></div>").appendTo(_waitDark).hide().fadeIn(300).append(msg).append("<br>");
	_waitLoader = new Loader(200, 200);
	_waitLoader.appendTo(_waitDiv);
	_waitLoader.start();
}

function unwait()
{
	_waitDark.remove();
	_waitLoader.stop();
	_waitDiv.remove();
}

function message(type, head, text, func)
{
	var dark =  $("<div class='dark'></div>").appendTo("body").hide().fadeIn(200);
	var msg =  $("<div class='msg'></div>").appendTo(dark).fadeIn(300);
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
		dark.remove();
		func();
	});
}