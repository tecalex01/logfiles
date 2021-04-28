package com.logfiles.entrypoint;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import com.logfiles.api.Host;
import com.logfiles.api.LogFile;
import com.logfiles.backend.FilterKeyword;
import com.logfiles.backend.LogFileDirectory;
import com.logfiles.backend.LogFileReader;
import com.logfiles.backend.ReadOrder;

/**
 * Class that is the entry point to handle REST-API request over
 * 1. http://[domain]/logfiles/api/v1/files/
 * 2. http://[domain]/logfiles/api/v1/files/{filename}
 * @author alexdel
 *
 */
@Path("logfiles/api/v1/files")
@Produces(MediaType.APPLICATION_JSON)
public class LogFileResource {
	/** Default log directory */	
	private String   defaultDir;
	/** File cursor position since the reading will start */
	private Long     startPos;
	/** Number of lines to be read */
	private Integer  nLines;
	/** Filter keyword to be apply over the lines selected */
	private String   keyword;
	/** File read on ascendant or descendant way */
	private Integer  orderBy;
	/** Hosts list separate by commas to be reached by a REST-API request
	 *  to query a single file or all files in its local log directory */
	private String   hosts;
	/** Controller reference to backend for a single file */
	LogFileReader    logFileReader;
	/** Controller reference to backend for a directory */
	LogFileDirectory logFileDirectory;
	/** Jersey client to makes REST-API requests */
	Client           jerseyClient;

	/**
	 * Constructor
	 * @param jerseyClient Jersey client 
	 * @param defaultDir   Default log directory
	 * @param startPos     Files read from cursor position
	 * @param nLines       Number of lines to be read
	 * @param orderBy      File read on ascendant(0) or descendant way(1). Default descendant
	 * @param hosts        Host list separated by commas to be query.
	 */
	public LogFileResource(Client jerseyClient,
						   String defaultDir, 
			               Long startPos,
			               Integer nLines, 
			               Integer orderBy,
			               String hosts) {
		this.jerseyClient = jerseyClient;
		this.defaultDir = defaultDir;
		this.startPos   = startPos;
		this.nLines     = nLines;
		this.orderBy    = orderBy;
		this.hosts      = hosts;
		this.logFileReader    = new LogFileReader();
		this.logFileDirectory = new LogFileDirectory(logFileReader);
	}

	/**
	 * Entry point for http://[domain]/logfiles/api/v1/files/ 
	 * Allows the following usage:
	 * 1. Header host parameter to specify another host to be query.
	 * 2. http://[domain]/logfiles/api/v1/files/
	 * 3. http://[domain]/logfiles/api/v1/files[?n_lines={#lines}[{@literal &}keyword={keyword}[{@literal &}orderBy={0|1}]]]
	 * @param nLines    Number of lines to be read
	 * @param keyword   keyword filter over the lines read.
	 * @param orderBy   Reading on ascendant (0) and descendant(1)
	 * @param hosts     Host list separate by comma to be query by REST-API request
	 * @return
	 */
	@GET
	@Timed
	public List<Host> getAllLogFiles(@QueryParam("n_lines") Optional<Integer> nLines,
										@QueryParam("keyword") Optional<String>  keyword,
										@QueryParam("order_by") Optional<Integer> orderBy,
									    @HeaderParam("X-hosts") Optional<String> hosts) {
		this.nLines = nLines.or(-1);   /* By default number of lines not specified */
		this.keyword = keyword.or(""); /* By default no keyword specified */
		this.orderBy = orderBy.or(1);  /* By default desc ordering */
		this.startPos = -1L;
		this.hosts   = hosts.or("localhost");   /* By default no hosts specified */
		
		validParameters();
		
		String[]     hostsArr  = this.hosts.split(",");
		List<String> hostsList = Arrays.asList(hostsArr);
		String       myIpAux;
		final String myIp;
		List<LogFile> myLocalFiles = null;
		List<Host> logFilesAllServers = new LinkedList<>();
		
		/* Get current host ip */
		try {
			InetAddress ip = InetAddress.getLocalHost();
			myIpAux = ip.getHostAddress();
		} catch (UnknownHostException ue) {
			myIpAux = "localhost";
		}
		
		myIp = myIpAux;
		
		/* If current host is specified in host header param,
		 * then look for all files in current machine
		 */
		if (hostsList.contains("localhost") || 
			hostsList.contains("127.0.0.1") ||
			hostsList.contains(myIp))
		{
			myLocalFiles = getAllFilesInDirectory(logFileDirectory, new File(defaultDir));
		}
		
		/* Execute in parallel way a filter to remove current host from host list */
		hostsList = hostsList.parallelStream().filter(str -> {return !str.equals("localhost") &&
                                                             !str.equals("127.0.0.1") &&
                                                             !str.equals(myIp);})
				                      .collect(Collectors.toList());
		
		/* If after remove current host, there are more host to query */
		if (hostsList.size() > 0)
		{
     		/* Execute on a parallel way a REST-API call for each host specified in hosts header param.
     		 * The function requestFileToOtherServers handle and parse the response and create a representation for 
     		 * each host with the files queried in case of status code 200.
     		 * 
     		 * NOTE: For remote request only params: n_lines, order_by and keyword are enable, 
     		 * start_pos is unable */
			logFilesAllServers = hostsList.parallelStream().map(host -> requestFileToOtherServers(host, ""))
	                                                	   .collect(Collectors.toList()); 
		}
		
		/* If local files were queried */
		if(myLocalFiles != null)
		{
			/* Set a representation for current host */
			Host host = new Host();			
			host.setHost(myIp);
			host.setCode(Status.OK.getStatusCode());
			host.setMessage(Status.OK.getReasonPhrase());			
			host.setLogfiles(myLocalFiles);
			/* Add host to the response */
			logFilesAllServers.add(host);
		}
		
		return logFilesAllServers;
	}

