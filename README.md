# logfiles
This is a REST-API to read log files
It is under development.

Currently, it supports two entry points.
1. http://domain/logfiles/api/v1/files/{filename}
2. http://domain/logfiles/apiv1/files

In both entries it support following query and header params:
```
 - n_lines={integer}  @QueryParam  : Number of lines to be read on a file
 - keyword={string}   @QueryParam  : Keyword to be looked in the filtering lines
 - order_by={0|1}     @QueryParam  : Ordering, 0 - Ascendant, 1 - Descendant
 - hosts={string}     @HeaderParam : List<Host> where the files (file) will be looked.
```
 
For http://domain/logfiles/api/v1/files/{filename} it supports one more query param that it only works in the main server.
```
  - start_pos={long} : Specify on which file position the reading will start.
```
  
The parameters can be used alternative, all of them or some of them.

For both entries, the REST-API respond with a JSON which represent a host with following information:

```yaml
 Host
 {
    host: string,                       /* Host name */
    code: http_response_code (int),     /* HTTP Response code : 200, 403 , 404, 500, 504 */
    message: http_message (string),     /* HTTP message */
    logfiles: List<LogFile>             /* List of log files on host */
 }
 
 LogFile
 {
 	host: string,                       /* Host name */
 	path: string,                       /* File path */
 	size: int,                          /* File size */
 	fileBuffered: FileBuffered          /* File Buffered */
 }
 
 FileBuffered
 {
 	lastPosRead: long,                  /* This helps to know which is the last byte read in the file */
 	lines: List<String>                 /* Lines read from the file */ 
 }
```
``` 
 This are the expected errors:
 200 - OK
 403 - Forbidden
 404 - Not found
 500 - Internal Server Error
 504 - Gateway Timeout
```

 
 Some examples:
 1. http://domain/logfiles/api/v1/files/{filename}
  If success:
    Return host resource with a log file resource with all its lines in descendant order. From the end to start.
 2. http://domain/logfiles/api/v1/files/{filename}?n_lines=#lines
  If success:
    Return host resource with a log file resource with #lines of its lines in descendant order. From the end to start.
 3. http://domain/logfiles/api/v1/files/{filename}?keyword=keyword
  If success:
    Return host resource with a log file resource with lines that contains the keyword in descendant order. From the end to start.
 4. http://domain/logfiles/api/v1/files/{filename}?order_by=0
  If success:
    Return host resource with a log file resource with lines in ascendant order. From start to end.
 5. http://domain/logfiles/api/v1/files/{filename}?start_pos=1000
 If success:
    Return host resource with a log file resource with lines in descendant order, from start to end. Starting the reading from the byte 1000 on the file.
 6. http://domain/logfiles/api/v1/files/{filename}?n_lines=#lines&keyword=keyword
 If success:
    Return host resource with a log file resource with #lines and for those #lines filter, apply the keyword filter to see how lines contains the keyword. The lines are read in descendant order. From end to start.
 7. http://domain/logfiles/api/v1/files/{filename}?n_lines=#lines&keyword=keyword&order_by=0
 If success:
    Return host resource with a log file resource with #lines and for those #lines filter, apply the keyword filter to see how lines contains the keyword. The lines are read in ascendant order. From start to end.
 8. http://domain/logfiles/api/v1/files/{filename}?keyword=keyword&order_by=0
 If success:
    Return host resource with a log file resource with all lines on the file and for those lines apply the keyword filter to see how lines contains the keyword. The lines are read in ascendant order. From start to end.     
 9. http://domain/logfiles/api/v1/files/{filename}?n_lines=#lines&order_by=0
 If success:
 	Return host resource with a log file resource with #lines from the file read in ascendant order from start to end.
 10. http://domain/logfiles/api/v1/files/{filename}?n_lines=#lines&order_by=0&start_pos=1000
 If success:
 	Return host resource with a log file resource with #lines from the file read in ascendant order from start_pos (1000 byte) to end.
 	NOTE: As you can see start_pos can allow you to move forward and backward on the lines. In the output in the FileBuffer resource there is the field lastPosRead than can help to make it possible.
 11. Any previous request are allowed if hosts are specified as a header parameter except points 5, 10. Which will have similar behaviors, but there is an internal request of the same API on the host specified and the responses are process to only return a list of Host resources, with the respective response of each host.
 
 12. http://domain/logfiles/apiv1/files
 If success:
 	Return host resource with a list of LogFile which represents all the files in the log directory. Each Logfile contains a LogFileBuffer resource which contains all the files on the file read in descendant order.
 13. http://domain/logfiles/apiv1/files?n_lines=#lines
 If success:
 	Return host resource with a list of LogFile which represents all the files in the log directory. Each Logfile contains a LogFileBuffer resource which contains #files of each file read in descendant order.
 14. http://domain/logfiles/apiv1/files?n_lines=#lines&order_by=0
 Return host resource with a list of LogFile which represents all the files in the log directory. Each Logfile contains a LogFileBuffer resource which contains #files of each file read in asscendant order.
 15. http://domain/logfiles/apiv1/files?n_lines=#lines&keyword=keyword
 Return host resource with a list of LogFile which represents all the files in the log directory. Each Logfile contains a LogFileBuffer resource which contains a filtering lines with keyword on the #line previous selected of each file read in descendant order.
 
 There are any other combinations of parameters you can use then together in any way.
 
##Tests (src/test)
 This code has some test. 
 java/com/logfile/backend/test/LogFileTest.java : Unit test for backend infrastructure
 javascript/LogFileResourceTest.js              : Javascript functional test to test REST-API.
 
## Documentation
 The documentation files are in docs directory.
 - docs/apidocs               : Java docs
 - docs/class_diagrams        : POJOS, backend, resource
 - rest-api_spec/logfile.yaml : REST-API definition with swagger 2.0
 
## Dependencies:
 - DropWizard core: Library that contains JETTY http server, full-featured jersey RESTful web framework, Jackson JSON library.
 - DropWizard client: jersey-client
 - JUnit for testing
 - junit-addons For file assert Testing
 - Chai for javascript assert
 - jasmine testing framework for javascript.
  
## Optimizations:
  - Each REST-API request is done in parallel using threads for request the resource on each host for a particular file and for all the files.
  - In the backend there is a cached that is only use for files read in descendant order and always start from the end.
  
 
How to run code:
- mvn test        : Run JUnit tests.
- mvn jasmine:bdd : Run the javascript tests.
- mvn javadoc:javadoc : Generates javadoc for the project. Which is already on docs directory.
- mvn clean package: To create target/logfiles-0.0.1-SNAPSHOT.jar
- Execute the app: java -jar target/logfiles-0.0.1-SNAPSHOT.jar

  
Things to be done:
1. For the case on where a host is specified, then send a body param JSON to specify its param personalized for each host. 
2. Add more testing: 
3. Add more optimization in backend: 
  1. General cache file for all the cases.
  2. Improve cache on InputStreams to read the file.
  3. Security for REST-API maybe with oauth.
  4. Many others ...
 