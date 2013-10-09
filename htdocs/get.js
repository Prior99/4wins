var urlVars;
var debug = getGet("debug") == "true";

function getUrlVars() 
{
	urlVars = {};
	window.location.href.replace("#", "").replace(/[?&]+([^=&]+)=([^&]*)/gi, function(m,key,value) 
	{
		urlVars[key] = value;
	});
	return urlVars;
}

function getGet(key) 
{
	if(urlVars == null) getUrlVars();
	return urlVars[key];
}