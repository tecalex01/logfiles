package com.logfiles.entrypoint;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import com.logfiles.api.LogFile;
import com.logfiles.api.LogFileBuffer;
import com.logfiles.backend.FilterKeyword;
import com.logfiles.backend.LogFileReader;
import com.logfiles.backend.ReadOrder;

@Path("logfiles/api/v1/files/")
@Produces(MediaType.APPLICATION_JSON)
public class LogFileResource {
	private String defaultDir;
	private Long   startPos;
	private Integer nLines;
	private String keyword;
	private Integer orderBy;
	private String hosts;

	public LogFileResource(String defaultDir, 
			               Long startPos,
			               Integer nLines, 
			               Integer orderBy,
			               String hosts) {
		this.defaultDir = defaultDir;
		this.startPos   = startPos;
		this.nLines     = nLines;
		this.orderBy    = orderBy;
		this.hosts      = hosts;
	}

	@GET
	@Timed
	public List<LogFile> getAllLogFiles(
			@HeaderParam("X-hosts") Optional<String> hosts) {
		List<LogFile> logFileBList = new LinkedList<LogFile>();
		return logFileBList;
	}

	@GET	
	@Path("/{fileName}")
	@Timed
	public LogFile getLogFile(@NotNull @PathParam("fileName") String fileName,
							   @QueryParam("start_pos") Optional<Long> startPos,
			                   @QueryParam("n_lines") Optional<Integer> nLines,
			                   @QueryParam("keyword")  Optional<String> keyword,
			                   @QueryParam("order_by") Optional<Integer> orderBy,
			                   @HeaderParam("X-hosts")   Optional<String> hosts)
	{
		LogFileReader logFileReader = new LogFileReader(new File(defaultDir + fileName));
		this.startPos = startPos.or(this.startPos);
		this.nLines   = nLines.or(this.nLines);
		this.keyword  = keyword.or("");
		this.orderBy  = orderBy.or(this.orderBy);
		this.hosts    = hosts.or(this.hosts);
		
		validParameters();
		LogFile       logFile       = getFileDetails(logFileReader);
		LogFileBuffer logFileBuffer = getFileBuffered(logFileReader, fileName);
		
		logFile.setFileBuffered(logFileBuffer);
		return logFile;		
		/*List<String> linesEx = new LinkedList<>();
		linesEx.add("Hola1");
		linesEx.add("Hola2");
		LogFileBuffer fileBuffered = new LogFileBuffer(linesEx, 0);
		LogFile logFileB = new LogFile("localhost", "/var/log/", 30);
		logFileB.setFileBuffered(fileBuffered);
		return logFileB;*/		
	}

	private LogFile getFileDetails(LogFileReader logFileReader) {		
		return logFileReader.getFileDetails();		
	}

	private LogFileBuffer getFileBuffered(LogFileReader logFileReader, String fileName) {
		LogFileBuffer logFileBuffer = null;
		ReadOrder order = ReadOrder.DESC;
		String msg = "";
		FilterKeyword filterKeyword = new FilterKeyword(keyword);
		
		/* Processing query params and header params */
		if (orderBy == 0) {
			order = ReadOrder.ASC;
		} 

		try {
			if (startPos >= 0) {
				logFileBuffer = logFileReader.readLines(startPos, nLines, order, filterKeyword);
			}
			else if (nLines > 0) {
				logFileBuffer = logFileReader.readLines(nLines, order, filterKeyword);
			} else {
				logFileBuffer = logFileReader.readLines(order, filterKeyword);
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
				msg = "File " + defaultDir + fileName + " not found";
				throw new WebApplicationException(msg, Status.NOT_FOUND);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}

		return logFileBuffer;
	}

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
