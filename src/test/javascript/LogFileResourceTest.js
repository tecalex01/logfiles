/* Declare a suite of javascript test.
 * This suite basically tries to see if the API is getting the right response or error, 
 * and if it is returning the right request number of lines
 */
describe("LogFile REST-API test suite", function() {
	/* This success if the file CBS.log exists on the log directory */
	it("Read specific file succesfully", function() {
		console.log("Reading specific file with success");
		getRequest("CBS.log", response200);
	});
	/* This success if the file NotFound.log does not exist on the log directory */ 
	it("Read specific file unsuccesfully", function() {
		console.log("Reading specific file with unsuccess");
		getRequest("NotFound.log", response404);
	});
	/* This success if the file CBS.log exists on the log directory and the
	 * return lines are equal to 5
	 */
	it("Read nlines for specific file", function() {
		console.log("Reading 5 lines for specific file");
		getRequest("CBS.log", 5, countLines)
	});
});

/* Makes async http request */
function getRequest(file, callbackFunc)
{
	getRequest(file, -1, callbackFunc);
}

/* Makes async http request and process result on callbackFunc function */
function getRequest(file, nlines, callbackFunc)
{
	var httpreq = new XMLHttpRequest();		
	httpreq.open("GET", "http://localhost:8080/logfiles/api/v1/files/".concat(file, "?n_lines=",nlines), true);
	httpreq.setRequestHeader("Content-Type", "application/json");
	httpreq.onreadystatechange = function() {
		if (httpreq.readyState == 4)
		{
			if (nlines == -1)
			{
				callbackFunc(httpreq);
			}
			else
			{
				callbackFunc(httpreq, nlines)
			}
		}
	};
	httpreq.send();
}

/*
 * Process an http response to be 200
 * @param httpreq XMLHttpRequest 
 */
function response200(httpreq)
{
	if (httpreq.readyState == 4)
	{
		expect(httpreq.status).toBe(200);
	}
}

/*
 * Process an http response to be 404
 * @param httpreq XMLHttpRequest 
 */
function response404(httpreq)
{
	if (httpreq.readyState == 4)
	{
		expect(httpreq.status).toBe(404);
	}
}


/*
 * Process and http response as JSON and parse to be if the number
 * of lines is what we are expecting.
 * @param httpreq XMLHttpRequest 
 * @param nlines  Number of expected lines in response.
 */
function countLines(httpreq, nlines)
{
	if (httpreq.readyState === 4 && httpreq.status == 200)
	{
		var resp = httpreq.response;
		var obj  = JSON.parse(resp);
		var fileBuf = JSON.parse(obj.fileBuffered);
		expect(fileBuf.lines.length).tobe(nlines);
	}
	else
	{
		expected(true).toBe(false);
	}
	
}