	/**
	 * Entry point for http://[domain]/logfiles/api/v1/files/{filename}
	 * Allows the following usage:
	 * 1. Header host parameter to specify another host to be query.
	 * 2. http://[domain]/logfiles/api/v1/files/{filename}
	 * 3. http://[domain]/logfiles/api/v1/files[?n_lines={#lines}[{@literal &}keyword={keyword}[{@literal &}orderBy={0|1}[{@literal &}start_pos={long_number}]]]]
	 * @param fileName   Filename looked
	 * @param startPos   File cursor reference since the reading will start
	 * @param nLines     Number of lines to be read
	 * @param keyword    Keyword filter to filter lines read.
	 * @param orderBy    Reading on ascendant(0) or descendant(1)
	 * @param hosts      Hosts list header parameter separated by comma to look the file through a REST-API request
	 * @return
	 */
	@GET	
	@Path("/{fileName}")
	@Timed
	public List<Host> getLogFile(@NotNull @PathParam("fileName") String      fileName,
							   @QueryParam("start_pos") Optional<Long>    startPos,
			                   @QueryParam("n_lines") Optional<Integer>   nLines,
			                   @QueryParam("keyword")  Optional<String>   keyword,
			                   @QueryParam("order_by") Optional<Integer>  orderBy,
			                   @HeaderParam("X-hosts")   Optional<String> hosts)
	{
		this.nLines   = nLines.or(-1);  /* By default number of lines not specified */
		this.keyword  = keyword.or(""); /* By default no keyword specified */
		this.orderBy  = orderBy.or(1);  /* By default desc ordering */
		this.startPos = startPos.or(-1L); /* By default no startPos specified */
		this.hosts    = hosts.or("localhost");   /* By default no hosts specified */
		
		validParameters();
						
		String[]     hostsArr  = this.hosts.split(",");
		List<String> hostsList = Arrays.asList(hostsArr);
		File         file      = new File(defaultDir + fileName);
		LogFile      myLogFile = null;
		String       myIpAux;
		final String myIp;
		List<Host> logFilesAllServers = new LinkedList<>();
				
		/* Get current host ip */
		try {
			InetAddress ip = InetAddress.getLocalHost();
			myIpAux = ip.getHostAddress();
		} catch (UnknownHostException ue) {
			myIpAux = "localhost";
		}
		
		myIp = myIpAux;
		
		/* If current host is specified in host header param,
		 * then look for all files in current machine
		 */
		if (hostsList.contains("localhost") || 
			hostsList.contains("127.0.0.1") ||
			hostsList.contains(myIp))
		{
			myLogFile = getFile(logFileReader, file);
		}
		
		/* Execute in parallel way a filter to remove current host from host list */
		hostsList = hostsList.parallelStream().filter(str -> {return !str.equals("localhost") &&
				                                !str.equals("127.0.0.1")  &&
				                                !str.equals(myIp);})
		                              .collect(Collectors.toList());

		/* If after remove current host, there are more host to query */
		if (hostsList.size() > 0)
		{
     		/* Execute on a parallel way a REST-API call for each host specified in hosts header param.
     		 * The function requestFileToOtherServers handle and parse the response and create a representation for 
     		 * each host with the files queried in case of status code 200.
     		 * 
     		 * NOTE: For remote request only params: n_lines, order_by and keyword are enable, 
     		 * start_pos is unable */
			logFilesAllServers = hostsList.parallelStream().map(host -> requestFileToOtherServers(host, fileName))
			.collect(Collectors.toList()); 
		}
		
		/* If local file were queried */
		if(myLogFile != null)
		{
			/* Set a representation for current host */
			Host host = new Host();
			List<LogFile> logFileList = new LinkedList<>();
			host.setHost(myIp);
			host.setCode(Status.OK.getStatusCode());
			host.setMessage(Status.OK.getReasonPhrase());
			logFileList.add(myLogFile);
			host.setLogfiles(logFileList);
			/* Add host to the response */
			logFilesAllServers.add(host);
		}
		
		return logFilesAllServers;		
	}
	
	/**
	 * Look for all the files in log directory for current host.
	 * @param logFileDir   Log file directory controller backend reference
	 * @param path         Log file directory
	 * @return
	 */
	private List<LogFile> getAllFilesInDirectory(LogFileDirectory logFileDir, File path)
	{
		ReadOrder order = ReadOrder.DESC;
		FilterKeyword filterKeyword = new FilterKeyword(keyword);
		List<LogFile> logFileList;
		
		if (orderBy == 0)
		{
			order = ReadOrder.ASC;
		}
		
		/* If number of lines specified */
		if (nLines > 0)
		{
			/* Filter all files by number of lines, ordering and keyword if were specified */
			logFileList = logFileDir.getAllFiles(path, nLines, order, filterKeyword);
		}
		else
		{
			/* Filter all files by order (default descendant) and keyword if were specified */
			logFileList = logFileDir.getAllFiles(path, order, filterKeyword);
		}
		
		return logFileList;
	}
	
	/**
	 * Look for a specific file in current host.
	 * @param logFileReader   Log file controller backend reference
	 * @param file            File looked in log directory
	 * @return
	 */
	private LogFile getFile(LogFileReader logFileReader, File file) {
		LogFile logFile = null;
		ReadOrder order = ReadOrder.DESC;
		String msg = "";
		FilterKeyword filterKeyword = new FilterKeyword(keyword);
		
		/* Processing query params and header params */
		if (orderBy == 0) {
			order = ReadOrder.ASC;
		} 

		try {
			if (startPos >= 0) {
				logFile = logFileReader.readLines(file, startPos, nLines, order, filterKeyword);
			}
			else if (nLines > 0) {
				logFile = logFileReader.readLines(file, nLines, order, filterKeyword);
			} else {
				logFile = logFileReader.readLines(file, order, filterKeyword);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
			if (!Files.isReadable((Paths.get(defaultDir))))
			{
				msg = "Path " + defaultDir + " permission denied";
				throw new WebApplicationException(msg, Status.FORBIDDEN);
			}
			else
			{
				msg = "File " + defaultDir + file.getName() + " not found";
				throw new WebApplicationException(msg, Status.NOT_FOUND);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}

		return logFile;
	}
	
	/**
	 * Handles REST-API request for every host name
	 * @param hostName   host name 
	 * @param fileName   file looked if apply
	 * @return
	 */
	private Host requestFileToOtherServers(String  hostName, 
			                               String  fileName)
	{
		String  url  = "http://" + hostName + "/";					
		Host    host = new Host();
		
		try
		{
			/* Target http server and set query params */
			WebTarget target = jerseyClient.target(url).path("logfiles/api/v1/files")
                                         			   .queryParam("n_lines", this.nLines)
                                                       .queryParam("keyword", this.keyword)
                                                       .queryParam("order_by", this.orderBy);
			
			/* If an specific file were specified */
			if (!fileName.equals(""))
			{
				target.path("/"+fileName);
			}
			
			/* Execute REST-API request for host */		
			Invocation.Builder invBuilder = target.request(MediaType.APPLICATION_JSON);
			Response response = invBuilder.get(Response.class);
			
			/* Handle and manage REST-API response */
			List<Host> hostsWithLogFile = response.readEntity(List.class);
			
			/* We are expecting a single response with the file or error reporting */
			host = hostsWithLogFile.get(0);			
		} catch(ProcessingException e)
		{
			/* In case the host can not be reachable */
			host.setHost(hostName);
			host.setCode(Status.GATEWAY_TIMEOUT.getStatusCode());
			host.setMessage(Status.GATEWAY_TIMEOUT.getReasonPhrase());
			host.setLogfiles(new LinkedList<>());
			System.out.print(e.getMessage());
		}
		
		return host;
	}

	/** 
	 * Validate REST-API parameters 
	 */
	private void validParameters() {
		boolean valid = true;
		String msg = "";

		if (startPos < 0 && startPos != -1)
		{
			msg   = "start_pos must be as minimum 0";
			valid = false;
		}
		if (nLines <= 0 && nLines != -1) {
			msg   = "n_lines must be as minimum 1";
			valid = false;
		}
		if (orderBy != 0 && orderBy != 1) {
			msg   = "order_by allowed values are 0 to ASC and 1 to DESC";
			valid = false;
		}

		if (!valid) {
			throw new WebApplicationException(msg, Status.BAD_REQUEST);
		}
	}
}
